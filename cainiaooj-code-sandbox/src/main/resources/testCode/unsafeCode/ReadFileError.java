import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

/**
 * 获取资源文件中查看你的密码
 */
public class Main{
    public static void main(String[] args) throws InterruptedException{
        String userDir = System.getProperty("user.dir");
        String filePath = userDir + File.separator + "src/main/resources/application.yml";
        try {
            List<String> strings = Files.readAllLines(Paths.get(filePath));
            System.out.println(String.join("\n", strings));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
