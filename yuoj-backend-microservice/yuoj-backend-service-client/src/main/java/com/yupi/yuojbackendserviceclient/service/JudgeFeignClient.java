package com.yupi.yuojbackendserviceclient.service;


import com.yupi.yuojbackendmodel.model.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 想一想判题服务的流程
 * 1)传入题目的提交ID，获取到对应的题目，提交信息(包括代码 编程语言等)
 * 2)调用沙箱 获取到执行结果
 * 3)根据沙箱的执行结果，设置题目的判题状态和信息
 */
@FeignClient(name = "yuoj-backend-judge-service",path = "/api/judge/inner")
public interface JudgeFeignClient {
    @PostMapping("/do")
    QuestionSubmit doJudge(@RequestParam("questionSubmitId") long questionSubmitId);
}
