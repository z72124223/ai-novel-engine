# Novelization Plan To Surpass QuarterFull

Status: `DOCS_ONLY_PRODUCT_PLAN`

Date: 2026-06-25

This document arranges a concrete path for this project to surpass QuarterFull when used as a novel-production system. It does not implement Kotlin source, schema, migrations, CLI commands, OpenAI API calls, web servers, QuarterFull control, browser automation, image generation, video generation, or SQLite writes.

## 1. Verdict

The project should not try to beat QuarterFull by becoming a prettier prompt box.

It should beat QuarterFull by becoming a stricter long-form story machine:

```text
accepted chapter
  -> fact ledger
  -> character memory/state ledger
  -> open thread ledger
  -> style source pack / expression DNA
  -> Character Nuwa Gate
  -> next-chapter writer brief
  -> draft from any source
  -> continuity audit
  -> anti-AI prose and dialogue audit
  -> authoring quality audit
  -> minimal revision brief
  -> accepted novel chapter
  -> script packet
  -> storyboard packet
  -> optional narrated-video / animatic sidecar
```

QuarterFull-like tools are useful for generating prose. The local project should own the memory, continuity, review, structured handoff, and downstream production path.

## 2. Definition Of "Surpass QuarterFull"

The project surpasses QuarterFull only when it can do these six things better:

| Dimension | QuarterFull-like strength | Our superior target |
| --- | --- | --- |
| Low-friction drafting | Fast prose from prompt. | Keep low-friction input, but force a writer brief before draft acceptance. |
| Memory | Convenient but can drift or forget. | Local fact, memory, thread, and continuity ledgers with explicit audit reports. |
| Dialogue / style | Can sound fluent but generic. | Source-backed expression DNA, Character Nuwa cards, subtext rules, and anti-AI prose audits. |
| Long-form continuity | May rewrite completed beats. | Drafts fail if they reset accepted chapter events or character state. |
| Revision | User must notice issues manually. | System produces focused minimal revision briefs with evidence. |
| Downstream production | Mostly prose-centered. | Every accepted chapter can become script packet, storyboard packet, and animatic input. |

The target is not "AI writes more words." The target is "every chapter can survive continuity, quality, and production review."

## 3. Product Spine

The novel system should have seven layers.

### 3.1 Accepted Text Layer

Inputs:

- accepted chapter `.txt` or `.md`;
- source metadata;
- chapter number and title;
- prior audit report references.

Rules:

- accepted text is the only source for user-facing story facts;
- rejected drafts may be mined for parts but do not become canon;
- raw external output is never treated as accepted text.

### 3.2 Story Ledger Layer

Extract or maintain local ledgers:

- chapter fact ledger;
- character state ledger;
- relationship ledger;
- location / scene ledger;
- object / injury / clothing / prop ledger;
- open question and unresolved thread ledger;
- forbidden regression list.

This is the main advantage over a black-box prose tool. The system remembers in structured form.

### 3.3 Writer Brief Layer

Before any next-chapter draft, produce a compact writer brief:

```text
previous chapter facts
current character states
open threads
must advance
must not repeat
allowed scenes
known unknowns
tone / pacing target
style contract / expression DNA
Character Nuwa cards / dialogue voice cards
anti-AI prose constraints
author-only exclusions
```

The writer brief is the replacement for dumping a full world bible, trusting memory, or asking a model to "write beautifully" without a style contract.

### 3.4 Style Distillation Layer

Before any next-chapter draft is accepted as usable, the system should have a project style contract built from approved sources.

Build on:

- `docs/AUTHORING_QUALITY_GUIDELINES.md`;
- `docs/HUMANLIKE_PROSE_AND_DIALOGUE_DISTILLATION_PLAN.md`.

Required outputs:

```text
style_source_pack.json
expression_dna.md
character_voice_cards.json
character_nuwa_gate_report.md
anti_ai_prose_audit.md
minimal_style_revision_brief.md
```

The target is not ornate prose. The target is dialogue caused by pressure, distinct character voices, restrained exposition, varied rhythm, sensory specificity, no generic AI cadence, and no speaking character without a Character Nuwa card or approved light voice card.

### 3.5 Draft Intake Layer

Drafts may come from:

- Codex;
- another GPT chat;
- QuarterFull;
- manual human writing;
- mixed notes.

All sources are equal at intake. None are trusted until audited.

### 3.6 Audit And Revision Layer

Every draft must pass gates:

1. Text integrity gate
   - no broken replacement characters;
   - no obvious encoding loss;
   - no missing chapter title or empty body.
2. Continuity gate
   - previous chapter anchors exist;
   - draft does not reset completed events;
   - required advancement anchors appear;
   - character state is not silently forgotten.
3. Knowledge boundary gate
   - characters do not know author-only facts;
   - narrator does not explain sealed causality too early;
   - dialogue stays within local evidence.
4. Authoring quality gate
   - source-backed style contract exists;
   - every important speaking character has passed the Character Nuwa Gate;
   - one-off background speakers have at least approved light voice cards;
   - recurring characters have dialogue voice cards;
   - dialogue lines are caused by pressure, status, fear, debt, or desire;
   - exposition is embedded in action, procedure, conflict, or withheld tension;
   - repeated AI-ish cadence patterns are patched;
   - `這不是XX，這是XXX`, `這不是 X，這是 Y`, `不是XX，而是XXX`, `不是 X，而是 Y`, `not X, but Y`, and equivalent calibration slogans are absent;
   - visible causality;
   - protagonist reaction beats;
   - NPC independence;
   - companion / animal behavior;
   - material culture consistency;
   - micro-prose quality;
   - actionable ending or chapter propulsion.
5. Production readiness gate
   - scenes can be segmented;
   - important visual anchors are stable;
   - asset candidates are identifiable;
   - script packet conversion is possible.

FAIL means the system must produce a minimal revision brief, not a vague complaint.

### 3.7 Production Handoff Layer

After a chapter is accepted:

```text
accepted prose
  -> script-ready scene packet
  -> storyboard frame packet
  -> asset needs
  -> optional narrated-video / animatic input
```

This is how the novel system becomes more valuable than a prose-only writer.

## 4. The Four Gates That Matter Most

### Gate A: Pre-Draft Memory Gate

Before writing, the system must know:

- what happened last chapter;
- what cannot happen again as a first-time event;
- what each recurring character knows;
- current location, injuries, possessions, social debts, and open threats;
- which mystery remains unknown to the character.

If this cannot be built, the system should stop and ask for the missing accepted chapter or facts.

### Gate B: Post-Draft Continuity Gate

After drafting, the system must check:

- did the draft repeat a completed rescue, first meeting, shelter, injury, debt, or discovery;
- did it erase a relationship;
- did it move characters or objects without cause;
- did it introduce a new location as if already approved;
- did it use broken text or encoding artifacts.

The recently added `audit-chapter-continuity` command is the first slice of this gate.

### Gate C: Humanlike Prose And Dialogue Gate

Before calling a draft good, the system must check:

- do recurring characters sound distinct without speaker tags;
- did every important speaking character pass the Character Nuwa Gate before drafting;
- does every important line have pressure behind it;
- does the narrator avoid translating all subtext immediately;
- does exposition appear through action, procedure, conflict, or cost;
- does the draft avoid repeated calibration phrases, symmetrical AI cadence, generic emotional labels, and the hard-banned `這不是XX，這是XXX` structure;
- does the prose respect the project expression DNA.

Use `docs/HUMANLIKE_PROSE_AND_DIALOGUE_DISTILLATION_PLAN.md` as the product plan for this gate.

### Gate D: Novel-To-Production Gate

Before calling anything "good enough," the chapter must be convertible into:

- beat list;
- action lines;
- dialogue lines;
- scene references;
- visual setup;
- continuity checks;
- storyboard notes;
- asset needs.

If it cannot be converted, the prose may be pretty but it is not production-ready.

## 5. What To Cut From The Current Way Of Working

Cut these habits:

- giving a generative tool only world lore and expecting it to infer continuity;
- calling a draft done before comparing it to the previous accepted chapter;
- letting the user be the first serious reviewer;
- rewriting entire chapters when a small patch would fix a focused issue;
- using external prose output as canon without local audit;
- mixing author-only notes into user-facing prose;
- asking a model to "write beautifully" without a source-backed style contract;
- accepting fluent but interchangeable dialogue;
- letting a character speak before a full Character Nuwa card or approved light voice card exists;
- allowing `這不是XX，這是XXX`, `這不是 X，這是 Y`, `不是XX，而是XXX`, `不是 X，而是 Y`, `not X, but Y`, or equivalent calibration slogans into accepted prose;
- treating video generation as the next step before script/storyboard/asset locks.

## 6. What To Build First

The first useful implementation path should be small and local.

### Phase 1: Chapter Ledger Pack

Goal: turn each accepted chapter into reviewable memory.

Outputs:

```text
chapter_facts.json
character_states.json
open_threads.json
continuity_anchors.json
forbidden_regressions.json
```

Manual version first is acceptable. Automatic extraction can come later.

Success criteria:

- a new GPT window can understand the story state from ledgers without reading the whole novel;
- continuity anchors can be reused by `audit-chapter-continuity`;
- user can edit facts before the next draft.

### Phase 2: Next-Chapter Writer Brief

Goal: produce the exact prompt package for any writer tool.

Output:

```text
writer_brief.md
```

Sections:

- recap;
- current stakes;
- character states;
- relationship states;
- must advance;
- must not repeat;
- allowed scenes;
- language / style constraints;
- Character Nuwa Gate status;
- character dialogue voice cards;
- anti-AI calibration hard bans;
- author-only exclusions;
- desired chapter ending pressure.

Success criteria:

- external tools receive enough continuity to avoid resets;
- external tools receive enough style and dialogue constraints to avoid generic AI prose;
- brief is short enough to paste into GPT or QuarterFull-like tools;
- brief is generated from ledgers, not from memory guesses.

### Phase 3: Novel Draft Intake Review

Goal: accept drafts from anywhere but trust none.

Inputs:

- previous accepted chapter;
- writer brief;
- draft chapter;
- continuity anchors.

Outputs:

```text
chapter_continuity_report.json
anti_ai_prose_audit.md
authoring_quality_report.md
minimal_revision_brief.md
```

Success criteria:

- repeated-first-time events are caught;
- broken text is caught;
- interchangeable dialogue and generic AI cadence are caught;
- review findings cite exact evidence;
- revision brief asks for minimal changes unless the draft is structurally wrong.

### Phase 4: Accepted Chapter To Script Packet

Goal: convert a PASS chapter into a script-ready scene packet.

Build on:

- `docs/NOVEL_TO_SCRIPT_PACKET_BLUEPRINT.md`;
- `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`;
- current `validate-script-packet-draft` command.

Success criteria:

- chapter segments by scene, not paragraph count;
- each beat has visible action and causal link;
- dialogue lines carry knowledge boundary;
- asset needs are listed as candidates, not approved assets.

### Phase 5: Storyboard / Animatic Sidecar

Goal: make the accepted chapter useful for future animation without installing local AI video models.

Outputs:

```text
storyboard_frames.json
asset_needs.json
narrated_video_input.txt
```

Use the standalone `小說朗讀` project only as a sidecar for:

- TTS;
- subtitles;
- simple MP4;
- animatic or narrated chapter preview.

Success criteria:

- the main Kotlin/JVM engine stays clean;
- video sidecar can be regenerated from accepted text and metadata;
- no video output is treated as canon or approved production asset.

## 7. Minimum Viable "Better Than QuarterFull" Loop

The first loop should be:

```text
1. Pick accepted chapter N.
2. Build chapter N ledgers.
3. Build or refresh style source pack and expression DNA.
4. Build or refresh Character Nuwa cards for all speaking characters.
5. Build writer brief for chapter N+1 with Character Nuwa Gate status.
6. Draft chapter N+1 from any source.
7. Run continuity audit.
8. Run anti-AI prose and dialogue audit.
9. Run authoring quality checklist.
10. Produce minimal revision brief.
11. Accept revised chapter only after PASS.
12. Convert accepted chapter to script packet draft.
```

This already beats a pure generation tool because it has memory, tests, and downstream structure.

## 8. Metrics

Track these metrics per chapter:

| Metric | Target |
| --- | --- |
| Continuity regressions | 0 critical |
| Broken text / replacement characters | 0 |
| Missing prior anchor references | 0 critical |
| Character state contradictions | 0 critical |
| Author-only leaks | 0 |
| Speaking character without Character Nuwa card | 0 |
| Interchangeable dialogue voices | 0 critical |
| Generic AI cadence / calibration patterns | patched before acceptance |
| `這不是XX，這是XXX` / `not X, but Y` structures | 0 |
| Missing style source pack | 0 for production chapters |
| Repeated first-time events | 0 |
| Unapproved new scene/location | 0 unless marked `sceneCandidate` |
| Script packet validation | PASS or PASS_WITH_WARNINGS |
| Human revision burden | focused patch, not full rewrite, unless structurally necessary |

## 9. Competitive Position

The project should position itself this way:

- QuarterFull is useful for prose generation.
- This project is a long-form continuity and production pipeline.
- QuarterFull can be one draft source, but not the source of truth.
- The source of truth is local accepted text plus ledgers plus audit reports.
- The end product is not only prose; it is prose that can become script, storyboard, and animatic.

## 10. Immediate Next Milestone Recommendation

Implement only Phase 1 first:

```text
accepted chapter
  -> manually editable chapter ledger pack
  -> manually editable style source pack
  -> manually editable Character Nuwa cards
  -> reusable writer brief
```

Do not jump to animation, image generation, or web UI.

Recommended first engineering milestone:

```text
create-chapter-ledger-template --chapter <path> --out <dir>
validate-chapter-ledger --ledger-dir <dir> [--out <report>]
create-style-source-pack-template --chapter <path> --out <dir>
validate-style-source-pack --style-dir <dir> [--out <report>]
create-character-nuwa-card-template --chapter <path> --out <dir>
validate-character-nuwa-gate --character-dir <dir> [--out <report>]
prepare-novel-writer-brief --ledger-dir <dir> --out <writer_brief.md>
```

These commands should be read-only or file-output only. They should not touch SQLite until a later explicitly approved import flow exists.
