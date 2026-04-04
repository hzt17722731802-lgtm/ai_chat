package com.ai.controller.admin;

import com.ai.common.Result;
import com.ai.entity.Employee;
import com.ai.service.EmployeeService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    @GetMapping("/page")
    public Result<Page<Employee>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String status) {
        
        Page<Employee> page = employeeService.page(pageNum, pageSize, name, department, status);
        return Result.success(page);
    }

    @GetMapping("/{id}")
    public Result<Employee> getById(@PathVariable String id) {
        try {
            Employee employee = employeeService.getById(id);
            return Result.success(employee);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping
    public Result<String> add(@RequestBody Employee employee) {
        try {
            String message = employeeService.add(employee);
            return Result.success(message);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping
    public Result<String> update(@RequestBody Employee employee) {
        try {
            String message = employeeService.update(employee);
            return Result.success(message);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable String id) {
        try {
            String message = employeeService.delete(id);
            return Result.success(message);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @PutMapping("/{id}/performance")
    public Result<String> updatePerformance(
            @PathVariable String id,
            @RequestParam(required = false) BigDecimal performanceScore,
            @RequestParam(required = false) BigDecimal salesAmount,
            @RequestParam(required = false) Integer completedOrders) {
        
        try {
            String message = employeeService.updatePerformance(id, performanceScore, salesAmount, completedOrders);
            return Result.success(message);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/statistics")
    public Result<EmployeeStatistics> getStatistics() {
        EmployeeStatistics statistics = employeeService.getStatistics();
        return Result.success(statistics);
    }

    @Data
    public static class EmployeeStatistics {
        private Integer totalEmployees;
        private BigDecimal avgPerformanceScore;
        private BigDecimal totalSalesAmount;
        private Integer totalCompletedOrders;
    }
}
