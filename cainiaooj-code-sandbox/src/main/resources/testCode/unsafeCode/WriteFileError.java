import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 往服务器写文件
 */
public class Main{
    public static void main(String[] args) throws InterruptedException {
        String userDir = System.getProperty("user.dir");
        String filePath = userDir.File.separator+"src/main/resources/木马程序.bat";
        String errorProgram = "java -version 2>&1";
        Files.write(Paths.get(filePath), Arrays.asList(errorProgram));
        System.out.println("写木马成功你玩了");
    }
}

/**
 * 使用一个代码的黑白名单
 * 如果在黑名单上的代码就不让执行，比如写文件，读文件，网络操作
 *
 * 这种的缺点是：你不可能想的出所有的黑名单
 * 不同的编程语言对应的关键词都不一样 -- 你如果全部搜集成本太大了
 */

/**
 * 所以换一种方法 直接限制用户的操作权限
 * 限制 用户对文件 内存 CPU  网络 等资源的操作和访问
 *
 * JAVA安全管理器Security Manager来实现-- JAVA提供的保护JVM，java安全的机制
 *
 * 但是这个比较麻烦的是 你得设置白名单，因为它把所有的权限都禁止了
 * 你得把你自己的需要的文件权限给他放开，不然它读取类都读取不了
 * 而且java17已经废弃Security Manager了
 * 所以说最后还是得上docker
 *
 */

