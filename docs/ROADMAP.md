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

## MVP-4

- Scene instances with visit numbers and lifecycle status.
- Scene requirements with evidence-backed progress.
- Story thread state without leaking director goals into character intent.
- Location-aware transition preview and validation.
- Scheduled world events and one-time acknowledgement/consume.
- World clock and travel-time accounting inside resolve transaction.
- Continuity constraints for actor location, item reachability and closed-scene summaries.
- Seven-turn cross-scene acceptance and twenty-turn deterministic soak.

## MVP-5

- Local setting pack JSON parse, validate, diff, impact preview and transaction apply.
- Additive `005_setting_pack_runtime.sql` migration.
- `setting_packs`, `setting_pack_imports`, `entity_revisions`, validation and impact report tables.
- Dependency edge provenance, visibility, runtime relevance and canonical hash fields.
- Stable entity IDs and monotonic `entityVersion` checks.
- Active-session guard for `PATCH_CANON`.
- Author-only secret boundary for character initial knowledge and turn context.
- Relevant setting excerpts in `ContextPacket` with an 8192-byte setting budget.
- Setting pack export/import UTF-8 round trip.
- 100-entity deterministic scale fixture.

## Later

- Non-blocking technical debt: `:app:installDist` currently generates scripts that cannot run because the app jar task is disabled to avoid OneDrive reparse-file issues during builds. CLI acceptance can still run through Gradle or the compiled `MainKt` classpath. Revisit packaging separately without changing MVP-4 runtime behavior.
- Richer dependency graph queries and persisted validation/impact run history.
- More granular visibility levels for secrets and facts.
- Replay/export tooling for turn logs.
- Optional human review queues for high-risk deltas.

Non-goals remain unchanged unless explicitly revised: no web server, no port, no OpenAI API integration, no Android UI, no Rust.
