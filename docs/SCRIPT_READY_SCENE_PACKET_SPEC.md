# Script-Ready Scene Packet Spec

Status: `DOCS_ONLY_CONTRACT`

Date: 2026-06-22

This document defines a script-ready scene packet, a minimal production scene registry contract, and the 04 / 05 / 06 handoff rules. It does not change Kotlin source, tests, schema, migrations, Validator, StateReducer, `resolve-turn`, `stateVersion`, `contextHash`, runtime gates, or setting-pack fields.

## 1. VERDICT

04 / 05 / 06 should stay paused until the project has a clear handoff contract. The next usable unit is not raw prose and not a runtime turn result. It is a structured scene packet that 05 can review, 06 can display, and a future Oiioii / animation pipeline can break down without inventing new production requirements every turn.

This document is a product and authoring contract only. It does not approve new runtime enforcement, schema fields, UI work, or animation integration.

## 2. Purpose

A script-ready scene packet is structured production data for:

- 05 review;
- 06 display / intake;
- future Oiioii or animation breakdown;
- 01 decision-making about scene candidates and content scope;
- 02 documentation or repo work when 01 explicitly assigns it.

It is not:

- final novel prose;
- a formal setting pack patch;
- a runtime scene definition;
- an AI result JSON;
- a direct player-facing transcript;
- a place to store full DB state.

The packet should carry enough information for review and display without leaking author-only secrets, director goals, sealed-truth answers, culprit identity, or knowledge a character should not have.

## 3. Packet Status

Every packet must carry a status.

| Status | Meaning | May 06 display to user? |
| --- | --- | --- |
| `draft` | 04 or another authoring window produced an unreviewed draft. | No |
| `needs_review` | Draft is complete enough for 05 but has not passed. | No |
| `review_passed` | 05 accepted the packet for display or downstream production handoff. | Yes |
| `historical_sample` | Kept as evidence or reference, not necessarily reusable. | Only if 01 approves |
| `formal_candidate` | 01 may consider it for canon / setting-pack / production use later. | Only after 05 PASS |

No packet becomes canon, setting-pack content, or an approved production scene merely by existing.

## 4. Required Packet Fields

The following fields are the minimum contract. This is a documentation shape, not an implemented JSON schema.

| Field | Required | Visibility | Notes |
| --- | --- | --- | --- |
| `packetTitle` | Yes | User-facing | Short human-readable title. |
| `packetStatus` | Yes | User-facing | One of the statuses above. |
| `sourceContext` | Yes | Review-only | Session id, turn id, playtest source, or prompt source. Do not include full DB dumps. |
| `sceneReference` | Yes | Mixed | Approved `sceneId`, `prologueBridge`, or `sceneCandidate`. |
| `scenePurpose` | Yes | User-facing | Why this scene exists in the experience. |
| `playerObjective` | Yes | User-facing | What the player can naturally try now. |
| `locationZone` | Yes | User-facing | Approved scene plus zone / blocking location. |
| `visualSetup` | Yes | User-facing | What can be seen, heard, lit, blocked, and reused visually. |
| `charactersPresent` | Yes | User-facing | Characters present and their visible position. |
| `characterState` | Yes | Review-only by default | Mood, constraints, knowledge boundary, immediate pressure. |
| `approvedProps` | Yes | Production-only by default | Objects and materials allowed in this scene. |
| `forbiddenProps` | Yes | Production-only by default | Objects, materials, tech, or reveals that must not appear. |
| `beatList` | Yes | User-facing after review | Ordered scene beats, not a full prose rewrite. |
| `actionLines` | Yes | User-facing after review | Animation-friendly actions and blocking. |
| `dialogueLines` | Optional but expected | User-facing after review | Usable lines with speaker and knowledge boundary. |
| `interactionChoices` | Yes | User-facing | Player choices that fit current objective. |
| `stateConstraints` | Yes | Review-only | What must not change, such as no sealed reveal or no scene transition. |
| `continuityChecks` | Yes | Review-only | Prior facts, scene locks, NPC state, material culture, and objective continuity. |
| `oiioiiNotes` | Optional | Production-only | Camera, motion, asset reuse, or animation notes. |
| `secrecyKnowledgeNotes` | Yes | Review-only | Author-only boundaries and character knowledge limits. |
| `reviewSummary` | Yes before 06 | Review-only | 05 PASS / NEEDS_REVIEW / FAIL with reasons. |

## 5. Visibility Rules

User-facing fields may be shown by 06 after 05 PASS:

- `packetTitle`;
- `packetStatus`;
- `scenePurpose`;
- `playerObjective`;
- `locationZone`;
- `visualSetup`;
- `charactersPresent`;
- reviewed `beatList`;
- reviewed `actionLines`;
- reviewed `dialogueLines`;
- `interactionChoices`.

Review-only fields are for 01 / 02 / 03 / 05:

- `sourceContext`;
- `characterState` when it includes hidden pressure or knowledge boundaries;
- `stateConstraints`;
- `continuityChecks`;
- `secrecyKnowledgeNotes`;
- `reviewSummary`.

Production-only fields are for animation / Oiioii preparation and should not be casually shown to the user:

- `approvedProps`;
- `forbiddenProps`;
- `oiioiiNotes`;
- blocking notes that reveal future setup;
- asset reuse notes;
- author-only boundaries.

Do not include author-only secrets, sealed warehouse truth, director goals, hidden culprit identity, future plot answers, or knowledge a character should not possess in user-facing packet sections.

## 6. Minimal Packet Template

```yaml
packetTitle: ""
packetStatus: draft
sourceContext:
  project: ""
  sessionId: ""
  turnNumber: null
  sourceArtifact: ""
sceneReference:
  type: approvedScene | prologueBridge | sceneCandidate
  sceneId: ""
  candidateId: ""
scenePurpose: ""
playerObjective: ""
locationZone:
  sceneId: ""
  zoneId: ""
  blocking: ""
visualSetup:
  publicDescription: ""
  visualAnchors: []
  lightingWeatherMaterials: []
charactersPresent:
  - characterId: ""
    visiblePosition: ""
    visibleConstraint: ""
characterState:
  - characterId: ""
    immediatePressure: ""
    knowledgeBoundary: ""
approvedProps: []
forbiddenProps: []
beatList:
  - beatId: ""
    purpose: ""
    visibleAction: ""
    causalLink: ""
actionLines:
  - actor: ""
    line: ""
    blockingZone: ""
dialogueLines:
  - speaker: ""
    line: ""
    knowledgeBoundary: ""
interactionChoices:
  - choiceId: ""
    label: ""
    allowedBecause: ""
stateConstraints: []
continuityChecks: []
oiioiiNotes:
  cameraZones: []
  assetReuse: []
  animationRisks: []
secrecyKnowledgeNotes:
  authorOnlyExcluded: true
  notes: []
reviewSummary:
  verdict: unreviewed
  reviewer: ""
  requiredFixes: []
```

## 7. Scene Registry Minimal Contract

Every approved production scene should have a registry entry before 04 drafts animation-oriented output.

Minimum fields:

- stable scene id;
- public visual description;
- zones / blocking zones;
- visual anchors;
- allowed NPCs;
- allowed objects / materials;
- forbidden additions;
- transition conditions;
- animation asset notes;
- author-only boundary.

Scene registry entries should answer production questions:

- What must remain visually stable?
- Which areas are only blocking zones, not new scenes?
- Which NPCs and props are allowed here?
- Which additions would create new asset cost?
- Which transition conditions must be met before moving elsewhere?
- Which details are public and which are author-only?

## 8. `sceneCandidate` Flow

04 / AI authoring may propose a new space only as `sceneCandidate`.

Required flow:

1. 04 labels the space as `sceneCandidate` and explains why the current approved scene set cannot cover it.
2. 01 decides whether to reject, merge into an existing zone, approve as a production scene candidate, or request more information.
3. 02 performs only the assigned docs / data / repo work if 01 approves a milestone.
4. 03 performs read-only verification.
5. Only after 01 accepts the verified work can the candidate be treated as approved for later 04/05/06 use.

04 must not directly treat `sceneCandidate` as an approved scene. 06 must not display scene-candidate material as production-ready unless 05 passes it and 01 allows display.

## 9. 04 Generation Contract

04 produces drafts only.

04 must:

- mark packet status as `draft`;
- include a short self-check summary;
- list known risks;
- mark any unapproved space as `sceneCandidate`;
- keep author-only secrets out of user-facing sections;
- identify whether the draft is prose-like, packet-like, or incomplete.

04 must not:

- produce final user-facing text;
- show drafts directly to the user;
- start a formal Turn;
- run CLI commands;
- write repo files;
- commit or push;
- write canon or setting pack content;
- approve its own scene candidates.

## 10. 05 Review Checklist

05 review should prioritize production readiness, not only literary polish.

| Check | PASS | NEEDS_REVIEW | FAIL |
| --- | --- | --- | --- |
| Scene registry alignment | Uses approved scene / zone or correctly labels `sceneCandidate`. | Small scene/zone ambiguity. | Invents unapproved scene as if approved. |
| Animation producibility | Visual anchors, blocking, props, and motion are producible and stable. | Missing one or two production details. | Requires uncontrolled new assets or impossible blocking. |
| Information completeness | Packet has enough fields for 06 and production review. | A field is thin but recoverable. | Critical fields are missing. |
| Player objective clarity | Player goal fits immediate knowledge and need. | Goal is visible but slightly broad. | Goal jumps to author plot without causality. |
| Character motivation and knowledge boundary | Characters act from role, pressure, and known facts. | One motivation or knowledge edge needs tightening. | NPCs reveal secrets, act as tools, or use unknown knowledge. |
| Material culture consistency | Props, tech, and institutions fit the approved setting. | One item needs source / downgrade. | Unauthorized modern or setting-breaking supply chain appears. |
| Beat causality | Beats connect by visible cause and reaction. | One causal bridge needs a short fix. | Plot gravity replaces causality. |
| Dialogue usability | Lines are speakable, role-bound, and not exposition dumps. | Some lines need trimming. | Dialogue leaks secrets or reads like setting report. |
| Interaction readiness | Choices are actionable and fit current objective. | Choices need labels or constraints. | Choices smuggle later plot or cannot be acted on. |
| Secrecy safety | User-facing packet avoids author-only secrets. | One review-only detail needs relocation. | Secrets or director goals appear in user-facing material. |

05 verdict rules:

- `PASS`: 06 may display the approved user-facing summary and full production packet sections allowed for display.
- `NEEDS_REVIEW`: return to 04 or 01 with minimal fixes; do not display.
- `FAIL`: stop and ask 01 for a correction milestone or user/product decision.

05 PASS is not user approval. It only means the packet may proceed to 06 for display and human review. 05 must not send unreviewed or unfinished drafts directly to the user.

## 11. 06 Display / Intake Contract

06 displays only 05 PASS content and acts as the human approval window. It is not an automatic PASS window.

06 may:

- show a user-readable summary;
- show approved scene purpose, player objective, visual setup, and interaction choices;
- provide the full production packet only when 01 says that level is safe to show;
- collect player input or user feedback;
- summarize feedback back to 01;
- report explicit user approval back to 01 when the user clearly approves.

06 must not:

- rewrite text;
- fill missing packet fields;
- generate new story;
- start a Turn;
- approve `sceneCandidate`;
- patch canon or setting pack;
- answer author-only questions with hidden information.
- treat user silence, non-response, vague approval, confused reaction, or topic change as approval.

If the user asks 06 to directly fix text, generate new content, approve a scene, or change canon, 06 should summarize the request and report it to 01. 06 is a display / intake window, not a generation or validation authority.

Manual approval gate:

```text
01 explicit handoff
-> 04 generation
-> 05 review
-> 06 display
-> user explicit approval
-> 06 reports approval to 01
-> 01 assigns the next step
```

No automation / scheduler should advance this workflow. A later 02 / 03 engineering milestone or formal Turn lifecycle may begin only after 01 receives explicit user approval and issues a new assignment.

Recommended display split:

1. User-readable summary:
   - scene title;
   - where the player is;
   - immediate objective;
   - visible pressure;
   - available choices.
2. Full production packet:
   - only after 01 allows it;
   - omit author-only secrets;
   - clearly mark review-only or production-only sections that should not be user-facing.

## 12. 01 / 02 / 03 Engineering Loop

The engineering loop remains:

1. 01 selects the next smallest milestone.
2. 02 executes docs / CLI / repo work, verifies, commits, and pushes.
3. 02 directly notifies 03 for read-only review.
4. 03 reviews and reports PASS / NEEDS_REVIEW / FAIL to 01.
5. If 03 passes, 01 chooses the next milestone.
6. If 03 fails or needs review, 01 assigns a correction milestone or stops to ask the user.

04 / 05 / 06 are not the engineering loop mainline. They are currently paused until the packet, registry, review, and display contracts are accepted.

When 04 / 05 / 06 resume, they still do not bypass 01:

- 01 explicitly hands the selected work to 04.
- 04 produces draft packet content only.
- 05 reviews and either passes, requests minimal correction, or fails it.
- 06 displays only 05 PASS content and collects user response.
- User approval must be explicit.
- 06 reports the user decision to 01.
- 01 then decides whether to assign 02 / 03 repo work, a formal Turn flow, another 04 draft, or a stop condition.

Do not use automation, scheduler wakeups, silence, or assumed consent to move from display to execution.

## 13. Boundaries

This document does not approve:

- Kotlin source changes;
- tests changes;
- schema or migration changes;
- Validator changes;
- StateReducer changes;
- `resolve-turn` changes;
- `stateVersion` or `contextHash` changes;
- runtime quality gates;
- new setting-pack fields;
- direct SQLite writes;
- 04 / 05 / 06 restart;
- Candidate B or MVP-8 work;
- Oiioii integration implementation.

Future implementation may separately propose formal fields such as `scriptPacketStatus`, `productionSceneId`, `sceneCandidate`, `reviewVerdict`, `displaySafeFields`, or `animationAssetNotes`. That requires a separate 01 / user-approved milestone.
