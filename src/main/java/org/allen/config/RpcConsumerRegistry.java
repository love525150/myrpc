package org.allen.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.allen.rpc.RpcClientInvocationHandler;
import org.allen.rpc.registry.ProviderLocation;
import org.allen.rpc.registry.ZkConsumerDiscover;
import org.allen.service.HelloService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RpcConsumerRegistry {
    private Map<Class<?>, Object> consumers;

    private ZkConsumerDiscover zkConsumerDiscover;

    public RpcConsumerRegistry() {
        consumers = new HashMap<>();
        zkConsumerDiscover = new ZkConsumerDiscover();
        register();
        zkConsumerDiscover.closeZkClient();
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
                List<ProviderLocation> providerLocations = zkConsumerDiscover.discoverProvider(iClass);
                Object o = Proxy.newProxyInstance(iClass.getClassLoader(), new Class[]{HelloService.class}, new RpcClientInvocationHandler(providerLocations));
                consumers.put(iClass, o);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Object getConsumer(Class<?> clazz) {
        return consumers.get(clazz);
    }
}
