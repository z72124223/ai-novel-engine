# Codex Whole Project Work Package V0

Status: `WHOLE_PROJECT_WORK_PACKAGE_V0_03_PASS`

Date: 2026-07-02

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## Purpose

This work package fixes the project direction before any more small or medium
tasks are assigned. It replaces the old habit of continuing renderer-manifest
micro-phases with a whole-project route that can later be split safely.

This is docs/control only. It does not implement Kotlin, schema, SQLite,
renderer execution, media generation, provider/API calls, canon, registry,
release, approval, or production lock.

## Current Decision

The current accepted direction is:

```text
whole-project blueprint alignment first
  -> completion/progress model
  -> positive human approval matrix
  -> demand-driven query templates
  -> 09 foundation card organization
  -> only then choose the next implementation route
```

Do not resume Phase 20, Phase 21, or the old renderer-manifest chain until this
work package is reviewed and 01/user explicitly selects that route again.

## Work Streams

| Stream | Output | Purpose | Boundary |
| --- | --- | --- | --- |
| A. Blueprint consistency | `CODEX_WHOLE_PROJECT_BLUEPRINT_V0.md`, supporting docs aligned | Make one top-level route entry. | No implementation approval. |
| B. Progress model | `CODEX_TRUE_PROJECT_COMPLETION_PROGRESS_MODEL_V0.md` | Separate old local baseline 100% from true final product completion. | Not a release metric. |
| C. Positive approval matrix | `CODEX_POSITIVE_APPROVAL_MATRIX_V0.md` | Define what human/user approval means. | 02/03 PASS is not approval. |
| D. Demand-driven query templates | `CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md` | Prepare scoped retrieval before 04/05/06/07/08. | No SQL/schema/runtime/provider. |
| E. 09 foundation organization | `CODEX_09_FOUNDATION_CARD_ORGANIZATION_V0.md` | Organize world/character/location/system cards before scene packets. | Not canon or setting pack. |

## Execution Policy

After this work package is accepted, 01 may split work into small or medium
tasks. Each task must name which stream it belongs to and must not silently jump
to a different route.

Recommended first execution order:

1. Stream A consistency fix and 03 review.
2. Stream B progress model and 03 review.
3. Stream C approval matrix and 03 review.
4. Stream D query templates and 03 review.
5. Stream E 09 foundation organization and 03 review.

Implementation routes such as renderer execution, asset registry storage,
setting-pack conversion, or 04/05/06/07/08 resumption require a later explicit
01/user route selection.

## Execution Control Follow-Up

The accepted work package is now wrapped by a lightweight execution control
layer:

- `docs/CODEX_BLUEPRINT_EXECUTION_CONTROL_INDEX_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`
- `docs/CODEX_ROUTE_STATUS_TAGS_V0.md`
- `docs/CODEX_DEFAULT_ROUTE_DECISION_RULES_V0.md`

This follow-up solves the low-cost problems identified by 01/user: speed,
document navigation, implementation-state clarity, and default route decisions.
It is still docs/control only and does not start the next product route.

## Stop Conditions

Stop and return to 01/user if a task needs:

- user worldbuilding decisions;
- 04/05/06/07/08 restart;
- schema, migration, SQL table, or direct SQLite writes;
- asset approval or registry storage decision;
- renderer/ffmpeg/TTS/media generation;
- provider/API or paid-tool spending;
- final media approval, provider/media handoff, canon, registry, release,
  approval, or production lock.
