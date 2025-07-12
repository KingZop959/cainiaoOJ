package com.yupi.yuojbackendjudgeservice.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.yupi.yuojbackendmodel.model.codesandbox.JudgeInfo;
import com.yupi.yuojbackendmodel.model.dto.question.JudgeCase;
import com.yupi.yuojbackendmodel.model.dto.question.JudgeConfig;
import com.yupi.yuojbackendmodel.model.entity.Question;
import com.yupi.yuojbackendmodel.model.enums.JudgeInfoMessageEnum;


import java.util.List;
import java.util.Optional;

/**
 * java程序得判题逻辑
 *
 */
public class JavaLanguageJudgeStrategy implements JudgeStrategy{
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        ///  这个是实际执行的judgeInfo
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        if (judgeInfo==null){
            System.out.println("judgeinfo是null");
        }
        List<String> inputList = judgeContext.getInputList();
        if (inputList==null){
            System.out.println("inputList是null");
        }
        List<String> outputList = judgeContext.getOutputList();
        if (outputList==null){
            System.out.println("outputList是null");
        }
        List<JudgeCase> judgeCaseList = judgeContext.getJudgeCaseList();
        if (judgeCaseList==null){
            System.out.println("judgeCaseList是null");
        }
        Question question = judgeContext.getQuestion();
        if (question==null){
            System.out.println("question是null");
        }
        if (judgeInfo.getMemory()==null){
            System.out.println("judgeInfo.getMemory()是null");
        }
        Long actualMemory = Optional.ofNullable(judgeInfo.getMemory()).orElse(0L);
        if (judgeInfo.getTime()==null){
            System.out.println("judgeInfo.getTime()是null");
        }
        Long actualTime = judgeInfo.getTime();


        //先给一个JudgeInfo一个初始值
        JudgeInfoMessageEnum judgeInfoMessageEnum = JudgeInfoMessageEnum.ACCEPTED;
        JudgeInfo judgeInfoResponse = new JudgeInfo();
        judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
        judgeInfoResponse.setMemory(actualMemory);
        judgeInfoResponse.setTime(actualTime);

        if (outputList.size()!=inputList.size()){
            judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
        for (int i = 0; i < judgeCaseList.size(); i++) {
            JudgeCase judgeCase = judgeCaseList.get(i);
            if (!judgeCase.getOutput().equals(outputList.get(i))){
                judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
                judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
                return judgeInfoResponse;
            }
        }
        /// 到这里是结果正确了 我们就要判断题目的限制满不满足了
        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfigObject = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);

        /// 如果是java程序会比其他程序多花10s，那我们应该限制条件多加十秒
        long JAVA_PROGRAM_TIME_COST = 10000;
        Long exceptedTimeLimit = judgeConfigObject.getTimeLimit();
        Long exceptedMemoryLimit = judgeConfigObject.getMemoryLimit();

        if (actualMemory>exceptedMemoryLimit){
            judgeInfoMessageEnum = JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
        if ((actualTime-JAVA_PROGRAM_TIME_COST)>exceptedTimeLimit){
            judgeInfoMessageEnum = JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }

        return judgeInfoResponse;
    }
}
