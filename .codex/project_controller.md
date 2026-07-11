# Project Controller — Playable Depth Reboot V2

## 1. Role

Codex CLI is the Lead Engineer for `AI Novel Engine`.

The user owns final product direction, formal Canon, character fate, romance direction, visual taste, provider spending and final media approval.

For `REBOOT-01`, Codex must execute the active Umbrella Work Package end-to-end rather than returning ordinary implementation planning to the user.

## 2. Mandatory Single Entry

Every Codex session starts with:

```text
PROJECT_STATE.md
```

Then read in order:

1. `.codex/project_controller.md`
2. `docs/CURRENT_ROUTE_GATE_V2.md`
3. `docs/work-packages/REBOOT-01_PLAYABLE_DEPTH_SLICE.md`
4. `docs/REBOOT_2026_PLAYABLE_DEPTH_BLUEPRINT_V1.md`
5. `docs/REBOOT_2026_CREATIVE_WORLD_EXECUTION_ADDENDUM_V1.md`
6. `docs/CODEX_MODEL_AND_BUDGET_POLICY_V1.md`
7. `AGENTS.md`
8. only the code, tests, world sources and historical evidence required by the current Stage

Do not use old image Work Packages, old HANDOFF, old route cards, historical charters, README status text or old `PLANS.md` sections as current routing authority.

## 3. Active Route

```text
Reality baseline
→ World／SQL／Runtime reality audit
→ validation simplification
→ SQL run-evidence persistence
→ non-Canon world candidate patch in Sandbox
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

Default main model:

```text
current GPT-5.6 Codex coding model
reasoning_effort = MEDIUM
```

Use `HIGH` only for:

- additive Migration design;
- SQLite Transaction／Repository write boundaries;
- Validator／StateReducer／Knowledge Boundary／contextHash／stateVersion;
- World Source Authority conflict;
- hard consistency or replay bugs;
- high-risk Git/data integration;
- final integration review;
- a core blocker that survived one evidence-based Medium attempt.

Use Low／fast modes or deterministic scripts for:

- search and inventory;
- test failure grouping;
- repetitive documentation;
- hashes, JSON, paths, diffs and statistics;
- formatting and non-core templates.

`MAX`／highest-cost mode is prohibited unless all evidence conditions in `docs/CODEX_MODEL_AND_BUDGET_POLICY_V1.md` are met.

After a High-only problem is resolved, return to Medium.

## 6. Multi-Agent Policy

Codex may launch up to four bounded Subagents:

- `RepositoryExplorer`;
- `RuntimeTester`;
- `NarrativeSystemsCritic`;
- `CostAndComplexityAuditor`.

Use Subagents primarily for read-only exploration, criticism, test analysis and benchmark comparison.

Only one Writer may modify core Kotlin／SQL at a time. Do not let multiple agents simultaneously edit `Models.kt`, `Main.kt`, migrations, repositories, transactions or core validators.

The main Codex process owns synthesis and final writes. A failed Subagent should be recorded and bypassed when the main agent can continue.

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

At least one non-Canon Chenxing World Patch Candidate must pass parse／validate／diff／impact preview and be applied only to a resettable Sandbox.

Do not modify AUTHOR_LOCKED Canon without a True Stop decision.

## 8. Validation Simplification

Only three classes are hard blockers:

1. protocol safety;
2. world legality／transaction safety;
3. irreversible human approval.

Quality issues such as prose style, pacing, repetition, weak subtext, emotional impact or predictability are `WARNING／SCORE`, not reasons to discard Run Evidence.

Preserve:

```text
raw draft
critic scores
revision suggestion
revised draft
validation findings
state delta
apply result
model/reasoning metadata when available
elapsed time
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

Raw and failed Run Evidence may use a dedicated additive evidence table or existing evidence structure, but must not masquerade as applied formal State.

## 10. NPC Autonomy Boundary

NPCs may perceive, remember, reflect, plan, schedule and act offscreen.

They may only use their own location, knowledge, memories, beliefs, goals, fears, relationships, resources and reachable actions.

NPC Agents propose actions. A Game Master／existing validation path resolves legal outcomes. NPCs do not directly write Canon or formal world State.

Every autonomous action must leave a queryable `DecisionTrace` and be replayable.

## 11. Drama, Voice And Predictability

The Drama Director chooses which legal conflict is worth presenting. It must not force characters to violate identity, inject Director goals into character knowledge, manufacture impossible actions or change formal State through prose.

Use independent Critics:

- Logic;
- Character／Drama;
- Prose／Pacing;
- audience expectation and predictability checks inside `NarrativeSystemsCritic` or an equivalent bounded module.

Automatic revision is limited to one pass. Re-run world legality after revision.

The protagonist Voice Profile is a non-Canon REBOOT candidate and should support:

```text
observational sarcasm
self-deprecating humor
risk-aware commentary
social consequences
serious-scene restraint
```

Do not turn the protagonist into an omniscient author avatar or a nonstop joke machine.

Any character entering obvious danger must have a documented motive, knowledge gap, pressure, duty, constraint or calculated tradeoff. Otherwise record `IDIOT_PLOT_RISK`.

Track audience expectation and fair reversal. Random twists and forced character stupidity are not acceptable substitutes for unpredictability.

## 12. Narrative Direction

The playable slice must include emotional contrast:

```text
humor／daily friction
→ warmth／attachment／romance potential
→ unease
→ conflict
→ value dilemma
→ cost／anger／loss／reflection
→ changed relationship
→ partial hope or unresolved pull
```

The protagonist can grow toward major influence, but power growth must add responsibility, sacrifice and moral pressure.

Important NPCs and opponents need independent growth and offscreen agendas.

The long-running world uses local closure and global continuation: arcs resolve meaningful promises and leave permanent State, while the world continues.

## 13. Web Boundary

The old permanent no-Web rule is retired.

After the engine, autonomy, drama and replay Stages pass, REBOOT-01 may create a minimal local Web interface using a small Kotlin API plus React／TypeScript or a smaller compatible local UI.

Allowed:

- localhost and local port;
- story/input/state/debug views;
- save/load;
- Critic／DecisionTrace／State Delta developer view.

Not allowed during REBOOT-01:

- public deployment;
- authentication or payment;
- cloud database;
- exposing SQLite directly to the browser;
- paid external model APIs;
- rewriting the engine core in JavaScript.

## 14. Image And Video Status

Image lane:

```text
IMAGE_LANE_PAUSED_AFTER_USER_APPROVED_CANDIDATE
```

The generated Daping panorama is not yet an accepted formal reference. Do not continue tasks 02–07 or silently mark it Canon／approved／production-locked.

Future art order starts with character style selection before background style.

Video lane:

```text
VIDEO_LANE_FUTURE_BAKEOFF_NO_SPEND
```

Do not buy, call or integrate a paid provider during REBOOT-01. Future provider choice uses a provider-neutral `VideoShotHandoff` and the bake-off plan.

## 15. Journal Rule

Append the Development Journal whenever there is:

- a product-direction change;
- a major creative decision;
- a World／SQL reality finding;
- a retired rule;
- a true engine failure;
- a public-project design formally absorbed;
- a model／cost change;
- a user criticism showing drift from original intent.

Do not delete uncomfortable failure evidence or rewrite history to look cleaner.

## 16. Change Discipline

- Read before editing.
- Use a clean reboot worktree.
- Preserve unrelated modifications.
- No `git add .`, `git add -A`, force push, destructive reset, clean or broad restore without explicit recovery authorization.
- Avoid unrelated refactors, formatting, renames and moves.
- Do not create duplicate Services or schemas when existing ones can be extended.
- Planning, candidate files, technical PASS and file existence do not equal approval.

## 17. Validation Contract

At minimum:

- inspect exact diff;
- run `git diff --check`;
- parse every new JSON;
- run targeted compile/tests for code changes;
- run a real engine loop and preserve SQL evidence;
- distinguish formal Canon, Sandbox Candidate, Runtime Evidence and planning-only data;
- classify unrelated failures separately;
- never claim a test, world-data, visual or provider capability that was not actually exercised.

## 18. True Stop Conditions

Stop and request a decision only when:

- author-locked sources conflict and no source rule resolves them;
- formal Canon, character fate, romance or visual taste must be decided;
- paid provider/API spending or a key is required;
- the requested work would weaken Transaction, Hash/version, Knowledge Boundary or irreversible approval safety;
- Git or data cannot be made safe without destructive action;
- one evidence-based Medium attempt and one evidence-based High attempt still cannot restore the core loop.

Ordinary technical uncertainty, additive Migration, Sandbox candidate design, test failure, architecture choice or local Web implementation is not a user stop.

## 19. Completion

Complete every REBOOT-01 Stage, Commit each Stage, Push `reboot/playable-depth-v1`, and report once with:

- real commands and tests;
- real SQL／Runtime evidence;
- world audit and candidate patch evidence;
- 5–10 turn results;
- autonomy／drama／predictability scores;
- replay comparison;
- local Web evidence;
- model/reasoning and retries;
- remaining limitations and recommended next step.
