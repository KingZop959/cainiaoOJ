package com.yupi.yuojbackendjudgeservice.judge;

import cn.hutool.json.JSONUtil;
import com.google.gson.Gson;
import com.yupi.yuojbackendcommon.common.ErrorCode;
import com.yupi.yuojbackendcommon.exception.BusinessException;
import com.yupi.yuojbackendjudgeservice.judge.codesandbox.CodeSandbox;
import com.yupi.yuojbackendjudgeservice.judge.codesandbox.CodeSandboxFactory;
import com.yupi.yuojbackendjudgeservice.judge.codesandbox.CodeSandboxProxy;
import com.yupi.yuojbackendjudgeservice.judge.strategy.JudgeContext;
import com.yupi.yuojbackendjudgeservice.rabbitmq.MyMessageProducer;
import com.yupi.yuojbackendmodel.model.codesandbox.ExecuteCodeRequest;
import com.yupi.yuojbackendmodel.model.codesandbox.ExecuteCodeResponse;
import com.yupi.yuojbackendmodel.model.codesandbox.JudgeInfo;
import com.yupi.yuojbackendmodel.model.dto.question.JudgeCase;
import com.yupi.yuojbackendmodel.model.entity.Question;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmit;
import com.yupi.yuojbackendmodel.model.enums.QuestionSubmitStatusEnum;

import com.yupi.yuojbackendmodel.model.vo.QuestionSubmitSolutionVO;
import com.yupi.yuojbackendserviceclient.service.QuestionFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class JudgeServiceImpl implements JudgeService{

    @Resource
    private QuestionFeignClient questionFeignClient;

    @Resource
    private JudgeManager judgeManager;

    @Value("remote")
    private String type;

    @Resource
    private MyMessageProducer myMessageProducer;
    @Resource
    private Gson Gson = new Gson();

    @Override
    public QuestionSubmit doJudge(long questionSubmitId) {
        /// 1)传入题目的提交ID，获取到对应的题目，提交信息(包括代码 编程语言等)
        QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        if (questionSubmit==null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"提交信息不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionFeignClient.getQuestionById(questionId);
        if (question==null){
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,"题目不存在");
        }
        /// 2)如果题目提交状态不为等待中，就不用重复执行了
        if(!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())){
            throw new BusinessException(ErrorCode.OPERATION_ERROR,"题目正在判题中");
        }
        /// 3）如果是等待中，可以进行判题，必须先修改状态为'判题中'，防止重复执行，也能让用户及时看状态
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"数据库更新错误");
        }
        /// 4)调用沙箱 获取到执行结果
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        CodeSandboxProxy codeSandboxProxy = new CodeSandboxProxy(codeSandbox);
        String code = questionSubmit.getCode();
        String language = questionSubmit.getLanguage();
        String judgeCaseStr = question.getJudgeCase();
        List<JudgeCase> judgeCaseList = JSONUtil.toList(judgeCaseStr, JudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(JudgeCase::getInput).collect(Collectors.toList());
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse =  codeSandboxProxy.executeCode(executeCodeRequest);
        /// 5)根据沙箱的执行结果，设置题目的判题状态和信息
        /// 如果我们有多个判题策略了，那么我们现在要选择一个策略
        /// 选择方式 如果全在这里写if else ，如果有是个策略就要写十次
        /// 不好维护 那么用一个类似工厂模式 专门返回策略
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setJudgeInfo(executeCodeResponse.getJudgeInfo());
        judgeContext.setInputList(inputList);
        judgeContext.setOutputList(executeCodeResponse.getOutputList());
        judgeContext.setJudgeCaseList(judgeCaseList);
        System.out.println("judgeCaseList:"+judgeContext.getJudgeCaseList());
        judgeContext.setQuestion(question);
        judgeContext.setQuestionSubmit(questionSubmit);
        JudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);
        /// 修改了一下逻辑 如果判题机返回了信息 那么 说明执行过程就有问题了 那么把判题信息改为判题机的问题
        if (executeCodeResponse.getMessage()!=null){
            judgeInfo.setMessage(executeCodeResponse.getMessage());
        }
        /// 判题结束后 修改数据库中的判题结果
        questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        /// 因为是修改数据库 所以 对象得转为Json字符串
        String judgeInfoResult = JSONUtil.toJsonStr(judgeInfo);
        questionSubmitUpdate.setJudgeInfo(judgeInfoResult);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCEED.getValue());
        questionSubmitUpdate.setQuestionId(questionId);
        update = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!update){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"数据库更新错误");
        }
        QuestionSubmit questionSubmitResult = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        QuestionSubmitSolutionVO questionSubmitSolutionVO = new QuestionSubmitSolutionVO();
        questionSubmitSolutionVO.setQuestionSubmit(questionSubmitResult);
        questionSubmitSolutionVO.setQuestion(question);
        myMessageProducer.sendMessage("ai_exchange","ai_submit", Gson.toJson(questionSubmitSolutionVO));
        return questionSubmitResult;
    }
}
