# Blueprint Redesign Decision Brief

Status: `DOCS_ONLY_BLUEPRINT_REDESIGN_DECISION`

Date: 2026-06-24

This brief reviews the current blueprint layer and decides what to preserve, cut, redesign, or postpone. It does not implement Kotlin source, tests, schema, migrations, Validator behavior, StateReducer behavior, `resolve-turn`, `stateVersion`, `contextHash`, CLI commands, SQLite data, setting-pack conversion, queryers, GPT automation, Seedance, Oiioii, video generation, storyboard generation, or 04 / 05 / 06 / 07 / 08 work.

## 1. Verdict

The current blueprint direction should be redesigned around a narrow product spine:

```text
low-friction author input
  -> new-world foundation brief
  -> non-canon blueprint candidate triage
  -> 09 world / situation / asset card organization
  -> demand-driven query templates
  -> script-ready scene packet
  -> reviewable storyboard packet
  -> optional future implementation only after approval
```

Preserve the stable engine and safety boundaries. Cut stale status claims, platform overreach, direct conversion, and auto-generation loops. Redesign the blueprint pool into selected, evidence-backed, source-restored, review-gated candidate batches.

## 2. Evidence Reviewed

- `AGENTS.md`
- `PLANS.md`
- `ROADMAP.md`
- `VERIFY.md`
- `DONE_CRITERIA.md`
- `docs/HANDOFF.md`
- `docs/PROJECT_COMPLETION_AUDIT.md`
- `docs/BLUEPRINT_ALIGNMENT.md`
- `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md`
- `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md`
- `docs/DEMAND_DRIVEN_QUERY_LAYER_BLUEPRINT.md`
- `docs/SIMULATION_FIRST_KNOWLEDGE_TRUTH_RULES.md`
- `docs/STORYBOARD_AND_CAMERA_PLANNING.md`
- `docs/PRODUCTION_ASSET_REGISTRY_PLANNING.md`
- `docs/ASSET_REGISTRY_IMPLEMENTATION_BLUEPRINT.md`
- `docs/ARCHITECTURE.md`
- `docs/DATA_MODEL.md`
- Current Git evidence: `68d6be9 docs: register blueprint planning data layer`

## 3. Preserve

Keep these as active blueprint anchors.

### 3.1 Stable Runtime Spine

Preserve:

- Kotlin/JVM CLI only;
- SQLite as formal source of truth;
- JSON turn protocol;
- `prepare-turn` / `resolve-turn` lifecycle;
- Validator and StateReducer guarded writes;
- `stateVersion`, `contextHash`, `resultId`, and transaction boundaries;
- context hard max `24576` actual UTF-8 bytes;
- setting-pack parse -> validate -> diff -> impact preview -> transaction apply;
- read-only audit and evidence export surfaces.

Reason: this is the project's strongest moat. It makes story generation auditable instead of merely convenient.

### 3.2 Simulation-First Truth Model

Preserve `docs/SIMULATION_FIRST_KNOWLEDGE_TRUTH_RULES.md` as a top product-layer guardrail.

Required principle:

- world truth is causality;
- actor knowledge follows contact, observation, role, access, evidence, bias, memory, and action;
- author-only boundaries prevent leaks but must not become reveal schedules.

Reason: this is the clearest difference from generic AI writing tools. It prevents "the plot knows" from replacing simulated life.

### 3.3 Storyboard-First Deliverable

Preserve the current deliverable target:

- reviewable storyboard images / packets first;
- high-quality animation and video tools only as optional future downstream work;
- scene lock, asset lock, and camera intent before any image or video pipeline.

Reason: storyboard-first creates a concrete review surface without prematurely committing to costly animation.

### 3.4 Manual Approval Chain

Preserve:

```text
01 explicit handoff
  -> 04 generation
  -> 05 review
  -> 06 display
  -> user explicit approval
  -> report to 01
  -> 07 asset candidates
  -> 08 storyboard pickup / verification
  -> user-reviewable storyboard delivery
```

Reason: silence and automation must not become approval.

### 3.5 Blueprint Register As Non-Canon Index

Preserve `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md` as an index of candidate inputs, not as canon.

Use it for:

- source-window provenance;
- candidate ids;
- verdict/status tracking;
- dependency and risk review;
- later selection.

Do not use it as:

- setting-pack content;
- runtime data;
- approved assets;
- a full world bible;
- a bulk conversion list.

## 4. Cut

These should be removed from active planning assumptions.

### 4.1 Old External Blueprint As Current Status

Cut any use of the external blueprint as current operational truth.

Reason: `docs/BLUEPRINT_ALIGNMENT.md` already marks it `KEEP_AS_DESIGN_INTENT_NOT_CURRENT_STATE`. Its stale notes about Pilot-1, MVP-6, Turn 11, coverage gaps, packaging, and Android-like future scope must not drive work.

### 4.2 Qingli As New Opening Base

Cut Qingli as a new-world opening base or assetization starting point.

Keep Qingli only as:

- historical sample;
- process evidence;
- dry-run format example;
- source of lessons about scene locks, review gates, and author-only boundaries.

Reason: `docs/WORLD_BIBLE_REBUILD_DECISION_BRIEF.md`, `ROADMAP.md`, and `docs/HANDOFF.md` already reject Qingli as the new base.

### 4.3 Direct Bulk Conversion

Cut the idea that the whole blueprint pool can become a setting pack, runtime data, or asset registry.

Reason: the pool is non-canon, partially source-dependent, and currently includes corrupted human-readable names. Bulk conversion would bypass foundation decisions and overload context.

### 4.4 Cloud Platform / Mobile Marketplace Scope

Cut any QuarterFull-style platform imitation from this repo:

- no web server;
- no mobile app;
- no React / Next.js;
- no cloud API;
- no OpenAI API integration;
- no marketplace / rankings / payment / ad stack;
- no reader platform implementation.

Reason: the repo's explicit boundary is local Kotlin/JVM CLI, SQLite, and JSON files.

Useful learning from QuarterFull should stay at the product-pattern level:

- reduce author friction;
- provide guided blueprint flow;
- preserve version history / creative evidence;
- make revision easy;
- track cost and scope before generation.

### 4.5 Candidate B / Pre-Resolve Gate By Default

Cut any assumption that narrative-quality audit work should become a runtime gate.

Reason: MVP-7 approved scope remains read-only Candidate A/A.1/A.2. Candidate B is not approved.

### 4.6 Auto-Resuming 04 / 05 / 06 / 07 / 08

Cut automatic resumption, scheduled wakeups, or "continue unless stopped" behavior for production windows.

Reason: current control docs require manual handoff only.

### 4.7 Guessing Corrupted Blueprint Names

Cut any attempt to restore mojibake / corrupted names by inference.

Reason: the ids and systems can guide triage, but names need source-window restoration before promotion.

## 5. Redesign

These areas should be redesigned before any implementation or conversion.

### 5.1 Blueprint Layer Hierarchy

Replace the current flat pool with a staged hierarchy:

```text
Product Direction
  -> World Foundation
  -> Blueprint Candidate Pool
  -> Selected Batch
  -> 09 Cards
  -> Query Templates
  -> Scene Packets
  -> Asset Candidates
  -> Storyboard Packets
  -> Optional Runtime / SQLite / Tooling Implementation
```

Each layer must say:

- whether it is canon;
- whether it is user-facing;
- whether it can enter prompts;
- whether it can enter setting packs;
- whether it can touch runtime.

### 5.2 Candidate Status Model

Replace broad "PASS by source window" confidence with repo-local status:

| Status | Meaning | Allowed next action |
| --- | --- | --- |
| `indexed` | Candidate exists in the register. | Source restoration or triage. |
| `source_restored` | Human-readable names and source evidence are confirmed. | Product review. |
| `foundation_compatible` | Compatible with approved new-world foundation. | Batch selection. |
| `selected_for_batch` | 01/user selected it for a specific purpose. | Decision brief. |
| `conversion_candidate` | Ready to plan setting-pack or template conversion. | Separate conversion plan. |
| `rejected_or_retired` | Not compatible or no longer useful. | Archive only. |

Current pool entries should remain `indexed` until source text and new-world fit are confirmed.

### 5.3 First-Batch Selection Rule

Redesign first-batch conversion around low-coupling system skeletons, not characters or dense lore.

Best first batch after a foundation brief:

- resource pressure basics;
- social / safety-zone constraints;
- simple formation / boundary rules if needed;
- power-system costs and limitations only at the lowest shared level;
- material culture limits that affect scenes and assets.

Avoid in the first batch:

- full NPC cards;
- hidden factions;
- advanced weapons / pills / poison / skill trees;
- large metaphysics;
- future-arc lore;
- any corrupted-name card that has not been source-restored.

Reason: the first batch should help 04 avoid invention without flooding 04 with author-only systems.

### 5.4 Story Card Queryer Naming

Redesign `Story Card Queryer` language to default to:

```text
Situation / Continuity / Pressure Queryer
```

Reason: "Story Card" can imply next-plot selection. The safer meaning is local situation, actor objective, continuity, pressure, evidence path, misconception, and constraints.

Keep `Story Card Queryer` only as a search alias until docs are fully renamed.

### 5.5 Asset Registry Path

Redesign asset registry work as two phases before SQLite:

1. asset id / template / storage policy;
2. draft-file validation and review checklist.

Only after that should a separate decision brief choose:

- same runtime DB versus sidecar DB;
- tracked references versus ignored local outputs;
- additive migration scope;
- import / export CLI.

Reason: visual asset drift is a real problem, but jumping straight to schema would mix production assets with runtime state too early.

### 5.6 Evidence Ledger For Creative Work

Add a future product concept: creative evidence ledger.

This is not implementation yet. It should combine:

- user input;
- questionnaire answers;
- selected blueprint candidates;
- 09 cards;
- query responses;
- scene packet revisions;
- asset candidate versions;
- storyboard frame approvals;
- final user approval events.

Reason: this is the local-engine version of a "creative fingerprint" without becoming a cloud platform. It supports provenance, review, and authorship evidence.

## 6. Postpone

Postpone these until earlier decisions exist.

| Work | Postpone Until |
| --- | --- |
| `NEW_WORLD_FOUNDATION_BRIEF` | User / 00 answers enough of the questionnaire. |
| 09 world / situation / asset card organization | Foundation brief exists and is approved. |
| Query request / response templates | 09 card organization exists or 01 explicitly asks to make query layer concrete. |
| First-batch blueprint conversion decision | 01 selects candidate systems for review. |
| Setting-pack conversion | Separate parse / validate / diff / preview / apply milestone is approved. |
| Asset registry schema / migrations | Draft templates, validation rules, and sidecar-vs-runtime DB decision exist. |
| 04 / 05 / 06 / 07 / 08 resume | 01/user explicitly resumes manual handoff flow. |
| GPT storyboard pickup | Approved packet, asset candidates, and storyboard-frame template exist. |
| Oiioii / Seedance / video | Approved storyboard packet and separate downstream decision exist. |
| Candidate B | Separate MVP-7 decision brief and explicit approval. |

## 7. Recommended New Blueprint Roadmap

### Phase 0: Current Decision Brief

Output:

- this document;
- no code;
- no runtime changes.

### Phase 1: Foundation

Input:

- user / 00 answers to `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md`.

Output:

- `NEW_WORLD_FOUNDATION_BRIEF`;
- preserved seeds;
- new-world premise;
- material culture;
- power / skill boundaries;
- initial scene candidates;
- social structure;
- author-only boundaries;
- storyboard suitability notes.

### Phase 2: Source Restoration And Pool Triage

Input:

- `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md`;
- source windows for corrupted names and source details.

Output:

- source-restored candidate list;
- cut / keep / redesign / postpone verdict per candidate;
- no setting-pack conversion.

### Phase 3: 09 Card Organization

Output:

- world cards;
- situation / continuity / pressure cards;
- asset need cards;
- visibility classifications;
- evidence paths;
- local misconceptions;
- actor knowledge boundaries.

### Phase 4: Query Templates

Output:

- World Card Queryer template;
- Situation / Continuity / Pressure Queryer template;
- Asset Queryer template;
- 05 query-scope review checklist.

### Phase 5: First Minimal Conversion Decision

Output:

- decision on whether any source-restored candidates should become a minimal setting-pack skeleton;
- required validation plan;
- explicit non-goals.

### Phase 6: Optional Implementation

Only after explicit approval:

- setting-pack conversion;
- draft asset validation;
- asset registry implementation;
- packet / storyboard audits;
- runtime or CLI extensions.

## 8. Immediate Cut / Redesign Matrix

| Area | Current State | Decision | Reason |
| --- | --- | --- | --- |
| External old blueprint | Design intent plus stale operational notes | Preserve intent, cut status claims | Repo docs supersede it. |
| Runtime spine | MVP-6 stable plus read-only MVP-7 audit | Preserve | Strongest safety / evidence base. |
| Qingli | Historical sample | Cut as new base, preserve as evidence | New world rebuild rejected it as opening base. |
| Blueprint pool | Non-canon indexed candidates | Redesign into staged status model | Current pool is not source-restored or foundation-compatible yet. |
| Mojibake names | Corrupted human-readable labels | Cut from promotion path until restored | Guessing names would create false canon. |
| Character candidate `bp-character-lu-hesheng-v0.2` | Non-canon retained candidate | Postpone | Character/NPC card is too coupled for first batch. |
| `bp-46-nine-system-supplement-v0.3` | Overall reference | Postpone / use only as overview | Individual v0.4 cards take priority where overlap exists. |
| Queryers | Future product-layer specs | Redesign into templates later | Not implemented and not ready without foundation / 09 cards. |
| Story Card Queryer wording | Ambiguous | Rename meaning to Situation / Continuity / Pressure | Avoid next-plot behavior. |
| Asset registry | Future planning | Redesign into draft templates before SQLite | Schema first would be premature. |
| Storyboard deliverable | Current target | Preserve | Concrete, reviewable output. |
| Oiioii / Seedance | Optional future downstream | Postpone | Costly and out of current scope. |
| Candidate B | Not approved | Cut from default path | Requires separate decision. |

## 9. Review Checklist For Future Blueprint Work

Before promoting any blueprint item, ask:

- Is the source restored and readable?
- Is the item compatible with the new-world foundation?
- Does it introduce canon, setting-pack content, runtime behavior, or asset approval?
- Does it need user / 01 approval first?
- Does it preserve simulation-first causality and actor knowledge boundaries?
- Does it help 04 / 05 / 06 / 07 / 08 without flooding them?
- Does it require a setting-pack conversion path?
- Does it create a production asset, or only describe a world rule?
- Does it belong in docs, draft files, SQLite, or runtime?
- What is the smallest verification step?

## 10. Next Safe Step

The next safe step is not implementation.

Recommended:

```text
User / 00 answers enough of docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md
  -> 01 assigns NEW_WORLD_FOUNDATION_BRIEF
  -> only then triage blueprint candidates against the approved foundation
```

If the user wants a more active next step before answering the questionnaire, the safest docs-only alternative is:

```text
BLUEPRINT_SOURCE_RESTORATION_AUDIT
```

Scope:

- locate source-window evidence for each corrupted blueprint name;
- restore names only from evidence;
- keep all entries non-canon;
- output a source-restoration report;
- no conversion.

## 11. 03 Review Focus

03 should verify:

- this brief uses current repo docs rather than the stale external blueprint as operational truth;
- preserve / cut / redesign / postpone decisions are explicit;
- the blueprint pool remains non-canon;
- mojibake names are not guessed;
- Qingli is not revived as the new base;
- QuarterFull learning is limited to product-pattern ergonomics, not platform stack;
- no runtime, schema, Validator, StateReducer, SQLite, setting-pack conversion, queryer implementation, asset registry implementation, GPT automation, Seedance, Oiioii, video generation, Candidate B, MVP-8, or 04 / 05 / 06 / 07 / 08 activation is implied.
