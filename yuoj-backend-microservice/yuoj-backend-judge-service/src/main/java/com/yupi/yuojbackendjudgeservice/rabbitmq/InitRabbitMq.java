package com.yupi.yuojbackendjudgeservice.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InitRabbitMq {
    //这种启动一次的方法，设为静态的，再主启动类中启动
    public static void doInit(){
        try{
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost("localhost");
            Connection connection = connectionFactory.newConnection();
            //channel相当于client
            Channel channel = connection.createChannel();
            String exchangeName = "code_exchange";
            channel.exchangeDeclare(exchangeName,"direct");
            String queueName = "code_queue";
            channel.queueDeclare(queueName,true,false,false,null);
            channel.queueBind(queueName,exchangeName,"judge");
            log.info("消息队列启动成功");
        }catch (Exception e){
            log.error("消息队列启动失败");
        }
    }
}
