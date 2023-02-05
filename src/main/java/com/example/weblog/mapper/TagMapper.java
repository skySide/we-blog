package com.example.weblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.weblog.entity.Blog;
import com.example.weblog.entity.Tag;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
    @Select("select count(*) from tb_blog_tag where tag_id = #{tagId}")
    Integer getBlogCountById(Integer tagId);

    @Select("select tb_tag.id, tb_tag.name from " +
            "tb_tag inner join tb_blog_tag " +
            "on tb_tag.id = tb_blog_tag.tag_id " +
            "where tb_blog_tag.blog_id = #{id}")
    List<Tag> getTagsByBlogId(Integer id);

    @Delete("delete from tb_blog_tag where tag_id = #{tagId} and blog_id = #{blogId}")
    void removeByTagIdAndBlogId(Integer tagId, Integer blogId);
    @Insert("insert into tb_blog_tag (tag_id, blog_id) values (#{tagId}, #{blogId})")
    void saveTagIdAndBlogId(Integer tagId, Integer blogId);

    @Delete("delete from tb_blog_tag where tag_id = #{tagId}")
    void removeById2(Integer tagId);
}
