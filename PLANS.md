# Plan

## Goal

Execute the approved Pilot-2 Turn 14 Codex-authored probe through prepare/audit only. 01 provided the exact player input content; 02 must save it verbatim, run formal CLI context preparation and context audit, document the evidence, and stop before any result JSON, narrative, validation, or resolve step.

## Global Strategy

Treat this as a formal Pilot-2 prepare checkpoint, not a full turn resolution. Confirm the repo, tag, branch, and formal session state first. If the session is exactly `stateVersion=13`, `nextTurnNumber=14`, and `pendingContexts=0`, save the 01-provided player input, run `show-session`, `explain-context-budget`, `prepare-turn`, and `audit-context`, then write a tracked docs report. Do not generate AI result JSON or narrative decision content.

## Non-Goals

- No AI result JSON.
- No narrative or probe decision content authored by 02.
- No `validate-result`.
- No `resolve-turn`.
- No formal Pilot-2 DB state advance beyond the pending context created by `prepare-turn`.
- No direct SQLite writes.
- No schema or migration changes.
- No `stateVersion` or `contextHash` calculation changes.
- No `resolve-turn` orchestration or StateReducer changes.
- No `mvp-6` retagging.
- No MVP-8 or unapproved Candidate B follow-up scope.
- No server/API/Docker/Android/Rust/embedding/vector DB.
- No deleting `data/`, `out/`, or `backups/`.
- No committing ignored `out/` or `data/` artifacts.

## Scope

Tracked files allowed:

- `PLANS.md`
- `docs/PILOT2_TURN14_CODEX_PROBE_PREPARE_AUDIT_REPORT.md`

Ignored artifacts to create but not commit:

- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_014_player_input.txt`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_014_context_budget.json`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_014_context.json`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_014_context_audit.json`

Formal DB:

- `data/pilot2.db`

Session:

- `session-ea110b6c-7509-40e6-a786-99cf00d8fc66`

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Current HEAD before this milestone is `99e74c0 fix: narrow Chinese transition refusal matching`.
- Working tree was clean at milestone start.
- `mvp-6` was confirmed as `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- 03 reviewed the D repair commit `99e74c09077763726c7c8d9f453fd46dcc7d8741` with `PASS`, per 01.
- 01 supplied exact Turn 14 Codex-authored player input and instructed 02 not to rewrite it.
- Turn 14 input is non-human system-probe evidence and must not count as real human-player evidence.

## Assumptions

- Formal Pilot-2 state should be `stateVersion=13`, `nextTurnNumber=14`, `pendingContexts=0`.
- `data/pilot2.db` and the known session id remain the formal Pilot-2 target unless CLI inspection proves otherwise.
- `prepare-turn` will create a pending context without advancing `stateVersion` or `nextTurnNumber`.

## Open Questions

- 01 must later provide result JSON / narrative / decision content if this context is accepted.
- The next worker must decide how to use the existing Turn 14 pending context; do not rerun `prepare-turn` unless 01 explicitly instructs it.

## Implementation Steps

- [x] Step 1: Read required control docs, workflow docs, Turn template, post-Turn13 handoff, and repair report.
- [x] Step 2: Confirm branch, HEAD, clean working tree, and `mvp-6` tag.
- [x] Step 3: Update `PLANS.md` for Turn 14 Codex-authored probe prepare/audit only.
- [x] Step 4: Check formal DB exists, inspect session with `show-session`, and stop if state is not `13/14/0` or if Turn 14 pending context already exists.
- [x] Step 5: Save the exact 01-provided Turn 14 player input to the ignored turn file.
- [x] Step 6: Run `explain-context-budget`, `prepare-turn`, and `audit-context`, writing ignored artifacts only.
- [x] Step 7: Inspect context/audit artifacts for `contextHash`, budget, audit verdict, transition target, state, and pending context evidence.
- [x] Step 8: Write `docs/PILOT2_TURN14_CODEX_PROBE_PREPARE_AUDIT_REPORT.md`.
- [x] Step 9: Run `git diff --check` and `gradlew.bat clean test --no-daemon`.
- [x] Step 10: Commit `docs: prepare Pilot-2 Turn 14 Codex probe context` and push if tracked docs only are staged.
- [x] Step 11: Directly notify 03 read-only review thread.

## Verification

Required:

- `git status`
- `git diff --check`
- `gradlew.bat clean test --no-daemon`

Pilot prepare/audit commands:

- `show-session`
- `explain-context-budget`
- `prepare-turn`
- `audit-context`

Safety checks:

- Confirm no `turn_014_result.json` or retry result is created.
- Confirm no `validate-result` or `resolve-turn` is run.
- Confirm ignored `out/` and `data/` artifacts are not staged.
- Confirm `mvp-6` remains unchanged.

## Risks

- Session state may not match `stateVersion=13`, `nextTurnNumber=14`, `pendingContexts=0`; if so, stop before writing or preparing context.
- A prior Turn 14 pending context may exist; if so, stop and report to 01.
- Context budget or semantic floor may fail; preserve evidence and stop.
- Accidentally generating result or narrative content; explicitly prohibited.
- Accidentally staging ignored `out/` or `data/` artifacts.

## Drift Log

- 2026-06-21: Started Pilot-2 Turn 14 Codex-authored probe prepare/audit-only milestone after 03 PASS on `99e74c0`.
- 2026-06-21: Formal result generation and resolution remain deferred to 01-provided result content in a later milestone.
- 2026-06-21: Pre-prepare state was confirmed as `stateVersion=13`, `nextTurnNumber=14`, `pendingContexts=0`; after `prepare-turn`, context `ctx-e7f8fb1f-ae6f-4b80-bc5a-cf71b06f2afc` is pending with `contextHash=45f5f8114dc08edf267f631835d741cb72010f1605acf457c2d6139e18d1d284`.
- 2026-06-21: `audit-context` passed with `actualBytes=21081/24576`, `hashValid=true`, and the awning transition available without unmet prerequisites.
- 2026-06-21: `git diff --check` passed. `gradlew.bat clean test --no-daemon` initially lacked Java in PATH; Temurin JDK 17 was installed for local verification and the full Gradle test suite then passed.
- 2026-06-21: Commit, push, and direct 03 notification are part of this handoff; if any post-commit delivery step fails, 01 must be notified immediately.
