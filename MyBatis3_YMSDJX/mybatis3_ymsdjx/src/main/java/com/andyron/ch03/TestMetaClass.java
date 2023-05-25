package com.andyron.ch03;

import com.alibaba.fastjson.JSON;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

/**
 * @author andyron
 **/
public class TestMetaClass {
    @Data
    @AllArgsConstructor
    private static class Order {
        String orderNo;
        String goodsName;

        Integer number;
    }

    @Test
    public void test() {
        MetaClass metaClass = MetaClass.forClass(Order.class, new DefaultReflectorFactory());
        // 获取所有有Getter方法的属性名
        System.out.println(JSON.toJSONString(metaClass.getGetterNames()));
        // 是否有默认构造方法
        System.out.println(metaClass.hasDefaultConstructor());
        // 某属性是否有对应的Getter/Setter方法
        System.out.println(metaClass.hasGetter("orderNo"));
        // 属性类型
        System.out.println(metaClass.getGetterType("number"));
        // 获取属性getter方法
        Invoker invoker = metaClass.getGetInvoker("orderNo");
        try {
            // 通过Invoker对象调用Getter方法获取属性值
            Object orderNo = invoker.invoke(new Order("order1234", "《代码大全》", 12), null);
            System.out.println(orderNo);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
