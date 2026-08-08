# Codex Route Status Tags V0

Status: `ROUTE_STATUS_TAGS_V0_03_PASS`

Date: 2026-07-02

## Purpose

These tags make route state explicit so future windows do not confuse planning,
implementation, review, and approval.

## Status Tags

| Tag | Meaning | Can start implementation? |
| --- | --- | --- |
| `CONTROL_ONLY` | Document controls navigation, scope, or decision rules only. | No. |
| `PLANNED_NOT_IMPLEMENTED` | Route is described but no runtime feature is active. | No, unless 01/user assigns it. |
| `IMPLEMENTED_LOCAL_JSON` | Local file-protocol JSON command or artifact exists. | Only within its reviewed boundary. |
| `DRAFT_FOR_REVIEW` | Artifact is ready for 03 read-only review. | No next route until review. |
| `REVIEW_PASS_NOT_APPROVAL` | 03 technical review passed. | Only 01/user can choose the next route. |
| `STOPPED_BEFORE_EDITS` | A task was stopped before file edits. | No continuation without new assignment. |
| `HUMAN_APPROVAL_REQUIRED` | User/owner must explicitly decide. | No. |
| `BLOCKED_BY_BOUNDARY` | Task needs forbidden or unapproved work. | No. |
| `OPTIONAL_FUTURE_PROVIDER` | External paid/provider path may exist later. | No, until user/funding approval. |

## Current Route Tag Table

| Route family | Current tag | Notes |
| --- | --- | --- |
| Whole-project blueprint | `REVIEW_PASS_NOT_APPROVAL` | 03 PASS, still not final product approval. |
| Blueprint execution control layer | `REVIEW_PASS_NOT_APPROVAL` | 03 PASS; next step still needs 01/user route selection. |
| A-group evolution system | `CONTROL_ONLY` | Highest-priority planning/control route; installs event/root-cause/rule-upgrade/regression-check behavior before ordinary route continuation. |
| True final progress model | `CONTROL_ONLY` | Separates local baseline 100% from true final completion. |
| Positive approval matrix | `CONTROL_ONLY` | Defines approval meaning; grants none. |
| 09 foundation cards | `PLANNED_NOT_IMPLEMENTED` | Organization exists; cards are not canon or setting pack. |
| Demand-driven query / retrieval | `PLANNED_NOT_IMPLEMENTED` | Templates exist; no SQL/schema/runtime/provider path. |
| Script-ready scene packets | `PLANNED_NOT_IMPLEMENTED` | Contracts exist; not resumed. |
| 04/05/06/07/08 | `HUMAN_APPROVAL_REQUIRED` | Paused manual flow. |
| Storyboard / camera packets | `PLANNED_NOT_IMPLEMENTED` | Planning exists; no image generation. |
| Asset lifecycle / registry | `HUMAN_APPROVAL_REQUIRED` | Storage/registry decision needed before implementation. |
| Voice timeline / subtitles / SFX | `PLANNED_NOT_IMPLEMENTED` | Needs standalone local JSON contract. |
| Local story-to-render chain | `IMPLEMENTED_LOCAL_JSON` | Local JSON chain exists; not final video readiness. |
| Renderer-manifest chain through Phase 19 | `REVIEW_PASS_NOT_APPROVAL` | Phase 20 stopped; no automatic continuation. |
| Local renderer execution | `HUMAN_APPROVAL_REQUIRED` | Do not run renderer/ffmpeg/TTS without explicit assignment. |
| External provider / funding | `OPTIONAL_FUTURE_PROVIDER` | No spending/provider/API calls approved. |

## Promotion Rules

1. `CONTROL_ONLY` can become `PLANNED_NOT_IMPLEMENTED` only by adding a route
   artifact, not by reviewer praise.
2. `PLANNED_NOT_IMPLEMENTED` can become implementation work only after explicit
   01/user route selection.
3. `DRAFT_FOR_REVIEW` can become `REVIEW_PASS_NOT_APPROVAL` only after 03 PASS.
4. `REVIEW_PASS_NOT_APPROVAL` never becomes human approval by itself.
5. Any route touching canon, registry, release, final media, paid tools,
   provider/API, direct SQLite writes, schema, renderer execution, or media
   generation becomes `HUMAN_APPROVAL_REQUIRED`.

## Boundary

These tags are labels only. They do not implement CLI commands, schema,
runtime enforcement, media generation, provider/API calls, or approval.
