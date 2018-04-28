package org.allen.rpc;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author Zhou Zhengwen
 */
public class TestClient {
    public static void main(String[] args) throws Exception{
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("127.0.0.1", 8080));
        OutputStream outputStream = socket.getOutputStream();
        outputStream.write("org.allen.service.HelloServiceImpl?sayHello".getBytes());
        outputStream.flush();
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
        Object o = objectInputStream.readObject();
        System.out.println(o);
    }
}
