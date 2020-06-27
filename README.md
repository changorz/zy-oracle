#  Oracle课程设计-教务爬虫

## 一、数据库构建

> 创建表空间

```sql
# 创建一个名叫
create tablespace data01 datafile '/home/oracle/data/data01.dbf' size 10m
extent management local uniform size 1m
segment space management manual;
```



> 查看表空间

参考文献：[查看表空间](https://blog.csdn.net/lizhangyong1989/article/details/49708825)

```sql
select *
  from (Select a.tablespace_name,
               to_char(a.bytes / 1024 / 1024, '99,999.999') total_bytes,
               to_char(b.bytes / 1024 / 1024, '99,999.999') free_bytes,
               to_char(a.bytes / 1024 / 1024 - b.bytes / 1024 / 1024,
                       '99,999.999') use_bytes,
               to_char((1 - b.bytes / a.bytes) * 100, '99.99') || '%' use
          from (select tablespace_name, sum(bytes) bytes
                  from dba_data_files
                 group by tablespace_name) a,
               (select tablespace_name, sum(bytes) bytes
                  from dba_free_space
                 group by tablespace_name) b
         where a.tablespace_name = b.tablespace_name
        union all
        select c.tablespace_name,
               to_char(c.bytes / 1024 / 1024, '99,999.999') total_bytes,
               to_char((c.bytes - d.bytes_used) / 1024 / 1024, '99,999.999') free_bytes,
               to_char(d.bytes_used / 1024 / 1024, '99,999.999') use_bytes,
               to_char(d.bytes_used * 100 / c.bytes, '99.99') || '%' use
          from (select tablespace_name, sum(bytes) bytes
                  from dba_temp_files
                 group by tablespace_name) c,
               (select tablespace_name, sum(bytes_cached) bytes_used
                  from v$temp_extent_pool
                 group by tablespace_name) d
         where c.tablespace_name = d.tablespace_name)
 order by tablespace_name ;
```

![](https://pic.gksec.com/2020/06/26/bac411da8903e/20200626162904.png)



>创建用户

```sql
CREATE USER chang IDENTIFIED BY **** DEFAULT TABLESPACE data01;
```



> 用户授权-->授权角色

参考文献：[Oracle用户授权](https://blog.csdn.net/kukulongzai_123/article/details/47416257?utm_medium=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.nonecase&depth_1-utm_source=distribute.pc_relevant.none-task-blog-BlogCommendFromMachineLearnPai2-1.nonecase)

```sql
grant create session, connect, resource to chang;
```



> 使用新用户登录

![](https://pic.gksec.com/2020/06/26/dfa75ae68235c/20200626164415.png)



> 可视化工具连接

我这里使用的是`Navicat Premium`，使用`chang`用户连接后，只能看到部分`system`用户的表，代表连接成功

![](https://pic.gksec.com/2020/06/26/dea6073e60c78/20200626164843.png)



> 创建表格

根据教务系统返回`json`数据创建用户表`user表`与成绩`performance表`

用户`json`数据

```json
{
    "fxzy": "无",                                         // 辅修专业
    "xh": "201716xxxx",                                   // 学号
    "xm": "某某某",                                        // 姓名
    "dqszj": "2017",                                      // 未知
    "usertype": "2",                                      // 用户类型
    "yxmc": "信息科学技术学院",                             // 院系名称
    "xz": 4,                                              // 学制
    "bj": "计算机类2017-10",                               // 班级
    "dh": "13600000000",                                  // 电话
    "email": "1540000000@qq.com",                         // 电子邮箱
    "rxnf": "2017",                                       // 入学年份
    "xb": "男",                                           // 性别
    "ksh": "00000000000",                                 // 高考考号
    "nj": "2017",                                         // 年级
    "qq": null,                                           // QQ号码
    "zymc": "计算机类(计算机科学与技术、网络工程、信息安全)"   // 专业名称
}
```

```sql
# 创建表格
CREATE TABLE "CHANG"."user" (
  "fxzy" VARCHAR2(10 CHAR) ,
  "xh" VARCHAR2(8 CHAR) NOT NULL ,
  "xm" VARCHAR2(6 CHAR) ,
  "dqszj" VARCHAR2(4 CHAR) ,
  "usertype" VARCHAR2(1 CHAR) ,
  "yxmc" VARCHAR2(10 CHAR) ,
  "xz" VARCHAR2(1 CHAR) ,
  "bj" VARCHAR2(20 CHAR) ,
  "dh" VARCHAR2(20 CHAR) ,
  "email" VARCHAR2(30 CHAR) ,
  "rxnf" VARCHAR2(4 CHAR) ,
  "xb" VARCHAR2(1 CHAR) ,
  "ksh" VARCHAR2(20 CHAR) ,
  "nj" VARCHAR2(4 CHAR) ,
  "qq" VARCHAR2(12 CHAR) ,
  "zymc" VARCHAR2(20 CHAR) ,
  CONSTRAINT "SYS_C0011103" PRIMARY KEY ("xh"),
  CONSTRAINT "SYS_C0011088" CHECK ("xh" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE
)
TABLESPACE "DATA01"
LOGGING
NOCOMPRESS
PCTFREE 10
INITRANS 1
STORAGE (
  INITIAL 1048576 
  NEXT 1048576 
  MINEXTENTS 1
  MAXEXTENTS 2147483645
  FREELISTS 1
  FREELIST GROUPS 1
  BUFFER_POOL DEFAULT
)
PARALLEL 1
NOCACHE
DISABLE ROW MOVEMENT
```

成绩`json`数据

```sql
    {
        "bz": null,  //未知
        "cjbsmc": null,  //特殊情况通报，例如“作弊”“缺考”
        "kclbmc": "必修", //课程类别名称
        "zcj": "88",  //总成绩
        "xm": "某某某",  //学生姓名
        "xqmc": "2017-2018-2",  //学期名称
        "kcxzmc": "公共基础课",  //课程性质名称，根据此项不同值可判断该科成绩是否计入GPA
        "kcywmc": "College students career development and guidance",  //课程英文名称
        "ksxzmc": "正常考试",  //考试性质名称,目前遇见的情况有正常考试，补考（x），重修（x），分别意为补考第x次和重修第x次，若补考未通过，正常考试条目和补考条目将同时存在，若补考通过，则只存在补考条目
        "kcmc": "大学生职业发展与就业指导",  //课程名称
        "xf": 1  //学分
    }
```

```sql
# 创建表格
CREATE TABLE "CHANG"."`performance" (
  "xh" VARCHAR2(8 CHAR) NOT NULL ,
  "bz" VARCHAR2(20 CHAR) ,
  "cjbsmc" VARCHAR2(20 CHAR) ,
  "kclbmc" VARCHAR2(30 CHAR) ,
  "zcj" VARCHAR2(6 CHAR) ,
  "xm" VARCHAR2(6 CHAR) NOT NULL ,
  "xqmc" VARCHAR2(12 CHAR) ,
  "kcxzmc" VARCHAR2(20 CHAR) ,
  "kcywmc" VARCHAR2(255 CHAR) ,
  "ksxzmc" VARCHAR2(20 CHAR) ,
  "kcmc" VARCHAR2(30 CHAR) NOT NULL ,
  "xf" VARCHAR2(4 CHAR) ,
  CONSTRAINT "SYS_C0011116" FOREIGN KEY ("xh") REFERENCES "CHANG"."swxy_user" ("xh") NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE,
  CONSTRAINT "SYS_C0011104" CHECK ("xh" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE,
  CONSTRAINT "SYS_C0011109" CHECK ("xm" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE,
  CONSTRAINT "SYS_C0011114" CHECK ("kcmc" IS NOT NULL) NOT DEFERRABLE INITIALLY IMMEDIATE NORELY VALIDATE
)
TABLESPACE "DATA01"
LOGGING
NOCOMPRESS
PCTFREE 10
INITRANS 1
STORAGE (
  INITIAL 1048576 
  NEXT 1048576 
  MINEXTENTS 1
  MAXEXTENTS 2147483645
  FREELISTS 1
  FREELIST GROUPS 1
  BUFFER_POOL DEFAULT
)
PARALLEL 1
NOCACHE
DISABLE ROW MOVEMENT
;

CREATE INDEX 
  ON "CHANG"."Untitled" ("xh");

CREATE INDEX 
  ON "CHANG"."Untitled" ("kcmc")
```



> 创建成功

![](https://pic.gksec.com/2020/06/26/40628cbbec290/20200626180935.png)



## 二、项目搭建

> maven依赖

```xml
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-jdbc</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
		</dependency>
		<dependency>
			<groupId>org.mybatis.spring.boot</groupId>
			<artifactId>mybatis-spring-boot-starter</artifactId>
			<version>2.1.3</version>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-devtools</artifactId>
			<scope>runtime</scope>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>com.oracle.ojdbc</groupId>
			<artifactId>ojdbc8</artifactId>
			<scope>runtime</scope>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>

		<dependency>
			<groupId>com.alibaba</groupId>
			<artifactId>druid</artifactId>
			<version>1.1.22</version>
		</dependency>

		<dependency>
			<groupId>us.codecraft</groupId>
			<artifactId>webmagic-core</artifactId>
			<version>0.7.3</version>
		</dependency>
		<dependency>
			<groupId>us.codecraft</groupId>
			<artifactId>webmagic-extension</artifactId>
			<version>0.7.3</version>
		</dependency>
		<dependency>
			<groupId>com.alibaba</groupId>
			<artifactId>fastjson</artifactId>
			<version>1.2.62</version>
		</dependency>
```

> yaml配置

```yaml
server:
  port: 7878


spring:
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: oracle.jdbc.driver.OracleDriver
    url: jdbc:oracle:thin:@106.13.168.109:1521:helowin
    username: chang
    password: root
    initialSize: 5
    minIdle: 10
    maxActive: 20
    maxWait: 60000
    timeBetweenEvictionRunsMillis: 60000
    minEvictableIdleTimeMillis: 300000
    validationQuery: SELECT 1 FROM DUAL
    testWhileIdle: true
    testOnBorrow: false
    testOnReturn: false
    poolPreparedStatements: true
    filters: stat,wall
    maxPoolPreparedStatementPerConnectionSize: 20
    useGlobalDataSourceStat: true
    connectionProperties: druid.stat.mergeSql=true;druid.stat.slowSqlMillis=500


mybatis:
  typeAliasesPackage: chang.pojo
  mapperLocations: classpath:mapper/*.xml
logging:
  level:
    chang:
      mapper: debug
```

> 数据库映射文件

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="chang.dao.UserMapper">

    <select id="getUserCount" resultType="int">
        SELECT count(*) FROM "swxy_user"
    </select>

    <insert id="addUser" parameterType="User">
        INSERT INTO "swxy_user"
        ("fxzy", "xh", "xm", "dqszj", "usertype", "yxmc", "xz", "bj", "dh", "email",
        "rxnf", "xb", "ksh", "nj", "qq", "zymc") VALUES
        (#{fxzy,jdbcType=VARCHAR}, #{xh,jdbcType=VARCHAR}, #{xm,jdbcType=VARCHAR}, #{dqszj,jdbcType=VARCHAR}, #{usertype,jdbcType=VARCHAR}, #{yxmc,jdbcType=VARCHAR}, #{xz,jdbcType=VARCHAR}, #{bj,jdbcType=VARCHAR}, #{dh,jdbcType=VARCHAR}, #{email,jdbcType=VARCHAR},
        #{rxnf,jdbcType=VARCHAR}, #{xb,jdbcType=VARCHAR}, #{ksh,jdbcType=VARCHAR}, #{nj,jdbcType=VARCHAR}, #{qq,jdbcType=VARCHAR}, #{zymc,jdbcType=VARCHAR})
    </insert>

</mapper>
```

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
<mapper namespace="chang.dao.PerformanceMapper">

    <select id="getPerformanceCount" resultType="int">
        select count(*) from "performance"
    </select>

    <insert id="addPerformance" parameterType="performance">
        INSERT INTO "CHANG"."performance"
        ("xh", "bz", "cjbsmc", "kclbmc", "zcj", "xm", "xqmc", "kcxzmc", 
        "kcywmc", "ksxzmc", "kcmc", "xf") 
        VALUES 
        (#{xh,jdbcType=VARCHAR}, #{bz,jdbcType=VARCHAR}, #{cjbsmc,jdbcType=VARCHAR}, #{kclbmc,jdbcType=VARCHAR}, #{zcj,jdbcType=VARCHAR}, #{xm,jdbcType=VARCHAR}, #{xqmc,jdbcType=VARCHAR}, #{kcxzmc,jdbcType=VARCHAR},
        #{kcywmc,jdbcType=VARCHAR}, #{ksxzmc,jdbcType=VARCHAR}, #{kcmc,jdbcType=VARCHAR}, #{xf,jdbcType=VARCHAR})
    </insert>

    <insert id="addPerformanceList" parameterType="performance">
         INSERT INTO "CHANG"."performance"
        ("xh", "bz", "cjbsmc", "kclbmc", "zcj", "xm", "xqmc", "kcxzmc",
        "kcywmc", "ksxzmc", "kcmc", "xf")
        <foreach collection="list" item="item" separator="union all">
            (SELECT
            #{item.xh,jdbcType=VARCHAR},
            #{item.bz,jdbcType=VARCHAR},
            #{item.cjbsmc,jdbcType=VARCHAR},
            #{item.kclbmc,jdbcType=VARCHAR},
            #{item.zcj,jdbcType=VARCHAR},
            #{item.xm,jdbcType=VARCHAR},
            #{item.xqmc,jdbcType=VARCHAR},
            #{item.kcxzmc,jdbcType=VARCHAR},
            #{item.kcywmc,jdbcType=VARCHAR},
            #{item.ksxzmc,jdbcType=VARCHAR},
            #{item.kcmc,jdbcType=VARCHAR},
            #{item.xf,jdbcType=VARCHAR}
            from dual)
        </foreach>
    </insert>

</mapper>
```

> 错误解决

```sql
### The error may exist in file [C:\Users\chang\Desktop\note\work\Oracle\work-oracle\target\classes\mapper\PerformanceMapper.xml]
### The error may involve chang.dao.PerformanceMapper.addPerformanceList-Inline
### The error occurred while setting parameters
### SQL: INSERT INTO "CHANG"."performance"         ("xh", "bz", "cjbsmc", "kclbmc", "zcj", "xm", "xqmc", "kcxzmc",         "kcywmc", "ksxzmc", "kcmc", "xf")..............
### Cause: java.sql.SQLException: ORA-01653: 表 CHANG.performance 无法通过 128 (在表空间 DATA01 中) 扩展

解决：
alter database datafile '/home/oracle/data/data01.dbf' autoextend on next 50M maxsize unlimited;
```

> 测试

![](https://pic.gksec.com/2020/06/27/8e4fb6bbc867c/20200627150842.png)

![](https://pic.gksec.com/2020/06/27/2c9b25bd40c64/20200627162232.png)



> 调优

- 增学号索引，提高查询效率
- 一次插入多行数据，减少连接带来的时间消耗
- 多线程并发



> 说明

由于这是数据库课程设计，详细项目代码请访问[https://github.com/changorz/zy-oracle](https://github.com/changorz/zy-oracle)

















