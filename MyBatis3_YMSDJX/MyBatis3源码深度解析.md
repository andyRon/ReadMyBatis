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

`unwrap()`方法用于返回未经过包装的JDBC驱动原始类型实例，可以通过该实例调用JDBC驱动中提供的非标准的方法。

`isWrapperFor()`方法用于判断当前实例是否是JDBC驱动中某一类型的包装类型。

```java
Statement stmt = conn.createStatement();
Class clzz = Class.forName("oracle.jdbc.OracleStatement");
if (stmt.isWrapperFor(clzz)) {
  OracleStatement os = (OracleStatement) stmt.unwrap(clzz);
  os.defineColumnType(1, Types.NUMBER);
}
```

👆🏻，Oracle数据库驱动中提供了一些非JDBC标准的方法，如果需要使用这些非标准的方法，则可以调用Wrapper接口提供的unwrap()方法获取Oracle驱动的原始类型，然后调用原始类型提供的非标准方法就可以访问Oracle数据库特有的一些特性了。



JDBC API中的`Connection`、`Statement`、`ResultSet`等接口都继承自Wrapper接口，这些接口都提供了对JDBC驱动原始类型的访问能力。

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

相对于`DriverManager`，JDBC 2.0提供的`DataSource`接口是一个更好的连接数据源的方式。两个优势：

- 首先，应用程序不需要像使用DriverManager一样对加载的数据库驱动程序信息进行**硬编码**。开发人员可以选择通过**JNDI**注册这个数据源对象，然后在程序中使用一个逻辑名称来引用它，JNDI会自动根据我们给出的名称找到与这个名称绑定的DataSource对象。然后我们就可以使用这个DataSource对象来建立和具体数据库的连接了。🔖
- 其次，体现在**连接池和分布式事务**上。连接池通过对==连接的复用==，而不是每次需要操作数据源时都新建一个物理连接来显著地提高程序的效率，适用于任务繁忙、负担繁重的企业级应用。



![](images/image-20230525121057539.png)



`javax.sql.PooledConnection`和`Connection`的不同之处在于，它提供了==连接池管理的句柄==。一个PooledConnection表示与数据源建立的物理连接，该连接在应用程序使用完后可以回收而不用关闭它，从而减少了与数据源建立连接的次数。

🔖

![](images/image-20230525121138502.png)



另外，javax.sql包中还包含`XADataSource`、`XAResource`和`XAConnection`接口，这些接口提供了分布式事务的支持，具体由JDBC驱动来实现。更多分布式事务相关细节可参考[**JTA（Java Transaction API）**规范文档](https://download.oracle.com/otndocs/jcp/jta-1.1-spec-oth-JSpec/?submit=Download)。

![](images/image-20230525121239798.png)



`RowSet`用于为数据源和应用程序在内容中建立一个映射。

相较于`java.sql.ResultSet`而言，RowSet的离线操作能够有效地利用计算机越来越充足的内存减轻数据库服务器的负担。【数据操作都是在内存中进行的，然后批量提交到数据源】

RowSet默认是一个==可滚动、可更新、可序列化==的结果集，而且它作为一个JavaBean组件，可以方便地在网络间传输，用于两端的数据同步。通俗来讲，RowSet就相当于数据库表==数据在应用程序内存中的映射==。

![](images/image-20230525121339868.png)



### 2.3 Connection详解

一个Connection对象表示通过JDBC驱动与数据源建立的连接，这里的数据源可以是关系型数据库管理系统（DBMS）、文件系统或者其他通过JDBC驱动访问的数据。

使用JDBC API的应用程序可能需要维护多个Connection对象，一个Connection对象可能访问多个数据源，也可能访问单个数据源。

从JDBC驱动的角度来看，Connection对象表示**客户端会话**，因此它需要一些相关的**状态信息**。

使用DataSource的具体实现获取Connection对象是比较推荐的一种方式，因为它增强了应用程序的可移植性，使代码维护更加容易，并且使应用程序能够透明地使用连接池和处理分布式事务。

#### JDBC驱动类型

##### 1.JDBC-ODBC Bridge Driver

![](images/image-20230525153337144.png)

##### 2.Native API Driver

这类驱动程序会直接调用数据库提供的**原生链接库或客户端**，因为没有中间过程，访问速度通常表现良好。但是**驱动程序与数据库和平台绑定无法达到JDBC跨平台的基本目的**。

![](images/image-20230525153348977.png)

##### 3.JDB-Net Driver

这类驱动程序会**将JDBC调用转换为独立于数据库的协议**，然后通过特定的**中间组件或服务器**转换为数据库通信协议，主要目的是获得更好的架构灵活性。

![](images/image-20230525153432897.png)

##### 4.Native Protocol Driver

这是最常见的驱动程序类型，开发中使用的驱动包基本都属于此类，通常由数据库厂商直接提供，例如mysql-connector-java，驱动程序把JDBC调用转换为数据库特定的网络通信协议。使用网络通信，驱动程序可以纯Java实现，支持跨平台部署，性能也较好。

![](images/image-20230525153626537.png)

#### java.sql.Driver接口

**==所有的JDBC驱动都必须实现Driver接口==**，而且实现类必须包含一个静态初始化代码块。

驱动实现类需要在静态初始化代码块中向DriverManager注册自己的一个实例：

```java
public class AcmeJdbcDriver implements java.sql.Driver {
	static {
		java.sql.DriverManager.registerDriver (new AcmeJdbcDriver());
  }
  ...
}
```

为了确保驱动程序可以使用这种机制加载，==`Driver`实现类需要提供一个无参数的构造方法==。

DriverManager类与注册的驱动程序进行交互时会调用Driver接口中提供的方法。DriverManager类可以通过Driver实现类的`acceptsURL()`来判断一个给定的URL是否能与数据库成功建立连接，通过`connect()`方法与数据库建立连接。

```java
Connection connect(String url, java.util.Properties info)
```

第一个参数为驱动能够识别的URL；第二个参数为与数据库建立连接需要的额外参数，例如用户名、密码等。

#### Java SPI机制简介

在JDBC 4.0版本之前，使用DriverManager获取Connection对象之前都需要通过代码显式地加载驱动实现类：

```java
Class.forName("com.mysql.cj.jdbc.Driver");
```

JDBC 4.0之后，就不需要显示加载了，这是得益于SPI机制。

==SPI==（Service Provider Interface）是JDK内置的一种服务提供发现机制。SPI是一种动态替换发现的机制。比如有一个接口，想在运行时动态地给它添加实现，只需要添加一个实现，SPI机制在程序运行时就会发现该实现类，整体流程：

![](images/image-20230525121535405.png)

当服务的提供者提供了一种接口的实现之后，需要在classpath下的`META-INF/services`目录中创建一个以服务接口命名的文件，这个文件中的内容就是这个接口具体的实现类。

各种驱动包中META-INF/services中都有java.sql.Driver做配置文件，这个配置文件中有接口的具体实现类名，根据这个类名加载服务实现类，然后就可以使用该服务了。

![](images/image-20230525165633510.png)

JDK中查找服务实现的工具类是`java.util.ServiceLoader`，使用方式：

```java
// 各种驱动包中META-INF/services中都有java.sql.Driver做配置文件，这个配置文件中有接口的具体实现类名
ServiceLoader<Driver> drivers = ServiceLoader.load(java.sql.Driver.class);
for (Driver driver : drivers) {
  System.out.println(driver.getClass().getName());
}
```

ServiceLoader类提供了一个静态的load()方法，用于==加载指定接口的所有实现类==。



DriverManager加载驱动实现类的过程：

```java
public class DriverManager {
	...
   static {
     loadInitialDrivers();
     println("JDBC DriverManager initialized");
   }
  ...
    
   private static void loadInitialDrivers() {
        ...

        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                ServiceLoader<Driver> loadedDrivers = ServiceLoader.load(Driver.class);  // SPI机制加载
                Iterator<Driver> driversIterator = loadedDrivers.iterator();

                try{
                    while(driversIterator.hasNext()) {
                        driversIterator.next();
                    }
                } catch(Throwable t) {
                // Do nothing
                }
                return null;
            }
        });
    
    ...
  
}
```

在loadInitialDrivers()方法中，通过JDK内置的ServiceLoader机制加载java.sql.Driver接口的实现类，然后对所有实现类进行遍历，这样就完成了驱动类的加载。驱动实现类会在自己的静态代码块中将驱动实现类的实例注册到DriverManager中，这样就取代了通过调用`Class.forName()`方法加载驱动的过程。



#### java.sql.DriverAction接口

Driver实现类在被加载时会调用DriverManager类的`registerDriver()`方法注册驱动。例如mysql8驱动的：

```java
public class Driver extends NonRegisteringDriver implements java.sql.Driver {
    public Driver() throws SQLException {
    }

    static {
        try {
            DriverManager.registerDriver(new Driver());
        } catch (SQLException var1) {
            throw new RuntimeException("Can't register driver!");
        }
    }
}
```

也可以在应用程序中显式地调用DriverManager类的`deregisterDriver()`方法来解除注册。

JDBC驱动可以通过实现`DriverAction`接口来监听DriverManager类的deregisterDriver()方法的调用。

🔖

DriverAction用于监听驱动类被解除注册事件，是驱动提供者需要关注的范畴，作为JDBC的使用者，我们只需要了解即可。

#### java.sql.DriverManager类

两个关键的静态方法：

- registerDriver()：该方法用于将驱动的实现类注册到DriverManager类中，这个方法会在驱动加载时隐式地调用，而且通常在每个驱动实现类的静态初始化代码块中调用。
- getConnection()：这个方法是提供给JDBC客户端调用的，可以接收一个JDBC URL作为参数，DriverManager类会对所有注册驱动进行遍历，调用Driver实现的connect()方法找到能够识别JDBC URL的驱动实现后，会与数据库建立连接，然后返回Connection对象。



#### javax.sql.DataSource接口

JDBC驱动程序都会实现DataSource接口，通过DataSource实现类的实例，返回一个Connection接口的实现类的实例。

使用DataSource对象可以提高应用程序的可移植性。可以使用JNDI（Java Naming and Directory Interface）把一个逻辑名称和数据源对象建立映射关系。🔖

DataSource对象用于表示能够提供数据库连接的数据源对象。如果数据库相关的信息发生了变化，则可以简单地修改DataSource对象的属性来反映这种变化，而不用修改应用程序的任何代码。

DataSource接口可以被实现，提供如下两种功能：

- 通过连接池提高系统性能和伸缩性。
- 通过XADataSource接口支持分布式事务。



DataSource接口的实现必须包含一个无参构造方法。

![](images/image-20230525121705665.png)



#### 使用JNDI API增强应用的可移植性🔖

==JNDI（Java Naming and Directory Interface，Java命名和目录接口）==为应用程序提供了一种通过网络访问远程服务的方式。

JNDI API的命名服务可以**把一个逻辑名称和一个具体的对象绑定**。



[JNDI规范文档](https://docs.oracle.com/cd/E17802_01/products/products/jndi/javadoc/)

#### 关闭Connection对象



### 2.4 Statement详解

#### java.sql.Statement接口

每个Connection对象可以同时创建多个Statement对象。

Connection接口中还提供了几个重载的createStatement()方法，用于通过Statement对象指定ResultSet（结果集）的属性，例如：

```java
				Connection conn = dataSource.getConnection();
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,  // 创建的ResultSet对象是可滚动的
                ResultSet.CONCUR_UPDATABLE,  // 可以修改的
                ResultSet.HOLD_CURSORS_OVER_COMMIT); // 当修改提交时ResultSet不会被关闭
```

Statement的主要作用是==与数据库进行交互==，该接口中定义了一些数据库操作以及检索SQL执行结果相关的方法，具体如下：

```java
# 批量执行SQl
void addBatch(String sql)
void clearBatch()
int[] executeBatch()
# 执行未知SQl语句
boolean execute(String sql)
boolean execute(String sql, int autoGeneratedKeys) boolean execute (String sql, int [] columnIndexes) boolean execute (String sql, String[] columnNames)
# 执行查询语句
ResultSet executeQuery(String sql)
# 执行更新语句，包括UPDATE、DELETE、INSERT
int executeUpdate(String sql)
int executeUpdate(String sql, int autoGeneratedKeys) 
int executeUpdate(String sql, int [] columnIndexes) 
int executeUpdate(String sql, String[] columnNames)
# SQl执行结果处理
long getLargeUpdateCount()
ResultSet getResultSet()
int getUpdateCount()
boolean getMoreResults()
boolean getMoreResults(int current)
ResultSet getGeneratedKeys()
# JDBC 4.2 新增，数据量大于 Integer.MAX_VALUE 时使用
long [] executeLargeBatch()
long executeLargeUpdate(String sql)
long executeLargeUpdate(String sql, int autoGeneratedKeys) long executeLargeUpdate(String sql, int [] columnIndexes) 
long executeLargeUpdate(String sql, String[] columnNames)
# 取消SQL执行，需要数据库和驱动支持
void cancel()
＃ 关闭 Statement 对象
void close()
void closeOnCompletion()
```

🔖

#### java.sql.PreparedStatement接口

PreparedStatement接口继承自Statement接口，在Statement接口的基础上增加了==参数占位符==功能。



```java
String sql = "insert into user (create_time, name, password, phone, nick_name) values(?,?,?,?,?)";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setString(1, "2022-10-26 10:20:30");
ps.setString(2, "andy");
ps.setString(3, "123456");
ps.setString(4, "1395226445");
ps.setString(5, "big day");
ps.execute();

Statement st = conn.createStatement();
ResultSet resultSet = st.executeQuery("select * from user");
DbUtils.dumpRS(resultSet);

conn.close();
```



PreparedStatement对象设置的参数在执行后不能被重置，需要显式地调用clearParameters()方法清除先前设置的值，再为参数重新设置值即可。🔖



![](images/image-20230525122127384.png)

PreparedStatement接口中提供了一个setObject()方法，可以将Java类型转换为JDBC类型。该方法可以接收三个参数，第一个参数为占位符位置，第二个参数为Java对象，第三个参数是要转换成的JDBC类型。

```JAVA
Integer value = new Integer(15);
ps.setObject(1, value, java.sql.Types.SHORT);
```



PreparedStatement接口中提供了一个getParameterMetaData()方法，用于获取`ParameterMetaData`（描述PreparedStatement对象的参数信息，包括参数个数、参数类型等）实例。

#### java.sql.CallableStatement接口🔖

`CallableStatement`接口继承自PreparedStatement接口，在PreparedStatement的基础上增加了==调用存储过程并检索调用结果==的功能。

#### 获取自增长的键值

Statement接口中提供了getGeneratedKeys()方法，用于获取数据库自动生成的值，该方法返回一个ResultSet对象，我们可以从ResultSet对象中获取数据库中所有自增长的键值。

### 2.5 ResultSet详解

ResultSet接口提供了检索和操作SQL执行结果相关的方法。

#### ResultSet类型

ResultSet对象的类型主要体现在两个方面：

1. 游标可操作的方式。
2. ResultSet对象的修改对数据库的影响。（ResultSet对象的==敏感性==）

ResultSet有3种不同的类型：

1. `TYPE_FORWARD_ONLY`：【默认值】不可滚动，游标只能向前移动；也就是只能使用next()方法，而不能使用previous()方法。
2. `TYPE_SCROLL_INSENSITIVE`：可滚动。对ResultSet对象的修改不会影响对应的数据库中的记录。
3. `TYPE_SCROLL_SENSITIVE`：可滚动。对ResultSet对象的修改会直接影响数据库中的记录。

#### ResultSet并行性

- `CONCUR_READ_ONLY`：【默认值】只能从ResulSet对象中读取数据，但是不能更新ResultSet对象中的数据。
- `CONCUR_UPDATABLE`：既可以从ResulSet对象中读取数据，又能更新ResultSet中的数据。



#### ResultSet可保持性

调用Connection对象的commit()方法能够关闭当前事务中创建的ResultSet对象。然而，在某些情况下，这可能不是我们期望的行为。ResultSet对象的`holdability`属性使得应用程序能够在Connection对象的commit()方法调用后控制ResultSet对象是否关闭。

- `HOLD_CURSORS_OVER_COMMIT`：当调用Connection对象的commit()方法时，不关闭当前事务创建的ResultSet对象。
- `CLOSE_CURSORS_AT_COMMIT`：当前事务创建的ResultSet对象在事务提交后会被关闭，对一些应用程序来说，这样能够提升系统性能。

#### ResultSet属性设置

ResultSet的类型、并行性和可保持性等属性可以在调用Connection对象的createStatement()、prepareStatement()或prepareCall()方法创建Statement对象时设置。



#### ResultSet游标移动

ResultSet对象中维护了一个游标，游标指向当前数据行。当ResultSet对象第一次创建时，游标指向数据的第一行。

- next()
- previous()
- first()
- last()
- beforeFirst()：移动游标到ResultSet对象的第一行之前，如果ResultSet对象不包含任何数据行，则该方法不生效。
- afterLast()：游标位置移动到ResultSet对象最后一行之后，如果ResultSet对象中不包含任何行，则该方法不生效。
- relative(int rows)：相对于当前位置移动游标。
- absolute(int row)：游标定位到ResultSet对象中的第row行。



#### 修改ResultSet对象🔖



#### 关闭ResultSet对象



> ResultSet对象关闭后，不会关闭由ResultSet对象创建的Blob、Clob、NClob或SQLXML对象，除非调用这些对象的free()方法。

### 2.6 DatabaseMetaData详解

DatabaseMetaData接口是由JDBC驱动程序实现的，用于提供**底层数据源相关的信息**。

DatabaseMetaData接口中包含超过150个方法，根据这些方法的类型可以分为以下几类：

1. 获取数据源信息。
2. 确定数据源是否支持某一特性或功能。
3. 获取数据源的限制。
4. 确定数据源包含哪些SQL对象以及这些对象的属性。
5. 获取数据源对事务的支持。

DatabaseMetaData接口中有超过40个字段，这些字段都是常量，用于DatabaseMetaData接口中各个方法的返回值。

#### 创建DatabaseMetaData对象

Connection对象中提供了一个getMetadata()方法，用于创建DatabaseMetaData对象。

#### 获取数据源的基本信息

```java
getURL()：获取数据库URL。
getUserName()：获取数据库已知的用户。
getDatabaseProductName()：获取数据库产品名。
getDatabaseProductVersion()：获取数据库产品的版本。
getDriverMajorVersion()：获取驱动主版本。
getDriverMinorVersion()：获取驱动副版本。
getSchemaTerm()：获取数据库供应商用于Schema的首选术语。
getCatalogTerm()：获取数据库供应商用于Catalog的首选术语。
getProcedureTerm()：获取数据库供应商用于Procedure的首选术语。
nullsAreSortedHigh()：获取null值是否高排序。
nullsAreSortedLow()：获取null值是否低排序。
usesLocalFiles()：获取数据库是否将表存储在本地文件中。
usesLocalFilePerTable()：获取数据库是否为每个表使用一个文件。
getSQLKeywords()：获取数据库SQL关键字。
```

#### 获取数据源支持特性

```java
supportsAlterTableWithDropColumn()：检索此数据源是否支持带有删除列的ALTER TABLE语句。
supportsBatchUpdates()：检索此数据源是否支持批量更新。supportsTableCorrelationNames()：检索此数据源是否支持表相关名称。
supportsPositionedDelete()：检索此数据源是否支持定位的DELETE语句。
supportsFullOuterJoins()：检索此数据源是否支持完整地嵌套外部连接。
supportsStoredProcedures()：检索此数据源是否存储过程。
supportsMixedCaseQuotedIdentifiers()：检索此数据源是否将用双引号引起来的大小写混合的SQL标识符视为区分大小写，并以混合大小写方式存储它们。下面的方法用于判断数据库对某些特性支持的级别。
supportsANSI92EntryLevelSQL()：检索此数据源是否支持ANSI92入门级SQL语法。
supportsCoreSQLGrammar()：检索此数据源是否支持ODBC核心SQL语法。
```

#### 获取数据源限制

```java
getMaxRowSize()：获取最大行数。
getMaxStatementLength()：获取此数据库在SQL语句中允许的最大字符数。
getMaxTablesInSelect()：获取此数据库在SELECT语句中允许的最大表数。
getMaxConnections()：获取此数据库支持的最大连接数。
getMaxCharLiteralLength()：获取数据库支持的字符串字面量长度。
getMaxColumnsInTable()：获取数据库表中允许的最大列数。
```

这些方法返回值为int类型，当返回值为0时，表示没有限制或限制未知。

#### 获取SQL对象及属性

```java
getSchemas()：获取Schema信息。
getCatalogs()：获取Catalog信息。
getTables()：获取表信息。
getPrimaryKeys()：获取主键信息。
getProcedures()：获取存储过程信息。
getProcedureColumns()：获取给定类别的存储过程参数和结果列的信息。
getUDTs()：获取用户自定义数据类型。
getFunctions()：获取函数信息。
getFunctionColumns()：获取给定类别的函数参数和结果列的信息。
```

这些方法的返回值是一个ResultSet对象。该ResultSet对象的类型为TYPE_FORWARD_ONLY，并行性为CONCUR_READ_ONLY。

#### 获取事务支持

```java
supportsTransactionIsolationLevel(int level)：是否支持某一事务隔离级别。
supportsTransactions()：是否支持事务。
getDefaultTransactionIsolation()：获取默认的事务隔离级别。
supportsMultipleTransactions()：是否支持同时开启多个事务。
```



### 2.7 JDBC事务🔖

事务用于提供数据完整性、正确的应用程序语义和并发访问的数据一致性。所有遵循JDBC规范的驱动程序都需要提供事务支持。

JDBC API中的事务管理符合SQL:2003规范，主要包含下面几个概念：

- 自动提交模式
- 事务隔离级别
- 保存点

## 3 MyBatis常用工具类

### 3.1 使用SQL类生成语句



![SQL工具类的方法及作用](images/SQL工具类的方法及作用.jpg)

`AbstractSQL`

```java
		/**
     * SQL语句拼接
     * @param builder SQL字符串构建对象
     * @param keyword SQL关键字
     * @param parts SQL关键字子句内容
     * @param open SQL关键字后开始字符
     * @param close SQL关键字后结束字符
     * @param conjunction SQL连接关键字，通常为AND或OR
     */
    private void sqlClause(SafeAppendable builder, String keyword, List<String> parts, String open, String close,
        String conjunction) {
```



### 3.2 使用ScriptRunner执行脚本

读取脚本文件中的SQL语句并执行。

仅提供了一个`runScript()`方法用于执行SQL脚本文件。其余各种属性通过setter方法设置。

```java
public class ScriptRunner {
  ...

  private final Connection connection;
  // SQL异常是否中断程序执行
  private boolean stopOnError;
  // 是否抛出SQLWarning警告
  private boolean throwWarning;
  // 是否自动提交
  private boolean autoCommit;
  // true，批量执行文件中的SQL语句
  // false，逐条执行SQL语句，默认SQL语句以分号分割
  private boolean sendFullScript;
  // 是否去除Windows系统换行符中的\r
  private boolean removeCRs;
  // 设置Statement属性是否支持转义处理
  private boolean escapeProcessing = true;

  // 日志输出位置，默认标准输入输出，即控制台
  private PrintWriter logWriter = new PrintWriter(System.out);
  private PrintWriter errorLogWriter = new PrintWriter(System.err);

  // 脚本文件中SQL语句的分隔符，默认为分号
  private String delimiter = DEFAULT_DELIMITER;
  // 是否支持SQL语句分隔符，单独一行
  private boolean fullLineDelimiter;

  public ScriptRunner(Connection connection) {
    this.connection = connection;
  }
  
  ...
} 
```



### 3.3 使用SqlRunner操作数据库

`SqlRunner`是非常实用的、用于操作数据库的工具类，该类对JDBC做了很好的封装，结合SQL工具类，能够很方便地通过Java代码执行SQL语句并检索SQL执行结果。

```java
SqlRunner#selectOne(String sql, Object… args)：执行SELECT语句，SQL语句中可以使用占位符，如果SQL中包含占位符，则可变参数用于为参数占位符赋值，该方法只返回一条记录。若查询结果行数不等于一，则会抛出SQLException异常。
SqlRunner#selectAll(String sql, Object… args)：该方法和selectOne()方法的作用相同，只不过该方法可以返回多条记录，方法返回值是一个List对象，List中包含多个Map对象，每个Map对象对应数据库中的一行记录。
SqlRunner#insert(String sql, Object… args)：执行一条INSERT语句，插入一条记录。
SqlRunner#update(String sql, Object… args)：更新若干条记录。SqlRunner#delete(String sql, Object… args)：删除若干条记录。
SqlRunner#run(String sql)：执行任意一条SQL语句，最好为DDL语句。
```



### 3.4 MetaObject详解

`MetaObject`是MyBatis中的反射工具类，该工具类在MyBatis源码中出现的频率非常高。通过它可以很优雅地获取和设置对象的属性值。

```java
public class TestMetaObject {
    @Data
    @AllArgsConstructor
    private static class User {
        List<Order> orders;
        String name;
        Integer age;
    }
    @Data
    @AllArgsConstructor
    private static class Order {
        String orderNo;
        String goodsName;
    }

    @Test
    public void test() {
        List<Order> orders = new ArrayList() {
            {
                add(new Order("order20171024010246", "《Mybatis源码深度解析》图书"));
                add(new Order("order20171024010248", "《代码大全》图书"));
            }
        };
        User user = new User(orders, "Andy", 29);
        MetaObject metaObject = SystemMetaObject.forObject(user);
        //
        System.out.println(metaObject.getValue("orders[0].goodsName"));
        System.out.println(metaObject.getValue("orders[1].goodsName"));
        // 为属性设置值
        metaObject.setValue("orders[1].orderNo", "order20171024010255");
        System.out.println(metaObject.hasGetter("orderNo"));
        System.out.println(metaObject.hasGetter("name"));
    }
}
```

调用SystemMetaObject类的forObject()静态方法创建一个与User对象关联的MetaObject对象。我们可以通过MetaObject对象的getValue()方法以表达式的方式获取User对象的属性值。我们还可以使用MetaObject对象的setValue()方法以表达式的方式为User对象的属性设置值。**当类的层级比较深时，使用MetaObject工具能够很方便地获取和设置对象的属性值**。除此之外，我们还可以使用MetaObject工具类的hasSetter()和hasGetter()方法通过名称判断对象是否有某个属性且该属性有对应的Getter/Setter方法。

### 3.5 MetaClass详解

MetaObject用于获取和设置对象的属性值，而MetaClass则用于获取类相关的信息。

```java
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
```



### 3.6 ObjectFactory详解

ObjectFactory是MyBatis中的对象工厂，MyBatis每次创建Mapper映射结果对象的新实例时，都会使用一个对象工厂（ObjectFactory）实例来完成。ObjectFactory接口只有一个默认的实现，即`DefaultObjectFactory`。

```java
ObjectFactory objectFactory = new DefaultObjectFactory();
List<Integer> list = objectFactory.create(List.class);
Map<String, String> map = objectFactory.create(Map.class);
list.addAll(Arrays.asList(1, 2,3));
map.put("code", "代码大全");
System.out.println(list);
System.out.println(map);
```

DefaultObjectFactory实现类支持通过接口的方式创建对象，例如当我们指定创建java.util.List实例时，实际上创建的是java.util.ArrayList对象。List、Map、Set接口对应的实现分别为ArrayList、HashMap、HashSet。



自定义ObjectFactory🔖

### 3.7 ProxyFactory详解

`ProxyFactory`是MyBatis中的代理工厂，主要用于创建**动态代理对象**，ProxyFactory接口有两个不同的实现，分别为`CglibProxyFactory`和`JavassistProxyFactory`。对应两种动态代理策略，分别为Cglib和Javassist动态代理。🔖新版本过期

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



## 5 SqlSession的创建过程

SqlSession的创建过程拆解为3个阶段：Configuration实例的创建过程、SqlSessionFactory实例的创建过程和SqlSession实例化的过程。

### 5.1 XPath方式解析XML文件

MyBatis的主配置文件和Mapper配置都使用的是XML格式。MyBatis中的Configuration组件用于描述主配置文件信息，框架在启动时会解析XML配置，将配置信息转换为Configuration对象。

JDK API中提供了3种方式解析XML，分别为DOM、SAX和XPath。



### 5.2　Configuration实例创建过程



### 5.3　SqlSession实例创建过程



## 6 SqlSession执行Mapper过程

### 6.1 Mapper接口的注册过程



### 6.2 MappedStatement注册过程



### 6.3 Mapper方法调用过程详解



### 6.4 SqlSession执行Mapper过程



## 7 MyBatis缓存

MyBatis提供了一级缓存和二级缓存，其中一级缓存基于SqlSession实现，而二级缓存基于Mapper实现

### 7.1 MyBatis缓存的使用



### 7.2 MyBatis缓存实现类



### 7.3 MyBatis一级缓存实现原理



### 7.4 MyBatis二级缓存实现原理



### 7.5 MyBatis使用Redis缓存



## 8 MyBatis日志实现



## 9 动态SQL实现原理



## 10 MyBatis插件原理及应用



## 11 MyBatis级联映射与懒加载

懒加载，就是当我们在一个实体对象中关联其他实体对象时，如果不需要获取被关联的实体对象，则不需要为被关联的实体执行额外的查询操作，仅当调用当前实体的Getter方法获取被关联实体对象时，才会执行一次额外的查询操作。



## 12 MyBatis与Spring整合案例



## 13 MyBatis Spring的实现原理
