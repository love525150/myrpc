package org.allen.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodInvocationUtil {

    public static Object invokeFromUrl(String content) {
        int index1 = content.indexOf("?");
        String className = content.substring(0, index1);
        String methodName = content.substring(index1 + 1, content.length());
        Object returnObject = null;
        try {
            Class<?> clazz = Class.forName(className);
            Method method = clazz.getMethod(methodName);
            returnObject = method.invoke(clazz.getDeclaredConstructor().newInstance());
        } catch (ClassNotFoundException e) {
            System.out.println("没有找到此类：" + className);
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            System.out.println("没有找到此方法：" + methodName);
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return returnObject;
    }
}
