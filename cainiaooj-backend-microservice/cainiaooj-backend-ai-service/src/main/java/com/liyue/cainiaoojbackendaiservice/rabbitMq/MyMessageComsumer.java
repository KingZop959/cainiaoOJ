package com.liyue.yuojbackendaiservice.rabbitMq;


import com.google.gson.Gson;
import com.liyue.yuojbackendaiservice.service.QuestionSolutionService;
import com.liyue.yuojbackendaiservice.service.QuestionSubmitSolutionService;
import com.rabbitmq.client.Channel;
import com.yupi.yuojbackendcommon.common.ErrorCode;
import com.yupi.yuojbackendcommon.exception.BusinessException;
import com.yupi.yuojbackendmodel.model.entity.Question;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmit;
import com.yupi.yuojbackendmodel.model.entity.QuestionSubmitSolution;
import com.yupi.yuojbackendmodel.model.vo.QuestionSubmitSolutionVO;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class MyMessageComsumer {

    private static final int MAX_RETRY_COUNT = 3;
    private final static Gson Gson = new Gson();
    @Resource
    private QuestionSolutionService questionSolutionService;
    @Resource
    private QuestionSubmitSolutionService questionSubmitSolutionService;
    @SneakyThrows
    @RabbitListener(queues = {"ai_solution_queue"},ackMode = "MANUAL")
    public void receiveMessage(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        log.info("ai_solution_queue receiveMessage message={}",message);
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
        Question question = Gson.fromJson(messageBody, Question.class);
        try{
            boolean aiSolution = questionSolutionService.getAiSolution(question);
            if (aiSolution){
                channel.basicAck(deliveryTag,false);
            }else {
                channel.basicNack(deliveryTag, false, true);
            }
        }catch (BusinessException e){
            if (e.getCode()==ErrorCode.SYSTEM_ERROR.getCode()){
                // 针对系统错误进行重试
                int retryCount = getRetryCountFromXDeath(message);
                if (retryCount < MAX_RETRY_COUNT) {
                    log.info("Task Ai solution {} 系统错误，重试第 {} 次", question.getId(), retryCount + 1);
                    // requeue 为 true ，让消息重新入队
                    channel.basicNack(deliveryTag, false, true);
                } else {
                    log.info("Task Ai solution{} 系统错误重试次数达到上限，更新状态并不再重试", question.getId());
                    updateTaskStatusToFailed(question.getId(), e.getMessage());
                    // 重试次数已达上限，不重新入队，消息将被丢弃（或进入死信队列）
                    channel.basicNack(deliveryTag, false, false);
                }
            }else {
                 // 非系统错误，直接更新状态通知用户，不重试
                updateTaskStatusToFailed(question.getId(), e.getMessage());
                channel.basicNack(deliveryTag,false,false);
            }
        }

    }
    @SneakyThrows
    @RabbitListener(queues = {"ai_submit_queue"},ackMode = "MANUAL")
    public void receiveSubmitMessage(Message message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag){
        log.info("ai_submit_queue receiveMessage message={}",message);
        String messageBody = new String(message.getBody(), StandardCharsets.UTF_8);
        QuestionSubmitSolutionVO questionSubmitSolutionVO = Gson.fromJson(messageBody, QuestionSubmitSolutionVO.class);
        System.out.println(questionSubmitSolutionVO);
        try{
            boolean aiSolution = questionSubmitSolutionService.getAiSubmitSolution(questionSubmitSolutionVO.getQuestionSubmit(),questionSubmitSolutionVO.getQuestion());
            if (aiSolution){
                channel.basicAck(deliveryTag,false);
            }else {
                channel.basicNack(deliveryTag, false, true);
            }
        }catch (BusinessException e){
            if (e.getCode()==ErrorCode.SYSTEM_ERROR.getCode()){
                // 针对系统错误进行重试
                int retryCount = getRetryCountFromXDeath(message);
                if (retryCount < MAX_RETRY_COUNT) {
                    log.info("Task Ai Submit solution {} 系统错误，重试第 {} 次", questionSubmitSolutionVO.getQuestionSubmit().getId(), retryCount + 1);
                    // requeue 为 true ，让消息重新入队
                    channel.basicNack(deliveryTag, false, true);
                } else {
                    log.info("Task Ai Submit solution {} 系统错误重试次数达到上限，更新状态并不再重试", questionSubmitSolutionVO.getQuestionSubmit().getId());
                    updateTaskStatusToFailed(questionSubmitSolutionVO.getQuestionSubmit().getId(), e.getMessage());
                    // 重试次数已达上限，不重新入队，消息将被丢弃（或进入死信队列）
                    channel.basicNack(deliveryTag, false, false);
                }
            }else {
                // 非系统错误，直接更新状态通知用户，不重试
                updateTaskStatusToFailed(questionSubmitSolutionVO.getQuestionSubmit().getId(), e.getMessage());
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
    private void updateTaskStatusToFailed(long questionId, String errorMsg) {

    }

}
