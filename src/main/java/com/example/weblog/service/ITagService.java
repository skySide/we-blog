package com.example.weblog.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.weblog.entity.Blog;
import com.example.weblog.entity.Tag;

import java.util.List;

public interface ITagService extends IService<Tag> {

    Integer getPages(Integer tagId, Integer pageSize);

    List<Tag> getTagsByBlogId(Integer id);

    Integer getBlogCountByTagId(Integer id);

    void removeByTagIdAndBlogId(Integer tagId, Integer blogId);

    void saveTagIdAndBlogId(Integer tagId, Integer blogId);

    void removeById2(Integer tagId);
}
