package com.example.weblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.weblog.entity.Comment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
    @Select("select tb_comment.id, blog_id, user_id, likes, comment_body, comment_type, comment_status, create_time, update_time " +
            " from " +
            "tb_comment inner join tb_comment_reply " +
            "on tb_comment.id = tb_comment_reply.reply_id " +
            "where tb_comment_reply.comment_id = #{commentId} " +
            "and tb_comment.comment_status = 1 " +
            "order by tb_comment.create_time")
    List<Comment> getRepliesByCommentId(Integer commentId);


    @Select("select count(distinct reply_id) from " +
            "tb_comment_reply " +
            "where comment_id = #{commentId}")
    Integer getReplyCountByCommentId(Integer commentId);

    @Insert("insert into tb_comment_reply (comment_id, reply_id) " +
            "values (#{commentId}, #{replyId})")
    void saveCommentAndReply(Integer commentId, Integer replyId);
}
