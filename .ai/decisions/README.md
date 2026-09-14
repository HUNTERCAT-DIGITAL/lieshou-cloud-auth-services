# `.ai/decisions/` — 历史 ADR 快照（只读）

> **这些不是本仓的决策**，而是历史单体仓 `lieshou-cloud-pro/.ai/decisions/`（43 份，0001–0043）中被本仓**代码实际引用到**的少数几份**快照**。
> 每份文件顶部标注出处与快照日期；**完整 43 份待归档抢救**（该仓属归档候选）。
> 本仓**自己的**新决策写 [`../../docs/adr/`](../../docs/adr/README.md)（编号接 framework 序列 0045+）。

## 为什么只搬这几份

代码里 `@see .ai/decisions/...` 与 `application.yml` 注释引用的路径必须真实存在（悬空引用 = 文档债）。
本仓当前只引用：

| 快照 | 被谁引用 |
| --- | --- |
| `0001-spring-cloud-feasibility.md` | `services/auth/src/main/resources/application.yml`（锁定 Boot 3.5.7 + Spring Cloud 2023.0.5 的兼容性检查器关闭理由） |
| `0017-spring-security-jwt.md` | `AuthApplication.java`、`web/AuthController.java`（JWT 闭环鉴权选型：HS256 / 双 token / 网关验签 + 透传） |

## 相关但未搬（如需阅读请求补快照）

`0023-auth-verification-code.md`（验证码通道）· `0028-rate-limiter.md`（限流）· `0029-nacos-config-profiles-pipeline.md`（Nacos 配置中心）。
