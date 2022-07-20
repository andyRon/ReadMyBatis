package com.andyron.demo6.phone;

/**
 * 装饰类
 * 添加录音功能
 *
 */
public class PhoneRecordDecorator implements Phone {
    private Phone decoratedPhone;

    public PhoneRecordDecorator(Phone decoratedPhone) {
        this.decoratedPhone = decoratedPhone;
    }

    @Override
    public String callIn() {
        System.out.println("启动录音...");
        String s = decoratedPhone.callIn();
        System.out.println("结束录音并保存...");
        return s;
    }

    @Override
    public Boolean callOut(String info) {
        System.out.println("启动录音...");
        Boolean r = decoratedPhone.callOut(info);
        System.out.println("结束录音并保存...");
        return r;
    }
}
