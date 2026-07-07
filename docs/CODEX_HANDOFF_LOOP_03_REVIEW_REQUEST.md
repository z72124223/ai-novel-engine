# CODEX_HANDOFF_LOOP_03_REVIEW_REQUEST

Status: `REVIEW_REQUEST_DRAFT_FOR_01`

Date: 2026-06-30

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

Control thread `01`: `019f0dd1-1543-7f22-b646-5c12d07d168d`

Worker thread `02`: `019f1882-d5c6-72a3-893c-6d43cd097835`

This packet is a draft review request for `01` decision support. It is not an actual `03` handoff, not review approval, not staging approval, and not a production decision.

`03` does not exist / is not contacted in this slice unless `01` or the user explicitly creates or sends to it later.

## Model Policy Note

Future task, review, planning, verification, or next-task decision turns should use `model=gpt-5.5` with `thinking=xhigh` when the tool supports model/thinking selection. This packet cannot independently force the enclosing worker session model; model choice must never override project boundaries, validators, stop conditions, or explicit `01` / user decisions.

## Exact Candidate Files For Review

- `docs/CODEX_HANDOFF_LOOP_03_REVIEW_REQUEST.md`
- `docs/HANDOFF.md`
- `docs/CODEX_CHAT_WINDOW_HANDOFF_PROTOCOL.md`
- `docs/CODEX_CHAT_WINDOW_HANDOFF_SMOKE_TEST.md`
- `docs/CODEX_DIRTY_WORKTREE_GROUPING_SNAPSHOT.md`
- `PLANS.md` top handoff-loop planning entries only

## What `03` Should Check

- Confirm `01` and `02` thread ids are consistent across the candidate files.
- Confirm the protocol requires explicit `NEXT_TASK` before worker execution.
- Confirm the loop is governed and has no unattended infinite loop.
- Confirm the protocol does not treat silence as approval.
- Confirm the first smoke-test evidence is recorded as reported back to `01`.
- Confirm model-selection policy says `gpt-5.5` with `thinking=xhigh` when supported.
- Confirm the grouping snapshot is read-only and decision-support only.
- Confirm `docs/HANDOFF.md` points to the protocol, smoke-test evidence, and model-selection policy without claiming canon, registry, release, production lock, staging approval, or review approval.

## What `03` Must Not Do Or Claim

- Do not edit files, run project commands, stage, commit, push, delete, move, or clean files.
- Do not make Kotlin behavior changes, schema changes, SQLite direct writes, provider/API calls, or web/server changes.
- Do not approve canon, registry, release, production lock, final media, staging, or production use.
- Do not create a new handoff loop, assign work to `02`, contact another worker, or run an unattended infinite loop.
- Do not claim this request is an actual `03` handoff or review approval.
- Do not broaden review beyond the exact candidate files unless `01` or the user explicitly assigns that later.

## Required `03` Reply Shape

If `01` or the user later creates or contacts `03`, `03` should reply with exactly one concise review report back to `01` and must not execute follow-up work.

Required shape:

```text
03 REVIEW REPORT
Status: PASS | NEEDS_REVISION | FAIL
Reviewed files:
- <candidate file>

Findings:
- <concise finding, or "No findings">

Boundary check:
- <confirm boundaries held, or name the violation>

Recommended 01 decision:
- <accept packet | request revision | stop>
```

This is exactly-one review-response guidance: one concise review report back to `01`, no parallel task execution, no silent approval, and no next-task assignment by `03`.

## Boundary Statement

This review request packet authorizes no staging, no commit, no push, no Kotlin behavior changes, no SQLite direct writes, no provider/API calls, no web/server, no canon, no registry, no release, no production lock, and no unattended infinite loop.

It also authorizes no creation of `03` and no message to `03`; any later `03` creation or review contact requires explicit `01` or user action.
