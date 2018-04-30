package org.allen.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author Zhou Zhengwen
 */
public class RpcClientInvocationHandler implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //Object o = invokeByBio(proxy, method);
        Object o = invokeByNio(proxy, method);
        return o;
    }

    private Object invokeByBio(Object proxy, Method method) throws IOException, ClassNotFoundException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("127.0.0.1", 8080));
        OutputStream outputStream = socket.getOutputStream();
        String queryString = getQueryString(proxy, method);
        outputStream.write(queryString.getBytes());
        outputStream.flush();
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
        return objectInputStream.readObject();
    }

    private String getQueryString(Object proxy, Method method) {
        Class<?>[] interfaces = proxy.getClass().getInterfaces();
        String interfaceName = interfaces[0].getName();
        String implementClassName = interfaceName + "Impl";
        return implementClassName + "?" + method.getName();
    }

    private Object invokeByNio(Object proxy, Method method) throws Throwable {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        Object returnResult;
        try {
            Bootstrap b = new Bootstrap(); // (1)
            RpcClientHandler rpcClientHandler = new RpcClientHandler();
            b.group(workerGroup); // (2)
            b.channel(NioSocketChannel.class); // (3)
            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(rpcClientHandler);
                }
            });

            // Start the client.
            ChannelFuture f = b.connect("127.0.0.1", 8080).sync(); // (5)
            Channel channel = f.channel();
            String queryString = getQueryString(proxy, method);
            channel.writeAndFlush(Unpooled.copiedBuffer(queryString.getBytes()));

            // Wait until the connection is closed.
            //channel.closeFuture().sync();
            while (!rpcClientHandler.hasResult) {
                Thread.sleep(50);
            }
            returnResult = rpcClientHandler.getReturnResult();
            channel.close();
        } finally {
            workerGroup.shutdownGracefully();
        }
        return returnResult;
    }
}
