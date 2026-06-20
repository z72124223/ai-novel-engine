# Plan

## Goal

Run the existing read-only MVP-7 narrative quality audit on additional non-Pilot sample families to improve sample diversity before any Candidate B discussion.

## Global Strategy

Do not change audit rules or runtime code. Use existing context/result JSON artifacts only, run `audit-narrative-quality` through the CLI, write generated reports under ignored `out/`, then document what the current audit does on more diverse samples.

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
- No Candidate A.3 implementation.
- No MVP-8.
- No web server, API, Docker, Android UI, Rust, embedding, vector DB, or multi-agent runtime.
- No retagging `mvp-6`.

## Scope

- `PLANS.md`
- New documentation report under `docs/`
- Ignored audit output under `out/`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Local `HEAD` and `origin/codex/mvp6-checkpoint-branching-spec` both point to `665b8cae4d626173fb76763d3988e5b7b6f55a4d`.
- `mvp-6` remains the old stable tag and points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- Candidate A.2 real sample rerun already used Pilot-1, Pilot-1b, and MVP-6 branch/checkpoint samples.
- Existing ignored `out/` artifacts include additional context/result pairs from memory E2E, MVP-4 scene/thread E2E, and MVP-4 soak runs.
- `audit-narrative-quality` is read-only and does not require `--db`.
- Generated audit reports under `out/` are ignored and should not be committed.

## Assumptions

- Some older E2E context/result pairs may be schema-light or synthetic and should be labeled as acceptance fixtures, not real prose-quality proof.
- If a sample cannot be parsed by the current audit command, documenting that incompatibility is more useful than inventing replacement data.

## Open Questions

- Whether enough non-Pilot samples parse cleanly to produce a meaningful diversity report.

## Implementation Steps

- [x] Step 1: Inventory available context/result JSON artifacts.
- [x] Step 2: Update `PLANS.md` for the diverse read-only audit sample milestone.
- [x] Step 3: Build/install CLI if needed and run `audit-narrative-quality` on selected non-Pilot samples.
- [x] Step 4: Write `docs/MVP7_DIVERSE_AUDIT_SAMPLE_RERUN_REPORT.md`.
- [x] Step 5: Verify with `git diff --check`.
- [x] Step 6: Verify with `gradlew.bat clean test --no-daemon`.
- [x] Step 7: Commit `docs: rerun MVP-7 audit on diverse samples`.
- [ ] Step 8: Push `codex/mvp6-checkpoint-branching-spec` to origin and confirm remote state.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- CLI audit commands for selected samples.
- Explicit UTF-8 JSON parse check for generated audit reports.

## Risks

- Treating synthetic E2E fixtures as representative story quality.
- Letting advisory findings become implied Validator gates.
- Accidentally committing ignored `out/` reports.

## Drift Log

- 2026-06-20: Started diverse read-only audit sample rerun after inventory found additional non-Pilot context/result artifacts.
- 2026-06-20: Ran `audit-narrative-quality` through the installDist CLI on 16 additional memory/MVP-4 E2E/soak samples. Generated reports were written under ignored `out/mvp7_diverse_audit_rerun/` and parsed as UTF-8 JSON.
- 2026-06-20: Added `docs/MVP7_DIVERSE_AUDIT_SAMPLE_RERUN_REPORT.md` and updated `ROADMAP.md` to record this completed read-only sample milestone.
- 2026-06-20: Verification passed before commit. `git diff --check` reported no whitespace errors, ignored `out/` reports stayed untracked, and `gradlew.bat clean test --no-daemon` exited `0` with `233 tests / 0 failures / 0 errors / 0 skipped`.
- 2026-06-20: Created docs-only commit `docs: rerun MVP-7 audit on diverse samples`.
