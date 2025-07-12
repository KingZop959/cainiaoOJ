package com.yupi.yuojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yupi.yuojbackendmodel.model.dto.questionSubmit.QuestionSubmitAddRequest;
import com.yupi.yuojbackendmodel.model.dto.questionSubmit.QuestionSubmitQueryRequest;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmit;
import com.yupi.yuojbackendmodel.model.entity.User;
import com.yupi.yuojbackendmodel.model.vo.QuestionSubmitVO;

/**
* @author zly
* @description 针对表【question_submit(题目提交)】的数据库操作Service
* @createDate 2025-04-02 15:09:38
*/
public interface QuestionSubmitService extends IService<QuestionSubmit> {
    /**
     * 提交问题的答案
     *
     * @param questionSubmitAddRequest
     * @param loginUser
     * @return  题目提交的id
     */
    long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);

    /**
     * 获取题目提交查询条件
     *
     * @param questionSubmitQueryRequest
     * @return
     */
    QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest);


    /**
     * 获取题目提交封装
     *
     * @param questionSubmit
     * @param loginUser
     * @return
     */
    QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser);

    /**
     * 分页获取题目提交封装
     *
     * @param questionSubmitPage
     * @param loginUser
     * @return
     */
    Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser);
}
