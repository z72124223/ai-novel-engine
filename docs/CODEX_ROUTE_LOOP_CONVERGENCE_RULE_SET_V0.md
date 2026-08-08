# Route Loop Convergence Rule Set V0

Status: `DRAFT_FOR_REVIEW`

Control artifact:
`RouteLoopConvergenceRuleSet`

This document is docs/control only. It installs a process-rule patch for
route-loop convergence so A/B groups stop producing endless status, review,
and route-decision documents when those documents no longer reduce final
product uncertainty. It does not create actual query instances, no actual
instances, no actual `QueryRequest`, `QueryResponse`, or
`QueryScopeReviewReport`, no `ScriptReadyScenePacket`, no sample JSON, no
story/source/query content, no concrete refs, no source-basis selection, no
evidence bundle, no Kotlin/JVM, no CLI, no runtime, no schema, no SQLite, no
provider/API, no web/server, no renderer/ffmpeg/TTS, no media generation, no
04/05/06/07/08 restart, no Phase 20/21 restart, no approval, no canon, no
registry, no release, no final media approval, and no production lock.

## Upstream Planning/Control Evidence

- A02 produced the P1 seed for route-loop convergence.
- A03 returned `SAFE_WITH_GUARDS`.
- A04 returned `PROCESS_RULE_PATCH_READY`.
- A01 approved one bounded docs/control implementation package with
  `AI_TECHNICAL_APPROVED` and `READY_FOR_B01_DISPATCH`.
- A01 checked current docs for duplicate route-loop convergence markers and
  found none before approving this package.
- Local pre-edit evidence found no existing `Route Loop Convergence Rule Set`,
  `Artifact-Shape-Ready Gate`, `Final-Product Distance Gate`,
  `No-New-Status-Doc Gate`, `Bundle Review Gate`, or
  `2 adjacent docs/control PASSes` marker.

B03 PASS, 03 PASS, `QA_PASS_NOT_APPROVAL`, and `REVIEW_PASS_NOT_APPROVAL` are
technical review states only. They are not user approval, asset approval,
registry approval, canon, release, final media approval, provider/media
handoff, final approval, or production lock.

## Rule Set Purpose

The rule set closes a governance gap: repeated docs/control status layers can
be technically safe while still moving the final product no closer to generated
video or voiced-comic delivery. A01 remains allowed to form and approve mature
implementation packages, and B01 remains the only B-group dispatch window for
B02/B03 work. The patch changes what A01 may approve after repeated adjacent
status/review/route-decision PASSes in the same family.

Selected process-rule outcome:

`ROUTE_LOOP_CONVERGENCE_PROCESS_RULE_PATCH_ALIGNED`

No blocker is selected. No A-branch is required by the reviewed evidence
because A02/A03/A04 already completed convergence and A04 returned
`PROCESS_RULE_PATCH_READY`.

## 2 Adjacent Docs/Control PASSes Stop Rule

After `2 adjacent docs/control PASSes` in the same route family that only
produce status, review, review-status, route-decision, or route-package layers,
A01 must stop approving another status slice in that family.

Instead A01 must choose one of these convergence paths:

- form a bundle surface that groups the unresolved slots and required final
  product chain tags into one reviewable docs/control artifact;
- form a process/rule patch when the loop itself is the root cause;
- route to A-branch classification if the route still lacks authority,
  ownership, safety classification, or final-product distance evidence;
- stop with an explicit blocker only when reviewed facts prove user input,
  boundary blocking, or a true stop condition.

The stop rule does not erase safety reminders. Route cards and dispatches must
still preserve non-goals, boundaries, and review gates. The rule only prevents
another narrow status document from being approved when the next safe move
should be convergence.

## A06 Same-Family Status-Layer Convergence Rule

Rule marker: `a06-no-new-same-family-status-layer-without-bundle-v0`

Selected same-family/status-layer convergence process-rule outcome:

`SAME_FAMILY_STATUS_LAYER_CONVERGENCE_RULE_PATCH_ALIGNED`

A06 lightweight rule: when a route family reaches the 3rd adjacent same-family
layer, A01 must not approve another standalone layer in that family. Same-family
layer types are status, review, review-status, route-decision, route-package,
package-review, and curation.

At the 3rd adjacent same-family layer, A01 must choose exactly one convergence
outcome:

1. final-product-chain-tagged bounded package candidate;
2. explicit blocker / `TRUE_STOP_CONDITION`;
3. process/rule patch with regression check.

Every next task must name the final-product chain it reduces: scene packet,
storyboard/camera, prompt/asset cards, voice/subtitle/SFX, renderer/package QA,
or human approval gates. A task that cannot name one of those chains must not
continue as another same-family standalone status layer.

Regression check:

- count adjacent same-family layer types before approving the next package;
- verify whether a convergence bundle already exists for unresolved slots;
- verify whether the proposed task is a bounded package candidate, explicit
  blocker / `TRUE_STOP_CONDITION`, or process/rule patch;
- verify the task names a final-product chain and explains uncertainty
  reduction;
- verify review gates, `DRAFT_FOR_REVIEW`, and `REVIEW_PASS_NOT_APPROVAL` stay
  explicit;
- verify the route still has no provider/API, no SQLite, no renderer/ffmpeg/TTS,
  no media generation, no approval, no canon, no registry, no release, no final
  approval, and no production lock.

This rule preserves A01 -> B01 -> B02/B03 authority separation. A01 may form and
approve mature packages, but B01 remains the only B-group dispatch authority.
A-group windows must not dispatch directly to B02/B03.

## Required Final-Product Chain Tags

Every proposed task must declare at least one final-product chain tag and state
which uncertainty it reduces:

- `FINAL_PRODUCT_CHAIN_CHARACTER_ASSETS`: character designs, model sheets,
  wardrobe, expression range, item/prop ownership, or asset identity.
- `FINAL_PRODUCT_CHAIN_SCENE`: source scene, beat, zone, continuity,
  visibility, setting constraints, or scene-level dependency.
- `FINAL_PRODUCT_CHAIN_STORYBOARD_CAMERA`: shot list, framing, camera move,
  storyboard beat, panel sequencing, or review-only camera planning.
- `FINAL_PRODUCT_CHAIN_PROMPT_ASSET_CARD`: prompt-safe asset note,
  negative-prompt boundary, reusable prompt/asset card shape, or protected
  prompt-surface eligibility.
- `FINAL_PRODUCT_CHAIN_VOICE_SUBTITLE_SFX`: voice planning, subtitle timing,
  SFX cue, audio visibility, or non-runtime sound planning.
- `FINAL_PRODUCT_CHAIN_RENDERER_PACKAGE_QA`: renderer readiness, package QA,
  delivery review shape, local artifact check, or final package boundary.

If a proposed task cannot name one of these tags, it must not proceed as
another status/review/route-decision slice. It must converge into a bundle,
rule patch, A-branch classification, or explicit blocker.

## Artifact-Shape-Ready Gate

The `Artifact-Shape-Ready Gate` asks whether the route has enough reviewed
docs/control structure to define the next artifact's shape without creating
the actual artifact.

Allowed shape-only outputs:

- named rows, slots, fields, citations, visibility classes, prompt-surface
  eligibility, protected-boundary references, review gates, and blocker
  labels;
- a bundle surface that groups unresolved items for one review;
- a rule patch that changes routing behavior.

Forbidden outputs:

- actual instances;
- actual query instances;
- actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`;
- actual `ScriptReadyScenePacket`;
- concrete refs;
- source-basis selection;
- evidence bundle creation;
- sample JSON;
- runtime/schema/provider/API/media work.

If the artifact shape is ready, A01 should prefer a bundle or artifact-shape
package over another status document. If the shape is not ready, A01 must name
the missing reviewed structure and route to A-branch classification or a rule
patch instead of approving more status churn.

## Final-Product Distance Gate

The `Final-Product Distance Gate` requires every proposed task to state how it
reduces uncertainty toward generated video or voiced-comic delivery.

Valid uncertainty reduction examples:

- narrowing which character asset shape is needed;
- tying scene/beat/zone constraints to a reviewable bundle;
- defining storyboard/camera planning shape;
- defining prompt-safe asset card boundaries;
- defining voice/subtitle/SFX planning shape;
- reducing renderer/package QA readiness uncertainty.

Invalid distance claims:

- "another status layer exists";
- "review PASS can be recorded" without changing artifact shape;
- "route-decision can name another route-decision";
- "non-approval boundaries are repeated" without bundling or reducing a final
  product chain uncertainty.

This gate does not skip review and does not authorize runtime/provider/API,
renderer, ffmpeg, TTS, media generation, release, or production lock.

## No-New-Status-Doc Gate

The `No-New-Status-Doc Gate` activates after the adjacent PASS stop rule or
whenever A01 sees a same-family chain whose latest proposed task only adds
status/review/review-status/route-decision metadata.

When active, A01 must not approve a new status-only docs/control task unless
one of these exceptions is explicitly supported by reviewed evidence:

- it closes a newly discovered boundary blocker;
- it records a convergence bundle review, not another route-status layer;
- it installs or amends a process rule;
- it records a true stop condition with exact blocker evidence.

Safety reminders remain mandatory. Route cards and rule documents may continue
to restate no actual instances, no sample JSON, no provider/API, no SQLite, no
renderer/ffmpeg/TTS, no media generation, and no production lock. Those
reminders must not be used as justification for another status-only slice.

## Bundle Review Gate

The `Bundle Review Gate` defines convergence review, not approval.

A convergence bundle review may verify:

- whether the bundle includes required final-product chain tags;
- whether artifact shape is ready at docs/control level;
- whether every unresolved slot has a citation or blocker;
- whether visibility, `allowedPromptSurface`, `protectedBoundaryRef`,
  `boundaryCategory`, and review gates are preserved;
- whether the bundle still avoids actual instances, concrete refs, runtime
  work, provider/API work, media generation, approval, canon, release, and
  production lock.

The bundle review must not approve assets, story content, source basis, query
execution, evidence bundles, media, canon, release, or final product readiness.

## Authority Separation

- A01 forms and approves mature docs/control implementation packages.
- B01 dispatches B02 implementation and B03 review.
- B02 implements only the single bounded dispatched package.
- B03 reviews only when B01 dispatches review.
- A01/00 must not directly contact B02 or B03 for implementation or review.

This separation preserves the routing chain:

```text
00 / 00-01 -> A01 package formation -> B01 dispatch -> B02 implementation -> B01 -> B03 review
```

## Missing Tags And Routing Gaps Closed

This patch identifies and closes these process gaps:

- no explicit stop rule for adjacent docs/control PASS chains that only add
  status/review/route-decision layers;
- no required final-product chain tags on every proposed task;
- no artifact-shape-ready test separating useful bundle work from status churn;
- no final-product distance test for generated video or voiced-comic delivery;
- no no-new-status-doc gate after repeated same-family PASSes;
- no bundle review gate that names convergence review as non-approval.

No unresolved missing status tag remains for this patch. Future gaps should be
handled through A01 classification, not by silently adding more status slices.

## Downstream Allowance

On B03 PASS, B01/A01/00 may use this rule set to classify future docs/control
packages and reject repeated same-family status churn. PASS must not auto-start
actual query instance creation, query execution, concrete refs, source-basis
selection, evidence bundle creation, actual packet work, sample JSON, story
content, runtime/schema/provider/API/media work, provider/API work, web/server
work, direct SQLite writes, 04/05/06/07/08 route, renderer/ffmpeg/TTS
execution, approval, canon, registry, release, final approval, or production
lock.

## Boundary Summary

- docs/control only
- Route Loop Convergence Rule Set only
- selected process-rule outcome is aligned
- no actual instances
- no actual query instances
- no actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
- no `ScriptReadyScenePacket`
- no sample JSON
- no story/source/query content
- no concrete refs
- no source-basis selection
- no evidence bundle
- no Kotlin/JVM, CLI, runtime, schema, or SQLite
- no provider/API
- no web/server
- no renderer/ffmpeg/TTS
- no media generation
- no 04/05/06/07/08 restart
- no Phase 20/21 restart
- no approval, canon, registry, release, final media approval, or production
  lock
