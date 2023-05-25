package com.andyron.ch02;

import com.andyron.common.DbUtils;
import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * @author andyron
 **/
public class TestStatement {
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
    public void testStatement() throws Exception {
//        DataSource dataSource = new UnpooledDataSource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:mybatis", "sa", "");
//        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,  // 创建的ResultSet对象是可滚动的
//                ResultSet.CONCUR_UPDATABLE,  // 可以修改的
//                ResultSet.HOLD_CURSORS_OVER_COMMIT); // 当修改提交时ResultSet不会被关闭
        Statement statement = conn.createStatement();
        String sql = "insert into user (create_time, name, password, phone, nick_name) values('2010-10-26 10:20:30', 'User14', 'test', '18700001111', 'User4')";
        String sql2 = "insert into user (create_time, name, password, phone, nick_name) values('2010-10-16 10:20:30', 'User13', 'test3', '18220001111', 'User13')";
        statement.addBatch(sql);
        statement.addBatch(sql2);
        statement.executeBatch();
        statement.execute("select * from user");

        ResultSet resultSet = statement.getResultSet();
        DbUtils.dumpRS(resultSet);

        conn.close();

    }

    @Test
    public void testPreparedStatement() throws Exception {
        String sql = "insert into user (create_time, name, password, phone, nick_name) values(?,?,?,?,?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, "2022-10-26 10:20:30");
        ps.setString(2, "andy");
        ps.setString(3, "123456");
        ps.setString(4, "1395226445");
        ps.setString(5, "big day");
        ps.execute();

        Statement st = conn.createStatement();
        ResultSet resultSet = st.executeQuery("select * from user");
        DbUtils.dumpRS(resultSet);

        conn.close();
    }

    @Test
    public void testParameterMetaData() throws Exception {
        String sql = "insert into user (create_time, name, password, phone, nick_name) values(?,?,?,?,?)";
        PreparedStatement ps = conn.prepareStatement(sql);
//        ps.setString(1, "2022-10-26 10:20:30");
//        ps.setString(2, "andy");
//        ps.setString(3, "123456");
//        ps.setString(4, "1395226445");
//        ps.setString(5, "big day");

        ParameterMetaData pmd = ps.getParameterMetaData();
        for (int i = 1; i <= pmd.getParameterCount(); i++) {
            String typeName = pmd.getParameterTypeName(i);
            String className = pmd.getParameterClassName(i);
            System.out.println("第" + i + "个参数，" + "typeName:" + typeName + ",className:" + className);

        }
    }

    @Test
    public void testGeneratedKeys() throws Exception {
        String sql = "insert into user(create_time, name, password, phone, nick_name) " +
                "values('2022-10-24 10:20:30','User13','test','18700001111','User13');";
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        ResultSet generatedKeys = stmt.getGeneratedKeys();
        if (generatedKeys.next()) {
            System.out.println(generatedKeys.getInt(1));
        }
    }
}
