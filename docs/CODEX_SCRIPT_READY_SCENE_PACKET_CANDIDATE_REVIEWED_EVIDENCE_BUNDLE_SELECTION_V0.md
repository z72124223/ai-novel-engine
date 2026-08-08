# Script-Ready Scene Packet Candidate Reviewed Evidence Bundle Selection V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Selection record id:
`script-ready-scene-packet-candidate-reviewed-evidence-bundle-selection-v0-001`

## Purpose

`ScriptReadyScenePacketCandidateReviewedEvidenceBundleSelection` is a
docs/control selection-decision note that attempts exactly one reviewed
evidence bundle selection outcome from existing reviewed docs/control evidence.

This document is not an actual `ScriptReadyScenePacket`, not an actual
`ScriptReadyScenePacketCandidate`, not sample JSON, not story/source content
generation, not runtime/CLI/schema/validator work, not route execution, not
final approval, and not production lock.

## Prior Reviewed Outcome

This selection references the reviewed prior outcome
`REVIEWED_EVIDENCE_BUNDLE_DEFINITION_READY_FOR_FUTURE_SELECTION` from
`docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_REVIEWED_EVIDENCE_BUNDLE_DEFINITION_V0.md`.

The definition creates a placeholder/reference-only structure for a future
reviewed evidence bundle. It does not fill concrete refs and does not assemble
the actual bundle.

## Selection Outcome

Outcome: `REVIEWED_EVIDENCE_BUNDLE_SELECTION_BLOCKED_BY_MISSING_REVIEWED_EVIDENCE`

No safe reviewed evidence bundle can be selected from existing reviewed
docs/control evidence in this slice.

This is not
`REVIEWED_EVIDENCE_BUNDLE_SELECTED_FOR_FUTURE_SOURCE_BASIS`, because no
reviewed bundle currently ties together source scene id, zone id, beat id,
relevant character refs, asset need refs, `QueryRequest` refs,
`QueryResponse` refs, `QueryScopeReviewReport` refs, continuity constraints,
visibility boundaries, prompt-surface rules, protected boundaries, and review
gates.

This is not
`REVIEWED_EVIDENCE_BUNDLE_SELECTION_BLOCKED_BY_USER_WORLDBUILDING`, because
the reviewed evidence proves a missing reviewed bundle, not a proven absence of
user/worldbuilding material.

This is not `REVIEWED_EVIDENCE_BUNDLE_SELECTION_BLOCKED_BY_BOUNDARY`, because a
docs/control blocked selection record can stop safely without creating packet
content, sample JSON, story content, runtime/schema/validator work,
provider/API work, media, approval, canon, registry, release, or production
lock.

## Selection Attempt

| Required selection field | Existing reviewed evidence | Selection result |
| --- | --- | --- |
| selection record id | This document records a selection-decision id. | Available for this blocked decision only |
| bundle id | The definition doc defines a future `bundleId` field, but no actual reviewed bundle id exists. | Not selected |
| source scene id / scene reference | The definition doc defines placeholder source scene fields; prior selection records found no concrete reviewed source scene id. | Not selected |
| zone id / location reference | The definition doc defines placeholder zone fields; prior evidence treats placeholder zone values as insufficient. | Not selected |
| beat id / beat reference | The definition doc defines placeholder beat fields; prior evidence treats placeholder beat values as insufficient. | Not selected |
| relevant character refs | The definition doc reserves character refs, but no reviewed bundle ties concrete character refs to the future first packet. | Not selected |
| asset need refs | Asset need refs remain future evidence refs; asset approval and registry approval remain separate. | Not selected |
| `QueryRequest` refs | Query surfaces exist, but placeholder/sample query values are not explicitly reviewed and promoted as a first-packet bundle. | Not selected |
| `QueryResponse` refs | Query response surfaces exist, but no reviewed bundle ties response refs to one first-packet basis. | Not selected |
| `QueryScopeReviewReport` refs | Review-report surfaces exist, but no selected reviewed report refs are attached to one bundle. | Not selected |
| continuity constraints | Existing docs define continuity rules. | Available as rules only |
| visibility boundaries | Existing docs define visibility boundaries. | Available as rules only |
| `allowedPromptSurface` and forbidden prompt-surface rules | Prompt-surface rules exist in the boundary doc. | Available as rules only |
| `protectedBoundaryRef` and generic `boundaryCategory` | Definition doc requires these fields. | Available as required fields only |
| reviewer evidence refs and review gates | Existing docs define review gates. | Available as gates only |
| unresolved evidence gaps / blockers | Prior route docs identify missing reviewed evidence. | Available as blocker evidence |

## Exact Missing Reviewed Evidence

The missing evidence is one reviewed evidence bundle that explicitly ties
together:

- bundle id;
- source scene id / scene reference;
- zone id / location reference;
- beat id / beat reference;
- relevant character refs;
- asset need refs;
- `QueryRequest` refs;
- `QueryResponse` refs;
- `QueryScopeReviewReport` refs;
- continuity constraints;
- visibility boundaries;
- `allowedPromptSurface` and forbidden prompt-surface rules;
- `protectedBoundaryRef` and generic `boundaryCategory`;
- reviewer evidence refs and review gates;
- unresolved evidence gaps / blockers, if any.

The definition doc is not that bundle. It defines how a future bundle should be
structured, but it does not select or fill the bundle.

Placeholder/sample query values remain insufficient unless they are explicitly
reviewed and promoted by existing docs/control evidence.

## Missing Input Classification

Current blocker:
`REVIEWED_EVIDENCE_BUNDLE_SELECTION_BLOCKED_BY_MISSING_REVIEWED_EVIDENCE`

Classification notes:

- The gap is reviewed-evidence selection blockage, not source content
  generation permission.
- The project may still have enough worldbuilding somewhere, so this slice does
  not claim a true user/worldbuilding blocker.
- A future task may classify whether the missing bundle can be assembled from
  reviewed evidence, whether user/worldbuilding input is required, or whether a
  boundary blocks the route.

## Prompt Surface And Visibility Rules

Any selection or future bundle field that may later feed prompts, screen
descriptions, negative prompts, prompt-safe asset notes, or voice/audio
planning notes must declare:

- `visibility`;
- `allowedPromptSurface`;
- `forbiddenPromptSurface`;
- `protectedBoundaryRef`;
- `boundaryCategory`;
- review status.

Follow `docs/CODEX_PROMPT_SURFACE_LEAKAGE_BOUNDARY_V0.md`:

- fields not explicitly listed in `allowedPromptSurface` must not enter
  prompts, screen descriptions, negative prompts, prompt-safe asset notes, or
  voice/audio planning notes;
- negative prompts are also prompt surface;
- `author_only` and `review_only` material must not be copied, hinted,
  inverted, summarized, or paraphrased into prompt-facing fields.

Use `protectedBoundaryRef` plus generic `boundaryCategory` where selection
notes must avoid leaking:

- hidden causality;
- culprit identity;
- sealed motive;
- director goal;
- future reveal;
- unavailable actor knowledge.

The generic `boundaryCategory` may identify the kind of protected boundary, but
must not reveal protected content.

## Technical PASS Is Not Approval

All technical PASS statuses remain non-approval.

- `B組03 PASS` and `03 PASS` are technical review for the declared scope only.
- `QA_PASS_NOT_APPROVAL` and `REVIEW_PASS_NOT_APPROVAL` do not equal user
  approval, asset approval, registry approval, canon, release, final media
  approval, provider/media handoff, final approval, or production lock.
- A PASS on this selection-decision note does not select a source-basis bundle,
  create packet content, generate an actual `ScriptReadyScenePacket`, create an
  actual `ScriptReadyScenePacketCandidate`, create sample JSON, start story
  generation, start runtime/CLI/schema/validator work, resume 04/05/06/07/08,
  run renderer/ffmpeg/TTS, call provider/API, generate media, grant approval,
  create canon, create registry approval, release, or create production lock.

## Review Status Terms

- `DRAFT_FOR_REVIEW`: selection-decision note is ready for technical review.
- `REVIEW_PASS_NOT_APPROVAL`: record is technically coherent, but grants no
  approval and starts no work.
- `NEEDS_REVISION`: evidence mapping, selection outcome, prompt-surface safety,
  visibility, or boundary wording needs correction.
- `REVIEWED_EVIDENCE_BUNDLE_SELECTED_FOR_FUTURE_SOURCE_BASIS`: one reviewed
  bundle is selected as future source-basis classification input only, not
  packet content and not approval.
- `REVIEWED_EVIDENCE_BUNDLE_SELECTION_BLOCKED_BY_MISSING_REVIEWED_EVIDENCE`:
  the definition exists, but no reviewed bundle ties the needed refs together.
- `REVIEWED_EVIDENCE_BUNDLE_SELECTION_BLOCKED_BY_USER_WORLDBUILDING`: selection
  would require new 00/user worldbuilding content that cannot be safely
  inferred from reviewed evidence.
- `REVIEWED_EVIDENCE_BUNDLE_SELECTION_BLOCKED_BY_BOUNDARY`: selection would
  cross actual packet generation, sample JSON/story content, runtime/schema,
  provider/API, media, approval, canon, registry, release, or production lock.
- `BLOCKED_BY_HUMAN_DECISION`: user/00/A組01 decision is required before future
  task classification.
- `BLOCKED_BY_BOUNDARY`: a proposed task crosses a forbidden boundary.

## Downstream Allowance

On B組03 review PASS, B組01/A組01/00 may use this selection result to classify a
future explicit task.

Because the outcome is
`REVIEWED_EVIDENCE_BUNDLE_SELECTION_BLOCKED_BY_MISSING_REVIEWED_EVIDENCE`, the
next route is blocked until a future explicit task supplies, cites, or reviews
one bundle that ties the required refs together, or reclassifies the missing
input as user/worldbuilding or boundary-blocked.

PASS must not auto-start:

- source-basis selection;
- actual packet generation;
- actual `ScriptReadyScenePacketCandidate` creation;
- sample JSON creation;
- story/source content generation;
- runtime/CLI/schema work;
- validator implementation;
- direct SQLite writes;
- queryer implementation;
- 04/05/06/07/08 restart;
- renderer/ffmpeg/TTS execution;
- provider/API work;
- media generation;
- approval, canon, registry, release, or production lock.

## Boundary Summary

- docs/control only
- selection-decision only
- no selected reviewed evidence bundle
- no concrete source-basis selection for implementation
- no actual `ScriptReadyScenePacket`
- no actual `ScriptReadyScenePacketCandidate`
- no actual packet content
- no sample JSON
- no story/source content generation
- no source content invention
- no Kotlin/JVM source
- no CLI command
- no runtime/schema/migration
- no validator implementation
- no direct SQLite writes
- no queryer implementation
- no provider/API
- no renderer/ffmpeg/TTS execution
- no media generation
- no package generation
- no QA execution against real media
- no artifact creation
- no actual candidate packet content
- no actual first packet generation
- no 04/05/06/07/08 restart
- no Phase 20 / Phase 21 restart
- no canon
- no asset approval
- no registry approval
- no release
- no final approval
- no final media approval
- no provider/media handoff
- no production lock
