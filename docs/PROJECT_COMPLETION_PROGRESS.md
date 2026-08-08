# Project Completion Progress

Status: `CONTROL_ESTIMATE_ACTIVE`

Date: 2026-06-27

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

This file defines the project-completion percentage shown by `01` after every `03` review report. The displayed number is now product deliverable completion, not gate hardening completion. The previous 79% value is retained only as a separate gate-hardening indicator because the old table did not add up.

## Display Rule

After every `03` PASS / REVISE / FAIL report, `01` must include:

```text
Project completion progress: NN%
```

Rules:

- `PASS`: update the estimate only if the accepted slice completes a planned product capability in `docs/PROJECT_PROGRESS_RUBRIC.md`.
- `REVISE`: keep the last accepted percentage and name the blocking issue.
- `FAIL`: keep the last accepted percentage and stop the loop until `01` / user decides.
- If the product target changes, revise `docs/PROJECT_PROGRESS_RUBRIC.md` and this file before trusting future percentages.
- Do not add gate count directly to product completion. A new gate can increase gate hardening without increasing product deliverable completion.

## Current Product Estimate

Project completion progress: `58%`

This replaces the previous `79% pending rubric repair` control value. The 79% number is not discarded; it is reclassified under "Gate Hardening Indicator" below.

Basis:

| Product deliverable area | Weight | Current | Evidence note |
| --- | ---: | ---: | --- |
| Stable local engine foundation | 20 | 20 | Runtime foundation and local CLI protocol exist. |
| Novelization intake / quality / revision gates | 15 | 13 | Gate chain exists, but end-to-end chapter quality fixture is still missing. |
| Accepted draft to script packet gates | 10 | 8 | Accepted draft and script packet boundary exists; full fixture still missing. |
| Reviewable storyboard packet path | 20 | 9 | Planning, draft, review, human-review and candidate-reference boundaries exist, but final reviewable storyboard packet and E2E fixture are not closed. |
| Candidate asset / image reference preparation | 15 | 6 | Candidate-only reference chain exists; no approval, registry write, lock, or production asset status. |
| Comic / video / audio production path | 15 | 0 | No image/video/audio generation, voice, or final media integration is approved or implemented. |
| Release hygiene, cleanup, packaging, handoff hardening | 5 | 2 | Handoff process exists, but release cleanup and packaging are incomplete. |
| Total | 100 | 58 | Sum of current column. |

## Gate Hardening Indicator

Gate hardening completion: `79%`

This is a separate control signal for how much of the local JSON gate chain has been hardened by 02/03 review. It must not be displayed as `Project completion progress` unless the product rubric is changed and the table supports the same number.

Why separate it:

- Gate hardening tracks validation coverage, source binding, forbidden wording guards, and review handoff discipline.
- Product deliverable completion tracks whether the project can deliver the requested user-facing outcome.
- The expert model review questioned the old 79% because, if the target is a reviewable storyboard packet or later comic/video/audio deliverable, 79% is likely too high.

## Last Counted PASS

Gate hardening counted through:

- `Accelerated Candidate Reference Registry Validation and Acceptance Boundary Batch` REVISE P1 fix: PASS by `03`.

Product deliverable count remains at 58 until a product milestone closes.

## Not Yet Counted As Product Completion

- `candidate_reference_acceptance_packet_validation`.
- E2E happy-path fixture from accepted draft to reviewable storyboard packet.
- Read-only story movement audit.
- Release hygiene and final verification.
- Any formal registry write, asset approval, approved reference, registered reference, production lock, canon, image/video/audio generation, voice, or final media integration.

## Next Progress Rules

Suggested next increments are defined in `docs/PROJECT_PROGRESS_RUBRIC.md`.

Recommended order:

1. Complete this docs/control repair.
2. Add `candidate_reference_acceptance_packet_validation` as boundary closure, without formal registry write.
3. Add an E2E happy-path fixture from accepted draft to reviewable storyboard packet.
4. Add read-only story movement audit and release hygiene.
