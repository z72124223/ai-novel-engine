# Codex True Project Completion Progress Model V0

Status: `TRUE_PROJECT_COMPLETION_PROGRESS_MODEL_V0_DRAFT_FOR_REVIEW`

Date: 2026-07-02

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

## Purpose

This model separates the historical local-baseline display from true final
project completion.

The old `Project completion progress: 100%` remains valid only for the selected current-phase local product-delivery baseline when same-line firewall language is present: not final media approval, not release, not production lock, not canon, not registry, not video completion, and not true product completion. It must not be used as the final project completion metric for the voiced-comic product.

Use `docs/CODEX_BLUEPRINT_PROGRESS_DISPLAY_MODEL_RULE_SET_V0.md` as the
controlling display rule set for `No Naked Percentage`, `Percent Change Gate`,
`Completion Claim Firewall`, and non-numeric true final product progress.

## Two Progress Numbers

| Metric | Meaning | Current value |
| --- | --- | --- |
| Local baseline progress | Historical selected local product-delivery baseline. | 100% -- old local baseline only; not final product; not final media approval; not release; not production lock; not canon; not registry; not video completion; not true product completion; change gate: accepted final-product capability movement only. |
| True final product progress | End-to-end voiced-comic product readiness. | not final / model under review; no numeric estimate before accepted model update. |

## True Final Product Rubric

| Area | Weight | Current status |
| --- | ---: | --- |
| Whole-project blueprint and governance | 10 | Drafted, needs 03 acceptance. |
| User / 00 input and foundation brief | 10 | Questionnaire exists; foundation brief not complete. |
| 09 foundation cards | 10 | Candidate pool exists; card organization not approved. |
| Demand-driven query / retrieval templates | 10 | Blueprint exists; templates and review reports not active. |
| Script-ready scene packets and 04/05/06/07/08 | 10 | Contracts exist; flow paused. |
| Storyboard / camera packets | 10 | Planning exists; deterministic packet chain not complete. |
| Asset lifecycle / registry | 10 | Planning exists; approval/storage decision not made. |
| Voice timeline / subtitle / SFX | 10 | Referenced; standalone contract not complete. |
| Local renderer execution / media package | 10 | Old Rain Gate local preview exists; final route not executed. |
| Human approval / release / production lock | 10 | Positive approval matrix drafted in this package; no approvals granted. |

## Display Rule

When reporting status to the user:

```text
Local baseline progress: 100% -- old local baseline only; not final product; not final media approval; not release; not production lock; not canon; not registry; not video completion; not true product completion; change gate: accepted final-product capability movement only.
True final product progress: not final / model under review
```

Do not compress both lines into one `100%` claim; not final media approval, not release, not production lock, not canon, not registry, not video completion, and not true product completion. Docs/control PASS, B03 PASS, A-group synthesis PASS, route status update, future-candidate docs, and non-approval review do not change true final product progress.

## Boundary

This document is a planning/control model only. It does not approve release,
provider/media work, canon, registry, final media, production lock, schema,
SQLite writes, renderer execution, or media generation.
