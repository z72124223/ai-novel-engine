# Plan

## Goal

Complete the approved Pilot-2 Turn 14 actor-specific knowledge fix by mechanically removing `claim-bd8d9c80-f41d-419a-8c87-1e10b0033f8b` only from `characterActions[0].usedKnowledgeClaimIds` in the existing ignored `turn_014_result.json`, then rerun formal validation and resolve. Do not rerun `prepare-turn`, do not rewrite narrative/result content, and do not touch runtime or schema.

## Global Strategy

Continue from the still-valid Turn 14 pending context. Make the single 01-approved JSON content fix, leaving `characterActions[2]` and all other content untouched. Then run JSON parse, `validate-result`, `resolve-turn`, post-resolve state/evidence checks, narrative audit, docs update, full verification, commit/push tracked docs only, and direct 03 notification. If retry resolve fails, stop and preserve evidence.

## Non-Goals

- No `prepare-turn`.
- No rewriting 01-provided narrative, `turnSummary`, `transitionProposal`, `stateDeltas`, or any other `characterActions`.
- No 02-authored probe narrative, decision content, or player input.
- No direct SQLite writes.
- No bypassing Validator, `contextHash`, `stateVersion`, or formal CLI.
- No schema or migration changes.
- No `stateVersion` or `contextHash` calculation changes.
- No `resolve-turn` orchestration or StateReducer changes.
- No Kotlin/runtime/test changes.
- No `mvp-6` retagging.
- No MVP-8 or unapproved Candidate B follow-up scope.
- No server/API/Docker/Android/Rust/embedding/vector DB.
- No deleting `data/`, `out/`, or `backups/`.
- No committing ignored `out/` or `data/` artifacts.

## Scope

Tracked files allowed:

- `PLANS.md`
- `docs/PILOT2_TURN14_RESOLVE_BLOCKER_REPORT.md`

Ignored artifacts to create/update but not commit:

- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_014_result.json`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_014_validate_actor_fix_output.txt`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_014_resolve_actor_fix_output.txt`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_014_evidence.json`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_014_audit_report.json`

Formal DB:

- `data/pilot2.db`

Session:

- `session-ea110b6c-7509-40e6-a786-99cf00d8fc66`

Required pending context:

- `contextId=ctx-e7f8fb1f-ae6f-4b80-bc5a-cf71b06f2afc`
- `contextHash=45f5f8114dc08edf267f631835d741cb72010f1605acf457c2d6139e18d1d284`
- `baseStateVersion=13`
- `turnNumber=14`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Current HEAD before this retry remains `4823518 docs: prepare Pilot-2 Turn 14 Codex probe context`.
- Working tree contains previous blocker docs changes: `PLANS.md` modified and `docs/PILOT2_TURN14_RESOLVE_BLOCKER_REPORT.md` untracked.
- `mvp-6` was confirmed as `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- Formal DB remains at `stateVersion=13`, `nextTurnNumber=14`, `pendingContexts=1` after previous failed resolves.
- Previous retry `resolve-turn` failed with `unknown_knowledge` for `characterActions[0]` using `claim-bd8d9c80-f41d-419a-8c87-1e10b0033f8b`.
- 01 now explicitly authorizes removing that claim only from `characterActions[0].usedKnowledgeClaimIds`.
- `characterActions[2]` also references the same claim and must remain unchanged.

## Assumptions

- Removing the invalid player actor claim should satisfy the actor-specific knowledge boundary error.
- A successful resolve should advance the formal session from `stateVersion=13`, `nextTurnNumber=14` to `stateVersion=14`, `nextTurnNumber=15`, and consume the pending context.
- If the transition applies, the current scene should become `scene-qingli-herb-awning` or an instance of that scene.

## Open Questions

- Whether `resolve-turn` will expose another knowledge/fact boundary issue after the player actor claim is removed.
- Whether narrative audit will emit advisory warnings after the transition succeeds.

## Implementation Steps

- [x] Step 1: Read current blocker state, `PLANS.md`, report, branch status, `mvp-6` tag, and current `characterActions` claim placement.
- [x] Step 2: Update `PLANS.md` for Turn 14 actor-specific knowledge fix.
- [x] Step 3: Set `characterActions[0].usedKnowledgeClaimIds` to `[]`, leaving `characterActions[2]` unchanged.
- [x] Step 4: Run JSON parse / structure check.
- [x] Step 5: Run `validate-result`; if it fails, preserve output and stop.
- [x] Step 6: If validate passes, run `resolve-turn`.
- [x] Step 7: If resolve passes, confirm state/evidence/current scene and pending context consumption.
- [x] Step 8: Run `audit-narrative-quality` with context/result/evidence.
- [x] Step 9: Update report docs and `PLANS.md` with final Turn 14 outcome.
- [x] Step 10: Run `git diff --check` and `gradlew.bat clean test --no-daemon`.
- [x] Step 11: Commit `docs: resolve Pilot-2 Turn 14 Codex probe` and push tracked docs only.
- [x] Step 12: Directly notify 03 read-only review thread.

## Verification

Required:

- `git status`
- JSON parse / structure check for `turn_014_result.json`
- `validate-result --db data/pilot2.db --context turn_014_context.json --result turn_014_result.json`
- `resolve-turn --db data/pilot2.db --session session-ea110b6c-7509-40e6-a786-99cf00d8fc66 --result turn_014_result.json`
- `show-session` and `export-session-evidence` after resolve PASS
- `audit-narrative-quality`
- `git diff --check`
- `gradlew.bat clean test --no-daemon`

Safety checks:

- Confirm no `prepare-turn` is run.
- Confirm result still uses context `ctx-e7f8fb1f-ae6f-4b80-bc5a-cf71b06f2afc` and hash `45f5f8114dc08edf267f631835d741cb72010f1605acf457c2d6139e18d1d284`.
- Confirm `characterActions[0].usedKnowledgeClaimIds=[]`.
- Confirm `characterActions[2].usedKnowledgeClaimIds` still includes `claim-bd8d9c80-f41d-419a-8c87-1e10b0033f8b`.
- Confirm ignored `out/` and `data/` artifacts are not staged.
- Confirm `mvp-6` remains unchanged.

## Risks

- Another actor-specific knowledge or fact boundary error may appear after the first one is fixed.
- `resolve-turn` could fail for transition-specific runtime checks even after validate PASS.
- Narrative audit may produce advisory warnings; those should be documented but not treated as Validator failures.
- Accidentally staging ignored `out/` or `data/` artifacts.

## Drift Log

- 2026-06-21: Started Turn 14 actor-specific knowledge fix after 03 confirmed the prior blocker and 01 authorized the single claim removal from `characterActions[0]`.
- 2026-06-21: Applied only the authorized claim removal. `characterActions[0].usedKnowledgeClaimIds=[]`; `characterActions[2]` still retains `claim-bd8d9c80-f41d-419a-8c87-1e10b0033f8b`.
- 2026-06-21: `validate-result` passed, `resolve-turn` passed, and formal session advanced to `stateVersion=14`, `nextTurnNumber=15`, `pendingContexts=0`.
- 2026-06-21: Current scene after resolve is `藥倉檐下查驗棚`; narrative audit passed with score 92 and one `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE` warning.
- 2026-06-21: `git diff --check` passed and `gradlew.bat clean test --no-daemon` passed (`BUILD SUCCESSFUL in 2m 23s`).
- 2026-06-21: Commit, push, and direct 03 notification are part of this handoff; if delivery fails, report to 01.
