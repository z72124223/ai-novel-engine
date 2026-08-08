# STORY-01 Step 2 — File handoff CLI

日期：2026-07-12

## 完成內容

- `create-story-chapter-handoff` 從 SQL World Source 查詢與 `reboot_run_evidence` Runtime Context 建立交接包。
- 交接包完整 JSON 計算 SHA-256，並在建立 production 時一併鎖定。
- 新增初稿、三評審、單次修訂、pilot 選定與狀態查詢 CLI。
- CLI 僅讀寫本機檔案協議與 SQLite；沒有連接任何模型 API。
- `StoryChapterFullTextReview` 要求評審保存全文摘要、原文引用、優點、問題、必要修改與 scorecard，不使用 Boolean 假裝全文評分。

## 驗證

```text
./gradlew.bat :app:test --tests com.localnovel.engine.StoryChapterProductionRepositoryTest --tests com.localnovel.engine.StoryChapterProductionCliTest --no-daemon
BUILD SUCCESSFUL
4 tests passed
```

## 範圍

沒有修改 Canon、圖片、影片、Provider 或原髒工作樹。
