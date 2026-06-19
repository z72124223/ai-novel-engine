# Plan

## Goal

Align the original external blueprint with the current verified repo state, then produce a conservative MVP-7 decision brief. This milestone is documentation-only and must not start MVP-7 implementation.

## Global Strategy

Treat `C:\Users\f7212\Downloads\AI_story_engine_blueprint.md` as design intent, not current operational truth. Record which parts remain useful, which parts are stale, and what decision should happen before any MVP-7 work begins.

## Non-Goals

- No MVP-7 implementation.
- No MVP-7 spec implementation.
- No Kotlin code edits.
- No migrations or schema edits.
- No runtime, protocol, Validator, state reducer, context budget, or transaction changes.
- No push, merge, tag creation, retagging, or branch deletion.
- No server, API, port, Web UI, Android UI, Rust, Docker, embedding, vector DB, or multi-agent runtime.

## Scope

- Add `docs/BLUEPRINT_ALIGNMENT.md`.
- Add `docs/MVP7_DECISION_BRIEF.md`.
- Update this `PLANS.md`.

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Current branch is synced to `origin/codex/mvp6-checkpoint-branching-spec` at `64439e10f951994eebd79614bb6fd229592f6e61` before this milestone.
- Remote is `https://github.com/z72124223/ai-novel-engine.git`.
- `mvp-6` points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- `ROADMAP.md` says MVP-7 is not approved and must first create a focused `docs/MVP7_*.md` spec if requested later.
- `VERIFY.md` says future MVP-7 work must define matching unit tests and CLI E2E before implementation.
- `docs/HANDOFF.md` records the stable branch, pushed remote, hard boundaries, and remote restore verification.
- The external blueprint states the same core safety principle: AI only produces JSON proposals, while Validator and `resolve-turn` govern SQLite writes.
- The external blueprint also contains stale operational state: it describes Pilot-1 as still around Turn 10/11 and MVP-6 as not started, while current repo docs and Git evidence show MVP-6 is complete and tagged.

## Assumptions

- The external blueprint is useful as a source of product intent and safety rules, even though its progress snapshot is stale.
- The safest next product step is a user-approved MVP-7 spec draft, not implementation.

## Open Questions

- Whether the user wants MVP-7 narrative quality work next, Pilot-2 playtesting, or another operational maintenance milestone.
- If MVP-7 is approved, which narrative quality failures should be measured first.

## Implementation Steps

- [x] Step 1: Read current Git state, control docs, and the external blueprint.
- [x] Step 2: Add blueprint alignment and MVP-7 decision docs.
- [x] Step 3: Run diff checks and tests, then commit only this documentation milestone if verification passes.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- Confirm `git status` and diff scope only include `PLANS.md`, `docs/BLUEPRINT_ALIGNMENT.md`, and `docs/MVP7_DECISION_BRIEF.md`.

## Risks

- Accidentally treating stale blueprint progress as current state.
- Accidentally implying MVP-7 approval by writing a decision brief.
- Over-specifying MVP-7 before the user approves the spec milestone.

## Drift Log

- 2026-06-20: Started blueprint alignment after user provided the original project blueprint from Downloads and approved a conservative follow-up.
- 2026-06-20: Added documentation-only alignment docs. `gradlew.bat clean test --no-daemon` passed after clearing a transient Gradle clean lock left by an earlier timeout.
