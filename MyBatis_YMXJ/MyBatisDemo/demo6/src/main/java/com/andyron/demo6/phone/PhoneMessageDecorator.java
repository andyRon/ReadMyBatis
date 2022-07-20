package com.andyron.demo6.phone;

public class PhoneMessageDecorator implements Phone {
    private Phone decoratedPhone;

    @Override
    public String callIn() {
        return decoratedPhone.callIn();
    }

    @Override
    public Boolean callOut(String info) {
        return decoratedPhone.callOut(info);
    }

    public String receiveMessage() {
        // ...
        return "receive message";
    }

    public String sendMessage() {
        // ...
        return "send message";
    }


}
