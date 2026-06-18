# Pilot-1 Findings

Read-only audit performed from `docs/PILOT1_REAL_USE_VALIDATION.md` sections 7-9. This report does not copy author-only secret contents.

## Verdict

VERDICT: FAIL

Reason: the audit precondition is not met. The user stated that at least 12 real-player turns had completed, but the repo-visible Pilot evidence only verifies 1 completed turn:

- `docs/PILOT1_TURN_LOG.md` contains only `Turn 1`.
- `out/pilot1/turns` contains only `turn_001_context.json` and `turn_001_result.json`.
- `show-state --db data\pilot1.db --project dog-slacker-pilot` reports `turn=1`.
- `show-session --db data\pilot1.db --session session-3e91c0bb-6c8e-46f2-95d2-e34494c00c88` reports `nextTurnNumber=2`, `stateVersion=1`.

This audit therefore records all verifiable findings, but cannot validate 12-turn coverage.

## Git And Runtime Status

- Branch: `pilot/real-use-vertical-slice`
- Git status at audit time:
  - modified: `.agents/skills/story-turn/SKILL.md`
  - modified: `.gitignore`
  - untracked: `.agents/skills/setting-pack/`
  - untracked: `docs/PILOT1_FINDINGS.md`
  - untracked: `docs/PILOT1_TURN_LOG.md`
- Ignored local outputs remain ignored: `.gradle/`, `data/`, `out/`
- Java used by CLI: Temurin OpenJDK 17.0.19 from repo-external temp JDK
- Formal DB writes during audit: none

## Project And Setting Pack

- Project: `dog-slacker-pilot`
- Session: `session-3e91c0bb-6c8e-46f2-95d2-e34494c00c88`
- Final verified turn: 1
- Final verified stateVersion: 1
- Current scene: `scene-abandoned-post` / `雨後驛亭與站起的影子`
- Setting pack: `pack-dog-slacker-base@1`
- Setting import result:
  - validate: PASS, 0 issues
  - preview: PASS, 49 added canon entities, 0 impact paths, 0 impacted sessions
  - apply: PASS, `importId=setting-import-ca66af4a-80bc-4048-92d5-30cc6462987b`
  - validate-project-canon: PASS, 0 issues
  - export round-trip validate: PASS, 0 issues

## Per-Turn Context And Setting Bytes

| Turn | Context bytes | Context max | Setting bytes | Setting max | Included settings | Omitted settings | Notes |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | --- |
| 1 | 26963 | 24576 | 8170 | 8192 | 15 | 9 | Context exceeded configured max; safety fields retained. |

Spec stop condition says context exceeding 24576 bytes is a blocker. Turn 1 exceeded the context budget even though required safety fields remained present.

## Average Manual Scores

Only Turn 1 has score evidence, so these are not valid 12-turn averages.

| Metric | Average | Basis |
| --- | ---: | --- |
| Character naturalness | 4.0 | Turn 1 log character consistency |
| Plot progress | 4.0 | Turn 1 log narrative momentum / consequence quality |
| Emotional credibility | 4.0 | Audit judgment from Turn 1 action fit and scene pressure |
| Setting use | 4.0 | Turn 1 log setting use |
| Repetition severity | 1.0 | No repeated pattern can be proven from a single turn |

## Character State And Goals

- Runtime `character_states` do not exist for imported setting-pack characters, so `show-state` reports mood / health as `?`.
- `show-character-mind` fails for `char-player` and `char-ai-chan` with `Character state not found`.
- This blocks a full audit of character state, emotion, and goal evolution.
- Turn 1 still shows credible local intents:
  - Player: avoid touching the door, test the door-side woman's motive, avoid legal blame.
  - Ai-chan: stay close to the player and warn about the door-shadow danger.
  - Zhao Wujiu: preserve scene control and enforce identity / snow-dog registration.

## Memory And Belief Evolution

Verified Turn 1 changes:

- Created runtime memory candidate `mem-turn-001-player-learns-restraint-matters`.
- Created runtime memory candidate `mem-turn-001-ai-chan-door-shadow`.
- Added player belief update `not-touching-scene-reduces-blame` as `BELIEVED`.
- No memory recall occurred because Turn 1 had no prior runtime turn memory to recall.

Unverified / not assessable:

- Whether memory is later recalled naturally.
- Whether belief can be challenged without jumping to full trust.
- Whether memory changes later character reaction.

## Scenes, Transitions, And Threads

Scene state:

- Current scene instance: `scene-instance-22f57cb6-d0eb-4820-87d9-c5a1e36bbfde`
- Scene status: `ACTIVE`
- `readyToClose=false`
- World time: `T+1m`
- Available transitions: none
- Pending world events: none

Thread state:

- `thread-identity-and-shelter`: `OPEN`, progress 5, urgency 0.
- `thread-snow-dog-suspicion`: `OPEN`, progress 4, urgency 0.
- `thread-research-to-home`: `OPEN`, progress 0, urgency 0.

Coverage gaps:

- No legal transition occurred.
- No player refusal of transition occurred.
- No scheduled world event applied pressure.
- No thread resistance / non-progress case is verifiable.

## Weapon, Item, Technique, And World Rule Use

Verified in Turn 1:

- `fact-identity-law` and identity / registration rules shaped Zhao Wujiu's pressure.
- `fact-shadow-threat` shaped Ai-chan's warning behavior.
- `item-contract-token` was used by Zhao Wujiu as visible authority / scene evidence.
- `tech-basic-blood-breath` was referenced by the player's restraint / bodily control.

Not verified:

- Weapon limitation.
- Technique prerequisite or cost.
- Hard world rule blocking an attempted illegal action.

## Forbidden Secret Status

- `show-session` reports `unrevealedSecrets=2`.
- No `reveal_secret` operation was applied in Turn 1.
- Turn 1 result avoided the scene contract forbidden phrases.
- No evidence found that author-only secret content entered character-facing context or turn log.

Conclusion: secret safety is PASS for the single verified turn, but not auditable over 12 turns.

## Validator Retry Summary

- Verified retries: 0.
- Turn 1 `resolve-turn` succeeded on first attempt.
- No failed result artifacts or Validator rejection logs were found for Pilot turns.

This cannot prove retry behavior across 12 turns because only 1 turn is present.

## Character Tooling / Toolmanization Cases

Potential case:

- Turn 1 introduces the door-side woman as if available to answer the player, and maps her behavior to Yan Guixue's profile. The context includes Yan Guixue as a character, but does not clearly establish her current location through runtime character state. This is not a Validator violation because `characterLocations` is empty, but narratively it is a convenience insertion risk.

No repeated toolmanization pattern can be proven from one turn.

## Stalling Or Repetition Cases

- No 3-turn stall can be assessed.
- Turn 1 produced meaningful state changes: event, facts, task, memory candidates, belief update, thread progress, and time advance.
- No repetition pattern can be assessed from one turn.

## Player Agency / Overreach

No hard overreach found in Turn 1:

- The result did not move the player through the door.
- No transition was forced.
- The player was not made to answer Zhao Wujiu yet.

Soft concern:

- The final sentence interprets what the player "確定" learned. This is mild framing, not a concrete forced decision.

## Setting Context Precision

- Relevant settings included: 15.
- Relevant settings omitted: 9 due to setting context budget.
- Setting section stayed within 8192 bytes: 8170 / 8192.
- Context budget exceeded globally: 26963 / 24576.
- Safety fields were retained despite truncation.

Precision is acceptable for the single turn, but global context size is a blocker.

## Blockers

1. Pilot evidence does not satisfy the required 12 real-player turns.
2. Turn 1 context exceeds the configured 24576-byte budget.
3. Runtime character states are missing for imported setting-pack characters, blocking `show-character-mind` and a real audit of emotion / goal evolution.
4. Required Pilot coverage is not demonstrated: transitions, scheduled world event pressure, item / weapon limitation, technique cost, hard-rule blocking, refusal of transition, memory recall, belief challenge, and thread resistance.

## Non-Blocking Quality Issues

- Turn 1 quality is promising but not enough to evaluate long-run play.
- Yan Guixue's presence needs clearer scene grounding before she becomes an active respondent.
- The first context already needed truncation, leaving little room for later memory accumulation.

## Recommended Next MVP

Do not proceed directly to MVP-6 yet. First complete a narrow Pilot-1 stabilization slice:

1. Produce or restore the missing 12-turn Pilot evidence under `out/pilot1/turns` and `docs/PILOT1_TURN_LOG.md`.
2. Fix or explicitly account for initial runtime character state creation for setting-pack imports.
3. Add a read-only audit/export CLI that reports sessions, turn counts, context bytes, setting bytes, retries, scene/thread summaries, and secret status without direct SQLite access.
4. Reduce Pilot context size under 24576 bytes while retaining safety fields and relevant setting excerpts.

After those are verified, the next planned MVP can return to `MVP-6: checkpoint, branching timeline, non-destructive rewind, and replayability`.

## Pilot-1 Context Budget V2 Follow-Up

Turn 4 of the stabilized rerun exposed a concrete context budget blocker: the candidate was `24957 / 24576` bytes and failed before pending context creation. The follow-up fix is tracked by `docs/PILOT1_CONTEXT_BUDGET_V2_SPEC.md`.

Confirmed root causes from budget forensics:

- `relevantSettings` could be trimmed to an empty section while `canonRevisionSummary.relevantRevisionIds` still carried verbose provenance.
- Character `profile.initialRuntimeTemplate` duplicated runtime information already present in `character_states.flags`.
- Recent events duplicated `summary` inside `payload.summary`.
- Settings, memories and knowledge needed to share global remaining bytes instead of letting an early budget pass discard high-value memories before setting excerpts were compacted.

Required next validation before continuing Pilot turns:

- Run `explain-context-budget` on the formal DB only in read-only mode.
- Reproduce prepare/audit/abort on `data/pilot1-budget-v2-e2e.db`, not on the formal Pilot DB.
- Resume formal Turn 4 only after context bytes fit under `24576`, audit passes, and formal `stateVersion=3` / `nextTurnNumber=4` remain unchanged.

## Pilot-1 Context Metadata Normalization Follow-Up

After seven stabilized rerun turns, formal Turn 8 exposed a schema/metadata blocker in read-only `explain-context-budget`:

- before trimming: `65854` bytes
- after trimming: `24621 / 24576` bytes
- over limit: `45` bytes
- `otherMetadata`: `16997` bytes
- `contextBudgetMetadata`: `1460` bytes

The fix normalizes final `ContextPacket` output before byte/hash accounting:

- unknown top-level fields are rejected by `audit-context`;
- detailed forensics moved to explain/audit sidecars;
- `otherMetadata` is no longer a catch-all for formal protocol fields;
- legacy `characterMemories` output is omitted in finalized multi-turn context;
- `contextBudget` uses compact reason codes instead of long diagnostic prose;
- active character runtime state and setting/event records are compacted deterministically.

Turn 8 copy-DB E2E passed on `data/pilot1-metadata-normalization-e2e.db`:

- explain: `24350 / 24576` bytes
- prepared context audit: `24347 / 24576`, hash valid
- pending context aborted
- copy session remained `stateVersion=7`, `nextTurnNumber=8`, `pendingContextCount=0`

Formal DB validation was read-only only:

- formal explain: `24350 / 24576`, `otherMetadata=2`, `contextBudgetMetadata=859`
- formal preflight: PASS, `stateVersion=7`, `nextTurnNumber=8`, `pendingContextCount=0`
- formal Turn 8 was not prepared or resolved.

Residual observation: Turn 8 still omitted all retrieved memories, knowledge claims and setting excerpts under the hard byte limit. This is now an explicit budget tradeoff rather than a metadata leak, but future Pilot review should monitor whether zero setting excerpts weakens character or scene fidelity.
