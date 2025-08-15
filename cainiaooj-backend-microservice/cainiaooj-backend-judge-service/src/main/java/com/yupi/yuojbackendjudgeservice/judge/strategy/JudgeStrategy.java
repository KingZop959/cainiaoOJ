package com.yupi.yuojbackendjudgeservice.judge.strategy;


import com.yupi.yuojbackendmodel.model.codesandbox.JudgeInfo;

/**
 * 我们的判题服务 不知需要一种判题逻辑，因为有时候判题目不是一定只有答案一个答案
 * 有些题可以有多种答案，只符和其中一个就可以，或者是全部都要有
 *
 * 有一些时候 因为编程语言的不同，判题逻辑有不一样
 * 可能用java 判题机启动JVM就得需要花一些时间
 * 我们不能把这个时间加到用户得头上
 * 所以可能java得判题对于时间上的限制就得宽松一些
 *
 * 这么多的情况 我们不可能全部写到判题服务的实现中
 * 那样让我们的代码无法维护
 * 那么我们就采用策略模式，就是为这种情况设计的
 */
public interface JudgeStrategy {
    /**
     * 执行判题
     * 返回的是judgeInfo，judgeInfo就是专门存判题的结果的
     */
    JudgeInfo doJudge(JudgeContext judgeContext);
}
