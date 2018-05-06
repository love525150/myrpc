package org.allen.rpc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.allen.config.JsonTest;
import org.allen.config.RpcProviderRegistry;
import org.allen.rpc.registry.ZkProviderRegister;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Zhou Zhengwen
 */
public class RpcServer {

    public static final int RPC_PORT = 8080;

    public void start() throws Exception{
        RpcProviderRegistry rpcProviderRegistry = registerProviders();

        new ZkProviderRegister(rpcProviderRegistry, RPC_PORT).doRegister();

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap sb = new ServerBootstrap();
            sb.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new RpcServerHandler(rpcProviderRegistry));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = sb.bind(RPC_PORT).sync();

            f.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private RpcProviderRegistry registerProviders() throws IOException, ClassNotFoundException {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("provider.json");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line.trim());
        }
        String content = stringBuilder.toString();
        JSONObject jsonObject = JSON.parseObject(content);
        JSONArray providers = jsonObject.getJSONArray("providers");
        Map<String, Class<?>> providerMap = new HashMap<>();
        for (Object provider : providers) {
            JSONObject json = (JSONObject) provider;
            String interfaceName = json.getString("interface");
            String className = json.getString("class");
            Class<?> cClass = Class.forName(className);
            providerMap.put(interfaceName, cClass);
        }
        return new RpcProviderRegistry(providerMap);
    }

    public static void main(String[] args) throws Exception{
        new RpcServer().start();
    }
}
