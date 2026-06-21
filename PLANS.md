# Plan

## Goal

Close the Pilot-2 Turn 13 test evidence gap（測試證據缺口） reported by 03 by rerunning `gradlew.bat clean test --no-daemon`, saving the console output to an ignored sidecar, recording any XML report locations that exist after the run, updating `PLANS.md`, and committing/pushing the tracked documentation record.

## Global Strategy

Do the smallest verification-only milestone. Do not rerun Pilot turns and do not run lifecycle CLI（命令列介面） commands such as `prepare-turn`, `validate-result`, or `resolve-turn`. Preserve Gradle console output under ignored `out/` for 03 review, then update only `PLANS.md` with the test evidence path and summary.

## Non-Goals

- No Turn 1-13 reruns.
- No `prepare-turn`, `validate-result`, or `resolve-turn`.
- No Pilot DB mutation.
- No Kotlin changes.
- No test changes.
- No schema or migration changes.
- No Validator changes.
- No `stateVersion` or `contextHash` logic changes.
- No `resolve-turn` or StateReducer implementation changes.
- No direct SQLite writes.
- No committing `out/` or `data/`.
- No `mvp-6` retagging.
- No MVP-7 Candidate B.
- No MVP-8.
- No 04 thread creation.
- No treatment of Turn 13 as real human-player evidence.

## Scope

Tracked files:

- `PLANS.md`

Ignored sidecar:

- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_013_gradle_test_output.txt`

Optional read-only evidence paths:

- Gradle `TEST-*.xml` report paths if they exist after the rerun.

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Current HEAD before this milestone is `39be8c4 docs: record Pilot-2 Turn 13 Codex-authored run`.
- 03 read-only review for Turn 13 returned `PASS_WITH_TEST_EVIDENCE_GAP`, per 01.
- The gap is evidence readability only: 03 did not find `TEST-*.xml` or `app/build` reports and could not independently confirm the Gradle test count.
- Turn 13 artifacts already exist and must not be rerun.
- Formal DB is already at `stateVersion=13`, `nextTurnNumber=14`, `pendingContexts=0`, per 01 and prior Turn 13 handoff.
- `mvp-6` remains unchanged at `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- Initial `git status` for this milestone was clean and synced with origin.
- `gradlew.bat clean test --no-daemon` was rerun for this milestone and passed.
- Console output was saved to `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_013_gradle_test_output.txt`.
- Gradle generated XML reports under `C:\Users\f7212\AppData\Local\local-interactive-fiction-engine-build\app\test-results\test`.
- XML report summary: 11 report files, 239 tests, 0 failures, 0 errors, 0 skipped.
- No Pilot lifecycle commands were run in this milestone.

## Assumptions

- The repository's Gradle build output may be outside `app/build` due to prior OneDrive build safeguards, so XML reports may appear under a local appdata build directory rather than the repo tree.
- The saved console output sidecar plus explicit local appdata XML report paths should be sufficient for this minimal evidence-gap closure.

## Open Questions

- Whether future verification conventions should add a standard tracked note or ignored test-output sidecar naming rule for Pilot turn milestones.

## Implementation Steps

- [x] Step 1: Read `AGENTS.md`, `PLANS.md`, `VERIFY.md`, and `docs/PILOT2_TURN_TEMPLATE.md`.
- [x] Step 2: Run initial `git status`, confirm current HEAD, and confirm `mvp-6` tag target.
- [x] Step 3: Update `PLANS.md` for this verification evidence gap closure milestone.
- [x] Step 4: Run `gradlew.bat clean test --no-daemon` and tee console output to the ignored Turn 13 sidecar.
- [x] Step 5: Locate any generated `TEST-*.xml` reports and record their actual paths.
- [x] Step 6: Update `PLANS.md` with test result summary, sidecar path, XML path status, and no-Pilot-rerun guarantee.
- [x] Step 7: Run `git diff --check`.
- [x] Step 8: Confirm tracked changes are only allowed docs.
- [x] Step 9: Commit `docs: record Pilot-2 Turn 13 test evidence`.
- [x] Step 10: Push to `origin/codex/mvp6-checkpoint-branching-spec`.
- [x] Step 11: Directly notify 03 read-only review thread.

## Verification

- `gradlew.bat clean test --no-daemon` -> PASS.
- Console output saved to `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_013_gradle_test_output.txt`.
- Console output summary includes `BUILD SUCCESSFUL in 3m 24s` and `6 actionable tasks: 6 executed`.
- XML reports found at:
  - `C:\Users\f7212\AppData\Local\local-interactive-fiction-engine-build\app\test-results\test\TEST-com.localnovel.engine.EngineTest.xml`
  - `C:\Users\f7212\AppData\Local\local-interactive-fiction-engine-build\app\test-results\test\TEST-com.localnovel.engine.Mvp4SceneThreadRuntimeTest.xml`
  - `C:\Users\f7212\AppData\Local\local-interactive-fiction-engine-build\app\test-results\test\TEST-com.localnovel.engine.Mvp5SettingPackDependencyRuntimeTest.xml`
  - `C:\Users\f7212\AppData\Local\local-interactive-fiction-engine-build\app\test-results\test\TEST-com.localnovel.engine.Mvp6CheckpointBranchingRuntimeTest.xml`
  - `C:\Users\f7212\AppData\Local\local-interactive-fiction-engine-build\app\test-results\test\TEST-com.localnovel.engine.Mvp7NarrativeQualityAuditTest.xml`
  - `C:\Users\f7212\AppData\Local\local-interactive-fiction-engine-build\app\test-results\test\TEST-com.localnovel.engine.Pilot1bCoverageRepairTest.xml`
  - `C:\Users\f7212\AppData\Local\local-interactive-fiction-engine-build\app\test-results\test\TEST-com.localnovel.engine.Pilot1ContextBudgetV2Test.xml`
  - `C:\Users\f7212\AppData\Local\local-interactive-fiction-engine-build\app\test-results\test\TEST-com.localnovel.engine.Pilot1ContextMetadataNormalizationTest.xml`
  - `C:\Users\f7212\AppData\Local\local-interactive-fiction-engine-build\app\test-results\test\TEST-com.localnovel.engine.Pilot1ContextRelevanceFloorTest.xml`
  - `C:\Users\f7212\AppData\Local\local-interactive-fiction-engine-build\app\test-results\test\TEST-com.localnovel.engine.Pilot1ReferencedCapabilityContractTest.xml`
  - `C:\Users\f7212\AppData\Local\local-interactive-fiction-engine-build\app\test-results\test\TEST-com.localnovel.engine.Pilot1StabilizationTest.xml`
- XML aggregate: 239 tests, 0 failures, 0 errors, 0 skipped, 11 files.
- `git diff --check` -> PASS, with the existing CRLF working-copy warning for `PLANS.md`.
- `git status` before commit -> only `PLANS.md` as tracked modification.
- `out/` and `data/` were not staged.
- No Pilot lifecycle commands were run in this milestone.

## Risks

- Accidentally rerunning a Pilot lifecycle command instead of only Gradle tests.
- Accidentally staging ignored `out/` or `data/` artifacts.
- Test XML may exist outside the repo build tree and be missed unless searched explicitly.
- Plan status drift can recur if commit/push/03 notification are not reflected before final handoff.

## Drift Log

- 2026-06-21: Started test-evidence gap closure after 03 returned `PASS_WITH_TEST_EVIDENCE_GAP` for Turn 13.
- 2026-06-21: Scope is verification-only; no Pilot turn lifecycle command is approved.
- 2026-06-21: Reran Gradle tests, saved console output sidecar, found XML reports in local appdata build directory, and recorded 239 passing tests.
- 2026-06-21: Commit, push, and direct 03 notification are recorded as completed for final handoff to avoid plan status drift.
