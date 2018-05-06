package org.allen.rpc.registry;

import io.netty.util.internal.StringUtil;
import org.allen.config.RpcProviderRegistry;
import org.allen.util.IpUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.Set;

public class ZkProviderRegister {
    private RpcProviderRegistry rpcProviderRegistry;

    private int rpcPort;

    private CuratorFramework client;

    public ZkProviderRegister(RpcProviderRegistry rpcProviderRegistry, int rpcPort) {
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.rpcPort = rpcPort;
        client = CuratorFrameworkFactory.newClient("localhost:2181", new ExponentialBackoffRetry(1000, 3));
    }

    public void doRegister() throws Exception {
        SafeZkClient safeZkClient = new SafeZkClient(client);
        try {
            client.start();
            String rootPath = "/myrpc";
            safeZkClient.safeCreate(rootPath, CreateMode.PERSISTENT);
            Set<String> interfaceNames = rpcProviderRegistry.getAllInterfaceNames();
            for (String interfaceName : interfaceNames) {
                safeZkClient.safeCreate(rootPath + "/" + interfaceName, CreateMode.PERSISTENT);
                String providerPath = rootPath + "/" + interfaceName + "/" + "providers";
                String providerUrl = IpUtil.getLocalHostAddress() + ":" + rpcPort;
                safeZkClient.safeCreate(providerPath, CreateMode.PERSISTENT);
                safeZkClient.safeCreate(providerPath + "/" + providerUrl, CreateMode.EPHEMERAL);
            }
        } catch (Exception e) {
            e.printStackTrace();
            client.close();
        }
    }
}
