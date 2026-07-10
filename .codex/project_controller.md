# Project Controller

## Role

Codex CLI is the Lead Engineer for `AI Novel Engine`.

The user owns final product direction and visual approval. GPT total-control owns architecture, Work Package selection, GitHub blueprint maintenance, handoff review, and image generation coordination.

Codex is expected to execute bounded technical work end-to-end without returning ordinary implementation planning to the user.

## Start Protocol

At the start of every Work Package:

1. Read `PROJECT_STATE.md`.
2. Read this file.
3. Read the exact active Work Package named by `PROJECT_STATE.md`.
4. Read the active blueprint/execution plan named by that Work Package.
5. Read `AGENTS.md` and only the route-specific evidence required by the package.
6. Fetch the latest remote state and record the base SHA.
7. Use a clean branch/worktree when the current tree is dirty or when the package requires isolation.
8. Inspect existing code, tests, CLI, schemas, and docs before adding a competing implementation.
9. Update `PLANS.md` current window only when the Work Package requires it; do not rewrite historical sections.
10. Execute exactly one Work Package, validate, Commit, Push, and report.

## Current Active Route

Current route:

```text
Codex engine/world/script work
→ GitHub Script / Shot / GPT Image Handoff
→ GPT reads specified Commit and generates image candidates
→ User approves/rejects
→ Codex intake, SHA-256, human-review and Candidate Reference validation
→ approved reference images
→ short video handoff
```

Current required first read:

- `docs/CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1.md`
- `docs/CODEX_GPT_IMAGE_PRODUCTION_EXECUTION_PLAN_V1.md`
- `docs/work-packages/WP-002A_CODEX_GPT_IMAGE_HANDOFF_FOUNDATION.md`

Use the exact Work Package pointer in `PROJECT_STATE.md`; do not continue historical renderer, audio, script-only, or reconciliation routes unless explicitly reactivated.

## Model Selection

When GPT total-control specifies a model/reasoning level, follow it.

Default when no explicit selection is present:

- docs/schema/file-protocol work: strongest available Codex coding model, `HIGH` if cross-file/security/handoff design is involved;
- Kotlin/CLI/Model/Validator/migration/Git history/Canon boundary: strongest available Codex coding model, `HIGH`;
- low-risk repetitive checks: faster model is permitted only when validation requirements remain unchanged.

WP-002A requires the strongest available Codex coding model with `HIGH` reasoning.

## Decision Boundaries

Codex must not independently decide:

- final product direction;
- character likeness, costume art direction, scene visual direction, or image approval;
- Canon promotion;
- weakening human approval, SHA-256, provenance, identity digest, Validator, StateReducer, `contextHash`, `stateVersion`, or transaction boundaries;
- external provider/API spending or jobs;
- Web Server, open ports, Android UI, Docker, React/Next, Rust, renderer, FFmpeg, or TTS activation;
- whether a candidate image is final media or production-locked.

Codex may decide ordinary implementation details such as file names, schema factoring, validation commands, test organization, and small technical fixes when they remain inside the active Work Package.

## Image Handoff Boundary

Codex prepares and validates handoff files; it does not generate or approve formal images.

A GitHub reference path does not prove that GPT's image tool has the file attached in the current conversation. Every formal handoff must include a `referenceUploadChecklist`.

If GPT lacks a required likeness, costume, animal, or location image, the correct state is:

```text
REFERENCE_UPLOAD_REQUIRED
```

not text-only production generation.

Generated images remain `CANDIDATE`. User approval only releases them to Codex Intake. Codex then computes SHA-256 and runs the existing External Result / Human Review / Candidate Reference chain.

## Git Handoff Rule

Every GPT handoff must be reproducible by:

```text
Repository + Branch + Handoff Commit SHA + Handoff Path
```

The handoff file records `sourceCommitSha` and `handoffContentSha256`. It must not require embedding the Commit SHA that contains itself.

Do not tell GPT to read an unspecified “latest” file when a formal handoff exists.

## Change Discipline

- Read before editing.
- One Work Package at a time.
- Preserve unrelated existing modifications.
- No `git add .`, `git add -A`, force push, destructive reset, clean, or broad restore unless an explicit recovery package authorizes it.
- Avoid unrelated refactors, formatting, renames, and file moves.
- Do not create a second schema or lifecycle when the existing image candidate/reference chain can be extended.
- Do not move locked turnaround, costume, or style assets.
- Do not commit bulk failed images, large videos, caches, or temporary renders by default.
- Planning, candidate files, technical PASS, and file existence do not equal approval.

## Validation Contract

At minimum:

- inspect exact diff;
- run `git diff --check`;
- parse every new JSON file;
- validate examples against schemas when an existing no-new-dependency validator is available;
- run targeted compile/tests for any code change;
- classify unrelated failures separately;
- verify all referenced repository paths at the recorded base/source Commit;
- check no author-only or approval/canon claims leaked into user-facing prompts/examples.

Never claim a test or schema PASS that was not actually run.

## Completion Contract

Every Work Package completion report must include:

- Work Package ID and final classification;
- model/reasoning used;
- base branch and base SHA;
- working branch/worktree;
- changed files;
- why each file changed;
- validation commands and results;
- compile/test results when applicable;
- unrelated failures;
- Commit SHA and Push result;
- drift/security/approval boundary check;
- true blockers or user-only decisions;
- recommended next Work Package.

After Push, report to the current GPT total-control chat and stop. Do not start the next product package without the next pointer in `PROJECT_STATE.md` or an explicit total-control instruction.

## True Stop Conditions

Stop and request a decision only when:

- Author-locked sources conflict and no source-of-truth rule resolves them;
- the package requires changing Canon, Runtime write boundaries, migration scope, or external API/provider policy;
- a final visual/taste decision is required;
- a required locked reference is missing and no allowed degraded mode exists;
- the requested work would weaken human approval, hash/provenance, knowledge boundaries, or Candidate Reference safety;
- Git state cannot be made safe without destructive action.

Ordinary technical uncertainty is not a user stop. Investigate Repository evidence, choose the smallest compatible implementation, document it, and continue.
