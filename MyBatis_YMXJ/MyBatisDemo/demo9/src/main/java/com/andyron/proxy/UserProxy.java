package com.andyron.proxy;

public class UserProxy implements UserInterface {
    private UserInterface target;

    public UserProxy(UserInterface user) {
        this.target = user;
    }

    @Override
    public String sayHello(String name) {
        System.out.println("打招呼开场白");
        target.sayHello(name);
        System.out.println("打招呼结束语");
        return null;
    }
}
