package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface DishMapper {

    @Select("select count(id) from dish where category_id = #{categoryId}")
    Integer countByCategoryId(Integer id);

    /*
    * 插入菜品数据
    * */
    @AutoFill(value = OperationType.INSERT)
    void insert(Dish dish);

    /*
    * 菜品分页查询
    * */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /*
    * 根据主键查询菜品
    * */
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    /*
    * 根据主键删除菜品数据
    * */
    @Delete("delete from dish where id = #{id}")
    void DeleteById(Long id);

    /*
    * 根据菜品id集合批量删除菜品数据
    * */
    void DeleteByIds(List<Long> ids);

    /*
    * 根据id动态修改菜品数据
    * */
    @AutoFill(value = OperationType.UPDATE)
    void update(Dish dish);

    /*
    * 根据分类id查询菜品
    * */
    List<Dish> list(Dish dish);

        /**
         * 根据条件统计菜品数量
         * @param map
         * @return
         */
    Integer countByMap(Map map);
}
