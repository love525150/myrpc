package org.allen.rpc;

import org.allen.util.MethodInvocationUtil;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

/**
 * @author Zhou Zhengwen
 */
@Deprecated
public class RpcServerTask implements Runnable {
    private Socket socket;

    public RpcServerTask(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            byte[] buffer = new byte[1024];
            int read = inputStream.read(buffer);
            byte[] content = Arrays.copyOf(buffer, read);
            String str = new String(content);
            Object result = MethodInvocationUtil.invokeFromUrl(str);
            System.out.println("完成方法调用，结果为：" + result);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectOutputStream.writeObject(result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
