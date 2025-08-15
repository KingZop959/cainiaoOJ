package com.liyue.cainiaoojcodesandbox.utils;

import com.liyue.cainiaoojcodesandbox.docker.CDockerSandBox;
import com.liyue.cainiaoojcodesandbox.docker.CppDockerSandBox;
import com.liyue.cainiaoojcodesandbox.docker.JavaDockerSandBox;
import com.liyue.cainiaoojcodesandbox.docker.PythonDockerSandBox;
import com.yupi.yuojcodesandbox.docker.*;
import com.liyue.cainiaoojcodesandbox.model.ExecuteCodeRequest;
import com.liyue.cainiaoojcodesandbox.model.ExecuteCodeResponse;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class SandboxManger {
    @Resource
    private JavaDockerSandBox javaDockerSandBox;
    @Resource
    private PythonDockerSandBox pythonDockerSandBox;
    @Resource
    private CppDockerSandBox cppDockerSandBox;
    @Resource
    private CDockerSandBox cDockerSandBox;
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest){
        String language = executeCodeRequest.getLanguage();
        if (language.equals("java")){
            return javaDockerSandBox.executeCode(executeCodeRequest);
        }else if (language.equals("python")){
            return pythonDockerSandBox.executeCode(executeCodeRequest);
        }else if (language.equals("c")){
            return cDockerSandBox.executeCode(executeCodeRequest);
        }else {
            return cppDockerSandBox.executeCode(executeCodeRequest);
        }
    }
}
