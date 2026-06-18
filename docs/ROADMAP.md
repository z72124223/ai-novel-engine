# Roadmap

## v1

- Kotlin/JVM CLI。
- SQLite schema migration。
- JSON context/result protocol。
- Demo seed。
- Validator + reducer。
- Minimal stall detection。
- Unit tests for schema、seed、context、validation、reducer、stall detector。

## MVP-2

- Session based multi-turn runtime.
- Pending context lifecycle.
- Stable context hash.
- `resultId` idempotency.
- Transaction rollback for resolve.
- Recent turn summaries capped at three.
- Stall pressure fields in context.
- Five-turn local CLI acceptance.

## MVP-3

- Deterministic character memory retrieval.
- Character knowledge claims separate from canonical facts.
- Knowledge-boundary validation for character action.
- Context budget fields and truncation accounting.
- Runtime memory candidates accepted only by the engine.
- Belief conflict handling without silent overwrite.
- Emotion deltas with trigger/source/reason.
- Recall logs and recall count updates only after successful resolve.
- Four-turn local memory/knowledge acceptance.

## Later

- Richer dependency graph queries.
- More granular visibility levels for secrets and facts.
- Replay/export tooling for turn logs.
- Optional human review queues for high-risk deltas.

Non-goals remain unchanged unless explicitly revised: no web server, no port, no OpenAI API integration, no Android UI, no Rust.
