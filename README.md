# ReadMyBatis



JDBC 

```java
```







Mybatis3 小结

```java

DataSourceFactory
	UnpooledDataSourceFactory❤️
  	PooledDataSourceFactory
  JndiDataSourceFactory
```







### 源码看#{}和${}的区别

https://blog.csdn.net/Zhs901026/article/details/103911937



- `#{param}`不仅仅涉及参数替换，还涉及参数类型的处理，这是`${}`不能代替的，也就是说使用`${}`来替换#{}本身就不符合mybatis的使用原则，所以两者并没有安全性比较的意义！
- `#{param}`只能用于statementType=“PREPARED"情况，因为#{param}在mybatis内部肯定会被替换成”?"的，这就要求必须使用`PreparedStatement`来处理，这是mybatis内部原理实现的，并不是很多博文所说的#{param}会加上"引号"云云…如果#{param}代表的是数字，mybatis断然不会给该数字加"引号"的。所以说#{}能有效预防sql注入是因为底层使用了PreparedStatement，而不是其他任何原因。
