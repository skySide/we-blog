package com.example.weblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.weblog.dto.BlogDTO;
import com.example.weblog.entity.Blog;

import java.util.List;

public interface IBlogService extends IService<Blog> {
    List<BlogDTO> getNewBlogs();

    List<BlogDTO> getHotBlogs();

    List<Blog> getBlogsByTagId(Integer tagId,Integer offset, Integer pageSize);


    List<Blog> getBlogsByCategoryId(Integer categoryId, int offset, Integer pageSize);

    Integer getPagesByCategoryId(Integer categoryId, Integer pageSize);

    Integer getBlogCountByCategoryId(Integer categoryId);
}
