package org.allen.rpc.registry;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;

public class CuratorTest {
    private CuratorFramework client;

    @Before
    public void before() {
        CuratorFramework client = CuratorFrameworkFactory.newClient("localhost:2181", new ExponentialBackoffRetry(1000, 3));
        client.start();
        this.client = client;
    }

    @Test
    public void testCreate() throws Exception {
        Stat stat = client.checkExists().forPath("/test");
        System.out.println(stat);
    }
}