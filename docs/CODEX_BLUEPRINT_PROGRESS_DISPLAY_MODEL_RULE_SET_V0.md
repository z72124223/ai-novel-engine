# Blueprint Progress Display Model Rule Set V0

Status: `BLUEPRINT_PROGRESS_DISPLAY_MODEL_RULE_SET_V0_DRAFT_FOR_REVIEW`

Date: 2026-07-04

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

## Purpose

This docs/control rule set prevents local progress percentages from becoming
false whole-project or final-product completion claims. It preserves the old
local baseline display while making every percentage line name its metric
object, scope, evidence range, non-meaning, and change gate.

Selected process-rule outcome:
`BLUEPRINT_PROGRESS_DISPLAY_MODEL_PROCESS_RULE_PATCH_ALIGNED`

## Upstream Planning/Control Evidence

- A02 completed `P1_BLUEPRINT_PROGRESS_DISPLAY_MODEL_SEED`.
- A03 completed `P2_BLUEPRINT_PROGRESS_DISPLAY_MODEL_RISK_REVIEW` with verdict
  `SAFE_WITH_GUARDS`.
- A04 completed `P3_BLUEPRINT_PROGRESS_DISPLAY_MODEL_SYNTHESIS` with decision
  `PROCESS_RULE_PATCH_READY` and recommended next owner
  `A01_APPROVE_PACKAGE`.
- A01 approved the bounded B01-dispatched implementation package
  `blueprint_progress_display_model_process_rule_patch_v0_docs_control`.

## Root Cause Being Patched

The legacy local dispatch-chain and local-baseline percentages can be misread
as whole-project completion. The old `Local baseline progress: 100%` remains a truthful old local baseline only with same-line firewall language: not final media approval, not release, not production lock, not canon, not registry, not video completion, and not true product completion.

## No Naked Percentage

The `No Naked Percentage` rule applies to every future percentage line in
blueprint, route, handoff, or progress-control surfaces. A percentage line is
not valid unless the same line, or a directly attached table cell in the same
row, states all of these:

- metric object: what is being measured;
- scope: local baseline, local evidence, chain-specific capability, or other
  bounded control surface;
- evidence range: what reviewed evidence the value is counted through;
- non-meaning: what the value does not approve, complete, or unlock;
- change gate: what accepted capability movement is required before it can
  change.

If the line cannot carry those fields, use a non-numeric status instead.

## Default Progress Display

Every current route card, project progress note, or B01-facing status display
that needs the old local baseline must use this two-line shape:

```text
Local baseline progress: 100% -- old local baseline only; not final product; not final media approval; not release; not production lock; not canon; not registry; not video completion; not true product completion; change gate: accepted final-product capability movement only.
True final product progress: not final / model under review
```

The `True final product progress: not final / model under review` line must
remain non-numeric until a future accepted progress model explicitly replaces
this rule. Do not infer a number from docs/control PASS, B03 PASS, A-group
synthesis PASS, review-status movement, route-decision movement, route-package
movement, or future-candidate naming.

## Dispatch-Chain Metric Narrowing

Any old local dispatch-chain count or percentage must be renamed or framed as:

`B組01 dispatch-chain local evidence metric`

This metric is `local fallback only / not global blueprint progress`. It may
show that a B01-dispatched local evidence chain was completed, but it does not
measure generated video readiness, 有聲漫畫 readiness, final media approval,
release, production lock, canon, registry, or true product completion.

## Percent Change Gate

The `Percent Change Gate` blocks progress inflation. A percentage may change
only after accepted final-product capability movement in at least one of these
chains:

- world/foundation;
- query/source scope;
- scene packet;
- storyboard/camera;
- asset cards;
- voice/subtitle/SFX;
- local render package;
- human approval gates.

Docs/control PASS, B03 PASS, A-group synthesis PASS, route status update,
future-candidate docs, non-approval review, review-status classification, and
route-decision classification do not change true final product progress by
themselves.

## Completion Claim Firewall

The `Completion Claim Firewall` says any `100%` line must state on the same line that it is not final media approval, not release, not production lock, not canon, not registry, not video completion, and not true product completion. If same-line firewall language is missing, the line must be
treated as invalid and routed back for docs/control correction before it is
shown as a progress claim.

## True Final Product Progress No-Numeric Rule

`True final product progress` remains `not final / model under review` with no
numeric estimate. A future accepted progress model may change this only if it:

- names the final-product metric object;
- defines chain weights or a non-percentage alternative;
- cites accepted evidence for each counted capability;
- preserves approval, canon, registry, release, final media approval, and
  production lock boundaries;
- passes the Percent Change Gate and Completion Claim Firewall.

## Authority Separation

- A01 forms and approves process-rule packages.
- B01 is the single-line dispatch controller for B02/B03.
- B02 implements only the current bounded B01-dispatched package.
- B03 reviews only when B01 dispatches review.
- A-group windows must not dispatch directly to B02 or B03.

## Regression Guards

- no naked percentage guard: any line with a percent must name metric object,
  scope, evidence range, non-meaning, and change gate.
- local-baseline guard: `Local baseline progress: 100%` must carry same-line firewall language: not final media approval, not release, not production lock, not canon, not registry, not video completion, and not true product completion.
- true-final-product guard: true final product progress remains non-numeric
  until accepted model update.
- dispatch-chain guard: B01 dispatch-chain metrics are local fallback only and
  not global blueprint progress.
- PASS-churn guard: docs/control PASS or review/status movement does not
  raise true product progress.
- authority guard: A01/B01 separation remains intact; B01 dispatches B02/B03.

## Missing Tags And Routing Gaps Closed

This patch closes these gaps:

- no explicit No Naked Percentage rule;
- no same-line firewall requirement for old local baseline `100%` lines that say not final media approval, not release, not production lock, not canon, not registry, not video completion, and not true product completion;
- no named non-numeric true final product progress rule;
- no narrowed dispatch-chain metric name;
- no Percent Change Gate for product-capability movement;
- no Completion Claim Firewall for `100%` lines that say not final media approval, not release, not production lock, not canon, not registry, not video completion, and not true product completion;
- no explicit guard against PASS/status churn being treated as product
  capability progress.

No unresolved missing status tag remains for this process-rule patch. Future
progress changes must be routed through an accepted model update, not through
status/review/PASS churn.

## Boundary Summary

- docs/control only
- no new numeric estimate for true final product progress
- no sample JSON
- no actual query instances
- no `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
- no `ScriptReadyScenePacket`
- no evidence bundle
- no source/story/query content
- no concrete refs
- no source-basis selection
- no Kotlin/JVM code
- no CLI/runtime/schema/validator implementation
- no SQLite writes
- no provider/API
- no web/server
- no renderer/ffmpeg/TTS
- no media generation
- no 04/05/06/07/08 restart
- no Phase 20/21 restart
- no approval, canon, registry, release, final media approval, or production
  lock
