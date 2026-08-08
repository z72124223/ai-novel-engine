# DIALOGUE-01 V2 — 華語真人對白定稿

Status: `READY_FOR_AUTONOMOUS_CODEX_EXECUTION`

## 1. 目的

把 SCREEN-01 選定版中仍有的 AI 對話感降下來。

不重寫故事，不改角色能力，不開始圖片或影片。

## 2. 來源

- Screenplay: `samples/screen01/screen01_screenplay_revised_zh-Hant-TW.md`
- Dialogue: `samples/screen01/screen01_dialogue_track_revised_zh-Hant-TW.json`
- Dialogue Bible: `samples/screen01/screen01_dialogue_bible_zh-Hant-TW.json`
- Shot list: `samples/screen01/screen01_shot_list.json`
- Pilot: `samples/screen01/screen01_video_pilot_60_90s.json`
- Subtitle: `samples/screen01/screen01_video_pilot_zh-Hant-TW.srt`

## 3. 不可改變

- 四場、三十一鏡與主要事件；
- 救人優先於箱子；
- 陸塵行先保護愛醬，造成撬點失去力量；
- 拾潮者左手受傷；
- 第二名行動者仍只是待驗證假設；
- 陸塵行聰明可靠；
- 愛醬主動；
- `NON_CANON_PILOT`；
- 影片／動畫是主要終點。

## 4. 要處理的問題

- 說話太完整；
- 每句太精準；
- 角色輪流發表論點；
- 金句密度太高；
- 對白與畫面重複；
- 角色聲線不夠分開；
- 危急場景仍有不像真人的句子；
- 台灣口氣與泛華語理解需要同時保留。

## 5. 每句六問

1. 真人會這樣說嗎？
2. 現場有時間說完嗎？
3. 能不能用動作或沉默取代？
4. 換別的角色說也成立嗎？
5. 是不是只為了顯聰明？
6. 台灣華語自然嗎？其他華語讀者懂嗎？

## 6. Codex 執行階段

### Stage 0 — Git 與來源

- 同步；
- 建立乾淨分支；
- 驗證來源 Hash；
- 確認沒有圖片／影片／Provider 權限。

### Stage 1 — 31 句逐句稽核

每句記錄：

```text
dialogueId
originalText
decision = KEEP / SHORTEN / REWRITE / OMIT_USE_ACTION
reason
proposedText
characterVoiceReason
taiwanMandarinNote
panChineseNote
performanceNote
```

### Stage 2 — AI 模擬朗讀

明確標記：

```text
AI_SIMULATED_READ_ALOUD
NOT_HUMAN_TABLE_READ
```

檢查呼吸、速度、危急時長度、演員是否能自然說出口、翻譯腔與「兩個 AI」感。

### Stage 3 — 三個獨立 Codex 子評審

1. 台灣日常口語；
2. 演員呼吸與可表演性；
3. 泛華語理解與在地化。

三者使用獨立上下文，綁定初版 Hash 並引用原句。

### Stage 4 — 最多一次修訂

- 只改對白與必要動作承接；
- 不改主要情節；
- 不讓主角變笨；
- 每場最多保留一個明顯記憶句；
- 修訂後不再自動第二次改寫。

### Stage 5 — 影片前期同步

同步更新：

- final screenplay；
- final dialogue track；
- Shot dialogue references；
- 82 秒 SRT；
- selection decision；
- video handoff overlay；
- Hash manifest。

### Stage 6 — 驗證與合併

- JSON；
- Hash；
- Review 引用；
- Dialogue ID／speaker；
- Shot 引用；
- 對白時長；
- SRT 無重疊；
- 私人路徑／秘密／Provider 權限；
- focused tests；
- full suite 一次；
- `git diff --check`；
- 普通 Push；
- 安全合併預設分支。

## 7. 必須產出

```text
samples/dialogue01/
├── dialogue01_line_audit.json
├── dialogue01_simulated_read_aloud_notes.md
├── reviews/
│   ├── taiwan_everyday_speech_review.json
│   ├── actor_breath_performance_review.json
│   └── pan_chinese_comprehension_review.json
└── final/
    ├── screen01_screenplay_dialogue_final_zh-Hant-TW.md
    ├── screen01_dialogue_track_final_zh-Hant-TW.json
    ├── screen01_video_pilot_dialogue_final_zh-Hant-TW.srt
    ├── dialogue01_selection_decision.json
    ├── dialogue01_hash_manifest.json
    └── dialogue01_video_handoff_overlay.json

docs/dialogue01/
├── DIALOGUE01_FINAL_REPORT.md
└── DIALOGUE01_GIT_HANDOFF.md
```

## 8. 模型

- 一般工程：Sol Medium；
- 對白初次修改與唯一修訂：限定 Sol High；
- 三個子評審：Sol Medium；
- 搜尋／測試：Terra Low／Medium；
- Hash、JSON、字幕、Diff：程式；
- Max／Ultra：禁止。

## 9. 完成分類

```text
DIALOGUE01_PASS_READY_FOR_HUMAN_TABLE_READ
DIALOGUE01_PASS_WITH_REGIONAL_WARNINGS
DIALOGUE01_NEEDS_HUMAN_VOICE_DECISION
DIALOGUE01_BLOCKED_BY_SCOPE_CONFLICT
DIALOGUE01_BLOCKED_BY_TECHNICAL_FAILURE
```

完成後不得自動開始圖片、影片、Provider 或下一章。
