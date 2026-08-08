# Adjacent Storyboard Suite Reproducibility

Status: `ADJACENT_STORYBOARD_SUITE_REPRODUCIBILITY_V1`

Date: 2026-06-27

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

This report records local command evidence for the adjacent storyboard planning / packet / human-review suite plus the existing product-proof carry-forward tests. It does not change source, tests, schema, runtime, CLI behavior, SQLite data, provider state, media output, staging status, commit status, or release status.

## 1. Scope

This slice follows the `minimal tracked handoff manifest` PASS by `03`, with official product progress displayed by `01` as `79%`.

In scope:

- Run adjacent storyboard focused Gradle tests.
- Carry forward the three focused product-proof tests from the previous reproducibility bundle.
- Record exact commands, results, and current inventory counts.
- State remaining release blockers.

Out of scope:

- Kotlin/source/test/schema/runtime/CLI behavior edits.
- Cleanup apply, staging, commit, push, revert, delete, or move.
- Full release verification.
- SQLite writes, schema migration, runtime state changes, provider/API calls, remote downloads, image/video/audio generation, asset approval, approved reference, registered reference, canon, user approval, production lock, or runtime scene creation.

## 2. Test Bundle Selection

The adjacent storyboard suite was selected from the actual test files present in `app/src/test/kotlin/com/localnovel/engine`.

Adjacent storyboard tests:

| Test | Reason |
| --- | --- |
| `StoryboardPlanningPreflightTest` | Verifies accepted script packet handoff can enter storyboard planning preflight. |
| `StoryboardPlanningBriefTest` | Verifies storyboard planning brief creation stays source-bound and candidate-only. |
| `StoryboardPacketDraftValidationTest` | Verifies text-only storyboard draft validation and downstream boundary guards. |
| `StoryboardPacketReviewTest` | Verifies storyboard draft review packet packaging remains review-ready, not approved. |
| `StoryboardPacketHumanReviewPreflightTest` | Verifies review packet readiness for human review without approval semantics. |
| `StoryboardPacketHumanReviewDecisionIntakeTest` | Verifies human decision intake boundary before asset-candidate preflight. |

Product-proof carry-forward tests:

| Test | Reason |
| --- | --- |
| `ReviewableStoryboardPacketTest` | Verifies the named local JSON reviewable storyboard product artifact. |
| `StoryboardStoryMovementAuditTest` | Verifies deterministic story movement readiness for the text-only storyboard skeleton. |
| `PipelineHappyPathFixtureTest` | Verifies the coherent local JSON pipeline reaches the human-reviewable storyboard endpoint used by v1. |

This selection is broader than the previous three-test focused bundle, but still narrower than full release verification.

## 3. Exact Commands And Results

### Adjacent Storyboard Suite

Command 1:

```powershell
./gradlew.bat test --tests "*StoryboardPlanningPreflightTest" --no-daemon --console=plain
```

Result:

```text
BUILD SUCCESSFUL in 16s
5 actionable tasks: 3 executed, 2 up-to-date
```

Command 2:

```powershell
./gradlew.bat test --tests "*StoryboardPlanningBriefTest" --no-daemon --console=plain
```

Result:

```text
BUILD SUCCESSFUL in 17s
5 actionable tasks: 3 executed, 2 up-to-date
```

Command 3:

```powershell
./gradlew.bat test --tests "*StoryboardPacketDraftValidationTest" --no-daemon --console=plain
```

Result:

```text
BUILD SUCCESSFUL in 17s
5 actionable tasks: 3 executed, 2 up-to-date
```

Command 4:

```powershell
./gradlew.bat test --tests "*StoryboardPacketReviewTest" --no-daemon --console=plain
```

Result:

```text
BUILD SUCCESSFUL in 17s
5 actionable tasks: 3 executed, 2 up-to-date
```

Command 5:

```powershell
./gradlew.bat test --tests "*StoryboardPacketHumanReviewPreflightTest" --no-daemon --console=plain
```

Result:

```text
BUILD SUCCESSFUL in 17s
5 actionable tasks: 3 executed, 2 up-to-date
```

Command 6:

```powershell
./gradlew.bat test --tests "*StoryboardPacketHumanReviewDecisionIntakeTest" --no-daemon --console=plain
```

Result:

```text
BUILD SUCCESSFUL in 17s
5 actionable tasks: 3 executed, 2 up-to-date
```

### Product-Proof Carry-Forward

Command 7:

```powershell
./gradlew.bat test --tests "*ReviewableStoryboardPacketTest" --no-daemon --console=plain
```

Result:

```text
BUILD SUCCESSFUL in 17s
5 actionable tasks: 3 executed, 2 up-to-date
```

Command 8:

```powershell
./gradlew.bat test --tests "*StoryboardStoryMovementAuditTest" --no-daemon --console=plain
```

Result:

```text
BUILD SUCCESSFUL in 17s
5 actionable tasks: 3 executed, 2 up-to-date
```

Command 9:

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

- The tracked handoff manifest counted 120 untracked entries before this report existed.
- This report adds one untracked docs entry, so the current inventory is expected to be 121 after the report is created.

Current result:

| Class | Count |
| --- | ---: |
| Tracked modified paths | 7 |
| Untracked entries | 121 |
| Untracked service files | 48 |
| Untracked test files | 49 |
| Untracked docs entries | 15 |
| Untracked sample entries | 7 |
| Untracked memory entries | 2 |
| Untracked other entries | 0 |
| Classified untracked sum | 121 |

## 5. Pass / Fail Interpretation

The adjacent storyboard suite reproducibility bundle passed.

This means:

- Storyboard planning preflight and brief tests remain reproducible.
- Storyboard packet draft validation, review packet creation, human review preflight, and human review decision intake remain reproducible.
- The reviewable storyboard packet product artifact, story movement audit, and E2E happy-path fixture remain reproducible after the adjacent suite was rerun.

This does not mean:

- The full test suite has passed.
- The repo is ready to stage, commit, push, or release.
- The storyboard is approved, canon, provider-ready, image-ready, video-ready, production-ready, or user-approved.
- Any asset, reference, registry, runtime scene, provider job, or media output has been approved, locked, registered, created, or written.

## 6. Remaining Release Blockers

- Full release verification has not run after all accepted artifacts.
- Shared tracked files such as `Main.kt` and `Models.kt` still span many groups and require a staging decision brief before any staging attempt.
- Image-facing delivery proof remains separate from storyboard JSON reproducibility.
- Planning-only and historical docs need ownership / inclusion review.
- `memory/` entries need `01` decision on repo release inclusion versus local-only support.
- Legacy path hygiene remains open for historical `PLANS.md` sections.

## 7. Recommended Next Step

Recommended next slice:

```text
first staging candidate decision brief
```

Purpose:

- Ask `01` to choose whether the first staging candidate should be the control-doc group, storyboard product-proof group, or another explicitly bounded group.
- Keep the decision brief read-only until `01` approves actual staging.

Alternative:

```text
full storyboard group reproducibility
```

Purpose:

- Run the broader `*Storyboard*Test` pattern in one command and compare it against the sequential evidence in this report.

## 8. Boundary Statement

This report is not:

- cleanup apply
- staging approval
- commit approval
- release candidate approval
- full release verification
- runtime / SQLite work
- provider / image / video / audio work
- asset approval
- approved reference
- registered reference
- canon
- user approval
- production lock
- runtime scene creation

This report only records adjacent storyboard suite local test evidence.

## 9. Verification Record

Commands run for this slice:

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
git status --short --untracked-files=all
git diff --check -- PLANS.md docs/ADJACENT_STORYBOARD_SUITE_REPRODUCIBILITY.md
```

Additional checks:

- New report trailing-whitespace scan.
- Old workspace full-path scan on this report and touched control docs.

Results:

- All nine focused test commands returned `BUILD SUCCESSFUL`.
- Inventory command result after adding this report: tracked modified `7`; untracked total `121`; services `48`; tests `49`; docs `15`; samples `7`; memory entries `2`; other `0`; classified sum `121`.
- Gradle tests were run sequentially to avoid Windows output / file-lock issues.
