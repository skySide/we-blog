package com.example.weblog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.weblog.dto.UserDTO;
import com.example.weblog.entity.Blog;
import com.example.weblog.entity.Comment;
import com.example.weblog.service.IBlogService;
import com.example.weblog.service.ICategoryService;
import com.example.weblog.service.ICommentService;
import com.example.weblog.service.ITagService;
import com.example.weblog.util.SystemConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class UserController {
    @Resource
    private IBlogService blogService;
    @Resource
    private ITagService tagService;
    @Resource
    private ICommentService commentService;

    @Resource
    private ICategoryService categoryService;

    @GetMapping({"/", "/index"})
    public ModelAndView index(HttpSession session, ModelAndView modelAndView){
        //获取当前登录用户总的blog数目
        List<Blog> blogs = blogService.list();
        modelAndView.addObject("blogCount", blogs.size());
        //获取当前登录用户总的评论数
        int commentCount = 0;
        for(Blog blog : blogs){
            //获取这篇blog的评论数(没有回复的评论 + 回复)
            commentCount += commentService.count(new LambdaQueryWrapper<Comment>().eq(Comment::getBlogId, blog.getId()));
        }
        modelAndView.addObject("commentCount", commentCount);
        //获取文章分类总数
        modelAndView.addObject("categoryCount", categoryService.count());
        //获取标签的总数
        modelAndView.addObject("tagCount", tagService.count());
        modelAndView.setViewName("/admin/index");
        return modelAndView;
    }
}
