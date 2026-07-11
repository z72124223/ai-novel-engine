# 2026-07-11 創作、世界觀與自主執行補充長誌 V1

Status: `DETAILED_RECONSTRUCTED_USER_VISIBLE_DISCUSSION_ADDENDUM`

## 0. 完整性說明

本文件補充 `2026-07-11_PROJECT_DISCUSSION_ARCHIVE_V1.md` 建立後，使用者提出的最新創作方向、世界觀落地要求、Codex 自主執行要求、模型成本問題與影片 Provider 想法。

它保存可取得的使用者可見內容與正式決策，不是平台逐字匯出，不包含隱藏推理、系統提示、憑證或無法存取的其他聊天。

## 1. 使用者要求不再回到 GPT 總控窗口

使用者表示：

- 希望 GPT 把藍圖更新完整；
- 之後直接讓 Codex 依藍圖做到完成；
- 不希望每一小步都回到同一個聊天窗口重新詢問；
- 希望 Codex 能自行處理一般技術問題、測試失敗、實作順序與小型架構取捨；
- 只有真正需要創作最終決定、付費、API Key、破壞性操作或核心安全邊界時才停止。

因此 REBOOT-01 被定義成 Umbrella Work Package：

```text
一個明確啟動的 Codex CLI Session
→ 乾淨 reboot branch/worktree
→ Stage 0–7 逐階段完成
→ 每階段驗證與 Commit
→ 最終 Push
→ 一次完成回報
```

這不是無限自動擴張權限，只對 REBOOT-01 生效。

## 2. Codex 模型等級決策

使用者質疑：既然 GPT-5.6 已經把藍圖寫清楚，Codex 是否仍需要全程使用最高模型與最高推理。

正式結論：不需要。

```text
主執行：GPT-5.6 Codex MEDIUM
```

只有以下項目升到 High：

- Migration；
- Transaction；
- Validator／StateReducer；
- Knowledge Boundary；
- World Source Authority 衝突；
- Replay／一致性核心 Bug；
- 高風險 Git／資料整合；
- 最終整合審查；
- 一個有證據的 Medium 嘗試仍無法解決的核心阻塞。

Low／快速模型或腳本負責：

- 搜尋；
- 盤點；
- 測試分類；
- Hash；
- JSON；
- 路徑；
- Diff；
- 統計；
- 重複文件工作。

Max／最高昂模式預設禁止。

## 3. 使用者希望的情緒節奏

使用者希望作品不是持續陰鬱，而是類似高低起伏的長篇情緒波形：

- 玩的時候能笑；
- 先被角色吸引、感到溫暖；
- 可能有戀愛、友情、可愛與日常；
- 突然因既有因果感到憤怒；
- 進入省思、無力或落淚；
- 情緒回落後仍有希望與繼續看下去的理由。

正式抽象：

```text
歡快／好奇
→ 吐槽與日常摩擦
→ 溫暖／友情／戀愛
→ 不安
→ 衝突
→ 價值兩難
→ 代價／憤怒／失落
→ 反思與關係改變
→ 局部希望與新的未解問題
```

這些靈感只用於高層節奏與結構，不複製特定作品的角色、台詞、情節或獨特表達。

## 4. 主角成長與群像

使用者希望主角逐步變強，最後甚至能左右戰局或重大局勢。

正式限制：

- 成長不能只有數值；
- 能力、知識、關係、社會影響、責任、犧牲與道德壓力同步上升；
- 每個階段都有新限制與新代價；
- 配角、對手、戀愛角色與普通人也要有自己的成長或惡化；
- 角色不能只為主角提供情報、任務或情緒服務；
- 某些關係可以留下意難平，但悲劇不是唯一深度來源。

## 5. 沒有最終終局，但必須局部收束

使用者不希望故事迎來「從此再也見不到所有角色」的永久結局。

正式原則：

```text
局部收束，全球延續
```

每個 Arc 必須：

- 回收一部分承諾；
- 改變關係；
- 留下永久 State；
- 完成角色階段；
- 打開新的生活面向或問題。

禁止用「沒有最終結局」合理化：

- 永不解決；
- 永遠重複同一爭論；
- 只靠更大敵人延命；
- 下一回合重置後果；
- 無限鋪伏筆不回收。

## 6. 主角毒舌與吐槽屬性

使用者提出：主角可以加入毒舌與吐槽屬性。

原因：

- 觀眾常能看出恐怖片或危險場景中的明顯陷阱；
- 有些作品讓角色為了劇情硬做愚蠢行為；
- 主角若能指出觀眾心中的疑問，可降低作者操控感；
- 主角識破一階套路後，世界與敵人需要用更高層次回應；
- 吐槽能建立親近感，為後續重情緒增加重量。

正式決策：先在 REBOOT-01 非 Canon Sandbox 測試，不直接升格 AUTHOR_LOCKED Canon。

Voice Profile 候選：

```text
observational sarcasm
self-deprecating humor
risk-aware commentary
social cost
serious-scene restraint
```

限制：

- 不能每句都吐槽；
- 不能把主角變成作者全知分身；
- 不能用笑話消解真實痛苦；
- 不能讓其他角色集體降智；
- 吐槽本身可以帶來關係與社會代價；
- 嚴重創傷場景中要有收斂。

## 7. 觀眾過度預測劇情

使用者指出：觀眾能一直預測劇情，是口碑變差的重要原因之一。

正式理解：

觀眾猜到部分事件不是錯誤。真正問題是：

- 所有轉折只有第一層答案；
- 角色明知危險卻沒有理由地作死；
- 所有人都照常見套路反應；
- 伏筆沒有第二層後果；
- 反轉只靠藏資料或隨機事件；
- 觀眾永遠比角色聰明太多。

新增設計：

```text
AudienceExpectationLedger
```

記錄：

- 觀眾可能預期什麼；
- 預期原因與伏筆；
- 角色自己預期什麼；
- 實際結果；
- 反轉類型；
- 因果基礎；
- 公平性證據；
- 第二層後果；
- 回收狀態。

允許的公平轉折：

1. 觀眾猜對事件，但沒猜到代價；
2. 角色因成長、愛、恐懼或利益主動改變結果；
3. 一階答案正確，但造成意外的二階後果；
4. 視角資訊落差，但前文公平；
5. 真正反轉是角色選擇犧牲什麼，而不是誰突然變壞。

## 8. Idiot Plot 防護

任何角色進入明顯危險，至少要有一項理由：

```text
insufficientKnowledge
urgentGoal
moralObligation
relationshipPressure
resourceConstraint
falseBelief
coercion
tradeoff
calculatedRisk
```

否則：

```text
IDIOT_PLOT_RISK
```

這個 Finding 是劇情品質警告，不直接丟棄 Run Evidence；Critic 必須提出具體修正。

## 9. 使用者指出世界觀只有規劃、沒有落地

使用者表示：看不到 GPT 對專案中的世界觀、設定與實際引擎載入狀況做完整檢測與更新；很多內容只有規劃文件，沒有 SQL／Runtime 落地。

這個批評成立。

新的強制區分：

```text
formal source exists
SQL contains it
Runtime Context retrieves it
Agent／Scene uses it
Scenario can trigger it
```

不能再用「文件存在」宣稱功能或世界觀已落地。

## 10. 世界觀 Reality Audit

REBOOT-01 強制新增：

```text
docs/reboot/REBOOT01_WORLD_AND_CHARACTER_REALITY_AUDIT.md
out/reboot01/world_audit/world_reality_inventory.json
```

稽核來源：

1. `query-world-source`；
2. `data/story.db`；
3. `world/chenxing/formal/`；
4. Setting Pack／Import evidence；
5. Runtime Seed／Session；
6. Context Packet；
7. 歷史文件只作證據。

狀態分類：

```text
AUTHOR_LOCKED
LOADED_IN_SQL
FORMAL_NOT_LOADED
TEXT_ONLY
RUNTIME_UNUSED
CONTRADICTED
MISSING_REQUIRED
PROVISIONAL_CANDIDATE
TEST_ONLY
SUPERSEDED
```

## 11. 世界觀實際修補

稽核後不能只交報告。

Codex 必須建立非 Canon World Patch Candidate，例如：

```text
samples/reboot01/chenxing_world_patch_candidate_v1.json
```

並走：

```text
parse
→ validate
→ diff
→ impact preview
→ sandbox apply
```

只套用到可重置 Sandbox／非 Canon Project 或 Session。

至少補足：

- 兩名有深度的 NPC；
- 一名有價值主張的對立者；
- Offscreen Agenda；
- 社會／資源／制度壓力；
- 價值兩難；
- 日常、溫暖與笑點條件；
- 不可逆 Runtime 後果；
- 主角可以吐槽但無法用一句話解決的人性或制度矛盾。

完成時必須證明 Patch 被 Runtime Context 讀到並在實際回合中生效。

## 12. 多代理與成本

使用者質疑一直沒有使用多代理。

正式策略：

- Main Codex：整合與唯一核心 Writer；
- RepositoryExplorer：只讀搜尋與依賴；
- RuntimeTester：測試、SQL、Replay；
- NarrativeSystemsCritic：自主性、情緒、Predictability、Stall；
- CostAndComplexityAuditor：成本、文件與程式膨脹。

最多四個 Subagents，按需啟動，不常駐。

核心 Kotlin／SQL 同一時間只有一個 Writer。

## 13. 網站與遊戲

永久禁止 Web 的舊規則已退休。

REBOOT-01 在引擎垂直切片通過後，可建立本機 Web 介面：

- 故事文字；
- 玩家輸入；
- 世界時間；
- 角色／關係；
- 存檔／讀檔；
- Agent／Critic／DecisionTrace／State Delta Debug。

不公開部署、不登入、不付費、不接外部模型 API。

## 14. 圖片與影片

圖片線目前暫停：

```text
IMAGE_LANE_PAUSED_AFTER_USER_APPROVED_CANDIDATE
```

大坪海岸圖仍只是 Candidate。

恢復美術時先做人物風格 A/B/C，再讓背景跟隨。

影片線：

```text
FUTURE_PROVIDER_BAKEOFF_NO_SPEND
```

使用者願意未來購買 API 或使用即夢／Seedance／Grok，但希望選最適合專案的 Provider。

正式策略：用 Provider-neutral `VideoShotHandoff`，同一 Keyframe／Motion Contract 做少量比較，不先綁單一供應商。

## 15. YouTube 影片

使用者提供：

```text
https://www.youtube.com/watch?v=dqbWp_Aqj1k
```

目前工具環境沒有取得完整逐字稿，因此不能把未核實口述內容當成 API 規格。

吸收的核心模式是：

```text
GPT／Codex 作 Orchestrator
→ 劇本／分鏡／資產／Prompt
→ 呼叫專用影片模型
→ Poll／Download／Review／Retry
```

## 16. 開發日誌要求

使用者要求保存的不只是最新一段，而是前面所有可取得的專案討論，避免未來 GPT 忘記最初目標。

已建立：

- `2026-07-11_PROJECT_DISCUSSION_ARCHIVE_V1.md`；
- 本 Addendum；
- `INDEX.md` 的後續追加規則。

完整性限制：

- 保存目前可取得的使用者可見內容與 Git 證據；
- 不是平台逐字匯出；
- 不保存隱藏推理、系統提示、憑證；
- 不假裝擁有無法取得的更早聊天；
- 未來每個重大方向與真實失敗都要追加。

## 17. 最終直接執行命令

使用者可直接對 Codex CLI 說：

```text
Fetch the latest remote default branch. Read PROJECT_STATE.md and execute the active REBOOT-01 umbrella Work Package end-to-end on a clean reboot/playable-depth-v1 worktree. Treat docs/REBOOT_2026_CREATIVE_WORLD_EXECUTION_ADDENDUM_V1.md as mandatory. Use GPT-5.6 Codex MEDIUM by default, escalate only according to the model policy, use bounded Subagents where useful, validate and Commit every internal Stage, Push the final branch, and report once at completion. Do not return for ordinary implementation decisions. Stop only for the True Stop Conditions in PROJECT_STATE.md and the active Work Package.
```

## 18. 給未來 Agent 的一句話

> 不要用更多規劃文件假裝世界已經活起來；請查出世界資料實際在哪裡、哪些進了 SQL、哪些被 Runtime 使用，讓角色能自主生活，讓主角能聰明吐槽而不是作者替他作弊，讓劇情意外但公平，最後用 5–10 個真實回合與可玩的本機介面證明專案有價值。
