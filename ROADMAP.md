# Project Roadmap

This roadmap controls stable project completion. It is intentionally conservative: one milestone at a time, no unrelated technology, and no commit before verification passes.

## Current Rule

- Stable baseline: MVP-6 checkpoint branching runtime, installDist packaging repair, local bundle backup, private remote push, remote restore verification, handoff index, and project completion audit are complete.
- Active product milestone: none after MVP-7 Candidate A.2 implementation, docs checkpoint alignment, real sample rerun, JSON robustness investigation, JSON verification docs hardening, diverse audit sample rerun, the next-candidate decision brief, and Pilot-2 scenario design.
- MVP-7 Candidate A, Candidate A.1, real audit sample collection, deliberately flawed audit sample pack, Candidate A.2 calibration planning, Candidate A.2 implementation, A.2 real sample rerun, audit JSON robustness investigation, audit JSON verification docs hardening, automatic loop bootstrap, diverse audit sample rerun, the next-candidate decision brief, and Pilot-2 scenario design are complete or selected as documentation-only work.
- Candidate A.2 remains read-only audit calibration. It did not approve Candidate B and did not alter `resolve-turn`, `StateReducer`, SQLite writes, Validator, `stateVersion`, or `contextHash`.
- Docs-only product contracts after Pilot-2 are complete through authoring quality guidelines, narrative review workflow, scene lock / animation asset boundary, script-ready scene packet contracts, and the Qingli scene registry seed brief.
- 04 / 05 / 06 are paused. They are not active runtime, not automation, and not an approved generation loop.
- Resuming 04 / 05 / 06 must use manual handoff only: `01 explicit handoff -> 04 generation -> 05 review -> 06 display -> user explicit approval -> report to 01 -> 01 assigns next step`.
- Do not use automation / scheduler wakeups to advance the 04 / 05 / 06 flow. Silence, ambiguity, or lack of response is not approval.
- Next milestone options after the script-ready contracts should favor formalizing/spec verification, docs consolidation, or read-only checks. Do not enter Candidate B, MVP-8, runtime enforcement, or Oiioii integration directly.
- MVP-7 implementation beyond explicitly approved read-only audit work is not approved. Any future Candidate B or gate behavior requires a separate decision brief and approval.

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
| MVP-7 Candidate A.2 Audit Calibration | Completed | Implement the approved read-only calibration for emotion telling, subtle NPC toolman detection, and consequence false-positive reduction. | `NarrativeQualityAuditService.kt`, `Mvp7NarrativeQualityAuditTest.kt`, `PLANS.md`. | Making warnings too noisy, treating advisory findings as Validator failures, touching runtime write paths. |
| MVP-7 Candidate A.2 Docs Checkpoint Alignment | Completed | Align status docs after A.2 implementation was pushed and post-push checkpointed. | `ROADMAP.md`, `PLANS.md`, `VERIFY.md`, `docs/MVP7_CANDIDATE_A2_CALIBRATION_PLAN.md`, `docs/MVP7_FLAWED_AUDIT_SAMPLE_PACK_REPORT.md`. | Accidentally implying Candidate B approval or rewriting historical sample results. |
| MVP-7 Candidate A.2 Real Sample Rerun | Completed | Rerun the read-only audit on real and near-real Pilot/Pilot-1b/MVP-6 samples after A.2 calibration. | `docs/MVP7_A2_REAL_SAMPLE_RERUN_REPORT.md`, `PLANS.md`. | Treating advisory warnings as gates, mistaking fixture warnings for story quality failures. |
| MVP-7 Audit JSON Robustness Investigation | Completed | Determine whether audit reports were invalid JSON or misread by verification tooling. | `docs/MVP7_AUDIT_JSON_ROBUSTNESS_INVESTIGATION.md`, `PLANS.md`. | Fixing Kotlin unnecessarily, confusing PowerShell decoding failure with malformed output. |
| MVP-7 Audit JSON Verification Docs Hardening | Completed | Document correct UTF-8 JSON verification commands for audit reports, especially PowerShell 5.1. | `VERIFY.md`, `docs/MVP7_AUDIT_JSON_ROBUSTNESS_INVESTIGATION.md`, `docs/MVP7_A2_REAL_SAMPLE_RERUN_REPORT.md`, `PLANS.md`. | Overstating a product bug, forgetting the PowerShell 5.1 encoding caveat. |
| Automatic Stable-Completion Loop Bootstrap | Completed | Start the authorized automatic loop and push the verified docs-hardening state to the current branch. | `PLANS.md`, remote sync verification. | Letting automation cross product-decision boundaries or enter Candidate B without approval. |
| MVP-7 Diverse Audit Sample Rerun | Completed | Run the read-only audit on additional non-Pilot sample families to check A.2 stability beyond the first Pilot sample set. | `docs/MVP7_DIVERSE_AUDIT_SAMPLE_RERUN_REPORT.md`, `PLANS.md`, ignored `out/mvp7_diverse_audit_rerun/` reports. | Treating synthetic acceptance fixtures as Candidate B proof, committing ignored reports. |
| MVP-7 Next Candidate Decision Brief | Completed locally | Compare Candidate A.3, Candidate B, Pilot-2, more real samples, and UI/product direction without implementation. | `docs/MVP7_NEXT_CANDIDATE_DECISION_BRIEF.md`, `PLANS.md`, `ROADMAP.md`. | Accidentally implying Candidate B approval or starting implementation from a decision document. |
| Pilot-2 Scenario Design | Completed locally | Define a real story-arc sample collection scenario, human turn format, per-turn evidence requirements, naming rules, audit flow, and pass/fail criteria. | `docs/PILOT2_SCENARIO_DESIGN.md`, `PLANS.md`, `ROADMAP.md`. | Accidentally implying Candidate B approval, hardcoding a story result, or treating scenario design as runtime implementation. |
| Authoring Quality Guidelines / Review Workflow | Completed and pushed | Preserve 04 playtest lessons as docs-only authoring guidance without turning them into runtime gates. | `docs/AUTHORING_QUALITY_GUIDELINES.md`, `PLANS.md`. | Hardcoding Qingli/Ai-chan specifics into engine core, implying runtime approval. |
| Narrative Review Gate And Eight-Point Checklist | Completed and pushed | Define draft -> self-check -> review gate -> minimal revision -> user-facing text and a fixed review checklist. | `docs/AUTHORING_QUALITY_GUIDELINES.md`, `PLANS.md`. | Treating review guidance as Validator behavior or letting users become first-line copyeditors. |
| Scene Lock And Animation Asset Boundary | Completed and pushed | Separate runtime scene continuity from production scene / animation asset planning. | `docs/SCENE_LOCK_AND_ANIMATION_ASSET_BOUNDARY.md`, `docs/AUTHORING_QUALITY_GUIDELINES.md`, `PLANS.md`. | Treating runtime transitions as approval for unlimited animation scenes. |
| Script-Ready Scene Packet Contracts | Completed and pushed | Define script-ready scene packet, scene registry, 04/05/06 contracts, manual human approval gate, and 01/02/03 loop. | `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`, `docs/AUTHORING_QUALITY_GUIDELINES.md`, `docs/SCENE_LOCK_AND_ANIMATION_ASSET_BOUNDARY.md`, `PLANS.md`. | Accidentally resuming 04/05/06, assuming user silence is approval, or turning contracts into runtime/schema gates. |
| Qingli Scene Registry Seed Brief | Completed and pushed | Create conservative seed registry entries for the four formal Pilot-2 scene ids as docs-only reference for future 04/05/06, script packet, and production planning work. | `docs/QINGLI_SCENE_REGISTRY_SEED_BRIEF.md`, `docs/SCENE_LOCK_AND_ANIMATION_ASSET_BOUNDARY.md`, `PLANS.md`. | Treating the seed brief as a formal setting-pack patch, runtime scene definition, approved animation asset lock, Oiioii integration, or approval to resume 04/05/06. |

## Temporarily Not Doing

- MVP-7 implementation beyond explicitly approved read-only Candidate A/A.1/A.2 work.
- Candidate A.3 unless explicitly approved as a separate milestone.
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
- Automation / scheduler advancement for 04 / 05 / 06 handoff.
- 04 / 05 / 06 generation, review, or display until 01 explicitly resumes that workflow.
- Oiioii integration or animation asset pipeline implementation.

## Selection Rule For The Next Milestone

Choose the smallest milestone that closes an already documented gap. A valid next milestone must name:

- confirmed facts from docs, tests, and code,
- assumptions or guesses marked as `推測`,
- files likely to change,
- required tests,
- required CLI E2E,
- commit message.

If a candidate milestone would require new infrastructure, it must be rejected unless a roadmap update explicitly approves that infrastructure first.

Recommended candidates after audit JSON verification docs hardening, automatic loop bootstrap, diverse audit sample rerun, the next-candidate decision brief, Pilot-2 scenario design, script-ready product contracts, script packet contract verification, and the Qingli scene registry seed brief:

1. `scene packet sample / registry dry-run review`: docs-only sample or review pass using the approved seed brief; requires 01 explicit handoff and must not resume 04/05/06 actual generation/display.
2. `Pilot-2 human turn collection`: only after the user/player provides explicit input or approval, then run the approved story-turn/evidence/audit flow without entering Candidate B.
3. `collect real author-led story arc samples`: requires user approval or user-provided material outside the existing Pilot-1/Pilot-1b/MVP-4/MVP-6 fixture families.
4. `Candidate A.3 read-only audit brief`: define a focused read-only target only after separate explicit approval; do not implement by default.
5. `remote restore smoke on demand`: rerun clone/test verification when needed.
