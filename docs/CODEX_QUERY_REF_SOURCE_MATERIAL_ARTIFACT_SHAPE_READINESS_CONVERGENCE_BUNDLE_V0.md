# Query Ref Source Material Artifact Shape Readiness Convergence Bundle V0

Status: `DRAFT_FOR_REVIEW`

Control artifact:
`QueryRefSourceMaterialArtifactShapeReadinessConvergenceBundle`

This document is docs/control only. It consolidates the reviewed query-ref
source-material curation chain into one artifact-shape readiness convergence
bundle for the query/source-scope to future `ScriptReadyScenePacket` chain.

Final-product chain reduced: query/source scope supporting future
`ScriptReadyScenePacket`. This reduces uncertainty about what reviewed
query-ref source-material surfaces must look like before future source-basis
or evidence planning can be classified. It does not create the source basis,
evidence bundle, actual packet, query instance, or concrete refs.

## Applied Convergence Rules

This bundle applies `docs/CODEX_ROUTE_LOOP_CONVERGENCE_RULE_SET_V0.md`.

- `Route Loop Convergence Rule Set`: the long same-family
  status/review/route-decision chain is consolidated into one convergence
  bundle instead of another status-only document.
- `No-New-Status-Doc Gate`: this bundle does not add another pure status
  layer. It summarizes reviewed inputs, names artifact shape, and preserves
  blockers/guardrails for future classification.
- `Artifact-Shape-Ready Gate`: the bundle defines shape-only fields,
  consumers, visibility rules, prompt-surface boundaries, protected-boundary
  refs, and review gates while creating no actual instances.
- `Bundle Review Gate`: any future review of this bundle is convergence review
  only, not approval, canon, release, final approval, or production lock.

Selected convergence outcome:
`QUERY_REF_SOURCE_MATERIAL_ARTIFACT_SHAPE_READINESS_CONVERGENCE_BUNDLE_SHAPE_READY_FOR_FUTURE_SOURCE_BASIS_EVIDENCE_PLANNING`

The blocked alternative is not selected because reviewed docs/control evidence
already defines the shape needed for future planning. The bundle remains blocked
from actual query-ref resolution, concrete refs, source-basis selection,
evidence bundle creation, and actual packet work until a later explicitly
dispatched and reviewed task supplies concrete reviewed inputs.

## Reviewed Inputs

Primary reviewed upstream input:

- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_AUTHORIZED_DOCS_CONTROL_CURATION_ADVANCEMENT_DOCS_CONTROL_CURATION_ROUTE_PACKAGE_V0.md`

Supporting convergence rules and route context:

- `docs/CODEX_ROUTE_LOOP_CONVERGENCE_RULE_SET_V0.md`
- `docs/CODEX_DEFAULT_ROUTE_DECISION_RULES_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`

Upstream row summary:

- `QueryRequest`: upstream
  `CURATION_ADVANCEMENT_DOCS_CONTROL_CURATION_ROUTE_PACKAGE_ROW_QueryRequest`
  preserves absent request content, absent runtime parameters, unresolved
  concrete refs, citation/path check status, visibility, `allowedPromptSurface`,
  `protectedBoundaryRef`, `boundaryCategory`, review gates, `DRAFT_FOR_REVIEW`,
  and `REVIEW_PASS_NOT_APPROVAL`.
- `QueryResponse`: upstream
  `CURATION_ADVANCEMENT_DOCS_CONTROL_CURATION_ROUTE_PACKAGE_ROW_QueryResponse`
  preserves absent query output, absent provider/API output, unresolved concrete
  refs, citation/path check status, visibility, `allowedPromptSurface`,
  `protectedBoundaryRef`, `boundaryCategory`, review gates, `DRAFT_FOR_REVIEW`,
  and `REVIEW_PASS_NOT_APPROVAL`.
- `QueryScopeReviewReport`: upstream
  `CURATION_ADVANCEMENT_DOCS_CONTROL_CURATION_ROUTE_PACKAGE_ROW_QueryScopeReviewReport`
  preserves absent scope-review verdict, absent evidence bundle, unresolved
  concrete refs, citation/path check status, visibility, `allowedPromptSurface`,
  `protectedBoundaryRef`, `boundaryCategory`, review gates, `DRAFT_FOR_REVIEW`,
  and `REVIEW_PASS_NOT_APPROVAL`.

Citation/path status inherited from the upstream reviewed chain: reviewed
docs/control paths are cited as control inputs, and concrete refs remain
unresolved/absent unless future reviewed docs/control citations explicitly
support resolution.

## Shape-Only Fields

The artifact-shape rows below define only the future field shape. They do not
create actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
instances.

| Shape row | Query-ref slot | Consumer | Shape-only fields | Visibility and prompt boundary | Review gates | Concrete-ref status |
| --- | --- | --- | --- | --- | --- | --- |
| `ARTIFACT_SHAPE_ROW_QueryRequest` | `QueryRequest` | Future source-basis or evidence planning for `ScriptReadyScenePacket` candidates. | `slotId`, `upstreamReviewedInputRef`, `sourceMaterialSurfaceType`, `requiredCitationPaths`, `allowedPromptSurface`, `protectedBoundaryRef`, `boundaryCategory`, `reviewGate`, `nonApprovalStatus`, `unresolvedConcreteRefStatus`, `rejectionConditions`. | Docs/control metadata only. Prompt use is allowed only for fields explicitly marked in `allowedPromptSurface`; protected story/worldbuilding content must use `protectedBoundaryRef` plus generic `boundaryCategory`. | `Artifact-Shape-Ready Gate`, citation/path review, boundary review, non-approval review, and future B03 review only when B01 dispatches. | Unresolved/absent: no actual request, no runtime query parameters, no source-basis selection, no concrete refs. |
| `ARTIFACT_SHAPE_ROW_QueryResponse` | `QueryResponse` | Future evidence-planning surface that can classify expected response evidence shape without provider/API output. | `slotId`, `upstreamReviewedInputRef`, `sourceMaterialSurfaceType`, `requiredCitationPaths`, `allowedPromptSurface`, `protectedBoundaryRef`, `boundaryCategory`, `reviewGate`, `nonApprovalStatus`, `unresolvedConcreteRefStatus`, `rejectionConditions`. | Docs/control metadata only. Prompt use is allowed only for fields explicitly marked in `allowedPromptSurface`; response facts remain absent until future reviewed evidence exists. | `Artifact-Shape-Ready Gate`, citation/path review, boundary review, non-approval review, and future B03 review only when B01 dispatches. | Unresolved/absent: no actual response, no query output, no provider/API output, no retrieved fact content, no concrete refs. |
| `ARTIFACT_SHAPE_ROW_QueryScopeReviewReport` | `QueryScopeReviewReport` | Future planning surface for review scope, eligibility, rejection conditions, and boundary checks before any evidence bundle or packet work. | `slotId`, `upstreamReviewedInputRef`, `sourceMaterialSurfaceType`, `requiredCitationPaths`, `allowedPromptSurface`, `protectedBoundaryRef`, `boundaryCategory`, `reviewGate`, `nonApprovalStatus`, `unresolvedConcreteRefStatus`, `rejectionConditions`. | Docs/control metadata only. Prompt use is allowed only for fields explicitly marked in `allowedPromptSurface`; protected review rationale must remain behind `protectedBoundaryRef` and generic `boundaryCategory`. | `Artifact-Shape-Ready Gate`, citation/path review, boundary review, non-approval review, and future B03 review only when B01 dispatches. | Unresolved/absent: no actual scope-review verdict, no concrete query linkage, no evidence bundle, no source-basis selection, no concrete refs. |

## Bundle Shape Contract

Future planning may consume this bundle only as a shape contract. A compliant
future task may classify source-basis or evidence-planning readiness by checking:

- all three query-ref slots are present;
- each slot cites its upstream reviewed docs/control input;
- each slot states `allowedPromptSurface`, `protectedBoundaryRef`, generic
  `boundaryCategory`, visibility, review gate, and non-approval status;
- concrete refs remain unresolved/absent;
- forbidden claims are rejected instead of silently promoted.

This bundle is shape-ready for future source-basis/evidence planning only. It
does not authorize actual source-basis selection, evidence bundle creation,
actual `ScriptReadyScenePacket` work, or packet/candidate content.

## Visibility And Boundary Rules

- `allowedPromptSurface`: only explicitly allowed metadata may enter prompts,
  prompt-safe asset notes, media planning notes, voice/audio planning notes, or
  negative prompts.
- `protectedBoundaryRef`: protected content such as hidden causality, sealed
  motive, director goal, future reveal, unavailable actor knowledge, or
  author-only worldbuilding remains referenced by protected boundary id only.
- `boundaryCategory`: must stay generic when a protected boundary exists.
- Visibility: default to docs/control review metadata; future consumer tasks
  must re-check visibility before using any field downstream.
- Review gates: future use requires citation/path review, prompt-surface review,
  protected-boundary review, non-approval review, and B01-dispatched review
  where required.

## Forbidden Claims

The following claims remain forbidden:

- actual query instances;
- actual `QueryRequest`, `QueryResponse`, or `QueryScopeReviewReport`
  instances;
- query execution;
- concrete refs;
- source/story/query invention;
- source-basis selection;
- evidence bundle creation;
- sample JSON;
- actual `ScriptReadyScenePacket` or `ScriptReadyScenePacketCandidate`;
- runtime/schema/provider/API/media work;
- web/server work;
- direct SQLite writes;
- renderer/ffmpeg/TTS execution;
- approval, canon, registry, release, final approval, or production lock.

Verification aliases for guard checks: no actual instances, no sample JSON,
no provider/API, no SQLite, production lock.

## Downstream Allowance

On future B03 PASS, B01/A01 may use this convergence bundle to classify or
dispatch a future explicit docs/control task for source-basis or evidence
planning readiness. PASS must not auto-start actual query instance creation,
query execution, concrete ref resolution, evidence bundle creation,
source-basis selection, actual packet work, story/source content, runtime
implementation, provider/API work, media work, approval, canon, release, final
approval, or production lock.

Any future task must restate the non-approval status and preserve
`REVIEW_PASS_NOT_APPROVAL` until an explicitly authorized non-docs/control route
exists.
