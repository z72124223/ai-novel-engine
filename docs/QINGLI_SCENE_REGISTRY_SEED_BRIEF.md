# Qingli Scene Registry Seed Brief

Status: `SEED_BRIEF_ONLY`

Date: 2026-06-22

This document is a conservative content-planning seed for Qingli scene registry work. It is not a schema, not a setting-pack patch, not a runtime scene definition, not an approved animation asset lock, and not an Oiioii integration plan.

It uses only existing Pilot-2 docs and public setting-pack scene / location / transition evidence. The setting pack also contains author-only / forbidden-reveal boundaries; this brief records those boundaries without copying secret content into user-facing sections.

## 1. VERDICT

`QINGLI_SCENE_REGISTRY_SEED_READY_FOR_READ_ONLY_REVIEW`

The four known formal scene ids can receive seed registry entries for future 04 / 05 / 06 and 02 / 03 work:

- `scene-qingli-herb-warehouse-perimeter`
- `scene-qingli-herb-awning`
- `scene-qingli-runoff-channel`
- `scene-qingli-outer-waiting-line`

These entries are draft planning references. They do not approve new scenes, do not resume 04 / 05 / 06, and do not authorize runtime, schema, Validator, StateReducer, setting-pack, or animation asset implementation.

## 2. Evidence Sources

Read-only sources used:

- `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`
- `docs/SCENE_LOCK_AND_ANIMATION_ASSET_BOUNDARY.md`
- `docs/AUTHORING_QUALITY_GUIDELINES.md`
- `docs/PILOT2_SCENARIO_DESIGN.md`
- `out/pilot2/bootstrap/pilot2_setting_pack.json`
- Pilot-2 evidence checkpoint docs that confirm long-running scene status and transition attempts.

Confirmed formal scene ids and names came from `out/pilot2/bootstrap/pilot2_setting_pack.json`:

| Scene id | Location id | Public title / name |
| --- | --- | --- |
| `scene-qingli-herb-warehouse-perimeter` | `loc-qingli-herb-warehouse-perimeter` | `青犁鎮藥市封倉粉線外` |
| `scene-qingli-herb-awning` | `loc-qingli-herb-awning` | `藥倉檐下查驗棚` |
| `scene-qingli-runoff-channel` | `loc-qingli-runoff-channel` | `藥倉排水溝邊` |
| `scene-qingli-outer-waiting-line` | `loc-qingli-outer-waiting-line` | `外圍待核線` |

Confirmed transitions from the perimeter:

| Transition id | To scene | Required player intent | Travel minutes |
| --- | --- | --- | --- |
| `transition-perimeter-to-awning-witnessed` | `scene-qingli-herb-awning` | `go to awning with witness` | 1 |
| `transition-perimeter-to-runoff-observe` | `scene-qingli-runoff-channel` | `observe runoff edge` | 2 |
| `transition-perimeter-to-waiting-line` | `scene-qingli-outer-waiting-line` | `wait outside the control line` | 1 |

All three transitions are non-forced.

## 3. Registry Entry Template

Each future Qingli production scene registry entry should include at least:

- stable scene id;
- public visual description;
- zones / blocking zones;
- visual anchors;
- allowed NPCs;
- allowed objects / materials;
- forbidden additions;
- transition conditions;
- animation asset notes;
- author-only boundary;
- user-facing safety.

`author-only boundary` should say that a boundary exists and who may use it. It should not quote forbidden reveals, sealed-truth answers, culprit motives, director goals, or character-unknown knowledge.

## 4. Seed Entry: `scene-qingli-herb-warehouse-perimeter`

Stable scene id: `scene-qingli-herb-warehouse-perimeter`

Public visual description:

Rain falls around the sealed herb warehouse perimeter. A powder / chalk control line separates the crowd and unregistered outsiders from the sealed warehouse door. Water runs along medicine-cart wheel marks near the line, and the sealed wooden door remains visible but not touchable.

Zones / blocking zones:

- powder / chalk line edge;
- outer side path along the control line;
- crowd edge;
- medicine-cart side / wheel-mark area;
- mud before the sealed door;
- registry or ledger-facing position when a clerk is present.

These are zones or blocking positions inside the perimeter scene. They are not new scenes.

Visual anchors:

- wet powder / chalk line;
- sealed wooden warehouse door;
- wet seal strips;
- rainwater and cart wheel ruts;
- damaged herb carts or cart-side evidence;
- public crowd pressure around the boundary.

Allowed NPCs:

- `char-player`;
- `char-ai-chan`;
- `char-clerk-luo`;
- `char-shen-qinghe`;
- `char-patrol-he`;
- merchants, buyers, patients' relatives, or other crowd pressure only when already supported by scenario / turn evidence.

Allowed objects / materials:

- ledger / seal book when carried by a clerk;
- powder / chalk line;
- sealed door and seal strips as visible but protected objects;
- rainwater, mud, wooden carts, wooden or iron-rimmed wheel marks;
- locked sample chest only as an existing controlled object, not freely opened.

Forbidden additions:

- new warehouse interior;
- hidden room, cellar, tunnel, or back door;
- modern vehicles, rubber tires, plastic containers, electric lights, firearms, petrochemical products;
- new official institutions not present in the setting pack or later approved docs;
- visible proof of sealed-truth answers.

Transition conditions:

- to `scene-qingli-herb-awning` through `transition-perimeter-to-awning-witnessed` with explicit intent `go to awning with witness`;
- to `scene-qingli-runoff-channel` through `transition-perimeter-to-runoff-observe` with explicit intent `observe runoff edge`;
- to `scene-qingli-outer-waiting-line` through `transition-perimeter-to-waiting-line` with explicit intent `wait outside the control line`.

Animation asset notes:

- Treat crowd edge, powder line, cart side, and doorfront mud as reusable blocking zones.
- Keep the sealed door and powder line visually stable across shots.
- Do not turn close-ups of seal strips, wheel marks, or ledger positions into new production scenes.

Author-only boundary:

- Existing scene contract has forbidden reveal boundaries.
- User-facing output must not reveal sealed-truth answers, director goals, hidden culprit motive, or character-unknown knowledge.

User-facing safety:

- Safe to show as a tense public perimeter with rain, line control, sealed door, carts, and procedural pressure.
- Do not show hidden cause, hidden falsification, or offscreen faction motive as fact.

## 5. Seed Entry: `scene-qingli-herb-awning`

Stable scene id: `scene-qingli-herb-awning`

Public visual description:

A temporary oilcloth inspection awning near the herb warehouse provides a sheltered place for witnessed checking, limited questioning, ledger writing, and controlled sample discussion. It is close enough to observe exterior traces but does not authorize entry into the sealed warehouse.

Zones / blocking zones:

- awning entrance;
- dry ledger position;
- edge facing the warehouse exterior;
- witness standing positions;
- controlled space around any sample box or sealed record.

Visual anchors:

- oilcloth awning;
- temporary table or dry writing surface;
- seal book / ledger when present;
- bamboo slips or markers if already supported;
- locked sample box as a controlled prop when supported;
- rain edge around the awning.

Allowed NPCs:

- `char-player`;
- `char-ai-chan` only under control / supervision;
- `char-clerk-luo`;
- `char-patrol-he`;
- `char-shen-qinghe`;
- other witnesses only when introduced through turn evidence.

Allowed objects / materials:

- seal book / ledger;
- writing tools;
- oilcloth, wood, bamboo, rainwater;
- locked or controlled sample container as visible procedure object, not freely opened.

Forbidden additions:

- unapproved interior access;
- opened herb goods without formal procedure;
- free evidence handling;
- hidden truth shown as visible proof;
- modern lab equipment or industrial tools.

Transition conditions:

- primary incoming transition is `transition-perimeter-to-awning-witnessed`;
- requires explicit player intent equivalent to `go to awning with witness`;
- movement should preserve witness, line, seal, and no-contact boundaries unless a later approved formal turn changes them.

Animation asset notes:

- This can reuse perimeter rain / warehouse exterior assets with a distinct awning cover and dry ledger zone.
- Keep the awning as one production scene, with entrance, table, and edge positions as blocking zones.

Author-only boundary:

- Existing scene contract has forbidden reveal boundaries.
- User-facing output may show limited exterior checking and witness procedure, not sealed-truth answers.

User-facing safety:

- Safe to show as a procedure-limited inspection shelter.
- Do not present arrival at the awning as permission to open the warehouse, handle goods, or assign final blame.

## 6. Seed Entry: `scene-qingli-runoff-channel`

Stable scene id: `scene-qingli-runoff-channel`

Public visual description:

A low drainage edge near the warehouse where dark rainwater and herb residue flow through disturbed mud. It can preserve observable traces but should not become free evidence collection without procedure.

Zones / blocking zones:

- channel edge;
- low mud bank;
- safe observation distance;
- runoff flow direction;
- path back toward the perimeter.

Visual anchors:

- dark rainwater;
- herb residue;
- disturbed mud prints;
- low ground / channel edge;
- water flow away from the warehouse side.

Allowed NPCs:

- `char-player`;
- `char-ai-chan` under control;
- `char-patrol-he` or another supervising witness when movement is controlled;
- `char-clerk-luo` if recording is formally brought to the edge;
- other NPCs only when turn evidence supports their presence.

Allowed objects / materials:

- mud;
- rainwater;
- herb residue;
- wooden or iron-rimmed wheel / cart traces if supported by current evidence;
- simple observation markers or ledger notes when introduced through procedure.

Forbidden additions:

- hidden culvert, tunnel, or secret exit unless separately approved;
- visible full contamination answer;
- modern forensic tools;
- direct handling of sealed goods;
- unapproved new river, road, or warehouse side area.

Transition conditions:

- incoming transition from perimeter is `transition-perimeter-to-runoff-observe`;
- requires explicit player intent `observe runoff edge`;
- observation should stay distinct from touching, collecting, or proving final cause.

Animation asset notes:

- Treat the runoff channel as a compact edge scene with water motion, mud, and trace close-ups.
- Keep evidence ambiguous unless formal turn evidence later narrows it.

Author-only boundary:

- Existing scene contract has forbidden reveal boundaries.
- User-facing output may show traces and ambiguity, not hidden cause.

User-facing safety:

- Safe to show visible runoff, disturbed mud, and cautious observation.
- Do not show culprit proof or final contamination explanation.

## 7. Seed Entry: `scene-qingli-outer-waiting-line`

Stable scene id: `scene-qingli-outer-waiting-line`

Public visual description:

A temporary waiting boundary farther from the sealed warehouse, used to separate unrelated people, reduce pressure near evidence, and hold outsiders until questioning or permission changes.

Zones / blocking zones:

- outer waiting line;
- crowd holding edge;
- quieter side position for questioning;
- path back toward the perimeter;
- separation space for animals or unregistered outsiders when supervised.

Visual anchors:

- temporary boundary line;
- distance from the sealed door;
- crowd and pressure reduced but still audible;
- wet ground and waiting positions;
- sightline back toward the warehouse perimeter.

Allowed NPCs:

- `char-player`;
- `char-ai-chan`;
- `char-patrol-he`;
- waiting crowd or unrelated people when supported;
- `char-clerk-luo` or `char-shen-qinghe` only if the turn brings them there.

Allowed objects / materials:

- boundary markers;
- rain-wet ground;
- simple waiting or questioning positions;
- personal carried items already established by current scene evidence.

Forbidden additions:

- new town gate interior;
- market street expansion;
- hidden official office;
- modern public infrastructure;
- secret revelation by crowd rumor unless formally supported.

Transition conditions:

- incoming transition from perimeter is `transition-perimeter-to-waiting-line`;
- requires explicit player intent `wait outside the control line`;
- this scene should usually reduce immediate evidence access rather than increase it.

Animation asset notes:

- This can reuse perimeter weather and crowd assets with greater distance and lower evidence-detail density.
- Use as a pressure-management scene, not a new investigation hub.

Author-only boundary:

- Existing scene contract has forbidden reveal boundaries.
- User-facing output should not let waiting crowd expose hidden motive or sealed truth.

User-facing safety:

- Safe to show separation, waiting, suspicion, and limited questioning.
- Do not turn waiting line into new canon location beyond the approved boundary.

## 8. Prologue Bridge / `sceneCandidate`

The mountain path / dense fog / hiking descent idea remains a playtest prologue bridge or `sceneCandidate`.

It is not:

- a formal runtime scene;
- an approved production scene;
- a setting-pack patch;
- a guaranteed opening;
- an asset lock.

If 01 later wants to formalize it, the minimum safe route is:

1. 01 opens a new milestone and states the intended layer.
2. 04 may draft only if explicitly handed off.
3. 05 reviews registry fit and production feasibility.
4. 06 displays only reviewed content and waits for explicit user approval.
5. 02 performs any docs / setting-pack / repo work only after 01 assigns it.
6. 03 verifies read-only.

## 9. 04 / 05 / 06 Usage Rules

04 usage:

- 04 may use this seed brief only after 01 explicit handoff.
- 04 produces draft packet content, not final output.
- 04 must mark any new space as `sceneCandidate`.
- 04 must not write repo files, start a Turn, or change canon / setting pack data.

05 usage:

- 05 checks whether the draft stays inside approved scene / zone boundaries.
- 05 checks whether props, NPCs, materials, and visual anchors match the relevant seed entry.
- 05 returns PASS / NEEDS_REVIEW / FAIL.
- 05 PASS allows 06 display; it is not user approval.

06 usage:

- 06 displays only 05 PASS content and only user-facing safe fields.
- 06 does not rewrite, fill missing material, approve new scenes, or generate story.
- 06 must wait for explicit user approval.
- Silence, unclear reaction, topic change, or non-response is not approval.
- 06 reports user approval or feedback to 01; 01 decides the next step.

## 10. Safety And Secrecy

Do not expose:

- author-only secrets;
- sealed warehouse truth;
- director goals;
- hidden culprit / cause;
- character-unknown knowledge;
- scene-contract forbidden reveals;
- production-only surprise setup.

Acceptable wording:

```text
This scene has an author-only boundary. Review / production notes may track it, but user-facing output must not disclose the hidden content.
```

Unacceptable wording:

```text
This scene secretly proves <hidden answer>.
```

## 11. Future Work Boundaries

Not approved by this brief:

- runtime scene registry enforcement;
- schema or migration fields;
- setting-pack patch;
- Validator / StateReducer changes;
- context protocol changes;
- Oiioii integration;
- animation asset lock;
- formal 04 / 05 / 06 restart;
- Candidate B or MVP-8.

Possible future docs-only next steps:

- 03 read-only review of this seed brief;
- a public-safe scene packet sample using one approved scene;
- a separate 01-approved scene registry patch proposal;
- a prologue bridge decision brief for the mountain / fog candidate.
