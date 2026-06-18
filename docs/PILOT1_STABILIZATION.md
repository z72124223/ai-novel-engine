# Pilot-1 Stabilization

Pilot-1 stabilization resolves the blocking issues found in real-use validation while staying inside the local Kotlin/JVM CLI + SQLite + JSON runtime.

## Implemented Fixes

- Final `ContextPacket` JSON is hard-capped at `24576` actual UTF-8 bytes.
- `contextBudget.actualBytes` is computed from the final serialized payload.
- `contextHash` is computed after byte accounting converges.
- `prepare-turn` fails before saving a pending context if mandatory context cannot fit.
- Setting-pack opening characters require `initialRuntimeTemplate`.
- `start-session` creates missing opening `character_states` from that template in the same transaction.
- Added read-only CLI commands:
  - `audit-context`
  - `export-session-evidence`
  - `pilot-preflight`

## Runtime Boundary

No migrations were added. The runtime template is stored in existing JSON fields:

- `entity_revisions.snapshot_json` preserves the setting-pack entity.
- `characters.profile_json.initialRuntimeTemplate` provides runtime access.

Formal writes still go only through existing transaction commands:

- `apply-setting-pack`
- `start-session`
- `prepare-turn`
- `resolve-turn`

`audit-context`, `pilot-preflight`, and `export-session-evidence` are read-only with respect to SQLite.

## Pilot Rerun Rule

Old Pilot sessions must not be reused as formal evidence. A new Pilot rerun starts only after:

1. Setting pack validate/preview/apply succeeds.
2. `validate-project-canon` succeeds.
3. `start-session` succeeds.
4. `pilot-preflight` returns `valid=true`.

After reporting the new `SESSION_ID`, execution stops for the author to provide real player turns.
