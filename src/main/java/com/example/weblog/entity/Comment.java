package com.example.weblog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {
    private Integer id;
    private Integer blogId;
    private Integer userId;
    private Integer likes = 0;//点赞数
    private String commentBody;
    private Integer commentType; //评论的类型，判断是否为一级评论
    private Integer commentStatus = 0; //评论是否已经审核通过,0表示没有通过，1表示通过
    private Date createTime;
    private Date updateTime;

    @TableField(exist = false)
    private String nickname;// 发布blog的用户

    @TableField(exist = false)
    private String icon = "/admin/dist/img/user/user_0.png"; //发布blog的用户头像

    @TableField(exist = false)
    private String blogName;

    @TableField(exist = false)
    private List<Comment> replies;
}
