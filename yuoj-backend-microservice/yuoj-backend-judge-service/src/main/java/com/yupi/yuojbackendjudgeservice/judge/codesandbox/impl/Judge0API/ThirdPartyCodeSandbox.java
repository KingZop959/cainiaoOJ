package com.yupi.yuojbackendjudgeservice.judge.codesandbox.impl.Judge0API;


import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.yupi.yuojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.yupi.yuojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.yupi.yuojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import com.yupi.yuojbackendmodel.model.codesandbox.JudgeInfo;
import com.yupi.yuojbackendmodel.model.enums.QuestionSubmitStatusEnum;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 第三方代码沙箱--调用别人的代码沙箱
 * 我们在这个里面调用别人的实现
 * 通过这个接口就可直接整合进项目
 */
@Component
public class ThirdPartyCodeSandbox implements CodeSandbox {

    @Resource
    private RestTemplate restTemplate;
    @Value("${Judge0API.rapidApiKey}")
    private String rapidApiKey;
    @Value("${Judge0API.judge0ApiUrl}")
    private String JUDGE_API;
    @Value("${Judge0API.judge0ApiHost}")
    private String HOST;

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-RapidAPI-Key", "9394b69563mshea2505d6da7a1fdp1d2220jsnc56a605ef509");
        headers.set("X-RapidAPI-Host", "judge0-ce.p.rapidapi.com");
        ExecuteCodeResponse executeCodeResponse =new ExecuteCodeResponse();
        // 构造请求体

        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();
        Judge0Language judge0Language = Judge0Language.fromDescription(language);
        System.out.println("语言id："+judge0Language.getId());
        List<String> outputList = new ArrayList<>();
        double maxTime = 0;
        long maxMemory = 0;
        Map<String, Object> requestMap = new HashMap<>();
        String encodedCode = Base64.getEncoder().encodeToString(code.getBytes(StandardCharsets.UTF_8));
        requestMap.put("language_id", judge0Language.getId());
        requestMap.put("source_code", encodedCode);
        System.out.println("encodedCode："+encodedCode);
        String url = "https://judge0-ce.p.rapidapi.com/submissions?base64_encoded=true&wait=true";
        for (String input:inputList){
            String[] split = input.split(" ");
            String join = String.join("\n", split);
            String encodedStdin = Base64.getEncoder().encodeToString(join.getBytes(StandardCharsets.UTF_8));
            requestMap.put("stdin", encodedStdin);
            String jsonStr = JSONUtil.toJsonStr(requestMap);
            String responseStr = HttpUtil.createPost(url)
                    .header(headers)
                    .body(jsonStr)
                    .execute()
                    .body();
            Judge0Response executeMessage = JSONUtil.toBean(responseStr,Judge0Response.class);
            System.out.println(executeMessage);
            if (StrUtil.isNotBlank(executeMessage.getCompile_output())){
                executeCodeResponse.setMessage(executeMessage.getCompile_output());
                break;
            }
            if (StrUtil.isNotBlank(executeMessage.getStderr())){
                executeCodeResponse.setMessage(executeMessage.getStderr());
                break;
            }
            maxTime = Math.max(Double.parseDouble(executeMessage.getTime()),maxTime);
            maxMemory = Math.max(maxMemory,executeMessage.getMemory());
            String decoded = new String(Base64.getDecoder().decode(executeMessage.getStdout()));
            decoded = StrUtil.removeSuffix(decoded, "\n");
            decoded = StrUtil.removeSuffix(decoded, "\r");
            decoded = StrUtil.removePrefix(decoded, "\n");
            decoded = StrUtil.removePrefix(decoded, "\r");
            outputList.add(decoded);
        }
        System.out.println("outputList:"+outputList);
        if (outputList.size()!=inputList.size()){
            executeCodeResponse.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
        }
        executeCodeResponse.setOutputList(outputList);
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMemory(maxMemory);
        judgeInfo.setTime((long)(maxTime*1000));
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }
}
