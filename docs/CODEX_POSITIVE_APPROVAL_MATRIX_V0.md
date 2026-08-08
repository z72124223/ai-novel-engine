# Codex Positive Approval Matrix V0

Status: `POSITIVE_APPROVAL_MATRIX_V0_DRAFT_FOR_REVIEW`

Date: 2026-07-02

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

## Purpose

This matrix defines what counts as approval. The project already has many
"not approval" warnings; this document makes the positive gates explicit.

## Approval Matrix

| Gate | Approver | Approves | Does not approve |
| --- | --- | --- | --- |
| 03 technical PASS | 03 reviewer | The reviewed slice matches its technical/doc boundary. | User taste, canon, final media, release, production lock. |
| 01 route decision | 01 control thread | Exactly one next bounded task or route choice. | Silent continuation or unattended loop. |
| User direction approval | User | Product direction, worldbuilding choice, funding/provider decision. | Automatic implementation beyond the named scope. |
| 05 review PASS | 05 reviewer when resumed | Narrative/packet quality for the assigned packet. | User approval or production approval. |
| 06 display approval | User after display | User-facing story/storyboard direction shown to the user. | Hidden canon, asset registry, final media. |
| Asset approval | User/01 designated approval gate | Candidate asset becomes approved reference. | Runtime SQLite writes unless separately approved. |
| Canon approval | User/01 designated canon gate | Content can become canon or setting-pack candidate. | Automatic setting-pack apply. |
| Registry approval | User/01 designated registry gate | Asset/metadata can enter approved registry. | Direct SQLite writes without approved pipeline. |
| Final media approval | User | Final video/audio/media deliverable accepted. | Release or production lock unless explicitly combined. |
| Release / production lock | User/01 release gate | Deliverable is locked for distribution. | Future provider/funding changes. |

## Never Counts As Approval

- Silence.
- A watchdog wakeup.
- Quota availability.
- 02 completion.
- 03 technical PASS.
- Local JSON PASS.
- A generated demo.
- A sample MP4.
- A draft prompt, draft storyboard, draft asset card, or draft manifest.

## Boundary

This is docs/control only. It grants no canon, registry, final media, release,
provider/media handoff, production lock, direct SQLite write, or implementation
permission.

