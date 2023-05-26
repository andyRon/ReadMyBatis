package com.andyron.ch05;

import com.alibaba.fastjson.JSON;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.converters.DateConverter;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.InputStream;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author andyron
 **/
public class TestXPath {

    // 通过JDK，将XML内容转换为User实体对象
    @Test
    public void testXPathParser() {
        try {
            // 创建DocumentBuilderFactory实例
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // 创建DocumentBuilder实例
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream inputStream = Resources.getResourceAsStream("com/andyron/ch05/users.xml");
            Document doc = builder.parse(inputStream);
            // 获取XPath实例
            XPath xPath = XPathFactory.newInstance().newXPath();
            // 执行XPath表达式，获取节点信息
            NodeList nodeList = (NodeList) xPath.evaluate("/users/*", doc, XPathConstants.NODESET);
            List<User> userList = new ArrayList<>();
            for (int i = 1; i < nodeList.getLength() + 1; i++) {
                String path = "/users/user[" + i + "]";
                String id = (String) xPath.evaluate(path + "/@id", doc, XPathConstants.STRING);
                String name = (String) xPath.evaluate(path + "/name", doc, XPathConstants.STRING);
                String createTime = (String) xPath.evaluate(path + "/createTime", doc, XPathConstants.STRING);
                String passward = (String) xPath.evaluate(path + "/passward", doc, XPathConstants.STRING);
                String phone = (String) xPath.evaluate(path + "/phone", doc, XPathConstants.STRING);
                String nickName = (String) xPath.evaluate(path + "/nickName", doc, XPathConstants.STRING);
                // 构建User
                User user = buildUser(id, name, createTime, passward, phone, nickName);
                userList.add(user);
            }
            System.out.println(JSON.toJSONString(userList));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private User buildUser(String id, String name, String createTime, String password, String phone, String nickName) throws InvocationTargetException, IllegalAccessException {
        User user = new User();
        DateConverter dateConverter = new DateConverter(null);
        dateConverter.setPattern("yyyy-MM-dd HH:mm:ss");
        ConvertUtils.register(dateConverter, Date.class);
        BeanUtils.setProperty(user, "id", id);
        BeanUtils.setProperty(user, "name", name);
        BeanUtils.setProperty(user, "createTime", createTime);
        BeanUtils.setProperty(user, "password", password);
        BeanUtils.setProperty(user, "phone", phone);
        BeanUtils.setProperty(user, "nickName", nickName);
        return user;
    }

    @Test
    public void testXPathParserByMybatis() throws Exception {
        Reader resource = Resources.getResourceAsReader("com/andyron/ch05/users.xml");
        XPathParser parser = new XPathParser(resource);
        // 注册日期转换器  // TODO
        DateConverter dateConverter = new DateConverter(null);
        dateConverter.setPattern("yyyy-MM-dd HH:mm:ss");
        ConvertUtils.register(dateConverter, Date.class);

        List<User> userList = new ArrayList<>();
        //
        List<XNode> nodes = parser.evalNodes("/users/*");
        //
        for (XNode node : nodes) {
            User user = new User();
            Long id = node.getLongAttribute("id");
            BeanUtils.setProperty(user, "id", id);
            List<XNode> childNodes = node.getChildren();
            for (XNode childNode : childNodes) {
                BeanUtils.setProperty(user, childNode.getName(), childNode.getStringBody());
            }
            userList.add(user);
        }
        System.out.println(JSON.toJSONString(userList));
    }
}
