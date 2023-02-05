package com.example.weblog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.weblog.dto.BlogDTO;
import com.example.weblog.entity.Blog;
import com.example.weblog.mapper.BlogMapper;
import com.example.weblog.service.IBlogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class IBlogServiceImpl extends ServiceImpl<BlogMapper, Blog> implements IBlogService {
    @Resource
    private BlogMapper blogMapper;

    @Override
    public List<BlogDTO> getNewBlogs() {
        List<Blog> newBlogs = blogMapper.getNewBlogs();
        ArrayList<BlogDTO> blogDTOS = new ArrayList<>();
        for(Blog newBlog : newBlogs){
            blogDTOS.add(BeanUtil.copyProperties(newBlog, BlogDTO.class));
        }
        return blogDTOS;
    }

    @Override
    public List<BlogDTO> getHotBlogs() {
        List<Blog> blogs = blogMapper.getHotBlogs();
        log.info("IBlogServiceImpl getHotBlogs = " + blogs);
        List<BlogDTO> hotBlogs = new ArrayList<>();
        blogs.forEach(blog -> {
            hotBlogs.add(BeanUtil.copyProperties(blog, BlogDTO.class));
        });
        return hotBlogs;
    }

    @Override
    public List<Blog> getBlogsByTagId(Integer tagId, Integer offset, Integer pageSize) {
        return blogMapper.getBlogsByTagId(tagId, offset, pageSize);
    }


    @Override
    public List<Blog> getBlogsByCategoryId(Integer categoryId, int offset, Integer pageSize) {
        return blogMapper.getBlogsByCategoryId(categoryId, offset, pageSize);
    }

    @Override
    public Integer getPagesByCategoryId(Integer categoryId, Integer pageSize) {
        Integer total = blogMapper.getBlogCountByCategoryId(categoryId);
        int pages = total / pageSize;
        if(total % pageSize == 0){
            return pages;
        }else{
            return pages + 1;
        }
    }

    @Override
    public Integer getBlogCountByCategoryId(Integer categoryId) {
        return blogMapper.getBlogCountByCategoryId(categoryId);
    }
}
