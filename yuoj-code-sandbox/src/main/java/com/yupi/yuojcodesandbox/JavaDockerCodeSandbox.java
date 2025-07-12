package com.yupi.yuojcodesandbox;

import cn.hutool.core.date.StopWatch;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.CreateContainerCmd;
import com.github.dockerjava.api.command.CreateContainerResponse;

import com.github.dockerjava.api.model.*;
import com.github.dockerjava.core.DockerClientBuilder;
import com.github.dockerjava.core.command.ExecStartResultCallback;

import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;
import com.yupi.yuojcodesandbox.model.ExecuteMessage;
import com.yupi.yuojcodesandbox.model.JudgeInfo;

import com.github.dockerjava.api.command.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;

import java.util.ArrayList;

import java.util.List;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class JavaDockerCodeSandbox extends JavaCodeSandboxTemplate{

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_JAVA_CLASS_NAME="Main.java";
    private static final long TIME_OUT = 5000L;
    private static boolean FIRST_INIT = true;


    @Override
    public List<ExecuteMessage> runFile(List<String> inputList, File userCodeFile) {
        /// 第三步 创建容器
        /// 两种方法 1. 拉取一个现成的java容器，往里面加入自己的代码 -- 只能运行java
        /// 2. 自己自定义 一个容器，里面包含 各种语言的运行环境

        //拉镜像--但是第一次执行才需要对吧
        DockerClient dockerClient = DockerClientBuilder.getInstance().build();
        String image = "openjdk:8-alpine";
        if(FIRST_INIT){
            PullImageCmd pullImageCmd = dockerClient.pullImageCmd(image);
            PullImageResultCallback pullImageResultCallback = new PullImageResultCallback(){
                @Override
                public void onNext(PullResponseItem item) {
                    System.out.println("下载镜像:"+item.getStatus());
                    super.onNext(item);
                }
            };
            try {
                pullImageCmd.exec(pullImageResultCallback).awaitCompletion();
            } catch (InterruptedException e) {
                System.out.println("拉取镜像异常");
                throw new RuntimeException(e);
            }
            System.out.println("下载完成");
            FIRST_INIT = false;
        }


        //创建一个可交互的容器--就是一直能够运行，可以获取多次值并获取它的输出，而不是一个用例创建一个容器
        CreateContainerCmd containerCmd = dockerClient.createContainerCmd(image);
        HostConfig hostConfig = new HostConfig();
        //这个是将主机上的文件挂载到容器，让容器可以访问这个文件
        //Volume是相当于映射到docker里的一个“/app”目录下
        hostConfig.setBinds(new Bind(userCodeFile.getParentFile().getAbsolutePath(),new Volume("/app")));
        hostConfig.withMemory(100*1000*1000L);
        //还可以用这个关闭docker中的linux的某些权限比如读写文件，但是他要写配置文件
        //但是这个就是在系统层面限制了
        //hostConfig.withSecurityOpts(Arrays.asList("seccomp=省略了"));
        CreateContainerResponse createContainerResponse = containerCmd
                .withNetworkDisabled(true)//不让使用网络
                .withReadonlyRootfs(true)//不能向docker的根目录写
                .withHostConfig(hostConfig)//设置一些docker的参数，比如限制容器可以使用的内存
                .withAttachStdin(true)//把docker和宿主机进行连接，这个应该是获取docker输出
                .withAttachStderr(true)//这个是获取docker的错误输出
                .withAttachStdout(true)//这个应该是进行对docker输入
                .withTty(true)//这是开启一个交互终端--这个开启之后就让这个容器变成守护进程，不会死
                .exec();
        System.out.println(createContainerResponse);
        String containerId = createContainerResponse.getId();

        //启动容器执行代码--因为我们开了tty所以这个执行之后容器一直运行起来的
        dockerClient.startContainerCmd(containerId).exec();



        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        for (String inputArgs : inputList) {
            StopWatch stopWatch = new StopWatch();
            String[] inputArg = inputArgs.split(" ");
            String[] cmdArray = ArrayUtil.append(new String[]{"java","-cp","/app","Main",},inputArg);
            ExecCreateCmdResponse execCreateCmdResponse = dockerClient.execCreateCmd(containerId)
                    .withCmd(cmdArray)
                    .withAttachStderr(true)//虽然上面加了 但是还是可以以防万一
                    .withAttachStdout(true)
                    .withAttachStdin(true)
                    .exec();
            System.out.println("执行命令创建成功"+execCreateCmdResponse);
            ExecuteMessage executeMessage = new ExecuteMessage();
            final String[] errorMessage = {null};
            final String[] message = {null};
            stopWatch.start();
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
                        System.out.println("输出错误结果:"+ errorMessage[0]);
                    }else {
                        message[0] = new String(frame.getPayload());
                        System.out.println("输出结果:"+ message[0]);
                    }
                    super.onNext(frame);
                }
            };

            final Long[] maxMemory = {0L};
            StatsCmd statsCmd = dockerClient.statsCmd(containerId);
            ResultCallback<Statistics> statisticsResultCallback = statsCmd.exec(new ResultCallback<Statistics>() {
                @Override
                public void onNext(Statistics statistics) {
                    System.out.println("内存:"+statistics.getMemoryStats().getUsage());
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
            statsCmd.exec(statisticsResultCallback);
            try{
                dockerClient.execStartCmd(execCreateCmdResponse.getId())
                        .exec(execStartResultCallback)
                        //这个等待执行结束可以设置最大时间限制,超时了他就会继续往下走而不是抛异常
                        .awaitCompletion(TIME_OUT, TimeUnit.MICROSECONDS);
                stopWatch.stop();
                statsCmd.close();
                if (timeout[0]) {
                    errorMessage[0] = new String("程序执行超时");
                }
            }catch (InterruptedException e){
                System.out.println("程序执行异常");
                throw new RuntimeException(e);
            }
            executeMessage.setErrorMessage(errorMessage[0]);
            executeMessage.setMessage(message[0]);
            executeMessage.setMemory(maxMemory[0]);
            executeMessageList.add(executeMessage);
            long lastTaskTimeMillis = stopWatch.getLastTaskTimeMillis();
            executeMessage.setTime(lastTaskTimeMillis);
        }

        dockerClient.stopContainerCmd(containerId).exec();
        dockerClient.removeContainerCmd(containerId).exec();
        return executeMessageList;
    }

    @Override
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
}
