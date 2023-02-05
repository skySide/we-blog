package com.example.weblog.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Utils {
    public static final String USER_SALT = "1a2b3c4d";//用户登录的盐值
    public static String formPassToDbPass(String formPass){
        String password = "" + USER_SALT.charAt(0) + USER_SALT.charAt(1) + formPass
                + USER_SALT.charAt(6) + USER_SALT.charAt(7);
        return DigestUtils.md5Hex(password);
    }
}
