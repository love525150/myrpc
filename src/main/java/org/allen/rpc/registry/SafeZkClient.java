package org.allen.rpc.registry;

import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;

public class SafeZkClient {
    private CuratorFramework client;

    public SafeZkClient(CuratorFramework client) {
        this.client = client;
    }

    public void safeCreate(String path, CreateMode createMode) throws Exception{
        if (client.checkExists().forPath(path) == null) {
            client.create().creatingParentsIfNeeded().withMode(createMode).forPath(path);
        }
    }

    public void safeCreate(String path, CreateMode createMode, String value) throws Exception{
        if (client.checkExists().forPath(path) == null) {
            client.create().creatingParentsIfNeeded().withMode(createMode).forPath(path, value.getBytes());
        }
    }

    public boolean hasNode(String path) throws Exception{
        return client.checkExists().forPath(path) != null;
    }

    public String safeGet(String path) throws Exception{
        if (client.checkExists().forPath(path) != null) {
            return new String(client.getData().forPath(path));
        }
        return "";
    }

    public boolean safeSet(String path, String value) throws Exception{
        if (client.checkExists().forPath(path) != null) {
            client.setData().forPath(path, value.getBytes());
            return true;
        }
        return false;
    }
}
