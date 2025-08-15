package com.liyue.yuojbackendaiservice.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liyue.yuojbackendaiservice.app.AiClient;
import com.liyue.yuojbackendaiservice.mapper.QuestionSolutionMapper;
import com.yupi.yuojbackendmodel.model.entity.Question;
import com.yupi.yuojbackendmodel.model.entity.QuestionSolution;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmit;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmitSolution;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class QuestionSolutionServiceImpl extends ServiceImpl<QuestionSolutionMapper, QuestionSolution> implements QuestionSolutionService{
    @Resource
    private AiClient aiClient;
    @Override
    public boolean getAiSolution(Question question) {
        String aiSolution = aiClient.getAiSolution(question);
        QuestionSolution questionSolution = new QuestionSolution();
        questionSolution.setQuestionId(question.getId());
        questionSolution.setAnswer(aiSolution);
        boolean saved = this.save(questionSolution);
        return saved;
    }

}
