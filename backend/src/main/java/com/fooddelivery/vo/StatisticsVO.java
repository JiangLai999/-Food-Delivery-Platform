package com.fooddelivery.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 统计数据VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsVO implements Serializable {

    /**
     * 今日订单数
     */
    private Long todayOrderCount;

    /**
     * 今日销售额
     */
    private BigDecimal todaySales;

    /**
     * 总订单数
     */
    private Long totalOrderCount;

    /**
     * 总销售额
     */
    private BigDecimal totalSales;

    /**
     * 用户总数
     */
    private Long totalUserCount;

    /**
     * 商家总数
     */
    private Long totalMerchantCount;

    /**
     * 活跃商家数
     */
    private Long activeMerchantCount;
}
