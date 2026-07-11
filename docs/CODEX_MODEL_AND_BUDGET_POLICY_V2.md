# Codex 模型、Subagent 與成本政策 V2

Status: `ACTIVE_REBOOT_POLICY`

Date: 2026-07-11

Supersedes for current execution:

- `docs/CODEX_MODEL_AND_BUDGET_POLICY_V1.md`

## 1. 決策摘要

REBOOT-01 不使用「全程最高等級」策略。

主執行者固定採用：

```text
model = gpt-5.6-sol
reasoning = medium
```

原因：

- REBOOT-01 是長時間、多階段、工具密集的工程工作；
- 主要難度在於遵守既有藍圖、修改、測試與累積可運行證據；
- 全程 High／Max 會顯著增加 Token、等待與成本，未必提高普通實作正確率；
- 高風險節點可臨時升級，解決後立即回到 Medium。

## 2. 官方模型定位對應

依目前 OpenAI Codex 模型文件：

- `gpt-5.6-sol`：旗艦模型，適合複雜、模糊、多步驟程式工作、研究與工具使用；
- `gpt-5.6-terra`：日常工作平衡型，成本較低，適合探索、讀取、測試分析與輔助工作；
- `gpt-5.6-luna`：快速且成本最低，適合清楚、重複、結構化的低風險工作；
- `gpt-5.5`：前一代模型，不再作本專案新工作預設。

官方也建議從預設 Reasoning 開始，只有任務需要時才提高；Medium 是日常平衡，High 適合複雜邏輯與邊界，Max／Ultra 應保留給極難問題。

官方來源：

- https://developers.openai.com/codex/models/
- https://developers.openai.com/codex/subagents/
- https://developers.openai.com/codex/config-reference/

啟動時若模型目錄已變更，Codex應查官方文件並記錄替代模型；不得默默退回 5.5。

## 3. 主執行模型

### 3.1 正常 Stage

```text
gpt-5.6-sol / medium
```

用於：

- Stage 整合與決策；
- Kotlin 實作；
- CLI；
- 一般資料模型；
- 非核心 Service；
- 測試補強；
- Web 垂直切片；
- Commit 與階段總結。

### 3.2 臨時 High

只有下列工作可切換：

```text
gpt-5.6-sol / high
```

- SQLite additive Migration；
- Transaction 與正式狀態寫入；
- Validator／StateReducer；
- Knowledge Boundary；
- `contextHash`／`stateVersion`／idempotency；
- 複雜資料遷移或 Git 歷史；
- 高風險安全與權限；
- Medium 有最小重現但一次修復失敗的核心 Bug；
- Stage 7 最終 Integrator Review。

每次升級必須在開發日誌／Stage 報告記錄：

```text
reason
scope
start point
result
return-to-medium point
```

不得一旦升級就讓後續所有工作留在 High。

### 3.3 XHigh／Max／Ultra

預設禁止。

只有全部成立才可臨時使用：

1. 問題直接阻擋 REBOOT-01 完成；
2. 已有最小重現；
3. Medium 與 High 各至少一次有證據的失敗；
4. 問題不能安全拆小；
5. 不涉及等待使用者決定的 Canon／美術／付費事項；
6. 使用理由寫入日誌；
7. 只針對該問題。

Ultra 會主動使用 Subagents，成本更高；本專案已明確配置 Subagents，因此不需要以 Ultra 作為常態入口。

## 4. Subagent 配置

允許最多四個並行子代理；只在任務可真正平行時啟動。

| Agent | 模型 | Reasoning | 權限 | 用途 |
| --- | --- | --- | --- | --- |
| `repository_explorer` | `gpt-5.6-terra` | low | read-only | 搜尋、依賴圖、現有功能、重複實作 |
| `runtime_tester` | `gpt-5.6-terra` | medium | workspace-write，但禁止改 tracked source | 跑測試、CLI、SQL／Evidence、重播與失敗分類 |
| `narrative_systems_critic` | `gpt-5.6-sol` | medium | read-only | 自主性、戲劇、吐槽、可預測性、停滯、情緒波形 |
| `cost_complexity_auditor` | `gpt-5.6-luna` | low | read-only | 重複文件、Token／時間、無效重試與複雜度 |
| `high_risk_reviewer` | `gpt-5.6-sol` | high | read-only | 核心狀態、Transaction、Knowledge、最終整合；按需啟動，取代上述一席 |

規則：

- 主 Agent 是唯一核心 Writer；
- Subagents 不得同時修改 `Models.kt`、`Main.kt`、Migration、Repository Transaction 或核心 Validator；
- 探索、評分和審查可平行；
- 寫入工作由主 Agent彙整後依序完成；
- Subagent 輸出必須精簡為證據、風險、建議，不要複製大量原文；
- 子代理失敗不得無限重啟；主 Agent接手或縮小任務。

官方 Codex 支援專案範圍 `.codex/agents/*.toml` 自訂 Agent，也提醒每個 Subagent 會增加 Token；因此本專案將 `max_threads` 固定為 4、`max_depth` 固定為 1。

## 5. 不應使用 LLM 的工作

以下優先用程式、Gradle、Git、SQLite、PowerShell、Python 或 Node：

- SHA-256；
- JSON parse；
- JSON Schema；
- 路徑存在性；
- 圖片尺寸；
- Git diff／status／log；
- 檔案／行數／測試數統計；
- Benchmark 算分；
- 重複字串與 ID 檢查；
- 測試報告解析；
- 同 Seed 指標比較。

禁止用 Sol High 反覆閱讀機器已能確定的結果。

## 6. Stage 模型路由

### Stage 0 — Reality Baseline

- 主控：Sol Medium；
- Explorer：Terra Low；
- Runtime Tester：Terra Medium；
- 成本稽核：Luna Low。

### Stage 1 — Evidence Persistence／Validation Simplification

- 一般盤點與 Model：Sol Medium；
- Migration／Transaction／正式 State：Sol High；
- High Risk Reviewer：Sol High read-only。

### Stage 2 — NPC Autonomy

- 主寫入：Sol Medium；
- 架構探索：Terra Low；
- Runtime：Terra Medium；
- Knowledge／Transaction修改：該小段升 High。

### Stage 3 — Drama Director／Critics

- 主寫入：Sol Medium；
- Narrative Critic：Sol Medium；
- 重複測試／統計：Luna Low 或腳本。

### Stage 4 — 5–10 回合垂直切片

- 主控：Sol Medium；
- Runtime：Terra Medium；
- Narrative Critic：Sol Medium；
- 不因文風不完美升 High。

### Stage 5 — Replay／Evolution

- 主控：Sol Medium；
- 批次統計：腳本／Luna Low；
- 規則取捨爭議：Sol High read-only review。

### Stage 6 — Local Web Slice

- 主控：Sol Medium；
- Explorer：Terra Low；
- Browser／integration issue 只有真實難解時升 High。

### Stage 7 — Final Integration

- 主控完成材料：Sol Medium；
- 最終只讀整合審查：Sol High；
- 修正後回 Medium 執行。

## 7. 成本記錄

每個 Stage 至少記錄：

```text
mainModel
mainReasoning
subagentsUsed
subagentModels
elapsedTime
commands
filesChanged
tests
retries
highEscalations
knownFailures
```

若 CLI 顯示 Token 或 Credits，記錄真實數字；若沒有，寫 `NOT_AVAILABLE`，不得估造。

## 8. 成本停止訊號

出現下列任一情況，先簡化，不得先升模型：

- 同一長文件完整重讀三次；
- 同類型規格又新增一層；
- 同一 Critic 意見連續重複；
- 測試已能判定結果，Agent仍推理；
- Subagent輸出高度重疊；
- 兩個 Stage 沒有新增可運行證據；
- 同一 Bug 重試但沒有建立最小重現；
- Context大量包含歷史路線而非目前 Stage。

優先措施：

```text
精確搜尋
→ 縮小 Context
→ 建最小重現
→ 寫腳本
→ 拆小工作
→ 關閉無效 Subagent
→ 必要時才升級
```

## 9. 專案配置

目前專案應使用：

```toml
model = "gpt-5.6-sol"
model_reasoning_effort = "medium"

[agents]
max_threads = 4
max_depth = 1
job_max_runtime_seconds = 1800
```

各專用 Agent 由 `.codex/agents/*.toml` 定義。

## 10. 最終建議

使用者只需在 Repository 根目錄啟動 Codex。主線不要選 High／Max／Ultra。

推薦：

```text
5.6 Sol / Medium
```

這是「照著完整藍圖一路執行」的最佳成本／能力平衡。High 是手術刀，不是整場施工的照明燈。