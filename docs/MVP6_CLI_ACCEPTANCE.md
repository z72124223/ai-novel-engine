# MVP-6 CLI Acceptance

This document verifies MVP-6 checkpoint, branch, checkout, replay audit, and timeline export through real local CLI commands.

## 1. Preconditions

- Run from the repo root.
- Use Windows PowerShell.
- Use JDK 17+.
- Do not use the formal Pilot DB.
- Do not directly edit SQLite.
- Do not use network, server, API, Android UI, Rust, Docker, embedding, vector DB, or multi-agent runtime.
- Keep `contextBudget.maxBytes` at `24576`.

Suggested environment:

```powershell
$Db = "data\mvp6-branching-e2e.db"
$Out = "out\mvp6"
New-Item -ItemType Directory -Force -Path "data", $Out | Out-Null
Remove-Item -LiteralPath $Db -Force -ErrorAction SilentlyContinue
```

## 2. Demo DB And Sample

Use:

- DB: `data\mvp6-branching-e2e.db`
- Seed: `samples\demo_project_seed.json`
- Project: `demo`
- Opening scene: `scene-shrine-questioning`

Initialize:

```powershell
gradlew.bat :app:run --args="init-db --db data\mvp6-branching-e2e.db"
gradlew.bat :app:run --args="seed-demo --db data\mvp6-branching-e2e.db --seed samples\demo_project_seed.json"
```

Expected:

- The DB file exists.
- Migrations through `006_checkpoint_branching_runtime.sql` are available.
- Project `demo` exists.

## 3. Start Session

```powershell
gradlew.bat :app:run --args="start-session --db data\mvp6-branching-e2e.db --project demo --scene scene-shrine-questioning"
```

Record:

- `SESSION_ID`
- `BRANCH_ID`
- `stateVersion`
- `nextTurnNumber`

Expected:

- Session starts at `stateVersion=0`.
- Session starts at `nextTurnNumber=1`.
- A branch id is printed.
- The started branch is the active writable branch.

## 4. Resolve Two Turns

For each turn:

```powershell
gradlew.bat :app:run --args="prepare-turn --db data\mvp6-branching-e2e.db --session SESSION_ID --input `"我先觀察，不靠近後殿。`" --out out\mvp6\turn1_context.json"
```

Create a matching local `AiTurnResult` JSON from the emitted context. The result must include:

- `projectId`
- `turnId`
- `contextId`
- `sessionId`
- `turnNumber`
- `baseStateVersion`
- `contextHash`
- unique `resultId`
- narrative
- at least one legal `add_event` state delta

Then resolve:

```powershell
gradlew.bat :app:run --args="resolve-turn --db data\mvp6-branching-e2e.db --session SESSION_ID --result out\mvp6\turn1_result.json"
```

Expected:

- `stateVersion` advances exactly once.
- `nextTurnNumber` advances exactly once.
- `pendingContextCount` returns to `0`.
- Validator passes.

Repeat once more for Turn 2.

## 5. create-checkpoint

```powershell
gradlew.bat :app:run --args="create-checkpoint --db data\mvp6-branching-e2e.db --session SESSION_ID --label before-branch --reason `"MVP-6 CLI acceptance fork point`""
```

Record:

- `CHECKPOINT_ID`
- checkpoint `branchId`
- checkpoint `turnNumber`
- checkpoint `stateVersion`
- `snapshotHash`

Expected:

- Checkpoint is created.
- Session `stateVersion` does not change.
- Session `nextTurnNumber` does not change.
- Snapshot hash is printed.

## 6. list-checkpoints And show-checkpoint

```powershell
gradlew.bat :app:run --args="list-checkpoints --db data\mvp6-branching-e2e.db --session SESSION_ID"
gradlew.bat :app:run --args="show-checkpoint --db data\mvp6-branching-e2e.db --checkpoint CHECKPOINT_ID"
```

Expected:

- The checkpoint appears in the list.
- `show-checkpoint` prints metadata and hash.
- Hidden or unrevealed secret content is not printed.

## 7. Resolve Original Branch Turn 3

Prepare and resolve Turn 3 on the original session.

Expected:

- Original branch now has a Turn 3 log.
- The checkpoint from Turn 2 remains unchanged.

## 8. fork-session

```powershell
gradlew.bat :app:run --args="fork-session --db data\mvp6-branching-e2e.db --checkpoint CHECKPOINT_ID --label alternate-choice --reason `"MVP-6 CLI acceptance alternate Turn 3`""
```

Record:

- `FORK_BRANCH_ID`
- `FORK_SESSION_ID`
- fork `stateVersion`
- fork `nextTurnNumber`

Expected:

- Fork creates a new branch and a new session.
- Fork session starts at checkpoint state.
- Fork session `nextTurnNumber` is `checkpoint.turnNumber + 1`.
- Original branch becomes inactive.
- Original session turn logs remain unchanged.

## 9. Resolve Fork Branch Turn 3

Prepare and resolve a different Turn 3 on `FORK_SESSION_ID`.

Expected:

- Fork session resolves successfully.
- Fork branch narrative/event evidence differs from original branch after the fork point.
- Source inactive branch does not accept new mutation.

## 10. checkout-branch

```powershell
gradlew.bat :app:run --args="checkout-branch --db data\mvp6-branching-e2e.db --branch BRANCH_ID"
```

Expected:

- Original branch becomes active.
- Fork branch becomes inactive.
- The previously active fork branch receives a `PRE_CHECKOUT` checkpoint.
- No source turn logs are deleted or rewritten.

## 11. show-timeline

```powershell
gradlew.bat :app:run --args="show-timeline --db data\mvp6-branching-e2e.db --project demo"
```

Expected:

- Timeline lists original and fork branches.
- Exactly one branch is active.
- Checkpoints are listed with turn and state version metadata.

Note: JSON export uses `activeBranchId` as the canonical active branch marker. Because the project JSON encoder omits default values, an active branch may omit `status` when the default value is `ACTIVE`; CLI `show-timeline` still prints `status=ACTIVE`.

## 12. verify-replay-chain

```powershell
gradlew.bat :app:run --args="verify-replay-chain --db data\mvp6-branching-e2e.db --session SESSION_ID --out out\mvp6\replay_original.json"
gradlew.bat :app:run --args="verify-replay-chain --db data\mvp6-branching-e2e.db --session FORK_SESSION_ID --out out\mvp6\replay_fork.json"
```

Expected:

- Both replay audits return `status=PASS`.
- Each audit checks applied turn logs in that session.
- No story state is changed by the read-only audit.

## 13. export-timeline

```powershell
gradlew.bat :app:run --args="export-timeline --db data\mvp6-branching-e2e.db --project demo --out out\mvp6\timeline_export.json"
```

Expected:

- Export file exists.
- Branch and checkpoint metadata are present.
- Snapshot JSON internals and hidden secret content are not exposed.

## 14. Final Assertions

The E2E passes only if:

- original turn logs remain unchanged after fork,
- fork session has a distinct session id and branch id,
- exactly one branch is active,
- no pending contexts remain,
- replay audit passes for original and fork sessions,
- timeline export redacts hidden secret content,
- no formal SQLite write happens outside CLI/runtime transactions,
- no server, port, network API, Android UI, Rust, Docker, embedding, vector DB, or multi-agent runtime is introduced.

## 15. Common Failure Causes

| Symptom | Likely Cause | First Check |
| --- | --- | --- |
| `stale_state` | Result JSON uses old `baseStateVersion` | Compare context and current `show-session` |
| `hash_mismatch` | Result JSON uses wrong `contextHash` | Recreate result from current context |
| `inactive_branch` | Preparing or resolving on a non-active branch | Run `show-timeline` |
| Duplicate result rejected | Reused `resultId` | Use a unique result id per turn |
| Replay audit fails | Broken turn identity or copied/corrupt log | Inspect replay audit JSON |
| Fork fails | Source session has pending context | Run `show-session` or abort pending context |
| Checkout fails | Target branch has no checkpoint | Run `list-checkpoints` |
| Export leaks content | Redaction bug | Stop and fix before commit |

## 16. Last Local Verification

Verified on: 2026-06-19

Command family exercised:

- `init-db`
- `seed-demo`
- `start-session`
- `prepare-turn`
- local JSON result creation
- `resolve-turn`
- `create-checkpoint`
- `list-checkpoints`
- `show-checkpoint`
- `fork-session`
- inactive source `prepare-turn` rejection
- fork branch `prepare-turn` / `resolve-turn`
- `show-timeline`
- `verify-replay-chain`
- `checkout-branch`
- `export-timeline`

Observed result:

```text
MVP6_E2E_PASS originalAudit=PASS originalTurns=3 forkAudit=PASS forkTurns=1 branches=2 checkpoints=4
```

Important observed IDs from the local run:

- original session: `session-970a2823-6793-4db5-baff-1256f2ad111d`
- original branch: `branch-665d0ffb-028d-4e60-81dd-a6ec22cb849f`
- fork session: `session-489d3a4e-b831-4aa6-9c1d-a8f8506bdc33`
- fork branch: `branch-a876892e-18f5-4e8b-a7ae-873a1840526d`
- fork checkpoint: `checkpoint-362421ef-21b0-4553-9aa3-520938ecb0c9`

This run used local files under `data\mvp6-branching-e2e.db` and `out\mvp6_*.json`. These are verification artifacts and must not be committed.
