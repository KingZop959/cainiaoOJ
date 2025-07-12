package com.yupi.yuojcodesandbox;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.dfa.WordTree;
import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;
import com.yupi.yuojcodesandbox.model.ExecuteMessage;
import com.yupi.yuojcodesandbox.model.JudgeInfo;
import com.yupi.yuojcodesandbox.utils.ProcessUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;


public class JavaNativeCodeSandboxOld implements CodeSandbox{

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_JAVA_CLASS_NAME="Main.java";
    private static final long TIME_OUT = 5000L;
    private static final String SECURITY_MANAGER_PATH ="E:\\ZLY\\ojSystem\\yuoj-code-sandbox\\src\\main\\resources\\security";
    private static final String SECURITY_MANAGER_CLASS ="SandboxSecurityManager";

    private static final List<String> blackList = Arrays.asList("Files","exec");
    ///在保存为文件前 检查黑名单
    //如果用户代码中有黑名单的代码.直接不用编译了
    //使用hutool的工具，判断好判断一点
   private static final WordTree wordTree;

   static {
       ///在保存为文件前 检查黑名单
       //如果用户代码中有黑名单的代码.直接不用编译了
       //使用hutool的工具，判断好判断一点
       wordTree = new WordTree();
       wordTree.addWords(blackList);
   }

    public static void main(String[] args) {
        JavaNativeCodeSandboxOld javaNativeCodeSandbox = new JavaNativeCodeSandboxOld();
        ExecuteCodeRequest executeCodeRequest = new ExecuteCodeRequest();
        executeCodeRequest.setInputList(Arrays.asList("1 3","1 4"));
        //String code = ResourceUtil.readStr("testCode/simpleComputeArgs/Main.java", StandardCharsets.UTF_8);
        String code = ResourceUtil.readStr("testCode/unsafeCode/runDangerousProcess.java", StandardCharsets.UTF_8);
        //String code = ResourceUtil.readStr("testCode/simpleCompute/Main.java", StandardCharsets.UTF_8);
        executeCodeRequest.setCode(code);
        executeCodeRequest.setLanguage("java");
        ExecuteCodeResponse executeCodeResponse = javaNativeCodeSandbox.executeCode(executeCodeRequest);
        System.out.println(executeCodeResponse);
        return;

    }
    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        ///在保存为文件前 检查黑名单
        //如果用户代码中有黑名单的代码.直接不用编译了
        //使用hutool的工具，判断好判断一点
        /*FoundWord foundWord = wordTree.matchWord(code);
        if (foundWord!=null){
            System.out.println(foundWord.getFoundWord());
            return null;
        }*/
        /// 第一步将用户的代码保存为文件

        // 这个是拿到项目的根目录
        String userDir = System.getProperty("user.dir");
        // 这个tmpCode是拿来临时保存用户要执行的代码的文件夹
        // 这个File.separator在window中是\\,他会根据不同系统变为不同的
        // 别直接写\\
        // 因为这个\\是在window的，在linux是/
        // 我们这个程序应该要能部署到所有的系统才行
        String globalCodePathName = userDir+ File.separator+ GLOBAL_CODE_DIR_NAME;

        // 判断全局代码目录是否存在，第一次启动还没创建肯定不存在，那么就要创建
        if (!FileUtil.exist(globalCodePathName)){
            FileUtil.mkdir(globalCodePathName);
        }

        // 把用户的代码隔离存放--每个用户的是隔离的
        String userCodeParentPath = globalCodePathName + File.separator + UUID.randomUUID();
        //这是实际的文件Main.java
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        // 写文件了
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);

        /// 第二步 编译代码 得到class文件
        String compileCmd = String.format("javac -encoding utf-8 %s",userCodeFile.getAbsoluteFile());
        try {
            Process compileProcess = Runtime.getRuntime().exec(compileCmd);
            ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(compileProcess,"编译");
            System.out.println(executeMessage);
        } catch (IOException e) {
            return getErrorResponse(e);
        }

        /// 第三步执行程序--依然是命令行
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        // -cp 的意思是 .class文件的path 所以这里使用的是Main.java的目录也就是userCodeParentPath
        // 后一个参数就是 输入用例
        for (String inputArgs: inputList){
            //String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s",userCodeParentPath,inputArgs);
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s",userCodeParentPath,SECURITY_MANAGER_PATH,SECURITY_MANAGER_CLASS,inputArgs);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                //监控线程 睡最大等待时间 如果它醒了 这个程序还没执行完 就杀掉
                new Thread(()->{
                    try {
                        Thread.sleep(TIME_OUT);
                        if (runProcess.isAlive()){
                            runProcess.destroy();
                        }
                    } catch (InterruptedException| SecurityException e) {
                        System.out.println("捕获异常了:"+e);
                        throw new RuntimeException(e);
                    }
                }).start();
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "执行");
                //ExecuteMessage executeMessage = ProcessUtils.runInteractProcessAndGetMessage(runProcess, "运行", inputArgs);
                System.out.println(executeMessage);
                executeMessageList.add(executeMessage);
            } catch (IOException | SecurityException e) {
                System.out.println("捕获异常了:"+e);
                return getErrorResponse(e);
            }
        }

        /// 第四步 整理输出结果
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        //我们来取每个用例执行的最大时间--只要有一个用例超时算法就不能算完成需求
        //因为后续判题服务判断超时 如果是最小值没超时 你不能判定全部用例都没超时
        //但是最大值没超时 那么肯定全部用例都没超时
        long maxTime = 0;
        for (ExecuteMessage executeMessage:executeMessageList){
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)){
                executeCodeResponse.setMessage(errorMessage);
                //
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
        }
        executeCodeResponse.setOutputList(outputList);
        //正常运行完成
        if (outputList.size()==executeMessageList.size()){
            executeCodeResponse.setStatus(1);
        }
        JudgeInfo judgeInfo = new JudgeInfo();
        //这个留到判题去填
        //judgeInfo.setMessage();
        /// todo 这里先不实现
        //judgeInfo.setMemory();
        judgeInfo.setTime(maxTime);
        executeCodeResponse.setJudgeInfo(judgeInfo);

        /// 第五步文件清理。执行完之后将文件删掉
        //这是因为 有可能创建文件夹的时候，服务器容量不够了 就没创建成功
        //虽然可能性小 但是我们可以避免
        if (userCodeFile.getParentFile()!=null){
            boolean del = FileUtil.del(userCodeParentPath);
            System.out.println("删除"+(del?"成功":"失败"));
        }
        /// 第六步 错误处理 提升程序的健壮性
        /// 就是这个getErrorResponse方法


        return executeCodeResponse;
    }

    /**
     * 用于做错误处理的
     * 比如前面的程序 还在编译过程中，程序都没执行就报错了 程序就停了 也就没有 返回ExecuteCodeResponse
     * 那么就返回一个代表错误的ExecuteCodeResponse
     */
    private ExecuteCodeResponse getErrorResponse(Throwable e){
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        executeCodeResponse.setOutputList(new ArrayList<>());
        executeCodeResponse.setMessage(e.getMessage());
        //2表示代码沙箱错误  3是用户的代码执行发生错误
        executeCodeResponse.setStatus(2);
        executeCodeResponse.setJudgeInfo(new JudgeInfo());
        return executeCodeResponse;

    }
}
