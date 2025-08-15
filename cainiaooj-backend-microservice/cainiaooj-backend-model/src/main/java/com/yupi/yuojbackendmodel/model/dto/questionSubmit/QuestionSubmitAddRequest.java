package com.yupi.yuojbackendmodel.model.dto.questionSubmit;

import lombok.Data;

import java.io.Serializable;

/**
 * 做题提交时的创建请求
 * 所以只要考虑提交的时候需要提交什么
 *
 * @author <a href="https://github.com/liyupi">程序员鱼皮</a>
 * @from <a href="https://yupi.icu">编程导航知识星球</a>
 */
@Data
public class QuestionSubmitAddRequest implements Serializable {

    /**
     * 编程语言
     */
    private String language;

    /**
     * 用户代码
     */
    private String code;

    /**
     * 题目 id
     */
    private Long questionId;


    private static final long serialVersionUID = 1L;
}