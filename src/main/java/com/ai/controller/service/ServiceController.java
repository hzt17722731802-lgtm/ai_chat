package com.ai.controller.service;

import com.ai.common.Result;
import com.ai.config.JwtUtil;
import com.ai.entity.Employee;
import com.ai.entity.vo.LoginRequest;
import com.ai.entity.vo.LoginResponse;
import com.ai.entity.vo.PerformanceData;
import com.ai.service.EmployeeService;
import com.ai.service.ScoreService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RequiredArgsConstructor
@RestController
@RequestMapping("/service")
public class ServiceController {

    private final EmployeeService employeeService;
    private final ScoreService scoreService;
    private final JwtUtil jwtUtil;

    /**
     * 客服登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            // 查询员工
            LambdaQueryWrapper<Employee> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Employee::getEmployeeNo, request.getEmployeeNo());
            Employee employee = employeeService.getOne(wrapper);

            if (employee == null) {
                return Result.error("员工不存在");
            }

            // 验证密码
            if (employee.getPassword() == null || !employee.getPassword().equals(request.getPassword())) {
                return Result.error("密码错误");
            }

            // 验证状态
            if (!"ACTIVE".equals(employee.getStatus())) {
                return Result.error("账号已被禁用");
            }

            // 生成JWT令牌
            String token = jwtUtil.generateToken(employee.getId(), employee.getEmployeeNo(), employee.getName());

            // 构建响应
            LoginResponse response = new LoginResponse();
            response.setToken(token);
            response.setEmployeeId(employee.getId());
            response.setEmployeeNo(employee.getEmployeeNo());
            response.setName(employee.getName());
            response.setDepartment(employee.getDepartment());
            response.setPosition(employee.getPosition());

            return Result.success(response);
        } catch (Exception e) {
            return Result.error("登录失败: " + e.getMessage());
        }
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current")
    public Result<Employee> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        try {
            String token = extractToken(authorization);
            if (!jwtUtil.validateToken(token)) {
                return Result.error("无效的令牌");
            }

            String employeeId = jwtUtil.getUserIdFromToken(token);
            Employee employee = employeeService.getById(employeeId);

            if (employee == null) {
                return Result.error("用户不存在");
            }

            // 清除敏感信息
            employee.setPassword(null);

            return Result.success(employee);
        } catch (Exception e) {
            return Result.error("获取用户信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取绩效数据
     */
    @GetMapping("/performance")
    public Result<PerformanceData> getPerformanceData(@RequestHeader("Authorization") String authorization) {
        try {
            String token = extractToken(authorization);
            if (!jwtUtil.validateToken(token)) {
                return Result.error("无效的令牌");
            }

            String employeeId = jwtUtil.getUserIdFromToken(token);
            Employee employee = employeeService.getById(employeeId);

            if (employee == null) {
                return Result.error("用户不存在");
            }

            // 计算平均评分
            BigDecimal averageRating = scoreService.getAverageScoreByEmployeeId(employeeId);

            // 构建绩效数据
            PerformanceData performanceData = new PerformanceData();
            performanceData.setEmployeeId(employee.getId());
            performanceData.setEmployeeNo(employee.getEmployeeNo());
            performanceData.setName(employee.getName());
            performanceData.setDepartment(employee.getDepartment());
            performanceData.setPosition(employee.getPosition());
            performanceData.setPerformanceScore(employee.getPerformanceScore());
            performanceData.setSalesAmount(employee.getSalesAmount());
            performanceData.setCompletedOrders(employee.getCompletedOrders());
            performanceData.setAverageRating(averageRating);

            return Result.success(performanceData);
        } catch (Exception e) {
            return Result.error("获取绩效数据失败: " + e.getMessage());
        }
    }

    /**
     * 提取JWT令牌
     */
    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        throw new RuntimeException("无效的授权头");
    }
}
