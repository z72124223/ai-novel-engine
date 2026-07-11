# 2026-07-11 AI Novel Engine 專案討論與決策長誌 V1

Status: `DETAILED_RECONSTRUCTED_USER_VISIBLE_DISCUSSION_LOG`

## 0. 完整性說明

本文件保存目前這條長對話中可取得的使用者需求、GPT 建議、Codex 回報、GitHub 處理、方向轉折與教訓。

它是高度詳細的重建紀錄，不是平台匯出的逐字聊天檔，也不包含隱藏推理、系統提示、憑證或其他對話中無法取得的內容。

使用者明確要求：不要只保存最新一段，而要讓未來 GPT 在忘記專案最初目標時能回頭理解一路以來的開發對話。本文件依這個要求建立。

---

## 1. 最初問題：GitHub 能否瀏覽別人的資料並下載

使用者先確認 GitHub 是否可以瀏覽其他人的公開資料、下載專案、Fork、Clone、查看歷史版本。

當時建立的基礎認識：

- Public Repository 可以瀏覽與下載；
- Private Repository 需要權限；
- 能下載不等於可自由商用，仍要看 License；
- MIT／Apache／GPL／No License 的法律義務不同；
- 對陌生專案先讀 README、License、依賴與架構，不直接執行未知腳本。

這開啟了「吸收其他公開專案優點」的長期方向。

## 2. 尋找 AI 小說與引擎公開專案

使用者希望找到類似自己的 AI 小說／引擎專案，研究哪些功能能幫助 `ai-novel-engine`。

當時列出的代表專案與價值：

### 2.1 Generative Agents

被視為 NPC 自主生活的重要參考：

```text
觀察
→ 記憶
→ 重要性
→ 反思
→ 計畫
→ 行動
```

價值：世界不圍繞主角、玩家不在場時角色仍生活、消息與關係能傳播。

### 2.2 KoboldAI

被視為 AI 小說前端與 Prompt 組裝參考：

- Memory；
- Author's Note；
- World Info；
- Story Save／Load；
- 本地與遠端模型；
- 長上下文裁切。

### 2.3 SillyTavern

被視為角色卡、Lorebook、Token 預算、多角色對話與成熟 UI 的參考。

### 2.4 ink

被視為確定性分支、旗標、任務與選擇的骨架，避免所有關鍵因果都由 LLM 決定。

### 2.5 Microsoft TextWorld

被視為形式化世界狀態、行動前置條件、效果與可重播測試的參考。

最早的組合建議：

```text
你的 Kotlin 引擎
├── TextWorld 類世界合法性
├── ink 類分支與旗標
├── Generative Agents 類 NPC 記憶與規劃
├── KoboldAI 類 Prompt／Memory 組裝
└── SillyTavern 類角色卡、Lorebook、UI
```

## 3. 讀取使用者 GitHub 並比較差距

使用者授權讀取：

```text
z72124223/ai-novel-engine
```

當時辨識出的專案優勢：

- AI 只能提出 `AiTurnResult`，不能直接寫正式狀態；
- `prepare-turn`／`resolve-turn`；
- `contextId`、`sessionId`、`turnNumber`、`baseStateVersion`、`contextHash`、`resultId`；
- 單一 SQLite Transaction；
- 角色記憶、知識、情緒與可見邊界；
- 場景、Story Thread、世界時間、事件、轉場與位置；
- Setting Pack 的 parse／validate／diff／impact／apply；
- Canon 依賴與版本；
- Context Byte Budget 與 Semantic Floor；
- Capability Contract；
- Provisional Layer；
- Narrative Audit。

核心定位被描述為：

> 一個讓 LLM 只能提出敘事與行動、不能任意竄改正式世界狀態的可驗證互動小說引擎。

當時也指出主要弱點：

- 沒有完整模型自動接入；
- 沒有成熟 UI；
- NPC 自主性仍弱於 Generative Agents；
- 缺少真正語意／向量檢索；
- Repo 混合引擎、內容、圖片、影片、作品素材；
- README、ROADMAP、PLANS、HANDOFF、Branch 名稱與實際進度不一致。

## 4. 第一次文件對齊

GPT 直接修改 README，補上：

- MVP-8 試用層；
- reader-first；
- script-first 歷史方向；
- 預設分支名稱只是歷史名稱；
- 影音實驗暫停狀態。

Commit：

```text
25b03fde8656678c6ed34333cc6d680622d5cbf6
```

但後續發現 ROADMAP、PLANS、HANDOFF 仍互相矛盾，這成為治理重整的起點。

## 5. GPT／Codex／GitHub 三方協作想法

最初分工：

```text
使用者：Product Owner
GPT：架構師／PM／審查
Codex：實作、測試、Commit
GitHub：唯一正式版本與共同記憶
```

使用者希望自己不再負責拆技術任務，只需要做產品與審美決定。

建立 Work Package 概念：

```text
一次只完成一個完整工作包
```

並計畫：

- Governance；
- Visual Production Pipeline；
- Character Bible；
- Scene Bible；
- Shot Pipeline。

後來使用者指出一個現實問題：這個聊天不能直接「啟動」Codex 長時間工作。於是形成：

```text
GPT 維護 GitHub 藍圖
→ Codex CLI 先讀藍圖
→ Codex 實作／測試／Push
→ 回報
```

## 6. 專案治理 WP-001 與髒工作樹危機

### 6.1 WP-001

Codex 建立：

- `PROJECT_STATE.md`
- `.codex/project_controller.md`
- 更新 `PLANS.md`

但第一次 Commit 把 `PLANS.md` 原有大量未提交內容一起包入，產生 3,606 行以上的混合 Commit。

GPT 決定：

- 建立 Backup Branch；
- `git reset --mixed HEAD^`；
- 只提交兩個全新治理文件；
- `PLANS.md` 留在工作樹；
- 禁止 hard reset／clean／stash／force push。

乾淨治理 Commit：

```text
89e522f18a946426232f5d56009fea0262329b47
```

### 6.2 遠端突然多 55 個 Commit

推送時發現遠端快速前進，本地 ahead 1／behind 55。

決策：

- 不在 115 項髒工作樹中 Pull／Merge／Rebase；
- 建立全新乾淨 Worktree；
- 從遠端最新 Commit 建分支；
- Cherry-pick 治理 Commit；
- 普通 Fast-forward Push；
- 原髒工作樹保持不動。

### 6.3 Repository Reality Audit

Codex 盤點出：

- 115 項原始髒工作樹；
- 7 tracked modified；
- 108 untracked；
- 47 production service；
- 45 tests；
- app、docs、memory、samples、PLANS 等分類。

之後建立 Functional Clustering：

- 8 個功能群；
- 92 個 Kotlin 檔；
- 104 個檔案與遠端完全相同；
- 9 個 diverged；
- 2 個 local-only；
- 真正核心分歧集中在 `Main.kt`、`Models.kt`、`PLANS.md` 和少數文件。

這證明「115 項全部都要救」是錯覺，大部分已在遠端存在。

### 6.4 Reconciliation 小包

先處理低風險文件差異：

```text
docs/PROJECT_STRUCTURE_REPORT.md
```

只移植一行 OneDrive Workspace 路徑。

再驗證 Candidate Reference 遠端版本：

- Compile PASS；
- 51 個直接安全測試 PASS；
- Full Suite 809 tests 中 4 個 World Source Hash／Byte Size 無關失敗；
- 遠端版本增加 `sourceFileSha256` 與 `imageResultIdentityDigest`；
- 決定 KEEP_REMOTE／本地舊版 SUPERSEDED。

這段工作展示了嚴格證據與 Git 安全的價值，但也逐漸顯示專案花太多時間在治理與審核。

## 7. 圖片／影片成品願景

使用者明確指出最終成品不是只有小說文字，而是：

> 角色與場景盡量不飄移、能連續觀看的 AI 小說影片。

因此建立視覺生產概念：

- 角色視覺聖經；
- 場景視覺聖經；
- Shot 狀態快照；
- 先圖後影片；
- 3–8 秒短鏡頭；
- 漂移檢查；
- Candidate／Approved／Rejected Drift 分層。

### 7.1 角色固定資產

已存在：

- 陸塵行 production turnaround；
- 陸塵行真人照片組；
- 開場服裝 turnaround；
- 愛醬 production turnaround；
- 風格參考。

### 7.2 場景固定資產

規劃需要：

```text
主全景
＋俯視 Zone Map
＋四方向視圖
＋空間錨點
＋局部近景
＋人物／愛醬／道具比例
```

360° Panorama 只作補充，不能取代平面幾何。

### 7.3 GPT 圖片交接藍圖

建立：

```text
Codex 跑引擎／世界資料
→ 劇本／Shot／圖片 Handoff Commit 到 GitHub
→ GPT 讀指定 Commit 與 Handoff
→ 缺 Reference 時 REFERENCE_UPLOAD_REQUIRED
→ GPT 生成 Candidate
→ 使用者接受／退件
→ Codex Intake／Hash／Human Review／Candidate Reference
```

重要規則：GitHub 路徑存在不等於圖片已附加到 GPT 圖片工具。

## 8. 圖片 Work Packages

### 8.1 WP-002A

建立 6 套 Schema：

- ChatGPT Image Handoff；
- Reference Upload Checklist；
- Shot Queue；
- Prompt Manifest；
- Image Review Log；
- Image Return To Codex。

並盤點既有 12 階段安全鏈：

```text
Storyboard Review
→ Asset Candidate
→ Image Candidate
→ Prompt
→ Provider Handoff
→ External Result
→ Human Review
→ Candidate Reference
→ Registry
→ Acceptance
```

結果：

```text
WP002A_PASS_WITH_SCHEMA_VALIDATOR_GAP
```

完整 JSON Schema Validator 未預裝，沒有新增依賴。

### 8.2 WP-003

盤點：

- 30 個唯一圖片；
- 31 個資產綁定；
- 角色／服裝／愛醬／風格；
- 15 張 candidates_v2；
- 10 張 rejected opening frames；
- 大坪海岸與遺跡 Location Bible 缺口；
- Shot 01–03 readiness。

發現：

```text
shot_queue_v1.characters.minItems = 1
```

無法誠實表示純 Location Bible 任務。

### 8.3 WP-003A

架構決定：不弱化正式 Shot Schema，不塞假角色，建立獨立 Location Bible Handoff。

任務 DAG：

1. Master Panorama
2. Top-down Zone Plan
3. Seam／Tidepool Detail
4. Lu／Aijiang Scale Reference
5. Shot 01 Facing View
6. Shot 02 Seam View
7. Shot 03 Sea Reaction View

只有 Task 01 初始 READY。

### 8.4 WP-003B

使用者上傳 cinematic-misty 風格圖，GPT 生成：

```text
daping_lb_01_master_panorama_candidate_v1.png
```

使用者回覆「同意」。

這張圖仍只是 Candidate，尚未完成 Codex Intake／Hash／Candidate Reference。

## 9. Grok 對專案的外部評論

使用者詢問 Grok 對全景、固定資產與藍圖的看法。

Grok 的核心評價：

- 角色固定資產已經能實際使用；
- Reference-first 是必要規則；
- 場景全景與 Zone Map 方向正確，但尚未形成完整可餵資產庫；
- 文件量遠大於可執行資產；
- 一張鎖定全景比十份未落地規劃更有實戰價值；
- 規劃完成不等於資產完成。

GPT 同意這個批評，但補充：一張 Panorama 仍不能單獨固定空間，需要 Plan／Direction／Anchors／Scale。

## 10. Grok 影片路線

使用者認為 Grok 最大價值之一是影片生成，漫畫／關鍵影格可以成為影片 Storyboard。

形成的未來路線：

```text
核准乾淨單鏡 Keyframe
＋ Motion／Camera／Continuity Contract
→ Provider-neutral VideoShotHandoff
→ Grok 或其他 Provider Image-to-video
→ 使用者 Review
→ Codex Hash／Continuity Intake
```

禁止把含對話框、字幕、Panel Border、多格漫畫頁直接當正式影片首幀。

## 11. 使用者對專案的 12 點批評與優先事項

使用者提出一組關鍵反思：

1. 公開專案在 NPC 自主性等方面比目前專案強，是否能吸收並讓劇情有深度；
2. 專案測驗與審核過多，資料一直不能進 SQL，引擎沒有真實使用；
3. Codex 一直最高模型是否浪費預算；
4. 為什麼沒有多代理工作群；
5. 能否讓劇情 Agent 自評、糾錯並持續進化，避免 AI 作文感、離題與無限輪迴；
6. 是否學習 Anthropic 對模型內部特徵／Circuit 的研究思路；
7. GPT-5.6 出現後，早期 5.5 規劃是否過時；
8. GPT 沒有幫助使用者發散競爭力，成本高但第一步仍未跨出；
9. 為什麼新版 GPT 能做網站，專案仍永久禁止網站／遊戲；
10. 為什麼沒有主動推薦 Apps／Plugins／MCP 等新能力；
11. 美術順序做反，應先選人物風格，再讓背景跟隨；
12. 能否正式吸收公開 GitHub 專案優點，而不只比較。

補充：

> 應該汰換老舊、落後的思維與模式。

這是 2026 Reboot 的直接起點。

## 12. 對過度治理的正式反省

GPT 接受批評：

```text
規格很多
審核很多
狀態標籤很多
真正連續遊玩的證據很少
```

新的判斷：

- 「避免犯錯」被優化得太過頭；
- 可驗證性被誤當成產品本身；
- 品質差的 Run 仍有研究價值；
- 文風／節奏不應阻止 Evidence 保存；
- 引擎應先跑 5–10 回合，再補真正需要的測試；
- 完整單元測試不能取代真實遊玩；
- 文件數量不能代表進度。

## 13. 新敘事願景

使用者進一步描述希望的情緒與長篇方向：

- 節奏先歡快、好笑、親切，讓讀者容易投入；
- 笑著笑著，可能轉為憤怒、反思或突然落淚；
- 情緒像波形，有抑有揚，不是一路陰鬱；
- 主角逐步變強，最後可以左右戰局，但其他角色也要成長；
- 角色要有溫暖、戀愛、友情與讓讀者意難平的關係；
- 讀者是因為喜歡角色，才會被事件傷害或感動；
- 希望世界沒有一個讓所有角色永遠離開觀眾的總結局；
- 但無結局不能變成無限拖延，應採「局部收束，全球延續」。

這些內容正式寫入 `docs/REBOOT_2026_PLAYABLE_DEPTH_BLUEPRINT_V1.md` 的敘事與情緒憲章。

## 14. 新架構方向

從公開專案正式吸收：

```text
Generative Agents：記憶、反思、規劃、日程、離場生活
Concordia 類：Agent Proposal + Game Master
SOTOPIA 類：信任、合作、欺騙、關係、多維社會評分
TextWorld 類：Reset／Step／Observation／Replay
ink 類：旗標、條件、選擇、局部結局
```

新的角色資料：

- 表面目標；
- 真正需要；
- 錯誤信念；
- 恐懼；
- 價值主張；
- 道德底線；
- 崩壞條件；
- 關係債務；
- 成長方向；
- Offscreen Agenda。

新的 Agent 群：

- NPC Agent；
- Game Master；
- Drama Director；
- Writer；
- Logic Critic；
- Character／Drama Critic；
- Prose／Pacing Critic。

同一 Agent 不得自己寫、自己評、自己宣布滿分；Revision 最多一次。

## 15. 驗證簡化與 SQL-first

只保留三種硬門檻：

1. 協議安全；
2. 世界合法性；
3. 不可逆批准。

下列改為 Warning／Score：

- AI 作文感；
- 重複；
- 節奏慢；
- 台詞直白；
- 缺少潛台詞；
- 情緒不足；
- 資訊傾倒。

新的保存原則：

```text
Raw Output 永遠保存為 Run Evidence
合法但不完美 → Provisional／Candidate
世界合法 State Delta → 套用 Runtime
品質問題 → 保存 Score／Revision Suggestion
```

## 16. 模型與預算

使用者質疑 Codex 是否需要永遠最高等級。

新的政策：

- GPT-5.6 Codex Medium 為主控預設；
- High 用於 SQL／Transaction／Validator／核心 Bug／最終整合；
- Low／快速模式用於搜尋、盤點與重複工作；
- Hash／JSON／路徑／Diff 用腳本；
- Max 預設禁止；
- Multi-Agent 只在任務可以分割時啟動；
- 同一時間只有一個核心 Writer。

詳見 `docs/CODEX_MODEL_AND_BUDGET_POLICY_V1.md`。

## 17. Web／遊戲方向

舊規則永久禁止 Web Server、Ktor、React、開 Port。

使用者指出新版 GPT 已能快速做網站，這個永久禁止已妨礙產品化。

新的順序：

```text
先完成可玩 5–10 回合核心
→ 再建立本機 Web 垂直切片
```

第一版只需：故事、輸入、角色／關係、世界時間、存檔／讀檔、Agent／Critic／State Delta Debug。

不先公開部署、不登入、不付費。

## 18. 美術順序修正

使用者指出一直沒有讓他先選人物風格，只先選背景。

新的順序：

```text
角色長相
→ 角色表現風格 A/B/C
→ 人狗同框
→ 服裝材質／光影
→ 場景風格
→ Location Bible
→ Keyframe
→ 影片
```

目前圖片線暫停，大坪海岸 Panorama 保留為 Candidate，不當最終風格鎖。

## 19. YouTube 影片與影片 API 自動化

使用者提供：

```text
https://www.youtube.com/watch?v=dqbWp_Aqj1k
```

可驗證標題：

```text
GPT5.6 + Codex：自动化生成视频全流程实战分享！
```

工具環境沒有取得完整逐字稿，因此不宣稱掌握每一個口述細節。

正式吸收的是工作模式：

```text
GPT／Codex 作 Orchestrator
→ 劇本、分鏡、資產、Prompt
→ 呼叫外部影片 API
→ Poll／Download／Review／Retry
```

不假設影片模型一定要來自 OpenAI。

## 20. 影片 Provider 選擇

使用者表示願意購買模型 API，也可使用即夢／Seedance，Grok 也能生影片，但希望找最適合專案的。

決策：不立刻綁定，建立 Provider Bake-off。

候選：

- Gemini Omni Flash；
- Veo 3.1；
- Grok Imagine Video；
- Seedance 2.0／即夢；
- Runway。

同一 Keyframe／Motion Contract，每家少量 Candidate，以角色一致性、場景幾何、動作、成本、API、商用與編輯能力評分。

目前不授權花費；詳見 `docs/VIDEO_PROVIDER_BAKEOFF_PLAN_V1.md`。

## 21. 2026 Playable Depth Reboot

新的主線：

```text
Reality Baseline
→ Validation Simplification
→ SQL Evidence Persistence
→ NPC Autonomy
→ Drama Director／Critics
→ 5–10 Turn Chenxing Slice
→ Replay／Evolution
→ Local Web Slice
```

圖片線：

```text
IMAGE_LANE_PAUSED_AFTER_USER_APPROVED_CANDIDATE
```

影片線：

```text
FUTURE_BAKEOFF_NO_SPEND
```

Codex 主控：GPT-5.6 Codex Medium；必要時 High。

REBOOT-01 允許在一個 Codex CLI 工作階段內連續完成內部 Stages，不必每階段回 GPT 總控。

## 22. 仍然不可丟掉的核心

即使重啟，以下仍保留：

- SQL 正式世界狀態；
- Transaction；
- State Version；
- Context Hash；
- 重複套用防護；
- Knowledge Boundary；
- 地點、物品、技能、成本合法性；
- Canon／正式資產／付費 Provider 需要人類批准；
- Provenance／Hash；
- Rejected Drift 證據。

## 23. 被正式退休的模式

- 文件數量代表進度；
- 每個 Warning 都阻擋；
- 接近滿分才存 SQL；
- 永遠 High／Max；
- 永遠單代理；
- 永久禁止 Web；
- 劇本或圖片其中一個永遠是唯一終點；
- 完整測試可以取代真實遊玩；
- 模型版本升級卻不重審藍圖；
- 每個小階段都要求使用者轉貼回 GPT；
- 背景風格先於人物風格；
- 規劃完成等於資產完成。

## 24. 目前給未來 Agent 的一句話

> 不要再用更多審核文件證明這個專案很嚴謹；請用一個真實可玩的、角色會自己生活、會讓人笑也會讓人難過的 5–10 回合世界證明它有價值。
