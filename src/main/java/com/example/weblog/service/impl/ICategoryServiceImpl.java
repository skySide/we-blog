package com.example.weblog.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.weblog.entity.Category;
import com.example.weblog.mapper.CategoryMapper;
import com.example.weblog.service.ICategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ICategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {
}
