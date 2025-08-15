package com.liyue.cainiaoojcodesandbox.utils;

import com.liyue.cainiaoojcodesandbox.model.ExecuteCodeResponse;
import com.liyue.cainiaoojcodesandbox.model.JudgeInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;

/**
 * 全局异常处理器
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(RuntimeException.class)
    private ExecuteCodeResponse runtimeExceptionHandler(RuntimeException e){
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage("程序执行异常:"+e.getMessage());
        //2表示代码沙箱错误  3是用户的代码执行发生错误
        executeCodeResponse.setStatus(2);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage("");
        judgeInfo.setMemory(0L);
        judgeInfo.setTime(0L);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;

    }
}
