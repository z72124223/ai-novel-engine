# Legacy Handoff Archive Index V1

Status: `ARCHIVE_POINTER_NOT_CURRENT_ROUTE`

Date: 2026-07-10

## Purpose

The former long `docs/HANDOFF.md` accumulated multiple generations of route state, including headings that called their own historical decisions “current”. It remains valuable as evidence, but it must not be the startup authority for a new window.

The complete pre-gate handoff content is preserved in Git history at:

```text
Repository: z72124223/ai-novel-engine
Commit: dfd3b0ec24c032ba67a75fdd5dd36127188d060d
Path: docs/HANDOFF.md
```

Use that version only when exact historical thread IDs, old Commit references, past verification, or route rationale are required.

## Current Replacement

New windows must read:

1. `PROJECT_STATE.md`
2. `.codex/project_controller.md`
3. `docs/CURRENT_ROUTE_GATE_V1.md`
4. the active Work Package named by `PROJECT_STATE.md`

The current `docs/HANDOFF.md` is a concise gateway, not the historical evidence body.

## Historical Route Classification

| Historical item | Classification | Notes |
| --- | --- | --- |
| full pre-gate `docs/HANDOFF.md` | `HISTORICAL_HANDOFF_EVIDENCE` | exact past state only |
| `world/chenxing/formal/29_project_script_first_charter_v1.json` | `HISTORICAL_PRODUCT_ROUTE_CHARTER` | script remains upstream; text-only terminal target is superseded |
| `docs/AUDIO_NOVEL_PHASE_HANDOFF_AND_ABORT_V1.md` | `HISTORICAL_EXPERIMENT_ABORT_EVIDENCE` | preserves why the audio-novel experiment stopped |
| `docs/CODEX_GROK_BUILD_HANDOFF_AND_IMAGE_PRODUCTION_LANE_V0.md` | `PARTIALLY_CURRENT_ROLE_EVIDENCE` | role, reference-first, candidate, and user-approval rules remain useful |
| old route cards, A/B window maps, renderer phases, and route-control bundles | `ON_DEMAND_HISTORICAL_CONTROL_EVIDENCE` | do not use for active package selection unless the current entry points to them |
| rejected opening preview frames | `REJECTED_DRIFT_EVIDENCE` | preserve decision evidence; never use as formal reference without explicit reversal |

## Retention Rule

Git history is the immutable archive for the former handoff body. Do not recreate another 500-line current handoff by copying old status sections forward.

When historical evidence is needed, cite:

```text
Commit + Path + exact section
```

Do not restore an old “current” label merely because its content is useful.

## Deletion Boundary

This archive index does not authorize deletion of historical docs, formal charters, tests, samples, Canon, source material, assets, or review evidence.

A later cleanup package must first produce a dependency and deletion manifest.