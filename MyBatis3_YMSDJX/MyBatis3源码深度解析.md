MyBatis3源码深度解析
-----

https://book.douban.com/subject/34836563/

优化了SQL配置方式，引用OGNL表达式来支持动态SQL配置

引入了SQL Mapper的概念

将XML文件中的SQL配置与一个Java接口进行绑定，SQL配置的命名空间对应Java接口的完全限定名，而具体的每个SQL语句的配置对应Java接口中的一个方法，建立绑定后，可以通过调用Java接口中定义的方法来执行XML文件中配置的SQL语句。



https://github.com/rongbo-j/mybatis-book



## 1 搭建MyBatis源码环境

https://github.com/mybatis/mybatis-3

https://github.com/mybatis/spring

https://github.com/mybatis/parent



### HSQLDB数据库简介

[HSQLDB官方文档](http://hsqldb.org/doc/2.0/guide/index.html)

[SQL-92规范文档](http://www.contrib.andrew.cmu.edu/~shadow/sql/sql1992.txt)

HSQLDB是纯Java语言编写的关系型数据库管理系统，支持大部分 **SQL-92、SQL:2008、SQL:2011规范**。它提供了一个小型的同时支持内存和磁盘存储表结构的数据库引擎，支持**Server模式**和**内存模式**两种运行模式。

Server模式是把HSQLDB作为一个单独的数据库服务运行，类似于我们常用的关系型数据库，例如Oracle、MySQL等。而内存模式则是把HSQLDB嵌入应用进程中，这种模式只能存储应用内部数据。由于HSQLDB能够很好地支持JDBC规范，因此我们可以使用它作为Java语言与关系型数据库交互的测试工具。



MyBatis源码中提供了大量的单元测试用例，都使用了HSQLDB的内存模式，我们不需要额外安装其他数据库就可以运行MyBatis源码中的测试用例。



MyBatis提供的两个工具类`ScriptRunner`和`SqlRunner`，分别用于批量执行数据库脚本和对数据库进行增删改查操作



## 2 JDBC规范详解

MyBatis框架对JDBC做了轻量级的封装

[JDBC4.2规范文档](https://download.oracle.com/otndocs/jcp/jdbc-4_2-mrel2-spec/index.html)

### 2.1 JDBC API简介

**==JDBC（Java Database Connectivity）==**是Java语言中提供的**==访问关系型数据的接口==**🔖非关系。在Java编写的应用中，使用JDBC API可以==执行SQL语句、检索SQL执行结果以及将数据更改写回到底层数据源==。JDBC API也可以用于==分布式、异构的环境中与多个数据源交互==。

当然，使用JDBC访问其他数据源（例如文件系统或者面向对象系统等）也是有可能的，只要该数据源提供JDBC规范驱动程序即可。

使用JDBC操作数据源大致步骤：

1. 与数据源建立连接。
2. 执行SQL语句。
3. 检索SQL执行结果。
4. 关闭连接。

#### 建立数据源连接

`Connection`接口用来表示与底层数据源的连接，是JDBC对数据源连接的抽象。

两种获取`Connection`对象的方式：

1. `DriverManager`：类，JDBC 1.0。当应用程序第一次尝试通过URL连接数据源时，DriverManager会自动加载CLASSPATH下所有的JDBC驱动。DriverManager类提供了一系列重载的getConnection()方法，用来获取Connection对象，例如：

   ```java
   DriverManager.getConnection("jdbc:hsqldb:mem:mybatis", "sa", "");
   ```

2. `DataSource1：接口，JDBC 2.0。 

比DriverManager更受欢迎，因为它提供了更多底层数据源相关的细节，而且对应用来说，不需要关注JDBC驱动的实现。一个DataSource对象的属性被设置后，它就代表一个特定的数据源。🔖

JDBC API中只提供了DataSource接口，没有提供DataSource的具体实现，DataSource具体的实现由JDBC驱动程序提供。

MyBatis框架中提供了DataSource接口的实现：

```java
DataSource dataSource = new UnpooledDataSource("org.hsqldb.jdbcDriver", "jdbc:hsqldb:mem:mybatis", "sa", "");
            Connection conn = dataSource.getConnection();
```

另外，MyBatis框架还提供了DataSource的工厂，即`DataSourceFactory`：

```java
UnpooledDataSourceFactory dsf = new UnpooledDataSourceFactory();
Properties properties = new Properties();
InputStream configStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("database.properties");
properties.load(configStream);
dsf.setProperties(properties);

DataSource dataSource = dsf.getDataSource();
Connection conn = dataSource.getConnection();
```

#### 执行SQL语句

JDBC API提供了访问SQL:2003规范中常用的实现特性，因为不同的JDBC厂商对这些特性的支持程度各不相同，所以JDBC API中提供了一个`DatabaseMetadata`接口，应用程序可以使用DatabaseMetadata的实例来确定目前使用的数据源是否支持某一特性。

```java
conn = dataSource.getConnection();
DatabaseMetaData dbmd = conn.getMetaData();
```

Connection接口中提供的方法创建`Statement`、`PreparedStatement`或者`CallableStatement`对象。

Statement接口可以理解SQL语句的**执行器**，其中`executeQuery()`方法执行**查询操作**，调用`executeUpdate()`方法执行**更新操作**，另外调用`executeBatch()`方法执行**批量处理**。当不知道SQL语句的类型时，可以调用`execute()`方法进行统一的操作，返回值可判断SQL语句类型：`true if the first result is a ResultSet object; false if it is an update count or there are no results`。最后可以通过`getResultSet()`方法来获取查询结果集，或者通过`getUpdateCount()`方法来获取更新操作影响的行数。

#### 处理SQL执行结果

`ResultSet`接口的实现类封装SQL查询的结果，可以对ResultSet对象进行遍历，然后通过ResultSet提供的一系列`getXXX()`方法（例如getString）获取查询结果集。`getMetaData()`方法获取结果集元数据信息（`ResultSetMetaData`）：字段名称、字段数量、字段数据类型等。



#### 使用JDBC操作数据库



### 2.2 JDBC API中的类与接口

JDBC API由java.sql和javax.sql两个包构成。

#### java.sql包详解

java.sql包中的所有接口、枚举和类：

```java
# 数据类型
java.sql.Array 
java.sql.Blob 
java.sql.Clob 
java.sgl.Date 
java.sql.NClob 
java.sql.Struct 
java.sql.Time

java.sql.Timestamp 
java.sql.SQLXML 
java.sql.Ref 
java.sql.RowId 
java.sql.SQLOutput 
java.sql.SQLData 
java.sql.SQLInput


# 枚举
java.sql.SQLType 
java.sql.JDBCType 
java.sql.Types 
java.sql.RowIdLifeTime 
java.sql.PseudoColumnUsage 
java.sql.ClientinfoStatus
# 驱动相关
java.sgl.Driver
java.sql.DriverAction 
java.sql.DriverManager 
java.sql.DriverPropertyInfo 
java.sql.SQLPermission 
java.sql.Savepoint
# 异常
java.sql.BatchUpdateException 
java.sql.DataTruncation 
java.sql.SQLClientInfoException 
java.sql.SQLDataException 
java.sql.SQLException 
java.sql.SQLFeatureNotSupportedException
Lava.sql.SQLIntegrityConstraintViolationException java.sql.SQLInvalidAuthorizationSpecException java.sql.SQLNonTransientConnectionException 
java.sql.SQLNonTransientException 
java.sql.SQLSyntaxErrorException 
java.sql.SQLTimeoutException 
java.sql.SOLTransactionRollbackException java.sql.SQLTransientConnectionException 
java.sql.SQLTransientException 
java.sql.SOLWarning

# API 相关
java.sql.Wrapper
java.sql.Connection
java.sql.Statement 
java.sql.CallableStatement
java.sql.PreparedStatement 
java.sql.DatabaseMetaData 
java.sql.ParameterMetaData 
java.sql.ResultSet
java.sal.ResultSetMetaData
```

大致可以分为数据类型接口、枚举类、驱动相关类和接口、异常类、API相关。

其中API相关对于开发人员最重要，这些接口都继承了`java.sql.Wrapper`接口。

```java
public interface Wrapper {
    <T> T unwrap(java.lang.Class<T> iface) throws java.sql.SQLException;
    boolean isWrapperFor(java.lang.Class<?> iface) throws java.sql.SQLException;
}

```



![](images/image-20230525120451147.png)



#### javax.sql包详解

```java
# 数据源
javax.sql.DataSource 
javax.sql.CommonDataSource
# 连接池相关
javax.sql.ConnectionPoolDataSource 
javax.sql.PooledConnection 
javax.sql.ConnectionEvent 
javax.sql.ConnectionEventListener 
javax.sql.StatementEvent 
javax.sql.StatementEventListener
# ResultSet 扩展
javax.sql.RowSet 
javax.sql.RowSetEvent 
javax.sql.RowSetInternal 
javax.sql.RowSetListener 
javax.sql.RowSetMetaData 
javax.sql.RowSetReader 
javax.sql.RowSetWriter
＃分布式扩展
javax.sql.XAconnection
javax.sql.XADatasource
```





![](images/image-20230525121057539.png)





![](images/image-20230525121138502.png)



![](images/image-20230525121239798.png)





![](images/image-20230525121339868.png)



### 2.3 Connection详解



#### JDBC驱动类型



#### java.sql.Driver接口





#### Java SPI机制简介



SPI（Service Provider Interface）是JDK内置的一种服务提供发现机制。SPI是一种动态替换发现的机制。

![](images/image-20230525121535405.png)



#### java.sql.DriverAction接口



#### java.sql.DriverManager类





#### javax.sql.DataSource接口



![](images/image-20230525121705665.png)



#### 使用JNDI API增强应用的可移植性

==JNDI（Java Naming and Directory Interface，Java命名和目录接口）==为应用程序提供了一种通过网络访问远程服务的方式。



#### 关闭Connection对象



### 2.4 Statement详解



#### java.sql.Statement接口





#### java.sql.PreparedStatement接口

PreparedStatement接口继承自Statement接口，在Statement接口的基础上增加了==参数占位符==功能。





![](images/image-20230525122127384.png)



#### java.sql.CallableStatement接口

CallableStatement接口继承自PreparedStatement接口，在PreparedStatement的基础上增加了==调用存储过程并检索调用结果==的功能。



#### 获取自增长的键值



### 2.5 ResultSet详解

#### ResultSet类型



#### ResultSet并行性



#### ResultSet可保持性



#### ResultSet属性设置



#### 修改ResultSet对象



#### 关闭ResultSet对象



### 2.6 DatabaseMetaData详解



### 2.7 JDBC事务



## 3 MyBatis常用工具类

### 3.1 使用SQL类生成语句



![SQL工具类的方法及作用](images/SQL工具类的方法及作用.jpg)



### 3.2 使用ScriptRunner执行脚本



### 3.3 使用SqlRunner操作数据库



### 3.4 MetaObject详解



### 3.5 MetaClass详解

MetaObject用于获取和设置对象的属性值，而MetaClass则用于获取类相关的信息。



### 3.6 ObjectFactory详解



### 3.7 ProxyFactory详解

ProxyFactory主要用于实现MyBatis的懒加载功能。



## 4 MyBatis核心组件介绍

### 4.1 使用MyBatis操作数据库



### 4.2 MyBatis核心组件



### 4.3 Configuration详解

MyBatis框架的配置信息有两种，一种是配置MyBatis框架属性的主配置文件；另一种是配置执行SQL语句的Mapper配置文件。



### 4.4 Executor详解



### 4.5 MappedStatement详解



### 4.6 StatementHandler详解



### 4.7 TypeHandler详解



### 4.8 ParameterHandler详解



### 4.9 ResultSetHandler详解
