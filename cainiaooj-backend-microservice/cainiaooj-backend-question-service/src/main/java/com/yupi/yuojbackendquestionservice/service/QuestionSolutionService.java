package com.yupi.yuojbackendquestionservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yuojbackendmodel.model.entity.Question;
import com.yupi.yuojbackendmodel.model.entity.QuestionSolution;

public interface QuestionSolutionService extends IService<QuestionSolution> {
    public QuestionSolution getAiSolutionByQuestionId(long questionId);
}
