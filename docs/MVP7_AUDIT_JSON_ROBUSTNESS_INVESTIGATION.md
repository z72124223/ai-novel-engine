# MVP-7 Audit JSON Robustness Investigation

Date: 2026-06-20

Status: `INVESTIGATION_COMPLETE`

This investigation checked why five `audit-narrative-quality` output files appeared to fail strict JSON parsing during the Candidate A.2 real sample rerun. It did not change Kotlin code, tests, audit rules, schema, Validator, `stateVersion`, `contextHash`, `resolve-turn`, `StateReducer`, or SQLite data.

Follow-up: verification instructions were hardened in `VERIFY.md` so audit report JSON is validated with explicit UTF-8 on Windows PowerShell 5.1. The investigation conclusion remains unchanged: the reports were valid UTF-8 JSON, and the apparent failures came from the verification read path.

## 1. Outputs That Initially Appeared To Fail

The apparent failures were the five late Pilot-1 reports generated under ignored `out/`:

| Output | Default PowerShell parse | Explicit UTF-8 parse | Node `JSON.parse` |
| --- | --- | --- | --- |
| `out/mvp7_a2_real_sample_rerun/pilot1_turn008_audit_report.json` | FAIL | PASS | PASS |
| `out/mvp7_a2_real_sample_rerun/pilot1_turn009_audit_report.json` | FAIL | PASS | PASS |
| `out/mvp7_a2_real_sample_rerun/pilot1_turn010_audit_report.json` | FAIL | PASS | PASS |
| `out/mvp7_a2_real_sample_rerun/pilot1_turn011_audit_report.json` | FAIL | PASS | PASS |
| `out/mvp7_a2_real_sample_rerun/pilot1_turn012_audit_report.json` | FAIL | PASS | PASS |

Default PowerShell read command that reproduced the apparent failure:

```powershell
Get-Content -LiteralPath out\mvp7_a2_real_sample_rerun\pilot1_turn008_audit_report.json -Raw |
    ConvertFrom-Json
```

Passing PowerShell read command:

```powershell
Get-Content -LiteralPath out\mvp7_a2_real_sample_rerun\pilot1_turn008_audit_report.json -Encoding UTF8 -Raw |
    ConvertFrom-Json
```

## 2. Outputs That Parsed Successfully

The following representative outputs parsed successfully even with the same audit command output style:

| Output | Explicit UTF-8 parse | Node `JSON.parse` |
| --- | --- | --- |
| `out/mvp7_a2_real_sample_rerun/pilot1_turn004_audit_report.json` | PASS | PASS |
| `out/mvp7_a2_real_sample_rerun/pilot1_turn005_audit_report.json` | PASS | not rerun in Node check |
| `out/mvp7_a2_real_sample_rerun/pilot1_turn001_audit_report.json` | PASS | not rerun in Node check |
| `out/mvp7_a2_real_sample_rerun/pilot1b_turn001_audit_report.json` | PASS | not rerun in Node check |
| `out/mvp7_a2_real_sample_rerun/mvp6_turn001_audit_report.json` | PASS | not rerun in Node check |

The originally suspected outputs also parsed successfully once read as UTF-8.

## 3. Validation Tools Used

Used:

- PowerShell `ConvertFrom-Json` with default `Get-Content -Raw`.
- PowerShell `ConvertFrom-Json` with `Get-Content -Encoding UTF8 -Raw`.
- Node.js `JSON.parse` with `fs.readFileSync(path, "utf8")`.
- Raw byte inspection with `[System.IO.File]::ReadAllBytes`.
- Search for stdout markers inside report files.

Unavailable:

- `python`: not available in this environment.
- `jq`: not installed.

Environment observations:

```text
PowerShell: 5.1.19041.6456
[Console]::InputEncoding: big5
[Console]::OutputEncoding: utf-8
$OutputEncoding: us-ascii
```

## 4. Failure Error Messages

Default PowerShell parse failures had this shape:

```text
Invalid array passed in, ',' expected. (1818): {
```

For `pilot1_turn008`, a direct comparison showed:

```text
default length=2907 utf8 length=2902
default parse fail: Invalid array passed in, ',' expected. (1818): {
utf8 parse ok
```

The length difference is evidence that the default read path decoded the same file differently before JSON parsing.

## 5. Minimal Reproduction Steps

Reproduce the false failure:

```powershell
$Path = "out\mvp7_a2_real_sample_rerun\pilot1_turn008_audit_report.json"
Get-Content -LiteralPath $Path -Raw | ConvertFrom-Json
```

Validate the same file correctly:

```powershell
$Path = "out\mvp7_a2_real_sample_rerun\pilot1_turn008_audit_report.json"
Get-Content -LiteralPath $Path -Encoding UTF8 -Raw | ConvertFrom-Json
```

Node validation:

```powershell
node -e "JSON.parse(require('fs').readFileSync('out/mvp7_a2_real_sample_rerun/pilot1_turn008_audit_report.json','utf8')); console.log('ok')"
```

Fresh audit rerun:

```powershell
gradlew.bat :app:run --args="audit-narrative-quality --context out\pilot1\rerun\turns\turn_008_context.json --result out\pilot1\rerun\turns\turn_008_result.json --evidence out\pilot1\rerun\turns\turn_008_evidence.json --out out\mvp7_json_robustness\pilot1_turn008_rerun_report.json"
Get-Content -LiteralPath out\mvp7_json_robustness\pilot1_turn008_rerun_report.json -Encoding UTF8 -Raw | ConvertFrom-Json
```

Observed rerun result:

```text
RERUN_UTF8_PARSE_OK verdict=PASS_WITH_WARNINGS score=76 findings=3
```

## 6. Root Cause Judgment

Root cause category:

`Windows / PowerShell encoding handling`, not malformed audit report JSON.

The evidence points to this path:

1. The report files are UTF-8 JSON written by Java/Kotlin through `Files.writeString(path, JsonSupport.json.encodeToString(report))`.
2. PowerShell 5.1 default `Get-Content -Raw` did not read the UTF-8 file correctly in this environment.
3. Corrupted decoded text was then passed to `ConvertFrom-Json`, which produced a JSON parse error.
4. Explicit UTF-8 reading and Node UTF-8 reading both parsed the same report files successfully.

The suspected categories from the task request are resolved as:

| Category | Judgment |
| --- | --- |
| `evidence` / `suggested_fix` / finding text not escaped | Not supported by evidence. UTF-8 parse passes. |
| stdout mixed into JSON file | Not supported. No `verdict=`, Gradle task, or `BUILD SUCCESSFUL` markers were found in the report file. |
| report writer wrote logs or prompts | Not supported. File begins with `{` and parses as JSON under UTF-8. |
| input JSON itself invalid | Not supported. Inputs parse when read as UTF-8. |
| Windows path / newline / quote handling | Not supported as root cause. Paths were serialized and parsed under UTF-8. |
| encoding issue | Supported. Default PowerShell read path corrupts UTF-8 text before parsing. |

## 7. Impact On Audit Verdict / Score / Findings

The audit verdict, score, and finding extraction remain credible.

Reasons:

- The CLI command completed successfully.
- The report files parse under UTF-8.
- Node `JSON.parse` accepts the suspected files.
- The same report values match CLI stdout summaries.
- There is no evidence of stdout contamination or malformed escaping in the actual file bytes.

The risk is in human or script verification that uses PowerShell 5.1 default text decoding. Such scripts may falsely report invalid JSON.

## 8. Does This Need Kotlin Changes?

No Kotlin change is required based on current evidence.

The current output is valid UTF-8 JSON. A future fix should not change audit rules, severity, Validator behavior, `resolve-turn`, `StateReducer`, SQLite writes, or schema.

Optional future hardening could be limited to:

- Ensure docs and verification commands always use `-Encoding UTF8` on Windows PowerShell 5.1.
- Add a tiny read-only test or CLI smoke that parses the emitted audit report with the same `JsonSupport.json` used by the app.
- Consider writing files with explicit `StandardCharsets.UTF_8` for clarity, even though Java `Files.writeString` already uses UTF-8 by default in modern Java.

## 9. Suggested Next Minimal Fix Scope

Recommended next milestone:

`MVP-7 audit JSON verification docs hardening`

Scope:

- Update `VERIFY.md` to show `Get-Content -Encoding UTF8 -Raw | ConvertFrom-Json` for audit reports on Windows PowerShell 5.1.
- Optionally update `docs/MVP7_A2_REAL_SAMPLE_RERUN_REPORT.md` with a short note that the earlier parse failure was a PowerShell decoding false alarm.
- Do not change Kotlin code unless a future reproduction proves real malformed output.

If a code hardening milestone is later approved, keep it narrow:

- Add a regression test that writes an audit report and re-parses it with `JsonSupport.json`.
- Do not modify audit rules or runtime write paths.

## 10. Candidate B Recommendation

Do not enter Candidate B yet.

This investigation removes one suspected blocker by showing the reports are valid UTF-8 JSON, but Candidate B still needs:

- broader real story samples,
- explicit decision brief,
- separation of advisory context-quality observations from possible gates,
- no unresolved report verification ambiguity,
- proof that Validator, `stateVersion`, `contextHash`, transactions, and SQLite write boundaries remain intact.

## 11. Verdict

INVESTIGATION VERDICT: `PASS_WITH_ENCODING_CAUTION`

The originally suspected audit reports are valid JSON when read as UTF-8. The apparent parse failures were caused by PowerShell 5.1 default text decoding in a Big5-oriented console environment. The next safest step is documentation hardening for UTF-8 verification, not Kotlin code changes and not Candidate B.
