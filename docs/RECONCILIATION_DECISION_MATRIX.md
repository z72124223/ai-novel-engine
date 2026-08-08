# WP-001F-1 Reconciliation Decision Matrix

This matrix records one recommendation per exceptional baseline entry. It is not an execution queue. The allowed actions are advisory only and require a later clean-worktree package and, where stated, human review.

| Path | Cluster | Status | Local unique value | Remote unique value | Dependency risk | Recommended action | Confidence |
|---|---|---|---|---|---|---|---|
| `PLANS.md` | Repository Governance | `REQUIRES_MANUAL_MERGE` | Dirty-worktree audit trail and local historical control context | Current MVP-8 plan window, migration/model/CLI direction | Very high; mixed 20k-line control history | `MANUAL_MERGE_REQUIRED` | HIGH |
| `app/src/main/kotlin/com/localnovel/engine/Main.kt` | Core Integration | `REQUIRES_MANUAL_MERGE` | 96 local-only handler/wiring declarations and service CLI registration | Separate remote pipeline CLIs and a smaller core entry surface | Very high; CLI, repository, Validator, StateReducer and ContextPacket | `MANUAL_MERGE_REQUIRED` | HIGH |
| `app/src/main/kotlin/com/localnovel/engine/model/Models.kt` | Core Integration | `REQUIRES_MANUAL_MERGE` | 26 storyboard/planning model types | 43 audio/comic/delivery/provisional/narrative model types | Very high; shared model compatibility | `MANUAL_MERGE_REQUIRED` | HIGH |
| `app/src/main/kotlin/com/localnovel/engine/service/CandidateReferenceAcceptancePacketService.kt` | Candidate Reference | `REMOTE_SUPERSEDES_LOCAL` | No local-only public symbol; older validation surface | Source-file hash comparison and identity digest helpers | High; packet validation and its test contract | `KEEP_REMOTE` | HIGH |
| `docs/HANDOFF.md` | Repository Governance | `REMOTE_SUPERSEDES_LOCAL` | Older stable-point wording | Current first-read override and route-control instructions | Medium; controls future windows | `KEEP_REMOTE` | HIGH |
| `docs/PROJECT_STRUCTURE_REPORT.md` | Repository Governance | `LOCAL_HAS_UNIQUE_VALUE` | Actual local OneDrive workspace path and local register update | Older Projects workspace path | Medium; documentation authority only | `PORT_LOCAL_DELTA` | MEDIUM |
| `docs/COMIC_VIDEO_PRODUCTION_FEATURE_PLAN.md` | Storyboard / Image Candidate | `REMOTE_SUPERSEDES_LOCAL` | Older product-plan wording | Current workbench and next-version blueprint references | Low runtime risk; product-direction risk | `KEEP_REMOTE` | HIGH |
| `docs/PROJECT_COMPLETION_PROGRESS.md` | Repository Governance | `REMOTE_SUPERSEDES_LOCAL` | Older active 58% estimate | Later closed-phase control estimate and newer progress-model references | Medium; can mislead project decisions | `KEEP_REMOTE` | HIGH |
| `docs/QUARTERFULL_WORLD_ALIGNMENT.md` | Local Memory / Content | `REMOTE_SUPERSEDES_LOCAL` | Older local-memory alignment wording/encoding | Later memory-parser evidence and normalized content | Medium; content baseline, not runtime | `KEEP_REMOTE` | MEDIUM |
| `docs/PROJECT_PROGRESS_RUBRIC.md` | Repository Governance / Local-only | `INSUFFICIENT_EVIDENCE` | Local 58% / 79% rubric referenced by local control docs | No exact remote counterpart | Medium; progress semantics may conflict | `NEEDS_MORE_EVIDENCE` | MEDIUM |
| `memory/` | Local Memory / Content | `LOCAL_HAS_UNIQUE_VALUE` | Two local support JSON files and project/world memory context | No exact remote counterpart | Medium; not formal runtime, but affects authoring context | `NEEDS_MORE_EVIDENCE` | LOW |

## Action totals

- `KEEP_REMOTE`: 5
- `PORT_LOCAL_DELTA`: 1
- `MANUAL_MERGE_REQUIRED`: 3
- `DEFER_EXPERIMENT`: 0
- `DROP_GENERATED_COPY`: 0
- `NEEDS_MORE_EVIDENCE`: 2

No file has a safe-to-drop recommendation. No matrix row authorizes an operation in the current dirty worktree.

## Mechanical-merge assessment

- `Main.kt`, `Models.kt`, `PLANS.md`: `UNKNOWN`; semantic conflicts are too broad.
- Candidate Reference service: `YES_WITH_TESTS` only for adopting the remote version later; no current replacement.
- Handoff, comic-video plan, completion progress and QuarterFull alignment: `YES_LOW_RISK` as a later remote-first documentation action, subject to control/content review.
- Project structure: `YES_WITH_TESTS` is not applicable to Markdown; use a manual path check before porting the local delta.
- Local-only rubric and memory: `UNKNOWN`.
