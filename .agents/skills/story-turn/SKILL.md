---
name: story-turn
description: Run exactly one complete safe manual-AI turn in this repo's local Kotlin/JVM interactive fiction engine. Use when Codex must operate a session through only official CLI commands: show-session, prepare-turn, read the generated context JSON, write a Traditional Chinese AiTurnResult JSON, resolve-turn through the Validator, show-session again, and report narrative/state/memory/knowledge/scene/thread/context budget.
---

# story-turn

Use this skill to execute one safe manual-AI turn. Do not use it to run a whole Pilot sequence unless the user explicitly asks for the next single turn.

## Guardrails

- Use only the official Gradle CLI commands in this repo.
- Do not write SQLite directly with SQL, `sqlite3`, JDBC snippets, scripts, Python, Kotlin scratch code, or DB tools.
- Do not add network, API, server, port, Docker, Rust, Android, web UI, embedding, vector database, or multi-agent behavior.
- Generate narrative and result fields in Traditional Chinese.
- Read only the generated context JSON as the story knowledge surface for the turn.
- Do not reveal `sceneContract.forbiddenReveals`, unrevealed secrets, or `author_only` setting content.
- Characters may act only from their own goals, emotions, memories, knowledge, relationships, visible items, techniques, world rules, current location, and available actions.
- Do not turn story thread `directorGoal` into character intent, motive, knowledge, or dialogue.
- If Validator rejects the result, edit only the result JSON and retry `resolve-turn`. Never bypass Validator.

## Required Inputs

Identify:

- DB path.
- `sessionId`.
- Player input for this turn.
- Context output path.
- Result JSON path.

If any required input is missing and cannot be inferred, ask for it.

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

3. Audit the generated context before reading it for story generation:

```powershell
.\gradlew.bat run --no-daemon --args="audit-context --file <contextPath> --out <auditPath>"
```

If audit fails, stop and report the audit violations. Do not raise the context budget and do not generate a result.

4. Read only `<contextPath>` for story generation.

5. Write `<resultPath>` using protocol version `2`. Copy identity fields exactly from context:

- `projectId`
- `turnId`
- `contextId`
- `sessionId`
- `turnNumber`
- `baseStateVersion`
- `contextHash`

Include a fresh `resultId`, `narrative`, `turnSummary`, `characterActions`, `stateDeltas`, and any legal memory, knowledge, scene, thread, transition, world-event, or time fields required by the context.

6. Ensure every `characterAction` has `characterFitScore >= 70` and references only context-visible IDs.

7. Produce at least one meaningful legal state or runtime change unless the context explicitly allows no state change. Include reasoned payloads for review.

8. Resolve the turn:

```powershell
.\gradlew.bat run --no-daemon --args="resolve-turn --db <db> --session <sessionId> --result <resultPath>"
```

If rejected, read errors, edit only `<resultPath>`, and retry this step.

9. Export read-only session evidence when doing Pilot work:

```powershell
.\gradlew.bat run --no-daemon --args="export-session-evidence --db <db> --session <sessionId> --out <evidencePath>"
```

10. Show the session again:

```powershell
.\gradlew.bat run --no-daemon --args="show-session --db <db> --session <sessionId>"
```

11. For Pilot work, append a concise row or section to `docs/PILOT1_TURN_LOG.md` without copying author-only secrets.

## Report

Report:

- narrative
- state deltas applied
- memory recalled or created
- knowledge or belief changes
- scene and thread changes
- setting entities included
- context bytes and setting bytes
- Validator retry count
- next turn number and state version
- confirmation that no direct SQLite writes were used
