# REBOOT-01 Stage 6 — Minimal Local Playable Web Slice

Date: 2026-07-12

Status: `STAGE_6_PASS`

## Delivered behavior

The Stage 6 interface is a dependency-free Kotlin/JDK local HTTP wrapper around the committed six-turn evidence. It binds only to the loopback interface and serves one responsive Traditional Chinese page.

The page supports:

- story text and player input;
- six explicit submit steps;
- world time;
- character and relationship summaries;
- local save and load;
- developer-mode Context, Critic, DecisionTrace, and State Delta inspection.

The browser never receives a SQLite path or direct database connection. There is no login, deployment, cloud service, provider key, or external model call. Request bodies are bounded, session state stays in local memory, and save files are validated against the scenario format and seed.

## Honest product boundary

This first UI is a `LOCAL_VERIFIED_REPLAY`, not a free-form model gateway. It records the player's local text and advances through the six engine-verified outcomes from Stage 4. The page displays this limitation explicitly. It is useful for validating the playable presentation, state/evidence views, and save/load contract without pretending that an unavailable external model generated new branches.

## Run locally

```powershell
$env:LOCALAPPDATA = Join-Path $env:TEMP 'ai-novel-engine-reboot01-localappdata'
.\gradlew.bat :app:run --no-daemon --args="serve-reboot01-web --port 8765"
```

Open `http://127.0.0.1:8765`. Stop with `Ctrl+C`.

## Verification

`RebootLocalWebServerTest` starts a real ephemeral loopback server and verifies:

- home page and health endpoint;
- loopback-only binding;
- all six turn submissions;
- story, world time, character, relationship, and debug evidence responses;
- completed state at `T+35m`;
- save and load into a new local Session.

Stage 4–6 integration selection: 3/3 PASS. `git diff --check` passed before staging.

No Canon, image, external provider, or original dirty-worktree content was modified.
