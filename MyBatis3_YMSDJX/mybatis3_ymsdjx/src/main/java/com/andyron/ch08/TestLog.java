package com.andyron.ch08;

import org.apache.ibatis.logging.Log;
import org.apache.ibatis.logging.LogFactory;
import org.junit.Test;

/**
 * @author andyron
 **/
public class TestLog {

    @Test
    public void test() {
        // 指定使用某种日志框架（前提是有其依赖）
        LogFactory.useCommonsLogging();
        //
        Log log = LogFactory.getLog(TestLog.class);
        //
        log.error("测试日志框架选用");
    }
}
