package org.allen.rpc;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;


public class NioTestClient {
    public static void main(String[] args) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

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
            channel.writeAndFlush(Unpooled.copiedBuffer("org.allen.service.HelloServiceImpl?sayHello".getBytes()));

            // Wait until the connection is closed.
            //channel.closeFuture().sync();
            while (!rpcClientHandler.hasResult) {
                System.out.println("waiting result...");
                Thread.sleep(50);
            }
            Object returnResult = rpcClientHandler.getReturnResult();
            System.out.println("rpc result: " + returnResult);
            channel.close();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
