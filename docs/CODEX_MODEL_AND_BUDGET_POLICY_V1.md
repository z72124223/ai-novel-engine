# Codex 模型與成本政策 V1

Status: `ACTIVE_REBOOT_POLICY`

Date: 2026-07-11

## 1. 目的

本政策修正「所有 Codex 工作都使用最高模型／最高推理」的做法。

最高等級不等於每個任務的最佳選擇。已經有清楚藍圖的普通實作，通常更需要穩定執行、精確 Diff、測試與成本控制，而不是最大推理時間。

## 2. 預設模型

主執行者：

```text
目前 Codex CLI 可選的 GPT-5.6 Codex coding model
reasoning_effort = medium
```

如果介面沒有完全相同的名稱，選擇目前主流 GPT-5.6 Codex 程式模型，不選 Max／Pro／最高昂模式作為預設。

## 3. 何時升到 High

只在下列情況使用 `HIGH`：

- 修改 SQLite Migration、Transaction 或正式狀態寫入；
- 修改 Validator、StateReducer、Knowledge Boundary、contextHash 或 stateVersion；
- 跨越多個核心模組的架構衝突；
- 難以重現的資料一致性 Bug；
- 安全、權限、Canon 或不可逆批准邊界；
- Git 歷史／資料遷移／高風險合併；
- 最終整合審查；
- Medium 已完成兩次有證據的嘗試，仍存在同一核心阻塞。

升級必須在開發日誌記錄原因。

## 4. 何時可用 Low／快速模型

可用較快或較便宜的模型處理：

- 檔案搜尋；
- 類別／測試對應盤點；
- 重複格式整理；
- 文件摘要；
- 測試失敗分類；
- 產生非核心樣板；
- 比對日誌與統計。

但不得降低驗證要求。

## 5. 不該使用 LLM 的工作

優先使用腳本或現有工具：

- SHA-256；
- JSON Parse；
- JSON Schema 驗證；
- 路徑存在性；
- Git Diff；
- 行數、檔案數、測試數統計；
- 格式檢查；
- 可重複的 Benchmark 計算。

禁止用高階模型反覆閱讀同一份輸出，完成可以由程式確定的工作。

## 6. Multi-Agent 成本策略

允許最多四個 Subagents，但按任務啟動，不常駐。

建議：

| Agent | 工作 | 模型／強度 |
| --- | --- | --- |
| Main Orchestrator | 整合、寫入、決策 | GPT-5.6 Codex Medium |
| RepositoryExplorer | 搜尋、依賴、重疊 | 快速模型 Low／Medium |
| RuntimeTester | 測試、SQL 證據、重播 | Medium |
| NarrativeSystemsCritic | 自主性、節奏、停滯 | Medium；最終爭議才 High |
| CostAndComplexityAuditor | Token、時間、文件與程式膨脹 | 快速模型 Low／Medium |

Subagent 只讀結果由 Main Orchestrator 彙整。核心程式同一時間只有一個 Writer。

## 7. Max／最高昂模式

預設禁止。

只有全部成立時才可使用：

1. 問題阻擋 REBOOT-01 的核心成功條件；
2. 已有最小重現；
3. Medium 與 High 各至少有一次有證據的失敗；
4. 問題不能安全拆小；
5. 使用理由與預期收益已寫入日誌；
6. 只針對該問題使用，不把後續普通工作都留在 Max。

## 8. 預算記錄

每個內部階段至少記錄：

```text
model
reasoning effort
subagent count
elapsed time
commands/tests run
files changed
retries
known failures
```

若 Codex 介面能提供 Token／成本，追加記錄；若不能，不得捏造數字。

## 9. 升降級規則

```text
Low/fast discovery
→ Medium implementation
→ targeted tests
→ High only for proven hard boundary
→ return to Medium after resolution
```

禁止：

```text
整個專案從頭到尾 High/Max
```

## 10. REBOOT-01 指定

REBOOT-01：

- 主控：GPT-5.6 Codex `MEDIUM`；
- Repository／文件探索 Subagents：Low 或 Medium；
- 戲劇與角色 Critic：Medium；
- SQL／Transaction／Knowledge Boundary 改動：High；
- 最終 Integrator Review：High；
- Max：除非符合第 7 節，否則不得使用。

## 11. 成本停止訊號

若出現以下任一情況，Codex 必須先簡化流程，而不是直接升模型：

- 同一文件被完整重讀三次以上；
- 同一類型狀態文件持續增加；
- 兩個連續工作日沒有可運行的新證據；
- Critic 重複給相同意見；
- 測試已能確定結果，Agent 仍反覆推理；
- 子代理輸出大量重複內容；
- 工作包超出垂直切片範圍。

優先措施：縮小 Context、使用精確搜尋、寫腳本、拆小 Bug、刪除重複任務，而不是增加推理強度。