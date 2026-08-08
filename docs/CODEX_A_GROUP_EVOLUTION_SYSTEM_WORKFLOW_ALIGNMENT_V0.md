# Codex A Group Evolution System Workflow Alignment V0

Status: `DRAFT_FOR_REVIEW`

Control artifact: `AGroupEvolutionSystemWorkflowAlignment`

This document is docs/control only. It aligns workflow rules around
`A_GROUP_EVOLUTION_SYSTEM_V0` as the highest-priority planning/control route
before ordinary feature continuation. It does not implement runtime behavior,
schema, provider/API work, media, SQLite writes, renderer/ffmpeg/TTS execution,
approval, canon, registry, release, final approval, or production lock.

## Referenced Control Inputs

This alignment cites:

- `docs/CODEX_A_GROUP_EVOLUTION_SYSTEM_V0.md`
- `docs/CODEX_WHOLE_PROJECT_BLUEPRINT_V0.md`
- `docs/CODEX_BLUEPRINT_EXECUTION_CONTROL_INDEX_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`
- `docs/CODEX_DEFAULT_ROUTE_DECISION_RULES_V0.md`
- `docs/CODEX_ROUTE_STATUS_TAGS_V0.md`
- `docs/CODEX_WINDOW_TITLE_AND_SINGLE_LANE_ALIGNMENT_V0.md`
- `docs/CODEX_PRODUCT_ARCHITECTURE_THINKING_LOOP_V0.md`

`A_GROUP_EVOLUTION_SYSTEM_V0` remains the highest-priority planning/control
route until the evolution workflow is reviewed and installed. Ordinary feature
continuation, including query/ref, script-ready scene packets,
storyboard/camera, asset lifecycle, voice timeline, renderer readiness, package
QA, Phase 20/21, or 04/05/06/07/08 continuation must wait unless a later
explicit route decision says otherwise.

## Handoff Alignment

The aligned handoff route is:

```text
00 / user symptom or planning-control signal
  -> 00-01 分流接頭
  -> A組01
  -> B組01
  -> B組02 only after one bounded B組01 dispatch
```

Rules:

- `00-01 分流接頭` routes evolution triggers, user-observed stalls, missed
  handoffs, false stops, unclear route authority, and system-strength concerns
  to `A組01`.
- `A組01` owns A-group evolution integration. It may coordinate A組02/A組03/A組04
  or an A-branch when needed, but it must not directly assign B組02 or B組03.
- `A組01` may send only mature `IMPLEMENTATION_PACKAGE` output with
  `AI_TECHNICAL_APPROVED` and `READY_FOR_B組01_DISPATCH` to `B組01`.
- `B組01` is the only B-group dispatch window. `B組01` converts one accepted
  package into exactly one bounded B組02 task, then routes completion to B組03
  when review is needed.
- `B組02` must not accept direct B組02 contact from `A組01` or `00` for this
  route. If direct contact occurs, treat it as a routing gap and return to
  `B組01`.
- B組03 PASS is `REVIEW_PASS_NOT_APPROVAL`: it never grants user approval,
  asset approval, canon, registry, release, final approval, or production lock.

## A-Group Signal Capture

When the evolution route is active, A組 captures four required signals and one
feedback signal:

| Signal | Captured by | Required shape | Workflow use |
| --- | --- | --- | --- |
| `EVOLUTION_EVENT` | `00-01 分流接頭` routes it to `A組01`; A組01 records or delegates intake. | Event id, observed symptom, evidence, expected behavior, actual behavior, impact, repeated-risk level, A02/A03/A04 needs, status. | Opens the evolution loop without starting B組02 implementation. |
| `ROOT_CAUSE_ANALYSIS` | A組03 or an A組01-designated reviewer pressure-tests the event. | Primary cause, contributing causes, missing rule, missing owner, missing regression check, what must not be blamed, status. | Prevents shallow blame and false stop conditions. |
| `RULE_UPGRADE_PROPOSAL` | A組02/A組04 produce or synthesize the rule change. | Rule to add/change, affected files or prompts, stronger behavior, regression check, forbidden side effects, owner, status. | Converts the event into reusable workflow change instead of one-off patching. |
| `REGRESSION_CHECK` | A組04/A組01 attach the future detector before closure. | Future symptom, required routing behavior, owner window, pass condition, fail condition. | Makes the same failure detectable in future windows. |
| Blueprint feedback | A組01 integrates accepted changes into blueprint/control docs or a B組01 package. | Accepted rule upgrade, blueprint/control docs to update, dispatch implications, preserved regression check. | Ensures lessons feed back into the blueprint and B-group dispatch path. |

## Status Tags And Routing Gaps

Existing reviewed/control docs already cover the primary route order and owner
chain. The following status-tag gaps are present but non-blocking for this
workflow alignment:

- `EVOLUTION_EVENT_OPEN`
- `ROOT_CAUSE_ANALYSIS_ANALYZED`
- `RULE_UPGRADE_PROPOSAL_READY_FOR_A01_INTEGRATION`
- `REGRESSION_CHECK_RECORDED`
- `EVOLUTION_WORKFLOW_ALIGNED`

These tags are not currently listed in `docs/CODEX_ROUTE_STATUS_TAGS_V0.md`.
The gap does not block this alignment because `docs/CODEX_A_GROUP_EVOLUTION_SYSTEM_V0.md`
already defines the required output shapes and statuses. A future A-branch may
formalize the tags if A組01 decides the route needs a dedicated evolution
lifecycle tag table.

Routing gaps identified:

- Current route and blueprint docs already state that `A_GROUP_EVOLUTION_SYSTEM_V0`
  is highest-priority before ordinary feature continuation.
- Current handoff docs already support `00-01 分流接頭 -> A組01 -> B組01`.
- The execution control index did not yet point to this workflow-alignment
  surface; this slice adds the pointer.
- No blocking routing gap is found for this bounded docs/control alignment.

## Outcome Labels

Use exactly one outcome:

- `aligned`
- `needs A-branch`
- `blocked`

Selected outcome: `aligned`.

Rationale: the route, owner chain, trigger shapes, root-cause pathway,
rule-upgrade pathway, regression-check requirement, and B組01-only dispatch
boundary already exist in reviewed/control docs. Missing evolution-specific
status tags are a non-blocking follow-up gap, not a true stop condition and not
enough to require an A-branch in this slice.

## Downstream Allowance

On B組03 PASS, B組01/A組01/00 may use this alignment to classify future
docs/control evolution-system tasks. PASS must not auto-start implementation,
runtime/schema/provider/API/media/SQLite work, renderer/ffmpeg/TTS execution,
Phase 20/21 old route restart, 04/05/06/07/08 restart, approval, canon,
registry, release, final approval, or production lock.

## Forbidden Claims

Reject these claims:

- implementation/runtime/schema/provider/API/media/SQLite/renderer/ffmpeg/TTS
  work is authorized;
- Phase 20/21 old route restart is authorized;
- 04/05/06/07/08 restart is authorized;
- A組01 or 00 may contact B組02 directly for implementation or review;
- B組02 may accept direct B組02 contact from A組01/00;
- B組03 PASS, technical PASS, or `REVIEW_PASS_NOT_APPROVAL` is approval;
- canon, registry, release, final approval, or production lock exists.

## Boundary Summary

- docs/control only
- A-group evolution workflow alignment only
- `A_GROUP_EVOLUTION_SYSTEM_V0` remains highest-priority before ordinary
  feature continuation
- `00-01 分流接頭 -> A組01 -> B組01` is the aligned handoff route
- A組 captures `EVOLUTION_EVENT`, `ROOT_CAUSE_ANALYSIS`,
  `RULE_UPGRADE_PROPOSAL`, `REGRESSION_CHECK`, and blueprint feedback
- missing evolution-specific status tags are non-blocking gaps
- selected outcome is aligned
- no implementation/runtime/schema/provider/API/media/SQLite/renderer/
  ffmpeg/TTS work
- no Phase 20/21 old route restart
- no 04/05/06/07/08 restart
- no direct B組02 contact from A組01/00
- no approval, canon, registry, release, final approval, or production lock
