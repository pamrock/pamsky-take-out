package com.sky.service;

import com.sky.dto.OrdersSubmitDTO;
import com.sky.vo.OrderSubmitVO;

public interface OrderService {
    /*
    * 用户下单
    * */
    OrderSubmitVO submitOreder(OrdersSubmitDTO ordersSubmitDTO);
}
