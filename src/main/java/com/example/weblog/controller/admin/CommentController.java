package com.example.weblog.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.weblog.dto.UserDTO;
import com.example.weblog.entity.Blog;
import com.example.weblog.entity.Category;
import com.example.weblog.entity.Comment;
import com.example.weblog.entity.User;
import com.example.weblog.service.IBlogService;
import com.example.weblog.service.ICommentService;
import com.example.weblog.service.IUserService;
import com.example.weblog.util.CommentConstants;
import com.example.weblog.util.PageResult;
import com.example.weblog.util.SystemConstants;
import com.example.weblog.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/comment")
public class CommentController {
    @Resource
    private ICommentService commentService;

    @Resource
    private IBlogService blogService;

    @Resource
    private IUserService userService;


    @RequestMapping("/")
    public String index(){
        return "admin/comment";
    }

    /*
     * 获取当前用户能够得到的所有评论
     */
    @RequestMapping("/list")
    @ResponseBody
    public Result list(@RequestParam(value = "currentPage", required = false)Long currentPage,
                              @RequestParam(value = "pageSize",required = false)Long pageSize,
                              @RequestParam(value = "target",required = false)String target,
                              HttpSession session){
        //获取当前登录用户
        UserDTO userDTO = (UserDTO)session.getAttribute(SystemConstants.LOGIN_USER);
        Integer userId = userDTO.getId();
        //获取每篇blog的评论
        List<Comment> comments;
        if(target == null) {
            //如果target为null，那么获取当前登录用户得到的所有评论
            List<Blog> blogs = blogService.list(new LambdaQueryWrapper<Blog>().eq(Blog::getUserId, userId)
                    .orderByDesc(Blog::getCreateTime));
            comments = new ArrayList<>();
            for(Blog blog : blogs){
                List<Comment> commentBlogs = commentService.list(new LambdaQueryWrapper<Comment>().eq(Comment::getBlogId, blog.getId()));
                for (Comment commentBlog : commentBlogs) {
                    //获取这个评论是在哪篇blog中发布的
                    commentBlog.setBlogName(blog.getTitle());
                    //获取这个评论的发布者
                    User user = userService.getById(commentBlog.getUserId());
                    commentBlog.setIcon(user.getIcon());
                    commentBlog.setNickname(user.getNickname());
                    comments.add(commentBlog);
                }
            }
        }else{
            //否则查询当前登录用户发布的所有评论
            comments = commentService.list(new LambdaQueryWrapper<Comment>().eq(Comment::getUserId, userId)
                                                                            .orderByDesc(Comment::getCreateTime));
            comments.forEach(comment->{
                comment.setNickname(userDTO.getNickname());
                comment.setIcon(userDTO.getIcon());
                //设置评论是在哪篇blog发布的
                comment.setBlogName(blogService.getById(comment.getBlogId()).getTitle());
            });
        }
        //获取总评论数
        long total = comments.size();
        //获取总页数
        long pages = total / pageSize;
        if(total % pageSize != 0){
            ++pages;
        }
        PageResult<Comment> pageResult = new PageResult<>();
        pageResult.setList(comments);
        pageResult.setTotal(total);
        pageResult.setCurrentPage(currentPage);
        pageResult.setPages(pages);
        return Result.success(pageResult);
    }

    @PostMapping("/reply")
    @Transactional
    @ResponseBody
    public Result reply(@RequestParam("id") Integer commentId,
                        @RequestParam("replyBody") String replyBody,
                        HttpSession session){
        //获取当前的登录用户
        UserDTO user = (UserDTO)session.getAttribute(SystemConstants.LOGIN_USER);
        Integer userId = user.getId();
        //获取被回复的评论
        Comment comment = commentService.getById(commentId);
        //插入新的评论,设置对应的参数
        Comment reply = new Comment();
        reply.setCreateTime(new Date());
        reply.setCommentType(CommentConstants.SECOND_COMMENT_TYPE);
        reply.setCommentBody(replyBody);
        reply.setUserId(userId);
        reply.setBlogId(comment.getBlogId());
        commentService.save(reply);
        //设置被回复的comment对应的回复,即插入中间表tb_comment_reply
        commentService.saveCommentAndReply(commentId, reply.getId());
        return Result.success();
    }

    @PostMapping("/checkDone")
    @Transactional
    @ResponseBody
    public Result checkDone(@RequestBody Integer[] ids){
        //将ids对应的评论的commentStatus更新为1,从而对评论进行审核
        for(int id : ids){
            commentService.update(new UpdateWrapper<Comment>().setSql("comment_status = 1")
                                                              .eq("id", id));
        }
        return Result.success();
    }

    /*
     * 删除对应id的评论，不仅仅要删除这条评论，还需要将这条评论对应的回复删除
     */
    @PostMapping("/delete")
    @Transactional
    @ResponseBody
    public Result delete(@RequestBody Integer[] ids){
        //进行批量删除操作
        commentService.removeBatchByIds(Arrays.asList(ids));
        //将id对应的回复删除
        for(Integer id : ids){
            //获取这一条评论的回复
            List<Integer> replyIds = commentService.getRepliesByCommentId(id).stream()
                                                         .map(Comment::getId).collect(Collectors.toList());
            //批量删除回复
            if(!replyIds.isEmpty()){
                commentService.removeBatchByIds(replyIds);
            }
        }
        return Result.success();
    }
}
