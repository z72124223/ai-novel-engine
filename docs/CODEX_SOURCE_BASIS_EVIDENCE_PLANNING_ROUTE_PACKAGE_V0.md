# Source Basis Evidence Planning Route Package V0

Status: `DRAFT_FOR_REVIEW`

Control artifact:
`SourceBasisEvidencePlanningRoutePackage`

This document is docs/control only. It defines how future source-basis/evidence
planning may proceed from the reviewed artifact-shape convergence bundle for
the query/source-scope to future `ScriptReadyScenePacket` chain. It does not
select source basis, create evidence bundles, create query instances, resolve
concrete refs, invent source/story/query content, or create packet content.

Consumed reviewed outcome:
`QUERY_REF_SOURCE_MATERIAL_ARTIFACT_SHAPE_READINESS_CONVERGENCE_BUNDLE_SHAPE_READY_FOR_FUTURE_SOURCE_BASIS_EVIDENCE_PLANNING`

Selected source-basis/evidence-planning route-package outcome:
`SOURCE_BASIS_EVIDENCE_PLANNING_ROUTE_PACKAGE_READY_FOR_REVIEW`

## Route Purpose

The route package converts shape-only query-ref source-material rows into a
future planning route. The route reduces final-product uncertainty by defining
which planning slots must be named and which evidence-source categories may be
used before future `ScriptReadyScenePacket` work can classify source-basis or
evidence readiness.

The route remains non-approval. `REVIEW_PASS_NOT_APPROVAL` and
`DRAFT_FOR_REVIEW` continue to apply until a later explicitly dispatched and
reviewed task changes the route.

## Required Future Planning Slots

Every future source-basis/evidence planning artifact must include the following
slots as planning fields. These slots are not concrete refs in this package.

| Planning slot | Required shape | Current status | Consumer |
| --- | --- | --- | --- |
| source scene id | A reviewed docs/control citation to a source-scene planning surface, or an explicit blocker if no reviewed source doc exists. | Shape required; value unresolved. | Future source-basis/evidence planning before `ScriptReadyScenePacket` candidate work. |
| zone id | A reviewed docs/control citation to a source-zone planning surface, or an explicit blocker if no reviewed source doc exists. | Shape required; value unresolved. | Future source-basis/evidence planning and scene continuity review. |
| beat id | A reviewed docs/control citation to a beat planning surface, or an explicit blocker if no reviewed source doc exists. | Shape required; value unresolved. | Future beat-to-packet traceability review. |
| character refs | Reviewed docs/control citations to character planning surfaces, or explicit blocker rows. | Shape required; values unresolved. | Future character asset and scene-packet dependency checks. |
| asset need refs | Reviewed docs/control citations to asset-need planning surfaces, or explicit blocker rows. | Shape required; values unresolved. | Future prompt/asset-card and renderer/package QA planning. |
| `QueryRequest` shape | Reference to the reviewed `ARTIFACT_SHAPE_ROW_QueryRequest` fields: upstream reviewed input ref, citation paths, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, `boundaryCategory`, review gate, non-approval status, unresolved concrete-ref status, and rejection conditions. | Shape inherited; actual request absent. | Future query/source-scope planning only. |
| `QueryResponse` shape | Reference to the reviewed `ARTIFACT_SHAPE_ROW_QueryResponse` fields: upstream reviewed input ref, citation paths, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, `boundaryCategory`, review gate, non-approval status, unresolved concrete-ref status, and rejection conditions. | Shape inherited; actual response absent. | Future evidence-shape planning only. |
| `QueryScopeReviewReport` shape | Reference to the reviewed `ARTIFACT_SHAPE_ROW_QueryScopeReviewReport` fields: upstream reviewed input ref, citation paths, visibility, `allowedPromptSurface`, `protectedBoundaryRef`, `boundaryCategory`, review gate, non-approval status, unresolved concrete-ref status, and rejection conditions. | Shape inherited; actual scope verdict absent. | Future review-scope and boundary planning only. |

## Admissible Evidence-Source Categories

Admissible evidence-source categories are limited to reviewed docs/control
citations only.

Allowed categories:

- reviewed artifact-shape convergence bundle citations;
- reviewed query-ref source-material route-package citations;
- reviewed route/control rule citations;
- future B01-dispatched, reviewed docs/control citations that explicitly define
  source scene, zone, beat, character, asset-need, query-request, query-response,
  or query-scope-review shape.

Forbidden evidence-source categories:

- no placeholders as concrete refs;
- no inferred story/source/query content;
- no unreviewed source docs;
- no query execution output;
- no provider/API output;
- no direct SQLite reads or writes as source evidence;
- no media artifacts as source-basis evidence unless a later reviewed
  docs/control route explicitly authorizes that category.

Placeholders may be used only as unresolved planning markers. They must not be
promoted into source scene id, zone id, beat id, character refs, asset need refs,
query refs, source basis, or evidence bundle entries.

## Evidence Selection Gates

Future evidence selection must pass all gates below before any source-basis or
evidence-planning artifact can advance:

- Visibility gate: every slot must state whether its reviewed citation is
  docs/control-only, prompt-safe, author-only, or blocked.
- `allowedPromptSurface` gate: prompt-facing use is allowed only for fields
  explicitly listed as prompt surface in reviewed docs/control citations.
- `protectedBoundaryRef` gate: protected content must be referenced by boundary
  id instead of exposed directly.
- `boundaryCategory` gate: protected categories must remain generic when a
  protected boundary exists.
- Review gate: every selected citation must name its review source and whether
  it is draft, reviewed, or blocked.
- Non-approval status gate: every planning artifact must preserve
  `DRAFT_FOR_REVIEW`, `REVIEW_PASS_NOT_APPROVAL`, or an explicit blocker.

If any gate fails, the route must record a blocker instead of selecting a source
basis or creating evidence.

## Blocker Categories

Future planning blockers must use one of these categories:

- `MISSING_REVIEWED_SOURCE_DOCS`: source scene, zone, beat, character, asset, or
  query-shape citations do not exist in reviewed docs/control inputs.
- `USER_WORLDBUILDING_NEED`: the missing value requires user or worldbuilding
  decision rather than AI planning.
- `BOUNDARY_BLOCKED`: visibility, `allowedPromptSurface`, `protectedBoundaryRef`,
  or `boundaryCategory` prevents safe exposure or use.
- `AI_RESOLVABLE_PLANNING_GAP`: AI may prepare a future docs/control planning
  surface from reviewed sources, but it must not invent concrete refs.

Blocker labels are route-planning classifications only. They are not approval,
canon, registry, release, final approval, or production lock.

## Future Candidate

One future bounded candidate may be named because it is not status-only and it
reduces final-product uncertainty by turning the route package into a planning
bundle without selecting actual refs:

`source_basis_evidence_planning_bundle_v0_docs_control`

This future candidate is future explicit classification only, docs/control-only,
planning-bundle-only, not auto-started, not approval, and not a source-basis or
evidence bundle creation task.

## Downstream Allowance

On future B03 PASS, B01/A01 may use this route package to classify or dispatch
one explicit docs/control planning-bundle task. PASS must not auto-start source
basis selection, evidence bundle creation, actual query instance creation,
query execution, concrete ref resolution, sample JSON, story/source invention,
`ScriptReadyScenePacket` creation, runtime/schema/provider/API/media work,
SQLite work, renderer/ffmpeg/TTS work, approval, canon, registry, release,
final approval, or production lock.

## Boundary Summary

- no actual source-basis selection
- no actual evidence bundle
- no query instances
- no concrete refs
- no sample JSON
- no source/story/query invention
- no `ScriptReadyScenePacket`
- no runtime/schema/provider/API/media work
- no provider/API
- no SQLite
- no renderer/ffmpeg/TTS
- no web/server
- no restarts
- no approval, canon, registry, release, final approval, or production lock
- no staging, commit, push, archive/window creation
