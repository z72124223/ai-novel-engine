# RETRIEVAL-01 — Offline Local Dual-Model Retrieval Benchmark

Status: `COMPLETE / SOURCE_LEVEL_ACCEPTANCE_PASS`

Pre-model Commit: `e01abc792dfe9444ba65dc8cfaa3cae08d536fd8`

Git integration: `REMOTE_DEFAULT_BRANCH_FAST_FORWARD_COMPLETE`

Date: `2026-07-13`

Execution Base Commit: `bc97365dc5853bd5fd80a608ac5bd701a7d68e2b`

Branch: `codex/retrieval01-offline-dual-model-benchmark`

Parent blueprint:
`docs/LOCAL_HYBRID_RETRIEVAL_KNOWLEDGE_BLUEPRINT_V1.md`

## 1. Goal

Build the deterministic benchmark harness, labeled query set and external-model
JSON protocol needed to compare the current exact SQL path with a future local
Embedding + Reranker path. Model binaries and Python/runtime dependencies stay
outside Git. The user explicitly approved the disk/network/GPU gate on
2026-07-13; the pinned models were downloaded, hash-verified and executed in an
external isolated runtime.

## 2. Candidate models

```text
Qwen/Qwen3-Embedding-0.6B
Qwen/Qwen3-Reranker-0.6B
```

Both candidates must be pinned by Repository revision and model file SHA-256.
No larger model fallback is allowed automatically.

## 3. Pre-model implementation scope

- supply-chain review and pinned candidate manifest;
- at least 50 labeled Traditional Chinese queries;
- current LIKE/SQL baseline evaluation;
- source path, source SHA-256 and expected-source evidence;
- deterministic model subprocess JSON protocol;
- visibility and author-only test cases;
- focused tests, JSON checks and baseline report;
- no external dependency, model or GPU execution.

## 4. Model execution scope after approval

Only after explicit disk/network/GPU approval:

1. create an isolated runtime outside the Repository;
2. download only pinned official artifacts;
3. verify every downloaded model SHA-256;
4. run Embedding top-50 recall;
5. rerank only top 20 candidates;
6. report final top 8, latency and peak VRAM;
7. compare against the committed exact baseline;
8. remove the isolated runtime on rollback without touching Git data.

The adapter must use bounded stdin/stdout or JSON files. It must not open a
server port.

## 5. Acceptance gate

- at least 50 labeled queries;
- zero author-only/visibility leaks;
- every hit has source path, source quote and source hash;
- Embedding top-50 answer-bearing recall at least 90%;
- semantic final-top-8 hit rate improves by at least 5 percentage points over
  the best deterministic baseline;
- exact-ID subset regresses by no more than 2 percentage points;
- deterministic tie ordering;
- at least 1 GiB free VRAM headroom;
- P50/P95 indexing and query latency recorded.

Failure records evidence and blocks promotion. It does not authorize a larger
model, cloud API, vector database or relaxed visibility boundary.

## 6. Stop conditions

Stop before:

- model/runtime download, package installation or GPU execution without
  explicit approval;
- any unpinned or third-party quantization replacing the official candidate;
- FlashAttention installation on the Pascal GPU;
- Python dependencies entering the Gradle/runtime dependency graph;
- a local Web/REST server or open port;
- hidden/author-only text entering actor-visible results;
- Canon, memory, knowledge or runtime writes;
- model files or caches entering Git.

## 7. Completion report

Report:

```text
query count and category counts
exact baseline metrics
pinned model revisions and artifact hashes
runtime/dependency decision
visibility findings
model download/execution status
focused/full test results
Git Commit and Push
acceptance or blocked verdict
```

## 8. Completion evidence

```text
docs/retrieval/RETRIEVAL01_MODEL_ENVIRONMENT_LOCK.json
docs/retrieval/RETRIEVAL01_MODEL_BENCHMARK_REPORT.md
docs/retrieval/RETRIEVAL01_MODEL_BENCHMARK_SUMMARY.json
docs/retrieval/RETRIEVAL01_MODEL_BENCHMARK_FULL.json
```

Final verdict:

```text
PASS_WITH_SOURCE_LEVEL_LABEL_LIMITATION
```

The current query labels prove expected source-document retrieval, not precise
answer-bearing chunk precision. Runtime promotion and NPC-ACTOR-01 remain
separate authorization gates.
