# MVP-7 Candidate B Gate Spec

Date: 2026-07-08

Status: `APPROVED_FOR_IMPLEMENTATION`

User approval: integrate narrative quality checks into the formal `validate-result` / `resolve-turn` flow.

## 1. Goal

Run the existing read-only narrative quality audit as a pre-resolve gate so obviously unsafe or knowledge-boundary-breaking result JSON cannot become formal history without an explicit author override for advisory findings.

## 2. Non-Goals

- No schema or migration changes.
- No changes to `StateReducer` write semantics.
- No automatic narrative rewriting.
- No web server, API, UI, or external AI.
- `context_quality` observations do not block resolve.
- Subjective style / AI-feeling / consequence warnings do not hard-block by default.
- `audit-narrative-quality` remains available as a standalone read-only command.

## 3. Entry Points

| Command | Gate behavior |
| --- | --- |
| `validate-result` | Run protocol validation, then narrative gate. Exit `1` on protocol failure or blocking gate findings. Exit `0` with printed advisory warnings otherwise. |
| `resolve-turn` | Run protocol validation, then narrative gate. Reject before SQLite writes on blocking findings. Print remaining advisory findings on stdout. Reject on other advisory findings only when `--require-narrative-quality-acknowledgement` is present and `--acknowledge-narrative-quality-warnings` is absent. |

Optional flags:

- `--require-narrative-quality-acknowledgement`: advisory findings block resolve unless acknowledged.
- `--acknowledge-narrative-quality-warnings`: allow resolve when advisory findings remain under required-acknowledgement mode.
- `--strict-narrative-quality`: also treat all `MAJOR` non-context findings as blocking.
- `--narrative-quality-out <path>`: write the audit report JSON.

## 4. Blocking vs Advisory

Blocking findings:

- Any `CRITICAL` finding.
- `KNOWLEDGE_FORBIDDEN_REVEAL`
- `KNOWLEDGE_UNKNOWN_FACT_USED`
- `KNOWLEDGE_UNKNOWN_CLAIM_USED`
- `KNOWLEDGE_MEMORY_NOT_RETRIEVED`
- All `ai_feeling` findings, including AI-like prose and emotion telling.
- All `style` findings.
- `CONTEXT_SETTING_TOO_THIN` and `CONTEXT_TRANSITION_UNAVAILABLE` when the project has an applied setting pack.
- `prepare-turn` rejects thin setting context before a turn draft starts when a setting pack exists.
- Other `context_quality` observations remain advisory.
- In `--strict-narrative-quality` mode: any remaining `MAJOR` finding.

Hard-block categories cannot be bypassed with `--acknowledge-narrative-quality-warnings`. The author must revise and regenerate the result JSON.

Advisory findings:

- Remaining `WARNING` findings for NPC liveliness, consequence, and world autonomy unless strict mode applies.

## 5. Safety Boundaries

- Gate runs before `repository.resolvePendingTurn`.
- Failed gate checks must not write SQLite state.
- `contextHash`, `stateVersion`, Validator, and transaction boundaries remain unchanged.
- Gate failure messages use code prefix `narrative_quality_gate`.

## 6. Verification

- Unit tests for blocking vs advisory classification.
- `resolve-turn` rejects forbidden reveal narrative before state writes.
- `resolve-turn` rejects advisory findings without acknowledge flag.
- `resolve-turn` succeeds with acknowledge flag and no blocking findings.
- `validate-result` exits `0` for advisory-only findings.