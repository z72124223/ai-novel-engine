# Codex Blueprint Execution Control Index V0

Status: `BLUEPRINT_EXECUTION_CONTROL_INDEX_V0_03_PASS`

Date: 2026-07-05

## Purpose

This is the first-read index for future 01/02/03 windows after the
whole-project blueprint update. It reduces navigation cost while preserving the
whole route.

The final product target remains a local-first voiced-comic video workbench.
This index only tells Codex where to read next and how to avoid route drift. It
does not implement features, approve media, approve canon, approve registry,
approve release, or restart the old Phase 20 / Phase 21 chain.

## Optimized First Read Path V0

Marker: `OPTIMIZED_FIRST_READ_PATH_V0`

Future windows should use this short path before reading route-specific history:

1. read this index;
2. read only the top `CURRENT_ACTIVE_ROUTE_DIGEST_V0` and
   `CURRENT_GOAL_MODE_OBJECTIVE_V0` sections of
   `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`;
3. when AGENTS/project rules require `PLANS.md`, read only the top
   `CURRENT_PLAN_WINDOW_V0` section first;
4. read `docs/CODEX_MULTI_WINDOW_OPERATING_MODEL_V0.md` only when the task
   involves window authority, A/B routing, or dispatch;
5. read `docs/CODEX_ROUTE_LOOP_CONVERGENCE_RULE_SET_V0.md` only when the task
   involves optimization, repeated same-family layers, route classification, or
   the `a06-no-new-same-family-status-layer-without-bundle-v0` rule;
6. read `docs/HANDOFF.md` only for exact current thread ids, exact file paths,
   and current evidence.

All older route pointers remain valid on-demand route history. They are not
the default reading queue. A proposed next task must still name the
final-product chain it reduces, preserve review gates, and preserve no
provider/API, no SQLite, no renderer/ffmpeg/TTS, no media generation, no
approval/canon/release, and no production lock boundaries.

## Required First Read

For a new window, read in this order unless the assigned task explicitly names a
route-specific document:

1. `docs/CODEX_BLUEPRINT_EXECUTION_CONTROL_INDEX_V0.md`
2. the top `CURRENT_ACTIVE_ROUTE_DIGEST_V0` and
   `CURRENT_GOAL_MODE_OBJECTIVE_V0` sections of
   `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`
3. the top `CURRENT_PLAN_WINDOW_V0` section of `PLANS.md` when project rules
   require plan context
4. `docs/CODEX_MULTI_WINDOW_OPERATING_MODEL_V0.md` only if the task involves
   window routing or dispatch authority.
5. `docs/HANDOFF.md` only for current evidence, current window ids, and exact
   file paths.

Do not read every blueprint document by default. Use route-specific documents
only when the assigned task names that route.

## Route Documents By Need

| Need | Read |
| --- | --- |
| Whole route overview | `docs/CODEX_WHOLE_PROJECT_BLUEPRINT_V0.md` |
| A-group evolution / learning loop | `docs/CODEX_A_GROUP_EVOLUTION_SYSTEM_V0.md` |
| A-group evolution workflow alignment | `docs/CODEX_A_GROUP_EVOLUTION_SYSTEM_WORKFLOW_ALIGNMENT_V0.md` |
| A-group optimization / lightening / drift prevention | `docs/CODEX_A_GROUP_OPTIMIZATION_AUDIT_LANE_V0.md` |
| Work package direction | `docs/CODEX_WHOLE_PROJECT_WORK_PACKAGE_V0.md` |
| Progress reporting | `docs/CODEX_TRUE_PROJECT_COMPLETION_PROGRESS_MODEL_V0.md` and `docs/PROJECT_COMPLETION_PROGRESS.md` |
| Human approval gates | `docs/CODEX_POSITIVE_APPROVAL_MATRIX_V0.md` |
| Query / retrieval planning | `docs/CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md` |
| Current world-source layering / source archive to future packet ladder | `docs/CODEX_WORLD_SOURCE_LAYERING_BLUEPRINT_GAP_AUDIT_V0.md` |
| 09 foundation cards | `docs/CODEX_09_FOUNDATION_CARD_ORGANIZATION_V0.md` |
| Ordered future route | `docs/CODEX_BLUEPRINT_FUTURE_ROUTE_INTEGRATION_MAP_V0.md` |
| Missing-route audit | `docs/CODEX_BLUEPRINT_FUTURE_ROUTE_GAP_AUDIT.md` |

## Current Goal-Mode Control State V0

Marker: `CURRENT_GOAL_MODE_CONTROL_STATE_V0`

- Whole-project blueprint full update package V0: 03 read-only review PASS.
- Blueprint execution control layer V0: 03 read-only review PASS, now aligned
  with goal-mode optimization.
- Current active layer: `goal_mode_optimization_and_route_convergence_v0` using
  `CURRENT_ACTIVE_ROUTE_DIGEST_V0`, `CURRENT_GOAL_MODE_OBJECTIVE_V0`, and
  `CURRENT_PLAN_WINDOW_V0`.
- A-group evolution system V0: accepted on-demand route evidence for
  root-cause, lesson, rule-upgrade, and regression-check learning after stalls;
  it is not the active route unless the current plan window names it again.
- Phase 20: stopped before edits.
- Phase 21: not started.
- Old renderer-manifest micro-phase chain: paused until explicit 01/user route
  selection.
- Next allowed transition: read-only route-family convergence inventory,
  final-product-chain-tagged bounded package candidate, explicit blocker /
  `TRUE_STOP_CONDITION`, or process/rule patch with regression check. Do not
  start another same-family status/review/route-decision layer without A01
  classification.

## Historical Control State Before Goal Mode

- Whole-project blueprint full update package V0: 03 read-only review PASS.
- Blueprint execution control layer V0: 03 read-only review PASS.
- A-group evolution system V0: highest-priority docs/control route because the
  user found that A-group thinking did not yet force root-cause, lesson,
  rule-upgrade, and regression-check learning after stalls.
- Phase 20: stopped before edits.
- Phase 21: not started.
- Old renderer-manifest micro-phase chain: paused until explicit 01/user route
  selection.
- Current active layer: blueprint execution control layer V0.
- Next allowed transition: route the A-group evolution system through
  `00-01 分流接頭` for A01/B01 governed handling before ordinary feature-route
  continuation.

## Window Roles

| Window | First action | Must not do |
| --- | --- | --- |
| 00-01 分流接頭 | Route evolution triggers and planning/control direction to A組01; route mature implementation packages to B組01. | Do not dispatch B02/B03 directly or diagnose A-group root cause itself. |
| A組06 優化稽核 | Audit route weight, duplicate layers, repeated status loops, and task-drift risk when A組01 asks. | Do not dispatch B02/B03, rewrite blueprint directly, or cut required final-product scope. |
| 01 control | Choose exactly one bounded next task after checking route and quota rules. | Do not treat silence, quota, 02 PASS, or 03 PASS as approval. |
| 02 worker | Execute only the assigned bounded task and report back to 01. | Do not start the next route, contact 03 directly, or make approval claims. |
| 03 reviewer | Read-only review and report PASS / NEEDS_REVISION / FAIL to 01. | Do not edit files, assign 02, or approve final product decisions. |

## Non-Approval Rule

The following never count as human approval:

- 02 completion;
- 03 technical PASS;
- local JSON PASS;
- generated demo/sample artifacts;
- quota availability;
- watchdog wakeup;
- elapsed time;
- silence.

## Boundary

This index is docs/control only. It does not approve provider/API calls,
web/server, direct SQLite writes, renderer/ffmpeg/TTS execution, media
generation, production manifest approval, final media approval, provider/media
handoff, canon, registry, release, approval, production lock, staging, commit,
or push.
