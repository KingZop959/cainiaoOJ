package com.yupi.yuojbackendjudgeservice.judge.strategy;

import cn.hutool.json.JSONUtil;
import com.yupi.yuojbackendmodel.model.codesandbox.JudgeInfo;
import com.yupi.yuojbackendmodel.model.dto.question.JudgeCase;
import com.yupi.yuojbackendmodel.model.dto.question.JudgeConfig;
import com.yupi.yuojbackendmodel.model.entity.Question;
import com.yupi.yuojbackendmodel.model.enums.JudgeInfoMessageEnum;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
public class SpecialJudge{
    public static boolean multipleAnswer(String answers,String output){
        String[] split = answers.split(";");
        for (String answer:split){
            if (answer.equals(output)){
                return true;
            }
        }
        return false;
    }
    public static boolean noSequence(String answer,String output){
        if (answer.length()!=output.length()){
            return false;
        }
        String[] split = answer.split(" ");
        String[] split2 = output.split(" ");
        Arrays.sort(split);
        Arrays.sort(split2);
        String join = String.join("", split);
        String join2 = String.join("", split2);
        if (!join2.equals(join)){
            return false;
        }
        return true;
    }
    public static boolean multipleAnswerNoSequence(String answers,String output){
        String[] split = answers.split(";");
        for (String answer:split){
            if (noSequence(answer,output)){
                return true;
            }
        }
        return false;
    }

}
