package com.andyron.ch04;

import com.alibaba.fastjson.JSON;
import com.andyron.ch04.entity.User;
import com.andyron.ch04.mapper.UserMapper;
import com.andyron.common.DbUtils;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.*;
import org.apache.ibatis.transaction.jdbc.JdbcTransaction;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.sql.SQLException;
import java.util.List;

/**
 * @author andyron
 **/
public class TestUser {
    @Before
    public void before() {
        DbUtils.initData();
    }

    @Test
    public void testMyabtis() throws IOException {
        InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession sqlSession = sqlSessionFactory.openSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

        List<User> users = userMapper.listAllUser();
        System.out.println(JSON.toJSONString(users));


    }

    @Test
    public void testSqlSessionManager() throws IOException {
        Reader mybatisConfig = Resources.getResourceAsReader("mybatis-config.xml");
        SqlSessionManager sqlSessionManager = SqlSessionManager.newInstance(mybatisConfig);
        sqlSessionManager.startManagedSession();
        UserMapper mapper = sqlSessionManager.getMapper(UserMapper.class);
        System.out.println(JSON.toJSONString(mapper.listAllUser()));
    }

    @Test
    public void testExecutor() throws IOException, SQLException {
        InputStream inputStream = Resources.getResourceAsStream("mybatis-config.xml");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession sqlSession = sqlSessionFactory.openSession();

        Configuration configuration = sqlSession.getConfiguration();
        // 从Configuration对象中获取描述SQL配置的MappedStatement对象
        MappedStatement listAllUserStmt = configuration.getMappedStatement("com.andyron.ch04.mapper.UserMapper.listAllUser");
        // 创建ReuseExecutor实例
        Executor reuseExecutor = configuration.newExecutor(new JdbcTransaction(sqlSession.getConnection()), ExecutorType.REUSE);
        // 调用query()方法执行查询操作
        List<User> userList = reuseExecutor.query(listAllUserStmt, null, RowBounds.DEFAULT, Executor.NO_RESULT_HANDLER);

        System.out.println(JSON.toJSONString(userList));
    }

}
