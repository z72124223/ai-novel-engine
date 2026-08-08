# WP-003A Daping Coast Location Bible Handoff Report

## Result

Primary result: `WP003A_READY_FOR_GPT_LOCATION_TASK_01`
WP-003A adds a separate preliminary Location Bible protocol and a real Daping
Coast handoff without weakening the existing story `shot_queue_v1` contract.
Exactly one task is structurally ready for GPT:

```text
daping-lb-01-master-panorama
```

This result enables only the first Location Bible candidate task. It does not
enable WP-003B automatically, does not make WP-004 ready, and does not generate
or approve an image.

## Evidence Base

- Base branch: `origin/codex/mvp6-checkpoint-branching-spec`
- Base SHA: `a6fbe48e0eb21e0e71f94b62c750448ba9dfb9d2`
- Working branch: `feature/wp003a-daping-location-bible-handoff`
- Accepted WP-003 evidence: `7cd3f82f49d05703c95c1b58e313a03d6967d6f4`
- WP-003 finding: `WP003_SCHEMA_GAP_FOUND`

The formal opening arc, prologue, storyboard, visual-style file, WP-003 location
gap inventory, WP-003 report, and four locked visual references were inspected.
No source conflict was found.

## Architecture Decision Implemented

Implemented:

```text
separate preliminary Location Bible handoff
```

Not implemented:

- no change to `schemas/production/shot_queue_v1.schema.json`;
- no fake character binding;
- no Kotlin compiler or provider adapter;
- no story Shot handoff;
- no image-generation job.

A Location Bible foundation task may contain zero visible characters. The new
protocol is upstream of story Shots and keeps normal story Shot validation strict.

## New Contracts

### `location_bible_handoff_v1`

The handoff schema defines:

- Git/source identity and non-self-referential content Hash;
- formal source artifacts with Repository path, SHA-256, byte size, and visibility;
- the bounded foundation-generation exception;
- seven task records with explicit dependencies;
- candidate-only and human-review boundaries;
- public-fact and author-secret separation;
- return-to-Codex routing through the existing safety chain.

### `location_bible_return_to_codex_v1`

The return schema identifies candidates by Location Bible task and component,
without a fake `shotId`. It preserves the standardized user-decision and drift
vocabulary and requires the existing External Result, Human Review, Candidate
Reference, Registry, and Acceptance stages.

## Real Task Graph

| Task | Component | Initial state | Dependencies |
| --- | --- | --- | --- |
| `daping-lb-01-master-panorama` | `MASTER_PANORAMA` | `READY_FOR_GPT` | none |
| `daping-lb-02-top-down-zone-plan` | `TOP_DOWN_ZONE_PLAN` | `BLOCKED_BY_PRIOR_TASK_APPROVAL` | 01 |
| `daping-lb-03-seam-tidepool-detail` | `ANCHOR_DETAIL` | `BLOCKED_BY_PRIOR_TASK_APPROVAL` | 01, 02 |
| `daping-lb-04-lu-aijiang-scale-reference` | `SCALE_REFERENCE` | `BLOCKED_BY_PRIOR_TASK_APPROVAL` | 01, 02, 03 |
| `daping-lb-05-shot01-facing-view` | `DIRECTIONAL_VIEW` | `BLOCKED_BY_PRIOR_TASK_APPROVAL` | 01, 02, 03, 04 |
| `daping-lb-06-shot02-seam-facing-view` | `DIRECTIONAL_VIEW` | `BLOCKED_BY_PRIOR_TASK_APPROVAL` | 01, 02, 03, 04 |
| `daping-lb-07-shot03-sea-reaction-view` | `DIRECTIONAL_VIEW` | `BLOCKED_BY_PRIOR_TASK_APPROVAL` | 01, 02, 03, 04 |

The dependency graph is acyclic. The optional 360 panorama is only recorded as
deferred work and is not in the initial ready queue.

The top-down plan uses `4:3`, one of the Work Package's permitted ratios, so the
production diagram can carry zone labels without changing later cinematic output
ratios.

## Reference Upload Convention

The existing `reference_upload_checklist_v1` schema has no per-task `required`
field. WP-003A therefore uses this bounded convention:

- the checklist's global `required` flag describes the currently executable task;
- `tasks[].requiredReferenceIds` describes references that become mandatory for
  a later task;
- later references remain `NOT_REQUIRED` until their dependency gate opens.

Initial required ChatGPT upload count: **1**

```text
world/chenxing/assets/style_reference/chenxing_opening_style_cinematic_misty_v1.png
SHA-256: a983170bb67dcd78961acf6deffb80068b6e2daf6387f87eb9f1720dd5288d6a
byte size: 3010320
```

Task 04 later requires the Lu production body turnaround, opening costume, and
Aijiang authoritative turnaround. The six Lu face photos are not requested by
this Location Bible foundation package.

## Hash And Path Evidence

Ten source artifacts and four reference checklist items were checked against the
worktree at the recorded base. Every present file matched both its SHA-256 and
byte size where recorded.

The real handoff content Hash is:

```text
a3b6d6f421fb51983f2652c48ff9bf9a9138e1ee1d497dcc5bce8442255798da
```

Normalization uses recursively sorted object keys, compact UTF-8 JSON, and
Unicode preservation. `handoffContentSha256` and `handoffCommitSha` are set to
JSON `null` only in the in-memory object used for calculation.

No output contains a private absolute path. Expected image filenames are future
candidate outputs, not claims that those files exist.

## Prompt And Secret Boundary

The real handoff copies only public, production-visible location facts into each
task's `textualAuthority`. Formal source paths are provenance evidence, not an
instruction to copy every source field into the prompt.

The task payload excludes:

- author-only causal explanations;
- crossing mechanics;
- later plot reveals;
- post-crossing content;
- private source paths.

Task 01 permits creative freedom only for non-causal rock, puddle, neutral
vegetation, cloud, and worn-surface variation. It forbids fantasy architecture,
anachronistic clothing, supernatural effects, hidden answers, and later-story
discoveries.

## Candidate And Approval Boundary

- `READY_FOR_GPT` means the file protocol and task are structurally complete.
- If the style reference is not attached to the active conversation, GPT must
  return `REFERENCE_UPLOAD_REQUIRED` with the exact Repository path.
- Any generated output remains a candidate.
- User approval applies only to the exact candidate.
- Codex must compute the file SHA-256 and run the existing intake/review/reference
  chain before a Location Reference can be accepted.
- No candidate or accepted Location Reference writes Canon or SQLite.
- A Location Reference does not approve later story Shots or final media.

## Validation

- JSON parse: `PASS`
- Draft 2020-12 declarations: `PASS`
- Local `$ref` resolution: `PASS`
- Schema regex compilation: `PASS`
- Full JSON Schema meta-validation: `SCHEMA_META_VALIDATION_NOT_AVAILABLE`
- Repository source/reference path, SHA-256, and byte-size validation: `PASS`
- Normalized handoff SHA-256: `PASS`
- Task dependency DAG: `PASS`
- Unique initial `READY_FOR_GPT` task: `PASS`
- Fake-character scan: `PASS`
- Candidate/approval/Canon boundary scan: `PASS`
- Private absolute-path scan: `PASS`
- Secret-boundary scan: `PASS`
- Kotlin/Gradle: not run because Kotlin changes are forbidden

No compatible JSON Schema meta-validator was preinstalled. No dependency was
installed. Custom contract checks were used for required fields, constants,
enums, local references, regexes, paths, hashes, task dependencies, and safety
boundaries; this is not misreported as full meta-schema validation.

## Files

New primary artifacts:

- `schemas/production/location_bible_handoff_v1.schema.json`
- `schemas/production/location_bible_return_to_codex_v1.schema.json`
- `samples/production_handoff/location_bible_handoff_v1.example.json`
- `samples/production_handoff/location_bible_return_to_codex_v1.example.json`
- `world/chenxing/production/location_bible_handoffs/daping_coast_v1/README.md`
- `world/chenxing/production/location_bible_handoffs/daping_coast_v1/location_bible_handoff.json`
- `world/chenxing/production/location_bible_handoffs/daping_coast_v1/reference_upload_checklist.json`
- `world/chenxing/production/location_bible_handoffs/daping_coast_v1/location_bible_return_template.json`
- `docs/production/WP003A_DAPING_LOCATION_BIBLE_HANDOFF_REPORT.md`

Current-package status only is updated in `PLANS.md` and `PROJECT_STATE.md`.

## Drift Report

- Deviation from the Work Package: none.
- Highest-risk area: keeping provenance files distinct from prompt-visible facts.
- Human confirmation still required: style-reference upload and every visual
  candidate decision.
- Recommended next round: controller review followed by a separately activated
  WP-003B for task 01 only. Do not start WP-004.

Primary result: `WP003A_READY_FOR_GPT_LOCATION_TASK_01`
