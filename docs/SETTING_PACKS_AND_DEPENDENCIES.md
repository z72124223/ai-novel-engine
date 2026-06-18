# Setting Packs And Dependencies

MVP-5 adds a local setting-pack import pipeline for canonical world data. It remains a Kotlin/JVM CLI + SQLite runtime. There is no server, port, network API, embedding store, vector database, Docker, Android UI, Rust, or multi-agent framework.

## Pipeline

Every setting pack must move through the same ordered pipeline:

1. `parse`: read a UTF-8 JSON setting pack.
2. `validate`: run schema and semantic canon checks.
3. `diff`: compare entity revisions against current canon.
4. `impact preview`: traverse dependency edges and active sessions.
5. `transaction apply`: write pack, import log, entity revisions, core entities, and dependency edges in one SQLite transaction.

`validate-setting-pack`, `preview-setting-import`, `diff-setting-pack`, `impact-report`, `export-setting-pack`, `show-dependencies`, and `validate-project-canon` are read-only for formal story state. `apply-setting-pack` is the only setting-pack command that writes canon data.

## JSON Shape

A setting pack contains:

- `schemaVersion`: currently `1.0`.
- `packId` and `packVersion`: stable import identity.
- `projectId`: target project.
- `mode`: `BASE`, `ADD_ON`, or `PATCH_CANON`.
- `source`: local authorship/provenance metadata.
- `dependencies`: pack/version prerequisites.
- `entities`: grouped canon entities such as characters, locations, items, techniques, facts, secrets, scenes, scene contracts, and story threads.
- `relations`: dependency graph edges.
- `assertions` and `metadata`: optional validation context.

Entities use stable IDs and `entityVersion`. Changing existing canon requires a higher `entityVersion`; same-version content changes are rejected.

## Visibility

Supported visibility values are:

- `public`
- `private`
- `director_only`
- `author_only`

Author-only secrets must not enter character starting knowledge unless the secret explicitly lists that character in `knownBy`. Author-only entities are excluded from character-facing turn context.

## Dependency Graph

Setting-pack relations are stored in `dependency_edges` with provenance fields:

- `source_pack_id`
- `source_pack_version`
- `visibility`
- `runtime_relevant`
- `strength`
- `reason`
- `metadata_json`
- `canonical_hash`
- `status`

The graph supports bounded traversal for impact reports and context relevance. The turn context includes only relevant setting excerpts, not the full setting pack or full dependency graph.

## Active Session Guard

`PATCH_CANON` cannot silently modify existing canon while an active session exists. Preview reports impacted sessions and a blocker. Apply rejects the pack. After the session is closed, a patch with a higher `entityVersion` can be previewed and applied.

## CLI

```powershell
./gradlew.bat run --args="create-setting-pack-template --project demo --out samples/setting_pack_template.json"
./gradlew.bat run --args="validate-setting-pack --db data/story.db --file samples/sample_setting_pack.json"
./gradlew.bat run --args="preview-setting-import --db data/story.db --file samples/sample_setting_pack.json --out out/setting_preview.json"
./gradlew.bat run --args="apply-setting-pack --db data/story.db --file samples/sample_setting_pack.json"
./gradlew.bat run --args="diff-setting-pack --db data/story.db --file samples/sample_setting_pack_patch.json"
./gradlew.bat run --args="impact-report --db data/story.db --file samples/sample_setting_pack_patch.json"
./gradlew.bat run --args="export-setting-pack --db data/story.db --project demo --pack sample-shrine-pack --version 1 --out out/exported_setting_pack.json"
./gradlew.bat run --args="show-dependencies --db data/story.db --project demo --entity-type character --entity-id char-shen"
./gradlew.bat run --args="validate-project-canon --db data/story.db --project demo"
```

Codex may generate setting pack JSON files, but must not write formal SQLite data directly with SQL, `sqlite3`, or ad hoc scripts. Formal writes go through `apply-setting-pack`.
