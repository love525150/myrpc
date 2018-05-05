package org.allen.service;

import org.allen.dto.Greeting;

/**
 * @author Zhou Zhengwen
 */
public class HelloServiceImpl implements HelloService{

    @Override
    public String sayHello() {
        return "hello";
    }

    @Override
    public Greeting sayGreeting() {
        Greeting greeting = new Greeting();
        greeting.setWord("Hello");
        return greeting;
    }
}
