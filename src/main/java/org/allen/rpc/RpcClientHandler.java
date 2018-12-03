package org.allen.rpc;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.allen.util.ObjectAndByteUtil;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class RpcClientHandler extends ChannelInboundHandlerAdapter {

    private Object returnResult = null;

    public boolean hasResult = false;

    private Condition condition = null;

    private Lock lock = null;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            ByteBuf buf = (ByteBuf) msg;
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            Object rpcResult = ObjectAndByteUtil.convertByteArrayToObject(bytes);
            this.returnResult = rpcResult;
            lock.lock();
            condition.signalAll();
        } finally {
            lock.unlock();
            ReferenceCountUtil.release(msg); //释放msg引用
        }
    }

    public Object getReturnResult() {
        return returnResult;
    }

    public void acceptCondition(Condition condition) {
        this.condition = condition;
    }

    public void acceptLock(Lock lock) {
        this.lock = lock;
    }
}
