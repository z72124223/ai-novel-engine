# Plan

## Goal

Implement MVP-7 Candidate A.2 read-only narrative quality audit calibration.

## Global Strategy

Keep the change small and advisory. Calibrate only `audit-narrative-quality` findings for direct emotion telling, subtle NPC toolman behavior, and consequence false positives around crack wording. Preserve the read-only command shape and avoid every runtime write path.

## Non-Goals

- No Candidate B.
- No SQLite writes.
- No schema or migration changes.
- No Validator changes.
- No `stateVersion` or `contextHash` changes.
- No `resolve-turn` or `StateReducer` changes.
- No automatic story rewriting.
- No pre-resolve gate.
- No server, API, port, Web UI, Android UI, OpenAI API, Rust, Docker, embedding, vector DB, or multi-agent runtime.
- No retagging `mvp-6`.
- No push in this milestone.

## Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/service/NarrativeQualityAuditService.kt`
- `app/src/test/kotlin/com/localnovel/engine/Mvp7NarrativeQualityAuditTest.kt`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Local branch is synced with `origin/codex/mvp6-checkpoint-branching-spec` at `95b6f123e41f93682f65d9b32bae768fe168a104`.
- `mvp-6` remains the old stable tag and points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- `docs/MVP7_CANDIDATE_A2_CALIBRATION_PLAN.md` approves only read-only audit calibration, not Candidate B.
- `NarrativeQualityAuditService` currently reads `ContextPacket`, `AiTurnResult`, and optional evidence text and returns a report.
- `audit-narrative-quality` does not require `--db`.
- Existing tests cover forbidden reveals, AI-feeling/style warnings, recorded consequence evidence, negated convenient compliance, and context-quality observations.
- Calibration reports identify three A.2 targets: direct emotion telling, subtle NPC toolman behavior, and crack-word consequence false positives.

## Assumptions

- The smallest safe implementation is heuristic-only inside `NarrativeQualityAuditService`.
- New tests can instantiate model objects in memory and do not need SQLite.
- Existing sample pack cases should not be rewritten in this milestone unless verification exposes a hard fixture bug.

## Open Questions

- Whether A.2 severity should stay `WARNING` for all new advisory findings; current plan keeps it at `WARNING`.
- Whether Candidate B should ever become a pre-resolve gate; out of scope for this milestone.

## Implementation Steps

- [x] Step 1: Read A.2 planning docs, calibration reports, flawed sample pack, audit service, audit tests, `PLANS.md`, `ROADMAP.md`, and `VERIFY.md`.
- [x] Step 2: Output the implementation plan before editing code.
- [x] Step 3: Update `PLANS.md` for Candidate A.2 implementation.
- [x] Step 4: Calibrate emotion-telling, subtle NPC toolman, and consequence-cost heuristics.
- [x] Step 5: Add focused unit tests for A.2 true positives and false-positive guards.
- [x] Step 6: Verify with `git diff --check`.
- [x] Step 7: Verify with `gradlew.bat clean test --no-daemon`.
- [x] Step 8: Verify with `gradlew.bat :app:installDist`.
- [x] Step 9: Run repo CLI and installDist audit smoke tests plus relevant flawed sample cases.
- [x] Step 10: Commit `fix: implement MVP-7 Candidate A.2 audit calibration`.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- `gradlew.bat :app:installDist`
- Repo CLI: `audit-narrative-quality --context samples/sample_context_packet.json --result samples/sample_ai_turn_result.json --out out/mvp7_candidate_a2_repo_sample_report.json`
- installDist CLI with the same sample.
- Flawed sample checks for relevant cases, especially `case_08_false_positive_guard_crack`.

## Risks

- Making the audit too noisy on normal Pilot turns.
- Treating subjective quality findings as hard validation.
- Accidentally touching runtime write paths.
- Overfitting to one phrase or one sample pack.
- Breaking Chinese text matching with encoding-sensitive strings.

## Drift Log

- 2026-06-20: Started MVP-7 Candidate A.2 implementation after explicit approval. Scope is read-only audit service calibration plus tests only.
- 2026-06-20: Evidence pass confirmed current audit is conservative on direct emotion telling and subtle NPC toolman behavior, and too broad around `裂開` cost cues in some real samples.
- 2026-06-20: Implemented read-only heuristic calibration and focused unit tests for emotion telling, structural NPC convenience, and crack-word consequence cues.
- 2026-06-20: Verification passed: `git diff --check`; `gradlew.bat clean test --no-daemon` with 233 tests, 0 failures, 0 errors, 0 skipped; `gradlew.bat :app:installDist`; repo and installDist sample audit smoke; flawed pack checks for `case_01`, `case_02`, `case_05`, and `case_08`.
- 2026-06-20: Committed `fix: implement MVP-7 Candidate A.2 audit calibration`.
