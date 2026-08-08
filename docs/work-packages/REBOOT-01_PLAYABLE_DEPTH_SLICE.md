# REBOOT-01 — Playable Depth Slice

Status: `READY_FOR_AUTONOMOUS_CODEX_EXECUTION`

Parent:

- `docs/REBOOT_2026_PLAYABLE_DEPTH_BLUEPRINT_V1.md`
- `docs/CODEX_MODEL_AND_BUDGET_POLICY_V1.md`
- `docs/CURRENT_ROUTE_GATE_V2.md`

## 1. 任務目標

在不等待每個內部階段人工批准的情況下，將現有 AI Novel Engine 推進到一個真正可運行、可保存、可重播、具有 NPC 自主性與戲劇深度的 5–10 回合垂直切片。

本工作包的完成定義是「引擎跑出證據」，不是再增加一組藍圖文件。

## 2. Codex 模型

主控：

```text
目前可用 GPT-5.6 Codex coding model
reasoning = MEDIUM
```

升級規則：

- SQL Migration、Transaction、Validator、StateReducer、Knowledge Boundary、難以重現核心 Bug與最終整合審查：`HIGH`；
- 搜尋、文件盤點、測試分類、重複工作：Low／Medium 或腳本；
- `MAX` 預設禁止，遵守 `docs/CODEX_MODEL_AND_BUDGET_POLICY_V1.md`。

## 3. 多代理

允許主控啟動最多四個 Subagents：

1. `RepositoryExplorer`
2. `RuntimeTester`
3. `NarrativeSystemsCritic`
4. `CostAndComplexityAuditor`

Subagent 優先只讀。核心程式同一時間只能有一個 Writer。

## 4. 執行授權

這是一個 Umbrella Work Package。

Codex 在同一明確啟動的 CLI 工作階段中，可以：

```text
Stage 0
→ Stage 1
→ Stage 2
→ Stage 3
→ Stage 4
→ Stage 5
→ Stage 6
→ Stage 7
```

每個 Stage 完成後：

- 執行最低必要驗證；
- 建立獨立 Commit；
- 繼續下一個 Stage；
- 不需要回到 GPT 總控索取下一個指令。

只有第 15 節的 True Stop Condition 才停止。

## 5. Branch／Worktree

先：

```powershell
git fetch --prune origin
```

從最新遠端預設分支建立乾淨 worktree／branch：

```text
reboot/playable-depth-v1
```

不得在 123 項舊髒工作樹中直接實作，不得複製整個舊髒樹。

## 6. Stage 0 — Reality Baseline

目的：先證明現有引擎到底能做什麼，不先改功能。

必做：

1. 讀取：
   - `PROJECT_STATE.md`
   - `.codex/project_controller.md`
   - `docs/CURRENT_ROUTE_GATE_V2.md`
   - 本工作包
   - `AGENTS.md`
   - 架構與 Runtime 相關文件
2. 建立現在的 CLI、Service、Repository、Migration、Test、世界資料與現有 Session 流程清單。
3. 執行：
   - compile；
   - targeted runtime tests；
   - full tests once；
   - `query-world-source`；
   - 一個既有最小 Session 的 start／prepare／resolve／show 流程。
4. 將已知的 World Source Hash／Byte Size 失敗重新驗證；若仍存在，修復真正原因，而不是將它藏成 unrelated failure。
5. 產生：

```text
docs/reboot/REBOOT01_BASELINE_REPORT.md
out/reboot01/baseline/**
```

報告只記錄實際命令、成功／失敗、SQL 狀態和缺口，不新增第二套治理框架。

Commit：

```text
reboot: establish executable engine baseline
```

## 7. Stage 1 — Validation Simplification And Evidence Persistence

目標：將硬門檻縮成協議、世界合法性、不可逆批准三類；品質問題改為 Score／Warning。

必做：

1. 盤點目前會阻止回合保存的檢查。
2. 分類：
   - `PROTOCOL_HARD_FAIL`
   - `WORLD_LEGALITY_HARD_FAIL`
   - `IRREVERSIBLE_APPROVAL_REQUIRED`
   - `QUALITY_WARNING`
3. 不破壞現有安全門檻，但不得讓純文風／節奏問題阻止 raw evidence 保存。
4. 建立或重用 SQL／Repository 結構，至少能保存：

```text
run id
session id
turn
scenario seed
raw draft
critic scores
revision suggestion
revised draft
validation findings
state delta
apply result
model metadata if available
elapsed time
```

5. Migration 只能 additive；若不需 Migration，優先重用現有 tables。
6. 失敗回合也要保存 Evidence，不代表套用正式 State Delta。
7. 建立 targeted tests：
   - 品質 Warning 不丟失 Evidence；
   - 世界非法結果不套用 State；
   - 同一 result 不可重複套用；
   - raw／revised 版本可追蹤。

Commit：

```text
feat(reboot): persist run evidence without overblocking quality
```

## 8. Stage 2 — NPC Autonomous Components

目標：加入一名以上 NPC 在玩家不在場時仍能依自身資訊與利益行動的最小閉環。

優先新增或重用：

```text
NpcPerceptionService
NpcMemoryImportanceService
NpcReflectionService
NpcGoalPlanner
NpcScheduleService
NpcActionProposalService
NpcTurnScheduler
OffscreenSimulationService
GameMasterResolutionService
```

名稱可依現有架構調整，不得為了對齊本文件而建立重複 Service。

每個 NPC 行動留下 `DecisionTrace`：

```text
activeGoal
relevantMemories
currentBeliefs
perceivedSituation
consideredActions
selectedAction
predictedCost
rejectedAlternatives
offscreenReason
```

規則：

- NPC 只能使用自己的知識、記憶、地點、資源與關係；
- Agent 只提出行動，Game Master／現有 Validator 判定結果；
- 玩家不在場時可推進，但不能直接修改 Canon；
- NPC 不得因為「玩家需要情報」而突然變成工具人；
- 同一 Tick 的正式寫入保持 Transaction；
- Offscreen 行動必須可查詢、可回放、可向玩家呈現後果。

最小測試：

- 玩家離場後世界時間推進；
- NPC 產生自主行動；
- 行動符合知識與地點；
- 不合法行動被拒絕但 Evidence 保存；
- 玩家回場後能觀察到後果。

Commit：

```text
feat(reboot): add bounded offscreen NPC autonomy
```

## 9. Stage 3 — Drama Director And Narrative Quality Loop

目標：建立有戲劇問題、情緒波形與有限自我糾錯的生成流程。

至少建立／重用：

```text
DramaticQuestion
ValueConflict
EmotionalWaveState
SceneValueState
PromisePayoffTracker
NarrativeStallDetector
LogicCritic
CharacterDramaCritic
ProsePacingCritic
NarrativeRevisionCoordinator
```

每場戲追蹤：

```text
sceneGoal
dramaticQuestion
valueBefore
valueAfter
stakes
cost
irreversibleChange
relationshipChange
reveal
emotionalBeat
```

Critic 分數遵守 Blueprint。

Revision：最多一次。

Narrative Stall：連續兩回合沒有 State／Relationship／Value／Cost／Reveal 變化時觸發，下一步只能升級、選擇、揭露、付代價或結束場景。

不得讓 Drama Director 直接控制角色意志，也不得把 Director metadata 寫進角色知識或讀者文字。

Commit：

```text
feat(reboot): add drama scoring and bounded narrative revision
```

## 10. Stage 4 — Chenxing Non-Canon Vertical Slice

目標：建立一個可重置、不升格 Canon 的塵行測試 Scenario。

位置建議：

```text
samples/reboot01/
```

或現有更合適的 experiment／sample 路徑。

不得修改正式 Canon 來配合測試。

Scenario 必須包含：

- 陸塵行或現有主角；
- 愛醬；
- 至少兩名有獨立目標的重要 NPC；
- 至少一名對立角色或對立力量；
- 一個清楚的 Dramatic Question；
- 一個價值兩難；
- 一次玩家不在場事件；
- 一次不可逆但非 Canon 的 Runtime 後果；
- 情緒波形：笑點、溫暖、衝突、憤怒／失落／反思、希望或未解問題。

若現有正式世界資料缺少適合 NPC，可使用 MVP-8 provisional candidate，但必須明確保持 non-canon。

Codex 在本工作階段可作為外部模型，依 Context 產生合法 AI result JSON；不得跳過引擎套用流程。

跑 5–10 回合，保存：

```text
turn input
context
raw draft
critic reports
revised draft
AI result
validation
state delta
SQL state snapshot
reader narrative
DecisionTrace
offscreen events
```

Commit：

```text
test(reboot): add Chenxing playable depth scenario
```

## 11. Stage 5 — Replay, Benchmark And Evolution

至少執行：

1. 同一 Scenario Seed 跑兩次；
2. 一次使用現行規則；
3. 一次使用一個有證據的小型規則修正；
4. 比較：
   - legal apply rate；
   - stall count；
   - autonomy score；
   - emotional wave coverage；
   - irreversible change count；
   - repetition／AI prose risk；
   - elapsed time；
   - model／reasoning；
   - retry count。

建立：

```text
docs/reboot/REBOOT01_REPLAY_AND_EVOLUTION_REPORT.md
out/reboot01/benchmark/**
```

只有比較證明更好，才採用新規則。

Commit：

```text
test(reboot): benchmark narrative evolution with replay
```

## 12. Stage 6 — Minimal Local Web Playable Slice

只有 Stage 0–5 的引擎閉環通過後執行。

第一版只需：

- 故事文字；
- 玩家輸入；
- 送出；
- 世界時間；
- 角色／關係摘要；
- 存檔／讀檔；
- 開發模式下的 Context、Critic、DecisionTrace、State Delta 檢視。

允許：

- 小型本機 API；
- React／TypeScript 或更小的 Web UI；
- Ktor 或符合現有 Kotlin 架構的本機 HTTP 層；
- 本機 Port。

限制：

- 不公開部署；
- 不登入；
- 不付費；
- 不接外部模型 API；
- 不把 SQLite 暴露給瀏覽器；
- 不重寫引擎核心到 JavaScript。

若 Codex 經 Repository 證據判斷現有 CLI 包裝成靜態本機介面更小，可採用替代方案，但必須真的可操作。

Commit：

```text
feat(web): add local playable depth interface
```

## 13. Stage 7 — Final Integration And Honest Report

執行：

- compile；
- targeted tests；
- full tests；
- 5–10 回合 end-to-end；
- replay benchmark；
- Web smoke test；
- SQL evidence query；
- `git diff --check`；
- secret／API key scan；
- scope／stale-doc check。

建立：

```text
docs/reboot/REBOOT01_FINAL_REPORT.md
docs/reboot/REBOOT01_PLAY_GUIDE.md
```

報告必須誠實列出：

- 完成能力；
- 未完成能力；
- 真實測試結果；
- 失敗與已知限制；
- 模型／推理使用；
- 子代理使用；
- 成本數據（僅在可取得時）；
- 下一個最有價值的產品步驟；
- 哪些舊規則已退休；
- 圖片線仍暫停在哪個狀態。

最後 Push：

```text
origin/reboot/playable-depth-v1
```

不得直接 force 更新預設分支。

## 14. 硬性完成條件

全部必須成立：

- 5–10 回合完整執行；
- SQL／Evidence 保存完整；
- 一次 offscreen NPC 行動；
- 一個價值兩難；
- 一個不可逆 Runtime 後果；
- 至少三次狀態或關係變化；
- Narrative Stall 可偵測；
- Critics 有具體分數與修訂；
- 最多一次自動修訂；
- 同 Seed Replay 比較完成；
- 有可操作的本機 Web Slice，或有不可迴避且有證據的技術阻塞；
- 不花費外部 Provider/API；
- 不修改正式 Canon；
- 不丟失舊圖片候選與歷史證據。

## 15. True Stop Conditions

只有以下情況停止並在 Codex 終端直接詢問使用者：

1. 必須由使用者決定正式 Canon／角色命運／美術／戀愛方向；
2. 必須購買或呼叫付費 API；
3. 需要 API Key；
4. 需要破壞性 Git 操作，且無 worktree／backup／cherry-pick 等安全替代；
5. 兩個 AUTHOR_LOCKED 來源直接衝突，Source-of-Truth 無法裁決；
6. 必須弱化 Transaction、Knowledge Boundary、stateVersion、contextHash 或不可逆批准；
7. Medium 與 High 各有至少一次有證據的修復仍無法恢復核心編譯／測試。

以下不是停止條件：

- 一般設計選擇；
- 測試失敗；
- 文件不一致；
- 需要小型 additive Migration；
- 需要建立新 Service／Model／Test；
- Critic 意見分歧；
- Web UI 技術選型；
- 需要重構一個明確瓶頸。

Codex 應選擇最小、安全、可回退方案並繼續。

## 16. 禁止事項

- 不開始圖片 Task 02–07；
- 不生成新圖片或影片；
- 不接 OpenAI／xAI／Google／ByteDance／Runway 等付費媒體 API；
- 不把 API Key 放進 Git；
- 不修改正式塵行 Canon 以方便測試；
- 不以增加十份文件代替一個可運行 Stage；
- 不把所有品質 Finding 重新變成硬門檻；
- 不使用 `git add .`／`git add -A`／force push；
- 不在舊 123 項髒工作樹上直接工作；
- 不因 full test 有一個無關失敗就放棄整個垂直切片，必須分類並修復真正基線問題。

## 17. 完成回報格式

Codex 最終只需向使用者回報一次：

```text
REBOOT-01 final classification:
Model/reasoning by stage:
Subagents used:
Base SHA:
Branch:
Commits:
Playable scenario:
Turns completed:
Offscreen actions:
Dilemmas and irreversible outcomes:
SQL/evidence persistence:
Critic/revision evidence:
Replay comparison:
Web URL / launch command:
Tests:
Known failures:
Files changed:
Provider spend:
Canon changes:
Image lane status:
Recommended next step:
```

不要要求使用者逐 Stage 轉貼回 GPT。