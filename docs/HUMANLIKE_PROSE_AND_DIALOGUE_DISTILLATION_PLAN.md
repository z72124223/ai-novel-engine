# Humanlike Prose And Dialogue Distillation Plan

Status: `DOCS_ONLY_PRODUCT_PLAN`

Date: 2026-06-25

This document defines how the project should upgrade prose, dialogue, and style generation so the accepted novel text does not read like generic AI output. It adapts Nuwa-style skill distillation into a local authoring workflow. It does not implement Kotlin source, schema, migrations, CLI commands, OpenAI API calls, web servers, QuarterFull control, browser automation, scraping, local model installation, SQLite writes, image generation, audio generation, or video generation.

## 1. Verdict

The most important product layer is text.

Comic panels, storyboard timing, voices, and video only matter if the underlying prose has believable dialogue, human rhythm, and scene pressure. The project should therefore add a style distillation layer before any draft is accepted:

```text
approved style sources
  -> style source pack
  -> expression DNA
  -> character dialogue voice cards
  -> scene pressure and subtext rules
  -> next-chapter writer brief
  -> draft from any source
  -> anti-AI prose audit
  -> minimal revision brief
  -> accepted chapter
  -> script / storyboard / comic video handoff
```

This is not a promise that a detector can prove text is "not AI." It is a production rule: drafts are not accepted unless they pass concrete human-writing gates.

## 2. Nuwa Adaptation

Nuwa is useful here because it does not merely say "write in a style." It asks for:

- source-backed extraction;
- expression DNA;
- anti-patterns;
- honest boundaries;
- quality checks;
- tests against real examples.

For this project, the target should not be a single-person imitation skill. It should be a project style framework:

```text
project-humanlike-novel-style
```

The framework should learn from:

- accepted project chapters;
- user-approved favorite passages;
- user-written taste notes;
- rejected drafts as negative samples;
- genre references described as principles, not cloned voices;
- character sheets and relationship pressure;
- scene constraints from the local engine.

Do not blindly imitate a copyrighted author, living writer, or private source. Distill usable craft traits and project taste.

## 3. Source Pack

Before writing or rewriting, build a source pack.

```yaml
styleSourcePack:
  packId: ""
  projectId: ""
  positiveSamples:
    - sourceId: ""
      sourceType: accepted_chapter | user_note | reference_excerpt
      path: ""
      approvedUse: rhythm | dialogue | tension | narration | atmosphere | structure
      whyItWorks: []
  negativeSamples:
    - sourceId: ""
      sourceType: rejected_draft | weak_passage | ai_fingerprint
      path: ""
      failureModes: []
  characterSources:
    - characterId: ""
      acceptedLines: []
      forbiddenLineTypes: []
  boundaries:
    noAuthorClone: true
    noPrivateScrape: true
    noUnapprovedCanon: true
```

The first version can be manual. Automatic extraction should come later.

## 4. Expression DNA

Expression DNA should describe how the project text moves, not just what adjectives it likes.

```yaml
expressionDna:
  narration:
    distance: close | medium | controlled_distant
    interiorityLevel: restrained | moderate | deep
    expositionStyle: embedded_in_action | withheld | explicit_when_earned
    sensoryPriority: []
    rhythmRules: []
  dialogue:
    defaultSubtextLevel: low | medium | high
    interruptionRate: low | medium | high
    directnessByStatus: {}
    silenceRules: []
    dialectOrRegisterRules: []
  scenePressure:
    everyBeatNeedsPressure: true
    pressureTypes:
      - danger
      - status
      - debt
      - time
      - uncertainty
      - material_constraint
      - relationship_cost
  antiPatterns:
    bannedPhrases: []
    bannedStructures: []
    bannedExpositionHabits: []
```

Good expression DNA is operational:

- it says when a sentence should be short;
- it says what a character refuses to say directly;
- it says what kind of sensory detail is allowed;
- it says what the narrator withholds;
- it says which AI-ish habits must be cut.

Bad expression DNA is decorative:

- "make it vivid";
- "write beautifully";
- "add tension";
- "use literary language";
- "make it immersive."

## 5. Character Nuwa Gate And Dialogue Voice Cards

Character voice in prose is different from TTS voice in video. Before a character can speak in an accepted draft, the draft pipeline must pass the Character Nuwa Gate.

Full Character Nuwa cards are mandatory for:

- point-of-view characters;
- protagonists and deuteragonists;
- recurring companions;
- antagonists;
- emotionally important supporting characters;
- any character who carries exposition, plot pressure, mystery, or relationship change.

Light voice cards are allowed only for one-off background speakers. A light card must still define identity, scene pressure, knowledge boundary, speech register, and forbidden speech patterns.

Hard rule:

```text
No character card -> no accepted dialogue.
```

This gate adapts Nuwa-style distillation from "make a person skill" into "make this fictional character operational before writing them." The goal is not to make every fictional person a huge standalone skill. The goal is to stop generic AI dialogue before it starts.

```yaml
characterVoiceCard:
  cardDepth: full_nuwa | light_voice
  gateStatus: draft | ready | blocked
  characterId: ""
  displayName: ""
  socialPosition: ""
  narrativeFunction: pov | companion | antagonist | authority | witness | vendor | passerby | other
  currentPressure: ""
  relationshipToProtagonist: ""
  knowledgeBoundary: []
  wantsFromScene: ""
  fearsInScene: ""
  hiddenMotiveIfAny: ""
  speechRegister: ""
  sentenceLengthBias: short | mixed | long
  directness: evasive | restrained | blunt | formal | playful
  silencePattern: ""
  pressureResponse: ""
  allowedTells: []
  forbiddenTells: []
  favoriteMoves:
    - deflects
    - asks_back
    - answers_with_procedure
    - changes_subject
    - tests_status
    - hides_concern_as_instruction
  neverDo:
    - recite_world_lore
    - solve_scene_for_protagonist
    - reveal_author_only_truth
    - speak_with_generic_polished_cadence
    - use_calibration_slogan
  sampleLines:
    accepted: []
    rejected: []
  bannedStructures:
    - "這不是XX，這是XXX"
    - "這不是 X，這是 Y"
    - "不是XX，而是XXX"
    - "不是 X，而是 Y"
    - "not X, but Y"
```

Useful test: remove speaker names from a dialogue scene. If all characters sound interchangeable, the draft fails.

Character Nuwa Gate checks:

- every speaking character has a full or light card;
- every important character has a full Nuwa-style card;
- no line exceeds the character's knowledge boundary;
- no line exists only to brief the reader;
- no line uses a banned calibration slogan;
- speech reflects current pressure, relationship, status, and desire;
- rejected sample lines are not reintroduced through paraphrase.

## 6. Dialogue Quality Gates

Dialogue should not be clean information transfer. Every important line should do at least one job:

- pursue a want;
- hide a want;
- test status;
- delay;
- refuse;
- bargain;
- wound;
- comfort badly;
- protect someone without saying so;
- expose misunderstanding;
- reveal knowledge boundary;
- create a new cost.

Hard fail patterns:

- a speaking character has no Character Nuwa card or approved light voice card;
- a character explains world lore because the reader needs it;
- all characters answer questions directly and politely;
- everyone uses the same balanced sentence shape;
- a character uses `這不是XX，這是XXX`, `這不是 X，這是 Y`, `不是XX，而是XXX`, `不是 X，而是 Y`, `not X, but Y`, or an equivalent calibration slogan;
- dialogue states emotions that action already shows;
- no one interrupts, evades, mishears, or refuses when pressure supports it;
- subtext is translated immediately by narration;
- the protagonist receives help without cost, condition, debt, delay, or misunderstanding.

Review question:

```text
What pressure caused this line to be spoken now?
```

If there is no pressure, cut or rewrite the line.

## 7. Anti-AI Prose Audit

The anti-AI audit should check visible failure modes, not vibes.

### Gate 1: Cadence

Hard fail if prose uses:

- `這不是 X，這是 Y`;
- `這不是XX，這是XXX`;
- `不是 X，而是 Y`;
- `不是XX，而是XXX`;
- `not X, but Y`;
- `not X, only Y`;
- any equivalent slogan that pauses the story to explain what the prose is trying to be.

Fail when prose repeatedly uses:

- symmetrical "not X, but Y" calibration;
- sentence pairs with identical rhythm;
- padded transitions;
- softening words after every action;
- polished summary after every beat.

### Gate 2: Causality

Fail when:

- a beat follows because the author wants plot movement, not because the scene caused it;
- NPCs cooperate without reason;
- danger, debt, injury, weather, tools, class, language, or social rules have no effect;
- the draft jumps from observation to conclusion without an action or inference path.

### Gate 3: Specificity

Fail when:

- sensory detail is generic or ornamental;
- objects appear without material or procedural logic;
- scene details do not affect choice, risk, or mood;
- names, ranks, injuries, props, and locations drift.

### Gate 4: Restraint

Fail when:

- the narrator explains the theme too early;
- subtext is translated after every line;
- mystery is replaced by exposition;
- emotional labels replace behavior.

### Gate 5: Character Voice

Fail when:

- recurring characters cannot be identified from speech alone;
- a character speaks outside current knowledge;
- directness, status, education, fear, and relationship pressure do not shape language;
- everyone has the same level of wit, patience, and verbal polish.

## 8. Writer Brief Integration

The next-chapter writer brief must include a style section, not only continuity.

```markdown
## Style Contract

Character Nuwa Gate:
Required full cards:
Approved light cards:
Blocked speakers:
Narration distance:
Sentence rhythm:
Dialogue pressure:
Subtext rule:
Exposition rule:
Sensory rule:
Character voice cards:
Forbidden AI-ish patterns:
Positive sample anchors:
Negative sample warnings:
```

The brief should be short enough to paste into Codex, GPT, QuarterFull-like tools, or a human editor.

## 9. Minimal Revision Brief

When a draft fails, the system should not ask for a full rewrite unless structure is broken. It should produce a focused revision brief:

```markdown
## Minimal Revision Brief

Verdict: PASS | NEEDS_PATCH | FAIL

Top issues:
1. <issue> - evidence: <quote or location>
2. <issue> - evidence: <quote or location>
3. <issue> - evidence: <quote or location>

Patch instructions:
- Replace generic exposition with action at <location>.
- Give <character> a pressure-caused refusal before cooperation.
- Cut repeated calibration phrasing in <range>.
- Restore <character> voice card rule: <rule>.

Do not change:
- accepted continuity;
- scene location;
- character knowledge boundary;
- approved props/costumes;
- chapter ending pressure unless requested.
```

This keeps revision precise and prevents endless full rewrites.

## 10. First Implementation Order

The first engineering milestone should be local and auditable:

1. `create-style-source-pack-template`
   - creates editable source-pack JSON or Markdown.
2. `validate-style-source-pack`
   - checks source labels, positive/negative sample separation, character IDs, and no private scrape assumption.
3. `create-character-nuwa-card-template`
   - creates full and light character-card templates.
4. `validate-character-nuwa-gate`
   - blocks missing full cards, missing light cards, knowledge-boundary leaks, generic speech, and banned calibration slogans.
5. `create-writer-brief`
   - combines continuity ledgers with style contract.
6. `audit-prose-style`
   - read-only report for dialogue voice, cadence, exposition, subtext, and AI-ish patterns.
7. `create-minimal-style-revision-brief`
   - turns audit findings into focused patch instructions.

No automatic rewrite should be trusted before these exist.

## 11. Success Metrics

A chapter is style-ready only if:

- recurring characters can be recognized from dialogue without speaker tags;
- all important speaking characters passed the Character Nuwa Gate;
- no accepted draft contains `這不是XX，這是XXX`, `這不是 X，這是 Y`, `不是XX，而是XXX`, `不是 X，而是 Y`, `not X, but Y`, or equivalent calibration slogans;
- important lines are caused by pressure, status, fear, debt, or desire;
- exposition is embedded in action, procedure, conflict, or withheld tension;
- the protagonist reacts to abnormal events instead of gliding through plot;
- sensory details attach to correct body/action logic;
- the draft has fewer repeated AI-ish cadence patterns after revision;
- the text can be converted into script packets without losing character voice.

## 12. Product Link To Comic Video

This style layer directly supports the user's comic-video target:

- dialogue cards become speech bubble style and TTS voice direction;
- subtext rules prevent flat comic captions;
- scene pressure creates better storyboard beats;
- character voice cards support independent voice lines;
- minimal revision briefs prevent bad prose from being animated.

If the prose sounds generic, the video will only make the weakness louder.

## 13. One-Sentence Operating Rule

```text
Do not ask the model to "write better"; give it memory, pressure, voice DNA, forbidden patterns, and a review gate that rejects generic prose.
```
