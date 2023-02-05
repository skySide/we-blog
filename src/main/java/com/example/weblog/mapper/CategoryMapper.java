package com.example.weblog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.weblog.entity.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CategoryMapper extends BaseMapper<Category> {
}
