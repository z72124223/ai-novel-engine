# RETRIEVAL-00 — Authority, Rebuild And Retention Foundation

Status: `NOT_ACTIVE / REQUIRES_EXPLICIT_ASSIGNMENT`

Date: `2026-07-13`

Parent blueprint:
`docs/LOCAL_HYBRID_RETRIEVAL_KNOWLEDGE_BLUEPRINT_V1.md`

## 1. Purpose

Create the data foundation required before Embedding, Reranker or model-assisted
extraction work. The package must make the project reproducible from Git,
separate truth from derived indexes, and classify bloat without deleting or
silently promoting data.

This is a future implementation specification. This document does not start
the package and does not approve code, schema, model, deletion, Canon, API,
MCP, Provider or media work.

## 2. Required Starting Evidence

Before implementation, reverify at the assigned base Commit:

- current `PROJECT_STATE.md` and route gate;
- all migration files and the repository migration order;
- all tracked world raw/formal/engine/production sources;
- `memory/active_world_source.json` and source manifests;
- ignored/local-only DB and memory references without modifying them;
- current Docs, Samples, Memory, media and schema counts;
- existing Setting Pack and World Source CLI behavior;
- current full-test result.

Observed planning baseline on 2026-07-13:

```text
15 migrations
66 SQLite tables
5 Chenxing Setting Packs
9 raw world sources / 206344 bytes / 255 rebuilt chunks
344 Docs
203 Samples
17 tracked Memory JSON files
24 formal Chenxing JSON files
40 Chenxing asset/preview images
23 ignored historical local DB files in the older worktree
```

These counts are evidence for planning only and must be recomputed when the
package is assigned.

## 3. Outcomes

RETRIEVAL-00 must produce:

1. a complete authority and retention inventory;
2. a machine-readable authority registry and validation report;
3. an ordered, source-hash-bound rebuild manifest;
4. one documented fresh-clone rebuild entrypoint;
5. logical-content verification of the rebuilt database;
6. explicit missing/private-path findings;
7. a non-destructive archive/delete candidate report;
8. regression tests for rebuild, authority, visibility and secrets;
9. updated operator documentation and Git handoff evidence.

## 4. Required Data Classification

Each item must have exactly one primary class:

```text
AUTHORITATIVE_SOURCE
FORMAL_STRUCTURED_DATA
REBUILDABLE_INDEX
TEST_OR_REVIEW_EVIDENCE
EPHEMERAL_OR_ARCHIVE_CANDIDATE
```

Each item must also have one retention status:

```text
CURRENT
HISTORICAL_REFERENCED
REBUILDABLE_CACHE
ARCHIVE_CANDIDATE
DELETE_CANDIDATE
UNKNOWN_REQUIRES_REVIEW
```

Classification is not deletion approval and is not Canon approval.

## 5. Fresh-Clone Rebuild Contract

The implementation must provide a bounded Kotlin/JVM CLI path that can:

1. verify tracked input files and hashes;
2. create a new local ignored SQLite database;
3. apply approved migrations in declared order;
4. import approved Setting Packs in declared order;
5. import approved World Source manifests and chunks;
6. rebuild deterministic exact-search indexes approved for this phase;
7. validate Canon, provenance, visibility and author-only boundaries;
8. emit a rebuild report and logical-content manifest.

No copied `data/story.db`, attachment path, old worktree or machine-specific
absolute path may be required.

## 6. Logical-Content Verification

Do not compare SQLite binary file hashes. Verify at least:

```text
schema/migration version
ordered source revisions and SHA-256 values
table row counts
stable primary identifiers
sorted normalized row/content hashes
visibility and authority counts
Canon validation result
World Source document/chunk counts
missing/unresolved source count
```

Generated timestamps, SQLite page layout and non-semantic insertion order must
not create false rebuild failures.

## 7. Authority And Safety Rules

- Formal source and explicit human approval remain authoritative.
- Runtime state changes still require Validator/StateReducer/transaction paths.
- Retrieval relevance never grants Canon, character knowledge or write access.
- Raw source import is archive/query preparation, not entity/Canon conversion.
- Extraction candidates require provenance, confidence and review status.
- Media remains file-based; SQL stores metadata and approval evidence only.
- Secrets and author-only content must remain filtered before and after query.
- API keys, tokens and private absolute paths must not enter Git artifacts.

## 8. Retention And Bloat Evidence

The package must report:

- file count and bytes by class and top-level route;
- exact duplicate hashes and logical duplicates;
- current/historical/unreferenced Docs, Samples and Memory artifacts;
- old ignored database inventory, age and known purpose;
- local-only source dependencies;
- missing files named by active control documents;
- index/cache size and rebuild time;
- archive and delete candidates with evidence and recovery path.

No file deletion, history rewrite, broad move or DB replacement is allowed in
RETRIEVAL-00. A later user-approved cleanup package may act only on accepted
candidates.

## 9. Allowed Future Implementation Scope

Only after explicit assignment, the package may propose the minimum required:

- additive migration or sidecar schema;
- Kotlin models, repository/service code and CLI command;
- JSON schema/manifest and reports;
- direct focused tests and full regression tests;
- documentation and Git handoff.

The design review must choose whether rebuild metadata belongs in formal
SQLite, a sidecar SQLite database or a Git manifest plus validated mirror.

## 10. Prohibited Scope

- Embedding/Reranker/extraction model download or execution;
- vector index implementation;
- OpenAI API, MCP, Provider, server, port or cloud upload;
- image/video/audio generation;
- Canon promotion or content rewriting;
- copying ignored historical DB files into Git;
- deleting/moving historical files or old DBs;
- force push or destructive Git operations;
- starting RETRIEVAL-01 before RETRIEVAL-00 acceptance.

## 11. Verification

At minimum:

- focused migration/rebuild/import/query tests;
- full Gradle test suite;
- fresh temporary directory or clean worktree rebuild;
- deterministic logical-content comparison across two independent rebuilds;
- Setting Pack validation/apply and Canon validation;
- World Source manifest/hash/import/query validation;
- author-only/character-visibility negative tests;
- absolute-path, secret and Git scope scans;
- `git diff --check`;
- explicit proof that no model, media, Provider or deletion action occurred.

## 12. Completion Gate

RETRIEVAL-00 passes only when another clean machine/worktree can recreate the
same logical formal data and query corpus from the reported Git Commit without
private files, copied databases or undocumented manual steps.

The completion report must include:

```text
base and final Commit SHA
branch
input source manifest and hashes
migration/schema version
logical-content manifest hash
table/document/chunk counts
authority/retention counts
missing/private-path findings
archive/delete candidates (not executed)
focused and full test results
Git push/merge result
RETRIEVAL-01 readiness verdict
```

Passing RETRIEVAL-00 does not automatically activate RETRIEVAL-01. The next
package still requires explicit assignment.
