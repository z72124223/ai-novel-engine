# QuarterFull World Alignment

Status: `QUARTERFULL_ALIGNED_LOCAL_MEMORY`

Date: 2026-06-24

This document records the user's decision to replace the active local creative baseline with the visible QuarterFull draft for `塵行 · 第 1 章 · 塵`.

This document does not delete historical evidence, modify SQLite, apply a setting pack, change runtime behavior, save or clear QuarterFull content, call an AI API, generate storyboards, or approve 04 / 05 / 06 / 07 / 08 work.

## 1. Verdict

The active creative world baseline is now:

```text
QuarterFull visible draft: 塵行 · 第 1 章 · 塵
  -> local memory capsule
  -> future Writer Brief / script packet / storyboard planning
```

The local memory capsule is:

```text
memory/quarterfull_world_memory_capsule.json
```

"Delete our worldview" means:

- the active local memory now carries only the QuarterFull-aligned `塵行` baseline;
- legacy exclusion lists are not carried into the memory capsule or QuarterFull support prompt;
- repo history, docs, samples, setting packs, databases, and generated evidence are not physically deleted in this milestone;
- do not promote the new memory capsule into formal runtime canon until a later approved validation / import flow exists.

## 2. Active World Baseline

The current baseline world direction is extracted from QuarterFull's visible story draft:

- series: `塵行`;
- chapter: `第一話　塵`;
- protagonist: `陸知行`;
- companion: `愛醬`, a long-haired husky / snow-dog companion;
- opening texture: survival, language barrier, frontier settlement, practical observation, companion protection;
- first major location: `灰關`, a dirty, functional, hierarchical frontier fortress;
- visible social structure: grey-uniform patrol soldiers, cultivators / special practitioners, ordinary workers, marginal laborers;
- visible technology: pre-modern frontier craft plus unusual materials such as glowing stones, repair adhesive, black-mineral blue flame, and a suspicious industrial-looking pendant symbol;
- active mystery anchor: the scarred officer's metal pendant with an industrial-feeling symbol.

## 3. Memory Capsule Usage

Use `memory/quarterfull_world_memory_capsule.json` as the first local memory file when QuarterFull forgets continuity.

Recommended flow:

```text
QuarterFull drafts prose
  -> Codex extracts / updates local memory
  -> Codex prepares a concise memory prompt
  -> user pastes memory into QuarterFull
  -> QuarterFull continues prose
  -> Codex checks drift against local memory
```

The capsule has a `quarterfullSupportPrompt.prompt` field that can be pasted or summarized back into QuarterFull.

Do not use the memory capsule as:

- a setting pack;
- a SQLite import file;
- a runtime scene definition;
- an AI result JSON;
- an approved storyboard packet;
- an approved asset registry entry.

## 4. Alignment Guardrails

Continue with these rules:

- 陸知行 survives by observation, practical repair sense, caution, and protection of 愛醬.
- 愛醬 remains dog-like and sensory; she must not become a human-shaped clue machine.
- 灰關 should feel material, dirty, scarce, hierarchical, and lived-in.
- Cultivation / power can be visible, but the ranks, metaphysics, costs, and institutions remain unknown until defined by evidence.
- The industrial-looking pendant symbol is a clue, not a reveal schedule.
- Secrets should not leak through narration, dialogue, or convenient NPC explanation.
- World causality remains prior to plot objective.

## 5. Open Questions

These must remain open until the user or later review approves answers:

- Is `陸知行` permanently approved as the protagonist name?
- Is 愛醬 ordinary, unusually perceptive, or setting-touched?
- What is the scarred officer's name, title, and actual knowledge?
- What is the origin of the industrial-looking pendant symbol?
- What is the cultivation / power system cost model?
- What consequences should follow the market conflict after 愛醬 is kicked?

## 6. Verification

This support document is ready for review when:

- `git diff --check` passes for this document;
- `memory/quarterfull_world_memory_capsule.json` remains a known local-memory blocker until a separate owner-approved repair path makes it parse as JSON;
- no formal memory validation, SQLite, setting-pack, migration, Validator, StateReducer, or QuarterFull write action occurs.
