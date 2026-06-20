# Plan

## Goal

Run the formal Pilot-2 Turn 10 prepare checkpoint on `data/pilot2.db` after the approved context compaction repair. Produce and audit the official Turn 10 context only. Do not draft a result, do not validate a result, and do not resolve the turn in this milestone.

## Global Strategy

Use the formal CLI path for `prepare-turn` and `audit-context`, preserving `stateVersion`, `contextHash`, and pending-context semantics. Keep the milestone narrow: create the official pending context if prepare succeeds, verify the final context is within the `24576` UTF-8 byte limit, and stop before any narrative result work.

## Non-Goals

- No Turn 10 result generation.
- No `validate-result`.
- No `resolve-turn`.
- No `StateReducer` changes.
- No Validator changes.
- No `stateVersion` or `contextHash` logic changes.
- No schema or migration changes.
- No direct SQLite writes.
- No Candidate B.
- No `mvp-6` retagging.
- No push without explicit main-control or user approval, and no push outside the current `codex/mvp6-checkpoint-branching-spec` branch.
- No unrelated docs or code cleanup.

## Scope

- `PLANS.md`
- Formal Pilot-2 ignored output files under `out/pilot2/pilot2-qingli-herb-warehouse/turns/`
- Formal `data/pilot2.db` only through the approved CLI `prepare-turn` operation

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- The branch is synced with `origin/codex/mvp6-checkpoint-branching-spec` at `40e6fc6`.
- `mvp-6^{}` points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- Pilot-2 Turn 10 context compaction repair is committed and pushed.
- Copied-DB Turn 10 prepare succeeded after the repair with `actualBytes=23906/24576` and `hashValid=true`.
- Formal `data/pilot2.db` was previously left at `stateVersion=9`, `nextTurnNumber=10`, and `pendingContexts=0`.
- Turn 10 player input exists at `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_010_player_input.txt`.

## Assumptions

- The formal DB still matches the expected pre-Turn-10 state; this must be confirmed through `show-session` before prepare.
- The formal prepare result should fit similarly to the copied-DB result unless ignored artifacts or formal DB state have drifted.
- `prepare-turn` may legitimately create one pending context on the formal DB; this is allowed for this milestone.

## Open Questions

- If formal prepare fails despite copied-DB success, stop and investigate without result generation.
- If audit-context fails or bytes exceed `24576`, stop before result generation.

## Implementation Steps

- [x] Step 1: Read current repo state, Pilot-2 turn template, and existing plan.
- [x] Step 2: Update `PLANS.md` for the formal Turn 10 prepare checkpoint.
- [x] Step 3: Confirm formal session state with `show-session`.
- [x] Step 4: Run formal `prepare-turn` using the existing Turn 10 player input and save `turn_010_context.json`.
- [x] Step 5: Run `audit-context` and save `turn_010_context_audit.json`.
- [x] Step 6: Confirm no Turn 10 result, validate, or resolve was run.
- [x] Step 7: Run `git diff --check`.
- [x] Step 8: Run `gradlew.bat clean test --no-daemon`.
- [x] Step 9: Commit this checkpoint documentation after verification passed.

## Verification

- `show-session --db data/pilot2.db --session session-ea110b6c-7509-40e6-a786-99cf00d8fc66`
- `prepare-turn --db data/pilot2.db --session session-ea110b6c-7509-40e6-a786-99cf00d8fc66 --input-file out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_010_player_input.txt --out out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_010_context.json`
- `audit-context --file out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_010_context.json --out out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_010_context_audit.json`
- Confirm final context is at or below `24576` UTF-8 bytes.
- Confirm `hashValid=true`.
- Confirm no `turn_010_result.json` was generated.
- Confirm no `validate-result` or `resolve-turn` command was run.
- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- `git status`

## Risks

- Formal DB may differ from copied DB and still fail context fitting.
- Running formal `prepare-turn` creates a pending context; this is expected, but result generation must remain blocked until a separate milestone.
- If a stale `turn_010_context.json` exists from failed attempts, it must be treated carefully and overwritten only by the formal successful prepare for the same Turn 10 input.

## Drift Log

- 2026-06-21: Started high-autonomy stable-completion mode. Selected the smallest safe next milestone: formal Pilot-2 Turn 10 prepare checkpoint after the committed context compaction repair. The milestone explicitly excludes result generation, validation, and resolve.
- 2026-06-21: Formal Pilot-2 Turn 10 prepare checkpoint completed on `data/pilot2.db`: `turn_010_context.json` was generated with `contextId=ctx-b2359562-807e-4e97-bc4c-f808d2ad710b`, `baseStateVersion=9`, and `actualBytes=23906/24576`; `audit-context` reported `valid=true` and `hashValid=true`. The formal session remains at `stateVersion=9`, `nextTurnNumber=10`, with `pendingContextCount=1` as expected after prepare. No Turn 10 result, validate, or resolve was run.
- 2026-06-21: Re-verified the existing Turn 10 pending context under high-autonomy mode instead of re-running `prepare-turn`. `audit-context` again reported `valid=true`, `actualBytes=23906/24576`, `declaredActualBytes=23906`, and `hashValid=true`; `turn_010_result.json` is still absent.
