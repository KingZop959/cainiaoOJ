package com.yupi.yuojcodesandbox.controller;

import com.yupi.yuojcodesandbox.JavaNativeCodeSandbox;
import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class MainController {

    @Resource
    private JavaNativeCodeSandbox javaNativeCodeSandbox;
    private static final String AUTH_REQUEST_HEADER = "auth";
    private static final String AUTH_REQUEST_KEY = "secretKey";
    /**
     * 其实这样直接暴露接口到公网是不安全的
     * 我们提高接口的安全性有两种方法：
     * 1.如果这个服务是一个内部服务，只有内部的才能调用，那么我们的调用方和被调用方可以商量一个字符串(要加密的)
     * 这种方法很常用--很简单--但是只适用于内部服务
     *
     */
    @PostMapping("/executeCode")
    public ExecuteCodeResponse executeCode(@RequestBody ExecuteCodeRequest executeCodeRequest, HttpServletRequest httpServletRequest,
                                           HttpServletResponse httpServletResponse){
        String authHeader = httpServletRequest.getHeader(AUTH_REQUEST_HEADER);
        if (!AUTH_REQUEST_KEY.equals(authHeader)){
            httpServletResponse.setStatus(403);
            return null;
        }
        if (executeCodeRequest==null){
            throw new RuntimeException("请求参数为空");
        }
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
        return executeCodeResponse;
    }
}
