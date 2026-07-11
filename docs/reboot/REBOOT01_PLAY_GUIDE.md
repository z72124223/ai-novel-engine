# REBOOT-01 Local Play Guide

## What this build is

REBOOT-01 contains a six-turn, resettable, non-Canon Chenxing slice. The local page is a verified replay interface: it records your text and advances through six outcomes already applied through the engine's validation and transaction path. It does not call an external model or create new branches.

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

Open:

```text
http://127.0.0.1:8765
```

The server accepts loopback connections only. Stop it with `Ctrl+C`.

## Play controls

1. The page starts a new local Session automatically.
2. Enter an action or use the suggested action.
3. Press `送出` to advance one verified turn.
4. World time, characters, and relationship summary update after each turn.
5. Enable `開發模式` before submitting to see Context, Critic, DecisionTrace, and State Delta evidence for that turn.
6. `存檔` downloads a small JSON save. `讀檔` restores it into a new local Session.

No SQLite path, API key, login, cloud account, or provider is exposed to the page.

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
drama evaluations: 5
run evidence rows: 6
irreversible approvals: 1
```

## Run the replay benchmark

```powershell
.\gradlew.bat :app:run --no-daemon --args="benchmark-reboot01-evolution --out-dir out/reboot01/benchmark --approval-grant samples/reboot01/irreversible_approval_grant.json"
```

The command succeeds only when the evolved rule meets the adoption gate. The current accepted comparison keeps the same normalized story-state fingerprint and reduces revision retries from one to zero.

## Known limitations

- The Web page is a fixed verified scenario, not free-form generation.
- There is no model gateway, local LLM adapter, or paid API connection.
- Player text is saved but does not create a new narrative branch in this build.
- Existing narrative-quality advisories remain visible evidence and need a future measured improvement pass.
- Four unrelated World Source #09 manifest hash/byte-size tests still fail; this does not affect the REBOOT slice but must be reconciled separately.
- The image lane remains paused. This build does not generate, accept, or promote visual assets.
