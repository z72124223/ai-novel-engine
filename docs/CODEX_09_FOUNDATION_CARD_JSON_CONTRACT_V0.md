# Codex 09 Foundation Card JSON Contract V0

Status: `FOUNDATION_CARD_JSON_CONTRACT_V0_DRAFT`

Date: 2026-07-02

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## Purpose

This document defines the first local file-protocol JSON contract for 09
foundation cards. It turns the accepted organization layer in
`docs/CODEX_09_FOUNDATION_CARD_ORGANIZATION_V0.md` into a concrete
`FoundationCardPack` shape that a future validator/CLI can validate.

This contract is not canon, not a setting pack, not runtime SQLite data, not
approved asset data, not storyboard output, not media, and not permission to
resume 04/05/06/07/08. It is a draft-only local JSON planning surface.

## FoundationCardPack Shape

Top-level file shape:

```json
{
  "schemaVersion": "foundation-card-pack-v0",
  "status": "FOUNDATION_CARD_PACK_DRAFT_NON_CANON",
  "packId": "09-foundation-pack-example-v0-001",
  "title": "Example foundation card pack",
  "locale": "zh-TW",
  "sourceNotes": [],
  "canonStatus": "non_canon_candidate",
  "approvalStatus": "not_approved",
  "settingPackReadiness": "not_ready",
  "retrievalTags": [],
  "downstreamUse": {
    "queryEligible": false,
    "scriptReadyScenePacketEligible": false,
    "settingPackConversionEligible": false,
    "assetRegistryEligible": false
  },
  "boundaryFlags": {
    "canonApproved": false,
    "settingPackData": false,
    "runtimeSqliteData": false,
    "approvedAssetData": false,
    "providerApiCalls": false,
    "rendererInvocation": "none",
    "ffmpegInvocation": "none",
    "ttsInvocation": "none",
    "mediaGenerated": false,
    "finalMediaApproval": false,
    "releaseApproved": false,
    "productionLock": false
  },
  "cards": []
}
```

## FoundationCard Shape

Each `cards[]` entry must use this shape:

```json
{
  "id": "09-world-premise-example-v0-001",
  "family": "world_premise",
  "title": "Example card",
  "sourceNotes": [],
  "visibility": "review_only",
  "canonStatus": "non_canon_candidate",
  "approvalStatus": "not_approved",
  "settingPackReadiness": "not_ready",
  "retrievalTags": [],
  "downstreamUse": {
    "queryEligible": false,
    "scriptReadyScenePacketEligible": false,
    "settingPackConversionEligible": false,
    "assetRegistryEligible": false
  },
  "boundaryFlags": {
    "canonApproved": false,
    "settingPackData": false,
    "runtimeSqliteData": false,
    "approvedAssetData": false,
    "providerApiCalls": false,
    "rendererInvocation": "none",
    "ffmpegInvocation": "none",
    "ttsInvocation": "none",
    "mediaGenerated": false,
    "finalMediaApproval": false,
    "releaseApproved": false,
    "productionLock": false
  },
  "publicSummary": "",
  "reviewOnlyNotes": [],
  "constraints": [],
  "forbiddenImplications": []
}
```

## Card Families

Allowed `family` values:

| Family value | Accepted route name | Purpose |
| --- | --- | --- |
| `world_premise` | World premise card | Core premise, public tone, and setting direction. |
| `character_foundation` | Character foundation card | Character traits, knowledge limits, role, visual cue, and voice cue. |
| `location_foundation` | Location foundation card | Location anchors, zones, mood, access limits, and forbidden additions. |
| `system_foundation` | System foundation card | Resource, social, skill, weapon, medicine, formation, or rule systems. |
| `constraint` | Constraint card | Hard boundaries for material culture, secrecy, power cost, visibility, and continuity. |
| `asset_seed` | Asset seed card | Candidate visual/audio seed before asset registry approval. |
| `situation_seed` | Situation seed card | Non-canon situation or pressure seed for later query requests. |

## Required Fields

Every pack must include:

- `schemaVersion`
- `status`
- `packId`
- `title`
- `locale`
- `sourceNotes`
- `canonStatus`
- `approvalStatus`
- `settingPackReadiness`
- `retrievalTags`
- `downstreamUse`
- `boundaryFlags`
- `cards`

Every card must include:

- `id`
- `family`
- `title`
- `sourceNotes`
- `visibility`
- `canonStatus`
- `approvalStatus`
- `settingPackReadiness`
- `retrievalTags`
- `downstreamUse`
- `boundaryFlags`
- `publicSummary`
- `reviewOnlyNotes`
- `constraints`
- `forbiddenImplications`

## Candidate-Only Rules

Foundation cards are candidates only:

- not canon;
- not setting pack;
- not runtime SQLite;
- not approved assets;
- not 04/05/06/07/08 resume;
- not storyboard generation;
- not renderer execution;
- not provider/API work;
- not final media approval;
- not release/production lock.

Required safe values for V0:

- `status=FOUNDATION_CARD_PACK_DRAFT_NON_CANON`
- `canonStatus=non_canon_candidate`
- `approvalStatus=not_approved`
- `settingPackReadiness=not_ready`
- `boundaryFlags.canonApproved=false`
- `boundaryFlags.settingPackData=false`
- `boundaryFlags.runtimeSqliteData=false`
- `boundaryFlags.approvedAssetData=false`
- `boundaryFlags.providerApiCalls=false`
- `boundaryFlags.rendererInvocation=none`
- `boundaryFlags.ffmpegInvocation=none`
- `boundaryFlags.ttsInvocation=none`
- `boundaryFlags.mediaGenerated=false`
- `boundaryFlags.finalMediaApproval=false`
- `boundaryFlags.releaseApproved=false`
- `boundaryFlags.productionLock=false`

## Stop Conditions

Stop before drafting, validating, promoting, or converting foundation cards if
any of these are true:

- user worldbuilding missing;
- canon approval is needed or implied;
- setting-pack conversion is requested or implied;
- direct SQLite/schema work would be needed;
- asset registry approval would be needed;
- 04/05/06/07/08 resumption would be needed;
- provider/API calls would be needed;
- renderer/ffmpeg/TTS/media generation would be needed;
- final media approval would be needed or implied;
- release/production lock would be needed or implied.

## Future Allowed Next Step

The only next implementation step this contract proposes is
`foundation_card_pack_validation_v0`.

That future task may add a validator/service/CLI to read a
`FoundationCardPack`, enforce the V0 candidate-only rules, and write a local
validation report. It must still avoid canon approval, setting-pack conversion,
SQLite direct writes, schema changes, asset registry approval, 04/05/06/07/08
resumption, provider/API calls, renderer/ffmpeg/TTS/media generation, final
media approval, release, and production lock unless a later explicit user/01
decision changes those boundaries.

## Progress Caveat

The older local baseline 100% remains a local baseline only. It is not final
product completion, not final video readiness, not renderer readiness, not
provider/media handoff, not canon approval, not registry approval, not release,
not final media approval, and not production lock.

Short form: local baseline 100% is not final product completion.

## Sample

Default story-facing review-only sample evidence:

`samples/local_story_input/chenxing_09_foundation_card_pack_contract_sample.json`

This Chenxing / `憛菔?` sample is draft/review-only, non-canon, not
setting-pack-applied, not runtime SQLite data, not approved assets, not
registry, not release, and not production lock. It cites the active raw source
manifest and raw paths under `world/chenxing/raw/`, but it does not modify,
rewrite, summarize, retitle, normalize, or approve the raw archive.

Legacy test/demo-only sample evidence:

`samples/local_story_input/rain_gate_09_foundation_card_pack_contract_sample.json`

Rain Gate is test/demo-only / non-active-world. It is not the active creative
worldview, not the user's authored story source, not canon, not setting-pack
approval, not script source, and not production source material.
Rain Gate test/demo-only / non-active-world files are retained for contract
evidence; they are not deleted by the Chenxing sample replacement.
