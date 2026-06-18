# Scene Thread Runtime

MVP-4 adds scene instances, scene requirements, story threads, scheduled world events, location-aware transitions, and a world clock to the existing local single-process runtime.

The engine remains Kotlin/JVM CLI + SQLite + JSON. There is no server, port listener, web UI, Android UI, external AI API, Rust, Docker, embedding, vector database, or multi-agent runtime.

## Scene Definition And Instance

`scenes` remain reusable definitions. A session creates `scene_instances` so revisiting the same scene creates a new instance with a new `visit_number`.

Supported scene instance statuses:

- `ACTIVE`
- `READY_TO_CLOSE`
- `CLOSED`
- `FAILED`
- `ABORTED`

A session must have exactly one current scene instance in `ACTIVE` or `READY_TO_CLOSE`. `READY_TO_CLOSE` means the scene can end, not that the engine must force a transition.

## Requirements

`scene_requirements` define scene-local goals. `session_scene_requirement_states` stores per-session progress.

Requirement types:

- `REQUIRED`: blocks normal transition until completed.
- `OPTIONAL`: can add value but does not block closure.
- `FAILURE_GUARD`: can fail the scene when marked failed.
- `EXIT_GATE`: reserved for explicit exit conditions.

AI results may propose `sceneRequirementUpdates`, but completion or failure must cite event, fact, or state-delta evidence.

## Story Threads

`story_threads` define director-facing continuity lines. `session_thread_states` tracks progress per session.

Thread `director_goal` is never character-facing knowledge. It must not be copied into `characterActions.intent`, `visibleAction`, or `hiddenMotive`. Characters may only act from their own goals, emotions, memories, knowledge, location, items, and available actions.

Only active, unresolved threads are placed in context, capped by priority.

## Transitions

`location_connections` describe reachability and travel time. `scene_transitions` describe allowed scene-to-scene movement.

`prepare-turn` emits `availableTransitions`; `resolve-turn` only accepts a `transitionProposal` that references one of those transitions and matches the current `sceneLifecycle.sceneInstanceId`.

Normal transitions require:

- matching current scene instance
- reachable target scene location
- satisfied player intent and prerequisites
- completed required scene requirements
- no explicit player refusal

`WORLD_FORCED` transitions also require a pending world event evidence id and a transition marked `forcedTransitionAllowed`.

## World Clock And Events

`story_sessions` stores `world_time` and `elapsed_minutes`. `timeAdvanceProposal` must include minutes, source, and reason. Travel time is added in the same transaction as an accepted transition.

`scheduled_world_events` become visible in `pendingWorldEvents` only when due. AI may acknowledge a due event with `worldEventAcknowledgements`; successful resolve consumes it once. Rejected, stale, or duplicate turns must not consume events.

## Continuity

`continuityConstraints` includes current location, character locations, item locations, and recent closed scene summaries. The validator rejects actions by characters outside the current scene location and item usage outside actor reach.

Closed scenes are not re-expanded into full narrative history. Only short summaries are placed into future context.

## Transaction Boundary

AI/Codex writes only result JSON. It must not write SQLite directly.

`resolve-turn` validates protocol identity, state version, context hash, forbidden reveals, character knowledge boundaries, scene requirements, story thread updates, transition proposals, world events, time advance, and continuity before opening formal writes. Accepted writes happen in one transaction; a failure rolls back scene/thread/time/world-event changes together with regular state deltas.

## CLI

MVP-4 adds read-only inspection commands:

```powershell
./gradlew.bat run --args="show-scene --db data/story.db --session SESSION_ID"
./gradlew.bat run --args="show-threads --db data/story.db --session SESSION_ID"
./gradlew.bat run --args="preview-transitions --db data/story.db --session SESSION_ID --input \"我離開後殿。\""
./gradlew.bat run --args="show-world-events --db data/story.db --session SESSION_ID"
./gradlew.bat run --args="explain-scene-state --db data/story.db --session SESSION_ID"
```

These commands do not write story state.
