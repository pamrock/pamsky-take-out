package com.sky.controller.user;

import com.sky.constant.StatusConstant;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @ClassName SetmealConrtoller
 * @Description 套餐管理相关接口
 * @Author 12459
 * @Date 2025/2/17 11:26
 **/
@RestController("userSetmealController")
@RequestMapping("/user/setmeal")
@Api(tags = "套餐管理相关接口")
@Slf4j
public class SetmealConrtoller {

    @Autowired
    private SetmealService setmealService;

    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    @Cacheable(cacheNames = "setmealCache", key = "#categoryId")
    public Result<List<Setmeal>> list(Long categoryId){
        log.info("根据分类id查询套餐");
        Setmeal setmeal = new Setmeal();
        setmeal.setCategoryId(categoryId);
        setmeal.setStatus(StatusConstant.ENABLE);
        List<Setmeal> setmeals =  setmealService.getByCategoryId(setmeal);
        return Result.success(setmeals);
    }

    /*
    * 根据套餐id查询包含的菜品
    * */
    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询包含的菜品")
    public Result<List<DishItemVO>> dishList(@PathVariable Long id){
        log.info("根据套餐id查询包含的菜品: {}", id);
        List<DishItemVO> list = setmealService.getDishesBySetmealId(id);
        return Result.success(list);
    }
}
