package com.andyron.ch04.mapper;

import com.andyron.ch04.entity.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author andyron
 **/
public interface UserMapper {
    List<User> listAllUser();

    @Select("select * from user where id=#{userId,jdbcType=INTEGER}")
    User getUserById(@Param("userId") String userId);

    List<User> getUserByEntity(User user);

    User getUserByPhone(@Param("phone") String phone);
}
