# Codex → GitHub → GPT Image Production Execution Plan V1

Status: `ACTIVE_EXECUTION_PLAN`

Date: 2026-07-10

Parent blueprint: `docs/CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1.md`

## 1. Executive Decision

目前正式路線固定為：

```text
Codex 終端版
  跑引擎、查世界來源、整理正式劇本與逐鏡資料
→ Git Commit / Push
  保存可重現的 Script / Shot / Image Handoff
→ ChatGPT / GPT Image
  讀指定 Commit 與交接包，使用已上傳的鎖定參考圖生成圖片候選
→ 使用者
  只做接受、退件或方向修正
→ Codex 終端版
  匯入圖片、計算 SHA-256、驗證人類決策與 Candidate Reference、再 Commit
→ 圖片鎖定後
  建立短鏡頭 image-to-video 交接
```

劇本是正式上游，不是最終終點；圖片是現行主線輸出，影片是圖片鎖定後的下一層。

本計畫不在 Kotlin 引擎內直接接圖片或影片 API。跨工具協作以 Git 文字／JSON 交接與使用者明確批准為核心。

## 2. Active Sequence

為避免先寫大量自動化卻沒有真實圖片 Pilot，現行順序調整為：

1. `WP-002A` — 建立 Codex/GPT 圖片交接檔案協議、Schema、範例與路徑。
2. `WP-003` — 盤點塵行角色、服裝、風格、海岸與遺跡場景基準。
3. `WP-004` — 以現有劇本建立 Shot 01–03 第一個正式交接包。
4. `WP-005` — GPT 讀取指定 Commit，生成場景基準與 Shot 01–03 圖片候選。
5. `WP-006` — Codex 匯入核准圖片，走現有 External Result / Human Review / Candidate Reference 鏈。
6. `WP-002B` — **條件式**自動化：只有 Pilot 證明人工組裝交接容易出錯或重複成本過高時，才新增最小 Handoff Compiler / Validator。
7. `WP-007` — 以核准影格建立第一個 3–8 秒短鏡頭影片交接。

`WP-002B` 不再自動排在視覺盤點之前。先以真實 Pilot 驗證資料形狀，再決定哪些部分值得寫 Kotlin。

## 3. Git Commit Identity Rule

交接包不得嘗試把「包含自己」的 Git Commit SHA 寫進同一個 Commit，否則會形成不可滿足的自我引用。

正式規則：

- `sourceCommitSha`：交接所依據的劇本、分鏡、Canon 與參考資產所在 Commit。
- `handoffContentSha256`：交接 JSON 本身的正規化內容雜湊。
- `handoffCommitSha`：由使用者或 GPT 呼叫時在命令中提供，或由 Git 外部 Locator 記錄；**不要求寫進交接包自身**。
- `branch` 與 `handoffPath`：必填，與 `handoffCommitSha` 一起精確定位。

使用者呼叫 GPT 時使用：

```text
Repository: z72124223/ai-novel-engine
Commit: <handoffCommitSha>
Handoff: <handoffPath>
依照 CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1 執行。
```

GPT 必須讀取指定 Commit 的指定路徑，並核對 `handoffContentSha256`；不得用模糊的「最新版本」替代。

## 4. Codex Terminal Responsibilities

### 4.1 Engine And World Data

Codex 使用既有 CLI 與正式資料來源：

- 以 `query-world-source` 優先查世界來源。
- 需要互動回合時，使用 `start-session`、`prepare-turn`、`resolve-turn`。
- 使用既有 Context、敘事品質、連續性、知識邊界與 Canon 驗證。
- 不直接修改 SQLite，不繞過 Validator、StateReducer、`contextHash`、`stateVersion` 或交易。
- 不把完整 DB Dump 當作 GPT 交接。

### 4.2 Script Package

可進圖片工作的劇本包至少包含：

- `projectId`、`worldId`、章／場景／Shot ID。
- 正式來源路徑與 `sourceCommitSha`。
- 可見敘事、動作、對白與時長。
- 畫面角色與服裝版本。
- 場景、區域、道具與空間位置。
- 連續性、禁止出現內容與知識／秘密邊界。
- 使用的正式視覺參考路徑。
- 場景基準缺口。

Script Package 必須先驗證、Commit、Push，不能只留在 `out/` 或終端輸出。

### 4.3 GPT Image Handoff

每個 `ChatGptImageHandoff` 至少包含：

- `schemaVersion`
- `handoffId`
- `status`
- `projectId`
- `worldId`
- `branch`
- `sourceCommitSha`
- `handoffContentSha256`
- `sourceArtifacts`
- `shotScope`
- `referenceBindings`
- `referenceUploadChecklist`
- `locationBibleRequirements`
- `promptItems`
- `negativeConstraints`
- `continuityRules`
- `outputContract`
- `reviewContract`
- `returnToCodexContract`
- `visibilityAndSecretBoundary`

`READY_FOR_GPT` 必須表示：

- JSON Schema 驗證通過。
- 所有來源路徑存在於 `sourceCommitSha`。
- 每個正式角色有鎖定外觀／服裝參考。
- 每個 Shot 有場景與連續性來源。
- 沒有作者限定秘密進入圖片提示詞。
- 輸出檔名、Shot ID 與候選狀態唯一且明確。

## 5. GPT Responsibilities

GPT 收到 Repository、Commit 與 Handoff Path 後：

1. 讀取指定 Commit 的交接包。
2. 讀取交接包列出的劇本、角色、服裝、愛醬、風格與場景資料。
3. 核對 `handoffContentSha256` 與來源路徑。
4. 檢查目前 ChatGPT 對話是否真的含有圖片生成所需的正式參考圖。
5. 缺參考圖時回覆 `REFERENCE_UPLOAD_REQUIRED` 與精確清單，不假裝已綁定。
6. 場景尚未鎖定時，先做 Location Bible，不直接做正式 Shot。
7. 一次處理一個 Shot 或明確的小組。
8. 所有輸出均為 `CANDIDATE`，不能自動批准、登錄或成為 Canon。
9. 對每張候選附上 Shot ID、版本、使用參考、已知風險與回傳檔名。

## 6. Reference Upload Boundary

GitHub 中存在圖片路徑，不等於圖片生成工具已在目前對話中取得該影像作為編輯／參考輸入。

交接包必須輸出 `referenceUploadChecklist`，分為：

- `requiredForGeneration`
- `requiredForLikeness`
- `requiredForCostume`
- `requiredForLocationGeometry`
- `requiredForStyleOnly`
- `optional`

每項包含：

- Repository path
- Asset role
- Required / optional
- Expected SHA-256（若已知）
- Why needed
- Can proceed without it
- Degraded mode（若缺少只能做什麼）

沒有角色長相／服裝正式參考時，不得生成正式角色影格；最多做不含人物的場景探索或構圖草圖。

## 7. Location Bible Before Shots

反覆出現場景先建立：

1. 主全景。
2. 俯視平面圖。
3. 四方向視圖。
4. 空間錨點表。
5. 固定道具與 Blocking Zone。
6. 必要近景。
7. 劇本需要的光線／天氣狀態。
8. 人物、愛醬、門、走廊與道具比例標尺。

360° 等距柱狀全景為選用補充，不代替平面圖、方向圖與錨點。

第一個場景 Pilot 為大坪海岸，先建立：

- 海岸主全景。
- 潮間帶俯視平面圖。
- Shot 01–03 取景方向圖。
- 人物／愛醬站位與岩面接縫錨點。
- 2026 台灣海岸時間、天氣與物件禁用表。

## 8. Human Review Contract

使用者只需回覆：

- `APPROVE_CANDIDATE`
- `REJECT_DRIFT: <code> <reason>`
- `REQUEST_VARIATION: <requested change>`
- `CHANGE_VISUAL_DIRECTION: <new direction>`

使用者不需自行建立 Manifest、計算 Hash、跑 CLI 或整理 Git。

批准只表示「可以交給 Codex Intake」，尚不等於 Reference Accepted、Canon、Final Media 或 Release。

## 9. Return To Codex

核准圖片由使用者提供給 Codex 可存取的本機路徑或 Codex 工作階段。

Codex 必須：

1. 核對檔名、Shot ID 與 Handoff ID。
2. 計算檔案 SHA-256。
3. 建立 Image Candidate External Result Intake。
4. 建立／驗證人類 Review Decision。
5. 建立／驗證 Image Candidate Reference Packet。
6. 建立／驗證 Candidate Reference Registry／Acceptance Packet。
7. 只 Commit 核准候選、必要 Metadata、Hash 與 Review Report。
8. 被拒絕圖預設不進 Git；必要時只保存 Drift Report 或低解析證據。

## 10. Artifact Layout

```text
world/chenxing/production/
├── script_packets/
├── gpt_image_handoffs/
│   └── <handoff-id>/
│       ├── chatgpt_image_handoff.json
│       ├── reference_upload_checklist.json
│       ├── shot_queue.json
│       ├── prompt_manifest.json
│       └── review_log.json
├── location_bibles/
└── video_handoffs/

world/chenxing/assets/image_candidates/
└── <handoff-id>/
```

- `production/` 保存可重現文字／JSON。
- 鎖定 turnaround、costume、style reference 不搬動。
- 大型影片與暫存輸出不直接進一般 Git。
- 是否使用 Git LFS 需另立決策，不能由 WP-002A 自動決定。

## 11. First Pilot

範圍：

- `world/chenxing/formal/26_volume_one_opening_storyboard_v1.json`
- `shot-01-tide-check`
- `shot-02-strange-find`
- `shot-03-overfocus`

前置：

- 陸塵行正式外觀與臉部參考。
- 開場現代服裝參考。
- 愛醬正式三視圖。
- 大坪海岸 Location Bible。
- 開場允許風格模式。

成功條件：

- 三鏡角色臉、年齡、身材、眼鏡、服裝一致。
- 愛醬毛色、面罩、眼睛、體型與比例一致。
- 海岸幾何、岩面接縫、站位與鏡頭連續。
- 不出現古裝、穿越後造型、錯誤建築或錯誤年代物件。
- 每個錯誤可單鏡重做。

## 12. True Stop Conditions

Codex 只有遇到以下情況才停止並回報：

- 必要正式來源互相矛盾，無法判定哪個是 Author Locked。
- 工作需要修改 Canon、Migration、Runtime 寫入邊界或外部 API。
- 需要使用者決定角色長相、服裝或場景美術方向。
- 找不到必須存在的正式參考資產，且無合法降級模式。
- 現有 Candidate Reference 安全邊界必須被弱化才可繼續。

一般檔名、目錄、Schema 或測試細節由 Codex依 Repository 慣例自行完成，不需把技術規劃退回使用者。

## 13. User Commands

對 Codex：

```text
讀取 PROJECT_STATE.md、.codex/project_controller.md 與目前 Work Package，依規格完整執行、驗證、Commit、Push，完成後回報此聊天窗口。
```

對 GPT：

```text
讀取 z72124223/ai-novel-engine，Commit <sha>，交接包 <path>，依 CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1 執行。缺正式參考圖就列出 REFERENCE_UPLOAD_REQUIRED，不要用純文字替代正式參考。
```

對 Codex Intake：

```text
讀取原 Handoff ID 與使用者核准圖片，執行 Hash、External Result、Human Review、Candidate Reference、Commit 與 Push；不得自動升格 Canon。
```
