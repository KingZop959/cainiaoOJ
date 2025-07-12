package com.yupi.yuojcodesandbox.model;

import lombok.Data;

/**
 * 封装进程的执行信息
 * 因为我们正常退出流和错误流是两个不同的渠道
 * 你得去判断
 * 所以我们用这个封装一下
 * 根据错误码来判断是什么流的有输出信息
 */
@Data
public class ExecuteMessage {
    private Integer exitValue;
    //这是正常返回结果
    private String message;
    //这是报错信息
    private String errorMessage;

    private Long time;

    private Long memory;
}
