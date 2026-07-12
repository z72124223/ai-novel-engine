# Project Controller — SCREEN-01 Video-First / Git-First

## 1. 角色

Codex CLI 是 `AI Novel Engine` 的主要工程執行者。

使用者保留：

- 最終產品方向；
- 正式 Canon；
- 永久角色命運與戀愛；
- 正式美術；
- Provider／API／預算；
- 最終影片批准。

Codex 現在只執行：

```text
SCREEN-01 — 影片優先影視劇本與華語對白試作
```

## 2. 必讀順序

每次開始先讀：

1. `PROJECT_STATE.md`
2. `.codex/project_controller.md`
3. `docs/CURRENT_ROUTE_GATE_V4.md`
4. `docs/VIDEO_FIRST_GIT_FIRST_PRODUCTION_CHARTER_V1.md`
5. `docs/work-packages/SCREEN-01_VIDEO_FIRST_SCRIPT_AND_DIALOGUE_PILOT.md`
6. `docs/CODEX_MODEL_AND_BUDGET_POLICY_V2.md`
7. `docs/development_journal/INDEX.md`
8. `AGENTS.md`
9. `samples/story01/story01_revised_draft.md`
10. 只有 SCREEN-01 需要的程式、測試與資料

舊 REBOOT Work Package、圖片 Work Package、HANDOFF、Route Gate V1–V3、README 狀態與舊 `PLANS.md` 都是歷史資料。

## 3. GitHub 最高權威

GitHub 遠端 Repository 是多人團隊的最高工作場所。

本機 Worktree、終端、SQLite、`out/**` 和暫存資料只是可替換執行環境。

所有重要成果必須 Commit／Push：

- Story Production Master；
- 影視劇本；
- 對白 Bible 與對白軌；
- 場景表；
- 鏡頭表；
- 評審；
- 修訂；
- 字幕；
- 作者決策；
- 媒體交接包；
- Hash；
- 測試與報告。

若資料只存在本機 SQLite，必須輸出成可讀、可追蹤的 Git 檔案，才算交付。

所有交接使用：

```text
Repository
Branch
Commit SHA
Repo-relative Path
Content SHA-256
```

禁止以私人絕對路徑或未 Push 工作樹作為團隊權威。

## 4. 分支

開始前：

1. `git fetch --prune origin`
2. 確認遠端預設分支
3. 記錄確切 Base SHA
4. 建立乾淨分支／Worktree：

```text
codex/screen01-video-first-pilot
```

不得在舊 123 項髒工作樹直接實作。

不得 force push、hard reset、clean 或廣泛 stage。

## 5. 最終產品

主要終點是影片／動畫。

```text
世界資料
→ 故事生產主檔
→ 影視劇本
→ 對白／場景／鏡頭
→ 圖片固定資產
→ 關鍵影格
→ 影片鏡頭
→ 完整影片
```

小說是來源、測試與可選衍生物，不是終點。

SCREEN-01 不得再生成第二、第三章小說。

## 6. 主角規則

陸塵行可以保持聰明、可靠與有能力。

不得為了戲劇強迫他：

- 忘記資訊；
- 忽略明顯危險；
- 重複無效行為；
- 突然失去專業能力；
- 做沒有理由的愚蠢決定。

他的主要成長在新世界適應：文化、制度、力量、政治、人情、責任與更大規模的選擇。

衝突來自正確決定的代價、資訊不足、價值兩難與對手適應，不靠降智。

## 7. 語言與對白

母稿：

```text
Traditional Chinese / zh-Hant-TW
```

陸塵行：自然台灣華語。

同時兼顧中國大陸、香港、馬來西亞、新加坡等華語讀者的理解。

禁止 AI 對話：

- 兩人輪流發表完整論點；
- 每句都總結價值；
- 每句都像金句；
- 全員同一語氣；
- 危急時說長句；
- 過多工整否定；
- 母稿亂混地區詞彙。

優先使用短句、打斷、停頓、沉默、誤解、逃避與動作表達。

## 8. 執行內容

Codex 要把 STORY-01 修訂稿轉成：

- Story Production Master；
- 初版繁體中文影視劇本；
- 對白 Bible；
- 初版對白軌；
- 台灣口語評審；
- 泛華語可讀性評審；
- 可表演／可拍性評審；
- 最多一次影視劇本／對白修訂；
- 場景表；
- 鏡頭表；
- 60–90 秒影片 Pilot 規格；
- 繁體中文字幕；
- 地區發行說明；
- 未來圖片／影片交接；
- 必要程式、測試與作者工作台；
- 最終報告。

所有成果 Push 到 GitHub。

## 9. 模型

一般工程：

```text
gpt-5.6-sol / medium
```

限定的劇本／對白生成與修訂，需要品質時可用：

```text
gpt-5.6-sol / high
```

三個獨立評審：

```text
gpt-5.6-sol / medium
```

搜尋與測試使用 Terra Low／Medium；Hash、JSON、字幕時間、Diff、統計使用程式。

Max／Ultra 預設禁止。

## 10. 寫入與審查

- 核心 Kotlin／SQL 同一時間只有主 Agent 一個 Writer。
- Subagents 主要做搜尋、測試與評審。
- 評審必須綁定來源 Hash，引用原文。
- 最多一次影視劇本／對白修訂。
- STORY-01 小說來源保持不變。
- 不得用文件存在冒充實作完成。

## 11. 現在禁止

不得：

- 生成圖片；
- 生成影片；
- 呼叫或購買 Provider；
- 選定 Provider 冠軍；
- 升格 Canon；
- 修改永久命運／戀愛；
- 寫第二、第三章小說；
- 再修改 STORY-01 小說；
- 加互動 RPG；
- 公開部署；
- 把必要成果只留本機。

## 12. 真正停止條件

只有以下情況停止詢問：

- 必須決定正式 Canon；
- 必須決定永久命運／戀愛；
- 必須決定正式美術；
- 必須花錢或使用 API Key；
- 無法避免破壞性 Git／資料操作；
- 必須弱化 Transaction、Knowledge、Hash 或批准安全；
- 同一核心阻塞經一次 Medium、一次 High 有證據修復仍失敗。

普通對白選擇、鏡頭數、字幕時間、檔案格式、測試或 UI 設計不是停止條件。

## 13. 完成方式

逐階段完成、驗證、Commit，最後普通 Push：

```text
origin/codex/screen01-video-first-pilot
```

最終一次回報：

- Base SHA；
- Branch；
- Commit chain；
- 選定劇本與對白；
- 三份評審；
- 場景／鏡頭／Pilot／字幕；
- 所有重要路徑與 SHA-256；
- 測試；
- 模型與 High 使用；
- Canon／圖片／影片／Provider 變更數；
- 剩餘問題；
- 下一步建議。

未 Push 的本機成果不算完成。
