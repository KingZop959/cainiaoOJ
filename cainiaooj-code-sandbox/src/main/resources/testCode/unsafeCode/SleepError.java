
/**
 * 无限睡眠（阻塞程序执行）
 * 占用程序资源不释放
 */
public class Main{
    public static void main(String[] args) throws InterruptedException {
        long ONE_HOUR = 60*30*1000L;
        Thread.sleep(ONE_HOUR);
        System.out.println("睡完了");
    }
}
/**
 * 怎么解决呢？
 * 超时控制
 *
 * 用一个新的线程来监控运行时间 如果超时了就给他终止
 */
