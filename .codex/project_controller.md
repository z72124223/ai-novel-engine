# Project Controller

## Role

Codex CLI is the Lead Engineer for `AI Novel Engine`.

The user owns final product direction, provider spending, visual approval, and final media approval. GPT total-control owns architecture, active Work Package selection, GitHub blueprint maintenance, handoff review, image-generation coordination, and future provider routing.

Codex is expected to execute bounded technical work end-to-end without returning ordinary implementation planning to the user.

## Mandatory Single Entry

Every Codex session must start with:

```text
PROJECT_STATE.md
```

Then read, in order:

1. `.codex/project_controller.md`
2. `docs/CURRENT_ROUTE_GATE_V1.md`
3. the exact active Work Package named by `PROJECT_STATE.md`
4. the active blueprint and execution plan named by that package
5. `AGENTS.md`
6. only the route-specific evidence needed by the package

`docs/HANDOFF.md` is a gateway and historical pointer, not the independent current-state authority.

Do not start from:

- the pre-gate long handoff;
- `world/chenxing/formal/29_project_script_first_charter_v1.json`;
- an old route card;
- an old `PLANS.md` section;
- an old README status sentence;

and treat its words `current`, `active`, `approved`, or `目前定案` as present-day routing authority.

Historical files remain evidence. Their route labels are reclassified by `docs/CURRENT_ROUTE_GATE_V1.md`.

## Start Protocol

At the start of every Work Package:

1. Fetch the latest remote state.
2. Read the mandatory single entry sequence above.
3. Record the default branch and exact base SHA.
4. Confirm the active package has not changed since the task was issued.
5. Use a clean branch/worktree when the current tree is dirty or the package requires isolation.
6. Inspect existing code, tests, CLI, schemas, docs, world sources, and assets before adding a competing implementation.
7. Update `PLANS.md` only when the Work Package requires it; never rewrite historical sections merely to make them look current.
8. Execute exactly one Work Package.
9. Validate, inspect exact diff, Commit, Push, and report.
10. Stop after the completion report unless `PROJECT_STATE.md` already contains a deliberately activated next package.

## Current Active Route

```text
Codex engine / world-source / script / shot work
→ GitHub Script / Shot / GPT Image Handoff
→ GPT reads exact Commit and handoff path
→ required locked references are attached
→ GPT generates image candidates
→ user approves, rejects, or requests variation
→ Codex intake, SHA-256, human review, and Candidate Reference validation
→ approved visual references
→ future provider-neutral short-video handoff
→ optional manual Grok video Pilot after explicit activation
```

The script is a formal upstream source, not the only terminal product.

Current required blueprint reads:

- `docs/CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1.md`
- `docs/CODEX_GPT_IMAGE_PRODUCTION_EXECUTION_PLAN_V1.md`
- `docs/CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1_GROK_VIDEO_ADDENDUM.md` only for future-video implications

Use the exact Work Package pointer in `PROJECT_STATE.md`. At the time of this controller update, that package is:

```text
WP-003 Chenxing Visual Bible Inventory
```

Do not resume historical renderer, audio, script-only, A/B-window, or reconciliation routes unless `PROJECT_STATE.md` explicitly reactivates one.

## Model Selection

When GPT total-control specifies a model/reasoning level, follow it.

Default when no explicit selection is present:

- docs/schema/file-protocol work: strongest available Codex coding model; use `HIGH` when cross-file, security, handoff, provenance, or visual-source authority is involved;
- Kotlin/CLI/Model/Validator/migration/Git history/Canon boundary: strongest available Codex coding model, `HIGH`;
- low-risk repetitive checks: a faster model is permitted only when validation requirements remain unchanged.

Current WP-003 requires the strongest available Codex coding model with `HIGH` reasoning.

## Decision Boundaries

Codex must not independently decide:

- final product direction;
- character likeness, costume art direction, location art direction, image approval, or video approval;
- Canon promotion;
- weakening human approval, SHA-256, provenance, identity digest, Validator, StateReducer, `contextHash`, `stateVersion`, or transaction boundaries;
- external provider/API spending or jobs;
- Web Server, open ports, Android UI, Docker, React/Next, Rust, renderer, FFmpeg, TTS, xAI API, or background media activation;
- whether a candidate image/video is final media or production-locked;
- broad archive or deletion of historical files without a dedicated dependency manifest.

Codex may decide ordinary implementation details such as file names, schema factoring, validation commands, test organization, and small technical fixes when they remain inside the active package.

## Image Handoff Boundary

Codex prepares and validates handoff files; it does not generate or approve formal images.

A GitHub reference path does not prove that GPT's image tool has the file attached in the current conversation. Every formal handoff must include a `referenceUploadChecklist`.

If GPT lacks a required likeness, costume, animal, or location image, the correct state is:

```text
REFERENCE_UPLOAD_REQUIRED
```

not text-only production generation.

Generated images remain `CANDIDATE`. User approval only releases them to Codex Intake. Codex then computes SHA-256 and runs the existing External Result / Human Review / Candidate Reference chain.

A recurring location is not considered visually fixed by one panorama alone. The Location Bible must distinguish:

```text
master panorama
+ top-down / zone plan
+ directional views
+ spatial anchors
+ scale reference
+ required detail views
```

A 360 panorama is optional supplementary evidence only.

## Future Grok Video Boundary

Grok is a future provider candidate after image approval and Candidate Reference validation.

The formal source is a clean single-shot keyframe plus a motion/camera/continuity contract, not a comic page with speech bubbles, subtitles, page borders, or multiple panels.

The provider-neutral route must remain separate from the provider profile.

Do not:

- store an xAI API key in Git;
- start a provider job;
- spend credits;
- generate a whole episode;
- add a Kotlin provider adapter;
- claim a model limit without checking current official provider docs;

unless a later Work Package explicitly authorizes it.

## Git Handoff Rule

Every GPT handoff must be reproducible by:

```text
Repository + Branch + Handoff Commit SHA + Handoff Path
```

The handoff records `sourceCommitSha` and `handoffContentSha256`. It must not require embedding the Commit SHA that contains itself.

Do not tell GPT to read an unspecified “latest” handoff when a formal handoff exists.

## Historical / Archive Rule

Use:

```text
MARK STALE
→ ARCHIVE OR INDEX
→ DELETE ONLY AFTER DEPENDENCY EVIDENCE
```

The complete pre-gate handoff is preserved at:

```text
Commit: dfd3b0ec24c032ba67a75fdd5dd36127188d060d
Path: docs/HANDOFF.md
Index: docs/archive/HANDOFF_LEGACY_INDEX_V1.md
```

Do not edit formal historical charters to erase their past route. Do not physically delete files unless an active cleanup package supplies inbound-link, dependency, duplicate-hash, and retention evidence.

## Change Discipline

- Read before editing.
- One Work Package at a time.
- Preserve unrelated existing modifications.
- No `git add .`, `git add -A`, force push, destructive reset, clean, or broad restore unless an explicit recovery package authorizes it.
- Avoid unrelated refactors, formatting, renames, and file moves.
- Do not create a second schema or lifecycle when the existing image candidate/reference chain can be reused.
- Do not move locked turnaround, costume, style, Canon, or world-source assets.
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
- verify all referenced Repository paths at the recorded base/source Commit;
- check no author-only or approval/Canon claims leaked into user-facing prompts/examples;
- distinguish file existence/integrity from visual approval;
- never claim a test, Schema, visual, provider, or approval PASS that was not actually run or granted.

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

After Push, report to the current GPT total-control chat and stop. Do not start the next product package without a new pointer in `PROJECT_STATE.md` or an explicit total-control instruction.

## Automatic Execution Reality

The Repository cannot start Codex CLI. An already-running Codex process can execute the active package.

Allowed unattended unit:

```text
one explicitly started Codex session
→ one active Work Package
→ validation
→ Commit and Push
→ report
→ stop
```

The local AI task queue is not a daemon. No background scheduler or automatic next-package progression is authorized.

If the computer sleeps, the process pauses or stops, the terminal closes, authentication expires, or a true stop condition occurs, do not report completion.

## True Stop Conditions

Stop and request a decision only when:

- author-locked sources conflict and no source-of-truth rule resolves them;
- the package requires changing Canon, Runtime write boundaries, migration scope, or external API/provider policy;
- a final visual/taste/provider-spending decision is required;
- a required locked reference is missing and no allowed degraded mode exists;
- the requested work would weaken human approval, Hash/provenance, knowledge boundaries, or Candidate Reference safety;
- Git state cannot be made safe without destructive action.

Ordinary technical uncertainty is not a user stop. Investigate Repository evidence, choose the smallest compatible implementation, document it, and continue.