# 测试规约 · TESTING（本仓增量）

> **基线（通用部分）**：`huntercat-digital/huntercat-ai-kit` → `templates/project/base/TESTING.md`
> （测试金字塔 / 命名 / 必做禁令 / CI 集成 / 测试数据）。本文件只写**本仓的增量与现状**。
> 配套：[`CONVENTIONS.md`](./CONVENTIONS.md) · [`SECURITY.md`](./SECURITY.md)。

---

## 1. 本仓现状（实测）

| 项 | 值 |
| --- | --- |
| 测试文件 | 3 个：`AuthApplicationTests`（上下文加载）· `JwtServiceTest`（4）· `AuthRateLimitTest`（2） |
| 用例数 | **7**（`mvn -B -ntp verify` → `Tests run: 7, Failures: 0, Errors: 0`） |
| 覆盖率 | JaCoCo 已接入（`verify` 出报告 `services/auth/target/site/jacoco/index.html`）；起步期门槛 40%，**不阻断** |
| 数据库 | **无**。本仓无 datasource / Flyway / JPA → **不用 Testcontainers，也没有 PG 测试** |
| 上游依赖 | `framework-auth` 在 reactor 内同源构建，其单测随 `verify` 一起跑（135 个量级） |

## 2. 必做（本仓）

- ✅ 新增公开方法（Controller 端点 / Feign 实现）→ 至少一个正例 + 一个负例
- ✅ Controller 用 `@WebMvcTest` 或 `MockMvc`（`AuthRateLimitTest` 即此模式）
- ✅ 限流行为改动 → 补 429 负例（`RequestNotPermitted`）
- ✅ 错误码改动 → 断言 `error` 字段（`USER_NOT_FOUND` / `INVALID_CREDENTIALS` / `RATE_LIMITED` …）
- ✅ 跨服务依赖（`UserAuthPort`）在测试里 **mock**，不打真实 `user-service`

## 3. 禁止（本仓）

- ❌ 引入 Testcontainers / 真库（本服务无库；需要真库的是 `user-service`）
- ❌ 用 `Thread.sleep` 等限流窗口（用 `@MockBean` / 可注入时钟代替）
- ❌ 在 CI 里 `-DskipTests` 换取绿
- ❌ 硬编码本机端口 / 真实 `JWT_SECRET`

## 4. 命令

```bash
mvn -B -ntp verify                       # 门：单测 + jacoco + spotless（CI 用的就是这条）
mvn -B -ntp test                         # 只跑测试
mvn -B -ntp test -Dtest=JwtServiceTest   # 单个测试类
mvn -B -ntp verify -DexcludedGroups=pg   # 本仓无需（无 pg 分组）；保留写法以备后续引入
```

> CI：`.gitlab-ci.yml` 的 `verify` job（runner `java, wqc`，镜像 `maven:3.9-eclipse-temurin-21`）。
