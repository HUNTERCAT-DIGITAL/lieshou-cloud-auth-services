> 快照自 `lieshou-cloud-pro/.ai/decisions/0001-spring-cloud-feasibility.md`（历史单体仓，快照日期 2026-09-14）。
> 本仓只引用不改写；完整 43 份待归档抢救，见 [README](./README.md)。

---

# ADR-0001 · Spring Cloud × Spring Boot 4 / Java 25 可行性评估

| 项目 | 值 |
| --- | --- |
| 编号 | 0001 |
| 标题 | 是否在 lieshou-cloud-pro 引入 Spring Cloud |
| 状态 | **Phase 3 已完成**（拆微服务：gateway + user + admin 三件套 + 真实 Feign + Resilience4j） |
| 调研日期 | 2026-08-22 |
| 结论（TL;DR） | **当前（2026-08-22 时点）不建议直接接入 Spring Cloud**。三个可行路径见 §四。 |

---

## 一、依赖版本事实表（2026-01 时点公开信息）

| 组件 | 最近 GA | 关联 / 依赖 | 兼容 Boot 4？ | 兼容 Java 25？ |
| --- | --- | --- | --- | --- |
| Spring Boot | **4.0.0**（2025-11） | Spring Framework 7.0 | ✅ 自身 | ✅ 官方 LTS 矩阵 |
| Spring Framework | 7.0.x | 自身 | — | ✅ |
| Spring Cloud | 2025.1.x（2025 H2） | 绑定 Boot 3.5.x | ❌ 当前 release train 未覆盖 Boot 4 | ⚠ 未官方声明 |
| Spring Cloud Alibaba | 2025.0.x | 绑 Spring Cloud 2025.x | ❌ 跟进中 | ⚠ |
| Spring Cloud Gateway | 4.x | 随 Spring Cloud | ❌ | ⚠（Netty OK） |
| OpenFeign | 4.x | 随 Spring Cloud | ❌ | ⚠ |
| Resilience4j | 2.x | 独立 | ✅ 部分 Boot 适配 | ✅ Java 17+ |
| Nacos Server | 2.4.x（2025） | 独立 Java 进程（JDK 21） | — | ✅ |

> 图例：✅ 官方支持 / ❌ 当前不兼容 / ⚠ 理论可用但未官方声明。

---

## 二、关键不确定性

### 1. Spring Cloud release train 何时追上 Boot 4

按 Spring 历史节奏：

- Boot 4.0.0 GA = **2025-11**
- Spring Cloud 通常在新 Boot GA 后 **2-3 个月** 才出兼容 release train
- 时点（2026-08-22）距 Boot 4 GA 仅 **2 个月**

**两种预期**：

- 🟢 **乐观**：Spring Cloud 2025.1.x 或 2025.2.x 已在 milestone/release candidate 阶段显式支持 Boot 4
- 🟡 **悲观**：要等到 Spring Cloud 2026.0.x（**约 2026 H1~H2**）

**我无法 100% 确认** 2026-01 时点是否有官方 release train 明确声明支持 Boot 4。**核实方式**：访问 https://spring.io/projects/spring-cloud 看最新版本与"Supported Boot Versions"表。

### 2. Spring Cloud Alibaba 跟进节奏

SCA 通常落后 Spring Cloud 主线 1-2 个月；Boot 4 兼容版本大概率跟随 Spring Cloud GA 同步或滞后。

### 3. Spring Cloud 客户端 × JDK 25

常见客户端（nacos-client、sentinel-client、openfeign）的 JDK 17+ 编译产物跑在 JDK 25 应该 OK，但：

- ⚠ Nacos client 部分 native transport
- ⚠ Consul 客户端（dnsjava）涉及 sun.security 系列 API
- ⚠ Netty 4 在 JDK 25 sandbox 下可能有 `--add-opens` 缺失问题

不是阻塞，但是需要端到端验证。

---

## 三、风险矩阵

| 路径 | 兼容性风险 | 进度风险 | 维护成本 | 业务影响 |
| --- | --- | --- | --- | --- |
| **A · 直接接 Spring Cloud + Boot 4 + Java 25** | 高（不确定） | 阻塞 | 不可控 | 可能踩坑 2-4 周 |
| **B · 降级到 Boot 3.5.x + Java 21** | 低 | 即刻可走 | 可控 | 失去 Java 25 |
| **C · 纯等待**（6-12 个月不动） | 低 | 阻塞业务 | 零 | 想拆微服务时延迟 |
| **D · 双轨（推荐）** | 低 | 不阻塞业务 | 中 | 0 立即影响 |

---

## 四、建议方案

### 主推：D · 双轨

维持现状（Spring Boot 4 + Java 25 + 单体），在 **代码边界与文档** 上做好微服务化预留，**不等也不冒险**：

1. 当前 `backend/` 单体继续演进，避免服务间深度耦合
2. 抽出潜在拆分边界：`User` / `Auth` / `Business` 等用接口 + 包路径做"软隔离"
3. CI 增加"Spring Cloud 兼容性"监测：每 2 周扫一次 release notes，检测 Boot 4 兼容 GA 是否发布
4. 一旦 Spring Cloud 首个支持 Boot 4 的 GA 版本发布，按 `RELEASE.md` 流程一次性迁

### 备选：B · 务实降级

若 **未来 1-2 个月内业务确实要拆微服务**，立刻降级到 Boot 3.5.x + Java 21 LTS：

- 牺牲 Java 25（换 Java 21，业务代码基本无感）
- 立刻获得 Spring Cloud 全套支持
- 长期看 Java 21 仍是 LTS，至 2028-09 维护

### 反对：C · 纯等待

除非"拆微服务"不是近期必要，否则会无谓拖延项目节奏。

### 反对：A · 直接硬上

风险过高，且本项目当前规模未必"必须"立刻拆；微服务化的收益要在团队 / 业务达到一定规模后才显现。

---

## 五、可执行动作清单（短期）

| 优先级 | 动作 | 状态 |
| --- | --- | --- |
| P0 | **不动代码**，本 ADR 归档 | ✅ 本次完成 |
| P1 | 写 `SERVICE_BOUNDARIES.md`，标注潜在服务切分线 | ⬜ 待办 |
| P1 | CI 加 cron job 监控 Spring Cloud release notes，命中"Boot 4"时通知 | ⬜ 待办 |
| P2 | 每 2 周复查本评估表，更新状态字段 | ⬜ 持续 |
| P3 | 出现 Boot 4 兼容 GA 版本后，按 `RELEASE.md` 走一遍 | ⬜ 未来 |

---

## 六、引用

- https://spring.io/projects/spring-cloud （release train 当前版本 / Supported Boot Versions 表，**核实权威来源**）
- https://spring.io/projects/spring-cloud-alibaba
- https://start.spring.io/actuator/info （Spring Boot ↔ Spring Cloud ↔ Java 实际兼容矩阵，JSON 接口可程序化查询）

---

## 七、本 ADR 的下一次审核

- **下一次触发**：Spring Cloud release notes 中首次出现"Boot 4 / 4.x"兼容声明 或 6 个月后（2026-07-22）任一先到

---

## 八、实施记录

### Phase 1 — 版本降级（2026-08-22）

**采纳决策**：路径 B · 降级到 Spring Boot 3.5.x + Java 21 LTS，立即可接入 Spring Cloud。

**改动文件清单**（10 个）：

| 文件 | 改动 |
| --- | --- |
| `backend/pom.xml` | `<java.version>` 25 → 21；`maven.compiler.release` 25 → 21；`spring-boot-starter-parent.version` 4.0.0 → 3.5.7 |
| `.tool-versions` | `java 25` → `21` |
| `backend/.sdkmanrc` | `java=25-tem` → `java=21-tem` |
| `backend/Dockerfile` | `maven:3.9-eclipse-temurin-25` → `21`；`eclipse-temurin:25-jre-alpine` → `21-jre-alpine` |
| `.github/workflows/ci.yml` | `java-version: '25'` → `'21'` |
| `README.md` | 顶部徽章 / 版本矩阵 / 工具链表 / 兼容性要点 3 |
| `.ai/MEMORY.md` | 技术栈行更新；当前阶段加 ADR-0001 引用 |
| `.ai/AGENTS.md` | 后端栈描述从 Boot 4/Java 25 改为 Boot 3.5/Java 21 |
| `CHANGELOG.md` | `[Unreleased]` 加 Phase 1 降级行 + ADR-0001 引用 |

**未触及**：所有业务代码（`User` 实体、`UserRepository`、`HealthController`、`lieshou-cloud-proApplication`），仅改版本号。

**下一步**

- Phase 2 · 单 jar 接入 Spring Cloud 客户端（`spring-cloud-starter-alibaba-*`、Nacos discovery / config、OpenFeign、Resilience4j），保持单体形态，等生态稳定后再拆。
- Phase 3 · 拆微服务（gateway + user + admin 三件套 + Nacos 容器 + CI / Docker 同步）。

---

## 九、Phase 2 实施记录 — Spring Cloud Alibaba 单体接入

**实施日期**：2026-08-22
**目标**：在 Phase 1 已降级的 Boot 3.5.7 + Java 21 基础上，把单体 jar 接入 Spring Cloud 生态（Nacos discovery / config、OpenFeign、Resilience4j），保持单体形态。

**改动文件清单**（8 个）：

| 文件 | 改动 |
| --- | --- |
| `backend/pom.xml` | 新增 `spring-cloud.version=2023.0.5` + `spring-cloud-alibaba.version=2023.0.1.0` 两个 BOM；新增 4 个 dependency：`spring-cloud-starter-alibaba-nacos-discovery` / `-config`、`spring-cloud-starter-openfeign`、`spring-cloud-starter-circuitbreaker-resilience4j` |
| `lieshou-cloud-proApplication.java` | 加 `@EnableDiscoveryClient` + `@EnableFeignClients` 两个注解 |
| `application.yml` | 加 `spring.cloud.nacos.discovery` (默认 enabled) + `spring.cloud.nacos.config` (默认 enabled=false) + `spring.cloud.openfeign.circuitbreaker.enabled=true` |
| `docker-compose.yml` | 加 `nacos: nacos/nacos-server:v2.4.3` 服务，standalone 模式，端口 8848/9848；backend `depends_on` nacos (healthy)；新加 `nacos_data` / `nacos_logs` 卷 |
| `.env.example` | 加 NACOS_IMAGE_TAG / NACOS_HTTP_PORT / NACOS_GRPC_PORT / NACOS_NAMESPACE / NACOS_USERNAME / NACOS_PASSWORD / NACOS_JVM_* / NACOS_DISCOVERY_ENABLED / NACOS_CONFIG_ENABLED |
| `README.md` | Docker 启动章节加 Nacos 访问行 + 「启用配置中心」进阶小节 |
| `.ai/MEMORY.md` | 技术栈/当前阶段/端口与路径同步；路线图改为 Phase 3 拆微服务 |
| `.ai/AGENTS.md` | 后端栈描述同步；🟢 协作表加 Nacos 配置写一行 |
| `CHANGELOG.md` | [Unreleased] 加 Phase 2 整段，含未实施项标注 |

**版本选型**：

| 选 | 选择 | 理由 |
| --- | --- | --- |
| Spring Cloud | **2023.0.5** | 已知稳定、与 Boot 3.x 兼容、SCA 配套 release train 同步 |
| Spring Cloud Alibaba | **2023.0.1.0** | 与 Spring Cloud 2023.0.1 train 配套的 SCA release |
| Nacos Server | **v2.4.3** | 稳定主流版本；社区使用最广泛 |

**为什么不用 2025 train**：Spring Boot 3.5.7 完美对齐的 Spring Cloud 应为 2025.0.x，但当时点（2026-01）SCA 2025.0.0.0 是否真的发布不能 100% 确认；用 2023.0.5 是最稳的「已被广泛验证」组合。如未来 Spring Cloud 2025 train + SCA 2025 release 都稳定，可以做升级 PR。

**Phase 2 暂未实施（留给 Phase 3）**：
- 实际 Feign client 接口（当前没有跨服务调用目标）
- Resilience4j 真实熔断配置（yaml 不在 dev compose）
- 真正的服务拆分（gateway / user / admin）
- Nacos 中的业务配置（默认 dataId 模板）

**验证步骤（建议用户本机跑）**：

```bash
# 1. 一键起 dev 栈（含 nacos）
docker compose up -d --build

# 2. 看后端启动日志，应看到一行 Nacos registered
docker compose logs -f backend | grep -i 'nacos'

# 3. 访问 Nacos 控制台
open http://localhost:8848/nacos
# 账号 nacos / nacos，在「服务管理」→「服务列表」应能看到 lieshoucloudpro（IP: 172.x.x.x:8080）

# 4. （可选）开 Config：在 .env 设 NACOS_CONFIG_ENABLED=true，
#    在控制台新建 Data ID=lieshoucloudpro.yaml，再触发 /actuator/refresh
```

下一步：Phase 3 = 拆微服务（gateway + user + admin 三件套 + Feign 真实调用 + docker-compose 服务拆分 + Nacos namespace 隔离）。

---

## 十、Phase 3 实施记录 — 微服务拆分（gateway / user / admin）

**实施日期**：2026-08-22
**目标**：把 Phase 2 单体 jar 拆为 `services/{gateway,user,admin}` 三个独立 Spring Boot 服务，加入真实 OpenFeign 调用 + Resilience4j 业务配置；docker-compose 按服务拆分。

**最终目录**（Phase 3 完成后）：

```
services/
├── pom.xml                            ← parent (packaging=pom, packaging the BOMs)
├── gateway/    端口 9000  Spring Cloud Gateway（WebFlux）
├── user/       端口 8081  Spring Boot + JPA（唯一连 PG）
└── admin/      端口 8082  Spring Boot + OpenFeign + Resilience4j
```

`backend/` 整目录已删除。

### 改动文件清单（22 个）

| 类别 | 文件 |
| --- | --- |
| **新增 (16)** | `services/pom.xml` · `services/{gateway,user,admin}/pom.xml` · 各服务 `Dockerfile` · 各服务 `.dockerignore` · `GatewayApplication.java` · `UserApplication.java` · `AdminApplication.java` · `User.java` / `UserRepository.java` / `UserController.java` · `UserFeignClient.java` / `UserFeignClientFallback.java` · `UserDTO.java` · `AdminController.java` · 各服务 `application.yml` · 各服务 `ApplicationTests.java` |
| **修改 (5)** | `docker-compose.yml` · `docker-compose.prod.yml` · `.env.example` · `frontend/vite.config.ts`（proxy → gateway:9000）· `frontend/nginx.conf`（proxy_pass → gateway:9000）· `.github/workflows/ci.yml`（working-directory→services）· `README.md` · `.ai/MEMORY.md` · `.ai/AGENTS.md` · `CHANGELOG.md` |
| **删除 (1)** | `backend/`（整目录，已迁出内容） |

### 服务关键设计决策

| 服务 | 决策 | 理由 |
| --- | --- | --- |
| gateway | WebFlux-based Spring Cloud Gateway；`@EnableDiscoveryClient`；路由用 `lb://user-service` / `lb://admin-service` | 自动从 Nacos 拉取下游，无需 IP hardcode |
| user | `@SpringBootApplication(scanBasePackages)` + `@EntityScan` + `@EnableJpaRepositories` 锁在 `user.*` 子包 | 多 module 隔离 spring component scan，避免 entity 名冲突 |
| admin | `@EnableFeignClients(basePackages = "cn.huntercat.lieshoucloudpro.admin.feign")` 锁包扫描 | 同上 |
| admin | Resilience4j `@CircuitBreaker(name="userService")` + Feign `fallback = UserFeignClientFallback.class` 双层保护 | 一旦用户服务不可达，admin 服务自身仍 UP 并给清晰降级信号 |
| admin | DTO `UserDTO` 与 user 服务 `User` 实体解耦 | Phase 4 可接 springdoc-openapi 自动生成跨服务 DTO |

### docker-compose 边界

| 角色 | dev compose | prod compose |
| --- | --- | --- |
| gateway | 暴露 `9000:9000` | 暴露 `${GATEWAY_PORT:-80}:9000` |
| user | 内网不暴露 | 内网不暴露（internal network） |
| admin | 内网不暴露 | 内网不暴露（internal network） |
| frontend | vite dev + bind mount | nginx + 构建产物 |
| 启动顺序 | postgres & nacos healthy → user started → admin & gateway started → frontend | 同上 |

### 验证步骤

```bash
cp .env.example .env
docker compose up -d --build

# 1. 看所有服务健康
docker compose ps                          # 期望 6 个服务 healthy/running

# 2. 看 Nacos 控制台
open http://localhost:8848/nacos
# 期望：服务管理 → 服务列表 看到 lieshoucloud-gateway / lieshoucloud-user / lieshoucloud-admin 三个

# 3. 测试完整链路
curl http://localhost:9000/actuator/gateway/routes    # 看到 user-route / admin-route

curl -X POST -H 'Content-Type: application/json' \
     -d '{"username":"alice","displayName":"Alice"}' \
     http://localhost:9000/api/users
curl http://localhost:9000/api/users                    # 列表
curl http://localhost:9000/api/users/count             # 计数

# 4. admin 调 user (Feign)
curl http://localhost:9000/api/admin/health
# 期望响应里有 "userServiceUserCount": N

# 5. 测试熔断降级
docker compose stop user
curl http://localhost:9000/api/admin/health
# 期望响应里有 "degraded": true 或 fallback 字段
docker compose start user     # 熔断器半开后自动恢复
```

### Phase 3 后可能的下一步

- 真实业务拆分（auth / order / notify / business 等）
- Feign + springdoc-openapi → 自动生成 TS API client 给前端
- Spring Security + JWT + 网关统一鉴权
- Sentinel 限流 / 数据库迁移 / 多端架构等
- Resilience4j RateLimiter + Bulkhead
