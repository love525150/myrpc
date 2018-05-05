package org.allen.rpc;

import org.allen.dto.Greeting;
import org.allen.service.HelloService;

import java.lang.reflect.Proxy;

/**
 * @author Zhou Zhengwen
 */
public class RpcClient {
    public static void main(String[] args) {
        Object o = Proxy.newProxyInstance(HelloService.class.getClassLoader(), new Class[]{HelloService.class}, new RpcClientInvocationHandler());
        HelloService helloService = (HelloService) o;
//        String s = helloService.sayHello();
        Greeting greeting = helloService.sayGreeting();
        System.out.println(greeting.getWord());
    }
}
