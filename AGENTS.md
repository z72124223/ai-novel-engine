# AGENTS.md — Playable Depth Reboot Rules

## 1. Mandatory Current Entry

Every agent must read in this order before treating any route as current:

1. `PROJECT_STATE.md`
2. `.codex/project_controller.md`
3. `docs/CURRENT_ROUTE_GATE_V2.md`
4. `docs/work-packages/REBOOT-01_PLAYABLE_DEPTH_SLICE.md`
5. `docs/REBOOT_2026_PLAYABLE_DEPTH_BLUEPRINT_V1.md`
6. `docs/CODEX_MODEL_AND_BUDGET_POLICY_V1.md`
7. this file
8. only the evidence required by the current REBOOT Stage

Historical files may contain `current`, `active`, `approved` or `目前定案`; those labels are date-scoped and do not override `PROJECT_STATE.md`.

## 2. Current Product Route

```text
engine reality baseline
→ validation simplification
→ SQL run-evidence persistence
→ bounded NPC autonomy
→ drama and independent critics
→ 5–10 turn Chenxing playable slice
→ replay and rule evolution
→ minimal local Web interface
```

Images are paused after one user-approved candidate. Video is future provider-neutral bake-off work with no current spending authorization.

## 3. Core Engine Rules That Remain Non-Negotiable

- Kotlin/JVM remains the engine core.
- SQLite remains formal Runtime state.
- JSON remains a controlled interchange/evidence format.
- Formal state changes go through existing Service／Repository／Validator／StateReducer and transaction boundaries.
- Preserve `contextHash`, `stateVersion`, duplicate-result protection and Session／Turn identity.
- Character knowledge, memory, location, item reachability, capabilities and costs remain bounded.
- Migrations are additive; do not edit historical migrations.
- AI／NPC／Drama agents may propose, score and revise; they do not directly write Canon or formal world state.
- Formal Canon, formal visual assets, destructive changes, release and paid providers require explicit human approval.
- API keys and credentials never enter Git.

## 4. Validation Simplification

Only these are hard blockers:

### A. Protocol Safety

- parseable data;
- required identity fields;
- Session／Turn／Version／Hash match;
- no duplicate apply.

### B. World Legality

- legal location;
- reachable item;
- available capability and cost;
- character knowledge boundary;
- valid State Delta;
- atomic transaction.

### C. Irreversible Approval

- Canon promotion;
- formal asset acceptance;
- destructive delete／overwrite;
- external release;
- paid provider/API;
- weakening security or state-safety boundaries.

These are not hard blockers and should be persisted as scores/warnings:

- prose quality;
- pacing;
- repetition;
- AI-writing feel;
- weak subtext;
- information dumping;
- emotional impact;
- scene focus;
- stylistic preference.

A poor-quality draft may be stored as Run Evidence. It must not be confused with applied state or approved content.

## 5. SQL-First Evidence

For every generated or simulated turn, preserve when available:

```text
run id
scenario seed
session and turn
player input
context
raw draft
critic scores
revision suggestion
revised draft
AI result
validation findings
state delta
apply result
DecisionTrace
model/reasoning metadata
elapsed time
```

World-illegal results do not apply State Delta, but their evidence remains useful.

Do not require near-perfect narrative scores before saving evidence.

## 6. NPC Autonomy

Important NPCs should support:

```text
identity
values
goals
needs
fears
beliefs
knowledge boundary
relationships
memory stream
reflection
plan
schedule
location
resources
offscreen agenda
```

Autonomy flow:

```text
observation filtered by perception/knowledge
→ memory candidate
→ importance
→ optional reflection
→ goal/plan update
→ action proposal
→ Game Master legality resolution
→ transactional result or rejected-evidence record
```

Rules:

- the world may progress while the player is absent;
- NPCs act from their own information and interests;
- NPCs must not become tools solely because the player needs exposition;
- offscreen actions require DecisionTrace and replay evidence;
- no NPC may bypass formal state application or Canon rules.

## 7. Drama And Narrative Rules

Each meaningful scene tracks:

```text
sceneGoal
dramaticQuestion
activeValueConflict
stakes
valueBefore
valueAfter
cost
irreversibleChange
relationshipChange
reveal
emotionalBeat
promiseAndPayoff
```

Desired emotional wave:

```text
humor／daily life
→ warmth／attachment／romance
→ unease
→ conflict
→ value dilemma
→ anger／loss／reflection
→ relationship change
→ partial hope or new unresolved pull
```

The protagonist may become increasingly powerful and influential, but power must increase responsibility, cost and moral pressure. Supporting characters and opponents require independent growth.

The long-running world uses local closure and global continuation. Arcs resolve meaningful questions and leave permanent state; the overall world continues.

## 8. Critic And Revision Rules

Do not let one Agent generate, grade and declare itself perfect.

Use independent Critic roles where useful:

- LogicCritic;
- CharacterDramaCritic;
- ProsePacingCritic.

Critics produce scores and actionable findings.

Automatic revision is limited to one pass:

```text
raw draft
→ independent critics
→ one bounded revision
→ world-legality re-check
→ save both versions
```

No infinite self-revision loop.

## 9. Narrative Stall

If two consecutive turns have no meaningful change in:

- scene goal;
- scene value;
- Runtime state;
- relationship state;
- cost;
- reveal;

mark `NARRATIVE_STALL`.

The next step must escalate conflict, force a choice, reveal a fact, impose a cost or end/transition the scene. Do not generate longer equivalent dialogue.

## 10. Replay And Evolution

All rule changes are versioned.

Use the same Scenario Seed to compare old and new rules on:

- legal apply rate;
- stall count;
- character autonomy;
- emotional-wave coverage;
- irreversible changes;
- repetition／AI-prose risk;
- elapsed time;
- retries;
- model／reasoning.

Adopt a new rule only when evidence improves.

## 11. Multi-Agent Policy

REBOOT-01 allows up to four bounded Subagents:

- RepositoryExplorer;
- RuntimeTester;
- NarrativeSystemsCritic;
- CostAndComplexityAuditor.

Subagents should primarily perform read-only work. Only one Writer edits core Kotlin／SQL at a time.

Do not parallel-edit `Models.kt`, `Main.kt`, migrations, repositories, transactions or core validators.

## 12. Model And Cost Policy

Default:

```text
GPT-5.6 Codex coding model
MEDIUM reasoning
```

Use High only for:

- Migration／Transaction;
- Validator／StateReducer／Knowledge Boundary;
- difficult consistency bugs;
- high-risk data/Git integration;
- final integration review;
- a blocker that survived an evidence-based Medium attempt.

Use faster modes or scripts for repetitive work.

Max/highest-cost mode is prohibited unless `docs/CODEX_MODEL_AND_BUDGET_POLICY_V1.md` conditions are met.

## 13. Local Web Rule

The old permanent prohibition on Web Server, local ports, Ktor, React and TypeScript is retired.

REBOOT-01 Stage 6 may create a local-only playable interface after the engine slice passes.

Allowed:

- localhost API;
- local port;
- Kotlin HTTP layer;
- React／TypeScript or a smaller compatible UI;
- story/input/state/save/load/debug views.

Not allowed in REBOOT-01:

- public deployment;
- cloud auth/payment;
- exposing SQLite directly to browser code;
- external paid model APIs;
- rewriting the engine core in JavaScript.

## 14. Image And Video Boundary

Current image state:

```text
IMAGE_LANE_PAUSED_AFTER_USER_APPROVED_CANDIDATE
```

The Daping Coast panorama remains a candidate until Codex Intake／Hash／Candidate Reference completes. Do not continue image tasks during REBOOT-01.

When image work resumes, select character rendering style before final scene style.

Current video state:

```text
FUTURE_PROVIDER_BAKEOFF_NO_SPEND
```

Do not purchase, call or integrate OpenAI, Google, xAI, ByteDance, Jimeng, Runway or other paid media APIs during REBOOT-01.

Do not store API keys in Git.

## 15. MVP And Historical Runtime Rules Still Applicable

### Scene／Thread Runtime

- Keep scene, thread, world event, time and transition writes behind `resolve-turn` validation and one transaction.
- `story_threads.director_goal` must never become character knowledge or motive.
- `READY_TO_CLOSE` allows transition; it does not force transition.
- Characters act only from their own goals, emotions, memories, knowledge, location, items and actions.

### Setting Pack

- Historical migrations remain immutable.
- Setting Pack remains parse → validate → diff → impact → transactional apply.
- Author-only secrets do not enter character-facing context.
- Active sessions block unsafe Canon patching.

### Context Budget

- Keep the existing hard byte ceiling unless a separately approved measured change proves a new policy is necessary.
- Do not put diagnostics into finalized AI-facing Context.
- Do not substitute recent facts for real memories.
- Referenced capabilities require authorization, prerequisites, limitations and costs.

### MVP-8 Provisional Layer

- Provisional content cannot overwrite locked Canon.
- AI may propose; formal promotion still requires the established promotion flow and human confirmation.
- Avoid desireless, costless, blind-spot-free information-delivery NPCs.
- Author-only secrets remain protected.

## 16. Git Discipline

- Fetch latest remote state first.
- Use a clean `reboot/playable-depth-v1` worktree.
- Preserve the unrelated 123-item dirty worktree.
- Use one Commit per internal Stage.
- Inspect exact Diff and run `git diff --check`.
- Do not use broad stage/force/destructive commands.

Forbidden by default:

```text
git add .
git add -A
git commit -a
git push --force
git push --force-with-lease
git reset --hard
git clean -fd
```

## 17. Development Journal

Use `docs/development_journal/INDEX.md` to understand origin and past failures.

Append major direction changes, real-playtest lessons, rule retirements, adopted public-project ideas and model/provider policy changes.

Do not include credentials, hidden prompts or invented history.

## 18. True Stop Conditions

Stop only for:

- formal Canon／character fate／romance／visual taste decision;
- paid API／provider／API key;
- unavoidable destructive Git operation;
- unresolved AUTHOR_LOCKED conflict;
- required weakening of Transaction／Knowledge／Version／Hash／approval boundaries;
- one Medium and one High evidence-based attempt both failing to restore the core loop.

Ordinary bugs, test failures, bounded refactors, additive migrations, local Web choices and Critic disagreement are not user stop conditions.

## 19. REBOOT-01 Execution

REBOOT-01 is an approved Umbrella Work Package. One explicitly started Codex CLI session may execute all internal stages through final Push and report once.

Do not claim completion without running the actual 5–10 turn slice, replay evidence and local playable interface or a documented unavoidable blocker.