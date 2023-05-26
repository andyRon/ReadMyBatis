package com.andyron.ch05;

import com.alibaba.fastjson.JSON;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.Configuration;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;

/**
 * @author andyron
 **/
public class TestConfiguration {
    @Test
    public void test() throws IOException {
        Reader reader = Resources.getResourceAsReader("mybatis-config.xml");
        XMLConfigBuilder builder = new XMLConfigBuilder(reader);
        Configuration config = builder.parse();
    }
}
