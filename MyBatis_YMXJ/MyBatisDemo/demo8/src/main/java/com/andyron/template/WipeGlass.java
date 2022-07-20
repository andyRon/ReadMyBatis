package com.andyron.template;

public class WipeGlass extends Cleaning {
    @Override
    void prepare() {
        System.out.println("找到抹布");
        System.out.println("洗抹布");
    }

    @Override
    void implement() {
        System.out.println("擦玻璃");
    }

    @Override
    void windup() {
        System.out.println("清理窗台");
    }
}
