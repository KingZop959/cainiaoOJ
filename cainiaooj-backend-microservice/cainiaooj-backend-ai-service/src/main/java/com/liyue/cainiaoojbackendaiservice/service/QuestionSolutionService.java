package com.liyue.yuojbackendaiservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yuojbackendmodel.model.entity.Question;
import com.yupi.yuojbackendmodel.model.entity.QuestionSolution;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

public interface QuestionSolutionService extends IService<QuestionSolution> {
    public boolean getAiSolution(Question question);
}
