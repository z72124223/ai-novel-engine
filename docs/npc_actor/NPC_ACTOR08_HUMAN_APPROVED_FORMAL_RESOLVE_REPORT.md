# NPC-ACTOR-08 Human-Approved Formal Resolve Report

## Result

`TECHNICAL_PASS_REAL_CANDIDATE_APPROVAL_NOT_PERFORMED`

NPC-ACTOR-08 已證明：一個通過既有 NPC Actor 安全迴路的候選，可以先停在人工核准 Gate；
只有精確綁定候選與正式 Context 的人工決定，才能要求既有 `resolveTurn` 完成一個非 Canon
正式回合。這次成功交易使用 test-only 人工決定，沒有把 Product Owner 的工作包授權當成
真實候選內容核准。

## Implemented Flow

```text
existing formal Session
→ prepareTurn creates one pending Context
→ shared actor-visible Context compiler
→ local-model generator abstraction
→ existing NPC-ACTOR-05 safety loop
→ engine-owned AiTurnResult compilation
→ candidate package SHA-256
→ exact human decision file
→ existing protocol / knowledge / physical / narrative validators
→ existing SQLite transaction
→ one applied non-Canon turn or exact pending-context abort
```

## Approval Safety

- 候選包內容以 SHA-256 綁定。
- 人工決定綁定 candidate、project、Session、turn、Context、state version、Context Hash、
  result id 與 canonical result Hash。
- `reviewedAt` 必須是含時區的 ISO-8601 時間；test-only reviewer 必須使用 `TEST_` 身分。
- 候選被改、決定綁錯、Context 過期、結果重播或 pending Context 不存在時安全拒絕。
- 過期候選不能核准套用，但仍可用精確綁定的拒絕決定清除舊 pending Context。
- CLI 若候選檔寫入失敗，會清除剛建立的 pending Context。

## Engine-Owned Result Boundary

模型只產生 `NpcActorPhysicalResponse`。引擎從已接受回應固定編譯：

- 可見敘事與一筆 `CharacterAction`；
- 一筆 bounded offscreen event；
- Game Master 已核准的時間推進；
- 一筆通過物理綁定的瞬時物理提案。

模型不能提供任意 state delta、Session／Context 身分、Memory、Knowledge、Canon、Migration 或
資料庫操作。第一版只允許 `REACH`／`USE_AS_SUPPORT` 等瞬時物理動作；需要保存物理狀態的
動作仍停在後續獨立審查，不會藉本包偷渡。

## Test-Only Transaction Evidence

自動測試建立隔離 SQLite Session，先產生候選，再使用明確標記 `testOnly=true` 的人工決定：

- 核准前：只有一個 pending Context，Session state version、next turn、applied turn、event、
  Memory 與 Knowledge 不變。
- 核准後：pending Context 消失，Session state version 與 next turn 各增加一，applied turn
  與 event 各增加一。
- Memory 與 Knowledge 筆數保持不變。
- 同一候選第二次套用被拒絕。
- 人工拒絕只刪除精確 pending Context，不增加正式回合或 event。

這是工程交易證據，不是 Product Owner 對任何真實 Qwen3-8B 內容的核准。

## Verification

- focused：14／14 PASS（NPC-ACTOR-08 8 + NPC-ACTOR-07 regression 6）。
- NPC Actor／Physical／core related：129／129 PASS。
- full：975／975 PASS。
- JSON：4／4 PASS（Schema、輸入範例、人工決定範本、證據 Manifest）。
- Kotlin compile、`git diff --check`：PASS。

第一次在原 Worktree 路徑執行完整測試時，4 個既有 Comic／Video 測試因資料夾名稱含
`approved` 而把絕對路徑誤判成資產狀態宣稱。未修改影片程式或測試；改由不含該字的中性
Junction 路徑重跑，同兩個測試類立即通過，完整 975／975 亦通過。這證明是路徑造成的既有
假陽性，不是 NPC-ACTOR-08 回歸。

## Commands Added

- `prepare-local-npc-actor-formal-turn`：產生候選包並停在人工 Gate。
- `resolve-local-npc-actor-formal-turn`：讀取候選包與精確人工決定，核准套用或拒絕清理。

## Remaining Gate

真實 Qwen3-8B 候選尚未逐筆產生、呈現與核准。本工作包完成後仍禁止：

- 自動接受模型候選；
- 使用普通聊天中的「好」取代候選決定檔；
- 雲端 API、Provider、server、port；
- Canon、Memory、Knowledge、圖片、影片、音訊或下一章工作。

Evidence manifest：`samples/npc_actor08/npc_actor08_evidence_manifest.json`。
