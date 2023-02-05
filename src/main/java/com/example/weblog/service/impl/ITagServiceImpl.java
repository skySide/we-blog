package com.example.weblog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.weblog.entity.Blog;
import com.example.weblog.entity.Tag;
import com.example.weblog.mapper.TagMapper;
import com.example.weblog.service.ITagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class ITagServiceImpl extends ServiceImpl<TagMapper, Tag> implements ITagService {
    @Autowired
    private TagMapper tagMapper;


    @Override
    public Integer getPages(Integer tagId, Integer pageSize) {
        //tagId一共有total篇blog
        Integer total = tagMapper.getBlogCountById(tagId);
        int pages = total / pageSize;
        if(total % pageSize == 0){
            return pages;
        }else{
            return pages + 1;
        }
    }

    @Override
    public List<Tag> getTagsByBlogId(Integer id) {
        return tagMapper.getTagsByBlogId(id);
    }

    @Override
    public Integer getBlogCountByTagId(Integer id) {
        return tagMapper.getBlogCountById(id);
    }

    @Override
    public void removeByTagIdAndBlogId(Integer tagId, Integer blogId) {
        tagMapper.removeByTagIdAndBlogId(tagId, blogId);
    }

    @Override
    public void saveTagIdAndBlogId(Integer tagId, Integer blogId) {
        tagMapper.saveTagIdAndBlogId(tagId, blogId);
    }

    @Override
    public void removeById2(Integer tagId) {
        tagMapper.removeById2(tagId);
    }
}
