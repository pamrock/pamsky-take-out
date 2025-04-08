package com.sky.service;

import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;

import javax.servlet.http.HttpServletResponse;
import java.time.LocalDate;

public interface ReportService {

    /*
    * 统计指定时间区间内的营业额数据
    * */
    TurnoverReportVO getTurnoverStatistics  (LocalDate startDate, LocalDate endDate);

    /*
     * 统计指定时间区间内的用户数据
     * */
    UserReportVO getUserStatistics  (LocalDate startDate, LocalDate endDate);

    /*
    * 统计指定时间区间内的订单数据
    * */
    OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end);

    /*
    * 查询销量排名top10
    * */
    SalesTop10ReportVO getSalesTop10RePort(LocalDate begin, LocalDate end);

    /*
    * 导出运营数据报表
    * */
    void exportBussinessData(HttpServletResponse response);
}
