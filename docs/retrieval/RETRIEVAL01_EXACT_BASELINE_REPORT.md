# RETRIEVAL-01 Exact Baseline Report

Status: `PRE_MODEL_BASELINE_PASS / MODEL_GATE_BLOCKED`

Date: `2026-07-13`

Base Commit: `0922bb2ce1599101c2726431f8b81b1259abc56d`

Branch: `codex/retrieval01-offline-dual-model-benchmark`

## Implemented

- 50-query Traditional Chinese labeled benchmark set;
- seven required categories;
- current full-query LIKE baseline;
- deterministic CJK-bigram / ASCII-token lexical baseline;
- Repository-relative source path, source SHA-256, chunk SHA-256 and bounded
  source quote on every returned hit;
- explicit policy blocking for character-visible requests against the
  author-source-only raw corpus;
- explicit corpus-gap results for code/development-document queries;
- pinned external-model JSON subprocess protocol;
- deterministic two-database regression test.

## Query set

```text
path: samples/retrieval/chenxing_retrieval_benchmark_queries_v1.json
canonical LF SHA-256:
64bbc5ca0b9cd88624cf6f93a47754fb6f2a45988962fbd46058c40403006a76
queries: 50
retrievable World Source queries: 38
knowledge-boundary policy blocks: 6
code/development corpus gaps: 6
```

Category counts:

```text
EXACT_ENTITY: 10
SEMANTIC_PARAPHRASE: 10
TEMPORAL_CURRENT_STATE: 6
CAUSAL: 6
CODE_DEVELOPMENT_DOC: 6
KNOWLEDGE_BOUNDARY: 6
CHAPTER_CONTINUITY: 6
```

## Baseline results

```text
LIKE full-query top-8 source hit: 10 / 38 = 26.3158%
deterministic lexical top-8 source hit: 38 / 38 = 100.0000%
visibility leaks: 0
blocked character-visible probes returned hits: 0
```

The lexical result is a broad source-level baseline over only nine source
documents and top 8 results. It is not proof of semantic understanding and is
not the model acceptance target. The later model comparison must use the same
labels and report chunk-level ranking/failure examples in addition to the
source-level metric.

The six code/development queries intentionally record `CORPUS_GAP`; code and
development docs are not silently mixed into the World Source collection.

## Cross-worktree correction

Testing in this clean Worktree exposed a false RETRIEVAL-00 failure caused by
mixed Git checkout line endings. The rebuild manifest now binds canonical Git
Blob LF bytes for all 21 text inputs. Verification reports working-tree byte
size and CRLF normalization separately. Both RETRIEVAL-00 and RETRIEVAL-01
focused tests pass after the correction.

## Current gate

```text
STATIC_SUPPLY_CHAIN_REVIEW_PASS
EXACT_BASELINE_PASS
MODEL_DOWNLOAD_NOT_AUTHORIZED
MODEL_EXECUTION_NOT_STARTED
RETRIEVAL_01_NOT_ACCEPTED
```

The next action requires explicit approval for approximately 2.4 GB of model
weights plus an isolated Python/PyTorch/Transformers environment, network use
and GTX 1080 Ti GPU execution. No such download or installation occurred.

## Verification

```text
focused: Retrieval00DataFoundationTest + Retrieval01ExactBaselineTest PASS
full: 876 tests / 0 failures / 0 errors / 0 skipped
JSON: query set, schemas, summary and emitted full report PASS
external JSON parser: PASS
```

Existing Storyboard serialization opt-in compiler warnings remain unchanged
and are outside this work package.
