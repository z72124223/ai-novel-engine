# NPC-ACTOR-06 本機模型 Pilot 報告

狀態：`NPC_ACTOR06_LOCAL_MODEL_TECHNICAL_PASS / FORMAL_RUNTIME_AUTOMATION_NOT_AUTHORIZED`

日期：2026-07-14

基線：`9b7851736c0a80d59e2b407e12ff50777d9ed264`

## 結論

Qwen3-8B Q5_K_M 已在本機 GTX 1080 Ti 上透過 llama.cpp Vulkan 直接程序完成兩個真實、
非 Canon Pilot。模型只收到角色可見 Prompt，輸出先經 JSON Schema 與 Kotlin strict parser，
再通過既有 NPC-ACTOR-05 安全閉環。本包沒有啟動伺服器、沒有開 port，也沒有寫 SQLite、
Canon、Memory 或 Knowledge。

這證明本機 8B 模型能在安全外殼內產生可解析的 NPC 候選，也能在不合格時被要求一次修訂
或最終拒絕；不代表它已具有 GPT 等級的長篇劇本品質、完整物理理解或正式 Runtime 權限。

## 安裝與供應鏈

- 模型：`Qwen/Qwen3-8B-GGUF`，`Qwen3-8B-Q5_K_M.gguf`，GGUF／Q5_K_M，Apache-2.0。
- 模型 revision：`7c41481f57cb95916b40956ab2f0b139b296d974`。
- 模型 SHA-256：`068bae163faa96ad48032daf4e071a6a28fe67d8dcc95367609c2ff165e52738`。
- Runtime：`ggml-org/llama.cpp` b10002，build `a7312ae94`，Vulkan Windows x64。
- Runtime archive SHA-256：`c2b66ab6912e9fad75c7c6d2000f660bb40bc1f063aa62ec671d471e27dd92ea`。
- 權重與執行器均放在 Repository 外，Git 不含數 GB 權重、執行檔或使用者絕對路徑。

## 實作邊界

- `LlamaCppNpcActorPhysicalGateway` 只以 `ProcessBuilder` 直接執行 `llama-cli`。
- 預設 4096 context tokens、512 output tokens、固定 seed、4 分鐘內逾時。
- 每個 Gateway 實例以串流方式核對 5.85GB 模型 SHA-256，避免一次載入 RAM。
- 含中文的 Repository 路徑不直接交給 llama.cpp；Schema 會複製到 ASCII 系統暫存目錄。
- 使用 raw completion（純文字續寫）與 single turn（單輪退出），避免聊天模板控制 token 與
  JSON grammar 衝突，也避免程序等待下一輪輸入。
- 模型最多兩次嘗試；第二次只看已清理的 `NpcActorSafetyRepairRequest`，不看引擎私有物理資料。
- 非零退出、逾時、缺檔、超大輸出、非單一 JSON、未知欄位與 request／actor 身分漂移均 fail closed。

## 真實 Pilot 1：Lian

- 輸入：`npc-actor05-lian-knowledge-repair` 的既有角色可見資料與引擎上下文。
- 模型設定：4096 context、384 output、reasoning off、seed 20260714。
- 模型耗時：34,533 ms；原始輸出 4,892 bytes。
- 生成：繁體中文短句「有異常壓力嗎？」與 `REACH chair` 候選。
- 安全結果：`ACCEPTED_FOR_FORMAL_RESOLVE_CANDIDATE`，第一次通過，正式狀態寫入為 false。
- 品質：可理解、短小且符合角色可見資料，但 metadata 仍混用英文，語句較像功能性測試，
  不能據此宣稱已達最終影視劇本或真人對白品質。

證據：`samples/npc_actor06/pilot/npc-actor06-lian-local-model.report.json`

## 真實 Pilot 2：Yan

- 輸入：`npc-actor05-yan-storm-gate` 高風險情境。
- 模型設定：4096 context、512 output、reasoning off、seed 20260714。
- 第一次耗時 41,717 ms；因 target IDs 不一致與 observable consequence 為空白而在 actor contract 被拒絕。
- 系統只回傳已清理修訂單，`engineOwnedContextIncluded=false`。
- 第二次耗時 68,032 ms；角色契約通過後，在真正的 physical affordance 關卡因
  `DESTINATION_REQUIRED` 與 `PUSH_LIMIT_EXCEEDED` 被拒絕。
- 最終結果：`REJECTED_AFTER_MAX_ATTEMPTS`，共兩次，正式狀態寫入為 false。
- 品質：模型會犯契約與物理錯誤；安全鏈先擋角色錯誤，修訂後又擋下缺少目的地且超過推力
  上限的動作。這是預期的防護證據，不應把模型錯誤包裝成通過。

證據：`samples/npc_actor06/pilot/npc-actor06-yan-local-model.report.json`

## Schema 說明

本 Pilot Schema 只開放現有 `NpcActorPhysicalResponse` 中短 NPC 行動所需的欄位；Kotlin 模型
有預設值的欄位可省略，但未知欄位仍被禁止。Capability use（能力使用）等更廣泛情境尚未獲得
正式 Runtime 自動化授權，因此不以本 Pilot 宣稱完整覆蓋。

## 測試

- 新增 focused tests：8／8 PASS。
- 覆蓋：合法輸出、未知欄位、JSON 外文字、身分漂移、逾時、非零退出、缺檔、超大輸出、
  engine context 洩漏、一次修訂與 CLI 缺少 Runtime 時 fail closed。
- 真實 Schema 已由 llama.cpp 接受並約束兩個 Pilot 的輸出。
- NPC Actor／Physics related tests：84／84 PASS。
- `./gradlew.bat clean test --no-daemon`：961／961 PASS，0 failure、0 error、0 skipped。
- 既有 `StoryboardPipelineModels.kt` experimental serialization warning 仍存在，未新增失敗。

## 已知限制與下一個 Gate

- 本機 8B 適合短 NPC 候選與離線草稿，不等於高品質長篇劇本主模型。
- 首次模型載入與模型 Hash 核對會增加等待時間；目前真實生成約 35～68 秒／次。
- 尚未授權正式 Runtime 自動呼叫、世界自動推進或模型輸出自動套用。
- 下一步若要上正式 Runtime，必須另開工作包，加入 session transaction（工作階段交易）、
  可取消工作佇列、品質評測與清楚的人工／系統 Gate，不得由本包自動延伸。

## 證據索引

- 安裝：`docs/npc_actor/NPC_ACTOR06_LOCAL_MODEL_INSTALLATION.md`
- 環境鎖：`samples/npc_actor06/npc_actor06_model_environment_lock.json`
- Hash：`samples/npc_actor06/npc_actor06_evidence_hashes.json`
- 輸出 Schema：`schemas/npc_actor/npc_actor_physical_response_v1.schema.json`
