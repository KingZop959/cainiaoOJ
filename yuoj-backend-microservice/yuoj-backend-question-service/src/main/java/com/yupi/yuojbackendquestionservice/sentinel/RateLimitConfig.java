package com.yupi.yuojbackendquestionservice.sentinel;


import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class RateLimitConfig {
    @PostConstruct
    public void initHotspotRules() {
        List<ParamFlowRule> rules = new ArrayList<>();

        rules.add(new ParamFlowRule("submitLimiter")
                .setParamIdx(0) // 只限这个组合 key
                .setCount(1)
                .setDurationInSec(60));

        ParamFlowRuleManager.loadRules(rules);
        System.out.println("规则注册成功了");
    }
}
