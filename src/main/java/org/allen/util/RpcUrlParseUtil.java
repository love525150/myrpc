package org.allen.util;

public class RpcUrlParseUtil {

    public static String parseInterfaceName(String rpcUrl) {
        int index1 = rpcUrl.indexOf("?");
        return rpcUrl.substring(0, index1);
    }

    public static String parseMethodName(String rpcUrl) {
        int index1 = rpcUrl.indexOf("?");
        return rpcUrl.substring(index1 + 1, rpcUrl.length());
    }
}
