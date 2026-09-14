# 本仓 ADR（架构决策记录）

> 本仓**自己的**决策记录。**编号接 `lieshou-framework` 序列**（framework 已到 0044，本仓从 **0045** 起，不可重号）。
> 历史单体仓 `lieshou-cloud-pro` 的 43 份 ADR 以**快照**形式留在 [`.ai/decisions/`](../../.ai/decisions/README.md)（只读，每份标注出处）；
> 两边互为交叉索引：**本目录 = 现在与将来的决策，`.ai/decisions/` = 历史**。

## 什么时候写

改以下任一项 → 必须新增一份：

- JWT 算法 / TTL / claims 结构；`SecurityConfig` 白名单与鉴权位置
- 限流策略与阈值
- 跨服务契约（`UserAuthClient` / `UserAuthPort` 实现 / 错误码）
- 制品与发布方式（Nexus 坐标、tag 规则、镜像仓库）
- 版本策略 / parent 关系 / 依赖来源（reactor vs 私服）

## 怎么用

```bash
cp docs/adr/0000-template.md docs/adr/0045-<slug>.md   # 从模板起
```

- 文件名：`NNNN-kebab-case.md`（四位编号从 0045 起递增）
- 状态：`提议` → `采纳` / `否决` / `被 NNNN 取代`
- 已采纳的 ADR **只增不改**：要改就写新的一份并标 `取代 NNNN`
- 每份 ADR 末尾一句 TL;DR，方便不读全文的人

## 索引

| 编号 | 标题 | 状态 | 日期 |
| --- | --- | --- | --- |
| — | （本目录暂无；下一份编号 **0045**） | — | — |
