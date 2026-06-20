# Pilot-2 Session Bootstrap

Date: 2026-06-20

Status: `BOOTSTRAPPED_NO_TURN_STARTED`

This document records the fresh Pilot-2 DB and session created for future real story-arc audit sample collection. No Pilot-2 Turn 1 was started. No `prepare-turn`, `resolve-turn`, or AiTurnResult generation was performed.

## 1. Bootstrap Summary

| Field | Value |
| --- | --- |
| DB path | `data/pilot2.db` |
| Project id | `pilot2` |
| Project name | `Pilot-2 青犁鎮藥市封倉` |
| Setting pack | `pack-pilot2-qingli-herb-warehouse@1` |
| Setting pack source file | `out/pilot2/bootstrap/pilot2_setting_pack.json` |
| Session id | `session-ea110b6c-7509-40e6-a786-99cf00d8fc66` |
| Branch id | `branch-8c1ff48b-830d-4876-a68b-b4fcd94e6efe` |
| Opening scene id | `scene-qingli-herb-warehouse-perimeter` |
| Next turn number | `1` |
| State version | `0` |
| Unrevealed secrets | `3` |

## 2. Commands Run

All writes used the existing local CLI. No direct SQL, `sqlite3`, ad hoc script mutation, `prepare-turn`, or `resolve-turn` was used.

Environment note:

```powershell
$env:JAVA_HOME = "C:\Users\f7212\AppData\Local\Temp\codex-temurin-jdk17\jdk-17.0.19+10"
```

The JDK path above is local environment evidence only. It is not written into Gradle, code, or runtime configuration.

### 2.1 Fresh DB Cleanup

Only the dedicated Pilot-2 DB files were removed before initialization:

```powershell
$Db = "data\pilot2.db"
Remove-Item -LiteralPath $Db, "$Db-shm", "$Db-wal" -Force -ErrorAction SilentlyContinue
```

### 2.2 Initialize DB

```powershell
.\gradlew.bat --no-daemon :app:run --args="init-db --db data\pilot2.db"
```

Observed output:

```text
Initialized SQLite database: data\pilot2.db
```

### 2.3 Validate Pilot-2 Setting Pack

```powershell
.\gradlew.bat --no-daemon :app:run --args="validate-setting-pack --db data\pilot2.db --file out\pilot2\bootstrap\pilot2_setting_pack.json --out out\pilot2\bootstrap\02_validate_setting_pack.json"
```

Observed output:

```text
valid=true
issueCount=0
```

### 2.4 Preview Setting Import

```powershell
.\gradlew.bat --no-daemon :app:run --args="preview-setting-import --db data\pilot2.db --file out\pilot2\bootstrap\pilot2_setting_pack.json --out out\pilot2\bootstrap\03_preview_setting_import.json"
```

Observed output:

```text
valid=true
issueCount=0
canonicalHash=cc2297f834c503c7290558385f86b6dcfb2a6e56b37ffd7d62f589b94547b1cd
diffEntries=34
impactPaths=0
impactedSessions=
```

### 2.5 Apply Setting Pack

```powershell
.\gradlew.bat --no-daemon :app:run --args="apply-setting-pack --db data\pilot2.db --file out\pilot2\bootstrap\pilot2_setting_pack.json --out out\pilot2\bootstrap\04_apply_setting_pack.json"
```

Observed output:

```text
applied=true
importId=setting-import-923a486c-0f5a-4272-b082-74e32c0302cc
pack=pack-pilot2-qingli-herb-warehouse@1
canonicalHash=cc2297f834c503c7290558385f86b6dcfb2a6e56b37ffd7d62f589b94547b1cd
diffEntries=34
impactPaths=0
```

### 2.6 Start Session

```powershell
.\gradlew.bat --no-daemon :app:run --args="start-session --db data\pilot2.db --project pilot2 --scene scene-qingli-herb-warehouse-perimeter"
```

Observed output:

```text
sessionId=session-ea110b6c-7509-40e6-a786-99cf00d8fc66
branchId=branch-8c1ff48b-830d-4876-a68b-b4fcd94e6efe
currentSceneId=scene-qingli-herb-warehouse-perimeter
nextTurnNumber=1
stateVersion=0
```

### 2.7 Read-Only Session Confirmation

```powershell
.\gradlew.bat --no-daemon :app:run --args="show-session --db data\pilot2.db --session session-ea110b6c-7509-40e6-a786-99cf00d8fc66"
```

Confirmed:

```text
sessionId=session-ea110b6c-7509-40e6-a786-99cf00d8fc66
branchId=branch-8c1ff48b-830d-4876-a68b-b4fcd94e6efe
nextTurnNumber=1
stateVersion=0
idleTurnCount=0
unrevealedSecrets=3
```

Some localized text in PowerShell output may display with console encoding artifacts. IDs, turn number, state version, and counts are stable.

## 3. Next Turn 1 Start Command

Do not run this until the user provides actual Pilot-2 Turn 1 player input.

Recommended first command after user input:

```powershell
.\gradlew.bat --no-daemon :app:run --args="show-session --db data\pilot2.db --session session-ea110b6c-7509-40e6-a786-99cf00d8fc66"
```

Then save the exact player input to:

```text
out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_001_player_input.txt
```

Only after that, future Pilot-2 Turn 1 should follow `docs/PILOT2_TURN_TEMPLATE.md`.

## 4. Future Output Locations

Recommended future turn directory:

```text
out/pilot2/pilot2-qingli-herb-warehouse/turns/
```

Recommended Turn 1 file names:

```text
turn_001_player_input.txt
turn_001_context_budget.json
turn_001_context.json
turn_001_context_audit.json
turn_001_result.json
turn_001_evidence.json
turn_001_narrative_audit.json
turn_001_human_notes.md
```

Session-level outputs:

```text
out/pilot2/pilot2-qingli-herb-warehouse/session_preflight.json
out/pilot2/pilot2-qingli-herb-warehouse/session_evidence_after_turn_012.json
out/pilot2/pilot2-qingli-herb-warehouse/pilot2_summary_notes.md
```

## 5. Ignored Artifacts Not For Git

These files and directories are generated local artifacts and must not be committed:

```text
data/pilot2.db
data/pilot2.db-shm
data/pilot2.db-wal
out/pilot2/bootstrap/
out/pilot2/pilot2-qingli-herb-warehouse/
```

Verified ignore rules:

```text
.gitignore:11:*.db    data/pilot2.db
.gitignore:10:out/** out/pilot2/bootstrap/pilot2_setting_pack.json
```

## 6. Stop Conditions

Stop and ask the user before continuing if:

- the next user message does not include actual Turn 1 player input;
- expected `nextTurnNumber` is not `1`;
- expected `stateVersion` is not `0`;
- `show-session` cannot read `session-ea110b6c-7509-40e6-a786-99cf00d8fc66`;
- any command would require `prepare-turn` before user input exists;
- any command would require `resolve-turn` or AiTurnResult generation;
- context preparation later exceeds `24576` UTF-8 bytes;
- `audit-context` fails later;
- future result generation would need new Kotlin code, schema, Validator, `stateVersion`, `contextHash`, `resolve-turn`, or `StateReducer` changes;
- the user asks for Candidate B, MVP-8, UI, server, API, Docker, Android, Rust, embedding, or vector DB work without a new explicit approval milestone.

## 7. What To Ask The User Next

Use this prompt:

```text
請提供 Pilot-2 Turn 1 玩家輸入。

DB：
data/pilot2.db

SESSION_ID：
session-ea110b6c-7509-40e6-a786-99cf00d8fc66

預期回合：
Turn 1

玩家輸入：
<你的完整行動與台詞>

本回合底線：
<不得替你做的重大決定>
```

## 8. Verdict

`PILOT2_SESSION_READY`

The fresh Pilot-2 DB and session are ready for a future human Turn 1. No story turn has been prepared or resolved.
