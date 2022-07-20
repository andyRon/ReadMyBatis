package com.andyron.demo6.phone;

public class Client {
    public static void main(String[] args) {
        System.out.println("原来Phone没有录音功能");
        TelePhone telePhone = new TelePhone();
        telePhone.callOut("hello! This is andyron");

        System.out.println();

        System.out.println("包装类添加录音功能");
        PhoneRecordDecorator phoneRecordDecorator = new PhoneRecordDecorator(telePhone);
        phoneRecordDecorator.callOut("hello! This is andyron");

    }
}
