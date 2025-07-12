package com.yupi.yuojbackendserviceclient.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yuojbackendmodel.model.entity.Question;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


import javax.servlet.http.HttpServletRequest;

/**
* @author zly
* @description 针对表【question(问题)】的数据库操作Service
* @createDate 2025-04-02 15:07:57
*/
@FeignClient(name = "yuoj-backend-question-service",path = "/api/question/inner")
public interface QuestionFeignClient{

    @GetMapping("/question/id")
    Question getQuestionById(@RequestParam("questionId") long questionId);

    @GetMapping("/question_submit/get/id")
    QuestionSubmit getQuestionSubmitById(@RequestParam("questionSubmitId") long questionSubmitId);

    @PostMapping("/question_submit/update")
    boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit);
}
