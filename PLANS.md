# Plan

## Goal

Implement MVP-7 Candidate A: a read-only narrative quality audit report. The tool analyzes existing context/result JSON and optionally a session evidence JSON file, then emits a stable report without writing SQLite or changing turn resolution.

## Global Strategy

Add a standalone audit surface. Keep it separate from `resolve-turn`, `StateReducer`, `AiTurnResultValidator`, migrations, and repository writes. The service should consume already-exported JSON data and produce report JSON only.

## Non-Goals

- No SQLite writes.
- No schema or migration edits.
- No `resolve-turn` behavior changes.
- No `StateReducer` changes.
- No `AiTurnResultValidator` safety-boundary changes.
- No `stateVersion` or `contextHash` changes.
- No story rewriting.
- No MVP-7 Candidate B/C/D.
- No server, API, port, Web UI, Android UI, OpenAI API, Rust, Docker, embedding, vector DB, or multi-agent runtime.
- No push, merge, tag creation, retagging, or branch deletion.

## Scope

- Add narrative quality report models.
- Add `NarrativeQualityAuditService`.
- Add CLI command `audit-narrative-quality`.
- Add tests for the read-only audit.
- Update README/VERIFY and focused MVP-7 docs as needed.
- Update this `PLANS.md`.

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- The branch is synced with `origin/codex/mvp6-checkpoint-branching-spec` at `fda77df` before this milestone.
- `mvp-6` points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- `ROADMAP.md` lists `MVP-7 Candidate A narrative quality audit report` as a recommended candidate after explicit approval.
- The user explicitly approved this Candidate A milestone.
- `docs/MVP7_NARRATIVE_QUALITY_SPEC.md` defines Candidate A as a read-only analyzer for context/result pairs.
- Existing CLI read-only commands use `writeJson(Path.of(out), report)` and print a short stdout summary.
- Existing JSON support is `JsonSupport.json` with `ignoreUnknownKeys = true`.
- `samples/sample_context_packet.json` and `samples/sample_ai_turn_result.json` are available as smoke-test inputs.

## Assumptions

- The first implementation can use deterministic heuristic rules and stable report fields, without claiming literary judgment is fully automated.
- Optional pilot evidence support can be limited to reading an exported JSON file for repeated narrative/summary signals.

## Open Questions

- Which audit warnings should become hard Validator failures in a later milestone, if any.
- Whether future Pilot scoring should use this report directly or remain manual.

## Implementation Steps

- [x] Step 1: Read current control docs, MVP-7 spec, decision brief, README, AGENTS, samples, source, and tests.
- [x] Step 2: Output read-only plan before editing code.
- [x] Step 3: Add report models and read-only audit service.
- [x] Step 4: Add CLI command and docs.
- [x] Step 5: Add tests and sample CLI verification.
- [x] Step 6: Run full verification, commit if all pass.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- `gradlew.bat :app:installDist`
- `gradlew.bat :app:run --args="audit-narrative-quality --context samples/sample_context_packet.json --result samples/sample_ai_turn_result.json --out out/mvp7_narrative_quality_report.json"`
- Installed distribution CLI smoke test for `audit-narrative-quality`.

## Risks

- Heuristic quality findings may be noisy; report should use warnings and scores, not block story resolution.
- Scope could drift into Validator enforcement; keep Candidate A read-only.
- Adding a CLI command requires docs and installDist verification.

## Drift Log

- 2026-06-20: Started MVP-7 Candidate A after explicit approval. Read-only plan confirms no DB write path and no resolve-turn changes.
- 2026-06-20: Added read-only audit service/CLI/tests/docs and verified tests, installDist, repo CLI smoke, and distribution CLI smoke.
