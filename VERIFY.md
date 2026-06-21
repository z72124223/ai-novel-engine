# Verification Guide

This file defines the required verification commands and failure triage order for stable project completion.

## 1. Baseline Commands

Run these before any commit:

```powershell
git status
git diff --stat
git diff --check
gradlew.bat clean test --no-daemon
```

If `JAVA_HOME` is not configured, a temporary JDK may be used from outside the repo. Do not copy a JDK into the repo and do not write an absolute JDK path into code, Gradle files, or docs.

### 1.1 Docs-Only Documentation / Control Exception

For an explicitly approved docs-only documentation/control milestone, 01 or the user may allow a lighter verification set when all of the following are true:

- the diff touches only docs / control files such as `PLANS.md`, `ROADMAP.md`, `VERIFY.md`, `DONE_CRITERIA.md`, or files under `docs/`;
- no Kotlin source, tests, schema, migrations, runtime protocol, Validator, StateReducer, CLI behavior, `stateVersion`, or `contextHash` files are changed;
- no formal Pilot or runtime command is run;
- no ignored `data/`, `out/`, backup, build, Gradle cache, or local machine artifact is staged;
- `git diff --check` passes;
- `git status`, `git diff --stat`, and staged files are inspected;
- the final report explains why full Gradle was not run;
- 02 sends a verifier-window handoff to 03 for read-only review.

This exception is narrow. It does not apply to code, runtime, schema, CLI, packaging, Pilot lifecycle, Validator, StateReducer, setting-pack runtime, or bugfix milestones.

## 2. MVP-6 CLI E2E Acceptance

Use a fresh DB. Do not use the formal Pilot DB.

Suggested paths:

```powershell
$Db = "data\mvp6-branching-e2e.db"
$Out = "out\mvp6"
New-Item -ItemType Directory -Force -Path "data", $Out | Out-Null
Remove-Item -LiteralPath $Db -Force -ErrorAction SilentlyContinue
```

### 2.1 Initialize And Seed

```powershell
gradlew.bat :app:run --args="init-db --db $Db"
gradlew.bat :app:run --args="seed-demo --db $Db --seed samples/demo_project_seed.json"
```

Expected:

- DB exists.
- Migrations `001` through `006` are applied.
- Demo project exists.

### 2.2 Start Session And Resolve Two Turns

```powershell
gradlew.bat :app:run --args="start-session --db $Db --project demo --scene scene-shrine-questioning"
```

Record `SESSION_ID` and `BRANCH_ID` from output.

For Turn 1 and Turn 2:

```powershell
gradlew.bat :app:run --args="prepare-turn --db $Db --session SESSION_ID --input `"我先觀察沈霜的反應。`" --out $Out\turn1_context.json"
```

Create a valid local result JSON matching the emitted context, then run:

```powershell
gradlew.bat :app:run --args="resolve-turn --db $Db --session SESSION_ID --result $Out\turn1_result.json"
```

Expected:

- `stateVersion` advances exactly once per resolved turn.
- `pendingContextCount` returns to `0`.
- No forbidden reveal.

### 2.3 Create Checkpoint

```powershell
gradlew.bat :app:run --args="create-checkpoint --db $Db --session SESSION_ID --label before-branch"
gradlew.bat :app:run --args="list-checkpoints --db $Db --session SESSION_ID"
```

Expected:

- A checkpoint id is printed.
- Session `stateVersion` and `nextTurnNumber` do not change.
- Checkpoint has stable `snapshotHash`.

### 2.4 Resolve Original Branch Turn

Prepare and resolve one more turn on the original session.

Expected:

- Original branch keeps its Turn 3 log.
- Checkpoint remains unchanged.

### 2.5 Fork Session

```powershell
gradlew.bat :app:run --args="fork-session --db $Db --checkpoint CHECKPOINT_ID --label alternate-choice"
```

Expected:

- New `branchId` and new `sessionId` are printed.
- Forked session starts at `checkpoint.turnNumber + 1`.
- Forked session `stateVersion` equals checkpoint `stateVersion`.
- Previous branch becomes inactive.
- Source turn logs remain readable and unchanged.

### 2.6 Resolve Fork Branch Turn

Prepare and resolve a different turn on the forked session.

Expected:

- Fork branch accepts mutation.
- Inactive source branch rejects new `prepare-turn` or `resolve-turn`.
- Fork branch has distinct result id/narrative/event evidence after fork point.

### 2.7 Checkout Branch

```powershell
gradlew.bat :app:run --args="checkout-branch --db $Db --branch BRANCH_ID"
```

Expected:

- Target branch becomes `ACTIVE`.
- Previously active branch becomes `INACTIVE`.
- A `PRE_CHECKOUT` checkpoint is created for the previously active branch.
- No source turn log is deleted or rewritten.

### 2.8 Show Timeline, Replay Audit, Export

```powershell
gradlew.bat :app:run --args="show-timeline --db $Db --project demo"
gradlew.bat :app:run --args="verify-replay-chain --db $Db --session SESSION_ID --out $Out\replay_audit_original.json"
gradlew.bat :app:run --args="verify-replay-chain --db $Db --session FORK_SESSION_ID --out $Out\replay_audit_fork.json"
gradlew.bat :app:run --args="export-timeline --db $Db --project demo --out $Out\timeline_export.json"
```

Expected:

- Replay audit status is `PASS` for clean sessions.
- Timeline export redacts unrevealed and author-only secret content.
- No pending contexts remain.

## 3. Future Verification Types

| Future Area | Verification Requirement |
| --- | --- |
| MVP-7 | Must add a focused `docs/MVP7_*.md` spec first, then define matching unit tests and CLI E2E before implementation. Candidate A additionally requires the read-only narrative quality audit smoke below. |
| Pilot | Must use fresh Pilot DB, `pilot-preflight`, `audit-context`, `export-session-evidence`, and a turn log/findings document. |
| UI | Not currently approved. If approved later, must define non-server local execution or explicitly update roadmap constraints first. |
| Packaging | Must verify `gradlew.bat clean test --no-daemon` plus generated distribution launch, without reintroducing OneDrive reparse-file issues. |

### MVP-7 Candidate A Narrative Quality Audit

Candidate A and its A.1/A.2 calibration passes are read-only. They read existing JSON files and must not require `--db`.

```powershell
New-Item -ItemType Directory -Force -Path out | Out-Null
gradlew.bat :app:run --args="audit-narrative-quality --context samples/sample_context_packet.json --result samples/sample_ai_turn_result.json --out out/mvp7_narrative_quality_report.json"
```

Expected:

- Command exits `0`.
- Report JSON exists.
- Report includes `verdict`, `score`, `severity`, `findings`, and `evidence`.
- No SQLite path is required or written.
- The command does not call `resolve-turn`, `StateReducer`, or any repository-backed write path.

Audit reports are expected to be UTF-8 JSON. On Windows PowerShell 5.1, always read the report with explicit UTF-8 before piping into `ConvertFrom-Json`:

```powershell
Get-Content -LiteralPath out/mvp7_narrative_quality_report.json -Encoding UTF8 -Raw |
    ConvertFrom-Json |
    Out-Null
```

Do not use the bare PowerShell 5.1 form below for audit report validation, because it can decode UTF-8 files through the system default code page and produce a false parse failure:

```powershell
Get-Content -LiteralPath out/mvp7_narrative_quality_report.json -Raw |
    ConvertFrom-Json
```

Node.js is an acceptable alternate strict JSON check when available:

```powershell
node -e "const fs=require('fs'); JSON.parse(fs.readFileSync(process.argv[1], 'utf8')); console.log('JSON OK')" out/mvp7_narrative_quality_report.json
```

Candidate A.2 calibration additionally requires a distribution smoke and selected flawed-pack checks:

```powershell
gradlew.bat :app:installDist --no-daemon

gradlew.bat :app:run --args="audit-narrative-quality --context samples/sample_context_packet.json --result samples/sample_ai_turn_result.json --out out/mvp7_candidate_a2/repo_sample_report.json"

$App = "$env:LOCALAPPDATA\local-interactive-fiction-engine-build\app\install\app\bin\app.bat"
& $App audit-narrative-quality --context samples/sample_context_packet.json --result samples/sample_ai_turn_result.json --out out/mvp7_candidate_a2/install_sample_report.json
& $App audit-narrative-quality --context samples/mvp7_flawed_audit_pack/case_02_ai_overwritten_style/context.json --result samples/mvp7_flawed_audit_pack/case_02_ai_overwritten_style/result.json --out out/mvp7_candidate_a2/case_02_ai_overwritten_style_report.json
& $App audit-narrative-quality --context samples/mvp7_flawed_audit_pack/case_05_missing_consequence/context.json --result samples/mvp7_flawed_audit_pack/case_05_missing_consequence/result.json --out out/mvp7_candidate_a2/case_05_missing_consequence_report.json
& $App audit-narrative-quality --context samples/mvp7_flawed_audit_pack/case_08_false_positive_guard_crack/context.json --result samples/mvp7_flawed_audit_pack/case_08_false_positive_guard_crack/result.json --out out/mvp7_candidate_a2/case_08_false_positive_guard_crack_report.json
```

Expected A.2 signals:

- Repo and installDist sample audits exit `0`.
- Generated report files parse as UTF-8 JSON with the explicit PowerShell or Node check above.
- `case_02_ai_overwritten_style` includes `AI_FEELING_EMOTION_TOLD_NOT_SHOWN`.
- `case_05_missing_consequence` still includes `CONSEQUENCE_COST_NOT_RECORDED`.
- `case_08_false_positive_guard_crack` remains PASS or at minimum has no `CONSEQUENCE_COST_NOT_RECORDED` finding.
- Generated reports stay under ignored `out/`.

## 4. Failure Triage Order

Use this order. Do not skip directly to broad rewrites.

1. Read the failing command output.
2. Confirm branch and working tree with `git status`.
3. If tests fail, inspect the specific test XML under the Gradle test-results directory.
4. If CLI E2E fails, rerun the last successful read-only inspection command.
5. Check for pending context residue before retrying a turn command.
6. Check `stateVersion`, `contextHash`, and `resultId` mismatch before changing runtime code.
7. Check migration order and old migration diffs.
8. Check whether context hard max or semantic floor failed before editing result JSON.
9. Fix the smallest failing unit.
10. Re-run `git diff --check`, `gradlew.bat clean test --no-daemon`, and the relevant CLI E2E.

## 5. Stop And Handoff Conditions

Stop and write a handoff report if:

- a required command cannot run because of environment or permissions,
- tests cannot be made to pass in the current session,
- an E2E requires human-authored story input,
- a change would require new technology not approved by `ROADMAP.md`,
- a migration conflict would require modifying old migrations.

The handoff report must include:

- completed work,
- blocker,
- exact command/output summary,
- files changed,
- next prompt for another session.
