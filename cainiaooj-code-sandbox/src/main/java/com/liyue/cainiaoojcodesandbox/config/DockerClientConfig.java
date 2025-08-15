package com.liyue.cainiaoojcodesandbox.config;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
public class DockerClientConfig {
    @Bean
    public DockerClient dockerClient(){
        ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(URI.create("unix:///var/run/docker.sock")).build();
        DockerClient dockerClient = DockerClientBuilder.getInstance()
                .withDockerHttpClient(httpClient).build();
        return dockerClient;
    }
}
