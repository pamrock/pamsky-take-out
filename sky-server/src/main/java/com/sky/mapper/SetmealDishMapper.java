package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {
/*
* 根据菜品id查询对应的套餐id
* */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);

    /*
    * 根据套餐id批量插入菜品
    * */
    void insertBranch(List<SetmealDish> list);

    /*
     *批量删除套餐中的菜品数据
    * */
    void deleteByIds(List<Long> setmealIds);

/*
* 根据套餐id查询菜品id
* */
    @Select("select dish_id from setmeal_dish where setmeal_id = #{id}")
    List<Long> getDishIdsBySetmealId(Long id);

    /*
    * 根据套餐id查询菜品
    * */
    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getById(Long id);

    /*
    * 根据菜品id和套餐id查询份数
    * */
    @Select("select copies from setmeal_dish where dish_id = #{dishId} and setmeal_id = #{setmealId}")
    Integer getCopiesByDishIdAndSetmealId(Long setmealId, Long dishId);
}
