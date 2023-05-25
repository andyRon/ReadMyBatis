package com.andyron.ch03;

import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author andyron
 **/
public class TestObjectFactory {

    @Test
    public void testObjectFactory() {
        ObjectFactory objectFactory = new DefaultObjectFactory();
        List<Integer> list = objectFactory.create(List.class);
        Map<String, String> map = objectFactory.create(Map.class);
        list.addAll(Arrays.asList(1, 2,3));
        map.put("code", "代码大全");
        System.out.println(list);
        System.out.println(map);
    }
}
