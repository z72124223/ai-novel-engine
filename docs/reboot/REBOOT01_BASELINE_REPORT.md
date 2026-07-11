# REBOOT-01 Stage 0 Executable Baseline Report

Status: `STAGE_0_BASELINE_ESTABLISHED_WITH_KNOWN_WORLD_SOURCE_DRIFT`

Date: 2026-07-11

Branch: `reboot/playable-depth-v1`

Remote baseline: `c0bebd316d4e429abcfa6b0d1b1ff5f08ea9c1ac`

## Outcome

The existing Kotlin/JVM engine compiles and can execute a real Chenxing turn through the official CLI and SQLite transaction boundary. The baseline is usable for REBOOT-01.

The full suite is not green: exactly four existing World Source tests fail because one tracked raw source no longer matches the byte size and SHA-256 recorded in the formal manifest. No Runtime, Setting Pack, Validator, transaction, memory, knowledge, scene/thread, or Pilot regression was added by Stage 0.

## Clean execution boundary

- Worktree: `C:\Users\f7212\OneDrive\文件\ai-novel-engine-reboot01`
- Branch: `reboot/playable-depth-v1`
- Original dirty worktree: unchanged at `89e522f18a946426232f5d56009fea0262329b47`, with 123 status entries at REBOOT-01 start.
- The REBOOT worktree used a dedicated temporary Gradle output root. The first compile attempt hit a shared-output file lock; no shared build output was deleted.
- No pull, merge, rebase, reset, stash, clean, force push, Canon edit, image task, provider call, or API key was used.

## Compile and tests

### Compile

```powershell
$env:LOCALAPPDATA = Join-Path $env:TEMP 'ai-novel-engine-reboot01-localappdata'
.\gradlew.bat :app:compileKotlin :app:compileTestKotlin --no-daemon --console=plain
```

Result: `PASS` in 3m40s after isolating the build output.

Existing warnings remain in `StoryboardPipelineModels.kt` for `ExperimentalSerializationApi`; they did not fail compilation and were not changed in Stage 0.

### Targeted Runtime tests

The targeted set covered `EngineTest`, `Mvp4SceneThreadRuntimeTest`, `Mvp5SettingPackDependencyRuntimeTest`, `Pilot1StabilizationTest`, and the three World Source import/query classes.

Result: `113 tests, 109 passed, 4 failed`.

The four failures were:

- `WorldSourceArchiveImportCliTest.cliAppliesWorldSourceArchiveIntoSqlite`
- `WorldSourceArchiveImportServiceTest.validatesChenxingWorldSourceArchiveManifest`
- `WorldSourceArchiveImportServiceTest.importsChenxingWorldSourceArchiveIntoSqlite`
- `WorldSourceQueryCliTest.cliQueriesImportedOfficialWorldSourceChunks`

All four report `WORLD_SOURCE_HASH_MISMATCH` and/or `WORLD_SOURCE_BYTE_SIZE_MISMATCH` for the same source-integrity problem.

### Full suite

```powershell
$env:LOCALAPPDATA = Join-Path $env:TEMP 'ai-novel-engine-reboot01-localappdata'
.\gradlew.bat :app:test --no-daemon --console=plain
```

Result: `809 tests, 805 passed, 4 failed` in 5m05s.

Classification: `KNOWN_BASELINE_SOURCE_INTEGRITY_FAILURE`; not a REBOOT Runtime code failure.

## World Source query evidence

The formal import correctly failed without writing archive rows:

```text
verdict=FAIL
status=WORLD_SOURCE_ARCHIVE_SQL_IMPORT_BLOCKED
archiveRowsWritten=0
documentRowsWritten=0
chunkRowsWritten=0
findings=2
```

Observed mismatch:

| Field | Formal manifest | Tracked raw file |
|---|---:|---:|
| source | `chenxing-world-raw-09-aijiang-detail` | same |
| byte size | 3019 | 3051 |
| SHA-256 | `F12334CBAA85EF31AA0250DDEA7189561A34D271E3C230D776F3102B029125DD` | `425582828EBD99BB648539E71A1010F5F96F9B4B6D8D337B410FCE007BE3B54F` |

Stage 0 did not alter the formal manifest or raw source. To prove that `query-world-source` itself still works, the temporary baseline database used an audit-local manifest copy whose two observed fields were updated. That temporary import wrote 1 archive, 9 documents, and 255 chunks, after which a classified query for 愛醬 returned five hits.

This audit-local copy is evidence only. It is not Canon, not a formal source-authority decision, and not permission to silently accept the drift.

## Real Chenxing turn

The baseline database was created only under `out/reboot01/baseline/`.

```text
init-db
→ validate-setting-pack
→ preview-setting-import
→ apply-setting-pack
→ start-session
→ prepare-turn
→ resolve-turn
→ show-session
→ export-session-evidence
→ direct read-only JDBC query
```

| Field | Value |
|---|---|
| session | `session-71977c1a-c7cf-418b-a767-291e39fe9242` |
| branch | `branch-d754ff3e-2e96-4d22-99c5-7daa65e2be7a` |
| context | `ctx-cdd1a3be-fcaf-47ce-b02a-f3c6952c7715` |
| context hash | `5772d5e79128aceae742d6cb608caf24228f62f5c8c52da529967d9157501d8c` |
| result | `reboot01-baseline-turn-001-result` |
| starting state version | 0 |
| applied state version | 1 |
| next turn | 2 |
| elapsed world time | 3 minutes |
| context bytes | 11919 / 24576 |

The applied turn produced one `add_event` delta and one persisted event. `turn_logs.validation_status=applied`, `has_state_change=1`, and no pending context remained.

## Baseline behavior discovered for Stage 1

The first valid protocol result was rejected by the narrative quality gate because ordinary wording about a safe route matched `CONTEXT_TRANSITION_UNAVAILABLE`. The same result succeeded after removing transition-like wording. This is direct evidence that a quality heuristic can currently act as a hard execution blocker even when protocol, world legality, state version, context hash, character references, and state delta are valid.

The successful result still emitted the advisory `NPC_LIVELINESS_MISSING_ACTION` because only the player character had a `characterAction`. This advisory did not block the transaction.

Stage 1 should preserve the protocol and world-legality checks while making the classification boundary explicit:

- `PROTOCOL_HARD_FAIL`
- `WORLD_LEGALITY_HARD_FAIL`
- `IRREVERSIBLE_APPROVAL_REQUIRED`
- `QUALITY_WARNING`

## SQLite evidence

The read-only JDBC query observed:

- 1 project
- 2 characters and 2 character states
- 1 active session
- 1 applied turn log
- 1 state delta
- 1 event
- 1 World Source archive
- 9 World Source documents
- 255 World Source chunks

The persisted delta and event both use turn identity `session-71977c1a-c7cf-418b-a767-291e39fe9242-turn-1`. The session moved atomically to `stateVersion=1`, `nextTurnNumber=2`, and `worldTime=T+3m`.

## Local evidence paths

The following are generated under ignored `out/**` and are not formal Repository state:

- `out/reboot01/baseline/story.db`
- `out/reboot01/baseline/setting-pack-validation.json`
- `out/reboot01/baseline/setting-pack-preview.json`
- `out/reboot01/baseline/setting-pack-apply.json`
- `out/reboot01/baseline/world-source-import-original.json`
- `out/reboot01/baseline/world-source-manifest-observed.json`
- `out/reboot01/baseline/world-source-import-observed.json`
- `out/reboot01/baseline/world-source-query-aijiang.json`
- `out/reboot01/baseline/turn-001-context.json`
- `out/reboot01/baseline/turn-001-result.json`
- `out/reboot01/baseline/turn-001-narrative-quality.json`
- `out/reboot01/baseline/session-evidence.json`
- `out/reboot01/baseline/sqlite-evidence.jsh`

## Stage decision

`STAGE_0_PASS_WITH_KNOWN_UNRELATED_FAILURE`

The executable baseline is established. Stage 1 may proceed. The formal World Source mismatch remains explicit technical debt and must not be silently repaired inside REBOOT-01 without a source-authority decision.
