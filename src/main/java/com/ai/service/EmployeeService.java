package com.ai.service;

import com.ai.controller.admin.EmployeeController.EmployeeStatistics;
import com.ai.entity.Employee;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;

public interface EmployeeService {

    Page<Employee> page(Integer pageNum, Integer pageSize, String name, String department, String status);

    Employee getById(String id);

    Employee getOne(LambdaQueryWrapper<Employee> wrapper);

    String add(Employee employee);

    String update(Employee employee);

    String delete(String id);

    String updatePerformance(String id, BigDecimal performanceScore, BigDecimal salesAmount, Integer completedOrders);

    EmployeeStatistics getStatistics();
}
