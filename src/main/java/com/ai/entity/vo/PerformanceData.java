package com.ai.entity.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PerformanceData {
    private String employeeId;
    private String employeeNo;
    private String name;
    private String department;
    private String position;
    private BigDecimal performanceScore;
    private BigDecimal salesAmount;
    private Integer completedOrders;
    private BigDecimal averageRating; // 平均评分
}
