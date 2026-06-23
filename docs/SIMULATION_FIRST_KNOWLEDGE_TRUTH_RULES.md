# Simulation-First Knowledge And Truth Rules

Status: `DOCS_ONLY_PRODUCT_LAYER_GUARDRAIL`

Date: 2026-06-23

This document records simulation-first knowledge and truth rules for product-layer authoring, review, query, asset, and storyboard work. It does not implement Kotlin source, tests, schema, migrations, Validator behavior, StateReducer behavior, `resolve-turn`, `stateVersion`, `contextHash`, CLI commands, SQLite data, setting-pack patches, queryer implementation, GPT automation, Oiioii integration, Seedance integration, or video generation.

Source evidence:

- Candidate window 11 produced the v0.1 simulation-first knowledge/truth rules candidate.
- Review window 12 passed the candidate.
- Audit window 13 reported `RISKS_FOUND`: no severe overreach had landed, but product-layer wording still risked drifting toward reveal rhythm, mystery product, or plot-first truth handling.

## 1. VERDICT

Bottom-layer truth is world causality, not a plot objective.

The world does not perform secrets for the player. It also does not perform secrets for NPCs, factions, sects, officials, merchants, ordinary people, or companion animals. Truth may become known when actors encounter phenomena, gather evidence, test explanations, gain access, trade information, or suffer consequences. Truth may also remain unknown, misunderstood, operationally useful but theoretically wrong, or captured by a faction narrative.

Therefore project documents should prefer these questions:

- Who can observe this?
- Who can infer this?
- Who can verify this?
- Who can legally or physically access this?
- How does evidence reach them?
- Which causal pressure is exposed by the situation?
- Which information exists only as world causality and not yet as actor knowledge?

Avoid treating truth as:

- something that must be revealed;
- something that must be hidden;
- something that must be slowly revealed;
- something that must become a twist;
- the main product of the story.

## 2. Simulation-First Principles

1. World causality exists whether or not anyone understands it.
   A disease, price shock, ritual failure, legal rule, material property, social taboo, or power-system cost can shape the world without becoming a narration task.

2. No actor is automatically omniscient.
   Knowledge must follow contact, location, profession, education, interest, bias, evidence, memory, records, investigation, faction access, or action.

3. Ignorance is a valid simulated state.
   A character, institution, or village may have only rumors, partial rules, operational tricks, wrong explanations, or self-serving propaganda.

4. Knowledge boundary is not reveal pacing.
   Author-only boundary protects against perspective leaks. It must not become a fixed schedule for when secrets are hidden, delayed, or exposed.

5. Discovery is a consequence of contact.
   If a player or NPC reaches the right place, asks the right person, obtains evidence, or suffers a result, knowledge can change. If they do not, the world can continue without forcing exposition.

6. Story value comes from life, choices, pressure, cost, relationship, and consequence.
   Mystery can exist, but the system should not collapse every scene, clue, asset, or query into one secret-answer pipeline.

## 3. Distributed Knowledge Model

Use these layers when planning 09 world data, 04 scene packets, 05 review, 07 assets, 08 storyboards, and future queryer results.

| Layer | Meaning | Guardrail |
| --- | --- | --- |
| `worldTruth` | Actual causal structure of the world. | Not actor knowledge by default. |
| `phenomena` | Observable effects: wounds, weather, scent, prices, rituals, records, material behavior. | Visible phenomena do not carry one automatic explanation. |
| `localExplanation` | Local folk explanation, rumor, taboo, street theory, village story. | May be useful, wrong, biased, or incomplete. |
| `institutionalTheory` | Faction, sect, office, guild, school, or temple theory. | More organized than rumor, still partial and politically shaped. |
| `operationalKnowledge` | A practice that works without full causal understanding. | "Can do" does not imply "knows why." |
| `wrongBelief` | Sincere false belief caused by limited evidence, fear, trauma, bias, bad samples, or bad teaching. | Must have a plausible origin; do not make actors foolish for plot convenience. |
| `propagandaOrControlNarrative` | Deliberately shaped explanation that protects power, market, doctrine, liability, or order. | May mix truth and distortion. |

## 4. Actor Knowledge Rules

### Player

The player can know what the protagonist directly experiences, observes, remembers, is told, investigates, holds as a document, or can reasonably infer from established skills and evidence.

The player cannot automatically know:

- author-only world causality;
- distant events not contacted;
- other actors' private motives;
- faction archives;
- future plot answers;
- hidden mechanisms with no evidence chain.

### NPCs

NPCs know from role, location, training, experience, memory, social network, faction access, and personal interest. They do not know world truth because the author knows it.

An NPC should not become:

- an exposition signboard;
- a deliberate clue machine;
- a person who misses obvious evidence only because the author wants secrecy.

### Ordinary People

Ordinary people may hold rumors, taboos, local practical rules, market signals, fear patterns, and inherited explanations. They usually do not know institutional theory, occult mechanics, official secrets, or technical causes unless their life puts them in contact with those things.

### Specialists

Specialists such as physicians, craftsmen, cultivators, clerks, guards, officials, engineers, merchants, or priests may recognize patterns in their domain. Their expertise does not automatically generalize to all domains.

Example logic:

- a physician may identify symptoms without knowing the true metaphysics;
- a craftsperson may know a material behavior without knowing the historical cause;
- an official may know procedure without knowing the hidden economy behind it;
- a cultivator may know a technique cost without knowing the full power-system origin.

### Organizations

Factions, sects, governments, offices, guilds, merchants, armies, schools, and temples are not single omniscient minds. Their knowledge is split across ranks, archives, departments, taboos, corruption, doctrines, field reports, and suppressed contradictions.

Organization knowledge should model:

- information latency;
- source confidence;
- internal disagreement;
- propaganda;
- compartmentalized access;
- decay or distortion over time.

### Animals And Nonverbal Companions

Animals or nonverbal companions may know through senses, training, habit, attachment, route memory, threat recognition, and established special abilities. They should not understand politics, institutional theory, abstract truth, or author-only causality unless canon later establishes a source.

For Ai-chan-like companions, use visible behavior and sensory limits first: nose, ears, paws, body angle, stopping, tracking, returning, hesitation, and response to the protagonist.

## 5. Vocabulary Guardrails

Prefer these terms:

- observe;
- infer;
- verify;
- access;
- contact;
- evidence path;
- source confidence;
- actor knowledge;
- local misconception;
- institutional theory;
- causal pressure;
- visible phenomenon;
- world causality;
- author-only boundary.

Use with caution:

- reveal;
- hidden truth;
- must not be revealed early;
- approved reveal beat;
- twist;
- secret answer;
- plot mystery;
- must be solved.

When those terms are unavoidable, attach the safer meaning:

- `reveal` means an actor gains evidence through contact, not that the plot performs a secret.
- `hidden` means currently outside a viewpoint or access path, not artificially concealed from a competent actor.
- `must not be shown` means user-facing or actor-facing leakage is unsafe, not that normal observable facts must be suppressed.

## 6. Author-Only Boundary

Author-only boundary remains necessary. It protects:

- future-facing design knowledge;
- world causality not yet reachable by the current actor;
- faction secrets not accessible to the current scene;
- production-only notes;
- review-only constraints;
- hidden motives that actors cannot know.

But author-only boundary must not be used to:

- block normal visible evidence;
- make actors ignore what they should observe;
- enforce a fixed reveal schedule;
- turn every scene into a mystery funnel;
- prevent natural consequences from exposing causal pressure.

## 7. Query Layer Impact

The demand-driven query layer should retrieve slices of simulated knowledge, not plot beats.

### World Card Queryer

World Card Queryer should retrieve world facts by visibility and evidence path:

- observable phenomena;
- local explanations;
- institutional theories;
- operational practices;
- contested claims;
- author-only causal notes, only for review or production safety.

It must not dump full world causality into 04 or user-facing packet fields.

### Story Card Queryer / Situation-Continuity-Pressure Queryer

The previous label `Story Card Queryer` can be retained as a file/search term, but its product meaning should be situation, continuity, and pressure retrieval.

Its focus is:

- current situation;
- local pressure;
- available evidence;
- actor objective;
- continuity constraints;
- local misconceptions;
- known blockers;
- allowed evidence paths;
- unresolved consequences.

It must not schedule the next plot, force a reveal, force concealment, or treat every card as a step toward a mystery answer.

### Asset Queryer

Asset Queryer should retrieve visible assets and prompt-safe production notes:

- what can be seen;
- which asset id / scene id / zone id / variant applies;
- what cannot be shown because no actor or camera should access it;
- which visible traces express a causal pressure without drawing the unseen cause itself.

It must not let storyboards or prompts smuggle in author-only truth.

## 8. Workflow Impact

### 09 New World Foundation / World Data Organization

09 should build multiple layers for each important causal area:

- world causality;
- visible phenomena;
- local explanation;
- operational knowledge;
- institutional theory;
- wrong belief;
- propaganda or control narrative;
- actor access paths.

09 output should not become canon without explicit 01/user approval.

### 10 Review Of 09

10 should check whether a world item exists only as a future reveal. If it lacks phenomena, actors, mistaken theories, practical effects, or social pressure, it is too plot-first.

### 04 Scene Packet Drafting

04 should draft from current contact range and returned query slices. It should not use author-only truth as prose, NPC knowledge, or player-facing explanation.

### 05 Review

05 should check for:

- forced reveal;
- forced concealment;
- exposition NPC;
- author-truth leakage;
- plot gravity;
- all roads lead to secret;
- missing evidence path;
- omniscient faction or NPC;
- normal visible evidence being suppressed.

### 06 Human Display

06 should show only current user-facing material after 05 PASS and explicit 01 handoff. It should not display a full world encyclopedia, author-only truth table, or future plot map.

### 07 Asset Candidates

07 should express visible phenomena and usable production references. If the cause is not visible to the scene, do not draw the cause directly; draw its visible traces, institutional symbols, or actor-level misunderstanding.

### 08 Storyboard Pickup

08 should depict what the camera, actor, or user-facing view can legitimately see. It should not use framing, hidden background objects, captions, or image prompts to smuggle in author-only truth.

## 9. Review Checklist

Use this checklist for future docs, packets, query templates, assets, and storyboards:

- Does this turn bottom-layer causality into a mandatory reveal task?
- Does this hide normal observable evidence to protect a mystery?
- Does this let an NPC, faction, or ordinary person know something without source?
- Does this make an institution omniscient?
- Does this provide multiple layers: phenomenon, local explanation, institutional theory, operational knowledge, wrong belief, and interest-shaped narrative?
- Does this give a plausible source for an actor's misconception?
- Does this make every clue point to one secret?
- Does this make the world serve the protagonist rather than continue independently?
- Does this use author-only truth in user-facing text, storyboard frames, image prompts, or character knowledge?
- Does this preserve author-only safety without suppressing valid observation?
- Does this query only the information needed for the current task and visibility level?
- Does this asset or frame show visible consequences rather than the inaccessible cause?

## 10. Project-Control Status

This document is a docs-only guardrail. It does not:

- start 09;
- restart 04 / 05 / 06 / 07 / 08;
- convert 11 / 12 candidate work into canon;
- patch a setting pack;
- add queryer implementation;
- add runtime enforcement;
- promote non-canon blueprint candidates from `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md` into canon, setting-pack data, runtime data, or approved assets;
- change schema, migrations, Validator, StateReducer, `stateVersion`, `contextHash`, `resolve-turn`, CLI, or SQLite;
- approve GPT browser automation, Oiioii, Seedance, video generation, or storyboard generation.

## 11. 03 Review Focus

03 should verify:

- bottom-layer truth is framed as world causality, not plot objective;
- distributed knowledge covers player, NPC, ordinary people, specialists, organizations, and animals/nonverbal companions;
- author-only boundary is preserved but not treated as reveal schedule;
- query layer wording is situation/evidence/pressure-oriented, not next-plot-oriented;
- blueprint candidate references remain non-canon planning inputs unless a later 01/user-approved milestone promotes a specific slice;
- 05 checks forced reveal, forced concealment, author-truth leakage, and plot gravity;
- no runtime, schema, Validator, StateReducer, SQLite, CLI, queryer implementation, 04/05/06/07/08, 09, Oiioii, Seedance, or video-generation work is implied.
