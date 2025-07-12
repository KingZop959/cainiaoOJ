package com.yupi.yuoj.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yupi.yuoj.annotation.AuthCheck;
import com.yupi.yuoj.common.BaseResponse;
import com.yupi.yuoj.common.ErrorCode;
import com.yupi.yuoj.common.ResultUtils;
import com.yupi.yuoj.constant.UserConstant;
import com.yupi.yuoj.exception.BusinessException;
import com.yupi.yuoj.model.dto.question.QuestionQueryRequest;
import com.yupi.yuoj.model.dto.questionSubmit.QuestionSubmitAddRequest;
import com.yupi.yuoj.model.dto.questionSubmit.QuestionSubmitQueryRequest;
import com.yupi.yuoj.model.entity.Question;
import com.yupi.yuoj.model.entity.QuestionSubmit;
import com.yupi.yuoj.model.entity.User;
import com.yupi.yuoj.model.vo.QuestionSubmitVO;
import com.yupi.yuoj.service.QuestionSubmitService;
import com.yupi.yuoj.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 题目提交接口
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/question_submit")
@Slf4j
@Deprecated
public class QuestionSubmitController {

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private UserService userService;

    /**
     * 题目提交
     *
     * @param questionSubmitAddRequest
     * @param request
     * @return 提交的题解的id
     */
    @PostMapping("/")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
                                               HttpServletRequest request) {
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录才能提交
        // 登录信息是存到http的session里的，所以不用放在Request里接收
        final User loginUser = userService.getLoginUser(request);
        long questionId = questionSubmitAddRequest.getQuestionId();
        long result = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
        return ResultUtils.success(result);
    }


    /**
     * 分页获取题目提交列表(除了管理员，普通用户只能看到非答案，提交代码等公开信息)
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                 HttpServletRequest request) {
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        //这一步只是从数据库得到了满足条件的原始的questionSubmit数据
        //还得脱敏
        Page<QuestionSubmit> questionSubmitPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        //脱敏
        ///为什么需要HttpServletRequest request
        /// 因为我们需要根据用户的权限给它封装VO，管理员和用户看到的东西是不一样的
        /// 所以我们封装VO的时候需要用户id去查它的权限
        /// 当然我们也可以在这里先查了 再把查出来的user对象传给他而不是传一整个httpRequest
        //Page<QuestionSubmitVO> questionSubmitVOPage = questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, request);

        //我们写完发现可以写在外面效率高一些因为 在里面去查 每次都要查一次User获取user权限，但是这个User，查一次就够了
        //所以我们写在外面
        final User loginUser = userService.getLoginUser(request);
        System.out.println(loginUser.getId());
        Page<QuestionSubmitVO> questionSubmitVOPage = questionSubmitService.getQuestionSubmitVOPage(questionSubmitPage, loginUser);
        return ResultUtils.success(questionSubmitVOPage);
    }

}
