# Project Handoff Index

This file is the first stop for a new Codex window. It records the current stable point, what is safe to touch, and what must not be started without explicit user approval.

## Stable Point

- Current stable integration branch: `codex/mvp6-checkpoint-branching-spec`
- Remote: `origin https://github.com/z72124223/ai-novel-engine.git`
- Current branch should track `origin/codex/mvp6-checkpoint-branching-spec`.
- Use `git status --short --branch` and `git log --oneline --decorate -10` for the latest mutable `HEAD`.
- Latest synchronized state before this handoff refresh: `a2e5a6f31f7d10483022e6e9e04a45c7cdeba63c`.
- MVP-6 release tag: `mvp-6`
- `mvp-6` target commit: `be527031335efe8e2b0facae0145e1bbc084f8c6`
- Main pushed branches:
  - `codex/mvp6-checkpoint-branching-spec`
  - `pilot/real-use-vertical-slice`
- Pushed tags:
  - `mvp-2`
  - `mvp-3`
  - `mvp-4`
  - `mvp-5`
  - `mvp-6`
  - `pilot-1-budget-v2`
  - `pilot-1-capability-contract`
  - `pilot-1-context-normalized`
  - `pilot-1-context-relevance`
  - `pilot-1-stabilized`
  - `pilot-1b-coverage-repair`

## Current Verified Baseline

Verified on 2026-06-20 during the automatic stable-completion loop:

- Branch and origin synchronized at `a2e5a6f31f7d10483022e6e9e04a45c7cdeba63c`.
- `mvp-6` still points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- `git diff --check` passed.
- `gradlew.bat clean test --no-daemon` passed with Temurin JDK 17.0.19.
- Test result: 233 tests, 0 failures, 0 errors, 0 skipped.
- `:app:installDist` was exercised during the diverse audit rerun milestone.
- `audit-narrative-quality` was run read-only on 16 additional non-Pilot samples; generated reports were ignored under `out/mvp7_diverse_audit_rerun/`.

## Read First

Read these before planning or editing:

1. `AGENTS.md`
2. `README.md`
3. `ROADMAP.md`
4. `PLANS.md`
5. `DONE_CRITERIA.md`
6. `VERIFY.md`
7. `docs/HANDOFF.md`
8. `docs/PROJECT_COMPLETION_AUDIT.md`
9. `docs/TURN_PROTOCOL.md`
10. `docs/VALIDATION_RULES.md`

For MVP-7 audit work, also read:

- `docs/MVP7_NARRATIVE_QUALITY_SPEC.md`
- `docs/MVP7_AUDIT_CALIBRATION_REPORT.md`
- `docs/MVP7_AUDIT_SAMPLE_COLLECTION_REPORT.md`
- `docs/MVP7_FLAWED_AUDIT_SAMPLE_PACK_REPORT.md`
- `docs/MVP7_CANDIDATE_A2_CALIBRATION_PLAN.md`
- `docs/MVP7_A2_REAL_SAMPLE_RERUN_REPORT.md`
- `docs/MVP7_AUDIT_JSON_ROBUSTNESS_INVESTIGATION.md`
- `docs/MVP7_DIVERSE_AUDIT_SAMPLE_RERUN_REPORT.md`

For Pilot or context-budget work, also read:

- `docs/PILOT1_FINDINGS.md`
- `docs/PILOT1B_COVERAGE_REPAIR.md`
- `docs/CONTEXT_RELEVANCE_FLOOR.md`
- `docs/REFERENCED_CAPABILITY_CONTRACTS.md`

## Completed Since MVP-6

- `:app:installDist` packaging repair.
- Local bundle backup and private GitHub remote push.
- Remote restore verification.
- Project control docs refresh.
- Original blueprint alignment.
- MVP-7 narrative quality spec draft.
- MVP-7 Candidate A read-only narrative quality audit.
- Candidate A.1 calibration refinements.
- Real audit sample collection.
- Deliberately flawed audit sample pack.
- Candidate A.2 calibration planning and implementation.
- A.2 real sample rerun.
- Audit JSON robustness investigation.
- Audit JSON verification docs hardening.
- Automatic stable-completion loop bootstrap.
- Diverse non-Pilot audit sample rerun.

## Hard Boundaries

Do not start or add any of these unless the user explicitly approves a new milestone:

- Candidate B or any pre-resolve narrative-quality gate.
- Candidate A.3 or MVP-8.
- Web server, HTTP API, port listener, Web UI, Android UI.
- Docker, Rust, embedding, vector DB, or multi-agent runtime.
- OpenAI API or any external AI call.
- Schema changes or migration edits.
- Validator, `stateVersion`, or `contextHash` bypass.
- `resolve-turn` or `StateReducer` write-path changes.
- Direct SQLite writes outside approved CLI/runtime flow.
- Retagging `mvp-6`.
- Pushing ignored/generated content such as `data/`, `out/`, `backups/`, build output, Gradle cache, or temp JDK.

## Normal Verification

Use these before committing any milestone:

```powershell
git status
git diff --stat
git diff --check
gradlew.bat clean test --no-daemon
```

For packaging work, also run:

```powershell
gradlew.bat :app:installDist
```

Then run the installed CLI from the generated distribution and verify at least:

```powershell
app.bat help
app.bat init-db --db <temp-db>
```

For MVP-7 audit report JSON validation on Windows PowerShell 5.1, read generated report JSON with explicit UTF-8:

```powershell
Get-Content -LiteralPath <audit-report.json> -Encoding UTF8 -Raw | ConvertFrom-Json
```

If `JAVA_HOME` is missing, a temporary JDK outside the repo may be used. Do not copy a JDK into the repo and do not write absolute JDK paths into project files.

## Remote Restore Check

To verify the GitHub backup from a clean location:

```powershell
$Clone = Join-Path $env:TEMP "ai-novel-engine-restore-check"
Remove-Item -LiteralPath $Clone -Recurse -Force -ErrorAction SilentlyContinue
git clone https://github.com/z72124223/ai-novel-engine.git $Clone
git -C $Clone switch codex/mvp6-checkpoint-branching-spec
git -C $Clone status
git -C $Clone tag --list
git -C $Clone rev-parse "mvp-6^{}"
```

Expected `mvp-6` target:

```text
be527031335efe8e2b0facae0145e1bbc084f8c6
```

## Branch Map

- `codex/mvp6-checkpoint-branching-spec`: current stable integration branch.
- `pilot/real-use-vertical-slice`: Pilot evidence branch pushed for preservation.
- `master`: older baseline branch, not the current stable line.
- Historical `feat/*` and `fix/*` branches may exist locally or in history. Do not delete or merge without a branch hygiene milestone and user approval.

## Current Next-Step Advice

Do not enter Candidate B automatically.

Recommended safe next steps:

1. Collect real author-led story arc audit samples when the user provides or approves them.
2. Draft a new explicit MVP-7 candidate brief if the user wants more read-only audit work.
3. Run a remote restore smoke check on demand.

Stop and ask before any work that would alter schema, Validator, `stateVersion`, `contextHash`, `resolve-turn`, `StateReducer`, or any formal SQLite write path.

## Suggested New Window Prompt

```text
You are the main integration window for this repo.
First read AGENTS.md, ROADMAP.md, PLANS.md, VERIFY.md, DONE_CRITERIA.md, docs/HANDOFF.md, and docs/PROJECT_COMPLETION_AUDIT.md.
Current integration branch: codex/mvp6-checkpoint-branching-spec.
Remote: https://github.com/z72124223/ai-novel-engine.git.
mvp-6 tag must remain at be527031335efe8e2b0facae0145e1bbc084f8c6.
Do not enter Candidate B, MVP-8, schema changes, Validator/stateVersion/contextHash changes, resolve-turn/StateReducer changes, server/API/UI/Docker/Rust/embedding work, or direct SQLite writes without explicit user approval.
Start by running git status --short --branch, git log --oneline --decorate -10, git tag --list, and git remote -v, then choose the next smallest milestone from current evidence.
```
