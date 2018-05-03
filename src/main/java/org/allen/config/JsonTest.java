package org.allen.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Deprecated
public class JsonTest {
    public static void main(String[] args) throws Exception{
        InputStream inputStream = JsonTest.class.getClassLoader().getResourceAsStream("provider.json");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder stringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            stringBuilder.append(line.trim());
        }
        String content = stringBuilder.toString();
        JSONObject jsonObject = JSON.parseObject(content);
        JSONArray providers = jsonObject.getJSONArray("providers");
        Map<String, Class> providerMap = new HashMap<>();
        for (Object provider : providers) {
            JSONObject json = (JSONObject) provider;
            String interfaceName = json.getString("interface");
            String className = json.getString("class");
            Class<?> cClass = Class.forName(className);
            providerMap.put(interfaceName, cClass);
        }
        return;
    }
}
