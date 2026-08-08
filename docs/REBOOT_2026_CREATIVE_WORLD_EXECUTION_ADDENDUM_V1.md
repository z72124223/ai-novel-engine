# AI Novel Engine — 2026 創作、世界觀與執行補充規格 V1

Status: `ACTIVE_MANDATORY_REBOOT_ADDENDUM`

Date: 2026-07-11

## 1. 目的

本文件補足 `REBOOT_2026_PLAYABLE_DEPTH_BLUEPRINT_V1` 尚未明確落地的三個要求：

1. 主角的毒舌／吐槽／觀察型幽默；
2. 降低觀眾過度預測劇情的能力，同時保持轉折公平、有因果；
3. 對現有塵行世界觀、正式來源、SQL 實際載入、Runtime 使用情況做真實稽核與候選修補，而不是只再寫規劃文件。

本 Addendum 是 REBOOT-01 的強制執行內容。Codex 不得只引用它而不產生實際 Runtime、SQL、Scenario 或測試證據。

## 2. 主角語氣：毒舌、吐槽與高觀察力

### 2.1 設計目的

主角的吐槽能力不是為了把作品變成純喜劇，而是：

- 讓角色對常見危險、荒謬命令、官僚話術、反派自我合理化與「明顯不對勁」保持敏感；
- 避免恐怖片式「觀眾早知道危險，但角色為了推劇情硬送頭」；
- 讓主角能說出讀者心中的疑問，降低作者操控感；
- 用笑點建立親近感，再讓後續代價更有重量；
- 將幽默變成壓力調節、自我保護、智力與人格的一部分。

### 2.2 語氣分層

主角的語氣至少分成三種模式：

```text
DAILY_QUIP
DANGER_OBSERVATION
GRIEF_RESTRAINT
```

- `DAILY_QUIP`：日常摩擦、自嘲、觀察型笑點、人際吐槽。
- `DANGER_OBSERVATION`：指出明顯陷阱、矛盾、風險與不合理要求；不能因吐槽就獲得超自然全知。
- `GRIEF_RESTRAINT`：真正死亡、創傷、背叛或他人痛苦發生時，降低笑料密度；可用苦笑、壓抑或自我防衛，但不得消解受害者的重量。

### 2.3 限制

禁止：

- 每一句都吐槽，讓角色只剩梗；
- 用吐槽替代選擇、行動與代價；
- 在嚴重創傷場景嘲弄受害者；
- 讓主角因為「知道套路」而變成作者全知分身；
- 讓所有其他角色降智，只為襯托主角聰明；
- 讓幽默永遠沒有社會代價。

吐槽可以造成後果，例如：

- 得罪權威；
- 讓盟友覺得不被尊重；
- 在緊張時刻救場；
- 暴露主角的恐懼；
- 形成戀愛與友情中的獨特默契；
- 被對手利用為可預測的防衛機制。

### 2.4 REBOOT-01 落地

在非 Canon 垂直切片中，將下列內容建立為 Scenario-level candidate，而不是直接改 AUTHOR_LOCKED Canon：

```text
protagonistVoiceProfile
sarcasmIntensity
selfDeprecation
riskAwareness
empathyGuard
seriousSceneSuppression
socialCostRules
```

至少測試：

1. 一次自然日常吐槽；
2. 一次主角識破明顯危險或不合理行動；
3. 一次吐槽造成關係代價；
4. 一次嚴肅場景中主角收住笑話；
5. Critic 能區分「有角色味的吐槽」與「作者站在劇情外講解」。

## 3. 觀眾預測、套路與公平轉折

### 3.1 問題定義

觀眾能預測部分劇情不是錯誤。問題是：

- 所有轉折都只有第一層答案；
- 角色明明看得出危險，卻無理由作死；
- 敵人、盟友和世界永遠照最常見套路反應；
- 伏筆沒有第二層後果；
- 反轉只靠隱藏資訊或隨機事件；
- 觀眾的推理永遠比角色聰明很多。

### 3.2 Audience Expectation Ledger

REBOOT-01 必須建立或以資料結構表示：

```text
AudienceExpectationLedger
```

至少包含：

```text
expectationId
sceneId
expectedOutcome
whyAudienceMayExpectIt
plantedClues
characterExpectations
actualOutcome
reversalType
causalBasis
fairnessEvidence
secondOrderConsequence
payoffStatus
```

### 3.3 允許的轉折方式

1. **預期成真，但代價不同**：觀眾猜到事件，沒猜到代價或關係後果。
2. **角色主動改變結果**：主角或 NPC 因成長、恐懼、愛、利益做出非預設選擇。
3. **第二層因果**：第一個答案正確，但導致更意外、且前文可追溯的後果。
4. **視角落差**：觀眾與角色擁有不同但公平的資訊，不靠作者作弊。
5. **價值衝突反轉**：真正意外的不是「誰贏」，而是角色選擇犧牲什麼。

禁止：

- 無伏筆的隨機反轉；
- 為了反轉強迫角色違反人格；
- 只藏關鍵資料，事後宣稱聰明；
- 每次都反預期，導致觀眾不再相信任何鋪陳；
- 角色集體降智的 `idiot plot`。

### 3.4 Risk Motivation Rule

任何角色進入明顯危險時，至少必須有一項可查證理由：

```text
insufficientKnowledge
urgentGoal
moralObligation
relationshipPressure
resourceConstraint
falseBelief
coercion
tradeoff
calculatedRisk
```

否則 Critic 標記：

```text
IDIOT_PLOT_RISK
```

### 3.5 Critic 擴充

`NarrativeSystemsCritic` 或等價模組至少評估：

```text
audiencePredictability
reversalFairness
characterIntelligence
setupPayoffIntegrity
tropeAwareness
secondOrderConsequence
```

低預測性不是唯一目標。公平、角色驅動與情緒影響優先於純粹驚嚇。

## 4. 世界觀真實稽核與候選修補

### 4.1 原則

不再把「formal JSON 存在」當成「引擎已使用」。

Codex 必須分開確認：

```text
文件中有設定
SQL 已載入
Runtime Context 會取到
角色／場景／事件實際使用
測試 Scenario 能觸發
```

### 4.2 強制輸出

REBOOT-01 在 Stage 0 後、正式 NPC／Drama 實作前，必須建立：

```text
docs/reboot/REBOOT01_WORLD_AND_CHARACTER_REALITY_AUDIT.md
```

並產生機器可讀資料，例如：

```text
out/reboot01/world_audit/world_reality_inventory.json
```

### 4.3 稽核來源順序

1. `query-world-source` 對正式 SQL Archive 的查詢結果；
2. `data/story.db` 內實際表與資料；
3. `world/chenxing/formal/`；
4. 目前有效 Setting Pack／Import evidence；
5. Runtime Seed／Session 初始化；
6. Context Packet 真正取得的資料；
7. 歷史文件只作證據，不作當前 Runtime 事實。

### 4.4 每項資料狀態

```text
AUTHOR_LOCKED
LOADED_IN_SQL
FORMAL_NOT_LOADED
TEXT_ONLY
RUNTIME_UNUSED
CONTRADICTED
MISSING_REQUIRED
PROVISIONAL_CANDIDATE
TEST_ONLY
SUPERSEDED
```

### 4.5 稽核面向

至少檢查：

- 年代、時間線與事件順序；
- 地理、距離、交通與場景連接；
- 勢力、制度、權力與利益；
- 經濟、物資、食物、醫療與日常生活；
- 技術／能力／限制／成本；
- 角色目標、需求、恐懼、錯誤信念與關係；
- 反派／對立力量的價值主張；
- 戀愛、友情、家庭與社會關係的可發展空間；
- 世界能持續產生事件的 Story Engines；
- 作者秘密與角色可知資訊；
- 已寫但 Runtime 永遠不會讀到的資料；
- SQL 有資料但場景、Context 或 Agent 沒有使用的資料；
- 只存在藍圖、沒有任何落地檔案／資料表／Runtime 路徑的宣稱。

### 4.6 實際修補，而非只報告

稽核後必須建立一份 **非 Canon 候選修補包**，路徑可依現有慣例選擇，但建議：

```text
samples/reboot01/chenxing_world_patch_candidate_v1.json
```

修補包至少補足垂直切片需要的：

- 兩名重要 NPC 的完整動機與 Offscreen Agenda；
- 一名對立角色或對立力量；
- 一個可持續產生事件的社會／資源／關係壓力；
- 一個價值兩難；
- 一個能讓主角吐槽但不能輕易解決的制度性或人性矛盾；
- 日常笑點與溫暖互動的條件；
- 不可逆 Runtime 後果。

必須走：

```text
parse
→ validate
→ diff
→ impact preview
→ sandbox apply
```

只可套用到 REBOOT-01 的可重置 Sandbox／非 Canon Project 或 Session。

不得在無使用者決定下修改 AUTHOR_LOCKED 正式 Canon。

### 4.7 落地證據

完成時必須證明：

- 候選修補資料已由 Runtime Context 讀取；
- NPC Agent 能使用其中的目標、恐懼、關係或資源；
- 至少一項世界壓力能在玩家不在場時推進；
- 至少一項原本只存在文件的資料被證明未載入或未使用；
- 至少一項真實缺口被修補並在 Sandbox 回合中觸發。

## 5. 情緒節奏與長篇結構

既有藍圖的情緒波形繼續有效：

```text
歡快／好奇
→ 吐槽與日常摩擦
→ 溫暖／友情／戀愛
→ 不安
→ 衝突
→ 價值兩難
→ 代價／憤怒／失落
→ 反思與關係改變
→ 局部希望與新的未解問題
```

要求：

- 笑點先建立依附，不是填充時間；
- 重情緒必須由既有因果累積；
- 主角變強同時增加責任、道德壓力與失去風險；
- 群像角色都有成長或惡化；
- 每一個 Arc 局部收束；
- 世界持續延伸，不設永久關閉所有角色的最終終局；
- `無最終結局` 不得變成無限重複或永不回收。

## 6. 多代理與模型策略

主執行：

```text
GPT-5.6 Codex
reasoning = MEDIUM
```

建議 Subagents：

- `RepositoryExplorer`：Low／Medium；
- `RuntimeTester`：Medium；
- `NarrativeSystemsCritic`：Medium；
- `CostAndComplexityAuditor`：Low／Medium。

以下升級 High：

- SQL Migration／Transaction；
- Validator／StateReducer／Knowledge Boundary；
- World Source Authority 衝突；
- Replay 不一致；
- 最終整合審查。

不得因工作包很長就全程 High／Max。清楚藍圖下的一般實作仍以 Medium 為預設。

## 7. REBOOT-01 整合位置

本 Addendum 必須整合進既有 Stages：

### Stage 0 擴充

- 執行世界觀／SQL／Runtime Reality Audit；
- 查出哪些資料只有規劃、哪些真的被引擎使用；
- 建立 Audit 報告與機器可讀 Inventory。

### Stage 1 擴充

- Evidence SQL 必須能保存世界稽核、角色語氣、Audience Expectation 與 Critic 分數。

### Stage 2 擴充

- NPC profiles 必須包含深層動機、錯誤信念、恐懼、關係債務與 Offscreen Agenda；
- 使用 Sandbox world patch candidate。

### Stage 3 擴充

- 加入主角 Voice Profile；
- 加入 Audience Expectation Ledger；
- 加入 `IDIOT_PLOT_RISK` 與轉折公平性評分；
- Critic 檢查吐槽是否破壞嚴肅情緒。

### Stage 4 擴充

- 5–10 回合必須實際測試毒舌／吐槽、風險辨識、幽默代價、意外但公平的劇情後果；
- 必須使用世界候選修補包並證明資料進入 Runtime。

### Stage 5 擴充

同 Seed 比較至少加入：

```text
audiencePredictability
twistFairness
characterIntelligence
worldDataUsage
sarcasmCharacterFit
seriousSceneRestraint
```

## 8. 完成條件

除原 REBOOT-01 條件外，必須達成：

1. 一份世界／角色 Reality Audit；
2. 一份已驗證並套用 Sandbox 的非 Canon World Patch Candidate；
3. 至少兩個深度 NPC 與一個有價值主張的對立者；
4. 主角吐槽在至少四種情境中的測試證據；
5. 至少一個 `IDIOT_PLOT_RISK` 測試與防護；
6. 至少一個觀眾可預測的一階結果，接續一個公平的二階後果；
7. 至少一項「文件存在但 SQL／Runtime 未使用」的真實發現；
8. 至少一項世界資料修補在 Sandbox 遊玩中實際生效；
9. Journal 追加本輪真實發現、失敗與規則變更；
10. 最終報告明確區分：正式 Canon、Sandbox Candidate、Runtime Evidence、尚未落地規劃。

## 9. 停止條件

只有下列事項需要回問使用者：

- 要把主角吐槽屬性正式升格成 AUTHOR_LOCKED Canon；
- 要決定正式戀愛對象、角色死亡、不可逆命運；
- 要修改既有 AUTHOR_LOCKED 世界來源；
- 要付費使用外部模型或 Provider；
- 無法安全避免破壞性 Git／資料庫操作；
- 需要弱化 Transaction、Knowledge Boundary、Version、Hash 或批准邊界。

在 Sandbox／非 Canon 垂直切片中測試上述創作假設，不是停止條件。
