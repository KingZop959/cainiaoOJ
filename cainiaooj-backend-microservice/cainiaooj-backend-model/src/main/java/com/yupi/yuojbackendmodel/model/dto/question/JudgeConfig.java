package com.yupi.yuojbackendmodel.model.dto.question;

import lombok.Data;
/**
 * 题目配置
 */
@Data
public class JudgeConfig {
    /**
     * 时间限制 ms
     */
    private Long timeLimit;
    /**
     * 内存限制 kb
     */
    private Long memoryLimit;
    /**
     * 堆栈限制 kb
     */
    private Long stackLimit;

    /**
     * 特判程序类型
     * 0-是不特判
     * 1-只多答案
     * 2-是无关顺序
     * 3-是多答案且每个答案都无关顺序
     */
    private int specialType;

}
