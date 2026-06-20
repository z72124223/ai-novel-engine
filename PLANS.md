# Plan

## Goal

Create a documentation-only MVP-7 Candidate A.2 calibration plan for the read-only narrative quality audit.

## Global Strategy

Keep Candidate A.2 in planning mode only. Use existing evidence from the MVP-7 narrative quality spec, calibration report, real sample collection, deliberately flawed sample pack, current audit service, and current audit tests. Do not implement any Kotlin code, test changes, audit-rule changes, database writes, schema changes, or Candidate B behavior in this milestone.

## Non-Goals

- No Kotlin code changes.
- No test changes.
- No audit rule changes.
- No schema or migration changes.
- No Validator changes.
- No `stateVersion` or `contextHash` changes.
- No `resolve-turn` or `StateReducer` changes.
- No SQLite writes.
- No Candidate B.
- No Candidate A.2 implementation in this milestone.
- No server, API, port, Web UI, Android UI, OpenAI API, Rust, Docker, embedding, vector DB, or multi-agent runtime.
- No retagging `mvp-6`.
- No push in this milestone.

## Scope

- `PLANS.md`
- `ROADMAP.md`
- `docs/MVP7_CANDIDATE_A2_CALIBRATION_PLAN.md`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Local branch is synced with `origin/codex/mvp6-checkpoint-branching-spec` at `02f146526812c7c2bda1b882583b553da2fcbdbb`.
- `mvp-6` remains the old stable tag and points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- Candidate A added a read-only `audit-narrative-quality` CLI command.
- Candidate A.1 reduced the known negated convenient-compliance false positive and added context-quality observations.
- The real audit sample collection found the audit useful but conservative, and identified likely `CONSEQUENCE_COST_NOT_RECORDED` false positives around `裂開`.
- The deliberately flawed sample pack found that obvious AI-feeling/style and knowledge-boundary failures are caught, but direct emotion telling, subtle NPC toolman behavior, and consequence false-positive behavior still need calibration.
- Existing `NarrativeQualityAuditService` is read-only and consumes `ContextPacket`, `AiTurnResult`, and optional evidence text.
- Existing `Mvp7NarrativeQualityAuditTest` covers stable report generation, forbidden reveal, AI-feeling/style, recorded consequence evidence, negated convenient compliance, and context-quality observations.

## Assumptions

- Candidate A.2 should remain advisory and read-only even if later approved for implementation.
- A planning document is enough for this milestone because the user explicitly prohibited implementation.
- `ROADMAP.md` should be updated because the previous next milestone was already completed by the flawed sample pack.

## Open Questions

- Whether Candidate A.2 implementation should be approved after this planning milestone.
- Whether Candidate A.2 should adjust severity thresholds or only add/adjust findings while keeping the same advisory verdict model.
- Whether more real Pilot-1b or MVP-6 branch/fork samples should be collected before Candidate A.2 implementation.

## Implementation Steps

- [x] Step 1: Confirm git status, relevant docs, audit service, audit tests, and sample evidence.
- [x] Step 2: Update `PLANS.md` for the Candidate A.2 planning milestone.
- [x] Step 3: Add `docs/MVP7_CANDIDATE_A2_CALIBRATION_PLAN.md`.
- [x] Step 4: Update `ROADMAP.md` to record sample collection, flawed sample pack, and Candidate A.2 planning state.
- [x] Step 5: Verify with `git diff --check`.
- [x] Step 6: Verify with `gradlew.bat clean test --no-daemon`.
- [x] Step 7: Commit `docs: plan MVP-7 Candidate A.2 audit calibration`.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- Manual diff review to confirm only allowed documentation files changed.

## Risks

- Accidentally implying Candidate B approval.
- Turning planning language into implementation commitments without user approval.
- Writing unverified guesses as facts.
- Touching files outside the approved documentation scope.

## Drift Log

- 2026-06-20: Started MVP-7 Candidate A.2 calibration planning after explicit approval. Scope is documentation only.
- 2026-06-20: Evidence pass confirmed Candidate A/A.1, real sample collection, and flawed sample pack are complete; the remaining issues are direct emotion telling, subtle NPC toolman behavior, and consequence false-positive calibration.
- 2026-06-20: Verification passed: `git diff --check`; `gradlew.bat clean test --no-daemon` passed with 227 tests, 0 failures, 0 errors, 0 skipped.
- 2026-06-20: Committed `docs: plan MVP-7 Candidate A.2 audit calibration`.
