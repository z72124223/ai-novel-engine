# Pilot-2 Transition Refusal Minimal Repair Report

Date: 2026-06-21

Status: `REPAIR_IMPLEMENTED_PROBE_DEFERRED`

## 1. VERDICT

`REPAIR_READY_FOR_REVIEW`

The minimal repair narrows transition refusal detection so that unrelated boundaries do not automatically block a player-consented scene transition. Direct movement refusal remains blocked. This implements the approved D path for transition refusal / Validator / runtime repair.

The formal B probe was not executed by 02. Per the corrected 01 boundary, any formal Codex-authored Pilot-2 probe content must be authored by 01 and assigned in a later milestone.

## 2. Scope

Changed source/test scope:

- `app/src/main/kotlin/com/localnovel/engine/service/SceneThreadServices.kt`
- `app/src/test/kotlin/com/localnovel/engine/Mvp4SceneThreadRuntimeTest.kt`

Control/report scope:

- `PLANS.md`
- `docs/PILOT2_TRANSITION_REFUSAL_MINIMAL_REPAIR_REPORT.md`

Not changed:

- schema;
- migrations;
- `stateVersion`;
- `contextHash`;
- `resolve-turn` orchestration;
- StateReducer;
- formal Pilot-2 DB data.

## 3. Problem Reproduced

Prior evidence from Turns 12-13 showed this pattern:

- context layer exposed a legal transition;
- result-file validation passed;
- full runtime validation during `resolve-turn` rejected with `player_refused_transition`;
- retry without transition resolved successfully.

The read-only investigation found that `TransitionValidator` used a broad full-input keyword scan. The generic English keyword `refuse` could classify a player boundary against other actions as a refusal of movement.

## 4. Regression Test

Added test:

```text
Mvp4SceneThreadRuntimeTest.nonMovementBoundaryDoesNotBlockConsentedTransition
```

The test prepares a turn where the player explicitly includes the required transition intent `離開`, while also saying:

```text
I refuse to open cargo, touch seals, or enter the sealed warehouse.
```

Before the repair, this failed with:

```text
player_refused_transition: Player input refuses transition; normal transition cannot be forced.
```

After the repair, validation passes, `resolveTurn` applies the transition, and the test session reaches:

```text
scene-shrine-questioning
```

Existing direct-refusal coverage remains in place through:

- `Mvp4SceneThreadRuntimeTest.playerRefusalBlocksNormalTransition`
- `Pilot1bCoverageRepairTest.playerRefusalCannotBeResolvedAsNormalTransition`

## 5. Repair Summary

`TransitionValidator` now calls a focused movement-refusal helper:

```text
context.playerInput.refusesSceneTransition()
```

The helper checks for explicit movement or transition refusal phrases, such as:

- Chinese movement refusal: `不走`, `留下`, `拒絕離開`, `不離開`, `不要走`
- English movement refusal: `refuse to leave`, `refuse to move`, `refuse to go`, `stay exactly`, `stay here`, `do not leave`, `do not move`, `do not go`

It no longer treats generic `refuse` by itself as a transition refusal. This preserves protection against forced movement while avoiding false positives when a player refuses only unrelated actions such as opening cargo or touching seals.

## 6. Verification

Focused regression run:

```text
Mvp4SceneThreadRuntimeTest.nonMovementBoundaryDoesNotBlockConsentedTransition
```

Result:

```text
PASS
```

Focused class XML evidence after the repair:

```text
TEST-com.localnovel.engine.Mvp4SceneThreadRuntimeTest.xml tests=31 failures=0 errors=0 skipped=0
TEST-com.localnovel.engine.Pilot1bCoverageRepairTest.xml tests=7 failures=0 errors=0 skipped=0
```

Full required verification:

```text
git diff --check
PASS, with expected Windows CRLF working-copy warnings.

gradlew.bat clean test --no-daemon
BUILD SUCCESSFUL in 3m 37s
6 actionable tasks: 6 executed
XML aggregate: tests=240 failures=0 errors=0 skipped=0
```

## 7. CLI E2E Decision

No formal Pilot-2 CLI E2E was run in this milestone because 01 explicitly withdrew 02's permission to author or execute the formal Codex-authored probe. The repair is covered through runtime tests using temporary test databases and formal runtime validation paths.

If 01 later supplies probe input/result instructions, the next milestone should run the formal Pilot-2 CLI flow through `prepare-turn`, `audit-context`, result, `validate-result`, `resolve-turn`, evidence export, and narrative audit.

## 8. Formal Probe Status

Formal B probe status:

```text
DEFERRED_TO_01
```

02 did not:

- write `turn_014_player_input.txt`;
- draft a formal Pilot-2 AI result;
- run `prepare-turn`;
- run `validate-result`;
- run `resolve-turn`;
- mutate `data/pilot2.db`;
- count any test fixture as real human evidence.

## 9. Remaining Risks

- The helper remains heuristic-based and may still need future refinement for broader Chinese phrasing.
- The repair intentionally does not change context transition recognition.
- The repair intentionally does not change `validate-result` / `resolve-turn` command-surface differences.
- A formal Pilot-2 probe is still needed if 01/user wants evidence on the real Turn 14 path.

## 10. 03 Review Checklist

03 should verify:

1. The new test reproduced the false-positive path before repair.
2. The repair narrows generic refusal without allowing direct movement refusal.
3. Existing direct-refusal tests still pass.
4. No schema, migration, `stateVersion`, `contextHash`, `resolve-turn`, or StateReducer files changed.
5. No formal Pilot-2 probe was executed by 02.
6. No ignored `out/` or `data/` artifacts are committed.
7. `mvp-6` remains unchanged.
