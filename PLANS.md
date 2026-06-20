# Plan

## Goal

Repair Pilot-2 Turn 10 context fitting by compacting final `ContextPacket` records so the exact Turn 10 input can fit under the existing `24576` UTF-8 byte hard max. Do not raise the limit, do not modify formal write paths, and do not resolve Turn 10.

## Global Strategy

Make the smallest deterministic final-context compaction changes in the prepare/context path. Preserve semantic-floor coverage, player input, safety fields, character autonomy, and transition constraints. Verify the exact Turn 10 input on a copied DB before any formal Turn 10 continuation.

## Non-Goals

- No `24576` byte limit increase.
- No schema or migration changes.
- No Validator changes.
- No `stateVersion` or `contextHash` boundary changes.
- No `resolve-turn` changes.
- No `StateReducer` changes.
- No direct SQLite writes.
- No formal Turn 10 result generation.
- No `validate-result`.
- No `resolve-turn`.
- No Candidate B.
- No retagging `mvp-6`.
- No push in this milestone.

## Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/service/ContextPacketNormalizationServices.kt`
- `app/src/test/kotlin/com/localnovel/engine/Pilot1ContextMetadataNormalizationTest.kt`
- ignored copied DB / out files for E2E verification

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Working tree was clean before this milestone.
- Pilot-2 Turn 10 did not formally execute.
- Failed Turn 10 report: `candidateBytesBeforeTrimming=93794`, `candidateBytesAfterTrimming=26904`, `maxContextBytes=24576`, `bytesOverLimit=2328`, `mandatoryBytes=10503`.
- Formal Pilot-2 state remained `stateVersion=9`, `nextTurnNumber=10`, and `pendingContexts=0`.
- Semantic coverage was `PASS`; the failure is byte pressure, not missing semantic requirements.
- Largest retained sections after trimming are `relevantSettings=3034`, `playerInput=2949`, `characterRuntimeStates=2918`, `facts=2731`, `retrievedMemories=2339`, `characterKnowledge=1676`, `availableTransitions=1183`, `openTasks=1119`, and `contextBudgetMetadata=1060`.
- Turn 10 differs from Turn 4 because pressure is more distributed and includes retained protected memories, a larger player input, accumulated facts, and a larger open task.
- `ContextPacketNormalizer` is the existing final-context compaction surface.
- `MultiTurnRuntime.prepareTurn` saves a pending context only after `ContextPacketFinalizer` succeeds.
- `MultiTurnRuntime.resolveTurn` and `StateReducer` are separate write paths and are out of scope.

## Assumptions

- The safest first repair is stronger compact views for open tasks, retrieved memories, facts/knowledge, transition setting references, and budget metadata.
- Compaction should preserve IDs and short summaries rather than changing semantic-floor selection.
- Copied-DB `prepare-turn` is sufficient to verify fitting without advancing formal `data/pilot2.db`.

## Open Questions

- If final context still cannot fit after compact views, the next decision would be whether to refine transition intent matching. That is not part of this first repair unless required.
- Whether future Pilot-2 turns will need scheduled world event pressure repair remains separate from this byte-fit milestone.

## Implementation Steps

- [x] Step 1: Read Turn 10 forensics artifacts, Turn 4 comparison, docs, source, and context tests.
- [x] Step 2: Output the implementation plan before code changes.
- [x] Step 3: Update `PLANS.md` for the Turn 10 compaction repair milestone.
- [x] Step 4: Implement minimal final-context compaction.
- [x] Step 5: Add or update focused tests for open task, retrieved memory, and Turn-10-like fitting pressure.
- [x] Step 6: Run `git diff --check`.
- [x] Step 7: Run `gradlew.bat clean test --no-daemon`.
- [x] Step 8: Run `gradlew.bat :app:installDist --no-daemon`.
- [x] Step 9: Verify exact Turn 10 `prepare-turn` on a copied DB only, without result/validate/resolve.
- [x] Step 10: Commit `fix: compact Pilot-2 Turn 10 context`.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- `gradlew.bat :app:installDist --no-daemon`
- copied DB Turn 10 `prepare-turn`
- confirm copied-DB generated `turn_010_context.json` is `<=24576` UTF-8 bytes
- confirm no Turn 10 result, validation, or resolve was run
- confirm formal `data/pilot2.db` still remains at `stateVersion=9`, `nextTurnNumber=10`, and `pendingContexts=0`

## Risks

- Over-compacting memories could make real recall too thin. Mitigation: retain `memoryId`, owner, type, summary, score, and one or two reasons.
- Over-compacting tasks could hide the active procedure. Mitigation: retain task id, status, title, current objective, and most relevant next options.
- Removing transition data could break formal transition reasoning. Mitigation: retain transition IDs, target scene/location, required intent, travel time, forced flag, and unmet prerequisites.
- Running `prepare-turn` on the formal DB would create a pending context. Mitigation: use a copied DB for verification.

## Drift Log

- 2026-06-21: Started Pilot-2 Turn 10 context compaction repair after user approved a small fitting repair and prohibited Turn 10 result/validate/resolve, write-path changes, schema changes, and budget increase.
- 2026-06-21: Confirmed Turn 10 needs compact representation rather than weaker semantic coverage: `semanticCoverage=PASS` but final candidate is `26904/24576`.
- 2026-06-21: Implemented compact final-context records for open tasks, retrieved memories, character states, facts, knowledge claims, world rules, recent events, and transition/location setting cards. Preserved player input, semantic-floor surfaces, IDs, available transitions, continuity constraints, and capability boundaries.
- 2026-06-21: Initial copied-DB prepare exposed a hash/accounting mismatch after truncating reason codes in normalizer. Reverted reason-code truncation to preserve existing `contextHash` convergence logic; audit then passed with `actualBytes=23906/24576` and `hashValid=true`.
- 2026-06-21: Verification passed: `git diff --check`, `gradlew.bat clean test --no-daemon` (`239 tests / 0 failures / 0 errors / 0 skipped`), `gradlew.bat :app:installDist --no-daemon`, copied DB Turn 10 `prepare-turn`, and `audit-context`. Formal `data/pilot2.db` remained `stateVersion=9`, `nextTurnNumber=10`, `pendingContexts=0`, with no result/validate/resolve.
