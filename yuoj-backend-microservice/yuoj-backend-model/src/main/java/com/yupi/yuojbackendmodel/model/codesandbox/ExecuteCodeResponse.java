package com.yupi.yuojbackendmodel.model.codesandbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteCodeResponse {

    private List<String> outputList;
    /**
     * 这个是就是如果说超时了--不是说超题目的限制了
     * 超内存了什么的
     * 超沙箱的限制额了
     * 或者说报错了
     */
    private String message;
    /**
     * 执行状态--成功失败什么的，其实也可以加到message里面
     */
    private Integer status;

    /**
     * 实际运行时的内存，时间什么的
     * 其实也可以放到message里面
     */
    private JudgeInfo judgeInfo;


}
