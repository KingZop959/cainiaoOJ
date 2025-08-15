package com.yupi.yuojbackendjudgeservice.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.yupi.yuojbackendmodel.model.codesandbox.JudgeInfo;
import com.yupi.yuojbackendmodel.model.dto.question.JudgeCase;
import com.yupi.yuojbackendmodel.model.dto.question.JudgeConfig;
import com.yupi.yuojbackendmodel.model.entity.Question;
import com.yupi.yuojbackendmodel.model.enums.JudgeInfoMessageEnum;


import java.util.List;

public class DefaultJudgeStrategy implements JudgeStrategy{
    @Override
    public JudgeInfo doJudge(JudgeContext judgeContext) {
        ///  这个是实际执行的judgeInfo
        JudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();
        List<JudgeCase> judgeCaseList = judgeContext.getJudgeCaseList();
        Question question = judgeContext.getQuestion();
        Long actualMemory = judgeInfo.getMemory();
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
        String judgeConfigStr = question.getJudgeConfig();
        JudgeConfig judgeConfigObject = JSONUtil.toBean(judgeConfigStr, JudgeConfig.class);
        int specialType = judgeConfigObject.getSpecialType();
        for (int i = 0; i < judgeCaseList.size(); i++) {
            JudgeCase judgeCase = judgeCaseList.get(i);
            String output = outputList.get(i);
            String answer = judgeCase.getOutput();
            boolean flag = true;
            if (specialType==0){
                flag = answer.equals(output);
            }else if (specialType==1){
                flag = SpecialJudge.multipleAnswer(answer,output);
            }else if (specialType ==2){
                flag = SpecialJudge.noSequence(answer,output);
            }else {
                flag = SpecialJudge.multipleAnswerNoSequence(answer,output);
            }
            if (!flag){
                System.out.println("答案匹配不上,用例输出是"+answer+"代码输出是"+output);
                judgeInfoMessageEnum = JudgeInfoMessageEnum.WRONG_ANSWER;
                judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
                return judgeInfoResponse;
            }
        }
        /// 到这里是结果正确了 我们就要判断题目的限制满不满足了
        Long exceptedTimeLimit = judgeConfigObject.getTimeLimit();
        Long exceptedMemoryLimit = judgeConfigObject.getMemoryLimit();

        if (actualMemory>exceptedMemoryLimit){
            judgeInfoMessageEnum = JudgeInfoMessageEnum.MEMORY_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }
        if (actualTime>exceptedTimeLimit){
            judgeInfoMessageEnum = JudgeInfoMessageEnum.TIME_LIMIT_EXCEEDED;
            judgeInfoResponse.setMessage(judgeInfoMessageEnum.getValue());
            return judgeInfoResponse;
        }

        return judgeInfoResponse;
    }
}
