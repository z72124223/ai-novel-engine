---
name: story-turn
description: Run one complete safe manual-AI turn in this local interactive fiction engine. Use when Codex needs to operate the repo's Kotlin/JVM CLI multi-turn story runtime by showing a session, preparing a context JSON, reading only that context, writing a compliant Traditional Chinese result JSON, resolving through the Validator, and reporting the resulting narrative/state.
---

# story-turn

Use this skill to execute exactly one safe manual-AI turn for the current repo's local interactive fiction engine.

## Guardrails

- Do not add network, API, server, port, Docker, Rust, Android, or web UI behavior.
- Do not write SQLite directly with scripts, SQL, Python, Java, Kotlin snippets, or DB tools.
- Only `resolve-turn` may mutate story state, and only after Validator accepts the result.
- If validation fails, edit only the result JSON and retry `resolve-turn`; never bypass validation or patch the database.
- Generate narrative and result fields in Traditional Chinese.
- Do not reveal `sceneContract.forbiddenReveals` or hidden secrets not explicitly allowed by the current context.
- Characters may act only from their own `currentGoal`, fear/desire/emotion, memory, known facts, relationships, available actions, and context-visible items/techniques.
- Do not invent author-only knowledge or put full history into the result.

## Required Inputs

Before starting, identify:

- DB path, usually `data/story.db` unless the user provides another path.
- `sessionId`.
- Player input for the turn.
- Output paths for context and result JSON, usually under ignored `out/`.

If any of these are missing and cannot be inferred from the user's request or repo state, ask for the missing value.

## Workflow

Run these steps in order.

1. Show the current session:

```powershell
.\gradlew.bat run --no-daemon --args="show-session --db <db> --session <sessionId>"
```

Record `nextTurnNumber` and `stateVersion`.

2. Prepare the turn:

```powershell
.\gradlew.bat run --no-daemon --args="prepare-turn --db <db> --session <sessionId> --input <playerInput> --out <contextPath>"
```

3. Read only the generated context JSON for story generation. Treat it as the full allowed knowledge surface for this turn.

4. Generate the result JSON at `<resultPath>` according to protocol version `2`. Include:

- `protocolVersion`
- `projectId`
- `turnId`
- `resultId`
- `contextId`
- `sessionId`
- `turnNumber`
- `baseStateVersion`
- `contextHash`
- `narrative`
- `turnSummary`
- `characterActions`
- `stateDeltas`
- `revealedFacts`
- `memoryEffects`
- `sceneProgressUpdates`
- `newTasks`
- `validationNotes`

Use exact identity fields from the context. Choose a fresh `resultId`.

5. Ensure every `characterAction` includes:

- `characterId`
- `intent`
- `visibleAction`
- `hiddenMotive`
- `triggeringConditions`
- `usedMemoryIds`
- `usedItemIds`
- `usedTechniqueIds`
- `characterFitScore`
- `storyProgressScore`

Set `characterFitScore >= 70`, and only reference memory/item/technique IDs present in the context.

6. Ensure at least one meaningful legal `stateDeltas` item unless the context explicitly allows no state change. Legal operations include:

- `update_character_state`
- `add_fact`
- `reveal_secret`
- `add_event`
- `upsert_task`
- `noop`

For each non-noop delta payload, include enough `old_value`, `new_value`, and `reason` detail for review.

7. Resolve the turn:

```powershell
.\gradlew.bat run --no-daemon --args="resolve-turn --db <db> --session <sessionId> --result <resultPath>"
```

If rejected, read the Validator errors, edit only `<resultPath>`, and retry this step.

8. Show the session again:

```powershell
.\gradlew.bat run --no-daemon --args="show-session --db <db> --session <sessionId>"
```

## Final Report

Report:

- narrative
- state deltas applied
- memory usage or memory effects
- secret state, especially unrevealed forbidden secrets
- next turn number
- state version
- any Validator retries
