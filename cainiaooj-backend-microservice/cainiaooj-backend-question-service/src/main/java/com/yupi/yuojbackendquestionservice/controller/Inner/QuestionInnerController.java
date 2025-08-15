package com.yupi.yuojbackendquestionservice.controller.Inner;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.google.gson.Gson;
import com.yupi.yuojbackendmodel.model.entity.Question;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmit;
import com.yupi.yuojbackendquestionservice.service.QuestionService;
import com.yupi.yuojbackendquestionservice.service.QuestionSubmitService;
import com.yupi.yuojbackendserviceclient.service.QuestionFeignClient;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/inner")
public class QuestionInnerController implements QuestionFeignClient {

    @Resource
    private QuestionService questionService;
    @Resource
    private QuestionSubmitService questionSubmitService;


    @Override
    @GetMapping("/question/id")
    public Question getQuestionById(@RequestParam("questionId") long questionId){
        return questionService.getById(questionId);
    }

    @Override
    @GetMapping("/question_submit/get/id")
    public QuestionSubmit getQuestionSubmitById(@RequestParam("questionSubmitId") long questionSubmitId){
        return questionSubmitService.getById(questionSubmitId);
    }

    @Override
    @PostMapping("/question_submit/update")
    public boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit){
        String judgeInfo = questionSubmit.getJudgeInfo();
        JSONObject jsonObject = JSONUtil.parseObj(judgeInfo);
        String message = jsonObject.getStr("message");
        System.out.println("从判题得到的message是:"+message);
        System.out.println("从QuestionSubmit得到的是questionId是:"+questionSubmit.getQuestionId());
        if ("Accepted成功".equals(message)){
            /// 更新题目提交数
            LambdaUpdateWrapper<Question> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Question::getId,questionSubmit.getQuestionId())
                    .setSql("acceptedNum = acceptedNum + 1");
            questionService.update(updateWrapper);
        }
        return questionSubmitService.updateById(questionSubmit);
    }

}
