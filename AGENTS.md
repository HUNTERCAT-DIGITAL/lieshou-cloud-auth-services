# AGENTS.md — 给 AI 协作者的指引

> 给"未来在这个仓库工作的 AI agent"读的指引。新会话先 `cat` 本文件 + `.ai/MEMORY.md`。

## 我是谁

你是 **lieshou-cloud-auth-services**（L2 底座 · 鉴权服务）的 AI 协作伙伴。
技术栈：Java 21 · Spring Boot 3.5.7 · Spring Cloud 2023.0.5 · Spring Cloud Alibaba 2023.0.1.0 · Maven（聚合构建，
parent = `framework-parent`，从内网 Nexus `maven-public` 按坐标解析，**本仓无 submodule**）· JUnit 5 + JaCoCo + Spotless。

## 怎么读这个仓库（启动顺序）

```bash
cat .ai/MEMORY.md        # 长期事实 + 当前阶段 + 待办（先读这个）
cat AGENTS.md            # 本文件（仓库根）
cat .ai/CONVENTIONS.md   # 代码规约（增量，基线见文首指针）
cat .ai/SECURITY.md      # 安全规约（JWT / 限流 / 密钥）
cat .ai/TESTING.md       # 测试规约 + 无 DB 事实
cat README.md            # 构建/运行/端点/制品
cat CHANGELOG.md         # 版本变更
# 决策：本仓 docs/adr/（新决策）· .ai/decisions/（历史快照，只读）
```

## 架构速览

```
pom.xml（聚合器：仅 services/auth；上游 framework 不随仓）
└── services/auth/        Spring Boot 薄壳：AuthController + SecurityConfig + OpenApiConfig
                          + feign/UserAuthClient（→ user-service 校验密码）
     parent = cn.huntercat:framework-parent（Nexus maven-public 解析，提供 BOM/spotless/jacoco/
     distributionManagement）；framework-auth / framework-common 是鉴权业务核心，同样走 Nexus
```

- **业务核心不在本仓**：登录/验证码/注册/重置/切换租户的编排在 `framework-auth` 的 `AuthService`
  （端口化：单体本地实现 / 微服务 Feign 实现）。本仓的实现是 `feign/UserAuthPort`。
- **鉴权位置**：gateway 校验 JWT → 透传 `X-User-*`；本服务 `permitAll`，`/me` 由 Controller 自行解析 Bearer。
- **无自有数据库**：本服务不持有表、无 Flyway/JPA；密码哈希与用户数据都在 `user-service` 侧。

## 协作边界（关键）

| 等级 | 操作 | 是否允许直接做 |
| --- | --- | --- |
| 🟢 | 读文件、grep、跑 `mvn verify` / 单测 / `spotless:check` | ✅ 直接 |
| 🟢 | 加测试、改 bug、补文档 | ✅ 直接 |
| 🟡 | 加依赖、改 `services/auth/pom.xml`、改 `application.yml` 配置结构 | ⚠️ 先告知影响 |
| 🟡 | 改 Feign 契约（`UserAuthClient` / `UserAuthPort` 实现）、错误码 | ⚠️ 先告知影响（跨服务契约） |
| 🟡 | 上游版本号前移（`framework-parent` / `framework-*` 的 `<version>`） | ⚠️ 先告知影响（改的是上游版本） |
| 🔴 | 改 `framework-*` 源码 | ❌ 绝不 —— 上游在 `lieshou-framework` 仓，改那里 → 发版 → 本仓升版本号 |
| 🔴 | 改 CI（`.gitlab-ci.yml`）/ 打 tag / 触发 Nexus 发布 | ❌ 先问（发布点只增不改） |
| 🔴 | 直推 `main`、`git push -f`、提交 secret | ❌ **绝不**（`main` 已保护，走特性分支 + MR） |

## 风格偏好

- **Commit**：Conventional Commits（`feat:` / `fix:` / `chore:` / `ci:` / `docs:` / `refactor:`）
- **后端**：Java 21 语法（record / switch 表达式 / 文本块）；构造器注入；公开类与公开方法写 Javadoc（中文）；
  google-java-format（GOOGLE 风格）；importOrder 里 **`java.*` 排在最后且与前组空一行**
- **回答风格**：先说**做什么**，再做；给命令前先解释影响；不堆大段解释不给操作
- **声称完成前必须有实测输出**（`mvn -B -ntp verify` / pipeline 链接 / Nexus 资产 URL）
