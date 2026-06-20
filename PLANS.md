# Plan

## Goal

Harden the MVP-7 audit report JSON verification instructions so Windows PowerShell 5.1 users validate audit output as UTF-8 JSON and do not misdiagnose valid reports as parse failures.

## Global Strategy

Keep this milestone documentation-only. Use the completed JSON robustness investigation as evidence, update verification guidance, and avoid Kotlin, tests, audit rules, schema, Validator, `stateVersion`, `contextHash`, `resolve-turn`, `StateReducer`, or SQLite writes.

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
- No retagging `mvp-6`.
- No push in this milestone.

## Scope

- `PLANS.md`
- `VERIFY.md`
- `docs/MVP7_AUDIT_JSON_ROBUSTNESS_INVESTIGATION.md`
- `docs/MVP7_A2_REAL_SAMPLE_RERUN_REPORT.md`

`README.md` is out of scope unless verification guidance there proves necessary.

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Local `HEAD` and `origin/codex/mvp6-checkpoint-branching-spec` both point to `fc244e1e168d38746b8c2b0c8425b6f296ccc8fb`.
- `mvp-6` remains the old stable tag and points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- The previous JSON robustness investigation concluded that suspected audit reports were valid UTF-8 JSON when read as UTF-8.
- The apparent parse failures came from PowerShell 5.1 default text decoding in a Big5-oriented environment, not from malformed Kotlin audit output.
- Correct PowerShell verification uses `Get-Content -Encoding UTF8 -Raw <path> | ConvertFrom-Json`.
- Node verification with `JSON.parse(fs.readFileSync(path, "utf8"))` also passed.
- `VERIFY.md` currently documents `audit-narrative-quality` smoke commands but does not explicitly require UTF-8 when parsing report JSON.
- `docs/MVP7_A2_REAL_SAMPLE_RERUN_REPORT.md` still records the earlier suspected malformed-output explanation and needs a historical correction note.

## Assumptions

- Updating `VERIFY.md` and the two MVP-7 reports is enough to prevent the same false parse-failure diagnosis.
- `README.md` does not need a broad change for this narrow verification hardening.

## Open Questions

- None blocking this milestone.

## Implementation Steps

- [x] Step 1: Read current Git state and relevant docs.
- [x] Step 2: Update `PLANS.md` with this documentation-only milestone.
- [x] Step 3: Update `VERIFY.md` with explicit UTF-8 JSON verification commands and a PowerShell 5.1 warning.
- [x] Step 4: Add small follow-up notes to the MVP-7 JSON robustness and A.2 real sample rerun reports.
- [x] Step 5: Verify with `git diff --check`.
- [x] Step 6: Verify with `gradlew.bat clean test --no-daemon`.
- [x] Step 7: Commit `docs: harden audit JSON verification instructions`.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- Manual review that tracked changes are documentation only and do not alter runtime behavior.

## Risks

- Over-editing historical reports and losing useful chronology.
- Accidentally implying a Kotlin bug exists when evidence says this is a verification encoding issue.
- Forgetting to warn against bare `Get-Content -Raw` on PowerShell 5.1.

## Drift Log

- 2026-06-20: Started MVP-7 audit JSON verification docs hardening after explicit approval. Scope is documentation-only.
- 2026-06-20: Updated `VERIFY.md` to require explicit UTF-8 JSON parsing for audit reports and annotated the MVP-7 reports with the corrected PowerShell 5.1 root cause.
- 2026-06-20: Verification passed. `git diff --check` reported no whitespace errors. `gradlew.bat clean test --no-daemon` exited `0` with Temurin JDK 17.0.19 and `233 tests / 0 failures / 0 errors / 0 skipped`.
- 2026-06-20: Created docs-only commit `docs: harden audit JSON verification instructions`; no push performed.
