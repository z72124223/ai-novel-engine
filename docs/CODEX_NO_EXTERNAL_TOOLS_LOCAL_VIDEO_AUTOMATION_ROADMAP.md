# Codex No-External-Tools Local Video Automation Roadmap

Status: `LOCAL_VIDEO_AUTOMATION_ROADMAP_DRAFT`

Date: 2026-07-01

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## 目標

這條 roadmap（路線圖）的目標，是先把目前的 local demo（本地示範）推到更像產品的狀態：可以從故事設定、角色、場景、分鏡、聲音時間線與本地渲染資料，穩定產生可播放的本地有聲漫畫預覽影片。

近期原則是 no external paid/provider tools（不使用外部付費或供應商工具）。外部工具可以等到 monetization/funding（變現或資金）之後再加入；它們不是目前本地 demo 路線的必要條件。

## 現在可以在本地自動化的事

- 從 setting pack（設定包）與 sample input（範例輸入）整理故事場景、角色、可見資訊與作者側資料。
- 產出 script packet（劇本包）、storyboard packet（分鏡包）、comic-video packet（有聲漫畫影片包）與 voice timeline fill（聲音時間線填充）。
- 透過 file-protocol JSON（檔案協議 JSON）在 CLI（命令列介面）之間交接資料。
- 使用本地 renderer（本地渲染器）生成 preview MP4（預覽影片）。
- 產出 verification summary（驗證摘要）、manifest（清單）與 QA（品質檢查）報告。

## 仍應維持 file-protocol JSON 的部分

- 故事輸入、世界觀設定、角色設定、分鏡、聲音時間線與素材卡，都應先保留為 file-protocol JSON。
- 人工或外部工具生成的內容，只能以 JSON 檔案交回本 repo，不直接接 provider/API。
- 驗證、差異檢查、審查包與本地渲染結果，都應能被本地 CLI 或 read-only docs 追蹤。
- 不要把 provider API integration（供應商 API 整合）做進近期主線；這會增加成本、依賴與邊界風險。

## Medium Phase A：Local Story Input Pack

目標：把一個新故事或世界觀輸入整理成可驗證的本地故事輸入包。

產物：

- 世界觀摘要 JSON。
- 角色卡 JSON。
- 場景卡 JSON。
- 風格與禁忌規則 JSON。
- 目標影片長度、語氣、節奏與受眾設定。

驗收：

- 所有 JSON 可解析。
- 沒有把作者秘密洩漏到角色可見資訊。
- 沒有直接寫 SQLite。
- 沒有 provider/API calls。

## Medium Phase B：Local Asset / Prompt Cards

目標：把影片需要的畫面、角色、背景、聲音、字幕與提示詞整理成素材卡與提示詞卡，但仍不送外部工具。

產物：

- visual asset cards（視覺素材卡）。
- character appearance cards（角色外觀卡）。
- background cards（背景卡）。
- voice cards（聲音卡）。
- prompt cards（提示詞卡）。
- review checklist（審查清單）。

驗收：

- 每張卡都有用途、來源、審查狀態與邊界。
- 素材卡不是 approved asset（已批准素材），不是 canon（正典），不是 registry（登錄）。
- 提示詞只作為本地準備，不代表外部生成已發生。

## Medium Phase C：Local Render Package And QA

目標：用本地資料生成一個更完整的 local render package（本地渲染包）與 QA 報告。

產物：

- comic-video packet。
- voice timeline fill。
- local preview MP4。
- render manifest。
- verification_summary.json。
- QA report。
- user-facing README。

驗收：

- MP4 存在且 byte size 大於 0。
- 可用時檢查 duration（時長）、video stream（影像串流）、audio stream（音訊串流）。
- full Gradle regression PASS 必須在進入包裝/審查前維持有效，除非 `01` 明確接受只做 docs/readiness。
- 03 read-only PASS 可以作為審查證據，但不是 release approval（釋出批准）。

## Stop Conditions / Fail-Stop Rules

遇到以下情況必須 fail-stop（失敗即停止）：

- JSON 無法解析或 validator（驗證器）不通過。
- 需要直接寫 SQLite。
- 需要 provider/API calls。
- 需要 web/server 或開 port。
- 需要外部 paid/provider tool 才能繼續。
- 需要正式 canon、registry、release、approval 或 production lock。
- 需要 staging、commit 或 push，但沒有 `01` 或使用者明確指派。
- 影片產物不存在、大小為 0、或媒體檢查失敗。
- 任務邊界不清，或需要使用者產品決策。

## Boundary Statement

這條 roadmap 是本地 demo/product readiness（本地示範與產品準備度）文件。它不是 no staging approval（不是暫存批准）、不是 commit approval（提交批准）、不是 release（正式釋出）、不是 production lock（生產鎖定），也不是 external provider/media line（外部供應商或媒體製作線）。

近期路線仍是本地 Kotlin/JVM CLI、SQLite、file-protocol JSON 與 generated local artifacts（本地生成產物）。外部工具可以之後再加，但不是現在把 demo 做強的前提。
