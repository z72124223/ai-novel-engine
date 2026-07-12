# AI Novel Engine — 現在狀態

> **這是唯一現況入口。** 新的 GPT、Codex、Reviewer 或其他 Agent 必須先讀本檔。舊 REBOOT、圖片 Work Package、HANDOFF、README 狀態與 `PLANS.md` 舊標題都是歷史資料，不能蓋過本檔。

## 0. SCREEN-01 執行覆寫（2026-07-12）

- Branch：`codex/screen01-video-first-pilot`
- Base：`510527c24a612331590e25e42b004d00cd66858a`
- Status：`COMPLETED_AWAITING_GPT_REVIEW`
- Completion class：`SCREEN01_PASS_WITH_DIALOGUE_WARNINGS`
- Selected screenplay：`samples/screen01/screen01_screenplay_revised_zh-Hant-TW.md`
- Selected screenplay SHA-256：`4b2ac13f84541dfa904096fe650ed99a9f8d58e1519ba9f0b0816d85e56a554c`
- Selected dialogue：`samples/screen01/screen01_dialogue_track_revised_zh-Hant-TW.json`
- Selected dialogue SHA-256：`6936ec2099f5a61ab07db631e9d19ef2bb49a8154f572fb5f360e0f7fdcec65c`
- Revision：`1 / 1`
- Scenes／Shots：`4 / 31`，總節拍 440 秒
- Future video Pilot：`82 seconds / shot-015..shot-023`
- Boundary：`NON_CANON_PILOT`

驗證：Full Gradle `849/849 PASS`；JSON／Hash／引用／Scene／Shot／Pilot／SRT／秘密／私人路徑／scope／diff 全部 PASS。生產成果已普通 Push 至同名遠端分支，驗證交付 Commit 為 `d7c09969188255ca43b64b15852c4df56d136db4`。下一步只有 GPT／產品負責人審查；不得自動生成圖片／影片、呼叫 Provider、升格 Canon 或寫下一章。

## 1. 最終產品

本專案的主要終點是：

> **可長期連載的動畫／影片故事。**

不是單純小說生成器，也不是目前優先做互動遊戲。

正式生產路線：

```text
世界觀與角色資料
→ 故事事件主檔
→ 影視劇本
→ 場景與正式對白
→ 分鏡與鏡頭表
→ 角色／場景固定資產
→ 關鍵影格
→ 單鏡影片
→ 剪輯、聲音、字幕
→ 完整影片
```

小說可以是故事測試、出版版本或補充內容，但不是最高生產權威。

## 2. GitHub 是團隊最高工作場所

正式規則：

> **GitHub 遠端 Repository 是多人團隊協作、版本、交接、批准紀錄與長期記憶的最高權威。**

本機電腦、Worktree、終端、暫存資料夾、`out/**`、本機 SQLite 和模型工作階段都只是可替換的執行環境。

以下成果不能只留在本機：

- 世界資料；
- 故事主檔；
- 小說試稿；
- 影視劇本；
- 對白軌；
- 場景表；
- 分鏡／鏡頭表；
- 評審；
- 作者決策；
- 圖片／影片交接包；
- Hash；
- 測試與開發日誌。

所有正式交接必須包含：

```text
Repository
Branch
Commit SHA
Repo-relative Path
Content SHA-256（需要時）
```

私人絕對路徑、未 Push 工作樹和「我本機最新版」不是正式交接。

完整規則：

```text
docs/VIDEO_FIRST_GIT_FIRST_PRODUCTION_CHARTER_V1.md
```

## 3. STORY-01 狀態

STORY-01 已作為技術與內容里程碑納入目前遠端預設分支。

來源：

```text
samples/story01/story01_revised_draft.md
```

SHA-256：

```text
ea0e6e148377947907e68883e08a3864b113acaa0c7b9fd30df2408bd1145e91
```

狀態：

```text
NON_CANON_PILOT
```

它證明正式世界來源、SQL、全文產生、三個評審、一次修訂與作者工作台可以運作。

它不代表最終產品已改成小說。

不得再自動做第二次小說修訂，也不得升格正式 Canon。

## 4. 現在唯一工作包

```text
SCREEN-01 — 影片優先影視劇本與華語對白試作
```

狀態：

```text
READY_FOR_AUTONOMOUS_CODEX_EXECUTION
```

Codex 實作分支：

```text
codex/screen01-video-first-pilot
```

基線：

```text
最新遠端預設分支
```

Codex 開始前先 `git fetch --prune origin`，再記錄實際 Base SHA。

完整工作包：

```text
docs/work-packages/SCREEN-01_VIDEO_FIRST_SCRIPT_AND_DIALOGUE_PILOT.md
```

## 5. 必讀順序

1. `PROJECT_STATE.md`
2. `.codex/project_controller.md`
3. `docs/CURRENT_ROUTE_GATE_V4.md`
4. `docs/VIDEO_FIRST_GIT_FIRST_PRODUCTION_CHARTER_V1.md`
5. `docs/work-packages/SCREEN-01_VIDEO_FIRST_SCRIPT_AND_DIALOGUE_PILOT.md`
6. `docs/CODEX_MODEL_AND_BUDGET_POLICY_V2.md`
7. `docs/development_journal/INDEX.md`
8. `AGENTS.md`
9. `samples/story01/story01_revised_draft.md`
10. 只有 SCREEN-01 真正需要的程式、測試和資料

## 6. SCREEN-01 要做什麼

把〈潮線之外〉轉成真正可接影像產線的資料：

- 故事生產主檔；
- 完整繁體中文影視劇本；
- 自然華語對白規格；
- 對白軌；
- 三個獨立全文評審；
- 最多一次影視劇本／對白修訂；
- 場景表；
- 鏡頭表；
- 60–90 秒未來影片試作規格；
- 繁體中文字幕；
- 中國大陸、台灣、香港、馬來西亞／新加坡的語言發行說明；
- 未來圖片／影片交接包；
- 所有成果、報告與 Hash Commit 到 GitHub。

現在不實際生成圖片或影片。

## 7. 主角方向

陸塵行可以聰明、可靠、敏銳。

不需要為了製造戲劇而故意變笨或犯低級錯誤。

他的主要成長線是進入新世界後：

- 適應文化與生活方式；
- 理解制度、階級、人情與政治；
- 理解力量帶來的責任；
- 從保護自己和愛醬，走向承擔更多人的命運；
- 最後能影響戰局與世界。

衝突應來自資訊不完整、價值兩難、對手適應、正確決定的代價，以及新世界與地球經驗的差異，不靠角色集體降智。

## 8. 華語對白方向

母稿：

```text
繁體中文 / zh-Hant-TW
```

陸塵行口語基礎：

```text
自然台灣華語
```

同時要讓中國大陸、香港、馬來西亞、新加坡及其他華語讀者容易理解。

禁止：

- 兩個 AI 輪流講完整論文；
- 每句都說清楚價值觀；
- 每句都像金句；
- 每個角色同一語氣；
- 危急時仍講很長的工整句；
- 過多「不是 A，而是 B」；
- 在同一母稿亂混各地詞彙。

優先：

- 短句；
- 打斷；
- 停頓；
- 答非所問；
- 誤會；
- 沉默；
- 動作代替解釋；
- 每個角色有不同語氣。

## 9. 模型

一般工程與整合：

```text
gpt-5.6-sol / medium
```

真正的影視劇本與對白轉換，可在限定範圍使用：

```text
gpt-5.6-sol / high
```

三個獨立評審：

```text
gpt-5.6-sol / medium
```

搜尋與測試分析使用 Terra Low／Medium；Hash、JSON、字幕時間、Diff 和統計使用程式，不浪費高級模型。

Max／Ultra 預設禁止。

## 10. Codex 執行方式

使用者啟動一次後，Codex可以：

```text
建立乾淨分支
→ 完成 SCREEN-01 所有階段
→ 每階段驗證與 Commit
→ 將劇本、對白、場景、鏡頭、字幕、評審和報告全部 Push
→ 最後一次回報
```

一般文字調整、檔案結構、Shot 數量、字幕時間、測試或作者工作台設計，不需要回來詢問。

## 11. 現在禁止

SCREEN-01 期間不得：

- 寫第二、第三章小說；
- 再修改 STORY-01 小說；
- 生成圖片；
- 生成影片；
- 呼叫 Grok、Gemini/Veo、Seedance／即夢、Runway 或其他 Provider；
- 購買 API 或保存 API Key；
- 選定最終影片 Provider；
- 升格 Canon；
- 決定永久死亡或戀愛結果；
- 新增互動 RPG 功能；
- 公開部署網站；
- 把必要成果只放在主機環境。

## 12. 真正停止條件

只有以下情況 Codex 才停下詢問：

- 必須決定正式 Canon；
- 必須決定永久角色命運或戀愛；
- 必須決定正式美術；
- 必須花錢、使用 Provider 或 API Key；
- 無法避免破壞性 Git／資料操作；
- 必須弱化 Transaction、Knowledge、Hash 或批准邊界；
- 同一核心問題經一次 Medium 和一次 High 有證據修復仍失敗。

## 13. 完成條件

SCREEN-01 只有在遠端 Git 分支可以讓另一台電腦上的 Agent 取得下列成果時才完成：

- 故事生產主檔；
- 初版與選定版影視劇本；
- 對白 Bible 與對白軌；
- 三份全文評審；
- 場景表；
- 鏡頭表；
- 60–90 秒影片 Pilot 規格；
- 字幕；
- 地區語言說明；
- 未來圖片／影片交接；
- 程式與測試（如有）；
- 最終報告；
- 所有重要 SHA-256；
- 最終 Branch 與 Commit。

本機 SQLite 有資料但未輸出／未 Commit，不算完成。

## 14. 直接交給 Codex 的命令

```text
先執行 git fetch --prune origin。
讀取最新遠端預設分支的 PROJECT_STATE.md，依其中必讀順序執行。
從最新遠端預設分支建立乾淨的 codex/screen01-video-first-pilot 分支與 worktree。
完整執行 SCREEN-01，將影視劇本、自然華語對白、評審、場景表、鏡頭表、60–90 秒 Pilot 規格、字幕、語言發行說明、未來媒體交接、程式、測試與報告全部 Commit 並 Push 到 GitHub。
GitHub 遠端是團隊最高權威；不得把必要成果只留在本機、SQLite、out 目錄或終端輸出。
一般工程與整合使用 gpt-5.6-sol / medium；限定的劇本與對白工作需要時使用 high；不得全程 Max／Ultra。
除 PROJECT_STATE.md 的真正停止條件外，不要中途詢問。
不要生成圖片、影片，不要呼叫或購買 Provider，不要升格 Canon，不要開始第二章小說或互動遊戲功能。
完成後 Push 並一次回報。
```
