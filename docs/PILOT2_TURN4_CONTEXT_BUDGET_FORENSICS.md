# Pilot-2 Turn 4 Context Budget Forensics

Date: 2026-06-21

Status: `READ_ONLY_FORENSICS`

This report analyzes why Pilot-2 Turn 4 failed to produce a final `ContextPacket`. It uses existing ignored artifacts under `out/pilot2/pilot2-qingli-herb-warehouse/turns/` and does not rerun Turn 4, generate a result, resolve a turn, or modify SQLite.

## 1. VERDICT

`CONTEXT_BUDGET_BLOCKED_CONFIRMED`

Turn 4 failed because the finalized candidate still required `26885` UTF-8 bytes after trimming, above the `24576` hard max by `2309` bytes. The failure was not caused by a missing semantic requirement: semantic coverage was `PASS`, but the semantic floor plus mandatory runtime context still could not fit.

Formal state remained safe:

- `stateVersion`: `3`
- `nextTurnNumber`: `4`
- `pendingContexts`: `0`
- no Turn 4 result was produced
- no Turn 4 resolve occurred

## 2. Failure Summary

The main pressure came from the combination of:

- transition/location relevance triggered by the player stepping back and asking for package/patient details before deciding whether to go to the awning;
- a due scheduled world event that had to remain visible;
- active character runtime states for five scene participants;
- retained facts and knowledge claims from Turns 1 to 3;
- duplicated transition details appearing in both `availableTransitions` and `relevantSettings`.

The player input is not the primary cause. It is `1848` bytes after trimming, but the final candidate is `2309` bytes over the hard max. Even deleting the whole player input would not be an acceptable or sufficient repair, and it would violate the protocol.

## 3. Byte Breakdown

Failure report source:

```text
out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_004_context_budget_failure.json
```

Overall budget:

| Field | Bytes |
| --- | ---: |
| maxContextBytes | 24576 |
| candidateBytesBeforeTrimming | 53956 |
| candidateBytesAfterTrimming | 26885 |
| bytesOverLimit | 2309 |
| mandatoryBytes | 10055 |
| optionalBytesRemaining | 16830 |

Top retained sections after trimming:

| Rank | Section | Bytes | Count | Notes |
| ---: | --- | ---: | ---: | --- |
| 1 | `relevantSettings` | 5350 | 7 | Transition, location connection, and pending world event setting cards. |
| 2 | `characterRuntimeStates` | 3608 | 5 | Goals, emotions, allowed actions, constraints for all active characters. |
| 3 | `facts` | 2722 | 8 | Public facts from the first three turns. |
| 4 | `characterKnowledge` | 1931 | 4 | Belief/knowledge claims selected by relevance. |
| 5 | `playerInput` | 1848 | 1 | Long but not the sole cause. |
| 6 | `availableTransitions` | 1522 | 3 | Three possible transitions and unmet prerequisites. |
| 7 | `contextBudgetMetadata` | 1025 | 2 | Minimal byte/count accounting for manual safety. |
| 8 | `worldRules` | 930 | 3 | Hard rules required for safety. |
| 9 | `activeCharacters` | 813 | 5 | Compact public character profiles. |
| 10 | `openTasks` | 793 | 1 | Turn 3 legal-opening task. |
| 11 | `worldEvents` | 616 | 1 | Due buyer-arrival pressure. |
| 12 | `recentEvents` | 571 | 1 | Last retained event summary. |
| 13 | `continuityConstraints` | 512 | 1 | Current scene/location and character locations. |

Before/after counts of important sections:

| Section | Before bytes | Before count | After bytes | After count |
| --- | ---: | ---: | ---: | ---: |
| `relevantSettings` | 8747 | 14 | 5350 | 7 |
| `characterRuntimeStates` | 6250 | 5 | 3608 | 5 |
| `retrievedMemories` | 5159 | 9 | 2 | 0 |
| `facts` | 4293 | 13 | 2722 | 8 |
| `recentEvents` | 2211 | 3 | 571 | 1 |
| `storyThreads` | 996 | 3 | 332 | 1 |
| `recentTurnSummaries` | 1007 | 3 | 2 | 0 |
| `availableTransitions` | 1522 | 3 | 1522 | 3 |
| `worldEvents` | 616 | 1 | 616 | 1 |

## 4. Largest Section Ranking

The largest final JSON paths were:

1. `$.relevantSettings` - `5350` bytes
2. `$.characterStates` - `3608` bytes
3. `$.facts` - `2722` bytes
4. `$.knowledgeClaims` - `1931` bytes
5. `$.playerInput` - `1848` bytes
6. `$.availableTransitions` - `1522` bytes
7. `$.worldRules` - `930` bytes
8. `$.openTasks` - `793` bytes
9. `$.pendingWorldEvents` - `616` bytes

The relevant settings are the largest section. Within `relevantSettings`, the largest retained objects were transition and location cards around:

- `transition-perimeter-to-awning-witnessed`
- `conn-perimeter-to-awning`
- `transition-perimeter-to-runoff-observe`
- `conn-perimeter-to-runoff`
- `transition-perimeter-to-waiting-line`
- `conn-perimeter-to-waiting-line`
- `world-event-herb-buyer-arrival-pressure`

## 5. Content That Cannot Be Safely Cut

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

Semantic floor requirements that were satisfied and should remain protected:

- `CHARACTER_GOAL`: active character goals are required for NPC autonomy.
- `WORLD_RULE`: hard world rules are required for legal/safety decisions.
- `TRANSITION_CONSTRAINT`: the planner treated this input as movement/withdrawal related.
- `LOCATION_CONSTRAINT`: current scene and character location constraints are needed.
- `RELEVANT_BELIEF`: character-facing belief overlaps the current question.
- `WORLD_EVENT_PRESSURE`: `world-event-herb-buyer-arrival-pressure` is due and must remain visible with public limits.

Safety fields were present, and the context hash was valid. The failure is therefore a fitting problem, not a missing-safety-field problem.

## 6. Content That Was Already Cut

The budgeter already removed or compacted:

- all `9` retrieved memories;
- `7` relevant setting cards;
- `5` facts;
- all `3` recent turn summaries;
- `2` story threads.

Important implication: Turn 4 did not fail because too many recalled memories were retained. `retrievedMemories` had already been reduced to an empty array.

## 7. Contents That Can Be Summarized Further

The likely safe repair surface is compaction and deduplication, not deletion.

### 7.1 Transition and setting duplication

Duplicate content appears across `availableTransitions` and `relevantSettings`, including:

- unmet prerequisite text such as `player intent 'go to awning with witness' not present`;
- target location IDs;
- target scene IDs;
- transition IDs;
- scheduled world event IDs and names.

Potential future repair:

- keep `availableTransitions` as the operational preview;
- make required `relevantSettings` cards for transitions/location connections smaller reference cards;
- avoid repeating the same unmet prerequisite text in both sections;
- keep travel time, target scene/location, forced/optional status, and player intent requirement.

### 7.2 Character runtime state flags

`characterRuntimeStates` retains `3608` bytes across five active characters. These are important for autonomy, but the current compact view still carries verbose flag values such as JSON-like action lists and relationship defaults.

Potential future repair:

- convert action lists in `flags` to compact semicolon strings or capped arrays at the model/serialization boundary;
- retain `current_goal`, `dominant_emotion`, constraints, and forbidden actions;
- avoid carrying verbose relationship defaults unless directly relevant.

### 7.3 Facts and knowledge claims

`facts` plus `characterKnowledge` retain `4653` bytes after trimming. The dangling reference check also shows selected knowledge claims refer to trimmed supporting facts/memories:

- `knowledge:claim-1b7ba3f2-33ea-4df1-85df-f4b361f50a55.fact:fact-pilot2-turn-002-crowd-pressure`
- `knowledge:claim-c3aeeda5-1dd9-472f-8d5b-f07c686c594b.fact:fact-pilot2-turn-001-player-kept-boundary`
- `knowledge:claim-f8d2e080-3bea-4c52-b1c8-c254f7ec9663.memory:memory-patrol-he-turn-001-unverified-observer`

Potential future repair:

- when a knowledge claim is retained but its source fact/memory is trimmed, include a very small `sourceSummary` or keep the source reference coherent;
- compact older facts into turn-scoped summaries, while preserving specific fact IDs needed by retained knowledge claims;
- ensure the dangling reference check becomes valid or explicitly marks source references as omitted with enough human-readable support.

### 7.4 Open task payload

`openTasks` keeps `793` bytes, with one task from Turn 3. The task is relevant because Turn 4 asks for patient, medicine, package number, and witness conditions.

Potential future repair:

- retain title, status, and the most relevant next option;
- trim older `nextOptions` that do not match the current input;
- keep the source turn and legal-opening purpose.

### 7.5 Context budget metadata

`contextBudgetMetadata` is `1025` bytes after trimming. It is mandatory, but may still have room for tighter final-context representation.

Potential future repair:

- keep `maxBytes`, `actualBytes`, included/omitted counts, and truncation flags;
- ensure detailed diagnostics stay only in `explain-context-budget` / `audit-context` sidecars.

## 8. Omitted Counts And Pressure Sources

Based on section before/after counts:

- omitted memories: `9` of `9`
- omitted relevant settings: `7` of `14`
- omitted facts: `5` of `13`
- omitted turn summaries: `3` of `3`
- omitted story threads: `2` of `3`

Remaining pressure sources:

- `threads`: one retained story thread, `332` bytes.
- `world events`: one due event, `616` bytes. This is a semantic floor item and should not be dropped.
- `scene transitions`: three available transitions, `1522` bytes, plus related setting cards inside `relevantSettings`.
- `tasks`: one open task, `793` bytes, relevant to the Turn 4 player request.
- `evidence`: exported evidence files are not part of the failed final context. The pressure comes from facts/events/tasks/knowledge derived from prior turns, not from the external evidence JSON itself.

## 9. Is Player Input The Main Cause?

No.

The Turn 4 player input is long (`1848` bytes), but the post-trim candidate exceeds the hard max by `2309` bytes. The input should stay intact because it is protocol-critical and represents the player's boundaries. The useful fix is to compact context sections that repeat transition/settings/procedure data, not to shorten or rewrite the player's human input.

The input did, however, contribute indirectly by containing movement language such as `退半步` and `離粉線更遠些`. `TurnSemanticPlanner` currently treats movement terms as a transition cue, which pulled in transition and location constraints. That behavior is safety-conservative, but in this case it may be too broad because the player is stepping back within the same scene rather than requesting a scene transition.

## 10. Diagnosis

This is not simply "budget too small." The hard cap is working as intended.

Best diagnosis:

1. Relevance floor became high because Turn 4 combined current task/procedure, movement wording, relevant beliefs, due world event pressure, and active multi-NPC state.
2. Compact records are not yet compact enough for this density, especially `relevantSettings`, `characterRuntimeStates`, `facts`, and `knowledgeClaims`.
3. Transition data is duplicated between operational transition previews and setting context.
4. Retained knowledge claims can outlive their source fact/memory references, creating dangling-reference risk even before byte fit succeeds.

## 11. Whether A Small Patch Is Needed

Yes, if the project wants to continue Pilot-2 Turn 4 with the same player input and without raising the hard max.

Recommended scope for a future small patch:

1. Add more compact transition/location setting cards when `availableTransitions` already contains the operational preview.
2. Compact character runtime state flag values while preserving goals, emotions, actions, constraints, and relationship-critical information.
3. Compact retained facts and knowledge claims together so source references remain coherent.
4. Review transition cue sensitivity so same-scene position changes do not always require all scene transition settings, while still preserving safety when actual movement/transition is requested.
5. Shrink final `contextBudgetMetadata` further if it can be done without losing auditability.

## 12. Not Recommended

Do not fix this by:

- raising `maxContextBytes` above `24576`;
- deleting world rules, forbidden reveals, continuity constraints, or due world events;
- removing character goals or runtime states;
- pretending recent facts are recalled memories;
- dropping all transition constraints from movement-related inputs;
- deleting memories/facts/settings from the DB;
- rewriting the player's input just to fit;
- bypassing `prepare-turn`, `audit-context`, Validator, `stateVersion`, `contextHash`, or transaction rules.

## 13. Safe Conditions Before Rerunning Turn 4

Before a future Turn 4 rerun:

1. Implement any compaction fix in a separate approved milestone.
2. Verify on a copied DB first, not by mutating `data/pilot2.db`.
3. Run `explain-context-budget` for the exact same Turn 4 input.
4. Confirm `candidateBytesAfterTrimming <= 24576`.
5. Confirm semantic coverage remains `PASS`.
6. Confirm dangling references are either valid or explicitly summarized without broken source IDs.
7. Confirm `pendingContexts=0`, `stateVersion=3`, and `nextTurnNumber=4` before formal rerun.
8. Only then run `prepare-turn`, `audit-context`, result drafting, validation, and `resolve-turn`.

## 14. Final Recommendation

Next milestone should be a narrow `Pilot-2 context compaction repair` if the user wants to continue Turn 4 with the same input. The repair should focus on transition/setting deduplication and compact runtime/fact/knowledge views. It should not increase the budget and should not weaken the safety or semantic floor.
