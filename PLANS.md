# Plan

## Goal

Implement MVP-7 Candidate A.1 audit calibration refinements: fix one known false positive and add small read-only context-quality observations to the existing narrative quality audit.

## Global Strategy

Keep Candidate A.1 advisory and read-only. Improve the existing `NarrativeQualityAuditService` heuristics without changing runtime writes, Validator behavior, schema, `resolve-turn`, `StateReducer`, `stateVersion`, or `contextHash`.

## Non-Goals

- No SQLite writes.
- No schema or migration edits.
- No `resolve-turn` behavior changes.
- No `StateReducer` changes.
- No Validator safety-boundary changes.
- No `stateVersion` or `contextHash` changes.
- No story rewriting.
- No Candidate B.
- No server, API, port, Web UI, Android UI, OpenAI API, Rust, Docker, embedding, vector DB, or multi-agent runtime.
- No push, merge, tag creation, retagging, or branch deletion.

## Scope

- Fix negated convenient-compliance phrase matching.
- Add advisory context-quality findings for thin setting context, missing transition availability, missing scheduled world-event pressure, and procedural stasis risk.
- Add/update unit tests.
- Update calibration documentation and this plan.
- Run required verification and commit if all pass.

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- The branch was synced with `origin/codex/mvp6-checkpoint-branching-spec` at `3fb3eaf` before this milestone.
- `mvp-6` exists and points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- `docs/MVP7_AUDIT_CALIBRATION_REPORT.md` identifies one false positive: `沒有立刻答應` matched `立刻答應`.
- The same calibration report identifies missed issues around setting bytes, missing legal scene transition, missing scheduled world-event pressure, and procedural stasis.
- `NarrativeQualityAuditService` currently uses simple `contains` matching for convenient-compliance terms.
- `ContextPacket` includes `settingContextBudget`, `availableTransitions`, `pendingWorldEvents`, `activeStoryThreads`, `sceneLifecycle`, `contextBudget`, and `playerInput`.
- `audit-narrative-quality` is a read-only CLI command that does not require `--db`.

## Assumptions

- The new context-quality observations should remain `WARNING`, not hard failures.
- "Too small" setting context can be treated as `actualBytes <= 16` when omitted settings or setting relevance exists.
- Missing transition observation should trigger only when the player/result language mentions retreat/exit/door/threshold/transition intent.
- Missing scheduled-world-event pressure should be framed as coverage risk, not a per-turn correctness violation.

## Open Questions

- Whether context-quality observations should eventually live in a separate context audit command instead of narrative quality audit.
- Whether thresholds should be configurable after more calibration samples.

## Implementation Steps

- [x] Step 1: Read MVP-7 spec, calibration report, audit service, tests, samples, PLANS, ROADMAP, and VERIFY.
- [x] Step 2: Output plan before code changes.
- [x] Step 3: Update this plan for Candidate A.1.
- [x] Step 4: Implement false-positive suppression and context-quality observations.
- [x] Step 5: Add/update tests.
- [x] Step 6: Update docs.
- [x] Step 7: Run full verification and CLI smokes.
- [x] Step 8: Commit `fix: calibrate narrative quality audit findings`.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- `gradlew.bat :app:installDist`
- Repo CLI sample audit smoke.
- installDist sample audit smoke.
- Turn 12 audit smoke confirming the previous false positive is gone.
- A constructed sample/context test that triggers at least one context-quality observation.

## Risks

- Context-quality observations could become noisy if they fire on intentionally tiny sample contexts.
- Transition/event observations can conflate "coverage gap" with "turn quality"; keep messages explicit and advisory.
- Scope could drift into Candidate B; do not make findings block `resolve-turn`.

## Drift Log

- 2026-06-20: Started Candidate A.1 after explicit approval. Plan confirms read-only scope and no Candidate B.
- 2026-06-20: Added negation-aware convenient-compliance matching plus read-only context-quality observations and tests.
- 2026-06-20: Verification passed: `git diff --check`, 227 tests with 0 failures/errors/skips, `gradlew.bat :app:installDist`, repo CLI sample audit, installDist sample audit, and Turn 12 audit smoke. Turn 12 no longer reports the old convenient-compliance false positive.
