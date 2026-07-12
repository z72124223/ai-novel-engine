# AI Novel Engine Development Journal

Status: `ACTIVE_PROJECT_MEMORY`

## 用途

本目錄保存專案的重要方向、失敗、批評、修正與實作證據，避免新的 GPT／Codex 只看到最近一份文件就偏離最初目標。

Journal 是歷史與意圖記憶，不是新的審核門檻。

目前狀態仍以：

```text
PROJECT_STATE.md
```

為最高入口。

## 現在先讀

1. `PROJECT_STATE.md`
2. `docs/CURRENT_ROUTE_GATE_V4.md`
3. `docs/VIDEO_FIRST_GIT_FIRST_PRODUCTION_CHARTER_V1.md`
4. `docs/work-packages/SCREEN-01_VIDEO_FIRST_SCRIPT_AND_DIALOGUE_PILOT.md`
5. 本 Journal

## 主要長期紀錄

### `2026-07-11_PROJECT_DISCUSSION_ARCHIVE_V1.md`

保存公開 GitHub 專案比較、治理、髒工作樹、引擎、圖片、影片、成本、多代理、Web 與 Reboot 的長篇重建紀錄。

### `2026-07-11_CREATIVE_WORLD_AND_AUTONOMOUS_EXECUTION_ADDENDUM_V1.md`

保存情緒節奏、NPC 自主性、主角成長、無單一終局、世界資料落地、模型成本與 Web 等決策。

### `2026-07-11_CREATIVE_VOICE_AND_AUTONOMOUS_EXECUTION_UPDATE_V1.md`

保存毒舌、反作死、公平意外、一次啟動 Codex 做到完，以及未來影片 Provider Bake-off。

### `2026-07-12_VIDEO_FIRST_GIT_FIRST_DIRECTION_V1.md`

保存最新且最重要的產品修正：

- 最終主要產品是影片／動畫，不是小說；
- STORY-01 是文字試作，不是最終格式；
- 陸塵行可以保持聰明，主要成長放在新世界適應、權力與責任；
- 不靠角色降智推劇情；
- 對話要像華人真人，不像兩個 AI 發表完整論點；
- 母稿使用繁體中文與自然台灣華語；
- 同時考慮中國大陸、香港、馬來西亞、新加坡等華人市場；
- GitHub 遠端是多人團隊最高協作與交接權威；
- 本機、SQLite、`out/**` 和終端只是執行環境；
- SCREEN-01 會把 STORY-01 轉成影視劇本、對白、場景與鏡頭資料。

## REBOOT-01 執行紀錄

路徑：

```text
docs/development_journal/reboot01/
```

包括：

- Stage 0 真實引擎基線；
- Stage 1 Evidence 與門檻簡化；
- Stage 2 NPC 自主性；
- Stage 3 Drama／Critics；
- Stage 4 六回合垂直切片；
- Stage 5 Replay；
- Stage 6 本機 Web；
- Stage 7 整合；
- Completion Audit。

## STORY-01 執行紀錄

路徑：

```text
docs/development_journal/story01/
```

包括：

- World Source 換行問題與正式匯入修復；
- 章節 SQL lifecycle；
- Git 檔案交接；
- 完整初稿；
- 三個獨立評審；
- 一次修訂；
- 作者工作台；
- 最終測試。

STORY-01 狀態：

```text
NON_CANON_PILOT
```

## SCREEN-01 執行紀錄

路徑：

```text
docs/development_journal/screen01/
```

目前已記錄：

- Base SHA；
- Git branch／Commit；
- Story Production Master；
- 影視劇本轉換；
- 對白 Bible 與對白軌；
- 三份華語／可拍性評審；
- 一次修訂；
- 場景與鏡頭表；
- 60–90 秒 Pilot 規格；
- 字幕與地區語言說明；
- Git handoff；
- 測試、失敗、模型與成本。

SCREEN-01 已建立 Story Production Master、初版／選定劇本、自然華語對白、三份獨立評審、唯一一次修訂、4 場／31 鏡、82 秒 Pilot、繁體中文字幕、區域語言矩陣與未來媒體交接。狀態以 `PROJECT_STATE.md` 與 `docs/screen01/SCREEN01_FINAL_REPORT.md` 為準。

## 未來追加規則

以下事情不得只留在聊天：

- 使用者改變產品終點；
- 重要角色／劇情／美術／語言方向；
- Git 協作規則；
- 世界資料與 Runtime 的真實差異；
- 模型／Provider／成本變更；
- 重大失敗；
- 舊規則退休；
- 真實內容品質發現；
- 評審與人類決策；
- 圖片／影片交接與批准。

## 完整性規則

- 不刪除使用者批評或失敗來美化歷史。
- 不寫 API Key、憑證、隱藏系統提示或私人推理。
- 無法取得的舊聊天要標示缺口，不假裝逐字完整。
- 關鍵成果必須有 Git Branch、Commit、相對路徑與必要 Hash。
- 如果成果只存在本機、SQLite 或 `out/**`，Journal 必須標記為尚未完成團隊交接。
