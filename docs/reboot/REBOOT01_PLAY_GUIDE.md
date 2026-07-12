# REBOOT-01 Local Play Guide

## What this build is

REBOOT-01 is a six-turn, resettable, non-Canon Chenxing Scenario. The local page is engine-backed: every submit creates an isolated SQLite sandbox and executes the real `prepare → validate → resolve` path through the selected turn.

The narrative result comes from a bounded deterministic Scenario adapter. It does not call an external model and is not free-form generation.

## Requirements

- Windows PowerShell
- JDK 17
- this Repository branch

## Launch

From the Repository root:

```powershell
$env:LOCALAPPDATA = Join-Path $env:TEMP 'ai-novel-engine-reboot01-localappdata'
.\gradlew.bat :app:run --no-daemon --args="serve-reboot01-web --port 8765"
```

Open `http://127.0.0.1:8765`. The server accepts literal loopback hosts only. Stop it with `Ctrl+C`.

## Play controls

1. The page creates a new isolated local Session automatically.
2. Enter an action or use the suggested action.
3. Press `送出`. The input enters the real Context and the engine validates and applies the bounded Scenario result.
4. World time, characters and relationship summary update after each turn.
5. Enable `開發模式` before submitting to inspect Context, Critic, DecisionTrace and State Delta.
6. `存檔` downloads replayable input history. `讀檔` rebuilds a fresh isolated SQLite Session and verifies it through the same engine path.

No SQLite path, API key, login, cloud account or provider is exposed to the browser.

## Run the engine slice directly

```powershell
$env:LOCALAPPDATA = Join-Path $env:TEMP 'ai-novel-engine-reboot01-localappdata'
.\gradlew.bat :app:run --no-daemon --args="run-reboot01-vertical-slice --db out/reboot01/manual/story.db --out-dir out/reboot01/manual/evidence --approval-grant samples/reboot01/irreversible_approval_grant.json"
```

Expected summary:

```text
turns: 6
state version: 6
world time: T+35m
offscreen DecisionTrace: 1
drama evaluations: 6
run evidence rows: 6
irreversible approvals: 1
```

The command also writes separate parse, validation, diff, impact-preview and sandbox-apply evidence for `samples/reboot01/chenxing_world_patch_candidate_v1.json`.

## Known limitations

- The engine-backed Web uses a bounded deterministic Scenario adapter, not a free-form model.
- There is no model gateway, local LLM adapter or paid API connection.
- Four unrelated World Source #09 manifest hash/byte-size tests remain unresolved.
- The image lane remains paused; this build does not generate or promote visual assets.
