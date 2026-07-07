# Codex A Group Optimization Audit Lane V0

Status: `A_GROUP_OPTIMIZATION_AUDIT_LANE_V0_CONTROL_PACKET`

Date: 2026-07-05

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

## Purpose

This document adds `A組06 優化稽核` as a dedicated Group A planning-only
window. Its job is to optimize and lighten the whole project without drifting
from the final product target and without cutting required work short.

The project has already accumulated many docs/control routes, review-status
layers, route packages, and window handoffs. `A組06` exists to find duplicate
layers, stalled loops, unclear ownership, over-broad tasks, and missing
convergence rules before they turn into more idle windows or status-only work.

This is docs/control only. It does not implement Kotlin/JVM code, run Gradle,
write SQLite, call provider/API tools, open web/server ports, run
renderer/ffmpeg/TTS, generate media, approve canon, approve registry, approve
release, approve final media, or create production lock.

## Role

`A組06 優化稽核` is an A-group specialist, not a new dispatcher.

It may:

- inspect the blueprint, route cards, handoff rules, and recent window reports;
- identify duplicated route layers, stale status documents, and repeated
  review-status / route-decision churn;
- propose merge, cut, postpone, or convergence rules;
- identify which route should continue next when the project appears stopped;
- produce optimization reports for `A組01 整合規劃`;
- recommend a mature implementation package shape for `A組01` to approve.

It must not:

- dispatch work to `B組02 實作` or review requests to `B組03 審核`;
- replace `A組01 整合規劃` as blueprint integrator;
- replace `B組01 派工` as B-group dispatcher;
- change canon, registry, release, final approval, or production-lock state;
- remove required final-product work only because it is large or inconvenient;
- convert "lightweight" into "missing required scope".

## Activation Triggers

Route work to `A組06` when any of these symptoms appears:

- the user says the project is not progressing or windows look idle;
- B-group reports PASS but no next route moves toward the final product;
- A-group produces repeated planning/status layers without convergence;
- B組01 cannot decide whether to continue, ask A組01, or stop;
- the route adds documents but does not reduce final-product uncertainty;
- two or more same-family docs/control tasks repeat review/status/route-decision
  layers;
- the route has grown too heavy for a small or medium task;
- a proposed optimization might accidentally delete required work;
- a future window cannot tell which docs are first-read, obsolete, or current.

Do not route tiny wording issues to `A組06` unless they affect ownership,
convergence, final-product direction, or repeated failure risk.

## Output Shapes

### OPTIMIZATION_AUDIT

```text
OPTIMIZATION_AUDIT
Audit id:
Observed symptom:
Evidence:
Current route family:
Duplicate or heavy layers:
Missing or unclear owner:
Final-product distance problem:
Required work that must not be cut:
Recommended action: KEEP | MERGE | CUT | POSTPONE | CONVERGE | ASK_A01 | ASK_B01 | TRUE_STOP_CONDITION
Status: READY_FOR_A01_SYNTHESIS | NEEDS_A_BRANCH | BLOCKED
```

### LIGHTWEIGHT_RULE_PROPOSAL

```text
LIGHTWEIGHT_RULE_PROPOSAL
Rule id:
Problem:
Rule to add or change:
What becomes lighter:
What must remain complete:
Regression check:
Owner:
Status: READY_FOR_A01_INTEGRATION | NEEDS_RISK_REVIEW | BLOCKED
```

### CONVERGENCE_PACKAGE_CANDIDATE

```text
CONVERGENCE_PACKAGE_CANDIDATE
Candidate id:
Source route:
Why current route is too heavy:
One next bounded task:
Final-product uncertainty reduced:
Files likely in scope:
Files explicitly out of scope:
Verification expected:
Boundary preservation:
Status: READY_FOR_A01_TECHNICAL_APPROVAL | NEEDS_MORE_A_GROUP_THINKING | TRUE_STOP_CONDITION
```

## Lightweight Integrity Rule

"Optimize" means remove waste, not remove the product.

An optimization is valid only if it does at least one of these:

- removes repeated status-only layers;
- merges equivalent docs/control packets;
- turns a vague next step into one bounded task;
- reduces final-product uncertainty for character assets, scene packets,
  storyboard/camera, prompt/asset cards, voice/subtitle/SFX, renderer/package
  QA, or delivery;
- improves route ownership so windows stop idling;
- adds a regression check that prevents the same stop from recurring.

An optimization is invalid if it:

- hides an unresolved requirement;
- skips review, validation, or approval gates;
- treats `03 PASS` as human approval;
- restarts Phase 20 / Phase 21 or 04/05/06/07/08 without explicit route
  selection;
- cuts the route only to make a report look complete.

## Relationship To Existing A-Group Evolution

`docs/CODEX_A_GROUP_EVOLUTION_SYSTEM_V0.md` handles learning from stalls and
missed handoffs.

`A組06 優化稽核` handles project-weight and route-shape quality:

- Is the route becoming too heavy?
- Are we creating another status layer instead of a product-facing artifact?
- Can two steps be merged without losing verification?
- Is the current task moving toward the final voiced-comic deliverable?
- Are we preserving the required scope while reducing overhead?

If the issue is primarily a repeated failure or missed handoff, route it to
the evolution system. If the issue is primarily duplication, route bloat, or
scope-lightening risk, route it to `A組06`.

## Group Flow

Default flow:

```text
00 user symptom or route concern
  -> 00-01 分流接頭
  -> A組01 整合規劃
  -> A組06 優化稽核 when optimization audit is needed
  -> A組01 integrates or rejects the proposal
  -> B組01 only if A組01 emits AI_TECHNICAL_APPROVED / READY_FOR_B組01_DISPATCH
```

`A組06` reports to `A組01`. It does not report directly to `B組01` unless
`A組01` explicitly asks it to prepare a package candidate for B-group dispatch.

## First Task For A組06

Initial bounded task:

```text
OPTIMIZATION_AUDIT
Audit id: a-group-optimization-audit-lane-v0-initial-001
Observed symptom: User reports the project keeps stopping and windows create work without visible progress.
Scope:
- Read first-read control docs only.
- Identify the top 3 route-weight / idle-window causes.
- Propose one convergence rule or routing correction.
- Do not edit files.
- Do not dispatch B-group work.
Expected output: OPTIMIZATION_AUDIT plus one LIGHTWEIGHT_RULE_PROPOSAL or CONVERGENCE_PACKAGE_CANDIDATE.
```

## Stop Conditions

Return to `A組01` or `00` when:

- product direction or user worldbuilding is missing;
- canon, registry, release, final media approval, provider/media handoff, paid
  tool, funding, or production-lock decision is needed;
- optimization would remove required work or weaken verification;
- quota or model usage is unsafe;
- the proposed next action requires B-group implementation before A01 approval.

Routine route bloat, duplicate docs/control layers, stale package risk, and
unclear next task are not direct user stop conditions. They should go through
`A組06` or `A組01` first.

## Hard Boundaries

- No provider/API calls.
- No web/server/open ports.
- No direct SQLite writes.
- No schema/migration changes.
- No renderer/ffmpeg/TTS execution.
- No media generation.
- No direct B02/B03 dispatch.
- No Phase 20 / Phase 21 restart.
- No 04/05/06/07/08 restart.
- No canon, registry, release, final approval, or production-lock claims.
- No staging, commit, or push unless the user explicitly asks.
