package com.andyron.ch09.mapper;

import com.andyron.ch09.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author andyron
 **/
public interface UserMapper {
    List<User> getUserByEntity(User user);

    User getUserInfo(User user);

}
