package com.liyue.cainiaoojcodesandbox.docker;


import cn.hutool.core.date.StopWatch;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.command.StatsCmd;
import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.liyue.cainiaoojcodesandbox.model.CodeSandbox;
import com.liyue.cainiaoojcodesandbox.model.ExecuteCodeRequest;
import com.liyue.cainiaoojcodesandbox.model.ExecuteCodeResponse;
import com.liyue.cainiaoojcodesandbox.model.ExecuteMessage;
import com.liyue.cainiaoojcodesandbox.model.JudgeInfo;
import com.liyue.cainiaoojcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Docker沙箱模板
 */
@Slf4j
public abstract class CodeSandboxTemplate_old implements CodeSandbox {

    private static final long TIME_OUT = 5000L;
    private static boolean FIRST_INIT = true;



    /**
     * 1.将用户的代码保存为文件
     */
    public File saveCodeToFile(String code,String global_code_path_name,String global_code_name) {
        /// 第一步将用户的代码保存为文件
        if (!FileUtil.exist(global_code_path_name)) {
            log.info("没有tempcode文件夹");
            FileUtil.mkdir(global_code_path_name);
        }

        // 把用户的代码隔离存放--每个用户的是隔离的
        String userCodeParentPath = global_code_path_name + File.separator + UUID.randomUUID();
        //这是实际的文件Main.java
        String userCodePath = userCodeParentPath + File.separator + global_code_name;
        log.info("userCodePath是"+userCodePath);
        // 写文件了
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        log.info("创建文件成功了"+userCodePath);
        return userCodeFile;
    }

    /**
     * 2. 将保存的用户代码文件编译
     */
    public ExecuteMessage compileFile(String compileCmd) {
        log.info("进编译了");
        /// 第二步 编译代码 得到class文件
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess, "编译");
            System.out.println(executeMessage);
            if (executeMessage.getExitValue() != 0) {
                throw new RuntimeException("编译错误");
            }
            return executeMessage;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 3.执行用户的代码文件
     */
    public List<ExecuteMessage> runFile(List<String> inputList, File userCodeFile,String[] executeCommands) {
        /// 第三步 创建容器
        /// 两种方法 1. 拉取一个现成的java容器，往里面加入自己的代码 -- 只能运行java
        /// 2. 自己自定义 一个容器，里面包含 各种语言的运行环境
        ApacheDockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(URI.create("unix:///var/run/docker.sock")).build();

        //拉镜像--但是第一次执行才需要对吧
        DockerClient dockerClient = DockerClientBuilder.getInstance()
                .withDockerHttpClient(httpClient).build();
        String image = "sandbox:test";
       /* if(FIRST_INIT){
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback(){
                @Override
                public void onNext(PullResponseItem item) {
                    super.onNext(item);
                }
            };
            try {
                pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            FIRST_INIT = false;
        }*/

        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        HostConfig hostConfig = new HostConfig();
        hostConfig.setBinds(new Bind(userCodeFile.getParentFile().getAbsolutePath(),new Volume("/app")));
        hostConfig.withMemory(100*1000*1000L);
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

        //启动容器执行代码--因为我们开了tty所以这个执行之后容器一直运行起来的
        dockerClient.startContainerCmd(containerId).exec();



        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            StopWatch stopWatch = new StopWatch();
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(executeCommands)
                    .withAttachStderr(true)//虽然上面加了 但是还是可以以防万一
                    .withAttachStdout(true)
                    .withAttachStdin(true)
                    .exec();
            ExecuteMessage executeMessage = new ExecuteMessage();
            final String[] errorMessage = {null};
            final String[] message = {null};
            final StringBuilder messageBuilder = new StringBuilder();
            stopWatch.start();
            log.info("开始计时");
            final boolean[] timeout = new boolean[]{true};
            ExecStartResultCallback execStartResultCallback = new ExecStartResultCallback(){
                @Override
                public void onComplete() {
                    timeout[0] = false;
                    super.onComplete();
                }

                @Override
                public void onNext(Frame frame) {
                    StreamType streamType = frame.getStreamType();
                    if (StreamType.STDERR.equals(streamType)) {
                        errorMessage[0] = new String(frame.getPayload());
                    }else {
                        messageBuilder.append(new String(frame.getPayload()));
                    }
                    super.onNext(frame);
                }
            };

            final Long[] maxMemory = {0L};
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    maxMemory[0] =Math.max(maxMemory[0],statistics.getMemoryStats().getUsage());
                }

                @Override
                public void onStart(Closeable closeable) {

                }

                @Override
                public void onError(Throwable throwable) {

                }

                @Override
                public void onComplete() {

                }

                @Override
                public void close() throws IOException {

                }
            });
            log.info("到执行前了");
            statsCmd.exec(statisticsResultCallback);
            try(ByteArrayInputStream inputStream = new ByteArrayInputStream(inputArgs.getBytes())){
                // 注意单位是 MILLISECONDS
                dockerClient.execStartCmd(execCreateCmdResponse.getId())
                        .withStdIn(inputStream)
                        .exec(execStartResultCallback)
                        //这个等待执行结束可以设置最大时间限制,超时了他就会继续往下走而不是抛异常
                        .awaitCompletion(TIME_OUT, TimeUnit.MILLISECONDS);
                stopWatch.stop();
                log.info("停止计时");
                statsCmd.close();
                if (timeout[0]) {
                    log.warn("超时了");
                    errorMessage[0] = new String("程序执行超时");
                }

            }catch (InterruptedException | IOException e){
                log.error("程序执行异常");
                throw new RuntimeException(e);
            }
            executeMessage.setErrorMessage(errorMessage[0]);
            executeMessage.setMessage(messageBuilder.toString());
            executeMessage.setMemory(maxMemory[0]);
            executeMessageList.add(executeMessage);
            long lastTaskTimeMillis = stopWatch.getLastTaskTimeMillis();
            executeMessage.setTime(lastTaskTimeMillis);
        }

        dockerClient.stopContainerCmd(containerId).exec();
        dockerClient.removeContainerCmd(containerId).exec();
        return executeMessageList;
    }

    public ExecuteCodeResponse getExecuteResponse(List<ExecuteMessage> executeMessageList) {
        /// 整理结果
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        //我们来取每个用例执行的最大时间--只要有一个用例超时算法就不能算完成需求
        //因为后续判题服务判断超时 如果是最小值没超时 你不能判定全部用例都没超时
        //但是最大值没超时 那么肯定全部用例都没超时
        long maxTime = 0;
        long maxMemory =0L;
        for (ExecuteMessage executeMessage:executeMessageList){
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)){
                executeCodeResponse.setMessage(errorMessage);
                executeCodeResponse.setStatus(3);
                break;
            }
            //如果每一条输入用例都正常返回了一个值 没有那些运行中错误那就可以返回了
            //这里的正常返回指得只是程序正常执行完返回了一个结果而没有报错 但是对不对我们不知道也不需要知道
            outputList.add(executeMessage.getMessage());

            Long time = executeMessage.getTime();
            if (time!=null){
                maxTime = Math.max(maxTime,time);
            }
            Long memory = executeMessage.getMemory();
            if (memory!=null){
                maxMemory = Math.max(maxMemory,memory);
            }
        }
        executeCodeResponse.setOutputList(outputList);
        //正常运行完成
        if (outputList.size()==executeMessageList.size()){
            executeCodeResponse.setStatus(1);
        }
        JudgeInfo judgeInfo = new JudgeInfo();
        //这个留到判题去填
        //judgeInfo.setMessage();
        judgeInfo.setMemory(maxMemory);
        judgeInfo.setTime(maxTime);
        executeCodeResponse.setJudgeInfo(judgeInfo);
        return executeCodeResponse;
    }


    /**
     * 5.文件清理
     */
    public boolean fileClean(File userCodeFile) {
        /// 第五步文件清理。执行完之后将文件删掉
        //这是因为 有可能创建文件夹的时候，服务器容量不够了 就没创建成功
        //虽然可能性小 但是我们可以避免
        boolean del = true;
        File parentFile = userCodeFile.getParentFile();
        if (parentFile != null) {
            del = FileUtil.del(parentFile.getAbsoluteFile());
            log.info("删除" + (del ? "成功" : "失败"));
        }
        return del;
    }

    public abstract ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest);

}

