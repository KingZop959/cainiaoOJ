package com.yupi.yuojbackendjudgeservice.judge.strategy;


import com.yupi.yuojbackendmodel.model.codesandbox.JudgeInfo;
import com.yupi.yuojbackendmodel.model.dto.question.JudgeCase;
import com.yupi.yuojbackendmodel.model.entity.Question;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;

/**
 * 上下文
 * 用于定义在策略中传递的参数
 * 就根据这个context来选择使用什么策略
 */
@Data
public class JudgeContext {

    private JudgeInfo judgeInfo;
    private List<String> inputList;
    private List<String> outputList;
    private List<JudgeCase> judgeCaseList;
    private QuestionSubmit questionSubmit;
    private Question question;
}
