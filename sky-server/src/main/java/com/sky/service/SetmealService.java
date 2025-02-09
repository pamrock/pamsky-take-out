package com.sky.service;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {

    /*
    * 新增套餐
    * */
    void saveWithDishes(SetmealDTO setmealDTO);

/*
* 套餐分页查询
* */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /*
    * 批量删除套餐
    * */
    void deleteBatch(List<Long> ids);

    /*
    * 套餐停售起售
    * */
    void startOrStop(Long id, Integer status);

    /*
    * 根据id查询套餐
    * */
    SetmealVO getById(Long id);

    /*
    * 修改套餐
    * */
    void update(SetmealDTO setmealDTO);
}
