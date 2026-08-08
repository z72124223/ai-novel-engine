# Codex Comic Video Local Demo Package

Status: `LOCAL_DEMO_PACKAGE_READY`

Date: 2026-06-30

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

Control thread `01`: `019f0dd1-1543-7f22-b646-5c12d07d168d`

Worker thread `02`: `019f1882-d5c6-72a3-893c-6d43cd097835`

Reviewer thread `03`: `019f1ad1-2926-70c3-b85c-267df6fe331e`

## Purpose

這份文件記錄本地 demo package（本地示範包）的內容與驗證結果。這個 package 是為了讓使用者能直接檢查與分享目前已驗證的 Rain Gate 有聲漫畫本地預覽影片。

這不是 formal final media approval（正式最終媒體批准）、external provider/media production（外部供應商或媒體製作線）、release（正式釋出）、canon（正典鎖定）、registry（登錄鎖定）、approval（批准），或 production lock（生產鎖定）。

## Package Path

```text
out/codex_local_demo_package/
```

Included files:

- `out/codex_local_demo_package/rain_gate_one_command_preview.mp4`
- `out/codex_local_demo_package/README.md`
- `out/codex_local_demo_package/verification_summary.json`

## MP4 Facts

Source MP4:

```text
out/codex_integrated_local_preview_verification/rain_gate_one_command_preview.mp4
```

Package MP4:

```text
out/codex_local_demo_package/rain_gate_one_command_preview.mp4
```

Verified facts:

- Byte size: `287815`
- Duration: `9.000000` seconds
- Video stream: `h264`, `1920x1080`
- Audio stream: `aac`

## Prior Verification

- focusedProductionPackage: `PASS`
- focusedLocalRendererSettingSource: `PASS`
- focusedSamples: `PASS`
- integratedLocalPreviewArtifact: `PASS`
- fullGradleRegression: `PASS`
- reviewer03: `PASS`

## Boundaries

- Package step uses no external paid/provider tool.
- no provider/API
- no web/server
- no release
- no production lock
- no staging/commit/push
- No Kotlin code edits.
- No sample edits.
- No direct SQLite writes.
- No final media approval.
- No provider/media handoff.
- No canon, registry, release, approval, or production lock.
- No new thread creation/contact.
- No unattended infinite loop.
