# RBAC 权限模型与登录认证原理

> smart_ordering_v2 项目的权限架构与认证链路速查。
> 技术栈：Spring Boot 3.2.5 + MyBatis-Plus 3.5.5 + Sa-Token 1.37 + Redis

---

## 1. RBAC 概念

RBAC（Role-Based Access Control，基于角色的访问控制）核心思想：

```
用户（User） ──多对多──> 角色（Role） ──多对多──> 权限/菜单（Menu/Permission）
```

- 不直接给用户分配权限，而是给用户分配「角色」，给「角色」分配「权限」
- 用户通过「拥有某个角色」间接获得该角色的所有权限

---

## 2. 数据模型（表结构）

| 表名 | 作用 | 关键字段 |
|---|---|---|
| `sys_user` | 用户表 | id, username, password(BCrypt), nickname, status, user_type |
| `sys_role` | 角色表 | id, name, code(如 admin/WAITER), status |
| `sys_user_role` | 用户-角色关联 | user_id, role_id（复合主键） |
| `sys_role_menu` | 角色-菜单关联 | role_id, menu_id（复合主键） |
| `sys_menu` | 菜单/权限表 | ⏳ 尚未创建 |

```
sys_user ──< sys_user_role >── sys_role ──< sys_role_menu >── sys_menu
```

种子数据：admin 用户(id=1) → 关联 admin 角色(id=1) → 关联 107 条菜单权限。密码均为 BCrypt 加密的 `123456`。

---

## 3. 实体与通用字段（BaseEntity）

所有实体继承 `BaseEntity`，自动获得公共字段：

| 字段 | 注解 | 说明 |
|---|---|---|
| `id` | `@TableId(type = IdType.ASSIGN_ID)` | 雪花算法 ID |
| `createBy` / `updateBy` | `@TableField(fill = INSERT / INSERT_UPDATE)` | 自动填充操作人 |
| `createTime` / `updateTime` | `@TableField(fill = ...)` | 自动填充时间 |
| `deleted` | `@TableLogic` | 逻辑删除（0=正常 1=已删） |

> 逻辑删除：`delete` 实际是 `UPDATE deleted=1`，查询自动拼 `AND deleted=0`，不物理删除。

---

## 4. 登录认证流程（Sa-Token）

### 4.1 登录

```
POST /api/auth/login
  → AuthController.login(dto)
  → SysUserServiceImpl.login(dto)
      ├─ 按 username 查用户（逻辑删除自动过滤）
      ├─ status == 0 → 抛「账号禁用」
      ├─ BCrypt.checkpw(明文, 库中密文) → 不匹配抛「密码错误」
      ├─ StpUtil.login(user.getId())   ← 生成 token 存 Redis
      └─ 返回 LoginVO{ token, tokenName="Authorization", userInfo }
```

### 4.2 token 存储与传递

- UUID 格式 token 存 Redis，有效期 30 天（`timeout: 2592000` 秒）
- 通过请求头 `Authorization` 传递（`sa-token.token-name: Authorization`）

### 4.3 退出

```
POST /api/auth/logout → StpUtil.logout() → 删除 Redis 中的 token
```

---

## 5. 权限加载与校验流程（StpInterface）

### 5.1 请求拦截（SaTokenConfig）

```
请求 → SaInterceptor
  ├─ 命中白名单(/auth/**, /health/**, /doc.html, /swagger-ui/** 等) → 放行
  └─ 否则 StpUtil.checkLogin()：从 Authorization 头取 token → Redis 校验
        └─ token 无效/过期 → 抛 NotLoginException
```

### 5.2 权限接口（StpInterfaceImpl）

Sa-Token 需要角色/权限时，回调 `StpInterface` 两个方法：

```
getRoleList(loginId)       → getUserRoles(userId)
                             → sys_user_role JOIN sys_role → ["admin"]

getPermissionList(loginId) → getUserPermissions(userId)
                             → sys_user_role JOIN sys_role_menu → ["1","2",...]
```

> 业务代码用 `StpUtil.hasRole()` / `hasPermission()` 判断时触发上述方法。

---

## 6. 缓存设计（Redis）

| 缓存名 | key | 内容 | TTL |
|---|---|---|---|
| `userRoles` | `#userId` | 用户角色编码列表 | 30 分钟 |
| `userPermissions` | `#userId` | 用户权限列表 | 30 分钟 |

```java
@Cacheable(value = "userRoles", key = "#userId")
public List<String> getUserRoles(Long userId) { ... }
```

- `@Cacheable` 只声明「要不要缓存」，TTL 由 `RedisCacheManager.entryTtl` 统一决定（`RedisConfig`）
- 业务代码与缓存后端解耦，换后端只改 `CacheManager` 配置

> ⚠️ 区分两个 TTL：`sa-token.timeout`（登录态 30 天，过期需重新登录）vs `entryTtl`（权限缓存 30 分钟，过期只重新查库）。

---

## 7. 关键代码位置索引

| 组件 | 路径 |
|---|---|
| 统一返回体 | `common/result/ApiResponse.java` |
| 业务异常 / 全局异常 | `common/exception/BusinessException.java` / `GlobalExceptionHandler.java` |
| 公共实体父类 | `modules/system/entity/BaseEntity.java` |
| 登录/注册控制器 | `modules/system/controller/AuthController.java` |
| 登录业务逻辑 | `modules/system/service/impl/SysUserServiceImpl.java` |
| 权限缓存 | `modules/system/service/impl/PermissionCacheServiceImpl.java` |
| Sa-Token 权限实现 | `framework/satoken/StpInterfaceImpl.java` |
| 登录拦截器 | `framework/config/SaTokenConfig.java` |
| Redis 缓存配置 | `framework/config/RedisConfig.java` |
| 角色/权限 SQL | `resources/mapper/system/SysUserMapper.xml` |

---

## 8. 当前状态与待办

**✅ 已完成**：登录/注册/退出/用户信息；用户→角色→权限四级联查；Sa-Token 存 Redis；BCrypt 加密；权限缓存。

**⏳ 待办**：
- `sys_menu` 表未建 —— 当前 `selectPermissionsByUserId` 是临时方案（返回 menu_id 数字），建表后改回关联 `m.perms`
- 菜单/用户/角色管理 CRUD
- 细粒度权限校验（`@SaCheckPermission`）

---

## 9. BCrypt 加密原理

### 9.1 为什么不用 MD5

| 算法 | 特点 | 问题 |
|---|---|---|
| MD5 | 快、固定 32 位 | 可暴力破解、无盐可反查、有碰撞 |
| BCrypt | 慢、自带盐、成本可调 | 专为密码存储设计 |

密码存储需要「慢」——慢到暴力破解不划算。

### 9.2 密文格式

```
$2a$10$D273KbUAFVgQ3IUx4LD8m.r9bowenLvwNgKXc4.e2MZxzoTK48T4W
│  │  │        └── 31 字符：哈希结果（Checksum）
│  │  └─────────── 22 字符：盐（Salt）
│  └────────────── 10 = 成本因子（2¹⁰=1024 次迭代）
└──────────────── 2a = 算法版本号
```

### 9.3 三大安全机制

1. **加盐（Salt）**：每次随机生成 16 字节盐（Base64 成 22 字符），存进密文。相同密码因盐不同而密文不同 → 废掉彩虹表
2. **慢哈希（Cost Factor）**：`10` = 1024 次 Blowfish 密钥扩展迭代，每 +1 计算量翻倍。单次约 100ms，暴力破解被指数级放大
3. **单向性**：只能「明文→密文」，无法反推

### 9.4 校验原理（不「解密」，而是「重算比对」）

```
1. 输入明文密码
2. 从密文提取 salt（$2a$10$ 后 22 字符）
3. 用相同 salt + cost 重新做 BCrypt 哈希
4. 结果与库中哈希逐字符比较
```

校验函数叫 `checkpw` 而非 `decrypt` —— 它从不解密，只「用同样的盐重算并比对」。

### 9.5 本项目代码

```java
// 注册
user.setPassword(BCrypt.hashpw(dto.getPassword()));
// 登录
if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
    throw new BusinessException("Invalid password");
}
```

> 用 Hutool `cn.hutool.crypto.digest.BCrypt`，与 Spring Security `BCryptPasswordEncoder` 同算法，`$2a$10$...` 格式兼容。

---

## 10. 登录请求完整调用链（DTO / VO / 注解）

### 10.1 三种对象

| 对象 | 方向 | 职责 | 例子 |
|---|---|---|---|
| **DTO** | 前端 → 后端 | 接收请求体 + 参数校验 | `LoginDTO` |
| **Entity** | 后端 ↔ 数据库 | 映射表，含敏感字段 | `SysUser`（含 password） |
| **VO** | 后端 → 前端 | 组装响应，只给该给字段 | `LoginVO` / `UserInfoVO` |

> 不直接用 Entity 收发，因为 Entity 含 `password` 等敏感字段。用 DTO 收、VO 出，隔离敏感数据。

### 10.2 两个注解

```java
public ApiResponse<LoginVO> login(@Valid @RequestBody LoginDTO dto)
```

- **`@RequestBody`**：把请求体 JSON 反序列化成 `LoginDTO`
- **`@Valid`**：触发 DTO 里的校验注解（如 `@NotBlank`），失败抛 `MethodArgumentNotValidException` → 全局异常返回 400

> 只写 `@RequestBody` 不写 `@Valid` = 不校验。要校验必须两个都写。

常用校验注解：`@NotBlank`、`@NotNull`、`@NotEmpty`、`@Size`、`@Min`/`@Max`、`@Email`。

### 10.3 调用链时序

```
POST /auth/login

① Spring 自动：JSON → @RequestBody 反序列化 → LoginDTO
② Spring 自动：@Valid 校验（失败 → 400）
③ Controller：AuthController.login(dto) → userService.login(dto)
④ Service：查库得 SysUser → BCrypt 校验 → StpUtil.login 发 token
            → new LoginVO + buildUserInfo（查角色/权限，走 Redis 缓存）
⑤ Controller：return ApiResponse.ok(vo)
⑥ Spring 自动：VO 序列化成 JSON 返回（不含 password）
```

| 对象 | 诞生 | 消耗 |
|---|---|---|
| DTO | 阶段①（Spring 反序列化） | Controller 参数 → Service |
| Entity | 阶段④（Service 查库） | 业务内部 |
| VO | 阶段④（Service 组装） | Service return → 序列化返回 |

---

## 11. 一句话总结

> 用户登录 → Sa-Token 发 token 存 Redis → 每次请求带 `Authorization` 头 → 拦截器校验登录态 → 需要权限时回调 `StpInterface` 联查「用户→角色→菜单」→ 完成鉴权。
