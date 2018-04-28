package org.allen.rpc;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Zhou Zhengwen
 */
public class RpcServer {
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    public void start() throws Exception{
        ServerSocket serverSocket = new ServerSocket();
        serverSocket.bind(new InetSocketAddress(8080));
        try {
            while (true) {
                Socket accept = serverSocket.accept();
                System.out.println("接收到一个新的连接");
                executor.execute(new RpcServerTask(accept));
            }
        } finally {
            serverSocket.close();
        }
    }

    public static void main(String[] args) throws Exception{
        new RpcServer().start();
    }
}
