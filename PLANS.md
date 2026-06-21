# Plan

## Goal

Create a docs-only Pilot-2 evidence checkpoint（證據檢查點） after Turns 1-12 so 01 can decide whether to continue Pilot-2, request real human Turn 13 input, approve a clearly labeled Codex-authored Turn 13, open a decision brief（決策簡報）, or stop for summary acceptance. This checkpoint must not approve Candidate B, Validator（驗證器） changes, runtime（執行期） changes, schema（資料結構） changes, or StateReducer（狀態化約器） changes.

## Global Strategy

Use only existing docs and ignored `out/pilot2` artifacts（產物）. Read the Turn 1-12 evidence（證據） and audit（稽核） reports, then add one focused checkpoint document under `docs/` and update `PLANS.md`. Do not rerun any Pilot-2 turn or any turn lifecycle CLI（命令列介面） command such as `prepare-turn`, `validate-result`, or `resolve-turn`.

## Non-Goals

- No Pilot-2 turn reruns.
- No `prepare-turn`, `validate-result`, or `resolve-turn`.
- No Kotlin changes.
- No test changes.
- No schema or migration changes.
- No Validator changes.
- No `stateVersion` or `contextHash` logic changes.
- No `resolve-turn` or `StateReducer` implementation changes.
- No direct SQLite writes.
- No Candidate B.
- No MVP-8.
- No server, API, Docker, Android, Rust, embedding, vector DB, or unrelated technology.
- No `mvp-6` retagging.
- No committing ignored `data/` or `out/` artifacts.

## Scope

- `PLANS.md`
- `docs/PILOT2_EVIDENCE_CHECKPOINT_TURNS_1_12.md`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Current HEAD before this milestone is `1085fdd docs: consolidate Pilot-2 Turn 12 transition finding`.
- Turn 1-12 player input, context, result, evidence, and audit report files exist under `out/pilot2/pilot2-qingli-herb-warehouse/turns/`.
- Turn 12 uses `turn_012_result_retry_01.json` as the resolved retry result.
- Turn 12 is explicitly Codex-authored player input and must not count as real human-player evidence.
- Turn 1-11 input files do not contain a Codex-authored marker; their human-source status is inherited from the prior Pilot-2 collection process, not independently proven by this checkpoint.
- Audit reports exist for Turns 1-12. Verdicts are PASS for Turns 1-4 and 7-12, PASS_WITH_WARNINGS for Turns 5-6.
- Recurring audit finding from Turns 5-12: active story threads exist at later turns, but no pending scheduled world event pressure is present.
- Final Turn 12 evidence shows `stateVersion=12`, `nextTurnNumber=13`, `currentSceneId=scene-qingli-herb-warehouse-perimeter`, and no formal scene transition.
- `docs/PILOT2_TURN12_TRANSITION_INTENT_FINDING.md` records the Turn 12 layer mismatch: context layer recognized exact intent, first transition result validate passed, resolve failed with `player_refused_transition`, retry resolved without transition.

## Assumptions

- No `ROADMAP.md` edit is required because this is a checkpoint report, not a new product milestone or roadmap status change.

## Open Questions

- Whether 01 wants a real human Turn 13 input before continuing Pilot-2.
- Whether 01 wants a separate transition/refusal heuristic decision brief before any more Codex-authored transition tests.

## Implementation Steps

- [x] Step 1: Read control docs, Pilot-2 docs, Turn 12 finding, and Turn 1-12 evidence/audit artifacts.
- [x] Step 2: Update `PLANS.md` for this docs-only evidence checkpoint milestone.
- [x] Step 3: Add `docs/PILOT2_EVIDENCE_CHECKPOINT_TURNS_1_12.md`.
- [x] Step 4: Inspect diff for scope drift and evidence accuracy.
- [x] Step 5: Run `git status`.
- [x] Step 6: Run `git diff --check`.
- [x] Step 7: Run `gradlew.bat clean test --no-daemon`.
- [x] Step 8: Commit `docs: checkpoint Pilot-2 evidence after Turn 12`.
- [x] Step 9: Push to `origin/codex/mvp6-checkpoint-branching-spec`.

## Verification

- `git status` -> only `PLANS.md` and `docs/PILOT2_EVIDENCE_CHECKPOINT_TURNS_1_12.md` changed before commit.
- `git diff --check` -> PASS, with only the existing CRLF working-copy warning for `PLANS.md`.
- `gradlew.bat clean test --no-daemon` -> PASS.
- Final `git status`

## Risks

- Overstating Turn 12 as real human evidence.
- Treating this checkpoint as approval to modify Validator, runtime, StateReducer, schema, or Candidate B.
- Committing ignored `out/` artifacts by mistake.
- Overclaiming Turn 1-11 source provenance beyond what local artifacts prove.

## Drift Log

- 2026-06-21: Started Pilot-2 evidence checkpoint after 01 reported 03 read-only PASS on the Turn 12 transition finding consolidation.
- 2026-06-21: Added `docs/PILOT2_EVIDENCE_CHECKPOINT_TURNS_1_12.md`, kept scope docs-only, and verified with `git diff --check` plus full Gradle tests before checkpoint commit/push.
