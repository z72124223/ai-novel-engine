# Pilot-1 Context Relevance Floor

## 0. 問題摘要

Context Metadata Normalization 已通過工程驗收：

- final context：24350 / 24576 bytes
- `otherMetadata`：16997 → 2 bytes
- `contextBudgetMetadata`：1460 → 859 bytes
- hash 與 actual bytes 正確
- 150 tests 全數通過

但正式 Turn 8 預覽仍有重大品質風險：

- retrieved memories：0 / 15
- knowledge claims：0 / 7
- relevant settings：0 / 24
- setting bytes：2 / 8192

這代表 ContextPacket 雖然「合法」，卻可能沒有足夠的人物記憶、信念與相關設定供 AI 判斷。這會使角色逐漸失去深度，並使 Pilot 無法驗證記憶召回、知識邊界與設定運用。

本階段只做：

1. 建立 turn-critical semantic coverage。
2. 在裁切前保留真正與本回合有關的 memory / knowledge / setting constraints。
3. 壓縮較低價值的重複協議與歷史資訊。
4. 使用相同 Turn 8 輸入在 DB 複本上完成 prepare / audit / abort。
5. 不執行正式 Turn 8。

## 1. 禁止事項

- 不提高 `maxContextBytes=24576`。
- 不降低 forbidden reveals、hard world rules、continuity constraints 的優先級。
- 不縮短或改寫正式 Turn 8 玩家輸入。
- 不針對特定 session、memory ID、entity ID 或中文句子寫硬編碼特例。
- 不修改正式 Pilot DB 的故事狀態。
- 不執行正式 Turn 8 prepare / resolve。
- 不修改 migration 001 至 005。
- 不新增 migration。
- 不刪除正式 memories、knowledge、facts、settings、turn logs 或 secrets。
- 不新增 Android、Web、API、server、port、Rust、Docker、embedding、向量資料庫或多代理框架。
- 不允許「為了湊數」載入無關 memory、knowledge 或 setting。
- 不允許 recent fact / event / summary 冒充 recalled memory。

## 2. Git 與基線

先封存 Metadata Normalization：

```bat
git status
gradlew.bat clean test --no-daemon
git diff --check
git add .
git commit -m "fix: normalize final context packet metadata"
git tag -a pilot-1-context-normalized -m "Normalized context schema and audit sidecars"
```

建立分支：

```bat
git switch -c fix/pilot1-context-relevance-floor
```

正式狀態必須保持：

- DB：`data/pilot1-rerun.db`
- SESSION_ID：`session-75e1b190-542c-49c0-9a5a-6b242bed8e97`
- stateVersion：7
- nextTurnNumber：8
- pendingContextCount：0
- 真人回合：7

所有 prepare / audit / abort E2E 使用 DB 複本：

`data/pilot1-context-relevance-e2e.db`

## 3. 不使用固定數量配額，改用 Semantic Requirements

不得單純規定「每回合一定塞一條記憶」或「一定塞一項設定」。

先由 deterministic `TurnSemanticPlanner`（名稱可依現有架構調整）從下列來源產生本回合 semantic requirements：

- 玩家輸入
- 當前 active characters
- 當前 scene / transition question
- recent accepted events / facts
- character current goals
- character beliefs / knowledge
- current inventory / techniques
- direct entity mentions
- explicit temporal cues，例如：
  - 剛才
  - 先前
  - 記得
  - 上次
  - 那件事
- dependency graph
- scene requirements
- world hard rules

每個 requirement 至少包含：

- requirementId
- semanticType
- source
- targetCharacterIds
- referencedEntityIds
- referencedMemoryCandidates
- referencedKnowledgeCandidates
- referencedSettingCandidates
- priority
- reason
- mustBeSatisfied
- acceptableContextPaths

`semanticType` 至少支援：

- `EXPLICIT_MEMORY_CUE`
- `RELEVANT_BELIEF`
- `REFERENCED_ITEM`
- `REFERENCED_TECHNIQUE`
- `TRANSITION_CONSTRAINT`
- `LOCATION_CONSTRAINT`
- `CHARACTER_GOAL`
- `WORLD_RULE`
- `RELATIONSHIP_STATE`
- `RECENT_EVENT_CONTINUITY`

Planner 必須 deterministic，不使用 embedding 或外部模型。

## 4. Context 優先級

### Tier 0：安全與協議，不可裁切

- protocol identity
- session / turn / state version / hash identity
- player input
- forbidden reveal safety representation
- hard world rules
- continuity constraints
- current scene identity / contract
- active character current runtime state
- current item / technique requirements、costs、limitations
- output protocol minimal schema

### Tier 1：本回合 Semantic Floor

所有 `mustBeSatisfied=true` 的 semantic requirement 所需 compact records。

例如：

- 玩家明確提到「剛才愛醬忍住低吼」，且存在可存取的 Turn 7 memory：
  - 至少一條最高相關、合法 memory 必須進入 `retrievedMemories`。
- 本回合詢問撤離：
  - location reachability、available transition、exit gate 或相關 world rule 必須存在。
- 角色決策依賴某 belief：
  - 該 belief 必須出現在 knowledge claims。
- 玩家提到某 item / technique：
  - 對應 constraints 必須存在。

Tier 1 不得被 recent summaries、次要 threads 或低相關 facts 擠掉。

### Tier 2：必要連續性

- recent accepted event/fact compact references
- relevant relationship state
- active story thread public guidance
- top additional memory / knowledge
- pending world events

### Tier 3：可選豐富資訊

- 額外 memories
- 額外 knowledge claims
- 額外 settings
- 次要 threads
- public prose
- provenance
- older summaries

裁切必須從 Tier 3 開始，再到 Tier 2。Tier 0 與滿足 Tier 1 的最小資料不可裁切。

## 5. Semantic Coverage Guard

新增 `ContextQualityGuard` 或等價服務。

在 final context 寫出前檢查：

1. 每個 `mustBeSatisfied` requirement 是否被 context 中某個 path 滿足。
2. memory requirement 必須由真正的 `retrievedMemories[].memoryId` 滿足。
3. knowledge requirement 必須由合法、角色可知的 knowledge claim 滿足。
4. referenced item / technique 必須保留 requirements、costs、limitations。
5. transition / location 問題必須保留可達性與 exit/entry constraints。
6. 不得用 recent fact 偽裝成 memory。
7. 不得用 author-only setting 滿足 character-facing requirement。
8. 不得留下 dangling reference。

若安全資料可 fit，但 semantic floor 無法 fit：

- prepare / preview fail
- error code：
  `CONTEXT_QUALITY_UNSATISFIABLE`
- 不建立可 resolve pending context
- 報告缺失 requirement、所需 bytes、可裁切候選與原因
- 不得產生「合法但內容空洞」的 ContextPacket

## 6. 預算策略

### 6.1 Hard 與 Soft Target

- hard max：24576 bytes
- soft target：22528 bytes

若在保留 Tier 0、Tier 1 後仍可行，應優先 fit 到 soft target，保留約 2048 bytes headroom。

若只能落在 22529..24576：

- 可 PASS
- 必須在 audit 中標記：
  `NEAR_HARD_LIMIT`
- 回報剩餘 bytes

不得為達 soft target 裁掉 Tier 1。

### 6.2 動態共享預算

memories、knowledge、settings、threads、recent summaries 不得各自先占固定額度。

流程：

1. 計算 Tier 0。
2. 計算 Tier 1 最小 records。
3. 將剩餘 bytes 依 Tier 2 / Tier 3 relevance 排序加入。
4. 每次加入後 canonical serialize，計算實際 UTF-8 bytes。
5. deterministic fitting。
6. 以最終 payload 產生 contextHash。

### 6.3 Compact Protocol Policy

檢查 `outputRequirements`、policy、validation hints 是否重複保存長篇自然語言。

正式 context 應使用：

- protocol version
- required output field names
- concise policy codes
-必要數值閾值

長篇規則保留在：

- AGENTS.md
- Skill
- protocol docs
- audit sidecar

但 final context 必須保留足以讓 Validator 正確解讀的協議資料。

## 7. Coverage Audit Sidecar

`explain-context-budget` 與 `audit-context` 新增：

```json
{
  "semanticCoverage": {
    "requirements": [],
    "satisfiedCount": 0,
    "unsatisfiedCount": 0,
    "satisfiedBy": [],
    "missingCriticalRequirements": [],
    "qualityStatus": "PASS | FAIL",
    "nearHardLimit": true,
    "remainingBytes": 0
  }
}
```

每個 requirement 顯示：

- ID
- type
- reason
- candidate IDs
- selected ID
- selected context path
- bytes
- omitted alternatives
- visibility check
- quality status

完整 coverage report 只存在 sidecar，不進正式 AI context。

## 8. Turn 8 特定驗收語意

不得硬編碼 ID，但相同 Turn 8 輸入應被 Planner 辨識為：

1. 對 Turn 7「愛醬忍住低吼、安靜守位」的明確記憶提示。
2. 對「是否能撤到門外」的 transition/location constraint 問題。
3. 玩家仍留在原地，因此本回合不得自動轉場。
4. 趙無咎可以依 current goal / belief 自主回答。
5. 愛醬可依被安撫與先前靜默經驗反應。

若合法可存取，至少應選入：

- 一條與 Turn 7 靜默觀察或愛醬守位直接相關的 retrieved memory；
- 至少一項與趙無咎撤離判斷相關的 belief / knowledge，若資料庫中存在；
- scene transition/location constraints。

若不存在合法 candidate，報告必須明確說明，而不是塞無關資料。

## 9. 測試

保留全部 150 tests，至少新增：

1. semantic planner deterministic。
2. 明確 memory cue 會產生 `EXPLICIT_MEMORY_CUE`。
3. 合法相關 memory 進入 retrievedMemories。
4. recent fact 不會被標成 recalled memory。
5. 角色未知 / author-only memory 不可滿足 requirement。
6. relevant belief 被選入 knowledge claims。
7. transition 問題保留 location / exit constraints。
8. referenced item/technique 保留 requirements/costs/limitations。
9. Tier 3 不會擠掉 Tier 1。
10. semantic floor 無法 fit 時回傳 `CONTEXT_QUALITY_UNSATISFIABLE`。
11. quality fail 不建立 pending context。
12. final context <=24576。
13. feasible case 優先 <=22528。
14. near hard limit 正確標記。
15. coverage sidecar read-only。
16. coverage report 不進 final context。
17. contextHash 使用 final semantic-fitted payload。
18. actualBytes 等於實際 UTF-8 bytes。
19. deterministic ordering。
20. no dangling references。
21. empty irrelevant settings 不為湊數載入。
22. Turn 8 fixture 至少包含一條合法 relevant memory。
23. Turn 8 fixture 包含 transition/location constraints。
24. Turn 8 不會自動套用 transition。
25. forbidden secret 不洩漏。
26. 正式 Turn 1 至 Turn 7 evidence 不被修改。

## 10. 複本 E2E

複製：

`data/pilot1-rerun.db`
→
`data/pilot1-context-relevance-e2e.db`

使用完全相同 Turn 8 玩家輸入：

```text
我摸了摸愛醬的頸側，讓牠知道我記得牠剛才忍住低吼守著我。

「趙無咎，既然安靜時它沒有追過來，我們能不能不碰它，慢慢退到驛亭門外？」

我仍留在原地，等他回答。
```

執行：

1. explain-context-budget
2. prepare-turn
3. audit-context
4. 不生成 result
5. abort-pending-turn

驗收：

- explain PASS
- prepare PASS
- audit PASS
- qualityStatus PASS
- context <=24576
- 優先達到 <=22528；若未達，需說明 Tier 0 / Tier 1 bytes
- 至少一條合法 Turn 7 相關 memory 被選入
- 合法相關 knowledge 若存在則被選入
- transition/location constraints 存在
- retrieved memory 有真實 memoryId
- recent fact 沒有冒充 memory
- setting 可為 0，但 audit 必須證明沒有未滿足的 referenced-setting requirement
- hash valid
- pending 可 abort
- copy DB stateVersion 仍為 7
- nextTurnNumber 仍為 8
- 沒有正式 turn log / state delta

## 11. 正式 DB 只讀驗證

對：

`data/pilot1-rerun.db`

只執行：

- explain-context-budget
- pilot-preflight

回報：

- predicted final bytes
- soft/hard target status
- semantic requirements
- selected memory IDs
- selected knowledge IDs
- setting requirements 是否存在
- transition/location coverage
- omitted counts
- remaining bytes
- qualityStatus
- stateVersion=7
- nextTurnNumber=8
- pendingContextCount=0

不得在正式 DB 執行 prepare 或 resolve。

## 12. 文件

更新：

- README.md
- AGENTS.md
- docs/ARCHITECTURE.md
- docs/TURN_PROTOCOL.md
- docs/VALIDATION_RULES.md
- docs/CONTEXT_PACKET_NORMALIZATION.md
- docs/CONTEXT_BUDGET_FORENSICS.md
- docs/PILOT1_FINDINGS.md

新增：

- `docs/CONTEXT_RELEVANCE_FLOOR.md`

說明：

- semantic requirements
- Tier 0..3
- memory / knowledge / setting coverage
- semantic quality guard
- soft target / hard max
- dynamic shared budget
- coverage sidecar
- 為何「合法但 0 memory / 0 knowledge」仍可能不合格
- 為何 setting count 不應強制湊數

## 13. 最終回報

執行：

```bat
gradlew.bat clean test --no-daemon
git diff --check
```

回報：

1. VERDICT
2. branch / status
3. tests 結果
4. Turn 8 semantic requirements
5. selected memories / knowledge / settings
6. rejected candidates 與原因
7. Tier 0 / Tier 1 / Tier 2 / Tier 3 bytes
8. before / after bytes
9. soft target / hard max 狀態
10. qualityStatus
11. transition/location coverage
12. safety fields
13. hash / actualBytes
14. 複本 E2E
15. 正式 DB read-only preflight
16. stateVersion / nextTurnNumber / pending count
17. 明確確認未正式執行 Turn 8
18. 尚未解決問題
