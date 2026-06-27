# First Staging Candidate Decision Brief

Status: `READ_ONLY_STAGING_DECISION_BRIEF_V1`

Date: 2026-06-27

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

This brief helps `01` choose a first staging candidate. It is not staging approval, commit approval, release approval, cleanup apply, runtime work, SQLite work, provider work, media generation, asset approval, approved reference, registered reference, canon, user approval, production lock, or runtime scene creation.

No `git add`, commit, push, branch change, cleanup, delete, move, or revert was performed for this brief.

## 1. Canonical Workspace And Command Discipline

All evidence for this brief was gathered from:

```text
C:\Users\f7212\Projects\AI 劇本
```

Required command discipline for any future staging step:

- Use the canonical workspace only.
- Recompute `git status --short --untracked-files=all` immediately before any staging decision.
- Do not stage shared tracked files such as `Main.kt` or `Models.kt` without an explicit `01` decision naming the chosen group.
- Do not treat this brief as permission to run `git add`.

## 2. Current Inventory

Inventory command:

```powershell
git status --short --untracked-files=all
```

Counting rule:

- Tracked modified paths are status lines not starting with `?? `.
- Untracked entries are status lines starting with `?? `.
- Untracked entries are classified by path prefix.
- Classification sum must equal untracked total.

Current result after adding this brief:

| Class | Count |
| --- | ---: |
| Tracked modified paths | 7 |
| Untracked entries | 122 |
| Untracked service files | 48 |
| Untracked test files | 49 |
| Untracked docs entries | 16 |
| Untracked sample entries | 7 |
| Untracked memory entries | 2 |
| Untracked other entries | 0 |
| Classified untracked sum | 122 |

The adjacent storyboard suite report counted 121 untracked entries before this brief existed. This brief adds one untracked docs entry, so the current inventory is expected to be 122 and remains fully classified with no unexplained delta.

## 3. Shared Tracked Surfaces

The following tracked modified files are cross-group surfaces:

| File | Evidence | Decision impact |
| --- | --- | --- |
| `PLANS.md` | Live control plan and drift log spanning many slices. | Safe only inside an approved control-doc staging candidate. |
| `app/src/main/kotlin/com/localnovel/engine/Main.kt` | Contains broad CLI wiring for many local JSON gate groups. | Must not be staged with only one service group unless `01` explicitly approves that split. |
| `app/src/main/kotlin/com/localnovel/engine/model/Models.kt` | Contains broad JSON model/report classes; read-only diff scan found 216 added `data class` declarations. | Must not be staged without the selected implementation groups that depend on it. |
| `docs/AUTHORING_QUALITY_GUIDELINES.md` | Modified planning / authoring guidance. | Needs ownership confirmation before staging with control docs or planning docs. |
| `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md` | Modified planning register. | Needs ownership confirmation before staging with control docs or planning docs. |
| `docs/HANDOFF.md` | Modified handoff notes. | Needs ownership confirmation before staging with control docs or planning docs. |
| `docs/PROJECT_STRUCTURE_REPORT.md` | Modified structure report. | Needs ownership confirmation before staging with control docs or planning docs. |

## 4. Candidate Options

### Option A: Control Docs Only

Summary:

- Stage only the docs/control reports that govern progress, lifecycle, release hygiene, grouping, reproducibility, handoff, and this decision brief.

Candidate files / path patterns:

- `PLANS.md`
- `docs/PROJECT_COMPLETION_PROGRESS.md`
- `docs/PIPELINE_LIFECYCLE_MAP.md`
- `docs/RELEASE_HYGIENE_AND_REPRODUCIBILITY_REPORT.md`
- `docs/RELEASE_GROUPING_PREVIEW.md`
- `docs/FOCUSED_REPRODUCIBILITY_BUNDLE.md`
- `docs/TRACKED_HANDOFF_MANIFEST.md`
- `docs/ADJACENT_STORYBOARD_SUITE_REPRODUCIBILITY.md`
- `docs/FIRST_STAGING_CANDIDATE_DECISION_BRIEF.md`

Required shared tracked files:

- `PLANS.md`

Minimum verification commands:

```powershell
git diff --check -- PLANS.md docs/PROJECT_COMPLETION_PROGRESS.md docs/PIPELINE_LIFECYCLE_MAP.md docs/RELEASE_HYGIENE_AND_REPRODUCIBILITY_REPORT.md docs/RELEASE_GROUPING_PREVIEW.md docs/FOCUSED_REPRODUCIBILITY_BUNDLE.md docs/TRACKED_HANDOFF_MANIFEST.md docs/ADJACENT_STORYBOARD_SUITE_REPRODUCIBILITY.md docs/FIRST_STAGING_CANDIDATE_DECISION_BRIEF.md
rg -n "C:\\Users\\f7212\\OneDrive\\文件\\AI 劇本|OneDrive\\文件\\AI 劇本" docs/PROJECT_COMPLETION_PROGRESS.md docs/PIPELINE_LIFECYCLE_MAP.md docs/RELEASE_HYGIENE_AND_REPRODUCIBILITY_REPORT.md docs/RELEASE_GROUPING_PREVIEW.md docs/FOCUSED_REPRODUCIBILITY_BUNDLE.md docs/TRACKED_HANDOFF_MANIFEST.md docs/ADJACENT_STORYBOARD_SUITE_REPRODUCIBILITY.md docs/FIRST_STAGING_CANDIDATE_DECISION_BRIEF.md
```

Benefits:

- Lowest technical risk.
- Does not require `Main.kt` or `Models.kt`.
- Preserves release governance before any implementation staging.
- Makes future staging decisions auditable.

Risks / blockers:

- Does not stage product implementation code.
- `PLANS.md` contains historical old-path relocation notes, so review must distinguish historical records from current canonical path.
- Some modified planning docs are not included and remain dirty.

Why it should or should not be first:

- Should be first if `01` wants a low-risk staging candidate that captures governance and reproducibility evidence.
- Should not be first if `01` wants immediate product-code handoff.

### Option B: Storyboard Product-Proof Group

Summary:

- Stage the current named reviewable storyboard product-proof path and its adjacent storyboard test surface.

Candidate files / path patterns:

- `app/src/main/kotlin/com/localnovel/engine/service/Storyboard*Service.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ReviewableStoryboardPacketService.kt`
- `app/src/test/kotlin/com/localnovel/engine/Storyboard*Test.kt`
- `app/src/test/kotlin/com/localnovel/engine/ReviewableStoryboardPacketTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/PipelineHappyPathFixtureTest.kt`

Required shared tracked files:

- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- Possibly `PLANS.md` and control docs if the staging candidate includes product-proof evidence.

Minimum verification commands:

```powershell
./gradlew.bat test --tests "*StoryboardPlanningPreflightTest" --no-daemon --console=plain
./gradlew.bat test --tests "*StoryboardPlanningBriefTest" --no-daemon --console=plain
./gradlew.bat test --tests "*StoryboardPacketDraftValidationTest" --no-daemon --console=plain
./gradlew.bat test --tests "*StoryboardPacketReviewTest" --no-daemon --console=plain
./gradlew.bat test --tests "*StoryboardPacketHumanReviewPreflightTest" --no-daemon --console=plain
./gradlew.bat test --tests "*StoryboardPacketHumanReviewDecisionIntakeTest" --no-daemon --console=plain
./gradlew.bat test --tests "*ReviewableStoryboardPacketTest" --no-daemon --console=plain
./gradlew.bat test --tests "*StoryboardStoryMovementAuditTest" --no-daemon --console=plain
./gradlew.bat test --tests "*PipelineHappyPathFixtureTest" --no-daemon --console=plain
```

Benefits:

- Closest to the current product-proof milestone.
- The adjacent storyboard suite and product-proof carry-forward tests are accepted by `03`.
- Demonstrates the reviewable storyboard packet path rather than only governance docs.

Risks / blockers:

- Requires broad shared tracked files `Main.kt` and `Models.kt`.
- `Main.kt` and `Models.kt` include many non-storyboard gate families, so staging them with only storyboard services may create a misleading partial package.
- Needs a precise file-level staging manifest before execution.

Why it should or should not be first:

- Should be first only if `01` wants product-proof code staged before governance-only docs and explicitly approves handling broad shared dependencies.
- Should not be first if `01` wants the least risky first staging candidate.

### Option C: No Staging Yet

Summary:

- Do not stage anything yet. Continue release-hardening evidence before choosing a staging candidate.

Candidate files / path patterns:

- None.

Required shared tracked files:

- None.

Minimum verification commands:

```powershell
git status --short --untracked-files=all
./gradlew.bat test --tests "*Storyboard*Test" --tests "*ReviewableStoryboardPacketTest" --tests "*PipelineHappyPathFixtureTest" --no-daemon --console=plain
```

Benefits:

- Avoids premature staging while the dirty tree still has 48 untracked service files, 49 untracked test files, and broad shared tracked files.
- Allows `01` to request full storyboard group reproducibility, full release verification, or a source-level manifest first.
- Avoids accidentally bundling planning-only docs or local memory files.

Risks / blockers:

- Does not reduce dirty-tree complexity.
- Delays transition from review evidence to actual version-control handoff.

Why it should or should not be first:

- Should be first if `01` wants more confidence before any staging.
- Should not be first if the project needs a concrete version-control handoff now.

### Option D: Full Storyboard Group Reproducibility Before Staging

Summary:

- Run a broader one-command storyboard group reproducibility check before choosing Option B.

Candidate files / path patterns:

- Same as Option B, but this option is only a verification slice, not staging.

Required shared tracked files:

- None unless a later staging decision is approved.

Minimum verification command:

```powershell
./gradlew.bat test --tests "*Storyboard*Test" --tests "*ReviewableStoryboardPacketTest" --tests "*PipelineHappyPathFixtureTest" --no-daemon --console=plain
```

Benefits:

- Tests the storyboard group in a combined invocation rather than only sequential focused commands.
- Useful before staging the product-proof group.

Risks / blockers:

- Still does not solve `Main.kt` / `Models.kt` shared-file ownership.
- May uncover command-combination issues that need a separate REVISE slice.

Why it should or should not be first:

- Should be first if `01` wants more product-code confidence but still no staging.
- Should not be first if `01` wants to begin with a low-risk docs-only staging candidate.

## 5. Recommendation

Recommended first staging candidate if `01` wants to start staging:

```text
Option A: control docs only
```

Reason:

- It has the lowest dependency risk.
- It does not require `Main.kt` or `Models.kt`.
- It preserves the progress rubric, lifecycle map, release hygiene report, grouping preview, reproducibility evidence, handoff manifest, and this decision brief before implementation staging begins.

Recommended next non-staging slice if `01` wants more confidence first:

```text
Option D: full storyboard group reproducibility before staging
```

Important:

- Neither recommendation is approval to stage.
- Actual staging requires explicit `01` approval naming the selected option and exact files.

## 6. Boundary Statement

This brief is not:

- staging approval
- commit approval
- release approval
- cleanup apply
- branch change
- runtime / SQLite work
- provider / image / video / audio work
- asset approval
- approved reference
- registered reference
- canon
- user approval
- production lock
- runtime scene creation

This brief only compares first staging candidate options for a future `01` decision.

## 7. Verification Record

Commands for this slice:

```powershell
git status --short --untracked-files=all
git diff --stat -- PLANS.md app/src/main/kotlin/com/localnovel/engine/Main.kt app/src/main/kotlin/com/localnovel/engine/model/Models.kt docs/AUTHORING_QUALITY_GUIDELINES.md docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md docs/HANDOFF.md docs/PROJECT_STRUCTURE_REPORT.md
git diff --check -- PLANS.md docs/FIRST_STAGING_CANDIDATE_DECISION_BRIEF.md
```

Additional checks:

- New report trailing-whitespace scan.
- Old workspace full-path scan on this report and touched control docs.

Results:

- Inventory command result after adding this brief: tracked modified `7`; untracked total `122`; services `48`; tests `49`; docs `16`; samples `7`; memory entries `2`; other `0`; classified sum `122`.
- Shared tracked diff scope: `Main.kt` and `Models.kt` remain broad cross-group dependencies; `Models.kt` read-only scan found 216 added `data class` declarations.
- Gradle was not rerun because this slice is docs/control only and changed no Kotlin/source/test/schema/runtime/CLI behavior.
