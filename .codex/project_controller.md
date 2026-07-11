# Project Controller — Playable Depth Reboot

## 1. Role

Codex CLI is the Lead Engineer for `AI Novel Engine`.

The user owns final product direction, formal Canon, character fate, romance direction, visual taste, provider spending and final media approval.

The active Repository files own ordinary implementation routing. For REBOOT-01, Codex must execute end-to-end rather than returning routine planning decisions to the user.

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
5. `docs/CODEX_MODEL_AND_BUDGET_POLICY_V1.md`
6. `AGENTS.md`
7. only the code, tests, world sources and historical evidence required by the current Stage

Do not treat these as current routing authority:

- `docs/CURRENT_ROUTE_GATE_V1.md`;
- old image Work Packages;
- old `docs/HANDOFF.md` content;
- old route cards;
- old `PLANS.md` sections;
- historical script-only or image-first charters;
- README status sentences.

They remain evidence only.

## 3. Active Route

```text
Reality baseline
→ simplify overblocking
→ persist SQL run evidence
→ bounded offscreen NPC autonomy
→ Drama Director and independent Critics
→ 5–10 turn Chenxing playable slice
→ same-seed replay and evolution
→ minimal local Web playable slice
→ final integration report
```

Images are paused after one user-approved panorama candidate. Video is a future provider bake-off with no spending authorization.

## 4. Umbrella Execution Authorization

`REBOOT-01 Playable Depth Slice` is an explicitly approved Umbrella Work Package.

One user-started Codex CLI session may:

```text
create clean reboot branch/worktree
→ complete Stage 0
→ validate and Commit
→ complete Stage 1
→ validate and Commit
→ continue through Stage 7
→ Push final branch
→ report once
```

Do not stop after each internal Stage. Do not request a new GPT total-control pointer for routine implementation choices.

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
- hard consistency or replay bugs;
- high-risk Git/data integration;
- final integration review;
- a core blocker that survived one evidence-based Medium attempt.

Use Low／fast modes or deterministic scripts for:

- search and file inventory;
- test failure grouping;
- repetitive documentation;
- hashes, JSON, paths, diffs and statistics;
- formatting and non-core templates.

`MAX`／highest-cost mode is prohibited unless all conditions in `docs/CODEX_MODEL_AND_BUDGET_POLICY_V1.md` are met.

After a High-only problem is resolved, return to Medium.

## 6. Multi-Agent Policy

Codex may launch up to four bounded Subagents:

- `RepositoryExplorer`;
- `RuntimeTester`;
- `NarrativeSystemsCritic`;
- `CostAndComplexityAuditor`.

Use Subagents primarily for read-only exploration, criticism, test analysis and benchmark comparison.

Only one Writer may modify core Kotlin／SQL at a time. Do not let multiple agents simultaneously edit `Models.kt`, `Main.kt`, migrations, repositories, transactions or core validators.

The main Codex process owns synthesis and final writes. A failed Subagent should be recorded and bypassed, not allowed to block the entire package when the main agent can continue.

## 7. Validation Simplification

Only three classes are hard blockers:

1. protocol safety;
2. world legality／transaction safety;
3. irreversible human approval.

Quality issues such as prose style, pacing, repetition, weak subtext, exposition and emotional impact are `WARNING／SCORE`, not reasons to discard Run Evidence.

Codex must preserve:

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

A world-illegal result must not apply state, but its evidence should still be saved.

## 8. Runtime And SQL Boundary

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

Do not directly patch formal runtime data with ad hoc SQL. Use Repository／Service／CLI paths.

Raw and failed Run Evidence may use a dedicated additive evidence table or existing evidence structure, but must not masquerade as applied formal state.

## 9. NPC Autonomy Boundary

NPCs may perceive, remember, reflect, plan, schedule and act offscreen.

They may only use their own:

- location;
- knowledge;
- memories;
- beliefs;
- goals;
- fears;
- relationships;
- resources;
- reachable actions.

NPC Agents propose actions. A Game Master／existing validation path resolves legal outcomes. NPCs do not directly write Canon or formal world state.

Every autonomous action must leave a queryable DecisionTrace and be replayable.

## 10. Drama And Critic Boundary

The Drama Director chooses which legal conflict is worth presenting. It must not:

- force characters to violate identity;
- inject Director goals into character knowledge;
- manufacture an impossible action;
- change formal state through prose;
- use tragedy as the only form of depth.

Use independent Critics:

- Logic;
- Character／Drama;
- Prose／Pacing.

Automatic revision is limited to one pass. Re-run world legality after revision.

## 11. Narrative Direction

The playable slice must include emotional contrast:

```text
humor／daily friction
→ warmth／attachment
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

## 12. Web Boundary

The old permanent no-Web rule is retired.

After REBOOT-01 Stages 0–5 pass, Stage 6 may create a minimal local Web interface using a small Kotlin API plus React／TypeScript or a smaller compatible local UI.

Allowed:

- localhost;
- local port;
- story/input/state/debug views;
- save/load;
- Critic／DecisionTrace／State Delta developer view.

Not allowed during REBOOT-01:

- public deployment;
- authentication;
- payment;
- cloud database;
- exposing SQLite directly to the browser;
- paid external model APIs;
- rewriting the engine core in JavaScript.

## 13. Image And Video Status

Image lane:

```text
IMAGE_LANE_PAUSED_AFTER_USER_APPROVED_CANDIDATE
```

The generated Daping panorama is not yet an accepted formal reference. Do not continue tasks 02–07 or silently mark it Canon／approved／production-locked.

Video lane:

```text
FUTURE_PROVIDER_BAKEOFF_NO_SPEND
```

Do not:

- buy API access;
- start provider jobs;
- store API keys;
- generate a whole episode;
- add provider adapters;
- resume image/video work during REBOOT-01.

Future provider research is documented in `docs/VIDEO_PROVIDER_BAKEOFF_PLAN_V1.md`.

## 14. Git Discipline

At package start:

1. `git fetch --prune origin`;
2. verify remote default branch and exact base SHA;
3. create a clean worktree and `reboot/playable-depth-v1` branch;
4. never copy the old 123-item dirty worktree wholesale;
5. preserve unrelated work.

Do not use:

```text
git add .
git add -A
git commit -a
git push --force
git push --force-with-lease
git reset --hard
git clean -fd
```

unless a separately approved recovery operation explicitly authorizes it.

Each internal Stage gets an isolated, reviewable Commit.

## 15. Development Journal

Read `docs/development_journal/INDEX.md` when origin, intent or prior failures are unclear.

Append a Journal entry when:

- the user changes product direction;
- a major architecture rule changes;
- an old rule is retired;
- a real playtest exposes an important problem;
- a provider/model/cost policy changes;
- a public project design is formally adopted;
- Codex discovers that the project drifted from its original goal.

Do not put API keys, credentials, hidden prompts or invented historical facts into the Journal.

## 16. True Stop Conditions

Stop and ask the user only when:

- formal Canon, character fate, romance or visual taste requires a decision;
- paid provider／API／API Key is required;
- destructive Git action is unavoidable with no safe alternative;
- AUTHOR_LOCKED sources directly conflict and authority rules cannot resolve them;
- continuing requires weakening Transaction, Knowledge Boundary, `stateVersion`, `contextHash` or irreversible approval safety;
- one evidence-based Medium attempt and one evidence-based High attempt still cannot restore the core build/test loop.

The following are not user stops:

- ordinary technical uncertainty;
- test failures;
- additive migrations;
- small refactors;
- local Web technology selection;
- Critic disagreement;
- need for new Services／Models／Tests;
- unrelated baseline failures that can be investigated.

## 17. Completion Contract

At final completion, Push `reboot/playable-depth-v1` and report once with:

```text
REBOOT-01 classification
model/reasoning by stage
Subagents used
base SHA
branch and Commits
turns completed
NPC offscreen actions
value dilemma and irreversible outcomes
SQL/evidence persistence
Critic/revision evidence
replay comparison
Web launch command
compile/tests
known failures
files changed
provider spend
Canon changes
image lane status
recommended next step
```

Do not claim completion if the 5–10 turn loop was not actually run.