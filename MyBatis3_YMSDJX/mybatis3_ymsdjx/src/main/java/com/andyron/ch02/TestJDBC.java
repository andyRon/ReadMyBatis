package com.andyron.ch02;

import com.andyron.common.DbUtils;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

/**
 * @author andyron
 **/
public class TestJDBC {
    @Test
    public void test1() throws SQLException {
        System.out.println(DbUtils.initData());
    }
    @Test
    public void testDriverManager() {
        try {
            Connection conn = DbUtils.initData();
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from user");
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String columnVal = resultSet.getString(columnName);
                    System.out.println(columnName + ":" + columnVal);
                }
                System.out.println("-------------");
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMyBatisDataSource() {
        DbUtils.initData();
        try {
            DataSource dataSource = new UnpooledDataSource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:mybatis", "sa", "");
            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from user");
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String columnVal = resultSet.getString(columnName);
                    System.out.println(columnName + ":" + columnVal);
                }
                System.out.println("============");
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMybatisDataSourceFactory() {
        DbUtils.initData();
        try {
            UnpooledDataSourceFactory dsf = new UnpooledDataSourceFactory();
            Properties properties = new Properties();
            InputStream configStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("database.properties");
            properties.load(configStream);
            dsf.setProperties(properties);

            DataSource dataSource = dsf.getDataSource();
            Connection conn = dataSource.getConnection();
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery("select * from user");
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            while (resultSet.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String columnVal = resultSet.getString(columnName);
                    System.out.println(columnName + ":" + columnVal);
                }
                System.out.println("~~~~~~~~~~~~~~~~~~~~~");
            }
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDatabaseMetadata() {
        DataSource dataSource = new UnpooledDataSource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:mybatis", "sa", "");
        Connection conn = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            DatabaseMetaData dbmd = conn.getMetaData();
            System.out.println("数据库已知的用户: " + dbmd.getUserName());
            System.out.println("数据库的系统函数的逗号分隔列表: " + dbmd.getSystemFunctions());
            System.out.println("数据库的时间和日期函数的逗号分隔列表: " + dbmd.getTimeDateFunctions());
            System.out.println("数据库的字符串函数的逗号分隔列表: " + dbmd.getStringFunctions());
            System.out.println("数据库供应商用于 'schema' 的首选术语: " + dbmd.getSchemaTerm());
            System.out.println("数据库URL: " + dbmd.getURL());
            System.out.println("是否允许只读:" + dbmd.isReadOnly());
            System.out.println("数据库的产品名称:" + dbmd.getDatabaseProductName());
            System.out.println("数据库的版本:" + dbmd.getDatabaseProductVersion());
            System.out.println("驱动程序的名称:" + dbmd.getDriverName());
            System.out.println("驱动程序的版本:" + dbmd.getDriverVersion());
            System.out.println("数据库中使用的表类型");
            rs = dbmd.getTableTypes();
            while (rs.next()) {
                System.out.println(rs.getString("TABLE_TYPE"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
                rs.close();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

    }


}
