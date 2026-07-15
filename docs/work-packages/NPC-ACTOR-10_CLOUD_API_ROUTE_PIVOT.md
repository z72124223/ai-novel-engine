# NPC-ACTOR-10 — Cloud API Route Pivot

Status: `COMPLETED / DOCUMENTATION_ONLY / NO_LIVE_API_CALL`

Date: `2026-07-15`

## Goal

依 Product Owner 決策，將 Qwen3-8B 移出未來 NPC Actor active route（正式主線），以 OpenAI Responses
API 作替代方案，並在任何程式實作或付費請求前固定模型、推理、資料、成本與安全邊界。

## Required model for this package

使用目前可用的強推理 Codex 模型完成 Repository 與官方文件分析。這是規劃工作，不呼叫 NPC 的
雲端模型，也不產生 API 費用。

## Allowed files

- `PLANS.md`
- `PROJECT_STATE.md`
- `.codex/project_controller.md`
- `docs/NPC_ACTOR_AND_MODEL_ROUTING_BLUEPRINT_V1.md`
- `docs/npc_actor/NPC_ACTOR_CLOUD_API_BLUEPRINT_V1.md`
- `docs/npc_actor/NPC_ACTOR10_CLOUD_API_ROUTE_PIVOT_REPORT.md`
- `docs/work-packages/NPC-ACTOR-10_CLOUD_API_ROUTE_PIVOT.md`

## Required decisions

- Qwen3-8B／llama.cpp 保留歷史檔案與證據，但退出正式與 fallback（備援）路線。
- 預設為 OpenAI Responses API、`gpt-5.6-terra`、`medium`。
- `gpt-5.6-sol`／`high` 只允許受控升級；不得無聲增加成本。
- 使用 strict Structured Outputs 接回既有 `NpcActorResponse`。
- API 只接收角色可見的最小 Context；不得直接寫正式狀態。
- Key 只來自環境變數；live Pilot 前必須另有隱私與預算授權。

## Forbidden

- 不刪除或移動本機模型。
- 不修改 Kotlin、Test、Migration、Schema、SQLite 或安全門檻。
- 不讀取、建立、測試或提交 API Key。
- 不送出 live API request，不產生成本。
- 不使用 server／port，不 resolve，不改 Canon／Memory／Knowledge。
- 不把 Qwen3-8B 留成未記錄的自動 fallback。

## Verification

1. Current route 與 controller 都指向 V22 與本工作包。
2. 正式路由文件不再把 Qwen3-8B 列為未來候選或 fallback。
3. 歷史證據未刪除或改寫。
4. 雲端路線保留角色可見篩選、Schema、Validator、物理、Game Master、人工 Gate 與 transaction。
5. Repository 不含實際 API Key；Provider 呼叫與費用皆為 0。
6. 只修改允許文件，`git diff --check` 通過。

## Exit

本包完成後停在治理 Gate。不得直接開始 live API。下一包先修訂 no-API 治理邊界，再以 Fake HTTP
實作 NPC-ACTOR-10A；它仍不得連線。
