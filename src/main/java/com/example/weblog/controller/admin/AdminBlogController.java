package com.example.weblog.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.weblog.dto.BlogDTO;
import com.example.weblog.dto.UserDTO;
import com.example.weblog.entity.Blog;
import com.example.weblog.entity.Category;
import com.example.weblog.entity.Tag;
import com.example.weblog.service.IBlogService;
import com.example.weblog.service.ICategoryService;
import com.example.weblog.service.ITagService;
import com.example.weblog.util.MyBlogUtils;
import com.example.weblog.util.PageResult;
import com.example.weblog.util.SystemConstants;
import com.example.weblog.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/blog")
@Slf4j
public class AdminBlogController {
    @Resource
    private IBlogService blogService;
    @Resource
    private ICategoryService categoryService;
    @Resource
    private ITagService tagService;
    @GetMapping("/")
    public String toList(){
        return "admin/blog";
    }

    @GetMapping("/list")
    @ResponseBody
    public Result index(@RequestParam(value = "currentPage", required = false)Long currentPage,
                       @RequestParam(value = "pageSize",required = false)Long pageSize,
                       @RequestParam(value = "keyword",required = false)String keyword,
                       HttpSession session){
        UserDTO userDTO = (UserDTO)session.getAttribute(SystemConstants.LOGIN_USER);
        Integer userId = userDTO.getId();
        //获取当前登录用户的所有title符合keyword形式的blog
        IPage<Blog> page = new Page<>(currentPage, pageSize);
        if(keyword == null || keyword.length() <= 0){
            //如果keyword为空，那么查询第currentPage的blog，每页有pageSize条记录
            blogService.page(page, new LambdaQueryWrapper<Blog>().eq(Blog::getUserId, userId)
                                                                 .orderByDesc(Blog::getCreateTime));
        }else{
            blogService.page(page, new LambdaQueryWrapper<Blog>().eq(Blog::getUserId, userId)
                                                                .like(Blog::getTitle, keyword)
                                                                .orderByDesc(Blog::getCreateTime));
        }
        List<Blog> blogs = page.getRecords();
        //获取blog的所在分类
        blogs.forEach(blog -> {
            Integer categoryId = blog.getCategoryId();
            if(categoryId != null) {
                blog.setCategoryName(categoryService.getById(categoryId).getName());
            }
        });
        List<BlogDTO> blogDTOs = new ArrayList<>();
        blogs.forEach(blog -> {
            blogDTOs.add(BeanUtil.copyProperties(blog, BlogDTO.class));
        });
        PageResult<BlogDTO> pageResult = new PageResult<>();
        pageResult.setList(blogDTOs);
        pageResult.setTotal(page.getTotal());
        pageResult.setCurrentPage(currentPage);
        pageResult.setPages(page.getPages());
        return Result.success(pageResult);
    }

    //新增或者编辑blog时，前往edit.html界面
    @GetMapping({"/edit", "/edit/{id}"})
    public ModelAndView toEdit(@PathVariable(value = "id", required = false)Integer id,
                         ModelAndView modelAndView){
        //获取需要编辑的blog
        if(id != null){
            Blog blog = blogService.getOne(new LambdaQueryWrapper<Blog>().eq(Blog::getId, id));
            Integer categoryId = blog.getCategoryId();
            if(categoryId != null) {
                blog.setCategoryName(categoryService.getById(categoryId).getName());
            }
            //获取blog的所有标签的名字
            List<String> tags = tagService.getTagsByBlogId(id).stream().map(Tag::getName).collect(Collectors.toList());
            if(!tags.isEmpty()){
                StringBuilder stringBuilder = new StringBuilder();
                //因为在edit.html界面中，利用jquery.tagsinput.min.js来实现多标签
                //而对应的值就是一个以逗号分割的字符串基础上实现多标签的
                for(String tag : tags){
                    stringBuilder.append(tag).append(",");
                }
                stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                modelAndView.addObject("tags", stringBuilder.toString());
            }
            modelAndView.addObject("blog", blog);
        }
        //获取所有的分类
        List<Category> categories = categoryService.list();
        modelAndView.addObject("categories", categories);
        modelAndView.setViewName("/admin/edit.html");
        return modelAndView;
    }

    /*
     * 修改blog: 关键是要修改这篇blog的标签
     * 1.1 获取原来的标签,然后遍历旧的标签，如果旧的标签不在新的标签中，
     * 说明这个旧的标签被删除，那么需要删除tb_blog_tag表中的数据
     * 1.2 遍历新的标签，如果不在旧的标签中，说明这个是新增的，然后判断这个
     * 新的标签是否在tb_tag中，如果不在，那么需要将这个新的标签插入到tb_tag
     * 然后在让这个blog和tag插入到tb_blog_tag表中；否则如果新的标签已经存在
     * tb_tag中，只需要将这个blog和tag插入到tb_blog_tag中
     *
     * 然后在修改原来的blog的属性,然后将修改后的blog作为参数传入到updateById方法
     * 即可
     */
    @PostMapping("/update")
    @Transactional
    @ResponseBody
    public Result update(Integer id,
                         String title,
                         String tags,
                         String content,
                         String description,
                         Integer blogStatus,
                         Integer enableComment,
                         Integer categoryId){
        //将原来的旧的标签删除
        List<Tag> oldTags = tagService.getTagsByBlogId(id);
        List<String> newTagNames = Arrays.asList(tags.split(","));
        oldTags.forEach(tag -> {
            if(!newTagNames.contains(tag.getName())){
                tagService.removeByTagIdAndBlogId(tag.getId(), id);
            }
        });
        List<String> oldTagNames = oldTags.stream().map(Tag::getName).collect(Collectors.toList());
        //将新的标签插入
        newTagNames.forEach(newTagName -> {
            if(!oldTagNames.contains(newTagName)){
                //判断newTagName是否存在tb_tag中
                Tag tagDB = tagService.getOne(new LambdaQueryWrapper<Tag>().eq(Tag::getName, newTagName));
                if(tagDB == null){
                    //不存在,那么将tagDB插入到tb_tag中
                    tagDB = new Tag();
                    tagDB.setName(newTagName);
                    tagService.save(tagDB);
                }
                //将当前这篇blog和新的标签建立联系
                tagService.saveTagIdAndBlogId(tagDB.getId(), id);
            }
        });
        //修改原来的blog数据
        Blog blog = new Blog();
        blog.setId(id);
        blog.setTitle(title);
        blog.setContent(content);
        blog.setBlogDescription(description);
        blog.setBlogStatus(blogStatus);
        blog.setEnableComment(enableComment);
        blog.setCategoryId(categoryId);
        blog.setUpdateTime(new Date());
        blogService.updateById(blog);
        return Result.success();
    }

    @PostMapping("/save")
    @Transactional
    @ResponseBody
    public Result save(String title,
                         String tags,
                         String content,
                         String description,
                         Integer blogStatus,
                         Integer enableComment,
                         Integer categoryId,
                       HttpSession session){
        //获取当前的登录用户
        UserDTO userDTO = (UserDTO)session.getAttribute(SystemConstants.LOGIN_USER);
        Integer userId = userDTO.getId();
        //添加新的blog
        Blog blog = new Blog();
        blog.setUserId(userId);
        blog.setTitle(title);
        blog.setContent(content);
        blog.setBlogDescription(description);
        blog.setBlogStatus(blogStatus);
        blog.setEnableComment(enableComment);
        blog.setCategoryId(categoryId);
        Date date = new Date();
        blog.setCreateTime(date);
        blog.setUpdateTime(date);
        blogService.save(blog);
        Integer blogId = blog.getId();
        //将新的标签插入
        String[] newTagNames = tags.split(",");
        for(String newTagName : newTagNames) {
            //判断newTagName是否存在tb_tag中
            Tag tagDB = tagService.getOne(new LambdaQueryWrapper<Tag>().eq(Tag::getName, newTagName));
            if (tagDB == null) {
                //不存在,那么将tagDB插入到tb_tag中
                tagDB = new Tag();
                tagDB.setName(newTagName);
                tagService.save(tagDB);
            }
            //将当前这篇blog和标签建立联系
            tagService.saveTagIdAndBlogId(tagDB.getId(), blogId);
        }
        return Result.success();
    }

    @PostMapping("/blogs/md/uploadfile")
    public void uploadFileByEditormd(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestParam(name = "editormd-image-file") MultipartFile file) throws IOException, URISyntaxException {
        String fileName = file.getOriginalFilename();
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        //生成文件名称通用方法
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Random r = new Random();
        StringBuilder tempName = new StringBuilder();
        tempName.append(sdf.format(new Date())).append(r.nextInt(100)).append(suffixName);
        String newFileName = tempName.toString();
        //创建文件
        File destFile = new File(SystemConstants.FILE_UPLOAD_DIC + newFileName);
        String fileUrl = MyBlogUtils.getHost(new URI(request.getRequestURL() + "")) + "/upload/" + newFileName;
        File fileDirectory = new File(SystemConstants.FILE_UPLOAD_DIC);
        try {
            if (!fileDirectory.exists()) {
                if (!fileDirectory.mkdir()) {
                    throw new IOException("文件夹创建失败,路径为：" + fileDirectory);
                }
            }
            file.transferTo(destFile);
            request.setCharacterEncoding("utf-8");
            response.setHeader("Content-Type", "text/html");
            response.getWriter().write("{\"success\": 1, \"message\":\"success\",\"url\":\"" + fileUrl + "\"}");
        } catch (UnsupportedEncodingException e) {
            response.getWriter().write("{\"success\":0}");
        } catch (IOException e) {
            response.getWriter().write("{\"success\":0}");
        }
    }

    @PostMapping("/delete")
    @ResponseBody
    @Transactional
    public Result delete(@RequestBody Integer[] ids){
        //将当前用户中对应ids的blog删除
        blogService.removeByIds(Arrays.asList(ids));
        return Result.success();
    }
}
