package org.allen.rpc.registry;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.List;
import java.util.stream.Collectors;

public class ZkConsumerDiscover {
    private CuratorFramework client;

    public ZkConsumerDiscover() {
        client = client = CuratorFrameworkFactory.newClient("localhost:2181", new ExponentialBackoffRetry(1000, 3));
        client.start();
    }

    public List<ProviderLocation> discoverProvider(Class iClass) throws Exception{
        String interfaceName = iClass.getName();
        String path = "/myrpc" + "/" + interfaceName + "/providers";
        List<String> strings = client.getChildren().forPath(path);
        return strings.stream().map(s -> {
            String[] split = s.split(":");
            return new ProviderLocation(split[0], Integer.parseInt(split[1]));
        }).collect(Collectors.toList());
    }

    public void closeZkClient() {
        client.close();
    }
}
