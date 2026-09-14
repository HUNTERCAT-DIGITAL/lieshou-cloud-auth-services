# `.ai/` — AI 协作目录

存放与 AI 编程协作相关的内容。**由人类维护者与 AI 共同维护**。项目根 `README.md` 给人看，本目录给 AI 看。

## 子目录约定

| 路径 | 用途 | 更新频率 |
| --- | --- | --- |
| `MEMORY.md` | 长期记忆：身份 / 架构 / 约定 / 当前阶段 / 待办 | 重大变更后 |
| `AGENTS.md` | 给 AI agent 的协作指引 + 启动读序列 | 风格/边界变更时 |
| `CONVENTIONS.md` · `SECURITY.md` · `TESTING.md` · `AI_USAGE.md` | 规约（**增量**：基线在 ai-kit，见各文件文首指针） | 规约变更时 |
| `decisions/` | **历史 ADR 快照**（出处 `lieshou-cloud-pro/.ai/decisions/`），只读引用 | 仅在需要补快照时 |
| `conversations/` · `prompts/` | 本仓不设（薄壳仓无内容；历史对话在维护者本机） | — |

> 本仓**自己的**新决策写 `docs/adr/`（编号接 framework 序列）；`decisions/` 只放历史快照，每份顶部标注出处。

## AI 助手怎么用

```bash
cat .ai/MEMORY.md        # 项目状态 + 关键决策 + 待办
cat .ai/AGENTS.md        # 协作边界 + 风格偏好 + 启动读序列
ls  .ai/decisions/       # 需要理解"当年为什么这么选"时展开
ls  docs/adr/            # 本仓决策
```

## ❌ 不要写在这里

- API key / 数据库密码 / OAuth secret（走环境变量 / CI 变量）
- 个人身份信息（PII）
- 客户专属内容（客户名 / 客户域名 / 客户定制值 —— 上游仓禁止，见 `.ai/SECURITY.md § 客户专属内容`）
