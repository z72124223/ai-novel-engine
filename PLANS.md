# Plan

## Goal

Draft the MVP-7 narrative quality specification as a documentation-only milestone. Define what narrative quality means for this engine, what MVP-7 must not do, what can be verified, and what decision is required before any implementation.

## Global Strategy

Keep this milestone as a spec draft only. Use current repo control docs and the blueprint alignment document as evidence. Do not modify Kotlin code, migrations, runtime behavior, CLI commands, Validator, `stateVersion`, or `contextHash`.

## Non-Goals

- No MVP-7 implementation.
- No Kotlin code edits.
- No schema or migration edits.
- No new CLI commands.
- No Validator, `stateVersion`, `contextHash`, or transaction changes.
- No web server, API, port, Web UI, Android UI, OpenAI API, Rust, Docker, embedding, vector DB, or multi-agent runtime.
- No push, merge, tag creation, retagging, or branch deletion.

## Scope

- Add `docs/MVP7_NARRATIVE_QUALITY_SPEC.md`.
- Update `ROADMAP.md` only to reflect that the MVP-7 spec draft milestone was selected and remains documentation-only.
- Update this `PLANS.md`.

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- The branch is synced with `origin/codex/mvp6-checkpoint-branching-spec` at `a35f5e9` before this milestone.
- `mvp-6` points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- `ROADMAP.md` says MVP-7 implementation is not approved and must start with a focused `docs/MVP7_*.md` spec if requested later.
- `VERIFY.md` says MVP-7 must define matching tests and CLI E2E before implementation.
- `DONE_CRITERIA.md` requires `git diff --check` and `gradlew.bat clean test --no-daemon` before commit.
- `docs/BLUEPRINT_ALIGNMENT.md` says the external blueprint is useful as design intent, but stale as current state.
- `docs/MVP7_DECISION_BRIEF.md` says MVP-7 is ready for a spec draft, not implementation.
- `docs/PROJECT_STRUCTURE_REPORT.md` is a historical snapshot and contains stale MVP-6 status.

## Assumptions

- The most useful MVP-7 spec is conservative: define measurable narrative quality before adding enforcement.
- A future MVP-7 implementation should likely start with audit/report behavior before hard Validator failures, but that remains a decision for the spec and user approval.

## Open Questions

- Whether MVP-7 implementation should begin after this spec draft.
- Which quality failures the user wants to prioritize first during real play.
- Whether future enforcement should be hard rejection, warning-only audit, or staged.

## Implementation Steps

- [x] Step 1: Read current control docs, blueprint alignment, MVP-7 decision brief, project report, MVP-6 acceptance, README, and AGENTS.
- [x] Step 2: Output read-only analysis before editing.
- [x] Step 3: Add MVP-7 narrative quality spec and minimal roadmap update.
- [x] Step 4: Run diff checks and tests, then commit only this documentation milestone if verification passes.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- Confirm `git status` and diff scope only include `PLANS.md`, `ROADMAP.md`, and `docs/MVP7_NARRATIVE_QUALITY_SPEC.md`.

## Risks

- Accidentally implying MVP-7 implementation approval.
- Turning subjective style preferences into impossible automatic requirements.
- Weakening safety boundaries in the name of better prose.
- Overfitting the spec to one Pilot story instead of the engine.

## Drift Log

- 2026-06-20: Started documentation-only MVP-7 narrative quality spec draft after explicit user approval.
- 2026-06-20: Added `docs/MVP7_NARRATIVE_QUALITY_SPEC.md` as a Chinese-first spec and updated `ROADMAP.md` without approving implementation.
- 2026-06-20: `git diff --check` passed and `gradlew.bat clean test --no-daemon` passed.
