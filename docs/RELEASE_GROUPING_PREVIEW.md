# Release Grouping Preview

Status: `READ_ONLY_GROUPING_PREVIEW_V1`

Date: 2026-06-27

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

This document is a read-only release grouping preview. It does not delete, move, stage, commit, push, revert, clean, package, approve, canonize, lock, register, generate media, call providers, write SQLite, or change runtime behavior.

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

Current result:

| Class | Count |
| --- | ---: |
| Tracked modified paths | 7 |
| Untracked entries | 118 |
| Untracked service files | 48 |
| Untracked test files | 49 |
| Untracked docs entries | 12 |
| Untracked sample entries | 7 |
| Untracked memory entries | 2 |
| Untracked other entries | 0 |
| Classified untracked sum | 118 |

Current service/test grouping counts:

| Product area | Service files | Test files |
| --- | ---: | ---: |
| Novelization gates | 8 | 8 |
| Script packet gates | 4 | 4 |
| Storyboard gates and product proof | 8 | 9 |
| Asset candidate gates | 6 | 6 |
| Image candidate gates | 13 | 13 |
| Candidate reference / registry boundary gates | 7 | 7 |
| Quality support gates | 2 | 2 |
| Total | 48 | 49 |

The accepted release hygiene report counted 117 untracked entries before this new preview report existed. This slice adds one untracked docs entry, so the current inventory is 118 and remains fully classified with no unexplained delta.

## 2. Grouping Principles

- Each group must be independently reviewable and have a focused verification command.
- Do not mix product-gate implementation with planning-only or historical documentation unless `01` explicitly approves a broader handoff package.
- Do not stage or commit any group from this preview without a later explicit `01` instruction.
- Do not hide dirty-tree risk by grouping unrelated files together.
- Prefer chronological product slices when possible, but keep verification cost and reviewer load manageable.
- Preserve candidate-only, non-canon, non-production semantics in every group.

## 3. Proposed Groups

### Group A: Control Docs / Progress / Lifecycle / Release Reports

Purpose:

- Preserve current project governance, progress rubric, lifecycle map, release hygiene evidence, and release grouping preview.

Candidate files / patterns:

- `PLANS.md`
- `docs/PROJECT_COMPLETION_PROGRESS.md`
- `docs/PIPELINE_LIFECYCLE_MAP.md`
- `docs/RELEASE_HYGIENE_AND_REPRODUCIBILITY_REPORT.md`
- `docs/RELEASE_GROUPING_PREVIEW.md`

Review status basis:

- `pipeline lifecycle map + progress rubric repair`: PASS by `03`.
- `minimal release hygiene and local reproducibility report` REVISE P1 repair: PASS by `03`.
- This grouping preview still requires `03` review.

Minimum verification:

```powershell
git diff --check -- PLANS.md docs/PROJECT_COMPLETION_PROGRESS.md docs/PIPELINE_LIFECYCLE_MAP.md docs/RELEASE_HYGIENE_AND_REPRODUCIBILITY_REPORT.md docs/RELEASE_GROUPING_PREVIEW.md
```

Release risk:

- High if stale progress numbers or stale inventory counts reappear.
- Medium if historical OneDrive relocation notes are confused with current canonical workspace.

Staging blocker:

- Requires `01` approval after `03` accepts this report.

### Group B: Novelization Gates / Writer Quality Path

Purpose:

- Preserve the local JSON novelization chain from preflight through accepted draft packet and prose/revision controls.

Candidate files / patterns:

- `app/src/main/kotlin/com/localnovel/engine/service/Novelization*Service.kt`
- `app/src/test/kotlin/com/localnovel/engine/Novelization*Test.kt`
- `samples/novelization_*`

Review status basis:

- Prior `03` PASS slices covered novelization preflight, writer brief, draft intake, prose quality audit, revision brief, revised draft intake, and accepted draft packet.

Minimum verification:

```powershell
./gradlew.bat test --tests "*Novelization*Test" --no-daemon --console=plain
```

Release risk:

- Medium: these gates are source-bound and read-only, but product-level chapter E2E proof is still separate.

Staging blocker:

- Needs a manifest that lists the exact model / Main.kt changes required alongside the untracked service/test files.

### Group C: Script Packet Gates

Purpose:

- Preserve accepted draft to script packet preflight, source binding, structure bridge, and accepted script handoff.

Candidate files / patterns:

- `app/src/main/kotlin/com/localnovel/engine/service/AcceptedScriptPacketHandoffPacketService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ScriptPacketDraft*Service.kt`
- `app/src/test/kotlin/com/localnovel/engine/AcceptedScriptPacketHandoffPacketTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ScriptPacketDraft*Test.kt`
- `samples/script_packet_draft_sample.json`

Review status basis:

- Prior `03` PASS slices covered accepted draft to script packet preflight, script packet source binding, structure bridge, and accepted script packet handoff.

Minimum verification:

```powershell
./gradlew.bat test --tests "*ScriptPacketDraft*Test" --tests "*AcceptedScriptPacketHandoffPacketTest" --no-daemon --console=plain
```

Release risk:

- Medium: source binding is critical because downstream storyboard planning depends on accepted script packet identity and hash.

Staging blocker:

- Needs grouping with shared `Models.kt` and `Main.kt` changes; do not stage services alone.

### Group D: Storyboard Gates And Product Proof

Purpose:

- Preserve storyboard planning, text-only storyboard packet draft/review/human review preflight, story movement audit, E2E happy-path fixture, and reviewable storyboard packet product artifact.

Candidate files / patterns:

- `app/src/main/kotlin/com/localnovel/engine/service/Storyboard*Service.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ReviewableStoryboardPacketService.kt`
- `app/src/test/kotlin/com/localnovel/engine/Storyboard*Test.kt`
- `app/src/test/kotlin/com/localnovel/engine/PipelineHappyPathFixtureTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ReviewableStoryboardPacketTest.kt`

Review status basis:

- `pipeline E2E happy-path fixture v1`: PASS by `03`.
- `minimal storyboard story movement audit gate` REVISE P1 repair: PASS by `03`.
- `minimal reviewable storyboard packet product artifact`: PASS by `03`.

Minimum verification:

```powershell
./gradlew.bat test --tests "*Storyboard*Test" --tests "*PipelineHappyPathFixtureTest" --tests "*ReviewableStoryboardPacketTest" --no-daemon --console=plain
```

Release risk:

- High: this is the current named local JSON product artifact path. It must not be mixed with image/provider/canon/approval claims.

Staging blocker:

- Requires explicit `01` approval because it is product-facing and depends on shared model/CLI wiring.

### Group E: Asset Candidate Gates

Purpose:

- Preserve candidate-only asset requirement, draft, validation, review preflight, and human review decision intake gates.

Candidate files / patterns:

- `app/src/main/kotlin/com/localnovel/engine/service/AssetCandidate*Service.kt`
- `app/src/test/kotlin/com/localnovel/engine/AssetCandidate*Test.kt`

Review status basis:

- Prior `03` PASS slices covered asset candidate preflight, brief, draft, validation, review preflight, and human review decision intake.

Minimum verification:

```powershell
./gradlew.bat test --tests "*AssetCandidate*Test" --no-daemon --console=plain
```

Release risk:

- Medium: candidate-only wording must remain distinct from approval, lock, final, or production-ready status.

Staging blocker:

- Should not be staged before storyboard product proof group unless `01` chooses a chronological gate package.

### Group F: Image Candidate Gates

Purpose:

- Preserve image-candidate prompt preparation, prompt validation, manual provider handoff preparation, external result intake, result review preflight, human review decision intake, reference packet, and validation gates.

Candidate files / patterns:

- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidate*Service.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidate*Test.kt`

Review status basis:

- Prior `03` PASS / REVISE repairs covered prompt `assetType` scan surfaces, provider handoff source continuity, external result URL/URI rejection, and item-level source binding.

Minimum verification:

```powershell
./gradlew.bat test --tests "*ImageCandidate*Test" --no-daemon --console=plain
```

Release risk:

- High: these gates mention provider handoff and external results, but must remain local JSON only and must not imply provider job execution or generated media registration.

Staging blocker:

- Needs a clear review note that all provider/image references in tests are negative boundary assertions or manual-result metadata, not generated repo output.

### Group G: Candidate Reference / Registry Boundary Gates

Purpose:

- Preserve candidate reference packet validation, human acceptance, registry preflight, registry packet draft, registry packet validation, acceptance decision, acceptance packet, and acceptance packet validation.

Candidate files / patterns:

- `app/src/main/kotlin/com/localnovel/engine/service/CandidateReference*Service.kt`
- `app/src/test/kotlin/com/localnovel/engine/CandidateReference*Test.kt`

Review status basis:

- Prior `03` PASS slices covered candidate reference acceptance packet validation boundary closure and registry preparation / acceptance boundary gates.

Minimum verification:

```powershell
./gradlew.bat test --tests "*CandidateReference*Test" --no-daemon --console=plain
```

Release risk:

- High: registry wording can be confused with formal registry write, approved reference, registered reference, canon, or production lock. This group remains candidate-only.

Staging blocker:

- Requires `01` approval that candidate registry preparation artifacts remain non-runtime and non-formal.

### Group H: Quality Support Gates / Samples / Memory Support

Purpose:

- Preserve supporting quality gates and local memory/sample inputs that inform the pipeline without changing runtime state.

Candidate files / patterns:

- `app/src/main/kotlin/com/localnovel/engine/service/ChapterContinuityAuditService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/CharacterNuwaGateValidationService.kt`
- `app/src/test/kotlin/com/localnovel/engine/ChapterContinuityAuditTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/CharacterNuwaGateValidationTest.kt`
- `samples/chapter_continuity_anchor_sample.json`
- `samples/character_nuwa_gate_sample.json`
- `memory/project_directory_memory.json`
- `memory/quarterfull_world_memory_capsule.json`

Review status basis:

- Prior work established character Nuwa gate / continuity audit support and local memory alignment artifacts.

Minimum verification:

```powershell
./gradlew.bat test --tests "*ChapterContinuityAuditTest" --tests "*CharacterNuwaGateValidationTest" --no-daemon --console=plain
```

Release risk:

- Medium: memory files and samples may contain project-specific context and should be reviewed for intended publication before staging.

Staging blocker:

- Needs human confirmation about whether `memory/` belongs in the repo release package or should remain local-only.

### Group I: Planning-Only And Historical Docs

Purpose:

- Preserve blueprint, feature, cleanup, directory, and QuarterFull alignment documents separately from implementation gates.

Candidate files / patterns:

- `docs/BLUEPRINT_REDESIGN_DECISION_BRIEF.md`
- `docs/CLEANUP_INVENTORY_REPORT.md`
- `docs/COMIC_VIDEO_PRODUCTION_FEATURE_PLAN.md`
- `docs/HUMANLIKE_PROSE_AND_DIALOGUE_DISTILLATION_PLAN.md`
- `docs/NOVELIZATION_SUPERIOR_TO_QUARTERFULL_PLAN.md`
- `docs/NOVEL_TO_SCRIPT_PACKET_BLUEPRINT.md`
- `docs/PROJECT_DIRECTORY_AND_SUBPROJECT_REGISTER.md`
- `docs/QUARTERFULL_WORLD_ALIGNMENT.md`
- modified planning docs such as `docs/AUTHORING_QUALITY_GUIDELINES.md`, `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md`, `docs/HANDOFF.md`, and `docs/PROJECT_STRUCTURE_REPORT.md`

Review status basis:

- Mixed: some docs are reviewed control artifacts; others are planning-only or historical cleanup inventory.

Minimum verification:

```powershell
git diff --check -- docs/BLUEPRINT_REDESIGN_DECISION_BRIEF.md docs/CLEANUP_INVENTORY_REPORT.md docs/COMIC_VIDEO_PRODUCTION_FEATURE_PLAN.md docs/HUMANLIKE_PROSE_AND_DIALOGUE_DISTILLATION_PLAN.md docs/NOVELIZATION_SUPERIOR_TO_QUARTERFULL_PLAN.md docs/NOVEL_TO_SCRIPT_PACKET_BLUEPRINT.md docs/PROJECT_DIRECTORY_AND_SUBPROJECT_REGISTER.md docs/QUARTERFULL_WORLD_ALIGNMENT.md
```

Release risk:

- High if historical or planning-only docs are mistaken for current product state, canon, runtime behavior, or release approval.

Staging blocker:

- Do not bundle this group with product-gate implementation groups without `01` approval and, if needed, expert-model review.

## 4. Blockers

Do not stage or commit any group yet because:

- `01` has not approved a release grouping manifest.
- `Main.kt` and `Models.kt` are shared tracked modified files across many groups; staging any service/test group without the correct shared changes would be non-reproducible.
- Planning-only and historical docs have mixed review status and should not be blended with product implementation groups.
- `memory/` may be local support context rather than release material.
- Full release verification has not run after all accepted artifacts.

## 5. Next-Step Recommendation

Recommended next slice:

```text
focused reproducibility bundle
```

Purpose:

- Rerun focused tests by group, starting with Group D because it is the current named product artifact path.
- Produce a manifest that maps each group to exact files and test commands.
- Keep the work read-only unless `01` explicitly approves staging / commit preparation.

Alternative if `01` wants packaging first:

```text
tracked handoff manifest
```

Purpose:

- Write the exact file list required for a reproducible handoff, without staging or committing it.

## 6. Boundary Statement

This preview is not:

- cleanup apply
- staging approval
- commit approval
- release candidate approval
- formal product release
- runtime / SQLite work
- provider / image / video / audio work
- asset approval
- approved reference
- registered reference
- canon
- user approval
- production lock
- runtime scene creation

This preview only maps current dirty-tree artifacts into reviewable release-group candidates for later `01` decisions.

## 7. Verification Record

Commands run for this slice:

```powershell
git status --short --untracked-files=all
git diff --check -- PLANS.md docs/RELEASE_GROUPING_PREVIEW.md
```

Additional checks:

- New report trailing-whitespace scan.
- Old workspace full-path scan on this report and touched control docs.

Results:

- Inventory command result: tracked modified `7`; untracked total `118`; services `48`; tests `49`; docs `12`; samples `7`; memory entries `2`; other `0`; classified sum `118`.
- `git diff --check` exited 0 with only the existing Windows line-ending warning for `PLANS.md`.
- New report trailing-whitespace scan returned no matches.
- Old workspace full-path scan returned no matches in this report. It still returns pre-existing historical relocation notes in `PLANS.md`.
- No Gradle command was run because this slice is docs/control only and changed no Kotlin/source/test/schema/runtime/CLI behavior.
