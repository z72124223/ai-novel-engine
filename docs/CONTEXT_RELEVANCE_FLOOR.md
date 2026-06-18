# Context Relevance Floor

The relevance floor prevents a final `ContextPacket` from being valid JSON, under budget, and still too empty for a safe manual AI turn.

## Semantic Requirements

`TurnSemanticPlanner` deterministically derives requirements from the player input and current context. It does not use embeddings, APIs, network calls, or hard-coded session/entity exceptions.

Supported requirement types include:

- `EXPLICIT_MEMORY_CUE`
- `RELEVANT_BELIEF`
- `REFERENCED_ITEM`
- `REFERENCED_TECHNIQUE`
- `TRANSITION_CONSTRAINT`
- `LOCATION_CONSTRAINT`
- `CHARACTER_GOAL`
- `WORLD_RULE`
- `RECENT_EVENT_CONTINUITY`

Explicit recall cues such as "剛才", "先前", "記得", and "上次" require a true `retrievedMemories[].memoryId` when a legal candidate exists. Recent facts, events, and summaries may support continuity, but they cannot masquerade as recalled memory.

## Tiers

Tier 0 is safety and protocol: identity fields, player input, forbidden reveal representation, hard world rules, active scene/contract, active runtime character state, output schema, and continuity constraints.

Tier 1 is the semantic floor: the minimum compact records needed to satisfy must-satisfy requirements for this turn.

Tier 2 is continuity: recent accepted facts/events, relationship state, active story-thread public guidance, pending world events, and extra relevant memory/knowledge.

Tier 3 is optional enrichment: extra settings, extra memories, older summaries, provenance, and secondary public prose.

Trimming starts from Tier 3, then Tier 2. Tier 0 and the minimum records satisfying Tier 1 are protected.

## Quality Guard

`ContextQualityGuard` checks the final context before it is written or accepted by audit:

- memory requirements must be satisfied by `retrievedMemories`;
- knowledge requirements must be satisfied by visible `knowledgeClaims`;
- item/technique references must keep visible requirements, costs, limitations, prerequisites, or forbidden-use data when present;
- transition/location questions must keep scene and continuity constraints;
- author-only settings cannot satisfy character-facing requirements.

If the semantic floor cannot fit under `24576` UTF-8 bytes, `prepare-turn` fails with `CONTEXT_QUALITY_UNSATISFIABLE` before creating a pending context.

## Budget Targets

The hard max remains `24576` bytes. The soft target is `22528` bytes. The finalizer tries to reach the soft target after preserving semantic coverage, but it must not drop Tier 1 data to do so.

A context in the `22529..24576` range can pass only with `nearHardLimit=true` in the sidecar report. The report includes Tier 0..3 byte estimates and remaining bytes.

## Sidecar Reports

`semanticCoverage` appears in:

- `explain-context-budget`
- `audit-context`
- prepare failure budget reports

It does not appear inside the AI-facing `ContextPacket`.

The sidecar lists each requirement, candidate IDs, selected ID, selected context path, omitted alternatives, visibility check, quality status, and overall `qualityStatus`.

## Setting Count Is Not A Quota

Relevant settings are included only when the current turn references them or dependency relevance selects them. A setting count of zero is valid when there is no unsatisfied referenced-setting requirement. Loading unrelated settings just to avoid zero is a quality bug because it consumes budget that may be needed for memory, belief, or safety fields.
