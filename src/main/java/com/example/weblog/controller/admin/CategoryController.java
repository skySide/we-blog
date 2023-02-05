package com.example.weblog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.weblog.dto.UserDTO;
import com.example.weblog.entity.Blog;
import com.example.weblog.entity.Category;
import com.example.weblog.service.IBlogService;
import com.example.weblog.service.ICategoryService;
import com.example.weblog.util.PageResult;
import com.example.weblog.util.SystemConstants;
import com.example.weblog.vo.Result;
import com.example.weblog.vo.ResultBean;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/category")
public class CategoryController {
    @Resource
    private ICategoryService categoryService;

    @Resource
    private IBlogService blogService;

    @GetMapping("/")
    public ModelAndView toList(ModelAndView modelAndView){
        //前往分类的界面
        modelAndView.setViewName("/admin/category");
        return modelAndView;
    }
    @GetMapping("/list")
    @ResponseBody
    public Result list(@RequestParam(value = "currentPage", required = false)Long currentPage,
                       @RequestParam(value = "pageSize",required = false)Long pageSize){
        //获取第currentPage页的记录，每页记录有pageSize条
        IPage<Category> page = new Page<>(currentPage, pageSize);
        categoryService.page(page);
        List<Category> categories = page.getRecords();
        categories.forEach(category -> {
            //获取当前登录用户中每个category的blog数目
            category.setBlogCount(blogService.getBlogCountByCategoryId(category.getId()));
        });
        PageResult<Category> pageResult = new PageResult<>();
        pageResult.setList(categories);
        pageResult.setTotal(page.getTotal());
        pageResult.setCurrentPage(currentPage);
        pageResult.setPages(page.getPages());
        return Result.success(pageResult);
    }

    /*
     * 新增category，那么需要判断这个categoryName是否已经在
     * 当前用户的分类中已经存在了，如果已经存在，那么给出相应的提示，否则
     * 如果不存在，那么就新增category
     */
    @PostMapping("/save")
    @ResponseBody
    @Transactional
    public Result save(Category category){
        //1、获取当前categoryName在数据库中对应的category
        Category categoryDB = categoryService.getOne(new LambdaQueryWrapper<Category>().eq(Category::getName, category.getName()));
        if(categoryDB == null){
            //1.1 如果不存在，那么说明category不存在,需要插入到category
            categoryService.save(category);
        }else{
            //1.2 当前用户已经存在了这个category
            return Result.fail(ResultBean.CATEGORY_ALREADY_EXIST_ERROR, null);
        }
        return Result.success();
    }

    /*
     * 更新category，判断当前用户是否已经存在了这个category，如果没有，
     * 那么就进行更新操作，否则当前用户已经存在了这个category，那么直接
     * 返回错误信息
     */
    @PostMapping("/update")
    @ResponseBody
    @Transactional
    public Result update(Category category){
        System.out.println(category);
        //1、判断当前用户是否已经存在了更新后的category是否已经存在tb_category中
        Category categoryDB = categoryService.getOne(new LambdaQueryWrapper<Category>().eq(Category::getName, category.getName()));
        if(categoryDB == null){
            //2.1 当前的用户并不存在更新后的category，直接更新
            categoryService.updateById(category);
            return Result.success();
        }else{
            //2.2 当前的用户已经存在了更新后的category,返回错误信息
            return Result.fail(ResultBean.CATEGORY_ALREADY_EXIST_ERROR, null);
        }
    }

    /*
     * 删除category，那么将这个category删除之后，当前用户的blog
     * 不可以再拥有这个category
     */
    @PostMapping("/delete")
    @ResponseBody
    @Transactional
    public Result delete(@RequestBody Integer[] ids){
        categoryService.removeBatchByIds(Arrays.asList(ids));
        return Result.success();
    }
}
