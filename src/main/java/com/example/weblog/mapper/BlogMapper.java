package com.example.weblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.weblog.entity.Blog;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface BlogMapper extends BaseMapper<Blog> {
    @Select("select id, title, blog_description, views, likes, category_id, content, blog_status, enable_comment, user_id, create_time, update_time " +
            "from tb_blog order by create_time desc limit 8")
    List<Blog> getNewBlogs();

    @Select("select id, title, blog_description, views, likes, category_id, content, blog_status, enable_comment, user_id, create_time, update_time  from tb_blog order by views desc limit 8 offset 0")
    List<Blog> getHotBlogs();

    @Select("select tb_blog.id, title, blog_description, views, likes, category_id, content, blog_status, enable_comment, user_id, create_time, update_time  " +
            "from " +
            "tb_blog inner join tb_blog_tag " +
            "on tb_blog.id = tb_blog_tag.blog_id " +
            "where tb_blog_tag.tag_id = #{tagId} " +
            "order by tb_blog.create_time desc " +
            "limit #{pageSize} offset #{offset}")
    List<Blog> getBlogsByTagId(Integer tagId, Integer offset, Integer pageSize);


    @Select("select id, title, blog_description, views, likes, category_id, content, blog_status, enable_comment, user_id, create_time, update_time  from tb_blog " +
            "where category_id = #{categoryId} " +
            "order by create_time desc " +
            "limit #{pageSize} offset #{offset}")
    List<Blog> getBlogsByCategoryId(Integer categoryId, int offset, Integer pageSize);


    @Select("select count(tb_blog.id) from tb_blog where category_id = #{categoryId}")
    Integer getBlogCountByCategoryId(Integer categoryId);

}
