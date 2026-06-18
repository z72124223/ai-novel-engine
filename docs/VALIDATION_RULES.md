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
