package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @ClassName ReportServiceImpl
 * @Description 统计数据实现
 * @Author 12459
 * @Date 2025/3/21 14:11
 **/
@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    /*
    * 营业额统计
    * */
    public TurnoverReportVO getTurnoverStatistics(LocalDate startDate, LocalDate endDate) {

        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(startDate);
        while (!startDate.equals(endDate)) {
            startDate = startDate.plusDays(1);
            dateList.add(startDate);
        }

        //存放每天的营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime begin = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", begin);
            map.put("end", end);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderMapper.sumByMap(map);
            turnover = turnover == null ? 0 : turnover;
            turnoverList.add(turnover);
        }

        return TurnoverReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /*
    * 统计指定时间区间内的用户数据
    * */
    public UserReportVO getUserStatistics(LocalDate startDate, LocalDate endDate) {
        //存放从开始到结束之间每一天对应的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(startDate);
        while (!startDate.equals(endDate)) {
            startDate = startDate.plusDays(1);
            dateList.add(startDate);
        }

        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate date : dateList) {
            LocalDateTime begin = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();

            map.put("begin", begin);
            Integer totalUser = userMapper.sumUserByMap(map);
            totalUser = totalUser == null ? 0 : totalUser;
            totalUserList.add(totalUser);

            map.put("end", end);
            Integer newUser = userMapper.sumUserByMap(map);
            newUser = newUser == null ? 0 : newUser;
            newUserList.add(newUser);

        }

        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    /*
    * 统计指定时间区间内的订单数据
    * */
    public OrderReportVO getOrderStatistics(LocalDate startDate, LocalDate endDate) {
        //存放从开始到结束之间每一天对应的日期
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(startDate);
        while (!startDate.equals(endDate)) {
            startDate = startDate.plusDays(1);
            dateList.add(startDate);
        }

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        Integer orderCountSum = 0;
        Integer validOrderCountSum = 0;

        for (LocalDate date : dateList) {
            LocalDateTime begin = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime end = LocalDateTime.of(date, LocalTime.MAX);

            Map map = new HashMap();
            map.put("begin", begin);
            map.put("end", end);

            Integer orderCount =  orderMapper.sumOrderCountByMap(map);
            orderCount = orderCount == null ? 0 : orderCount;
            Integer validOrderCount =  orderMapper.sumValidOrderCountByMap(map);
            validOrderCount = validOrderCount == null ? 0 : validOrderCount;
            orderCountSum += orderCount;
            validOrderCountSum += validOrderCount;

            validOrderCountList.add(validOrderCount);
            orderCountList.add(orderCount);

        }
        Double orderCompletionRate = (double) validOrderCountSum / orderCountSum;

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .orderCompletionRate(orderCompletionRate)
                .totalOrderCount(orderCountSum)


                .validOrderCount(validOrderCountSum)
                .build();
    }

    /*
    * 查询销量排名top10
    * */
    public SalesTop10ReportVO getSalesTop10RePort(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> sealsTop10 = orderMapper.getSealsTop10(beginTime, endTime);

        List<String> names = sealsTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        String name = StringUtils.join(names, ",");

        List<Integer> numbers = sealsTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());
        String number = StringUtils.join(numbers, ",");
        return SalesTop10ReportVO.builder()
                .nameList(name)
                .numberList(number)
                .build();
    }
}
