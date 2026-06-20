# Plan

## Goal

Investigate Pilot-2 Turn 10 context budget failure as read-only forensics, document the exact byte pressure sources, and recommend the smallest safe future repair. Do not rerun Turn 10, do not prepare a new context, do not draft a result, and do not modify SQLite.

## Global Strategy

Use existing evidence artifacts from `out/pilot2/pilot2-qingli-herb-warehouse/turns/` and compare them with the earlier Turn 4 budget failure. Treat all numeric claims as facts only when confirmed from JSON artifacts, docs, source code, or command output. Produce a documentation-only milestone and verify it with the standard repo checks.

## Non-Goals

- No Turn 10 rerun.
- No `prepare-turn`.
- No `turn_010_context.json`.
- No `turn_010_result.json`.
- No `validate-result`.
- No `resolve-turn`.
- No SQLite writes.
- No Kotlin code changes.
- No test changes.
- No schema or migration changes.
- No Validator changes.
- No `stateVersion` or `contextHash` changes.
- No `StateReducer` changes.
- No Candidate B.
- No `mvp-6` retag.
- No push in this milestone.

## Scope

- `PLANS.md`
- `docs/PILOT2_TURN10_CONTEXT_BUDGET_FORENSICS.md`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Working tree was clean before this milestone.
- Turn 10 was not formally executed.
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_010_context_budget.json` reports `candidateBytesBeforeTrimming=93794`.
- The same report shows `candidateBytesAfterTrimming=26904`, `maxContextBytes=24576`, and `bytesOverLimit=2328`.
- `mandatoryBytes=10503`.
- Semantic coverage is `PASS`.
- Formal session evidence before and after the failure remained at `stateVersion=9` and `nextTurnNumber=10`.
- Top retained sections after trimming are `relevantSettings=3034`, `playerInput=2949`, `characterRuntimeStates=2918`, `facts=2731`, `retrievedMemories=2339`, `characterKnowledge=1676`, `availableTransitions=1183`, `openTasks=1119`, and `contextBudgetMetadata=1060`.
- Turn 4 failed at `26885/24576`; Turn 10 failed at `26904/24576`.
- Compared with Turn 4, Turn 10 retained fewer setting bytes but more retrieved memory bytes and a larger player input.

## Assumptions

- A future repair should focus on compaction and deduplication rather than changing semantic floor selection.
- Turn 10's explicit awning movement intent and previous-turn recall cues explain why transition constraints and retrieved memories both remained active.
- `contextBudgetMetadata` may be reducible in final context while preserving full diagnostics in sidecar reports.

## Open Questions

- Whether transition intent matching should distinguish exact formal transition intent from broad movement language is a future repair decision, not part of this forensics milestone.
- Whether retrieved memory cards can be made more compact without harming manual result drafting requires code-level design in a later approved repair.

## Implementation Steps

- [x] Step 1: Read Turn 10 player input, budget report, before/after evidence, Turn 4 forensics, control docs, and context services.
- [x] Step 2: Extract section-level byte breakdown, retained/removed item counts, semantic coverage, duplicates, and top JSON paths.
- [x] Step 3: Update `PLANS.md` for the Turn 10 read-only forensics milestone.
- [x] Step 4: Add `docs/PILOT2_TURN10_CONTEXT_BUDGET_FORENSICS.md`.
- [x] Step 5: Run `git diff --check`.
- [x] Step 6: Run `gradlew.bat clean test --no-daemon`.
- [ ] Step 7: Commit `docs: investigate Pilot-2 Turn 10 context budget failure`.

## Verification

- `git diff --check`
- `gradlew.bat clean test --no-daemon`
- Confirm no Turn 10 context/result/validate/resolve files were created by this milestone.
- Confirm git diff contains only the planned documentation files.

## Risks

- Misdiagnosing a semantic-floor issue as pure byte pressure. Mitigation: report semantic coverage status and satisfied requirements directly from the budget JSON.
- Accidentally implying budget should be raised. Mitigation: explicitly reject raising the `24576` hard max.
- Accidentally treating user input as the problem. Mitigation: classify player input as large but protocol-critical and not the main repair target.

## Drift Log

- 2026-06-21: Started read-only Pilot-2 Turn 10 context budget forensics after user approved documentation-only analysis and prohibited rerun, prepare, result, validation, resolve, DB writes, Kotlin changes, and push.
- 2026-06-21: Confirmed Turn 10 failure is byte pressure after semantic coverage `PASS`, not missing semantic coverage.
- 2026-06-21: Confirmed the largest retained sections after trimming are `relevantSettings`, `playerInput`, `characterRuntimeStates`, `facts`, `retrievedMemories`, `characterKnowledge`, `availableTransitions`, `openTasks`, and `contextBudgetMetadata`.
- 2026-06-21: Verified `git diff --check` passed with only CRLF normalization warnings.
- 2026-06-21: First `clean test` attempt timed out and left Gradle daemon file locks; stopped Gradle daemons with `gradlew.bat --stop`.
- 2026-06-21: Verified `gradlew.bat clean test --no-daemon` passed after stopping stale Gradle daemons.
