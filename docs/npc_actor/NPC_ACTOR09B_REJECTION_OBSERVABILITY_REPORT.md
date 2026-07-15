# NPC-ACTOR-09B 安全拒絕可觀察性完成報告

日期：2026-07-15

狀態：`TECHNICAL_PASS / REAL_MODEL_RETRY_NOT_AUTHORIZED`

## 白話結論

上一包的 Qwen3-8B 在兩次安全嘗試後被拒絕，當時系統只留下「拒絕」結果，沒有留下可判讀的
安全階段。NPC-ACTOR-09B 已修補這個診斷缺口：未來若再發生同類拒絕，系統會自動建立一份
sidecar（旁存證據檔），但不會保存模型原文，也不會把候選或世界狀態寫進正式資料。

這一包沒有重新呼叫模型，因此不能倒推出 NPC-ACTOR-09A 那一次的精確拒絕原因。若要取得真實
sidecar，必須由下一個獨立工作包核准一次受監督的重跑。

## 實作結果

- 新增嚴格的 `LocalNpcActorFormalTurnRejectionReport` 與逐次嘗試證據 model（資料結構）。
- 安全迴路耗盡時，prepare 服務先以既有 `abortPendingContext` 清除本次 pending Context。
- 清除後重新擷取 Session、turn、event、Memory、Knowledge 與物理狀態計數，全部必須與執行前相同。
- 自由文字 findings 不直接輸出；只保留符合 `^[A-Za-z0-9_.:-]{1,96}$` 的安全代碼，最多 16 個。
- CLI 拒絕時不建立候選檔，改寫入 `<candidate>.rejection.json`，並回傳狀態 2。
- 使用者可用 `--rejection-out` 指定 sidecar 路徑。
- 成功候選與人工 approve／reject resolve 路徑沒有改變。

## 明確不會寫入的內容

拒絕 sidecar 不含：

- Prompt 文字、玩家輸入與 actor request；
- rejected generated response、raw output 或 response JSON；
- 角色私密 Memory、Knowledge 或作者秘密；
- 物理 snapshot、archetype measurement 或 engine-owned geometry；
- Canon、正式事件、狀態 delta、Memory 或 Knowledge 寫入。

只保留：安全階段、白名單代碼、引擎提供的安全修正指示、模型身分與呼叫 Hash／metadata，以及
正式狀態未變的前後證明。

## 測試結果

- Focused（針對性）：`LocalNpcActorFormalTurnServiceTest`，`9/9` PASS。
- Related（相關）：正式候選、安全迴路、影子流程、llama.cpp gateway、物理綁定、CLI 與契約測試 PASS。
- Full（全專案）：`976/976` PASS，`0` failure、`0` error、`0` skipped，共 `165` 個測試套件。
- Strict JSON：Kotlin strict round-trip 與 PowerShell JSON parse PASS。
- Schema：檔案可解析，top-level `additionalProperties=false`，常數邊界與 false-only 欄位均有約束。
- Secret／raw-content：測試證明序列化 sidecar 不含 `generatedResponses`、`promptText`、`responseJson`、
  `physicalSnapshot`、`AUTHOR_ONLY` 或被拒絕的狗肩支撐句子。
- Git：`git diff --check` PASS；沒有 Migration、Repository、Validator、StateReducer、Canon 或媒體變更。

全專案命令的外層等待在三分鐘時先超時，但 Gradle 測試程序繼續正常完成；最終結果直接取自
Gradle 產生的 165 份 XML 報告，合計 976 個測試且沒有失敗。

## SHA-256

- `NpcActorCli.kt`：`63b70673545f021c5286dd3b770e161a290a0933641cc358afc412fbf4b011cf`
- `LocalNpcActorFormalTurnModels.kt`：`b1eb5a413ce0c14e1951031c64e918e7be5293f858cc70999d246c909dadb68c`
- `LocalNpcActorFormalTurnService.kt`：`eb6638f8b2a10cf5e74399b4e9a73f3f6073751096cd048d55e2b4a6a953eaeb`
- `LocalNpcActorFormalTurnServiceTest.kt`：`a4eb61ad0c61793d76e7b5999f3f1e6d6ccccd002c6ea4b58b9360d08a075f36`
- `npc_actor_formal_rejection_evidence_v1.schema.json`：
  `5d0f5c4fc1d269aefd23a5446e2669c9a05ac89859c7276f63ae0e4275057031`

## Git 與下一步

本包從 `fa7ed12f108d15ef0f7bb1ea089e39538fd839d2` 建立乾淨 Worktree；原髒工作樹未觸碰。

下一步不是自動重跑模型。若 Product Owner 授權，才建立
`NPC-ACTOR-09C_REAL_REJECTION_DIAGNOSIS`：重用有效的離場設定，只執行一次受監督的 Qwen3-8B prepare，
保存真實 sidecar 或候選，仍然不 resolve。
