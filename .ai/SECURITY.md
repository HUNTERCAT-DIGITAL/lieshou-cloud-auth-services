# 安全规约 · SECURITY（本仓增量）

> **基线（通用部分）**：`huntercat-digital/huntercat-ai-kit` → `templates/project/base/SECURITY.md`
> （网络/传输、输入校验、输出转义、HTTP 头、数据保护、依赖安全、密钥管理、容器、DevSecOps、合规）。
> 本文件只写**本仓（鉴权服务）的增量**。配套：[`CONVENTIONS.md`](./CONVENTIONS.md) · [`TESTING.md`](./TESTING.md)。

---

## 1. 本仓在鉴权链路中的位置

```
前端 → gateway（全局过滤器验 JWT，白名单放行）→ 透传 X-User-* → 本服务（permitAll，仅 /me 自解析 Bearer）→ user-service（Feign，校验密码）
```

- 本服务**不重复做授权**（网关已做）；`SecurityConfig` 刻意 `permitAll` + stateless，**不要**把这里改成"再验一遍"。
- `SecurityConfig` 白名单：`/api/auth/**`、`/v3/api-docs/**`、`/swagger-ui/**`、`/actuator/health`。

## 2. JWT（本仓签发）

| 规则 | 现状 |
| --- | --- |
| 算法 | HS256（对称；gateway 与本服务共享 secret） |
| `JWT_SECRET` | **≥ 32 字节**（HS256 硬要求），由环境注入；`application.yml` 只有占位默认值 |
| Access / Refresh TTL | 1800s / 604800s（`JWT_ACCESS_TTL` / `JWT_REFRESH_TTL`） |
| 签发内容 | `uid` / `tid` / `tcode` / `username` / roles；**不放**敏感字段 |
| 登出 | 当前为 no-op（客户端清 token）；服务端黑名单（Redis）未做 → 见 `docs/adr/` |
| ❌ | 不把 token 放 URL / query；不把 token、密码、验证码写进日志 |

## 3. 密码与验证码

- 密码哈希：BCrypt（`BCryptPasswordEncoder`，本仓只做校验委托，落库在 `user-service`）；策略 ≥8 位且含字母+数字（上游 `framework-auth` 强制）。
- 验证码：一次性 + 频道（SMS/EMAIL）+ 用途（LOGIN/REGISTER/RESET_PASSWORD）；DB 层 60s 频率限制 + 应用层限流双重。
- 注册：`auth.register.allow-open-register` 默认 **false** → 无验证码注册被拒（`REGISTER_CODE_REQUIRED`）。

## 4. 限流（公开端点防爆破 / 防轰炸）

`resilience4j.ratelimiter`，超限立即拒绝（`timeoutDuration: 0s`）→ `429 RATE_LIMITED`：

| 实例 | 作用端点 | 限值 |
| --- | --- | --- |
| `authLogin` | `POST /api/auth/login` | 10 次/分钟（含失败尝试） |
| `authSendCode` | `POST /api/auth/send-code` | 5 次/分钟 |
| `authCodeFlow` | `/login/code` · `/register` · `/reset-password` | 见 `application.yml` |

> 计数是**单实例内存态**；分布式限流（Redis / 网关 `RequestRateLimiter`）未做。

## 5. CORS / CSRF

- **本仓不配 CORS**：浏览器跨域请求只到 gateway，白名单在 gateway（本仓保持同编号占位，避免下游文档引用错位）。
- CSRF：无 cookie 会话（stateless + Bearer），故 `csrf.disable()`；若未来引入 cookie，必须重新评估。

## 6. 密钥与配置

| 项 | 规则 |
| --- | --- |
| `JWT_SECRET` | 环境变量注入；`application.yml` 默认值仅为本地占位（`please-change-me...`），**生产必须覆盖** |
| Nacos 账号 | `NACOS_USERNAME` / `NACOS_PASSWORD` 环境注入 |
| CI 发布凭证 | GitLab 项目 CI 变量 `NEXUS_DEPLOY_USER` / `NEXUS_DEPLOY_PASS`（masked + protected） |
| ❌ | 任何 secret 不入库、不进镜像 layer、不写进 `.ai/` 文档 |

## 7. 客户专属内容禁入上游仓

上游仓（`lieshou-*` / `open/*` / `huntercat-*`）禁止：客户身份串（客户名 / 域名 / 品牌色）、客户端专有代码、
客户业务定制数据。正确落点：客户包 `packages/<client>/`、部署侧 `deploy/<client>-flyway/seed/`。
规则唯一来源：`huntercat-ci/guard/client-identifiers.json`；commit 有本地 pre-commit 守卫（fail-open）。

## 8. 审核触发点（本仓专属）

改以下任一项 → 必须写 `docs/adr/` 并在 MR 描述里说明影响：

- JWT 算法 / TTL / claims 结构（影响全部 4 端与 gateway）
- `SecurityConfig` 白名单 / stateless 策略
- 限流阈值（防爆破底线）
- `UserAuthClient` 契约与错误码（跨服务）
