# Project Progress Rubric

Date: 2026-06-27

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

This rubric separates two numbers that were previously mixed together.

- Product deliverable completion: the user-facing project completion shown by `01`.
- Gate hardening completion: how much local JSON validation and review boundary work has been hardened.

## Current Numbers

| Metric | Current | Displayed by 01? | Meaning |
| --- | ---: | --- | --- |
| Product deliverable completion | 58% | Yes | Conservative estimate of progress toward a usable novel-to-reviewable-storyboard pipeline and later media deliverables. |
| Gate hardening completion | 79% | No | Internal signal that many JSON gates have been implemented and reviewed, but they are not the final product. |

`01` should display:

```text
Project completion progress: 58%
```

The previous 79% should be treated as `gate hardening completion`, not project completion. It may be mentioned in control notes, but it should not replace the displayed product number unless this rubric is revised and the table supports it.

## Why The Old 79% Was Repaired

The old `docs/PROJECT_COMPLETION_PROGRESS.md` table displayed `Project completion progress: 79%`, but its detail rows were:

```text
20 + 20 + 15 + 20 + 15 + 3 = 93
```

That made the control estimate internally inconsistent. The expert model also questioned 79% because if the nearest product target is a reviewable storyboard packet, 79% can overstate deliverable readiness.

## Product Deliverable Completion Table

This is the source of truth for the displayed project progress.

| Product deliverable area | Weight | Current | Advance rule |
| --- | ---: | ---: | --- |
| Stable local engine foundation | 20 | 20 | Already counted. Do not increase. |
| Novelization intake / quality / revision gates | 15 | 13 | Add up to 2 only after a real chapter fixture proves the revision chain improves prose quality without continuity damage. |
| Accepted draft to script packet gates | 10 | 8 | Add up to 2 after E2E fixture proves source-bound accepted draft to script packet flow. |
| Reviewable storyboard packet path | 20 | 9 | Add only after `candidate_reference_acceptance_packet_validation` and E2E happy-path fixture close the reviewable storyboard boundary. |
| Candidate asset / image reference preparation | 15 | 6 | Add only for candidate-only validation closure and reviewed local references; no asset approval, no registry write. |
| Comic / video / audio production path | 15 | 0 | Add only after explicit future approval for generation/audio/video work. |
| Release hygiene, cleanup, packaging, handoff hardening | 5 | 2 | Add after cleanup, package documentation, and reproducible verification. |
| Total | 100 | 58 | Must always equal the displayed product progress. |

## Gate Hardening Completion Table

This number is separate from product deliverable completion.

| Gate hardening area | Weight | Current | Notes |
| --- | ---: | ---: | --- |
| Novelization and prose quality gates | 20 | 18 | Strong local gate coverage, still needs fixture proof. |
| Accepted draft to script packet gates | 15 | 13 | Mostly hardened through source-binding and structure bridge. |
| Storyboard planning/review gates | 20 | 16 | Strong review and human-decision boundaries, still no final fixture. |
| Asset/image/candidate-reference gates | 25 | 22 | Many candidate-only and forbidden-wording guards passed 02/03 review. |
| Progress/control governance | 10 | 4 | This repair starts governance, but it still needs expert and 03 review. |
| Release hygiene | 10 | 6 | Handoff discipline exists, final cleanup not done. |
| Total | 100 | 79 | Internal gate hardening indicator only. |

## 01 Display Policy

After every `03` PASS / REVISE / FAIL:

- Always display `Project completion progress: NN%`.
- Use the product deliverable completion table for NN.
- For `REVISE` or `FAIL`, do not increase NN.
- If a slice only adds guards, reports, or review boundaries, it may increase gate hardening notes but should not automatically increase product deliverable completion.
- If a future slice changes the target from reviewable storyboard packet to full comic/video/audio delivery, revise this rubric before changing NN.

## Next Recommended Progress Events

1. Docs/control repair PASS by expert and 03: governance can be trusted again, but product progress should remain 58 unless 01 explicitly counts governance under release hygiene.
2. `candidate_reference_acceptance_packet_validation`: may add product progress only if it closes the candidate reference acceptance boundary without creating formal registry write.
3. E2E happy-path fixture from accepted draft to reviewable storyboard packet: likely product-progress event.
4. Read-only story movement audit: likely product-progress event if it catches stagnant or AI-like story movement before reviewable output.
5. Release hygiene: can add final release-hygiene points when cleanup and reproducible verification are complete.
