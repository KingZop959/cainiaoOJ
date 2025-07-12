package com.yupi.yuojcodesandbox.Security;
import java.net.InetAddress;
import java.security.Permission;

public class SandboxSecurityManager extends SecurityManager {

    @Override
    public void checkPermission(Permission perm) {
        // 针对文件操作，禁止写入和删除操作
        if (perm instanceof java.io.FilePermission) {
            String actions = perm.getActions();
            if (actions.contains("write") || actions.contains("delete")) {
                throw new SecurityException("File write or delete operations are prohibited: " + perm.getName());
            }
        }
        // 针对系统属性写操作也禁止（写操作可能影响全局环境）
        if (perm instanceof java.util.PropertyPermission) {
            String actions = perm.getActions();
            if (actions.contains("write")) {
                throw new SecurityException("System property write operations are prohibited: " + perm.getName());
            }
        }
        // 其他操作放行（包括读取系统属性、文件读取等）
        // 注意：我们不调用 super.checkPermission(perm) 以避免默认策略产生额外限制，
        // 从而达到“默认放开所有”权限的效果。
    }

    @Override
    public void checkPermission(Permission perm, Object context) {
        checkPermission(perm);
    }

    // 禁止所有网络操作
    @Override
    public void checkConnect(String host, int port) {
        throw new SecurityException("Network operations are prohibited: connect to " + host + ":" + port);
    }

    @Override
    public void checkConnect(String host, int port, Object context) {
        throw new SecurityException("Network operations are prohibited: connect to " + host + ":" + port);
    }

    @Override
    public void checkAccept(String host, int port) {
        throw new SecurityException("Network operations are prohibited: accept connection from " + host + ":" + port);
    }

    @Override
    public void checkListen(int port) {
        throw new SecurityException("Network operations are prohibited: listening on port " + port);
    }

    @Override
    public void checkMulticast(InetAddress maddr) {
        throw new SecurityException("Network operations are prohibited: multicast " + maddr);
    }

    @Override
    public void checkMulticast(InetAddress maddr, byte ttl) {
        throw new SecurityException("Network operations are prohibited: multicast " + maddr);
    }

    // 禁止创建新进程
    @Override
    public void checkExec(String cmd) {
        throw new SecurityException("Process creation is prohibited: " + cmd);
    }
}
