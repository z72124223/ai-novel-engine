# MVP-7 Diverse Audit Sample Rerun Report

Date: 2026-06-20

Status: `DIVERSE_AUDIT_SAMPLE_RERUN_COMPLETE`

This milestone reran the existing read-only `audit-narrative-quality` command on additional non-Pilot sample families. It did not change Kotlin code, tests, audit rules, schema, Validator, `stateVersion`, `contextHash`, `resolve-turn`, `StateReducer`, or SQLite data.

## 1. Data Sources

The rerun used existing ignored `out/` artifacts:

- MVP-3 memory E2E samples.
- MVP-4 scene/thread E2E samples.
- MVP-4 deterministic soak samples.

These are acceptance and soak fixtures, not full author-led story arcs. They are useful for audit stability but should not be treated as broad prose-quality proof.

## 2. Audit Command

The installed distribution CLI was used after rebuilding it:

```powershell
gradlew.bat :app:installDist --no-daemon

$App = "$env:LOCALAPPDATA\local-interactive-fiction-engine-build\app\install\app\bin\app.bat"
& $App audit-narrative-quality --context <context> --result <result> --out <report>
```

Reports were written under ignored `out/`:

```text
out/mvp7_diverse_audit_rerun/
```

All generated reports parsed as UTF-8 JSON with:

```powershell
Get-Content -LiteralPath <report> -Encoding UTF8 -Raw | ConvertFrom-Json
```

Environment note: an initial app launch attempt failed because that PowerShell process did not have `JAVA_HOME` / `java` on `PATH`. The rerun succeeded after setting the same temporary JDK used by the Gradle verification. This was an environment setup issue, not an audit or runtime issue.

## 3. Samples Used

| Sample | Source | Context | Result |
| --- | --- | --- | --- |
| `memory_e2e_001` | MVP-3 memory E2E | `out/memory_e2e_context_1.json` | `out/memory_e2e_result_1.json` |
| `memory_e2e_002` | MVP-3 memory E2E | `out/memory_e2e_context_2.json` | `out/memory_e2e_result_2.json` |
| `memory_e2e_003` | MVP-3 memory E2E | `out/memory_e2e_context_3.json` | `out/memory_e2e_result_3.json` |
| `memory_e2e_004` | MVP-3 memory E2E | `out/memory_e2e_context_4.json` | `out/memory_e2e_result_4.json` |
| `mvp4_e2e_turn_001` | MVP-4 scene/thread E2E | `out/mvp4_e2e_turn_001_context.json` | `out/mvp4_e2e_turn_001_result.json` |
| `mvp4_e2e_turn_002` | MVP-4 scene/thread E2E | `out/mvp4_e2e_turn_002_context.json` | `out/mvp4_e2e_turn_002_result.json` |
| `mvp4_e2e_turn_003` | MVP-4 scene/thread E2E | `out/mvp4_e2e_turn_003_context.json` | `out/mvp4_e2e_turn_003_result.json` |
| `mvp4_e2e_turn_004` | MVP-4 scene/thread E2E | `out/mvp4_e2e_turn_004_context.json` | `out/mvp4_e2e_turn_004_result.json` |
| `mvp4_e2e_turn_005` | MVP-4 scene/thread E2E | `out/mvp4_e2e_turn_005_context.json` | `out/mvp4_e2e_turn_005_result.json` |
| `mvp4_e2e_turn_006` | MVP-4 scene/thread E2E | `out/mvp4_e2e_turn_006_context.json` | `out/mvp4_e2e_turn_006_result.json` |
| `mvp4_e2e_turn_007` | MVP-4 scene/thread E2E | `out/mvp4_e2e_turn_007_context.json` | `out/mvp4_e2e_turn_007_result.json` |
| `mvp4_soak_turn_001` | MVP-4 deterministic soak | `out/mvp4_soak_turn_001_context.json` | `out/mvp4_soak_turn_001_result.json` |
| `mvp4_soak_turn_002` | MVP-4 deterministic soak | `out/mvp4_soak_turn_002_context.json` | `out/mvp4_soak_turn_002_result.json` |
| `mvp4_soak_turn_003` | MVP-4 deterministic soak | `out/mvp4_soak_turn_003_context.json` | `out/mvp4_soak_turn_003_result.json` |
| `mvp4_soak_turn_004` | MVP-4 deterministic soak | `out/mvp4_soak_turn_004_context.json` | `out/mvp4_soak_turn_004_result.json` |
| `mvp4_soak_turn_005` | MVP-4 deterministic soak | `out/mvp4_soak_turn_005_context.json` | `out/mvp4_soak_turn_005_result.json` |

## 4. Per-Sample Audit Results

| Sample | Verdict | Score | Severity | Finding count | Rule IDs |
| --- | --- | ---: | --- | ---: | --- |
| `memory_e2e_001` | `PASS` | 100 | `INFO` | 0 | none |
| `memory_e2e_002` | `PASS` | 100 | `INFO` | 0 | none |
| `memory_e2e_003` | `PASS` | 100 | `INFO` | 0 | none |
| `memory_e2e_004` | `PASS` | 100 | `INFO` | 0 | none |
| `mvp4_e2e_turn_001` | `PASS` | 92 | `WARNING` | 1 | `CONTEXT_SETTING_TOO_THIN` |
| `mvp4_e2e_turn_002` | `PASS` | 92 | `WARNING` | 1 | `CONTEXT_SETTING_TOO_THIN` |
| `mvp4_e2e_turn_003` | `PASS` | 92 | `WARNING` | 1 | `CONTEXT_SETTING_TOO_THIN` |
| `mvp4_e2e_turn_004` | `PASS` | 92 | `WARNING` | 1 | `CONTEXT_SETTING_TOO_THIN` |
| `mvp4_e2e_turn_005` | `PASS_WITH_WARNINGS` | 84 | `WARNING` | 2 | `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_SETTING_TOO_THIN` |
| `mvp4_e2e_turn_006` | `PASS_WITH_WARNINGS` | 84 | `WARNING` | 2 | `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_SETTING_TOO_THIN` |
| `mvp4_e2e_turn_007` | `PASS_WITH_WARNINGS` | 84 | `WARNING` | 2 | `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_SETTING_TOO_THIN` |
| `mvp4_soak_turn_001` | `PASS` | 92 | `WARNING` | 1 | `CONTEXT_SETTING_TOO_THIN` |
| `mvp4_soak_turn_002` | `PASS` | 92 | `WARNING` | 1 | `CONTEXT_SETTING_TOO_THIN` |
| `mvp4_soak_turn_003` | `PASS` | 92 | `WARNING` | 1 | `CONTEXT_SETTING_TOO_THIN` |
| `mvp4_soak_turn_004` | `PASS` | 92 | `WARNING` | 1 | `CONTEXT_SETTING_TOO_THIN` |
| `mvp4_soak_turn_005` | `PASS_WITH_WARNINGS` | 84 | `WARNING` | 2 | `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_SETTING_TOO_THIN` |

Summary:

- `PASS`: 12
- `PASS_WITH_WARNINGS`: 4
- `NEEDS_REVIEW`: 0
- `FAIL`: 0
- Average score: 92
- Minimum score: 84
- Maximum score: 100

## 5. Warning Distribution

| Category | Count | Rule IDs |
| --- | ---: | --- |
| `context_quality` | 16 | `CONTEXT_SETTING_TOO_THIN`, `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE` |

| Rule ID | Count |
| --- | ---: |
| `CONTEXT_SETTING_TOO_THIN` | 12 |
| `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE` | 4 |

## 6. Useful Warnings

The audit remained stable and mostly quiet:

- MVP-3 memory E2E samples all passed with no findings.
- MVP-4 samples consistently surfaced `CONTEXT_SETTING_TOO_THIN`, which is plausible because these older acceptance fixtures do not carry rich setting-pack excerpts.
- Later MVP-4 E2E / soak samples surfaced missing scheduled world event pressure only after active threads were present.

These findings are useful as context-quality observations. They should remain advisory because the underlying samples were written for runtime acceptance, not final prose-quality review.

## 7. Noisy Or Questionable Warnings

No new confirmed false positives were found.

The main caveat is sample type:

- MVP-4 acceptance and soak fixtures are synthetic and intentionally compact.
- A `CONTEXT_SETTING_TOO_THIN` warning on these fixtures does not prove the story engine is failing; it only shows that the audit notices thin setting context.
- These samples should not be used as Candidate B gate thresholds.

## 8. Suspected Missed Issues

The rerun did not exercise several A.2 rules:

- No `AI_FEELING_EMOTION_TOLD_NOT_SHOWN`.
- No `NPC_TOOLMAN_CONVENIENT_COMPLIANCE`.
- No `CONSEQUENCE_COST_NOT_RECORDED`.
- No knowledge-boundary findings.

This is not evidence that those rules are unnecessary. It means the additional acceptance fixtures are not diverse enough to stress style, subtle NPC agency, or consequence semantics.

## 9. Candidate B Readiness

Do not enter Candidate B yet.

This rerun improves confidence that A.2 did not become noisy on older runtime fixtures, but it still does not prove gate readiness.

Before Candidate B discussion, the project still needs at least one of:

- a fresh non-Pilot story arc with real per-turn evidence,
- more author-led turns outside the shrine/post crisis pattern,
- a decision brief that separates advisory context-quality observations from any possible gate,
- explicit approval to design Candidate B.

## 10. Next Step Recommendation

Recommended next milestone:

1. Continue collecting real author-led story arcs when available.
2. Or define a new explicit MVP-7 Candidate A.3 / next-candidate brief if a small read-only improvement is selected.
3. Do not default into Candidate B.

## 11. Verdict

DIVERSE RERUN VERDICT: `PASS_WITH_LIMITATIONS`

The current read-only audit stayed stable across 16 additional non-Pilot artifacts. It did not become noisy, did not introduce new consequence false positives, and produced only context-quality warnings on synthetic runtime fixtures. The sample set is broader than Pilot-1 but still not enough to approve Candidate B.
