package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
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
    @Autowired
    WorkspaceService workspaceService;
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

    /*
    * 导出运营数据报表
    * */
    public void exportBussinessData(HttpServletResponse response) {
        //查询数据库，获取营业数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().plusDays(1);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));

        //通过POI将数据写入到excel中
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        //基于模板文件创建一个新的excel文件
        try {
            XSSFWorkbook excel = new XSSFWorkbook(in);
            XSSFSheet sheet = excel.getSheet("Sheet1");

            sheet.getRow(1).getCell(1).setCellValue("时间：" + dateBegin + "至" + dateEnd);
            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            for (int i = 0; i < 30; i++) {
                LocalDate date = dateBegin.plusDays(i);
                //查询某一天的营业数据
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            //通过输出流将excel文件下载到客户端浏览器
            ServletOutputStream out = response.getOutputStream();
            excel.write(out);

            out.close();
            excel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
