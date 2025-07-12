package com.yupi.yuoj.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.yupi.yuoj.model.dto.question.JudgeCase;
import com.yupi.yuoj.model.dto.question.JudgeConfig;
import com.yupi.yuoj.judge.codesandbox.model.JudgeInfo;
import com.yupi.yuoj.model.entity.Question;
import com.yupi.yuoj.model.enums.JudgeInfoMessageEnum;

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
        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();
        List<JudgeCase> judgeCaseList = judgeContext.getJudgeCaseList();
        Question question = judgeContext.getQuestion();
        Long actualMemory = Optional.ofNullable(judgeInfo.getMemory()).orElse(0L);
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
