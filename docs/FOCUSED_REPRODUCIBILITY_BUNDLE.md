# Focused Reproducibility Bundle

Status: `FOCUSED_REPRODUCIBILITY_BUNDLE_V1`

Date: 2026-06-27

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

This report records focused local reproducibility evidence for the current reviewable storyboard packet product-proof path. It does not change source, tests, schema, runtime, CLI behavior, SQLite data, provider state, media output, or release status.

## 1. Scope

This slice follows the `minimal release grouping preview` PASS by `03`, with official product progress displayed by `01` as `76%`.

In scope:

- Run the minimum focused product-proof Gradle tests.
- Record exact commands and results.
- Record current dirty-tree inventory with the same reproducible command mouth used by release hygiene reports.
- State the remaining release blockers.

Out of scope:

- Source/test/schema/runtime/CLI edits.
- Cleanup apply, staging, commit, push, revert, delete, or move.
- Full release verification.
- SQLite writes, schema migration, runtime state changes, provider/API calls, remote downloads, image/video/audio generation, asset approval, approved reference, registered reference, canon, user approval, production lock, or runtime scene creation.

## 2. Test Bundle Selection

The minimum focused bundle uses the three highest-value product-proof tests from the release grouping preview's Group D:

| Test | Reason |
| --- | --- |
| `ReviewableStoryboardPacketTest` | Verifies the named local JSON product artifact can still be created from audited evidence. |
| `StoryboardStoryMovementAuditTest` | Verifies deterministic story movement readiness still works for the text-only storyboard skeleton. |
| `PipelineHappyPathFixtureTest` | Verifies the coherent local JSON pipeline still reaches the human-reviewable storyboard endpoint used by v1. |

Adjacent storyboard suites such as `*StoryboardPacket*Test` and `*StoryboardPlanning*Test` are intentionally left for a later broader reproducibility slice. This report is the minimum bundle, not full release verification.

## 3. Exact Commands And Results

Command 1:

```powershell
./gradlew.bat test --tests "*ReviewableStoryboardPacketTest" --no-daemon --console=plain
```

Result:

```text
BUILD SUCCESSFUL in 14s
5 actionable tasks: 2 executed, 3 up-to-date
```

Command 2:

```powershell
./gradlew.bat test --tests "*StoryboardStoryMovementAuditTest" --no-daemon --console=plain
```

Result:

```text
BUILD SUCCESSFUL in 17s
5 actionable tasks: 3 executed, 2 up-to-date
```

Command 3:

```powershell
./gradlew.bat test --tests "*PipelineHappyPathFixtureTest" --no-daemon --console=plain
```

Result:

```text
BUILD SUCCESSFUL in 17s
5 actionable tasks: 3 executed, 2 up-to-date
```

## 4. Inventory After This Report

Inventory command:

```powershell
git status --short --untracked-files=all
```

Counting rule:

- Tracked modified paths are status lines not starting with `?? `.
- Untracked entries are status lines starting with `?? `.
- Untracked entries are classified by path prefix.
- Classification sum must equal untracked total.

Expected drift:

- The release grouping preview counted 118 untracked entries before this report existed.
- This report adds one untracked docs entry, so the current inventory is expected to be 119 after the report is created.

Current result:

| Class | Count |
| --- | ---: |
| Tracked modified paths | 7 |
| Untracked entries | 119 |
| Untracked service files | 48 |
| Untracked test files | 49 |
| Untracked docs entries | 13 |
| Untracked sample entries | 7 |
| Untracked memory entries | 2 |
| Untracked other entries | 0 |
| Classified untracked sum | 119 |

## 5. Pass / Fail Interpretation

The focused bundle passed.

This means:

- The current named local JSON storyboard product artifact path remains reproducible.
- The story movement audit gate remains reproducible.
- The E2E happy-path fixture remains reproducible.

This does not mean:

- The full test suite has passed.
- The repo is ready to stage, commit, push, or release.
- The storyboard is approved, canon, provider-ready, image-ready, video-ready, production-ready, or user-approved.
- Any registry, asset, reference, runtime scene, or media output has been approved, locked, registered, created, or written.

## 6. Remaining Release Blockers

- Full release verification has not run after all accepted artifacts.
- Shared tracked files such as `Main.kt` and `Models.kt` still span many groups and need a manifest before staging.
- Many service/test files remain untracked and must be deliberately grouped.
- Planning-only and historical docs need ownership / inclusion review.
- `memory/` entries need `01` decision on repo release inclusion versus local-only support.
- Legacy path hygiene remains open for historical `PLANS.md` sections.

## 7. Recommended Next Step

Recommended next slice:

```text
tracked handoff manifest
```

Purpose:

- List exact files required for a reproducible handoff by group.
- Include the shared tracked files each group depends on.
- Keep the work read-only until `01` explicitly approves staging or commit preparation.

Alternative:

```text
adjacent storyboard suite reproducibility
```

Purpose:

- Expand from the minimum product-proof tests to broader `*Storyboard*Test` coverage.

## 8. Boundary Statement

This report is not:

- cleanup apply
- staging approval
- commit approval
- release candidate approval
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

This report only records focused local test evidence for the current product-proof path.

## 9. Verification Record

Commands run for this slice:

```powershell
./gradlew.bat test --tests "*ReviewableStoryboardPacketTest" --no-daemon --console=plain
./gradlew.bat test --tests "*StoryboardStoryMovementAuditTest" --no-daemon --console=plain
./gradlew.bat test --tests "*PipelineHappyPathFixtureTest" --no-daemon --console=plain
git status --short --untracked-files=all
git diff --check -- PLANS.md docs/FOCUSED_REPRODUCIBILITY_BUNDLE.md
```

Additional checks:

- New report trailing-whitespace scan.
- Old workspace full-path scan on this report and touched control docs.

Results:

- Inventory command result: tracked modified `7`; untracked total `119`; services `48`; tests `49`; docs `13`; samples `7`; memory entries `2`; other `0`; classified sum `119`.
- `git diff --check` exited 0 with only the existing Windows line-ending warning for `PLANS.md`.
- New report trailing-whitespace scan returned no matches.
- Old workspace full-path scan returned no matches in this report. It still returns pre-existing historical relocation notes in `PLANS.md`.
