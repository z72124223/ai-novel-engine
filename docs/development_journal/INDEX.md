# AI Novel Engine Development Journal

Status: `ACTIVE_PROJECT_MEMORY`

## Purpose

本目錄保存專案從最初目標、公開 GitHub 專案比較、架構與世界觀討論、GitHub／Codex 協作、治理失衡、圖片／影片路線、成本、多代理、Web、使用者批評、失敗教訓，到 2026 Playable Depth Reboot 的長期決策記憶。

新的 GPT、Codex 或協作者若需要理解「為什麼專案變成現在這樣」，先讀：

1. `PROJECT_STATE.md`
2. `docs/CURRENT_ROUTE_GATE_V3.md`
3. `docs/work-packages/REBOOT-01_PLAYABLE_DEPTH_SLICE.md`
4. `docs/work-packages/REBOOT-01_EXECUTION_ADDENDUM_V1.md`
5. `docs/REBOOT_2026_PLAYABLE_DEPTH_BLUEPRINT_V1.md`
6. `docs/REBOOT_2026_CREATIVE_WORLD_EXECUTION_ADDENDUM_V1.md`
7. `docs/REBOOT_2026_NARRATIVE_VOICE_AND_UNPREDICTABILITY_ADDENDUM_V1.md`
8. `docs/CODEX_MODEL_AND_BUDGET_POLICY_V2.md`
9. 本目錄中的 Journal

Journal 是歷史、意圖與失敗證據，不直接覆蓋目前 Work Package。

## Entries

### `2026-07-11_PROJECT_DISCUSSION_ARCHIVE_V1.md`

從公開專案吸收、治理重整、髒工作樹修復、引擎優缺點、圖片 Location Bible、Grok／影片、模型成本、多代理、Web、敘事深度到 Playable Depth Reboot 的長篇重建紀錄。

這是可取得之使用者可見對話、Codex 回報與 Git 證據的詳細重建，不是假裝由平台匯出的逐字完整聊天，也不包含隱藏推理、系統提示或憑證。

### `2026-07-11_CREATIVE_WORLD_AND_AUTONOMOUS_EXECUTION_ADDENDUM_V1.md`

補充記錄：

- 使用者要求 Codex 不再每階段回 GPT，而是完成整個 Umbrella Work Package；
- Codex 預設 `gpt-5.6-sol / medium`，High 有條件升級，Max 預設禁止；
- 使用 Subagents 做探索、測試、Critic 與成本稽核，核心程式維持單一 Writer；
- 歡快、溫暖、戀愛、憤怒、反思與落淚的情緒波形；
- 主角逐步變強並能左右局勢，但責任、代價與道德壓力同步上升；
- 局部收束、全球延續的無單一最終終局結構；
- 主角毒舌、吐槽、風險辨識與嚴肅場景收斂；
- Audience Expectation Ledger、公平意外與 `IDIOT_PLOT_RISK`；
- 世界觀不能只存在文件，必須檢查正式來源、SQL、Context、Runtime 與 Scenario 真實使用；
- 非 Canon World Patch Candidate 必須進可重置 Sandbox 並在真實回合中生效；
- 本機 Web、未來影片 Provider Bake-off 與完整日誌保存要求。

### `2026-07-11_CREATIVE_VOICE_AND_AUTONOMOUS_EXECUTION_UPDATE_V1.md`

保存同一最新方向的快速執行版，特別強調：

- 一次啟動 Codex 並做到 Stage 7；
- Sol／Terra／Luna 分級成本策略；
- 吐槽不是第四牆全知；
- 角色作死防護與公平二階意外；
- 人物風格先於背景風格；
- YouTube 完整逐字稿無法驗證時不得冒充已看完；
- 未來 Gemini／Veo／Grok／Seedance／Runway 使用同 Keyframe 進行公平 Bake-off。

這份與上一份 Addendum 有部分重疊，但用途不同：上一份保存完整長誌，這份提供未來執行者快速恢復最新操作意圖。不得因此把任何一份當作 Current Route；Current Route 仍由 `PROJECT_STATE.md` 決定。

### `reboot01/`

REBOOT-01 執行期間，每個 Stage 都必須新增或更新實際運行日誌，包括：

- 真實命令；
- 測試與失敗；
- SQL／Runtime 發現；
- 模型與 Reasoning；
- Subagents；
- High 升級；
- 規則改動；
- 5–10 回合觀察；
- Replay 比較；
- Web 使用方式；
- 未解問題。

Current REBOOT-01 entries:

- `reboot01/STAGE_0_EXECUTABLE_BASELINE.md` — executable Kotlin/CLI/SQLite baseline, real Chenxing turn, World Source integrity classification, and world/character reality audit.
- `reboot01/STAGE_1_VALIDATION_AND_RUN_EVIDENCE.md` — explicit safety/quality classes, additive SQL run evidence, irreversible human approval boundary, transaction tests, and final high-reasoning review.
- `reboot01/STAGE_2_NPC_AUTONOMY.md` — sandbox-only off-screen NPC proposal loop, Game Master legality checks, atomic DecisionTrace, rejected evidence, and return-observable consequences.
- `reboot01/STAGE_3_DRAMA_AND_REVISION.md` — dramatic framing, expectation ledger, eleven critic dimensions, stall control, and one bounded narrative revision.

## Future Append Rule

每次出現下列事情，Codex 應追加 Journal，而不是只留在聊天：

- 使用者改變產品方向；
- 重要美術、劇情或 Canon 原則；
- 主角或重要角色的語氣、價值與成長方向；
- 重大技術架構決定；
- 世界來源、SQL、Context、Runtime 與 Scenario 之間的真實差異；
- 真實遊玩發現；
- 模型、Provider 或成本策略變更；
- 一個舊規則被退休；
- 一個公開專案的設計被正式吸收；
- 重大失敗與教訓；
- 使用者指出專案偏離初衷。

## Integrity Rule

- 不把 Journal 當成目前狀態；目前狀態只由 `PROJECT_STATE.md` 宣告。
- 不刪除使用者批評或失敗紀錄來美化歷史。
- 不寫入 API Key、私人憑證、隱藏系統提示或私人推理。
- 對無法取得的舊聊天內容要標示缺口，不得假裝逐字完整。
- 可以保存使用者可見對話、Codex 回報、決策摘要與具體 Commit 證據。
- 若一個規劃沒有真正進入 SQL、Runtime、Scenario 或可執行產品，Journal 必須明確標記為未落地。
- Journal 是記憶，不是另一組需要逐層 PASS 的官僚門檻。
