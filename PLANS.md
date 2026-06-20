# Plan

## Goal

Repair Pilot-2 Turn 4 context fitting by compacting final `ContextPacket` records so the exact Turn 4 input can fit under the existing `24576` UTF-8 byte hard max. Do not raise the limit, do not modify formal write paths, and do not resolve Turn 4.

## Global Strategy

Make the smallest deterministic final-context compaction changes in the prepare/context path. Prefer removing duplicated AI-facing text over removing semantic floor items. Verify on tests and a copied Pilot-2 DB before any formal Turn 4 continuation.

## Non-Goals

- No `24576` byte limit increase.
- No schema or migration changes.
- No Validator changes.
- No `stateVersion` or `contextHash` boundary changes.
- No `resolve-turn` changes.
- No `StateReducer` changes.
- No direct SQLite writes.
- No Turn 4 result generation.
- No `validate-result`.
- No `resolve-turn`.
- No Candidate B.
- No retagging `mvp-6`.
- No push in this milestone.

## Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/service/ContextPacketNormalizationServices.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/MemoryKnowledgeServices.kt` only if an extra deterministic finalizer reduction step is required
- `app/src/test/kotlin/com/localnovel/engine/*Context*Test.kt`
- `docs/PILOT2_TURN4_CONTEXT_BUDGET_FORENSICS.md` only if verification results need an addendum
- ignored copied DB / out files for E2E verification

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Working tree was clean before this milestone.
- Pilot-2 Turn 4 did not formally execute.
- Failed Turn 4 report: `candidateBytesBeforeTrimming=53956`, `candidateBytesAfterTrimming=26885`, `maxContextBytes=24576`, `bytesOverLimit=2309`.
- Formal Pilot-2 state remained `stateVersion=3`, `nextTurnNumber=4`, and `pendingContexts=0`.
- Largest retained sections after trimming are `relevantSettings`, `characterRuntimeStates`, `facts`, `characterKnowledge`, `playerInput`, and `availableTransitions`.
- `retrievedMemories` were already trimmed from 9 to 0.
- Transition data appears in both `availableTransitions` and `relevantSettings`.
- `prepare-turn` writes a pending context row; therefore Turn 4 prepare verification should use a copied DB unless the user separately approves mutating the formal Pilot-2 DB.

## Assumptions

- The best first repair is compacting duplicated transition/setting and runtime-state text, not changing semantic-floor selection.
- Existing `ContextPacketNormalizer` is the safest place for most compaction because it runs before final byte accounting and hash calculation.
- A copied DB E2E prepare is sufficient to prove the repair without advancing formal Pilot-2 state.

## Open Questions

- Whether same-scene "step back" wording should trigger all transition settings remains a future semantic-planner refinement; this milestone will avoid changing that behavior unless compaction alone is insufficient.

## Implementation Steps

- [x] Step 1: Read Turn 4 forensics artifacts, docs, source, and existing context tests.
- [x] Step 2: Update `PLANS.md` for the compaction repair milestone.
- [x] Step 3: Implement minimal final-context compaction.
- [x] Step 4: Add focused tests for transition/setting deduplication, runtime flag compaction, and knowledge source safety.
- [x] Step 5: Run `git diff --check`.
- [x] Step 6: Run `gradlew.bat clean test --no-daemon`.
- [x] Step 7: Run `gradlew.bat :app:installDist --no-daemon`.
- [x] Step 8: Verify Turn 4 `prepare-turn` on a copied DB, without result/validate/resolve.
- [ ] Step 9: Commit `fix: compact Pilot-2 context packets`.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- `gradlew.bat :app:installDist --no-daemon`
- copied DB Turn 4 `prepare-turn`
- confirm generated copied-DB `turn_004_context.json` is `<=24576` UTF-8 bytes
- confirm no Turn 4 result, validation, or resolve was run
- confirm formal `data/pilot2.db` still remains at `stateVersion=3`, `nextTurnNumber=4`, and `pendingContexts=0`

## Risks

- Over-compacting could remove information needed for safe manual result drafting. Mitigation: retain IDs, targets, player intent requirements, travel time, forced/optional status, world-event public limits, character goals, emotions, and allowed actions.
- Removing source references from knowledge claims could reduce traceability. Mitigation: only drop source IDs that are not present in the final context to avoid dangling references.
- Running `prepare-turn` on the formal DB would create a pending context. Mitigation: use a copied DB for verification.

## Drift Log

- 2026-06-21: Started Pilot-2 context compaction repair after user approved a small fitting repair and prohibited Turn 4 result/resolve, write-path changes, schema changes, and budget increase.
- 2026-06-21: Implemented final-context compaction in `ContextPacketNormalizer` for transition setting cards, location connection cards, world event cards, available transition preview text, character runtime flags, facts, and knowledge source references.
- 2026-06-21: Verified `git diff --check` passed with only CRLF normalization warnings.
- 2026-06-21: Verified `gradlew.bat clean test --no-daemon` passed.
- 2026-06-21: Verified `gradlew.bat :app:installDist --no-daemon` passed.
- 2026-06-21: On copied DB `data/pilot2-turn4-context-repair-e2e.db`, `explain-context-budget` for exact Turn 4 input changed from failing `26885/24576` to fitting `24065/24576`; `prepare-turn` wrote copied-DB context `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_004_context.json`.
- 2026-06-21: `audit-context` on copied-DB Turn 4 context passed with `actualBytes=24065/24576`, `declaredActualBytes=24065`, and `hashValid=true`.
- 2026-06-21: Formal `data/pilot2.db` remains unadvanced at `stateVersion=3`, `nextTurnNumber=4`; read-only evidence export reported `pendingContexts=0`.
