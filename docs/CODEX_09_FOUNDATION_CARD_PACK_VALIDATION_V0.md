# Codex 09 Foundation Card Pack Validation V0

Status: `FOUNDATION_CARD_PACK_VALIDATION_V0_DRAFT`

Date: 2026-07-02

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## Purpose

This document records `foundation_card_pack_validation_v0`, a deterministic
local file-protocol JSON validator for the 09 `FoundationCardPack` contract.
It reads a draft non-canon pack, validates candidate-only fields and boundary
flags, then writes a reviewable local JSON validation report.

This is not canon, not setting-pack data, not runtime SQLite data, not approved
asset data, not media generation, not provider/API work, not 04/05/06/07/08
resumption, not final media approval, not release, and not production lock.

## Command

```text
validate-09-foundation-card-pack --input <pack.json> --out <report.json> [--validation <id>]
```

## Input

- Contract doc: `docs/CODEX_09_FOUNDATION_CARD_JSON_CONTRACT_V0.md`
- Sample input:
  `samples/local_story_input/rain_gate_09_foundation_card_pack_contract_sample.json`
- Required input status: `FOUNDATION_CARD_PACK_DRAFT_NON_CANON`
- Required schema version: `foundation-card-pack-v0`

## Output

- PASS status: `FOUNDATION_CARD_PACK_VALIDATION_PASS`
- FAIL status: `FOUNDATION_CARD_PACK_VALIDATION_FAIL`
- Downstream boundary:
  `foundation_card_pack_validation_v0_only_not_canon_or_runtime`
- PASS next allowed downstream step: `09_foundation_card_pack_review`
- Sample output:
  `samples/local_story_input/rain_gate_09_foundation_card_pack_validation_report_sample.json`

The next step is review first because the accepted 09 organization rules say
draft cards must be reviewed before they feed query templates. This is safer
than sending the pack directly to demand-driven query contract work.

## Validation Rules

The validator rejects or reports FAIL for:

- missing or blank pack id, title, locale, or card ids;
- wrong schema version or wrong pack status;
- missing required family among world premise, character foundation, location
  foundation, system foundation, constraint, asset seed, and situation seed;
- unknown card family;
- duplicate card ids;
- missing source notes;
- missing retrieval tags;
- missing downstream use;
- unsafe boundary flags;
- `canonStatus` that is not candidate/non-canon;
- `approvalStatus` that is not draft/unapproved;
- `settingPackReadiness` claiming ready;
- `queryEligible=true`;
- `scriptReadyScenePacketEligible=true`;
- `settingPackConversionEligible=true`;
- `assetRegistryEligible=true`;
- provider/API, renderer, ffmpeg, TTS, media, runtime SQLite, approved asset,
  canon, final media approval, release, or production lock claims.

## Boundary

The validator output remains local JSON only:

- no provider/API calls;
- no web/server/open ports;
- no direct SQLite writes;
- no schema or migration changes;
- no renderer/ffmpeg/TTS/media generation;
- no GUI, screenshots, mouse, or keyboard operation;
- no canon, registry, release, final media approval, approval, or production
  lock;
- no Phase 20 / Phase 21;
- no staging, commit, push, delete, clean, or file moves.

## Progress Caveat

The local baseline 100% is not final product completion. It is not final video
readiness, renderer readiness, canon approval, provider/media handoff, release,
final media approval, or production lock.
