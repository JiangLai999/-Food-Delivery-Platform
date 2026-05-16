package com.fooddelivery.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * ECharts数据VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataVO implements Serializable {

    /**
     * X轴数据（如日期、分类名称等）
     */
    private List<String> xAxisData;

    /**
     * Y轴数据（如订单量、销售额等）
     */
    private List<Object> yAxisData;

    /**
     * 系列名称
     */
    private String seriesName;
}
