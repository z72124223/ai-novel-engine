# RETRIEVAL-01 Offline Dual-Model Benchmark Report

Status: `PASS_WITH_SOURCE_LEVEL_LABEL_LIMITATION`

Date: `2026-07-13`

Branch: `codex/retrieval01-offline-dual-model-benchmark`

Base Commit: `bc97365dc5853bd5fd80a608ac5bd701a7d68e2b`

## 1. Result

The authorized offline Qwen Embedding + Reranker benchmark passed every
acceptance check defined by the current source-document labels.

```text
queries: 50
retrievable: 38
policy-blocked: 6
corpus gaps: 6
indexed chunks: 255
Embedding top-50 source hit: 38/38 (100%)
Embedding top-8 source hit: 38/38 (100%)
Reranker final top-8 source hit: 38/38 (100%)
visibility leaks: 0
```

The six `CHARACTER` queries were stopped before model execution. The six code
and development-document questions remained explicit corpus gaps. Neither set
received model hits.

## 2. Compared baseline

The exact LIKE path was the acceptance comparator because it is the current
strict exact-only path:

```text
EXACT_ENTITY LIKE top-8: 10/10
EXACT_ENTITY model final top-8: 10/10
exact regression: 0 percentage points

SEMANTIC_PARAPHRASE LIKE top-8: 0/10
SEMANTIC_PARAPHRASE model final top-8: 10/10
semantic improvement: +100 percentage points
```

The deterministic lexical baseline also reached 38/38 at source-document
level, but it searches a corpus containing only nine source documents and
returns broad source matches. It remains useful context and is not presented as
proof that lexical retrieval already solves answer-bearing chunk ranking.

## 3. Model and supply-chain lock

```text
Embedding: Qwen/Qwen3-Embedding-0.6B
revision: 97b0c614be4d77ee51c0cef4e5f07c00f9eb65b3
model SHA-256: 0437e45c94563b09e13cb7a64478fc406947a93cb34a7e05870fc8dcd48e23fd
tokenizer SHA-256: def76fb086971c7867b829c23a26261e38d9d74e02139253b38aeb9df8b4b50a

Reranker: Qwen/Qwen3-Reranker-0.6B
revision: e61197ed45024b0ed8a2d74b80b4d909f1255473
model SHA-256: 27cd75a405b9c1b46b59abfd88aaa209e6fed2a1972cde9b70e7659537c5e65b
tokenizer SHA-256: aeb13307a71acd8fe81861d94ad54ab689df773318809eed3cbe794b4492dae4
```

The files came from the two official Qwen Hugging Face repositories at the
pinned revisions. The downloaded byte sizes and all four SHA-256 values match
the pre-download supply-chain record. No third-party quantization, remote model
code, FlashAttention, larger fallback, cloud API, Provider, server, port or
vector database was used.

The isolated runtime stayed outside the Repository:

```text
Python 3.13.9
PyTorch 2.7.1+cu118
Transformers 4.53.3
CUDA runtime 11.8
GPU NVIDIA GeForce GTX 1080 Ti / compute capability 6.1
attention: eager
Embedding batch: 4
Reranker batch: 2
```

The exact environment lock is in
`docs/retrieval/RETRIEVAL01_MODEL_ENVIRONMENT_LOCK.json`.

## 4. Performance and VRAM

Final bounded-batch run:

```text
full 255-chunk indexing: 39,215.52 ms
index batch P50 / P95: 539.47 / 1,332.49 ms
Embedding query P50 / P95: 120.38 / 125.46 ms
Reranker top-20 query P50 / P95: 5,246.68 / 6,923.66 ms
Embedding peak allocated VRAM: 2,204.38 MiB
Reranker peak allocated VRAM: 1,988.86 MiB
observed minimum system-free VRAM: 1,821 MiB
```

An earlier batch-8/4 evidence run reached the accuracy gate but observed only
498 MiB system-free VRAM and was rejected. Reducing batches to 4/2 restored the
required 1 GiB headroom without changing the models, query set, ranking rules or
accuracy. No user process was terminated to manufacture headroom.

## 5. Evidence integrity

Every returned hit records:

- rank and chunk ID;
- source ID and Repository-relative source path;
- source-document SHA-256 and chunk SHA-256;
- heading and a bounded source quote;
- Embedding score and, for final hits, Reranker score.

Files:

```text
docs/retrieval/RETRIEVAL01_MODEL_BENCHMARK_SUMMARY.json
docs/retrieval/RETRIEVAL01_MODEL_BENCHMARK_FULL.json
```

The full evidence JSON SHA-256 is
`f395efd39a6d2bac019f0af14254024cb5f72748bc2922113a316d2eec1804fd`.

The summary JSON SHA-256 is
`862bd924493859ec3128068763bc8a30f5529c24eedcd676d039e479f7a41a61`.

Tie ordering is explicitly score-descending, then source ID, chunk order and
chunk ID. The final run set `CUBLAS_WORKSPACE_CONFIG=:4096:8`; no stochastic
sampling was used.

## 6. Important limitation

The committed 50-query set labels expected answer-bearing **source IDs**, not
individual answer-bearing chunk IDs. Therefore the 100% result is a
source-document-level hit rate. It must not be rewritten as 100% answer-bearing
chunk precision, and it does not authorize direct runtime promotion.

The next runtime package should preserve this evidence while adding precise
chunk relevance labels or explicit human review before using Reranker order as
an actor-visible production path.

## 7. Verification

```text
focused Retrieval00DataFoundationTest + Retrieval01ExactBaselineTest: 4/4 PASS
full Gradle suite: 876/876 PASS
JSON parse: PASS
source path / source hash / quote evidence check: PASS
private absolute path and secret-like string scan: PASS
git diff --check: PASS
```

The full build still reports pre-existing Kotlin serialization opt-in warnings
in `StoryboardPipelineModels.kt`; they are warnings, not failures, and this
documentation/evidence package did not modify that file.

## 8. Boundary verdict

```text
RETRIEVAL_01_SOURCE_LEVEL_ACCEPTANCE_PASS
NO_VISIBILITY_LEAK
NO_RUNTIME_INTEGRATION
NO_CANON_OR_STATE_WRITE
NO_API_OR_PROVIDER
NO_AUTOMATIC_LARGER_MODEL_FALLBACK
NPC_ACTOR_01_REQUIRES_SEPARATE_ACTIVATION
```
