package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName SetmealServiceImpl
 * @Description 套餐管理服务层实现
 * @Author 12459
 * @Date 2025/2/8 10:16
 **/
@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    SetmealMapper setmealMapper;
    @Autowired
    SetmealDishMapper setmealDishMapper;
    @Autowired
    DishMapper dishMapper;

    /*
    * 新增套餐
    * */
    @Transactional
    public void saveWithDishes(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.insert(setmeal);

        List<SetmealDish> list = setmealDTO.getSetmealDishes();
        if (list != null && list.size() > 0) {
            for (SetmealDish setmealDish : list) {
                setmealDish.setSetmealId(setmeal.getId());
            }
            setmealDishMapper.insertBranch(list);
        }
    }

    /*
    * 套餐分页查询
    * */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> voPage = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(voPage.getTotal(), voPage.getResult());
    }

    /*
    * 批量删除套餐
    * */
    @Transactional
    public void deleteBatch(List<Long> ids) {
        ids.forEach(id -> {
            Setmeal setmeal = setmealMapper.getById(id);
            if(StatusConstant.ENABLE == setmeal.getStatus()){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });
        setmealMapper.deleteByIds(ids);
        setmealDishMapper.deleteByIds(ids);
    }

    /*
    * 套餐停售起售
    * */
    public void startOrStop(Long id, Integer status) {
        //查询该套餐下的菜品是否停售
        //根据套餐id查询菜品id
        List<Long> dishIds = setmealDishMapper.getDishIdsBySetmealId(id);
        dishIds.forEach(dishId -> {
            //根据菜品id查询菜品状态
            Integer dishStatus = dishMapper.getById(dishId).getStatus();
            Setmeal setmeal = Setmeal.builder()
                    .status(status)
                    .build();
            //如要起售套餐
            if (status == StatusConstant.DISABLE) {
                if (dishStatus == StatusConstant.DISABLE) {
                    throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
                setmealMapper.update(setmeal);
            }
            //如要禁售套餐
            if (status == StatusConstant.ENABLE) {
                setmealMapper.update(setmeal);
            }

        });

    }

    /*
    * 根据id查询套餐
    * */
    public SetmealVO getById(Long id) {
        Setmeal setmeal = setmealMapper.getById(id);
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        List<SetmealDish> list = setmealDishMapper.getById(id);
        setmealVO.setSetmealDishes(list);
        return setmealVO;
    }

    /*
    * 修改套餐
    * */
    public void update(SetmealDTO setmealDTO) {
        //修改基础数据
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        //删除原有套餐的菜品数据
        List<Long> list = new ArrayList<>();
        list.add(setmeal.getId());
        setmealDishMapper.deleteByIds(list);
        //添加新数据
        if (setmealDishes != null && setmealDishes.size() > 0) {
            for (SetmealDish setmealDish : setmealDishes) {
                setmealDish.setSetmealId(setmeal.getId());

            }
            setmealDishMapper.insertBranch(setmealDishes);
        }
    }
}
