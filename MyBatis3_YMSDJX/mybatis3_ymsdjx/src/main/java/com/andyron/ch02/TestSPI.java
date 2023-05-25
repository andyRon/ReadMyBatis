package com.andyron.ch02;

import org.junit.Test;

import java.sql.Driver;
import java.util.ServiceLoader;

/**
 * @author andyron
 **/
public class TestSPI {
    @Test
    public void testSPI() {
        // 各种驱动包中META-INF/services中都有java.sql.Driver做配置文件，这个配置文件中有接口的具体实现类名
        ServiceLoader<Driver> drivers = ServiceLoader.load(java.sql.Driver.class);
        for (Driver driver : drivers) {
            System.out.println(driver.getClass().getName());
        }
    }
}
