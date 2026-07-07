# Script-Ready Scene Packet Candidate Reviewed Evidence Bundle Ref Resolution Plan V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`ScriptReadyScenePacketCandidateReviewedEvidenceBundleRefResolutionPlan`

This document is docs/control only. It is not an actual
`ScriptReadyScenePacket`, not an actual `ScriptReadyScenePacketCandidate`, not a
sample JSON file, not a packet candidate, not source/story content invention,
not source-basis selection, not concrete ref resolution, not evidence bundle
creation, not route execution, not runtime/CLI/schema/validator work, not
provider/API work, not media generation, and not production lock.

## Upstream Reviewed Outcomes

This plan is downstream of two reviewed outcomes:

- `EXISTING_REVIEWED_EVIDENCE_BUNDLE_NOT_AVAILABLE`
- `AI_RESOLVABLE_GAP_CLOSURE_PATH_DEFINED_FOR_FUTURE_CLASSIFICATION`

Those outcomes mean the project has reviewed rules, templates, prompt-surface
constraints, and protected-boundary constraints, but it does not yet have
reviewed concrete refs tying a future first scene-packet candidate together.

## Ref-Resolution Plan Outcome

Exact ref-resolution-plan outcome:
`REF_RESOLUTION_PLAN_DEFINED_FOR_FUTURE_PACKET_CLASSIFICATION`.

This outcome does not resolve concrete refs. It defines how a future explicit
bounded task may resolve them with reviewed citations while preserving every
missing ref as an AI-resolvable planning gap unless reviewed evidence proves one
alternate classification: user/worldbuilding blocker or boundary-blocked.

## Missing Concrete Refs

The following concrete refs are unresolved and must not be inferred from
placeholders, samples, hidden notes, runtime state, or direct database
inspection:

| Missing ref | Current classification | Future resolution requirement |
| --- | --- | --- |
| source scene id / scene reference | AI-resolvable planning gap | Cite a reviewed source scene id from an allowed evidence source, or keep it unresolved with a reason. |
| zone id / location reference | AI-resolvable planning gap | Cite a reviewed zone id tied to the source scene, or keep it unresolved with a reason. |
| beat id / beat reference | AI-resolvable planning gap | Cite a reviewed beat id tied to the source scene, or keep it unresolved with a reason. |
| character refs | AI-resolvable planning gap | Cite reviewed character refs and their visibility boundaries, or keep them unresolved with reasons. |
| asset need refs | AI-resolvable planning gap | Cite reviewed asset need refs as planning refs only, not approved assets or registry entries. |
| `QueryRequest` refs | AI-resolvable planning gap | Cite reviewed `QueryRequest` refs or state why retrieval request evidence remains unresolved. |
| `QueryResponse` refs | AI-resolvable planning gap | Cite reviewed `QueryResponse` refs or state why retrieval response evidence remains unresolved. |
| `QueryScopeReviewReport` refs | AI-resolvable planning gap | Cite reviewed `QueryScopeReviewReport` refs or state why retrieval review evidence remains unresolved. |

## Allowed Evidence Sources

A future ref-resolution execution/package task may use only these evidence
sources:

- existing reviewed docs/control surfaces;
- future explicit reviewed docs/control evidence produced by a bounded task;
- user/worldbuilding input only if the plan proves the missing ref cannot be
  safely derived from reviewed project evidence.

Allowed evidence must include a stable citation, review status, visibility
classification, and prompt-surface classification before it can be used to
resolve a ref.

## Forbidden Evidence Sources

A future ref-resolution execution/package task must not use:

- placeholders;
- samples not explicitly reviewed/promoted;
- inferred story/source content;
- hidden `author_only` or `review_only` material copied or paraphrased into
  prompt surface;
- runtime database inspection;
- direct SQLite writes;
- provider/API output;
- renderer/ffmpeg/TTS output;
- generated media or artifact output.

If the only available value comes from a forbidden evidence source, the ref
must remain unresolved.

## Acceptance Criteria For A Future Ref-Resolution Package

A future ref-resolution execution/package task must:

- cite concrete reviewed refs for source scene id, zone id, beat id, character
  refs, asset need refs, `QueryRequest`, `QueryResponse`, and
  `QueryScopeReviewReport`, or explicitly mark each missing ref unresolved;
- distinguish missing refs from selected refs;
- preserve the review status and citation path of every selected ref;
- preserve visibility boundaries for every selected ref;
- preserve `allowedPromptSurface` and forbidden prompt-surface rules for every
  field that may later feed prompts, screen descriptions, negative prompts,
  prompt-safe asset notes, or voice/audio planning notes;
- include `protectedBoundaryRef` plus generic `boundaryCategory` where notes
  must avoid leaking hidden causality, culprit identity, sealed motive,
  director goal, future reveal, or unavailable actor knowledge;
- remain non-approval and avoid user approval, asset approval, registry
  approval, canon, release, final media approval, provider/media handoff, final
  approval, or production lock claims;
- leave source-basis selection, actual packet generation, sample JSON creation,
  story content generation, runtime/CLI/schema/validator work,
  04/05/06/07/08 restart, renderer/ffmpeg/TTS execution, provider/API work,
  and media generation out of scope.

## Classification Rules

Every missing ref stays classified as `AI-resolvable planning gap` unless
reviewed evidence proves exactly one alternate classification:

- user/worldbuilding blocker: the ref requires new 00/user worldbuilding input
  that cannot be safely derived from reviewed project evidence.
- boundary-blocked: resolving the ref would cross source/story invention,
  actual packet generation, evidence bundle creation, sample JSON creation,
  runtime/CLI/schema/validator work, provider/API, renderer/ffmpeg/TTS, media
  generation, approval, canon, registry, release, final approval, or production
  lock boundaries.

Do not downgrade uncertainty into a user/worldbuilding blocker merely because a
reviewed citation is currently missing.

## Next Future Package Candidate

The next future package candidate is
`script_ready_scene_packet_candidate_reviewed_evidence_bundle_ref_resolution_packet_v0_docs_control`.

That candidate is classification-only. It must not be auto-started by this
plan or by technical review PASS.

## Prompt Surface And Visibility Rules

Prompt surface is deny-by-default. Fields not explicitly listed in
`allowedPromptSurface` must not enter prompts, screen descriptions, negative
prompts, prompt-safe asset notes, or voice/audio planning notes.

Negative prompts are also prompt surface.

Any future ref-resolution package must define visibility for each field that
could later feed prompts, screen descriptions, negative prompts,
prompt-safe asset notes, or voice/audio planning notes. `author_only` and
`review_only` material must not be copied, inverted, summarized, hinted, or
paraphrased into prompt-facing fields.

Where notes must avoid leaking hidden causality, culprit identity, sealed
motive, director goal, future reveal, or unavailable actor knowledge, they must
use `protectedBoundaryRef` plus a generic `boundaryCategory` instead of copying
or paraphrasing protected content.

## Review Status Terms

- `DRAFT_FOR_REVIEW`: this ref-resolution plan is ready for technical review.
- `REVIEW_PASS_NOT_APPROVAL`: technical review passed, but this is not user
  approval, asset approval, registry approval, canon approval, release approval,
  final media approval, provider/media handoff, final approval, or production
  lock.
- `NEEDS_REVISION`: the plan lacks required evidence-source rules,
  classification rules, prompt-surface rules, or boundary rules.
- `BLOCKED_BY_HUMAN_DECISION`: a human/user decision is required before future
  task classification.
- `BLOCKED_BY_BOUNDARY`: continuing would cross source/story invention, actual
  packet generation, sample JSON/story content, runtime/schema, provider/API,
  media, approval, canon, registry, release, final approval, or production lock.

## Technical PASS Is Not Approval

`B03 PASS`, `03 PASS`, `QA_PASS_NOT_APPROVAL`, and
`REVIEW_PASS_NOT_APPROVAL` do not equal user approval, asset approval, registry
approval, canon, release, final media approval, provider/media handoff, final
approval, or production lock.

A technical PASS on this plan does not select source basis, resolve concrete
refs, create an evidence bundle, create an actual `ScriptReadyScenePacket`,
create an actual `ScriptReadyScenePacketCandidate`, create sample JSON, start
story generation, start runtime/CLI/schema/validator work, resume
04/05/06/07/08, run renderer/ffmpeg/TTS, call provider/API, generate media,
grant approval, create canon, create registry approval, release, or create
production lock.

## Downstream Allowance

On B03 review PASS, B01/A01/00 may use this ref-resolution plan to classify a
future explicit task. PASS must not auto-start source-basis selection, evidence
bundle creation, actual packet generation, sample JSON creation, story content
generation, runtime/CLI/schema/validator work, 04/05/06/07/08 restart,
renderer/ffmpeg/TTS execution, provider/API work, media generation, approval,
canon, registry, release, or production lock.

## Boundary Summary

- docs/control only
- no concrete ref resolution
- no source-basis selection
- no actual `ScriptReadyScenePacket`
- no actual `ScriptReadyScenePacketCandidate`
- no packet candidate
- no evidence bundle creation
- no source/story content invention
- no sample JSON
- no Kotlin/JVM source
- no runtime/CLI/schema/validator work
- no migration
- no direct SQLite writes
- no queryer implementation
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
