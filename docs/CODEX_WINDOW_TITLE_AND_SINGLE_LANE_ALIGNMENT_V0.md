# Codex Window Title And Role Alignment V1

Status: `CURRENT_CONTROL_PACKET`

This document records the current chat-window naming and routing settings after
the user corrected the workflow naming rule:

- the prefix is the group;
- the number is the flow order inside that group;
- the suffix is the function.

This is docs/control only. It does not dispatch work, edit implementation
files, approve canon, approve registry, generate media, call providers, resume
04/05/06/07/08, resume Phase 20 / Phase 21, or claim production lock.

## Current Live Window Titles

Top control:

- `00 總控決策`: `019f0dd1-1543-7f22-b646-5c12d07d168d`

Group A planning / thinking:

- `A組01 整合規劃`: `019f2227-aeea-7870-9418-c7f5d399a2b1`
- `A組02 發散提案`: `019f258f-f29d-7843-bd2d-725476bf346a`
- `A組03 風險審閱`: `019f2590-1ce1-7271-ad94-036b27662b52`
- `A組04 綜合分支`: `019f2590-461b-7502-afb0-f5f65d79fc45`
- `A組05 分支提示詞邊界`: `019f25cb-dce1-78c1-9c93-5acd3c48f7aa`
- `A組06 優化稽核`: `019f30fe-b469-7ed1-aeea-2cfd3d97ccc3`

Group B single-lane implementation:

- `B組01 派工`: `019f25e7-7b64-7c91-b552-7287ef206e7d`
- `B組02 實作`: `019f1882-d5c6-72a3-893c-6d43cd097835`
- `B組03 審核`: `019f1ad1-2926-70c3-b85c-267df6fe331e`

Archived obsolete standby windows:

- `90 封存 舊02B副線`: `019f2228-1aaf-7513-9457-7e7557fca40c`
- `91 封存 舊03B副線`: `019f2228-756a-7091-8294-885dc5ddaadd`

Archived windows are not active project roles and must not receive new work
unless the user explicitly reopens or replaces the operating model.

## Current Group Model

Group A is the planning/thinking group:

`00 -> A組01 -> A組02/A組03/A組04/A組06/optional A組 branch -> A組01 -> 00`

Group A may create bounded planning branches when `00` approves a
`SPAWN_REQUEST`. Planning branches return `BRANCH_REPORT`; they do not
implement code, edit project files, or dispatch work to Group B.

`A組06 優化稽核` is a planning-only optimization audit lane. It checks route
weight, duplicate layers, stalled loops, and drift risk, then reports to
`A組01`; it does not dispatch implementation work.

Group B is the implementation group and is single-lane only:

`approved work package -> B組01 -> B組02 -> B組01 -> B組03 -> B組01 -> 00/A組01`

`B組01 派工` is the only Group B dispatch authority. `B組02 實作` is the only
active worker. `B組03 審核` is the only active reviewer. No parallel B組02/B組03
windows are available under the current operating model.

## Authority Split

- `00 總控決策` owns user-facing product decisions and may approve route
  changes.
- `A組01 整合規劃` integrates A-group thinking, updates or proposes blueprint
  changes, and prepares implementation-package candidates for `00`.
- `A組01 整合規劃` must not assign implementation or review tasks to `B組02` or
  `B組03`.
- `B組01 派工` receives an accepted implementation package and turns it into
  exactly one bounded task for `B組02`.
- `B組01 派工` sends completed `B組02` work to `B組03` for review.
- `B組01 派工` reports review results back to `00` / `A組01`.
- `B組01 派工` must not rewrite the blueprint or run A-group ideation.

## Alignment Rules

- Use the group-number-function titles above in new handoff prompts and status
  reports.
- Treat old labels `01`, `02`, `03`, `02A`, `02B`, `03A`, and `03B` as
  historical aliases only.
- If an old document says global `01` is the implementation dispatcher, follow
  this document instead: `A組01` is A-group integration and `B組01` is B-group
  dispatch.
- Do not assign work to `90 封存 舊02B副線` or `91 封存 舊03B副線`.
- Do not start parallel implementation, parallel review, support audits, or
  standby verification unless the user explicitly changes the operating model.
- If a task appears suitable for parallelism, route it back to `00` for a human
  operating-mode decision instead of starting parallel work.

## Watchdog Rule

The whole-project watchdog checks all active project windows, but it must
preserve the split authority:

- route A-group planning outputs to `A組01`;
- route accepted B-group packages or B-group unhandled outputs to `B組01`;
- never route implementation work directly from `00` or `A組01` to `B組02` /
  `B組03`;
- never use archived windows.

## Boundaries

- No provider/API calls.
- No web/server/open ports.
- No direct SQLite writes.
- No schema/migration changes.
- No renderer/ffmpeg/TTS/media generation.
- No 04/05/06/07/08 restart.
- No Phase 20 / Phase 21 restart.
- No canon, registry, release, approval, final media approval, or production
  lock claims.
- No staging, commit, or push unless the user explicitly asks.
