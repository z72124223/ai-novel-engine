# Current Route Gate V1

Status: `ACTIVE_SINGLE_CURRENT_ENTRY_GATE`

Date: 2026-07-10

## 1. One Current Entry

Every new GPT, Codex CLI, reviewer, or external collaborator window must begin with:

```text
PROJECT_STATE.md
```

`PROJECT_STATE.md` is the only file allowed to declare the current product direction and the active Work Package.

Required startup order:

1. `PROJECT_STATE.md`
2. `.codex/project_controller.md`
3. `docs/CURRENT_ROUTE_GATE_V1.md`
4. the exact active Work Package named by `PROJECT_STATE.md`
5. the active blueprint and execution plan named by that Work Package
6. `AGENTS.md`
7. only the route-specific source, code, tests, Canon, or asset evidence required by the package

Do not start by reading `docs/HANDOFF.md`, an old charter, an old route card, or an old `PLANS.md` section and treating its use of words such as “current”, “active”, or “approved” as present-day authority.

## 2. Current Product Route

The active route is:

```text
Codex CLI runs engine, world-source, script, and shot preparation
→ reproducible Script / Shot / GPT image handoff is committed to GitHub
→ GPT reads an exact handoff Commit and path
→ required locked references are attached to the active GPT image session
→ GPT creates candidate images only
→ user approves, rejects, or requests a variation
→ Codex imports the selected file, computes SHA-256, and runs the existing review and Candidate Reference chain
→ approved visual references support a later short-video handoff
```

The script remains a formal upstream source. It is no longer the only terminal product.

The image route remains the documented resumable production route, but its current execution state is `WP-003B_PAUSED_AFTER_TASK_01`. One Task 01 candidate is archived; no later image task is active. Video remains a planned downstream product only after a future explicit resume and the required image-reference gates.

The active image blueprint is:

- `docs/CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1.md`
- `docs/CODEX_GPT_IMAGE_PRODUCTION_EXECUTION_PLAN_V1.md`

The future Grok-capable video addendum is:

- `docs/CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1_GROK_VIDEO_ADDENDUM.md`

It is planning only until the image approval and Candidate Reference gates are complete.

Current pause boundary:

```text
Task 01 candidate archived with APPROVE_CANDIDATE
Task 02-07 remain BLOCKED_BY_PRIOR_TASK_APPROVAL
no Canon / Production Lock / final scene / accepted Location Reference
no automatic resume
```

## 3. Supersession Rules

Historical content is not rewritten merely because the product direction changed. Its label is reclassified here.

| Historical source | Current classification | Still valid | Superseded for current routing |
| --- | --- | --- | --- |
| pre-gate full `docs/HANDOFF.md` | `HISTORICAL_HANDOFF_EVIDENCE` | old Commit ids, past verification, historical reasoning | any statement that it is the first current entry; “only script” as terminal product; images/video paused as current route |
| `world/chenxing/formal/29_project_script_first_charter_v1.json` | `HISTORICAL_PRODUCT_ROUTE_CHARTER` | script quality, script as upstream, no silent Canon mutation | script as the only terminal product |
| `docs/AUDIO_NOVEL_PHASE_HANDOFF_AND_ABORT_V1.md` | `HISTORICAL_EXPERIMENT_ABORT_EVIDENCE` | audio experiment was stopped; do not revive that exact failed chain silently | prohibition on every future image/video route |
| `docs/CODEX_GROK_BUILD_HANDOFF_AND_IMAGE_PRODUCTION_LANE_V0.md` | `PARTIALLY_CURRENT_ROLE_EVIDENCE` | Grok story contribution, Codex integration, reference-first, candidate-only, user approval | terminal deliverable is text only |
| old route cards and whole-project control docs | `ON_DEMAND_HISTORICAL_CONTROL_EVIDENCE` | safety, approval, no-silence-as-approval, source boundaries | active Work Package selection unless `PROJECT_STATE.md` points to them |
| README text saying art/video is paused | `STALE_STATUS_SUMMARY` | no in-engine external API, no automatic approval | current product direction |

When a historical file conflicts with the current route, preserve the historical file as evidence and follow the current gate. Do not edit formal historical story or charter content solely to make it look current.

## 4. Handoff Label Rule

A document written in the past may contain a heading such as:

```text
目前定案
Current User Override
Current Route
Active Milestone
```

Those words are scoped to the document's recorded date and Commit. They are not self-renewing labels.

A label is current only when all of the following are true:

1. `PROJECT_STATE.md` points to it or to a Work Package that names it.
2. Its route does not conflict with the latest explicit user direction.
3. Its stated files and Commit evidence still exist.
4. A newer route gate has not reclassified it as historical.

## 5. Archive And Deletion Policy

Use three steps:

```text
MARK STALE
→ ARCHIVE OR INDEX
→ DELETE ONLY AFTER EVIDENCE
```

### 5.1 Mark stale

Prefer a current-route table, archive pointer, or status banner. Do not rewrite historical reasoning or Canon to pretend it was never valid.

### 5.2 Archive

Archive candidates may be moved or replaced by a pointer only after:

- inbound link search;
- code/test/sample dependency scan;
- verification that no active Work Package reads them;
- a reversible Commit;
- exact old Commit/path recorded in an archive index.

The pre-gate full handoff is preserved by Git history and indexed in:

- `docs/archive/HANDOFF_LEGACY_INDEX_V1.md`

### 5.3 Delete

Deletion is allowed only for a separately approved cleanup package and only when the item is proven unnecessary.

Potential deletion candidates:

- generated caches;
- duplicate temporary exports;
- unreferenced local output copies;
- bulk failed renders whose useful decision evidence is preserved in a drift report;
- obsolete generated copies that are byte-identical to an authoritative source.

Do not delete without explicit evidence:

- formal Canon or world-source files;
- migrations;
- tests or fixtures still referenced by tests;
- accepted or author-locked assets;
- provenance, Hash, review, or Candidate Reference evidence;
- rejected-drift evidence that explains a binding production decision;
- user-provided raw sources;
- files named by an active Work Package.

No physical archive/delete cleanup is authorized by this gate. A later bounded cleanup package must create the dependency and deletion manifest first.

## 6. Grok Future Video Boundary

Grok video generation is a planned downstream provider option, not the current active Work Package.

The future route is:

```text
approved clean keyframe or approved visual references
→ provider-neutral VideoShotHandoff
→ manual Grok video Pilot
→ user video review
→ Codex records file identity, Hash, review, and continuity evidence
```

A comic page with speech bubbles, subtitles, borders, or multiple panels is not the formal video source frame. The video source should be a clean single-shot keyframe plus a motion/camera contract.

Grok must not change story, Canon, character design, costume, scene geometry, or shot intent during video generation.

No xAI API key, paid job, provider integration, video automation, or Kotlin API work is approved now.

## 7. Automatic Execution Reality

The Repository can tell Codex what to do after Codex is started. It cannot start a Codex CLI process by itself.

Current allowed unattended unit:

```text
one explicitly started Codex session
→ one active Work Package
→ validation
→ Commit and Push
→ completion report
→ stop
```

Codex must not automatically advance to the next product Work Package after Push. The next package may require controller review, a reference upload, or a user visual decision.

The local AI task queue is not a background daemon. A running terminal, automation service, or scheduler would have to invoke it. None is authorized by this gate.

If the computer sleeps, the terminal closes, authentication expires, a true stop condition occurs, or the process is interrupted, no result should be assumed.

## 8. Conflict Resolution

Current authority order:

1. latest explicit user instruction;
2. `PROJECT_STATE.md`;
3. `.codex/project_controller.md`;
4. this gate;
5. active Work Package and its active blueprint;
6. verified code, tests, CLI output, Canon, and locked asset evidence;
7. historical handoffs, charters, plans, and route cards.

Historical evidence may constrain safety and provenance. It cannot silently override the current product direction.
