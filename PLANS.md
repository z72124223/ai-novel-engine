# Plan

## Goal

Refresh project control documents so they match the verified post-MVP-6 state: MVP-6 is complete, installDist packaging is repaired, local bundle backup is complete, private remote push is complete, remote restore verification is complete, and MVP-7 remains not approved.

## Global Strategy

This is a documentation/control milestone. Update only project-control documents that currently drift from confirmed Git and verification evidence. Avoid runtime, schema, protocol, validator, and test changes.

## Non-Goals

- No MVP-7.
- No Kotlin code edits.
- No migrations or schema edits.
- No runtime, protocol, Validator, state reducer, context budget, or transaction changes.
- No push, merge, tag creation, retagging, or branch deletion in this milestone.
- No server, API, port, Web UI, Android UI, Rust, Docker, embedding, vector DB, or multi-agent runtime.

## Scope

- Update `ROADMAP.md`.
- Update `docs/ROADMAP.md`.
- Update `docs/HANDOFF.md`.
- Update this `PLANS.md`.

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Current branch is synced to `origin/codex/mvp6-checkpoint-branching-spec` at `43e0169882f449bb2b0c0f809b5bfdee8ca389de` before this refresh.
- Remote is `https://github.com/z72124223/ai-novel-engine.git`.
- `mvp-6` points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- `docs/PROJECT_COMPLETION_AUDIT.md` reports `PASS_WITH_CONTROL_DOC_DRIFT`.
- Remote restore clone and `gradlew.bat clean test --no-daemon` passed with 220 tests, 0 failures, 0 errors, 0 skipped.
- Root `ROADMAP.md` still described MVP-6 as current and MVP-6 CLI acceptance as next.
- `docs/ROADMAP.md` still described installDist as later non-blocking technical debt.
- `docs/HANDOFF.md` still referenced the pre-audit stable HEAD.

## Assumptions

- The next safe milestone after this refresh should be chosen explicitly by the user, not inferred as MVP-7.
- Avoiding a mutable current HEAD hash in `docs/HANDOFF.md` will reduce future drift; `mvp-6` remains the fixed release anchor.

## Open Questions

- Whether to later define an MVP-7 spec.
- Whether to later push or prune historical local `feat/*` and `fix/*` branches.
- Whether to configure GitHub branch protection on the private repo.

## Implementation Steps

- [x] Step 1: Read current Git state and relevant control docs.
- [x] Step 2: Update control docs to reflect post-MVP-6 verified state.
- [ ] Step 3: Run diff checks and tests, then commit only this documentation/control milestone if verification passes.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- Confirm `git status` and diff scope only include `ROADMAP.md`, `docs/ROADMAP.md`, `docs/HANDOFF.md`, and `PLANS.md`.

## Risks

- Roadmap wording can drift again after the next milestone; keep `docs/HANDOFF.md` focused on stable anchors and commands instead of mutable HEAD hashes.
- This milestone intentionally does not push; user approval is required after commit.

## Drift Log

- 2026-06-20: Started control-doc refresh after project completion audit identified stale roadmap and handoff state.
