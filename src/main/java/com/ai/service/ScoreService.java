package com.ai.service;

import com.ai.entity.Score;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import java.math.BigDecimal;
import java.util.List;

public interface ScoreService {

    Page<Score> pageByEmployeeId(String employeeId, Integer pageNum, Integer pageSize);

    List<Score> listByEmployeeId(String employeeId);

    BigDecimal getAverageScoreByEmployeeId(String employeeId);

    void addScore(Score score);
}
