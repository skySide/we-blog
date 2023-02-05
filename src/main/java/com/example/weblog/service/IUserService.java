package com.example.weblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.weblog.entity.User;

public interface IUserService extends IService<User> {
    User getUserByUsernameAndPassword(String username, String password);

    User getUserByCommentId(Integer id);
}
