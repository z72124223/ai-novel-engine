# Codex Chat-Window Handoff Protocol

Status: `CHAT_WINDOW_HANDOFF_PROTOCOL_V1`

Date: 2026-06-30

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

First smoke-test evidence: `docs/CODEX_CHAT_WINDOW_HANDOFF_SMOKE_TEST.md`

Control thread: `01`

Known Codex thread id for `01`: `019f0dd1-1543-7f22-b646-5c12d07d168d`

This protocol defines how a worker Codex chat window reports completed work to `01`, then waits for `01` to assign exactly one next task.

It is a chat-window coordination protocol only. It is not a scheduler, unattended automation, provider/API workflow, release approval, registry write, canon change, production lock, or permission to bypass validators.

## 1. Roles

| Role | Responsibility |
| --- | --- |
| `01` | Selects the next smallest milestone, resolves priority, and stops the loop when the project goal is complete or blocked. |
| Worker window | Executes one assigned slice, verifies it, updates `PLANS.md`, then reports completion to `01`. |
| `03` | Optional read-only reviewer when `01` requires review before accepting a slice. |
| User | Can override, stop, redirect, or approve any project boundary. |

The worker window must not invent the next milestone when `01` is available. It may suggest a next useful slice, but `01` chooses.

## 2. Model Selection Policy

For task assignment, implementation, verification, planning, review, or next-task decision turns, `01` and worker windows should use high-capability settings by default when the tool supports model and thinking selection:

```text
model=gpt-5.5
thinking=xhigh
```

`thinking=xhigh` is the highest supported reasoning setting in this environment. Do not use `thinking=max` unless a future tool schema explicitly supports it.

Lightweight status reads, thread title changes, and simple monitoring may use the current/default tool call without forcing high model settings.

If a task has already started at a lower setting, do not interrupt it only to change model. Apply this policy from the next `NEXT_TASK`.

Model choice never overrides project boundaries, validators, stop conditions, or explicit user / `01` decisions.

## 3. Loop Shape

```text
01 assigns one smallest verified slice
  -> worker executes only that slice
  -> worker verifies and updates PLANS.md
  -> worker reports completion to 01
  -> 01 either assigns one next slice, asks for 03 review, asks the user, or stops
```

The loop may repeat until the project ends. It must not run as an unattended infinite loop.

## 4. Completion Report To 01

When a worker finishes a slice, send `01` a report with this shape:

```text
01 handoff report

Canonical workspace: C:\Users\f7212\Projects\AI 劇本
Branch: codex/mvp6-checkpoint-branching-spec
Completed slice: <short name>
Files changed:
- <path>

Verification:
- <command/result>

Boundary check:
- no SQLite direct writes
- no provider/API calls
- no web/server
- no approval/canon/registry/release/production lock
- no unreviewed broadening beyond PLANS.md

Current result:
- <PASS / NEEDS_REVIEW / BLOCKED>

Request to 01:
Please assign exactly one next smallest task, or respond STOP if no next task should start.
Do not edit files or run commands in this reply unless the user explicitly asks 01 to execute work.
```

## 5. 01 Next-Task Reply Shape

`01` should reply with one of these decisions:

```text
NEXT_TASK:
<one small task>

ACCEPTANCE:
<what must be true when done>

VERIFY:
<minimum verification>

BOUNDARY:
<what must not be touched>
```

or:

```text
SEND_TO_03:
<review request>
```

or:

```text
ASK_USER:
<decision needed>
```

or:

```text
STOP:
<reason>
```

The worker may continue only from `NEXT_TASK`. Any other reply is a stop point for the worker.

## 6. Stop Conditions

Stop immediately and report instead of continuing if any of these occurs:

- The worker thread and `01` control thread are the same thread.
- `01` does not assign a concrete next task.
- `01` asks for user approval.
- `01` asks for `03` review and no reviewed result exists yet.
- Verification fails.
- The current `PLANS.md` strategy becomes invalid.
- The next task requires provider/API calls, external media generation, registry, canon, approval, release, production lock, schema migration, direct SQLite write, web server, or other forbidden boundary.
- The worktree has ambiguous unrelated changes in files required for the next task.
- The user says pause, stop, or redirect.

## 7. Codex App Thread Procedure

Before sending to `01`:

1. Confirm the `01` thread still exists.
2. Confirm the worker thread is not the same thread as `01`.
3. Confirm the completion report is decision-only.
4. Repeat the canonical workspace path in the message because older thread metadata may show OneDrive.
5. Ask for exactly one next task, not multiple parallel tasks.
6. Do not request that `01` edit files or run commands unless the user explicitly asks for that.

If the worker thread is already the `01` thread, do not send a thread message to itself. Record a local completion report, explain that cross-window handoff needs a distinct worker thread or a distinct control thread, and stop.

After sending to `01`:

1. The worker may read the `01` reply.
2. If `01` replies with `NEXT_TASK`, the worker can treat it as the next assignment.
3. If `01` replies with `SEND_TO_03`, `ASK_USER`, `STOP`, unclear text, or no reply, the worker stops and reports the state to the user.

## 8. Infinite-Loop Feasibility Check

Technically feasible:

- Codex can identify the `01` chat window by thread id.
- Codex can send a bounded completion report to that thread.
- Codex can read thread summaries later and continue from a clear `NEXT_TASK`.
- The cycle can repeat many times while `01` keeps issuing one verified next task at a time.

Required condition:

- `01` and the worker must be distinct chat windows. A self-delegation back into the same thread is not a valid closed loop.

Not allowed:

- A self-triggering infinite loop that advances without `01` or user decisions.
- A worker sending a report to itself and treating the echoed delegation as `01` approval.
- Scheduler wakeups that silently continue work.
- Treating silence as approval.
- Skipping `03` review when `01` requires review.
- Continuing after verification failure or boundary ambiguity.

Conclusion: the project can use a repeatable `worker -> 01 -> worker` loop, but not an unattended infinite loop. The safe version is an explicitly governed loop that terminates on `STOP`, blocker, failed verification, missing review, or project completion.
