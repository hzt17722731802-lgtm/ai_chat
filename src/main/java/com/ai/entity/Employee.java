package com.ai.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("employee")
public class Employee {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String employeeNo;

    private String name;

    private String department;

    private String position;

    private String phone;

    private String email;

    private BigDecimal performanceScore;

    private BigDecimal salesAmount;

    private Integer completedOrders;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
