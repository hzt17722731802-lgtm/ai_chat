package com.ai.service.impl;

import com.ai.controller.admin.EmployeeController.EmployeeStatistics;
import com.ai.entity.Employee;
import com.ai.mapper.EmployeeMapper;
import com.ai.service.EmployeeService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;

    @Override
    public Page<Employee> page(Integer pageNum, Integer pageSize, String name, String department, String status) {
        Page<Employee> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        
        if (name != null && !name.isBlank()) {
            wrapper.like(Employee::getName, name);
        }
        if (department != null && !department.isBlank()) {
            wrapper.eq(Employee::getDepartment, department);
        }
        if (status != null && !status.isBlank()) {
            wrapper.eq(Employee::getStatus, status);
        }
        
        wrapper.orderByDesc(Employee::getCreateTime);
        return employeeMapper.selectPage(page, wrapper);
    }

    @Override
    public Employee getById(String id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new RuntimeException("员工不存在");
        }
        return employee;
    }

    @Override
    public Employee getOne(LambdaQueryWrapper<Employee> wrapper) {
        return employeeMapper.selectOne(wrapper);
    }

    @Override
    public String add(Employee employee) {
        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        if (employee.getStatus() == null) {
            employee.setStatus("ACTIVE");
        }
        if (employee.getPerformanceScore() == null) {
            employee.setPerformanceScore(BigDecimal.ZERO);
        }
        if (employee.getSalesAmount() == null) {
            employee.setSalesAmount(BigDecimal.ZERO);
        }
        if (employee.getCompletedOrders() == null) {
            employee.setCompletedOrders(0);
        }
        employeeMapper.insert(employee);
        return "添加成功";
    }

    @Override
    public String update(Employee employee) {
        Employee existingEmployee = employeeMapper.selectById(employee.getId());
        if (existingEmployee == null) {
            throw new RuntimeException("员工不存在");
        }
        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.updateById(employee);
        return "更新成功";
    }

    @Override
    public String delete(String id) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new RuntimeException("员工不存在");
        }
        employeeMapper.deleteById(id);
        return "删除成功";
    }

    @Override
    public String updatePerformance(String id, BigDecimal performanceScore, BigDecimal salesAmount, Integer completedOrders) {
        Employee employee = employeeMapper.selectById(id);
        if (employee == null) {
            throw new RuntimeException("员工不存在");
        }
        
        if (performanceScore != null) {
            employee.setPerformanceScore(performanceScore);
        }
        if (salesAmount != null) {
            employee.setSalesAmount(salesAmount);
        }
        if (completedOrders != null) {
            employee.setCompletedOrders(completedOrders);
        }
        
        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.updateById(employee);
        return "业绩更新成功";
    }

    @Override
    public EmployeeStatistics getStatistics() {
        LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Employee::getStatus, "ACTIVE");
        List<Employee> employees = employeeMapper.selectList(wrapper);
        
        EmployeeStatistics statistics = new EmployeeStatistics();
        statistics.setTotalEmployees(employees.size());
        
        if (!employees.isEmpty()) {
            double avgPerformance = employees.stream()
                    .mapToDouble(e -> e.getPerformanceScore().doubleValue())
                    .average()
                    .orElse(0);
            statistics.setAvgPerformanceScore(BigDecimal.valueOf(avgPerformance));
            
            BigDecimal totalSales = employees.stream()
                    .map(Employee::getSalesAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            statistics.setTotalSalesAmount(totalSales);
            
            int totalOrders = employees.stream()
                    .mapToInt(Employee::getCompletedOrders)
                    .sum();
            statistics.setTotalCompletedOrders(totalOrders);
        } else {
            statistics.setAvgPerformanceScore(BigDecimal.ZERO);
            statistics.setTotalSalesAmount(BigDecimal.ZERO);
            statistics.setTotalCompletedOrders(0);
        }
        
        return statistics;
    }
}
