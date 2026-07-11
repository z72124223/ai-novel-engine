# Current Route Gate V2 — Playable Depth Reboot

Status: `ACTIVE_SINGLE_CURRENT_ENTRY_GATE`

Date: 2026-07-11

## 1. 唯一入口

所有新的 GPT、Codex CLI、Reviewer、Subagent 或外部協作者必須依序讀：

1. `PROJECT_STATE.md`
2. `.codex/project_controller.md`
3. `docs/CURRENT_ROUTE_GATE_V2.md`
4. `docs/work-packages/REBOOT-01_PLAYABLE_DEPTH_SLICE.md`
5. `docs/REBOOT_2026_PLAYABLE_DEPTH_BLUEPRINT_V1.md`
6. `docs/REBOOT_2026_NARRATIVE_VOICE_AND_UNPREDICTABILITY_ADDENDUM_V1.md`
7. `docs/CODEX_MODEL_AND_BUDGET_POLICY_V1.md`
8. `AGENTS.md`
9. 只讀取目前 Stage 所需的程式、測試、世界資料與歷史證據

V1 route gate、圖片 Work Package、舊 HANDOFF、舊 charter、舊 route card 與舊 PLANS section 都是歷史證據，不再選擇目前工作。

## 2. 目前路線

```text
先證明現有引擎真實狀況
→ 簡化過度阻擋
→ 保存所有 Run Evidence
→ 加入有界 NPC 自主性
→ 加入 Drama Director 與 Critics
→ 跑 5–10 回合塵行垂直切片
→ 同 Seed Replay 與規則演化
→ 建立本機 Web 可玩介面
→ 完成誠實報告
```

敘事測試必須遵守正式 addendum：歡快／溫暖／衝突／代價／反思的情緒波形、陸塵行的有界毒舌與類型敏感、反作死、`fair_surprise` 而非廉價反轉、群像成長與 `local_closure + persistent_world_continuation`。

圖片線暫停；影片線只保留未來 Provider-neutral Bake-off 規劃。

## 3. 被保留的硬邊界

- SQLite 正式狀態與 Transaction；
- `stateVersion`、`contextHash`、重複套用防護；
- 角色知識邊界；
- 地點、物品、技能與成本合法性；
- Canon／正式資產／破壞性操作／付費 Provider 的人類批准；
- API Key 不進 Git；
- 正式世界來源與 Provenance。

## 4. 被退休的模式

- 文件數量代表進度；
- 每個品質警告都是硬門檻；
- 接近滿分才存 SQL；
- Codex 永遠 High／Max；
- 永遠禁止多代理；
- 永久禁止本機 Web；
- 完整單元測試可以取代真實遊玩；
- 每個內部階段都必須回 GPT 總控；
- 圖片或劇本任一路線永遠是唯一終點；
- 模型版本更新後仍無限沿用舊規劃。

## 5. 自動執行例外

REBOOT-01 是一個使用者明確批准的 Umbrella Work Package。

Codex 可在一個已啟動的 CLI 工作階段中完成所有內部 Stage、逐 Stage Commit、最終 Push，而不必在每個 Stage 停止。

這是對 V1「一個工作包後停止」規則的有限覆蓋；不代表未來所有工作都可無限自行擴張。

## 6. True Stop Conditions

只有以下情況必須詢問使用者：

- Canon、角色命運、戀愛、正式美術等創作最終決定；
- 付費 API／Provider／API Key；
- 無法避免的破壞性 Git 操作；
- AUTHOR_LOCKED 來源衝突；
- 必須弱化 Transaction／Knowledge／Version／Hash／批准邊界；
- Medium 與 High 各一次有證據修復仍無法恢復核心。

一般 Bug、測試失敗、架構小取捨、Web 技術選擇與 additive Migration 不是停止條件。

## 7. 圖片與影片狀態

```text
IMAGE_LANE_PAUSED_AFTER_USER_APPROVED_CANDIDATE
VIDEO_LANE_FUTURE_BAKEOFF_NO_SPEND
```

大坪海岸主全景候選已由使用者表示同意，但尚未完成 Codex Intake／Hash／Candidate Reference。不得宣稱正式 Location Reference、Canon 或 Production Lock。

未來影片 Provider 選擇遵守 `docs/VIDEO_PROVIDER_BAKEOFF_PLAN_V1.md`，啟動時重新查官方文件與價格。

## 8. 權威順序

1. 使用者最新明確指示
2. `PROJECT_STATE.md`
3. `.codex/project_controller.md`
4. 本 Gate
5. REBOOT-01 Work Package
6. Reboot Blueprint／Narrative Addendum／Model Policy
7. 已驗證程式、測試、SQL、正式世界來源
8. 歷史藍圖、Handoff、圖片工作包與舊路線

## 9. 完成後

Codex 在 REBOOT-01 最終 Push 後，向使用者直接回報一次，不必返回原 GPT 總控窗口。

新的下一步由使用者在當時介面決定；不得自行開始付費 Provider、正式 Canon 或圖片／影片批量生產。