# Plan

## Goal

Create a docs-only authoring quality boundary note that preserves playtest learning about narrative prose, NPC consistency, review workflow, and anti-AI-writing guidance while clearly separating engine layer rules from content / setting pack specifics.

## Global Strategy

Treat 04 playtest learning as authoring evidence, not runtime approval. First record this milestone in `PLANS.md`, then add a focused `docs/AUTHORING_QUALITY_GUIDELINES.md` that can guide future engine prompts, review checklists, and setting-pack authoring without hardcoding Qingli content, Ai-chan details, or copyrighted external styles into engine core.

## Non-Goals

- No Kotlin source changes.
- No tests or runtime behavior changes.
- No schema or migration changes.
- No Validator, StateReducer, `resolve-turn`, `stateVersion`, or `contextHash` changes.
- No direct SQLite writes.
- No `mvp-6` retagging.
- No deletion of `data/`, `out/`, or backups.
- No committing ignored artifacts.
- No hardcoding Qingli, Ai-chan, specific NPCs, or copyrighted work elements as engine core.

## Scope

Tracked files allowed:

- `PLANS.md`
- `docs/AUTHORING_QUALITY_GUIDELINES.md`

Docs to inspect:

- `AGENTS.md`
- `PLANS.md`
- `ROADMAP.md`
- `VERIFY.md`
- `DONE_CRITERIA.md`
- relevant existing narrative-quality docs found by search, especially `docs/MVP7_NARRATIVE_QUALITY_SPEC.md` and Pilot-2 scenario/turn docs.

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Working tree was clean at milestone start.
- `ROADMAP.md` records MVP-7 narrative-quality work as completed read-only audit/spec work and does not approve Candidate B.
- `VERIFY.md` requires `git diff --check` and normally `gradlew.bat clean test --no-daemon`; the current user instruction allows skipping full Gradle for docs-only if explained.
- `DONE_CRITERIA.md` says documentation/control milestones need docs to match current behavior and `git diff --check` to pass.
- Search found `docs/MVP7_NARRATIVE_QUALITY_SPEC.md` as the existing narrative-quality spec.
- Search did not find an existing dedicated authoring quality guidelines file that separates engine layer rules from content / setting pack layer specifics.
- User instruction for this milestone explicitly requests docs-only authoring quality / style guide / review workflow work and forbids runtime/schema/Validator/StateReducer changes.

## Assumptions

- `docs/AUTHORING_QUALITY_GUIDELINES.md` is the right location because the requested content is cross-cutting authoring guidance, not a Pilot-only report.
- Full Gradle test is not necessary for this docs-only milestone if `git diff --check` passes and no code/runtime files are touched.

## Open Questions

- Whether future milestones should turn parts of the guideline into prompt templates, setting-pack schema fields, or read-only audit checks. This milestone does not decide that.

## Implementation Steps

- [x] Step 1: Read AGENTS, PLANS, ROADMAP, VERIFY, DONE_CRITERIA, and search existing docs for authoring/narrative-quality coverage.
- [x] Step 2: Update `PLANS.md` for this docs-only authoring quality boundary milestone.
- [x] Step 3: Add `docs/AUTHORING_QUALITY_GUIDELINES.md`.
- [x] Step 4: Run `git diff --check` and inspect diff scope.
- [x] Step 5: Commit `docs: add authoring quality guidelines` and push tracked docs only.
- [x] Step 6: Directly notify 03 read-only review thread.

## Verification

Required:

- `git status`
- `git diff --check`
- `git diff --stat`

Not planned:

- Full Gradle test, because this milestone only changes documentation and `PLANS.md`; no Kotlin, tests, schema, runtime, or CLI behavior is touched. If the final diff expands beyond docs, rerun full tests.

Safety checks:

- Confirm only `PLANS.md` and `docs/AUTHORING_QUALITY_GUIDELINES.md` are staged.
- Confirm no ignored `data/` or `out/` artifacts are staged.
- Confirm `mvp-6` remains unchanged.

## Risks

- Accidentally turning playtest-specific Qingli/Ai-chan/NPC details into engine requirements.
- Accidentally implying Candidate B or runtime quality gates are approved.
- Overfitting anti-AI-writing guidance into brittle phrase bans instead of review principles.
- Copying or imitating protected external story style too closely.

## Drift Log

- 2026-06-21: Started docs-only authoring quality / style guide / review workflow milestone after 01 classified 04 playtest learning as docs-first evidence, not runtime approval.
- 2026-06-21: Added `docs/AUTHORING_QUALITY_GUIDELINES.md` with engine/content layer separation, anti-AI-writing guidance, NPC consistency checks, review loop, and copyright/style safety. No runtime or schema files touched.
- 2026-06-21: `git diff --check` passed with only Windows line-ending warnings. Full Gradle was intentionally skipped because this milestone changed only docs and `PLANS.md`.
