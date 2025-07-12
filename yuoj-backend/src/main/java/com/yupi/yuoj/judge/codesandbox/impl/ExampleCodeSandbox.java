package com.yupi.yuoj.judge.codesandbox.impl;

import com.yupi.yuoj.judge.codesandbox.CodeSandbox;
import com.yupi.yuoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.yupi.yuoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.yupi.yuoj.judge.codesandbox.model.JudgeInfo;
import com.yupi.yuoj.model.enums.JudgeInfoMessageEnum;
import com.yupi.yuoj.model.enums.QuestionSubmitStatusEnum;

import java.util.List;

/**
 * 为了我们跑通流程的示例沙箱
 */

/**
 * 考虑一个代理模式的问题：
 * 如果我们需要在调用代码沙箱前输出请求信息到日志，代码执行结束后，输出执行信息到日志
 * 便于管理员分析
 * 难道我们要每个调用代码沙箱的时候都写一遍log.info?或者是在每个沙箱返回的时候都写一遍再沙箱的源码内?
 * 这样不好
 * 通过代理模式--这里用的是静态代理--增强CodeSandbox的能力
 * 就是一个类实现CodeSandbox的接口，并在里面有一个CodeSandBox的实例
 * 并在执行代码前后加上日志输出在返回
 * 就只写了一次，每次调用代码沙箱就使用这个代理类就好了
 * 就有点像Aop切面，在需要的地方插入代码，但是不影响源代码--因为AOP也是代理模式
 */

public class ExampleCodeSandbox implements CodeSandbox {
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(inputList);
        executeCodeResponse.setMessage("代码执行成功");
        executeCodeResponse.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(JudgeInfoMessageEnum.ACCEPTED.getText());
        judgeInfo.setMemory(100L);
        judgeInfo.setTime(100L);
        executeCodeResponse.setJudgeInfo(judgeInfo);

        return executeCodeResponse;
    }
}
