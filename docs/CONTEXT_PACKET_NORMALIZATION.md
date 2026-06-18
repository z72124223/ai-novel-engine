# Context Packet Normalization

Final Pilot turn context is the JSON payload read by the manual AI window. It must stay within `24576` actual UTF-8 bytes and must not carry diagnostics that are only useful to the engine.

## Final Allowlist

Final `ContextPacket` top-level fields must be in the versioned allowlist represented by `ContextPacketTopLevelAllowlist`. Unknown top-level keys fail `audit-context`.

The allowlist contains protocol identity, player input, project and scene summaries, scene contract, active character views, runtime states, world rules, facts, items, techniques, recent events, session/hash fields, memories, knowledge claims, scene/thread/world-event views, continuity constraints, relevant settings and compact budget summaries.

There is no final `otherMetadata` catch-all. Forensics may still report an `otherMetadata` diagnostic section, but it should be empty for finalized Pilot context.

## Diagnostics Sidecar

Detailed diagnostics belong only in sidecar reports:

- `explain-context-budget`
- `audit-context`
- prepare failure budget reports

These reports may include largest JSON paths, duplicate groups, retained/removed items, section bytes, metadata path breakdowns and dangling-reference checks. They are not written into the AI-facing context.

## Legacy Alias Removal

`retrievedMemories` is the canonical runtime memory surface. Finalized multi-turn context omits the old `characterMemories` alias so the same memory is not serialized twice.

`contextBudget.truncationReasons` and `settingContextBudget.reasons` are legacy diagnostic fields. Final context uses compact `truncationReasonCodes` / `reasonCodes`.

## Compact Records And References

Active character records keep only turn-relevant public profile data. Runtime states keep current location, mood, health, inventory, current goal, dominant emotion, emotion values, available/forbidden actions and compact belief or relationship constraints.

Facts, events, summaries and settings are compacted so the same prose is not repeated across multiple sections. Event payloads drop duplicated `summary` and debug data. Setting excerpts keep current requirements, costs, limitations and other legality data while trimming repeated public prose and long relation paths.

## Empty And Default Omission

The JSON serializer omits empty/default optional fields when Kotlin defaults can restore the value on read. Required protocol fields and compact budget summaries remain explicit.

## Hash And Byte Accounting

The order is strict:

1. Build the candidate context.
2. Apply deterministic fitting and normalization.
3. Serialize with `contextHash = ""`.
4. Compute SHA-256 over those UTF-8 bytes.
5. Converge `contextBudget.actualBytes` against the final serialized payload.
6. Save the pending context only after bytes and hash are stable.

`audit-context` recomputes the same hash and byte count from the final file and rejects mismatches.
