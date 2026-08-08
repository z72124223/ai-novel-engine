# 有聲小說試驗層 — 工作交接與開發中止

**狀態：** `ABORTED_HANDOFF_V1`  
**日期：** 2026-07-09  
**決策：** 作者中止「有聲小說／YT 試聽出片」開發；**產品主線退回「只產生劇本」**。  
**對齊章程：** `world/chenxing/formal/29_project_script_first_charter_v1.json`（劇本優先，非本地成片）

---

## 1. 一句話結論

本階段在 MVP-8 讀者試玩之上，**實驗性**串接了「回合敘事 → 口述分段 → Edge TTS → 燒錄字幕 MP4 → publish 審核」試聽鏈。  
作者判定試聽品質與維護成本不符預期，**正式放棄有聲小說為 repo 目標**；後續窗口**不得**把 `export-audio-novel-episode`、預覽出片當主線，應回到 **互動劇本／分鏡文字／story-turn** 工作流。

程式與產物**保留在 repo 作凍結證據**，預設不刪、不擴充，除非作者明確重開。

---

## 2. 開發中斷記錄

| 項目 | 內容 |
|------|------|
| 中止功能 | 有聲小說試聽（Audio Novel / YT preview） |
| 中止原因（作者） | 字幕節奏、間隔、語音與審核鏈維護成本高；與「只產劇本」產品方向不一致 |
| 恢復主線 | 互動劇本引擎 + 分鏡稿 + `story-turn` 回合協議 |
| 程式處置 | **凍結**（保留 CLI／服務／tools，不當 active 開發） |
| 產物處置 | `out/chenxing/audio_novel/` 與 `reader_play_v4` 留作試驗證據，非正式交付 |

---

## 3. 本階段已完成（交接清單）

### 3.1 敘事與試玩

- **Session v4：** `session-abaf533e-42cd-40e0-af54-c7291760eef4`（塵行開場 3 回合）
- **回合證據：** `out/chenxing/reader_play_v4/turn0{1,2,3}_{context,result}.json`
- **敘事修正：**
  - 回合 3 愛醬改為默契跟隨（無「跟我走」口令）
  - 陸塵行對白改為自言「上面那棟……至少能擋風。」
  - oral 來源：`NarrativeDeliveryResolver.kt`、`26_volume_one_opening_storyboard_v1.json`

### 3.2 引擎（Kotlin，已合入，凍結）

| 元件 | 路徑 | 用途 |
|------|------|------|
| 口述分段 | `NarrativeOralFormatter.kt` | 旁白／對白分句、說話者標註 |
| 回合匯出 | `AudioNovelEpisodeExporter.kt` | 從 session 產 `voice_timeline.json` |
| 敘事審核 | `NarrativeQualityAuditService.kt` | 含默契同伴、簡體字、對白標註規則 |
| Publish 審核 | `AudioNovelPublishAuditService.kt` | 匯出／出片前閘門 |
| 字幕審核 | `AudioNovelSubtitleAuditService.kt` | SRT／timeline 規則（見 §4） |
| CLI | `export-audio-novel-episode`、`audit-audio-novel-publish` | 見 `Main.kt` |

### 3.3 出片工具（Node／PS1，凍結）

| 工具 | 路徑 |
|------|------|
| Edge TTS 渲染 | `tools/render-audio-novel-preview.mjs` |
| 一鍵出片 | `tools/build-audio-novel-preview.ps1`（`-Publish` 會跑審核） |

### 3.4 最後試聽產物（v4）

| 項目 | 路徑 |
|------|------|
| 預覽 MP4 | `out/chenxing/audio_novel/episode01_v4_full/episode_preview_v4.mp4` |
| 字幕 | `out/chenxing/audio_novel/episode01_v4_full/episode_subtitles.srt` |
| 音訊時間軸 | `out/chenxing/audio_novel/episode01_v4_full/voice_timeline.json` |
| 審核報告 | `out/chenxing/audio_novel/episode01_v4_full/publish_audit.json` |
| 聽一遍審核 | `out/chenxing/audio_novel/episode01_v4_full/listen_through_audit.json` |

**最後審核狀態：** `publishAuditVerdict=PASS`（0 MAJOR，7 WARNING；主要為 turn3 簡體「竖起／收紧」）

### 3.5 本階段修過的問題（供日後若重開參考）

1. 愛醬口令／默契旁白不一致 → `COMPANION_COMMAND_DIALOGUE_INCONGRUENT`
2. 陸塵行誤用女聲 → 統一 `zh-TW-YunJheNeural`
3. 開發字幕標記 `【回合…】` → `-Publish` 模式清除
4. oral 分段誤切「在他身側」→ `NarrativeOralFormatter` 修正
5. 破折號字幕斷行 → `SUBTITLE_BROKEN_EM_DASH` 規則 + 渲染修正
6. 回合靜音 2.8s（雙重 gap）→ 改 **1.0s** 單次
7. 字幕停留過長 → `SUBTITLE_DWELL_TOO_LONG` + 分段上限 2.1s

---

## 4. 已寫入引擎的審核規則（凍結保留）

**敘事（回合級）**

- `DIALOGUE_REQUIRES_SPEAKER_ATTRIBUTION`
- `COMPANION_COMMAND_DIALOGUE_INCONGRUENT`
- `SIMPLIFIED_CHINESE_IN_NARRATIVE`（WARNING）

**字幕／出片（episode 級）**

- `SUBTITLE_DWELL_TOO_LONG`（MAJOR）
- `SUBTITLE_GAP_TOO_LONG`（WARNING）
- `SUBTITLE_DOUBLE_TURN_GAP`（MAJOR）
- `SUBTITLE_BROKEN_EM_DASH`（MAJOR）
- `SUBTITLE_ORPHAN_PUNCTUATION`（MAJOR）
- `SIMPLIFIED_CHINESE_IN_PUBLISH_TEXT`（WARNING）
- `SUBTITLE_SINGLE_CHUNK_LONG_DWELL_RISK`（WARNING，匯出前預警）

政策常數：`AudioNovelSubtitlePolicy`（回合間隔 1000ms、單條字幕 ≤2100ms 等）

---

## 5. 後續窗口應做什麼（主線）

### 5.1 唯一產品目標

**產出可驗證的互動劇本與分鏡文字**，不走本地有聲／MP4 產線。

### 5.2 標準工作流

1. 讀 `docs/HANDOFF.md` 與 `29_project_script_first_charter_v1.json`
2. 用 **`.agents/skills/story-turn/SKILL.md`** 跑回合（`prepare-turn` → 寫 result → `resolve-turn`）
3. 打磨正式分鏡／劇本 JSON：
   - `world/chenxing/formal/26_volume_one_opening_storyboard_v1.json`
   - `world/chenxing/formal/28_volume_one_ruin_interior_storyboard_v1.json`
4. MVP-8 試用層規格：`docs/MVP8_PROVISIONAL_LAYER_SPEC.md`（讀者模式，非 App）

### 5.3 明確不要做（除非作者重開）

- 不要執行 `export-audio-novel-episode`、`build-audio-novel-preview.ps1` 當日常任務
- 不要擴充 Edge TTS、字幕燒錄、YT publish 鏈
- 不要把有聲試聽當驗收標準
- 不要為出片再開新 session 證據鏈（Pilot 規則仍適用）

### 5.4 若僅需「劇本稿」輸出

從已 resolve 的 session 或分鏡 JSON 整理 Markdown／script packet；  
參考 `out/chenxing/reader_play_v4/` 與 `episode_script.md` 的**文字部分**即可，無需再生 MP4。

---

## 6. 已知未收尾項（不阻擋劇本主線）

| 項目 | 說明 |
|------|------|
| turn3 簡體字 | 「竖起」「收紧」→ 應為「豎起」「收緊」（僅影響已凍結試聽稿） |
| 有聲程式碼 | 留在 repo，無需為中止而刪除 |
| `node_modules/`、`app/data/` | 試驗產物；勿當正式 canon |

---

## 7. 快速指令備查（凍結功能，僅考古）

```powershell
# 僅在作者明確要求重開試聽時使用
.\gradlew.bat run --args="export-audio-novel-episode --publish --db data/story.db --session session-abaf533e-42cd-40e0-af54-c7291760eef4 --out-dir out/chenxing/audio_novel/episode01_v4_full --cover world/chenxing/assets/cover/chenxing_cover_v1.png"

.\tools\build-audio-novel-preview.ps1 -Publish -EpisodeDir "out\chenxing\audio_novel\episode01_v4_full" -OutputVideo "out\chenxing\audio_novel\episode01_v4_full\episode_preview_v4.mp4"

.\gradlew.bat run --args="audit-audio-novel-publish --db data/story.db --session session-abaf533e-42cd-40e0-af54-c7291760eef4 --episode-dir out/chenxing/audio_novel/episode01_v4_full"
```

**日常劇本開發：**

```powershell
.\gradlew.bat run --args="show-session --db data/story.db --session <sessionId>"
.\gradlew.bat run --args="prepare-turn --db data/story.db --session <sessionId> --input \"<玩家輸入>\" --out out/turn_context.json"
.\gradlew.bat run --args="resolve-turn --db data/story.db --session <sessionId> --result out/turn_result.json"
```

---

## 8. 相關文件索引

| 文件 | 用途 |
|------|------|
| `docs/HANDOFF.md` | 總索引（已加中止決策條目） |
| `docs/MVP8_PROJECT_DECISION.md` | MVP-8 試用層決策 |
| `world/chenxing/formal/29_project_script_first_charter_v1.json` | 劇本優先章程 |
| `.agents/skills/story-turn/SKILL.md` | 單回合安全操作 |
| `docs/MVP8_PROVISIONAL_LAYER_SPEC.md` | 試用層規格 |

---

**簽核：** 作者 2026-07-09 口頭定案「有聲小說放棄，退回只產劇本」。  
**下一位接手者：** 從 §5 主線開始，勿從有聲出片繼續。