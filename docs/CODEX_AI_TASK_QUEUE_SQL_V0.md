# AI Task Queue SQL V0

Marker: `AI_TASK_QUEUE_SQL_V0`

Status: `READY_EMPTY_QUEUE`

## Purpose

This creates a local SQLite task queue so the user, 00, or B組01 can place work
into `data/story.db`, and an AI window can claim the next task after completing
its current one.

The queue replaces fragile "chat memory only" task handoff with a small SQL
state machine.

## Tables

- `ai_task_queue`
- `ai_task_events`

Current main database status:

- Database: `data/story.db`
- Queue status output: `memory/ai_task_queue_status.json`
- Current count: `0`

No fake task was inserted.

## CLI Commands

Add or update a task:

```powershell
.\gradlew.bat :app:run --args="upsert-ai-task --db data/story.db --task <id> --title <text> --payload <json-file> --target B組02 --priority 10"
```

Claim next task:

```powershell
.\gradlew.bat :app:run --args="claim-ai-task --db data/story.db --worker B組02 --target B組02 --out memory/claimed_task.json"
```

Complete a task:

```powershell
.\gradlew.bat :app:run --args="complete-ai-task --db data/story.db --task <id> --worker B組02 --result PASS --summary <text> --report <path>"
```

List tasks:

```powershell
.\gradlew.bat :app:run --args="list-ai-tasks --db data/story.db --status READY"
```

## Expected Flow

1. User / 00 / B組01 inserts a `READY` task.
2. Worker calls `claim-ai-task`.
3. The task becomes `IN_PROGRESS`.
4. Worker completes it with `PASS`, `FAIL`, `NEEDS_REVISION`, or `BLOCKED`.
5. `PASS` turns the task into `DONE`.
6. Worker or dispatcher may call `claim-ai-task` again to get the next `READY`
   task.

## Boundaries

This is local SQLite + CLI only. It does not create a background daemon, does
not call providers/API, does not open web/server ports, does not run
renderer/ffmpeg/TTS, does not generate media, does not approve canon/registry/
release/final media/production lock, and does not stage, commit, or push.
