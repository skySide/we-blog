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
public class Blog {
    private Integer id;
    private String title;
    private String blogDescription;
    private Integer views;
    private Integer likes;
    private Integer categoryId;
    private String content;
    private Integer blogStatus;//blog的状态: 是否已经发布
    private Integer enableComment;//是否开启了评论
    private Integer userId;
    private Date createTime;
    private Date updateTime;
    @TableField(exist = false)
    private List<Tag> tags;
    @TableField(exist = false)
    private String categoryName;
    @TableField(exist =  false)  //使用这个注解，在数据库表中不存在这个字段，但是实体类中可以存在这个属性
    private String nickname;
    @TableField(exist = false)
    private String icon; //用户头像
}
