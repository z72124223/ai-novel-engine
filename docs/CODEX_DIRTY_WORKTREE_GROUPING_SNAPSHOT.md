# Codex Dirty Worktree Grouping Snapshot

Status: `READ_ONLY_DECISION_SUPPORT_SNAPSHOT`

Date: 2026-06-30

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

Control thread `01`: `019f0dd1-1543-7f22-b646-5c12d07d168d`

Worker thread `02`: `019f1882-d5c6-72a3-893c-6d43cd097835`

This snapshot is read-only and decision-support only. It records current dirty-worktree groups so `01` can choose the next review or staging direction without guessing.

It is not cleanup apply, no staging, no commit, no push, no deletion, no file move, no Kotlin behavior change, no schema change, no SQLite write, no provider/API work, no web/server work, no canon, no registry, no release, no production lock, and no media handoff.

## 1. Inventory Basis

Read-only commands used:

```powershell
git status --short --branch
git status --short --untracked-files=all
git diff --stat
rg --files samples docs .agents app/src/main/kotlin app/src/test/kotlin
```

Current inventory before adding this snapshot:

| Class | Count |
| --- | ---: |
| Tracked modified paths | 7 |
| Untracked paths | 56 |
| Untracked docs paths | 3 |
| Untracked `.agents` paths | 1 |
| Untracked main-code paths | 6 |
| Untracked test paths | 8 |
| Untracked sample paths | 38 |

Tracked modified paths:

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `docs/COMIC_VIDEO_PRODUCTION_FEATURE_PLAN.md`
- `docs/COMIC_VIDEO_WORKBENCH_CLI_WORKFLOW.md`
- `docs/COMIC_VIDEO_WORKBENCH_CURRENT_PHASE_COMPLETION.md`
- `docs/HANDOFF.md`

This snapshot adds one additional untracked docs path: `docs/CODEX_DIRTY_WORKTREE_GROUPING_SNAPSHOT.md`.

## 2. Handoff-Loop Protocol/Docs Group

Purpose:

- Preserve the governed `01 -> 02 -> 01` chat-window handoff protocol, smoke-test evidence, and model-selection policy.

Candidate paths:

- `docs/HANDOFF.md`
- `docs/CODEX_CHAT_WINDOW_HANDOFF_PROTOCOL.md`
- `docs/CODEX_CHAT_WINDOW_HANDOFF_SMOKE_TEST.md`
- `docs/CODEX_DIRTY_WORKTREE_GROUPING_SNAPSHOT.md`
- `.agents/skills/comic-video-production-package/SKILL.md`
- `PLANS.md`

Blockers and risks:

- `docs/HANDOFF.md` and `PLANS.md` are shared tracked files touched by many slices.
- `docs/CODEX_CHAT_WINDOW_HANDOFF_PROTOCOL.md` and smoke-test docs are untracked and should be reviewed together if `01` wants to preserve the handoff loop.
- `.agents/skills/comic-video-production-package/SKILL.md` is a repo-local Codex skill/runbook, related to workflow operation but not the same as the chat-window handoff protocol.
- This group is read-only decision-support only; it is no staging approval.

Suggested minimum review:

```powershell
git diff --check -- PLANS.md docs/HANDOFF.md docs/CODEX_CHAT_WINDOW_HANDOFF_PROTOCOL.md docs/CODEX_CHAT_WINDOW_HANDOFF_SMOKE_TEST.md docs/CODEX_DIRTY_WORKTREE_GROUPING_SNAPSHOT.md .agents/skills/comic-video-production-package/SKILL.md
```

## 3. Comic-Video Production Package Code Group

Purpose:

- Preserve the draft production package models, generation service, validation service, and CLI wiring.

Candidate paths:

- `app/src/main/kotlin/com/localnovel/engine/model/ComicVideoProductionPackageModels.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ComicVideoProductionPackageDraftService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ComicVideoProductionPackageValidationService.kt`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `docs/COMIC_VIDEO_WORKBENCH_CLI_WORKFLOW.md`
- `docs/COMIC_VIDEO_WORKBENCH_CURRENT_PHASE_COMPLETION.md`
- `docs/CODEX_SKILL_SHORT_DRAMA_WORKFLOW_BLUEPRINT.md`
- `samples/comic_video_setting_source/production_package_preview/`
- `samples/comic_video_workbench_zh_tw/production_package_preview/`

Blockers and risks:

- `Main.kt` and `Models.kt` are shared tracked files and cannot be staged as if they belong only to this group unless `01` approves that dependency grouping.
- Package code depends on existing comic-video packet, voice fill, and validation surfaces.
- Production package JSON remains draft-only and review-only. It is not provider media, user approval, canon, registry, release, or production lock.
- Untracked sample files are evidence and must not be mixed with code staging unless `01` explicitly wants a sample-inclusive review bundle.

Suggested minimum review:

```powershell
./gradlew.bat test --no-daemon --console=plain --tests "com.localnovel.engine.ComicVideoProductionPackage*"
```

Gradle was not run for this snapshot because this task is read-only docs/control work.

## 4. Comic-Video Local Renderer / Setting-Source Bridge Code Group

Purpose:

- Preserve local preview MP4 rendering, one-command local preview wrapper behavior, setting-pack-to-script draft bridge, and setting-to-image-facing-proof preview bridge.

Candidate paths:

- `app/src/main/kotlin/com/localnovel/engine/service/ComicVideoLocalRendererService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/SettingPackScriptPacketDraftService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/SettingImageFacingProofPreviewBuildService.kt`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `docs/COMIC_VIDEO_WORKBENCH_CLI_WORKFLOW.md`
- `docs/COMIC_VIDEO_WORKBENCH_CURRENT_PHASE_COMPLETION.md`
- `samples/comic_video_setting_source/`
- `samples/comic_video_workbench/`
- `samples/comic_video_workbench_zh_tw/`

Blockers and risks:

- `Main.kt` and `Models.kt` are shared tracked dependencies for multiple CLI and model slices.
- Local renderer work may generate files under `out/`; this snapshot does not include ignored/generated media outputs.
- Setting-source sample files are untracked evidence and should be reviewed separately from code if `01` wants narrow code review.
- This group must remain local renderer / local preview only. It is not provider/API automation, final media approval, canon, registry, release, or production lock.

Suggested minimum review:

```powershell
./gradlew.bat test --no-daemon --console=plain --tests "com.localnovel.engine.*ComicVideo*"
./gradlew.bat test --no-daemon --console=plain --tests "com.localnovel.engine.Setting*"
```

Gradle was not run for this snapshot because this task is read-only docs/control work.

## 5. Comic-Video Tests Group

Purpose:

- Preserve focused tests that verify comic-video packet flow, local renderer, production package draft/validation, sample packs, and setting-source bridge.

Candidate paths:

- `app/src/test/kotlin/com/localnovel/engine/ComicVideoLocalPreviewBuildCliTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ComicVideoLocalRendererServiceTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ComicVideoProductionPackageDraftServiceTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ComicVideoProductionPackageValidationServiceTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ComicVideoWorkbenchSamplePackTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/SettingComicVideoLocalPreviewBuildCliTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/SettingImageFacingProofPreviewBuildCliTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/SettingPackScriptPacketDraftServiceTest.kt`

Blockers and risks:

- These tests depend on the matching untracked main-code services and shared `Main.kt` / `Models.kt` changes.
- Some tests exercise generated sample JSON under `samples/`; do not review tests without deciding whether sample evidence is part of the same review bundle.
- Kotlin daemon memory may need the local one-shot setting previously used for full test verification, but this snapshot does not change project configuration.

Suggested minimum review:

```powershell
$env:ORG_GRADLE_PROJECT_kotlin_daemon_jvmargs='-Xmx1536m'
./gradlew.bat test --no-daemon --console=plain --tests "com.localnovel.engine.ComicVideo*"
```

## 6. Comic-Video Samples Group

Purpose:

- Preserve local JSON sample packs and production package evidence for English, Traditional Chinese, and setting-source Rain Gate workflows.

Candidate paths:

- `samples/comic_video_workbench/`
- `samples/comic_video_workbench_zh_tw/`
- `samples/comic_video_setting_source/`

Current sample classes:

- English workbench packet / fill / validation / render handoff sample.
- Traditional Chinese workbench packet / fill / validation / render handoff sample.
- Traditional Chinese production package draft and validation sample.
- Rain Gate setting source pack, script draft, image-facing preview chain, voice fill, production package draft, and production package validation sample.

Blockers and risks:

- 38 untracked sample paths are present before adding this snapshot.
- Samples are evidence, not formal canon, approval, registry, release, production lock, or provider media.
- Some Traditional Chinese sample text can display incorrectly in default PowerShell code page unless read as UTF-8.
- Samples should not be staged without `01` deciding whether samples are included with code review or reviewed as a separate evidence bundle.

Suggested minimum review:

```powershell
git diff --check -- samples/comic_video_workbench samples/comic_video_workbench_zh_tw samples/comic_video_setting_source
```

## 7. Shared Tracked Files Group

Purpose:

- Identify tracked files that multiple dirty slices depend on and that should not be staged as single-group leftovers.

Candidate paths:

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `docs/COMIC_VIDEO_PRODUCTION_FEATURE_PLAN.md`
- `docs/COMIC_VIDEO_WORKBENCH_CLI_WORKFLOW.md`
- `docs/COMIC_VIDEO_WORKBENCH_CURRENT_PHASE_COMPLETION.md`
- `docs/HANDOFF.md`

Blockers and risks:

- `PLANS.md` is the live plan and drift log for many slices.
- `Main.kt` contains shared CLI wiring across comic-video, setting-source, renderer, and production-package commands.
- `Models.kt` contains shared model/report surfaces beyond the new separate production package model file.
- Workflow and completion docs reference both code behavior and sample evidence.
- `docs/HANDOFF.md` now includes handoff-loop protocol, smoke-test evidence, and model-selection policy pointers.
- This shared group is the main blocker to narrow staging. `01` must decide whether to include shared tracked files with a code bundle, docs bundle, or a broader integrated review.

Suggested minimum review:

```powershell
git diff --check -- PLANS.md app/src/main/kotlin/com/localnovel/engine/Main.kt app/src/main/kotlin/com/localnovel/engine/model/Models.kt docs/COMIC_VIDEO_PRODUCTION_FEATURE_PLAN.md docs/COMIC_VIDEO_WORKBENCH_CLI_WORKFLOW.md docs/COMIC_VIDEO_WORKBENCH_CURRENT_PHASE_COMPLETION.md docs/HANDOFF.md
```

## 8. Practical Decision Options For 01

Option A: Review handoff-loop docs first.

- Smallest current docs-only group.
- Candidate scope: protocol, smoke test, model policy, grouping snapshot, `docs/HANDOFF.md`, and relevant `PLANS.md` entries.
- Risk: `PLANS.md` and `docs/HANDOFF.md` are shared tracked files, so a clean staging plan still needs care.

Option B: Review comic-video production package code first.

- Candidate scope: production package models, draft service, validation service, CLI wiring, focused tests, and production-package samples.
- Risk: requires shared `Main.kt`, `Models.kt`, workflow docs, tests, and samples to be considered together or explicitly split.

Option C: Review local renderer / setting-source bridge first.

- Candidate scope: local renderer service, setting script packet draft service, setting image-facing proof preview service, setting/local preview tests, and setting-source samples.
- Risk: overlaps heavily with shared `Main.kt`, `Models.kt`, workflow docs, and sample packs.

Option D: Keep grouping only and ask for a 03 read-only review prompt.

- Candidate scope: this report only.
- Risk: 03 should not review as staging approval; it should only check whether grouping is complete and non-misleading.

## 9. Boundary Statement

This report is read-only and decision-support only.

It grants no staging, no commit, no push, no cleanup apply, no deletion, no file move, no provider/media handoff, no direct SQLite work, no web/server work, no canon, no registry, no release, and no production lock.

Any next action requires a new explicit `NEXT_TASK` from `01`.

