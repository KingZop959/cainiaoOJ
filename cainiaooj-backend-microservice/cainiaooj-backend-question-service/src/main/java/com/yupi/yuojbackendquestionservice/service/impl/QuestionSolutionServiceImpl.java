package com.yupi.yuojbackendquestionservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuojbackendmodel.model.entity.QuestionSolution;
import com.yupi.yuojbackendquestionservice.mapper.QuestionSolutionMapper;
import com.yupi.yuojbackendquestionservice.service.QuestionSolutionService;
import org.springframework.stereotype.Service;

@Service
public class QuestionSolutionServiceImpl extends ServiceImpl<QuestionSolutionMapper, QuestionSolution> implements QuestionSolutionService {


    @Override
    public QuestionSolution getAiSolutionByQuestionId(long questionId) {
        LambdaQueryWrapper<QuestionSolution> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(QuestionSolution::getQuestionId,questionId);
        QuestionSolution questionSolution = this.getOne(lambdaQueryWrapper);
        return questionSolution;
    }
}
