# Codex Comic Video Workbench Local Delivery Brief

Status: `LOCAL_DELIVERY_BRIEF_DRAFT_FOR_USER`

Date: 2026-06-30

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## 目前結果

目前這個專案可以由 Codex 操作為本地工作台（local workbench）：它能從 Rain Gate 範例的世界觀/設定輸入與聲音時間線輸入，產生一支已驗證的本地預覽 MP4（local preview MP4）。

這支影片是本地成品檔證據（artifact evidence），位置在：

```text
out/codex_integrated_local_preview_verification/rain_gate_one_command_preview.mp4
```

MP4 事實：

- 檔案大小：`287815` bytes。
- 時長：`9.000000` seconds。
- 影像：`h264`, `1920x1080` video。
- 音訊：`aac` audio。

## 如何重跑

在專案根目錄執行：

```powershell
.\gradlew.bat :app:run --args="build-setting-comic-video-local-preview --setting-pack samples/comic_video_setting_source/setting_pack_rain_gate_sample.json --scene scene-rain-gate --voice-timeline-fill samples/comic_video_setting_source/voice_timeline_fill_rain_gate_preview_sample.json --output-dir out/codex_integrated_local_preview_verification --out-video out/codex_integrated_local_preview_verification/rain_gate_one_command_preview.mp4 --build codex-integrated-local-preview-verification-001 --panel-duration-ms 3000"
```

這個指令會走已文件化的一鍵 Rain Gate 本地預覽流程：設定包 JSON（setting pack JSON）到 image-facing proof（面向圖像生成的可審查證據），再到 comic-video local preview（有聲漫畫本地預覽）。

## 已通過驗證

- production package focused verification PASS：production package（製作包）草稿與驗證層通過聚焦測試。
- local renderer / setting-source focused verification PASS：local renderer（本地渲染器）與 setting-source bridge（設定來源橋接）通過聚焦測試。
- sample pack focused verification PASS：sample pack（範例包）通過聚焦測試與 sample diff hygiene check（範例差異格式檢查）。
- integrated local MP4 artifact PASS：已產生本地 MP4，並用 `ffprobe` 確認時長、影像串流與音訊串流。
- full Gradle regression PASS：完整 Gradle regression（完整回歸測試）通過。

## 這對你代表什麼

Codex 現在可以把這個 repo 當成本地工作台使用：從結構化 setting/sample inputs（設定/範例輸入）開始，經過 JSON file protocol（JSON 檔案協議）、validator（驗證器）與 local renderer（本地渲染器），產生可播放的有聲漫畫本地預覽 MP4。

用白話說：我們已經不只是「有藍圖」；現在有一條可重跑、可驗證、能輸出影片檔的本地流程。

## 這還不代表什麼

- This is not formal final media approval：這不是正式最終媒體批准。
- 這不是 external provider/media production line（外部供應商/媒體生產線）。
- 這不是 provider/media handoff（外部供應商或媒體交付）。
- 這不是 canon（正典）、registry（登錄）、release（發布）、production lock（製作鎖定）。
- 這不是 staging（暫存）、commit（提交）、push（推送）。
- 這也不是最終美術、最終配音、正式字幕審核或正式發片。

## 下一步決策

Next recommended decision options, exactly three options:

A. package/review current local workbench changes（打包/審查目前本地工作台變更）

B. create a new story/worldbuilding input and generate a fresh custom preview（建立新的故事/世界觀輸入並生成一支新的客製預覽）

C. open a 03 read-only review of the handoff/docs packet（開啟 03 對交接/文件包的唯讀審查）

## 邊界聲明

本交付簡報只描述本地 Kotlin/JVM CLI（Kotlin/JVM 命令列介面）加 SQLite / JSON file protocol（SQLite / JSON 檔案協議）的現況與本地 MP4 證據。

它不授權直接寫 SQLite、不呼叫 provider/API（外部供應商/API）、不建立 web/server（網站伺服器）、不做 final media approval（最終媒體批准）、不做 production media handoff（製作媒體交付）、不做 canon / registry / release / approval / production lock（正典 / 登錄 / 發布 / 批准 / 製作鎖定）、不 staging / commit / push（暫存 / 提交 / 推送），也不啟動 unattended infinite loop（無人值守無限循環）。
