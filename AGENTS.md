# AGENTS.md — SCREEN-01 現行規則

## 1. 先讀什麼

每個 Agent 依序讀：

1. `PROJECT_STATE.md`
2. `.codex/project_controller.md`
3. `docs/CURRENT_ROUTE_GATE_V4.md`
4. `docs/VIDEO_FIRST_GIT_FIRST_PRODUCTION_CHARTER_V1.md`
5. `docs/work-packages/SCREEN-01_VIDEO_FIRST_SCRIPT_AND_DIALOGUE_PILOT.md`
6. `docs/CODEX_MODEL_AND_BUDGET_POLICY_V2.md`
7. `docs/development_journal/INDEX.md`
8. 本檔
9. `samples/story01/story01_revised_draft.md`
10. SCREEN-01 必要程式、測試與資料

舊 REBOOT、圖片 Work Package、Route Gate V1–V3、HANDOFF、README 狀態和歷史 `PLANS.md` 只作歷史證據。

## 2. 現在目標

```text
STORY-01 小說試作
→ 故事生產主檔
→ 繁體中文影視劇本
→ 自然華語對白
→ 場景表
→ 鏡頭表
→ 60–90 秒影片 Pilot 規格
→ 未來圖片／影片交接
```

現在不生成圖片或影片。

## 3. 最終產品

主要終點是動畫／影片。

小說是故事來源、測試或衍生品，不是唯一最終產品。

不得把 SCREEN-01 做成第二次小說生產工作。

## 4. GitHub 優先

GitHub 遠端是團隊最高權威。

本機電腦、Worktree、SQLite、`out/**`、終端和模型 Session 都是可替換執行環境。

重要成果必須 Commit／Push：

- 故事主檔；
- 劇本；
- 對白；
- 場景／鏡頭；
- 評審；
- 修訂；
- 字幕；
- 決策；
- 媒體交接；
- Hash；
- 報告與測試。

交接使用：

```text
Repository + Branch + Commit SHA + Repo-relative Path + SHA-256
```

禁止以本機絕對路徑、未 Push Worktree 或「本機最新版」作正式交接。

## 5. 分支與 Git 安全

- 先 `git fetch --prune origin`。
- 從最新遠端預設分支建立乾淨 `codex/screen01-video-first-pilot`。
- 記錄 Base SHA。
- 保留舊髒工作樹。
- 使用聚焦 Commit。
- 檢查 Diff 並執行 `git diff --check`。

預設禁止：

```text
git add .
git add -A
git commit -a
git push --force
git push --force-with-lease
git reset --hard
git clean -fd
```

## 6. 主角

陸塵行可以保持聰明、有能力與可靠。

禁止靠角色降智推劇情。

他的成長重點是新世界適應：

- 文化；
- 制度；
- 階級；
- 人情；
- 力量規則；
- 政治；
- 責任；
- 從保護自己／愛醬到承擔更大局勢。

戲劇來自資訊不足、價值兩難、對手學習和正確選擇的代價。

## 7. 語言

正式母稿：

```text
Traditional Chinese / zh-Hant-TW
```

陸塵行口語：自然台灣華語。

同時要讓台灣、中國大陸、香港、馬來西亞、新加坡等華語讀者容易理解。

### 禁止 AI 式對話

- 完整論文式對話；
- 兩人輪流說對稱觀點；
- 每句都解釋價值；
- 每句都像金句；
- 全員同一聲線；
- 危急時說長句；
- 過多「不是 A，而是 B」；
- 同一母稿亂混地區詞彙。

### 優先

- 短句；
- 打斷；
- 停頓；
- 未完整回答；
- 誤解；
- 逃避；
- 沉默；
- 動作代替說明；
- 不同年齡、職業、地區和關係有不同口氣。

## 8. SCREEN-01 必要成果

追蹤檔案至少包括：

```text
samples/screen01/screen01_story_production_master.json
samples/screen01/screen01_screenplay_initial_zh-Hant-TW.md
samples/screen01/screen01_dialogue_bible_zh-Hant-TW.json
samples/screen01/screen01_dialogue_track_initial_zh-Hant-TW.json
samples/screen01/reviews/*.json
samples/screen01/screen01_screenplay_revised_zh-Hant-TW.md
samples/screen01/screen01_dialogue_track_revised_zh-Hant-TW.json
samples/screen01/screen01_scene_list.json
samples/screen01/screen01_shot_list.json
samples/screen01/screen01_video_pilot_60_90s.json
samples/screen01/screen01_video_pilot_zh-Hant-TW.srt
samples/screen01/screen01_locale_release_matrix.json
samples/screen01/screen01_video_handoff.json
docs/screen01/*.md
```

若增加 SQL／作者工作台，選定結果也必須輸出成 Git 追蹤檔案。

## 9. 三個獨立評審

### 台灣口語評審

檢查真實台灣華語、角色聲線、急迫對話與 AI 味。

### 泛華語可讀性評審

檢查台灣、中國大陸、香港、馬來西亞／新加坡的理解與需要在地化的詞。

### 可表演／可拍性評審

檢查演員能否自然說出口、對白時長、動作與對話是否重複，以及資訊能否用畫面呈現。

評審必須綁定來源 Hash、引用原文並給具體修改。

最多一次影視劇本／對白修訂。

## 10. 模型

一般工程：

```text
gpt-5.6-sol / medium
```

限定劇本／對白生成與修訂可按需要：

```text
gpt-5.6-sol / high
```

三個評審：

```text
gpt-5.6-sol / medium
```

搜尋／測試：Terra Low／Medium。

Hash、JSON、字幕、Diff、統計：程式工具。

Max／Ultra 預設禁止。

## 11. 核心安全

仍然保留：

- SQLite Transaction；
- `stateVersion`／`contextHash`／idempotency；
- Knowledge Boundary；
- project/source allowlist；
- source／content Hash；
- Canon 與正式資產人類批准；
- API Key 不進 Git；
- AI 不直接寫正式 Canon。

## 12. 現在禁止

- 第二、第三章小說；
- 第二次修改 STORY-01 小說；
- 圖片生成；
- 影片生成；
- Provider/API 花費；
- Provider 冠軍決定；
- Canon 升格；
- 永久死亡／戀愛決定；
- 互動 RPG；
- 公開部署；
- 本機獨有的重要成果。

## 13. 停止條件

只在以下情況停止：

- 必須決定正式 Canon；
- 必須決定永久命運／戀愛；
- 必須決定正式美術；
- 必須使用付費 Provider/API Key；
- 無法避免破壞性 Git／資料操作；
- 必須弱化 Transaction、Knowledge、Hash 或批准安全；
- 同一核心問題經 Medium、High 各一次有證據修復仍失敗。

普通劇本、對白、Shot 數、字幕、UI、格式、測試和 additive migration 由 Codex 自行決定。

## 14. 完成

完成後普通 Push：

```text
origin/codex/screen01-video-first-pilot
```

最終報告要提供：

- Base SHA；
- Final SHA；
- Commit chain；
- 重要檔案路徑與 SHA-256；
- 選定劇本／對白；
- 三份評審；
- 場景／鏡頭／Pilot／字幕；
- 測試；
- 模型／High 使用；
- Canon、圖片、影片、Provider 變更數；
- 剩餘問題與下一步。

未 Push 的本機成果不算完成。
