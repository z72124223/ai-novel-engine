# Blueprint Data Layer And Planning Register

Status: `BLUEPRINT_PLANNING_REGISTERED`

Date: 2026-06-23

This document records the current blueprint planning register supplied by 01. It is docs-only planning. It is not canon, not a setting pack, not runtime data, not approved asset data, not SQLite data, and not permission to start 09 / 04 / 05 / 06 / 07 / 08.

## 1. VERDICT

The current 01-managed blueprint planning state is registered as `BLUEPRINT_PLANNING_REGISTERED`.

The entries below form a non-canon candidate blueprint pool. They can guide future review, foundation planning, query templates, setting-pack conversion decisions, and storyboard-first production planning only after 01 explicitly selects a next milestone.

Nothing in this document:

- makes a candidate canon;
- patches a setting pack;
- changes runtime behavior;
- approves an asset;
- starts a window;
- schedules automation;
- writes SQLite;
- approves Candidate B, MVP-8, Oiioii, Seedance, or external API work.

## 2. Blueprint Data Layer Schema

Every future blueprint register entry should use these fields:

| Field | Meaning | Requirement |
| --- | --- | --- |
| `id` | Stable blueprint record id. | Required. |
| `name` | Human-readable title. | Required. |
| `system` | System or subsystem described by the blueprint. | Required. |
| `sourceWindow` | Window number or source handoff that produced the candidate. | Required when known. |
| `sourceVersion` | Version label from the source window. | Required when known. |
| `verdict` | Review verdict such as PASS by 14, REVISE, FAIL, or source handoff. | Required. |
| `status` | Current product-control status. | Required. |
| `dependencies` | Other blueprint records, foundation choices, or product contracts this entry depends on. | Optional but recommended. |
| `risks` | Main risks if promoted, merged, or converted too early. | Required. |
| `canonBoundary` | Boundary statement for canon / setting pack / runtime / asset status. | Required. |
| `settingPackReady` | Whether the entry is ready for direct setting-pack conversion. | Required boolean. |
| `nextAction` | Next permitted action. | Required. |

Template:

```yaml
id: ""
name: ""
system: ""
sourceWindow: ""
sourceVersion: ""
verdict: ""
status: "non-canon candidate"
dependencies: []
risks: []
canonBoundary: "not canon / not setting pack / not runtime / not approved asset"
settingPackReady: false
nextAction: "01 explicit assignment required"
```

## 3. Default Candidate Fields

Unless a row explicitly says otherwise, every blueprint pool entry below inherits:

```yaml
status: "non-canon candidate"
canonBoundary: "not canon / not setting pack / not runtime / not approved asset"
settingPackReady: false
nextAction: "由 01 明確指派"
```

Default dependencies:

- user / 00 answers or 01-approved foundation material;
- simulation-first knowledge and truth guardrails;
- storyboard-first final deliverable boundary;
- future 01 selection before any setting-pack or runtime conversion.

Default risks:

- candidate details may conflict with the final new-world foundation;
- bulk conversion can overload setting-pack or runtime context;
- source-window names can be mistaken for canon;
- derived aliases can drift from reviewed source cards;
- system mechanics can imply unapproved material culture, power-system, social, or asset commitments.

## 4. Non-Canon Candidate Blueprint Pool

| id | name | system | sourceWindow | sourceVersion | verdict | status |
| --- | --- | --- | --- | --- | --- | --- |
| `bp-index-v0.2` | 藍圖候選池整合索引 v0.2 | Blueprint candidate pool index | 45 | v0.2 | PASS by 14 | non-canon candidate blueprint index |
| `bp-15-resource-v0.4` | 15 資源系統 v0.4 | Resource system | 15 | v0.4 | PASS by 14 | non-canon candidate |
| `bp-16-social-v0.4` | 16 社會 / 國家 / 要塞治理系統 v0.4 | Social / state / fortress governance system | 16 | v0.4 | PASS by 14 | non-canon candidate |
| `bp-17-internal-skill-v0.4` | 17 內功系統 v0.4 | Internal skill system | 17 | v0.4 | PASS by 14 | non-canon candidate |
| `bp-18-external-skill-v0.4` | 18 外功系統 v0.4 | External skill system | 18 | v0.4 | PASS by 14 | non-canon candidate |
| `bp-19-weapons-v0.4` | 19 武器兵器系統 v0.4 | Weapons system | 19 | v0.4 | PASS by 14 | non-canon candidate |
| `bp-21-war-tools-v0.4` | 21 戰爭工具系統 v0.4 | War tools system | 21 | v0.4 | PASS by 14 | non-canon candidate |
| `bp-24-poison-v0.4` | 24 毒系統 v0.4 | Poison system | 24 | v0.4 | PASS by 14 | non-canon candidate |
| `bp-25-pills-v0.4` | 25 丹藥系統 v0.4 | Pills / elixir system | 25 | v0.4 | PASS by 14 | non-canon candidate |
| `bp-27-formations-v0.4` | 27 陣法系統 v0.4 | Formation system | 27 | v0.4 | PASS by 14 | non-canon candidate |
| `bp-46-nine-system-supplement-v0.3` | 46 九套總補完 v0.3 | Overall nine-system supplement | 46 | v0.3 | PASS by 14 as overall reference | non-canon candidate reference |

Priority rule:

- Individual v0.4 cards take priority over `bp-46-nine-system-supplement-v0.3` where they overlap.
- `bp-46-nine-system-supplement-v0.3` is an overall reference only. It does not override later individual v0.4 cards.
- `bp-index-v0.2` indexes candidates. It does not make any candidate canon.

## 5. Derived Alias Rule

`derivedIndexAliases` are always `needs future review`.

Derived aliases may help future humans find related ideas, but they must not impersonate 14 PASS original card names. A derived alias must record:

- original `id`;
- original `name`;
- proposed alias;
- reason for alias;
- reviewer;
- date;
- status `needs future review`.

Unsafe:

```yaml
derivedIndexAliases:
  - "approved resource canon"
```

Safer:

```yaml
derivedIndexAliases:
  - alias: "resource pressure baseline"
    originalId: "bp-15-resource-v0.4"
    originalName: "15 資源系統 v0.4"
    status: "needs future review"
```

## 6. Retained Window Candidate Intake

This section records a pending character-branch candidate intake from 01. It does not make the character canon, setting-pack data, runtime data, or an approved asset.

| id | name | system | sourceWindow | sourceVersion | verdict | status | nextAction |
| --- | --- | --- | --- | --- | --- | --- | --- |
| `bp-character-lu-hesheng-v0.2` | 陸禾生 | character / NPC candidate | 47 人物創造答案化補完 (`019ef1d4-53f8-7800-bc57-38aab7488cc3`) | v0.2 | PASS by 38 人物內在系統總審核 (`019ef1ad-72e5-79b1-98eb-d28b680265dd`) | non-canon candidate character blueprint | wait for 01; do not include in first setting-pack conversion batch by default |

Inherited boundary:

```yaml
canonBoundary: "not canon / not setting pack / not runtime / not approved asset"
settingPackReady: false
```

Related pending branch status:

- 34: REVISE, waiting for later 01 decision.
- 37: REVISE, waiting for later 01 decision.
- 35: PASS.
- 36: PASS.
- 47: PASS.
- 38: standby.

## 7. Setting Pack Conversion Rule

Do not put the entire blueprint pool into a setting pack or runtime.

Only 01 may later select a first minimal, low-coupling, verifiable skeleton for conversion. The first candidate direction may include:

- fortress-style safety zone;
- 息潮層 limitation;
- lowest shared rules for resource, social, and formation systems.

Formal conversion requires a separate explicit milestone. That milestone must use the setting-pack pipeline:

```text
parse -> validate -> diff -> preview -> apply
```

It must not:

- directly write SQLite;
- silently patch active canon;
- bypass setting-pack validation;
- bypass author-only boundaries;
- raise context budgets;
- convert unreviewed aliases as if they were reviewed source cards.

Until that milestone exists, every pool entry remains:

```yaml
settingPackReady: false
canonBoundary: "not canon / not setting pack / not runtime / not approved asset"
```

`bp-character-lu-hesheng-v0.2` should not be included in the first setting-pack conversion batch by default. It requires a later 01 decision because it is a character / NPC candidate, not a low-coupling shared-system skeleton.

## 8. Window Archive Rules

This section records policy only. It does not archive windows from the repo.

Keep active or accessible:

- 00;
- 01;
- 14;
- 45;
- 38;
- 43;
- 44;
- 47;
- 02 / 03;
- any window 01 says is still waiting for review, repair, or handoff.

Already archived according to 01:

- 11;
- 12;
- 13;
- 15;
- 16;
- 17;
- 18;
- 19;
- 21;
- 24;
- 25;
- 27;
- 46.

Archive conditions:

- PASS / FAIL has closed the task; or
- REVISE has been transferred to a new task and the original window no longer needs real-time interaction.

Before archiving, record the shortest useful state:

1. system name;
2. final or latest version;
3. PASS / REVISE / FAIL;
4. three highlights;
5. three risks / pending issues;
6. whether it can enter the blueprint pool.

## 9. Manual Handoff Only

The current control model remains manual handoff only:

- 00 handles communication, organization, and short user-facing reports.
- 01 maintains the task queue, blockers, and PASS / REVISE / FAIL status table.
- 14 or other review windows send short reports to 01.
- 01 decides whether to send a source window back for revision or create a new 02 repo milestone.
- 45 remains available but standby. It must not self-schedule another batch.
- 02 executes repo docs / CLI / engineering milestones only after 01 explicit handoff.
- 03 performs read-only verification after 02 directly notifies it.

No automation / scheduler handoff is approved.

## 10. Relationship To Existing Product Docs

This register sits above current docs-only product planning:

- `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md`: collects user / 00 answers before a foundation brief.
- `docs/DEMAND_DRIVEN_QUERY_LAYER_BLUEPRINT.md`: defines future scoped World / Asset / Situation queryers.
- `docs/SIMULATION_FIRST_KNOWLEDGE_TRUTH_RULES.md`: defines the top product-layer rule that world truth is causality and distributed actor knowledge, not a plot objective.
- `docs/STORYBOARD_AND_CAMERA_PLANNING.md`: keeps current final deliverable focused on reviewable storyboard images / packets.
- `docs/PRODUCTION_ASSET_REGISTRY_PLANNING.md`: explains why 07 asset candidates and 08 storyboard pickup need asset consistency.

Blueprint entries can inform these documents later, but only through 01-approved milestones. They must not be silently promoted into any current workflow.

## 11. Future 03 Review Focus

03 should verify:

- the document status is `BLUEPRINT_PLANNING_REGISTERED`;
- every required schema field is present;
- all listed pool entries are non-canon candidates;
- `bp-character-lu-hesheng-v0.2` is recorded only as a retained non-canon character blueprint, with 34/37 still REVISE and 38 standby;
- defaults include `canonBoundary`, `settingPackReady: false`, and 01-controlled `nextAction`;
- derived aliases are marked `needs future review`;
- setting-pack conversion requires a later parse / validate / diff / preview / apply milestone;
- window archive rules are documented as policy only;
- manual handoff only remains intact;
- no runtime, schema, SQLite, setting-pack apply, approved asset, automation, 09, 04/05/06/07/08, Candidate B, or MVP-8 approval is implied.
