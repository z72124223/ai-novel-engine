# Control-Doc Staging Candidate Dry Run

Status: `READ_ONLY_CONTROL_DOC_STAGING_DRY_RUN_V1`

Date: 2026-06-27

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

## Purpose

This report dry-runs Option A from `docs/FIRST_STAGING_CANDIDATE_DECISION_BRIEF.md`: `control docs only`.

It lists the exact candidate files, current `git status` surface, excluded dirty worktree areas, and checks required before any future staging decision. This is not staging approval, not actual staging, not commit approval, and not release approval.

## Boundary

- No `git add`, commit, push, branch change, cleanup, delete, move, or revert was performed.
- No Kotlin source, test, schema, runtime, or CLI behavior was changed.
- No `--db`, SQLite write, schema migration, server, port, external AI/API, QuarterFull/browser, provider job, remote download, image/video/audio generation, asset approval, approved reference, registered reference, canon, user approval, production lock, or runtime scene creation is part of this dry run.
- The report only prepares evidence for `03` read-only review and a later explicit `01` decision.

## Inventory Command And Counts

Inventory command:

```powershell
git status --short --untracked-files=all
```

Pre-report result, before adding this file:

| Category | Count |
| --- | ---: |
| Tracked modified | 7 |
| Untracked total | 122 |
| Untracked services | 48 |
| Untracked tests | 49 |
| Untracked docs | 16 |
| Untracked samples | 7 |
| Untracked memory entries | 2 |
| Untracked other | 0 |
| Classified untracked sum | 122 |

Verified post-report result after adding this file:

| Category | Count |
| --- | ---: |
| Tracked modified | 7 |
| Untracked total | 123 |
| Untracked services | 48 |
| Untracked tests | 49 |
| Untracked docs | 17 |
| Untracked samples | 7 |
| Untracked memory entries | 2 |
| Untracked other | 0 |
| Classified untracked sum | 123 |

The post-report increase is this new docs entry only: `docs/CONTROL_DOC_STAGING_CANDIDATE_DRY_RUN.md`.

## Option A Candidate Exact Files

| File | Expected existence after this report | Current status surface | Why included |
| --- | --- | --- | --- |
| `PLANS.md` | yes | tracked modified (`M`) | Active control plan, drift log, and 03 handoff state. |
| `docs/PROJECT_COMPLETION_PROGRESS.md` | yes | untracked (`??`) | Official progress display and product/gate rubric. |
| `docs/PIPELINE_LIFECYCLE_MAP.md` | yes | untracked (`??`) | Lifecycle authority for gate/artifact boundaries and next stages. |
| `docs/RELEASE_HYGIENE_AND_REPRODUCIBILITY_REPORT.md` | yes | untracked (`??`) | Dirty tree and local reproducibility baseline. |
| `docs/RELEASE_GROUPING_PREVIEW.md` | yes | untracked (`??`) | First release grouping preview and group principles. |
| `docs/FOCUSED_REPRODUCIBILITY_BUNDLE.md` | yes | untracked (`??`) | Product-proof focused command evidence. |
| `docs/TRACKED_HANDOFF_MANIFEST.md` | yes | untracked (`??`) | File/path ownership map for later staging decisions. |
| `docs/ADJACENT_STORYBOARD_SUITE_REPRODUCIBILITY.md` | yes | untracked (`??`) | Adjacent storyboard suite command evidence. |
| `docs/FIRST_STAGING_CANDIDATE_DECISION_BRIEF.md` | yes | untracked (`??`) | Accepted decision brief recommending Option A as the lowest-risk first candidate. |
| `docs/CONTROL_DOC_STAGING_CANDIDATE_DRY_RUN.md` | yes | untracked (`??`) | This dry-run report. |

Option A is intentionally limited to these control docs. It excludes implementation services, tests, samples, memory files, and older planning-only docs.

## Excluded Dirty Surfaces

These dirty worktree areas are deliberately outside Option A:

- `app/src/main/kotlin/com/localnovel/engine/Main.kt`: tracked modified shared CLI wiring across many accepted JSON gates; not a control-doc-only staging candidate.
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`: tracked modified shared JSON model surface across many accepted gates; not a control-doc-only staging candidate.
- `docs/AUTHORING_QUALITY_GUIDELINES.md`, `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md`, `docs/HANDOFF.md`, and `docs/PROJECT_STRUCTURE_REPORT.md`: tracked modified historical/planning docs with separate ownership and review history.
- `app/src/main/kotlin/com/localnovel/engine/service/*`: 48 untracked service files; implementation groups require their own verification and shared `Main.kt` / `Models.kt` handling.
- `app/src/test/kotlin/com/localnovel/engine/*`: 49 untracked test files; should follow their matching service or product-proof group rather than a control-doc-only dry run.
- `samples/*`: 7 untracked sample files; data fixtures need separate review with their matching gates.
- `memory/*`: 2 untracked support files; local support memory should not be bundled into control docs without explicit `01` approval.
- Planning-only / historical docs such as `docs/BLUEPRINT_REDESIGN_DECISION_BRIEF.md`, `docs/CLEANUP_INVENTORY_REPORT.md`, `docs/COMIC_VIDEO_PRODUCTION_FEATURE_PLAN.md`, `docs/HUMANLIKE_PROSE_AND_DIALOGUE_DISTILLATION_PLAN.md`, `docs/NOVELIZATION_SUPERIOR_TO_QUARTERFULL_PLAN.md`, `docs/NOVEL_TO_SCRIPT_PACKET_BLUEPRINT.md`, `docs/PROJECT_DIRECTORY_AND_SUBPROJECT_REGISTER.md`, and `docs/QUARTERFULL_WORLD_ALIGNMENT.md`.

## Required Pre-Stage Checks

Before any future actual Option A staging, `01` should explicitly approve a separate staging step and rerun:

```powershell
git status --short --untracked-files=all
```

```powershell
git diff --check -- PLANS.md docs/PROJECT_COMPLETION_PROGRESS.md docs/PIPELINE_LIFECYCLE_MAP.md docs/RELEASE_HYGIENE_AND_REPRODUCIBILITY_REPORT.md docs/RELEASE_GROUPING_PREVIEW.md docs/FOCUSED_REPRODUCIBILITY_BUNDLE.md docs/TRACKED_HANDOFF_MANIFEST.md docs/ADJACENT_STORYBOARD_SUITE_REPRODUCIBILITY.md docs/FIRST_STAGING_CANDIDATE_DECISION_BRIEF.md docs/CONTROL_DOC_STAGING_CANDIDATE_DRY_RUN.md
```

```powershell
$legacy = 'C:\Users\f7212' + '\OneDrive\文件\AI 劇本'
Select-String -SimpleMatch $legacy -Path docs/PROJECT_COMPLETION_PROGRESS.md,docs/PIPELINE_LIFECYCLE_MAP.md,docs/RELEASE_HYGIENE_AND_REPRODUCIBILITY_REPORT.md,docs/RELEASE_GROUPING_PREVIEW.md,docs/FOCUSED_REPRODUCIBILITY_BUNDLE.md,docs/TRACKED_HANDOFF_MANIFEST.md,docs/ADJACENT_STORYBOARD_SUITE_REPRODUCIBILITY.md,docs/FIRST_STAGING_CANDIDATE_DECISION_BRIEF.md,docs/CONTROL_DOC_STAGING_CANDIDATE_DRY_RUN.md
```

`PLANS.md` may contain historical relocation/drift notes that mention old paths. Those should be reviewed separately as historical records, not interpreted as current canonical workspace instructions.

Gradle is not required for this dry-run slice because it changes docs/control only. If `01` expands the candidate beyond docs/control, the matching focused Gradle tests from `docs/FOCUSED_REPRODUCIBILITY_BUNDLE.md` and `docs/ADJACENT_STORYBOARD_SUITE_REPRODUCIBILITY.md` should be rerun.

## Dry-Run Verification Record

Commands run in the canonical workspace:

```powershell
git status --short --untracked-files=all
```

Result: tracked modified `7`, untracked total `123`, services `48`, tests `49`, docs `17`, samples `7`, memory entries `2`, other `0`, classified sum `123`.

```powershell
git diff --check -- PLANS.md docs\CONTROL_DOC_STAGING_CANDIDATE_DRY_RUN.md
```

Result: exit `0`; only the existing `PLANS.md` Windows line-ending warning was emitted.

```powershell
Select-String -Path docs\CONTROL_DOC_STAGING_CANDIDATE_DRY_RUN.md -Pattern '\s+$'
```

Result: no trailing whitespace matches.

Legacy full-path scan result: the new report is clean. `PLANS.md` still has pre-existing historical relocation notes in older drift sections; those are not current workspace instructions.

## Recommendation

Option A can be considered for explicit `01` staging approval only after this dry-run passes `03` read-only review. This dry-run itself does not stage files and does not authorize any future `git add`.

## Review Request Shape

For `03` review, verify:

- The report is docs/control only.
- The inventory command and counts are reproducible.
- The candidate exact-file list matches Option A and excludes implementation/runtime/test/sample/memory surfaces.
- The boundary language does not imply staging, commit, release approval, runtime behavior, DB/schema changes, provider work, media generation, canon, approval, or production lock.
