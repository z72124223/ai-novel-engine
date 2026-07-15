# NPC-ACTOR-09 — Real Supervised Pilot

## Status

`SAFE_REJECTION / BLOCKED_BY_SESSION_BOOTSTRAP_SEMANTICS / NO_CANDIDATE`

## Objective

在隔離、非 Canon 的正式 Session 中，以本機 Qwen3-8B 產生一份真實 NPC 行動候選，完成
NPC-ACTOR-08 的 prepare 階段並推送可審查證據。這個工作包不包含 resolve 授權。

## Required Flow

```text
official init-db / seed-demo / start-session
→ before-state evidence
→ prepare-local-npc-actor-formal-turn
→ Qwen3-8B direct llama.cpp process
→ existing safety loop and engine-owned result compiler
→ exact candidate package and Hash evidence
→ GitHub Push
→ Product Owner reviews this exact candidate
→ stop before resolve
```

## Model Decision

- Primary：`Qwen/Qwen3-8B-GGUF`，`Qwen3-8B-Q5_K_M.gguf`，已驗證。
- Runtime：llama.cpp b10002 Vulkan `llama-cli.exe` 直接程序，已驗證。
- 不使用雲端 API；不使用目前較快但角色品質較弱的 Qwen3-1.7B 作本次主要候選。
- 模型與執行器留在 Repository 外，Git 只保存身分、Hash、輸入與結果證據。

## Allowed Changes

- `PLANS.md`、`PROJECT_STATE.md`、`.codex/project_controller.md`；
- 本工作包與 `docs/npc_actor/NPC_ACTOR09_REAL_SUPERVISED_PILOT_REPORT.md`；
- `samples/npc_actor09/**` 的非 Canon fixture、輸入、候選、狀態摘要與 Hash Manifest。

## Forbidden Changes

- 不修改 Production Kotlin、Test、Migration、Repository、Validator、StateReducer 或 Canon；
- 不把普通聊天同意寫成候選決定檔，不執行 `resolve-local-npc-actor-formal-turn`；
- 不直接 SQL、不提交 SQLite、不啟動 server／port、不接雲端 API 或付費 Provider；
- 不反覆生成到碰巧通過，不放寬秘密、物理、知識、世界或時間規則；
- 不生成媒體、下一章、多 NPC 或多回合內容。

## True Stops

1. 模型或 runtime 身分／Hash 不符；
2. 正式 Session 無法透過官方 CLI 建立；
3. 候選在最多兩次模型嘗試後仍被安全拒絕；
4. 候選已產生並推送，等待 Product Owner 對該精確內容核准或拒絕；
5. 任何步驟需要降低 Validator、直接寫 SQL、使用秘密或連接雲端。

## Completion Boundary

完成表示真實本機候選已安全產生並可審查，正式回合尚未套用。只有新的、逐候選決定才能
進入 NPC-ACTOR-09 的後半段 resolve；本工作包授權本身永遠不能代替該決定。

## Actual Result

- Qwen3-8B prepare 執行一次，設定的模型嘗試上限為 2，最終為
  `REJECTED_AFTER_MAX_ATTEMPTS`。
- 候選檔未建立，`resolve-local-npc-actor-formal-turn` 未執行。
- 原 fixture 雖把玩家設在後殿，但 opening scene 帶有 `locationId`；既有 `start-session` 會把
  專案內全部角色移到 opening location，因此玩家與沈霜最後同在舊廊。
- 這不是降低安全規則的理由。依真正停止條件封存證據；修正 fixture 與重跑屬新的工作包。
- 驗證：focused 8/8、NPC Actor related 73/73、JSON 5/5、Manifest Hash 4/4 與
  前後 Session 證據逐位元相同，全部通過。
