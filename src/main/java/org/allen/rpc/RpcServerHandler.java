package org.allen.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import org.allen.config.RpcProviderRegistry;
import org.allen.util.ObjectAndByteUtil;
import org.allen.util.RpcUrlParseUtil;

import java.lang.reflect.Method;


public class RpcServerHandler extends ChannelInboundHandlerAdapter {

    private RpcProviderRegistry rpcProviderRegistry;

    public RpcServerHandler(RpcProviderRegistry rpcProviderRegistry) {
        this.rpcProviderRegistry = rpcProviderRegistry;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        String url = in.toString(CharsetUtil.UTF_8);
        String iName = RpcUrlParseUtil.parseInterfaceName(url);
        Class<?> providerClass = rpcProviderRegistry.getProviderClass(iName);
        String methodName = RpcUrlParseUtil.parseMethodName(url);
        Method method = providerClass.getMethod(methodName);
        Object returnObj = method.invoke(providerClass.getDeclaredConstructor().newInstance());
        Thread.sleep(2000);
        System.out.println(returnObj);
        byte[] returnBytes = ObjectAndByteUtil.convertObjectToByteArray(returnObj);
        ctx.writeAndFlush(Unpooled.copiedBuffer(returnBytes));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
