# EPISODE-01A — Formal First-Episode Recovery

狀態：`COMPLETE / READY_FOR_PRODUCT_OWNER_REVIEW / NON_CANON_DRAFT`

## 目標

把先前誤用〈潮線之外〉的 EPISODE-01 V1 封存為拒絕證據，依作者鎖定的正式來源全新產生
〈第一話　塵〉。本包只做劇本與可追溯證據，不修改 Runtime、Canon 或媒體。

## 必讀

1. `docs/episode01/EPISODE01_FULL_PROJECT_HISTORY_AUDIT.md`
2. `world/chenxing/formal/20_protagonist_pair_v1.json`
3. `world/chenxing/formal/21_volume_one_opening_arc_v1.json`
4. `world/chenxing/formal/22_volume_one_opening_prologue_v1.json`
5. `world/chenxing/formal/26_volume_one_opening_storyboard_v1.json`
6. `world/chenxing/engine/chenxing_setting_pack_v4_opening_scenes.json`
7. `docs/HUMANLIKE_PROSE_AND_DIALOGUE_DISTILLATION_PLAN.md`
8. `docs/ENGINE_ENFORCED_PRODUCTION_RULES_V1.md`

`30_volume_one_secrets_author_only_v1.json` 只可用來防止洩密，不得摘錄秘密進交接或劇本。

## 產線

```text
Git tracked source
→ fresh SQLite rebuild
→ Canon／Setting Pack／dependency verification
→ complete initial screenplay
→ LOGIC_PHYSICS review
→ CHARACTER_DIALOGUE review
→ READER_ANTI_AI review
→ at most one bounded revision
→ Hash／secret／scope／test verification
```

## 故事範圍

- 章名：〈第一話　塵〉。
- 時空：2026 台灣基隆八斗子大坪海岸。
- 角色：陸塵行、愛醬。
- 事件：prologue＋`tide_check` 到 `retreat_to_hillside_ruin`。
- 結尾：愛醬在遺構門口遲疑；內部 beats 10–18 留給下一話／下一包。

## 寫作規則

- 先寫環境改變，再寫感知、移動、取捨、反應與有限台詞。
- 陸塵行不因劇情需要忘記潮汐、浪況、手機、背包、狗或退路。
- 他的錯誤來自作者鎖定的過度專注，不是缺乏常識。
- 愛醬只能做犬隻身體可行的行動；牽繩、站位、重心與坡面保持連續。
- 壓力升高時減少幽默與完整句；不使用翻譯腔、校正式句型或金句收尾。
- 所有物件先出現再使用，所有路線先可達再移動。

## 三個評審

- `LOGIC_PHYSICS`：事件因果、潮浪、路線、道具、犬隻與人體物理。
- `CHARACTER_DIALOGUE`：陸塵行與愛醬的身分、關係、語氣、知識與壓力反應。
- `READER_ANTI_AI`：可讀性、節奏、畫面、人味、重複、旁白與 AI 腔。

每份評審必須引用初稿原文、列出可操作修正，並綁定初稿 SHA-256。只允許一次修訂。

## 邊界

- `NON_CANON_DRAFT`；Product Owner 未閱讀前不得宣稱選定、Canon 或正式出版。
- 不生成圖片、影片、音訊、字幕或下一話。
- 不呼叫 API、本機模型、Provider 或 web server。
- 不修改 Kotlin、Test、Migration、Schema、SQLite 正式資料、Memory、Knowledge 或 Canon。
- 不重寫 V1 Commit 歷史，不 force push。

## 完成條件

- 現場 fresh rebuild 與 Canon 驗證通過。
- 初稿為完整新稿，與 V1／〈潮線之外〉無情節及對白複製。
- 三份角色隔離評審完成，最多一次修訂。
- 最終稿精確覆蓋正式九拍，沒有作者秘密與物理硬錯。
- JSON、Hash、引用、禁句、測試、Git scope 與 `git diff --check` 通過。

## 完成產物

- 全專案與歷史稽核：`docs/episode01/EPISODE01_FULL_PROJECT_HISTORY_AUDIT.md`
- 初稿：`samples/episode01/v2/episode01_screenplay_initial_v2_zh-Hant-TW.md`
- 三份內部角色分離評審：`samples/episode01/v2/reviews/`
- 唯一修訂後候選：`samples/episode01/episode01_screenplay_engine_v2_zh-Hant-TW.md`
- 來源與 Hash：`samples/episode01/episode01_engine_source_manifest_v2.json`
- 完成報告：`docs/episode01/EPISODE01_ENGINE_SCREENPLAY_V2_REPORT.md`

驗證結果：fresh rebuild／Canon PASS、聚焦測試 48／48、完整測試 976／976。未修改 Production、
Test、Migration、Schema、正式 SQLite 或 Canon；未呼叫 API／Provider，也未生成媒體。
