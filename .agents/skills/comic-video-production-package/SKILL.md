---
name: comic-video-production-package
description: Operate this repo's local comic-video production package workbench through official Kotlin/JVM CLI commands only. Use when Codex must create, validate, inspect, or report a draft-only production package from PASS image-facing proof and voice timeline fill artifacts, including storyboard rows, shot prompt drafts, asset prompt cards, voice style cards, local preview MP4 evidence, and safety boundaries. Never use for provider/API handoff, external generation, approval, canon, registry, release, or production lock.
---

# comic-video-production-package

Use this skill to operate the local comic-video production package workflow.

## Guardrails

- Use only official Gradle CLI commands in this repo.
- Do not write SQLite directly with SQL, `sqlite3`, JDBC snippets, scripts, Python, Kotlin scratch code, or DB tools.
- Do not add network, API, server, port, Docker, Rust, Android, web UI, external provider automation, or OpenAI API behavior.
- Do not create provider job ids, remote URLs, submitted prompts, generated provider media, asset approval, user approval, canon changes, registry entries, release tags, or production locks.
- Treat `ComicVideoProductionPackageDraft` as draft-only and review-required.
- Treat `ComicVideoProductionPackageValidationReport` as a local validation gate only, not approval.
- If validation fails, edit only the upstream local JSON artifact that the finding identifies, then rerun the official CLI.

## Required Inputs

Identify these before running commands:

- Repository root.
- Source type:
  - existing PASS `ImageFacingDeliveryProof` plus edited `ComicVideoVoiceTimelineFill`; or
  - public setting pack scene plus edited voice timeline fill.
- Output directory for generated reports and local preview media.
- Stable ids for `build`, `packet`, `package`, and `validation` when the user wants reproducible outputs.

If the edited voice timeline fill is missing, create a template with `create-comic-video-voice-timeline-fill-template` and ask the author/Codex operator to fill spoken lines before validation.

## Workflow

Run only the steps needed for the current task.

### 1. Inspect Current Evidence

Use read-only checks first:

```powershell
git status --short --branch
rg -n "ComicVideoProductionPackage|create-comic-video-production-package|validate-comic-video-production-package" PLANS.md docs app samples .agents
```

If using existing artifacts, inspect their verdicts and ids before generating new files.

### 2. Build Image-Facing Proof From Setting Data

Use this only when starting from a public setting pack scene:

```powershell
.\gradlew.bat :app:run --args="build-setting-image-facing-proof-preview --setting-pack <settingPackJson> --scene <sceneId> --output-dir <proofOutputDir> --build <buildId> --script-packet <scriptPacketId> --storyboard-brief <briefId> --storyboard-draft <draftId> --storyboard-review <reviewId> --human-review-preflight <preflightId> --story-movement-audit <auditId> --reviewable-storyboard <reviewableStoryboardId> --proof <proofId>"
```

Required result:

- `verdict=PASS`;
- `nextAllowedDownstreamStep=image_facing_delivery_proof_ready`;
- no copied non-public source material.

### 3. Build Local Preview MP4

Use this when the user needs a playable local preview:

```powershell
.\gradlew.bat :app:run --args="build-comic-video-local-preview --image-facing-delivery-proof <proofJson> --voice-timeline-fill <voiceFillJson> --output-dir <previewOutputDir> --out-video <previewMp4> --build <buildId> --packet <packetId> --validation <validationId> --manifest <manifestId> --render <renderId> --panel-duration-ms 3000 --report-out <previewReportJson>"
```

Or, when starting from setting data:

```powershell
.\gradlew.bat :app:run --args="build-setting-comic-video-local-preview --setting-pack <settingPackJson> --scene <sceneId> --voice-timeline-fill <voiceFillJson> --output-dir <previewOutputDir> --out-video <previewMp4> --build <buildId> --packet <packetId> --panel-duration-ms 3000"
```

Required result:

- PASS build report;
- local preview MP4 only;
- no provider media, canon, registry, approval, release, or production lock.

### 4. Create Production Package Draft

Use this after a PASS image-facing proof and an edited voice timeline fill exist:

```powershell
.\gradlew.bat :app:run --args="create-comic-video-production-package-draft --image-facing-delivery-proof <proofJson> --voice-timeline-fill <voiceFillJson> --package <packageId> --packet <packetId> --panel-duration-ms 3000 --out <packageDraftJson>"
```

Required result:

- `verdict=PASS`;
- `storyboardRows` greater than 0;
- `shotPrompts` greater than 0;
- `assetPromptCards` greater than 0;
- `voiceStyleCards` greater than 0;
- `downstreamBoundary=comic_video_production_package_draft_only`;
- `nextAllowedDownstreamStep=comic_video_production_package_review`.

### 5. Validate Production Package

Always validate the draft before reporting it as ready for review:

```powershell
.\gradlew.bat :app:run --args="validate-comic-video-production-package --production-package-draft <packageDraftJson> --validation <validationId> --out <packageValidationJson>"
```

Required result:

- `verdict=PASS`;
- `crossReferenceMismatches=0`;
- `promptSafetyFindings=0`;
- `remoteOrProviderReferences=0`;
- `forbiddenClaims=0`;
- `forbiddenStatuses=0`;
- `authorOnlyLeaks=0`;
- `nextAllowedDownstreamStep=comic_video_production_package_validated_for_review`.

### 6. Verify Locally

Use focused tests after changing code, models, samples, or CLI behavior:

```powershell
$env:ORG_GRADLE_PROJECT_kotlin_daemon_jvmargs='-Xmx1536m'
.\gradlew.bat test --no-daemon --console=plain --tests "com.localnovel.engine.ComicVideo*"
```

Use full tests before declaring a code-bearing slice complete:

```powershell
$env:ORG_GRADLE_PROJECT_kotlin_daemon_jvmargs='-Xmx1536m'
.\gradlew.bat test --no-daemon --console=plain
```

The one-shot Kotlin daemon memory environment variable is local verification support only. Do not write it into project config unless the user asks for an environment tuning slice.

## Report

Report:

- files generated or updated;
- proof/package/validation ids;
- PASS/FAIL verdicts;
- storyboard row, shot prompt, asset card, voice style card, and duration counts;
- cross-reference, prompt safety, provider/reference, forbidden claim/status, and author-only leak counts;
- local preview MP4 path if generated;
- whether `PLANS.md` stayed aligned;
- confirmation that no direct SQLite writes, provider/API calls, approval, canon, registry, release, or production lock occurred.
