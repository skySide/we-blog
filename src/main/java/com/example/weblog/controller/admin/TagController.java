package com.example.weblog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.weblog.entity.Tag;
import com.example.weblog.service.ITagService;
import com.example.weblog.util.PageResult;
import com.example.weblog.vo.Result;
import com.example.weblog.vo.ResultBean;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/tag")
public class TagController {
    @Resource
    private ITagService tagService;

    //前往标签管理的界面
    @GetMapping({"/", "index"})
    public String index(){
        return "admin/tag";
    }

    //获取所有的标签以及对应的blog数量
    @GetMapping("/list")
    @ResponseBody
    public Result list(@RequestParam(value = "currentPage", required = false)Long currentPage,
                       @RequestParam(value = "pageSize",required = false)Long pageSize){
        //获取第currentPage页的记录，并且每页有pageSize条记录
        IPage<Tag> page = new Page<>(currentPage, pageSize);
        tagService.page(page);
        List<Tag> tags = page.getRecords();
        tags.forEach(tag -> {
            tag.setBlogCount(tagService.getBlogCountByTagId(tag.getId()));
        });
        PageResult<Tag> pageResult = new PageResult<>();
        pageResult.setList(tags);
        pageResult.setTotal(tagService.count());
        pageResult.setPages(page.getPages());
        pageResult.setCurrentPage(currentPage);
        return Result.success(pageResult);
    }

    @PostMapping("/save")
    @Transactional
    @ResponseBody
    public Result save(@RequestParam("tagName")String tagName){
        Tag tag = tagService.getOne(new LambdaQueryWrapper<Tag>().eq(Tag::getName, tagName));
        if(tag != null){
            return Result.fail(ResultBean.TAG_ALREADY_EXIST, null);
        }
        tag = new Tag();
        tag.setName(tagName);
        tagService.save(tag);
        return Result.success();
    }

    @PostMapping("/delete")
    @Transactional
    @ResponseBody
    public Result delete(@RequestBody Integer[] ids){
        //批量删除tag
        tagService.removeBatchByIds(Arrays.asList(ids));
        //将删除中间表tb_blog_tag中的数据
        for(Integer id : ids){
            tagService.removeById2(id);
        }
        return Result.success();
    }
}
