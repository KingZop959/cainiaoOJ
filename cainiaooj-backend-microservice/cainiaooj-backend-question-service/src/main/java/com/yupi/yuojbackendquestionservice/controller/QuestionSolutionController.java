package com.yupi.yuojbackendquestionservice.controller;

import com.yupi.yuojbackendcommon.common.BaseResponse;
import com.yupi.yuojbackendcommon.common.ResultUtils;
import com.yupi.yuojbackendmodel.model.entity.QuestionSolution;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmitSolution;
import com.yupi.yuojbackendquestionservice.service.QuestionSolutionService;
import com.yupi.yuojbackendquestionservice.service.QuestionSubmitSolutionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


/**
 * 帖子接口
 *
 * @author <a href="https://github.com/liyupi">程序员小新</a>
 */
@RestController
@RequestMapping("/question_solution")
@Slf4j
public class QuestionSolutionController {

    @Resource
    private QuestionSolutionService questionSolutionService;
    @Resource
    private QuestionSubmitSolutionService questionSubmitSolutionService;

    // region 增删改查

    /**
     * 获取该问题Ai题解
     *
     * @param id
     * @return
     */
    @GetMapping("/getAiSolution")
    public BaseResponse<QuestionSolution> getCommentList(long id) {
        return ResultUtils.success(questionSolutionService.getAiSolutionByQuestionId(id));
    }

    /**
     * 获取该问题Ai题解
     *
     * @param id
     * @return
     */
    @GetMapping("/getAiSubmitSolution")
    public BaseResponse<QuestionSubmitSolution> getSubmitSolution(long id) {
        return ResultUtils.success(questionSubmitSolutionService.getAiSubmitSolutionByQuestionId(id));
    }


}