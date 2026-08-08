# Comic Video Workbench Current Phase Completion

Status: `PASS_CURRENT_PHASE_LOCAL_JSON_WORKBENCH_PLUS_SETTING_SOURCE_IMAGE_FACING_PROOF_AND_LOCAL_PREVIEW_RENDERER`

Date: 2026-06-30

This report closes the current Codex-operated comic video workbench phase as a local Kotlin/JVM CLI plus JSON file protocol capability with a setting-source script draft bridge, a local-preview image-facing proof bridge, and an optional local preview MP4 renderer. The setting-source sample now reaches a playable local preview MP4 when paired with an editable local voice fill JSON. It does not close formal registry, release, provider/media production, sidecar rendering, final art, final voice acting, approval, canon, runtime integration, or production-lock work.

## Current-Phase Verdict

Verdict: `PASS`

The workbench can now prepare, fill, validate, hand off, and locally preview a comic video package through local JSON files:

```text
public SettingPack JSON
  -> build-setting-comic-video-local-preview
     -> create-setting-script-packet-draft
     -> PASS ScriptPacketDraftValidationReport
     -> build-setting-image-facing-proof-preview
  -> PASS ImageFacingDeliveryProof JSON
  -> editable ComicVideoVoiceTimelineFill JSON
  -> create-comic-video-packet
  -> ...
```

The already-proven downstream local preview chain is:

```text
PASS ImageFacingDeliveryProof JSON
  -> create-comic-video-packet
  -> create-comic-video-voice-timeline-fill-template
  -> fill-comic-video-voice-timeline
  -> validate-comic-video-packet
  -> create-comic-video-render-handoff-manifest
  -> PASS metadata-only render handoff manifest
  -> render-comic-video-local
  -> local preview MP4
```

This is enough for Codex to act as a local workbench for structured voiced-comic planning, handoff, and local preview video output. It is not a finished media-production system.

## Evidence Matrix

| Capability | Evidence | Current result |
| --- | --- | --- |
| Setting source script draft bridge | `create-setting-script-packet-draft`, `SettingPackScriptPacketDraftServiceTest`, `samples/comic_video_setting_source/` | PASS: derives a candidate `ScriptPacketDraft` from public setting-pack scene data, excludes non-public source material, and writes a PASS draft validation report. |
| Setting source image-facing proof preview bridge | `build-setting-image-facing-proof-preview`, `SettingImageFacingProofPreviewBuildCliTest`, `samples/comic_video_setting_source/image_facing_preview_build/` | PASS: derives a local-preview `ImageFacingDeliveryProof` through script validation, storyboard validation, storyboard review, human-review preflight, story movement audit, reviewable storyboard packaging, and image-facing proof creation. |
| Setting source local preview MP4 sample | `samples/comic_video_setting_source/voice_timeline_fill_rain_gate_preview_sample.json`, `build-comic-video-local-preview`, `ComicVideoLocalPreviewBuildCliTest`, `out/comic_video_setting_source_preview/rain_gate_setting_preview.mp4` | PASS: combines the setting-source proof with editable voice/subtitle metadata and renders a 9-second local preview MP4 with H.264 video plus AAC audio. |
| One-command setting source local preview MP4 wrapper | `build-setting-comic-video-local-preview`, `SettingComicVideoLocalPreviewBuildCliTest`, `out/setting_comic_video_one_command_preview/rain_gate_one_command_preview.mp4` | PASS: orchestrates setting-to-proof and proof-plus-voice-fill-to-MP4 in one local command while preserving both subchain report directories. |
| Comic video packet candidate | `create-comic-video-packet`, `ComicVideoPacketService`, `ComicVideoPacketServiceTest` | PASS: derives panel tracks from PASS `ImageFacingDeliveryProof` and keeps voice/audio/subtitle empty until filled. |
| Editable voice timeline template | `create-comic-video-voice-timeline-fill-template`, `ComicVideoVoiceTimelineFillServiceTest` | PASS: creates one editable audio/subtitle cue pair per packet panel using exact panel ids and panel durations. |
| Voice timeline fill | `fill-comic-video-voice-timeline`, `ComicVideoVoiceTimelineFillServiceTest` | PASS: merges voice cast, audio cues, subtitle cues, and timing metadata while rejecting unsafe media/provider fields. |
| Packet validation | `validate-comic-video-packet`, `ComicVideoPacketValidationTest` | PASS: checks source binding, cross references, positive timing, metadata-only boundary, and forbidden completion/media/provider claims. |
| Render handoff manifest | `create-comic-video-render-handoff-manifest`, `ComicVideoRenderHandoffManifestTest` | PASS: accepts only PASS validation reports and emits metadata-only panel, voice, subtitle, and render-setting timelines. |
| Local preview MP4 renderer | `render-comic-video-local`, `ComicVideoLocalRendererServiceTest` | PASS: dry-run validates PASS/FAIL behavior, and the sample manifest rendered to a local MP4 with H.264 video plus AAC audio. |
| End-to-end CLI JSON chain | `ComicVideoWorkbenchPipelineFixtureTest` | PASS: temp-file CLI chain reaches a PASS render handoff manifest from a PASS image-facing proof. |
| Human operator workflow | `docs/COMIC_VIDEO_WORKBENCH_CLI_WORKFLOW.md` | PASS: documents command order, editable fill template, validation gate, manifest gate, local preview renderer, and boundary checklist. |
| Committed sample pack | `samples/comic_video_workbench/`, `ComicVideoWorkbenchSamplePackTest` | PASS: committed sample files rerun through the CLI JSON chain and reach a PASS metadata-only handoff manifest. |

## What Is Complete

- Local JSON file protocol for the comic video workbench.
- Setting-pack public scene to validated script packet draft bridge.
- Setting-pack public scene to local-preview image-facing proof bridge through candidate storyboard gates.
- Setting-source proof plus editable voice fill to local preview MP4 sample path.
- One-command setting-source plus editable voice fill local preview MP4 wrapper.
- CLI commands for packet creation, voice timeline template creation, voice timeline fill, validation, and render handoff manifest creation.
- Structural validation that blocks failed upstream reports, cross-reference mismatches, non-positive timing, non-empty media/provider fields, and forbidden final-output claims.
- Metadata-only render handoff manifest for a future manual renderer or sidecar.
- Local preview renderer command that can generate PNG panels, WAV narration, and a playable MP4 from a PASS render handoff manifest.
- Local render report JSON that records generated artifacts and tool evidence.
- End-to-end CLI fixture evidence.
- Human-facing workflow documentation.
- Rerunnable committed sample JSON pack.

## What Is Not Complete

- Final panel art.
- Final narration or character voice acting.
- Final rendered subtitles as reviewed production assets.
- Final production MP4 or other release media.
- Provider/API submission.
- Sidecar renderer execution.
- Formal registry write.
- Asset approval, user approval, approved reference, registered reference, canon application, runtime scene creation, release tag, release branch, or production lock.

## Next-Version Prototype Addendum

After the current-phase baseline, the first production-package prototype slice adds:

```text
PASS ImageFacingDeliveryProof JSON
  + edited ComicVideoVoiceTimelineFill JSON
  -> create-comic-video-production-package-draft
  -> PASS ComicVideoProductionPackageDraft JSON
  -> validate-comic-video-production-package
  -> PASS ComicVideoProductionPackageValidationReport JSON
```

The sample output at `samples/comic_video_setting_source/production_package_preview/production_package_draft.json` contains 3 storyboard production rows, 3 shot prompt drafts, 5 asset prompt cards, 2 voice style cards, 9000 ms total duration, and 0 findings.

The sample validation report at `samples/comic_video_setting_source/production_package_preview/production_package_validation.json` contains 0 cross-reference mismatches, 0 prompt safety findings, 0 remote/provider references, 0 forbidden claims, 0 forbidden statuses, and 0 author-only leaks.

The repo-local Codex skill at `.agents/skills/comic-video-production-package/SKILL.md` now records the official local CLI workflow for inspecting, generating, validating, and reporting comic-video production packages without provider/API handoff.

This addendum does not change the current-phase verdict and does not create provider jobs, submitted prompts, generated media, asset approval, canon changes, registry entries, releases, or production locks.

## Verification Snapshot

Latest verification performed during this workbench phase:

```text
.\gradlew.bat test --no-daemon --console=plain --tests "com.localnovel.engine.ComicVideoVoiceTimelineFillServiceTest"
.\gradlew.bat test --no-daemon --console=plain --tests "com.localnovel.engine.ComicVideoLocalRendererServiceTest"
.\gradlew.bat test --no-daemon --console=plain --tests "com.localnovel.engine.SettingPackScriptPacketDraftServiceTest"
.\gradlew.bat test --no-daemon --console=plain --tests "com.localnovel.engine.SettingImageFacingProofPreviewBuildCliTest"
.\gradlew.bat test --no-daemon --console=plain --tests "com.localnovel.engine.ComicVideoProductionPackageDraftServiceTest"
.\gradlew.bat test --no-daemon --console=plain --tests "com.localnovel.engine.ComicVideoProductionPackageValidationServiceTest"
.\gradlew.bat test --no-daemon --console=plain --tests "com.localnovel.engine.ComicVideo*"
.\gradlew.bat test --no-daemon --console=plain
git diff --check
manual trailing-whitespace scan
git status --short --branch
ffprobe out/comic_video_local_renderer/sample_local_render.mp4
create-setting-script-packet-draft against samples/comic_video_setting_source/setting_pack_rain_gate_sample.json
build-setting-image-facing-proof-preview against samples/comic_video_setting_source/setting_pack_rain_gate_sample.json
build-comic-video-local-preview against samples/comic_video_setting_source/image_facing_preview_build/image_facing_delivery_proof.json plus voice_timeline_fill_rain_gate_preview_sample.json
build-setting-comic-video-local-preview against samples/comic_video_setting_source/setting_pack_rain_gate_sample.json plus voice_timeline_fill_rain_gate_preview_sample.json
create-comic-video-production-package-draft against samples/comic_video_setting_source/image_facing_preview_build/image_facing_delivery_proof.json plus voice_timeline_fill_rain_gate_preview_sample.json
validate-comic-video-production-package against samples/comic_video_setting_source/production_package_preview/production_package_draft.json
ffprobe out/comic_video_setting_source_preview/rain_gate_setting_preview.mp4
ffprobe out/setting_comic_video_one_command_preview/rain_gate_one_command_preview.mp4
ffmpeg volumedetect out/comic_video_setting_source_preview/rain_gate_setting_preview.mp4
ffmpeg volumedetect out/setting_comic_video_one_command_preview/rain_gate_one_command_preview.mp4
preview frame extraction and visual inspection
```

Results:

- focused voice timeline template/fill tests: PASS;
- focused local renderer tests: PASS;
- focused setting-pack script draft bridge tests: PASS;
- focused setting-pack image-facing proof preview bridge tests: PASS;
- focused production package draft tests: PASS;
- focused production package validation tests: PASS;
- focused `ComicVideo*` tests: PASS;
- full Gradle test suite: PASS;
- `git diff --check`: PASS with only Windows line-ending warnings;
- trailing-whitespace scan: PASS;
- sample local preview MP4: PASS, 1920x1080 H.264 video plus AAC audio;
- setting source sample draft: PASS `ScriptPacketDraftValidationReport`, 3 generated beats, 3 generated frame candidates, 1 excluded secret, 0 findings;
- setting source image-facing preview proof: PASS `ImageFacingDeliveryProof`, 3 generated storyboard frames, 3 image-facing frame cards, 1 excluded secret, PASS story movement audit, PASS reviewable storyboard packet, 0 findings;
- setting source local preview MP4: PASS `ComicVideoLocalPreviewBuildReport`, 9.000000 seconds, 1920x1080 H.264 video stream, AAC audio stream, non-silent audio, visible preview frame;
- one-command setting source local preview MP4: PASS setting proof report, PASS comic-video local preview report, 9.000000 seconds, 1920x1080 H.264 video stream, AAC audio stream, non-silent audio, visible preview frame;
- setting source production package draft: PASS `ComicVideoProductionPackageDraft`, 3 storyboard rows, 3 shot prompts, 5 asset prompt cards, 2 voice style cards, 9000 ms total duration, 0 findings;
- setting source production package validation: PASS `ComicVideoProductionPackageValidationReport`, 0 cross-reference mismatches, 0 prompt safety findings, 0 remote/provider references, 0 forbidden claims, 0 forbidden statuses, 0 author-only leaks;
- no Java/Gradle process remained after verification.

## Drift Check

No drift from the current workbench boundary was detected.

The implementation stayed inside:

- Kotlin/JVM CLI;
- local JSON file protocol;
- no SQLite writes for this workbench slice;
- no web server;
- no OpenAI/API/provider calls;
- generated media limited to local preview files under `out/`;
- no formal approval/canon/registry/release/production lock.

## Recommended Future Work

Future work should be planned as separate reviewed slices:

- Traditional Chinese package sample, production package human review gate, and later refinements to the repo-local Codex skill/runbook, tracked in `docs/CODEX_SKILL_SHORT_DRAMA_WORKFLOW_BLUEPRINT.md`;
- world/setting-driven Traditional Chinese render sample;
- stronger visual panel composition and title/subtitle layout;
- multi-character voice cast authoring from accepted script roles;
- installDist command examples;
- sidecar renderer input adapter;
- provider/media production;
- formal registry or release decision.
