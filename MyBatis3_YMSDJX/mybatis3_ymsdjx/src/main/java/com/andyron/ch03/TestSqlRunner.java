package com.andyron.ch03;

import com.alibaba.fastjson.JSON;
import com.andyron.common.IOUtils;
import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.SQL;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.jdbc.SqlRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Properties;

/**
 * @author andyron
 **/
public class TestSqlRunner {
    public Connection conn = null;

    @Before
    public void before() throws Exception {
        DataSourceFactory dsf = new UnpooledDataSourceFactory();
        Properties properties = new Properties();
        InputStream configStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("database.properties");
        properties.load(configStream);
        dsf.setProperties(properties);
        DataSource dataSource = dsf.getDataSource();
        conn = dataSource.getConnection();

        // 使用Mybatis的ScriptRunner工具类执行数据库脚本
        ScriptRunner scriptRunner = new ScriptRunner(conn);
        // 不输出sql日志
        scriptRunner.setLogWriter(null);
        scriptRunner.runScript(Resources.getResourceAsReader("create-table.sql"));
        scriptRunner.runScript(Resources.getResourceAsReader("init-data.sql"));
    }

    @Test
    public void testSelectOne() throws SQLException {
        SqlRunner sqlRunner = new SqlRunner(conn);
        String qryUserSql = new SQL() {{
            SELECT("*");
            FROM("user");
            WHERE("id = ?");
        }}.toString();
        Map<String, Object> resultMap = sqlRunner.selectOne(qryUserSql, Integer.valueOf(1));
        System.out.println(resultMap);
        System.out.println(JSON.toJSONString(resultMap));
    }

    @Test
    public void testDelete() throws SQLException {
        SqlRunner sqlRunner = new SqlRunner(conn);
        String deleteUserSql = new SQL(){{
            DELETE_FROM("user");
            WHERE("id = ?");
        }}.toString();
        sqlRunner.delete(deleteUserSql, Integer.valueOf(1));

        System.out.println(JSON.toJSONString(sqlRunner.selectAll("select * from user")));
    }

    @Test
    public void testUpdate() throws SQLException {
        SqlRunner sqlRunner = new SqlRunner(conn);
        String updateUserSql = new SQL(){{
            UPDATE("user");
            SET("nick_name = ?");
            WHERE("id = ?");
        }}.toString();
        sqlRunner.update(updateUserSql, "Jane", Integer.valueOf(1));

        System.out.println(JSON.toJSONString(sqlRunner.selectAll("select * from user")));
    }

    @Test
    public void testInsert() throws SQLException {
        SqlRunner sqlRunner = new SqlRunner(conn);
        String insertUserSql = new SQL(){{
            INSERT_INTO("user");
            INTO_COLUMNS("create_time,name,password,phone,nick_name");
            INTO_VALUES("?,?,?,?,?");
        }}.toString();
        String createTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        sqlRunner.insert(insertUserSql,createTime,"Jane","test","18700000000","J");

        System.out.println(JSON.toJSONString(sqlRunner.selectAll("select * from user")));
    }

    @After
    public void closeConnection() {
        IOUtils.closeQuietly(conn);
    }
}
