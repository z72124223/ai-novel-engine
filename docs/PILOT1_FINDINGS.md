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
