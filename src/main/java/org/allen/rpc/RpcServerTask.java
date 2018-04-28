package org.allen.rpc;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.Arrays;

/**
 * @author Zhou Zhengwen
 */
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
            Object result = parse(str);
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

    public Object parse(String content) {
        int index1 = content.indexOf("?");
        String className = content.substring(0, index1);
        String methodName = content.substring(index1 + 1, content.length());
        Object returnObject = null;
        try {
            Class<?> clazz = Class.forName(className);
            Method method = clazz.getMethod(methodName);
            returnObject = method.invoke(clazz.getDeclaredConstructor().newInstance());
        } catch (ClassNotFoundException e) {
            System.out.println("没有找到此类：" + className);
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            System.out.println("没有找到此方法：" + methodName);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return returnObject;
    }
}
