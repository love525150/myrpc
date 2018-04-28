package org.allen.rpc;

import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * @author Zhou Zhengwen
 */
public class RpcClientHandler implements InvocationHandler {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("127.0.0.1", 8080));
        OutputStream outputStream = socket.getOutputStream();
        Class<?>[] interfaces = proxy.getClass().getInterfaces();
        String interfaceName = interfaces[0].getName();
        String implementClassName = interfaceName + "Impl";
        String queryString = implementClassName + "?" + method.getName();
        outputStream.write(queryString.getBytes());
        outputStream.flush();
        ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());
        Object o = objectInputStream.readObject();
        return o;
    }
}
