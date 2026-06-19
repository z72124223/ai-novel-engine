# MVP-6 Checkpoint Branching Runtime Spec

## 0. Summary

MVP-6 adds local checkpoint, branching timeline, non-destructive rewind, and replay evidence on top of the existing single-process Kotlin/JVM CLI runtime.

The purpose is not to improve narrative style or add new story features. The purpose is to let a real user safely explore alternate choices after a validated Pilot run:

```text
applied session turns
→ create checkpoint
→ fork a new branch from checkpoint
→ continue from the branch
→ keep original turns readable and unmodified
→ verify replay evidence
```

Pilot-1b unblocked this stage by proving legal transitions, scheduled world events, and precise setting context can work. MVP-6 must preserve all safety guarantees already established by MVP-1 through Pilot-1b.

## 1. Non-Goals And Hard Limits

MVP-6 must not add:

- Android, Web UI, HTTP server, port listener, network API, OpenAI API, Rust, Docker, embedding, vector database, or multi-agent runtime.
- Direct SQLite writes by Codex, AI, scripts, or manual SQL for formal story state.
- Automatic story generation, automatic branch choice, or AI-driven replay.
- Full-history context expansion.
- Destructive rewind that edits, deletes, or overwrites applied turn logs from the source timeline.
- A raised context hard limit. `contextBudget.maxBytes` remains `24576`.

Migration rule:

- Do not modify migrations `001` through `005`.
- MVP-6 may add exactly one additive migration: `006_checkpoint_branching_runtime.sql`.

## 2. Key Design Principle

The current runtime has several project-level mutable tables such as `character_states`, `facts`, `events`, `items`, `secrets`, and `task_queue`. Therefore MVP-6 must not pretend that multiple fully live branches can safely mutate the same project state at the same time.

MVP-6 defines branching like a local working tree:

- Applied source turns remain immutable evidence.
- A checkpoint stores a canonical snapshot of runtime state.
- Forking creates a new session and branch from that snapshot.
- Checking out or continuing a branch restores that branch snapshot into the project runtime inside a transaction.
- Only one branch per project is active for mutation at a time.
- Inactive branches remain readable through checkpoints, turn logs, and replay evidence.

This keeps the feature feasible without refactoring every runtime table into branch-scoped storage.

## 3. Required Concepts

### Timeline Branch

A named line of play derived from either:

- an initial session,
- a checkpoint inside an existing session,
- or another branch checkpoint.

Each branch has:

- stable `branchId`
- `projectId`
- optional `parentBranchId`
- optional `sourceCheckpointId`
- active session id
- status: `ACTIVE`, `INACTIVE`, `ARCHIVED`
- label and reason

### Checkpoint

A durable, canonical snapshot of story runtime state at a specific session state:

- `sessionId`
- `branchId`
- `turnNumber`
- `stateVersion`
- optional `sourceTurnLogId`
- `snapshotHash`
- `snapshotJson`
- kind: `AUTO_TURN`, `MANUAL`, `BRANCH_START`, `PRE_CHECKOUT`

The checkpoint is internal runtime evidence. It may include hidden DB state required to restore correctness, but any CLI output or exported report must redact author-only secret content.

### Non-Destructive Rewind

Rewind never edits the old session. It creates or checks out a branch from a checkpoint.

If the user wants to "go back to Turn 8", the engine creates a new branch session from the Turn 8 checkpoint. The original Turn 9 through Turn 12 logs remain in the original session.

### Replay Evidence

MVP-6 replay is a verification surface, not an AI storytelling surface.

It must prove:

- turn logs form a valid chain,
- each applied result matched its pending context identity,
- state versions incremented exactly once,
- checkpoint snapshot hashes match stored snapshot JSON,
- branch source checkpoints still exist,
- no applied turn was duplicated or silently moved.

## 4. Additive Data Model

Add `app/src/main/resources/migrations/006_checkpoint_branching_runtime.sql`.

### New Tables

`timeline_branches`

- `id`
- `project_id`
- `label`
- `status`
- `parent_branch_id`
- `source_checkpoint_id`
- `active_session_id`
- `created_from_session_id`
- `created_from_turn_number`
- `created_from_state_version`
- `reason`
- `metadata_json`
- `created_at`
- `updated_at`

`session_checkpoints`

- `id`
- `project_id`
- `session_id`
- `branch_id`
- `turn_number`
- `state_version`
- `source_turn_log_id`
- `kind`
- `label`
- `reason`
- `snapshot_hash`
- `snapshot_json`
- `created_at`

`timeline_events`

- `id`
- `project_id`
- `branch_id`
- `session_id`
- `event_type`
- `source_checkpoint_id`
- `details_json`
- `created_at`

`replay_audit_runs`

- `id`
- `project_id`
- `session_id`
- `branch_id`
- `status`
- `checked_turn_count`
- `report_json`
- `created_at`

### Additive Columns

Add nullable `branch_id` to:

- `story_sessions`
- `turn_logs`

Existing pre-MVP-6 sessions may have null `branch_id` until they are adopted into a default branch by an MVP-6 command.

## 5. Snapshot Shape

`session_checkpoints.snapshot_json` must be stable and hashable. It must include:

- `snapshotVersion`
- `projectId`
- `sessionId`
- `branchId`
- `turnNumber`
- `stateVersion`
- `worldTime`
- `elapsedMinutes`
- current project active scene id
- story session row
- character states for the project
- revealed secret statuses and revealed turn numbers
- facts
- events
- open tasks
- runtime character memories
- character knowledge
- scene instances for the session
- session scene requirement states
- session thread states
- scheduled world event statuses
- current item owner/location runtime fields
- enough metadata to restore the active branch deterministically

It must not include:

- full setting pack JSON unless already required by an imported canon table checksum
- Gradle paths, local JDK paths, user home paths, or temporary file paths
- sidecar diagnostics intended only for `audit-context` or `explain-context-budget`

## 6. CLI Commands

### create-checkpoint

```powershell
./gradlew.bat run --args="create-checkpoint --db data/story.db --session SESSION_ID --label before-choice"
```

Writes one checkpoint for the current active session state.

Requirements:

- session exists and is active or closed
- no pending context exists for the session
- snapshot hash is computed from canonical UTF-8 JSON
- command does not change `stateVersion` or `nextTurnNumber`

### list-checkpoints

```powershell
./gradlew.bat run --args="list-checkpoints --db data/story.db --session SESSION_ID"
```

Read-only. Shows checkpoint id, label, turn, state version, branch, kind, and created time.

### show-checkpoint

```powershell
./gradlew.bat run --args="show-checkpoint --db data/story.db --checkpoint CHECKPOINT_ID"
```

Read-only. Shows redacted snapshot metadata and hash. Author-only secret content must not be printed.

### fork-session

```powershell
./gradlew.bat run --args="fork-session --db data/story.db --checkpoint CHECKPOINT_ID --label alternate-choice"
```

Creates a new branch and a new active session from a checkpoint.

Requirements:

- source checkpoint exists
- source session and turn logs are not modified
- no pending context exists in the source session
- new session starts at `checkpoint.turnNumber + 1`
- new session starts with `stateVersion = checkpoint.stateVersion`
- project runtime state is restored from the checkpoint in one transaction
- previous active branch is marked `INACTIVE`
- new branch is marked `ACTIVE`
- the command emits new `branchId`, `sessionId`, `stateVersion`, and `nextTurnNumber`

### checkout-branch

```powershell
./gradlew.bat run --args="checkout-branch --db data/story.db --branch BRANCH_ID"
```

Restores the latest checkpoint for a branch into the project runtime.

Requirements:

- no pending context exists for the currently active branch session
- target branch has at least one checkpoint
- restore runs in one transaction
- command creates a `PRE_CHECKOUT` checkpoint for the previously active branch before switching
- source and target turn logs remain unmodified

### show-timeline

```powershell
./gradlew.bat run --args="show-timeline --db data/story.db --project PROJECT_ID"
```

Read-only. Shows branch tree, sessions, checkpoint counts, last state version, and active branch.

### verify-replay-chain

```powershell
./gradlew.bat run --args="verify-replay-chain --db data/story.db --session SESSION_ID --out out/replay_audit.json"
```

Read-only by default. It writes the audit JSON file only and must not modify SQLite unless the user explicitly passes `--record`.

Optional recorded audit:

```powershell
./gradlew.bat run --args="verify-replay-chain --db data/story.db --session SESSION_ID --out out/replay_audit.json --record"
```

With `--record`, the command may write one `replay_audit_runs` row, but it still must not modify story state, sessions, branches, checkpoints, turn logs, facts, events, memories, knowledge, scenes, or state deltas.

Checks:

- no duplicate applied turn numbers inside the session
- no duplicate result ids
- each applied turn has matching `contextId`, `sessionId`, `turnNumber`, `baseStateVersion`, and `contextHash`
- `baseStateVersion` chain increments by one per applied turn
- pending turns are excluded from replay
- source checkpoint exists for forked sessions

### export-timeline

```powershell
./gradlew.bat run --args="export-timeline --db data/story.db --project PROJECT_ID --out out/timeline.json"
```

Read-only. Exports redacted branch and checkpoint evidence.

## 7. Protocol Rules

MVP-6 does not require a new turn protocol version.

`ContextPacket` may add:

- `branchId`
- `checkpointSummary`

These fields are informational and must not replace `sessionId`, `contextId`, `baseStateVersion`, or `contextHash`.

`AiTurnResult` must not add branch mutation commands. The AI cannot create checkpoints, fork sessions, or checkout branches.

All branch operations are explicit CLI commands outside the AI result protocol.

## 8. Validation Rules

Branch-aware validation must enforce:

- `prepare-turn` only works on the active branch session.
- `resolve-turn` rejects a pending context if its session is no longer the active branch session.
- `resolve-turn` rejects stale contexts after branch checkout.
- `fork-session` rejects checkpoints from another project.
- `fork-session` rejects source sessions with pending contexts.
- `checkout-branch` rejects target branches with corrupt or missing latest checkpoint snapshots.
- `verify-replay-chain` must fail if a turn log has missing context/result identity fields.
- `show-checkpoint` and `export-timeline` must redact author-only secret contents.

Existing rules remain mandatory:

- Validator before state changes.
- Single SQLite transaction for formal writes.
- `stateVersion`, `contextHash`, and result idempotency.
- No forbidden reveal.
- No context over `24576` UTF-8 bytes.

## 9. Runtime Services

Add minimal services:

- `CheckpointService`
- `TimelineBranchService`
- `BranchRestoreService`
- `ReplayAuditService`

Responsibilities:

- `CheckpointService` builds canonical snapshots and hashes.
- `TimelineBranchService` creates branches, lists timelines, and tracks active branch status.
- `BranchRestoreService` restores checkpoint snapshots into current project runtime tables transactionally.
- `ReplayAuditService` checks turn log chain integrity without replaying AI.

Do not move existing Validator or StateReducer responsibilities into these services.

## 10. Safety And Redaction

Snapshot JSON may contain data needed for restoration. User-facing output must redact:

- unrevealed secret content
- author-only entity content
- full setting pack snapshots
- local absolute paths

Context packets generated after a branch fork must still exclude author-only secrets and full setting packs.

## 11. Tests

Add tests for:

1. `006_checkpoint_branching_runtime.sql` initializes without modifying migrations `001` through `005`.
2. `create-checkpoint` stores a stable snapshot and does not change `stateVersion`.
3. `list-checkpoints` and `show-checkpoint` are read-only and redact author-only secrets.
4. `fork-session` creates a new branch and session from a checkpoint without modifying source turn logs.
5. A context from the source branch cannot be resolved after checkout to another branch.
6. `checkout-branch` creates a `PRE_CHECKOUT` checkpoint before switching.
7. `resolve-turn` works normally on the forked branch after `prepare-turn`.
8. Branch result ids remain globally idempotent.
9. `verify-replay-chain` passes for a clean session.
10. `verify-replay-chain` fails on a copied/corrupt session fixture with broken state version chain.
11. `export-timeline` redacts hidden secret content.
12. Context hard max remains `24576` after branch fields are added.
13. Setting context remains bounded; branch metadata must not cause full history expansion.

## 12. MVP-6 E2E Acceptance

Use a fresh database:

`data/mvp6-branching-e2e.db`

Flow:

1. Initialize DB.
2. Apply a setting pack with at least one legal transition and one scheduled world event.
3. Start a session.
4. Resolve three turns.
5. Create a manual checkpoint after Turn 2.
6. Resolve Turn 3 on the original branch.
7. Fork a new branch from the Turn 2 checkpoint.
8. Confirm original session still has its Turn 3 log.
9. Prepare and resolve a different Turn 3 on the forked branch.
10. Verify the two branches now have different narratives/events after the fork point.
11. Verify both replay chains.
12. Export timeline evidence.

Required final assertions:

- original branch turn logs remain unchanged
- forked branch has its own session id and branch id
- active branch is explicit
- no pending contexts remain
- checkpoint hashes are stable
- hidden secrets remain unrevealed unless legally revealed
- no server, port, network API, Android, Rust, Docker, embedding, vector DB, or multi-agent runtime exists

## 13. Implementation Stages

### Stage 1: Spec And Baseline

- Confirm Pilot-1b pass evidence.
- Create this spec.
- Do not implement code yet.

### Stage 2: Migration And Models

- Add migration `006_checkpoint_branching_runtime.sql`.
- Add models for branches, checkpoints, replay audits, and redacted exports.

### Stage 3: Checkpoint Creation

- Implement snapshot builder and stable hash.
- Add `create-checkpoint`, `list-checkpoints`, and `show-checkpoint`.

### Stage 4: Fork And Checkout

- Implement branch creation and branch restore.
- Block pending contexts before branch switch.
- Ensure old turn logs are never deleted or edited.

### Stage 5: Replay Evidence

- Implement `verify-replay-chain` and `export-timeline`.
- Add redaction checks.

### Stage 6: E2E And Hardening

- Run full old test suite.
- Run MVP-6 E2E.
- Run `git diff --check`.

## 14. Final Report Format

When MVP-6 is implemented, report:

1. VERDICT: PASS / FAIL
2. Branch and commit
3. Java and Gradle versions
4. Test count, failures, errors
5. Migration status: confirm only `006` was added and `001` through `005` were not modified
6. New tables and CLI commands
7. Checkpoint creation result
8. Fork/session result
9. Checkout result
10. Replay audit result
11. E2E branch divergence summary
12. Context bytes after branch fields
13. Redaction and forbidden secret status
14. Files changed
15. Known blockers
16. Recommended next step
