# Project Completion Progress

Status: `CONTROL_ESTIMATE_ACTIVE`

Date: 2026-06-27

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

This file defines the project-completion percentage shown by `01` after every `03` review report. The displayed percentage is a control estimate for product deliverable completion, not a runtime metric, not SQLite data, and not approval to skip `03` review.

## Display Rule

After every `03` PASS / REVISE / FAIL report, `01` must include:

```text
Project completion progress: NN%
```

Rules:

- `PASS`: update the estimate only if the accepted slice completes a planned product capability.
- `REVISE`: keep the last accepted product-deliverable percentage and name the blocking issue.
- `FAIL`: keep the last accepted product-deliverable percentage and stop the loop until `01` / user decides.
- If the product target changes, revise this file before trusting future percentages.

## Current Display Estimate

Project completion progress: `80%`

This replaces the previous `79%` display value. The previous table was internally inconsistent: its rows added to `93`, while the displayed total was `79`. The new display value separates product deliverable completion from gate hardening completion.

## Product Deliverable Rubric

This is the percentage `01` displays.

| Area | Weight | Current | Evidence |
| --- | ---: | ---: | --- |
| Stable local engine foundation | 20 | 20 | MVP-1 through MVP-6 local runtime, SQLite, validator/reducer, branching, packaging, and recovery evidence are treated as complete. |
| Novelization intake / quality / revision gates | 15 | 13 | Draft intake, prose quality audit, revision brief, revised draft intake, and accepted draft packet gates exist, but need an end-to-end chapter fixture. |
| Accepted draft to script packet gates | 15 | 12 | Accepted draft to script packet and structure/source-binding gates exist, but final product proof still depends on storyboard E2E. |
| Storyboard / asset / candidate-reference JSON gates | 20 | 12 | Many deterministic gates are implemented and reviewed, including candidate-reference acceptance packet validation and the reviewable storyboard packet packaging gate; image-facing delivery and release proof remain open. |
| Reviewable storyboard packet / image-facing deliverable path | 20 | 13 | E2E happy-path fixture v1 reaches `storyboard_packet_human_review_ready`; story movement audit verifies minimum deterministic story movement evidence; `ReviewableStoryboardPacket` now exists as a named local JSON product artifact, and adjacent storyboard suite reproducibility is accepted. |
| Release hygiene, cleanup, packaging, handoff hardening | 10 | 10 | Release hygiene report, grouping preview, focused product-proof reproducibility bundle, tracked handoff manifest, first staging candidate decision brief, control-doc staging dry-run, and control-doc staging apply are accepted; commit, push, and final release verification remain separate future decisions. |
| Total | 100 | 80 | Product-deliverable control estimate. |

## Gate Hardening Indicator

Gate hardening completion: `82%`

This secondary indicator records how much local JSON gate and source-binding hardening has been accepted by `03`. It must not be displayed as the project completion percentage unless this file is revised again.

Counted gate-hardening progress through:

- `minimal reviewable storyboard packet product artifact`: PASS by `03`.

## Not Yet Counted For Product Completion

- Image-facing delivery proof after the reviewable storyboard packet artifact.
- Image-facing delivery proof, final verification, actual staging execution, and handoff hardening.
- Any future formal registry decision boundary. This remains not approved until separately planned and reviewed.

## Expert Model Note

`01` asked a ChatGPT expert model to review the blueprint. The expert verdict was `REVISE`: the project is strong at safety boundaries, source binding, forged upstream JSON defense, and candidate-only semantics, but weak at product delivery proof, lifecycle clarity, story movement validation, and progress governance. Lifecycle clarity, progress governance, first-version story movement validation, and the first named reviewable storyboard packet artifact have since been partially addressed.

## Last Control Review

Counted through:

- `pipeline lifecycle map + progress rubric repair`: PASS by `03` on 2026-06-27.
- `candidate_reference_acceptance_packet_validation` boundary closure: PASS by `03` on 2026-06-27.
- `pipeline E2E happy-path fixture v1`: PASS by `03` on 2026-06-27.
- `minimal storyboard story movement audit gate` REVISE P1 repair: PASS by `03` on 2026-06-27.
- `minimal reviewable storyboard packet product artifact`: PASS by `03` on 2026-06-27.
- `minimal release hygiene and local reproducibility report` REVISE P1 repair: PASS by `03` on 2026-06-27.
- `minimal release grouping preview`: PASS by `03` on 2026-06-27.
- `minimal focused reproducibility bundle`: PASS by `03` on 2026-06-27.
- `minimal tracked handoff manifest`: PASS by `03` on 2026-06-27.
- `minimal adjacent storyboard suite reproducibility`: PASS by `03` on 2026-06-27.
- `minimal first staging candidate decision brief`: PASS by `03` on 2026-06-27; not counted as a product-deliverable percentage increase because it is not staging approval or product behavior.
- `minimal control-doc staging candidate dry run`: PASS by `03` on 2026-06-27; not counted as a product-deliverable percentage increase because it did not stage files.
- `minimal control-doc staging apply`: PASS by `03` on 2026-06-27; not counted as a product-deliverable percentage increase because it staged control docs only and did not commit, push, release, or add product behavior.

Display remains:

```text
Project completion progress: 80%
```
