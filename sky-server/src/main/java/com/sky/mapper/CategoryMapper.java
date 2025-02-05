package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.CategoryPageQueryDTO;
import com.sky.entity.Category;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CategoryMapper {

    /*
    * 插入分类
    * */
    @Insert("insert into category(type, name, sort, status, create_time, update_time, create_user, update_user)"
     + "values "
     + "(#{type}, #{name}, #{sort}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser})")
    void insert(Category category);

    /*
    * 分页查询分类
    * */
    Page<Category> pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    /*
    * 启用禁用分类
    * */
    void update(Category category);

    @Delete("delete from category where id = #{id}")
    void deleteById(Integer id);
}
