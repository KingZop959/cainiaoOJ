import java.util.ArrayList;

/**
 * 无限占用内存
 * JVM超出了JVM堆内存之后 会自动报错 默认是8G 这是JVM的保护机制
 */
public class Main{
    public static void main(String[] args) throws InterruptedException {
        List<byte[]> w = new ArrayList<>();
        while (true){
            w.add(new byte[10000]);
        }
    }
}
/**
 * 我们不能让每个java进程执行占用的JVM的最大堆内存空间都和系统的一致，实际上应该小一点，比如说256MB
 * JAVA 参数  -Xmx256m  最大堆大小    -Xms初始堆空间大小
 *
 * 他只是说超过了最大堆内存 就会终止程序，但是终止是需要时间的，所以实际占用会超出一些
 * 如果要更严格的限制，需要在系统层面进行对程序进行限制，而不是用JVM来限制
 */