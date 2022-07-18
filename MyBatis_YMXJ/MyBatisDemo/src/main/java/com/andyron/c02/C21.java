package com.andyron.c02;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class C21 {
    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        // 1
        Class.forName("com.mysql.cj.jdbc.Driver");
        // 2
        Connection conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/jdbc?useSSL=false&useUnicode=true&characterEncoding=UTF-8&allowPublicKeyRetrieval=true", "root", "iop654321");
        // 3
        Statement statement = conn.createStatement();
        ResultSet resultSet = statement.executeQuery("Select * From users");
        // 4
        List<User> userList = new ArrayList<>();
        while (resultSet.next()) {
            User user = new User();
            user.setId(resultSet.getLong("id"));
            user.setName(resultSet.getString("name"));
            user.setName(resultSet.getString("password"));
            user.setEmail(resultSet.getString("email"));
            user.setBirthday(resultSet.getDate("birthday"));
            userList.add(user);
            System.out.println(user);
        }
        // 5
        conn.close();
    }
}
