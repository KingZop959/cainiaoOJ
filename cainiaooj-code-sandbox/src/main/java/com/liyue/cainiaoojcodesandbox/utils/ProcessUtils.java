package com.liyue.cainiaoojcodesandbox.utils;

import cn.hutool.core.util.StrUtil;
import com.liyue.cainiaoojcodesandbox.model.ExecuteMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.StopWatch;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ProcessUtils {
    public static ExecuteMessage runProcessAndGetMessage(Process runProcess,String opName){
        ExecuteMessage executeMessage = new ExecuteMessage();
        try {
            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            //这是等待程序执行完毕
            //返回值 0 是正常退出 其他的就是异常退出 返回的就是错误码
            int exitValue = runProcess.waitFor();
            executeMessage.setExitValue(exitValue);
            if (exitValue==0){
                System.out.println(opName+"成功");
                //获取控制台的输出信息
                //控制台的输入信息就在输入流里
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                String compileOutputLine;
                List<String> tempOutputStrList = new ArrayList<>();
                while ((compileOutputLine = bufferedReader.readLine())!=null){
                    tempOutputStrList.add(compileOutputLine);
                }
                String outputStrList = StringUtils.join(tempOutputStrList, "\n");
                executeMessage.setMessage(outputStrList);
            }else {
                //异常退出
                System.out.println(opName+"失败,错误码:"+exitValue);
                //获取控制台的输出信息
                //控制台的输入信息就在输入流里--因为java程序编译之后如果有输出在控制台显示--就相当于往控制台程序输入
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
                String compileOutputLine;
                List<String> tempCompileStrList = new ArrayList<>();
                while ((compileOutputLine = bufferedReader.readLine())!=null){
                    tempCompileStrList.add(compileOutputLine);
                }
                executeMessage.setMessage(StringUtils.join(tempCompileStrList,"\n"));
                //获取控制台的输出信息
                //控制台的输入信息就在输入流里
                BufferedReader errorBufferedReader = new BufferedReader(new InputStreamReader(runProcess.getErrorStream()));
                String errorCompileOutputLine;
                List<String> tempErrorOutputStrList = new ArrayList<>();
                while ((errorCompileOutputLine = errorBufferedReader.readLine())!=null){
                    tempErrorOutputStrList.add(errorCompileOutputLine);
                }
                executeMessage.setErrorMessage(StringUtils.join(errorCompileOutputLine,"\n"));
                System.out.println(errorCompileOutputLine);
            }
            stopWatch.stop();
            executeMessage.setTime(stopWatch.getLastTaskTimeMillis());
        }catch (Exception e){
            System.out.println("捕获异常3："+e.getMessage());
            throw new RuntimeException(e);
        }
        return executeMessage;
    }

    /**
     * ACM模式
     * 用Scanner来读取参数的那种程序
     * 我们得先把用例输入进控制台，再去控制台读取
     *
     * 这个只是参考 我们程序 还是以上面的方法为主
     */
    public static ExecuteMessage runInteractProcessAndGetMessage(Process runProcess, String opName,String args) {

        ExecuteMessage executeMessage = new ExecuteMessage();
        try {
            InputStream inputStream = runProcess.getInputStream();

            //这是相当于控制台等待用户输入
            OutputStream outputStream = runProcess.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            //输入--这个\n是回车
            String[] split = args.split(" ");
            String s = StrUtil.join("\n", split) + "\n";
            outputStreamWriter.write(args);
            //按回车
            outputStreamWriter.flush();

            StringBuilder compileOutputStringBuilder = new StringBuilder();
            //获取控制台的输出信息
            //控制台的输入信息就在输入流里
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            String compileOutputLine;
            while ((compileOutputLine = bufferedReader.readLine()) != null) {
                compileOutputStringBuilder.append(compileOutputLine);
            }
            executeMessage.setMessage(compileOutputStringBuilder.toString());
            outputStreamWriter.close();
            outputStream.close();
            inputStream.close();
            runProcess.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return executeMessage;
    }
}
