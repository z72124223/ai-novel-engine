# Script-Ready Scene Packet Candidate Unresolved Ref Source Catalog V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Control artifact:
`ScriptReadyScenePacketCandidateUnresolvedRefSourceCatalog`

This document is docs/control only. It is not source/story content invention,
not source-basis selection, not reviewed evidence bundle creation, not an
actual `ScriptReadyScenePacket`, not an actual `ScriptReadyScenePacketCandidate`,
not sample JSON, not packet candidate generation, not route execution, not
concrete story/source content resolution, not runtime/CLI/schema/validator work,
not provider/API work, not media generation, and not production lock.

## Current Blocker

The current blocker is
`REF_RESOLUTION_PACKET_BLOCKED_BY_UNRESOLVED_REVIEWED_REFS` from
`docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_REVIEWED_EVIDENCE_BUNDLE_REF_RESOLUTION_PACKET_V0.md`.

That packet keeps all eight required ref slots unresolved as
`REF_SLOT_UNRESOLVED_AI_RESOLVABLE_PLANNING_GAP` because reviewed docs/control
evidence did not cite concrete refs for this route.

## Catalog Outcome

Selected catalog outcome:
`UNRESOLVED_REF_SOURCE_CATALOG_PARTIAL_CANDIDATES_FOUND_BUT_NO_COHERENT_SET`.

Existing project material can supply several candidate source refs that need
review, but the catalog does not find a coherent set covering all required
slots. Query evidence refs remain missing, and Qingli-derived candidates remain
candidate material only. Therefore this catalog names no next future package
candidate.

## Source Classification Vocabulary

Each unresolved slot is classified as exactly one of:

- `CANDIDATE_EXISTING_SOURCE_REF_NEEDS_REVIEW`: existing project material
  contains a concrete candidate ref or candidate source path, but it is not
  selected, not promoted, not an approved source basis, and needs future review.
- `NO_EXISTING_SOURCE_REF_FOUND_AI_PLANNING_GAP`: no concrete source ref was
  found from allowed existing material; the gap remains AI-resolvable planning
  work.
- `USER_WORLDBUILDING_REQUIRED`: existing reviewed evidence proves new user/00
  worldbuilding is required before the slot can be resolved.
- `BOUNDARY_BLOCKED`: resolving the slot would cross forbidden boundaries such
  as source/story invention, source-basis selection, evidence bundle creation,
  actual packet generation, runtime/schema work, provider/API, media,
  approval, canon, registry, release, final approval, or production lock.

## Candidate Source Path Rules

Candidate source paths must be exact existing paths. They do not select source
basis and do not resolve the ref slot.

Do not use these as candidate source refs:

- placeholders;
- samples not explicitly reviewed/promoted;
- generic templates;
- inferred story/source content;
- hidden `author_only` or `review_only` material copied into prompt surface;
- runtime database inspection;
- direct SQLite writes.

If a candidate path cannot be cited, it must not be used as a candidate.

## Slot Catalog

| Slot | Classification | Candidate source paths | Notes |
| --- | --- | --- | --- |
| source scene id | `CANDIDATE_EXISTING_SOURCE_REF_NEEDS_REVIEW` | `docs/QINGLI_SCENE_REGISTRY_SEED_BRIEF.md`; `docs/QINGLI_SCRIPT_PACKET_DRY_RUN_REVIEW.md` | Existing docs contain `scene-qingli-herb-warehouse-perimeter`, but this is candidate material only and not selected source basis for the current route. |
| zone id | `CANDIDATE_EXISTING_SOURCE_REF_NEEDS_REVIEW` | `docs/QINGLI_SCRIPT_PACKET_DRY_RUN_REVIEW.md` | Existing docs contain `powder-line-edge`, but it needs review and is not promoted from this catalog. |
| beat id | `CANDIDATE_EXISTING_SOURCE_REF_NEEDS_REVIEW` | `docs/QINGLI_SCRIPT_PACKET_DRY_RUN_REVIEW.md` | Existing docs contain candidate beats such as `beat-01-orient-at-line`, `beat-02-request-procedure`, and `beat-03-options-remain-local`; none are selected here. |
| character refs | `CANDIDATE_EXISTING_SOURCE_REF_NEEDS_REVIEW` | `docs/QINGLI_SCRIPT_PACKET_DRY_RUN_REVIEW.md`; `docs/QINGLI_SCENE_REGISTRY_SEED_BRIEF.md` | Existing docs contain `char-player`, `char-ai-chan`, `char-patrol-he`, and `char-clerk-luo`; these are candidate refs needing review, not approved packet refs. |
| asset need refs | `CANDIDATE_EXISTING_SOURCE_REF_NEEDS_REVIEW` | `docs/QINGLI_SCRIPT_PACKET_DRY_RUN_REVIEW.md`; `docs/QINGLI_SCENE_REGISTRY_SEED_BRIEF.md` | Existing docs contain candidate visual anchors, approved props, and animation asset notes; they are not approved assets, registry approvals, or production locks. |
| `QueryRequest` refs | `NO_EXISTING_SOURCE_REF_FOUND_AI_PLANNING_GAP` | none | `docs/CODEX_DEMAND_DRIVEN_QUERY_CONTRACT_SPEC_V0.md` and `docs/CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md` define the `QueryRequest` surface, but no concrete reviewed request ref is found. |
| `QueryResponse` refs | `NO_EXISTING_SOURCE_REF_FOUND_AI_PLANNING_GAP` | none | The query docs define the `QueryResponse` surface, but no concrete reviewed response ref is found. |
| `QueryScopeReviewReport` refs | `NO_EXISTING_SOURCE_REF_FOUND_AI_PLANNING_GAP` | none | The query docs define the `QueryScopeReviewReport` surface, but no concrete reviewed report ref is found. |

## Coherence Check

This catalog does not find a coherent set of existing candidate refs.

Reasons:

- only source scene id, zone id, beat id, character refs, and asset need refs
  have candidate source paths;
- `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport` refs remain
  missing;
- Qingli material needs review before it can become current route source basis;
- candidate paths are not selected refs and do not create a reviewed evidence
  bundle.

Because the set is incomplete, no next future package candidate is named.

## Existing Material That Is Not Promoted

The following material is useful for understanding why candidate refs may exist,
but this catalog does not promote it into selected refs:

- `docs/STORYBOARD_AND_CAMERA_PLANNING.md` includes a historical Qingli opening
  frame example, but it states that the example is historical planning material,
  not current new-world base, not approved canon, and not a storyboard
  deliverable.
- `samples/local_story_input/` contains local story/video sample artifacts, but
  samples are not used as candidate source refs unless explicitly reviewed and
  promoted by docs/control evidence.
- `docs/CODEX_DEMAND_DRIVEN_QUERY_CONTRACT_SPEC_V0.md` and
  `docs/CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md` define query surfaces, not
  concrete first-packet query refs.

## Prompt Surface And Visibility Rules

Prompt surface is deny-by-default. Fields not explicitly listed in
`allowedPromptSurface` must not enter prompts, screen descriptions, negative
prompts, prompt-safe asset notes, or voice/audio planning notes.

Negative prompts are also prompt surface.

Any future catalog or downstream packet must define visibility for each field
that could later feed prompts, screen descriptions, negative prompts,
prompt-safe asset notes, or voice/audio planning notes. `author_only` and
`review_only` material must not be copied, inverted, summarized, hinted, or
paraphrased into prompt-facing fields.

Where notes must avoid leaking hidden causality, culprit identity, sealed
motive, director goal, future reveal, or unavailable actor knowledge, they must
use `protectedBoundaryRef` plus a generic `boundaryCategory` instead of copying
or paraphrasing protected content.

## Review Status Terms

- `DRAFT_FOR_REVIEW`: this source catalog is ready for technical review.
- `REVIEW_PASS_NOT_APPROVAL`: technical review passed, but this is not user
  approval, asset approval, registry approval, canon approval, release approval,
  final media approval, provider/media handoff, final approval, or production
  lock.
- `NEEDS_REVISION`: slot classification, source citation, prompt-surface rules,
  or boundary wording needs correction.
- `BLOCKED_BY_HUMAN_DECISION`: a human/user decision is required before future
  task classification.
- `BLOCKED_BY_BOUNDARY`: continuing would cross source/story invention,
  source-basis selection, evidence bundle creation, actual packet generation,
  sample JSON/story content, runtime/schema, provider/API, media, approval,
  canon, registry, release, final approval, or production lock.

## Technical PASS Is Not Approval

`B03 PASS`, `03 PASS`, `QA_PASS_NOT_APPROVAL`, and
`REVIEW_PASS_NOT_APPROVAL` do not equal user approval, asset approval, registry
approval, canon, release, final media approval, provider/media handoff, final
approval, or production lock.

A technical PASS on this catalog does not resolve concrete refs, select source
basis, create an evidence bundle, create an actual `ScriptReadyScenePacket`,
create an actual `ScriptReadyScenePacketCandidate`, create sample JSON, start
story generation, start runtime/CLI/schema/validator work, resume
04/05/06/07/08, run renderer/ffmpeg/TTS, call provider/API, generate media,
grant approval, create canon, create registry approval, release, or create
production lock.

## Downstream Allowance

On B03 review PASS, B01/A01/00 may use this catalog result to classify a future
explicit task. PASS must not auto-start concrete ref resolution, source-basis
selection, evidence bundle creation, actual packet generation, sample JSON
creation, story content generation, runtime/CLI/schema/validator work,
04/05/06/07/08 restart, renderer/ffmpeg/TTS execution, provider/API work, media
generation, approval, canon, registry, release, or production lock.

## Boundary Summary

- docs/control only
- source catalog only
- no concrete ref resolution
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
