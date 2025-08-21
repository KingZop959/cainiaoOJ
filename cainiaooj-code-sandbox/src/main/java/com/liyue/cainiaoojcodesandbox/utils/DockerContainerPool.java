package com.liyue.cainiaoojcodesandbox.utils;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.InspectContainerResponse;
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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.scheduling.annotation.Scheduled;

@Component
public class DockerContainerPool {
    private final BlockingQueue<String> containerQueue = new LinkedBlockingQueue<>();
    private final Set<String> allContainers = ConcurrentHashMap.newKeySet();
    private final DockerClient dockerClient;
    private static final String USER_DIR = System.getProperty("user.dir");
    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_CODE_PATH_NAME = USER_DIR + File.separator + GLOBAL_CODE_DIR_NAME;

    // 新增：核心池、最大池、获取超时
    private int corePoolSize = 3;
    private int maxPoolSize = 10;
    private long acquireTimeoutMs = 2000;
    private final AtomicInteger totalContainers = new AtomicInteger(0);

    public DockerContainerPool(DockerClient dockerClient) {
        this.dockerClient = dockerClient;
    }

    @PostConstruct
    public void init() {
        for (int i = 0; i < corePoolSize; i++) {
            String id = createAndStartContainer();
            containerQueue.offer(id);
        }
    }

    private String createAndStartContainer() {
        HostConfig hostConfig = new HostConfig();
        hostConfig.setBinds(new Bind(GLOBAL_CODE_PATH_NAME, new Volume("/app")));
        hostConfig.withMemory(100 * 1000 * 1000L);

        CreateContainerCmd containerCmd = dockerClient.createContainerCmd("sandbox:test");
        CreateContainerResponse resp = containerCmd
                .withNetworkDisabled(true)
                .withReadonlyRootfs(true)
                .withHostConfig(hostConfig)
                .withAttachStdin(true)
                .withAttachStderr(true)
                .withAttachStdout(true)
                .withTty(true)
                .exec();

        String containerId = resp.getId();
        dockerClient.startContainerCmd(containerId).exec();
        allContainers.add(containerId);
        totalContainers.incrementAndGet();
        return containerId;
    }

    // 获取容器：优先拿空闲；无空闲时若可突发就创建；否则限时等待，超时失败
    public String acquireContainer() throws InterruptedException {
        String id = containerQueue.poll();
        if (id != null) return id;

        if (totalContainers.get() < maxPoolSize) {
            synchronized (this) {
                if (totalContainers.get() < maxPoolSize) {
                    return createAndStartContainer();
                }
            }
        }

        id = containerQueue.poll(acquireTimeoutMs, TimeUnit.MILLISECONDS);
        if (id == null) {
            throw new IllegalStateException("系统繁忙，请稍后重试");
        }
        return id;
    }

    // 释放容器：若总量超过核心且空闲已足够，则回收以缩容；否则归还队列
    public void releaseContainer(String containerId) {
        if (totalContainers.get() > corePoolSize && containerQueue.size() >= corePoolSize) {
            try {
                dockerClient.removeContainerCmd(containerId).withForce(true).exec();
            } finally {
                allContainers.remove(containerId);
                totalContainers.decrementAndGet();
            }
            return;
        }
        containerQueue.offer(containerId);
    }

    @PreDestroy
    public void cleanup() {
        allContainers.forEach(id -> dockerClient.removeContainerCmd(id).withForce(true).exec());
    }

    private boolean isHealthy(String id) {
		InspectContainerResponse r = dockerClient.inspectContainerCmd(id).exec();
		// 先确保容器在运行
		if (r.getState() == null || !Boolean.TRUE.equals(r.getState().getRunning())) return false;
		// 配置了 HEALTHCHECK：状态有 healthy / unhealthy / starting
		if (r.getState().getHealth() != null) {
			String s = r.getState().getHealth().getStatus();
			return "healthy".equalsIgnoreCase(s);
		}
		// 未配置 HEALTHCHECK 时，仅以 running 为准
		return true;
	}

	// 启动类需加 @EnableScheduling
	@Scheduled(initialDelay = 20000, fixedDelay = 60000)
	public void sweepHealth() {
		java.util.List<String> idle = new java.util.ArrayList<>(containerQueue); // 只扫空闲，避免打断在跑的任务
		for (String id : idle) {
			// 对 starting 可选择跳过一轮，这里一律按不健康处理也可
			if (!isHealthy(id)) {
				containerQueue.remove(id);
				allContainers.remove(id);
				try { dockerClient.removeContainerCmd(id).withForce(true).exec(); } catch (Exception ignore) {}
				String newId = createAndStartContainer();
				containerQueue.offer(newId);
			}
		}
	}
}
