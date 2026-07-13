# RETRIEVAL-01 Supply-Chain Review

Status: `STATIC_REVIEW_PASS / DOWNLOAD_NOT_AUTHORIZED`

Date: `2026-07-13`

## Official candidates

The official Qwen Hugging Face repositories were inspected without downloading
model weights.

### Embedding

```text
model: Qwen/Qwen3-Embedding-0.6B
revision: 97b0c614be4d77ee51c0cef4e5f07c00f9eb65b3
license: Apache-2.0
gated: false
model.safetensors bytes: 1,191,586,416
model.safetensors SHA-256:
0437e45c94563b09e13cb7a64478fc406947a93cb34a7e05870fc8dcd48e23fd
tokenizer.json bytes: 11,423,705
tokenizer.json SHA-256:
def76fb086971c7867b829c23a26261e38d9d74e02139253b38aeb9df8b4b50a
```

### Reranker

```text
model: Qwen/Qwen3-Reranker-0.6B
revision: e61197ed45024b0ed8a2d74b80b4d909f1255473
license: Apache-2.0
gated: false
model.safetensors bytes: 1,191,588,280
model.safetensors SHA-256:
27cd75a405b9c1b46b59abfd88aaa209e6fed2a1972cde9b70e7659537c5e65b
tokenizer.json bytes: 11,422,654
tokenizer.json SHA-256:
aeb13307a71acd8fe81861d94ad54ab689df773318809eed3cbe794b4492dae4
```

Official implementation reference:

```text
https://github.com/QwenLM/Qwen3-Embedding
revision: 44548aa5f0a0aed1c76d64e19afe47727a325b8f
```

Official sources:

- `https://huggingface.co/Qwen/Qwen3-Embedding-0.6B`
- `https://huggingface.co/Qwen/Qwen3-Reranker-0.6B`
- `https://github.com/QwenLM/Qwen3-Embedding`
- `https://qwenlm.github.io/blog/qwen3-embedding/`

## Local machine evidence

```text
GPU: NVIDIA GeForce GTX 1080 Ti
VRAM: 11264 MiB
driver: 576.88
compute capability: 6.1 (Pascal)
Python runtime: not installed; Windows Store aliases only
Node.js: installed
```

The two BF16 weight files total approximately 2.38 GB before tokenizer,
framework, activations and CUDA allocator overhead. Capacity appears plausible
but is not a performance or compatibility result.

## Runtime decision

The official examples require `transformers>=4.51.0`; Sentence Transformers
examples also require `sentence-transformers>=2.7.0`. The official Reranker
path is a causal-LM yes/no scoring flow, not a plain cosine encoder.

Current decision:

```text
ISOLATED_EXTERNAL_TRANSFORMERS_SUBPROCESS_CANDIDATE
```

Reasons:

- it can use both official Safetensors repositories;
- it avoids an unverified third-party Reranker quantization;
- it can expose a bounded stdin/stdout JSON protocol without a port;
- it keeps Python, model cache and CUDA packages outside the Kotlin/Gradle
  runtime and outside Git.

This is not installation approval. Exact Python, PyTorch, CUDA and Transformers
versions must be resolved in the isolated environment and recorded before
execution.

## Rejected or deferred paths

- `llama-server`: rejected because the Repository forbids opening a port.
- official Embedding GGUF + third-party Reranker GGUF: deferred because Qwen
  publishes an official Embedding GGUF but no matching official Reranker GGUF
  was found in the official Qwen collection.
- FlashAttention: prohibited for this pilot; the GTX 1080 Ti is Pascal and the
  Qwen guidance treats FlashAttention as optional.
- vLLM/Docker/GBrain/MCP: outside scope.
- 4B/8B models: not an automatic fallback.

## Download and rollback plan

If later approved:

- use a new isolated directory outside the Repository;
- pin all package and model revisions;
- disable arbitrary post-install/project scripts where supported;
- verify the two model and tokenizer SHA-256 values above;
- set Hugging Face cache to that isolated directory;
- never copy weights, caches or environment secrets into Git;
- rollback by stopping the subprocess and deleting only the verified isolated
  directory after showing its resolved absolute path.

## Current verdict

```text
STATIC_SUPPLY_CHAIN_REVIEW_PASS
MODEL_DOWNLOAD_NOT_AUTHORIZED
MODEL_EXECUTION_NOT_STARTED
```
