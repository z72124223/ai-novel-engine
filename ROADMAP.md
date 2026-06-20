# Project Roadmap

This roadmap controls stable project completion. It is intentionally conservative: one milestone at a time, no unrelated technology, and no commit before verification passes.

## Current Rule

- Stable baseline: MVP-6 checkpoint branching runtime, installDist packaging repair, local bundle backup, private remote push, remote restore verification, handoff index, and project completion audit are complete.
- Active product milestone: none after MVP-7 Candidate A.2 calibration planning.
- MVP-7 Candidate A, Candidate A.1, real audit sample collection, and deliberately flawed audit sample pack are complete and pushed through `02f146526812c7c2bda1b882583b553da2fcbdbb`.
- Candidate A.2 calibration planning documents a possible next read-only audit refinement, but does not approve implementation.
- Next milestone options: explicitly approve Candidate A.2 implementation, collect a more diverse audit sample set, or define a new explicit MVP-7 candidate. Do not enter Candidate B yet.
- MVP-7 implementation beyond explicitly approved read-only audit work is not approved. Candidate A/A.1/A.2 work must stay read-only and must not affect `resolve-turn`, `StateReducer`, SQLite writes, Validator, `stateVersion`, or `contextHash`.

## Milestone Table

| Milestone | Status | Purpose | Scope | Main Risks |
| --- | --- | --- | --- | --- |
| MVP-1 Local CLI Runtime | Completed | Prove the smallest local story loop: player input, context JSON, AI result JSON, validation, state apply, SQLite persistence, narrative output. | Kotlin/JVM CLI, SQLite schema, base models, validator, reducer, samples, schema/seed/context/validation/reducer/stall tests. | Forbidden reveal leaks, fake or empty services, schema drift. |
| MVP-2 Reliable Multi-turn Runtime | Completed | Make turns resumable and safe across sessions. | `story_sessions`, pending contexts, `stateVersion`, `contextHash`, result idempotency, transaction rollback, five-turn acceptance. | Stale context apply, duplicate result writes, partial transaction writes. |
| MVP-3 Memory and Knowledge Runtime | Completed | Separate canon facts from character belief and memory. | Runtime memories, knowledge claims, recall logs, emotion deltas, memory candidates, knowledge boundary validation. | Characters using facts they do not know, memory leaks, recall writes before validation. |
| MVP-4 Scene Thread World Event Runtime | Completed | Add scene lifecycle, story threads, legal transitions and scheduled pressure. | Scene instances, requirements, transitions, story threads, world clock, scheduled events, continuity checks. | Forced transitions, director goals becoming character intent, unreachable items/actors. |
| MVP-5 Setting Pack Dependency Runtime | Completed | Import canon safely through local JSON setting packs. | Parse, validate, diff, impact preview, transaction apply, revisions, dependency graph, active session patch guard. | Author-only secret leaks, silent canon mutation during active sessions, full setting pack flooding context. |
| Pilot-1 Stabilization | Completed | Repair real-use blockers found during Pilot-1. | UTF-8 context byte hard limit, runtime character initialization, read-only audit/export/preflight, context budget forensics. | Raising byte limit, using old Pilot evidence, pending context residue. |
| Pilot-1 Context Repairs | Completed | Keep final context compact but semantically useful. | Metadata normalization, context relevance floor, capability contracts. | Empty setting context, recent facts pretending to be recalled memories, capability use from description only. |
| Pilot-1b Coverage Repair | Completed | Repair Pilot-1 coverage gaps without entering MVP-6. | Setting pack runtime topology, legal transition coverage, scheduled world event pressure, setting context precision. | Hardcoded E2E behavior, forced transition, scheduled event pressure omitted from context. |
| MVP-6 Checkpoint Branching Runtime | Completed | Add safe local alternate timeline exploration. | Additive `006` migration, checkpoint snapshots, one active branch per project, fork, checkout, redacted timeline export, replay audit. | Destructive rewind, branch writes on inactive session, snapshot restore cascading canon deletes, hidden secret export. |
| MVP-6 CLI Acceptance Evidence | Completed | Prove MVP-6 works through real CLI commands, not only unit tests. | Fresh demo DB, create checkpoint, fork session, checkout branch, show timeline, verify replay chain, export timeline, document expected results. | CLI docs drifting from real syntax, E2E relying on uncommitted data, output paths accidentally tracked. |
| installDist Packaging Repair | Completed | Make generated Windows distribution launch the CLI and load packaged migrations. | Restore app JAR generation while preserving OneDrive build safeguards; verify distribution `help` and `init-db`. | Reintroducing OneDrive reparse-file build issues, broken packaged resource loading. |
| Local And Remote Backup | Completed | Protect the stable repo state outside the working tree. | Local Git bundle, private GitHub remote, pushed stable branches and tags, remote restore clone and test. | Private repo accidentally made public, generated data pushed by mistake. |
| Handoff And Completion Audit | Completed | Make the project restartable by future Codex windows. | `docs/HANDOFF.md`, `docs/PROJECT_COMPLETION_AUDIT.md`, remote restore evidence, next-milestone guidance. | Handoff docs drifting after each commit. |
| Project Control Docs Refresh | Completed | Align control docs with the verified post-MVP-6 state. | `ROADMAP.md`, `docs/ROADMAP.md`, `docs/HANDOFF.md`, `PLANS.md`. | Accidentally implying MVP-7 approval or changing product scope. |
| Blueprint Alignment | Completed | Preserve useful external blueprint intent while rejecting stale operational state. | `docs/BLUEPRINT_ALIGNMENT.md`, `docs/MVP7_DECISION_BRIEF.md`, `PLANS.md`. | Treating old Pilot/MVP-6 notes as current facts. |
| MVP-7 Narrative Quality Spec Draft | Completed | Define narrative quality, NPC liveliness, AI-feeling output, style constraints, verifiable checks and implementation decision gates. | `docs/MVP7_NARRATIVE_QUALITY_SPEC.md`, `ROADMAP.md`, `PLANS.md`. | Accidentally starting MVP-7 implementation or making subjective prose rules brittle. |
| MVP-7 Candidate A Narrative Quality Audit | Completed | Add the lowest-risk MVP-7 entry point: a read-only report over context/result/evidence JSON. | Report models, read-only audit service, `audit-narrative-quality` CLI, tests, docs. | Treating subjective warnings as hard Validator failures, writing SQLite, or expanding into story rewriting. |
| MVP-7 Candidate A.1 Audit Calibration Refinements | Completed | Reduce a known false positive and add advisory context-quality observations found during calibration. | Negated convenient-compliance matching, thin-setting / missing-transition / missing-event / stasis observations, tests, docs. | Turning coverage observations into hard validation, making warnings too noisy, drifting into Candidate B. |
| MVP-7 Candidate A.1 Docs Checkpoint Alignment | Completed | Record that Candidate A.1 is complete, pushed, and post-push checkpointed. | `ROADMAP.md`, `PLANS.md`, `docs/MVP7_DECISION_BRIEF.md`, `docs/MVP7_AUDIT_CALIBRATION_REPORT.md`. | Accidentally implying Candidate B approval or changing runtime scope. |
| MVP-7 Audit Sample Collection | Completed | Run the existing read-only audit on more real or near-real Pilot turns and document calibration behavior. | `docs/MVP7_AUDIT_SAMPLE_COLLECTION_REPORT.md`, `PLANS.md`. | Treating one story arc as broad quality proof, overreacting to advisory context-quality observations. |
| MVP-7 Flawed Audit Sample Pack | Completed | Add deliberately bad fixtures to test whether the audit catches obvious narrative-quality problems and false-positive guards. | `samples/mvp7_flawed_audit_pack/`, `docs/MVP7_FLAWED_AUDIT_SAMPLE_PACK_REPORT.md`, `PLANS.md`. | Mistaking fixture results for runtime validation readiness, drifting into rule changes. |
| MVP-7 Candidate A.2 Calibration Planning | Completed | Plan a narrow future calibration pass for emotion telling, subtle NPC toolman detection, and consequence false-positive reduction. | `docs/MVP7_CANDIDATE_A2_CALIBRATION_PLAN.md`, `ROADMAP.md`, `PLANS.md`. | Accidentally approving implementation, Candidate B, or runtime write-path changes. |

## Temporarily Not Doing

- MVP-7 implementation beyond explicitly approved read-only Candidate A/A.1 work and documentation-only A.2 planning.
- Candidate A.2 implementation unless explicitly approved as a separate milestone.
- Candidate B or any pre-resolve narrative quality gate.
- Web server, Web UI, HTTP API, port listener.
- Android UI.
- OpenAI API integration or external AI calls.
- Rust.
- Docker.
- Embedding, vector database, semantic search infrastructure.
- Multi-agent runtime.
- Timeline UI, replay UI, graphical branch browser.
- Branch deletion, retagging, or broad branch hygiene unless selected as a milestone.
- GitHub branch protection or repository settings changes unless selected as a milestone.

## Selection Rule For The Next Milestone

Choose the smallest milestone that closes an already documented gap. A valid next milestone must name:

- confirmed facts from docs, tests, and code,
- assumptions or guesses marked as `推測`,
- files likely to change,
- required tests,
- required CLI E2E,
- commit message.

If a candidate milestone would require new infrastructure, it must be rejected unless a roadmap update explicitly approves that infrastructure first.

Recommended candidates after Candidate A.2 planning:

1. `implement Candidate A.2 calibration`: only if explicitly approved; keep it read-only and limited to emotion telling, subtle NPC toolman detection, and consequence false-positive reduction.
2. `collect more diverse audit samples`: run the read-only audit on Pilot-1b, MVP-6 branch/fork, or other non-demo turns before considering stronger gates.
3. `define a new explicit MVP-7 candidate`: write a focused brief before implementation; Candidate B is not the default next step.
4. `remote restore smoke on demand`: rerun clone/test verification when needed.
