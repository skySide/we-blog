package com.example.weblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.weblog.entity.Comment;
import com.example.weblog.entity.User;

import java.util.List;

public interface ICommentService extends IService<Comment> {

    List<Comment> getRepliesByCommentId(Integer commentId);

    void saveCommentAndReply(Integer commentId, Integer replyId);
}
