package com.example.weblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.weblog.entity.User;
import com.example.weblog.mapper.UserMapper;
import com.example.weblog.service.IUserService;
import com.example.weblog.util.MD5Utils;
import com.example.weblog.util.SystemConstants;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IUserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    private UserMapper userMapper;
    /*
     * 前台传递给后台的密码只经过了一次MD5加密，但是写入数据库的时候还需要进行
     * 1次MD5加密，所以我们查询的时候也需要先进行MD5加密，在查询
     */
    @Override
    public User getUserByUsernameAndPassword(String username, String password) {
        String dbPassword = MD5Utils.formPassToDbPass(password);
        LambdaQueryWrapper<User> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(User::getNickname, username);
        lambdaQueryWrapper.eq(User::getPassword, dbPassword);
        return userMapper.selectOne(lambdaQueryWrapper);
    }

    @Override
    public User getUserByCommentId(Integer id) {
        return userMapper.getUserByCommentId(id);
    }
}
