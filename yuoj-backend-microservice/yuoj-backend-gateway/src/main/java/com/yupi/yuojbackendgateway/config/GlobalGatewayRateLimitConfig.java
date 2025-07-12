package com.yupi.yuojbackendgateway.config;

import com.alibaba.csp.sentinel.adapter.gateway.common.SentinelGatewayConstants;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiDefinition;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPathPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.ApiPredicateItem;
import com.alibaba.csp.sentinel.adapter.gateway.common.api.GatewayApiDefinitionManager;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayFlowRule;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.sc.SentinelGatewayFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class GlobalGatewayRateLimitConfig {

    @Bean
    public SentinelGatewayFilter sentinelGatewayFilter() {
        return new SentinelGatewayFilter();
    }

    // 注册 API 分组规则（匹配所有路径）
    @PostConstruct
    public void initApiDefinitions() {
        Set<ApiDefinition> definitions = new HashSet<>();
        ApiDefinition apiDefinition = new ApiDefinition("global-api")
                .setPredicateItems(new HashSet<ApiPredicateItem>() {{
                    add(new ApiPathPredicateItem().setPattern("/api/**").
                            setMatchStrategy(SentinelGatewayConstants.URL_MATCH_STRATEGY_PREFIX));
                }});

        definitions.add(apiDefinition);
        GatewayApiDefinitionManager.loadApiDefinitions(definitions);
        System.out.println("[Sentinel] 注册 API 规则: global-api");
    }

    // 配置限流规则：每秒最多 50 个请求
    @PostConstruct
    public void initGatewayFlowRules() {
        Set<GatewayFlowRule> rules = new HashSet<>();
        rules.add(new GatewayFlowRule("global-api")
                .setCount(50)              // QPS 限制为 50
                .setIntervalSec(1));       // 每 1 秒为一个统计窗口
        GatewayRuleManager.loadRules(rules);
        System.out.println("[Sentinel] 限流规则加载成功: global-api 1 QPS");
    }
}
