# AI Novel Engine — 現在狀態

> **這是唯一現況入口。** 新的 GPT、Codex、Reviewer 或其他 Agent 必須先讀本檔。舊 REBOOT、SCREEN-01、圖片 Work Package、HANDOFF、README 狀態與 `PLANS.md` 舊標題都是歷史資料，不能蓋過本檔。

## Current Override — ENGINE-RULES-01 / DIALOGUE-02

- Status: `ENGINE_RULES01_COMPLETE_MERGED`
- Current work: `ENGINE_RULES01_DIALOGUE02`
- Collaboration: `GPT_PLANS_CODEX_IMPLEMENTS`
- Implementation branch: `codex/engine-rules01-dialogue02`
- Goal: `ENGINE_ENFORCED_PRODUCTION_RULES`
- Key rule: bad drafts may exist, but any BLOCKER prevents final selection.
- Mandatory negative fixture: `好。先碰你。`
- Required gate: `docs/CURRENT_ROUTE_GATE_V7.md`
- Required charter: `docs/ENGINE_ENFORCED_PRODUCTION_RULES_V1.md`
- Required work package: `docs/work-packages/ENGINE-RULES-01_DIALOGUE-02.md`
- DIALOGUE-01 is historical evidence and must be re-evaluated, not silently trusted.
- Rule Set: `engine-production-rules@1.0.0`
- Initial Artifact: `NOT_SELECTABLE`, SHA-256 `805cb62cd19ab42ca10df6b98cdde315fb6580a6d098ded7e235cd178f28a0a9`, blockers `9`.
- Revised Artifact: `SELECTED`, SHA-256 `0d7c137a5180d87f32f88d8a42b9475ec8a6a4e12b185ca1abedbebd0b69c0f2`, blockers `0`, warnings `1`.
- Verification: focused PASS; full suite `872/872` PASS; High review `PASS_WITH_RISKS`, release blockers `NONE`.
- Completion classification: `ENGINE_RULES01_PASS_WITH_SEMANTIC_WARNINGS`.
- Implementation commits: `31f7072da143559c6a1280b3631ea2f70cad404d`, `8064d3746c1a8454bf1d0ba432537249a7ed124e`.
- Remote default branch contains the complete implementation and evidence through `8064d3746c1a8454bf1d0ba432537249a7ed124e`; the final governance-only commit is the Git commit containing this state update.
- No next work package is active. Do not automatically start images, video, Provider, Canon, next chapter or interactive game.
- GitHub remote is the highest team authority.
- Do not start image, video, Provider, Canon, next chapter or interactive game.

## Future Blueprint Candidate — Local Hybrid Retrieval

- Blueprint: `docs/LOCAL_HYBRID_RETRIEVAL_KNOWLEDGE_BLUEPRINT_V1.md`
- Status: `DOCS_ONLY_FUTURE_CANDIDATE_NOT_ACTIVE`
- Mandatory first candidate: `RETRIEVAL-00 — Authority, Rebuild And Retention Foundation`
- Work package: `docs/work-packages/RETRIEVAL-00_AUTHORITY_REBUILD_RETENTION_FOUNDATION.md`
- `RETRIEVAL-01 — Offline Local Dual-Model Retrieval Benchmark` is blocked until RETRIEVAL-00 is explicitly assigned, implemented, verified and accepted.
- Intended route: deterministic import and optional local extraction → SQL/FTS/BM25 exact retrieval → Embedding candidate recall → Reranker ordering → a small visibility-safe evidence packet for downstream GPT use.
- Formal SQLite state, Canon, `AiTurnResultValidator`, `StateReducer`, character knowledge visibility, `contextHash`, `stateVersion`, and transaction boundaries remain authoritative and unchanged.
- No retrieval model, extraction model, GBrain component, MCP service, vector database, provider, or API has been downloaded, installed, approved, or activated.
- This candidate does not create an active next work package. A future explicit approval must activate `RETRIEVAL-00`; RETRIEVAL-00 completion still does not automatically activate RETRIEVAL-01. Until then, the current completed ENGINE-RULES state and all existing stop boundaries remain in force.

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

GitHub 遠端 Repository 是多人團隊協作、版本、交接、批准紀錄與長期記憶的最高權威。

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

正式交接必須包含：

```text
Repository
Branch
Commit SHA
Repo-relative Path
Content SHA-256（需要時）
```

私人絕對路徑、未 Push 工作樹和「我本機最新版」不是正式交接。

## 3. SCREEN-01 已完成

SCREEN-01 已完成並納入目前預設分支。

Selected screenplay：

```text
samples/screen01/screen01_screenplay_revised_zh-Hant-TW.md
```

SHA-256：

```text
4b2ac13f84541dfa904096fe650ed99a9f8d58e1519ba9f0b0816d85e56a554c
```

Selected dialogue：

```text
samples/screen01/screen01_dialogue_track_revised_zh-Hant-TW.json
```

SHA-256：

```text
6936ec2099f5a61ab07db631e9d19ef2bb49a8154f572fb5f360e0f7fdcec65c
```

Boundary：

```text
NON_CANON_PILOT
```

SCREEN-01 的四場、三十一鏡、82 秒 Pilot 與歷史版本不得被 DIALOGUE-01 覆寫。

## 4. 現在唯一工作包

```text
DIALOGUE-01 V2 — 華語真人對白定稿
```

狀態：

```text
DIALOGUE01_COMPLETE_MERGED
```

Codex 實作分支：

```text
codex/dialogue01-human-voice-finalization
```

Codex 開始前必須：

1. `git fetch --prune origin`
2. 從最新遠端預設分支建立乾淨 Worktree／Branch
3. 記錄 Base SHA
4. 完整執行 DIALOGUE-01 V2
5. 所有成果 Commit／Push
6. 測試通過後安全合併預設分支
7. 一次最終回報

## 5. 必讀順序

1. `PROJECT_STATE.md`
2. `.codex/dialogue01_controller.md`
3. `docs/CURRENT_ROUTE_GATE_V6.md`
4. `docs/GPT_CODEX_COLLABORATION_V2.md`
5. `docs/work-packages/DIALOGUE-01_HUMAN_CHINESE_DIALOGUE_FINALIZATION_V2.md`
6. `docs/handoffs/DIALOGUE01_CODEX_ONLY_HANDOFF.md`
7. `docs/handoffs/DIALOGUE01_MERGE_PLAN_V2.md`
8. `samples/screen01/screen01_screenplay_revised_zh-Hant-TW.md`
9. `samples/screen01/screen01_dialogue_track_revised_zh-Hant-TW.json`
10. `samples/screen01/screen01_dialogue_bible_zh-Hant-TW.json`
11. `samples/screen01/screen01_shot_list.json`
12. `samples/screen01/screen01_video_pilot_60_90s.json`
13. `samples/screen01/screen01_video_pilot_zh-Hant-TW.srt`
14. 只有 DIALOGUE-01 真正需要的程式、測試和資料

## 6. 對白方向

母稿：

```text
繁體中文 / zh-Hant-TW
```

陸塵行口語基礎：

```text
自然台灣華語
```

同時讓中國大陸、香港、馬來西亞、新加坡及其他華語讀者容易理解。

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
- 未完整回答；
- 誤會；
- 沉默；
- 動作代替解釋；
- 每個角色有不同語氣。

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

## 8. 模型

一般工程與整合：

```text
gpt-5.6-sol / medium
```

對白初次修改與唯一修訂，可在限定範圍使用：

```text
gpt-5.6-sol / high
```

三個獨立評審：

```text
gpt-5.6-sol / medium
```

搜尋與測試分析使用 Terra Low／Medium；Hash、JSON、字幕時間、Diff 和統計使用程式。

Max／Ultra 禁止。

## 9. 現在禁止

DIALOGUE-01 期間不得：

- 使用獨立 WORK；
- 覆寫 SCREEN-01 歷史版本；
- 改主要劇情；
- 讓主角故意變笨；
- 第二次自動修訂；
- 寫下一章；
- 生成圖片；
- 生成影片；
- 呼叫或購買 Provider；
- 升格 Canon；
- 決定永久死亡或戀愛結果；
- 新增互動 RPG 功能；
- 公開部署網站；
- 把必要成果只放在本機；
- force push。

## 10. 真正停止條件

只有以下情況 Codex 才停下詢問：

- 必須決定正式 Canon；
- 必須決定永久角色命運或戀愛；
- 必須決定正式美術；
- 必須花錢、使用 Provider 或 API Key；
- 無法避免破壞性 Git／資料操作；
- 必須弱化 Transaction、Knowledge、Hash 或批准邊界；
- 必須 force push；
- 修改核心情節才能解決對白問題；
- 同一核心問題經一次 Medium 和一次 High 有證據修復仍失敗。

## 11. 完成條件

DIALOGUE-01 只有在遠端 Git 可以讓另一台電腦上的 Agent 取得下列成果時才完成：

- 31 句逐句稽核；
- AI 模擬朗讀紀錄；
- 三份獨立評審；
- 最終影視劇本對白版；
- 最終對白軌；
- 最終 82 秒 SRT；
- Selection Decision；
- Hash Manifest；
- Video Handoff Overlay；
- 測試與報告；
- 最終 Branch／Commit／Merge SHA。

## 12. 直接交給 Codex 的命令

```text
先執行 git fetch --prune origin。
讀取最新遠端預設分支的 PROJECT_STATE.md，依其中必讀順序執行。
從最新遠端預設分支建立乾淨的 codex/dialogue01-human-voice-finalization 分支與 Worktree。
完整執行 DIALOGUE-01 V2：逐句稽核 31 句對白、AI 模擬朗讀、三個獨立 Codex 子評審、最多一次修訂，並同步更新最終影視劇本、對白軌、Shot 引用、82 秒 SRT、Selection、Hash 與 Git Handoff。
所有成果都必須 Commit 並 Push 到 GitHub。GitHub 遠端是團隊最高權威，不得把必要成果只留在本機、SQLite、out 或終端輸出。
一般工程使用 gpt-5.6-sol / medium；對白初次修改與唯一修訂需要時使用 high；三個評審使用 medium；不得使用 Max／Ultra。
除 PROJECT_STATE.md 的真正停止條件外，不要中途詢問。
不要使用 WORK，不要改主要劇情，不要讓陸塵行故意變笨，不要生成圖片／影片，不要呼叫 Provider，不要升格 Canon，不要寫下一章或新增互動遊戲。
完成實作、驗證、Push 並安全合併預設分支後，一次回報。
```
