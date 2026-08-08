# Codex Demand-Driven Query Contract Spec V0

Status: `DEMAND_DRIVEN_QUERY_CONTRACT_SPEC_V0_DRAFT_FOR_REVIEW`

Route label: `demand_driven_query_contract_spec_v0`

Date: 2026-07-02

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

## Purpose

This packet defines a local docs/spec/data-contract layer for future
demand-driven query work. It turns the earlier blueprint and template notes
into reviewable request, response, and review-report requirements for:

- World Card Queryer.
- Asset Queryer.
- Situation / Continuity / Pressure Queryer.

This is not canon, not setting-pack data, not approved asset data, and not
runtime data. It grants no provider/API work, no SQLite work, no runtime
implementation, no CLI, no schema, no vector DB, no embedding, no external
retrieval, no GPT automation, no 04/05/06/07/08 restart, no media generation,
no release, and no production lock.

The local baseline 100% is not true final product completion.

## Route Boundary

Accepted upstream evidence:

- `docs/CODEX_09_FOUNDATION_CARD_PACK_REVIEW_V0.md`
- `samples/local_story_input/rain_gate_09_foundation_card_pack_review_report_sample.json`
- `docs/DEMAND_DRIVEN_QUERY_LAYER_BLUEPRINT.md`
- `docs/CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md`
- `docs/CODEX_BLUEPRINT_FUTURE_ROUTE_INTEGRATION_MAP_V0.md`
- `docs/CODEX_CURRENT_ROUTE_CARD_V0.md`

The 09 foundation card pack review gate may allow this contract/spec task, but
it does not start queryer implementation. This spec may recommend that 01 later
consider a docs-only `script_ready_scene_packet_alignment_v0` route after
review, but that is only a recommendation. It is not approval, not dispatch,
and not permission to implement queryers or restart downstream windows.

## Common Contract Requirements

All query families use the same reviewable pattern:

1. A `QueryRequest` declares the narrow information need.
2. A `QueryResponse` returns only scoped evidence with citations.
3. A `QueryScopeReviewReport` checks whether the scope was safe and sufficient.

Each object must preserve these boundaries:

- It must state `demand_driven_query_contract_spec_v0` as the governing route.
- It must cite accepted planning docs or 09 foundation card ids.
- It must include a visibility level: `author_only`, `review_only`,
  `production_only`, or `user_facing`.
- It must explain why the request cannot be answered from a smaller source.
- It must not ask 04/05/06/07/08 to read a full world bible by default.
- It must not invent formal ids, facts, assets, approvals, canon, registry
  state, runtime state, provider/API state, SQLite state, or production lock.

## QueryRequest Requirements

Required common fields:

```json
{
  "contractVersion": "demand-driven-query-contract-spec-v0",
  "requestId": "",
  "queryFamily": "World Card Queryer|Asset Queryer|Situation / Continuity / Pressure Queryer",
  "requestingRoute": "01|02|04|05|06|07|08",
  "sourceTaskId": "",
  "sourcePacketId": "",
  "sceneId": "",
  "beatId": "",
  "frameId": "",
  "informationNeed": [],
  "minimumScopeReason": "",
  "visibilityTarget": "author_only|review_only|production_only|user_facing",
  "maxCards": 0,
  "allowedSourceRefs": [],
  "forbiddenTopics": [],
  "mustNotInclude": [],
  "intendedUse": ""
}
```

Request rules:

- `queryFamily` must name exactly one queryer family.
- `informationNeed` must be concrete enough for 05 or 03 to detect over-query
  and under-query.
- `allowedSourceRefs` must point to 09 foundation cards, 09 review evidence, or
  accepted planning docs.
- `forbiddenTopics` must include author-only secrets when the target is lower
  than `author_only`.
- `maxCards` must be bounded. A request for "everything" is too broad.
- `intendedUse` must name a product-layer use such as script-ready packet
  planning, review, asset pickup, or storyboard consistency.

## QueryResponse Requirements

Required common fields:

```json
{
  "contractVersion": "demand-driven-query-contract-spec-v0",
  "responseId": "",
  "sourceRequestId": "",
  "queryFamily": "World Card Queryer|Asset Queryer|Situation / Continuity / Pressure Queryer",
  "verdict": "PASS|NEEDS_REVISION|FAIL",
  "cards": [],
  "omittedBecauseHidden": [],
  "omittedBecauseOutOfScope": [],
  "missingButRequired": [],
  "reviewWarnings": [],
  "boundarySummary": {},
  "sourceRefs": []
}
```

Response rules:

- Every returned card or asset must cite its source.
- A response may omit `author_only` material, but it must name that omission in
  `omittedBecauseHidden` without leaking the hidden fact.
- A response that lacks a required source must return `NEEDS_REVISION` or
  `FAIL`; it must not fill the gap by invention.
- A response must preserve the requested visibility target or stricter.
- A response must keep candidate asset status, review-only status, and
  production-only status separate from approval.

## QueryScopeReviewReport Requirements

Required common fields:

```json
{
  "contractVersion": "demand-driven-query-contract-spec-v0",
  "reviewId": "",
  "reviewedRequestIds": [],
  "reviewedResponseIds": [],
  "verdict": "PASS|NEEDS_REVISION|FAIL",
  "tooBroad": false,
  "tooNarrow": false,
  "missingRequiredSource": false,
  "authorOnlyLeak": false,
  "inventedIdFactOrAsset": false,
  "approvalOverclaim": false,
  "checks": [],
  "findings": [],
  "recommendedNext01Decision": ""
}
```

Review rules:

- `PASS` means the contract sample or future query output is scoped, cited, and
  visibility-safe. It is not canon approval and not implementation approval.
- `NEEDS_REVISION` means missing citations, overly broad or narrow requests,
  unclear visibility, or incomplete omissions.
- `FAIL` means author-only leakage, invented formal id/fact/asset, false
  approval, false production lock, or an attempt to start runtime work.

## Visibility Levels And Leakage Rules

- `author_only`: may be used only for author/control reasoning. It must not
  enter 04 prose, 06 display, 08 prompts, user-facing packets, prompt-safe asset
  notes, or production-only output.
- `review_only`: may be shown to 01/02/03/05 for technical review. It must not
  be copied into user-facing output.
- `production_only`: may support 07/08 production preparation such as prompt
  constraints or asset consistency. It must not reveal author-only causality.
- `user_facing`: safe for future 06 display only after the proper upstream
  handoff and review. It is still not canon by itself.

Leakage controls:

- Never downgrade `author_only` to `review_only`, `production_only`, or
  `user_facing`.
- Never put future answers, director goals, sealed motives, hidden causality, or
  unavailable actor knowledge into prompt-safe or user-facing fields.
- Never use hidden facts to force a player objective or storyboard direction.
- Hidden omissions may be counted, but the hidden fact itself must remain hidden.

## Minimum-Scope Rules

04/05/06/07/08 must request only the evidence needed for the current packet,
frame, scene, objective, or review task. They must not read a full world bible
by default.

Minimum-scope defaults:

- Prefer specific 09 foundation card ids over full packs.
- Prefer one scene, beat, frame, or objective at a time.
- Prefer `maxCards` small enough for review, typically 3 to 7 unless 01 assigns
  a larger docs-only audit.
- Treat any request for "all world lore", "all assets", "all story arcs", or
  "all secrets" as too broad unless 01/user explicitly assigns a separate
  review-only audit task.
- If needed evidence does not exist, return `missingButRequired` rather than
  inventing facts.

## Source And Citation Rules

Every request and response should cite at least one accepted source, such as:

- a 09 foundation card id from
  `samples/local_story_input/rain_gate_09_foundation_card_pack_contract_sample.json`;
- the 09 review gate:
  `samples/local_story_input/rain_gate_09_foundation_card_pack_review_report_sample.json`;
- `docs/DEMAND_DRIVEN_QUERY_LAYER_BLUEPRINT.md`;
- `docs/CODEX_DEMAND_DRIVEN_QUERY_TEMPLATES_V0.md`;
- `docs/CODEX_BLUEPRINT_FUTURE_ROUTE_INTEGRATION_MAP_V0.md`.

Citation requirements:

- A response card must carry `sourceRefs`.
- Asset responses must distinguish sample asset seeds from approved asset
  registry entries.
- Review reports must flag `missingRequiredSource=true` if a required source is
  absent or too vague.

## World Card Queryer

Purpose: retrieve only the world facts needed now.

Request expectations:

- Include `sceneId`, `zoneId` when known, `informationNeed`, `maxCards`,
  `visibilityTarget`, and `allowedSourceRefs`.
- Ask for premise, location, custom, cost, taboo, material culture, or review
  constraints only when tied to the current packet.
- Include forbidden topics such as author-only future causality when they are
  outside the target visibility.

Response expectations:

- Return `worldCards` or common `cards` with `cardType` values such as
  `world_premise`, `location_foundation`, `system_foundation`, or `constraint`.
- Include `constraints`, `forbiddenImplications`, and `sourceRefs`.
- Omit out-of-scope world lore and author-only details.

Hard stops:

- Do not dump a full world bible.
- Do not turn questionnaire answers into canon.
- Do not create material culture, institutions, maps, factions, or power rules.

## Asset Queryer

Purpose: retrieve only the production asset evidence needed for the current
packet or storyboard frame.

Request expectations:

- Include `frameId` when known, `sceneId`, `zoneId`, `neededCharacters`,
  `neededObjects`, `variantNeeds`, `minimumStatus`, `promptSafeOnly`, and
  `allowedSourceRefs`.
- State whether candidate assets are acceptable or approved references are
  required.

Response expectations:

- Return `assets` or common `cards` with asset ids, asset type, status, variant
  relationship, prompt-safe summary, required references, forbidden drift, and
  source refs.
- Use `missingButRequired` or `missingAssets` when a requested asset has no
  source.
- Keep `candidate`, `review_ready`, `approved_reference`, and
  `production_locked` distinct.

Hard stops:

- Do not approve candidate assets.
- Do not generate images, audio, or video.
- Do not redesign characters, scenes, props, weapons, costumes, or materials.
- Do not treat an asset seed as a production lock.

## Situation / Continuity / Pressure Queryer

The old name "Story Card Queryer" is only a legacy alias. The safer product
name is Situation / Continuity / Pressure Queryer because it retrieves local
situation, continuity, pressure, evidence, misconception, objective, and
constraint data. It must not decide the next plot.

Request expectations:

- Include `currentSituationId`, `sceneId`, `protagonistObjective`,
  `desiredHorizon`, `actorKnowledgeFocus`, `visibilityTarget`, and
  `allowedSourceRefs`.
- Ask what pressure and continuity matter now, not what the story should do.

Response expectations:

- Return cards with `cardType` values such as `situation`, `continuity`,
  `pressure`, `objective`, `constraint`, `evidence`, `misconception`, or
  `choice`.
- Separate actor knowledge from world causality.
- Include continuity refs, knowledge limits, available evidence, and local
  misconceptions when cited.

Hard stops:

- Do not write final prose, AI result JSON, or storyboard frames.
- Do not turn world causality into forced reveal or forced concealment.
- Do not force a character objective outside the character's current knowledge.

## Reviewer Checklist

Reviewers should check:

- Too broad: Did the request ask for a full world bible, all lore, all assets,
  all story arcs, or all secrets?
- Too narrow: Did the request omit a card/source needed to avoid invention?
- Missing required source: Did every returned card, asset, or constraint cite a
  09 card or accepted planning doc?
- Author-only leak: Did `author_only` material appear in `review_only`,
  `production_only`, `user_facing`, prompt-safe, or display fields?
- Invented id/fact/asset: Did the response create formal ids, facts, scene ids,
  zone ids, variants, assets, approvals, or registry states without a source?
- Approval overclaim: Did the response claim canon, setting-pack conversion,
  approved asset, registry approval, release, final media approval, production
  lock, no provider/API approval, no SQLite approval, or no runtime
  implementation approval incorrectly?

## Sample JSON

Local sample:

```text
samples/local_story_input/rain_gate_demand_driven_query_contract_spec_sample.json
```

The sample demonstrates:

- one World Card Queryer request/response;
- one Asset Queryer request/response;
- one Situation / Continuity / Pressure Queryer request/response;
- one `QueryScopeReviewReport` covering all three query families.

The sample is intentionally small so it can show minimum-scope behavior without
pretending to be a full world source.

## Recommended Next 01 Decision

After review, 01 may decide whether to send this packet to a read-only reviewer.
If it receives PASS, a safe next recommendation is a docs-only
`script_ready_scene_packet_alignment_v0` route. That recommendation does not
start implementation, does not restart 04/05/06/07/08, and does not approve
canon, registry, release, media, provider/API, SQLite, runtime, or production
lock work.
