package com.andyron.ch16;

import com.andyron.entity.User;
import org.apache.ibatis.ognl.Ognl;
import org.apache.ibatis.ognl.OgnlException;

import java.util.HashMap;
import java.util.Map;

public class OgnlTest {
    public static void main(String[] args) throws OgnlException {
        User user1 = new User(1, "andy", 18);
        User user2 = new User(2, "jack", 19);
        Map<String, User> userMap = new HashMap<>();
        userMap.put("user1", user1);
        userMap.put("user2", user2);

        // 读取根对象的属性值
        Integer age = (Integer) Ognl.getValue("age", userMap.get("user1"));
        System.out.println(age);

        // 设置根对象的属性
        Ognl.getValue("age=22", userMap.get("user1"));
        age  = (Integer) Ognl.getValue("age", userMap.get("user1"));
        System.out.println(age);

        String userName2 = (String) Ognl.getValue("#user2.name", userMap, new Object());
        System.out.println("读取环境中的信息，得到user2的name：" + userName2);

        // 读取环境中信息，并进行判断
//        Boolean res = (Boolean) Ognl.getValue("#user2.name==`jack`", userMap, new Object());
//        System.out.println(res);

        // 设置环境中信息
        Ognl.getValue("#user2.name=`小明`", userMap, new Object());  // 🔖
        String newUserName2 = (String) Ognl.getValue("#user2.name", userMap, new Object());
        System.out.println(newUserName2);
    }
}
