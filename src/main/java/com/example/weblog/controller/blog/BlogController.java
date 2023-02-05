package com.example.weblog.controller.blog;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.conditions.ChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.weblog.dto.BlogDTO;
import com.example.weblog.dto.UserDTO;
import com.example.weblog.entity.*;
import com.example.weblog.service.*;
import com.example.weblog.util.CommentConstants;
import com.example.weblog.util.SystemConstants;
import com.example.weblog.vo.Result;
import com.example.weblog.vo.ResultBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.net.URI;
import java.util.Date;
import java.util.List;
@Slf4j
@Controller
@RequestMapping("/blog")
public class BlogController {
    @Resource
    private IBlogService blogService;

    @Resource
    private IUserService userService;

    @Resource
    private ICategoryService categoryService;

    @Resource
    private ITagService tagService;

    @Resource
    private ICommentService commentService;

    /*
     * 获取第currentPage页的blog，并且页数为size
     * 同时需要将每篇blog的作者，以及所在的分类查找出来
     *
     * 其次还需要查找所有的标签,最新发布的blog(根据创作时间降序排序，然后获取前面8条记录)
     */
 /*   @GetMapping({"/page/{currentPage}","/"})
    @ResponseBody
    public Result page(@PathVariable(value = "currentPage", required = false) Integer currentPage,
                             @RequestParam(value="pageSize", defaultValue = "1", required = false)Integer size){
        if(currentPage == null){
            currentPage = 1;
        }
        IPage<Blog> page = new Page<Blog>(currentPage, size);
        //获取所有已经发布了的blog,并且根据发布的时间降序排序
        blogService.page(page,new LambdaQueryWrapper<Blog>()
                .eq(Blog::getBlogStatus, 1)
                .orderByDesc(Blog::getCreateTime));
        List<Blog> blogs = page.getRecords();
        blogs.forEach(blog ->{
            //对于每一篇blog，需要设置作者的名字
            findUserByBlog(blog);
            findCategoryByBlog(blog);
        });
        return Result.success(new Object[]{blogs, page.getPages()});
    }*/
    @GetMapping({"/page","/"})
    public ModelAndView page(@RequestParam(value="currentPage", defaultValue = "1", required = false)Integer currentPage,
                             @RequestParam(value="pageSize", defaultValue = "1", required = false)Integer size,
                             ModelAndView modelAndView){
        IPage<Blog> page = new Page<Blog>(currentPage, size);
        //获取所有已经发布了的blog,并且根据发布的时间降序排序
        blogService.page(page,new LambdaQueryWrapper<Blog>()
                .eq(Blog::getBlogStatus, 1)
                .orderByDesc(Blog::getCreateTime));
        List<Blog> blogs = page.getRecords();
        blogs.forEach(blog ->{
            //对于每一篇blog，需要设置作者的名字
            findUserByBlog(blog);
            findCategoryByBlog(blog);
        });
        modelAndView.addObject("blogs", blogs);
        modelAndView.addObject("currentPage", currentPage);
        modelAndView.addObject("pages", page.getPages());//页数
        //获取所有的标签,以及标签对应的博客数量
        List<Tag> tags = tagService.list();
        tags.forEach(tag -> {
            tag.setBlogCount(tagService.getBlogCountByTagId(tag.getId()));
        });
        modelAndView.addObject("tags", tags);
        //获取最新发布的blog
        modelAndView.addObject("newBlogs",blogService.getNewBlogs());
        //获取点击最多的blog
        modelAndView.addObject("hotBlogs", blogService.getHotBlogs());
        modelAndView.setViewName("/blog/amaze/index.html");
        return modelAndView;
    }
    @GetMapping("/hot")
    public Result queryHotBlogs(){
        //获取浏览历史最多的blog,根据views降序排序，然后获取前面9篇blog
        return Result.success(blogService.getHotBlogs());
    }

    @GetMapping("/new")
    public Result queryNewBlogs(){
        //获取最新blog
        return Result.success(blogService.getNewBlogs());
    }

    /*
     * 根据种类查找blog,如果没有发送查询categoryId，那么默认就是1，即查找默认
     * 分类的blog,查找的是第currentPage页的记录，并且每页有pageSize条
     */
    @GetMapping("/category")
    public ModelAndView getBlogByCategoryId(@RequestParam(value = "categoryId", defaultValue = "1", required = false)Integer categoryId,
                                            @RequestParam(value="currentPage", defaultValue = "1", required = false)Integer currentPage,
                                            @RequestParam(value="pageSize", defaultValue = "1", required= false)Integer pageSize,
                                            ModelAndView modelAndView){
        List<Blog> blogs = blogService.getBlogsByCategoryId(categoryId, (currentPage - 1) * pageSize, pageSize);
        blogs.forEach(blog -> {
            findUserByBlog(blog);
            findCategoryByBlog(blog);
        });
        modelAndView.addObject("blogs", blogs);
        modelAndView.addObject("currentPage", currentPage);
        modelAndView.addObject("pages", blogService.getPagesByCategoryId(categoryId, pageSize));
        //获取所有的标签以及每个标签对应的博客数量
        List<Tag> tags = tagService.list();
        tags.forEach(tag -> {
            tag.setBlogCount(tagService.getBlogCountByTagId(tag.getId()));
        });
        modelAndView.addObject("tags", tags);
        //获取所有的最新发布的blog
        modelAndView.addObject("newBlogs", blogService.getNewBlogs());
        //获取浏览最多的blog
        modelAndView.addObject("hotBlogs", blogService.getHotBlogs());
        modelAndView.setViewName("/blog/amaze/list.html");
        //获取请求的url
        String url = "/blog/category?categoryId=" + categoryId;
        modelAndView.addObject("url", url);
        return modelAndView;

    }

    /*
     * 根据标签id查找blog,如果没有发送查询tagId，那么默认就是1，即查找默认
     * 分类的blog,查找的是第currentPage页的记录，并且每页有pageSize条
     */
    @GetMapping("/tag")
    public ModelAndView getBlogByTagId(@RequestParam(value = "tagId", defaultValue = "1", required = false)Integer tagId,
                                       @RequestParam(value="currentPage", defaultValue = "1", required = false)Integer currentPage,
                                            @RequestParam(value="pageSize", defaultValue = "1", required= false)Integer pageSize,
                                            ModelAndView modelAndView){
        List<Blog> blogs = blogService.getBlogsByTagId(tagId, (currentPage - 1) * pageSize, pageSize);
        blogs.forEach(blog -> {
            findCategoryByBlog(blog);
            findUserByBlog(blog);
        });
        modelAndView.addObject("blogs", blogs);
        modelAndView.addObject("currentPage", currentPage);
        //获取这个标签一共有多少页记录
        modelAndView.addObject("pages", tagService.getPages(tagId, pageSize));
        //获取所有的标签以及每个标签对应的博客数量
        List<Tag> tags = tagService.list();
        tags.forEach(tag -> {
            tag.setBlogCount(tagService.getBlogCountByTagId(tag.getId()));
        });
        modelAndView.addObject("tags", tags);
        //获取所有的最新发布的blog
        modelAndView.addObject("newBlogs", blogService.getNewBlogs());
        //获取浏览最多的blog
        modelAndView.addObject("hotBlogs", blogService.getHotBlogs());
        modelAndView.setViewName("/blog/amaze/list.html");
        //获取请求的url
        String url = "/blog/tag?tagId=" + tagId;
        modelAndView.addObject("url", url);
        return modelAndView;

    }

    /*
     * 获取某一个id的blog的详细内容,同时需要获取这个blog的所有评论
     */
    @GetMapping("/detail")
    public ModelAndView detail(@RequestParam(name="id", defaultValue = "1", required = false)Integer id,
                               @RequestParam(name = "currentPage", defaultValue = "1", required = false)Integer currentPage,
                               @RequestParam(name = "pageSize", defaultValue = "4", required = false)Integer pageSize,
                               ModelAndView modelAndView){
        blogService.update().setSql("views = views + 1").eq("id", id).update();
        Blog blog = blogService.getOne(new LambdaQueryWrapper<Blog>().eq(Blog::getId, id));
        findUserByBlog(blog);
        findCategoryByBlog(blog);
        //1、获取这个blog的所有标签
        List<Tag> tags = tagService.getTagsByBlogId(id);
        blog.setTags(tags);
        modelAndView.addObject("blog", blog);
        //2、获取这个blog的所有评论以及总的评论数
        //2.1 先获取所有已经审核了的一级评论
        IPage<Comment> page = new Page<>(currentPage, pageSize);
        commentService.page(page, new LambdaQueryWrapper<Comment>().eq(Comment::getBlogId, id)
                                               .eq(Comment::getCommentType, CommentConstants.FIRST_COMMENT_TYPE)
                                               .eq(Comment::getCommentStatus, CommentConstants.CHECK_DONE)
                                               .orderByDesc(Comment::getCreateTime));
        List<Comment> comments = page.getRecords();
        int commentTotal = comments.size();
        //2.2 获取已经审核了的二级评论(也即一级评论的回复)
        for(Comment comment : comments) {
            //2.2.1 获取发布一级评论的用户
            findUserByComment(comment);
            //2.2.2 获取当前这一条评论的回复
            List<Comment> replies = commentService.getRepliesByCommentId(comment.getId());
            commentTotal += replies.size();
            //设置每一条reply的用户
            replies.forEach(reply -> {
                findUserByComment(reply);
            });
            comment.setReplies(replies);
        }
        //获取评论页数
        modelAndView.addObject("commentTotal", commentTotal);
        modelAndView.addObject("pages", page.getPages());
        modelAndView.addObject("currentPage", currentPage);
        modelAndView.addObject("comments", comments);
        /*获取所有的标签以及每个标签对应的博客数量
        List<Tag> allTags = tagService.list();
        allTags.forEach(tag -> {
            tag.setBlogCount(tagService.getBlogCountByTagId(tag.getId()));
        });
        modelAndView.addObject("tags", allTags);
        //获取所有的最新发布blog
        modelAndView.addObject("newBlogs", blogService.getNewBlogs());
        //获取点击最多的blog
        modelAndView.addObject("hotBlogs", blogService.getHotBlogs());*/
        //设置url
        String url = "/blog/detail?id=" + id;
        modelAndView.addObject("url", url);
        modelAndView.setViewName("/blog/amaze/detail.html");
        return modelAndView;
    }

    @PostMapping("/comment")
    @Transactional
    @ResponseBody
    public Result comment(@RequestParam("id")Integer blogId,
                          @RequestParam("verifyCode")String verifyCode,
                          @RequestParam("commentator")String commentator,
                          @RequestParam("commentBody")String commentBody,
                          HttpSession session){
        //获取验证码,判断验证码是否一致
        String realVerifyCode = (String)session.getAttribute(SystemConstants.LOGIN_VERIFY_CODE);
        if(!verifyCode.equals(realVerifyCode)){
            return Result.fail(ResultBean.LOGIN_VERIFY_ERROR, null);
        }
        //获取当前评论的用户id
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getNickname, commentator));
        if(user == null){
            return Result.fail(ResultBean.USER_NOT_EXIST, null);
        }
        //创建新的评论
        Comment comment = new Comment();
        comment.setBlogId(blogId);
        comment.setCommentBody(commentBody);
        comment.setCreateTime(new Date());
        comment.setUserId(user.getId());
        comment.setCommentType(CommentConstants.FIRST_COMMENT_TYPE);
        commentService.save(comment);
        return Result.success();

    }
    private void findUserByComment(Comment comment) {
        User user = userService.getOne(new LambdaQueryWrapper<User>().eq(User::getId, comment.getUserId()));
        comment.setNickname(user.getNickname());
        comment.setIcon(user.getIcon());
    }

    /*
     * 根据keyword进行模糊查找，寻找blog中title或者content包含keyword的blog
     */
    @RequestMapping("/search")
    public ModelAndView search(@RequestParam("keyword")String keyword,
                               @RequestParam(value="currentPage", defaultValue = "1", required = false)Integer currentPage,
                               @RequestParam(value="pageSize", defaultValue = "1", required = false)Integer pageSize,
                               ModelAndView modelAndView){
        //获取相关的blog
        IPage<Blog> page = new Page<>(currentPage, pageSize);
        blogService.page(page, new LambdaQueryWrapper<Blog>().like(Blog::getTitle, keyword)
                .like(Blog::getContent, keyword)
                .orderByDesc(Blog::getCreateTime));
        List<Blog> blogs = page.getRecords();
        //获取每篇blog的分类以及作者
        blogs.forEach(blog -> {
            findCategoryByBlog(blog);
            findUserByBlog(blog);
        });
        modelAndView.addObject("currentPage", currentPage);
        modelAndView.addObject("pages", page.getPages());
        modelAndView.addObject("blogs", blogs);
        //获取所有的标签以及每个标签对应的博客数量
        List<Tag> allTags = tagService.list();
        allTags.forEach(tag -> {
            tag.setBlogCount(tagService.getBlogCountByTagId(tag.getId()));
        });
        modelAndView.addObject("tags", allTags);
        //获取所有的最新发布的blog
        modelAndView.addObject("newBlogs", blogService.getNewBlogs());
        //获取点击做多的blog
        modelAndView.addObject("hotBlogs", blogService.getHotBlogs());
        String url = "/blog/search?keyword=" + keyword;
        modelAndView.addObject("url", url);
        modelAndView.setViewName("/blog/amaze/list.html");
        return modelAndView;
    }


    private void findUserByBlog(Blog blog) {
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getId, blog.getUserId());
        User user = userService.getOne(lambdaQueryWrapper);
        blog.setNickname(user.getNickname());
        blog.setIcon(user.getIcon());
    }

    private void findCategoryByBlog(Blog blog){
        Integer categoryId = blog.getCategoryId();
        if(categoryId != null){
            blog.setCategoryName(categoryService.getById(categoryId).getName());
        }
    }
}
