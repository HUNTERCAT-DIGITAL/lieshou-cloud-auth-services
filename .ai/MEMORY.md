# 项目记忆 MEMORY

> 长期事实 + 当前阶段 + 待办。**每次会话开始先读**；重大变更后更新。

## 项目身份

- 名称：lieshou-cloud-auth-services（聚合器 artifactId `lieshoucloud-auth-services-aggregator`）
- 类型：L2 底座服务仓（鉴权 / JWT 签发与刷新）· 薄壳，业务核心在上游 `lieshou-framework/framework-auth`
- 仓库：`ssh://git@gitlab.ts.huntercat.cn:22220/huntercat-digital/lieshou-cloud-auth-services.git`
- 默认分支：`main`（受保护，`push_access_level=0` → 一律特性分支 + MR + squash）
- 许可证：Apache-2.0（`LICENSE`，逐字复制上游）
- 技术栈：Java 21 · Spring Boot 3.5.7 · Spring Cloud 2023.0.5 · Spring Cloud Alibaba 2023.0.1.0 ·
  jjwt 0.12.6 · SpringDoc 2.8.17 · Resilience4j（HTTP 层）· Maven 聚合构建

## 架构速览

```
pom.xml（聚合器：modules = framework + services/auth）
├── framework/  → submodule lieshou-framework（pin 见 `git ls-tree HEAD framework`）
│                 framework-parent 提供 BOM / pluginManagement / spotless / jacoco / distributionManagement
└── services/auth/
    ├── web/AuthController.java        10 个端点（/api/auth/**）+ 异常映射
    ├── config/SecurityConfig.java     stateless + permitAll（鉴权在 gateway）
    ├── config/OpenApiConfig.java      bearerAuth scheme
    ├── feign/UserAuthClient.java      @FeignClient(name="user-service") + UserAuthErrorDecoder
    └── resources/application.yml      JWT / 租户 / 限流 / Nacos
```

- 依赖方向：本仓 → `framework-auth`/`framework-common`（**不反向**）。跨服务只调 `user-service`（Feign，lb）。
- **无数据库**：无 Flyway / JPA / datasource；用户与密码哈希在 `user-service`。

## 关键约定

- 版本：本模块版本继承 `framework-parent`（当前 **0.3.1**；Nexus 首发版本 0.3.1）。
  > 例外：文档/制品类的补丁发布可显式在 `services/auth/pom.xml` 声明 `<version>`（如 0.3.2）。
- 分支：特性分支 `ci/*` `docs/*` `feat/*` `fix/*` → MR（squash）→ `main`；tag 打在合并后的 `main` tip。
- 发布：`.gitlab-ci.yml` 的 `deploy` stage 仅 `v*` tag 触发；tag 必须 == `project.version`，否则拒发。
- Spotless（google-java-format + importOrder：`java.*` 最后）由 `framework-parent` 绑定到 `verify`。
- 异常契约：业务否定抛 `BaseException`（透传错误码）；依赖故障 → `503 SERVICE_UNAVAILABLE`。

## 当前阶段

- 2026-09-14：CI（GitLab，verify）+ 制品发布（Nexus `maven-releases`）已上线并跑绿（`cn.huntercat:lieshoucloud-auth:0.3.1`）。
- 2026-09-14：外围文件补齐（LICENSE / CHANGELOG / AGENTS / `.ai/` / `docs/adr/`）。

## 待办

- [ ] CD：镜像构建推 Harbor（`services/auth/Dockerfile`）+ 部署 job —— 未做
- [ ] 决定是否发 `-sources.jar` / `-javadoc.jar`（当前只有 fat jar + pom）
- [ ] 内网 CA 进 job 镜像，去掉 `GIT_SSL_NO_VERIFY` 妥协（跨仓基础设施项）
- [ ] runner `concurrent` 串行限制（跨仓基础设施项）
- [ ] `docker pull` 基础镜像是否统一走 Harbor proxy-cache（基础设施项）

## 关键决策

- 2026-09-14：制品只发本仓模块（`-pl services/auth`），`framework-*` 归上游仓发布 —— 避免同坐标重复发撞 Nexus 重发策略。
- 2026-09-14：`distributionManagement` 由 `framework-parent` 继承，本仓不重复声明。
- 2026-09-14：pin `framework` 从 v0.3.0 前移到 v0.3.1（0.3.1 起 `framework-parent` 自带 Nexus 分发地址）。
- 2026-08：鉴权业务核心上收 `lieshou-framework/framework-auth`（本仓只留薄壳 + Feign 端口）。
- 2026-09：注册验证码必填收紧（`auth.register.allow-open-register` 默认 `false`）。

## 环境变量 / 常用命令

| 用途 | 命令 |
| --- | --- |
| 构建 + 门 | `JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64 mvn -B -ntp verify` |
| 打包 | `mvn -B -ntp -DskipTests package` → `services/auth/target/lieshoucloud-auth.jar` |
| 格式化 | `mvn -B -ntp spotless:apply` |
| 运行 | `java -jar services/auth/target/lieshoucloud-auth.jar`（`AUTH_PORT`，默认 8083） |

关键环境变量：`JWT_SECRET`（≥32 字节，HS256）· `JWT_ACCESS_TTL` / `JWT_REFRESH_TTL` · `JWT_ISSUER` ·
`AUTH_DEFAULT_TENANT_CODE` · `AUTH_ALLOW_OPEN_REGISTER` · `NACOS_SERVER_ADDR` / `NACOS_CONFIG_ENABLED` ·
CI：`NEXUS_DEPLOY_USER` / `NEXUS_DEPLOY_PASS`（masked + protected）
