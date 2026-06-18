# Pilot-1 Stabilization：阻塞修補與重新預檢

## 0. 範圍

目前 Pilot-1 稽核為 FAIL，已確認三個阻塞點：

1. 只有 1 個真人輸入回合，尚未達到 12 回合驗證前提。
2. Turn 1 context 為 `26963 / 24576 bytes`，超過硬上限。
3. Setting pack 匯入的角色沒有建立初始 runtime `character_states`，導致角色心智、情緒與目標無法完整稽核。

本階段只修復以上阻塞點並補充只讀稽核能力。

不得進入 MVP-6。
不得自行替玩家跑完 12 回合。
不得以增加 context 上限掩蓋超預算問題。

## 1. 禁止事項

- 不新增 Android、Web UI、HTTP server、port、外部 API。
- 不加入 Rust、Docker、embedding、向量資料庫或多代理框架。
- 不修改 `001` 至 `005` migration。
- 預設不新增 migration；若現有 schema 確實無法完成，必須先在回報中證明原因，再新增 additive `006_pilot_stabilization.sql`。
- Codex / AI 不得直接寫 SQLite。
- 不得繞過 Validator、transaction、contextHash、stateVersion。
- 不得刪除或提高 `maxContextBytes=24576`。
- 不得降低 forbidden reveal、world hard rule、continuity、item/technique cost 等安全資料優先級。
- 不得用硬編碼刪除某個 Pilot entity 來通過預算。
- 不得重用目前只有一回合且已超預算的 session 作正式 12 回合證據。
- 不得在本修補中處理一般敘事風格問題；只記錄，不大改敘事引擎。

## 2. Git 基線

目前應位於：

`pilot/real-use-vertical-slice`

先執行：

```bat
git status
gradlew.bat clean test --no-daemon
git diff --check
```

若 Pilot skills、Pilot 規格與 `docs/PILOT1_FINDINGS.md` 尚未提交，先提交：

```bat
git add .agents docs/PILOT1_REAL_USE_VALIDATION.md docs/PILOT1_TURN_LOG.md docs/PILOT1_FINDINGS.md
git commit -m "chore: add Pilot-1 workflow and initial findings"
```

確認不會加入：

- data/*.db*
- out/*.json
- build/
- .gradle/
- 暫存 JDK

建立修補分支：

```bat
git switch -c fix/pilot1-stabilization
```

## 3. 修補 A：全域 Context 硬預算

### 3.1 問題

目前各區段局部預算可能通過，但最終完整 JSON 仍超過 `24576 bytes`。

### 3.2 要求

新增或修正最終階段的 `FinalContextBudgetEnforcer`（名稱可依現有架構調整）。

執行順序：

1. 組裝完整 ContextPacket。
2. 套用區段預算。
3. 對完整 payload 做穩定序列化。
4. 以 UTF-8 實際 bytes 計算最終大小。
5. 若超過上限，依優先級移除最低優先的可選項。
6. 重新序列化與計算，直到：
   - `actualBytes <= maxContextBytes`；或
   - 只剩不可裁切資料仍超限，此時 prepare-turn 必須 fail。

不得依字元數或估算 bytes。

### 3.3 不可裁切欄位

至少包含：

- protocolVersion
- contextId / sessionId / turnNumber / baseStateVersion
- playerInput
- current scene identity 與 scene contract
- active character 最新 runtime state
- forbidden reveals 的安全表示
- world hard rules
- continuity constraints
- 當前 item / weapon / technique 的 requirements、costs、limitations
- pending world event 的必要安全摘要
- output requirements
- knowledge boundary 所需的限制資料

### 3.4 可裁切順序

由最先裁切到最後裁切：

1. 低相關、非當前使用的 relevant settings
2. 次要 story threads
3. 低相關 knowledge claims
4. 低分 memories
5. 較舊 recent turn summaries
6. 重複 public descriptions
7. 非必要 metadata / explanatory text

任何裁切必須保留 entity ID 與必要引用一致性，不得留下 dangling reference。

### 3.5 Setting 預算

同時維持：

- `maxContextBytes = 24576`
- `maxSettingBytes = 8192`

設定區不得超過自己的上限，也不得讓總 context 超限。

實際設定可用額度必須受剩餘全域預算約束，而不是永遠固定使用完整 8192 bytes。

### 3.6 actualBytes 穩定

若 `actualBytes` 本身存在 JSON 內，需迭代計算直到欄位值與最終 UTF-8 payload 大小一致。

`contextHash` 必須以最終裁切後的 canonical payload 產生，沿用既有排除/佔位規則，避免循環。

### 3.7 無法滿足預算

若不可裁切核心資料本身超限：

- prepare-turn fail
- error code：`CONTEXT_BUDGET_UNSATISFIABLE`
- 輸出 per-section byte report
- 不建立可 resolve 的 pending context
- 不修改正式故事狀態

不得靜默截斷字串或產生無效 JSON。

## 4. 修補 B：Setting Pack 角色初始 Runtime Template

### 4.1 Setting Pack Character 擴充

角色 canon 可新增：

```json
{
  "initialRuntimeTemplate": {
    "initialLocationId": "string",
    "initialGoal": "string",
    "dominantEmotion": "string",
    "emotionState": {
      "emotion-name": 0
    },
    "availableActions": [],
    "relationshipDefaults": {},
    "initiativeLevel": 0
  }
}
```

這是「新 session 的初始模板」，不是 live runtime state，也不得覆蓋 canon personality。

驗證：

- emotion values：0..100
- initiativeLevel 使用現有合法範圍
- initialLocationId 必須存在
- initialGoal 不得是 story thread director goal
- availableActions 不得包含未持有 item、未知 technique、未知 secret
- author-only truth 不得進入模板
- 模板不得直接建立已揭露 secret
- 任何會在開場 scene 出現的角色必須有合法 template

### 4.2 Session 初始化

`start-session` 必須在單一 transaction 中：

1. 建立 session。
2. 建立第一個 scene instance。
3. 找出開場 scene 的 active/participating characters。
4. 依 `initialRuntimeTemplate` 建立缺少的 `character_states`。
5. 初始化 location、goal、emotion、available actions、relationship defaults。
6. 驗證角色位置與開場 scene/location 一致。
7. 驗證 initial item owner/location 與 technique prerequisites。
8. 任一角色缺少必要 template 時整個 start-session fail。

錯誤 code 至少包含：

- `MISSING_INITIAL_RUNTIME_TEMPLATE`
- `INVALID_INITIAL_CHARACTER_LOCATION`
- `INVALID_INITIAL_CHARACTER_ACTION`
- `INITIAL_RUNTIME_KNOWLEDGE_LEAK`

不得建立空白、全零或通用工具人 fallback state。

### 4.3 重複與安全

- 同一角色 state 不得重複初始化。
- 重試 start-session 不得產生部分 session。
- 失敗時 session、scene instance、character states 全部 rollback。
- `show-character-mind` 對開場 active characters 必須立即可用。
- 非開場角色不需要在 start-session 全部具現化；其首次進入 scene 時需依同一模板規則初始化，或在缺失時拒絕其出場。

## 5. 修補 C：只讀 Audit / Export CLI

新增以下 CLI，名稱可配合現有風格，但功能不可省略。

### 5.1 audit-context

```bat
gradlew.bat run --args="audit-context --file out\turn_context.json --out out\turn_context_audit.json"
```

輸出：

- exact UTF-8 total bytes
- max bytes
- per-section bytes
- setting bytes
- memory / knowledge / summary counts
- mandatory field presence
- truncation applied
- omitted entity IDs 與原因
- contextHash verification
- PASS / FAIL

必須 read-only。

### 5.2 export-session-evidence

```bat
gradlew.bat run --args="export-session-evidence --db data\pilot1-rerun.db --session SESSION_ID --out out\pilot1_evidence.json"
```

輸出：

- project/session IDs
- turnNumber / stateVersion
- 每回合 player input 摘要
- narrative 摘要
- context bytes / setting bytes
- resultId / contextId
- validator retries（若可由 logs 推導）
- character goals / emotions
- memories recalled / created
- knowledge/belief changes
- scene/thread changes
- active scene count
- unrevealed secret count
- world time
- state delta counts
- transition/world event summary

不得輸出 author-only secret 的完整內容。
必須 read-only。

### 5.3 pilot-preflight

```bat
gradlew.bat run --args="pilot-preflight --db data\pilot1-rerun.db --project PROJECT_ID --session SESSION_ID --out out\pilot1_preflight.json"
```

至少檢查：

- project canon validation PASS
- exactly one active scene instance
- opening active characters 都有 runtime states
- show-character-mind 所需資料存在
- no initial knowledge leak
- current context dry run <= 24576 bytes
- setting section <= 8192 bytes
- mandatory safety fields 未裁切
- forbidden secrets 仍 unrevealed
- stateVersion / nextTurnNumber 合理
- pending context 無殘留衝突

預檢不得改正式 runtime state。若需要 prepare dry-run，必須使用 read-only preview 模式或完成後不留下 pending context。

## 6. Skills 修正

### setting-pack Skill

新增：

- 對會出場的每個角色產生 `initialRuntimeTemplate`
- validate/preview 必須阻止 template 缺失或 knowledge leak
- 匯入後建立 session 前先執行 canon validation

### story-turn Skill

新增：

- prepare 後先執行 `audit-context`
- audit FAIL 時不得生成 result
- resolve 成功後可執行 `export-session-evidence`
- 不得因 budget fail 而提高上限
- 不得替玩家自動連續執行多個回合

## 7. 測試

保留全部 96 個既有測試，至少新增：

### Context Budget

1. 最終 serialized UTF-8 bytes 不超過 24576。
2. setting section 不超過 8192。
3. 各 section 局部合法但總量超限時，global enforcer 會裁切可選內容。
4. mandatory fields 永不被裁切。
5. current weapon/technique requirements、costs、limitations 永不被裁切。
6. 低相關 setting 先於高相關 memory 被裁切。
7. 裁切後不存在 dangling IDs。
8. actualBytes 與實際檔案 bytes 一致。
9. hash 基於最終 payload。
10. mandatory payload 本身超限時 prepare fail，DB 不變且不留下可 resolve pending context。
11. UTF-8 中文 bytes 計算正確。

### Runtime Template

12. 開場角色有 template 時 start-session 會建立 character state。
13. 缺少 template 時 start-session 全部 rollback。
14. author-only secret 出現在 template 時被拒絕。
15. invalid location 被拒絕。
16. unknown item/technique action 被拒絕。
17. emotion 超界被拒絕。
18. show-character-mind 在 session start 後可用。
19. 重試不重複初始化 state。
20. 非開場角色首次合法入場可初始化；缺失 template 時拒絕其出場。
21. initial goal 不得直接複製 director goal。

### Audit

22. audit-context read-only。
23. export-session-evidence read-only。
24. pilot-preflight read-only。
25. audit 不輸出 author-only secret 全文。
26. preflight 能抓到 missing character state。
27. preflight 能抓到 context over budget。
28. preflight PASS 時 DB counts/stateVersion 前後一致。

既有 MVP-1 至 MVP-5 測試必須全部通過。

## 8. Stabilization E2E

使用新的 DB：

`data\pilot1-stabilization-e2e.db`

不得使用舊 Pilot DB。

流程：

1. init DB
2. 使用一份含至少 3 角色且都有合法 `initialRuntimeTemplate` 的 setting pack
3. validate
4. preview
5. apply
6. validate-project-canon
7. start-session
8. show-character-mind 檢查所有開場角色
9. prepare 一個 dry-run turn
10. audit-context
11. pilot-preflight
12. resolve 一個合法測試回合
13. export-session-evidence

驗收：

- context <= 24576
- setting <= 8192
- active characters 都有 runtime state
- forbidden secrets 未進 knowledge
- prepare/audit/preflight 不改正式 state
- resolve 後只增加一次 stateVersion
- evidence export 可讀且不洩漏 author-only secret

另建立 scale fixture，使未裁切原始 payload 明確大於 24576，證明 final enforcer 能裁到合法範圍，且安全欄位完整。

## 9. 真實 Pilot 重新開始

Stabilization E2E 通過後：

1. 不要繼續使用舊 Pilot session。
2. 使用原真實作品的 setting pack，補齊 `initialRuntimeTemplate`。
3. 建立全新 DB：
   `data\pilot1-rerun.db`
4. validate / preview / apply / validate-project-canon。
5. start-session。
6. 執行 `pilot-preflight`。
7. 只有 preflight PASS 才回報新的 SESSION_ID。
8. 此時停止，不得由 Codex 自行執行 12 回合。

使用者之後必須親自提供至少 12 個玩家輸入回合。

每回合：

```text
$story-turn
→ audit-context
→ generate result
→ resolve
→ export-session-evidence
→ append PILOT1_TURN_LOG
```

## 10. 文件

更新：

- README.md
- AGENTS.md
- docs/ARCHITECTURE.md
- docs/DATA_MODEL.md
- docs/TURN_PROTOCOL.md
- docs/VALIDATION_RULES.md
- docs/SETTING_PACKS_AND_DEPENDENCIES.md
- docs/PILOT1_REAL_USE_VALIDATION.md
- docs/PILOT1_FINDINGS.md

新增：

- `docs/PILOT1_STABILIZATION.md`

說明：

- exact global context budgeting
- final fitting order
- unsatisfiable budget behavior
- initial runtime template
- session character initialization
- audit-context
- export-session-evidence
- pilot-preflight
- 為何舊一回合 session 不可當作重新驗收證據

AGENTS.md 只加入長期規則並引用文件，不複製全文。

## 11. 驗收與回報

執行：

```bat
gradlew.bat clean test --no-daemon
git diff --check
```

最終回報：

1. VERDICT：PASS / FAIL
2. branch / commit status
3. 新增與修改檔案
4. tests 總數與結果
5. context budget 演算法摘要
6. scale fixture 裁切前後 bytes
7. 不可裁切欄位驗證
8. runtime template schema 摘要
9. start-session 初始化結果
10. missing/invalid template rollback
11. audit-context 結果
12. export-session-evidence 結果
13. pilot-preflight 結果
14. stabilization E2E context/setting bytes
15. forbidden secret / knowledge boundary
16. 舊 Pilot session 如何處理
17. 新 `pilot1-rerun.db` 與 SESSION_ID
18. 明確確認 Codex 未自行執行 12 回合
19. 尚未解決問題

只有以上全部 PASS，才交還使用者開始 12 個真人輸入回合。
