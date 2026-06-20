# Pilot-2 Turn 10 Context Budget Forensics

Date: 2026-06-21

Status: `READ_ONLY_FORENSICS`

This report analyzes why Pilot-2 Turn 10 could not fit into the final `ContextPacket` hard limit. It uses existing ignored artifacts under `out/pilot2/pilot2-qingli-herb-warehouse/turns/` and does not rerun Turn 10, generate a context, draft a result, validate a result, resolve a turn, or modify SQLite.

## 1. VERDICT

`CONTEXT_BUDGET_BLOCKED_CONFIRMED`

Turn 10 failed because the candidate context still required `26904` actual UTF-8 bytes after trimming, above the `24576` hard max by `2328` bytes. The failure was not caused by missing semantic coverage. The budget report shows semantic coverage `PASS`; the problem is that the mandatory floor plus selected semantic floor still could not fit.

Formal state remained safe:

- `stateVersion`: `9`
- `nextTurnNumber`: `10`
- `pendingContexts`: `0`
- no Turn 10 context was produced
- no Turn 10 result was produced
- no Turn 10 validation or resolve occurred

## 2. Failure Summary

Turn 10 combines several expensive relevance signals:

- explicit formal movement intent: `go to awning with witness`
- transition and location constraints for the awning route
- explicit recall cues around earlier wording, witness limits, and Shen Qinghe's damp-package judgment
- retained facts from Turns 8 and 9
- an open task that repeats the same legal-opening checklist
- five active character runtime states
- context budget metadata that remains mandatory in final context

The player input is large, but it is not the main cause. It is protocol-critical, mandatory, and represents the player's exact consent and boundaries. The safer repair direction is compacting repeated runtime context, not shortening or rewriting the player's input.

## 3. Byte Breakdown

Failure report source:

```text
out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_010_context_budget.json
```

Overall budget:

| Field | Bytes |
| --- | ---: |
| maxContextBytes | 24576 |
| candidateBytesBeforeTrimming | 93794 |
| candidateBytesAfterTrimming | 26904 |
| bytesOverLimit | 2328 |
| mandatoryBytes | 10503 |
| optionalBytesRemaining | 16401 |

Top retained sections after trimming:

| Rank | Section | Bytes | Count | Notes |
| ---: | --- | ---: | ---: | --- |
| 1 | `relevantSettings` | 3034 | 6 | Transition and location connection cards remain relevant to awning/runoff/waiting-line movement. |
| 2 | `playerInput` | 2949 | 1 | Long but mandatory; it carries consent, limits, and exact transition intent. |
| 3 | `characterRuntimeStates` | 2918 | 5 | Current goals, emotions, flags, constraints for active characters. |
| 4 | `facts` | 2731 | 8 | Retained recent procedure, water, buyer pressure, route, merchant, and porter facts. |
| 5 | `retrievedMemories` | 2339 | 4 | Explicit memory cue retained four memory cards. |
| 6 | `characterKnowledge` | 1676 | 4 | Relevant beliefs/knowledge claims retained by semantic floor. |
| 7 | `availableTransitions` | 1183 | 3 | Operational transition preview for awning, runoff, and waiting-line candidates. |
| 8 | `openTasks` | 1119 | 1 | Legal-opening task repeats patient, water, cart, package, witness, and liability details. |
| 9 | `contextBudgetMetadata` | 1060 | 2 | Final budget summary remains mandatory. |
| 10 | `recentEvents` | 969 | 1 | Recent event continuity from the runoff-edge check. |
| 11 | `worldRules` | 930 | 3 | Hard world rules required for safety. |
| 12 | `activeCharacters` | 813 | 5 | Compact public character profiles. |
| 13 | `continuityConstraints` | 512 | 1 | Current scene, location, and character location constraints. |

Before/after section size:

| Section | Before bytes | Before count | After bytes | After count |
| --- | ---: | ---: | ---: | ---: |
| `facts` | 14892 | 43 | 2731 | 8 |
| `characterRuntimeStates` | 11893 | 5 | 2918 | 5 |
| `retrievedMemories` | 10463 | 18 | 2339 | 4 |
| `characterKnowledge` | 10093 | 21 | 1676 | 4 |
| `relevantSettings` | 8862 | 16 | 3034 | 6 |
| `activeCharacters` | 5345 | 5 | 813 | 5 |
| `recentEvents` | 3803 | 3 | 969 | 1 |
| `playerInput` | 2949 | 1 | 2949 | 1 |
| `recentTurnSummaries` | 1647 | 3 | 2 | 0 |
| `availableTransitions` | 1345 | 3 | 1183 | 3 |
| `openTasks` | 1119 | 1 | 1119 | 1 |
| `storyThreads` | 996 | 3 | 332 | 1 |
| `worldRules` | 930 | 3 | 930 | 3 |
| `contextBudgetMetadata` | 855 | 2 | 1060 | 2 |

Removed item counts:

| Section | Removed |
| --- | ---: |
| `facts` | 35 |
| `characterKnowledge` | 17 |
| `retrievedMemories` | 14 |
| `relevantSettings` | 10 |
| `recentTurnSummaries` | 3 |
| `storyThreads` | 2 |

## 4. Largest Section Ranking

Largest final JSON paths:

| Rank | JSON path | Bytes | Mandatory |
| ---: | --- | ---: | --- |
| 1 | `$.relevantSettings` | 3034 | no |
| 2 | `$.playerInput` | 2949 | yes |
| 3 | `$.characterStates` | 2918 | yes |
| 4 | `$.facts` | 2731 | no |
| 5 | `$.retrievedMemories` | 2339 | no |
| 6 | `$.knowledgeClaims` | 1676 | no |
| 7 | `$.availableTransitions` | 1183 | no |
| 8 | `$.openTasks` | 1119 | no |
| 9 | `$.openTasks[0]` | 1031 | no |
| 10 | `$.recentEvents` | 969 | no |
| 11 | `$.worldRules` | 930 | yes |
| 12 | `$.contextBudget` | 697 | yes |

## 5. Difference From Turn 4 Budget Failure

Turn 4 and Turn 10 both failed after semantic coverage had been satisfied, and both exceeded the hard cap by roughly the same amount:

| Field | Turn 4 | Turn 10 |
| --- | ---: | ---: |
| candidate before trimming | 53956 | 93794 |
| candidate after trimming | 26885 | 26904 |
| bytes over limit | 2309 | 2328 |
| mandatory bytes | 10055 | 10503 |
| relevantSettings after trimming | 5350 | 3034 |
| characterRuntimeStates after trimming | 3608 | 2918 |
| facts after trimming | 2722 | 2731 |
| retrievedMemories after trimming | 2 | 2339 |
| playerInput after trimming | 1848 | 2949 |
| availableTransitions after trimming | 1522 | 1183 |
| openTasks after trimming | 793 | 1119 |
| worldEvents after trimming | 616 | 2 |

The Turn 4 repair reduced setting and transition duplication. Turn 10 therefore no longer fails primarily because `relevantSettings` are huge. Instead, the remaining pressure is more distributed: exact player input, retrieved memories, facts, open task, transition preview, and mandatory runtime state all add up.

Turn 10 also has no retained scheduled world event pressure in the final candidate, while Turn 4 had a due event. This matches the repeated narrative audit observation that Pilot-2 still lacks pending scheduled world event pressure in later turns.

## 6. Is Player Input The Main Cause?

No.

The player input is the second-largest retained section at `2949` bytes, and it is bigger than Turn 4's `1848` bytes. But it is mandatory and contains exact player consent:

- the player explicitly chooses the awning route;
- the player names the limited responsibility;
- the player lists what they will not touch;
- the player distinguishes `查水` from `開貨`;
- the player protects against buyer and merchant distortion.

Removing or rewriting this input would violate the protocol and weaken player autonomy. The input contributes to the pressure, but the main fix should be further compaction of repeated context surfaces.

## 7. Retrieved Memories, Facts, Open Tasks, And Transition Pressure

These are now the major optional pressure sources.

### Retrieved memories

`retrievedMemories` retained `4` of `18` candidates and still cost `2339` bytes. The semantic coverage report shows an explicit memory cue requirement, with these candidate memories:

- `memory-clerk-luo-turn-008-check-water-not-open`
- `memory-clerk-luo-turn-009-wording-before-awning`
- `memory-clerk-luo-turn-007-check-reason-without-package`
- `memory-shen-qinghe-turn-008-damp-package-needs-id`

The selected semantic evidence was `memory-shen-qinghe-turn-008-damp-package-needs-id`, and the final candidate retained all four memory cards. This is understandable: Turn 10 explicitly builds on earlier wording about `查水不開貨`, awning movement, and Shen Qinghe's limited medical judgment.

### Facts

`facts` retained `8` of `43` candidates and cost `2731` bytes. Retained facts cluster around:

- Turn 8 formalized procedure pressure
- Turn 8 water-check limited record
- Turn 8 Shen Qinghe limited next check
- Turn 9 legal awning route but not taken
- Turn 9 buyer pressure
- Turn 9 limited-liability wording
- Turn 9 merchant reaction
- Turn 9 porter pointing toward the weigh keeper

These facts overlap with the current transition intent and the open task.

### Open task

`openTasks` retained one task at `1119` bytes, with `$.openTasks[0]` alone taking `1031` bytes. Duplicate analysis shows the task title appears twice:

```text
$.openTasks[0].payload.title
$.openTasks[0].title
```

The task also repeats the patient, right-side water mark, post-third-watch cart, low dunnage, weigh-keeper vehicle number, package number, and witness checklist. This overlaps the retained facts and player input.

### Transition and setting surfaces

`availableTransitions` retained three candidates at `1183` bytes. `relevantSettings` retained six cards at `3034` bytes:

- `location_connection:conn-perimeter-to-awning`
- `location_connection:conn-perimeter-to-runoff`
- `location_connection:conn-perimeter-to-waiting-line`
- `scene_transition:transition-perimeter-to-awning-witnessed`
- `scene_transition:transition-perimeter-to-runoff-observe`
- `scene_transition:transition-perimeter-to-waiting-line`

Duplicate groups confirm overlap between transition previews and setting cards:

- `loc-qingli-herb-awning` appears in `availableTransitions` and `relevantSettings`.
- `scene-qingli-herb-awning` appears in `availableTransitions` and `relevantSettings`.
- `transition-perimeter-to-runoff-observe` appears in both surfaces.
- target locations for waiting-line/runoff/awning are repeated.

Turn 10's exact transition intent legitimately pulls these sections in, but they can be represented more compactly.

## 8. Hard Floor And Relevance Floor Content

Hard mandatory sections:

- `protocolIdentity`
- `playerInput`
- `sceneLifecycle`
- `sceneContract`
- `projectSummary`
- `activeCharacters`
- `characterRuntimeStates`
- `worldRules`
- `continuityConstraints`
- `forbiddenReveals`
- `outputRequirements`
- `contextBudgetMetadata`

Satisfied semantic-floor requirements:

- `CHARACTER_GOAL`
- `EXPLICIT_MEMORY_CUE`
- `WORLD_RULE`
- `TRANSITION_CONSTRAINT`
- `LOCATION_CONSTRAINT`
- `RELEVANT_BELIEF`
- `RECENT_EVENT_CONTINUITY`

These categories should not be removed just to fit bytes. Future repairs should compress their representation or deduplicate repeated content while preserving the same evidence.

## 9. What Can Be Summarized Or Deduplicated

### 9.1 `openTasks`

The open task is relevant but overly repetitive. A safe compaction target is:

- keep `taskId`, `status`, and current objective;
- keep only current next options relevant to awning/witness/check-water intent;
- avoid duplicating `title` under both root and payload;
- summarize older patient/package/witness checklist details when the same facts are retained elsewhere.

### 9.2 `retrievedMemories`

Memory cue coverage must remain real recalled memory, not a recent-fact substitute. But retained memory cards could be shorter:

- keep `memoryId`, owner, salience/visibility, and one compact content line;
- move long source/evidence text out of final context when the matching fact or event is already retained;
- keep enough content for manual result drafting to avoid fake recall.

### 9.3 `facts` and `characterKnowledge`

Facts and knowledge claims still overlap. A future repair could:

- collapse same-turn related facts into a compact bundle while retaining individual IDs when referenced;
- remove or shorten source references when the source fact/memory is also present;
- preserve claims required by `RELEVANT_BELIEF`.

### 9.4 Transition settings versus `availableTransitions`

Operational transition data should live primarily in `availableTransitions`. Related `relevantSettings` can become reference cards:

- ID
- target scene/location
- required player intent
- travel time
- forced/not forced
- unmet prerequisites

Avoid repeating long descriptions, labels, or the same target fields in both sections.

### 9.5 `contextBudgetMetadata`

`contextBudgetMetadata` is `1060` bytes and mandatory. It may be possible to keep only final AI-facing essentials in `ContextPacket`, such as:

- `maxBytes`
- `actualBytes`
- truncation flag
- omitted counts
- reason codes

Full path breakdowns, duplicate groups, and forensic details should stay in `explain-context-budget` and `audit-context` sidecars. The current report does not prove those detailed diagnostics are in final context, but it does show the budget metadata object remains over one kilobyte and is worth reviewing.

### 9.6 Character runtime state and knowledge

`characterRuntimeStates` already shrank from `11893` to `2918`, but five active characters still cost almost three kilobytes. A future repair can review:

- verbose flag names or action lists;
- repeated constraints already covered by world rules;
- relationship/default fields that are not turn-relevant.

Do not remove current goals, emotions, known constraints, or action availability; those are required for NPC autonomy.

## 10. Not Recommended

Do not fix Turn 10 by:

- raising the `24576` byte hard max;
- shortening or rewriting the player's input;
- deleting memories, facts, settings, tasks, or world rules from the DB;
- dropping forbidden reveals or world rules;
- dropping transition/location constraints from an explicit transition request;
- treating recent facts as recalled memories;
- bypassing `prepare-turn`, `audit-context`, Validator, `stateVersion`, `contextHash`, or transaction rules;
- forcing the awning transition without formal prerequisites;
- entering Candidate B.

## 11. Recommended Small Repair Scope

Recommended next milestone: `Pilot-2 Turn 10 context compaction repair`.

Smallest useful scope:

1. Compact `openTasks` by removing duplicate root/payload titles and trimming task payload to current relevant next options.
2. Compact retrieved memory cards while preserving real `memoryId` coverage for explicit memory cues.
3. Further deduplicate transition target/intent/location data between `availableTransitions` and `relevantSettings`.
4. Review `contextBudgetMetadata` final-context payload and keep full forensic details in sidecars only.
5. Compact fact/knowledge source references together to avoid repeated source text.
6. Verify first on a copied DB using the exact same Turn 10 input.

This is a compaction repair, not a semantic-rule change. The goal is to preserve the same semantic floor and fit under `24576`.

## 12. Safe Conditions Before Rerunning Turn 10

Before a future formal Turn 10 rerun:

1. Implement any compaction fix in a separately approved milestone.
2. Verify the exact Turn 10 input on a copied DB first.
3. Run `explain-context-budget` and confirm `candidateBytesAfterTrimming <= 24576`.
4. Confirm semantic coverage remains `PASS`.
5. Confirm no context is generated on formal `data/pilot2.db` until the copied DB passes.
6. Confirm formal `data/pilot2.db` remains `stateVersion=9`, `nextTurnNumber=10`, and `pendingContexts=0`.
7. Only then run formal `prepare-turn` and `audit-context`.
8. Do not draft result JSON or resolve until the formal context passes.

## 13. Final Recommendation

Turn 10 is blocked by distributed context density, not by a missing rule or a bad player input. The earlier Turn 4 compaction helped, but Pilot-2 has now accumulated enough memories, facts, tasks, and transition references that another narrow compaction pass is likely needed.

The next safe milestone is a small read/write-code repair limited to final context compaction. It should not raise the limit, weaken semantic floor, alter write paths, or advance the formal Pilot-2 DB before copied-DB verification passes.
