package org.allen.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.allen.rpc.registry.ProviderLocation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

/**
 * @author Zhou Zhengwen
 */
public class RpcClientInvocationHandler implements InvocationHandler {

    private List<ProviderLocation> providerLocations;

    private int roundRobinIndex = 0;

    public RpcClientInvocationHandler(List<ProviderLocation> providerLocations) {
        this.providerLocations = providerLocations;
    }

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
        return interfaceName + "?" + method.getName();
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
            ProviderLocation providerLocation = roundRobin(providerLocations);
            ChannelFuture f = b.connect(providerLocation.getAddr(), providerLocation.getPort()).sync(); // (5)
            Channel channel = f.channel();
            String queryString = getQueryString(proxy, method);
            channel.writeAndFlush(Unpooled.copiedBuffer(queryString.getBytes()));

            // Wait until the connection is closed.
            //channel.closeFuture().sync();
            int sleepCount = 0;
            while (!rpcClientHandler.hasResult) {
                int sleepInterval = 50;
                Thread.sleep(sleepInterval);
                sleepCount++;
                if (sleepCount * sleepInterval > 5000) throw new RuntimeException("rpc timeout");
            }
            returnResult = rpcClientHandler.getReturnResult();
            channel.close();
        } finally {
            workerGroup.shutdownGracefully();
        }
        return returnResult;
    }

    private ProviderLocation roundRobin(List<ProviderLocation> providerLocations) {
        ProviderLocation providerLocation = providerLocations.get(roundRobinIndex);
        roundRobinIndex++;
        roundRobinIndex = roundRobinIndex % providerLocations.size();
        return providerLocation;
    }
}
