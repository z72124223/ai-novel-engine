# AGENTS.md — Playable Depth Reboot Rules V2

## 1. Mandatory Current Entry

Every Agent must read in this order:

1. `PROJECT_STATE.md`
2. `.codex/project_controller.md`
3. `docs/CURRENT_ROUTE_GATE_V2.md`
4. `docs/work-packages/REBOOT-01_PLAYABLE_DEPTH_SLICE.md`
5. `docs/REBOOT_2026_PLAYABLE_DEPTH_BLUEPRINT_V1.md`
6. `docs/REBOOT_2026_CREATIVE_WORLD_EXECUTION_ADDENDUM_V1.md`
7. `docs/CODEX_MODEL_AND_BUDGET_POLICY_V1.md`
8. this file
9. only the evidence required by the current REBOOT Stage

Historical HANDOFF, image Work Packages, route cards, charters, README status text and old `PLANS.md` sections are evidence only.

## 2. Active Goal

The active goal is a real playable depth slice:

```text
engine reality baseline
→ world／SQL／Runtime audit
→ validation simplification
→ SQL run-evidence persistence
→ non-Canon world candidate patch in Sandbox
→ bounded NPC autonomy
→ drama／critics／predictability checks
→ 5–10 turn Chenxing playable slice
→ replay and rule evolution
→ minimal local Web interface
```

Images are paused after one user-approved candidate. Video is future provider-neutral bake-off work with no current spending authorization.

## 3. Core Engine Rules That Remain Non-Negotiable

- Kotlin/JVM remains the engine core.
- SQLite remains formal Runtime state.
- JSON remains controlled interchange/evidence.
- Formal State changes go through existing Service／Repository／Validator／StateReducer and Transaction boundaries.
- Preserve `contextHash`, `stateVersion`, duplicate-result protection and Session／Turn identity.
- Character knowledge, memory, location, item reachability, capabilities and costs remain bounded.
- Migrations are additive; historical migrations remain immutable.
- AI／NPC／Drama agents may propose, score and revise; they do not directly write Canon or formal world State.
- Formal Canon, formal visual assets, destructive changes, release and paid providers require explicit human approval.
- API keys and credentials never enter Git or logs.

## 4. Retired Rules

The following old permanent modes are retired for REBOOT-01:

- permanent prohibition on localhost Web Server, local port, Ktor, React or TypeScript;
- every quality warning blocks persistence;
- content must be near-perfect before Run Evidence is saved;
- Codex always uses High／Max;
- all work must remain single-agent;
- every internal Stage must return to GPT;
- full test coverage can replace real play evidence;
- script-only or image-first is permanently the only mainline.

## 5. Validation Simplification

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
- atomic Transaction.

### C. Irreversible Approval

- Canon promotion;
- formal asset acceptance;
- destructive delete／overwrite;
- external release;
- paid provider/API;
- weakening security or State safety.

Quality issues are persisted as scores/warnings:

- prose quality;
- pacing;
- repetition;
- AI-writing feel;
- weak subtext;
- information dumping;
- emotional impact;
- predictability;
- reversal fairness;
- character-intelligence risk.

Poor drafts may be saved as Run Evidence. They must not be confused with applied State or approved content.

## 6. SQL-First Evidence

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
AudienceExpectation evidence
model/reasoning metadata
elapsed time
```

World-illegal results do not apply State Delta, but their evidence remains useful.

## 7. World Reality Rule

A formal file is not proof the engine uses its content.

Verify separately:

```text
formal source exists
SQL contains it
Runtime Context retrieves it
Agent／Scene uses it
Scenario can trigger it
```

REBOOT-01 must create:

```text
docs/reboot/REBOOT01_WORLD_AND_CHARACTER_REALITY_AUDIT.md
out/reboot01/world_audit/world_reality_inventory.json
```

It must also create a non-Canon Chenxing World Patch Candidate, run parse／validate／diff／impact preview, and apply it only to a resettable Sandbox.

Do not modify AUTHOR_LOCKED Canon without a True Stop decision.

## 8. NPC Autonomy

Important NPCs should support identity, values, goals, needs, fears, beliefs, knowledge boundary, relationships, memory stream, reflection, plan, schedule, location, resources and offscreen agenda.

Autonomy flow:

```text
observation filtered by perception／knowledge
→ memory candidate
→ importance
→ optional reflection
→ goal／plan update
→ action proposal
→ Game Master legality resolution
→ transactional result or rejected-evidence record
```

NPCs act from their own information and interests. They must not become tools solely because the player needs exposition. Offscreen actions require DecisionTrace and replay evidence.

## 9. Drama, Voice And Predictability

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
→ warmth／attachment／romance potential
→ unease
→ conflict
→ value dilemma
→ anger／loss／reflection
→ relationship change
→ partial hope or new unresolved pull
```

For the REBOOT Sandbox, test a non-Canon protagonist Voice Profile with:

```text
observational sarcasm
self-deprecating humor
risk-aware commentary
social consequences
serious-scene restraint
```

Do not create a nonstop joke machine or omniscient author avatar. Humor must not erase grief or other characters' pain.

Any character entering obvious danger must have a motive, knowledge gap, pressure, duty, constraint or calculated tradeoff. Otherwise mark `IDIOT_PLOT_RISK`.

Track `AudienceExpectationLedger` or equivalent. Prefer fair character-driven reversal or second-order consequences over random twists.

The protagonist may become increasingly powerful and influential, but power must increase responsibility, cost and moral pressure. Supporting characters and opponents require independent growth.

The long-running world uses local closure and global continuation. Arcs resolve meaningful questions and leave permanent State; the overall world continues.

## 10. Critic And Revision Rules

Do not let one Agent generate, grade and declare itself perfect.

Use independent roles where useful:

- LogicCritic;
- CharacterDramaCritic;
- ProsePacingCritic;
- audience expectation／predictability checks inside `NarrativeSystemsCritic` or equivalent.

Automatic revision is limited to one pass:

```text
raw draft
→ independent critics
→ one bounded revision
→ world-legality re-check
→ save both versions
```

No infinite self-revision loop.

## 11. Narrative Stall

If two consecutive turns have no meaningful change in scene goal, scene value, Runtime State, relationship State, cost or reveal, mark `NARRATIVE_STALL`.

The next step must escalate conflict, force a choice, reveal a fact, impose a cost or end／transition the scene. Do not generate longer equivalent dialogue.

## 12. Replay And Evolution

All rule changes are versioned.

Use the same Scenario Seed to compare old and new rules on:

- legal apply rate;
- stall count;
- character autonomy;
- emotional-wave coverage;
- irreversible changes;
- repetition／AI-prose risk;
- audience predictability;
- twist fairness;
- character intelligence;
- world-data usage;
- sarcasm character fit;
- serious-scene restraint;
- elapsed time;
- retries;
- model／reasoning.

Adopt a new rule only when evidence improves.

## 13. Multi-Agent And Model Policy

REBOOT-01 allows up to four bounded Subagents:

- RepositoryExplorer;
- RuntimeTester;
- NarrativeSystemsCritic;
- CostAndComplexityAuditor.

Subagents primarily perform read-only work. Only one Writer edits core Kotlin／SQL at a time.

Default:

```text
GPT-5.6 Codex coding model
MEDIUM reasoning
```

Use High only for Migration／Transaction／Validator／StateReducer／Knowledge Boundary／World Source Authority conflict／difficult consistency or replay bugs／final integration.

Use faster modes or scripts for repetitive work, hashes, JSON, paths, diffs and statistics.

Max／highest-cost mode is prohibited unless the model policy conditions are met.

## 14. Local Web Rule

The old permanent prohibition on Web Server, local ports, Ktor, React and TypeScript is retired.

REBOOT-01 may create a local-only playable interface after the engine slice passes.

Allowed:

- localhost API and local port;
- Kotlin HTTP layer;
- React／TypeScript or smaller compatible UI;
- story/input/state/save/load/debug views.

Not allowed:

- public deployment;
- cloud auth/payment;
- exposing SQLite directly to browser code;
- external paid model APIs;
- rewriting the engine core in JavaScript.

## 15. Historical Runtime Rules Still Applicable

### Scene／Thread Runtime

- Keep scene, thread, world event, time and transition writes behind `resolve-turn` validation and one Transaction.
- `story_threads.director_goal` must never become character knowledge or motive.
- `READY_TO_CLOSE` allows transition; it does not force transition.

### Setting Pack

- Setting Pack remains parse → validate → diff → impact → transactional apply.
- Author-only secrets do not enter character-facing Context.
- Active sessions block unsafe Canon patching.

### Context Budget

- Keep the existing hard byte ceiling unless a separately approved measured change proves a new policy is necessary.
- Do not put diagnostics into finalized AI-facing Context.
- Do not substitute recent facts for real memories.
- Referenced capabilities require authorization, prerequisites, limitations and costs.

### MVP-8 Provisional Layer

- Provisional content cannot overwrite locked Canon.
- AI may propose; formal promotion still requires the established flow and human confirmation.
- Avoid desireless, costless, blind-spot-free information-delivery NPCs.
- Author-only secrets remain protected.

## 16. Git Discipline

- Fetch latest remote State first.
- Use a clean `reboot/playable-depth-v1` worktree.
- Preserve the unrelated old dirty worktree.
- Use one Commit per internal Stage.
- Inspect exact Diff and run `git diff --check`.
- Do not use broad stage, force or destructive commands.

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

## 17. Image And Video Boundary

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

Do not purchase, call or integrate paid media APIs during REBOOT-01. Do not store API keys in Git.

## 18. Development Journal

Use `docs/development_journal/INDEX.md` to understand origin and past failures.

Append major direction changes, world audit findings, real-playtest lessons, rule retirements, adopted public-project ideas, creative principles and model/provider policy changes.

Do not include credentials, hidden prompts or invented history.

## 19. True Stop Conditions

Stop only for:

- formal Canon／character fate／romance／visual taste decision;
- paid API／provider／API key;
- unavoidable destructive Git operation;
- unresolved AUTHOR_LOCKED conflict;
- required weakening of Transaction／Knowledge／Version／Hash／approval boundaries;
- one Medium and one High evidence-based attempt both failing to restore the core loop.

Ordinary bugs, additive migrations, Sandbox candidate design, bounded refactors, local Web choices and Critic disagreement are not user stops.
