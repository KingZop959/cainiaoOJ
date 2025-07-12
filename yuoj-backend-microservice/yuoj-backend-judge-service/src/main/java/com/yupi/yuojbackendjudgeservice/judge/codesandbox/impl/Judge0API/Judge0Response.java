package com.yupi.yuojbackendjudgeservice.judge.codesandbox.impl.Judge0API;

import lombok.Data;

@Data
public class Judge0Response {
    // 根据 API 文档定义需要的字段
    private String token;
    private String status;
    private String stdout;
    private String stderr;
    private String compile_output;
    private String time;
    private Long memory;
}