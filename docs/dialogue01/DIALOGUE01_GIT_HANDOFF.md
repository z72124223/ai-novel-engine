# DIALOGUE-01 V2 Git 交接

## 身分

- 實作分支：`codex/dialogue01-human-voice-finalization`
- 乾淨 Worktree：只作執行環境，不是交接識別。
- Base SHA：`d61aa1bf73dd377e4cd12ff10989b343196dfa15`
- SCREEN-01 來源 Commit：`513c9d9ac1b1e168ac7c1217ea3f6ba581181719`
- 遠端預設分支：`codex/mvp6-checkpoint-branching-spec`
- 邊界：`NON_CANON_PILOT`

實作成果 Commit：`90c619552b602a462e007886c4c59e27eb3eb761`。該 Commit 已普通 Push 至實作分支，並由預設分支從 `d61aa1bf73dd377e4cd12ff10989b343196dfa15` 安全 fast-forward。最終治理收尾 Commit 由外部最終回報提供，避免 Commit 自我引用。

## 另一台電腦的 Agent 必讀

1. `PROJECT_STATE.md`
2. `docs/dialogue01/DIALOGUE01_FINAL_REPORT.md`
3. `samples/dialogue01/final/dialogue01_selection_decision.json`
4. `samples/dialogue01/final/dialogue01_hash_manifest.json`
5. `samples/dialogue01/final/dialogue01_video_handoff_overlay.json`
6. 最終劇本、對白與字幕

## 最終輸出

```text
samples/dialogue01/dialogue01_line_audit.json
samples/dialogue01/dialogue01_simulated_read_aloud_notes.md
samples/dialogue01/reviews/taiwan_everyday_speech_review.json
samples/dialogue01/reviews/actor_breath_performance_review.json
samples/dialogue01/reviews/pan_chinese_comprehension_review.json
samples/dialogue01/final/screen01_screenplay_dialogue_final_zh-Hant-TW.md
samples/dialogue01/final/screen01_dialogue_track_final_zh-Hant-TW.json
samples/dialogue01/final/screen01_video_pilot_dialogue_final_zh-Hant-TW.srt
samples/dialogue01/final/dialogue01_selection_decision.json
samples/dialogue01/final/dialogue01_hash_manifest.json
samples/dialogue01/final/dialogue01_video_handoff_overlay.json
```

## Shot 與字幕同步

- Shot ID 與 dialogueId 的完整綁定在 `dialogue01_video_handoff_overlay.json`。
- 唯一文字變更 dlg-022 仍綁定 shot-023。
- Pilot 仍為 shot-015..023、82 秒。
- SRT cue 10 已同步成 `潮表還沒到。`；其餘 cue 與時間碼不變。

## 驗證紀錄

以下項目必須在最終 Push 前全數記錄 PASS：

- JSON parse
- 31-line audit / final dialogue cardinality
- reviewer identity / independence / exact quote
- dialogue ID / speaker / Shot reference
- final screenplay / dialogue / SRT text synchronization
- 82-second Pilot and non-overlapping SRT
- SHA-256 manifest
- private absolute path / secret scan
- no Provider / generated media / Canon promotion
- focused tests
- full Gradle suite
- `git diff --check`
- implementation branch push and safe default-branch update

Focused tests：

```text
gradlew.bat :app:test --tests NarrativeOralFormatterTest --tests Mvp8NarrativeDeliveryTest --tests NovelizationProseQualityAuditTest --no-daemon
BUILD SUCCESSFUL
```

Full suite：

```text
gradlew.bat clean test --no-daemon --console=plain
BUILD SUCCESSFUL in 6m 4s
```

## 後續限制

本交接只代表對白前期成果。真人桌讀、圖片、影片、Provider、Canon 或下一章都需要新的明確工作包；不得由本分支自動啟動。
