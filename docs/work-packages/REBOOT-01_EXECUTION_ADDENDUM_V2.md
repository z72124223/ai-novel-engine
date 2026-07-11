# REBOOT-01 Execution Addendum V2 — World Reality, Narrative Voice, and Fair Surprise

Status: `MANDATORY_ACTIVE_ADDENDUM`

Date: 2026-07-11

Parent documents:

- `docs/work-packages/REBOOT-01_PLAYABLE_DEPTH_SLICE.md`
- `docs/REBOOT_2026_PLAYABLE_DEPTH_BLUEPRINT_V1.md`
- `docs/CODEX_MODEL_AND_BUDGET_POLICY_V1.md`
- `docs/CURRENT_ROUTE_GATE_V2.md`

This addendum is mandatory. It closes gaps found after the first REBOOT-01 blueprint was written. When it conflicts with an older REBOOT-01 stage description, this addendum controls.

## 1. Why This Addendum Exists

The user identified four missing requirements:

1. The project has many plans, but formal worldbuilding, SQL contents, runtime usability, and actual playable depth have not been audited together.
2. The protagonist needs a sharp-tongued, skeptical, tsukkomi-like voice that can challenge predictable genre behavior without becoming omniscient or breaking the world.
3. Predictable plot progression is a major quality risk; surprise must be earned, causal, and character-driven rather than random.
4. Codex must complete the whole REBOOT-01 umbrella package in one explicitly started CLI program, not return to the old GPT control window after every internal stage.

The new operational priority is:

```text
world/source/SQL reality
→ playable rules
→ autonomous characters
→ dramatic and emotional rhythm
→ anti-stall and fair-surprise loop
→ 5–10 real turns
→ replay and evolution
→ local Web slice
```

## 2. Mandatory Stage 0A — World, Canon, SQL, and Runtime Reality Audit

Run this after the original Stage 0 baseline and before changing narrative systems.

### 2.1 Goal

Determine what the project actually knows and can use, not what documents merely say is planned.

Audit these layers separately:

```text
raw source
formal author-locked source
setting-pack / canon candidate
SQLite imported world source
runtime state
provisional / candidate content
samples and historical fixtures
planning-only documents
```

### 2.2 Required Map

For every world-critical entity used by the vertical slice, record:

```text
entityId
entityType
sourceAuthority
formalSourcePaths
sqlPresence
sqlRecordIds
runtimePresence
settingPackPresence
provisionalPresence
knowledgeVisibility
currentRevision
contradictions
missingFields
planOnlyClaims
usableInScenario
recommendedAction
```

Minimum categories:

- protagonist and Aijiang;
- two important NPCs;
- at least one opposing character, faction, institution, natural force, or social pressure;
- opening location and immediate adjacent locations;
- time period and material culture;
- technology and communication limits;
- economic resources and scarcity;
- factions and power relationships;
- abilities, items, costs, and hard limits;
- known secrets and character knowledge boundaries;
- current plot promises and unresolved causal questions.

### 2.3 Required Classification

Use only these primary classifications:

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

Do not treat a planning document, class name, schema, sample, or test fixture as implemented world content.

### 2.4 What Codex May Fix Automatically

Codex may complete technical alignment without asking the user when the content is already author-approved and the change is non-creative:

- repair broken world-source hashes or byte-size metadata;
- repair deterministic import/query/index paths;
- import existing approved world-source data through the existing controlled CLI;
- add missing non-creative IDs, source links, provenance, or indexes;
- repair runtime initialization from existing approved templates;
- add additive migrations required only to preserve run evidence or existing approved data;
- correct stale documentation that falsely claims an implemented feature is absent or a plan-only feature is complete.

All such changes require tests or executable evidence.

### 2.5 What Codex Must Not Silently Decide

Codex must not silently invent or finalize:

- a new AUTHOR_LOCKED fact;
- a character death, romance outcome, betrayal, permanent moral choice, or final allegiance;
- a new power-system law that changes existing Canon;
- a hidden culprit or sealed causal answer;
- a final geopolitical outcome;
- a formal visual style or approved image.

When the vertical slice requires missing creative data, use one of these routes:

1. existing MVP-8 provisional content;
2. a clearly labeled non-canon scenario fixture;
3. a candidate Setting Pack patch that is not applied to formal Canon.

Do not stop REBOOT-01 merely because a non-canon test detail is missing.

### 2.6 Required Outputs

```text
docs/reboot/REBOOT01_WORLD_CANON_SQL_REALITY_REPORT.md
out/reboot01/world_reality/**
samples/reboot01/world_runtime_fixture/**
```

If a candidate patch is useful:

```text
out/reboot01/candidate_setting_pack_patch/**
```

The report must identify every `PLAN_ONLY_NOT_IMPLEMENTED` claim that affects the vertical slice.

### 2.7 Success Criteria

- the selected scenario can query its required world facts from controlled sources;
- formal source, SQL, runtime, and provisional layers are not conflated;
- missing creative facts are isolated as provisional/non-canon;
- existing approved data is actually reachable by the engine;
- no world-critical fact exists only in a long planning document without being flagged;
- at least one real SQL-backed session can start from the audited data.

## 3. Protagonist Voice Contract — Sharp Tongue and Tsukkomi

The protagonist may gain a sharp-tongued, skeptical, and tsukkomi-like voice. This is a narrative and character-system requirement, not permission to turn every line into a joke.

### 3.1 Narrative Function

The voice should:

- say what a genre-aware audience is already thinking;
- question obviously dangerous or irrational behavior;
- expose hypocrisy, empty authority, and convenient exposition;
- reduce the feeling that the story forces characters to act stupidly;
- create warmth and familiarity through recurring verbal habits;
- act as an emotional defense mechanism;
- make later silence, fear, sincerity, anger, or grief more powerful by contrast.

### 3.2 Required Character Fields

Use existing character models where possible; do not create duplicates merely to match names.

Conceptually support:

```text
humorStyle
sarcasmTargets
riskLiteracy
tsukkomiTriggers
tensionDeflectionPattern
emotionalMask
lineHeWillNotCross
peopleHeWillNotMock
humorFailureConditions
sincerityBreakConditions
```

### 3.3 Rational Horror / Danger Behavior

When a situation resembles an obvious danger pattern, the protagonist should normally:

- notice the danger;
- state or imply the obvious risk;
- seek information, tools, exits, allies, or safer alternatives;
- refuse senseless separation unless pressure makes it rational;
- learn when bullets, ordinary force, or repeated tactics fail;
- avoid repeating an already disproven action without a new reason.

The plot must create difficulty through limited information, conflicting values, time pressure, resource scarcity, deception, responsibility, or unavoidable cost—not by making otherwise competent characters forget obvious danger.

### 3.4 Limits

The protagonist is not omniscient.

He may still make mistakes because of:

- incomplete or false information;
- pride;
- curiosity;
- attachment;
- fatigue;
- trauma;
- social obligation;
- moral refusal;
- a deliberate gamble;
- protecting another person.

Sarcasm must have consequences. It can:

- hurt an ally;
- reveal insecurity;
- provoke an authority figure;
- fail in front of real grief;
- become inappropriate and force an apology;
- stop when the character is truly afraid or emotionally exposed.

### 3.5 Prose Rule

Do not generate a joke quota.

A joke is valid only when it serves at least one of:

- character revelation;
- relationship chemistry;
- tension release;
- reader-proxy observation;
- setup for later emotional contrast;
- critique of irrational behavior;
- concealment of fear or affection.

Repeated meme language, modern internet slang that does not fit the world, and constant quipping during tragedy are quality failures.

## 4. Fair Surprise and Audience Prediction Control

The project should lower predictability without using arbitrary twists.

### 4.1 Audience Prediction Critic

The narrative critic must estimate the three most likely audience predictions for the next meaningful beat:

```text
prediction1
prediction2
prediction3
confidence
whyExpected
```

This is not shown to the reader.

The Drama Director must then decide whether to:

- fulfill the expectation because payoff is earned;
- delay it while adding new pressure;
- invert it through character choice;
- redirect it through a previously planted causal fact;
- let the expected event happen but change its cost or meaning.

It must not reject the most likely prediction merely for novelty.

### 4.2 Fair-Surprise Ledger

For each major surprise record:

```text
setupEvidence
readerAssumption
misdirection
hiddenButLegalCause
reveal
immediateConsequence
longTermConsequence
retrospectiveFairness
```

A surprise fails if it requires information that never existed, violates world rules, depends on sudden stupidity, or has no consequence after the reveal.

### 4.3 Predictability Risk Signals

Flag:

- the same scene structure repeated three times;
- every suspicious character being guilty;
- every warning being ignored;
- every victory arriving at the expected moment;
- every side character serving one obvious function;
- every apparent death being reversed;
- every conflict escalating only through a larger enemy;
- the protagonist always choosing the morally safest option without cost;
- exposition immediately followed by the exact event it described;
- the antagonist waiting for the protagonist instead of advancing independently.

### 4.4 Surprise Safety

Do not use:

- random betrayal without relationship history;
- sudden hidden powers with no cost or setup;
- secret twins or memory wipes as default solutions;
- retroactive world-rule changes;
- fake-out death loops;
- endless mystery-box postponement;
- shock violence as the only emotional tool.

## 5. Emotional Rhythm and Long-Running Structure

The active rhythm remains:

```text
fun / curiosity
→ character chemistry
→ warmth / romance / belonging
→ unease
→ pressure
→ value conflict
→ anger / loss / reflection
→ changed relationship
→ earned relief or hope
```

The system must support a reader laughing, then becoming angry, thoughtful, or unexpectedly moved because the emotional turn grew from the same characters and prior choices.

### 5.1 Strength Growth

The protagonist may grow from ordinary participant to a person capable of changing battles, institutions, or history.

Every major power increase must also increase at least one of:

- responsibility;
- visibility;
- enemies;
- dependence from others;
- moral pressure;
- political consequence;
- personal sacrifice;
- risk of losing ordinary life.

### 5.2 No Final Ending

Use:

```text
local closure + persistent world continuation
```

Each arc must close meaningful promises while leaving characters alive in a world that continues. No-final-ending is not permission for unresolved repetition.

## 6. Multi-Agent Execution for REBOOT-01

Use one main writer and up to four subagents.

Recommended roles:

1. `WorldRealityAuditor`
   - source/formal/SQL/runtime mapping;
   - plan-only detection;
   - import and query evidence.
2. `RuntimeTester`
   - compile, tests, real CLI sessions, SQL snapshots, replay.
3. `NarrativeSystemsCritic`
   - autonomy, emotional wave, protagonist voice, fair surprise, stall and AI-prose risk.
4. `CostAndComplexityAuditor`
   - model level, repeated reads, document growth, retries, elapsed time, test value.

The Main Orchestrator is the only core-code writer.

Subagents should run in parallel only for independent read-heavy tasks. They must return concise evidence summaries with file paths and commands, not dump full intermediate reasoning.

## 7. Model and Budget Override

Default main Codex execution:

```text
model: current GPT-5.6 Codex-capable model
reasoning effort: MEDIUM
```

Use Low or a lower-cost GPT-5.6 tier for scans, inventories, log classification, and repeated checks.

Use High only for:

- additive migration and SQL transaction design;
- Validator / StateReducer / Knowledge Boundary changes;
- high-risk Git history or data recovery;
- a difficult reproducible core bug;
- final integrator review.

Return to Medium after the high-risk task is resolved.

Do not use Max/Pro by default.

## 8. No-Return Autonomous Program Rule

The user has authorized one explicitly started Codex CLI session to complete REBOOT-01 end to end.

Codex must:

- fetch the latest default branch;
- create a clean `reboot/playable-depth-v1` branch/worktree;
- run all REBOOT-01 stages, including this addendum;
- make one focused Commit per stage;
- run required verification;
- continue automatically to the next internal stage;
- push the final branch;
- report directly to the user in the Codex interface;
- not require the user to return to the old GPT total-control window.

Ordinary bugs, test failures, small architecture choices, missing non-canon fixture details, and local Web technology choice are not stop conditions.

True stop conditions remain:

- irreversible Canon or character-fate decision;
- final romance outcome;
- paid provider/API spending or API key access;
- destructive Git action with no safe alternative;
- unresolved AUTHOR_LOCKED source conflict;
- required weakening of transaction, knowledge, version/hash, or approval safety;
- Medium and High both fail on the same reproducible core blocker.

## 9. Deliverable Discipline

Do not create additional route, status, review, or approval documents unless one of these is true:

- required by an existing contract;
- contains executable evidence;
- records a real failure and rule change;
- is the final stage report.

Prefer:

```text
code + SQL + scenario + run evidence + replay comparison + usable Web slice
```

over more planning prose.

## 10. Required Final Evidence

REBOOT-01 is incomplete without:

- a world/formal/SQL/runtime reality report;
- at least one real SQL-backed session;
- 5–10 continuous turns;
- autonomous offscreen NPC action;
- protagonist voice evidence showing wit and limits;
- one fair surprise or expectation inversion with setup;
- one value dilemma and irreversible consequence;
- one laughter/warmth-to-pain/reflection emotional transition;
- raw and revised narrative saved together;
- critic scores and decision traces;
- same-seed replay comparison;
- local Web playable slice or a measured blocker report;
- exact model/reasoning/subagent/retry records;
- a final honest pass/fail report.

## 11. Reference Sources

Design influences remain architecture references, not code-copy instructions:

- Generative Agents: memory, reflection, planning, schedules;
- Concordia: agent proposals plus Game Master resolution;
- SOTOPIA: social objectives and multidimensional evaluation;
- TextWorld: reset/step/observe/replay;
- ink: deterministic flags, choices, and branch structure;
- Anthropic interpretability research: decision traces and counterfactual probes at the application layer.

Provider selection remains future work under `docs/VIDEO_PROVIDER_BAKEOFF_PLAN_V1.md` and does not block REBOOT-01.
