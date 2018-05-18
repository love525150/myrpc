package org.allen.rpc.registry;

import org.allen.config.RpcConsumerRegistry;
import org.allen.dto.Greeting;
import org.allen.service.HelloService;
import org.junit.Test;

import java.util.Scanner;

/**
 * @author Zhou Zhengwen
 */
public class ClientTest {


    public static void main(String[] args) {
        RpcConsumerRegistry rpcConsumerRegistry = new RpcConsumerRegistry();
        HelloService helloService = (HelloService) rpcConsumerRegistry.getConsumer(HelloService.class);
        Scanner scanner = new Scanner(System.in);
        System.out.println("请开始输入:");
        while (scanner.hasNextLine()) {
            String next = scanner.nextLine();
            if (next.equals("exit")) System.exit(0);
            else {
                Greeting greeting = helloService.sayGreeting();
                System.out.println(greeting.getWord());
            }
        }
    }
}
