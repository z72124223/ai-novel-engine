# Codex 09 Foundation Card Organization V0

Status: `FOUNDATION_CARD_ORGANIZATION_V0_DRAFT_FOR_REVIEW`

Date: 2026-07-02

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

## Purpose

This document organizes the future 09 foundation card layer before any
script-ready scene packet, storyboard, asset registry, or renderer route is
resumed.

It is not canon, not a setting pack, not runtime data, not approved asset data,
and not permission to start 04/05/06/07/08.

## Card Families

| Family | Purpose | Example ids |
| --- | --- | --- |
| World premise card | Core public premise and tone. | `09-world-premise-*` |
| Character foundation card | Character public traits, knowledge limits, visual anchors. | `09-character-*` |
| Location foundation card | Scene/location anchors, zones, forbidden additions. | `09-location-*` |
| System foundation card | Resource, social, skill, weapon, medicine, formation, or rule systems. | `09-system-*` |
| Constraint card | Hard boundaries for material culture, secrecy, power cost, visibility. | `09-constraint-*` |
| Asset seed card | Candidate visual/audio asset seed before registry approval. | `09-asset-seed-*` |
| Situation seed card | Non-canon situation/pressure seed for later query requests. | `09-situation-seed-*` |

## FoundationCard Template

```yaml
id: ""
family: ""
title: ""
sourceRefs: []
status: "draft_non_canon"
visibility: "author_only|review_only|production_only|user_facing"
publicSummary: ""
reviewOnlyNotes: []
constraints: []
forbiddenImplications: []
settingPackReady: false
assetRegistryReady: false
queryEligible: false
nextAllowedStep: "review_only"
```

## Promotion Rules

1. Candidate planning entries may become 09 draft cards only after 01 assigns
   that route.
2. 09 draft cards must be reviewed before they feed query templates.
3. Query-eligible does not mean canon or setting-pack ready.
4. Setting-pack conversion requires a separate explicit milestone using the
   setting-pack pipeline.
5. Asset registry approval requires a separate explicit registry decision.

## Boundary

This document does not approve canon, setting packs, runtime data, asset
registry entries, SQLite writes, schema changes, storyboard generation, renderer
execution, provider/API calls, or 04/05/06/07/08 resumption.

