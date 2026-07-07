# Codex Chenxing 09 Foundation Card Pack Contract Sample V0

Status: `DRAFT_FOR_REVIEW`

Control artifact:
`Chenxing09FoundationCardPackContractSample`

Selected contract-sample outcome:

`CHENXING_09_FOUNDATION_CARD_PACK_CONTRACT_SAMPLE_DRAFT_READY_FOR_REVIEW`

This document explains the first Chenxing / `憛菔?` 09 foundation card pack
contract sample:

`samples/local_story_input/chenxing_09_foundation_card_pack_contract_sample.json`

The JSON sample is review-only. It is non-canon, not setting-pack-applied, not
runtime SQLite data, not approved assets, not a registry entry, not release,
not final approval, and not production lock. It does not create an actual
`ScriptReadyScenePacket`, storyboard packet, asset registry entry,
voice/subtitle/SFX timeline, renderer package, generated media, runtime data,
schema change, provider/API work, direct SQLite write, or canon approval.

## Source Authorities Consumed

Only these sources were used:

- `memory/active_world_source.json`
- `memory/chenxing_world_source_manifest.json`
- `world/chenxing/raw/`
- `memory/quarterfull_world_memory_capsule.json`
- `docs/QUARTERFULL_WORLD_ALIGNMENT.md`

The raw archive under `world/chenxing/raw/` is source authority only. This
slice does not edit, rewrite, summarize, retitle, normalize, or delete raw
archive files. Source citations in the JSON point back to manifest source ids
and raw paths; they are not copied into the JSON as raw prose.

## Sample Purpose

The sample replaces Rain Gate test/demo-only / non-active-world data only as
the default story-facing 09 foundation card contract sample direction. Rain
Gate test/demo-only / non-active-world files are not deleted or rewritten by
this task.

The final-product chain reduced is:

- primary: scene packet;
- secondary: prompt/asset cards, storyboard/camera, and human approval gates.

The sample supplies enough `FoundationCardPack` structure to support future
source-basis planning for a `ScriptReadyScenePacket` without creating an actual
packet. It keeps all downstream eligibility flags false until future explicit
review and validation tasks change them.

## Contract Shape

The JSON follows the local `FoundationCardPack` V0 shape from
`docs/CODEX_09_FOUNDATION_CARD_JSON_CONTRACT_V0.md`:

- `world_premise`
- `character_foundation`
- `location_foundation`
- `system_foundation`
- `constraint`
- `asset_seed`
- `situation_seed`

Every card remains:

- `visibility=review_only`
- `canonStatus=non_canon_candidate`
- `approvalStatus=not_approved`
- `settingPackReadiness=not_ready`
- `scriptReadyScenePacketEligible=false`
- `assetRegistryEligible=false`

## Source Citation Handling

The sample cites all nine manifest raw source ids and raw paths:

- `chenxing-world-raw-01-core-settings` ->
  `world/chenxing/raw/01_world_core_settings.md`
- `chenxing-world-raw-02-systems-civilization` ->
  `world/chenxing/raw/02_systems_and_civilization.md`
- `chenxing-world-raw-03-systems-deepening` ->
  `world/chenxing/raw/03_systems_deepening_weapon_cultivation_profession_factions.md`
- `chenxing-world-raw-04-economy-items` ->
  `world/chenxing/raw/04_economy_items_treasures_mounts.md`
- `chenxing-world-raw-05-power-map-politics` ->
  `world/chenxing/raw/05_power_map_fortress_politics.md`
- `chenxing-world-raw-06-cultivation-geography` ->
  `world/chenxing/raw/06_cultivation_geography_secret_realms.md`
- `chenxing-world-raw-07-core-characters` ->
  `world/chenxing/raw/07_core_character_ensemble.md`
- `chenxing-world-raw-08-volume-opening` ->
  `world/chenxing/raw/08_volume1_opening_skeleton_luochen.md`
- `chenxing-world-raw-09-aijiang-detail` ->
  `world/chenxing/raw/09_aijiang_detailed_setting.md`

The citations identify source authority for future review. They do not approve
canon, choose a source-basis bundle, create concrete refs, or authorize runtime
query/source work.

## Boundaries

- No deletion of old samples.
- No mutation of `world/chenxing/raw/*`.
- No setting-pack apply.
- No direct SQLite writes.
- No schema/runtime/provider/API/media work.
- No actual `ScriptReadyScenePacket`, storyboard packet, asset registry entry,
  voice/subtitle/SFX timeline, renderer package, or generated media.
- No canon approval, registry approval, release, final approval, or production
  lock.
- No staging, commit, push, archive/window creation, B03 contact, or next-task
  start.
