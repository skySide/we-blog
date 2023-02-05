package com.example.weblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.weblog.entity.Comment;
import com.example.weblog.entity.User;
import com.example.weblog.mapper.CommentMapper;
import com.example.weblog.service.ICommentService;
import com.example.weblog.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ICommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    @Resource
    private CommentMapper commentMapper;

    @Override
    public List<Comment> getRepliesByCommentId(Integer commentId) {
        return commentMapper.getRepliesByCommentId(commentId);
    }


    @Override
    public void saveCommentAndReply(Integer commentId, Integer replyId) {
        commentMapper.saveCommentAndReply(commentId, replyId);
    }
}
