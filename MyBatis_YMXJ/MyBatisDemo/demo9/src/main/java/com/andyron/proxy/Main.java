package com.andyron.proxy;

public class Main {
    public static void main(String[] args) {
        // 生成被代理对象
        User user = new User();

        // 生成代理对象
        UserProxy userProxy = new UserProxy(user);

        // 触发代理fangf
        userProxy.sayHello("andy");
    }
}
