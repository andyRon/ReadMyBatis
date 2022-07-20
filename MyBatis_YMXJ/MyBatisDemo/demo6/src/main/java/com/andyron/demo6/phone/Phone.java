package com.andyron.demo6.phone;

public interface Phone {
    /**
     * 接收
     */
    String callIn();

    /**
     * 发送
     */
    Boolean callOut(String info);
}
