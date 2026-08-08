# Local Hybrid Retrieval And Knowledge Blueprint V1

Status: `DOCS_ONLY_FUTURE_CANDIDATE_NOT_ACTIVE`

Date: `2026-07-13`

This document refines the future demand-driven query route with a local hybrid retrieval design. It does not authorize model downloads, runtime implementation, schema changes, OpenAI API calls, Web/REST servers, MCP, Provider use, Canon changes, or media generation.

## 1. Decision Summary

The planned knowledge path has two separate operating layers:

```text
Layer A — import / update time
source documents
→ deterministic parsing
→ optional lightweight local extraction
→ provisional entities / relations / events with provenance
→ rebuildable retrieval index

Layer B — query time
query routing and visibility filter
→ SQL / FTS / BM25 exact retrieval
  + local Embedding semantic retrieval
→ merge and deduplicate candidates
→ local Reranker
→ final 3–8 evidence chunks
→ optional cloud-safe evidence packet
→ GPT reasoning only after a separate cloud/API decision
```

The extraction model, Embedding model and Reranker are three different responsibilities. The extraction model runs only when source material changes; daily retrieval primarily uses Embedding and Reranker.

## 2. Confirmed Corrections

### 2.1 Parameter notation

`B` means billion parameters:

```text
1B = 10 億
4B = 40 億
8B = 80 億
```

`4B = 400 億` is incorrect.

### 2.2 Confirmed local hardware

Read-only local inspection on 2026-07-13 reported:

```text
GPU: NVIDIA GeForce GTX 1080 Ti
VRAM: 11264 MiB
Compute Capability: 6.1
CPU: Intel Core i7-8700 @ 3.20 GHz
OS: Windows 10 Home
```

The CPU is `i7-8700`, not `i7-8700K`.

The GPU has enough VRAM to justify a 0.6B + 0.6B pilot, but capacity does not guarantee acceptable speed. GTX 1080 Ti is Pascal/GP102 and does not have the modern Tensor Core path of RTX hardware. FP16 weight-size estimates are planning estimates only, not throughput guarantees.

### 2.3 Qwen model roles

Qwen's official materials list both Embedding and Reranker families in 0.6B, 4B and 8B sizes. The Embedding model is a dual encoder that maps text independently to vectors; the Reranker is a cross encoder that reads the query-document pair and produces a relevance score.

The first pilot candidate is therefore:

```text
Qwen3-Embedding-0.6B
+ Qwen3-Reranker-0.6B
```

This is a benchmark candidate, not an approved dependency or installed capability.

### 2.4 GBrain relationship

GBrain is reference architecture only. Its official Repository describes hybrid search, keyword/vector fusion, source boosts and replaceable embedding/reranker providers, including local options. It does not prove that installing GBrain automatically provisions an extraction model, local Embedding model and local Reranker for this project.

The first pilot must not clone or integrate GBrain. Useful concepts may be studied independently after a static license/dependency/security review.

### 2.5 MCP relationship

MCP is an integration bridge, not the retrieval engine. Current OpenAI documentation says ChatGPT connects to remote MCP servers; local/private servers require a supported tunnel path. Product availability and plan permissions are changeable external facts and must be rechecked immediately before any MCP decision.

The project must not make MCP a prerequisite for the retrieval pilot.

## 3. Position In The Product Architecture

This layer supports the final animation/video product by improving the evidence supplied to:

- world and character lookup;
- story production master preparation;
- screenplay and dialogue review;
- scene and shot continuity;
- visual asset lookup;
- later prompt/handoff packets.

It does not generate stories, images or videos by itself.

```text
formal world / character / timeline / chapter sources
                    ↓
local retrieval evidence layer
                    ↓
demand-driven World / Situation / Asset query packets
                    ↓
story master → screenplay → shots → visual/media handoff
```

## 4. Authority Boundaries

### 4.1 Formal truth remains formal

The following remain authoritative:

- formal Repository files and approved source revisions;
- formal SQLite runtime state;
- setting-pack validation/apply rules;
- `AiTurnResultValidator`, `StateReducer`, transactions, `contextHash` and `stateVersion`;
- character knowledge, visibility and author-only boundaries.

The retrieval index is derived and rebuildable. A high similarity score does not make a statement true, Canon, known by a character, approved, or current.

### 4.2 Extraction results are provisional

An extraction candidate must retain at least:

```text
sourceFile
sourceRevision / sourceCommitSha
sourceChunkId
sourceQuote
sourceByteHash
entity / relation / event candidate
confidence
extractorModelId
extractorModelHash
extractedAt
reviewStatus
visibility
```

Extraction may propose entities, facts and relations. It must not directly modify formal Canon, runtime tables, memories, knowledge claims or character state.

### 4.3 Knowledge boundaries apply twice

Visibility filtering must occur:

1. before retrieval, to restrict searchable collections and rows;
2. after reranking, before evidence enters any user-, actor- or cloud-facing packet.

`author_only`, future reveals, director goals, sealed motives and unavailable actor knowledge must not leak through semantic similarity.

## 5. Source Classification And Chunking

Future collections should be explicit rather than one undifferentiated index:

```text
characters
world_rules
locations
timeline
chapter_history
runtime_state
assets
code
development_docs
```

Deterministic sources should be parsed deterministically first:

- Markdown headings and links;
- JSON fields and IDs;
- SQL/runtime identifiers;
- chapter, scene, shot and dialogue IDs;
- file path and Commit metadata.

Natural-language extraction is only a supplement for ambiguous facts, events and relations.

Initial chunk parameters are benchmark starting points:

```yaml
chunk_target_han_characters: 400-800
chunk_overlap_han_characters: 60-100
preserve_heading_path: true
preserve_source_quote: true
preserve_visibility: true
preserve_temporal_scope: true
```

Do not split a character fact, timeline event, dialogue exchange or rule constraint merely to reach a target length.

## 6. Query-Time Hybrid Retrieval

### 6.1 Router

A deterministic-first router should derive:

```json
{
  "collections": ["characters", "timeline", "chapter_history"],
  "entities": ["林月", "掌門"],
  "timeScope": "current",
  "visibilityTarget": "character_visible",
  "queryIntent": "knowledge_state"
}
```

Model-assisted routing may be studied later, but route output must be auditable and may not broaden visibility.

### 6.2 Candidate retrieval

Use two complementary channels:

```text
exact channel:
SQL identifiers + SQLite FTS5/BM25 candidate search

semantic channel:
local Embedding vector candidate search
```

The channels are merged, source-deduplicated and tagged with retrieval reasons before reranking. Exact identifiers such as character IDs, dates, chapter IDs, skills and item names must retain deterministic boosts.

### 6.3 Reranking

Reranker input is restricted to the question and the candidate text needed to judge relevance. It must not receive hidden collections excluded by the router.

Initial benchmark values:

```yaml
embedding_model_candidate: Qwen3-Embedding-0.6B
reranker_model_candidate: Qwen3-Reranker-0.6B

embedding_top_k: 50
reranker_candidates: 20
final_top_k: 8

embedding_batch_size: 4
reranker_batch_size: 1
reranker_max_length: 512
```

These are starting values, not fixed production constants.

## 7. Mandatory Data Foundation Before Model Work

The project must not begin Embedding or Reranker integration while authority,
rebuild and retention remain split across tracked files, ignored databases,
local-only memory and historical evidence. The mandatory first candidate is:

Candidate only:

```text
RETRIEVAL-00 — Authority, Rebuild And Retention Foundation
```

Status:

```text
NOT_ACTIVE / REQUIRES_EXPLICIT_ASSIGNMENT
```

Detailed work package:

```text
docs/work-packages/RETRIEVAL-00_AUTHORITY_REBUILD_RETENTION_FOUNDATION.md
```

### 7.1 Data classes

Every governed item must belong to exactly one primary class:

| Class | Purpose | Authority | Storage rule | Retention rule |
| --- | --- | --- | --- | --- |
| `AUTHORITATIVE_SOURCE` | Raw source, author decision, approved screenplay, formal asset file | Git-reviewed source and explicit approval | Human-reviewable files; binary media remains a file | Keep; version deliberately |
| `FORMAL_STRUCTURED_DATA` | Current entities, relations, timelines, rules, approvals and runtime state | Validated SQLite rows derived from approved sources | SQLite with source revision, source hash and visibility | Keep current plus required audit history |
| `REBUILDABLE_INDEX` | FTS/BM25 chunks, embeddings, retrieval metadata and caches | Never truth; derived from the first two classes | Sidecar SQLite/index selected by later evidence | Delete/rebuild whenever source or model changes |
| `TEST_OR_REVIEW_EVIDENCE` | Fixtures, labeled queries, reviews, regression cases and selection evidence | Test/review authority only | Git when needed for reproducibility | Keep referenced evidence; archive superseded bulk |
| `EPHEMERAL_OR_ARCHIVE_CANDIDATE` | Generated reports, duplicate snapshots, old local DBs and terminal/build output | No current product authority until proven otherwise | Outside formal DB or in a declared archive | Expire, archive or delete only after uniqueness/reference proof |

Images, audio and video are never copied into the formal knowledge database by
default. SQL stores their stable id, repo-relative path, SHA-256, version,
status, provenance and approval evidence.

### 7.2 Authority registry

RETRIEVAL-00 must define one machine-readable authority registry. The future
implementation may use a Git JSON manifest with a validated SQLite mirror, but
it must expose at least:

```text
recordId
primaryClass
repoRelativePath or structuredRecordRef
contentSha256 or logicalContentHash
sourceRevision / sourceCommitSha
authorityStatus
visibility
supersedes / supersededBy
rebuildRecipe
retentionStatus
retentionReason
lastVerifiedAt
```

Private absolute paths are diagnostic only and may not be required to rebuild
the project on another computer.

### 7.3 Fresh-clone rebuild contract

A future approved implementation must provide one documented CLI entrypoint
that starts from a clean Git clone and performs this bounded sequence:

```text
verify tracked source hashes
→ create a new local SQLite database
→ apply all approved additive migrations
→ import approved Setting Packs in declared order
→ import world source archives and chunks
→ rebuild approved exact-search indexes
→ run Canon / provenance / visibility / secret checks
→ emit a logical-content manifest and rebuild report
```

The generated SQLite file remains local and ignored. Rebuild equivalence is
measured by sorted logical row/content hashes, schema version, source hashes
and validation results—not by the binary SQLite file hash, page layout,
timestamps or generated row order.

### 7.4 Retention and anti-bloat policy

Each governed artifact receives one status:

```text
CURRENT
HISTORICAL_REFERENCED
REBUILDABLE_CACHE
ARCHIVE_CANDIDATE
DELETE_CANDIDATE
UNKNOWN_REQUIRES_REVIEW
```

No automated deletion is allowed merely because a file is old, duplicated by
name or absent from the current route. `DELETE_CANDIDATE` requires evidence
that it has no unique content, no live references, no approval/audit duty and
can be reconstructed or recovered from Git history.

The rebuild report must measure at least:

- tracked file count and bytes by primary class;
- duplicate content hashes and duplicate logical records;
- current versus historical Docs/Samples/Memory evidence;
- local ignored database count and age;
- unreferenced artifacts;
- formal SQLite row counts by authority/visibility;
- rebuildable index size and rebuild duration;
- unresolved absolute-path and missing-source references.

### 7.5 RETRIEVAL-00 acceptance gate

- a clean clone can build a usable local SQLite database without a copied
  `data/story.db` or private attachment path;
- all 15 current migrations, five current Chenxing Setting Packs and nine raw
  world sources are represented by an ordered, hash-bound rebuild recipe;
- every formal structured row is traceable to an approved source revision or
  an explicit validated runtime transaction;
- author-only and character-knowledge visibility survive import and querying;
- media stays file-based and is represented in SQL only by metadata;
- ignored/local-only data is either replaced by tracked source, declared as a
  local cache, or marked `UNKNOWN_REQUIRES_REVIEW`;
- no historical file or DB is deleted during the foundation package;
- a logical-content report proves deterministic rebuild equivalence;
- the retention report separates current, historical, rebuildable, archive and
  delete candidates without treating the classification as deletion approval.

Failure blocks RETRIEVAL-01. It does not authorize copying an old database into
Git, broad deletion or silent Canon conversion.

## 8. Second Candidate Work Package

Only after RETRIEVAL-00 passes and receives explicit acceptance:

```text
RETRIEVAL-01 — Offline Local Dual-Model Retrieval Benchmark
```

Status:

```text
NOT_ACTIVE / BLOCKED_BY_RETRIEVAL_00 / REQUIRES_EXPLICIT_ASSIGNMENT
```

### Scope

1. Select a read-only corpus from approved world, character, timeline and chapter sources.
2. Build a labeled query set containing exact-name, semantic-paraphrase, temporal, causal, code/document and knowledge-boundary cases.
3. Record the current LIKE/SQL result as baseline.
4. Add an FTS/BM25 baseline if separately approved.
5. Run Embedding top-50 retrieval.
6. Rerank only the top 20 candidates.
7. Report the final top 8 with scores, source paths, quotes and visibility decisions.
8. Compare quality, latency, peak VRAM and failure cases.

### Minimum evaluation set

At least 50 labeled queries across:

- exact entity/ID lookup;
- paraphrased world facts;
- timeline and current-state questions;
- actor knowledge versus author knowledge;
- chapter continuity;
- false-friend / same-name collisions;
- code and development-document queries.

### Candidate acceptance gate

- zero visibility or author-only leaks;
- every result has source path, source quote and source hash;
- top-50 answer-bearing recall at least 90% on the labeled set;
- semantic-query final top-8 hit rate improves by at least 5 percentage points over the best exact-only baseline;
- exact-ID subset regresses by no more than 2 percentage points;
- same index/model/config produces deterministic ordering within documented tie rules;
- peak GPU memory leaves at least 1 GiB operational headroom;
- P50/P95 indexing and query latency are recorded and explicitly accepted before any production claim.

Failure does not authorize a larger model automatically. It produces evidence for chunking, routing, index, model or hardware decisions.

## 9. Model Upgrade Order

Only after RETRIEVAL-01 passes:

1. tune chunking, routing and exact-search fusion;
2. test a larger Reranker before a larger Embedding model;
3. compare one 4B component at a time;
4. keep the other component at 0.6B during the comparison;
5. record quality gain per latency/VRAM cost.

Do not begin with two FP16 4B models. Their raw weight estimate alone is about 16 GB before runtime overhead, exceeding 11 GB VRAM. Quantized deployment remains a separate compatibility and quality experiment.

## 10. Later Phases

### RETRIEVAL-02 — Collection router and visibility enforcement

- formal collection routing;
- entity/time/knowledge-scope filters;
- query audit and omission reasons;
- no cloud or MCP requirement.

### RETRIEVAL-03 — Provenance-bound entity/relation extraction

- deterministic parsing first;
- optional 1B–3B Q4 extraction candidate;
- strict JSON output;
- provenance and confidence required;
- human/reviewer promotion gate;
- no automatic formal truth writes.

### RETRIEVAL-04 — Cloud-safe evidence handoff

Requires a separate OpenAI/API/privacy decision. The packet must contain only:

```text
question
allowed current state
allowed timeline facts
final 3–8 evidence chunks
source references
explicit unknowns
cloud export audit
```

The full database is not uploaded, but selected chunks still leave the local machine. The accurate privacy statement is therefore “local-first retrieval with bounded cloud evidence,” not “100% local data.”

### RETRIEVAL-05 — Optional MCP integration

MCP is last. It requires current plan/permission verification, threat review, prompt-injection controls, authentication and a deliberate deployment decision. It must not be used to justify a Web Server or open port inside the current Repository.

### NPC-ACTOR-01 — Single NPC Actor Pilot

After RETRIEVAL-00 and RETRIEVAL-01 acceptance, the next product consumer is
the provider-neutral NPC Actor route defined in
`docs/NPC_ACTOR_AND_MODEL_ROUTING_BLUEPRINT_V1.md`. It receives only scoped,
visibility-safe evidence and returns a structured proposal. It cannot write
formal state, and live GPT API use remains behind API Key/cost/privacy approval.

## 11. Cloud And Secret Controls

Before any cloud handoff:

- default `cloudEligible` to false;
- redact API keys, tokens, phone numbers, addresses and user-sensitive identifiers;
- prohibit designated collections from cloud export;
- record exported chunk IDs, hashes, destination and timestamp;
- distinguish author knowledge, character knowledge and user-facing evidence;
- instruct the cloud model to answer `資料不足` when evidence is insufficient;
- require source references for consequential conclusions;
- never allow cloud output to directly write formal SQLite or Canon.

## 12. Repository Integration Boundary

The current Repository remains Kotlin/JVM CLI + SQLite + JSON file protocol.

The first implementation package, if later approved, must choose one compatible adapter:

- local subprocess with JSON files/stdin/stdout;
- a separate explicitly approved sidecar Repository;
- another bounded local interface that does not open a server port.

This blueprint does not authorize Python dependencies, Docker, Ktor/Spring/FastAPI/Express, REST endpoints, direct OpenAI API calls or model binaries committed to Git.

The existing `MemoryRetriever`, Context budget, `knowledgeClaims`, Validator and StateReducer remain unchanged until a separate runtime-integration decision and regression suite exist.

## 13. Download And Supply-Chain Gate

Before any model or external Repository is downloaded or executed:

- verify official source and exact revision;
- record license and model license;
- inspect README, dependency manifests and executable scripts statically;
- pin model name, revision and file hashes;
- prohibit arbitrary post-install scripts;
- define storage outside Git;
- define offline deletion/rollback;
- obtain explicit assignment for disk, network and GPU use.

## 14. Non-Goals

This blueprint does not approve:

- GBrain integration;
- model installation or execution;
- two 4B models running together;
- extraction results becoming Canon;
- removal of SQL/FTS search;
- direct changes to runtime memory or knowledge;
- OpenAI API, ChatGPT local-host access or MCP Tunnel;
- Web Server, REST, open ports or public deployment;
- image/video/audio generation;
- automatic next-work-package execution.

## 15. Evidence Sources

Official sources verified on 2026-07-13:

- Qwen3 Embedding architecture and model sizes: `https://qwenlm.github.io/blog/qwen3-embedding/`
- Qwen3 Embedding official Repository: `https://github.com/QwenLM/Qwen3-Embedding`
- GBrain official Repository: `https://github.com/garrytan/gbrain`
- NVIDIA GTX 1080 Ti specifications: `https://www.nvidia.com/en-us/geforce/news/nvidia-geforce-gtx-1080-ti/`
- NVIDIA Pascal mixed-precision notes: `https://developer.nvidia.com/blog/mixed-precision-programming-cuda-8/`
- OpenAI Developer Mode and MCP apps: `https://help.openai.com/en/articles/12584461`

External product facts must be reverified before implementation because model support, runtimes, drivers and plan permissions may change.
