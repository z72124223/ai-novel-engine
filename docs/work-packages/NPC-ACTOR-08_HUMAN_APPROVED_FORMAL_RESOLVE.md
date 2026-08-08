# NPC-ACTOR-08 — Human-Approved Formal Resolve

## Status

`COMPLETED / TECHNICAL_PASS / REAL_CANDIDATE_APPROVAL_REQUIRED`

## Objective

建立一條兩階段、非 Canon 的正式 NPC 回合流程：

1. `prepare`：從正式 Session 建立 pending context，使用本機 NPC Actor 產生候選並完成安全檢查；
2. `resolve`：只有精確綁定候選內容與正式 Context 的人工決定為核准時，才呼叫既有
   `MultiTurnRuntime.resolveTurn`。

工作包授權不是候選核准。任何真實模型候選都必須先輸出供人閱讀，再由獨立決定檔逐筆核准。

## Required Flow

```text
formal Session
→ prepareTurn / pending Context
→ actor-visible request
→ local model candidate
→ actor + knowledge + physical + world + time safety loop
→ engine-owned AiTurnResult compiler
→ candidate payload + SHA-256
→ HUMAN_APPROVAL_REQUIRED
→ exact decision binding validation
→ existing resolveTurn validators and narrative quality gate
→ existing SQLite transaction
→ applied non-Canon turn or safe rejection
```

## Approval Binding

人工決定至少必須綁定 candidate id／Hash、project／session／turn／context id、Context Hash、
turn number、base state version、result id／Hash，以及 reviewer、review time、明確 decision
和固定 acknowledgement。

允許的 decision 只有 `APPROVE_NON_CANON_FORMAL_TURN` 與 `REJECT_NON_CANON_FORMAL_TURN`。

## Engine-Owned Compilation

- 模型回應不得提供或覆寫正式 Session／Context 身分。
- 模型不得提供任意 `stateDelta`、Memory、Knowledge、Canon 或 migration。
- 引擎只可從已接受的 response／proposal 編譯可見敘事、角色行動、bounded event、
  Game Master 核准的時間推進與物理提案。
- 正式物理 Context 必須重新綁定 pending Context 與 canonical `AiTurnResult` Hash。
- 任何需要 V2 保存但缺少合法物理版本／Hash 的行動必須由既有物理 Gate 拒絕。

## Failure Rules

- 產生器或安全迴路失敗：清除本次建立的 pending context，不輸出可核准候選。
- 人工拒絕：精確驗證決定綁定後清除該 pending context，正式回合不套用。
- Hash／身分不符、候選竄改、過期或重播：fail closed，不套用，不自行改寫決定檔。
- `resolveTurn` Validator／敘事品質失敗：不得繞過；保留證據並讓人工明確 reject／abort。

## Allowed Changes

- NPC Actor 的 Kotlin models、services、CLI 與直接測試；
- `Main.kt` 的新命令註冊與 Help；
- `schemas/npc_actor/**`、`samples/npc_actor08/**`；
- 本工作包、實證報告、`PLANS.md`、`PROJECT_STATE.md`、controller。

## Forbidden Changes

- 不修改既有 Migration、Repository transaction、`StateReducer` 或 Validator 以降低門檻；
- 不接雲端 API、付費 Provider、server、port，不下載模型；
- 不自動核准真實模型輸出，不把工作包授權當候選核准；
- 不修改 Canon、世界設定、圖片、影片、音訊、下一章或互動遊戲功能；
- 不使用 force push、`git add .` 或原始髒 Worktree。

## Required Tests

1. 準備成功會建立一個 Hash-bound 候選與 pending context，但不套用回合。
2. 缺少人工決定不能套用。
3. 候選內容被改、決定 Hash 錯、Session／Context／turn／state version 錯時拒絕。
4. 人工拒絕會清除該 pending context且不增加 Session 版本、event 或 applied turn。
5. test-only 人工核准會經完整 `resolveTurn`，只套用引擎編譯的 bounded 結果。
6. 已套用候選重播時拒絕。
7. 生成／安全迴路失敗不留下 pending context。
8. 作者秘密、其他角色記憶與 engine-owned 測量不進入模型請求。
9. focused、related、full tests 與 JSON／Schema／Hash／Git scope 全部通過。

## Completion Boundary

通過表示「人工逐候選核准可安全驅動一個非 Canon 正式回合」。不表示真實 8B 候選已被
Product Owner 核准、不表示自動模式已授權，也不表示可升格 Canon 或連接雲端服務。
