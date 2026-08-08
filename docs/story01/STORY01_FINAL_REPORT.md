# STORY-01 Final Report

日期：2026-07-12
分支：`codex/story01-real-chapter-pilot`

## 結論

STORY-01 已完成工程與真實內容生產測試。這次不再以固定六句 Scenario 或 Boolean critic 宣稱故事能力，而是：

1. 實跑 Chenxing 隔離 SQLite Runtime。
2. 匯入 1 archive／9 documents／255 World Source chunks。
3. 從已套用 Runtime evidence 與官方來源 allowlist 建立 Hash-bound 交接包。
4. Codex 閱讀交接包後產生 2,941 字元完整中文初稿〈潮線之外〉。
5. 三個隔離 reviewer 實際閱讀全文並引用原句。
6. 依 LOGIC／READER 的具體問題做唯一一次局部修訂。
7. 保存初稿、三評語、修訂稿與 selected pilot 到 SQLite lifecycle。
8. 提供本機作者工作台與耐久、Hash-bound 決策 artifact。

工程結果：`PASS`。
產品結果：`PILOT_READY_FOR_HUMAN_REVIEW`，不是 Canon 或正式出版 PASS。

## 內容證據

| Artifact | Evidence |
|---|---|
| Source packet | 43,016 bytes；SHA-256 `12449236e779f8a8b2a8c1281e0e95ec52add641fc526eff4e436ab82d1e4a01` |
| Initial draft | 2,941 chars；SHA-256 `05f0d58935c73018295ce7c8ac2c2e16389d2dd220a2234605b982641012027e` |
| Logic review | `REVISE`／76；7 findings；引用全文原句 |
| Character review | `PASS`／96；角色聲線、愛醬主體性與秘密邊界通過 |
| Reader review | `REVISE`／86；開場、節奏與續讀意願高，要求連續性與自然度修正 |
| Revised draft | 2,978 chars；SHA-256 `ea0e6e148377947907e68883e08a3864b113acaa0c7b9fd30df2408bd1145e91` |
| SQL final | `SELECTED_PILOT_DRAFT`／3 reviews／revision 1／`NON_CANON_PILOT` |

## High review 修正

- World Source query 綁定 project label、official authority 與每一查詢的 source ID allowlist，並有跨專案同詞負向測試。
- Runtime evidence 限定 `APPLIED` 與 `PASS|QUALITY_WARNING`，驗證 project、session、runtime seed、Context JSON 與 Context Hash。
- 所有 review intake 都必須是完整 `StoryChapterFullTextReview`，角色／instance／draft hash／verdict 一致，至少三段引用且引用實際存在於初稿。
- 有 `REVISE` 時禁止以相同 Hash 冒充修訂。
- Web 使用 SQLite `VACUUM INTO` 取得一致性 snapshot，不直接複製可能含 WAL 的主檔。
- Web 決策輸出可持久化 Hash-bound artifact；正式來源 DB 由獨立 CLI 驗證 artifact 後才套用。
- Migration 013 加入跨欄位 CHECK，拒絕空 selected draft、空人工 actor 或 revision count 不一致等不可能狀態。

## 驗證

```text
Focused STORY-01 tests: PASS
Full Gradle suite: 849 tests, 0 failures, 0 errors, 0 skipped
World Source import: PASS, 9 documents, 255 chunks, 0 findings
SQL lifecycle E2E: DRAFT_INGESTED → REVIEWS_COMPLETE → REVISION_INGESTED → SELECTED_PILOT_DRAFT
JSON parse: PASS
git diff --check: PASS
Original dirty worktree: HEAD 89e522f, status 123, unchanged
```

## 誠實限制

- 引擎沒有接模型 API；本次由 Codex desktop 透過 file handoff 實際產生全文。
- 三位 reviewer 的隔離由三個獨立 agent 執行；本機檔案可證明內容、Hash 與 instance ID，但不是外部身分簽章服務。
- 修訂稿未經使用者逐段閱讀，不得稱為正式採用。
- 作者工作台只在本機 loopback 運作，不是公開 Web 產品。
- 圖片、影片與 Provider 仍暫停。

## 下一步

只允許 GPT／產品負責人審查修訂稿與此分支。沒有新工作包前，不得自動開始第二次修訂、WP-002、圖片或影片。
