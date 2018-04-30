package org.allen.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

public class RpcClientHandler extends ChannelInboundHandlerAdapter {

    private Object returnResult = null;

    public boolean hasResult = false;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            ByteBuf buf = (ByteBuf) msg;
            String rpcResult = buf.toString(CharsetUtil.UTF_8);
            this.returnResult = rpcResult;
            this.hasResult = true;
        } finally {
            ReferenceCountUtil.release(msg); //释放msg引用
        }
    }

    public Object getReturnResult() {
        return returnResult;
    }
}
