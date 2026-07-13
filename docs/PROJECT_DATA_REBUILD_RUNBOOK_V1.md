# Project Data Rebuild Runbook V1

Status: `ACTIVE`

Date: `2026-07-13`

This runbook recreates the Chenxing formal SQLite data and deterministic World
Source query corpus from tracked Git inputs. It does not copy a historical
database, call a model, promote Canon, write runtime session state, or delete
files.

## Authority

The ordered, hash-bound input registry is:

```text
world/chenxing/engine/chenxing_data_rebuild_manifest_v1.json
```

Its schema is:

```text
schemas/retrieval/project_data_rebuild_manifest_v1.schema.json
```

The manifest binds 15 additive migrations, 5 ordered Setting Packs and the
World Source manifest. The World Source manifest separately binds all 9 raw
source files and their byte hashes.

## Fresh rebuild

From the Repository root, choose a new database path that does not exist:

```powershell
./gradlew.bat run --args="rebuild-project-data --repo . --manifest world/chenxing/engine/chenxing_data_rebuild_manifest_v1.json --db <temporary-directory>/story.db --out <temporary-directory>/rebuild-report.json" --no-daemon
```

The command refuses to overwrite an existing database. The database and full
machine-readable report should remain outside Git unless a later work package
explicitly approves a bounded evidence artifact.

Successful output must report:

```text
verdict=PASS
verifiedInputs=21
worldSourceDocuments=9
worldSourceChunks=255
```

The current expected logical-content hash is recorded in:

```text
docs/retrieval/RETRIEVAL00_VALIDATION_SUMMARY.json
```

Compare logical-content hashes, not SQLite binary hashes. Timestamps, random
import/revision IDs, absolute local database paths and SQLite page layout are
excluded from the semantic digest.

## Safety checks

The rebuild fails when:

- an input is missing or its byte size / SHA-256 differs;
- an input path is absolute or escapes the Repository;
- the target database already exists;
- a Setting Pack cannot validate or apply in declared order;
- World Source validation/import fails;
- Canon validation fails;
- a governed tracked file has no authority classification;
- a required tracked path is missing.

Historical documents containing old private absolute paths are reported as
retention evidence. They are not runtime dependencies and are not deleted by
this command.

## Verification

```powershell
./gradlew.bat :app:test --tests com.localnovel.engine.Retrieval00DataFoundationTest --no-daemon
./gradlew.bat clean test --no-daemon
git diff --check
```

No Embedding, Reranker, extraction model, OpenAI API, Provider or media tool is
part of this runbook.
