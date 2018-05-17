package org.allen.rpc;

import org.allen.config.RpcConsumerRegistry;
import org.allen.dto.Greeting;
import org.allen.service.HelloService;

import java.lang.reflect.Proxy;

/**
 * @author Zhou Zhengwen
 */
public class RpcClient {
    public static void main(String[] args) {
        RpcConsumerRegistry rpcConsumerRegistry = new RpcConsumerRegistry();
        HelloService helloService = (HelloService) rpcConsumerRegistry.getConsumer(HelloService.class);
        Greeting greeting = helloService.sayGreeting();
        System.out.println(greeting.getWord());
        Greeting greeting2 = helloService.sayGreeting();
        System.out.println(greeting2.getWord());
    }
}
