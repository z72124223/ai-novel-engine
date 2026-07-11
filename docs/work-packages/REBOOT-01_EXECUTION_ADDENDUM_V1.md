# REBOOT-01 — 自主執行與敘事驗收補充 V1

Status: `ACTIVE_UMBRELLA_WORK_PACKAGE_ADDENDUM`

Date: 2026-07-11

Parent:

- `docs/work-packages/REBOOT-01_PLAYABLE_DEPTH_SLICE.md`
- `docs/REBOOT_2026_PLAYABLE_DEPTH_BLUEPRINT_V1.md`
- `docs/REBOOT_2026_NARRATIVE_VOICE_AND_UNPREDICTABILITY_ADDENDUM_V1.md`
- `docs/CODEX_MODEL_AND_BUDGET_POLICY_V2.md`

本補充使 REBOOT-01 可由一次已啟動的 Codex CLI 工作階段一路做到完成，不需要再回到原 GPT 總控窗口取得每個 Stage 的指令。

## 1. 執行模式

使用者只啟動一次 Codex。

Codex 必須：

```text
fetch latest default branch
→ 建立乾淨 worktree / reboot/playable-depth-v1
→ Stage 0
→ 驗證與 Commit
→ Stage 1
→ 驗證與 Commit
→ Stage 2
→ 驗證與 Commit
→ Stage 3
→ 驗證與 Commit
→ Stage 4
→ 真實 5–10 回合
→ Stage 5 replay/evolution
→ Stage 6 local Web slice
→ Stage 7 final integration/report
→ Push
→ 直接向使用者回報
→ 停止
```

一般 Bug、測試失敗、架構小取捨、additive Migration、Web 技術選擇、Critic 分數不理想，都不是回來詢問的理由。

## 2. 模型

主 Agent：

```text
gpt-5.6-sol
reasoning = medium
```

必須遵守 `docs/CODEX_MODEL_AND_BUDGET_POLICY_V2.md`。

- 探索／大量檔案掃描：Terra Low；
- Runtime／測試分析：Terra Medium；
- 敘事 Critic：Sol Medium；
- 成本稽核：Luna Low；
- 核心 Transaction／Knowledge／Migration／最終審查：該小段 Sol High；
- XHigh／Max／Ultra 預設禁止。

## 3. Subagents

在 Stage 0、2、3、4、5、6 主動使用專用 Subagents，但最多四條並行執行緒、深度一層。

核心寫入只能由主 Agent完成。

建議流程：

```text
RepositoryExplorer ┐
RuntimeTester      ├→ 主 Agent 彙整 → 單一 Writer 修改
NarrativeCritic   ┤
CostAuditor       ┘
```

## 4. Stage 0：先跑現有引擎

不得先重構。

需要實際執行：

- compile；
- targeted runtime tests；
- full tests 一次；
- query-world-source；
- 一個 start-session → prepare-turn → resolve-turn → show-session；
- 實際 SQLite 查詢；
- 現有 World Source Hash／byte-size 問題的根因修復。

所有失敗要留下命令、輸出、分類與最小重現。

## 5. Stage 1：門檻縮減與 Evidence 保存

只保留：

1. protocol hard fail；
2. world legality hard fail；
3. irreversible approval required。

文風、節奏、AI 味、資訊傾倒、潛台詞不足與情緒不足改成 Score／Warning。

Raw Draft、Critic、修訂版、失敗原因、非法 State Delta、合法 Apply Result都必須能保存。品質差不等於資料應消失。

## 6. Stage 2：NPC 自主性

至少一名 NPC 在玩家不在場時：

- 根據自己的知識、記憶、地點、利益與關係感知；
- 形成 Goal／Plan／Schedule；
- 提出行動；
- 由 Game Master 與世界合法性判定；
- 合法後在 Transaction 中留下後果；
- 玩家回來能觀察到結果。

每次行動保存 DecisionTrace。

## 7. Stage 3：Drama Director、聲音與 Critics

除原工作包外，必須實作／記錄下列維度：

```text
sarcasmVoiceFit
banterNaturalness
genreAwarenessWithoutOmniscience
stupidityDrivenPlotRisk
audiencePredictionRisk
fairSurprise
cheapTwistRisk
secondOrderConsequence
antagonistAdaptation
emotionalWaveCoverage
readerAttachment
```

### 7.1 主角聲音

陸塵行在 non-canon Pilot 中具有：

- 毒舌；
- 吐槽；
- 自嘲；
- 對明顯危險的類型敏感；
- 對不合理行為的質疑；
- 高壓時會減少笑話，真正沉重時可以沉默。

他不能第四牆全知，也不能因吐槽而知道未取得的資訊。

### 7.2 防止角色作死

任何看似愚蠢的冒險都必須有清楚的資訊、時間、關係、責任、錯誤信念或代價理由。

### 7.3 公平意外

至少建立：

```text
obviousDanger
characterRecognition
firstLayerCountermeasure
worldOrAntagonistAdaptation
secondOrderSurprise
retrospectiveClueValidity
```

不可用任意反轉替代因果。

## 8. Stage 4：塵行 5–10 回合強制內容

Scenario 為 non-canon／resettable，不修改 AUTHOR_LOCKED 正式世界。

至少包含：

- 陸塵行；
- 愛醬；
- 兩名有獨立目標的重要 NPC；
- 一名對立角色或對立力量；
- 一個主要 Dramatic Question；
- 一次玩家離場 NPC 行動；
- 一次自然吐槽；
- 一次角色明確辨認顯而易見的危險；
- 第一層危險被合理降低或避開；
- 一次由對手／世界回應產生的第二層公平意外；
- 一次笑點或吐槽後續被因果／關係回收；
- 一次高壓時主角停止耍嘴皮；
- 一次溫暖、友情、可愛或戀愛張力；
- 一次價值兩難；
- 一次不可逆 Runtime 後果；
- 一次憤怒、失落或反思；
- 一次局部收束；
- 一個讓讀者願意繼續追的未解問題。

禁止只用測試 Fixture 宣稱完成；必須保存真實 Context、Result、SQL、Narrative、Critic 與 DecisionTrace。

## 9. Stage 5：同 Seed 演化

至少比較兩次 Run：

- Baseline Rule Set；
- 一項由真實失敗導出的有限規則修正。

比較：

```text
legalApplyRate
stallCount
autonomyScore
emotionalWaveCoverage
sarcasmVoiceFit
stupidityDrivenPlotRisk
audiencePredictionRisk
fairSurprise
cheapTwistRisk
readerAttachment
irreversibleChangeCount
retryCount
elapsedTime
modelAndReasoning
```

只有指標與人工可讀證據共同改善才採用新規則。

## 10. Stage 6：本機 Web 遊戲切片

在 Stage 0–5 通過後執行。

永久禁止 Web 的舊規則已退休。允許小型本機 Ktor／HTTP 層與 React／TypeScript，或 Repository 證據顯示更小的替代方案。

至少有：

- 故事顯示；
- 玩家輸入；
- 送出；
- 世界時間；
- 角色／關係摘要；
- 存檔／讀檔；
- 開發模式 Context／Critic／DecisionTrace／State Delta；
- 5–10 回合垂直切片可從 UI 操作。

不得公開部署、登入、付費、雲端或外部模型 API。

## 11. Stage 7：誠實完成報告

最終必須回報：

- 哪些實際跑通；
- 哪些只做了部分；
- 真實測試命令和結果；
- 全套測試剩餘失敗；
- SQLite 實際保存內容；
- NPC 自主行動證據；
- 5–10 回合完整路徑；
- Replay 比較；
- Web 操作方式；
- 各 Stage 模型／Reasoning／Subagents；
- High 升級次數與原因；
- 未解技術債；
- 下一步建議。

不准用「文件存在」冒充功能完成。

## 12. True Stop Conditions

只有以下情況停止並詢問使用者：

- 正式 Canon、角色死亡／命運、戀愛定案；
- 正式美術批准；
- 付費 API／Provider／API Key；
- 無法避免的破壞性 Git 操作；
- AUTHOR_LOCKED 來源矛盾；
- 必須弱化 Transaction、Knowledge、Version、Hash 或不可逆批准邊界；
- 同一核心阻塞已由 Medium 與 High 各完成一次有證據修復仍無法前進。

其他事項由 Codex在本工作包內自行決定並記錄。

## 13. Git 與交付

- 每個 Stage 獨立 Commit；
- 最終 Push `reboot/playable-depth-v1`；
- 不在舊 123 項髒工作樹實作；
- 不 force push；
- 不廣泛清理歷史；
- 不啟動圖片、影片或付費 Provider；
- 完成後直接向使用者回報，不回原 GPT 總控要求下一包。

## 14. Development Journal

每個 Stage 追加：

```text
docs/development_journal/reboot01/
```

記錄決策、失敗、模型路由、規則演化和真實遊玩發現。日誌不得只寫成功，也要保留使用者批評與失敗教訓。