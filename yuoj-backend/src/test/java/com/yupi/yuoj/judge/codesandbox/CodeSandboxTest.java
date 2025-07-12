package com.yupi.yuoj.judge.codesandbox;

import com.yupi.yuoj.judge.codesandbox.impl.ExampleCodeSandbox;
import com.yupi.yuoj.judge.codesandbox.model.ExecuteCodeRequest;
import com.yupi.yuoj.judge.codesandbox.model.ExecuteCodeResponse;
import com.yupi.yuoj.model.enums.QuestionSubmitLanguageEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

/**
 *
 * 测试怎么调用代码沙箱
 */
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class CodeSandboxTest {
    @Test
    void executeCode(){
        /**
         * 在这里，我们虽然是解耦了远程沙箱，第三方沙箱，示例沙箱
         * 但是我们选择我们要用哪个的时候是不是还是new你要用的那个实现类
         * 如果我现在代码里面写的是new RemoteSandbox，
         * 有一天我要改成用其他人的沙箱，我是不是得改这个源代码啊
         * 如果我源代码里面已经接入了别人的代码沙箱
         * 我项目都上线了
         * 我提供给用户选 用哪个沙箱都可以
         * 我怎么做到
         * 我已经打包好了我怎么改源代码
         * 我们可以通过一种方法 传入一个参数 根据这个参数选择new得这个对象是哪个实现类
         *
         * 这个就叫工厂模式
         */
        CodeSandbox codeSandbox = new ExampleCodeSandbox();
        String code = "int main(){ }";
        String language = QuestionSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2","3 4");
        /// 这个...的链式写法 就是 建造者模式 我们就不用每个都去set()了
        /// 创建的时候就一次性传递完了
        /// 也不用去针对每个参数数量写不同的构造方法
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);
    }

    @Test
    void executeCodeFactory(){
        /**
         * 模拟用法 单元测试不让scanner
         *
         * 我们可以通过controller接收参数
         * 也可以通过配置文件
         */
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()){
            String type = scanner.next();
            CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
            String code = "int main(){ }";
            String language = QuestionSubmitLanguageEnum.JAVA.getValue();
            List<String> inputList = Arrays.asList("1 2","3 4");
            ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                    .code(code)
                    .language(language)
                    .inputList(inputList)
                    .build();
            ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        }

    }
    @Value("${CodeSandbox.type}")
    private String type;
    @Test
    void executeCodeByValue(){
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        String code = "int main(){ }";
        String language = QuestionSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2","3 4");
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse = codeSandbox.executeCode(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);
    }

    @Test
    void executeCodeByProxy(){
        CodeSandbox codeSandbox = CodeSandboxFactory.newInstance(type);
        CodeSandboxProxy codeSandboxProxy = new CodeSandboxProxy(codeSandbox);
        String code = "public class Main {\n" +
                "    public static void main(String[] args) {\n" +
                "        int a = Integer.parseInt(args[0]);\n" +
                "        int b = Integer.parseInt(args[1]);\n" +
                "        System.out.println(\"结果:\"+(a+b));\n" +
                "\n" +
                "    }\n" +
                "}";
        String language = QuestionSubmitLanguageEnum.JAVA.getValue();
        List<String> inputList = Arrays.asList("1 2","3 4");
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder()
                .code(code)
                .language(language)
                .inputList(inputList)
                .build();
        ExecuteCodeResponse executeCodeResponse =  codeSandboxProxy.executeCode(executeCodeRequest);
        Assertions.assertNotNull(executeCodeResponse);
    }
}