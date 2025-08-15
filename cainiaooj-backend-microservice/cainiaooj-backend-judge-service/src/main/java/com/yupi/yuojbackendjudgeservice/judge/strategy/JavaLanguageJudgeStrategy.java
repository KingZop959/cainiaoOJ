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
            System.out.println("答案匹配不上,因为数量就不对");
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
