# NPC-ACTOR-09C — One Supervised Real Rejection Diagnosis

## Objective

Run exactly one local Qwen3-8B formal candidate preparation against a fresh valid offscreen non-Canon Session.
Persist either the candidate package or the sanitized NPC-ACTOR-09B rejection sidecar to GitHub, then stop before
resolve.

## Required flow

```text
verified NPC-ACTOR-09A seed
→ fresh ignored SQLite database
→ official init-db / seed-demo / start-session
→ official pre-prepare Session evidence
→ hard location and pending-Context Gate
→ exactly one prepare-local-npc-actor-formal-turn command
→ candidate OR sanitized rejection sidecar
→ official post-prepare Session evidence
→ JSON / Schema / Hash / secret / Git verification
→ Commit and Push
→ stop before resolve
```

## Model and runtime

- Model: `Qwen3-8B-Q5_K_M.gguf`, GGUF Q5_K_M, expected SHA-256
  `068bae163faa96ad48032daf4e071a6a28fe67d8dcc95367609c2ff165e52738`.
- Runtime: `llama.cpp-b10002-vulkan/llama-cli.exe`, expected SHA-256
  `f0741939ea8adecc58db40cfeabd8526d156878fdaa1ab2c8c1b6de08bdd8d9e`.
- Direct local process only; no server, port, cloud API or paid Provider.
- Do not substitute the separately installed 1.7B, embedding or reranker models.

## Hard gates

Before model invocation, official evidence must prove:

1. `char-player` is at `loc-npc09a-rear-hall`;
2. `char-shen-shuang` is at `loc-npc09a-old-corridor`;
3. those locations differ;
4. Session state version is `0`, next turn is `1`, and no pending Context exists.

If any gate fails, stop without invoking the model.

## Result handling

- Candidate success: commit the candidate package and post-prepare Session evidence; do not resolve. Product Owner
  review of that exact candidate is required.
- Safety rejection: commit the sanitized rejection sidecar and post-prepare Session evidence; do not retry.
- Program or environment failure: preserve non-secret error evidence, abort any pending Context through official
  behavior, and stop without a second prepare.

## Allowed repository changes

- `PLANS.md`, `PROJECT_STATE.md`, `.codex/project_controller.md`;
- this work package and `docs/npc_actor/NPC_ACTOR09C_REAL_REJECTION_DIAGNOSIS_REPORT.md`;
- `samples/npc_actor09c/**` evidence only.

## Forbidden

- Production Kotlin, tests, migrations, Repository, validators, StateReducer or safety thresholds;
- direct SQL, reused failed Session, committed SQLite database or absolute local model paths;
- resolve, automatic approval, Canon／Memory／Knowledge writes;
- second model prepare, prompt tuning, cloud API, server／port, media or next chapter.

## Completion

- Status: `SAFE_PROGRAM_REJECTION / NO_CANDIDATE / NO_RESOLVE`.
- Exactly one Qwen3-8B prepare ran against a fresh valid offscreen Session.
- The proposal passed the local safety loop and Game Master, then formal Runtime validation rejected it with
  `low_character_fit` because `characterActions[0].characterFitScore < 70`.
- No candidate and no NPC-ACTOR-09B safety sidecar were written. Existing failure cleanup cleared the pending
  Context; official before／after Session evidence is byte-identical.
- A sanitized program-failure evidence file records only the stage, fixed error code, invocation metadata and
  unchanged state. It contains no Prompt, model text, author secret or physical snapshot.
- No retry, resolve, Production Kotlin, Test, Migration, cloud API, server／port, Canon or media change occurred.
- Next package, if separately authorized: `NPC-ACTOR-09D_POST_SAFETY_VALIDATION_OBSERVABILITY`.
