package com.yupi.yuojbackenduserservice.oss;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OssConfig {

    @Bean
    public OSS ossClient() {
        return new OSSClientBuilder().build("https://oss-cn-chengdu.aliyuncs.com", System.getenv("OSS_ACCESS_KEY_ID"), System.getenv("OSS_ACCESS_KEY_SECRET"));
    }
}