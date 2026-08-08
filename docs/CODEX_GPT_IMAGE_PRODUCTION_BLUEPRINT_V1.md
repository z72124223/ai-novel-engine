# Codex → GitHub → GPT Image Production Blueprint V1

Status: `USER_APPROVED_ACTIVE_BLUEPRINT`

Date: 2026-07-10

## 1. Purpose

本文件定義 `AI Novel Engine` 接下來的正式生產路線：

```text
Codex 終端版執行引擎與世界資料工作
→ 產生、驗證並整理劇本／分鏡／圖片工作包
→ Commit 與 Push 到 GitHub
→ 使用者在 ChatGPT 要求 GPT 讀取指定 Commit 與交接包
→ GPT 依鎖定角色、服裝、場景與風格參考生成圖片候選
→ 使用者只做必要的審美批准／退件
→ Codex 將核准結果重新匯入、驗證、登記並 Commit
→ 圖片鎖定後再進入短鏡頭影片交接
```

最終產品目標是：

> 由正式世界資料與劇本驅動、角色和場景盡量不飄移、可逐鏡審查與重做的圖片／影片作品。

本路線不是「把整章文字一次丟進影片模型」。正式製作必須先有劇本、視覺聖經、場景空間錨點、逐鏡狀態與人類批准。

## 2. Current User Direction Override

本文件依 2026-07-10 使用者明確指示建立，對目前生產路線具有優先權：

- 保留 Kotlin/JVM CLI、SQLite 與 JSON 檔案協議作為底層引擎。
- 保留 MVP-8 讀者優先、試用層、Canon 升格與知識邊界。
- 劇本不再只是最終交付；劇本是圖片與後續影片的正式上游。
- 圖片工作重新成為主線，但採 `Git handoff + ChatGPT image generation`，不是在 Repository 內自動呼叫外部 API。
- 影片仍是最終產品方向；正式影片工作必須等圖片參考和關鍵影格鎖定後才開始。
- 舊文件中「圖片／影片永久暫停」或「劇本是唯一終點」的文字，視為歷史狀態；除非本文件特別保留，不再控制目前路線。
- 舊有的人工批准、候選資產、來源雜湊、禁止自動升格與 Canon 邊界繼續有效。

## 3. Non-Goals And Safety Boundaries

本藍圖目前不批准：

- 在 Kotlin 引擎內直接接 OpenAI API、Sora、Seedance、Oiioii 或其他圖片／影片 API。
- 建立 Web Server、開 Port、Android UI、Docker 或新後端框架。
- 讓 GPT 圖片生成結果自動成為 Canon、正式資產或 Production Lock。
- 讓 Codex、GPT 或技術測試 PASS 取代使用者的視覺批准。
- 把 `data/story.db` 的完整二進位資料庫當作跨工具交接格式。
- 把作者限定秘密、未揭露因果或角色不應知道的資訊送進公開圖片提示詞。
- 將所有失敗、重複或飄移圖片都提交進 Git，造成 Repository 膨脹。
- 在角色／場景鎖定前一次生成整章或長影片。

## 4. Roles

| 角色 | 主要責任 | 不負責 |
| --- | --- | --- |
| 使用者 | 最終產品方向、角色像不像、場景是否正確、圖片接受／退件、最終影片接受 | 不需拆 Kotlin 任務、設計 Schema、寫 CLI、跑測試或維護工作包 |
| GPT 總控 | 讀 GitHub、理解劇本與正式設定、指定 Codex 工作包、審查交接、生成圖片、判斷漂移、維持整體路線 | 不直接繞過引擎寫 SQLite，不把圖片自動升格，不假裝已取得未附加的參考圖 |
| Codex CLI | 跑引擎、查世界資料、產生與驗證劇本／分鏡／提示詞包、建立 Git 交接、匯入圖片結果、計算雜湊、跑測試、Commit／Push | 不替使用者決定角色美術，不自動批准圖片，不自行改 Canon 或產品方向 |
| GitHub | 正式文字規格、劇本、交接包、核准參考資產、版本、來源與審查紀錄的共同事實來源 | 不代替 SQLite Runtime，也不代表「檔案存在即已批准」 |
| 圖片模型 | 依 GPT 指令和已提供的參考圖生成圖片候選 | 不決定 Canon、角色設定、場景結構、批准狀態或影片剪輯 |
| 影片模型／剪輯工具 | 在圖片與逐鏡狀態鎖定後製作短鏡頭與拼接 | 不重新設計角色、服裝、場景或故事 |

## 5. Source-Of-Truth Layers

不得把不同層混成同一種「正式」。

1. **Runtime truth**
   - SQLite、`resolve-turn`、Validator、StateReducer、`contextHash`、`stateVersion`。
   - 管理實際互動回合與正式狀態。

2. **World and Canon truth**
   - `world/chenxing/formal/`、經驗證 Setting Pack、正式世界來源。
   - 定義角色、時代、服裝、地點、事件與秘密邊界。

3. **Script and shot truth**
   - 經審查的劇本、ScriptReadyScenePacket、Storyboard／Shot Packet。
   - 定義本次要畫什麼，不直接改 Runtime 或 Canon。

4. **Visual reference truth**
   - 作者鎖定的角色三視圖、服裝圖、愛醬三視圖、風格參考、場景聖經。
   - 只有明確 `AUTHOR_LOCKED`、`APPROVED_REFERENCE` 或既有 Candidate Reference 正式流程核准的資產可作正式參考。

5. **Image candidate layer**
   - GPT 或其他工具生成的圖。
   - 預設為 `CANDIDATE`；不得因生成成功或放進 Git 就視為正式。

6. **Approved image layer**
   - 使用者明確接受、來源檔與 SHA-256 被驗證、通過 Candidate Reference 流程的圖。
   - 可成為後續鏡頭或影片參考，但仍不等於故事 Canon。

7. **Video handoff layer**
   - 已鎖定影格、首尾幀、運動、時長、音訊與禁止漂移條件。
   - 只在圖片層完成後建立。

## 6. End-To-End Production Route

### Stage A — Codex Runs Engine And World Data

Codex 終端版依 Repository 規則執行：

- `query-world-source` 查正式世界來源。
- `start-session`、`prepare-turn`、`resolve-turn` 或既有劇本／小說化流程。
- `audit-context`、`audit-narrative-quality`、連續性／視角／知識邊界檢查。
- Setting Pack、Provisional Layer、場景與角色資料只能走既有正式流程。

Codex 不得把整個 DB Dump 交給 GPT。應整理成最小必要的劇本與視覺交接資料。

### Stage B — Script And Shot Package

Codex 必須把可進圖片工作的內容整理為：

- 劇本來源與 Git 路徑。
- 劇本或分鏡狀態。
- 場景 ID、Shot ID、順序與持續時間。
- 畫面上可見的角色、服裝、道具、位置與動作。
- 角色知識邊界與不可顯示的作者秘密。
- 連續性限制。
- 視覺參考路徑。
- 場景空間基準需求。
- 圖片生成提示詞草稿與禁止項。

劇本包必須 Commit 到 GitHub。交接不得只存在終端輸出或聊天文字。

### Stage C — GPT Image Handoff Package

Codex 建立 `ChatGptImageHandoff`，其中必須鎖定：

- Repository、Branch、Commit SHA。
- 劇本／分鏡來源檔。
- 本次 Shot 範圍。
- 角色／服裝／愛醬／風格／場景參考路徑。
- GPT 當前對話中必須附加的 Reference Upload Checklist。
- 每一鏡 Prompt、Negative Constraints、Continuity From／To。
- 預期輸出類型：角色基準、場景基準、關鍵影格或重生成。
- 審查狀態與回傳 Codex 的檔名規則。

交接包只有在已 Commit 並帶有 Commit SHA 後，才可標記 `READY_FOR_GPT`。

### Stage D — GPT Reads Git And Generates Images

使用者在 ChatGPT 輸入最小命令，例如：

```text
讀取 z72124223/ai-novel-engine，
Commit <commit-sha>，
交接包 <repo-path>，
依照 CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1 執行 shot-01。
```

GPT 必須：

1. 讀取指定 Commit 的交接包，不使用模糊的「最新檔案」替代。
2. 讀取交接包列出的正式角色、服裝、場景、風格與 Shot 來源。
3. 檢查所有必要參考圖是否已在目前 ChatGPT 對話中可供圖片工具使用。
4. 若缺少正式參考圖，只回報 `REFERENCE_UPLOAD_REQUIRED` 與精確檔案清單，不得假裝已綁定參考圖。
5. 先建立／核對角色與場景基準，再生成正式 Shot。
6. 一次生成一個 Shot 或一個明確的小組，不一次生成整章。
7. 生成結果一律標為 Candidate，等待使用者接受或退件。

### Stage E — Human Review

使用者只需做以下決定：

- `APPROVE_CANDIDATE`
- `REJECT_DRIFT`
- `REQUEST_VARIATION`
- `CHANGE_VISUAL_DIRECTION`

常見漂移代碼：

- `FACE_DRIFT`
- `BODY_DRIFT`
- `AGE_DRIFT`
- `HAIR_OR_GLASSES_DRIFT`
- `COSTUME_DRIFT`
- `AIJIANG_MARKING_DRIFT`
- `SCALE_DRIFT`
- `SCENE_GEOMETRY_DRIFT`
- `PROP_DRIFT`
- `WEATHER_OR_TIME_DRIFT`
- `STYLE_DRIFT`
- `SHOT_CONTINUITY_DRIFT`
- `EXTRA_CHARACTER_OR_OBJECT`

### Stage F — Return Image Result To Codex

目前 ChatGPT 圖片輸出不應被假設能直接寫入此私人 Repository。正式回傳流程是：

1. 使用者核准或保留圖片候選。
2. 將圖片檔放到 Codex 可存取的本機 intake 路徑，或在 Codex 工作階段提供該檔。
3. Codex 計算 SHA-256、建立 External Result Intake、執行人類審查與 Candidate Reference 驗證。
4. 只有核准圖片與必要 Metadata／Review Report 進 Git。
5. 被拒絕圖片預設不提交；只保留 Drift Report 或必要的低解析 Contact Sheet 證據。

這是目前不可完全取消的最小人工檔案交接。未來若工具提供安全的直接檔案連接，再另外批准自動化。

### Stage G — Video Handoff

只有在下列條件全部成立後才建立影片工作包：

- 角色與服裝 Reference 已鎖定。
- 場景空間基準已鎖定。
- Shot 關鍵影格已核准。
- 前後鏡連續性已檢查。
- 首幀／尾幀或可接受的運動範圍已定義。

正式影片原則：

- 每鏡以 3–8 秒為主。
- 優先 image-to-video，不讓影片模型重新設計角色。
- 先做微動、運鏡、雨霧、衣物與呼吸，再逐步增加大動作。
- 單鏡失敗只重做該鏡，不重做整章。
- 影片檔預設不直接進一般 Git；先以 Manifest、Hash、Review Report 管理，是否使用 Git LFS 另行決定。

## 7. Drift-Controlled Visual Bible

### 7.1 Character Bible

每個正式角色至少需要：

- 正面、側面、背面全身圖。
- 臉部近照與必要表情。
- 身高、體型、年齡、髮型、眼鏡、膚色等鎖定條件。
- 服裝版本。
- 道具與角色比例。
- 明確 Forbidden Drift。

塵行目前優先引用：

- `world/chenxing/formal/23_character_visual_appearance_v1.json`
- `world/chenxing/formal/25_lu_chenxing_opening_modern_costume_v1.json`
- `world/chenxing/assets/turnaround/lu_chenxing_photo_closest_174_80_turnaround_v2.png`
- `world/chenxing/assets/costume/lu_chenxing_opening_modern_costume_turnaround_v1.png`
- `world/chenxing/assets/turnaround/aijiang_production_turnaround_v1.jpg`

### 7.2 Location Bible

反覆出現的主要場景，不得只靠一段 Prompt。至少建立：

1. **主全景圖**：空間氣氛、尺度、主要結構。
2. **俯視平面圖**：入口、出口、牆、道路、固定物件與 Blocking Zone。
3. **四方向視圖**：北、東、南、西或場景自訂方向。
4. **空間錨點表**：每個固定物件的相對位置與不可移動規則。
5. **重要區域近景**：門洞、祭壇、石像、黃金暗格、玻璃瓶區等。
6. **光線／天氣版本**：只建立劇本真正需要的版本。
7. **比例標尺**：人物、愛醬、門、走廊、道具尺寸關係。

### 7.3 360° Panorama Rule

360° 等距柱狀全景是選用資產，不是唯一空間權威。

適合：

- 固定室內空間。
- 洞穴、廢墟、庭院、街口。
- 會從多個方向反覆取景的場景。

不適合單獨承擔：

- 複雜多房間地圖。
- 角色定裝。
- 精確道具位置。
- 所有鏡頭的透視與構圖。

正式場景基準仍採：

```text
主全景 + 俯視平面圖 + 四方向視圖 + 空間錨點 + 必要近景
```

360° 圖只作補充。若接縫、上下極點或幾何扭曲嚴重，標記 `REJECTED_DRIFT`。

## 8. Git Artifact Layout

新工作不得搬動現有已鎖定資產。建議新增的交接結構：

```text
world/chenxing/production/
├── script_packets/
│   └── <episode-or-scene>/
├── gpt_image_handoffs/
│   └── <handoff-id>/
│       ├── chatgpt_image_handoff.json
│       ├── reference_upload_checklist.json
│       ├── shot_queue.json
│       ├── prompt_manifest.json
│       └── review_log.json
└── video_handoffs/
    └── <handoff-id>/

world/chenxing/assets/
├── existing locked categories...
└── image_candidates/
    └── <handoff-id>/
```

規則：

- `production/` 儲存可重現的文字／JSON 交接。
- `assets/image_candidates/` 只放需要保留或已通過 Intake 的候選圖。
- 既有正式 turnaround、costume、style_reference 不移動。
- 核准資產是否移到其他分類，由 Candidate Reference／Registry 流程決定，不由檔案複製自動決定。
- 大型影片、重複失敗圖與暫存輸出留在 `out/` 或外部工作目錄，預設不 Commit。

## 9. Handoff State Machine

```text
DRAFT
→ VALIDATED
→ COMMITTED
→ READY_FOR_GPT
→ REFERENCE_UPLOAD_REQUIRED | GENERATION_READY
→ CANDIDATE_GENERATED
→ USER_APPROVED | REJECTED_DRIFT | VARIATION_REQUESTED
→ CODEX_INTAKE_READY
→ INTAKE_VALIDATED
→ REFERENCE_ACCEPTED
→ VIDEO_HANDOFF_READY
```

限制：

- `COMMITTED` 必須有 Git Commit SHA。
- `READY_FOR_GPT` 不代表參考圖已附加到 ChatGPT。
- `REFERENCE_UPLOAD_REQUIRED` 不是失敗，而是正式防飄移前置條件。
- `USER_APPROVED` 仍必須經 Codex Intake／Hash／Reference 驗證。
- `REFERENCE_ACCEPTED` 不等於 Canon 或 Final Media。

## 10. First Production Pilot

第一個正式防飄移 Pilot 不做完整第一話。範圍固定為：

```text
world/chenxing/formal/26_volume_one_opening_storyboard_v1.json
shot-01-tide-check
shot-02-strange-find
shot-03-overfocus
```

選擇理由：

- 三鏡都在同一海岸場景。
- 陸塵行與愛醬持續出現。
- 可測試臉、體型、眼鏡、服裝、狗花紋、人物比例與場景連續性。
- 動作幅度低，適合先驗證 Reference-First 流程。

Pilot 前置資產：

1. 陸塵行正式三視圖與臉部參考。
2. 開場現代服裝三視圖。
3. 愛醬正式三視圖。
4. 大坪海岸主全景。
5. 大坪海岸俯視平面圖。
6. Shot 01–03 需要的方向視圖與潮間帶錨點。
7. 開場可用風格模式。

Pilot 驗收：

- 三鏡為同一人物、同一身材、同一眼鏡、同一服裝。
- 愛醬毛色、面罩、眼睛、體型不改變。
- 海岸接縫、礁石與人物站位具有可追蹤連續性。
- Shot 01 → 02 → 03 的相機與行動合理。
- 不混入古裝、穿越後服裝、奇幻門樓或錯誤時代物件。
- 所有圖片維持 Candidate，直到使用者批准。

Pilot 通過後的順序：

1. Shot 04–09 海岸撤退。
2. 遺跡門檻與第一室 Location Bible。
3. Shot 10–13。
4. 遺跡深處 Location Bible。
5. Shot 14–18。
6. 短鏡頭 image-to-video Pilot。

## 11. Work Package Sequence

### WP-002A — Codex/GPT Image Handoff Foundation

文件、Schema、範例、路徑與現有 CLI 對接盤點；不修改 Kotlin。

### WP-002B — Handoff Compiler And Validator

在必要時新增最小 Kotlin／CLI，使正式 Script／Storyboard 與既有 Image Candidate Provider Handoff Packet 可產生 ChatGPT 專用交接包。不得接外部 API。

### WP-003 — Chenxing Visual Bible Inventory

盤點陸塵行、愛醬、服裝、風格、大坪海岸與遺跡的現有參考、缺口、已拒絕漂移與需要新生產的基準圖。

### WP-004 — Opening Shots 01–03 Handoff

由 Codex 產生並 Commit 第一個正式 `READY_FOR_GPT` 交接包。

### WP-005 — GPT Image Candidate Session

GPT 讀取指定 Commit，確認 Reference Upload，生成基準圖與 Shot 01–03 候選；使用者批准／退件。

### WP-006 — Image Intake And Reference Acceptance

Codex 匯入核准圖、計算 SHA、執行現有 External Result／Human Review／Candidate Reference 鏈並 Commit。

### WP-007 — Short Video Handoff Pilot

使用核准影格建立 3–8 秒 image-to-video 交接包；先不自動呼叫外部影片 API。

## 12. Model Selection

由 GPT 總控依工作包指定 Codex 模型：

- 文件、Schema、範例：目前可用 Codex 模型，`Medium` 或 `High`。
- 多檔案 Kotlin、CLI、Model、Validator：目前最強 Codex 模型，`High`。
- Git 歷史、資料遷移、Canon／安全邊界：目前最強 Codex 模型，`High`。
- 重複性檢查、低風險格式整理：可用較快模型，但不得降低驗證要求。

圖片生成由 GPT 依任務選擇圖片能力；正式角色圖必須優先使用 Reference-First，不得因模型較新就省略參考鎖。

## 13. User Interaction Contract

使用者不需要自行規劃、拆任務或跑整條產線。

正常情況只需：

1. 對 Codex 說：`讀取 PROJECT_STATE 與目前 Work Package，開始執行。`
2. Codex 完成並 Push 劇本／交接包後，在 ChatGPT 說：`讀取指定 Commit 與交接包，開始生成。`
3. 若 GPT 回報 `REFERENCE_UPLOAD_REQUIRED`，上傳清單中的參考圖。
4. 對圖片回答：`接受`、`退回：<漂移原因>` 或 `再生一版`。
5. 將核准圖片交給 Codex Intake；其餘 Git、Hash、驗證、Commit 由 Codex 處理。

## 14. Definition Of Done For This Blueprint

本藍圖真正完成，不是文件存在，而是：

- Codex 能從引擎／世界資料產生 Script／Shot 交接。
- 交接帶有 Git Commit SHA 和精確路徑。
- GPT 能讀取交接並知道需要哪些參考圖。
- 大坪海岸 Location Bible 已建立。
- Shot 01–03 已生成、審查並回傳 Codex。
- 核准圖通過 SHA 與 Candidate Reference 流程。
- 至少一個短鏡頭使用鎖定關鍵影格完成 image-to-video Pilot。
- 任何失敗都可定位到單一資產、單一 Shot 或單一交接，不需重做整章。
