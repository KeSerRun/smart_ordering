# SmartOrdering 智慧点餐系统

跨端智慧点餐系统：**商家管理后台** + **顾客扫码点餐小程序**（微信等平台）+ **后厨大屏**，基于独立单体服务架构，基础设施全部用 Docker 一键拉起。

- 技术栈：`Spring Boot 3.2.5` + `MyBatis-Plus` + `Sa-Token` + `Redis` + `RabbitMQ` + `MinIO` + `WebSocket(STOMP)`
- 前端管理后台：`Vue 3` + `Vite` + `Naive UI` + `Pinia`（精简 JS 版）
- 点餐小程序：`uni-app`（Vue 3，一次编写多端发布）

---

## 目录结构

```
smart_ordering/
├── docker-compose.yml     # 一键拉起 MySQL / Redis / RabbitMQ / MinIO
├── data/docker/           # Docker 数据卷（运行数据，已 gitignore）
├── server/                # 后端 Spring Boot 单体服务（端口 8080）
│   └── src/main/resources/
│       ├── schema.sql     # 启动自动建表（每次启动执行）
│       ├── data.sql       # 启动自动灌种子数据（admin 用户 / 权限）
│       └── application.yml / application-dev.yml
├── admin/                 # 商家管理后台前端（Vite 端口 5173，代理 /api→8080）
├── miniapp/               # 顾客扫码点餐 uni-app 小程序（多端）
└── doc/
    └── RBAC.md            # RBAC 权限模型 + Sa-Token 认证原理速查
```

---

## 基础设施（Docker）

一键启动全部中间件：

```powershell
docker compose up -d
```

| 服务 | 端口 | 账号 / 密码 | 说明 |
|---|---|---|---|
| MySQL 8.0 | 3306 | `root` / `root123456` | 库 `smart_ordering` |
| Redis 7 | 6379 | 密码 `redis123456` | Sa-Token 登录态 + 权限缓存 |
| RabbitMQ | 5672 / **15672** | `so` / `so123456` | 可靠消息（管理台 http://localhost:15672） |
| MinIO | 9000 / **9001** | `minioadmin` / `minioadmin123` | 文件上传对象存储（控制台 http://localhost:9001） |

> 默认账号密码与端口配置见 `server/src/main/resources/application-dev.yml`，修改后重启后端即可。

---

## 后端启动（server/）

环境要求：JDK 17+、Maven 3.6+。首次请先 `docker compose up -d` 确保中间件就绪。

```powershell
cd server
mvn spring-boot:run
```

- 后端端口：`8080`，全局上下文路径 `/api`
- **Knife4j 接口文档**：http://localhost:8080/api/doc.html
- 健康检查：http://localhost:8080/api/health
- 建表 / 种子数据：每次启动自动执行 `schema.sql` / `data.sql`（`spring.sql.init.mode: always`）

管理端默认登录：`admin` / `123456`（BCrypt 加密，可查看 `doc/RBAC.md` 了解认证与 RBAC 链路）。

---

## 管理后台启动（admin/）

环境要求：Node 18+。

```powershell
cd admin
npm install
npm run dev
```

- 前端端口：`5173`，已配置 `/api` 代理到后端 `http://localhost:8080`
- 前端框架：Vue 3 + Naive UI + Vue Router + Pinia
- 登录后按角色展示菜单（RBAC 动态路由，见 `doc/RBAC.md`）

生产构建：`npm run build`（产物输出到 `admin/dist`）。

---

## 点餐小程序（miniapp/）

基于 uni-app（Vue 3）的多端小程序，源码位于 `miniapp/src`。

```powershell
cd miniapp
npm install
npm run dev:mp-weixin     # 微信开发者工具
npm run dev:h5            # H5
npm run build:mp-weixin   # 构建发布
```

- 平台：微信 / 支付宝 / H5 / 百度 / 京东 / 快手 等，脚本见 `package.json`
- API 地址配置：`miniapp/src/config/env.js`（开发者工具默认 `127.0.0.1:8080`，真机联调改局域网地址或用 storage key 覆盖）
- 说明：小程序端当前默认关闭 WebSocket（后端是 STOMP 端点，未适配小程序原生 ws 协议），后厨大屏推送在管理后台使用

---

## 功能模块（server/src/main/java/com/smartordering）

后端按 `modules/<模块>/` 划分，每个模块含 `controller / service / mapper / entity / dto / vo`：

| 模块 | 说明 |
|---|---|
| `system` | 认证（登录/注册）、用户、角色、RBAC 权限、操作日志、登录日志 |
| `dish` | 菜品分类、菜品、规格组/规格项（含加价选项） |
| `order` | 下单、订单、订单明细、订单操作日志 |
| `cart` | 购物车 |
| `payment` | 支付记录、现金结算 |
| `coupon` | 优惠券模板、用户券、批量发放任务 |
| `member` | 会员等级、积分/成长值、会员中心 |
| `banner` | 首页轮播 |
| `table` | 桌台区域、桌台、桌台二维码（ZXing 生成） |
| `kitchen` | 后厨大屏任务（WebSocket 实时推送） |
| `review` | 订单评价 / 菜品评价 |
| `feedback` | 顾客反馈 |
| `mq` | RabbitMQ 可靠消息（消息表 + 重发） |
| `report` | 经营报表（营收、销量排行、翻台率） |

公共能力：`common/`（统一返回体、异常处理、分页）与 `framework/`（Sa-Token 拦截、CORS、MinIO、Redis、WebSocket）。

---

## 关键设计

- **统一返回体**：`ApiResponse<T>` / `PageResult<T>`
- **逻辑删除**：所有实体继承 `BaseEntity`，`deleted` 字段 @TableLogic 软删除
- **认证鉴权**：Sa-Token + Redis，token 存 30 天，权限走 `用户→角色→菜单` RBAC（详见 `doc/RBAC.md`）
- **数据库初始化**：仿 Spring Boot `schema.sql` / `data.sql`，启动即建表 + 种子数据，无需手动执行 SQL
- **代码规范**：所有 Java/XML/YAML 均写注释；DTO 收、VO 出，Entity 不直接暴露（隔离 password 等敏感字段）

---

## 文档

- [doc/RBAC.md](doc/RBAC.md) — RBAC 权限模型、Sa-Token 认证流程、BCrypt 加密原理、DTO/VO 调用链