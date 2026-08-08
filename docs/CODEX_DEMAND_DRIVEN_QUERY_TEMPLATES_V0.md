# Codex Demand-Driven Query Templates V0

Status: `DEMAND_DRIVEN_QUERY_TEMPLATES_V0_DRAFT_FOR_REVIEW`

Date: 2026-07-02

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

## Purpose

This document turns the existing demand-driven query blueprint into concrete
future JSON templates. It is not an implementation.

No CLI, SQLite table, schema, vector DB, embedding, external retrieval, provider
API, GPT automation, or runtime enforcement is approved here.

## Contract Alignment

Use `docs/CODEX_DEMAND_DRIVEN_QUERY_CONTRACT_SPEC_V0.md` as the governing
contract/spec for `demand_driven_query_contract_spec_v0`. This template packet
is a compact shape reference only; the contract spec defines minimum-scope
rules, citations, visibility levels, reviewer checks, and downstream boundaries
for World Card Queryer, Asset Queryer, and Situation / Continuity / Pressure
Queryer.

The contract/spec and these templates are not canon, no provider/API, no
SQLite, no runtime implementation, and no production lock. Future 04/05/06/07/08
work must not read a full world bible by default.

## QueryRequest Template

```json
{
  "requestId": "",
  "requestingRoute": "04|05|06|07|08|01|02",
  "requestType": "world_card|asset|situation_continuity_pressure",
  "sourcePacketId": "",
  "sceneId": "",
  "beatId": "",
  "informationNeed": [],
  "visibilityTarget": "author_only|review_only|production_only|user_facing",
  "maxCards": 0,
  "forbiddenTopics": [],
  "reason": "",
  "mustNotInclude": []
}
```

## QueryResponse Template

```json
{
  "responseId": "",
  "sourceRequestId": "",
  "verdict": "PASS|FAIL",
  "cards": [
    {
      "cardId": "",
      "cardType": "world|asset|situation|continuity|pressure|objective|constraint|evidence|misconception",
      "visibility": "author_only|review_only|production_only|user_facing",
      "summary": "",
      "constraints": [],
      "sourceRefs": [],
      "forbiddenImplications": []
    }
  ],
  "omittedBecauseHidden": [],
  "omittedBecauseOutOfScope": [],
  "reviewWarnings": [],
  "findings": []
}
```

## QueryScopeReviewReport Template

```json
{
  "reviewId": "",
  "sourceRequestId": "",
  "sourceResponseId": "",
  "verdict": "PASS|FAIL",
  "checks": [],
  "tooBroad": false,
  "tooNarrow": false,
  "authorOnlyLeak": false,
  "missingRequiredSource": false,
  "allowedNextStep": "",
  "findings": []
}
```

## Review Rules

- A query must have a reason and a bounded information need.
- User-facing responses must exclude author-only material.
- 04/05/06/07/08 must not read a full world bible by default.
- Query responses are scoped evidence, not canon approval.
- Query templates do not approve SQL, schema, direct SQLite writes, vector DB,
  provider/API calls, or runtime enforcement.
- Query review must flag too broad, too narrow, missing required source,
  author-only leak, invented id/fact/asset, approval overclaim, and production
  lock overclaim.
