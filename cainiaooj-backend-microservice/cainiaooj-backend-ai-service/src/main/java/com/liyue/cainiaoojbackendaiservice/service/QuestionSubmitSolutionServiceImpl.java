package com.liyue.yuojbackendaiservice.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liyue.yuojbackendaiservice.app.AiClient;
import com.liyue.yuojbackendaiservice.mapper.QuestionSubmitSolutionMapper;
import com.yupi.yuojbackendmodel.model.entity.Question;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmit;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmitSolution;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class QuestionSubmitSolutionServiceImpl extends ServiceImpl<QuestionSubmitSolutionMapper, QuestionSubmitSolution> implements QuestionSubmitSolutionService {
    @Resource
    private AiClient aiClient;
    @Override
    public boolean getAiSubmitSolution(QuestionSubmit questionSubmit, Question question) {
        String aiSolution = aiClient.getAiSubmitSolution(questionSubmit,question);
        QuestionSubmitSolution questionSolution = new QuestionSubmitSolution();
        questionSolution.setQuestionSubmitId(questionSubmit.getId());
        questionSolution.setAnalysis(aiSolution);
        boolean saved = this.save(questionSolution);
        return saved;
    }


}
