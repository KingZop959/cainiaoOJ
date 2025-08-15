package com.yupi.yuojbackendquestionservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yupi.yuojbackendmodel.model.entity.QuestionSolution;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmitSolution;
import com.yupi.yuojbackendquestionservice.mapper.QuestionSolutionMapper;
import com.yupi.yuojbackendquestionservice.mapper.QuestionSubmitSolutionMapper;
import com.yupi.yuojbackendquestionservice.service.QuestionSolutionService;
import com.yupi.yuojbackendquestionservice.service.QuestionSubmitSolutionService;
import org.springframework.stereotype.Service;

@Service
public class QuestionSubmitSolutionServiceImpl extends ServiceImpl<QuestionSubmitSolutionMapper, QuestionSubmitSolution> implements QuestionSubmitSolutionService {


    @Override
    public QuestionSubmitSolution getAiSubmitSolutionByQuestionId(long questionSubmitId) {
        LambdaQueryWrapper<QuestionSubmitSolution> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(QuestionSubmitSolution::getQuestionSubmitId,questionSubmitId);
        QuestionSubmitSolution one = this.getOne(lambdaQueryWrapper);
        return one;
    }
}
