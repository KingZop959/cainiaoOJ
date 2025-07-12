package com.yupi.yuojbackendjudgeservice.rabbitmq;

import com.rabbitmq.client.Channel;
import com.yupi.yuojbackendcommon.common.ErrorCode;
import com.yupi.yuojbackendcommon.exception.BusinessException;
import com.yupi.yuojbackendjudgeservice.judge.JudgeService;
import com.yupi.yuojbackendmodel.model.codesandbox.JudgeInfo;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmit;
import com.yupi.yuojbackendmodel.model.enums.QuestionSubmitStatusEnum;
import com.yupi.yuojbackendserviceclient.service.QuestionFeignClient;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MyMessageComsumer {

    private static final int MAX_RETRY_COUNT = 3;
    @Resource
    private JudgeService judgeService;
    @Resource
    private QuestionFeignClient questionFeignClient;
    @SneakyThrows
    @RabbitListener(queues = {"code_queue"},ackMode = "MANUAL")
    public void receiveMessage(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        log.info("receiveMessage message={}",message);
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
        long questionSubmitId = Long.parseLong(messageBody);
        try{
            judgeService.doJudge(questionSubmitId);
            channel.basicAck(deliveryTag,false);
        }catch (BusinessException e){
            if (e.getCode()==ErrorCode.SYSTEM_ERROR.getCode()){
                // 针对系统错误进行重试
                int retryCount = getRetryCountFromXDeath(message);
                if (retryCount < MAX_RETRY_COUNT) {
                    log.info("Task {} 系统错误，重试第 {} 次", questionSubmitId, retryCount + 1);
                    // requeue 为 true ，让消息重新入队
                    channel.basicNack(deliveryTag, false, true);
                } else {
                    log.info("Task {} 系统错误重试次数达到上限，更新状态并不再重试", questionSubmitId);
                    updateTaskStatusToFailed(questionSubmitId, e.getMessage());
                    // 重试次数已达上限，不重新入队，消息将被丢弃（或进入死信队列）
                    channel.basicNack(deliveryTag, false, false);
                }
            }else {
                 // 非系统错误，直接更新状态通知用户，不重试
                updateTaskStatusToFailed(questionSubmitId, e.getMessage());
                channel.basicNack(deliveryTag,false,false);
            }
        }

    }

    /**
     * 根据消息属性中 x-death 头获取消息已重试的次数
     */
    @SuppressWarnings("unchecked")
    private int getRetryCountFromXDeath(Message message) {
        MessageProperties properties = message.getMessageProperties();
        Map<String, Object> headers = properties.getHeaders();
        if (headers != null && headers.containsKey("x-death")) {
            List<Map<String, Object>> xDeathList = (List<Map<String, Object>>) headers.get("x-death");
            if (xDeathList != null && !xDeathList.isEmpty()) {
                // 取 x-death 头中第一个记录的 count 数值
                Number count = (Number) xDeathList.get(0).get("count");
                return count != null ? count.intValue() : 0;
            }
        }
        return 0;
    }

    /**
     * 更新任务状态为失败，记录错误信息并通知用户（此方法可根据实际业务调整）
     */
    private void updateTaskStatusToFailed(long questionSubmitId, String errorMsg) {
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setId(questionSubmitId);
        questionSubmit.setStatus(QuestionSubmitStatusEnum.FAILED.getValue());
        JudgeInfo judgeInfo = new JudgeInfo();
        judgeInfo.setMessage(errorMsg);
        // 调用远程接口或者本地服务更新任务状态
        questionFeignClient.updateQuestionSubmitById(questionSubmit);
    }

}
