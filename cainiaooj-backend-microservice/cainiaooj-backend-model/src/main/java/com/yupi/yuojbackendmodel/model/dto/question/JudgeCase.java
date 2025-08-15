package com.yupi.yuojbackendmodel.model.dto.question;

import lombok.Data;
/**
 * 题目用例 -- 因为是json 我们要提取里面的
 * 所以需要写个对象来接收 便于操作
 */

@Data
public class JudgeCase {
    private String input;
    private String output;
}
