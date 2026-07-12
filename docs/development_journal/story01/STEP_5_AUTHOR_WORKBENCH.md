# STORY-01 Step 5 — Author workbench

日期：2026-07-12

## 完成內容

- 新增 `serve-story01-author-workbench --db <path> --production <id>`。
- Web 顯示初稿、修訂稿、三份全文評語、revision count、SQL status 與 `NON_CANON_PILOT` 邊界。
- 支援在工作台隔離資料庫中選定初稿／修訂稿、退回或要求人工決策。
- 啟動時以 SQLite `VACUUM INTO` 建立一致性暫存 snapshot；所有按鈕只修改副本，來源 DB 維持不變。
- 每次決策另寫出 Hash-bound decision artifact；正式來源 DB 只能經 `apply-story-chapter-decision-artifact` 驗證後套用。
- 僅綁定 loopback，拒絕 foreign Host／Origin，限制 request body，停止後刪除暫存 DB。
- 頁面明確標示不會升格 Canon、發布或觸發圖片。

## 驗證

```text
./gradlew.bat :app:test --tests com.localnovel.engine.StoryChapterAuthorWorkbenchServerTest --no-daemon
BUILD SUCCESSFUL
```

測試實證：頁面與 API 可讀三份評語；選定修訂稿後隔離 DB 成為 `SELECTED_PILOT_DRAFT`，來源 DB 仍為 `REVISION_INGESTED`；foreign Host／Origin 403；close 後暫存工作區不存在。
