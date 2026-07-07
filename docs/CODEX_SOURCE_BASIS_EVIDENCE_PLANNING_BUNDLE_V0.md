# Source-Basis Evidence Planning Bundle V0

Status: `DRAFT_FOR_REVIEW`

Control artifact:
`SourceBasisEvidencePlanningBundle`

This source-basis evidence planning bundle is docs/control only. It converts the
reviewed source-basis/evidence planning route package into one planning-bundle
matrix for future reviewed-evidence selection. It does not select actual source
basis, create an actual evidence bundle, create query instances, resolve
concrete refs, create sample JSON, invent story/source/query content, or create
an actual `ScriptReadyScenePacket`.

Consumed reviewed route-package outcome:
`SOURCE_BASIS_EVIDENCE_PLANNING_ROUTE_PACKAGE_READY_FOR_REVIEW`

Selected source-basis/evidence-planning bundle outcome:
`SOURCE_BASIS_EVIDENCE_PLANNING_BUNDLE_READY_FOR_FUTURE_REVIEWED_EVIDENCE_SELECTION`

## Bundle Purpose

This bundle reduces final-product uncertainty for the query/source-scope to
future `ScriptReadyScenePacket` chain by gathering the required planning slots,
admissible reviewed-doc evidence sources, visibility gates, prompt-surface
boundaries, protected-boundary rules, and blocker categories into one
non-approval matrix.

The bundle is ready for future reviewed-evidence selection only. It remains
blocked from actual source-basis selection, actual evidence bundle creation,
query execution, concrete ref resolution, sample JSON, runtime/provider/media
work, approval, canon, registry, release, final approval, and production lock.

## Reviewed Inputs

Primary reviewed input:

- `docs/CODEX_SOURCE_BASIS_EVIDENCE_PLANNING_ROUTE_PACKAGE_V0.md`

Supporting reviewed inputs:

- `docs/CODEX_QUERY_REF_SOURCE_MATERIAL_ARTIFACT_SHAPE_READINESS_CONVERGENCE_BUNDLE_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`

The route package requires these future planning slots: source scene id, zone
id, beat id, character refs, asset need refs, `QueryRequest`, `QueryResponse`,
and `QueryScopeReviewReport`. The route package limits admissible sources to
reviewed docs/control citations only and forbids placeholders as concrete refs.

## Planning-Bundle Matrix

Each matrix row defines planning shape only. No row contains actual source ids,
actual character ids, actual asset ids, actual query refs, actual query output,
or actual evidence records.

| Planning slot | Admissible reviewed-doc evidence source | Visibility gate | Prompt-surface boundary | Protected-boundary rule | Blocker category |
| --- | --- | --- | --- | --- | --- |
| source scene id | Reviewed docs/control citation that defines a source-scene planning surface. Placeholders may mark unresolved state but are not concrete refs. | Must classify as docs/control-only, prompt-safe, author-only, or blocked before downstream use. | May enter prompts only if the reviewed citation explicitly lists the field in `allowedPromptSurface`. | If source-scene information touches hidden causality, future reveal, or author-only worldbuilding, use `protectedBoundaryRef` and generic `boundaryCategory`. | `MISSING_REVIEWED_SOURCE_DOCS` until a reviewed source-scene citation exists; `BOUNDARY_BLOCKED` if visibility prevents safe use; `USER_WORLDBUILDING_NEED` if the value requires user decision; otherwise `AI_RESOLVABLE_PLANNING_GAP`. |
| zone id | Reviewed docs/control citation that defines a source-zone planning surface. Placeholders may mark unresolved state but are not concrete refs. | Must classify zone visibility before any scene-continuity planning use. | Zone metadata may be prompt-facing only through reviewed `allowedPromptSurface`. | Protected location, faction, or reveal information must stay behind `protectedBoundaryRef` plus generic `boundaryCategory`. | `MISSING_REVIEWED_SOURCE_DOCS`, `BOUNDARY_BLOCKED`, `USER_WORLDBUILDING_NEED`, or `AI_RESOLVABLE_PLANNING_GAP`. |
| beat id | Reviewed docs/control citation that defines a beat planning surface. Placeholders may mark unresolved state but are not concrete refs. | Must classify beat visibility before packet traceability use. | Beat metadata may enter prompts only when reviewed as prompt-safe. | Protected beat causality or reveal timing must be referenced only by `protectedBoundaryRef` and generic `boundaryCategory`. | `MISSING_REVIEWED_SOURCE_DOCS`, `BOUNDARY_BLOCKED`, `USER_WORLDBUILDING_NEED`, or `AI_RESOLVABLE_PLANNING_GAP`. |
| character refs | Reviewed docs/control citations that define character planning surfaces. Placeholders may mark unresolved state but are not concrete refs. | Must classify whether each character ref is prompt-safe, docs/control-only, author-only, or blocked. | Character-facing prompt use must be limited to reviewed `allowedPromptSurface` fields. | Hidden motive, unavailable actor knowledge, sealed identity, or future reveal must use `protectedBoundaryRef` and generic `boundaryCategory`. | `MISSING_REVIEWED_SOURCE_DOCS`, `BOUNDARY_BLOCKED`, `USER_WORLDBUILDING_NEED`, or `AI_RESOLVABLE_PLANNING_GAP`. |
| asset need refs | Reviewed docs/control citations that define asset-need planning surfaces. Placeholders may mark unresolved state but are not concrete refs. | Must classify whether the asset need is prompt-safe, docs/control-only, author-only, or blocked. | Asset prompt use is allowed only for reviewed `allowedPromptSurface` fields; no hidden story material may leak into asset prompts. | Protected asset rationale must use `protectedBoundaryRef` and generic `boundaryCategory`. | `MISSING_REVIEWED_SOURCE_DOCS`, `BOUNDARY_BLOCKED`, `USER_WORLDBUILDING_NEED`, or `AI_RESOLVABLE_PLANNING_GAP`. |
| `QueryRequest` | Reviewed `ARTIFACT_SHAPE_ROW_QueryRequest` citation from the artifact-shape convergence bundle and reviewed docs/control citations that later define request-planning shape. | Must remain docs/control-only until a later reviewed task authorizes prompt-safe request metadata. | Request fields may enter prompts only when reviewed in `allowedPromptSurface`; no runtime query parameters are created here. | Protected request rationale must use `protectedBoundaryRef` and generic `boundaryCategory`. | `MISSING_REVIEWED_SOURCE_DOCS`, `BOUNDARY_BLOCKED`, `USER_WORLDBUILDING_NEED`, or `AI_RESOLVABLE_PLANNING_GAP`; current state remains unresolved. |
| `QueryResponse` | Reviewed `ARTIFACT_SHAPE_ROW_QueryResponse` citation from the artifact-shape convergence bundle and reviewed docs/control citations that later define response-evidence shape. | Must remain docs/control-only until reviewed evidence exists; provider/API output is absent. | Response fields may enter prompts only when reviewed in `allowedPromptSurface`; no query output is created here. | Protected response rationale must use `protectedBoundaryRef` and generic `boundaryCategory`. | `MISSING_REVIEWED_SOURCE_DOCS`, `BOUNDARY_BLOCKED`, `USER_WORLDBUILDING_NEED`, or `AI_RESOLVABLE_PLANNING_GAP`; current state remains unresolved. |
| `QueryScopeReviewReport` | Reviewed `ARTIFACT_SHAPE_ROW_QueryScopeReviewReport` citation from the artifact-shape convergence bundle and reviewed docs/control citations that later define review-scope shape. | Must remain docs/control-only until a reviewed scope decision exists; no verdict is created here. | Scope-review fields may enter prompts only when reviewed in `allowedPromptSurface`. | Protected review rationale must use `protectedBoundaryRef` and generic `boundaryCategory`. | `MISSING_REVIEWED_SOURCE_DOCS`, `BOUNDARY_BLOCKED`, `USER_WORLDBUILDING_NEED`, or `AI_RESOLVABLE_PLANNING_GAP`; current state remains unresolved. |

## Slot Gates

Every future reviewed-evidence selection task must pass these gates before
using this bundle:

- Reviewed-doc evidence gate: every slot must cite reviewed docs/control input.
- Visibility gate: every slot must state prompt-safe, docs/control-only,
  author-only, or blocked.
- Prompt-surface boundary gate: downstream prompt use is allowed only for
  fields listed in `allowedPromptSurface`.
- Protected-boundary rule gate: protected material must be represented by
  `protectedBoundaryRef` and generic `boundaryCategory`, not exposed directly.
- Non-approval gate: every row remains `DRAFT_FOR_REVIEW`,
  `REVIEW_PASS_NOT_APPROVAL`, or an explicit blocker.
- Review gate: future use requires B01-dispatched review where required; this
  bundle itself is not approval.

## Blocker Categories

- `MISSING_REVIEWED_SOURCE_DOCS`: reviewed docs/control evidence is absent for a
  required slot.
- `AI_RESOLVABLE_PLANNING_GAP`: AI may prepare a future docs/control planning
  surface from reviewed sources, but must not invent concrete refs.
- `BOUNDARY_BLOCKED`: visibility, prompt-surface, protected-boundary, or
  boundary-category rules prevent safe use.
- `USER_WORLDBUILDING_NEED`: missing value requires user/worldbuilding decision
  rather than AI planning.

No true stop condition is selected in this bundle. Missing values are
classification facts only, not permission to invent source basis or evidence.

## Future Candidate

One future bounded candidate may be named because it reduces final-product
uncertainty and is not status-only:

`source_basis_reviewed_evidence_selection_plan_v0_docs_control`

This future candidate is future explicit classification only, docs/control-only,
selection-plan-only, not auto-started, not approval, and not actual source-basis
selection or evidence bundle creation.

## Downstream Allowance

On future B03 PASS, B01/A01 may use this planning bundle to classify or dispatch
one explicit docs/control reviewed-evidence selection-plan task. PASS must not
auto-start source-basis selection, actual evidence bundle creation, query
instance creation, query execution, concrete ref resolution, sample JSON,
story/source invention, actual `ScriptReadyScenePacket` creation,
runtime/schema/provider/API/media work, SQLite work, renderer/ffmpeg/TTS work,
approval, canon, registry, release, final approval, or production lock.

## Boundary Summary

- no actual source-basis selection
- no actual evidence bundle
- no query instances
- no concrete refs
- no sample JSON
- no source/story/query invention
- no actual `ScriptReadyScenePacket`
- no runtime/schema/provider/API/media work
- no provider/API
- no SQLite
- no renderer/ffmpeg/TTS
- no web/server
- no approval, canon, registry, release, final approval, or production lock
- no staging, commit, push, archive/window creation
