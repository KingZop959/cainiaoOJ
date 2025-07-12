import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 往服务器写木马程序之后 运行,或者直接执行电脑上的其他程序
 */
public class Main {
    public static void main(String[] args) throws InterruptedException {
        String userDir = System.getProperty("user.dir");
        String filePath = userDir.File.separator + "src/main/resources/木马程序.bat";
        Process exec = Runtime.getRuntime().exec(filePath);
        int exitValue = runProcess.waitFor();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));
        String compileOutputLine;
        while ((compileOutputLine = bufferedReader.readLine()) != null) {
            System.out.println(compileOutputLine);
        }
    }
}