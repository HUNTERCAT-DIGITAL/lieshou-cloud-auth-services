# Changelog

本仓（`lieshoucloud-auth`）的版本变更。版本号 = `services/auth` 的 Maven 版本（显式声明，与上游
`framework-parent` 同线）；制品发到内网 Nexus `maven-releases`（见 `.ai/MEMORY.md`）。
格式参考 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)。

## 0.3.3（2026-09-15）

上游依赖改走内网 Nexus，**移除 `framework` submodule**（无业务代码行为变更）。

### 变更

- 删除 `framework` submodule（`.gitmodules` / gitlink）：`framework-parent` / `framework-auth` / `framework-common`
  一律从内网 Nexus `maven-public` 按坐标解析
- `services/auth` parent `framework-parent` 0.3.1 → `0.3.3`，并删除 `relativePath`（不再指向本地 submodule）
- CI（`.gitlab-ci.yml`）：删掉 `git submodule update` / `GIT_SSL_NO_VERIFY` / 子仓 URL 重写；改为
  `before_script` 统一导入内网 CA + 生成 `-s /tmp/ci-settings.xml`（mirror → Nexus）
- `services/auth/Dockerfile`：构建阶段改名 `builder`；删除对 submodule 的依赖（不再 `-am`）；
  容器内 `keytool` 导入内网 CA；改 `-s services/.m2-settings.xml`（BuildKit cache mount 会遮住
  `/root/.m2/settings.xml`，COPY 进去反而失效）
- `services/.m2-settings.xml` 瘦身（只留 Nexus mirror）；新增 `services/.ci/caddy-root.crt`（内网 CA）

### 移除

- `CHANGELOG` 中“bump framework submodule pin”类操作 —— 今后只升版本号

## 0.3.2（2026-09-14）

文档补齐（本仓首次把外围文件配齐；无代码行为变更）。

### 新增

- `LICENSE`（Apache-2.0，逐字复制上游 `lieshou-framework`）
- `AGENTS.md` · `CHANGELOG.md`
- `.ai/`：`README.md` · `MEMORY.md` · `CONVENTIONS.md` · `SECURITY.md` · `TESTING.md` · `AI_USAGE.md`
- `.ai/decisions/`：历史 ADR 快照 `0001`（Spring Cloud 可行性）· `0017`（Spring Security + JWT），
  出处 `lieshou-cloud-pro/.ai/decisions/`（代码里的 `@see .ai/decisions/...` 引用从此不再悬空）
- `docs/adr/`：本仓决策入口（`README.md`）+ 模板（编号接 framework 序列 0045+）

### 变更

- `README.md` 纠偏：删除已不存在的"组合 services/common → lieshou-cloud-common +
  services/jwt-support → lieshou-cloud-jwt-support"描述（两仓 2026-08-27 已归档，能力并入 `lieshou-framework`）；
  补齐真实构建/运行/端点/制品/文档导航
- `services/auth/pom.xml` 显式声明 `<version>0.3.2</version>`（此前继承 `framework-parent` 的 0.3.1）
- `.gitignore` 忽略 `.codegraph/`

## 0.3.1（2026-09-14）

制品首次入内网 Nexus。

### 新增

- 制品发布：`cn.huntercat:lieshoucloud-auth:0.3.1` → Nexus `maven-releases`（CI `deploy` stage，仅 `v*` tag 触发；
  校验 tag == `project.version`；`-pl services/auth` 只发本仓模块 —— `framework-*` 归上游仓发布）

### 变更

- `framework` submodule `v0.3.0 (739b696)` → `v0.3.1 (d023687)`：`framework-parent` 0.3.1 起自带
  `distributionManagement`，本仓不再重复声明分发地址
- `services/auth` parent `framework-parent` 0.3.0 → 0.3.1

## 0.3.0（2026-09-14）

### 变更

- 切换到统一 parent `cn.huntercat:framework-parent:0.3.0`：BOM / `pluginManagement` / spotless / jacoco
  全部上收，删除本仓 274 行的 `services/pom.xml`
- 聚合器 `<modules>`：`services` → `services/auth`
- `framework` submodule pin → `739b696 (v0.3.0)`

## 0.2.0（2026-09-14）

### 变更

- `framework` 升级 0.2.0（含全仓 spotless 格式对齐 `f2b53f0`）
- 本仓源码 spotless 格式对齐；CI 由 GitHub Actions 迁到 GitLab（`.gitlab-ci.yml`，runner `java, wqc`）

### 移除

- `.github/workflows/ci.yml`（GitHub Actions 在内网 GitLab 上从不触发，且引用已删的 submodule 结构）

## 0.1.0（2026-08-26）

- 自 `lieshou-cloud-core/services/auth` 拆分建仓（组合 common / jwt-support 库）：JWT 签发 + 刷新 +
  `/me`，Feign 调 `user-service` 校验密码，SpringDoc bearerAuth 契约
