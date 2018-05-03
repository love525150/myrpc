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
import org.allen.config.ProviderRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Zhou Zhengwen
 */
public class RpcServer {
    private static ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    public void start() throws Exception{
        ProviderRegistry providerRegistry = registerProviders();

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap sb = new ServerBootstrap();
            sb.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            socketChannel.pipeline().addLast(new RpcServerHandler(providerRegistry));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture f = sb.bind(8080).sync();

            f.channel().closeFuture().sync();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private ProviderRegistry registerProviders() throws IOException, ClassNotFoundException {
        InputStream inputStream = JsonTest.class.getClassLoader().getResourceAsStream("provider.json");
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
        return new ProviderRegistry(providerMap);
    }

    public static void main(String[] args) throws Exception{
        new RpcServer().start();
    }
}
