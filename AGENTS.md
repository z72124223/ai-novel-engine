# AGENTS.md — Autonomous Playable Depth Rules V3

## 1. Mandatory Current Entry

Every Agent must read in this order:

1. `PROJECT_STATE.md`
2. `.codex/project_controller.md`
3. `docs/CURRENT_ROUTE_GATE_V3.md`
4. `docs/work-packages/REBOOT-01_PLAYABLE_DEPTH_SLICE.md`
5. `docs/work-packages/REBOOT-01_EXECUTION_ADDENDUM_V1.md`
6. `docs/REBOOT_2026_PLAYABLE_DEPTH_BLUEPRINT_V1.md`
7. `docs/REBOOT_2026_CREATIVE_WORLD_EXECUTION_ADDENDUM_V1.md`
8. `docs/REBOOT_2026_NARRATIVE_VOICE_AND_UNPREDICTABILITY_ADDENDUM_V1.md`
9. `docs/CODEX_MODEL_AND_BUDGET_POLICY_V2.md`
10. `docs/development_journal/INDEX.md`
11. this file
12. only the evidence required by the active REBOOT Stage

Historical HANDOFF files, image Work Packages, Route Gates V1/V2, Model Policy V1, old route cards, charters, README status text and historical `PLANS.md` sections are evidence only.

## 2. Active Goal

The active goal is runnable evidence:

```text
engine reality baseline
→ world/formal/SQL/runtime reality audit
→ non-Canon Sandbox world patch
→ validation simplification and SQL run-evidence persistence
→ bounded offscreen NPC autonomy
→ Game Master／Drama Director／independent Critics
→ protagonist voice／anti-idiot-plot／fair-surprise controls
→ real 5–10 turn Chenxing slice
→ same-seed replay and measurable evolution
→ minimal local Web playable interface
→ honest final report and Push
```

Images are paused after one user-kept panorama candidate. Video is future provider-neutral bake-off work with no current spending authorization.

## 3. Core Engine Rules

- Kotlin/JVM remains the engine core.
- SQLite remains formal Runtime state.
- JSON remains a controlled interchange and evidence format.
- Formal state changes go through existing Service／Repository／Validator／StateReducer and one atomic Transaction.
- Preserve `contextHash`, `stateVersion`, Session／Turn identity and duplicate-result protection.
- Preserve character Knowledge Boundary, memory, location, item reachability, capability authorization and costs.
- Historical migrations are immutable; new migrations are additive only.
- AI／NPC／Drama agents may propose, score and revise; they never directly write formal state or Canon.
- Formal Canon, formal visual assets, destructive actions, release and paid providers require explicit human approval.
- API keys and credentials never enter Git, logs, prompts or reports.

## 4. Three Hard Gates Only

Hard blockers:

### A. Protocol Safety

- parseable data;
- required IDs;
- Session／Turn／Version／Hash match;
- no duplicate apply.

### B. World Legality

- legal location;
- reachable item;
- available ability and valid cost;
- character knowledge boundary;
- valid State Delta;
- atomic Transaction.

### C. Irreversible Approval

- formal Canon promotion;
- formal asset acceptance;
- destructive delete／overwrite;
- external release;
- paid provider/API;
- weakening a security or state-safety boundary.

The following are Scores/Warnings rather than persistence blockers:

- prose quality;
- pacing;
- repetition;
- AI-writing feel;
- weak subtext;
- information dumping;
- emotional impact;
- scene focus;
- predictability;
- stylistic preference.

A poor draft may be stored as Run Evidence. It must not be confused with applied state or approved content.

## 5. SQL-First Run Evidence

For each generated or simulated turn, preserve when available:

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
protocol/world findings
rejected State Delta
accepted State Delta
apply result
DecisionTrace
offscreen events
model/reasoning metadata
elapsed time
reader narrative
```

World-illegal results do not apply State, but their evidence remains queryable. Do not require near-perfect narrative scores before saving evidence.

## 6. World／Canon／SQL／Runtime Reality

Do not treat a formal file, schema, planning document or passing fixture as proof that the engine uses the data.

Prove separately:

```text
source authority exists
→ controlled import/query reaches it
→ SQL contains or indexes it
→ Runtime Context retrieves it
→ Agent／Scene uses it
→ Scenario can trigger it
```

Classify vertical-slice data as:

```text
FORMAL_AND_RUNTIME_READY
FORMAL_BUT_NOT_IN_SQL
IN_SQL_BUT_NOT_RUNTIME_USABLE
PROVISIONAL_RUNTIME_ONLY
PLAN_ONLY_NOT_IMPLEMENTED
HISTORICAL_OR_SUPERSEDED
CONFLICTING_AUTHORITIES
MISSING_FOR_VERTICAL_SLICE
```

Existing approved data may be technically repaired or imported through controlled paths. Missing creative details remain non-Canon Sandbox candidates. Do not silently invent AUTHOR_LOCKED facts.

REBOOT-01 must apply at least one validated non-Canon Chenxing World Patch Candidate to a resettable Sandbox and prove Runtime and NPC use.

## 7. NPC Autonomy

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
- NPCs must not become exposition tools solely because the player needs information;
- every offscreen action requires a replayable DecisionTrace;
- no NPC bypasses formal state or Canon rules.

## 8. Drama and Emotional Wave

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

Desired wave:

```text
humor／daily friction
→ warmth／attachment／romance
→ unease
→ conflict
→ value dilemma
→ anger／loss／reflection
→ relationship change
→ partial hope or new unresolved pull
```

The protagonist may grow toward major influence, but power must add responsibility, cost, enemies, political consequence and moral pressure. Important NPCs and opponents require independent growth.

The long-running world uses local closure and global continuation. Arcs resolve meaningful promises and leave permanent state while the world continues.

## 9. Protagonist Voice and Anti-Idiot-Plot

For the resettable non-Canon Scenario, Lu Chenxing may use:

```text
DAILY_QUIP
DANGER_OBSERVATION
GRIEF_RESTRAINT
```

He may use observational sarcasm, self-deprecation and risk-aware commentary. He must not become an omniscient author avatar or nonstop joke machine. Humor may create social consequences and should recede under genuine grief.

Any character entering obvious danger needs a traceable reason such as incomplete information, urgent duty, relationship pressure, time pressure, coercion, false belief, resource constraint, moral obligation or calculated risk. Otherwise record:

```text
IDIOT_PLOT_RISK
```

## 10. Fair Surprise and Audience Expectation

Track an `AudienceExpectationLedger` or equivalent.

Prefer:

```text
obvious danger recognized
→ intelligent first-layer countermeasure
→ opponent/world adaptation
→ fair second-order consequence
```

A surprise must have planted causality and retrospective fairness. Do not use collective stupidity, random betrayal, retroactive world rules, costless hidden powers or repeated fake deaths as substitutes for unpredictability.

## 11. Critics and Revision

Do not let one Agent generate, grade and declare itself perfect.

Use independent roles where useful:

- Logic Critic;
- Character／Drama Critic;
- Prose／Pacing Critic;
- Audience Expectation／Predictability evaluation.

Critics produce scores and actionable findings. Automatic revision is limited to one pass:

```text
raw draft
→ independent critics
→ one bounded revision
→ world-legality re-check
→ save both versions
```

No infinite self-revision loop.

## 12. Narrative Stall and Evolution

If two consecutive turns have no meaningful change in scene goal, scene value, Runtime state, relationship state, cost or reveal, mark:

```text
NARRATIVE_STALL
```

The next step must escalate conflict, force a choice, reveal a fact, impose a cost or end/transition the scene.

Version rule changes and compare them with the same Scenario Seed. Adopt a new rule only when executable evidence improves legality, autonomy, emotional-wave coverage, stall rate, predictability, cost and reader pull.

## 13. Model and Subagents

Default main model:

```text
gpt-5.6-sol / medium
```

Use:

- `gpt-5.6-terra / low` for repository exploration;
- `gpt-5.6-terra / medium` for runtime/test analysis;
- `gpt-5.6-sol / medium` for normal implementation and narrative systems;
- `gpt-5.6-luna / low` or scripts for repetitive cost/complexity checks;
- temporary `gpt-5.6-sol / high` only for Transaction, Migration, StateReducer, Validator, Knowledge, Version/Hash, high-risk integration or final read-only review.

XHigh／Max／Ultra are prohibited by default. Follow `docs/CODEX_MODEL_AND_BUDGET_POLICY_V2.md` and `.codex/config.toml`.

REBOOT-01 allows up to four direct Subagents with `max_depth = 1`. Only one Writer edits core Kotlin／SQL at a time. Do not parallel-edit `Models.kt`, `Main.kt`, migrations, repositories, transactions or core validators.

## 14. Local Web Rule

The old permanent prohibition on a local Web Server, local port, Ktor, React and TypeScript is retired.

REBOOT-01 Stage 6 may create a local-only playable interface after Stages 0–5 pass.

Allowed:

- localhost API;
- local port;
- Kotlin HTTP layer;
- React／TypeScript or a smaller compatible UI;
- story, input, state, save/load and developer evidence views.

Not authorized:

- public deployment;
- cloud authentication or payment;
- exposing raw SQLite to browser code;
- external paid model APIs;
- rewriting the engine core in JavaScript.

## 15. Image and Video Boundary

```text
IMAGE_LANE_PAUSED_AFTER_USER_APPROVED_CANDIDATE
VIDEO_LANE_FUTURE_PROVIDER_BAKEOFF_NO_SPEND
```

The Daping panorama remains a Candidate until Codex Intake／Hash／Candidate Reference completes. Do not continue image tasks during REBOOT-01.

When image work resumes, select character rendering style before final scene style.

Do not purchase, call or integrate OpenAI, Google, xAI, ByteDance, Jimeng, Runway or another paid media API during REBOOT-01. Future provider choice follows `docs/VIDEO_PROVIDER_BAKEOFF_PLAN_V2.md`.

## 16. Engine Rules Still Applicable

### Scene／Thread Runtime

- keep scene, thread, world event, time and transition writes behind `resolve-turn` validation and one transaction;
- `story_threads.director_goal` never becomes character knowledge or motive;
- `READY_TO_CLOSE` permits transition; it does not force it;
- characters act only from their own goals, emotions, memories, knowledge, location, items and actions.

### Setting Pack

- historical migrations remain immutable;
- Setting Pack remains parse → validate → diff → impact → transactional apply;
- author-only secrets do not enter character-facing context;
- active sessions block unsafe Canon patching.

### Context and Capabilities

- keep the existing hard Context ceiling unless a separately approved measured change proves otherwise;
- diagnostics stay out of finalized AI-facing Context;
- do not substitute recent facts for real memories;
- referenced capabilities require authorization, prerequisites, limitations and costs.

### MVP-8 Provisional Layer

- provisional content cannot overwrite locked Canon;
- AI may propose; formal promotion still requires established flow and human confirmation;
- avoid desireless, costless, blind-spot-free information-delivery NPCs;
- author-only secrets remain protected.

## 17. Git Discipline

- Fetch latest remote first.
- Use a clean `reboot/playable-depth-v1` worktree.
- Preserve unrelated dirty worktrees.
- Make focused Commit(s) per Stage.
- Inspect exact Diff and run `git diff --check`.
- Avoid unrelated refactors, formatting, renames, moves and duplicate implementations.

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

## 18. Development Journal

Every REBOOT Stage must add or update:

```text
docs/development_journal/reboot01/
```

Record failures, user criticism, model routing, rules retired/adopted, Runtime findings and replay results. Do not include credentials, hidden prompts or fabricated history.

## 19. True Stop Conditions

Stop only for:

- formal Canon or unresolved AUTHOR_LOCKED conflict;
- permanent character fate/death or final romance decision;
- final visual/taste approval;
- paid API／provider／API key;
- unavoidable destructive Git operation;
- required weakening of Transaction／Knowledge／Version／Hash／approval boundaries;
- the same reproducible core blocker surviving one evidenced Medium attempt and one evidenced High attempt.

Ordinary Bugs, tests, additive Migration, bounded refactors, non-Canon Sandbox design, local Web choice, Critic disagreement and low narrative scores are not stop conditions.

## 20. Autonomous REBOOT-01 Execution

One explicitly started Codex CLI session may execute all REBOOT-01 Stages through final Push and report once directly to the user.

Do not claim completion without:

- a real engine baseline;
- world/formal/SQL/runtime audit;
- a real SQL-backed 5–10 turn slice;
- offscreen NPC autonomy;
- Critic and DecisionTrace evidence;
- same-seed replay;
- local playable interface or measured blocker;
- final honest report.
