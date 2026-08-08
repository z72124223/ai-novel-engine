# NPC-ACTOR-06 — Local Model Pilot

狀態：`COMPLETE — NPC_ACTOR06_LOCAL_MODEL_TECHNICAL_PASS / FORMAL_RUNTIME_AUTOMATION_NOT_AUTHORIZED`

日期：2026-07-14

## 目的

將一個本機 8B 文本生成模型接到既有 provider-neutral（供應者中立）NPC Actor 邊界，
讓它只產生結構化候選，再由 NPC-ACTOR-05 已完成的五階段安全閉環判斷接受、要求一次修訂，
或安全拒絕。本包不把模型輸出直接套用到正式世界。

## 授權

Product Owner 已明確授權：

- 安裝適合本機硬體的 7B／8B 文本生成模型；
- 依需求調用本機已安裝模型；
- 完成本機生成模型的規劃、實作與驗證。

本授權不包含付費雲端 API、圖片／影片 Provider、Canon 升格或正式狀態自動寫入。

## 選定模型與執行方式

首選：

```text
modelRepository = Qwen/Qwen3-8B-GGUF
modelFile = Qwen3-8B-Q5_K_M.gguf
format = GGUF
quantization = Q5_K_M
runtime = llama.cpp CLI direct process
reasoning = off
context = <= 8192 tokens for the Pilot
```

理由：模型大小與 GTX 1080 Ti 11GB／32GB RAM 相容，Q5_K_M 比 Q4 保留更多品質，同時仍
保留足夠空間給 context cache（上下文快取）。實際 GPU 相容性、速度與記憶體占用必須安裝後
量測，不能只根據檔案大小宣稱通過。

回退：已驗證的 Qwen3-1.7B Q8_0 只可作 smoke test（冒煙測試）與故障診斷，不作正式品質
證據。Embedding／Reranker 不具生成能力，不得拿來演 NPC。

## 無伺服器邊界

- 只允許 `llama-cli` 或等價直接程序呼叫。
- 不得啟動 `llama-server`、Ollama server、LM Studio server 或任何 port listener。
- Kotlin 以 `ProcessBuilder` 執行明確設定的本機可執行檔與模型路徑。
- 路徑由環境變數或未追蹤的本機設定提供；不得把使用者絕對路徑寫進 Git 證據。
- 不得下載或執行模型 Repository 中的任意程式碼；只接受官方 GGUF 權重與官方 llama.cpp
  發行檔，並核對 Hash。

## 模型可見資料

允許：

- `NpcActorRequest` 中的角色可見／自有資料；
- `NpcActorPhysicalPrompt.allowedPhysicalActionTypes`；
- `NpcActorPhysicalPrompt.visiblePhysicalTargetIds`；
- 第一次失敗後的 `NpcActorSafetyRepairRequest`；
- 嚴格輸出 Schema 與固定產生規則。

禁止：

- `NpcActorPhysicalEngineContext`；
- physical archetype、質量、尺寸、承重、重力、障礙座標與其他 engine-owned 測量；
- 作者秘密、其他角色私有記憶／信念、director goal、完整 Canon 或完整資料庫；
- API Key、系統環境秘密、本機使用者路徑與模型的 private reasoning（私有思考過程）。

## 生成契約

模型必須輸出一個符合既有 `NpcActorPhysicalResponse` 的 JSON object，不得輸出 Markdown、
說明文字或思考內容。執行器必須：

1. 使用 `--json-schema-file` 約束輸出；
2. 關閉 reasoning；
3. 使用 output file，停用 prompt 顯示與 timing 輸出；
4. 設定固定最大 token、context、逾時與輸出 byte 上限；
5. 非零退出、逾時、缺檔、空檔、無效 JSON、未知欄位或識別不符時 fail closed；
6. 以 Kotlin strict parser 再解析，不信任模型自行宣稱成功。

## 安全閉環串接

```text
actor-visible prompt
→ local llama.cpp generation attempt 1
→ NpcActorResponseValidator
→ NpcActorPhysicalBindingValidator
→ PhysicalAffordanceValidator
→ GameMasterResolutionService
→ time budget
→ accepted candidate / sanitized repair request / final rejection
```

若第一次失敗，只可把 `NpcActorSafetyRepairRequest` 加入第二次 Prompt。第二次仍失敗就停止。
不得把原始 engine finding、engine context 或隱藏測量回送模型。

## 允許修改

- `PLANS.md`、`PROJECT_STATE.md`、`AGENTS.md`、`.codex/project_controller.md`
- 本工作包與 `docs/npc_actor/NPC_ACTOR06_*`
- 新的本機 NPC Actor model／service／CLI 檔與直接相關 `Main.kt`
- 直接相關 tests、Schema、非 Canon samples、Hash 與安裝契約
- `.gitattributes`（只為新 JSON／文字證據固定 LF）

## 禁止修改

- Migration、Repository、`StateReducer`、`MultiTurnRuntime`、正式 SQLite transaction
- Chenxing Canon、Setting Pack、正式 Memory／Knowledge、SCREEN-01 封存成品
- NPC-ACTOR-03／04／04A 已封存答案與人工／內部評審
- 圖片、影片、音訊、下一章、Web、port、Docker、OpenAI SDK 或 Provider API

## 必測案例

- 正常模型輸出可被 strict parser 與既有 Validator 接受為候選。
- 模型輸出未知欄位、非 JSON、錯誤 request／actor id 時拒絕。
- 程序逾時、非零退出、輸出過大或缺少 output file 時拒絕。
- Prompt 不含 engine context、作者秘密、其他角色私有資料或本機路徑。
- 第一次失敗只能收到 sanitized repair request；第二次仍不安全時最終拒絕。
- 模型成功也不會寫 SQLite、Canon、Memory 或 Knowledge。
- 未設定本機執行器／模型時，CLI 給出明確錯誤，不自動下載或改用雲端。

## 真實 Pilot

至少執行兩種非 Canon 證據：

1. 一個可由模型首次或一次修訂後通過的短對白／行動情境；
2. 一個物理或世界條件不安全、即使模型嘗試仍被既有 Validator 拒絕的情境。

保存：模型身分、權重 SHA-256、執行器版本、硬體摘要、Prompt hash、原始 response hash、
strict parse 結果、安全閉環結果、耗時、生成速度與誠實品質結論。不得保存秘密或絕對路徑。

## 驗證與完成條件

- 官方來源、模型檔、runtime archive 與 SHA-256 均可核對。
- focused unit／CLI／safety-loop tests 通過。
- NPC Actor／Physics related tests 與完整 Gradle tests 不退化。
- JSON、Schema、Hash、秘密、絕對路徑、port listener 與 Git scope 檢查通過。
- 權重與 runtime 不進 Repository，Git 只保存可重建契約與證據。
- 普通 Commit／Push 到 `codex/npc-actor06-local-model-pilot`，不 force push。
- `PROJECT_STATE.md` 誠實記錄本機模型品質、速度、失敗與下一個 Gate。

## 完成後狀態

若全部通過：

```text
NPC_ACTOR06_LOCAL_MODEL_TECHNICAL_PASS / FORMAL_RUNTIME_AUTOMATION_NOT_AUTHORIZED
```

這只證明本機模型能在安全外殼內產生候選，不代表它等同 GPT、不代表正式 NPC 已上線，也不
授權自動推進世界。正式 Runtime 自動調用、模型品質冠軍或雲端比較必須另開工作包。
