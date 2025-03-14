package com.sky.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.OrderBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.properties.WeChatProperties;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.HttpClientUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import com.sky.websocket.WebSocketServer;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName OrderServiceImpl
 * @Description 用户下单实现
 * @Author 12459
 * @Date 2025/2/24 16:17
 **/
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMaper shoppingCartMaper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WebSocketServer webSocketServer;

    @Value("${sky.shop.address}")
    private String shopAddress;
    @Value("${sky.baidu.ak}")
    private String ak;

    /*
    * 用户下单
    * */
    @Transactional
    public OrderSubmitVO submitOreder(OrdersSubmitDTO ordersSubmitDTO) {
        //处理业务异常（地址簿为空，购物车数据为空）
        AddressBook addressBook = addressBookMapper.getById(Math.toIntExact(ordersSubmitDTO.getAddressBookId()));
        if(addressBook == null){
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        //检查是否超出配送范围
        checkOutOfRange(addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> cartList = shoppingCartMaper.list(shoppingCart);
        if (cartList == null || cartList.size() == 0) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //向订单表插入1条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setPhone(addressBook.getPhone());
        orders.setConsignee(addressBook.getConsignee());
        orders.setUserId(BaseContext.getCurrentId());
        orders.setAddress(addressBook.getDetail());
        orderMapper.insert(orders);

        //向订单明细表插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<OrderDetail>();

        for (ShoppingCart cart : cartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart, orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
            
        }

        orderDetailMapper.insertBatch(orderDetailList);

        //清空当前用户的购物车数据
        shoppingCartMaper.deleteByUserId(BaseContext.getCurrentId());

        //封装VO返回结果
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderAmount(orders.getAmount())
                .orderNumber(orders.getNumber())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        //通过websocket向客户端推送提醒 type, orderId, content
        Map map = new HashMap();
        map.put("type", 1);
        map.put("orderId", ordersDB.getId());
        map.put("content", "订单号： " + outTradeNo);

        String json = JSONObject.toJSONString(map);
        webSocketServer.sendToAllClient(json);
    }

    /*
    * 查询历史订单
    * */
    public PageResult pageQuery4User(int page, int pageSize, Integer status) {

        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        PageHelper.startPage(page, pageSize);
        Page<Orders> pages = orderMapper.pageQuery(ordersPageQueryDTO);

        return null;
    }

    /*
    * 查询订单详情
    * */
    public OrderVO detail(Long id) {
        Orders orders = orderMapper.getById(id);
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);

        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);
        orderVO.setOrderDetailList(orderDetails);
        return orderVO;
    }

    /*
    * 取消订单
    * */
    public void cancelById(Long id) throws Exception {
//        订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        Orders orders = orderMapper.getById(id);
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        if (orders.getStatus() > 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders1 = new Orders();
        orders1.setId(id);
        // 订单处于待接单状态下取消，需要进行退款
        if (orders.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            //调用微信支付接口
            weChatPayUtil.refund(
                    orders.getNumber(),//商户订单号
                    orders.getNumber(),//商户退款单号
                    new BigDecimal(0.01),//退款金额，单位 元
                    new BigDecimal(0.01)//原订单金额
            );
            orders1.setPayStatus(Orders.REFUND);

        }

        //更新订单状态、取消原因、取消时间
        orders1.setStatus(Orders.CANCELLED);
        orders1.setCancelReason("用户取消");
        orders1.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders1);
    }

    /*
    * 再来一单
    * */
    public void repetition(Long id) {
        //根据订单id查询订单详情
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x ->{
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(x, shoppingCart, "id");

            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());

            return shoppingCart;
        }).collect(Collectors.toList());

        shoppingCartMaper.insertBatch(shoppingCartList);
    }

    /*
    * 根据条件搜索订单
    * */
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<Orders> pages = orderMapper.pageQuery(ordersPageQueryDTO);

        //部分订单状态需要额外返回订单信息，将Orders转化为OrderVO
        List<OrderVO> list = getOrderVOList(pages);
        return new PageResult(pages.getTotal(), list);
    }

    private List<OrderVO> getOrderVOList(Page<Orders> pages) {
        List<OrderVO> orderVOList = new ArrayList<>();
        List<Orders> ordersList = pages.getResult();
        for (Orders orders : ordersList) {
            //将相同字段复制到ordervo
            OrderVO orderVO = new OrderVO();
            BeanUtils.copyProperties(orders, orderVO);

            //将订单菜品信息封装到ordervo中，并添加到ordervolist
            orderVO.setOrderDishes(getOrderDishes(orders));
            orderVOList.add(orderVO);
        }
        return orderVOList;
    }

    /*
    * 根据orders查询订单菜品信息
    * */
    private String getOrderDishes(Orders orders) {

        //查询订单菜品详情信息（订单中的菜品和数量）
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());

        //将每一条订单中的菜品信息拼接为字符串，格式：菜品名称*数量
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        //将该订单的所有菜品信息拼接在一起
        return String.join("", orderDishList);
    }

    /*
    * 各个状态的订单数量统计
    * */
    public OrderStatisticsVO statistics() {
        Integer confirmed = orderMapper.countByStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.countByStatus(Orders.DELIVERY_IN_PROGRESS);
        Integer toBeConfirmed = orderMapper.countByStatus(Orders.TO_BE_CONFIRMED);

        OrderStatisticsVO orderStatisticsVO = OrderStatisticsVO.builder()
                .confirmed(confirmed)
                .deliveryInProgress(deliveryInProgress)
                .toBeConfirmed(toBeConfirmed)
                .build();
        return orderStatisticsVO;
    }

    /*
    * 接单
    * */
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(ordersConfirmDTO.getStatus())
                .build();

        orderMapper.update(orders);
    }

    /*
    * 拒单
    * */
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) throws Exception {

        Orders orders1 = orderMapper.getById(ordersRejectionDTO.getId());
        //状态为待接单时（2）才可拒单
        if(orders1 == null || !orders1.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //如果已付款，则退款
        if(orders1.getStatus() == Orders.PAID){
            String refund = weChatPayUtil.refund(
                    orders1.getNumber(),
                    orders1.getNumber(),
                    new BigDecimal(0.01/*orders.getAmount()*/),
                    new BigDecimal(0.01)
            );
        }

        Orders orders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /*
    * 取消订单
    * */
    public void cancel(OrdersCancelDTO ordersCancelDTO) throws Exception {

        Orders orders = orderMapper.getById(ordersCancelDTO.getId());
        //如已支付，需退款

        if(orders.getPayStatus() == Orders.PAID){
            weChatPayUtil.refund(
                    orders.getNumber(),
                    orders.getNumber(),
                    new BigDecimal(0.01),
                    new BigDecimal(0.01)
            );
        }

        Orders orders1 = Orders.builder()
                .id(ordersCancelDTO.getId())
                .status(Orders.CANCELLED)
                .cancelTime(LocalDateTime.now())
                .cancelReason(ordersCancelDTO.getCancelReason())
                .build();

        orderMapper.update(orders1);
    }

    /*
    * 派送订单
    * */
    public void delivery(Long id) {
        Orders orders = orderMapper.getById(id);

        //校验订单是否存在且状态为已结单
        if(orders == null || orders.getStatus() != Orders.CONFIRMED){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders1 = Orders.builder()
                .id(orders.getId())
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();

        orderMapper.update(orders1);
    }


    /*
    * 完成订单
    * */
    public void compelete(Long id) {
        Orders orders = orderMapper.getById(id);

        //校验订单是否存在且状态为已结单
        if(orders == null || orders.getStatus() != Orders.DELIVERY_IN_PROGRESS){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders1 = Orders.builder()
                .id(orders.getId())
                .status(Orders.COMPLETED)
                .deliveryTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders1);
    }

    /*
    * 检查是否超出配送范围
    * */
    private void checkOutOfRange(String address){

        Map map = new HashMap();
        map.put("address", shopAddress);
        map.put("ak", ak);
        map.put("output", "json");

        //获取店铺坐标
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);
        JSONObject jsonObject = JSONObject.parseObject(shopCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("店铺地址解析失败");
        }
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        Float flng = location.getFloat("lng");
        Float flat = location.getFloat("lat");
        String lng = String.format("%.5f", flng);
        String lat = String.format("%.5f", flat);
        String shopLocation = lat + "," + lng;

        //获取用户坐标
        map.put("address", address);
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);
        jsonObject = JSONObject.parseObject(userCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("收获地址解析失败");
        }
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        flng = location.getFloat("lng");
        flat = location.getFloat("lat");
        lng = String.format("%.5f", flng);
        lat = String.format("%.5f", flat);
        String userLocation = lat + "," + lng;

        //规划配送路线
        map.clear();
        map.put("origin", shopLocation);
        map.put("destination", userLocation);
        map.put("ak", ak);
        String json = HttpClientUtil.doGet("https://api.map.baidu.com/direction/v2/riding", map);
        jsonObject = JSONObject.parseObject(json);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("配送路线规划失败");
        }

        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = result.getJSONArray("routes");
        Integer distance = ((JSONObject)jsonArray.get(0)).getInteger("distance");
        if(distance > 5000){
            throw new OrderBusinessException("超出配送范围");
        }

    }

    /*
    * 客户催单
    * */
    public void reminder(Long id) {
        Orders orders = orderMapper.getById(id);

        //校验订单是否存在且状态为已结单
        if(orders == null || orders.getStatus() != Orders.DELIVERY_IN_PROGRESS){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Map map = new HashMap();
        map.put("type", 2);//1:来单提醒， 2：客户催单
        map.put("orderId", id);
        map.put("content", "订单号：" + orders.getNumber());

        webSocketServer.sendToAllClient(JSONObject.toJSONString(map));
    }
}
