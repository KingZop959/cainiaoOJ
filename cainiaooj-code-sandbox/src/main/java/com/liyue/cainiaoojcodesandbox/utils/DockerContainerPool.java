package com.liyue.cainiaoojcodesandbox.utils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.model.Bind;
import com.github.dockerjava.api.model.HostConfig;
import com.github.dockerjava.api.model.Volume;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class DockerContainerPool {
    private final BlockingQueue<String> containerQueue = new LinkedBlockingQueue<>();
    private final Set<String> allContainers = ConcurrentHashMap.newKeySet();
    private final DockerClient dockerClient;
    private static final String USER_DIR = System.getProperty("user.dir");
    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_CODE_PATH_NAME = USER_DIR + File.separator + GLOBAL_CODE_DIR_NAME;

    private int poolSize =3;

    public DockerContainerPool(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    @PostConstruct
    public void init() {
        HostConfig hostConfig = new HostConfig();
        hostConfig.setBinds(new Bind(GLOBAL_CODE_PATH_NAME,new Volume("/app")));
        hostConfig.withMemory(100*1000*1000L);
        for (int i = 0; i < poolSize; i++) {
            CreateContainerCmd containerCmd = dockerClient.createContainerCmd("sandbox:test");

            CreateContainerResponse createContainerResponse = containerCmd
                    .withNetworkDisabled(true)//不让使用网络
                    .withReadonlyRootfs(true)//不能向docker的根目录写
                    .withHostConfig(hostConfig)//设置一些docker的参数，比如限制容器可以使用的内存
                    .withAttachStdin(true)//把docker和宿主机进行连接，这个应该是获取docker输出
                    .withAttachStderr(true)//这个是获取docker的错误输出
                    .withAttachStdout(true)//这个应该是进行对docker输入
                    .withTty(true)//这是开启一个交互终端--这个开启之后就让这个容器变成守护进程，不会死
                    //.withEnv("TERM=xterm")
                    .exec();
            String containerId = createContainerResponse.getId();
            containerQueue.offer(containerId);
            allContainers.add(containerId);
            dockerClient.startContainerCmd(containerId).exec();
        }
    }

    public String acquireContainer() throws InterruptedException {
        return containerQueue.take();
    }

    public void releaseContainer(String containerId) {
        containerQueue.offer(containerId);
    }

    @PreDestroy
    public void cleanup() {
        allContainers.forEach(id -> dockerClient.removeContainerCmd(id).withForce(true).exec());
    }
}
