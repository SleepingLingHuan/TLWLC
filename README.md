# 校园二手交易平台

一个基于 Vue.js 和 Spring Boot 的校园二手交易平台，支持用户注册、登录、发布商品、购买商品等功能。

## 功能特性

### 用户功能
- 用户注册和登录
- 个人中心管理
- 个人信息编辑
- 查看交易记录

### 商品功能
- 发布二手商品
- 商品分类展示
- 商品标签管理（最多3个标签）
- 商品搜索功能

### 交易功能
- 商品购买
- 交易记录查看
- 订单状态管理

## 技术栈

### 前端
- Vue.js 3
- Ant Design Vue
- Vue Router
- Axios
- Element Plus

### 后端
- Spring Boot
- Spring Security
- JWT 认证
- MySQL
- MyBatis

## 项目结构

```
campus-trade/
├── campus/                 # 前端项目
│   ├── src/
│   │   ├── components/    # Vue 组件
│   │   ├── router/        # 路由配置
│   │   ├── api/          # API 接口
│   │   └── App.vue       # 根组件
│   └── package.json      # 前端依赖配置
│
└── campus-trade-backend/  # 后端项目
    ├── src/
    │   └── main/
    │       ├── java/     # Java 源代码
    │       └── resources/# 配置文件
    └── pom.xml          # 后端依赖配置
```

## 开发环境要求

- Node.js >= 14.0.0
- Java >= 8
- MySQL >= 5.7
- Maven >= 3.6

## 安装和运行

### 前端

1. 进入前端项目目录：
```bash
cd campus
```

2. 安装依赖：
```bash
npm install
```

3. 启动开发服务器：
```bash
npm run dev
```

### 后端

1. 进入后端项目目录：
```bash
cd campus-trade-backend
```

2. 安装依赖：
```bash
mvn install
```

3. 启动应用：
```bash
mvn spring-boot:run
```

## 配置说明

### 数据库配置
在 `campus-trade-backend/src/main/resources/application.properties` 中配置数据库连接信息：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/campus_trade
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 前端配置
在 `campus/src/api/config.js` 中配置后端 API 地址：
```javascript
baseURL: 'http://localhost:8080'
```

## 主要更新内容

### 2024-03-xx
- 实现用户注册和登录功能
- 添加个人中心页面
- 实现商品发布功能
- 添加商品标签管理（最多3个标签）
- 优化页面布局和交互体验

## 贡献指南

1. Fork 本仓库
2. 创建新的功能分支
3. 提交你的更改
4. 发起 Pull Request

## 许可证

MIT License
