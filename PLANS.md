# Plan

## Goal

Create a small handoff index for future Codex windows so the project can resume from the current stable point without rediscovering branch, tag, remote, verification, and safety-boundary state.

## Global Strategy

Keep this milestone documentation-only. Record only facts confirmed from Git state, existing control docs, remote verification, and test output. Do not start MVP-7, do not alter runtime behavior, and do not change any schema, validator, `stateVersion`, or `contextHash` boundary.

## Non-Goals

- No product features.
- No Kotlin code changes.
- No migration changes.
- No remote changes, pushes, branch deletion, tag creation, or retagging.
- No server, API, Web UI, Android UI, Rust, Docker, embedding, vector DB, or multi-agent runtime.
- No edits to Pilot story state, DB files, generated output, local backups, build cache, or temp JDK.

## Scope

- Add `docs/HANDOFF.md`.
- Update this `PLANS.md` to reflect the current documentation milestone.

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Current branch tracks `origin/codex/mvp6-checkpoint-branching-spec`.
- Current remote is `https://github.com/z72124223/ai-novel-engine.git`.
- Current HEAD is `514120afdc0b87b2e5ff6bdbe01701e6a288e491`.
- `mvp-6` points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- Remote restore clone from GitHub succeeded on 2026-06-20.
- Remote restore clone `gradlew.bat clean test --no-daemon` succeeded with 220 tests, 0 failures, 0 errors, 0 skipped.
- `data/`, `out/`, `backups/`, and build/cache directories are ignored and were not pushed as tracked files.

## Assumptions

- Future work will benefit from a single, short handoff entry point before reading the larger roadmap and specs.
- The next product milestone is not yet approved; any MVP-7 work must begin with an explicit spec and user approval.

## Open Questions

- Whether to later refresh `ROADMAP.md` so its "Current Rule" reflects the post-MVP-6 remote backup state.
- Whether to push additional historical feature/fix branches beyond the two stable branches already pushed.

## Implementation Steps

- [x] Step 1: Gather current Git, remote, tag, and verification facts.
- [x] Step 2: Add `docs/HANDOFF.md` with stable-point, safety-boundary, verification, and next-session guidance.
- [ ] Step 3: Verify documentation diff and decide whether to commit this documentation-only milestone.

## Verification

- Run `git diff --check`.
- Run `git status`.
- For this documentation-only milestone, no runtime test is required unless the user requests a commit under the global done criteria.

## Risks

- The handoff could become stale if future milestones change branch, remote, tags, or verification status.
- `ROADMAP.md` still contains some milestone wording from before the MVP-6 release seal; that is documented as an open question rather than silently changed in this milestone.

## Drift Log

- 2026-06-20: Started documentation-only handoff milestone after remote push and remote restore verification completed.
