package com.liyue.yuojbackendaiservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yuojbackendmodel.model.entity.Question;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmit;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmitSolution;

public interface QuestionSubmitSolutionService extends IService<QuestionSubmitSolution> {
    public boolean getAiSubmitSolution(QuestionSubmit questionSubmit, Question question);
}
