package com.andyron.demo22;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.websocket.server.PathParam;
import java.util.List;

@RestController
@RequestMapping("/")
public class MainController {

    @Autowired
    private UserMapper userMapper;

    @RequestMapping("/index")
    public Object index() {
        List<User> userList = userMapper.getUserList();
        for (User user : userList) {
            System.out.println(user.toString());
        }
        return userList;

    }

    @GetMapping("/user/{id}")
    public Object user(@PathVariable Long id) {
        User user = userMapper.getUserById(id);
        System.out.println(user);
        return user;


    }


}
