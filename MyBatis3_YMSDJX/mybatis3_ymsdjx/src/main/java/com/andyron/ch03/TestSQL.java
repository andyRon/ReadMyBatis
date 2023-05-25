package com.andyron.ch03;

import org.apache.ibatis.jdbc.SQL;
import org.junit.Test;

/**
 * @author andyron
 **/
public class TestSQL {
    @Test
    public void test() {
        SQL sql = new SQL();
        sql.SELECT("*");
        sql.FROM("des");
        sql.JOIN("user", "orders");
        System.out.println(sql.toString());
    }

    @Test
    public void testSql() {
        String sql = new SQL().INSERT_INTO("person")
                .VALUES("id, first_name", "#{id}, #{firstName}")
                .VALUES("last_name", "#{lastName}").toString();
        System.out.println(sql);

        sql = new SQL().DELETE_FROM("person")
                .WHERE("id = #{id}").toString();
        System.out.println(sql);

        sql = new SQL().UPDATE("person")
                .SET("first_name = #{firstName}")
                .WHERE("id = #{id}").toString();
        System.out.println(sql);

    }
}
