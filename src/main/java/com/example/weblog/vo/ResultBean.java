package com.example.weblog.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum ResultBean {


    SUCCESS(200, "操作成功"),
    ERROR(400, "操作失败"),
    LOGIN_VERIFY_ERROR(400001, "验证码不一致"),
    CATEGORY_ALREADY_EXIST_ERROR(400002, "该分类已经存在"),
    USER_NOT_EXIST(400004, "该用户不存在"),
    FILE_UPLOAD_ERROR(400005, "文件上传失败"),
    TAG_ALREADY_EXIST(400006, "该分类已经存在"),
    ;
    private Integer code;
    private String msg;
}
