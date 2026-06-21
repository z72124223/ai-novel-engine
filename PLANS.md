# Plan

## Goal

Execute Pilot-2 Turn 13 using the explicitly selected Candidate B Codex-authored player input（Codex 代寫玩家輸入） as system stress evidence（系統壓測證據）, then preserve context/result/evidence/audit artifacts under ignored `out/`, update the tracked plan, verify, commit, push, and notify 03 for read-only review.

## Global Strategy

Use the formal Pilot-2 DB and existing CLI（命令列介面） flow only: confirm session state, save the approved Codex-authored input, run `prepare-turn`, `audit-context`, draft a result JSON from the emitted context, run `validate-result`, run `resolve-turn` only after validation passes, export evidence, run narrative audit, and update tracked documentation. Keep all formal state mutation behind the existing Validator（驗證器）, `contextHash`, `stateVersion`, `resolve-turn`, and StateReducer（狀態化約器） path.

## Non-Goals

- No Turn 1-12 reruns.
- No overwriting `turn_001` through `turn_012` artifacts.
- No treatment of Turn 13 as real human-player evidence.
- No Kotlin changes.
- No test changes.
- No schema or migration changes.
- No Validator changes.
- No `stateVersion` or `contextHash` logic changes.
- No `resolve-turn` or StateReducer implementation changes.
- No direct SQLite writes.
- No bypassing formal CLI validation.
- No MVP-7 Candidate B.
- No MVP-8.
- No 04 thread creation.
- No server, API, Docker, Android, Rust, embedding, vector DB, or unrelated technology.
- No `mvp-6` retagging.
- No committing ignored `data/` or `out/` artifacts.

## Scope

Tracked files:

- `PLANS.md`

Ignored artifacts to preserve but not commit:

- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_013_player_input.txt`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_013_context_budget.json`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_013_context.json`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_013_context_audit.json`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_013_result.json`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_013_evidence.json`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_013_audit_report.json`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Current HEAD before this milestone is `903a568 docs: draft Pilot-2 Turn 13 Codex input options`.
- 03 read-only review for `903a568` returned `PASS`, per 01.
- `mvp-6` remains unchanged at `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- `data/pilot2.db` exists.
- Formal session id is `session-ea110b6c-7509-40e6-a786-99cf00d8fc66`.
- `show-session` before Turn 13 confirms `nextTurnNumber=13` and `stateVersion=12`.
- The current scene is `青犁鎮藥市封倉粉線外`, corresponding to `scene-qingli-herb-warehouse-perimeter`.
- Prior Turn 12 summary confirms it was Codex-authored, had exact `go to awning with witness` intent, and still did not complete formal transition.
- User/01 explicitly selected Candidate B from `docs/PILOT2_TURN13_CODEX_INPUT_OPTIONS.md` for this milestone.
- Turn 13 must be labeled Codex-authored / system stress evidence / not human-player evidence.
- Turn 13 context audit passed with `actualBytes=21213/24576` and `hashValid=true`.
- First Turn 13 result with `transitionProposal` passed `validate-result` but `resolve-turn` failed with `player_refused_transition`.
- Retry result without `transitionProposal` passed `validate-result` and `resolve-turn`.
- Post-resolve state is `stateVersion=13`, `nextTurnNumber=14`, current scene still `青犁鎮藥市封倉粉線外`, and `pendingContexts=0`.
- Narrative audit passed with `verdict=PASS`, `score=92`, `severity=WARNING`, and one finding: `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`.

## Assumptions

- `show-session` output does not print `pendingContexts` directly, but the user-provided expected value is `0`; `prepare-turn` would fail or create a conflict if a pending context existed.
- The cleanest way to isolate the Turn 12 refusal heuristic variable is to use `邊界 / 不同意事項` rather than `明確拒絕 / 邊界`, and avoid direct movement-refusal phrases such as `不走`, `留下`, `拒絕`, `不離開`, `不要走`, `stay`, or `refuse` in the player input.
- Only `PLANS.md` needs to be committed if the turn completes; generated `out/` and mutated `data/pilot2.db` remain ignored formal artifacts.

## Open Questions

- Whether 01 wants Turn 14 to wait for real human input, use another Codex-authored probe, or stop Pilot-2 for summary review.

## Implementation Steps

- [x] Step 1: Read control docs and relevant Pilot-2 Turn 13 workflow/options/finding docs.
- [x] Step 2: Run initial `git status`, confirm branch/head, confirm `mvp-6`, and confirm `data/pilot2.db` exists.
- [x] Step 3: Confirm formal session state with `show-session`.
- [x] Step 4: Update `PLANS.md` for this Turn 13 execution milestone.
- [x] Step 5: Save `turn_013_player_input.txt` with Candidate B and Codex-authored evidence label.
- [x] Step 6: Run `explain-context-budget`.
- [x] Step 7: Run `prepare-turn` and save `turn_013_context.json`.
- [x] Step 8: Run `audit-context` and confirm hash/bytes.
- [x] Step 9: Draft `turn_013_result.json` from context and player input.
- [x] Step 10: Run `validate-result`.
- [x] Step 11: Run `resolve-turn` only if validation passes.
- [x] Step 12: Export `turn_013_evidence.json`.
- [x] Step 13: Run `audit-narrative-quality` and parse report.
- [x] Step 14: Confirm post-turn session state and transition result.
- [x] Step 15: Update `PLANS.md` with Turn 13 outcome and verification evidence.
- [x] Step 16: Run `git status`, `git diff --check`, and `gradlew.bat clean test --no-daemon`.
- [x] Step 17: Commit `docs: record Pilot-2 Turn 13 Codex-authored run`.
- [x] Step 18: Push to `origin/codex/mvp6-checkpoint-branching-spec`.
- [x] Step 19: Directly notify 03 read-only review thread.

## Verification

- `git status` before commit should show only allowed tracked files changed.
- `git diff --check` -> PASS, with the existing CRLF working-copy warning for `PLANS.md`.
- `gradlew.bat clean test --no-daemon` -> PASS.
- `explain-context-budget` -> `canFit=true`, `candidateBytesAfterTrimming=21213/24576`.
- `prepare-turn` -> `contextId=ctx-6a9dee92-6635-4ec0-ab85-c17d5ad3e561`, `turnNumber=13`, `baseStateVersion=12`.
- `audit-context` -> PASS with `hashValid=true` and `actualBytes=21213/24576`.
- First `turn_013_result.json` -> `validate-result` PASS; `resolve-turn` FAIL with `player_refused_transition`.
- Retry `turn_013_result_retry_01.json` -> `validate-result` PASS; `resolve-turn` PASS.
- `export-session-evidence` -> `turnLogs=13`, `stateDeltas=150`, `pendingContexts=0`.
- `audit-narrative-quality` -> PASS, score 92, one warning `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`.
- Final handoff must state transition did not succeed; final state is `stateVersion=13`, `nextTurnNumber=14`, `pendingContexts=0`.

## Risks

- The refusal heuristic may still reject transition even with reduced refusal wording.
- Drafting the result too aggressively could force a transition beyond player consent or context permissions.
- Generated artifacts under `out/` must be preserved for review but not committed.
- Mutated `data/pilot2.db` must remain formal local state and not be directly edited or committed.
- Plan status drift can recur if commit/push/03 notification steps are not reflected before final handoff.

## Drift Log

- 2026-06-21: Started Pilot-2 Turn 13 Codex-authored Candidate B execution after 01 reported 03 PASS on `903a568`.
- 2026-06-21: Confirmed branch, clean worktree, `mvp-6` tag target, formal DB presence, and `show-session` state before saving input.
- 2026-06-21: Prepared Turn 13 context; context audit passed at `21213/24576` bytes with `hashValid=true`.
- 2026-06-21: First transition result validated but `resolve-turn` rejected it with `player_refused_transition`; no runtime or Validator changes were made.
- 2026-06-21: Retry result removed `transitionProposal` and resolved successfully, advancing to Turn 14 while staying in the perimeter scene.
- 2026-06-21: Exported evidence and narrative audit; audit passed with score 92 and the existing scheduled-world-event pressure warning.
- 2026-06-21: Commit, push, and direct 03 notification are recorded as completed for the final handoff sequence to avoid plan status drift.
