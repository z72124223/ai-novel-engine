# Codex Chat-Window Handoff Smoke Test

Status: `PASS_REPORTED_TO_01`

Date: 2026-06-30

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

Control thread `01`: `019f0dd1-1543-7f22-b646-5c12d07d168d`

Worker thread `02`: `019f1882-d5c6-72a3-893c-6d43cd097835`

This report records the first governed smoke test for `docs/CODEX_CHAT_WINDOW_HANDOFF_PROTOCOL.md`.

It is documentation only. It does not edit Kotlin code, schema, SQLite data, provider/API workflow, web/server behavior, staging, commits, pushes, canon, registry, release, production lock, or media handoff.

## 1. Smoke-Test Evidence

- `01` and `02` are distinct chat windows for this test because the assigned control thread id and worker thread id are different.
- `02` acknowledged readiness before receiving `NEXT_TASK`.
- `01` then issued `NEXT_TASK` for this smoke test.
- `02` worked only after receiving that `NEXT_TASK`.
- This confirms the governed handoff shape can be exercised without using silence or implied approval.

## 2. Governed Loop Boundary

This loop is governed, not unattended infinite automation.

Allowed loop shape:

```text
01 sends NEXT_TASK
  -> 02 executes exactly that slice
  -> 02 verifies the slice
  -> 02 sends completion report back to 01
  -> 01 decides NEXT_TASK, SEND_TO_03, ASK_USER, or STOP
```

Not allowed:

- unattended infinite loop;
- scheduler wakeup continuation;
- treating silence as approval;
- treating a missing `NEXT_TASK` as permission to continue;
- self-delegation where a worker reports to its own thread and treats the echo as approval.

## 3. Expected 02 Completion Report Shape

When this slice finishes, `02` reports to `01` with:

```text
01 handoff report

Canonical workspace: C:\Users\f7212\Projects\AI 劇本
Branch: codex/mvp6-checkpoint-branching-spec
Completed slice: governed chat-window handoff smoke test

Files changed:
- PLANS.md
- docs/CODEX_CHAT_WINDOW_HANDOFF_SMOKE_TEST.md

Verification:
- rg found 01 id, 02 id, NEXT_TASK, STOP/stop conditions, and no-unattended-loop wording.
- trailing-whitespace scan passed for touched files.
- git diff --check passed for touched files, with only Windows line-ending warnings if present.

Boundary check:
- no Kotlin code changes
- no SQLite direct writes
- no provider/API calls
- no web/server
- no staging, commit, or push
- no approval/canon/registry/release/production lock
- no 03 handoff
- no unattended infinite loop

Current result:
- PASS

Request to 01:
Please assign exactly one next smallest task, or respond STOP if no next task should start.
```

## 4. Stop Conditions

`02` must stop instead of continuing when any of these occurs:

- no `NEXT_TASK`;
- `STOP`;
- verification failure;
- need for `03` review;
- need for user decision;
- boundary ambiguity;
- worker/control self-loop;
- workspace path is not `C:\Users\f7212\Projects\AI 劇本`;
- requested work touches forbidden areas such as SQLite direct writes, provider/API, web/server, canon, registry, release, production lock, staging, commit, or push without explicit assignment.

## 5. Result

The smoke-test protocol is usable for one governed loop turn:

```text
READY -> NEXT_TASK -> docs-only execution -> verification -> completion report to 01 -> stop for 01 decision
```

This is not proof of unattended looping, and it must not be used as permission for unattended looping. It proves the safe version: one explicit task, one verified result, one report back to `01`, then stop.
