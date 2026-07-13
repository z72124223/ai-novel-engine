# WORLD-PHYSICS-05 — Embodied Scene Pilot

## Authorization

The Product Owner replied `開始` on 2026-07-14 after WORLD-PHYSICS-04 passed,
then instructed Codex to finish the package and continue only where the
reviewed blueprint has a clear, non-paid and non-Canon next step.

## Objective

Run one five-turn, story-grounded, non-Canon physical continuity pilot through
the existing formal runtime. The source scene is SCREEN-01 `scene-03`, the
Daping Coast recess rescue in 〈潮線之外〉. This package tests whether a rejected
dog-support action remains unwritten and whether a held tool, actor movement,
support placement and release remain coherent across later turns.

## Source Boundary

- story master: `samples/screen01/screen01_story_production_master.json`;
- scene list: `samples/screen01/screen01_scene_list.json`;
- selected scene: `scene-03` / `loc-daping-coast-recess`;
- source boundary: `NON_CANON_PILOT`;
- physical overlay boundary: `NON_CANON_EVALUATION`;
- exact source SHA-256 values must be recorded in the committed pilot report.

The overlay may reuse validated adult-human, medium-dog, rigid-tool,
stable-support and opening profiles. Its metric values are test assumptions,
not Chenxing Canon measurements.

## Five-Turn Contract

1. Propose adult support on Aijiang's shoulder; validation must reject it.
   Retry the same pending turn with a stable rock support and resolve safely.
2. Lift the nearby pry bar through the formal V2 state path.
3. Pass through the recess opening while holding the pry bar. The held tool
   must move with its holder and remain held.
4. Place the pry bar on the declared stable fulcrum surface.
5. Lift the pry bar again and drop it in the bounded safe zone.

The rejected proposal must not advance the story turn, physical version or
transition count. Rejected-attempt audit evidence is allowed; world-state
mutation is not.

## Required Runtime Path

```text
SCREEN-01 source and non-Canon overlay
→ MultiTurnRuntime.prepareTurn
→ AiTurnResultValidator and formal physical gates
→ PhysicalStateReducer
→ SqliteStoryRepository.resolvePendingTurn
→ same SQLite transaction and rollback boundary
→ read-only state and transition evidence
```

No test or fixture may write the physical tables directly.

## Allowed Scope

- V12 plan, project controller, state and blueprint route;
- evaluation-only model, fixture factory, runner and CLI;
- strict pilot JSON Schema, fixed pilot report and SHA-256 manifest;
- a minimal generic relation-following correction only if the pilot proves an
  existing held/supported/contained dependent can be left behind;
- focused tests and existing WORLD-PHYSICS regression tests.

## Prohibited Scope

- no new Migration or change to Migrations 001–016;
- no Setting Pack, Canon, Knowledge, Memory or SCREEN-01 story edit;
- no free-text physical extraction, AI provider, API Key or paid call;
- no model download, image, video, audio, Web server or game UI;
- no answer-key disclosure or alteration of NPC-ACTOR-03/04 evidence;
- no direct SQL, force push, broad staging or original dirty-worktree edits.

## Completion Evidence

- exactly five resolved turns from one formal Session;
- one rejected dog-support attempt before the first resolution;
- zero rejected-attempt world-state mutations;
- exact source paths and SHA-256 values;
- per-turn story and physical versions, actions, relations and entity positions;
- holder/tool co-location after passage;
- expected support relation after placement and cleared relations after drop;
- strict JSON, Schema and SHA-256 evidence;
- focused, related and full Gradle tests;
- implementation report, explicit claim boundary and ordinary Git push.

## Completion Decision

Use `WORLD_PHYSICS05_EMBODIED_SCENE_PILOT_PASS` only if every turn uses the
formal runtime and all continuity checks pass. A pass proves one bounded
story-grounded trajectory, not complete physical understanding, live-model
quality or Canon truth.
