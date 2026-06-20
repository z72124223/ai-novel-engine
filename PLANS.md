# Plan

## Goal

Run MVP-7 Candidate A.2 read-only narrative quality audit against a broader set of real or near-real samples, then document whether the A.2 rules are stable, noisy, or still missing obvious issues.

## Global Strategy

Use existing `audit-narrative-quality` only as a read-only reviewer aid. Collect evidence from existing JSON artifacts, write generated audit outputs only under ignored `out/`, and commit only documentation. Do not change Kotlin code, tests, audit rules, schemas, runtime write paths, or Candidate B planning.

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
- No Candidate A.3.
- No retagging `mvp-6`.
- No push in this milestone.

## Scope

- `PLANS.md`
- `docs/MVP7_A2_REAL_SAMPLE_RERUN_REPORT.md`
- ignored generated audit reports under `out/mvp7_a2_real_sample_rerun/`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Local branch is up to date with `origin/codex/mvp6-checkpoint-branching-spec` at `d79d3e9f2c99d361ff5acdc231fe464dd3e5fa4a`.
- Working tree was clean before this milestone.
- `mvp-6` remains the old stable tag and points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- Candidate A.2 implementation and docs checkpoint were completed and pushed.
- Latest verified test baseline before this milestone was `233 tests / 0 failures / 0 errors / 0 skipped`.
- The installDist app path from `VERIFY.md` is not currently present after the last clean test, so this rerun will use the repository Gradle CLI.
- Existing usable sample artifacts include Pilot-1 stabilized rerun turns, Pilot-1b coverage turns, and MVP-6 branch/checkpoint turns with context/result JSON files.

## Assumptions

- A mixed sample set from Pilot-1, Pilot-1b, and MVP-6 branch artifacts is more useful than rerunning only the previous Pilot-1 turns.
- Full session evidence is acceptable when per-turn evidence is unavailable, but the report must label that evidence precision limitation.
- `out/` remains ignored and suitable for generated audit output.

## Open Questions

- Whether future sample collection should generate fresh per-turn evidence for Pilot-1b and MVP-6 branch turns.
- Whether Candidate B should remain blocked until more story arcs beyond Pilot-1/Pilot-1b are audited.

## Implementation Steps

- [x] Step 1: Confirm Git status, remote, tags, and current docs.
- [x] Step 2: Identify at least 10 existing context/result/evidence sample groups without inventing data.
- [x] Step 3: Update `PLANS.md` for this A.2 real sample rerun milestone.
- [x] Step 4: Run `audit-narrative-quality` on the selected sample groups using the repo CLI.
- [x] Step 5: Summarize audit output into `docs/MVP7_A2_REAL_SAMPLE_RERUN_REPORT.md`.
- [x] Step 6: Verify with `git diff --check`.
- [x] Step 7: Verify with `gradlew.bat clean test --no-daemon`.
- [x] Step 8: Commit `docs: rerun MVP-7 A.2 audit on real samples`.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- Manual review that tracked changes are documentation only.
- Audit output JSON under ignored `out/mvp7_a2_real_sample_rerun/`.

## Risks

- Treating ignored/generated reports as source-of-truth instead of summarizing them in docs.
- Over-claiming precision when some samples lack per-turn evidence.
- Mistaking advisory context-quality findings for Validator or runtime failures.
- Accidentally proceeding into Candidate B without a separate decision brief.

## Drift Log

- 2026-06-20: Started MVP-7 Candidate A.2 real sample rerun after explicit approval. Scope is read-only audit plus documentation.
- 2026-06-20: Evidence pass found enough existing artifacts for a 12-sample cross-source rerun: Pilot-1 stabilized rerun turns, Pilot-1b coverage turns, and MVP-6 branch/checkpoint turns.
- 2026-06-20: Expanded the rerun to 14 samples by adding Pilot-1 Turns 1 and 5 to recheck previously suspected consequence false positives.
- 2026-06-20: Repo CLI audit completed for all 14 samples. Results: 3 PASS, 11 PASS_WITH_WARNINGS, 0 NEEDS_REVIEW, 0 FAIL; no `CONSEQUENCE_COST_NOT_RECORDED` findings.
- 2026-06-20: Verification passed: `git diff --check` reported no whitespace errors, and `gradlew.bat clean test --no-daemon` completed with `233 tests / 0 failures / 0 errors / 0 skipped`.
- 2026-06-20: Created docs-only commit `docs: rerun MVP-7 A.2 audit on real samples`; no push performed.
