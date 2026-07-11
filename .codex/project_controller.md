# Project Controller — Autonomous Playable Depth Reboot V3

## 1. Role

Codex CLI is the Lead Engineer for `AI Novel Engine`.

The user owns final product direction, formal Canon, permanent character fate, romance direction, visual taste, provider spending and final media approval.

For `REBOOT-01`, Codex must execute the active Umbrella Work Package end-to-end rather than returning ordinary implementation planning to the user.

## 2. Mandatory Single Entry

Every Codex session starts with:

```text
PROJECT_STATE.md
```

Then read in order:

1. `.codex/project_controller.md`
2. `docs/CURRENT_ROUTE_GATE_V3.md`
3. `docs/work-packages/REBOOT-01_PLAYABLE_DEPTH_SLICE.md`
4. `docs/work-packages/REBOOT-01_EXECUTION_ADDENDUM_V1.md`
5. `docs/REBOOT_2026_PLAYABLE_DEPTH_BLUEPRINT_V1.md`
6. `docs/REBOOT_2026_CREATIVE_WORLD_EXECUTION_ADDENDUM_V1.md`
7. `docs/REBOOT_2026_NARRATIVE_VOICE_AND_UNPREDICTABILITY_ADDENDUM_V1.md`
8. `docs/CODEX_MODEL_AND_BUDGET_POLICY_V2.md`
9. `docs/development_journal/INDEX.md`
10. `AGENTS.md`
11. only the code, tests, world sources and historical evidence required by the current Stage

Do not use old image Work Packages, old HANDOFF, old route cards, historical charters, README status text, Route Gates V1/V2, Model Policy V1 or old `PLANS.md` sections as current routing authority.

## 3. Active Route

```text
Reality baseline
→ World／SQL／Runtime reality audit
→ validated non-Canon world candidate patch in Sandbox
→ validation simplification
→ SQL run-evidence persistence
→ bounded offscreen NPC autonomy
→ Drama Director／Critics／Audience Expectation checks
→ 5–10 turn Chenxing playable slice
→ same-seed replay and evolution
→ minimal local Web playable slice
→ final integration report
```

Images are paused after one user-approved panorama candidate. Video is a future provider bake-off with no spending authorization.

## 4. Umbrella Execution Authorization

`REBOOT-01 Playable Depth Slice` is explicitly approved as an Umbrella Work Package.

One user-started Codex CLI session may:

```text
create clean reboot branch/worktree
→ complete every internal Stage
→ validate and Commit each Stage
→ continue without returning for ordinary decisions
→ Push final branch
→ report once
```

Do not stop after each Stage. Do not request a new GPT pointer for routine implementation choices.

This exception applies only to REBOOT-01. It is not permission for unattended infinite product expansion.

## 5. Model Selection

Project defaults are defined in `.codex/config.toml`:

```text
model = gpt-5.6-sol
reasoning_effort = medium
```

Follow `docs/CODEX_MODEL_AND_BUDGET_POLICY_V2.md`.

Routing:

- main integration and ordinary implementation: `gpt-5.6-sol / medium`;
- repository exploration: `gpt-5.6-terra / low`;
- Runtime／test analysis: `gpt-5.6-terra / medium`;
- narrative systems criticism: `gpt-5.6-sol / medium`;
- cost／complexity audit: `gpt-5.6-luna / low`;
- high-risk read-only review: `gpt-5.6-sol / high`.

Use High only for:

- additive Migration design;
- SQLite Transaction／Repository write boundaries;
- Validator／StateReducer／Knowledge Boundary／contextHash／stateVersion;
- World Source Authority conflict;
- hard consistency or replay bugs;
- high-risk Git/data integration;
- final integration review;
- a core blocker that survived one evidence-based Medium attempt.

`xhigh`／`max`／`ultra` are prohibited by default. After a High-only problem is resolved, return to Medium.

Hashes, JSON, paths, diffs, statistics and benchmark arithmetic must use deterministic tools rather than expensive model reasoning.

## 6. Multi-Agent Policy

Codex may launch up to four direct Subagents with `max_depth = 1`.

Project profiles under `.codex/agents/` include:

- repository explorer;
- Runtime evidence tester;
- narrative systems critic;
- cost and complexity auditor;
- high-risk read-only reviewer as an on-demand replacement.

Use Subagents primarily for read-only exploration, criticism, test analysis and benchmark comparison.

Only the root/main Agent modifies core Kotlin／SQL. Do not let multiple agents simultaneously edit `Models.kt`, `Main.kt`, migrations, repositories, transactions or core validators.

The main Agent owns synthesis and final writes. A failed Subagent should be recorded and bypassed when the main Agent can continue.

## 7. World Reality Audit

Do not equate a formal JSON file with working engine data.

Codex must distinguish:

```text
formal source exists
SQL contains it
Runtime Context retrieves it
Agent or Scene uses it
Scenario can trigger it
```

REBOOT-01 must create:

```text
docs/reboot/REBOOT01_WORLD_AND_CHARACTER_REALITY_AUDIT.md
out/reboot01/world_audit/world_reality_inventory.json
```

At least one non-Canon Chenxing World Patch Candidate must pass:

```text
parse
→ validate
→ diff
→ impact preview
→ Sandbox apply
```

It may only be applied to a resettable non-Canon Sandbox. Do not modify AUTHOR_LOCKED Canon without a True Stop decision.

The final evidence must prove the patch entered Context／Runtime and was used by an NPC or world pressure.

## 8. Validation Simplification

Only three classes are hard blockers:

1. protocol safety;
2. world legality／transaction safety;
3. irreversible human approval.

Quality issues such as prose style, pacing, repetition, weak subtext, emotional impact or predictability are `WARNING／SCORE`, not reasons to discard Run Evidence.

Preserve when available:

```text
raw draft
critic scores
revision suggestion
revised draft
validation findings
rejected state delta
accepted state delta
apply result
DecisionTrace
AudienceExpectationLedger
model/reasoning metadata
elapsed time
replay seed
```

A world-illegal result must not apply State, but its evidence should still be saved.

## 9. Runtime And SQL Boundary

Always preserve:

- SQLite formal state;
- transactionality;
- `stateVersion`;
- `contextHash`;
- duplicate-result protection;
- character knowledge boundaries;
- location／item／capability legality;
- additive migrations only;
- existing Canon promotion boundaries.

Do not directly patch formal Runtime data with ad hoc SQL. Use Repository／Service／CLI paths.

Raw and failed Run Evidence may use an additive evidence table or an existing evidence structure, but must not masquerade as applied formal state.

## 10. NPC Autonomy Boundary

NPCs may perceive, remember, reflect, plan, schedule and act offscreen.

They may only use their own location, knowledge, memories, beliefs, goals, fears, relationships, resources and reachable actions.

NPC Agents propose actions. A Game Master／existing validation path resolves legal outcomes. NPCs do not directly write Canon or formal world state.

Every autonomous action requires a queryable DecisionTrace and replay evidence.

## 11. Drama, Voice And Fair Surprise

The Drama Director selects which legal conflict is worth presenting. It must not force characters to violate identity, inject Director goals into knowledge, manufacture impossible actions or change formal state through prose.

Independent Critics:

- Logic;
- Character／Drama;
- Prose／Pacing;
- Audience Expectation／Fair Surprise dimensions.

Automatic revision is limited to one pass. Re-run world legality after revision.

Lu Chenxing's REBOOT Sandbox voice candidate may include observational sarcasm, self-deprecating humor, risk-aware commentary and socially costly quips. It must not become fourth-wall omniscience, constant meme dialogue or humor that erases grief.

Any character entering obvious danger requires a documented motive, knowledge gap, urgency, obligation, constraint, false belief or calculated tradeoff. Otherwise record `IDIOT_PLOT_RISK`.

At least one first-order expected danger must be acknowledged or mitigated, followed by a causally fair second-order consequence.

## 12. Narrative Direction

The playable slice should demonstrate:

```text
humor／daily friction
→ warmth／attachment／romance
→ unease
→ conflict
→ value dilemma
→ cost／anger／loss／reflection
→ changed relationship
→ partial hope or unresolved pull
```

The protagonist can grow toward major influence, but power growth must add responsibility, sacrifice and moral pressure.

Important NPCs and opponents need independent growth and offscreen agendas.

The long-running world uses local closure and global continuation: arcs resolve meaningful promises and leave permanent state, while the world continues.

## 13. Web Boundary

The old permanent no-Web rule is retired.

After the engine and narrative Stages pass, REBOOT-01 may create a minimal local Web interface using a small Kotlin API plus React／TypeScript or a smaller compatible local UI.

Allowed:

- localhost;
- local port;
- story/input/state/debug views;
- save/load;
- Critic／DecisionTrace／State Delta developer view.

Not allowed during REBOOT-01:

- public deployment;
- authentication or payment;
- cloud database;
- exposing SQLite directly to browser code;
- paid external model APIs;
- rewriting the engine core in JavaScript.

## 14. Image And Video Status

Image lane:

```text
IMAGE_LANE_PAUSED_AFTER_USER_APPROVED_CANDIDATE
```

The Daping panorama is not yet an accepted formal reference. Do not continue tasks 02–07 or silently mark it Canon／approved／production-locked.

Video lane:

```text
FUTURE_PROVIDER_BAKEOFF_NO_SPEND
```

Future provider planning is in `docs/VIDEO_PROVIDER_BAKEOFF_PLAN_V2.md`.

Do not buy API access, start Provider jobs, store API keys, generate a whole episode or add Provider adapters during REBOOT-01.

## 15. Git Discipline

At package start:

1. `git fetch --prune origin`;
2. verify the remote default branch and exact base SHA;
3. create a clean worktree and `reboot/playable-depth-v1` branch;
4. never copy the old dirty worktree wholesale;
5. preserve unrelated work.

Do not use broad stage, force or destructive commands. Each internal Stage receives an isolated, reviewable Commit.

## 16. Development Journal

Read `docs/development_journal/INDEX.md` when origin, intent or prior failures are unclear.

Every Stage adds or updates an entry under:

```text
docs/development_journal/reboot01/
```

Record real failures, user criticism, model decisions, adopted and retired rules, world audit findings, playtest observations and replay results.

Do not include API keys, credentials, hidden prompts or invented history.

## 17. True Stop Conditions

Stop and ask the user only when:

- formal Canon, permanent character fate, romance or visual taste requires a decision;
- paid Provider／API／API Key is required;
- destructive Git action is unavoidable with no safe alternative;
- AUTHOR_LOCKED sources directly conflict and authority rules cannot resolve them;
- continuing requires weakening Transaction, Knowledge Boundary, `stateVersion`, `contextHash` or irreversible approval safety;
- one evidence-based Medium attempt and one evidence-based High attempt still cannot restore the same core blocker.

The following are not user stops:

- ordinary technical uncertainty;
- test failures;
- additive migrations;
- small refactors;
- local Web technology selection;
- Critic disagreement;
- low narrative scores;
- need for new Services／Models／Tests;
- Sandbox world candidate design.

## 18. Completion Contract

At final completion, Push `reboot/playable-depth-v1` and report once with:

```text
REBOOT-01 classification
model/reasoning by Stage
Subagents used
base SHA
branch and Commits
world reality audit
Sandbox patch and Runtime-use evidence
turns completed
NPC offscreen actions
value dilemma and irreversible outcomes
SQL/evidence persistence
Critic/revision/fair-surprise evidence
replay comparison
Web launch command
compile/tests
known failures
files changed
Provider spend
Canon changes
image lane status
recommended next step
```

Do not claim completion if the real 5–10 turn slice, replay evidence and local playable interface or an unavoidable documented blocker do not exist.