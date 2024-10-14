# huayang-starter-parent
### 1. 特殊说明：
1. [shiro-redis](shiro-redis)是由于中央仓库中的代码对新版jedis兼容性问题，进行代码修改后发布私服的源码；
2. [hmc-flowable7-ui](hmc-flowable7-ui)是由于flowable7没有ui了，此代码提供基本的流程设计，是一个独立的应用程序；
3. 项目中引入hutool包的6.0.0里程碑版本,尽量不要使用(在确保没有问题的情况下可以使用,优先使用旧版本,等未来6.x正式版本发布后再推荐使用)
### 2. 基于若依RuoYi v4.7.9-springboot3.0版本创建的原型功能：
#### 2.1 项目源代码地址：https://gitee.com/y_project/RuoYi/tree/springboot3/
#### 2.2 已经实现了如下集成：
1. 升级springboot到最新版本3.x；
2. 使用undertow来替代tomcat容器；
3. 集成mybatis-plus实现mybatis增强；
4. 集成easyexcel实现excel表格增强；
5. 集成dynamic-datasource实现多数据源增强；
6. 待续。。。
### 3. 使用说明:
1. [project-starter](project-starter)是原型工程:会发布到私服仓库中,本地创建项目步骤:
   1. 文件--新建--项目;
   2. Maven Archetype--目录(huayang-snapshot)--Archetype(com.huayang.project:project-starter-archetype);
   3. 版本最新的;
   4. 修改项目名称:等待项目创建完成;
   5. 修改数据库和Redis连接信息,启动项目,进入开发;
2. 只是使用本框架提供的同意springboot版本管理,使用方法如下:
   1. 创建Maven项目,将parent修改如下:
   ```
   <parent>
       <groupId>com.huayang.product</groupId>
       <artifactId>huayang-starter-parent</artifactId>
       <version>0.1-SNAPSHOT</version>
       <relativePath/> <!-- lookup parent from repository -->
   </parent>
   ```
   2. 本框架中定义了shiro,mybatis-plus,mybatis-plus-join,dynamic-datasource,pagehelper,hutool包,flowable等常用组件的版本号,项目中使用到的其他特殊组件另行定义;
