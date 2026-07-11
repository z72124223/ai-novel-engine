# AI Novel Engine — 2026 可玩深度重啟藍圖 V1

Status: `USER_APPROVED_ACTIVE_REBOOT_BLUEPRINT`

Date: 2026-07-11

## 0. 重啟理由

本藍圖不是推翻既有引擎，而是修正專案的優先順序。

專案已建立大量有價值的可靠性能力：SQLite 正式狀態、交易、狀態版本、Context Hash、角色知識邊界、記憶、情緒、場景、世界事件、Setting Pack、MVP-8 試用層、圖片候選與來源追蹤。這些能力保留。

但專案也出現明顯失衡：

- 規格、審核、狀態標籤與中間文件遠多於真實遊玩證據。
- 很多品質警告被當成硬阻擋，導致資料長期無法進入 SQL，也無法累積真實失敗樣本。
- 引擎尚未以一個有深度、有節奏、可連續推進的 5–10 回合垂直切片證明效果。
- NPC 自主性、玩家不在場時的世界推進、戲劇價值衝突與角色成長尚未形成可運行閉環。
- Codex 長期使用最高推理等級，沒有依任務風險做成本分級。
- 多代理能力沒有被用於探索、角色模擬、劇情批評與測試分析。
- 圖片與影片產線建立了重要基礎，但目前應暫停於候選狀態，先把故事引擎真正跑起來。

新的總原則：

> **先跑、先保存、先觀察，再依真實失敗修改；不再用更多規劃文件代替產品運行。**

## 1. 產品北極星

AI Novel Engine 的長期產品定位是：

> 一個具有正式世界狀態、自主 NPC、主題驅動戲劇、可持續演化、可視化遊玩介面，以及角色／場景視覺一致性的長篇 AI 敘事世界。

它不是：

- 單純聊天角色前端；
- 單純 AI 續寫器；
- 只有測試與規格的資料治理工具；
- 一次生成後就結束的線性小說；
- 讓 LLM 可以任意竄改世界的文字沙盒。

它應同時具備：

1. **世界可信**：正式狀態由引擎保存，人物、地點、知識、道具與時間不任意漂移。
2. **角色自主**：NPC 有自己的目標、恐懼、錯誤信念、利益、關係和離場行動。
3. **戲劇有深度**：場景不只發生事件，而是迫使角色在價值衝突中選擇並付出代價。
4. **節奏有波形**：歡快、溫暖、戀愛、荒謬、憤怒、反思、失落、希望彼此交替，不持續陰鬱，也不持續搞笑。
5. **世界可長期生活**：每個篇章有局部收束，但整體世界不設一個讓所有角色永久消失的終局。
6. **可重播與可比較**：同一場景可以用不同規則、Agent 或模型重跑，評估改動是否真的變好。
7. **可產品化**：核心垂直切片通過後，建立本機 Web 遊玩介面，再恢復圖片與短影片產線。

## 2. 敘事與情緒憲章

### 2.1 高層靈感，而非複製

本專案可以借鑑作品的高層結構與情緒節奏，但不得複製特定作品的受保護台詞、角色、情節或獨特表達。

使用者希望的體驗可抽象為：

- 以日常、幽默、角色缺點與人際溫度降低讀者進入門檻；
- 讓讀者先喜歡角色、習慣角色、願意陪角色生活；
- 在讀者放鬆或發笑時，讓既有因果突然產生真正代價；
- 情緒可從歡笑轉為憤怒、無力、反思或落淚，但轉折必須由前文累積，而不是廉價突襲；
- 主角逐步變強、變有影響力，最後能左右重大局勢，但能力、責任、失去與道德壓力同步成長；
- 配角、對手、戀愛角色與普通人都有自己的成長，不只圍繞主角提供功能；
- 某些關係或命運可以留下「意難平」，但不能把死亡或悲劇當作唯一深度來源；
- 世界需要溫暖、友情、戀愛、家、笑料與可愛時刻，否則悲劇沒有重量。

### 2.2 情緒波形

每一段長篇內容應使用情緒波形，而不是單一色調：

```text
輕鬆／好奇
→ 角色互動與笑點
→ 小型目標與日常摩擦
→ 溫暖或親密
→ 不安訊號
→ 衝突升高
→ 價值兩難
→ 代價／憤怒／失落
→ 反思與關係改變
→ 局部喘息或新的希望
```

不要求每一場戲完整走完全部節點，但一個 5–10 回合垂直切片必須至少出現：

- 一次自然笑點；
- 一次溫暖或關係靠近；
- 一次真實衝突；
- 一次非黑即白以外的兩難；
- 一次不可逆後果；
- 一次情緒回落與反思；
- 一個讓讀者願意繼續看下去的未解問題。

### 2.3 主角成長

主角成長不得只等於數值上升。

需要同時追蹤：

- 能力；
- 知識；
- 關係；
- 社會影響力；
- 能承擔的責任；
- 必須做出的犧牲；
- 被誤解或失去的可能；
- 對自身錯誤信念的修正。

主角可以逐步成為足以左右戰局、政治或文明方向的人，但成長曲線必須分階段，且每個階段都有新的限制，避免無代價的力量幻想。

### 2.4 群像成長

每個重要角色至少要有：

```text
surfaceGoal
 deeperNeed
 falseBelief
 fear
 valueClaim
 moralLine
 breakingCondition
 relationshipDebt
 growthDirection
 offscreenAgenda
```

中文意義：

- 表面目標；
- 真正需要；
- 錯誤信念；
- 最害怕的事；
- 對世界的主張；
- 當前不願跨越的底線；
- 什麼可能迫使他跨越底線；
- 欠誰、怨誰、愛誰、怕誰；
- 可能成長，也可能惡化；
- 玩家不在場時仍會推進的事。

反派或對立角色不得只是阻擋器。他應代表一種可以理解、甚至有部分正確性的世界觀，迫使主角面對自己的矛盾。

### 2.5 無終局、但不能無限拖延

使用者希望世界沒有一個「從此再也看不到所有角色」的最終結局。

因此採用：

> **局部收束，全球延續。**

每個篇章、卷、季或重大事件都必須：

- 回收至少一個承諾；
- 改變至少一段關係；
- 留下永久世界狀態；
- 完成一個角色階段；
- 打開新的問題或新生活面向。

禁止把「沒有最終結局」誤用成：

- 永遠不解決問題；
- 角色不停重複同一爭論；
- 每次都用更大的敵人延命；
- 所有後果在下一回合重置；
- 無限鋪伏筆而不回收。

## 3. 從公開專案吸收的架構優點

不直接複製整套技術棧，而是吸收設計。

### 3.1 Generative Agents

吸收：

- 記憶流；
- 記憶重要性；
- 反思；
- 短期／長期計畫；
- 日程；
- 玩家不在場時持續模擬。

### 3.2 Concordia 類 Game Master 架構

吸收：

- Agent 以自然語言提出行動；
- Game Master 根據世界狀態與規則判定結果；
- 記憶、感知、推理、行動元件可組裝；
- Agent 不直接寫正式世界。

### 3.3 SOTOPIA 類社會評分

吸收：

- 目標達成之外的社會維度；
- 信任、合作、欺騙、說服、關係與代價；
- 多維評分，而非單一成功／失敗。

### 3.4 TextWorld 類可重播環境

吸收：

```text
resetScenario
→ stepAction
→ observeState
→ scoreOutcome
→ replaySeed
```

同一劇情可重跑以比較 Agent、規則與模型。

### 3.5 ink 類確定性敘事骨架

吸收：

- 旗標；
- 條件；
- 選擇；
- 變數；
- 局部結局；
- 可重播的確定性主線節點。

LLM 負責角色推理、變化與表達；關鍵因果與不可逆條件不完全交給 LLM。

## 4. 新核心架構

### 4.1 角色 Agent

每個重要 NPC 擁有：

```text
NpcIdentity
NpcValues
NpcGoals
NpcNeeds
NpcFears
NpcBeliefs
NpcKnowledgeBoundary
NpcRelationships
NpcMemoryStream
NpcReflectionState
NpcPlan
NpcSchedule
NpcLocation
NpcResources
NpcOffscreenAgenda
```

### 4.2 感知、反思與計畫

```text
world observation
→ perception filtered by location and knowledge
→ memory candidate
→ importance score
→ reflection when threshold is reached
→ goal / plan update
→ action proposal
```

NPC 的記憶與反思不直接改正式世界。它們產生候選，經合法性與交易邊界處理。

### 4.3 Game Master

Game Master 負責：

- 檢查地點、物品、技能、時間與知識；
- 判斷行動結果；
- 產生正式可套用的 State Delta；
- 不負責把每件事寫成漂亮小說。

### 4.4 Drama Director

Drama Director 不控制角色意志，而是從合法事件與行動中選擇值得呈現的衝突。

它應追蹤：

```text
dramaticQuestion
activeValueConflict
stakes
pressure
promiseAndPayoff
sceneGoal
sceneValueBefore
sceneValueAfter
irreversibleChange
emotionalWavePosition
```

禁止把 `directorGoal` 洩漏成角色知識或強迫角色做不符合人格的行動。

### 4.5 Writer

Writer 只負責將已確定的事件寫成可讀敘事，遵守：

- 角色視角；
- 潛台詞；
- 節奏；
- 不重複設定；
- 不自行改正式狀態；
- 不將評分或系統術語寫進讀者文字。

### 4.6 Critic 群

同一個 Agent 不得自己生成、自己評分、自己宣布滿分。

最少使用三種獨立 Critic：

1. `LogicCritic`：因果、地點、時間、知識、物品。
2. `CharacterDramaCritic`：自主性、角色一致性、價值衝突、代價與成長。
3. `ProsePacingCritic`：AI 作文感、重複、離題、資訊傾倒、場景停滯。

Critic 預設只產生分數與具體修訂建議，不直接改 SQL。

### 4.7 Revision Policy

每個回合最多自動修訂一次。

```text
first draft
→ critics score
→ one bounded revision if needed
→ world legality re-check
→ save both versions and scores
```

禁止無限自我修訂迴圈。

## 5. 驗證與資料保存簡化

### 5.1 三個硬門檻

只保留三類硬阻擋：

#### Gate A — 協議安全

- JSON 可解析；
- 必填 ID 存在；
- Session／Turn／Version／Hash 匹配；
- 不允許重複套用結果。

#### Gate B — 世界合法性

- 地點合法；
- 物品可達；
- 角色確實知道該資訊；
- 技能與成本合法；
- State Delta 可在單一 Transaction 完成。

#### Gate C — 不可逆批准

只阻擋：

- 正式 Canon 升格；
- 正式資產批准；
- 破壞性刪除／覆寫；
- 對外發布；
- 付費 Provider/API；
- 安全／權限邊界的弱化。

### 5.2 其他品質檢查改為 Warning／Score

下列問題不得再因為不完美而阻止資料保存：

- 太像 AI 作文；
- 節奏慢；
- 台詞太直白；
- 資訊傾倒；
- 情緒不足；
- 缺少潛台詞；
- 重複；
- 場景目的不清楚。

它們以分數、Finding、Revision Suggestion 保存。

### 5.3 SQL-first Evidence Policy

不再要求內容接近滿分才存入 SQL。

```text
raw model output
→ always save as run evidence

legal but unpolished result
→ save as provisional / candidate

world-legal state delta
→ apply to runtime state

quality issues
→ save score and revision suggestion
```

失敗、退件、重複與被修訂的版本也有研究價值，必須可追蹤。

## 6. 劇情邏輯、停滯與自我進化

### 6.1 劇情評分維度

每場戲 0–10 分：

```text
causalCoherence
characterAutonomy
characterConsistency
goalProgress
conflictEscalation
valuePressure
choiceCost
irreversibleChange
subtext
revealPacing
repetitionRisk
aiProseRisk
emotionalImpact
readerPull
```

### 6.2 防止一直講不到重點

每場戲只允許一個主要 `dramaticQuestion`。

每個段落至少服務一項：

- 推進決定；
- 提供阻力；
- 揭露角色；
- 改變關係；
- 建立必要伏筆；
- 支付或回收代價。

完全不服務者應縮短或刪除。

### 6.3 Narrative Stall

若連續兩回合符合：

```text
sceneGoal unchanged
sceneValueAfter unchanged
runtime state unchanged
relationship state unchanged
no new cost or reveal
```

標記：

```text
NARRATIVE_STALL
```

下一回合只能採用：

- 升級衝突；
- 做出選擇；
- 揭露新事實；
- 產生代價；
- 結束或轉場。

不得再用更長但等價的對話拖延。

### 6.4 規則演化

每次規則修改都版本化：

```text
rule-v1
rule-v2
rule-v3
```

以同一 Scenario Seed 重跑，比較：

- 合法性；
- 情緒波形；
- 角色自主性；
- 停滯次數；
- Token／成本；
- 讀者吸引力分數。

只有實測改善才升級正式規則。

## 7. 多代理工作群

### 7.1 Codex 內部角色

允許一個主控 Codex 加最多四個平行 Subagents：

- `RepositoryExplorer`：只讀搜尋與依賴圖；
- `RuntimeTester`：跑測試、場景與 SQL 證據；
- `NarrativeSystemsCritic`：檢查自主性、戲劇與停滯；
- `CostAndComplexityAuditor`：追蹤 Token、模型、文件與程式複雜度。

### 7.2 寫入規則

- 平行 Agent 優先做只讀探索、批評與測試分析；
- 同一時間只有一個 Writer 修改核心 Kotlin／SQL；
- 不允許多個 Agent 同時改 `Models.kt`、`Main.kt`、Migration 或核心 Transaction；
- 主控彙整後再寫入；
- Subagent 失敗不得阻塞整體，只需記錄並由主控接手。

## 8. Codex 模型與成本政策

詳細規則見 `docs/CODEX_MODEL_AND_BUDGET_POLICY_V1.md`。

核心決策：

- **主執行 Codex 預設使用目前可用的 GPT-5.6 Codex，Reasoning `MEDIUM`。**
- `HIGH` 只用於核心架構衝突、Migration／Transaction、難以重現 Bug、安全邊界與最終整合審查。
- 搜尋、文件盤點、測試分類與重複任務使用較快／較低成本模式。
- `MAX`／最高昂模式預設禁止，除非兩次有證據的 Medium／High 嘗試仍無法解決關鍵阻塞。
- 固定格式、Hash、JSON、路徑、Diff、統計交由腳本，不消耗高階模型。

## 9. Web 與遊戲產品化

舊的「永久禁止 Web Server／React／Ktor」規則退休。

新的規則：

1. 在垂直切片未跑通前，不先做大型前端。
2. 垂直切片通過後，允許建立本機 Web 介面。
3. 第一版只需要：
   - 故事文字；
   - 玩家輸入；
   - 送出；
   - 世界時間；
   - 角色／關係摘要；
   - 存檔／讀檔；
   - Agent／Critic／狀態 Delta 除錯面板。
4. 首選架構：現有 Kotlin Core + 小型本機 API + TypeScript/React Web Client；若 Repository 證據顯示其他更小方案更合理，Codex 可選擇並記錄。
5. 不開放公網、不加入登入、付費或雲端部署，直到本機體驗有證據成立。

## 10. 美術與媒體路線

### 10.1 圖片線暫停

目前大坪海岸主全景已生成並由使用者表示同意，但尚未完成 Codex Intake／Hash／Candidate Reference。

狀態：

```text
IMAGE_LANE_PAUSED_AFTER_USER_APPROVED_CANDIDATE
```

- 不丟棄既有藍圖；
- 不繼續 Task 02–07；
- 不將候選宣稱為 Canon、正式 Location Reference 或 Production Lock；
- 待核心引擎垂直切片通過後再恢復。

### 10.2 美術順序修正

恢復圖片線時，先選人物表現風格，再讓背景跟隨：

```text
角色長相基準
→ 角色表現風格 A/B/C
→ 陸塵行＋愛醬同框
→ 服裝材質與光影
→ 場景風格
→ Location Bible
→ Shot 關鍵影格
→ 影片
```

### 10.3 影片 Provider

未來影片不綁單一供應商。詳見 `docs/VIDEO_PROVIDER_BAKEOFF_PLAN_V1.md`。

先建立 Provider-neutral `VideoShotHandoff`，再以同一個核准 Keyframe／Motion Contract 測試多個 Provider。

目前不批准：

- 購買或呼叫任何付費影片 API；
- 儲存 API Key 到 Git；
- 背景批次生成；
- 整話生成；
- Provider 專屬架構侵入核心引擎。

## 11. 自動執行授權

使用者明確授權 Codex 依本藍圖執行到 REBOOT-01 完成，不需要每個內部里程碑返回 GPT 總控。

允許：

```text
一個明確啟動的 Codex CLI 工作階段
→ 建立乾淨 reboot branch/worktree
→ 依 REBOOT-01 內部階段持續執行
→ 每階段驗證與 Commit
→ 自動進入下一個內部階段
→ 完成總驗收後 Push 與回報使用者
```

僅在真正停止條件時中止：

- 需要使用者美術／故事 Canon 最終選擇；
- 需要付費 API、API Key 或 Provider 支出；
- 需要破壞性 Git 操作而沒有安全替代；
- 來源權威互相衝突，Repository 規則無法解決；
- 需要弱化 SQL Transaction、Knowledge Boundary、State Version 或不可逆批准；
- 兩次有證據的修復仍無法恢復可編譯／可測狀態。

普通技術不確定、測試失敗、設計取捨或小型重構不是使用者停止條件。Codex 應自行調查、選擇最小方案並繼續。

## 12. REBOOT-01 成功定義

REBOOT-01 不是文件完成，而是可運行證據完成。

必須達成：

1. 一個塵行非 Canon／可重置垂直切片。
2. 主角、兩名重要 NPC、至少一名對立角色或對立力量。
3. 5–10 個連續回合。
4. 至少一次玩家不在場的 NPC 自主行動。
5. 至少一個價值兩難。
6. 至少一個不可逆後果。
7. 至少三次世界／關係／角色狀態的永久變化。
8. 原始輸出、Critic 分數、修訂前後版本、State Delta、SQL 狀態與事件證據均可追蹤。
9. 不因文風分數不完美而丟失資料。
10. 至少一次同 Seed 重跑比較，證明規則演化可測。
11. 無連續兩回合的 Narrative Stall；若發生，系統能辨識並恢復。
12. 核心通過後提供可操作的本機 Web 垂直切片或明確證據說明為何應延後。
13. 產出實際遊玩報告，而不是只產出架構文件。

## 13. 來源與後續閱讀

- `docs/work-packages/REBOOT-01_PLAYABLE_DEPTH_SLICE.md`
- `docs/CODEX_MODEL_AND_BUDGET_POLICY_V1.md`
- `docs/VIDEO_PROVIDER_BAKEOFF_PLAN_V1.md`
- `docs/development_journal/INDEX.md`
- `PROJECT_STATE.md`
- `.codex/project_controller.md`
- `docs/CURRENT_ROUTE_GATE_V2.md`

歷史圖片、MVP、reconciliation 與 route 文件仍保留為證據，但不再選擇目前工作。