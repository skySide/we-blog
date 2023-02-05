package com.example.weblog.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> {
    List<T> list;//第currentPage的记录
    Long total;//记录总数
    Long currentPage;//第几页
    Long pages;//总页数
}
