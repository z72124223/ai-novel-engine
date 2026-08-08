# Codex Active World Source Lock V0

Status: `ACTIVE_WORLD_SOURCE_LOCK`

Date: 2026-07-06

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

## Purpose

This document prevents future Codex windows from selecting old sample/demo
world data as the active creative worldview.

The active creative worldview is:

```text
塵行
  -> data/story.db world_source_* official source tables
  -> query-world-source CLI lookup
  -> memory/chenxing_world_source_manifest.json
  -> world/chenxing/raw/ source audit files
```

Rain Gate, Pilot stress evidence, renderer-manifest chain samples, and other
old local samples are test/demo only. They are not active creative worldview,
not author worldview, not canon, not setting-pack approval, and not production
source material.

## Active Source Authority

Use these as the first source of truth for current creative-world work:

1. `memory/active_world_source.json`
2. `data/story.db` through
   `query-world-source --db data/story.db --q <text> [--classification <id>]`
3. `memory/chenxing_world_source_sql_import_result.json`
4. `memory/chenxing_world_source_manifest.json`
5. `world/chenxing/raw/`
6. `memory/quarterfull_world_memory_capsule.json`
7. `docs/QUARTERFULL_WORLD_ALIGNMENT.md`

`world/chenxing/raw/` stores the user-provided `穿越4000年` worldbuilding text
word-for-word as raw source material. Its classification metadata lives in
`memory/chenxing_world_source_manifest.json`; the raw text itself must not be
rewritten, summarized, corrected, translated, or retitled.

`data/story.db` now stores the same official source story data in controlled
SQLite tables:

- `world_source_archives`
- `world_source_documents`
- `world_source_chunks`

The SQL import result is `WORLD_SOURCE_ARCHIVE_SQL_IMPORT_PASS` with 9 source
documents and 255 query chunks. Future windows should query SQL first and use
raw files only when exact source-text audit is needed.

The active world facts currently include:

- series: `塵行`
- provided source title: `穿越4000年`
- chapter: `第一話　塵`
- protagonist: `陸知行`
- companion: `愛醬`
- first major location: `灰關`
- current story pressure: consequences after 愛醬 is kicked by a higher-status
  young cultivator

## Non-Active Sources

The following source families must not be used as creative-world basis unless a
future explicit source-authority update says otherwise:

| Source family | Allowed use | Forbidden use |
| --- | --- | --- |
| Rain Gate samples | Test/demo only | Active worldview, script source, canon, setting pack for the user's story |
| Pilot turn logs | Runtime/stress evidence only | Main story worldview replacement |
| Local renderer / manifest samples | Pipeline evidence only | Story source or worldbuilding source |
| QuarterFull platform notes | Product-pattern learning only | Platform imitation or external dependency |

## Deletion Policy

Do not physically delete sample/test files just because they are not active
worldview.

Physical deletion is allowed only after all of these are true:

1. A dependency scan shows the file is not referenced by Kotlin tests, docs,
   samples, or handoff evidence.
2. Any needed test fixture has a `塵行` replacement or the test has been
   intentionally retired.
3. `PLANS.md` records the exact deletion list.
4. Scoped verification passes after deletion.

Until then, old samples should be treated as archived evidence or test fixtures,
not as creative source material.

## Required Next Cleanup Step

The preferred next content step is:

```text
chenxing_09_foundation_card_pack_contract_sample.json
```

That task should now consume SQL source lookup first, then convert the active
`塵行` source material into clean 09 foundation cards and later setting-pack /
script-ready surfaces. It should replace Rain Gate as the default story-facing
sample for future script, query, storyboard, asset, and video routes.

## Boundaries

This document is docs/control evidence for the active source lock. The SQL
import itself was performed by the controlled CLI and is not a direct ad hoc SQL
write. It does not apply a setting pack, write runtime character knowledge, call
provider/API, open web/server ports, run renderer/ffmpeg/TTS, generate media,
approve canon, approve registry entries, approve final media, approve a release,
or create a production lock.
