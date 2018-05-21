package org.allen.rpc.registry;

import org.allen.config.RpcConsumerRegistry;
import org.allen.dto.Greeting;
import org.allen.service.HelloService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
                try {
                    Greeting greeting = helloService.sayGreeting();
                    System.out.println(greeting.getWord());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void testListener() throws Exception{
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181", new ExponentialBackoffRetry(1000, 3));
        client.start();
        PathChildrenCache pathChildrenCache = new PathChildrenCache(client, "/myrpc/" + HelloService.class.getName() + "/providers", true);
        PathChildrenCacheListener childrenCacheListener = new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                    case CHILD_ADDED:
                        ChildData child = event.getData();
                        String path = child.getPath();
                        System.out.println(path);

                        break;
                    case CHILD_REMOVED:
                        break;
                }
            }
        };
        pathChildrenCache.getListenable().addListener(childrenCacheListener);
        System.out.println("Register zk watcher successfully!");
        pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        System.in.read();
    }

    @Test
    public void testThread() {
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181", new ExponentialBackoffRetry(1000, 3));
        client.start();
        HashMap<Class<?>, List<ProviderLocation>> map = new HashMap<>();
        map.put(HelloService.class, new ArrayList<>());
        Thread thread = new Thread(new ProviderChangeListenerTask(client, map));
        thread.start();
        while (true) {

        }
    }

}
