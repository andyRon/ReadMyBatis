package com.andyron.ch09.entity;

import lombok.Data;

import java.util.Date;

@Data
public class User {
    private Long id;
    private String name;
    private Date createTime;
    private String password;
    private String phone;
    private String nickName;
}
