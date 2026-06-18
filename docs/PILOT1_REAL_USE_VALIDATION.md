# Pilot-1：真實使用垂直切片驗證

## 0. 目的

MVP-1 至 MVP-5 的自動化測試已證明工程不容易破壞資料，但尚未證明：

- 作者能否順利輸入一套新設定。
- Codex 作為唯一窗口時，實際操作是否順暢。
- 角色在真實對話中是否仍有自主性與深度。
- 記憶是否自然，而不是只在測試 fixture 中成立。
- 場景與劇情線是否自然推進。
- setting context 是否真的只載入相關內容。
- 敘事是否重複、工具人化、停滯或提前爆雷。

本 Pilot 不新增大功能。先用目前工程完成一個真實、可玩的垂直切片。

## 1. 安全基線

先確認並封存 MVP-5：

```bat
git status
gradlew.bat clean test --no-daemon
git diff --check
```

必須確認：

- 96 tests
- 0 failures
- 0 errors
- 001 至 004 migration 無修改
- data、out、build、.gradle、暫存 JDK 未被追蹤

提交：

```bat
git add .
git commit -m "feat: implement setting packs dependency graph and impact analysis"
git tag -a mvp-5 -m "Setting packs, canon validation, dependency graph, and impact analysis"
```

建立 Pilot 分支：

```bat
git switch -c pilot/real-use-vertical-slice
```

## 2. 建立兩個 repo-scoped Codex Skills

### 2.1 setting-pack

路徑：

`.agents/skills/setting-pack/SKILL.md`

用途：把作者自然語言設定轉為 setting pack JSON，並只走正式 CLI。

固定流程：

1. 讀取 AGENTS.md 與 setting pack 文件。
2. 只建立或修改 JSON pack。
3. 執行 `validate-setting-pack`。
4. 修正所有 blocking errors。
5. 執行 `preview-setting-import`。
6. 顯示 diff、impact、active-session blockers 與 warnings。
7. 無阻塞錯誤時才執行 `apply-setting-pack`。
8. 執行 `validate-project-canon`。
9. 回報 entity、relations、revisions 與 dependency paths。
10. 不得用 SQL、sqlite3、自寫腳本直接改正式 DB。

### 2.2 story-turn

路徑：

`.agents/skills/story-turn/SKILL.md`

用途：以 Codex 作為唯一 AI 窗口，安全執行一個互動回合。

固定流程：

1. `show-session`
2. `prepare-turn`
3. 只讀本回合 context JSON
4. 依協議產生 result JSON
5. 使用繁體中文
6. 不得揭露 forbidden reveals
7. 角色只能根據自己的 goal、emotion、memory、knowledge、relationship、items、techniques、world rules 行動
8. 場景/thread director goal 不得變成角色內在任務
9. 不得直接寫 SQLite
10. `resolve-turn`
11. 若被拒絕，只修 result JSON，不繞過 Validator
12. 成功後 `show-session`
13. 回報 narrative、state changes、memory/knowledge、scene/thread、context budget

Skill 不得新增網路、API、server、port 或其他 runtime。

## 3. 建立新的真實試玩專案

不得使用 demo project 或 MVP 測試 pack 的 IDs。

由作者提供一份 300 至 1000 字的自然語言設定。若作者暫時沒有完整設定，可使用下列欄位填寫：

```text
作品名稱：
類型與基調：
世界基本規則：
力量系統：
主要地點：
主角：
重要 NPC：
主要勢力：
一件關鍵武器或道具：
一套功法或能力：
目前衝突：
一個公開事實：
兩個秘密：
玩家在故事中的身分：
開場場景：
```

使用 `$setting-pack` 建立並匯入全新 project。

最小內容：

- 3 characters
- 2 world rules，其中至少 1 條 hard rule
- 1 faction
- 4 locations 與 connections
- 1 unique weapon
- 2 general items
- 2 techniques，至少一條 prerequisite
- 2 facts
- 2 secrets
- 3 scenes
- 3 story threads
- 15 relations

完成後：

- validate PASS
- preview PASS
- apply PASS
- validate-project-canon PASS
- export 後重新 validate PASS

## 4. 真實試玩規則

建立全新 DB，例如：

`data\pilot1.db`

建立 session 後，由使用者親自輸入至少 12 個回合。

禁止：

- Codex 自己同時扮演玩家並替玩家決策全部回合。
- 預先硬編碼 12 個結果。
- 為了達成測試目標讓角色無理由配合。
- 手動修改 DB。
- 遇到 Validator fail 時繞過檢查。

每回合使用 `$story-turn`。

至少自然覆蓋：

1. 一次角色拒絕玩家。
2. 一次角色因 memory 改變反應。
3. 一次 belief 被證據動搖，但不瞬間完全信任。
4. 一次 weapon/item 限制行動。
5. 一次 technique prerequisite 或 cost 生效。
6. 一次 hard world rule 阻止不合法行為。
7. 一次 scheduled world event 施加壓力。
8. 一次玩家拒絕轉場，系統不得強迫。
9. 至少一次合法轉場。
10. 一條 thread 前進。
11. 一條 thread 因角色阻力沒有前進。
12. 至少形成一條 runtime memory，後續再被召回。
13. forbidden secret 全程不提前揭露。
14. 不同角色說話與行動風格可辨識。

## 5. 每回合觀察紀錄

建立：

`docs/PILOT1_TURN_LOG.md`

每回合追加：

- turn number
- 玩家輸入
- narrative 摘要
- active character intent
- characterFitScore
- state deltas
- memory recalled / created
- knowledge/belief changes
- scene/thread changes
- setting entities included
- context bytes
- setting bytes
- validator retry count
- 人工評分：
  - 角色自然度 1..5
  - 劇情推進 1..5
  - 情緒可信度 1..5
  - 設定運用 1..5
  - 重複程度 1..5，5 表示嚴重重複
- 問題備註

不得把完整 author-only secret 寫進一般 turn log。

## 6. 停止條件

遇到下列任一情況，停止試玩並記錄 blocker：

- 正式 DB 被繞過 Validator 修改
- 未揭露 secret 進入角色 knowledge
- 角色使用自己未知的 item/technique/fact
- 同一 result 被套用兩次
- branch/session/stateVersion 失真
- 場景轉場造成兩個 ACTIVE scene
- context 超過 24576 bytes
- setting section 超過配置預算且裁掉硬規則/代價
- 連續 3 回合完全沒有有意義狀態變化
- 角色無理由完全改變立場
- closed scene 全文不斷累積進 context

若只是敘事品質問題，不要立即改架構；先記錄。

## 7. Pilot 後稽核

完成至少 12 回合後，讓 Codex執行只讀稽核。

建立：

`docs/PILOT1_FINDINGS.md`

內容至少包括：

1. project / session / final turn / stateVersion
2. 角色狀態與目標演變
3. memory recall 與 runtime memory 品質
4. knowledge/belief 演變
5. scenes、transitions、threads
6. weapon/item/technique/world rule 的實際作用
7. secrets 安全狀態
8. 每回合 context bytes 與 setting bytes
9. 相關設定載入精確度
10. Validator 被拒絕與重試原因
11. 敘事重複模式
12. 工具人化案例
13. 停滯案例
14. 過度主動或替玩家決策案例
15. 角色聲音是否可區分
16. 平均人工評分
17. blockers
18. non-blocking quality issues
19. 建議下一階段優先級

## 8. 修改原則

Pilot 分支預設不改核心功能。

只有符合以下條件才修：

- data corruption
- security/knowledge leak
- transaction/idempotency bug
- context budget violation
- CLI 無法完成正式流程
- 明確違反已存在規格

修正 blocker 時：

1. 先新增失敗測試。
2. 再修程式。
3. 跑全部 tests。
4. 記錄修正原因。

敘事品質、節奏、風格問題先記錄，不在 Pilot 中大改引擎。

## 9. 最終回報

Pilot 完成後回報：

1. VERDICT：PASS / FAIL / PASS_WITH_QUALITY_ISSUES
2. Git branch/status
3. project 與 setting pack 統計
4. validate/preview/apply/export 結果
5. 試玩回合數
6. final stateVersion
7. 每回合 context/setting bytes
8. 平均人工評分
9. memory/knowledge/thread/scene 摘要
10. forbidden secret 狀態
11. Validator retry 統計
12. blockers
13. quality issues
14. 建議下一個 MVP

Pilot 通過後，下一階段預設為：

`MVP-6：checkpoint、分支時間線、非破壞性回溯與可重播性`

若 Pilot 顯示敘事品質才是主要問題，則先做 narrative policy / style runtime，而不是盲目增加基礎設施。
