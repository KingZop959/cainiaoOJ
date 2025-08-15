package com.liyue.cainiaoojbackendaiservice;

import com.liyue.cainiaoojbackendaiservice.rabbitMq.InitRabbitMq;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.liyue.yuojbackendaiservice.mapper")
public class YuojBackendAiServiceApplication {

    public static void main(String[] args) {
        InitRabbitMq.doInit();
        SpringApplication.run(YuojBackendAiServiceApplication.class, args);
    }

}
