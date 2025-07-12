package com.yupi.yuojcodesandbox;


import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;

/**
 *  这里为什么是接口不是类
 *  因为是为了解耦
 *  如果我们要用别人的代码沙箱
 *  只需要将别人的引入进来
 *  用别人的沙箱实现一下这个接口就好了
 *  其他的代码是不用动的
 */

public interface CodeSandbox {
    ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);
}
