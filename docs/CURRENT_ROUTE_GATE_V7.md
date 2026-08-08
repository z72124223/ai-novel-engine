# Current Route Gate V7 — Engine-Enforced Rules

Status: `ACTIVE_SINGLE_CURRENT_ENTRY_GATE`

Supersedes current execution routing in V6. V6 and DIALOGUE-01 remain historical evidence.

## 1. Current Work

```text
ENGINE-RULES-01 / DIALOGUE-02
```

目标：

> 将生产、对话、评审与选定规则真正写入 Kotlin／SQLite 引擎，使坏句可以生成，但不能成为选定版。

## 2. Required Read Order

1. `PROJECT_STATE.md`
2. `.codex/engine_rules01_controller.md`
3. `docs/CURRENT_ROUTE_GATE_V7.md`
4. `docs/ENGINE_ENFORCED_PRODUCTION_RULES_V1.md`
5. `docs/work-packages/ENGINE-RULES-01_DIALOGUE-02.md`
6. `docs/handoffs/ENGINE_RULES01_CODEX_HANDOFF.md`
7. DIALOGUE-01 final artifacts and report
8. existing Runtime/Repository/Migration/Test code

## 3. Branch

```text
codex/engine-rules01-dialogue02
```

## 4. Completion Is Code, Not Documentation

不接受：

- 只新增 Markdown；
- 只修改 Prompt；
- 只加入 reviewer 建议；
- 只手动修正「好。先碰你。」；
- 只加正则；
- 只写测试却不接 Selection Gate。

必须有：

- Kotlin models/services/repository/CLI；
- additive SQLite migration；
- structured review intake；
- final selection hard gate；
- negative and bypass tests；
- DIALOGUE-01 re-evaluation evidence。

## 5. True Stop Conditions

- formal Canon decision；
- paid Provider/API key；
- unavoidable destructive Git/data action；
- weakening Transaction/Hash/Knowledge/Approval；
- schema requires destructive migration；
- same core blocker survives one Medium and one High attempt。
