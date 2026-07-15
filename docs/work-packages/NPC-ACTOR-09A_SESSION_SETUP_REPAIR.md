# NPC-ACTOR-09A — Session Setup Repair

## Status

`SAFE_REJECTION / SETUP_REPAIR_PASS / NO_CANDIDATE`

## Objective

修正 NPC-ACTOR-09 的測試設定，而不是修改引擎或降低安全規則。建立一個全新的非 Canon
正式 Session，先證明玩家與沈霜分處不同地點，再允許一次 Qwen3-8B prepare。成功候選只供
審查，不得在本包 resolve。

## Required Flow

```text
new non-Canon fixture with no opening location override
→ official init-db / seed-demo / start-session
→ export-session-evidence
→ hard precondition: player location != NPC location
→ one prepare-local-npc-actor-formal-turn invocation
→ candidate or safe rejection evidence
→ focused / related / JSON / Hash / secret / Git checks
→ Commit and Push
→ stop before resolve
```

## Model Decision

- Primary：`Qwen3-8B-Q5_K_M.gguf`，GGUF／Q5_K_M，本機直接 llama.cpp Vulkan 程序。
- 模型與 runtime 只從 Repository 外既有安裝讀取；不下載、不開 server／port。
- 一般 advisor 登錄的 1.7B 不是本包替代品；本包以專案已驗證的 8B 品質證據為準。
- 模型輸出只可能成為 `ACCEPTED_FOR_HUMAN_REVIEW` 候選，不能直接寫正式狀態。

## Allowed Changes

- `PLANS.md`、`PROJECT_STATE.md`、`.codex/project_controller.md`；
- 本工作包與 `docs/npc_actor/NPC_ACTOR09A_SESSION_SETUP_REPAIR_REPORT.md`；
- `samples/npc_actor09a/**` 的 fixture、輸入、候選或拒絕、Session 證據與 Hash Manifest。

## Forbidden Changes

- 不修改 Production Kotlin、Test、Migration、Repository、Validator、StateReducer 或 Canon；
- 不改寫或刪除 `samples/npc_actor09/**`；
- 不直接 SQL、不提交 SQLite、不重用失敗 Session；
- 不啟動或呼叫 server／port、雲端 API、付費 Provider；
- 不執行 `resolve-local-npc-actor-formal-turn`；
- 不反覆生成到碰巧通過，不放寬角色、知識、物理、世界或時間安全規則；
- 不生成媒體、下一章、多 NPC 或多回合內容。

## Precondition Gate

模型呼叫前，官方 `export-session-evidence` 必須證明：

1. `char-player` 位於 `loc-npc09a-rear-hall`；
2. `char-shen-shuang` 位於 `loc-npc09a-old-corridor`；
3. 兩者位置不同；
4. `stateVersion=0`、`nextTurnNumber=1`；
5. 沒有任何正式回合已套用。

任一條不成立就停止，不能呼叫模型。

## True Stops

1. 模型／runtime 身分或 Hash 不符；
2. 新 Session 無法透過官方 CLI 建立；
3. 位置分離前置條件不成立；
4. 候選在最多兩次模型嘗試後仍被安全拒絕；
5. 候選已產生並推送，等待 Product Owner 對精確內容作決定；
6. 任何步驟需要降低 Validator、直接寫 SQL、使用秘密或連接雲端。

## Completion Boundary

完成表示設定根因已修復並經正式匯出證據驗證，且一次模型 prepare 的結果已保存至 GitHub。
成功候選仍未套用；本工作包授權永遠不能代替逐候選人工決定。

## Actual Result

- 設定修復通過：玩家在後殿、沈霜在舊廊，`stateVersion=0`、`nextTurnNumber=1`。
- Qwen3-8B prepare 只執行一次；內部最多兩次嘗試後為 `REJECTED_AFTER_MAX_ATTEMPTS`。
- 候選檔未建立，`resolve-local-npc-actor-formal-turn` 未執行，前後 Session 匯出證據逐位元相同。
- 依真正停止條件不重跑。後續應先建立安全的拒絕原因 sidecar（旁證報告），再決定是否調整
  Prompt、輸入契約或模型路由；本工作包不授權該實作。
- 驗證：focused 8/8、NPC Actor related 73/73、JSON 5/5、Manifest Hash 4/4、位置分離、
  Session 不變、秘密／私人路徑與 Git scope 全部 PASS。
