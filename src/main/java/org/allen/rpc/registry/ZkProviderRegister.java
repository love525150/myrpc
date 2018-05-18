package org.allen.rpc.registry;

import org.allen.config.RpcProviderRegistry;
import org.allen.util.IpUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ZkProviderRegister {
    private static final Logger logger = LoggerFactory.getLogger(ZkProviderRegister.class);

    private RpcProviderRegistry rpcProviderRegistry;

    private int rpcPort;

    private CuratorFramework client;

    public ZkProviderRegister(RpcProviderRegistry rpcProviderRegistry, int rpcPort) {
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.rpcPort = rpcPort;
        logger.info("creating zookeeper client...");
        client = CuratorFrameworkFactory.newClient("localhost:2181", new ExponentialBackoffRetry(1000, 3));
    }

    public void doRegister() throws Exception {
        SafeZkClient safeZkClient = new SafeZkClient(client);
        try {
            client.start();
            logger.info("start zookeeper connection...");
            String rootPath = "/myrpc";
            safeZkClient.safeCreate(rootPath, CreateMode.PERSISTENT);
            Set<String> interfaceNames = rpcProviderRegistry.getAllInterfaceNames();
            for (String interfaceName : interfaceNames) {
                safeZkClient.safeCreate(rootPath + "/" + interfaceName, CreateMode.PERSISTENT);
                String providerPath = rootPath + "/" + interfaceName + "/" + "providers";
                String providerUrl = IpUtil.getLocalHostAddress() + ":" + rpcPort;
                safeZkClient.safeCreate(providerPath, CreateMode.PERSISTENT);
                safeZkClient.safeCreate(providerPath + "/" + providerUrl, CreateMode.EPHEMERAL);
                logger.info("register {} to registry", interfaceName);
            }
            logger.info("finish registration");
        } catch (Exception e) {
            logger.debug("error in registering providers", e);
            client.close();
        }
    }
}
