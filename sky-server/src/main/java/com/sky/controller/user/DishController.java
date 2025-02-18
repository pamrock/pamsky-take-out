package com.sky.controller.user;

import com.sky.entity.Dish;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName DishController
 * @Description 菜品管理相关接口
 * @Author 12459
 * @Date 2025/2/17 9:31
 **/
@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "菜品管理相关接口")
public class DishController {

    @Autowired
    private DishService dishService;
    /*
    * 根据分类id查询菜品
    * */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        log.info("根据分类id查询菜品");
        List<DishVO> list = dishService.listWithFlavor(categoryId);
        return Result.success(list);
    }
}
