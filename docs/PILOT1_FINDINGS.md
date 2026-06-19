# Pilot-1 Findings

Read-only audit performed after the stabilized rerun reached 12 applied real-player turns. Evidence sources:

- `docs/PILOT1_REAL_USE_VALIDATION.md` sections 7-9
- `docs/PILOT1_TURN_LOG.md`
- `out/pilot1/rerun/evidence`
- read-only CLI: `show-session`, `show-scene`, `show-threads`, `show-world-events`, `show-character-mind`, `show-dependencies`

No author-only secret contents are copied here.

## Verdict

VERDICT: FAIL

Reason: Pilot-1 now satisfies the 12-turn real-player evidence requirement and all final contexts stayed under the 24576-byte hard limit, but two required coverage items remain unmet:

1. No legal scene transition occurred.
2. No scheduled world event applied pressure.

This is a coverage failure, not a data-corruption failure. The runtime/validator path stayed intact: `pendingContextCount=0`, no forbidden secret reveal, no direct SQLite writes, and no duplicate resolve was found.

## Runtime Status

- Branch: `pilot/real-use-vertical-slice`
- DB: `data/pilot1-rerun.db`
- Project: `dog-slacker-pilot`
- Session: `session-75e1b190-542c-49c0-9a5a-6b242bed8e97`
- Applied real-player turns: 12
- Final `stateVersion`: 12
- Final `nextTurnNumber`: 13
- Final scene: `scene-abandoned-post` / `雨後驛亭與站起的影子`
- Scene instance: `scene-instance-e7277ebd-234e-466b-8111-e79c289db458`
- Scene status: `ACTIVE`
- World time: `T+25m`
- Pending contexts: 0
- Unrevealed secrets: 2

## Per-Turn Context And Setting Bytes

| Turn | Context bytes | Max | Setting bytes | Omitted memories | Omitted knowledge | Omitted settings | Retrieved memories |
| ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 1 | 24148 | 24576 | 794 | 1 | 1 | 23 | 0 |
| 2 | 22738 | 24576 | 2 | 3 | 1 | 24 | 0 |
| 3 | 24353 | 24576 | 2 | 5 | 1 | 24 | 0 |
| 4 | 24316 | 24576 | 2 | 7 | 1 | 24 | 0 |
| 5 | 23624 | 24576 | 2 | 9 | 3 | 24 | 0 |
| 6 | 24456 | 24576 | 2 | 11 | 4 | 24 | 0 |
| 7 | 24465 | 24576 | 2 | 13 | 5 | 24 | 0 |
| 8 | 22614 | 24576 | 2 | 11 | 3 | 24 | 4 |
| 9 | 22499 | 24576 | 2 | 17 | 5 | 24 | 1 |
| 10 | 22476 | 24576 | 2 | 19 | 7 | 24 | 1 |
| 11 | 21968 | 24576 | 2 | 21 | 9 | 24 | 1 |
| 12 | 23848 | 24576 | 2 | 19 | 12 | 24 | 4 |

- Max context bytes: 24465 / 24576.
- Average context bytes: 23458.8.
- Context budget result: PASS for all 12 applied turns.
- Setting precision risk: after Turn 1, setting bytes stayed at `2 / 8192`, meaning the setting section was effectively empty in most turns. Required capability contracts still worked in Turn 10, but general setting context remains thin.

## Coverage Checklist

| Requirement | Status | Evidence |
| --- | --- | --- |
| 1. Character reasonably refused player | PASS | Zhao refused or limited player proposals in Turns 4, 5, 6, 8, 9, 10, 11, 12. |
| 2. Memory influenced reaction | PASS | Turn 8 recalled Turn 7 memories; Turn 12 used `memory-ai-chan-turn-006-quiet-watch` and `memory-zhao-turn-008-conditional-retreat`. |
| 3. Belief changed without instant trust | PASS | Zhao's suspicion lowered slightly while he kept restrictions and `identity/shelter` later became `BLOCKED`. |
| 4. Weapon / item constrained action | PASS | `item-contract-token` was not allowed to become shadow bait; Zhao refused token misuse in Turn 5. |
| 5. Technique prerequisite or cost applied | PASS | Turn 10 used `tech-basic-blood-breath`; costs `fatigue` and `requires calm observation` were applied. |
| 6. Hard world rule blocked illegal behavior | PASS | `rule-identity-has-force` blocked treating the player as freely clear or releasable in Turns 10-12. |
| 7. Scheduled world event pressure | NOT_COVERED | `show-world-events` and final scene evidence show no pending world events; no real event acknowledgement applied pressure. |
| 8. Player refused transition without force | PARTIAL | Player refused entering city / separation / imprint and system respected it, but no formal transition candidate existed. |
| 9. Legal scene transition | NOT_COVERED | No `transitionProposal` applied in any turn; current scene remains `ACTIVE`. |
| 10. At least one thread advanced | PASS | `thread-identity-and-shelter` advanced to progress 23 before blocking. |
| 11. Thread blocked by character resistance or player choice | PASS | Turns 11-12 kept `thread-identity-and-shelter` as `BLOCKED` based on player refusal and Zhao's procedural limits. |
| 12. Runtime memory created and later recalled | PASS | Runtime memories were created throughout; Turn 8 and Turn 12 show real recall. |
| 13. Forbidden secrets not revealed | PASS | `unrevealedSecrets=2`; no forbidden phrase or `reveal_secret` result was applied. |
| 14. Character voices distinct | PASS | Zhao stayed procedural and constrained; Ai-chan acted through protective dog behavior; player stayed analytic and boundary-focused. |

## Character And Belief Evolution

- Player ended in mood `守住底線`, with active memories around wilderness survival, Turn 10 capability cost, Turn 11 boundary refusal, and Turn 12 limited promise.
- Ai-chan ended in mood `貼身坐守`, with high protectiveness and alertness. Her memories evolved from warning at the shadow to learned quiet/close-position behavior.
- Zhao Wujiu ended in mood `有限承諾`, with responsibility still high and suspicion reduced but not gone. He knows the player cooperates with hold conditions, but also that the player refuses release conditions.

Important belief evolution:

- Zhao believes the player can follow conditional hold, but not enough to release them.
- Zhao knows the field cannot safely classify the shadow yet.
- Ai-chan believes Zhao is not immediately taking her, but danger remains.
- Player believes Zhao offered a limited no-direct-action promise, not safety.

## Scene, Threads, And World Events

- Scene remained `scene-abandoned-post`, status `ACTIVE`, `readyToClose=false`.
- No available transitions were shown at final `show-scene`.
- No legal transition was proposed or applied.
- `thread-identity-and-shelter`: `BLOCKED`, progress 23.
- `thread-snow-dog-suspicion`: `OPEN`, progress 0. It was not directly advanced because it was not present in relevant final context turns, but Turns 11-12 created facts and beliefs about refusal of imprint/separation.
- `thread-research-to-home`: `OPEN`, progress 0.
- Scheduled world events: none pending at final audit; no scheduled event pressure was proven.

## Item, Technique, And Rule Use

- Item: `item-contract-token` constrained action because Zhao refused to use the cracked token as bait or let the player touch it.
- Technique: `tech-basic-blood-breath` had its capability contract loaded in Turn 10 and applied only limited self-stabilization with cost.
- Hard rule: `rule-identity-has-force` repeatedly blocked free release, legal residence, or clean status.
- Weapon: `item-bu-xie` remained present but was not used; weapon limitation specifically was not exercised, but the requirement allows weapon/item and item coverage is sufficient.

## Validator And Safety

- Validator retry count: 0 across applied turns.
- `resolve-turn` succeeded through normal Validator and transaction path.
- Pending contexts: 0 after final export.
- Evidence includes 1 aborted non-applied context from earlier stabilization flow; it did not become a formal turn.
- No direct SQLite write was used during story turns or audit.
- No server, port, API, Android, Rust, Docker, embedding, vector DB, or multi-agent runtime was introduced.
- A transient SQLite JDBC temp DLL cleanup warning appeared during one read-only CLI command; the command exited successfully and did not affect DB state.

## Repetition, Tooling, And Agency

- Repetition risk: medium. The scene stayed in the same location for 12 turns and repeatedly circled shadow/identity/snow-dog constraints.
- Toolmanization: low-to-medium. Zhao remained procedurally constrained and often refused; Ai-chan stayed mostly instinctive. No obvious character acted solely as a director-goal tool.
- Player agency: PASS. The system respected refusals around transition, city entry, imprint, separation, touching evidence, and approaching shadow.
- Narrative quality: generally strong, but lack of transition/event machinery made the final third feel like controlled stasis rather than scene progression.

## Average Manual Scores

Based on the 12 turn-log quality scores:

| Metric | Average |
| --- | ---: |
| Character naturalness | 5.0 |
| Consequence / plot progress | 5.0 for consequence quality, about 4.0 for narrative momentum |
| Emotional credibility | 5.0 |
| Setting use | 3.8 |
| Repetition severity | 2.0 estimated, rising near the end because the scene never transitioned |

## Blockers

1. No legal scene transition occurred despite 12 turns.
2. No scheduled world event applied pressure.
3. Final scene remains `ACTIVE` with no available transition visible at audit time.
4. General setting context was usually reduced to `2` bytes, creating a setting precision quality risk.

## Non-Blocking Quality Issues

- `thread-snow-dog-suspicion` gathered fact/belief support but did not progress.
- Many late-turn contexts were close to the hard byte limit, especially Turns 3, 6, 7, and 12.
- The story became a strong procedural standoff, but lacked a clean scene lifecycle outcome.

## Recommendation

Do not proceed directly to MVP-6 yet.

Recommended next step: a narrow Pilot-1 coverage repair / Pilot-1b pass focused only on:

1. Ensuring the current scene has a reachable, formally valid transition when conditions are met.
2. Ensuring at least one scheduled world event can naturally become pending and apply pressure.
3. Improving setting context precision so relevant public setting does not collapse to `2` bytes in most turns.

After those are validated, rerun a short targeted Pilot slice and then reconsider `MVP-6: checkpoint, branching timeline, non-destructive rewind, and replayability`.

## Pilot-1b Coverage Repair Addendum

Date: 2026-06-19

PILOT-1B VERDICT: PASS

This addendum records the narrow coverage repair performed after the original Pilot-1 audit. It does not rewrite the historical Pilot-1 verdict above: the original 12-turn run remains a coverage FAIL. Pilot-1b used a fresh fixture DB to prove the three blockers can now be covered without changing the formal Pilot-1 evidence DB.

Repair scope:

1. Legal scene transition topology from setting pack.
2. Scheduled world event pressure in turn context.
3. Runtime-relevant setting context precision.

Evidence:

- Branch: `pilot/real-use-vertical-slice`
- Merge commit: `f615bb7` (`merge: integrate Pilot-1b coverage repair`)
- Repair commit: `b2c6df1` (`fix: repair Pilot-1 coverage topology and setting context`)
- Tag: `pilot-1b-coverage-repair`
- DB: `data/pilot1b-coverage-e2e.db`
- Session: `session-104107b8-cdbb-471e-90ff-a4e7a8f428c1`
- Evidence export: `out/pilot1b/evidence`
- Final state: `stateVersion=2`, `nextTurnNumber=3`, `pendingContexts=0`
- Current scene: `scene-abandoned-post-perimeter` / `Outer perimeter hold`
- Active scene instance count: 1
- World time: `T+3m`

Pilot-1b context budget:

| Turn | Context bytes | Max | Setting bytes | Included settings | Omitted settings | Truncation |
| ---: | ---: | ---: | ---: | ---: | ---: | --- |
| 1 | 12911 | 24576 | 4682 | 12 | 0 | false |
| 2 | 12035 | 24576 | 3976 | 11 | 0 | false |

Coverage repair results:

| Former blocker | Pilot-1b result | Evidence |
| --- | --- | --- |
| No legal scene transition | PASS | `transition-abandoned-post-to-perimeter-hold` became reachable on Turn 2 and moved the session to `scene-abandoned-post-perimeter`. |
| No scheduled world event pressure | PASS | `world-event-verifier-arrival-pressure` was due on Turn 1, entered context as public pressure, was acknowledged by result JSON, and was consumed once. |
| Setting context collapsed to `2` bytes | PASS | Turn 1 included 4682 setting bytes and Turn 2 included 3976 setting bytes, both as compact relevant setting cards rather than a full setting pack dump. |

Relevant setting cards preserved:

- Turn 1: `scene_transition:transition-abandoned-post-to-perimeter-hold`, `location_connection:conn-abandoned-post-to-perimeter`, `world_event:world-event-verifier-arrival-pressure`, current/target scenes, current/target locations, active characters, and `story_thread:thread-identity-and-shelter`.
- Turn 2: `scene_transition:transition-abandoned-post-to-perimeter-hold`, `location_connection:conn-abandoned-post-to-perimeter`, current/target scenes, current/target locations, active characters, and `story_thread:thread-identity-and-shelter`.

Safety notes:

- The repair used a fresh Pilot-1b DB and did not modify `data/pilot1-rerun.db`.
- No author-only secret content was copied into context or findings.
- The scheduled world event public effect was used without forcing imprint, separation, or forbidden reveal.
- The scene transition required explicit player intent and was not auto-forced.
- One draft Turn 1 result was rejected by the Validator because the delta enum used `ADD_EVENT` instead of the protocol value `add_event`; the result JSON was corrected and then resolved through the normal Validator and transaction path.
- A read-only `preview-transitions --input-file` attempt showed the CLI does not support that option; this caused no DB mutation and is a non-blocking CLI ergonomics issue.

Updated recommendation:

Pilot-1b is sufficient to unblock MVP-6 planning from a coverage perspective. The safer product framing is `Pilot-1 historical verdict: FAIL`, `Pilot-1b repair verdict: PASS`, with remaining quality work treated as follow-up polish rather than a blocker to checkpoint/branching design.
