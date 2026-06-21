# Authoring Quality Guidelines

Status: `DOCS_ONLY_GUIDANCE`

Date: 2026-06-21

This document records playtest authoring lessons as guidance. It does not change runtime, schema, Validator, StateReducer, `resolve-turn`, `stateVersion`, or `contextHash`. It also does not approve Candidate B or any pre-resolve quality gate.

## 1. Purpose

The project needs a place to preserve authoring quality lessons without confusing engine layer（通用引擎層） rules with content / setting pack layer（內容包 / 設定包層） specifics.

The engine can absorb general constraints, checklists, prompts, and review workflow. The content layer should keep concrete worldbuilding, characters, names, local prose choices, and playtest-specific style applications.

## 2. Engine Layer Guidance

These rules are suitable for generic engine-adjacent documentation, prompt templates, read-only audit rubrics, or future approved quality tooling.

### Opening Scene Beats

An opening scene should establish:

- where the player is and what they can perceive;
- what immediate pressure exists before the player acts;
- which NPCs or forces have independent goals;
- what the player can safely choose now;
- what remains unknown without leaking author-only secrets.

Do not make the opening depend on a specific setting. A mountain fog descent, warehouse perimeter, abandoned post, tavern, ferry, or clinic can all use the same generic beat structure.

### Narrative Prose Quality

Narrative prose should make causality visible. A good turn shows why the next sentence happens: weather changes footing, a guard's duty limits cooperation, a ledger changes who can speak, or a remembered injury affects risk.

Prefer concrete sensory and procedural detail over summary labels. Instead of telling the reader that a scene is tense, show who pauses, what rule blocks action, what object cannot be touched, and what consequence is now harder to avoid.

### Viewpoint, Causality, And Rhythm

Keep viewpoint stable enough that the reader knows what the player can actually observe. Do not jump into NPC-only knowledge unless the result format explicitly supports it and the knowledge boundary permits it.

Use rhythm to create decision pressure:

- short beats for immediate risk or interruption;
- longer beats for observation, procedure, or consequence;
- pauses when a rule, memory, or NPC boundary changes the available options.

Avoid explaining every implication at once. Let observable facts, NPC reactions, and unresolved tasks carry some of the pressure.

### NPC Dialogue Boundaries

NPC dialogue should be constrained by identity, pressure, and knowledge boundary:

- identity: role, status, relationship, dialect level, and duty;
- pressure: fear, loss, deadline, authority, public scrutiny, or personal stake;
- knowledge boundary: what the NPC saw, heard, remembers, believes, or can legally infer.

An NPC can cooperate, but cooperation should have a reason, cost, condition, misunderstanding, delay, or procedural limit when the context supports one.

An NPC should not become a tool for the player's convenience. They should not reveal hidden truth, grant access, change loyalty, or solve the scene only because the player asked politely.

### Terminology Consistency

Terms, titles, and forms of address should remain consistent unless the text shows a reason for change.

Minimal example:

- If a character first says "dog" in casual fear, later "canine" or "snow dog" can be valid only if the scene gives a visible reason: official record, legal wording, someone recognizing a breed, or a shift into formal speech.
- If there is no reason, switching terms reads like author drift rather than character voice.

This consistency check applies to names, job titles, locations, ranks, items, procedures, injuries, and faction terms.

### Review Workflow

The engine layer can standardize review questions:

- What did the player choose?
- What changed because of that choice?
- Which NPCs acted from their own goals?
- Which facts or beliefs were updated?
- Did any line reveal author-only knowledge?
- Did the prose preserve viewpoint, causality, and rhythm?
- Did terminology remain stable or change for an observable reason?

Review workflow should stay advisory unless a future explicitly approved milestone changes runtime behavior.

## 3. Content / Setting Pack Layer

The following should remain in content, setting packs, or scenario notes, not engine core:

- the Qingli world, herb warehouse, local procedure, and local trade/legal texture;
- Ai-chan's concrete role, species/breed presentation, sensory habits, and relationship with the player;
- specific NPCs such as Luo the clerk, Shen Qinghe, Patrolman He, merchants, buyers, families, or local officials;
- local names, place names, medicine names, records, seals, ledgers, routes, or faction details;
- a specific playtest prose flavor, pacing preference, or opening prologue;
- any future mountain fog / hiking descent bridge scene unless approved as content or setting-pack material.

Engine guidance can say "a companion animal's sensory behavior must be constrained by context." It should not hardcode one specific companion, scent trail, breed detail, or plot function.

## 4. Anti-AI-Writing Guidance

Avoid "AI-feeling" prose by strengthening story mechanics, not by piling on decorations.

Prefer:

- grounded observation;
- visible cause and effect;
- stable viewpoint;
- NPCs with independent pressure;
- concrete stakes and consequences;
- varied sentence rhythm that matches the moment.

Avoid:

- repetitive emotional labels;
- decorative gestures that do not change the scene;
- ornate or obscure dialogue that exists only to sound literary;
- every NPC speaking with the same polished cadence;
- immediate summaries of every theme;
- catchphrases used as a substitute for character identity.

Good anti-AI revision usually asks "what pressure caused this line?" rather than "what verbal texture can I add?"

## 5. NPC Consistency Check

For each important NPC, reviewers should ask:

- What does this NPC want right now?
- What duty, fear, relationship, or procedure constrains them?
- What do they know directly?
- What are they guessing?
- What words would this person use in this social situation?
- Did their terms of address or naming change? If yes, is the reason visible?
- Did they preserve, resist, delay, or condition the player's request in a way that fits their role?

If an NPC changes tone, terms, or attitude, the result should provide evidence: a new fact, public pressure, formal record, visible risk, memory, order, injury, threat, or relationship shift.

## 6. Review Loop

Playtest output can be reviewed, corrected, and fed back into future authoring. The recommended loop is:

1. Save the player input, context, result, evidence, and audit report.
2. Review narrative quality and NPC consistency with a human-facing checklist.
3. Mark issues as engine-general or content-specific.
4. Convert engine-general lessons into docs, prompt checklists, or read-only audit ideas.
5. Convert content-specific lessons into setting-pack patch proposals, character cards, scenario notes, or future player-input guidance.
6. Keep author-only secrets out of review notes that may be shown to players or used as character-facing context.

Review must not leak author-only secrets. A reviewer can say "NPC reveals more than they should know" without naming the hidden truth in a player-facing note.

## 7. Copyright And Style Safety

External works may be used only for abstract mechanism analysis. Safe abstraction examples:

- "start with disorientation, then reveal social rules through blocked action";
- "let factions pressure the player through procedure rather than exposition";
- "distinguish NPC voices by duty, fear, and status";
- "use a companion's senses as a limited clue source with costs and uncertainty."

Do not copy:

- sentences or distinctive phrasing;
- character names, place names, factions, schools, sects, or institutions;
- proprietary setting rules or magic systems;
- recognizable plot beats or scene sequences;
- a living author's identifiable style;
- unique terminology that points back to the source.

Style safety means distilling craft, not cloning surface.

## 8. Future Work Boundaries

Possible future follow-ups, each requiring separate approval:

- prompt templates for opening scene beats;
- a human review checklist template for playtest notes;
- read-only audit observations for terminology drift or NPC voice sameness;
- setting-pack fields for character voice and naming conventions;
- content patch proposals for specific scenarios or characters.

Not approved by this document:

- runtime quality gates;
- Validator changes;
- schema or migration changes;
- StateReducer changes;
- automatic prose rewriting;
- hardcoding specific worlds or characters into engine behavior.

## 9. Minimal Example

Problem:

```text
Turn 1 calls the companion "dog"; Turn 2 calls the same companion "snow dog"; Turn 3 calls them "official canine witness" with no scene reason.
```

Review:

```text
This is terminology drift. It may be valid if a clerk formalizes the record, a guard changes to official speech, or a character identifies the breed. Without visible cause, choose one term per speaker/register or add the reason for the change.
```

Layer decision:

- Engine layer: keep the consistency rule and review question.
- Content layer: decide the actual companion identity, local formal term, and who uses which term.
