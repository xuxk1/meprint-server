# meprint-server
<h1 style="text-align: center">MEPRINT 质量管理平台</h1>
<div style="text-align: center">


</div>

#### 项目简介
一个基于 Spring Boot 2.1.0 、 Spring Boot Jpa、Spring Boot Mybatis、 JWT、Spring Security、Redis、Vue的前后端分离的后台管理系统


**账号密码：** `admin / 123456`

#### 项目源码

|          |    后端源码                                         |    前端源码                                     |
|--- |---  |                                                 --- |                                              ---|
|  gitlab  |  http://gitlab.diy8.com/mihui_qa1/meprint-server    |  http://gitlab.diy8.com/mihui_qa1/meprint-web   |

#### 主要特性
- 使用最新技术栈，社区资源丰富。
- 高效率开发，代码生成器可一键生成前后端代码
- 支持数据字典，可方便地对一些状态进行管理
- 支持接口限流，避免恶意请求导致服务层压力过大
- 支持接口级别的功能权限与数据权限，可自定义操作
- 自定义权限注解与匿名接口注解，可快速对接口拦截与放行
- 对一些常用地前端组件封装：表格数据请求、数据字典等
- 前后端统一异常拦截处理，统一输出异常，避免繁琐的判断
- 支持在线用户管理与服务器性能监控，支持限制单用户登录
- 支持运维管理，可方便地对远程服务器的应用进行部署与管理

####  系统功能
- 首页：数据大盘
- 用例管理：对用例和项目进行分配，可根据用户设置对应的操作权限
- 项目管理：可配置项目，树形表格展示
- 用户管理：提供用户的相关配置，新增用户后，默认密码为123456
- 角色管理：对权限与菜单进行分配，可根据部门设置角色的数据权限
- 菜单管理：已实现菜单动态路由，后端可配置化，支持多级菜单
- 部门管理：可配置系统组织架构，树形表格展示
- 岗位管理：配置各个部门的职位
- 字典管理：可维护常用一些固定的数据，如：状态，性别等

#### 项目结构
项目采用按功能分模块的开发方式，结构如下

- `meprint-common` 为系统的公共模块，各种工具类，公共配置存在该模块

- `meprint-system` 为系统核心模块也是项目入口模块，也是最终需要打包部署的模块

- `meprint-logging` 为系统的日志模块，其他模块如果需要记录日志需要引入该模块

- `meprint-tools` 为第三方工具模块，包含：图床、邮件、云存储、本地存储、支付宝

- `meprint-generator` 为系统的代码生成模块，代码生成的模板在 system 模块中

#### 详细结构

```
- meprint-common 公共模块
    - annotation 为系统自定义注解
    - aspect 自定义注解的切面
    - base 提供了Entity、DTO基类和mapstruct的通用mapper
    - config 自定义权限实现、redis配置、swagger配置、Rsa配置等
    - exception 项目统一异常的处理
    - utils 系统通用工具类
- meprint-system 系统核心模块（系统启动入口）
	- config 配置跨域与静态资源，与数据权限
	    - thread 线程池相关
	- modules 系统相关模块(登录授权、系统监控、定时任务、运维管理等)
- meprint-logging 系统日志模块
- meprint-tools 系统第三方工具模块
- meprint-generator 系统代码生成模块
```