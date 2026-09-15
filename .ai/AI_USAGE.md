# AI 开发使用规范 · AI_USAGE（本仓增量）

> **基线（通用部分）**：`huntercat-digital/huntercat-ai-kit` → `templates/project/base/AI_USAGE.md`
> （适用场景 / 模型选择 / 启动协议 / 会话纪律）。本文件只写**本仓的增量**。

---

## 1. 本仓启动读序列

```bash
cat .ai/MEMORY.md         # 身份 / 架构 / 阶段 / 待办
cat AGENTS.md            # 协作边界 + 启动读序列
cat .ai/CONVENTIONS.md    # 代码规约（含“上游 framework 边界”）
cat .ai/SECURITY.md       # JWT / 限流 / 白名单 / 客户内容禁令
cat .ai/TESTING.md        # 无 DB 事实 + 7 个既有用例
cat README.md CHANGELOG.md
```

## 2. 本仓特有纪律

| 纪律 | 原因 |
| --- | --- |
| 改 `framework-*` 源码前先停手 | 上游在 `lieshou-framework` 仓（本仓无 submodule）；改上游 → 发版 → 本仓升版本号 |
| 改 JWT / 白名单 / 限流 / Feign 契约 → 先写 `docs/adr/` | 影响 gateway + 全部 4 端 |
| 打 tag / 触发 Nexus 发布 → 必须先问人 | 发布点只增不改；Nexus maven-releases 不容重复发 |
| 声称"绿了/发布了"必须贴证据 | `mvn verify` 输出、pipeline URL、Nexus 资产 URL 三选一以上 |
| 不要把客户名 / 客户域名写进本仓 | 上游仓客户内容禁令（pre-commit + CI 双重守卫） |

## 3. 归档

重要会话结论 → 更新 `.ai/MEMORY.md`（当前阶段 / 待办 / 关键决策）；架构或契约决策 → `docs/adr/NNNN-*.md`。
本仓不建 `.ai/conversations/`（薄壳仓无内容，历史对话由维护者本机保存）。
