# Chenxing Official World Source SQL Index V0

Marker: `CHENXING_OFFICIAL_WORLD_SOURCE_SQL_INDEX_V0`

Status: `SQL_IMPORT_PASS`

## Purpose

The user-provided `塵行` / `穿越4000年` worldbuilding is official source story
data. It must be looked up through SQLite first so Codex windows stop
rediscovering it through slow workspace scans or stale demo files.

## Current SQL Storage

- Database: `data/story.db`
- Archive table: `world_source_archives`
- Full document table: `world_source_documents`
- Fast chunk lookup table: `world_source_chunks`
- Import result: `memory/chenxing_world_source_sql_import_result.json`
- Source manifest: `memory/chenxing_world_source_manifest.json`

Import evidence:

- `verdict=PASS`
- `status=WORLD_SOURCE_ARCHIVE_SQL_IMPORT_PASS`
- `archiveRowsWritten=1`
- `documentRowsWritten=9`
- `chunkRowsWritten=255`
- `sourceCount=9`
- `totalByteSize=206344`
- `downstreamBoundary=official_world_source_sql_import_only_not_runtime_entity_conversion`

## Required Query Entrypoint

Use this before scanning raw files:

```powershell
.\gradlew.bat :app:run --args="query-world-source --db data/story.db --q 愛醬 --classification companion_detailed_setting --limit 5"
```

General shape:

```powershell
.\gradlew.bat :app:run --args="query-world-source --db data/story.db --q <text> [--classification <id>] [--limit <n>] [--out <path>]"
```

Sample query evidence:

- `memory/chenxing_world_source_query_aijiang_sample.json`
- `memory/chenxing_world_source_query_huiguan_sample.json`

## Boundary

This SQL layer is official source storage and retrieval. It is not direct
runtime character knowledge, not direct setting-pack/canon conversion, not
asset registry approval, not media generation, not final media approval, not
release, and not production lock.

Future conversion into setting packs, runtime facts, character knowledge,
script packets, or asset prompts must use separate controlled transforms.
