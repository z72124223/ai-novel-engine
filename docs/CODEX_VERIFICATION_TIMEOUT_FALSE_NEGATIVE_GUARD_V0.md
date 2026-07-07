# Verification Timeout False Negative Guard V0

Status: `VERIFICATION_TIMEOUT_FALSE_NEGATIVE_GUARD_V0_DRAFT_FOR_REVIEW`

Date: 2026-07-05

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

## Purpose

This docs/control rule set prevents an incomplete `rg` or batch verification
command from becoming a false negative. A timeout with partial output is not
missing evidence, PASS evidence, FAIL evidence, or NEEDS_REVISION evidence. It
is `INCONCLUSIVE_TIMEOUT` until completed fallback checks or an explicit
remaining verification gap resolves the ambiguity.

Selected process-rule outcome:
`VERIFICATION_TIMEOUT_FALSE_NEGATIVE_GUARD_PROCESS_RULE_PATCH_ALIGNED`

## Upstream Planning/Control Evidence

- A02 identified the verification-timeout false-negative gap.
- A03 returned `SAFE_WITH_GUARDS`.
- A04 synthesis was unavailable due thread delivery failure.
- A01 classified this as AI-resolvable docs/control process-rule work, not a
  true stop condition, and approved B01 dispatch.

## INCONCLUSIVE_TIMEOUT

`INCONCLUSIVE_TIMEOUT` means a verification command stopped before producing a
complete, trustworthy verdict. It is a temporary classification for incomplete
tool output. It must not be interpreted as:

- proof that a required string is missing;
- proof that a required string is present;
- PASS evidence;
- FAIL evidence;
- NEEDS_REVISION evidence;
- permission to ignore the remaining verification gap.

The owner must either run a precise fallback ladder or report the remaining
gap honestly.

## Timeout Is Inconclusive, Not Fail

`Timeout Is Inconclusive, Not Fail` is mandatory for `rg`, broad batch
verification, large dirty-worktree searches, and any command where partial
output can look like a complete result.

If a command times out:

- classify the timed-out command as `INCONCLUSIVE_TIMEOUT`;
- record the partial output status;
- do not claim absent evidence from the timed-out command;
- do not claim PASS from partial successful lines;
- do not claim FAIL or NEEDS_REVISION from missing lines that could have
  appeared after the timeout;
- move to the Fallback Precision Ladder.

## Fallback Precision Ladder

Use the `Fallback Precision Ladder` when broad verification is too large,
dirty, or timeout-prone:

```text
broad search -> touched files -> scoped control docs -> single file -> exact phrase -> stale/forbidden negative phrase check
```

Each fallback step must narrow scope and preserve the original verification
intent. The owner may stop the ladder only when completed output is enough to
answer the original verification question, or when a remaining verification
gap is explicitly named.

## Completed-Output Verdict Only

`Completed-Output Verdict Only` means PASS, FAIL, NEEDS_REVISION, or clean
verification language can rely only on completed command output, deterministic
file inspection, or explicitly acknowledged manual inspection.

Timed-out output can be cited as context, but only with `INCONCLUSIVE_TIMEOUT`
metadata. It is not verdict evidence by itself.

## Dirty-Worktree Scope Guard

`Dirty-Worktree Scope Guard` prevents unrelated modified or untracked files
from turning a broad search timeout into a false negative.

When the worktree is dirty, verification must state:

- whether the command searched the whole worktree or touched files only;
- whether unrelated dirty files could inflate command time;
- which touched docs/control files were checked;
- whether scoped fallback commands completed;
- whether `docs/HANDOFF.md` or another dirty file was excluded and why.

## Required Timeout Metadata

Any timeout report must include:

- original command;
- original scope;
- timeout duration;
- partial output status;
- fallback commands/scopes;
- remaining verification gaps.

If any field is unknown, say it is unknown. Do not fill it with inferred data.

## When Full Batch Rerun Is Still Required

A full batch rerun is still required when:

- the required proof is cross-file and cannot be answered by scoped fallback;
- fallback commands cover only positive matches but the task requires global
  absence of a forbidden phrase;
- the selected outcome marker count depends on all touched files and scoped
  counting was not completed;
- the timeout happened after file writes and no completed post-write check
  covers the modified files;
- B01 or B03 explicitly requests a full batch rerun.

If a full batch rerun is impossible within the current slice, report
`INCONCLUSIVE_TIMEOUT` and name the exact remaining verification gap.

## False-Negative Note

`False-Negative Note` is required in any completion report or review that
passes after fallback checks following a timeout. The note must state:

- which broad command timed out;
- why the timeout was not treated as missing evidence;
- which fallback commands completed;
- which exact phrases or files were confirmed;
- whether any verification gap remains.

If no timeout occurred, say no False-Negative Note was needed.

## Review And Dispatch Authority

- B01 remains the single-line dispatch controller for B02 and B03.
- B02 reports implementation and verification back to B01 only.
- B03 review authority is preserved; B02 must not contact B03 directly.
- A-group windows must not dispatch directly to B02 or B03.
- A timeout classification cannot replace B03 review, B01 dispatch, or user
  approval.

## Boundary Summary

- docs/control only
- no runtime/schema/provider/API/media work
- no SQLite writes
- no renderer/ffmpeg/TTS
- no web/server
- no sample JSON
- no actual query instances
- no evidence bundle
- no source-basis selection
- no approval, canon, registry, release, final approval, or production lock
- no 04/05/06/07/08 restart
- no Phase 20/21 restart
- no direct B02/B03 dispatch by A-group windows
- no staging, commit, push, archive/window creation
