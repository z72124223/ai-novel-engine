# NPC-ACTOR-09C 真實模型拒絕診斷報告

日期：2026-07-15

狀態：`SAFE_PROGRAM_REJECTION / NO_CANDIDATE / NO_RESOLVE`

## 白話結論

本包只呼叫一次本機 Qwen3-8B。模型產生的內容通過了本地安全迴路，也通過 Game Master（規則裁判），
但在準備正式候選的最後驗證中，被 `low_character_fit` 擋下：沈霜的角色一致性分數未達 70。

這表示安全邊界有正常工作，失敗內容沒有成為候選，也沒有寫入世界。測試前後的正式 Session 證據逐位元
相同，pending Context（待處理上下文）已清除，沒有執行 resolve（正式套用）。

本次也找出一個新的可觀察性缺口：NPC-ACTOR-09B 只會為「安全迴路用完兩次仍拒絕」建立 sidecar
（旁存證據檔）；本次已通過該迴路，卻在後面的正式 Runtime 驗證失敗，所以既有 sidecar 沒有被建立。
依工作包規則，本包沒有重跑模型，也沒有修改 Production Kotlin。

## 執行環境

- 基線 Commit：`5f0d214a25520cbbe681a024398ac397d9d16963`
- 分支：`codex/npc-actor09c-real-rejection-diagnosis`
- 模型：`Qwen3-8B-Q5_K_M.gguf`，GGUF／Q5_K_M
- 模型 SHA-256：`068bae163faa96ad48032daf4e071a6a28fe67d8dcc95367609c2ff165e52738`
- 執行器：`llama.cpp-b10002-vulkan/llama-cli.exe`
- 執行器 SHA-256：`f0741939ea8adecc58db40cfeabd8526d156878fdaa1ab2c8c1b6de08bdd8d9e`
- 執行方式：本機直接程序；沒有 server、port、雲端 API 或付費 Provider
- 臨時 SQLite：位於 Git 忽略的 `out/npc_actor09c/`，沒有提交

## 前置條件

- 使用已驗證的 NPC-ACTOR-09A 非 Canon seed 建立全新資料庫與全新 Session。
- Session：`session-3ad47f02-c93f-44c3-9fd8-9d1a0d6580dc`
- 玩家位置：`loc-npc09a-rear-hall`
- 沈霜位置：`loc-npc09a-old-corridor`
- 兩者位置不同：PASS
- `stateVersion=0`、`nextTurnNumber=1`、pending Context 0：PASS

所有資料庫操作都走官方 CLI，沒有直接 SQL，也沒有重用先前失敗的 Session。

## 唯一一次模型執行

- 命令：`prepare-local-npc-actor-formal-turn`
- preparation：`npc-actor09c-real-diagnosis-001`
- 外層 prepare 呼叫：1 次
- 模型內部嘗試上限：2
- timeout：240 秒
- seed：20260715
- 程序時間：約 50 秒
- 結果：`POST_SAFETY_FORMAL_VALIDATION_REJECTED`
- 錯誤代碼：`low_character_fit`
- 安全訊息：`characterActions[0] characterFitScore must be >= 70.`
- 候選檔：未建立
- NPC-ACTOR-09B safety sidecar：未建立
- 安全化程序失敗證據：已建立
- 第二次 prepare：未執行
- resolve：未執行

從既有程式路徑可確定：只有 local safety loop（本地安全迴路）與 Game Master 都接受後，才會進入發生
這個錯誤的正式 `runtime.validate`。因此，本次不是模型載入失敗，也不是離場位置設定錯誤，而是正式角色
一致性規則拒絕了模型結果。

## 正式狀態安全證據

| 項目 | prepare 前 | 失敗後 |
|---|---:|---:|
| `stateVersion` | 0 | 0 |
| `nextTurnNumber` | 1 | 1 |
| 玩家位置 | 後殿 | 後殿 |
| 沈霜位置 | 舊廊 | 舊廊 |
| pending Context | 0 | 0 |
| 正式回合套用 | 否 | 否 |

前後 Session 證據 SHA-256 都是：
`2977296df66428c33b5c3110dd61d8aa575e1d13316eed213b35528b31425bac`。
兩份檔案逐位元相同。

臨時 SQLite 因建立並清除 pending Context 而有位元變化，但官方匯出的語意狀態完全相同。資料庫沒有提交
GitHub。

## 安全化證據

`npc_actor09c_program_failure_evidence.json` 只保存：

- 失敗階段與固定錯誤代碼；
- 單次呼叫設定；
- 模型與執行器 Hash；
- 前後 Session 狀態；
- 沒有候選、沒有 resolve、pending 已清除的事實。

它不保存 Prompt、模型原文、generated response、作者秘密、角色私密資料或物理 snapshot。

## 驗證結果

- Focused（針對性）`LocalNpcActorFormalTurnServiceTest`：`9/9` PASS。
- Related（相關）全部 `*NpcActor*`：`74/74` PASS，`0` failure、`0` error、`0` skipped。
- JSON：`5/5` 可解析。
- Manifest：`4/4` 檔案 Hash 與 bytes 重算一致。
- prepare 前後 Session 證據：逐位元相同。
- 候選檔：0；safety sidecar：0；resolve：0。
- 臨時資料庫 Git ignore：PASS。
- Production Kotlin、Test、Migration、Repository、Validator、StateReducer：0 項修改。
- 完整測試未重跑；本包沒有修改 Production 或 Test，且 NPC Actor 相關回歸已全部通過。基線
  NPC-ACTOR-09B 的完整測試為 `976/976` PASS。

## 下一步

停止在新的診斷 Gate，不再呼叫模型。若要繼續，下一個最小工作包應為：

`NPC-ACTOR-09D_POST_SAFETY_VALIDATION_OBSERVABILITY`

它只補「local safety loop 已接受，但正式 Runtime 驗證拒絕」時的安全 sidecar，並加入測試；不得降低
`characterFitScore >= 70`、不得保存模型原文，也不得重跑 Qwen3-8B。待這個診斷缺口補好並另獲授權後，
才可考慮下一次模型測量。
