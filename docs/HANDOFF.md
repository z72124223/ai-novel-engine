# Project Handoff Gateway

Status: `ACTIVE_GATEWAY`

Date: 2026-07-10

## Start Here

This file is no longer the full project-state archive and must not independently declare the current product route.

Every new GPT, Codex CLI, reviewer, or collaborator window must read in this order:

1. `PROJECT_STATE.md`
2. `.codex/project_controller.md`
3. `docs/CURRENT_ROUTE_GATE_V1.md`
4. the exact active Work Package named by `PROJECT_STATE.md`
5. the active blueprint and execution plan named by that Work Package
6. `AGENTS.md`
7. only the route-specific evidence required by the package

Do not start from an old charter, old route card, old `PLANS.md` section, or old handoff section and treat words such as `current`, `active`, `approved`, or `目前定案` as self-renewing authority.

## Current Route Summary

```text
Codex CLI runs engine, world-source, script, and shot preparation
→ Script / Shot / GPT image handoff is committed to GitHub
→ GPT reads an exact Commit and handoff path
→ required locked references are attached
→ GPT generates image candidates
→ user approves, rejects, or requests variation
→ Codex imports the retained file, computes SHA-256, and runs the existing review / Candidate Reference chain
→ approved images support a later short-video handoff
```

The script is a formal upstream source, not the only terminal product.

Images are active mainline output. Video is a planned downstream output after the image Pilot proves stable character and location references.

## Current Blueprint Pointers

- `docs/CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1.md`
- `docs/CODEX_GPT_IMAGE_PRODUCTION_EXECUTION_PLAN_V1.md`
- `docs/CODEX_GPT_IMAGE_PRODUCTION_BLUEPRINT_V1_GROK_VIDEO_ADDENDUM.md`

The Grok video addendum is future planning only. It does not authorize provider calls, spending, API integration, or video automation now.

## Current Work Package

The exact active Work Package is declared only by `PROJECT_STATE.md`.

At the time of this gateway update, the active package is:

```text
WP-003 Chenxing Visual Bible Inventory
```

Codex must fetch the latest remote default branch and verify `PROJECT_STATE.md` before relying on this snapshot.

## Historical Handoff

The full pre-gate handoff, including old thread IDs, route maps, verification baselines, and historical “current” headings, remains available at:

```text
Commit: dfd3b0ec24c032ba67a75fdd5dd36127188d060d
Path: docs/HANDOFF.md
Archive index: docs/archive/HANDOFF_LEGACY_INDEX_V1.md
```

Historical classification:

- the audio-novel abort remains historical evidence;
- the script-first charter remains historical product-route evidence;
- MVP-8 reader-first and Canon boundaries remain active engine constraints;
- Grok/Codex role, reference-first, candidate-only, and human-approval rules remain useful;
- statements that images/video are currently paused or that script is the only terminal product are superseded.

Do not rewrite historical charter content merely to make it look current. Use the supersession table in `docs/CURRENT_ROUTE_GATE_V1.md`.

## World Source Entry

For Chenxing worldview work, use the controlled Repository/CLI authority named by the active Work Package. When the package requires SQL world-source retrieval, prefer:

```text
query-world-source --db data/story.db --q <text> [--classification <id>]
```

Do not expose author-only secrets or dump the entire database into an image or provider handoff.

## Approval Boundary

The following never count as user approval:

- Codex completion;
- GPT generation success;
- technical PASS;
- JSON or Schema PASS;
- file existence in Git;
- provider success;
- silence;
- elapsed time;
- a generated preview.

Generated images and videos remain candidates until the explicit user decision and the required Codex intake/review/Hash chain complete.

## Automatic Execution Boundary

The Repository can instruct an already-running Codex CLI session. It cannot start Codex by itself.

Allowed unit:

```text
one explicitly started Codex session
→ one active Work Package
→ validation
→ Commit and Push
→ report
→ stop
```

Codex must not automatically advance to the next product Work Package after Push unless `PROJECT_STATE.md` has already been deliberately updated to authorize it.

The local task queue is not a daemon. If the computer sleeps, the terminal closes, authentication expires, or a true stop condition occurs, no completion should be assumed.

## Cleanup Boundary

The current gateway replaces stale routing text but does not authorize broad deletion.

Use:

```text
MARK STALE
→ ARCHIVE OR INDEX
→ DELETE ONLY AFTER DEPENDENCY EVIDENCE
```

Do not delete Canon, world sources, migrations, tests, accepted/locked assets, provenance, Hash/review evidence, or rejected-drift evidence without a bounded cleanup package and dependency manifest.