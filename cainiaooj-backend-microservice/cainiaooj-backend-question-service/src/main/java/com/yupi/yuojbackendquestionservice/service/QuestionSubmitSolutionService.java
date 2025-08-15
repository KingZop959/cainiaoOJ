package com.yupi.yuojbackendquestionservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yuojbackendmodel.model.entity.QuestionSolution;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmitSolution;

public interface QuestionSubmitSolutionService extends IService<QuestionSubmitSolution> {
    public QuestionSubmitSolution getAiSubmitSolutionByQuestionId(long questionSubmitId);
}
