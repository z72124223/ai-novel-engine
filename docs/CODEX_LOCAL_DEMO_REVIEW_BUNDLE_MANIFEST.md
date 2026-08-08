# Codex Local Demo Review Bundle Manifest

Status: `LOCAL_DEMO_REVIEW_BUNDLE_DRAFT`

Date: 2026-07-01

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

Control thread `01`: `019f0dd1-1543-7f22-b646-5c12d07d168d`

Worker thread `02`: `019f1882-d5c6-72a3-893c-6d43cd097835`

Reviewer thread `03`: `019f1ad1-2926-70c3-b85c-267df6fe331e`

## Purpose

This manifest records the local demo review bundle for preserving the current comic-video workbench result. It is decision-support only: no staging approval, no commit approval, no release, no production lock, and no external provider/media line.

## Accepted Local Demo Package

Accepted local demo package path:

```text
out/codex_local_demo_package/
```

Exact generated package files:

- `out/codex_local_demo_package/rain_gate_one_command_preview.mp4`
- `out/codex_local_demo_package/README.md`
- `out/codex_local_demo_package/verification_summary.json`

Verified package facts:

- `rain_gate_one_command_preview.mp4`
- Byte size: `287815`
- Duration: `9.000000` seconds
- Video: `h264`, `1920x1080`
- Audio: `aac`
- Package status: `LOCAL_DEMO_PACKAGE_READY`
- full Gradle regression PASS
- 03 read-only PASS

## Review Scope If Preserving The Current Work

### Dirty Tracked Files

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `docs/COMIC_VIDEO_PRODUCTION_FEATURE_PLAN.md`
- `docs/COMIC_VIDEO_WORKBENCH_CLI_WORKFLOW.md`
- `docs/COMIC_VIDEO_WORKBENCH_CURRENT_PHASE_COMPLETION.md`
- `docs/HANDOFF.md`

### Untracked Docs And Control Files

- `.agents/skills/comic-video-production-package/SKILL.md`
- `docs/CODEX_CHAT_WINDOW_HANDOFF_PROTOCOL.md`
- `docs/CODEX_CHAT_WINDOW_HANDOFF_SMOKE_TEST.md`
- `docs/CODEX_COMIC_VIDEO_LOCAL_DEMO_PACKAGE.md`
- `docs/CODEX_COMIC_VIDEO_LOCAL_PREVIEW_ARTIFACT_VERIFICATION.md`
- `docs/CODEX_COMIC_VIDEO_WORKBENCH_LOCAL_DELIVERY_BRIEF.md`
- `docs/CODEX_DIRTY_WORKTREE_GROUPING_SNAPSHOT.md`
- `docs/CODEX_HANDOFF_LOOP_03_REVIEW_REQUEST.md`
- `docs/CODEX_LOCAL_DEMO_REVIEW_BUNDLE_MANIFEST.md`
- `docs/CODEX_NO_EXTERNAL_TOOLS_LOCAL_VIDEO_AUTOMATION_ROADMAP.md`
- `docs/CODEX_SKILL_SHORT_DRAMA_WORKFLOW_BLUEPRINT.md`

### Untracked Main Code Files

- `app/src/main/kotlin/com/localnovel/engine/model/ComicVideoProductionPackageModels.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ComicVideoLocalRendererService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ComicVideoProductionPackageDraftService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ComicVideoProductionPackageValidationService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/SettingImageFacingProofPreviewBuildService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/SettingPackScriptPacketDraftService.kt`

### Untracked Test Files

- `app/src/test/kotlin/com/localnovel/engine/ComicVideoLocalPreviewBuildCliTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ComicVideoLocalRendererServiceTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ComicVideoProductionPackageDraftServiceTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ComicVideoProductionPackageValidationServiceTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ComicVideoWorkbenchSamplePackTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/SettingComicVideoLocalPreviewBuildCliTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/SettingImageFacingProofPreviewBuildCliTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/SettingPackScriptPacketDraftServiceTest.kt`

### Untracked Sample Files

- `samples/comic_video_setting_source/README.md`
- `samples/comic_video_setting_source/setting_pack_rain_gate_sample.json`
- `samples/comic_video_setting_source/script_packet_draft_rain_gate_sample.json`
- `samples/comic_video_setting_source/script_packet_draft_rain_gate_validation.json`
- `samples/comic_video_setting_source/setting_script_packet_draft_build_report.json`
- `samples/comic_video_setting_source/voice_timeline_fill_rain_gate_preview_sample.json`
- `samples/comic_video_setting_source/image_facing_preview_build/image_facing_delivery_proof.json`
- `samples/comic_video_setting_source/image_facing_preview_build/reviewable_storyboard_packet.json`
- `samples/comic_video_setting_source/image_facing_preview_build/script_packet_draft.json`
- `samples/comic_video_setting_source/image_facing_preview_build/script_packet_draft_validation.json`
- `samples/comic_video_setting_source/image_facing_preview_build/setting_image_facing_proof_preview_build_report.json`
- `samples/comic_video_setting_source/image_facing_preview_build/setting_script_packet_draft_build_report.json`
- `samples/comic_video_setting_source/image_facing_preview_build/storyboard_human_review_preflight.json`
- `samples/comic_video_setting_source/image_facing_preview_build/storyboard_packet_draft.json`
- `samples/comic_video_setting_source/image_facing_preview_build/storyboard_packet_draft_validation.json`
- `samples/comic_video_setting_source/image_facing_preview_build/storyboard_packet_review.json`
- `samples/comic_video_setting_source/image_facing_preview_build/storyboard_planning_brief.json`
- `samples/comic_video_setting_source/image_facing_preview_build/storyboard_story_movement_audit.json`
- `samples/comic_video_setting_source/production_package_preview/production_package_draft.json`
- `samples/comic_video_setting_source/production_package_preview/production_package_validation.json`
- `samples/comic_video_workbench/README.md`
- `samples/comic_video_workbench/comic_video_packet_filled_sample.json`
- `samples/comic_video_workbench/comic_video_packet_sample.json`
- `samples/comic_video_workbench/comic_video_packet_validation_sample.json`
- `samples/comic_video_workbench/comic_video_render_handoff_manifest_sample.json`
- `samples/comic_video_workbench/image_facing_delivery_proof_sample.json`
- `samples/comic_video_workbench/voice_timeline_fill_sample.json`
- `samples/comic_video_workbench/voice_timeline_fill_template_sample.json`
- `samples/comic_video_workbench_zh_tw/README.md`
- `samples/comic_video_workbench_zh_tw/comic_video_packet_filled_zh_tw_sample.json`
- `samples/comic_video_workbench_zh_tw/comic_video_packet_validation_zh_tw_sample.json`
- `samples/comic_video_workbench_zh_tw/comic_video_packet_zh_tw_sample.json`
- `samples/comic_video_workbench_zh_tw/comic_video_render_handoff_manifest_zh_tw_sample.json`
- `samples/comic_video_workbench_zh_tw/image_facing_delivery_proof_zh_tw_sample.json`
- `samples/comic_video_workbench_zh_tw/production_package_preview/production_package_draft_zh_tw_sample.json`
- `samples/comic_video_workbench_zh_tw/production_package_preview/production_package_validation_zh_tw_sample.json`
- `samples/comic_video_workbench_zh_tw/voice_timeline_fill_template_zh_tw_sample.json`
- `samples/comic_video_workbench_zh_tw/voice_timeline_fill_zh_tw_sample.json`

## Explicit Excluded Items

- no staging approval
- no commit approval
- no release
- no production lock
- no external provider/media line
- no final media approval
- no provider/API calls
- no web/server
- no direct SQLite writes
- no canon or registry approval
- no unattended infinite loop

## Minimal Verification Already Passed

- Focused production package verification: PASS.
- Focused local renderer / setting-source verification: PASS.
- Focused sample verification: PASS.
- Integrated local MP4 artifact verification: PASS.
- Local demo package verification: PASS.
- full Gradle regression PASS.
- 03 read-only PASS.
- Current package JSON check: `out/codex_local_demo_package/verification_summary.json` is valid JSON and contains `LOCAL_DEMO_PACKAGE_READY`.

## Suggested Next Review Order

1. Review docs/control evidence first: `docs/CODEX_COMIC_VIDEO_LOCAL_DEMO_PACKAGE.md`, this manifest, roadmap, `docs/HANDOFF.md`, and `PLANS.md` top sections.
2. Review shared tracked files next: `Main.kt`, `Models.kt`, and comic-video workflow/completion docs, because multiple local demo features depend on them.
3. Review untracked main-code services and tests together, because tests depend on the new services and shared CLI/model surfaces.
4. Review sample packs last as evidence fixtures, unless a test or JSON validation issue points to a sample blocker earlier.

This order should not require user intervention unless a blocker appears, a boundary becomes ambiguous, or `01` decides that staging/commit/package scope must change.

