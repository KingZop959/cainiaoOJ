package com.yupi.yuojcodesandbox;


import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.yupi.yuojcodesandbox.model.ExecuteCodeRequest;
import com.yupi.yuojcodesandbox.model.ExecuteCodeResponse;
import com.yupi.yuojcodesandbox.model.ExecuteMessage;
import com.yupi.yuojcodesandbox.model.JudgeInfo;
import com.yupi.yuojcodesandbox.utils.ProcessUtils;
import lombok.extern.slf4j.Slf4j;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
/**
 * 我们沙箱实现--原生实现和Docker实现其中大部分流程是一样的
 * 只有少部分不一样
 * 所以我们可以用模板设计模式
 * 把一样的提取出来成为一个方法
 *
 * 这个模板类基于nativeSandbox提取
 *
 * dockerSandbox实现的时候 可以将不一样的方法模块拿出来重写
 */
@Slf4j
public abstract class JavaCodeSandboxTemplate implements CodeSandbox {

    private static final String GLOBAL_CODE_DIR_NAME = "tmpCode";
    private static final String GLOBAL_JAVA_CLASS_NAME = "Main.java";
    private static final long TIME_OUT = 5000L;
    private static final boolean FIRST_INIT = true;
    private static final String USER_DIR = System.getProperty("user.dir");
    private static final String GLOBAL_CODE_PATH_NAME = USER_DIR + File.separator + GLOBAL_CODE_DIR_NAME;
    private static final String SECURITY_MANAGER_PATH ="E:\\ZLY\\ojSystem\\yuoj-code-sandbox\\src\\main\\resources\\security";
    private static final String SECURITY_MANAGER_CLASS ="SandboxSecurityManager";
    /**
     * 1.将用户的代码保存为文件
     */
    public File saveCodeToFile(String code) {
        /// 第一步将用户的代码保存为文件

        // 这个是拿到项目的根目录
        //String userDir = System.getProperty("user.dir");
        // 这个tmpCode是拿来临时保存用户要执行的代码的文件夹
        // 这个File.separator在window中是\\,他会根据不同系统变为不同的
        // 别直接写\\
        // 因为这个\\是在window的，在linux是/
        // 我们这个程序应该要能部署到所有的系统才行
        //String globalCodePathName = userDir+ File.separator+ GLOBAL_CODE_DIR_NAME;

        // 判断全局代码目录是否存在，第一次启动还没创建肯定不存在，那么就要创建
        if (!FileUtil.exist(GLOBAL_CODE_PATH_NAME)) {
            FileUtil.mkdir(GLOBAL_CODE_PATH_NAME);
        }

        // 把用户的代码隔离存放--每个用户的是隔离的
        String userCodeParentPath = GLOBAL_CODE_PATH_NAME + File.separator + UUID.randomUUID();
        //这是实际的文件Main.java
        String userCodePath = userCodeParentPath + File.separator + GLOBAL_JAVA_CLASS_NAME;
        // 写文件了
        File userCodeFile = FileUtil.writeString(code, userCodePath, StandardCharsets.UTF_8);
        return userCodeFile;
    }

    /**
     * 2. 将保存的用户代码文件编译
     */
    public ExecuteMessage compileFile(File userCodeFile) {
        /// 第二步 编译代码 得到class文件
        String compileCmd = String.format("javac -encoding utf-8 %s", userCodeFile.getAbsoluteFile());
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
    public List<ExecuteMessage> runFile(List<String> inputList, File userCodeFile) {
        String userCodeParentPath = userCodeFile.getParentFile().getAbsolutePath();
        /// 第三步执行程序--依然是命令行
        List<ExecuteMessage> executeMessageList = new ArrayList<>();
        // -cp 的意思是 .class文件的path 所以这里使用的是Main.java的目录也就是userCodeParentPath
        // 后一个参数就是 输入用例
        for (String inputArgs : inputList) {
            String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s;%s -Djava.security.manager=%s Main %s",userCodeParentPath,SECURITY_MANAGER_PATH,SECURITY_MANAGER_CLASS,inputArgs);
           // String runCmd = String.format("java -Xmx256m -Dfile.encoding=UTF-8 -cp %s Main %s", userCodeParentPath, inputArgs);
            try {
                Process runProcess = Runtime.getRuntime().exec(runCmd);
                //监控线程 睡最大等待时间 如果它醒了 这个程序还没执行完 就杀掉
                new Thread(() -> {
                    try {
                        Thread.sleep(TIME_OUT);
                        if (runProcess.isAlive()) {
                            runProcess.destroy();
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
                ExecuteMessage executeMessage = ProcessUtils.runProcessAndGetMessage(runProcess, "执行");
                //ExecuteMessage executeMessage = ProcessUtils.runInteractProcessAndGetMessage(runProcess, "运行", inputArgs);
                if (executeMessage.getExitValue() != 0) {
                    throw new RuntimeException("程序执行了异常代码");
                }
                System.out.println(executeMessage);
                executeMessageList.add(executeMessage);
            } catch (Exception e) {
                throw new RuntimeException("程序执行异常:" + e);
            }
        }
        return executeMessageList;
    }

    /**
     * 5.整理输出结果
     */
    public ExecuteCodeResponse getExecuteResponse(List<ExecuteMessage> executeMessageList) {
        ExecuteCodeResponse executeCodeResponse = new ExecuteCodeResponse();
        List<String> outputList = new ArrayList<>();
        //我们来取每个用例执行的最大时间--只要有一个用例超时算法就不能算完成需求
        //因为后续判题服务判断超时 如果是最小值没超时 你不能判定全部用例都没超时
        //但是最大值没超时 那么肯定全部用例都没超时
        long maxTime = 0;
        for (ExecuteMessage executeMessage : executeMessageList) {
            String errorMessage = executeMessage.getErrorMessage();
            if (StrUtil.isNotBlank(errorMessage)) {
                executeCodeResponse.setMessage(errorMessage);
                //
                executeCodeResponse.setStatus(3);
                break;
            }
            //如果每一条输入用例都正常返回了一个值 没有那些运行中错误那就可以返回了
            //这里的正常返回指得只是程序正常执行完返回了一个结果而没有报错 但是对不对我们不知道也不需要知道
            outputList.add(executeMessage.getMessage());

            Long time = executeMessage.getTime();
            if (time != null) {
                maxTime = Math.max(maxTime, time);
            }
        }
        executeCodeResponse.setOutputList(outputList);
        //正常运行完成
        if (outputList.size() == executeMessageList.size()) {
            executeCodeResponse.setStatus(1);
        }
        JudgeInfo judgeInfo = new JudgeInfo();
        //这个留到判题去填
        //judgeInfo.setMessage();
        /// todo 这里先不实现
        //judgeInfo.setMemory();
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
            System.out.println("删除" + (del ? "成功" : "失败"));
        }
        return del;
    }

    @Override
    public ExecuteCodeResponse executeCode(ExecuteCodeRequest executeCodeRequest) {
        List<String> inputList = executeCodeRequest.getInputList();
        String code = executeCodeRequest.getCode();
        String language = executeCodeRequest.getLanguage();

        File userCodeFile = saveCodeToFile(code);
        try {
            ExecuteMessage compileFileExecuteMessage = compileFile(userCodeFile);
        } catch (Exception e) {
            fileClean(userCodeFile);
            throw new RuntimeException(e);
        }

        List<ExecuteMessage> executeMessageList;
        try {
            //执行代码
            executeMessageList = runFile(inputList, userCodeFile);
        } catch (Exception e) {
            fileClean(userCodeFile);
            throw new RuntimeException(e);
        }

            //整理结果
            ExecuteCodeResponse executeResponse = getExecuteResponse(executeMessageList);

            //文件清理
            boolean deleteSucced = fileClean(userCodeFile);
            if (!deleteSucced) {
                log.error("deleteFile error,userCodeFile:{}", userCodeFile.getAbsoluteFile());
            }

            return executeResponse;
        }

}

