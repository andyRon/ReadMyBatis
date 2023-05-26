package com.andyron.ch06;

import com.alibaba.fastjson.JSON;
import com.andyron.ch04.entity.User;
import com.andyron.ch04.mapper.UserMapper;
import com.andyron.common.DbUtils;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author andyron
 **/
public class TestSqlSession {
    @Before
    public void before() {
        DbUtils.initData();
    }
    @Test
    public void testSqlSession() throws IOException {
        InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        List<User> users = sqlSession.selectList("com.andyron.ch04.mapper.UserMapper.listAllUser");

        System.out.println(JSON.toJSONString(users));
    }
}
