package org.allen.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.allen.rpc.RpcClientInvocationHandler;
import org.allen.rpc.registry.ProviderChangeListenerTask;
import org.allen.rpc.registry.ProviderLocation;
import org.allen.rpc.registry.ZkConsumerDiscoverClient;
import org.allen.service.HelloService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RpcConsumerRegistry {
    private Map<Class<?>, Object> consumers;

    private Map<Class<?>, List<ProviderLocation>> providerLocationsConfig;

    private CuratorFramework client;

    private static Thread listenerThread;

    public RpcConsumerRegistry() {
        consumers = new HashMap<>();
        providerLocationsConfig = new HashMap<>();
        client = CuratorFrameworkFactory.newClient("localhost:2181", new ExponentialBackoffRetry(1000, 3));
        client.start();
        register();
//        zkConsumerDiscoverClient.closeZkClient();
    }

    private void register() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("consumer.json");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        try {
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line.trim());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        String content = stringBuilder.toString();
        JSONObject jsonObject = JSON.parseObject(content);
        JSONArray providers = jsonObject.getJSONArray("consumers");
        for (Object provider : providers) {
            JSONObject json = (JSONObject) provider;
            String interfaceName = json.getString("interface");
            try {
                Class<?> iClass = Class.forName(interfaceName);
//                List<ProviderLocation> providerLocations = zkConsumerDiscoverClient.discoverProvider(iClass);
                List<ProviderLocation> providerLocations = new ArrayList<>();
                providerLocationsConfig.put(iClass, providerLocations);
                consumers.put(iClass, Proxy.newProxyInstance(iClass.getClassLoader(), new Class[]{HelloService.class}, new RpcClientInvocationHandler(providerLocations)));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 把从zk发现服务的任务交给监听zk的线程
        ProviderChangeListenerTask providerChangeListenerTask = new ProviderChangeListenerTask(client, providerLocationsConfig);
        listenerThread = new Thread(providerChangeListenerTask, "zkProviderListener");
        listenerThread.start();
    }

    public Object getConsumer(Class<?> clazz) {
        return consumers.get(clazz);
    }

    public Map<Class<?>, List<ProviderLocation>> getProviderLocationsConfig() {
        return providerLocationsConfig;
    }
}
