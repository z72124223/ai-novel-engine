# Script-Ready Scene Packet Candidate Query Ref Source Doc Reviewed Input Gap Status Route Decision V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`ScriptReadyScenePacketCandidateQueryRefSourceDocReviewedInputGapStatusRouteDecision`

This document is docs/control only. It records a route-decision surface for the
reviewed-input-gap status chain. It does not create actual `QueryRequest`,
`QueryResponse`, or `QueryScopeReviewReport` instances, does not execute
queries, does not implement a queryer, does not resolve concrete refs, does not
select source basis, does not create evidence bundles, does not create an
actual `ScriptReadyScenePacket`, does not create an actual
`ScriptReadyScenePacketCandidate`, does not generate sample JSON, does not
invent story/source/query content, does not promote Qingli candidates, does not
start runtime/schema/provider/API/media work, does not run renderer/ffmpeg/TTS,
does not grant approval, and does not create production lock.

## Upstream Reviewed Outcome

This route-decision surface follows:
`docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PACKET_REVIEW_STATUS_V0.md`.

Upstream reviewed outcome:
`QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PACKET_REVIEW_STATUS_READY_FOR_REVIEW`.

Upstream route/context labels preserved here:

- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PACKET_REVIEW_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PACKET_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PLAN_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_MATRIX_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_DOC_PLACEHOLDER_PACKET_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_DOC_DEFINITION_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_DOC_GAP_CLOSURE_ROUTE_DEFINED_FOR_FUTURE_SOURCE_DOCS`
- `BLOCKED_BY_MISSING_REVIEWED_QUERY_REF_SOURCE_DOCS`
- `QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_BLOCKED_BY_MISSING_REVIEWED_REFS`
- `NO_REVIEWED_SOURCE_FOUND_AI_PLANNING_GAP`
- `REVIEWED_INPUTS_MISSING_AI_PLANNING_GAP`
- `CLOSURE_PLAN_DEFINED_FOR_AI_RESOLVABLE_GAP`
- `REVIEW_PASS_NOT_APPROVAL`

The upstream status surface carries B03 PASS only as technical
`REVIEW_PASS_NOT_APPROVAL`. It does not equal user approval, asset approval,
registry approval, canon, release, final media approval, provider/media
handoff, final approval, or production lock.

## Referenced Reviewed Inputs

This route-decision surface relies on these reviewed docs/control inputs:

- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PACKET_REVIEW_STATUS_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PACKET_REVIEW_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PACKET_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_CLOSURE_PLAN_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_MATRIX_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_PLACEHOLDER_PACKET_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_DEFINITION_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_SOURCE_DOC_GAP_CLOSURE_ROUTE_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_REVIEWED_REF_DISCOVERY_MATRIX_V0.md`
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`

Citation rule: every route-decision claim must cite exact reviewed
docs/control paths. Templates, placeholders, Qingli-derived candidates, hidden
notes, runtime database inspection, direct SQLite writes, provider/API output,
renderer/ffmpeg/TTS output, and media artifacts are not valid concrete
query-ref source docs.

## Route-Decision Vocabulary

Each source-doc slot must keep exactly one route-decision state:

- `ROUTE_DECISION_AI_DOCS_CONTROL_NEXT_SAFE`: reviewed evidence supports a
  specific next docs/control-only package without resolving concrete refs.
- `ROUTE_DECISION_REQUIRES_A_GROUP_CLASSIFICATION`: route may continue only
  after A-group classification or a future explicit B01/A01/00 route decision.
- `ROUTE_DECISION_REQUIRES_USER_WORLDBUILDING`: reviewed evidence proves the
  next step needs user/worldbuilding input.
- `ROUTE_DECISION_BOUNDARY_BLOCKED`: continuing would cross forbidden
  boundaries such as actual query instance creation, query execution, concrete
  ref resolution, source-basis selection, evidence bundle creation, actual
  packet work, sample JSON, story content, runtime/schema, provider/API,
  renderer/ffmpeg/TTS, media, approval, canon, registry, release, final
  approval, or production lock.
- `ROUTE_DECISION_MISSING_REVIEWED_INPUTS_BLOCKED`: reviewed evidence proves a
  required reviewed input is missing and no safe AI docs/control route is
  available yet.

## Current Route-Decision Outcome

Allowed current route-decision outcome labels are:

- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_STATUS_ROUTE_DECISION_READY_FOR_REVIEW`
- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_STATUS_ROUTE_DECISION_REQUIRES_A_GROUP_CLASSIFICATION`
- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_STATUS_ROUTE_DECISION_BLOCKED_BY_MISSING_REVIEWED_INPUTS`
- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_STATUS_ROUTE_DECISION_BLOCKED_BY_BOUNDARY`
- `QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_STATUS_ROUTE_DECISION_REQUIRES_USER_WORLDBUILDING`

Selected current route-decision outcome:

`QUERY_REF_SOURCE_DOC_REVIEWED_INPUT_GAP_STATUS_ROUTE_DECISION_REQUIRES_A_GROUP_CLASSIFICATION`

Rationale: the status chain is technically reviewed, but all three query-ref
source-doc slots still carry unresolved concrete inputs. The current reviewed
evidence supports a route-decision record, not an automatic next package. A
future continuation should therefore require A-group classification or another
explicit B01/A01/00 route decision before any new docs/control package is
named or dispatched.

No future bounded package candidate is named by this document. The selected
outcome requires A-group classification, so naming a speculative next package
would exceed this route-decision surface.

## Route-Decision Rows

Each source-doc slot appears exactly once below. Rows preserve prior
classification/status/review status and keep unresolved concrete inputs
unresolved.

| Future source-doc slot | Prior classification | Closure-plan status | Review status | Route-decision state | Unresolved concrete inputs |
| --- | --- | --- | --- | --- | --- |
| `QueryRequest` | `REVIEWED_INPUTS_MISSING_AI_PLANNING_GAP` | `CLOSURE_PLAN_DEFINED_FOR_AI_RESOLVABLE_GAP` | `REVIEW_PASS_NOT_APPROVAL` | `ROUTE_DECISION_REQUIRES_A_GROUP_CLASSIFICATION` | Concrete source basis, reviewed citation, prompt eligibility, and review evidence remain unresolved. |
| `QueryResponse` | `REVIEWED_INPUTS_MISSING_AI_PLANNING_GAP` | `CLOSURE_PLAN_DEFINED_FOR_AI_RESOLVABLE_GAP` | `REVIEW_PASS_NOT_APPROVAL` | `ROUTE_DECISION_REQUIRES_A_GROUP_CLASSIFICATION` | Concrete response source linkage, reviewed citation, prompt eligibility, and review evidence remain unresolved. |
| `QueryScopeReviewReport` | `REVIEWED_INPUTS_MISSING_AI_PLANNING_GAP` | `CLOSURE_PLAN_DEFINED_FOR_AI_RESOLVABLE_GAP` | `REVIEW_PASS_NOT_APPROVAL` | `ROUTE_DECISION_REQUIRES_A_GROUP_CLASSIFICATION` | Concrete scope-review evidence, request/response linkage, prompt eligibility, and review evidence remain unresolved. |

No route-decision row above resolves concrete refs. No row upgrades an
AI-resolvable planning gap into user/worldbuilding or boundary blocking
because the reviewed evidence does not prove those alternate classifications.

## Aggregate Route Decision

The aggregate route cannot safely name the next docs/control-only package from
this surface alone. It can continue only through A-group classification or a
future explicit B01/A01/00 route decision that cites reviewed evidence for the
next bounded task.

This is not a user/worldbuilding blocker and not a boundary block. It is a
classification requirement caused by unresolved concrete query-ref source-doc
inputs and by the instruction not to invent a safe path.

## Guardrails Preserved

- `allowedPromptSurface`: fields not explicitly allowed must not enter prompts,
  screen descriptions, negative prompts, prompt-safe asset notes, voice/audio
  planning notes, or media planning notes.
- `protectedBoundaryRef`: hidden causality, culprit identity, sealed motive,
  director goal, future reveal, or unavailable actor knowledge must remain
  protected by reference rather than exposed.
- `boundaryCategory`: use generic categories to describe restricted boundary
  classes without leaking protected facts.
- `DRAFT_FOR_REVIEW`: status before technical review.
- `REVIEW_PASS_NOT_APPROVAL`: technical PASS only, not user approval, asset
  approval, registry approval, canon, release, final media approval,
  provider/media handoff, final approval, or production lock.
- `BLOCKED_BY_HUMAN_DECISION`: stop if a future step requires human/user
  decision before continuing.
- `BLOCKED_BY_BOUNDARY`: stop if a future step would cross actual query
  instance creation, query execution, concrete query-ref resolution,
  source-basis selection, evidence bundle creation, actual packet work, sample
  JSON/story content, runtime/schema, provider/API, renderer/ffmpeg/TTS, media
  generation, approval, canon, registry, release, final approval, or production
  lock.

## Actual Instance Exclusion

This route-decision surface does not create actual `QueryRequest`,
`QueryResponse`, or `QueryScopeReviewReport` instances.

It also does not execute queries, implement a queryer, resolve concrete refs,
select source basis, create evidence bundles, create actual
`ScriptReadyScenePacket` or `ScriptReadyScenePacketCandidate` content, generate
sample JSON, invent story/source/query content, promote Qingli candidates, or
start runtime/schema/provider/API/media work.

## Technical PASS Is Not Approval

`B03 PASS`, `03 PASS`, `QA_PASS_NOT_APPROVAL`, and
`REVIEW_PASS_NOT_APPROVAL` do not equal user approval, asset approval, registry
approval, canon, release, final media approval, provider/media handoff, final
approval, or production lock.

A technical PASS on this route-decision surface does not create `QueryRequest`,
`QueryResponse`, or `QueryScopeReviewReport` instances, does not execute a
query, does not implement a queryer, does not promote Qingli candidates, does
not resolve concrete refs, does not select source basis, does not create an
evidence bundle, does not create an actual `ScriptReadyScenePacket`, does not
create an actual `ScriptReadyScenePacketCandidate`, does not create sample JSON,
does not start story generation, does not start runtime/CLI/schema/validator
work, does not resume 04/05/06/07/08, does not run renderer/ffmpeg/TTS, does
not call provider/API, does not generate media, does not grant approval, does
not create canon, does not create registry approval, does not release, and does
not create production lock.

## Downstream Allowance

On B03 PASS, B01/A01/00 may use this route-decision surface to classify or
dispatch a future explicit docs/control task. PASS must not auto-start actual
query instance creation, query execution, concrete ref resolution,
source-basis selection, evidence bundle creation, actual packet work, sample
JSON, story content, runtime/CLI/schema/validator work, 04/05/06/07/08 route,
renderer/ffmpeg/TTS execution, provider/API work, media generation, approval,
canon, registry, release, final approval, or production lock.

## Boundary Summary

- docs/control only
- route-decision surface only
- no future package candidate named by this document
- no actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
  instances
- no query execution or queryer implementation
- no concrete query-ref resolution
- no source/story/query invention
- no Qingli promotion
- no source-basis selection
- no evidence bundle creation
- no actual `ScriptReadyScenePacket`
- no actual `ScriptReadyScenePacketCandidate`
- no sample JSON
- no runtime/CLI/schema/validator work
- no provider/API
- no web/server
- no direct SQLite writes
- no renderer/ffmpeg/TTS execution
- no media generation
- no 04/05/06/07/08 restart
- no Phase 20/21 restart
- no approval
- no canon
- no registry approval
- no release
- no final approval
- no production lock
