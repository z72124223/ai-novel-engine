# Memory Owner Decision

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

This memo records the owner decision status for the remaining local memory files. It is a docs/control note only. It does not stage, approve, repair, delete, or promote either memory file.

## Decision Status

- Status: `NEEDS_OWNER_DECISION_FOR_MEMORY`
- Status: `NEEDS_PARSE_RESULT_RECONCILIATION`
- Current decision: keep both memory files untracked/local for now.
- This memo does not authorize staging either memory file.

## Memory Files

### `memory/project_directory_memory.json`

- Parser evidence: PowerShell `ConvertFrom-Json` OK.
- Parser evidence: Node `JSON.parse` OK.
- Current role: local directory support memory.
- Observed status: `ACTIVE_LOCAL_DIRECTORY_MEMORY`.
- Observed `updatedAt`: `2026-06-25`.
- Current disposition: keep untracked/local unless 01 explicitly approves a later memory staging decision.

### `memory/quarterfull_world_memory_capsule.json`

- Parser evidence: Node `JSON.parse` OK.
- Parser evidence: PowerShell `ConvertFrom-Json` FAIL in 03 canonical verification with `Invalid object passed in, ':' or '}' expected. (263): {`.
- Current role: local QuarterFull/world support memory with a parser discrepancy.
- Observed status from Node-readable content: `ACTIVE_LOCAL_WORLD_MEMORY`.
- Current disposition: keep untracked/local until parser discrepancy is reconciled through a separate 01 owner-approved repair path.

## Boundaries

The memory files and this memo are:

- not canon;
- not runtime state;
- not SQLite input or a SQLite write path;
- not asset approval;
- not a registered or approved reference;
- not a production lock;
- not provider, image, video, or audio readiness.

The memory files must not be used as app/runtime input, SQLite input, provider handoff input, media-output proof, approval evidence, canon evidence, or production-lock evidence.

## Reference Surface

Current scans found no `app/src` references to either memory file. Existing references are docs/control references only.

Known docs/control references include release grouping, tracked handoff, project directory registration, and QuarterFull alignment support notes. Those references describe support context and do not make the memory files product/runtime dependencies.

## Next Possible Owner Decisions

1. Keep both files local-only and optionally add a precise ignore rule later after separate 01 approval.
2. Repair `memory/quarterfull_world_memory_capsule.json` later only under an explicit 01 owner-approved repair path.
3. Do not stage either memory file until parser discrepancy is reconciled and 01 explicitly approves staging.

## Future Repair Acceptance Gates

If a future repair is authorized, the repaired memory file must pass all of these gates before any staging consideration:

- Node `JSON.parse` OK.
- PowerShell `ConvertFrom-Json` OK.
- Secret/API key scan clean.
- Old active workspace path scan clean, or hits classified only as relocation/support context.
- Forbidden wording scan clean: no canon, approval, registered reference, production-lock, or runtime-state claims.
- No `app/src` runtime dependency.
- No SQLite or runtime write path.

## Non-Approval Statement

This memo is not staging approval for memory files. It is not repair approval. It is not cleanup/delete approval. It is not canon approval, runtime approval, asset approval, provider readiness, media readiness, or production readiness.
