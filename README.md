# lieshou-cloud-auth-services · 鉴权服务

> L2 底座服务仓。自 `lieshou-cloud-core/services/auth` 拆分（2026-08 细粒度）。
> 鉴权业务核心（登录/验证码/注册/重置/切换租户）在 **framework-auth**（上游 `lieshou-framework`，从内网 Nexus 按坐标解析）；
> 本仓无 submodule，只保留 Spring Boot 薄壳（Controller + 配置 + Feign 端口实现）。
> 服务定位：签发/刷新 JWT，密码校验经 Feign 委托 `user-service`；网关校验 token 后透传 `X-User-*` 给下游。

## 构建

```bash
# 聚合构建（上游 framework 从内网 Nexus 解析，本仓无 submodule）
mvn -B -ntp verify                      # 门：编译 + 单测 + jacoco + spotless:check
mvn -B -ntp -DskipTests package         # 仅打包 → services/auth/target/lieshoucloud-auth.jar
mvn -B -ntp spotless:apply              # 本地格式化
```

前置：JDK 21（`JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64`）、Maven 3.8+，且 `~/.m2/settings.xml` 已 mirror 到内网 Nexus `maven-public`（`framework-*` 只在私服）。

## 运行

```bash
java -jar services/auth/target/lieshoucloud-auth.jar     # 默认 8083；经 gateway 走 9000
```

关键环境变量：`JWT_SECRET`（≥32 字节，HS256）· `JWT_ACCESS_TTL` / `JWT_REFRESH_TTL` · `JWT_ISSUER` ·
`AUTH_DEFAULT_TENANT_CODE` · `AUTH_ALLOW_OPEN_REGISTER` · `NACOS_SERVER_ADDR` / `NACOS_CONFIG_ENABLED`。
**本服务无自有数据库**（用户与密码哈希在 `user-service`）。

## 对外接口

| 端点 | 说明 |
| --- | --- |
| `POST /api/auth/login` | 用户名/密码登录 → access + refresh（限流 `authLogin`） |
| `POST /api/auth/switch-tenant` | 用 refresh token 切换租户 |
| `POST /api/auth/send-code` · `/login/code` · `/register` · `/reset-password` | 验证码通道（限流 `authSendCode` / `authCodeFlow`） |
| `POST /api/auth/activate` | 管理员建号后首次登录设密码 |
| `POST /api/auth/refresh` · `/logout` · `GET /me` | 刷新 / 登出（客户端清 token）/ 当前用户 |

契约见运行时 SpringDoc：`/v3/api-docs`、`/swagger-ui.html`（经 gateway 时带 bearerAuth）。

## 制品

- Maven：`cn.huntercat:lieshoucloud-auth` → 内网 Nexus `maven-releases`
  （依赖：JDK/Spring 版本、parent `framework-parent` 由上游发布到内网 Nexus `maven-public`，版本线 0.3.3，见 `.ai/MEMORY.md`）
- 镜像：待接 Harbor（CD 未做，见 `.ai/MEMORY.md` 待办）

## 文档导航

| 文件 | 用途 |
| --- | --- |
| `.ai/MEMORY.md` | 长期事实 / 当前阶段 / 待办（**会话先读**） |
| `.ai/AGENTS.md` | AI 协作边界与启动读序列 |
| `.ai/CONVENTIONS.md` · `.ai/SECURITY.md` · `.ai/TESTING.md` | 代码 / 安全 / 测试规约（增量，基线在 ai-kit） |
| `docs/adr/` | 本仓决策（历史 ADR 快照见 `.ai/decisions/`，出处 `lieshou-cloud-pro`） |
| `CHANGELOG.md` | 版本变更 |
