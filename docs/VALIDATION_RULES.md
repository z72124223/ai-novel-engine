# Validation Rules

`AiTurnResultValidator` 在套用狀態前執行。

## Required Checks

- `result.projectId` 必須等於 context project id。
- `result.turnId` 必須等於 context turn id。
- `narrative` 不可空白。
- `update_character_state` 的 target character state 必須存在。
- `add_fact` 必須提供 `payload.content`。
- `add_event` 必須提供 `payload.summary`。
- `upsert_task` 建立新 task 時必須提供 `payload.title`。
- `reveal_secret` 的 target secret 必須存在，且必須列在 active scene contract 的 `allowedSecretIds`。

## Forbidden Reveal

validator 會從 SQLite 載入未揭露 secrets。

`narrative` 不得包含任何未揭露 secret 的完整 content，除非同一個 result 內存在合法的 `reveal_secret` operation 且該 secret 被 scene contract 允許。

active scene contract 的 `forbiddenReveals` 片語也不得出現在 narrative。

## MVP-2 Rules

- `protocolVersion` must be `2`.
- `contextId`、`sessionId`、`turnNumber`、`baseStateVersion`、`contextHash` must match the pending context.
- `resultId` is required and cannot have been applied before.
- `characterActions` are required; every action must use actors and memory/item/technique IDs present in the context.
- `characterFitScore` must be at least `70`.
- Forbidden reveals are scanned across narrative、turnSummary、revealedFacts、stateDeltas payloads、memoryEffects and newTasks.
- `resolve-turn` applies all writes in one SQLite transaction; any failed write rolls back state deltas, facts, events, turn log update, scene progress and session state.

## MVP-3 Rules

- Forbidden reveals are also scanned across `memoryCandidates` and `knowledgeUpdates`.
- `characterActions` may only reference memories in `retrievedMemories` owned by that actor, public facts or actor knowledge claims, and context-visible items/techniques.
- `memoryCandidates` must have a valid owner, valid source event/fact evidence, character value, and must not copy the narrative wholesale.
- Reframe candidates must set `supersedesMemoryId`; the old memory is retained.
- `knowledgeUpdates` cannot use hidden canonical facts as automatic character knowledge.
- If a requested update tries to jump a conflicted belief straight to `KNOWN` without strong evidence, the runtime keeps the conflict visible and transitions toward `DOUBTED` instead.
- Emotion deltas require `emotion`、`delta`、`trigger`、`sourceType/sourceId` and `reason`; normal single-turn absolute change is capped at 25.
- `prepare-turn` cannot write recall logs or increase recall counts. Successful `resolve-turn` records recall logs and increments each included memory once.

## MVP-4 Scene Thread Rules

- A session must have one current `ACTIVE` or `READY_TO_CLOSE` scene instance.
- `sceneRequirementUpdates` must reference requirements in context and cite event, fact, or state-delta evidence for completion/failure.
- Normal transitions require completed `REQUIRED` requirements, reachable locations, satisfied prerequisites, matching `fromSceneInstanceId`, and no explicit player refusal.
- `WORLD_FORCED` transitions require a pending world event evidence id and a transition that allows forced movement.
- Scene completion does not force player transition by itself.
- `threadProgressUpdates` must reference active threads in context, cite evidence, and keep progress deltas bounded.
- `story_threads.director_goal` must not become a character intent, visible action, hidden motive, or character knowledge.
- `worldEventAcknowledgements` must reference due `pendingWorldEvents`; rejected, stale, or duplicate results must not consume events.
- `timeAdvanceProposal` requires minutes, source and reason; accepted transition travel time is applied in the same transaction.
- Character actions must respect current scene location and item owner/location reachability.
- Forbidden reveals are scanned across MVP-4 result surfaces including requirement updates, thread updates, transition proposal, world event acknowledgements and time advance reason.

## MVP-5 Setting Pack Rules

- A setting pack must parse as UTF-8 JSON and declare schema version `1.0`.
- `packId`, `packVersion`, `projectId`, `name`, `mode` and `source.author` are required.
- `mode` must be `BASE`, `ADD_ON`, or `PATCH_CANON`.
- IDs must be stable ASCII identifiers and duplicate entity IDs within a type are rejected.
- Dependencies must already be imported.
- Existing canon can only change with a higher `entityVersion`.
- Author-only secrets must not enter character starting knowledge unless the secret explicitly lists the character in `knownBy`.
- Unknown entity references, incompatible relations, dependency cycles, hard world-rule conflicts, unbounded powerful techniques and item owner/location conflicts are rejected.
- `PATCH_CANON` updates against existing canon are blocked while active sessions exist.
- Rejected packs must not write import logs, entity revisions, dependency edges or core canon rows.
- Successful apply writes pack, import log, entity revisions, dependency edges and core canon rows in one SQLite transaction.

## Pilot-1 Stabilization Rules

- Final `ContextPacket` JSON must fit within `24576` actual UTF-8 bytes. The limit must not be raised.
- `contextBudget.actualBytes` must match the exact final file bytes produced by `prepare-turn`.
- `audit-context` must fail when bytes, declared bytes, hash, or mandatory fields do not match.
- `prepare-turn` must fail with `CONTEXT_BUDGET_UNSATISFIABLE` before saving a pending context if mandatory context cannot fit.
- Opening active setting-pack characters must define `initialRuntimeTemplate`.
- `initialRuntimeTemplate.initialLocationId` must exist and match the opening scene location for opening active characters.
- `initialRuntimeTemplate.initialGoal` and `dominantEmotion` must be non-empty.
- `emotionState` values and `initiativeLevel` must be in `0..100`.
- The template must not contain unrevealed secret content, author-only secret content, or a story-thread `directorGoal`.
- `start-session` creates missing opening `character_states` from the template in one transaction and rolls back on initialization failure.
- `pilot-preflight` and `export-session-evidence` are read-only commands.

## Pilot-1 Context Budget V2 Rules

- `explain-context-budget` must be read-only: it cannot create pending contexts, advance stateVersion, write recall logs, or apply state deltas.
- `prepare-turn` may produce a budget failure report, but failed fitting must still leave no pending turn.
- Compact mandatory views may remove duplicated prose and provenance, but must preserve protocol identity, scene contract, forbidden reveals, hard world rules, active character runtime state, continuity constraints, and item/technique legality data that remains in context.
- Trimming order must be deterministic and must not depend on session-specific or player-input-specific hard-coded exceptions.
- `settingContextBudget.maxBytes` remains a maximum, not a reserved quota; settings must yield to higher-priority safety and runtime sections when the global context budget is tight.
- `contextBudget.actualBytes` and `contextHash` must be computed from the final compacted payload.

## Pilot-1 Context Metadata Normalization Rules

- `audit-context` rejects unknown top-level fields instead of treating them as `otherMetadata`.
- Final `ContextPacket` must not contain diagnostics such as `perSectionBytesBefore`, `topLargestJsonPaths`, `duplicateContentGroups`, `removedItems` or `retainedItems`.
- Final `contextBudget` uses `truncationReasonCodes`; legacy `truncationReasons` and `settingContextBudget.reasons` are audit failures when present in a finalized context file.
- `contextHash` and `contextBudget.actualBytes` must match the normalized serialized payload, not a pre-normalization candidate.
- Removing legacy aliases must not weaken validation: result memory references still have to point at IDs present in canonical `retrievedMemories`, and item/technique references still have to be present in the final context.
