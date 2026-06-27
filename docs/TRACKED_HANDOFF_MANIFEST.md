# Tracked Handoff Manifest

Status: `READ_ONLY_HANDOFF_MANIFEST_V1`

Date: 2026-06-27

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

This manifest maps the current dirty worktree into reviewable handoff groups. It is documentation only. It is not cleanup apply, staging approval, commit approval, release approval, runtime work, SQLite work, provider work, media generation, asset approval, approved reference, registered reference, canon, user approval, production lock, or runtime scene creation.

## 1. Inventory Command And Counts

Read-only command:

```powershell
git status --short --untracked-files=all
```

Counting rule:

- Tracked modified paths are status lines not starting with `?? `.
- Untracked entries are status lines starting with `?? `.
- Untracked entries are classified by path prefix.
- Classification sum must equal untracked total.

Current result after adding this manifest:

| Class | Count |
| --- | ---: |
| Tracked modified paths | 7 |
| Untracked entries | 120 |
| Untracked service files | 48 |
| Untracked test files | 49 |
| Untracked docs entries | 14 |
| Untracked sample entries | 7 |
| Untracked memory entries | 2 |
| Untracked other entries | 0 |
| Classified untracked sum | 120 |

The focused reproducibility bundle counted 119 untracked entries before this manifest existed. This manifest adds one untracked docs entry, so the current inventory is expected to be 120 and remains fully classified with no unexplained delta.

## 2. Shared Tracked Dependencies

These tracked modified files are shared across multiple accepted slices. They must not be staged as if they belonged to one small group only.

| File | Why shared | Handoff blocker |
| --- | --- | --- |
| `PLANS.md` | Live control plan and drift log for many reviewed slices. | Stage only with an approved control-doc handoff. |
| `app/src/main/kotlin/com/localnovel/engine/Main.kt` | Adds CLI wiring for novelization, script packet, storyboard, asset candidate, image candidate, candidate reference, and quality-support gates. | Stage only with the matching model and service/test groups selected by `01`. |
| `app/src/main/kotlin/com/localnovel/engine/model/Models.kt` | Adds JSON model/report classes for the same gate families. | Stage only with the matching service/test groups selected by `01`. |
| `docs/AUTHORING_QUALITY_GUIDELINES.md` | Planning / authoring guidance touched outside the current release-hardening reports. | Needs ownership confirmation before product-gate staging. |
| `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md` | Planning register touched across blueprint and directory slices. | Needs ownership confirmation before product-gate staging. |
| `docs/HANDOFF.md` | Project handoff notes touched by prior directory / support work. | Needs ownership confirmation before product-gate staging. |
| `docs/PROJECT_STRUCTURE_REPORT.md` | Project structure report touched by prior directory / support work. | Needs ownership confirmation before product-gate staging. |

Read-only diff scope evidence:

- `Main.kt`: tracked modified, 1785 inserted lines in current diff, including CLI entries for accepted local JSON gates.
- `Models.kt`: tracked modified, 3296 inserted lines in current diff, including report/data models for accepted local JSON gates.

## 3. Exact Group Manifests

Every group below remains blocked from staging until `01` explicitly approves a staging or handoff decision. Path patterns are used where the current worktree already groups files by accepted gate family.

### Group A: Control Docs / Progress / Lifecycle / Release Reports

Purpose:

- Preserve current governance, progress display, lifecycle map, release hygiene evidence, release grouping, focused reproducibility, and this handoff manifest.

Candidate files / patterns:

- `PLANS.md`
- `docs/PROJECT_COMPLETION_PROGRESS.md`
- `docs/PIPELINE_LIFECYCLE_MAP.md`
- `docs/RELEASE_HYGIENE_AND_REPRODUCIBILITY_REPORT.md`
- `docs/RELEASE_GROUPING_PREVIEW.md`
- `docs/FOCUSED_REPRODUCIBILITY_BUNDLE.md`
- `docs/TRACKED_HANDOFF_MANIFEST.md`

Required shared tracked files:

- `PLANS.md`

Minimum verification:

```powershell
git diff --check -- PLANS.md docs/PROJECT_COMPLETION_PROGRESS.md docs/PIPELINE_LIFECYCLE_MAP.md docs/RELEASE_HYGIENE_AND_REPRODUCIBILITY_REPORT.md docs/RELEASE_GROUPING_PREVIEW.md docs/FOCUSED_REPRODUCIBILITY_BUNDLE.md docs/TRACKED_HANDOFF_MANIFEST.md
```

Review basis:

- `pipeline lifecycle map + progress rubric repair`: PASS by `03`.
- `minimal release hygiene and local reproducibility report` REVISE P1 repair: PASS by `03`.
- `minimal release grouping preview`: PASS by `03`.
- `minimal focused reproducibility bundle`: PASS by `03`.
- This manifest still requires `03` review.

Staging blocker:

- Requires `01` approval because these docs govern progress display and release grouping.

### Group B: Novelization Gates / Writer Quality Path

Purpose:

- Preserve the local JSON novelization chain from Character Nuwa and preflight through accepted draft packet.

Candidate files / patterns:

- `app/src/main/kotlin/com/localnovel/engine/service/Novelization*Service.kt`
- `app/src/test/kotlin/com/localnovel/engine/Novelization*Test.kt`
- `samples/novelization_*`

Required shared tracked files:

- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`

Minimum verification:

```powershell
./gradlew.bat test --tests "*Novelization*Test" --no-daemon --console=plain
```

Review basis:

- Prior `03` PASS slices covered novelization preflight, writer brief, draft intake, prose quality audit, revision brief, revised draft intake, accepted draft packet, and script packet preflight boundary.

Staging blocker:

- Needs `01` approval that the shared `Main.kt` / `Models.kt` changes are included with this group or intentionally deferred.

### Group C: Script Packet Gates

Purpose:

- Preserve accepted draft to script packet source binding, structure bridge, and accepted script handoff.

Candidate files / patterns:

- `app/src/main/kotlin/com/localnovel/engine/service/AcceptedScriptPacketHandoffPacketService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ScriptPacketDraft*Service.kt`
- `app/src/test/kotlin/com/localnovel/engine/AcceptedScriptPacketHandoffPacketTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ScriptPacketDraft*Test.kt`
- `samples/script_packet_draft_sample.json`

Required shared tracked files:

- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`

Minimum verification:

```powershell
./gradlew.bat test --tests "*ScriptPacketDraft*Test" --tests "*AcceptedScriptPacketHandoffPacketTest" --no-daemon --console=plain
```

Review basis:

- Prior `03` PASS slices covered accepted script packet handoff, script packet source binding, and structure bridge.

Staging blocker:

- Do not stage service/test files without the matching shared model and CLI wiring.

### Group D: Storyboard Gates And Product Proof

Purpose:

- Preserve storyboard planning, text-only storyboard packet draft/review/human review preflight, story movement audit, E2E fixture, and the named reviewable storyboard packet artifact.

Candidate files / patterns:

- `app/src/main/kotlin/com/localnovel/engine/service/Storyboard*Service.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ReviewableStoryboardPacketService.kt`
- `app/src/test/kotlin/com/localnovel/engine/Storyboard*Test.kt`
- `app/src/test/kotlin/com/localnovel/engine/PipelineHappyPathFixtureTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ReviewableStoryboardPacketTest.kt`

Required shared tracked files:

- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`

Minimum verification:

```powershell
./gradlew.bat test --tests "*ReviewableStoryboardPacketTest" --tests "*StoryboardStoryMovementAuditTest" --tests "*PipelineHappyPathFixtureTest" --no-daemon --console=plain
```

Review basis:

- `pipeline E2E happy-path fixture v1`: PASS by `03`.
- `minimal storyboard story movement audit gate` REVISE P1 repair: PASS by `03`.
- `minimal reviewable storyboard packet product artifact`: PASS by `03`.
- `minimal focused reproducibility bundle`: PASS by `03`.

Staging blocker:

- Product-facing group. Requires explicit `01` approval before staging and should not be mixed with image/provider/candidate-reference groups unless `01` asks for a broad product-proof package.

### Group E: Asset Candidate Gates

Purpose:

- Preserve candidate-only asset requirement, draft, validation, review preflight, and human review decision gates.

Candidate files / patterns:

- `app/src/main/kotlin/com/localnovel/engine/service/AssetCandidate*Service.kt`
- `app/src/test/kotlin/com/localnovel/engine/AssetCandidate*Test.kt`

Required shared tracked files:

- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`

Minimum verification:

```powershell
./gradlew.bat test --tests "*AssetCandidate*Test" --no-daemon --console=plain
```

Review basis:

- Prior `03` PASS slices covered asset candidate preflight, brief, draft, draft validation, review preflight, and human review decision intake.

Staging blocker:

- Needs explicit note that this group remains candidate-only and is not asset approval, final asset selection, or production lock.

### Group F: Image Candidate Gates

Purpose:

- Preserve prompt preparation, prompt validation, provider handoff preparation, external result intake, result review preflight, human decision intake, candidate reference packet, and validation gates.

Candidate files / patterns:

- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidate*Service.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidate*Test.kt`

Required shared tracked files:

- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`

Minimum verification:

```powershell
./gradlew.bat test --tests "*ImageCandidate*Test" --no-daemon --console=plain
```

Review basis:

- Prior `03` PASS / REVISE repairs covered prompt `assetType` scan surfaces, manual provider handoff source continuity, external result URL/URI rejection, and item-level source binding.

Staging blocker:

- Needs explicit `01` approval because this group mentions manual provider handoff and external results. It remains local JSON only and must not imply provider job execution or generated media registration.

### Group G: Candidate Reference / Registry Boundary Gates

Purpose:

- Preserve candidate reference packet validation, human acceptance, registry preflight, registry packet draft, registry packet validation, acceptance decision, acceptance packet, and acceptance packet validation.

Candidate files / patterns:

- `app/src/main/kotlin/com/localnovel/engine/service/CandidateReference*Service.kt`
- `app/src/test/kotlin/com/localnovel/engine/CandidateReference*Test.kt`

Required shared tracked files:

- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`

Minimum verification:

```powershell
./gradlew.bat test --tests "*CandidateReference*Test" --no-daemon --console=plain
```

Review basis:

- Prior `03` PASS slices covered candidate reference registry preparation, registry validation and acceptance boundary, and candidate reference acceptance packet validation.

Staging blocker:

- Requires `01` approval that all registry wording remains candidate-only. This group must not be treated as formal registry write, approved reference, registered reference, canon, or production lock.

### Group H: Quality Support Gates

Purpose:

- Preserve supporting continuity and Character Nuwa gates used by prose and story-quality workflows.

Candidate files / patterns:

- `app/src/main/kotlin/com/localnovel/engine/service/ChapterContinuityAuditService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/CharacterNuwaGateValidationService.kt`
- `app/src/test/kotlin/com/localnovel/engine/ChapterContinuityAuditTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/CharacterNuwaGateValidationTest.kt`
- `samples/chapter_continuity_anchor_sample.json`
- `samples/character_nuwa_gate_sample.json`

Required shared tracked files:

- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`

Minimum verification:

```powershell
./gradlew.bat test --tests "*ChapterContinuityAuditTest" --tests "*CharacterNuwaGateValidationTest" --no-daemon --console=plain
```

Review basis:

- Prior `03` PASS slices covered Character Nuwa gate and continuity quality support.

Staging blocker:

- Needs `01` confirmation that samples are intended handoff materials.

### Group I: Samples And Memory Support

Purpose:

- Preserve local JSON/Markdown fixtures and local support memory files separately from source/test gate implementation.

Candidate files / patterns:

- `samples/chapter_continuity_anchor_sample.json`
- `samples/character_nuwa_gate_sample.json`
- `samples/novelization_draft_intake_sample.md`
- `samples/novelization_revision_continuity_anchors_sample.json`
- `samples/novelization_revision_handling_declaration_sample.json`
- `samples/novelization_writer_brief_sample.json`
- `samples/script_packet_draft_sample.json`
- `memory/project_directory_memory.json`
- `memory/quarterfull_world_memory_capsule.json`

Required shared tracked files:

- None for code compilation, but docs may need cross-reference review.

Minimum verification:

```powershell
git diff --check -- samples/chapter_continuity_anchor_sample.json samples/character_nuwa_gate_sample.json samples/novelization_draft_intake_sample.md samples/novelization_revision_continuity_anchors_sample.json samples/novelization_revision_handling_declaration_sample.json samples/novelization_writer_brief_sample.json samples/script_packet_draft_sample.json memory/project_directory_memory.json memory/quarterfull_world_memory_capsule.json
```

Review basis:

- Mixed: samples support reviewed gates; memory files support local project continuity and QuarterFull alignment history.

Staging blocker:

- Needs human confirmation whether `memory/` belongs in the release handoff or remains local-only support.

### Group J: Planning-Only And Historical Docs

Purpose:

- Keep blueprint, feature, cleanup, directory, and QuarterFull alignment documents separate from product-gate source/test groups.

Candidate files / patterns:

- `docs/AUTHORING_QUALITY_GUIDELINES.md`
- `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md`
- `docs/BLUEPRINT_REDESIGN_DECISION_BRIEF.md`
- `docs/CLEANUP_INVENTORY_REPORT.md`
- `docs/COMIC_VIDEO_PRODUCTION_FEATURE_PLAN.md`
- `docs/HANDOFF.md`
- `docs/HUMANLIKE_PROSE_AND_DIALOGUE_DISTILLATION_PLAN.md`
- `docs/NOVELIZATION_SUPERIOR_TO_QUARTERFULL_PLAN.md`
- `docs/NOVEL_TO_SCRIPT_PACKET_BLUEPRINT.md`
- `docs/PROJECT_DIRECTORY_AND_SUBPROJECT_REGISTER.md`
- `docs/PROJECT_STRUCTURE_REPORT.md`
- `docs/QUARTERFULL_WORLD_ALIGNMENT.md`

Required shared tracked files:

- `PLANS.md` only if the handoff includes historical planning context.

Minimum verification:

```powershell
git diff --check -- docs/AUTHORING_QUALITY_GUIDELINES.md docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md docs/BLUEPRINT_REDESIGN_DECISION_BRIEF.md docs/CLEANUP_INVENTORY_REPORT.md docs/COMIC_VIDEO_PRODUCTION_FEATURE_PLAN.md docs/HANDOFF.md docs/HUMANLIKE_PROSE_AND_DIALOGUE_DISTILLATION_PLAN.md docs/NOVELIZATION_SUPERIOR_TO_QUARTERFULL_PLAN.md docs/NOVEL_TO_SCRIPT_PACKET_BLUEPRINT.md docs/PROJECT_DIRECTORY_AND_SUBPROJECT_REGISTER.md docs/PROJECT_STRUCTURE_REPORT.md docs/QUARTERFULL_WORLD_ALIGNMENT.md
```

Review basis:

- Mixed: some are historical or planning-only. They should not be represented as current product behavior without separate review.

Staging blocker:

- Do not combine with product implementation groups without `01` approval and, if needed, expert-model review.

## 4. Cross-Group Blocking Rules

- No group is approved for staging, commit, push, cleanup, move, delete, or revert by this manifest.
- Any implementation group needs `Main.kt` and `Models.kt` included or explicitly deferred by `01`.
- Product-facing storyboard group should be verified before image/provider/candidate-reference groups if `01` wants a narrow release candidate.
- Planning-only docs and memory support need ownership decisions before public or release handoff.
- Full release verification has not run.

## 5. Release Next-Step Recommendation

Recommended next slice:

```text
adjacent storyboard suite reproducibility
```

Purpose:

- Expand from the minimum focused product-proof tests to broader `*Storyboard*Test` coverage before any staging decision.

Alternative:

```text
first staging candidate decision brief
```

Purpose:

- Ask `01` to choose exactly one initial staging candidate group and approve the needed shared tracked dependencies. This should still be a decision brief, not staging itself.

## 6. Boundary Statement

This manifest is not:

- cleanup apply
- staging approval
- commit approval
- release approval
- formal release
- runtime / SQLite work
- provider / image / video / audio work
- asset approval
- approved reference
- registered reference
- canon
- user approval
- production lock
- runtime scene creation

This manifest only records current dirty-tree handoff groups for later `01` decisions.

## 7. Verification Record

Commands for this slice:

```powershell
git status --short --untracked-files=all
git diff --stat -- app/src/main/kotlin/com/localnovel/engine/Main.kt app/src/main/kotlin/com/localnovel/engine/model/Models.kt
git diff --check -- PLANS.md docs/TRACKED_HANDOFF_MANIFEST.md
```

Additional checks:

- New report trailing-whitespace scan.
- Old workspace full-path scan on this report and touched control docs.

Results:

- Inventory command result after adding this manifest: tracked modified `7`; untracked total `120`; services `48`; tests `49`; docs `14`; samples `7`; memory entries `2`; other `0`; classified sum `120`.
- Shared tracked diff scope: `Main.kt` has 1785 inserted lines; `Models.kt` has 3296 inserted lines.
- Gradle was not rerun because this slice is docs/control only and changed no Kotlin/source/test/schema/runtime/CLI behavior.
