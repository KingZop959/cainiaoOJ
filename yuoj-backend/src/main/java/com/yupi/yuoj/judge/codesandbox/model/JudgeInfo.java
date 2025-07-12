package com.yupi.yuoj.judge.codesandbox.model;

import lombok.Data;

/**
 * 题目用例 -- 因为是json 我们要提取里面的
 * 所以需要写个对象来接收 便于操作
 */

@Data
public class JudgeInfo {
    /**
     * 程序执行信息 -- 当然这个包括错误信息，成功什么的，不要和QuestionSubmit的status搞混了
     * 虽然都有Enum，但是QuestionSubmit得status是告诉用户这个题还没开始判，还是正在判，还是成功--看成功得结果
     * 失败--看失败的原因
     *
     * 这个成功和失败得结果和原因都是存在JudgeInfo里面得
     */
    private String message;
    /**
     * 消耗内存 kb
     */
    private Long memory;
    /**
     * 消耗时间 ms
     */
    private Long time;
}
