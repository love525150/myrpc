package org.allen.config;

import java.util.Map;
import java.util.Set;

public class RpcProviderRegistry {
    private Map<String, Class<?>> providerMap;

    public RpcProviderRegistry(Map<String, Class<?>> providerMap) {
        this.providerMap = providerMap;
    }

    public Class getProviderClass(String interfaceName) {
        return providerMap.get(interfaceName);
    }

    public Set<String> getAllInterfaceNames() {
        return providerMap.keySet();
    }
}
