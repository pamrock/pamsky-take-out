package com.sky.service;

import com.sky.dto.CategoryDTO;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import com.sky.result.PageResult;

import java.util.List;


public interface CategoryService {

    /*
    * 新增分类
    * */
    void save(CategoryDTO categoryDTO);

    /*
    * 分类分页查询
    * */
    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /*
    * 启用禁用分类
    * */
    void startOrStop(Integer status, long id);

    /*
    * 编辑分类信息
    * */
    void update(CategoryDTO categoryDTO);

    /*
    * 根据id删除分类
    * */
    void deleteById(Integer id);

    List<Category> list(Integer type);
}
