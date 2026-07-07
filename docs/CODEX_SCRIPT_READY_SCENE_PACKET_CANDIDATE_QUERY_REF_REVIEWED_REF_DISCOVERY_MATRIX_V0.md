# Script-Ready Scene Packet Candidate Query Ref Reviewed Ref Discovery Matrix V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`ScriptReadyScenePacketCandidateQueryRefReviewedRefDiscoveryMatrix`

This document is docs/control only. It inventories possible reviewed source
documents for future query-ref evidence. It does not create actual
`QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport` instances, does
not execute queries, does not implement queryers, does not resolve concrete
query refs, does not invent source/story content, does not promote Qingli
candidates, does not select source basis, does not create a reviewed evidence
bundle, does not create an actual `ScriptReadyScenePacket`, does not create an
actual `ScriptReadyScenePacketCandidate`, does not create sample JSON, does not
perform runtime/CLI/schema/validator work, grants no provider/API work, does
not run renderer/ffmpeg/TTS, does not generate media, and does not create
production lock.

## Current Blocker

Current blocker:
`QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_BLOCKED_BY_MISSING_REVIEWED_REFS`.

The latest reviewed placeholder packet says `QueryRequest`, `QueryResponse`,
and `QueryScopeReviewReport` slots remain unresolved because no concrete
reviewed docs/control refs exist.

## Upstream Reviewed Outcomes

This matrix preserves these upstream reviewed/control outcomes:

- `QUERY_REF_EVIDENCE_SURFACE_DEFINED_FOR_FUTURE_REVIEW`
- `QUERY_REF_GAP_AI_RESOLVABLE_DOCS_CONTROL_ROUTE_AVAILABLE`
- `CANDIDATE_EXISTING_SOURCE_REF_NEEDS_REVIEW`
- `NO_EXISTING_SOURCE_REF_FOUND_AI_PLANNING_GAP`

Qingli-derived candidates remain unpromoted and are not sufficient for actual
packet work. Template and placeholder material remains insufficient for
reviewed query-ref selection.

## Classification Vocabulary

Each query-ref slot must have exactly one classification:

- `DISCOVERY_CANDIDATE_NEEDS_REVIEW`: a concrete reviewed-doc path appears to
  contain a candidate query-ref source, but it still needs future review and is
  not selected or promoted by this matrix.
- `NO_REVIEWED_SOURCE_FOUND_AI_PLANNING_GAP`: reviewed docs/control evidence
  contains rules, templates, or placeholders, but no concrete reviewed source
  for the slot.
- `USER_WORLDBUILDING_REQUIRED`: existing reviewed evidence proves new user/00
  worldbuilding is required before this slot can be resolved.
- `BOUNDARY_BLOCKED`: resolving the slot would cross forbidden boundaries such
  as actual query instance creation, query execution, concrete ref resolution,
  source-basis selection, evidence bundle creation, actual packet generation,
  runtime/schema work, provider/API, renderer/ffmpeg/TTS, media generation,
  approval, canon, registry, release, final approval, or production lock.

## Discovery Rules

This matrix may cite exact reviewed-doc paths only when a document contains a
candidate source for a query-ref slot.

Do not infer discovery candidates from:

- placeholders;
- templates;
- samples;
- hidden `author_only` or `review_only` material;
- runtime database inspection;
- direct SQLite writes;
- provider/API output;
- renderer/ffmpeg/TTS output;
- media artifacts;
- Qingli candidates unless they are explicitly reviewed/promoted by
  docs/control evidence for query-ref use.

If no reviewed source is found for a slot, classify it as
`NO_REVIEWED_SOURCE_FOUND_AI_PLANNING_GAP` unless existing reviewed evidence
proves `USER_WORLDBUILDING_REQUIRED` or `BOUNDARY_BLOCKED`.

## Discovery Matrix

| Query-ref slot | Classification | Candidate reviewed-doc paths | Discovery notes |
| --- | --- | --- | --- |
| `QueryRequest` refs | `NO_REVIEWED_SOURCE_FOUND_AI_PLANNING_GAP` | none | `docs/CODEX_DEMAND_DRIVEN_QUERY_CONTRACT_SPEC_V0.md` and `docs/CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md` define `QueryRequest` rules and templates, but they do not contain a concrete reviewed `QueryRequest` ref for the future first script-ready scene packet candidate. |
| `QueryResponse` refs | `NO_REVIEWED_SOURCE_FOUND_AI_PLANNING_GAP` | none | The query contract/spec and templates define `QueryResponse` rules and template shape, but no concrete reviewed `QueryResponse` ref exists for the future first packet. |
| `QueryScopeReviewReport` refs | `NO_REVIEWED_SOURCE_FOUND_AI_PLANNING_GAP` | none | The query contract/spec and templates define `QueryScopeReviewReport` rules and template shape, but no concrete reviewed `QueryScopeReviewReport` ref exists for the future first packet. |

Every slot above has exactly one classification.

## Inspected Reviewed Surfaces

The following reviewed docs/control surfaces were inspected or carried forward
as relevant rule sources. They are not discovery candidates for concrete query
refs:

- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_EVIDENCE_PLACEHOLDER_PACKET_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_EVIDENCE_SURFACE_DEFINITION_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_QUERY_REF_GAP_CLOSURE_DECISION_BRIEF_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_UNRESOLVED_REF_SOURCE_CATALOG_V0.md`
- `docs/CODEX_DEMAND_DRIVEN_QUERY_CONTRACT_SPEC_V0.md`
- `docs/CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md`
- `docs/CODEX_SCRIPT_READY_SCENE_PACKET_ALIGNMENT_V0.md`
- `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`
- `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`

These files support boundaries, definitions, and query-shape rules. They do not
tie `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport` refs into a
concrete reviewed source set for actual packet work.

## Future Package Candidate

No future docs/control package is named because not all three slots have
`DISCOVERY_CANDIDATE_NEEDS_REVIEW`.

Current blocker: all three query-ref slots are
`NO_REVIEWED_SOURCE_FOUND_AI_PLANNING_GAP`. B01/A01/00 may use this matrix
after B03 review to decide whether to stop, request human/user input, or
classify another bounded docs/control task. This matrix itself does not make
that next-route decision and must not auto-start any follow-up task.

## Prompt Surface And Visibility Rules

Prompt surface is deny-by-default. Fields not explicitly listed in
`allowedPromptSurface` must not enter prompts, screen descriptions, negative
prompts, prompt-safe asset notes, or voice/audio planning notes.

Negative prompts are also prompt surface.

Any future query-ref discovery or downstream packet must define visibility for
fields that may later feed prompts, screen descriptions, negative prompts,
prompt-safe asset notes, or voice/audio planning notes. `author_only` and
`review_only` material must not be copied, inverted, summarized, hinted, or
paraphrased into prompt-facing fields.

Where notes must avoid leaking hidden causality, culprit identity, sealed
motive, director goal, future reveal, or unavailable actor knowledge, they must
use `protectedBoundaryRef` plus a generic `boundaryCategory` instead of copying
or paraphrasing protected content.

## Review Status Terms

- `DRAFT_FOR_REVIEW`: this discovery matrix is ready for technical review.
- `REVIEW_PASS_NOT_APPROVAL`: technical review passed, but this is not user
  approval, asset approval, registry approval, canon approval, release approval,
  final media approval, provider/media handoff, final approval, or production
  lock.
- `NEEDS_REVISION`: slot classification, citation logic, prompt-surface rules,
  or boundary wording needs correction.
- `BLOCKED_BY_HUMAN_DECISION`: a human/user decision is required before future
  task classification.
- `BLOCKED_BY_BOUNDARY`: continuing would cross actual query instance
  creation, query execution, concrete ref resolution, source-basis selection,
  evidence bundle creation, actual packet generation, sample JSON/story
  content, runtime/schema, provider/API, renderer/ffmpeg/TTS, media generation,
  approval, canon, registry, release, final approval, or production lock.

## Technical PASS Is Not Approval

`B03 PASS`, `03 PASS`, `QA_PASS_NOT_APPROVAL`, and
`REVIEW_PASS_NOT_APPROVAL` do not equal user approval, asset approval, registry
approval, canon, release, final media approval, provider/media handoff, final
approval, or production lock.

A technical PASS on this matrix does not create `QueryRequest`,
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

On B03 review PASS, B01/A01/00 may use this matrix to classify a future
explicit task or blocker. PASS must not auto-start actual query instance
creation, query execution, concrete ref resolution, source-basis selection,
evidence bundle creation, actual packet generation, sample JSON creation, story
content generation, runtime/CLI/schema/validator work, 04/05/06/07/08 restart,
renderer/ffmpeg/TTS execution, provider/API work, media generation, approval,
canon, registry, release, or production lock.

## Boundary Summary

- docs/control only
- reviewed-ref discovery matrix only
- no discovery candidates found for the three query-ref slots
- no future package candidate named
- no actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
  instance creation
- no query execution or queryer implementation
- no concrete query-ref resolution
- no promotion of Qingli candidates
- no source-basis selection
- no reviewed evidence bundle creation
- no actual `ScriptReadyScenePacket`
- no actual `ScriptReadyScenePacketCandidate`
- no packet candidate generation
- no source/story content invention
- no sample JSON
- no Kotlin/JVM source
- no runtime/CLI/schema/validator work
- no migration
- no direct SQLite writes
- no provider/API
- no web/server
- no renderer/ffmpeg/TTS execution
- no media generation
- no package generation
- no QA execution against real media
- no artifact creation
- no 04/05/06/07/08 restart
- no Phase 20 / Phase 21 restart
- no approval, canon, asset approval, registry approval, release, final media
  readiness, final approval, provider/media handoff, or production lock
