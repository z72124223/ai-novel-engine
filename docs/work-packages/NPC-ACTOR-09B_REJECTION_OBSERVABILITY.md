# NPC-ACTOR-09B — Safe Rejection Observability

## Outcome

When local formal NPC candidate preparation exhausts its safety attempts, the engine must produce a strict,
sanitized rejection evidence file while leaving no candidate and applying no formal turn.

## Allowed changes

- `PLANS.md`
- `PROJECT_STATE.md` and `.codex/project_controller.md` only for final status alignment
- NPC formal-turn models, preparation service, CLI wiring and directly related tests
- `schemas/npc_actor/npc_actor_formal_rejection_evidence_v1.schema.json`
- this work package and `docs/npc_actor/NPC_ACTOR09B_REJECTION_OBSERVABILITY_REPORT.md`

## Evidence contract

The rejection report may contain:

- formal request and Context identity fields;
- rejection stage and allowlisted rejection codes;
- safe repair instructions already produced by the engine;
- local invocation audit metadata and SHA-256 values;
- before and after-abort formal-state counters;
- explicit false boundaries for formal state writes, raw model output and engine-owned Context.

The rejection report must not contain:

- generated model responses, raw output or response JSON;
- prompt text, actor request, visible summary or player input;
- physical snapshot, archetype measurements or engine-owned geometry;
- author-only secrets, foreign character memory or private knowledge;
- Canon, Memory, Knowledge or state delta writes.

## CLI behavior

`prepare-local-npc-actor-formal-turn` keeps the existing candidate `--out`. On safety rejection it writes a
separate file from `--rejection-out`, or defaults to `<candidate filename>.rejection.json` in the same directory.
The command returns a non-zero status, prints only safe summary fields, and never creates the candidate file.

## Verification gates

1. Strict JSON round-trip and JSON Schema validation.
2. Two failed attempts are represented by two sanitized attempt records.
3. Pending Context is cleared before after-state evidence is captured.
4. Formal Session, turn, event, Memory, Knowledge and physical-state counters remain unchanged.
5. Serialized rejection evidence does not contain known secret, prompt or raw-output markers.
6. Existing successful candidate and human-decision paths still pass.
7. Focused, related and full tests plus JSON, secret, path and Git-scope checks pass.

## Stop conditions

Stop without weakening safety if the only way to create evidence would expose rejected model content, author
secrets or engine-owned measurements; bypass Repository APIs; or alter formal state beyond pending cleanup.
