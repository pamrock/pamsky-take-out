package com.sky.controller.admin;

import com.sky.dto.OrdersDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @ClassName OrderControler
 * @Description 订单控制相关接口
 * @Author 12459
 * @Date 2025/3/3 11:47
 **/
@RestController
@RequestMapping("/admin/order")
@Slf4j
public class OrderControler {

    /*
    * 根据条件搜索订单
    * */
    @GetMapping("/conditionSearch")
    @ApiOperation("根据条件搜索订单")
    public Result<PageResult> conditionSearch(OrdersDTO ordersDTO) {
        log.info("根据条件搜索订单: {}", ordersDTO);

        return null;
    }
}
