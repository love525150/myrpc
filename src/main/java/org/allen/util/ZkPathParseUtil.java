package org.allen.util;

import org.allen.rpc.registry.ProviderLocation;

/**
 * @author Zhou Zhengwen
 */
public class ZkPathParseUtil {

    public static ProviderLocation parseProviderPath(String path) {
        int index = path.lastIndexOf("/");
        String[] split = path.substring(index + 1).split(":");
        String ip = split[0];
        String port = split[1];
        return new ProviderLocation(ip, Integer.parseInt(port));
    }
}
