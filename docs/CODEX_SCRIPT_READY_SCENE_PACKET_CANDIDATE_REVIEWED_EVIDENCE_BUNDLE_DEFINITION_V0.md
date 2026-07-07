# Script-Ready Scene Packet Candidate Reviewed Evidence Bundle Definition V0

Status: `DRAFT_FOR_REVIEW`

Route family: `script_ready_scene_packet_candidate`

Definition record id:
`script-ready-scene-packet-candidate-reviewed-evidence-bundle-definition-v0-001`

## Purpose

`ScriptReadyScenePacketCandidateReviewedEvidenceBundleDefinition` is a
docs/control definition for the future reviewed evidence bundle that may later
support a first `ScriptReadyScenePacket` candidate.

This document is not an actual `ScriptReadyScenePacket`, not an actual
`ScriptReadyScenePacketCandidate`, not source-basis selection, not
evidence-bundle selection, not sample JSON, not story/source content
generation, not runtime/CLI/schema/validator work, not route execution, not
final approval, and not production lock.

## Prior Reviewed Outcome

This definition references the reviewed prior outcome
`REVIEWED_EVIDENCE_BUNDLE_GAP_AI_RESOLVABLE` from
`docs/CODEX_SCRIPT_READY_SCENE_PACKET_CANDIDATE_REVIEWED_EVIDENCE_BUNDLE_GAP_RESOLUTION_V0.md`.

The prior gap-resolution note says the blocker is AI-resolvable as a
docs/control evidence-bundle definition/classification gap. It does not select
a concrete source-basis bundle and does not start packet generation.

## Definition Outcome

Outcome: `REVIEWED_EVIDENCE_BUNDLE_DEFINITION_READY_FOR_FUTURE_SELECTION`

This slice can safely define required fields, evidence categories, review
gates, visibility rules, prompt-surface rules, and status terms for a future
reviewed evidence bundle.

This outcome does not mean a concrete source scene id, zone id, beat id,
character refs, asset need refs, `QueryRequest` refs, `QueryResponse` refs, or
`QueryScopeReviewReport` refs are selected. Those remain future-only and must
be supplied or cited by a later explicitly classified task.

## Future Reviewed Evidence Bundle Structure

The future reviewed evidence bundle should be placeholders/references only
until reviewed evidence explicitly supports filling concrete refs.

### Bundle Metadata

Expected placeholder fields:

- `bundleId`
- `bundleVersion`
- `routeFamily`
- `sourceGapResolutionRef`
- `sourceDefinitionRef`
- `status`
- `definitionOutcome`
- `progressCaveat`

The `progressCaveat` must preserve that the old local baseline 100% is not
true final product completion, final approval, release, or production lock.

### Review Metadata

Expected placeholder fields:

- `reviewId`
- `reviewStatus`
- `reviewerEvidenceRefs`
- `reviewFindings`
- `reviewGates`
- `technicalPassIsNotApproval`

Review metadata must support `REVIEW_PASS_NOT_APPROVAL` only. It must not claim
user approval, asset approval, registry approval, canon, release, final media
approval, provider/media handoff, or production lock.

### Source Basis Placeholders

Expected placeholder fields:

- `sourceSceneRef`
  - `source scene id`
  - `sourceSceneEvidenceRef`
  - `sourceSceneReviewStatus`
  - `sourceSceneUnresolvedGap`
- `zoneRef`
  - `zone id`
  - `locationReference`
  - `zoneEvidenceRef`
  - `zoneUnresolvedGap`
- `beatRef`
  - `beat id`
  - `beatReference`
  - `beatEvidenceRef`
  - `beatUnresolvedGap`
- `relevantCharacterRefs`
- `assetNeedRefs`

These are not filled by this definition. Placeholder-only values remain
insufficient for source-basis selection.

### Query Evidence Refs

Expected placeholder fields:

- `queryRequestRefs` for `QueryRequest`
- `queryResponseRefs` for `QueryResponse`
- `queryScopeReviewReportRefs` for `QueryScopeReviewReport`

The bundle definition may require these refs and describe how to cite them, but
it must not invent query evidence and must not promote placeholder query/sample
values into concrete first-packet evidence.

### Continuity, Visibility, And Prompt Surface

Expected placeholder fields:

- `continuityConstraints`
- `visibilityBoundaries`
- `allowedPromptSurface`
- `forbiddenPromptSurface`
- `protectedBoundaryRef`
- `boundaryCategory`

Prompt surface (提示詞表面) is deny-by-default. Fields not explicitly listed in
`allowedPromptSurface` must not enter prompts, screen descriptions, negative
prompts, prompt-safe asset notes, or voice/audio planning notes. Negative
prompts are also prompt surface.

Use `protectedBoundaryRef` plus generic `boundaryCategory` where bundle notes
must avoid leaking:

- hidden causality;
- culprit identity;
- sealed motive;
- director goal;
- future reveal;
- unavailable actor knowledge.

The generic `boundaryCategory` may identify the kind of protected boundary, but
must not reveal protected content.

### Review Gates And Blockers

Expected placeholder fields:

- `minimumEvidenceGate`
- `sourceAlignmentGate`
- `promptSurfaceSafetyGate`
- `visibilityBoundaryGate`
- `continuityGate`
- `downstreamRouteSafetyGate`
- `unresolvedEvidenceGaps`
- `blockers`

Blockers must distinguish missing reviewed evidence, true user/worldbuilding
input, human decision needs, and boundary violations.

## Safe Definition Versus Future-Only Work

This definition may safely establish:

- required fields;
- evidence categories;
- review gates;
- visibility and prompt-surface rules;
- status terms;
- placeholder rejection rules;
- downstream allowance wording.

This definition must not:

- fill concrete scene, zone, beat, character, asset, or query refs unless those
  refs already appear in reviewed evidence and are clearly safe;
- select the first packet source basis;
- assemble the actual future evidence bundle;
- create a `ScriptReadyScenePacket`;
- create a `ScriptReadyScenePacketCandidate`;
- create sample JSON;
- generate story/source content;
- start runtime/CLI/schema/validator implementation.

Default posture is placeholders/references only.

## Required Status Terms

- `DRAFT_FOR_REVIEW`: definition is ready for technical review.
- `REVIEW_PASS_NOT_APPROVAL`: definition is technically coherent, but grants no
  approval and starts no work.
- `NEEDS_REVISION`: field structure, gates, prompt-surface safety, visibility,
  outcome classification, or boundary wording needs correction.
- `REVIEWED_EVIDENCE_BUNDLE_DEFINITION_READY_FOR_FUTURE_SELECTION`: this
  definition is sufficient for a future explicitly classified selection task.
- `REVIEWED_EVIDENCE_BUNDLE_DEFINITION_BLOCKED_BY_MISSING_RULES`: the
  definition cannot safely proceed because required structure, gates, or safety
  rules are missing.
- `REVIEWED_EVIDENCE_BUNDLE_DEFINITION_BLOCKED_BY_USER_WORLDBUILDING`: the
  definition cannot proceed without new 00/user worldbuilding content.
- `REVIEWED_EVIDENCE_BUNDLE_DEFINITION_BLOCKED_BY_BOUNDARY`: the proposed next
  step would cross actual packet generation, story content, runtime/schema,
  provider/API, media, approval, canon, registry, release, or production lock
  boundaries.
- `BLOCKED_BY_HUMAN_DECISION`: user/00/A組01 decision is required before future
  task classification.
- `BLOCKED_BY_BOUNDARY`: a proposed task crosses a forbidden boundary.

## Technical PASS Is Not Approval

All technical PASS statuses remain non-approval.

- `B組03 PASS` and `03 PASS` are technical review for the declared scope only.
- `QA_PASS_NOT_APPROVAL` and `REVIEW_PASS_NOT_APPROVAL` do not equal user
  approval, asset approval, registry approval, canon, release, final media
  approval, provider/media handoff, or production lock.
- A PASS on this definition does not select a source-basis bundle, assemble an
  evidence bundle, generate an actual `ScriptReadyScenePacket`, create an
  actual `ScriptReadyScenePacketCandidate`, create sample JSON, start story
  generation, start runtime/CLI/schema/validator work, resume 04/05/06/07/08,
  run renderer/ffmpeg/TTS, call provider/API, generate media, grant approval,
  create canon, create registry approval, release, or create production lock.

## Recommended Future Safe Package

Recommended future package candidate:

`script_ready_scene_packet_candidate_reviewed_evidence_bundle_selection_v0_docs_control`

This recommendation is classification-only. It does not auto-start B組02 work,
B組03 review, evidence-bundle selection, concrete source-basis selection,
actual packet generation, sample JSON creation, story/source content
generation, runtime/CLI/schema/validator work, 04/05/06/07/08 restart,
provider/API work, renderer/ffmpeg/TTS execution, media generation, approval,
canon, registry, release, or production lock.

## Downstream Allowance

On B組03 review PASS, B組01/A組01/00 may use this definition to classify a
future explicit task.

PASS must not auto-start:

- evidence-bundle selection;
- concrete source-basis selection;
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
- definition only
- placeholders/references only
- no actual reviewed evidence bundle
- no evidence-bundle selection
- no concrete source-basis selection
- no concrete scene/zone/beat selection
- no actual `ScriptReadyScenePacket`
- no actual `ScriptReadyScenePacketCandidate`
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
