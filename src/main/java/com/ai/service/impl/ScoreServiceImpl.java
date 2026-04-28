package com.ai.service.impl;

import com.ai.entity.Score;
import com.ai.mapper.ScoreMapper;
import com.ai.service.ScoreService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ScoreServiceImpl implements ScoreService {

    private final ScoreMapper scoreMapper;

    @Override
    public Page<Score> pageByEmployeeId(String employeeId, Integer pageNum, Integer pageSize) {
        Page<Score> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Score::getEmployeeId, employeeId);
        wrapper.orderByDesc(Score::getCreateTime);
        return scoreMapper.selectPage(page, wrapper);
    }

    @Override
    public List<Score> listByEmployeeId(String employeeId) {
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Score::getEmployeeId, employeeId);
        wrapper.orderByDesc(Score::getCreateTime);
        return scoreMapper.selectList(wrapper);
    }

    @Override
    public BigDecimal getAverageScoreByEmployeeId(String employeeId) {
        List<Score> scores = listByEmployeeId(employeeId);
        if (scores == null || scores.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal sum = scores.stream()
                .map(Score::getScoreValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return sum.divide(new BigDecimal(scores.size()), 2, BigDecimal.ROUND_HALF_UP);
    }

    @Override
    public void addScore(Score score) {
        score.setCreateTime(LocalDateTime.now());
        scoreMapper.insert(score);
    }
}
