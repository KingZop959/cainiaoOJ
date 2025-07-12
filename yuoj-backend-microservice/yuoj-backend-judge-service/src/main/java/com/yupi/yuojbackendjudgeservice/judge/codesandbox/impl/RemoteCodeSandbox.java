package com.yupi.yuojbackendjudgeservice.judge.codesandbox.impl;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;

import com.yupi.yuojbackendcommon.common.ErrorCode;
import com.yupi.yuojbackendcommon.exception.BusinessException;
import com.yupi.yuojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.yupi.yuojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.yupi.yuojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import org.apache.commons.lang3.StringUtils;

/**
 * 远程代码沙箱-- 实际去调用的沙箱
 */
public class RemoteCodeSandbox implements CodeSandbox {
    private static final String AUTH_REQUEST_HEADER = "auth";
    private static final String AUTH_REQUEST_KEY = "secretKey";
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        System.out.println("远程代码沙箱");
        String url = "http://localhost:8090/executeCode";
        String jsonStr = JSONUtil.toJsonStr(executeCodeRequest);
        String responseStr = HttpUtil.createPost(url)
                .header(AUTH_REQUEST_HEADER,AUTH_REQUEST_KEY)
                .body(jsonStr)
                .execute()
                .body();
        if (StringUtils.isBlank(responseStr)){
            throw new BusinessException( ErrorCode.API_REQUEST_ERROR,"exexute remoteSandbox error,message="+responseStr);
        }

        return JSONUtil.toBean(responseStr,ExecuteCodeResponse.class);
    }
}
