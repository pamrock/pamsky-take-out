package com.sky.controller.user;

import com.sky.entity.Category;
import com.sky.result.Result;
import com.sky.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @ClassName CategoryController
 * @Description 用户端分类相关控制
 * @Author 12459
 * @Date 2025/2/17 9:18
 **/
@RestController("UserCategoryController")
@Slf4j
@RequestMapping("/user/category")
@Api(tags = "分类管理相关接口")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;
    /*
    * 根据类型查询分类列表
    * */
    @GetMapping("/list")
    @ApiOperation("根据类型查询分类列表")
    public Result<List<Category>> list(Integer type) {
        log.info("根据类型查询分类列表: {}",type);
        List<Category> list = categoryService.list(type);
        return Result.success(list);
    }
}
