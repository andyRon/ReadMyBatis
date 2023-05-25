package com.andyron.ch01;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.jdbc.SqlRunner;
import org.junit.Before;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @author andyron
 **/
public class HSQLDBTest {
    private Connection conn = null;

    @Before
    public void initData() {
        try {
            Class.forName("org.hsqldb.jdbcDriver");
            conn = DriverManager.getConnection("jdbc:hsqldb:mem:mybatis", "sa", "");
            // 使用Mybatis的ScriptRunner工具类执行数据库脚本
            ScriptRunner scriptRunner = new ScriptRunner(conn);
            scriptRunner.setLogWriter(null);
            scriptRunner.runScript(Resources.getResourceAsReader("create-table.sql"));
            scriptRunner.runScript(Resources.getResourceAsReader("init-data.sql"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testHsqldbQuery() {
        SqlRunner sqlRunner = new SqlRunner(conn);
        try {
            List<Map<String, Object>> results = sqlRunner.selectAll("select * from user");
            results.forEach(System.out::println);
            sqlRunner.closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



}
