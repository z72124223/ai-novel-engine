# REBOOT-01 World And Character Reality Audit

Status: `STAGE_0_REALITY_AUDIT_COMPLETE`

Date: 2026-07-11

This audit distinguishes formal text, loaded SQLite data, Runtime-visible context, unused material, and missing REBOOT-01 requirements. It does not promote raw text, samples, or audit conclusions into Canon.

## Evidence order

1. `world/chenxing/formal/`
2. `world/chenxing/engine/chenxing_setting_pack_v1.json`
3. `memory/chenxing_world_source_manifest.json` and verified SQL import evidence
4. The Stage 0 baseline SQLite database
5. The generated turn context and applied session evidence
6. Raw text and samples, which remain `TEXT_ONLY`, `TEST_ONLY`, or `SUPERSEDED` unless a higher authority says otherwise

## Loaded opening reality

| Area | Status | Evidence |
|---|---|---|
| Project `chenxing` | `LOADED_IN_SQL` | setting pack + baseline DB |
| 陸塵行 | `AUTHOR_LOCKED`, `LOADED_IN_SQL` | formal protagonist pair + setting pack |
| 愛醬 | `AUTHOR_LOCKED`, `LOADED_IN_SQL` | formal protagonist pair + setting pack |
| 大坪海岸 opening | `AUTHOR_LOCKED`, `LOADED_IN_SQL` | formal opening arc + setting pack |
| Storm, tide, isolation pressure | `FORMAL_NOT_LOADED` with partial Runtime facts/rules | opening arc + setting pack |
| Two non-protagonist meaningful NPCs | `MISSING_REQUIRED` | only two protagonists are loaded |
| Adaptive value-based opponent | `MISSING_REQUIRED` | environmental pressure exists; no loaded adaptive actor |
| Persistent offscreen agenda | `MISSING_REQUIRED` | current models can express actions/events, not persistent NPC agenda evidence |
| Approved irreversible Chenxing consequence | `PROVISIONAL_CANDIDATE`, `RUNTIME_UNUSED` | durable deltas exist, but no approved scenario consequence exists |

## Protagonist reality

The formal protagonist evidence supports a curious, analytical, risk-aware lead whose weakness is overcommitting to a puzzle. The Stage 0 context loads concrete goals, emotions, inventory, available actions, location, and initiative. This is sufficient for an opening turn, but it is not yet a complete long-term personality planner.

The real baseline turn demonstrated that the character can choose observation and safety without violating the setting or revealing author-only information.

## 愛醬 reality

愛醬 is a co-protagonist, not a mascot, clue dispenser, combat item, or substitute for the two meaningful NPCs required by REBOOT-01. Formal evidence supports independent perception, warning behavior, attachment, and a growth line. Runtime loads a separate goal, emotion state, available actions, location, and initiative.

The successful Stage 0 turn exposed a useful gap: the narrative included 愛醬's behavior, but the result had no separate `characterAction` for her, producing `NPC_LIVELINESS_MISSING_ACTION`. Stage 2–4 should turn co-protagonist and NPC agency into explicit evidence instead of relying only on prose.

## Supporting cast and opposition gap

The opening Runtime currently has no two meaningful non-protagonist NPCs. Raw ensemble material and the older production sample contain candidate names and situations, but they also contain superseded identity or route assumptions. They remain `TEXT_ONLY` or `SUPERSEDED` and cannot be loaded automatically.

The coast provides a credible external countdown through weather, tide, unstable terrain, and reduced mobility. It does not provide an adaptive opponent with a defensible value position. REBOOT-01 therefore needs a non-Canon sandbox patch containing:

- two supporting NPCs with needs, costs, false beliefs, refusal ability, relationship debt, knowledge boundaries, and offscreen agendas;
- one adaptive opposing actor or force with a coherent value position;
- one social, resource, or institutional pressure that continues without the player;
- one resettable, demonstrably irreversible Runtime consequence.

## Offscreen autonomy reality

Existing models can express character actions, hidden motives, story-thread director pressure, scheduled world events, locations, and available actions. They do not yet provide evidence for persistent NPC schedules, offscreen planning, rejected alternatives, decision cost, or replayable `DecisionTrace`.

Status: `MISSING_REQUIRED`.

Stage 2 must add these as proposals and evidence. It must not let an NPC, Game Master, or Drama Director write SQLite directly.

## Irreversible consequence reality

The current engine can persist state deltas, events, memory, knowledge, emotion, scene, thread, world time, and provisional proposals. That is general durability, not proof of an approved irreversible story consequence.

Status: `PROVISIONAL_CANDIDATE`.

The first irreversible consequence must be created only in a resettable non-Canon scenario and must require the explicit irreversible-approval boundary defined by Stage 1.

## Narrative voice reality

| Marker | Current status | Evidence judgment |
|---|---|---|
| `DAILY_QUIP` | `PROVISIONAL_CANDIDATE`, `RUNTIME_UNUSED` | Possible character material exists, but no formal Runtime marker or scored scene exists. |
| `DANGER_OBSERVATION` | `PROVISIONAL_CANDIDATE`, `FORMAL_NOT_LOADED` | Protagonist observation and risk reasoning support it, but no marker-level execution evidence exists. |
| `GRIEF_RESTRAINT` | `PROVISIONAL_CANDIDATE`, `RUNTIME_UNUSED` | Style compatibility exists; no actual grief scene proves restraint. |

No marker is Canon. Stage 3 must measure them through scenario-level evidence, critic scores, and before/after revision rather than document claims.

## Secret and knowledge boundary

- Formal indexes identify author-only sources without making them player-visible.
- The setting pack keeps unrevealed secrets out of character knowledge.
- Context compilation excludes author-only and inaccessible knowledge.
- Scene `forbiddenReveals` are prohibitions, not prompts, goals, or character motives.
- The Stage 0 inventory records only counts and safe summaries; it does not copy secret contents.

The baseline DB had three unrevealed secrets and the applied result revealed none.

## World Source integrity

The formal World Source manifest is currently `CONTRADICTED` by the tracked bytes of `chenxing-world-raw-09-aijiang-detail`. The official import correctly blocks. Stage 0 used an audit-local observed manifest only to prove the query implementation and temporary SQL archive path.

This is not permission to edit the manifest or raw source. A later source-authority decision must choose whether to restore the original 3019-byte source or formally accept the 3051-byte tracked source.

## Reality inventory

Machine-readable local evidence:

`out/reboot01/world_audit/world_reality_inventory.json`

The file is intentionally under ignored `out/**`. It records entity status, SQL counts, source-integrity mismatch, secret-boundary checks, and Stage 2 candidate requirements without becoming Canon.

## Stage 0 conclusion

The current repository is a real executable state engine with a valid Chenxing opening pair and environment. It is not yet the promised living-world drama slice. The missing cast, adaptive opposition, offscreen agenda, irreversible consequence, explicit voice evidence, and replayable decision traces must be added as bounded non-Canon candidates in later REBOOT stages.

Verdict: `REALITY_BASELINE_SUFFICIENT_TO_CONTINUE`.
