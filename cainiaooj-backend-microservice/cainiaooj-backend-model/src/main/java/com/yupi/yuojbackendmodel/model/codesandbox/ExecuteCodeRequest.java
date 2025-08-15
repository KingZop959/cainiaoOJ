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
public class ExecuteCodeRequest {

    private List<String> inputList;
    private String code;
    private String language;
    /**
     * 需不需要时间限制，内存限制等
     * 可加可不加
     * 加的话可以通过超出时间中断程序，节省内存
     * 但不加的话通用性更好
     * 这个项目不加最好
     */

}
