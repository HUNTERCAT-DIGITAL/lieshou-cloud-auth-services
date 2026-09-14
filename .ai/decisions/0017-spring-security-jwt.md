> 快照自 `lieshou-cloud-pro/.ai/decisions/0017-spring-security-jwt.md`（历史单体仓，快照日期 2026-09-14）。
> 本仓只引用不改写；完整 43 份待归档抢救，见 [README](./README.md)。

---

# ADR-0017 · Spring Security + JWT 闭环鉴权

| 项目 | 值 |
| --- | --- |
| 编号 | 0017 |
| 标题 | 新增 auth-service + gateway AuthenticationFilter + SpringDoc bearerAuth scheme |
| 状态 | **采纳**（Phase 5 · 鉴权闭环） |
| 调研日期 | 2026-08-22 |

## 决策

承接 ADR-0016（OpenAPI），落地 JWT 闭环鉴权，让前端 4 端能用 Bearer token 调受保护 API.

### 第 1 层 · 选型

| 维度 | 选型 | 备选 |
| --- | --- | --- |
| Security | ** Spring Security 6.x** | Keycloak (overkill) / 自写 (anti-pattern) |
| JWT 库 | ** jjwt 0.12.6** | nimbus-jose-jwt / java-jwt |
| 算法 | ** HS256**（对称） | RS256（公私钥） |
| Token 类型 | **Access Token (30min) + Refresh Token (7d)** | 单 token / OAuth2 full |
| 密码哈希 | ** BCrypt**（Spring Security 默认） | Argon2 / PBKDF2 |
| 鉴权位置 | **gateway 全局过滤器** | 每个 service 各自验 |
| 白名单 | /api/auth/** / v3/api-docs/** / swagger-ui/** / actuator/health / /api/users/auth/**（service-to-service） | — |

### 第 2 层 · 关键决策

| # | 决策 | 理由 |
| --- | --- | --- |
| 1 | **HS256 对称签名** | gateway + auth 共享 secret；简单 |
| 2 | **JWT_SECRET 必须 ≥ 32 字节**（HS256 要求） | fail-fast 启动检查 |
| 3 | **新 service auth-service:8083** | 鉴权解耦；Feign 调 user-service 验密码 |
| 4 | **gateway GlobalFilter 验 JWT** | 单一入口；其他 service 不感知鉴权 |
| 5 | **透传 X-User-* headers** | 下游 service 直接读，零鉴权负担 |
| 6 | **白名单 /api/users/auth/**（service-to-service） | auth-service 调 user-service 不需要带 user JWT |
| 7 | **user entity 加 passwordHash 字段** | BCrypt；service-to-service 用 UserAuthView 返回 |
| 8 | **SpringDoc bearerAuth scheme** | OpenAPI spec 文档化鉴权要求 |
| 9 | **CORS 加 X-User-* headers** | 前端跨域读 X-User-Id（如有） |
| 10 | **JWT_SECRET 由 .env 注入** | `.env.example` 占位；不入库 |
| 11 | **Phase 1 不接 Redis 黑名单** | logout 仅客户端清 cookie；Phase 2+ 加 |
| 12 | **测试覆盖 JwtService + Application 加载** | 起步期 jaCoCo 不阻断；Phase 2+ 加 MockMvc |

### 第 3 层 · 架构

```
                ┌──────────────────┐
   client ───→ │ gateway:9000    │
                │ (AuthenticationFilter: 验 JWT)
                │ (透传 X-User-Id/Name/Roles)
                └──────────────────┘
                         ↓
        ┌────────────────┼────────────────┐
        ↓                ↓                ↓
┌─────────────┐  ┌─────────────┐  ┌─────────────┐
│ auth:8083   │  │ user:8081   │  │ admin:8082  │
│ (JWT 签发) │  │ (User CRUD)  │  │ (Feign)     │
│ /api/auth/**│  │ /api/users/**│  │ /api/admin/**│
│             │  │ /api/users/  │  │             │
│             │  │  auth/**（S2S）│  │             │
└─────────────┘  └─────────────┘  └─────────────┘
```

### 第 4 层 · 文件清单

```
新增 (16 文件) ──────────────────────────────────────
services/auth/
├── pom.xml
├── Dockerfile
├── .dockerignore
├── src/main/resources/application.yml
├── src/main/resources/application-dev.yml
├── src/main/java/.../AuthApplication.java
├── src/main/java/.../config/SecurityConfig.java
├── src/main/java/.../config/OpenApiConfig.java
├── src/main/java/.../service/JwtService.java
├── src/main/java/.../service/AuthService.java
├── src/main/java/.../web/AuthController.java
├── src/main/java/.../web/dto/AuthDtos.java
├── src/main/java/.../feign/UserAuthClient.java
├── src/main/java/.../feign/dto/UserAuthView.java
├── src/test/java/.../AuthApplicationTests.java
└── src/test/java/.../service/JwtServiceTest.java

services/gateway/
├── src/main/java/.../security/JwtService.java (gateway)
└── src/main/java/.../security/AuthenticationFilter.java

修改 (8 文件) ──────────────────────────────────────
services/pom.xml                      ← auth module + jjwt.version
services/user/pom.xml                 ← spring-security-crypto
services/user/.../domain/User.java    ← passwordHash + @Schema
services/user/.../web/UserController.java ← authByUsername + CreateUserRequest DTO
services/user/.../web/dto/UserAuthView.java (新增内嵌 DTO)
services/gateway/pom.xml             ← jjwt 依赖
services/gateway/.../config/CorsConfig.java ← allowed-headers 加 X-User-*
services/gateway/src/main/resources/application.yml  ← JWT 配置 + auth route
docker-compose.yml                    ← auth-service + JWT env
.env.example                          ← JWT_SECRET 等
```

### 第 5 层 · 端到端流程

#### 1. 登录
```http
POST /api/auth/login
Content-Type: application/json

{ "username": "futurewl", "password": "secret123" }

→ 200 OK
{ "accessToken": "eyJ...", "refreshToken": "eyJ...", "expiresIn": 1800,
  "tokenType": "Bearer", "userId": 1, "username": "futurewl" }
```

#### 2. 访问受保护资源
```http
GET /api/users/1
Authorization: Bearer eyJ...

→ 200 OK
{ "id": 1, "username": "futurewl", "displayName": "Future Wang" }
```

#### 3. 刷新
```http
POST /api/auth/refresh
{ "refreshToken": "eyJ..." }

→ 200 OK
{ "accessToken": "eyJ...", "refreshToken": "eyJ...", "expiresIn": 1800, ... }
```

#### 4. 当前用户
```http
GET /api/auth/me
Authorization: Bearer eyJ...

→ 200 OK
{ "userId": 1, "username": "futurewl", "roles": ["USER"] }
```

## 设计原则

1. **单一入口鉴权** —— gateway 统一验 JWT，其他 service 不重复
2. **零信任透传** —— 下游信 gateway 注入的 X-User-* headers
3. **service-to-service 白名单** —— Feign 调用走专门路径，不带用户 JWT
4. **密码哈希隔离** —— User entity 仅存 hash；DTO 控制暴露
5. **SpringDoc bearerAuth** —— 前端 codegen 自动生成带 token 中间件
6. **JWT_SECRET 环境注入** —— 不入库；生产用 Secret Manager

## 与已有治理的关系

- 承接 ADR-0016（SpringDoc OpenAPI）—— bearerAuth scheme 在 OpenApiConfig 显式声明
- 承接 ADR-0003（Security Framework）—— JWT 实现其 §5 鉴权章节
- user-service 加 passwordHash 后，DATA_SECURITY.md 第 6 节"凭据存储"已合规

## 反对意见与回应

| 反对 | 回应 |
| --- | --- |
| "用 Keycloak / OAuth2 server" | Phase 1 overkill；自管 JWT 够用 |
| "RS256 更安全" | RS256 适合多服务信任链；本项目 HS256 已够（gateway + auth 共签） |
| "用 Spring Security OAuth2 Resource Server" | ResourceServer 需 JWKS endpoint；自写 JwtService 更可控 |
| "每 service 各自验 JWT" | 重复代码；gateway 单一入口更高效 |
| "Phase 1 接 Redis 黑名单" | 起步期不需要；Phase 2+ 加 |

## 实施

- 本 ADR 阶段（24 文件）：
  - 新增 16 文件（auth-service 全套 + gateway 2 个 security）
  - 修改 8 文件（pom / docker / .env / application.yml / CorsConfig / User entity / UserController / UserAuthView）

## 留给下次 AI / 维护者的提示

- **JWT_SECRET 必须 ≥ 32 字节** —— 启动检查 fail-fast
- **secret 与 issuer 必须 gateway 和 auth 完全一致** —— 不一致 → token 跨服务失败
- **每次新增受保护 service 端点** —— 默认在 gateway 鉴权白名单之外；要走鉴权
- **service-to-service 调用** —— 用专门路径（如 `/api/users/auth/**`）并加入白名单
- **CORS allowed-headers 加 X-User-*** —— 前端需要读这些 header
- **生产部署** —— JWT_SECRET 用 k8s Secret / Vault，绝不入库
- **Phase 2+ 路线** —— 服务端 refresh 黑名单（Redis） + logout 真失效 + 多设备会话管理

## 关联文档

- `.ai/decisions/0016-springdoc-openapi.md`（前置 OpenAPI）
- `.ai/SECURITY.md` §5 鉴权章节（更新）
- `PROJECT_MANAGEMENT.md` Sprint 流程
- `.ai/AI_USAGE.md` AI 工具使用
- ADR-0001 ~ 0016 历史决策