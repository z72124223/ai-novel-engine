# STORY-01 Step 1 — Chapter lifecycle

日期：2026-07-12

## 完成內容

- 新增 additive migration `013_story_chapter_production.sql`，未修改 001–012。
- 新增 `story_chapter_productions` 與 `story_chapter_reviews`。
- 評審角色固定為 `LOGIC`、`CHARACTER`、`READER`，同一 production 不可重複角色或 reviewer instance。
- 初稿、修訂稿與選定稿都以 SHA-256 綁定；source packet Hash 不符時拒絕 intake。
- 修訂次數由 Repository 與 SQL CHECK 共同限制為 0–1。
- 選稿必須有具名人工 `APPROVE_PILOT_DRAFT`，且永遠保持 `NON_CANON_PILOT`。
- 三份評審使用單一 transaction 寫入；任一 insert 失敗時全部 rollback，production 狀態不前進。

## 驗證

```text
./gradlew.bat :app:test --tests com.localnovel.engine.StoryChapterProductionRepositoryTest --no-daemon
BUILD SUCCESSFUL
3 tests passed
```

## 範圍

沒有修改 Canon、圖片、影片、外部 Provider、既有 Migration 或原髒工作樹。
