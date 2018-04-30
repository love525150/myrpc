package org.allen.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import org.allen.util.MethodInvocationUtil;
import org.allen.util.ObjectAndByteUtil;


public class RpcServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        String url = in.toString(CharsetUtil.UTF_8);
        Object returnObj = MethodInvocationUtil.invokeFromUrl(url);
        System.out.println(returnObj);
//        byte[] bytes = ObjectAndByteUtil.convertObjectToBytes(returnObj);
        String str = (String) returnObj;
        ctx.writeAndFlush(Unpooled.copiedBuffer(str.getBytes()));
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        cause.printStackTrace();
        ctx.close();
    }
}
