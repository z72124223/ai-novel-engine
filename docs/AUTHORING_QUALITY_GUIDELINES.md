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

### Narrative Review Gate And Eight-Point Checklist

Use a fixed authoring workflow so the user is not the first line-by-line reviewer.

Recommended flow:

```text
draft -> self-check -> review gate -> minimal revision -> user-facing text
```

Workflow rules:

1. 04 or any authoring window first produces a draft.
2. The draft includes a short self-check summary and is not treated as final.
3. 01 or a designated review gate uses the fixed checklist below.
4. The review asks for minimal revision, not a full rewrite by default.
5. Only the revised text is shown to the user.
6. The user mainly gives direction-level feedback: premise, mood, pacing, taste, or product intent.

Eight-point checklist:

| Check | Question |
| --- | --- |
| Character objective alignment | Does the opening or turn goal fit the character's current motive, knowledge boundary, and immediate need? |
| Protagonist reaction beats | Do major abnormalities or pressures receive enough noticing, checking, adjustment, and then advancement? |
| NPC consistency | Are terms of address, knowledge, voice, pressure, and social position continuous? Do NPCs avoid taking turns reciting setting information? |
| Companion / animal behavior | Does the companion or nonverbal character fit species, senses, leash / no-leash state, recallability, and non-human behavior? |
| Material culture consistency | Do objects, tools, materials, transport, and institutions fit the setting's epoch and tech level? |
| Micro-prose quality | Does the prose avoid AI calibration phrasing, mechanical balanced clauses, sensory misattribution, random synonym swaps, and over-explanation? |
| Scene causality | Do scene beats connect causally, without plot gravity dragging the protagonist toward the author's preferred mystery? |
| Actionable ending | Does the ending leave clear, natural, non-UI player choices that fit the current objective and do not smuggle in later plot? |

Additional scene-lock check for animation-oriented projects:

- Is the scene inside the approved scene set?
- Are visual anchors stable?
- Did the draft introduce an unapproved new location, building, room, or interior?
- Did it mistake a camera corner, blocking zone, or local position for a new scene?
- If it needs a new space, is it labeled `sceneCandidate` and handed to 01 instead of being sent as approved output?

04 self-check summary template:

```text
Authoring self-check:

1. Character objective alignment: PASS / RISK / FAIL - <one sentence>
2. Protagonist reaction beats: PASS / RISK / FAIL - <one sentence>
3. NPC consistency: PASS / RISK / FAIL - <one sentence>
4. Companion / animal behavior: PASS / RISK / FAIL - <one sentence>
5. Material culture consistency: PASS / RISK / FAIL - <one sentence>
6. Micro-prose quality: PASS / RISK / FAIL - <one sentence>
7. Scene causality: PASS / RISK / FAIL - <one sentence>
8. Actionable ending: PASS / RISK / FAIL - <one sentence>

Minimal revision needed:
<one to three concrete fixes, not a full rewrite plan>
```

Review gate expectations:

- Mark `PASS` when the issue is acceptable as-is.
- Mark `RISK` when a small localized edit should fix the issue.
- Mark `FAIL` when the draft's objective, causality, knowledge boundary, or setting fit is structurally wrong.
- Prefer one narrow patch over rewriting the whole scene.
- Do not leak author-only secrets in the self-check or review note.

Layer decision:

- This is an authoring guideline and review checklist.
- It is not a runtime quality gate.
- It does not approve schema, migration, Validator, StateReducer, setting-pack field, or context protocol changes.
- Qingli, Ai-chan, and any specific opening text may be used as private review context, but they are not hardcoded into engine core.

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

### Micro-Prose Review

Reviewers should watch for AI calibration phrasing: prose that repeatedly defends itself with constructions such as "not X, only Y" instead of letting the visible action carry the limit.

When the scene needs to show that a character did not rush forward, did not cross a line, or can still be called back, prefer visible behavior over explanatory narration.

Check for stacked weakening words. Repeated "suddenly", "just", "not", "only", "half-step", or equivalent softeners can make the picture feel mushy and the action less clean. The issue is not any single word; it is the pattern of hedging after every motion.

For animals or nonverbal characters, prioritize visible behavior:

- nose lifting toward a scent;
- ears turning before the body moves;
- front paws stopping at a boundary;
- a half-step that stops on its own;
- a look back toward the handler;
- breath, posture, and scent direction.

Do not over-explain the animal's psychology when a physical beat can show control, hesitation, curiosity, or recall.

Minimal abstract revision:

```text
Weak: It was not a charge, only a half-step forward.
Stronger: Its nose lifted; one front paw landed half a step out, then stopped.
```

Layer decision:

- Engine layer: keep the micro-prose review rule and example pattern.
- Content layer: decide the actual animal, scene boundary, scent, handler relationship, and local vocabulary.

### Sensory Attribution And Body-Part Logic

Sensory attribution must match physiology and visible action. Do not attach a stimulus to the wrong body part just because the sentence needs to satisfy two prompt conditions at once.

Use the right sensory surface:

- Smell / scent: nose tip, nostrils, breath, muzzle, body angle, front paws, steps, pause.
- Hearing / sound: ears, stopping, head turn, alert posture, looking back, quieting down.
- Sight / visual cue: gaze, head direction, narrowed eyes, line of sight, where attention rests.
- Touch / ground feel: soles, fingers, skin, fur, weight of steps, pressure through the body.

If one beat needs multiple stimuli, distribute the reactions to the correct body parts. For example, ears may answer the protagonist's voice while the nose remains angled toward a scent source.

Avoid mechanical "while / on one side / on the other side" balancing when it reads like prompt condition stitching. Use visible action to show divided attention or conflict.

For animals and nonverbal characters, do not write human-style inner conflict when body language can carry it. Attention, obedience, distraction, and hesitation should be visible through ears, nose, paws, body angle, stopping, looking back, or returning to the handler.

Micro-prose review questions:

- Does this sensory cue land on the right organ?
- Is the action visible?
- Did an abstract sentence replace the picture?
- Is the sentence forcing two unrelated reactions into one balanced construction?
- Would splitting the beat make the animal or nonverbal character clearer?

Minimal abstract revision:

```text
Weak: The animal's ears listened to the handler while the scent pulled them forward.
Stronger: Its ears flicked back at the handler's voice; its nose stayed angled toward the scent.
```

Layer decision:

- Engine layer: keep the sensory attribution and body-part review rule.
- Content layer: decide the actual species, scent source, handler cue, and local vocabulary.
- This section is authoring guideline and review checklist only. It does not approve runtime Validator, schema, setting-pack field, StateReducer, or context protocol changes.

### Epoch And Material Culture Consistency

Reviewers should check whether objects, tools, transport, containers, weapons, lighting, medicine, and infrastructure fit the setting's epoch, material culture, and tech level.

Generic review questions:

- What materials does this object require?
- What craft, toolchain, energy source, or workshop does it require?
- Who can manufacture it?
- Who can repair it?
- Who can afford it?
- Does it imply roads, storage, commerce, warfare, institutions, or supply chains that must also exist?
- Would it change the setting's social structure or combat balance if it were common?

If an object exceeds the setting tech level, the text needs a clear source before using it:

- a transported item;
- a ruin or relic;
- foreign technology;
- abnormal trade;
- magic / cultivation exception;
- explicit author approval in the setting pack.

If there is no source, revise to an era-fitting substitute. Do not only swap the noun; check the material and institution behind it.

Example: a "tire" is not just a wheel. Modern rubber tires may imply rubber processing, petrochemical products, vulcanization, industrial supply, repair tools, and road conditions. If the setting pack does not authorize those systems, use a period-appropriate trace instead.

Abstract playtest example:

- Reasonable by default: `cart tracks`, `wooden wheel ruts`, `iron-rimmed cart wheel marks`.
- Requires explicit exception or prohibition: `modern rubber tires`, `plastic containers`, `electric lamps`, `firearms`, `cars`, `petrochemical products`.
- If the text says "medicine cart wheel marks", read it as wooden or iron-rimmed cart marks unless the player or setting explicitly establishes a modern vehicle.
- Do not upgrade "wheel marks" into modern "tire tracks" by convenience.

When player input introduces a modern object:

- Decide whether it is a transported remnant, usable tool, mistaken description, or forbidden artifact.
- Do not let the AI result make the world native to an unauthorized modern supply chain.
- Prefer narrative refusal, translation, or downgrading when appropriate. For example, "car" may be interpreted as a beast-drawn cart / wooden-wheel vehicle unless the player clearly means a modern automobile and the setting permits it.
- If the object would affect social structure, combat power, medicine, travel, or trade, stop for 01 / user decision before formalizing it as canon.

Layer decision:

- This section is authoring guideline and review checklist only.
- It does not approve runtime Validator, schema, setting-pack field, StateReducer, or context protocol changes.
- Future engineering may separately design fields such as `techLevel`, `materialCulture`, `allowedMaterials`, `forbiddenArtifacts`, or `exceptionSources`, but this document does not implement them.

### Protagonist Reaction Beats

Abnormality or pressure should not only push the plot forward. It should trigger a reasonable protagonist reaction.

In an opening or turn output, when environmental abnormality accumulates, include at least a short reaction chain:

1. abnormal cue appears;
2. protagonist notices;
3. protagonist checks or adjusts;
4. then the scene advances.

Useful checks or adjustments include:

- stopping for a beat;
- looking back at the route;
- confirming a landmark;
- slowing down;
- calling to a companion;
- listening for expected or missing sound;
- checking scent, wind, tracks, light, weather, or terrain;
- changing posture, grip, route, or pace.

Leaving mystery is not the same as omitting reaction. The text may withhold the world's truth, but the protagonist should not feel numb to obviously unreasonable phenomena.

Keep reaction beats short and concrete. Avoid turning the moment into a long internal monologue, UI-style summary, or explanatory essay. A single visible check can be enough when the scene needs pace.

Animals and companions can act as reaction mirrors. A companion might stop first, turn its ears, look back at the protagonist, refuse a path, or track a missing scent. That visible behavior can make the protagonist notice the abnormality without over-explaining the world.

If a modern object such as a phone is used as a checking tool, respect material culture and transported item boundaries. Unless the scene is formally handling a transported modern object, prefer landmarks, the return path, sound, scent, weather, terrain, or companion reaction as the confirmation method.

Layer decision:

- This section is authoring guideline and review checklist only.
- It does not approve runtime Validator, schema, setting-pack field, StateReducer, or context protocol changes.
- Future runtime support for protagonist cognition, reaction-state tracking, or opening-scene checklists requires a separate approved milestone.

### Initial Survival Orientation And Goal Transition

Opening player objectives must fit the protagonist's current knowledge boundary and immediate need. A newly displaced, transported, lost, injured, or disoriented protagonist should not immediately inherit the author's local mystery as their own main goal.

For displacement openings, the first stage usually centers on:

- orientation;
- immediate safety;
- return path;
- companion protection;
- finding help;
- checking whether familiar rules still apply.

Local events or mysteries may appear as obstacle, pressure, or context. They should not become the protagonist's main objective without visible causality.

Use staged goal transition:

1. return home / locate self;
2. attempt fails or is blocked;
3. seek help or survival resources;
4. local rules, people, danger, debt, duty, or misunderstanding pull the protagonist into the local event.

Scene beats should include protagonist reaction and objective handoff. The reader should see the goal changing because the character's situation changed, not because plot gravity pulls them toward the author's planned mystery.

Ending action options should fit the current objective. Do not smuggle later plot into choices. For example, a protagonist who has just arrived somewhere impossible should usually see options such as "check the route back", "protect the companion", "ask where this is", or "find shelter" before options such as "investigate the sealed warehouse" unless the scene has already established why that local event now blocks survival or return.

Good opening pressure:

- The local event blocks the road home.
- A local authority stops the protagonist from leaving.
- The companion is endangered by local rules.
- The protagonist needs help from someone involved in the local event.
- A misunderstanding makes the protagonist part of the local procedure.

Weak opening pressure:

- The protagonist ignores being displaced and immediately acts like a local investigator.
- The narrative treats the local mystery as obvious priority before the protagonist understands where they are.
- Action options assume the protagonist cares about local disputes without a survival, return-path, or companion-protection bridge.

Layer decision:

- Engine layer: keep the orientation-first and staged goal-transition review rule.
- Content layer: decide the local mystery, faction, obstacle, and exact survival pressure.
- This section is authoring guideline and review checklist only. It does not approve runtime Validator, schema, setting-pack field, StateReducer, or context protocol changes.

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
- a production scene registry for animation asset planning;
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
- treating runtime scene transitions as approval to create unlimited animation scenes.

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
