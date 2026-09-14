# 代码规约 · CONVENTIONS（本仓增量）

> **基线（通用部分）**：`huntercat-digital/huntercat-ai-kit` → `templates/project/base/CONVENTIONS.md`
> （命名 / 长度阈值 / 坏味道 12 条 / 工具链）。本文件只写**本仓的增量与差异**，冲突时以本文件为准。
> 配套：[`SECURITY.md`](./SECURITY.md) · [`TESTING.md`](./TESTING.md)。

---

## 1. 包与命名（本仓既有事实）

| 项 | 值 |
| --- | --- |
| 包根 | `cn.huntercat.lieshoucloudpro.auth`（**服务仓统一 `lieshoucloudpro`**，framework 侧是 `cn.huntercat.lieshou.framework.*`） |
| 分层 | `web/`（Controller）· `config/`（Security / OpenApi）· `feign/`（跨服务端口实现）；**本仓没有 service/ 层** —— 业务编排在 `framework-auth` |
| DTO | `web/dto/` 下 `record`（`LoginRequest` / `TokenResponse` / …），Controller 不复用 entity（本仓无 entity） |
| 异常映射 | Controller 内 `@ExceptionHandler`（`UsernameNotFoundException`→404，`BadCredentialsException`→401，`RequestNotPermitted`→429） |

## 2. 上游边界（本仓最重要的一条）

- **不直接改 `framework/`**（submodule）。要在 `framework-auth` 里加/改逻辑 → 改上游 `lieshou-framework` 仓 → 发版 → 本仓 bump pin。
- 依赖只允许单向：本仓 → `framework-*`。反过来（framework 依赖服务仓）永远不允许。
- 需要"上游没有的"业务能力时：先看是否属于**通用**能力（→ 上游），还是**本服务专属**（→ 本仓 `web/`/`config/` 下新建类）。

## 3. Spotless / 格式（CI 门，必须过）

由 `framework-parent` 绑定在 `verify` 阶段：`spotless:check`。

- google-java-format **GOOGLE** 风格（2 空格缩进、100 列）
- `importOrder`：`org.springframework, org.springframework.cloud, com.alibaba.cloud, jakarta, org, com, lombok, #`
  → **`java.*` 未列名，必须排在最后且与前一组空一行**（历史 CI 从未跑过时踩过的坑）
- `removeUnusedImports` 开启

```bash
mvn -B -ntp spotless:apply     # 本地修
mvn -B -ntp spotless:check     # 只检查
```

## 4. 跨服务调用（Feign）

| 规则 | 实现 |
| --- | --- |
| 客户端必带 fallback / 错误解码 | `feign/UserAuthClient` + `feign/UserAuthErrorDecoder` |
| 超时必须显式 | `application.yml` → `spring.cloud.openfeign.client.config.default.{connectTimeout:2000, readTimeout:5000}` |
| 业务否定 vs 依赖故障**不许混** | 业务否定 → `BaseException`（透传 errorCode）；依赖故障 → `503 SERVICE_UNAVAILABLE` 且带 `cause` + `warn` 日志 |
| 服务间认证 | 走 gateway 透传头（`X-User-Id` / `X-User-Name` / `X-User-Roles`），本服务不再二次校验 |

## 5. 提交与合并

1. 特性分支（`feat/*` `fix/*` `ci/*` `docs/*`）→ MR → squash merge（`main` 受保护，无人可直推）
2. MR 前本地自检：`mvn -B -ntp verify`（编译 + 单测 + jacoco + spotless）
3. Commit：Conventional Commits；正文写**动机 / 变更 / 影响面**
4. 涉及行为变更 → 同步 `CHANGELOG.md`；涉及架构/契约决策 → `docs/adr/NNNN-*.md`
5. **发布**：合并后打 `vX.Y.Z` tag（tag 必须 == `project.version`）→ CI `deploy` stage 发 Nexus；**不发 SNAPSHOT**

## 6. 本仓禁止

- ❌ 直接改 submodule 内容、直接推 `main`
- ❌ 在 `application.yml` 里写真实 `JWT_SECRET` / 密码 / token（一律 `${ENV:default}` 形式）
- ❌ 关闭 `spotless` / 跳过测试换取"绿"（`-DskipTests` 只允许本地打包预览，不允许出现在 CI）
- ❌ 在日志里打 token / 密码 / 验证码
