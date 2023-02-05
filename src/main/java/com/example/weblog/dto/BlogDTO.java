package com.example.weblog.dto;

import com.example.weblog.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogDTO {
    private Integer id;
    private String title;
    private Integer views;
    private Integer likes;
    private Integer blogStatus;//blog的状态: 是否已经发布
    private String categoryName;
    private Date createTime;
    private Date updateTime;

}
