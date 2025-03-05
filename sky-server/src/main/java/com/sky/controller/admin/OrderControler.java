package com.sky.controller.admin;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    private OrderService orderService;

    /*
    * 根据条件搜索订单
    * */
    @GetMapping("/conditionSearch")
    @ApiOperation("根据条件搜索订单")
    public Result<PageResult> conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("根据条件搜索订单: {}", ordersPageQueryDTO);
        PageResult pageResult = orderService.conditionSearch(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    /*
    * 各个状态的订单数量统计
    * */
    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statistics() {
        log.info("各个状态的订单数量统计");
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }

    /*
    * 查询订单详情
    * */
    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> details(@PathVariable Long id) {
        log.info("查询订单详情: {}", id);
        OrderVO orderVO = orderService.detail(id);
        return Result.success(orderVO);
    }

    /*
    * 接单
    * */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("接单");
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    /*
    * 拒单
    * */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) throws Exception {
        log.info("拒单: {}", ordersRejectionDTO);
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    /*
    * 取消订单
    * */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) throws Exception {
        log.info("取消订单: {}", ordersCancelDTO);
        orderService.cancel(ordersCancelDTO);
        return Result.success();
    }

    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result delivery(@PathVariable Long id) {
        log.info("派送订单: {}", id);
        orderService.delivery(id);
        return Result.success();
    }

    /*
    * 完成订单
    * */
    @PutMapping("/compelete/{id}")
    @ApiOperation("完成订单")
    public Result compelete(@PathVariable Long id) {
        log.info("完成订单: {}",id);
        orderService.compelete(id);
        return Result.success();
    }
}
