package com.example.weblog.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private Integer id;
    private String icon;
    private String nickname;
    private String password;
    private String salt;//盐值，用于密码的加密和解密
}
