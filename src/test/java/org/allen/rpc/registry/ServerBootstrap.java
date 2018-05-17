package org.allen.rpc.registry;

import org.allen.rpc.RpcServer;
import org.junit.Test;

/**
 * @author Zhou Zhengwen
 */
public class ServerBootstrap {

    @Test
    public void startServer() throws Exception{
        new RpcServer(8080).start();
        new RpcServer(8081).start();
    }
}
