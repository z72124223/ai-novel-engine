# Plan

## Goal

Create an MVP-7 deliberately flawed audit sample pack that exercises obvious narrative-quality failures and a known false-positive guard case using the existing read-only `audit-narrative-quality` command.

## Global Strategy

Keep this milestone fixture-only and documentation-only. Add sample JSON context/result pairs plus a report based on actual CLI audit output. Do not change Kotlin code, tests, audit rules, schema, Validator, `stateVersion`, `contextHash`, `resolve-turn`, `StateReducer`, or SQLite data.

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
- No server, API, port, Web UI, Android UI, OpenAI API, Rust, Docker, embedding, vector DB, or multi-agent runtime.
- No retagging `mvp-6`.
- No push in this milestone.

## Scope

- `PLANS.md`
- `samples/mvp7_flawed_audit_pack/`
- `docs/MVP7_FLAWED_AUDIT_SAMPLE_PACK_REPORT.md`
- Ignored audit output under `out/mvp7_flawed_audit_pack/`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Local branch is synced with `origin/codex/mvp6-checkpoint-branching-spec` at `7f6c864cebd755ca648893bf6b8c1831213b37bc`.
- `mvp-6` remains the old stable tag and points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- Candidate A and Candidate A.1 are complete.
- `NarrativeQualityAuditService` is read-only and consumes `ContextPacket`, `AiTurnResult`, and optional evidence text.
- `audit-narrative-quality` does not require `--db`.
- Existing rules can report NPC liveliness, AI-feeling, world-autonomy, context-quality, knowledge-boundary, style, consequence, and pilot-evidence findings.
- `samples/sample_context_packet.json` and `samples/sample_ai_turn_result.json` provide a valid baseline JSON shape, but stronger fixture coverage needs canonical fields such as `characterActions` and `stateDelta`.

## Assumptions

- Eight compact flawed cases are sufficient for this milestone because the user requested 6-8 cases.
- The sample pack should be committed because the user explicitly requested samples.
- Generated audit reports under `out/` should stay ignored and uncommitted.

## Open Questions

- Whether any findings from this sample pack should lead to Candidate A.2 rule calibration is a post-report decision, not part of this milestone.
- Whether Candidate B should exist remains undecided and out of scope.

## Implementation Steps

- [x] Step 1: Confirm git status, recent log, sample directory state, and `mvp-6` tag.
- [x] Step 2: Read MVP-7 specs, calibration reports, audit service, audit tests, samples, `PLANS.md`, `ROADMAP.md`, and `VERIFY.md`.
- [x] Step 3: Publish a concise plan before writing samples.
- [x] Step 4: Create 6-8 deliberately flawed context/result sample cases and a manifest.
- [x] Step 5: Run `audit-narrative-quality` against every flawed case.
- [x] Step 6: Write `docs/MVP7_FLAWED_AUDIT_SAMPLE_PACK_REPORT.md`.
- [x] Step 7: Verify with `git diff --check` and `gradlew.bat clean test --no-daemon`.
- [x] Step 8: Commit `docs: add flawed audit sample pack`.

## Verification

- Run the existing audit CLI against every sample pair.
- Inspect generated audit verdicts, scores, finding counts, and rule IDs.
- Run `git diff --check`.
- Run `gradlew.bat clean test --no-daemon`.

## Risks

- Accidentally making samples invalid JSON or invalid protocol fixtures.
- Expecting the current audit to catch quality issues it does not yet implement.
- Committing ignored audit output instead of only samples and docs.
- Letting a fixture milestone drift into audit rule changes.

## Drift Log

- 2026-06-20: Started deliberately flawed audit sample pack after explicit approval. Scope is samples plus documentation only.
- 2026-06-20: Added eight flawed sample cases under `samples/mvp7_flawed_audit_pack/`.
- 2026-06-20: Ran `audit-narrative-quality` against all eight cases and generated ignored reports under `out/mvp7_flawed_audit_pack/`.
- 2026-06-20: Added `docs/MVP7_FLAWED_AUDIT_SAMPLE_PACK_REPORT.md` with expected versus actual findings, missed issues, false-positive observations, and Candidate B readiness.
- 2026-06-20: Verification passed: `git diff --check` and `gradlew.bat clean test --no-daemon` with 227 tests, 0 failures, 0 errors, 0 skipped.
- 2026-06-20: Committed `docs: add flawed audit sample pack`.
