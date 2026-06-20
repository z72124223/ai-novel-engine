# Roadmap

This file summarizes product/runtime evolution. The root `ROADMAP.md` is the control roadmap for current milestone selection.

## v1

- Kotlin/JVM CLI.
- SQLite schema migration.
- JSON context/result protocol.
- Demo seed.
- Validator + reducer.
- Minimal stall detection.
- Unit tests for schema, seed, context, validation, reducer, stall detector.

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

## MVP-6

- Local checkpoint creation for session state.
- Branch timeline metadata with one active writable branch per project.
- Non-destructive rewind by forking a new session from a checkpoint.
- Branch checkout through explicit CLI command and transaction restore.
- Replay-chain verification for applied turn logs, state versions, result IDs and context hashes.
- Redacted timeline export that does not expose author-only or unrevealed secret content.
- Additive `006_checkpoint_branching_runtime.sql` migration only.
- Full old-test preservation plus branch/fork/replay E2E acceptance.
- CLI acceptance guide: `docs/MVP6_CLI_ACCEPTANCE.md`.
- Release tag: `mvp-6`.

## Post-MVP-6 Stabilization

- `:app:installDist` packaging repair is complete.
- Generated distribution now includes the app JAR and can launch the CLI.
- Distribution smoke tests covered `help` and `init-db` with packaged migrations.
- Local Git bundle backup was created and verified.
- Private GitHub remote was created at `https://github.com/z72124223/ai-novel-engine.git`.
- Stable branches and tags were pushed.
- Remote restore clone ran `gradlew.bat clean test --no-daemon`; the latest verified baseline is 233 tests, 0 failures, 0 errors, 0 skipped.
- Handoff and completion audit docs now exist:
  - `docs/HANDOFF.md`
  - `docs/PROJECT_COMPLETION_AUDIT.md`

## MVP-7 Read-Only Narrative Quality Audit Track

MVP-7 has not become a runtime gate. The approved work so far is a read-only audit track:

- Narrative quality spec draft: `docs/MVP7_NARRATIVE_QUALITY_SPEC.md`.
- Candidate A read-only `audit-narrative-quality` CLI.
- Candidate A.1 calibration refinements for advisory context-quality observations.
- Real audit sample collection: `docs/MVP7_AUDIT_SAMPLE_COLLECTION_REPORT.md`.
- Deliberately flawed audit sample pack: `samples/mvp7_flawed_audit_pack/`.
- Candidate A.2 calibration planning and read-only implementation.
- Candidate A.2 real sample rerun: `docs/MVP7_A2_REAL_SAMPLE_RERUN_REPORT.md`.
- Audit JSON robustness investigation: `docs/MVP7_AUDIT_JSON_ROBUSTNESS_INVESTIGATION.md`.
- Audit JSON verification hardening in `VERIFY.md`; PowerShell 5.1 must read reports with explicit UTF-8.
- Diverse non-Pilot audit sample rerun: `docs/MVP7_DIVERSE_AUDIT_SAMPLE_RERUN_REPORT.md`.

Candidate B, any pre-resolve gate, and any write-path integration remain unapproved.

## Later

- Richer dependency graph queries and persisted validation/impact run history.
- More granular visibility levels for secrets and facts.
- Replay/export tooling for turn logs.
- Optional human review queues for high-risk deltas.
- Optional GitHub branch protection or repository settings hardening.
- Further MVP-7 work only after an explicit user-approved candidate brief.

Non-goals remain unchanged unless explicitly revised: no web server, no port, no OpenAI API integration, no Android UI, no Rust, no Docker, no embedding/vector DB.
