# Project Handoff Index

This file is the first stop for a new Codex window. It records the current stable point, what is safe to touch, and what must not be started without explicit user approval.

## Stable Point

- Current stable branch: `codex/mvp6-checkpoint-branching-spec`
- Remote: `origin https://github.com/z72124223/ai-novel-engine.git`
- Current branch should track `origin/codex/mvp6-checkpoint-branching-spec`
- Use `git log --oneline --decorate -5` for the latest mutable HEAD.
- Release tag: `mvp-6`
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

## Last Verified

Verified on 2026-06-20:

- Remote clone from `https://github.com/z72124223/ai-novel-engine.git` succeeded.
- Remote clone checked out `codex/mvp6-checkpoint-branching-spec`.
- Remote clone saw `pilot/real-use-vertical-slice`.
- Remote clone saw `mvp-6` at `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- Remote clone ran `gradlew.bat clean test --no-daemon`.
- Test result: 220 tests, 0 failures, 0 errors, 0 skipped.
- Project completion audit was committed and pushed as `43e0169882f449bb2b0c0f809b5bfdee8ca389de`.

## Read First

Read these before planning or editing:

1. `AGENTS.md`
2. `README.md`
3. `ROADMAP.md`
4. `DONE_CRITERIA.md`
5. `VERIFY.md`
6. `docs/MVP6_CHECKPOINT_BRANCHING_SPEC.md`
7. `docs/MVP6_CLI_ACCEPTANCE.md`
8. `docs/TURN_PROTOCOL.md`
9. `docs/VALIDATION_RULES.md`

For Pilot or context-budget work, also read:

- `docs/PILOT1_FINDINGS.md`
- `docs/PILOT1B_COVERAGE_REPAIR.md`
- `docs/CONTEXT_RELEVANCE_FLOOR.md`
- `docs/REFERENCED_CAPABILITY_CONTRACTS.md`

## Hard Boundaries

Do not start or add any of these unless the user explicitly approves a new milestone:

- MVP-7 implementation.
- Web server, HTTP API, port listener, Web UI, Android UI.
- Docker, Rust, embedding, vector DB, or multi-agent runtime.
- OpenAI API or any external AI call.
- Schema changes or migration edits.
- Validator, `stateVersion`, or `contextHash` bypass.
- Direct SQLite writes outside approved CLI/runtime flow.
- Retagging `mvp-6`.
- Pushing ignored/generated content such as `data/`, `out/`, `backups/`, build output, Gradle cache, or temp JDK.

## Normal Verification

Use these before committing code or runtime changes:

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

If `JAVA_HOME` is missing, a temporary JDK outside the repo may be used. Do not copy a JDK into the repo and do not write absolute JDK paths into project files.

## Remote Restore Check

To verify the GitHub backup from a clean location:

```powershell
$Clone = Join-Path $env:TEMP "ai-novel-engine-restore-check"
Remove-Item -LiteralPath $Clone -Recurse -Force -ErrorAction SilentlyContinue
git clone https://github.com/z72124223/ai-novel-engine.git $Clone
git -C $Clone status
git -C $Clone tag --list
git -C $Clone rev-list -n 1 mvp-6
```

Expected `mvp-6` target:

```text
be527031335efe8e2b0facae0145e1bbc084f8c6
```

## Branch Map

- `codex/mvp6-checkpoint-branching-spec`: current stable integration branch.
- `pilot/real-use-vertical-slice`: Pilot evidence branch pushed for preservation.
- `master`: older baseline branch, not the current stable line.
- `feat/*` and `fix/*`: historical milestone branches. Do not delete or merge without a branch hygiene milestone and user approval.

## Current Next-Step Advice

Do not enter MVP-7 automatically.

Recommended next small milestone:

- Choose the next milestone explicitly from current evidence. Good candidates are branch strategy review, remote restore smoke on demand, or an MVP-7 spec draft only if the user explicitly approves MVP-7 planning.

Alternative safe milestone:

- Run a read-only repository audit after remote backup: check ignored files, branch divergence, stale plans, and docs that still describe old "current" work.

## Suggested New Window Prompt

```text
請先閱讀 AGENTS.md 與 docs/HANDOFF.md。
目前穩定分支是 codex/mvp6-checkpoint-branching-spec，
remote 是 https://github.com/z72124223/ai-novel-engine.git，
mvp-6 必須仍指向 be527031335efe8e2b0facae0145e1bbc084f8c6。

請不要開 MVP-7。
先執行 git status、git log --oneline --decorate -8、git tag --list、git remote -v，
然後根據 HANDOFF.md 建議一個最小 milestone。
```
