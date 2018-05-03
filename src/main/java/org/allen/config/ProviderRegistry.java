package org.allen.config;

import java.util.Map;

public class ProviderRegistry {
    private Map<String, Class<?>> providerMap;

    public ProviderRegistry(Map<String, Class<?>> providerMap) {
        this.providerMap = providerMap;
    }

    public Class getProviderClass(String interfaceName) {
        return providerMap.get(interfaceName);
    }
}
