# Codex Script-Ready Scene Packet Alignment V0

Status: `SCRIPT_READY_SCENE_PACKET_ALIGNMENT_V0_DRAFT_FOR_REVIEW`

Route label: `script_ready_scene_packet_alignment_v0`

Date: 2026-07-02

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

## Purpose

This docs-only alignment note connects the accepted demand-driven query
contract/spec to the existing script-ready scene packet spec. It explains how
future `QueryRequest`, `QueryResponse`, and `QueryScopeReviewReport` evidence
can support a script-ready scene packet without asking 04 to read a full world
bible by default.

Primary references:

- `docs/CODEX_DEMAND_DRIVEN_QUERY_CONTRACT_SPEC_V0.md`
- `samples/local_story_input/rain_gate_demand_driven_query_contract_spec_sample.json`
- `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`
- `docs/CODEX_BLUEPRINT_FUTURE_ROUTE_INTEGRATION_MAP_V0.md`

This is planning/spec alignment only. It is not packet generation, not story
content, not runtime data, no 04/05/06/07/08 restart, no provider/API, no
SQLite, no runtime implementation, no schema, no vector DB, no embeddings, no
external retrieval, no canon, no setting-pack conversion, no registry approval,
no asset approval, no release, no final media approval, and no production lock.

The local baseline 100% is not true final product completion.

## Alignment Rule

Future script-ready scene packet work should treat demand-driven query evidence
as scoped support material, not as a replacement for 01 routing, 05 review, 06
display, or user approval.

The bridge is:

```text
QueryRequest
-> QueryResponse
-> QueryScopeReviewReport
-> future script-ready scene packet draft/review
```

This route only documents the bridge. It does not start the future packet draft
or review route.

## Query Evidence Intake

### QueryRequest

A future packet draft may cite a `QueryRequest` to prove that the scene packet
asked for a bounded information need. Useful fields include:

- `queryFamily`;
- `sceneId`, `zoneId`, `beatId`, or `frameId`;
- `informationNeed`;
- `minimumScopeReason`;
- `visibilityTarget`;
- `allowedSourceRefs`;
- `forbiddenTopics`;
- `mustNotInclude`.

Packet use: `sourceContext`, `sceneReference`, `continuityChecks`, and
`secrecyKnowledgeNotes` may cite a `QueryRequest` to show why only scoped
evidence was requested.

### QueryResponse

A future packet draft may cite a `QueryResponse` as the returned scoped evidence.
Useful fields include:

- returned `cards` or `assets`;
- `omittedBecauseHidden`;
- `omittedBecauseOutOfScope`;
- `missingButRequired`;
- `reviewWarnings`;
- `boundarySummary`;
- `sourceRefs`.

Packet use: scene packet sections may use response data only at the same
visibility level or a stricter one. A `QueryResponse` must not be used to invent
formal ids, facts, assets, or approvals.

### QueryScopeReviewReport

A future packet review may cite a `QueryScopeReviewReport` to prove the query
scope was checked before the packet moves toward display or production planning.
Useful fields include:

- `tooBroad`;
- `tooNarrow`;
- `missingRequiredSource`;
- `authorOnlyLeak`;
- `inventedIdFactOrAsset`;
- `approvalOverclaim`;
- `checks`;
- `findings`.

Packet use: `reviewSummary`, `continuityChecks`, and `secrecyKnowledgeNotes`
may cite the report. A PASS report is not approval, not auto-start, and not
permission to generate story content or resume downstream windows.

## Packet Section Mapping

| Query family | May inform packet sections | Must not inform |
| --- | --- | --- |
| World Card Queryer | `sceneReference`, `scenePurpose`, `locationZone`, `visualSetup`, `stateConstraints`, `continuityChecks`, `forbiddenProps`, `secrecyKnowledgeNotes` | Final prose, canon promotion, setting-pack conversion, full world bible import |
| Asset Queryer | `approvedProps`, `forbiddenProps`, `storyboardNotes`, `oiioiiNotes`, production-only asset reuse notes | Asset approval, registry approval, image/audio/video generation, production lock |
| Situation / Continuity / Pressure Queryer | `playerObjective`, `characterState`, `beatList`, `interactionChoices`, `stateConstraints`, `continuityChecks`, `reviewSummary` | Forced next plot, forced reveal, forced concealment, AI result JSON, runtime state |

The term "approvedProps" in the older packet spec is a packet-field label. It
does not mean asset registry approval. Future packet text should clarify
whether each prop is a candidate, review-ready reference, approved reference, or
production-only note.

## Visibility Boundary

All packet sections must preserve the demand-driven query visibility levels:

- `author_only`: may be counted as omitted or protected, but must not enter
  04 prose, 06 display, 08 prompts, user-facing packet fields, or prompt-safe
  asset notes.
- `review_only`: may support 01/02/03/05 review sections such as
  `sourceContext`, `characterState`, `stateConstraints`, `continuityChecks`,
  `secrecyKnowledgeNotes`, and `reviewSummary`.
- `production_only`: may support 07/08 preparation sections such as
  `approvedProps`, `forbiddenProps`, `storyboardNotes`, `oiioiiNotes`, and
  asset reuse notes. It must not leak `author_only` causality.
- `user_facing`: may support 06 display fields only after proper review and
  01 handoff.

Do not downgrade `author_only` material into `review_only`, `production_only`,
or `user_facing` packet sections. A scene packet that fits the template but
leaks hidden causality is not acceptable.

## Minimum-Scope Rule

04 must not read a full world bible by default. Future packet work should ask
for the smallest query evidence that can support the current scene, beat, frame,
objective, or review question.

Minimum-scope defaults:

- cite specific query request and response ids;
- cite specific 09 foundation card ids or accepted planning docs;
- keep query evidence tied to one scene or beat when possible;
- treat missing evidence as `missingButRequired`, not as permission to invent;
- keep hidden omissions hidden.

## 05 Review Alignment

The existing 05 review checklist should read query scope discipline through the
new contract/spec:

- Was each packet claim backed by a `QueryResponse` or accepted packet source?
- Was each query tied to a `QueryRequest` with bounded `informationNeed`?
- Did a `QueryScopeReviewReport` check over-query, under-query, missing source,
  author-only leak, invented id/fact/asset, and approval overclaim?
- Did user-facing sections exclude `author_only` and `review_only` material?
- Did production-only sections avoid claiming registry approval, asset approval,
  or production lock?

05 PASS remains technical packet review. It is not user approval, not final
media approval, not canon, not release, and not permission to restart
04/05/06/07/08 without explicit 01 handoff.

## Future Downstream Candidate

If this alignment receives review PASS, 01 may consider a future docs-only
`script_ready_scene_packet_template_or_review_v0` route. That candidate would
still be not approval, not auto-start, no packet generation, no
04/05/06/07/08 restart, no provider/API, no SQLite, no runtime implementation,
and no production lock.
