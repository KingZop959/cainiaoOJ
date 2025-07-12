package com.yupi.yuoj.judge.codesandbox;

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

import com.yupi.yuoj.judge.codesandbox.impl.ExampleCodeSandbox;
import com.yupi.yuoj.judge.codesandbox.impl.RemoteCodeSandbox;
import com.yupi.yuoj.judge.codesandbox.impl.ThirdPartyCodeSandbox;

/**
 * 代码沙箱工厂--工厂模式--这里用的静态工厂模式,还有其他的工厂模式
 * 根据传入得字符串参数创建指定的代码沙箱
 */

public class CodeSandboxFactory {
    /**
     * 返回创建好的代码沙箱
     * 很简单 就是switch就行了
     *
     * 拓展--如果确定代码沙箱的示例不会出现线程安全问题，可复用
     * 可以使用单例工厂模式
     */
    public static CodeSandbox newInstance(String type){
        switch (type){
            case "example":
                return new ExampleCodeSandbox();
            case "remote":
                return new RemoteCodeSandbox();
            case "thirdParty":
                return new ThirdPartyCodeSandbox();
            default:
                return new ExampleCodeSandbox();
        }
    }

}
