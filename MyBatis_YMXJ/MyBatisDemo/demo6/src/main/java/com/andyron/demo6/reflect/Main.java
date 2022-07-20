package com.andyron.demo6.reflect;

import org.apache.ibatis.reflection.property.PropertyCopier;
import org.apache.ibatis.reflection.property.PropertyNamer;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
        User user1 = new User(1, "tom", 18);
        User user2 = new User(2, "jack", 19);
        Map<String, String> diff = diffObj(user1, user2);
        for (Map.Entry<String, String> entry : diff.entrySet()) {
            System.out.println("属性："+entry.getKey() + "，变化：" + entry.getValue());
        }
        System.out.println();

        Book book1 = new Book("语文", 300);
        Book book2 = new Book("数学", 350);
        diff = diffObj(book1, book2);
        for (Map.Entry<String, String> entry : diff.entrySet()) {
            System.out.println("属性："+entry.getKey() + "，变化：" + entry.getValue());
        }

        System.out.println("property子包使用：");
        // 将user1的属性全部复制给user2
        PropertyCopier.copyBeanProperties(User.class, user1, user2);
        System.out.println(user2);

        System.out.println(PropertyNamer.methodToProperty("setName"));
        System.out.println(PropertyNamer.isProperty("getName"));
        System.out.println(PropertyNamer.isGetter("setAge"));

        String tokenString = "company.departs[0].departName";
//        tokenString = "student[sId].name";
        PropertyTokenizer tokenizer = new PropertyTokenizer(tokenString);
        System.out.println(tokenizer);
        System.out.println(tokenizer.getName());
        System.out.println(tokenizer.getIndex());
        System.out.println(tokenizer.getChildren());
        System.out.println(tokenizer.getIndexedName());



    }

    public static Map<String, String> diffObj(Object oldObj, Object newObj) {
        Map<String, String> difMap = new HashMap<>();
        try {
            Class<?> oldObjClass = oldObj.getClass();
            Class<?> newObjClass = newObj.getClass();
            // 判断对象是否属于同一个类
            if (oldObjClass.equals(newObjClass)) {
                // 获取对象的所有属性
                Field[] fields = oldObjClass.getDeclaredFields();
                for (Field field : fields) {
                    field.setAccessible(true);
                    Object oldValue = field.get(oldObj);
                    Object newValue = field.get(newObj);
                    if ((oldValue == null && newValue != null) || (oldObj != null && !oldObj.equals(newValue))) {
                        difMap.put(field.getName(), "from " + oldValue + " to " + newValue);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return difMap;
    }
}


class User {
    private int id;
    private String name;
    private int age;

    public User(int id, String name, int age) {
        this.id = id;
        this.name = name;
        this.age = age;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}

class Book {
    private String name;
    private int pages;

    public Book(String name, int pages) {
        this.name = name;
        this.pages = pages;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}