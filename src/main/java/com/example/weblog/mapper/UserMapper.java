package com.example.weblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.weblog.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("select tb_user.id, icon, nickname from " +
            "tb_user inner join tb_comment " +
            "on tb_user.id = tb_comment.user_id " +
            "where tb_comment.id = #{id}")
    User getUserByCommentId(Integer id);
}
