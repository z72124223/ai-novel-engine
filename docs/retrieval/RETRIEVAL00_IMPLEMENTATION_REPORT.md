# RETRIEVAL-00 Implementation Report

Status: `PASS / IMPLEMENTED`

Date: `2026-07-13`

Base Commit: `8caeacc728a5dcb49b6bf9181c4f50fb1ec19d89`

Branch: `codex/retrieval00-authority-rebuild-runtime`

## Outcome

RETRIEVAL-00 now has a bounded Kotlin/JVM CLI that rebuilds a new SQLite
database from tracked, hash-bound inputs and emits a complete authority,
retention and logical-content report.

The implementation added:

- an ordered rebuild manifest and JSON Schema;
- authority/retention classification rules over governed data routes;
- source size and SHA-256 verification;
- migration, Setting Pack and World Source rebuild orchestration;
- Canon and author-only boundary checks;
- deterministic logical table digests;
- exact and normalized duplicate evidence;
- historical private-path and local database candidate evidence;
- overwrite, path escape, Canon promotion, runtime mutation, deletion and
  Provider-call barriers;
- focused deterministic rebuild tests and a full regression run.

## Rebuild evidence

```text
verified inputs: 21 / 21
migrations: 15
Setting Packs applied: 5
Canon validation: PASS
World Source documents: 9
World Source chunks: 255
logical tables: 13
logical rows: 380
logical-content SHA-256:
30fcd67770e4977548d1eaa7777baca0518bd09085fb6397d7fd93fdb3056c32
```

Two independent temporary database rebuilds produced the same table digests
and root logical-content SHA-256.

Cross-worktree verification additionally found that Windows `core.autocrlf`
may materialize migrations 014/015 with CRLF. Input verification therefore
uses canonical LF text bytes while reporting working-tree bytes and whether
normalization occurred. The canonical hashes remain the Git Blob content
hashes recorded by the manifest; non-line-ending changes still fail.

## Authority and retention evidence

Governed tracked data inventory:

```text
files: 678
bytes: 28,771,060

AUTHORITATIVE_SOURCE: 41 files / 20,138,027 bytes
FORMAL_STRUCTURED_DATA: 52 files / 326,801 bytes
REBUILDABLE_INDEX: 15 files / 73,972 bytes
TEST_OR_REVIEW_EVIDENCE: 570 files / 8,232,260 bytes

CURRENT: 102 files / 20,541,323 bytes
HISTORICAL_REFERENCED: 561 files / 8,155,765 bytes
REBUILDABLE_CACHE: 15 files / 73,972 bytes
```

Findings:

```text
unclassified governed paths: 0
missing required paths: 0
absolute/private paths required for rebuild: 0
historical tracked files containing old private absolute-path metadata: 75
ignored/local database candidates in the clean worktree: 0
archive candidates approved: 0
delete candidates approved: 0
```

The 75 historical path findings are portability/retention evidence, not active
runtime dependencies. This package did not rewrite or delete those documents.

Exact duplicate groups found:

1. two tracked Canon validation mirrors;
2. two Dialogue-02 revised view files;
3. two Reboot-01 apply-result fixtures;
4. the Lu Chenxing face reference and its source photo copy.

No duplicate was deleted or moved. The image duplicate may be intentional
provenance and therefore cannot be treated as a cleanup candidate without a
later human decision.

## Tests

Focused test:

```text
Retrieval00DataFoundationTest: 3 / 3 PASS
```

Full regression:

```text
./gradlew.bat clean test --no-daemon
875 tests / 0 failures / 0 errors / 0 skipped
BUILD SUCCESSFUL
```

The existing Storyboard serialization opt-in warnings remain warnings and
were not modified because they are outside this work package.

## Safety and scope

No model was downloaded or executed. No OpenAI API, MCP, Provider, server,
port, image, video, audio, Canon promotion, runtime session mutation, database
replacement, file deletion or force push occurred.

RETRIEVAL-01 remains a separate local-model benchmark gate. NPC-ACTOR-01
remains blocked until retrieval gates are accepted; its offline Fake/File
gateway may be implemented before any paid API call, but live API use still
requires API key, cost and privacy approval.
