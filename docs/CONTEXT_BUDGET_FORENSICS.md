# Context Budget Forensics

Pilot turn context is capped at `24576` actual UTF-8 bytes. The cap is intentionally small so manual AI turns stay inspectable, deterministic and safe. Do not raise it to hide budget bugs.

## Section Accounting

`explain-context-budget` reports context bytes before and after fitting. Sections are grouped by runtime purpose:

- `protocolIdentity`
- `playerInput`
- `sceneLifecycle`
- `sceneContract`
- `activeCharacters`
- `characterKnowledge`
- `retrievedMemories`
- `relevantSettings`
- `storyThreads`
- `recentTurnSummaries`
- `worldRules`
- `worldEvents`
- `continuityConstraints`
- `forbiddenReveals`
- `outputRequirements`
- `contextBudgetMetadata`
- `canonRevisionSummary`
- `otherMetadata`

The section totals are diagnostic accounting, not an alternate protocol. The final source of truth remains the serialized `ContextPacket` bytes and `contextHash`.

## Mandatory And Optional Bytes

Mandatory sections include protocol identity, player input, scene lifecycle, scene contract, active characters, hard world rules, continuity constraints, forbidden reveals, output requirements and budget metadata.

Optional sections include retrieved memories, character knowledge, relevant settings, recent summaries, secondary threads, world events, canon revision provenance and public prose that can be represented by compact references.

Mandatory does not mean verbose. A mandatory compact view may remove repeated prose and provenance if it preserves the fields needed for validation and safe roleplay.

## Duplicate Fragment Detection

Forensics scans string fragments from the final JSON, normalizes whitespace and groups duplicate fragments by SHA-256 hash. This catches repeated content such as:

- event `summary` repeated inside `payload.summary`
- scene IDs repeated across protocol fields
- setting summaries already represented in world rules, facts, items or techniques
- provenance IDs retained after the setting excerpt itself was removed

Duplicate detection is diagnostic; fitting still uses deterministic reducers.

## Compact Mandatory View

Budget V2 compacts character profiles when the same runtime information is already available in `characterStates.flags`.

Safe-to-remove examples:

- `profile.initialRuntimeTemplate` after `start-session` has created runtime `character_states`
- duplicated public descriptions
- verbose provenance that the AI does not need to act safely

Must remain available:

- current goal
- mood and dominant emotion
- emotion values and causal flags
- available actions
- inventory and item ownership
- scene contract and forbidden reveals
- hard world rules
- continuity constraints

## Dynamic Allocation

`settingContextBudget.maxBytes=8192` is a maximum, not a reserved quota. Relevant settings compete with memories, knowledge, recent summaries and threads for the global remaining context bytes.

The runtime first keeps safety and active runtime state, then trims optional material deterministically. Low-relevance setting excerpts must not cause high-priority memories to disappear before settings have yielded their share.

## Deterministic Trimming

Reducers run in stable order:

1. Compact canon revision provenance.
2. Compact duplicated character runtime templates.
3. Deduplicate setting summaries.
4. Omit lowest-priority setting excerpts.
5. Omit lowest-ranked memories.
6. Omit lowest-priority knowledge claims.
7. Omit oldest recent summaries.
8. Omit secondary story threads or world events.
9. Trim optional prose only after higher-priority reducers are exhausted.

Every fitting pass re-serializes the candidate and recomputes actual UTF-8 bytes.

## Failure Report

If fitting still cannot reach `24576` bytes, `prepare-turn` fails before saving a pending context and writes a budget failure report next to the requested output path, unless `--budget-report` is provided.

The report includes:

- `candidateBytesBeforeTrimming`
- `candidateBytesAfterTrimming`
- `bytesOverLimit`
- `mandatoryBytes`
- `optionalBytesRemaining`
- `perSectionBytesBefore`
- `perSectionBytesAfter`
- `topLargestJsonPaths`
- `duplicateContentGroups`
- `danglingReferenceCheck`
- `safetyFieldPresence`
- `unsatisfiedReason`

## Read-Only Explain

Use:

```powershell
./gradlew.bat run --args="explain-context-budget --db data/pilot1-rerun.db --session SESSION_ID --input-file out/turn_input.txt --out out/context_budget_explain.json"
```

This command is read-only. It does not create `turn_logs`, does not advance `stateVersion`, does not write recall logs and does not apply state deltas.

## Safety Invariants

- Do not raise `contextBudget.maxBytes`.
- Do not delete memories, settings, facts, secrets or turn logs to pass budget.
- Do not hard-code a specific session, entity ID or player sentence.
- Do not emit invalid JSON or arbitrary string truncation.
- Do not allow forbidden secret content into compact views.
- Always compute `contextHash` from the final compacted payload.
