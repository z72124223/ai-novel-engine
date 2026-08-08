# NPC-ACTOR-07 正式工作階段影子接入報告

狀態：`TECHNICAL_PASS / SHADOW_ONLY / NO_FORMAL_STATE_WRITE`

日期：2026-07-15

## 結論

本工作包已把本機 `Qwen3-8B Q5_K_M` 接到真正的 `MultiTurnRuntime` Session 預覽資料，但只執行影子模式。模型收到的是單一 NPC 可見的記憶、知識、目標、關係、事件與物理白名單，不會收到完整世界資料、作者秘密、導演目標或引擎內部幾何數值。

影子流程不建立 pending context，不呼叫 `resolve-turn`，也不寫入 SQLite、Canon、Memory、Knowledge、Event 或正式物理狀態。

## 正式流程邊界

```text
正式 Session
→ previewTurnContext（只讀預覽）
→ NPC 可見性過濾
→ 本機 Qwen3-8B 候選演出
→ 角色契約／物理／世界／時間安全鏈
→ 影子報告
→ STOP（不得進入 resolve-turn）
```

CLI 命令：

```text
run-local-npc-actor-shadow
```

## 自動驗證涵蓋

1. 離場 NPC 的安全候選可以通過完整安全鏈，但不寫正式狀態。
2. 同場對話刺激可以進入 NPC 可見提示，但現有離場世界規則會安全拒絕。
3. 玩家私有記憶、玩家私有知識、未揭露作者秘密與 director-only 內容不會進入模型提示。
4. 「成人扶著狗肩站起來」會被物理規則拒絕。
5. 本機模型執行檔或模型檔缺失時，CLI 會 fail closed（安全失敗）且不產生假報告。
6. `ContextPacket` 已採用 `retrievedMemories` 後，世界裁判也接受同一個 NPC 的正式檢索記憶，不再誤判為外來記憶。

## 真實 8B Pilot

- Project：`demo`
- Session：`session-b50b6cfe-eefc-446c-839d-024881bc1373`
- Turn：`1`
- Context：`ctx-19ac9c9a-3990-47b4-a05a-4e4e98ddcc7e`
- Context SHA-256：`4f195d404a3ef0cb5ecff4b37bff4787949e8628259dabb95317439f6656cbc5`
- 模型：`Qwen/Qwen3-8B-GGUF`
- 格式／量化：`GGUF / Q5_K_M`
- 模型 SHA-256：`068bae163faa96ad48032daf4e071a6a28fe67d8dcc95367609c2ff165e52738`
- Runtime：`llama.cpp-b10002-vulkan`
- 嘗試次數：`2`（最多一次修訂）
- 結果：`SHADOW_CANDIDATE_REJECTED_NO_STATE_WRITE`

第一次輸出因缺少可觀察後果而被角色契約拒絕；安全修訂後，第二次輸出通過角色與物理綁定，但因示範 Session 中玩家仍與 NPC 同地點，被既有離場世界規則正確拒絕。第二次也引用了 sandbox profile 的 false-belief 識別碼；它不是正式知識 claim，因此世界裁判維持拒絕。這是安全行為，不是狀態寫入失敗。

## 不變證據

前後資料庫 SHA-256 都是：

```text
330be6263f7a0a8928a0f38738183fb5e6171248b4759b02bc034e04bae687d9
```

前後均保持：

- Session state version：`0`
- next turn number：`1`
- applied turns：`0`
- turn logs：`0`
- events：`0`
- formal physical transitions：`0`
- pending preview context：`false`
- server started：`false`
- port opened：`false`
- formal state write：`false`

## 證據檔

- `schemas/npc_actor/npc_actor_formal_runtime_shadow_v1.schema.json`
- `samples/npc_actor07/pilot/npc_actor07_formal_runtime_shadow_input.json`
- `samples/npc_actor07/pilot/npc_actor07_formal_runtime_shadow_report.json`
- `samples/npc_actor07/npc_actor07_evidence_manifest.json`

Pilot 報告 SHA-256：

```text
98ce61a35d1f2ec5b876b7987ce941160f126f4051ae841d15b868999dd2be45
```

## 下一個真正 Gate

NPC-ACTOR-08 才能討論「人類批准後，把候選送入既有正式 resolve-turn」。本工作包沒有授權任何自動寫入、背景排程、API、Server、Port 或 Canon 升格。

## 最終驗證

- focused tests：`6/6 PASS`
- NPC Actor／Physics related tests：`82/82 PASS`
- full tests：`967/967 PASS`
- JSON parse：`PASS`
- evidence Hash manifest：`3/3 PASS`
- zero-write report：`PASS`
- secret scan：`PASS`
- absolute-path scan：`PASS`
- server／port code scan：`PASS`
- `git diff --check`：`PASS`
