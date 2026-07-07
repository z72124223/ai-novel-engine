# Codex Local Renderer Adapter Implementation Plan V0

Status: `LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_V0_IMPLEMENTED`

Date: 2026-07-01

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## 目的

Phase 8 是本地 renderer adapter implementation plan（渲染器轉接器實作計畫）草稿層。它讀取 Phase 7 的 `LOCAL_RENDERER_ADAPTER_DRY_RUN_PASS` 報告，輸出一份本地 JSON 計畫，說明未來 adapter 實作需要處理哪些欄位、風險和驗證。

這一步仍然不是 adapter implementation（轉接器實作）。它不產生 `ComicVideoRenderHandoffManifest`，不執行 renderer（渲染器）、ffmpeg 或 TTS（語音合成），不產生 media（媒體）或 final video（最終影片）。

## 指令

```powershell
.\gradlew.bat :app:run --args="plan-local-renderer-adapter-implementation --dry-run-report samples/local_story_input/rain_gate_local_renderer_adapter_dry_run_report_sample.json --plan rain-gate-local-renderer-adapter-implementation-plan-v0-001 --out samples/local_story_input/rain_gate_local_renderer_adapter_implementation_plan_sample.json"
```

輸入:

- Phase 7 dry-run report JSON

輸出:

- Phase 8 implementation plan JSON

## PASS 代表什麼

PASS 只代表:

- Phase 7 dry-run report 是 `PASS`
- Phase 7 狀態是 `LOCAL_RENDERER_ADAPTER_DRY_RUN_PASS`
- Phase 7 downstream boundary 是 `local_renderer_adapter_dry_run_only_not_media_generation`
- Phase 7 只允許下一步 `local_renderer_adapter_implementation_plan`
- panel mapping（分鏡映射）足夠形成未來 adapter 實作計畫
- `targetManifestFieldMappingPlan` 是 planning notes（規劃筆記），不是 manifest（清單）
- 下一步只允許 `local_renderer_adapter_implementation_review`

PASS 不代表:

- adapter 已實作
- `ComicVideoRenderHandoffManifest` 已產生
- renderer 已執行
- ffmpeg 或 TTS 已執行
- MP4 或 final video 已產生
- renderer readiness（渲染器就緒）
- MP4 readiness（MP4 就緒）
- final media approval（最終媒體批准）
- provider/media handoff（外部工具媒體交付）
- canon（正式正史）
- registry（正式登錄）
- release（發佈）
- approval（正式批准）
- production lock（生產鎖定）

## Rain Gate Sample

輸入:

```text
samples/local_story_input/rain_gate_local_renderer_adapter_dry_run_report_sample.json
```

輸出:

```text
samples/local_story_input/rain_gate_local_renderer_adapter_implementation_plan_sample.json
```

預期重點:

- `verdict=PASS`
- `status=LOCAL_RENDERER_ADAPTER_IMPLEMENTATION_PLAN_DRAFT`
- `downstreamBoundary=local_renderer_adapter_implementation_plan_only_not_implementation`
- `nextAllowedDownstreamStep=local_renderer_adapter_implementation_review`
- `mediaGenerated=false`
- `rendererInvocation=none`
- `providerInvocation=none`
- `ttsInvocation=none`
- `manifestGenerated=false`
- `adapterImplementationExecuted=false`
- `noExternalPaidProviderTools=true`

## Boundaries

Phase 8 固定邊界:

- local file-protocol JSON only（只走本地 JSON 檔案協議）
- no direct SQLite writes（不直接寫 SQLite）
- no provider/API calls（不呼叫外部 provider/API）
- no web/server（不建立網頁或伺服器）
- no renderer/ffmpeg/TTS execution（不執行渲染器、ffmpeg 或語音合成）
- no media generation（不產生媒體）
- no adapter implementation execution（不執行轉接器實作）
- no `ComicVideoRenderHandoffManifest` generation（不產生渲染交接清單）
- no final media approval（不做最終媒體批准）
- no provider/media handoff（不做外部工具媒體交付）
- no canon / registry / release / approval / production lock（不宣稱正式正史、登錄、發佈、批准或生產鎖定）
