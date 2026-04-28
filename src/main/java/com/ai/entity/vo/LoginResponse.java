package com.ai.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private String employeeId;
    private String employeeNo;
    private String name;
    private String department;
    private String position;
}
