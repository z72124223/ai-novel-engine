# Codex Chenxing World Source Archive V0

Status: `CHENXING_WORLD_SOURCE_RAW_ARCHIVE_READY`

Date: 2026-07-06

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

## Purpose

This document records the clean source archive for the user's provided
worldbuilding. The raw text is preserved word-for-word in
`world/chenxing/raw/`; classification metadata is stored separately in
`memory/chenxing_world_source_manifest.json`.

Do not rewrite, summarize, correct, translate, retitle, or merge raw source
text inside `world/chenxing/raw/`.

## Source Authority

Read in this order for current worldview/source-material work:

1. `memory/active_world_source.json`
2. `memory/chenxing_world_source_manifest.json`
3. `world/chenxing/raw/`
4. `memory/quarterfull_world_memory_capsule.json`
5. `docs/QUARTERFULL_WORLD_ALIGNMENT.md`

The current archive stores the user's provided `穿越4000年` worldbuilding under
the active `塵行` source lane. The title relationship is not resolved here:
`穿越4000年` may be a working title, subtitle, or related source title. Future
windows must preserve both labels until an explicit source-authority decision
changes that.

## Raw Files

| Order | Classification | Raw path |
| --- | --- | --- |
| 1 | World core truth and initial premise | `world/chenxing/raw/01_world_core_settings.md` |
| 2 | Systems and civilization | `world/chenxing/raw/02_systems_and_civilization.md` |
| 3 | Weapons, cultivation, professions, factions | `world/chenxing/raw/03_systems_deepening_weapon_cultivation_profession_factions.md` |
| 4 | Economy, items, treasures, mounts | `world/chenxing/raw/04_economy_items_treasures_mounts.md` |
| 5 | Power map, fortress politics, nations, undercurrents | `world/chenxing/raw/05_power_map_fortress_politics.md` |
| 6 | Cultivation realms, geography, secret realms, forbidden lands | `world/chenxing/raw/06_cultivation_geography_secret_realms.md` |
| 7 | Core character ensemble | `world/chenxing/raw/07_core_character_ensemble.md` |
| 8 | Volume one opening skeleton: 落塵 | `world/chenxing/raw/08_volume1_opening_skeleton_luochen.md` |
| 9 | Companion detailed setting: 愛醬 | `world/chenxing/raw/09_aijiang_detailed_setting.md` |

## Integrity Rules

- `rawTextUnmodified=true` means the raw file is source material and must not
  be edited casually.
- Classification belongs in `memory/chenxing_world_source_manifest.json`, not
  inside the raw text.
- Derived products such as 09 foundation cards, query responses, script-ready
  packets, storyboard packets, asset cards, or voice timelines must cite these
  raw files instead of copying hidden assumptions.
- If a future task needs correction, create a derived reviewed file; do not
  mutate the raw archive unless the user explicitly asks to replace source
  material.

## Next Allowed Step

The preferred next content step is:

```text
chenxing_09_foundation_card_pack_contract_sample
```

That step should transform this raw archive into a clean 09 foundation card
pack without deleting or rewriting the raw source text.

## Boundaries

This archive is local file-protocol source storage only.

It does not approve canon, write SQLite, change runtime state, apply a setting
pack, approve assets, call provider/API, open web/server ports, run
renderer/ffmpeg/TTS, generate media, approve final media, approve release, or
create a production lock.
