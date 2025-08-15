package com.yupi.yuojbackendquestionservice.sentinel;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.yupi.yuojbackendcommon.common.ErrorCode;
import com.yupi.yuojbackendcommon.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class SentinelLimiterUtil {

    @SentinelResource(value = "submitLimiter", blockHandler = "handleSubmitBlock")
    public  void submitLimiter(String combinedKey) {
        System.out.println("进入 submitLimiter 方法：" + combinedKey);
        // 方法体为空即可，仅用于限流检测
    }

    public void handleSubmitBlock(String combinedKey, BlockException ex) {
        System.out.println("触发限流：" + combinedKey);
        throw new BusinessException(ErrorCode.OPERATION_ERROR, "操作过于频繁，请稍后再试！");
    }
}
