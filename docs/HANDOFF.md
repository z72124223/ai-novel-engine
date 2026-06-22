# Project Handoff Index

This file is the first stop for a new Codex window. It records the current stable point, what is safe to touch, and what must not be started without explicit user approval.

## Stable Point

- Current stable integration branch: `codex/mvp6-checkpoint-branching-spec`
- Remote: `origin https://github.com/z72124223/ai-novel-engine.git`
- Current branch should track `origin/codex/mvp6-checkpoint-branching-spec`.
- Use `git status --short --branch` and `git log --oneline --decorate -10` for the latest mutable `HEAD`.
- Latest product-contract baseline before this control-doc refresh: `63cc2f17b968dd9f1c9fff602150802d1ebf2ff2`.
- Use `git log --oneline --decorate -10` for the actual latest `HEAD` after any newer control-doc alignment commits.
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

Additional docs-only product-control baseline as of 2026-06-22:

- Authoring quality guidelines, narrative review gate, scene lock / storyboard asset boundary, script-ready scene packet contracts, Qingli scene registry seed brief, Qingli script packet registry dry-run review, storyboard / camera planning, production asset registry planning, and asset registry implementation blueprint are completed and pushed.
- User product decision: current final deliverable is reviewable storyboard images / storyboard packets. High-quality animation is not the current deliverable.
- Seedance / Oiioii / video generation is optional future downstream work only. It is not implemented and not the current mainline.
- Qingli is historical sample / process evidence after `docs/WORLD_BIBLE_REBUILD_DECISION_BRIEF.md`; do not use it as the new-world opening base or assetization starting point.
- Storyboard / camera planning, production asset registry planning, and asset registry implementation blueprint are future-planning docs. They are not runtime, schema, image-generation, Seedance, or Oiioii implementations.
- `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md` is the current user / 00 input entry for rebuilding the non-Qingli world foundation. It is not canon, a setting pack, or a runtime artifact.
- `docs/DEMAND_DRIVEN_QUERY_LAYER_BLUEPRINT.md` defines future World Card Queryer, Asset Queryer, and Story Card Queryer contracts for scoped product-layer retrieval. It is not implemented runtime, schema, CLI, SQLite, external API, GPT automation, Seedance, or Oiioii work.
- `docs/SIMULATION_FIRST_KNOWLEDGE_TRUTH_RULES.md` defines product-layer guardrails that treat bottom-layer truth as world causality and distributed actor knowledge, not as a plot objective, forced reveal, forced concealment, or mystery product. It is not runtime enforcement or canon world content.
- Script packet contract baseline commit: `63cc2f17b968dd9f1c9fff602150802d1ebf2ff2`.
- Qingli scene registry seed baseline commit: `c248cefd0dcde8a878aae290bbb3215ca4d5cb1b`.
- Qingli script packet registry dry-run baseline commit: `ee80a16f83825ac56b2449285c19981a81f746a1`.
- Latest local docs-only planning may include uncommitted storyboard / camera and asset registry blueprint files. Check `git status --short` before assuming remote state.
- 04 / 05 / 06 / 07 / 08 are paused. They are not active runtime and must not be resumed without 01 explicit handoff.
- Manual handoff only: `01 explicit handoff -> 04 generation -> 05 review -> 06 display -> user explicit approval -> report to 01 -> 07 asset candidates -> 08 GPT storyboard pickup / verification -> user-reviewable storyboard delivery`.
- Do not use automation / scheduler wakeups to advance the 04 / 05 / 06 / 07 / 08 flow.

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
- Authoring quality guidelines / review workflow docs.
- Narrative review gate and eight-point checklist.
- Scene lock and animation asset boundary.
- Script-ready scene packet contracts.
- Qingli scene registry seed brief.
- Qingli script packet registry dry-run review.
- Storyboard / camera planning, aligned to storyboard-first final deliverables.
- Production asset registry planning, aligned to storyboard consistency before optional downstream video.
- Asset registry implementation blueprint, future-only and not schema/runtime approval.
- New world foundation questionnaire, as a docs-only user-input surface before any foundation brief or 04/05/06/07/08 restart.

## Hard Boundaries

Do not start or add any of these unless the user explicitly approves a new milestone:

- Candidate B or any pre-resolve narrative-quality gate.
- Candidate A.3 or MVP-8.
- Web server, HTTP API, port listener, Web UI, Android UI.
- Docker, Rust, embedding, vector DB, or multi-agent runtime.
- Automation / scheduler advancement for 04 / 05 / 06 / 07 / 08 handoff.
- Resuming 04 / 05 / 06 generation, review, display, playtest, 07 asset-candidate creation, or 08 storyboard pickup without 01 explicit handoff and user approval where required.
- Seedance / Oiioii integration, high-quality video generation, or animation asset pipeline implementation.
- Production asset registry schema, migrations, import CLI, generated image storage, or direct SQLite asset writes.
- Demand-driven queryer implementation, query audit tables, query CLI commands, runtime query enforcement, or external retrieval services.
- Treating simulation-first guardrails as schema, Validator behavior, StateReducer behavior, setting-pack canon, or approval to start 09 / 04 / 05 / 06 / 07 / 08.
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

For explicitly approved docs-only documentation/control milestones, 01 or the user may allow the narrow docs-only exception in `VERIFY.md`: `git diff --check`, diff/stat/staged inspection, no code/runtime touched, no ignored artifacts staged, final explanation for skipped Gradle, and direct 03 read-only verification. Do not apply that exception to code/runtime/schema/CLI/Pilot work.

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

1. Wait for user / 00 answers to `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md`.
2. Draft `NEW_WORLD_FOUNDATION_BRIEF` only after the user provides enough world-preference material.
3. Draft a 09 world-card / situation-continuity-pressure card organization brief only after a foundation brief exists; keep it docs-only unless 01/user separately approves implementation, and preserve the simulation-first rule that causality is not a reveal schedule.
4. Draft query request / response templates only after 01 explicitly asks to make the demand-driven query layer more concrete; do not implement schema, CLI, SQLite, or runtime enforcement.
5. Draft a 04/05/06/07/08 manual resumption decision brief only if 01/user asks whether to resume that manual handoff flow. It must not itself generate, review, display, retrieve storyboards, or create assets.
6. Collect Pilot-2 human turn input only when the user/player provides explicit input or approval.
7. Collect real author-led story arc audit samples when the user provides or approves them.
8. Draft a new explicit MVP-7 Candidate A.3 read-only audit brief only if 01/user explicitly asks.
9. Run a remote restore smoke check on demand.

Stop and ask before any work that would alter schema, Validator, `stateVersion`, `contextHash`, `resolve-turn`, `StateReducer`, or any formal SQLite write path.

## Suggested New Window Prompt

```text
You are the main integration window for this repo.
First read AGENTS.md, ROADMAP.md, PLANS.md, VERIFY.md, DONE_CRITERIA.md, docs/HANDOFF.md, and docs/PROJECT_COMPLETION_AUDIT.md.
Current integration branch: codex/mvp6-checkpoint-branching-spec.
Remote: https://github.com/z72124223/ai-novel-engine.git.
mvp-6 tag must remain at be527031335efe8e2b0facae0145e1bbc084f8c6.
Do not enter Candidate B, MVP-8, schema changes, Validator/stateVersion/contextHash changes, resolve-turn/StateReducer changes, server/API/UI/Docker/Rust/embedding/Oiioii/Seedance integration work, automation/scheduler handoff, or direct SQLite writes without explicit user approval.
Current final deliverable is reviewable storyboard images / storyboard packets, not high-quality video. Seedance / Oiioii are optional future downstream only.
04/05/06/07/08 are paused. Resuming them requires manual handoff only: 01 explicit handoff -> 04 generation -> 05 review -> 06 display -> user explicit approval -> report to 01 -> 07 asset candidates -> 08 GPT storyboard pickup / verification -> user-reviewable storyboard delivery.
When 02 finishes a milestone, notify 03 using a verifier-window prompt: "You are 03 驗收稽核窗口..." with commit, push, changed files, verification, scope, safety boundaries, and review focus.
Start by running git status --short --branch, git log --oneline --decorate -10, git tag --list, and git remote -v, then choose the next smallest milestone from current evidence.
```
