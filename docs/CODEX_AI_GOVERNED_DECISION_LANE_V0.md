# Codex AI-Governed Decision Lane V0

Status: `CURRENT_CONTROL_PACKET`

This document records the corrected decision rule for the AI screenplay /
voiced-comic project. Routine technical planning decisions should be made by
the planning windows, not by treating `00` or the user as a manual approval
gate.

This is docs/control only. It does not start implementation, create a live
thread, approve canon, approve registry data, approve assets, generate media,
call providers, resume 04/05/06/07/08, resume Phase 20 / Phase 21, or claim
release / final approval / production lock.

## Principle

AI windows should decide within objective project boundaries.

`00` remains the user-facing direction window, but `00` should not be a routine
human review checkpoint for every mature technical proposal. If the proposal is
inside already-approved project direction and does not cross a stop condition,
Group A may promote it to a B-group implementation package.

## AI Technical Approval Gate

`A組01 整合規劃` may mark a proposal as `AI_TECHNICAL_APPROVED` and hand it to
`B組01 派工` when all of these are true:

- `A組02` / `A組03` / `A組04` or a planning branch produced enough evidence.
- The proposal is tied to the final storyboard / voiced-comic / video
  deliverable.
- Fixed core, variable modules, and composition rules are clear enough for the
  next bounded task.
- The next task is bounded and can be reviewed by `B組03`.
- Expected files or artifact types are known.
- Forbidden work is explicit.
- Validation or review evidence is defined.
- The proposal does not require new user worldbuilding content.
- The proposal does not require provider/API, external paid tools, funding,
  renderer/ffmpeg/TTS/media generation, final media approval, canon, registry,
  release, production lock, or a product-direction change.
- The proposal does not restart 04/05/06/07/08 or Phase 20 / Phase 21.

If these conditions pass, `A組01` should output:

```text
IMPLEMENTATION_PACKAGE
Decision: AI_TECHNICAL_APPROVED
Package:
Goal:
Accepted scope:
Forbidden work:
Files likely touched:
Validation expected:
Suggested B組01 task:
Reviewer expectation:
Stop conditions:
Status: READY_FOR_B組01_DISPATCH
```

That package may go to `B組01` without waiting for routine `00` approval.

## When 00 Is Still Needed

Return to `00` / user only when the decision is truly product-level or
external-state-dependent:

- product direction is uncertain or changes the final deliverable;
- user worldbuilding content is missing;
- a budget, funding, external paid/provider tool, or provider/media handoff
  decision is needed;
- canon, registry, release, final media approval, approval, or production lock
  is needed;
- quota is unsafe for the proposed next work;
- the proposal cannot be reviewed by the current A/B window system;
- the loop is repeating without new evidence;
- true final project completion may be reached.

## B-Group Dispatch Rule

`B組01 派工` may accept an `AI_TECHNICAL_APPROVED` package from `A組01` as an
accepted package. `B組01` still must convert it into exactly one bounded
`B組02` task and preserve the single-lane implementation pipeline:

`B組01 -> B組02 -> B組01 -> B組03 -> B組01 -> A組01/00`

`B組01` must not rewrite blueprint direction, run Group A ideation, or dispatch
more than one active implementation task.

## A-Group Hold Relay Rule

An A-group hold is not automatically a user stop and not automatically a
whole-project stop.

If `B組01` receives any of these from `A組01`:

- `A_GROUP_BRANCH_REQUIRED`
- `HOLD_FOR_A_GROUP_PLANNING`
- `HOLD_FOR_EXPLICIT_CLASSIFICATION`
- a route-authority gap that `A組01` says is not a `TRUE_STOP_CONDITION`

then `B組01` must route exactly one bounded planning/control request back to
`A組01`. The request should ask `A組01` to execute or coordinate the needed
A-group planning branch, define the missing route authority, or return an
`IMPLEMENTATION_PACKAGE` with `Decision: AI_TECHNICAL_APPROVED` and
`Status: READY_FOR_B組01_DISPATCH`.

`B組01` must not send this case to `00` / user for routine technical approval,
and must not invent the A-group planning result itself. `00` is contacted only
after `A組01` explicitly returns `TRUE_STOP_CONDITION` or a genuine user-only
decision is required.

If one sub-route is blocked, `B組01` should ask `A組01` whether to produce a
safe continuation package, coordinate an A-group branch, or switch to another
unblocked blueprint route. One blocked sub-route is not final project
completion and not a full-project stop.

## Watchdog Rule

If the watchdog sees an `AI_TECHNICAL_APPROVED` `READY_FOR_B組01_DISPATCH`
package from `A組01` and no B-group handler has processed it, the watchdog may
route exactly one status handoff to `B組01`. It should not pause merely because
`00` has not manually approved the package.

If the output is only `READY_FOR_00_DECISION`, the watchdog should return it to
`A組01` for reclassification under this AI technical approval gate before
routing it to `B組01`.

## Pending Prompt-Surface Candidate

The `prompt_surface_leakage_boundary_v0_docs_control` candidate is implemented
as the docs/control pointer `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`.
It records deny-by-default prompt surface rules only. It does not authorize
Kotlin/CLI/runtime/schema/sample/provider/API/media work, 04/05/06/07/08
restart, Phase 20 / Phase 21 restart, canon, registry, release, final approval,
or production lock.

## Hard Boundaries

- No provider/API calls.
- No web/server/open ports.
- No direct SQLite writes.
- No schema/migration changes unless explicitly assigned and validated through
  project rules.
- No renderer/ffmpeg/TTS/media generation unless explicitly assigned.
- No 04/05/06/07/08 restart unless explicitly assigned.
- No Phase 20 / Phase 21 restart unless explicitly assigned.
- No canon, registry, release, final media approval, approval, or production
  lock claims.
- No staging, commit, or push unless the user explicitly asks.
