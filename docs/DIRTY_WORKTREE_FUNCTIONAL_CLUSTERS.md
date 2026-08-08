# WP-001E Dirty Worktree Functional Clusters

## WP-001F-1 Exceptional Paths By Cluster

The nine divergent paths and two local-only status entries are the only paths in scope for WP-001F-1. The 104 `REMOTE_IDENTICAL` paths are intentionally excluded from this work package.

| Exceptional path | Cluster used for WP-001F-1 |
|---|---|
| `PLANS.md` | `REPOSITORY_GOVERNANCE` |
| `app/src/main/kotlin/com/localnovel/engine/Main.kt` | `CORE_INTEGRATION` |
| `app/src/main/kotlin/com/localnovel/engine/model/Models.kt` | `CORE_INTEGRATION` |
| `app/src/main/kotlin/com/localnovel/engine/service/CandidateReferenceAcceptancePacketService.kt` | `CANDIDATE_REFERENCE` |
| `docs/HANDOFF.md` | `REPOSITORY_GOVERNANCE` |
| `docs/PROJECT_STRUCTURE_REPORT.md` | `REPOSITORY_GOVERNANCE` |
| `docs/COMIC_VIDEO_PRODUCTION_FEATURE_PLAN.md` | `STORYBOARD / IMAGE_CANDIDATE` |
| `docs/PROJECT_COMPLETION_PROGRESS.md` | `REPOSITORY_GOVERNANCE` |
| `docs/QUARTERFULL_WORLD_ALIGNMENT.md` | `LOCAL_MEMORY / CONTENT` |
| `docs/PROJECT_PROGRESS_RUBRIC.md` | `REPOSITORY_GOVERNANCE / LOCAL_ONLY` |
| `memory/` | `LOCAL_MEMORY / CONTENT` |

## Audit boundary

This document clusters the original dirty worktree by product/runtime responsibility. It is a dependency and source map, not an approval list. No cluster is being integrated, compiled, repaired, committed or pushed. The original 115 entries remain in `DIRTY_WORKTREE_INDEX.md`; the five audit documents are counted separately.

## Cluster summary

| ID | Purpose | Production | Tests | Entry point / call relationship | Migration / repository | Runtime change | Maturity |
|---|---|---:|---:|---|---|---|---|
| `A CORE_INTEGRATION` | Shared model surface and CLI wiring | 2 | 0 | `Main.kt` imports dirty services and owns CLI dispatch; `Models.kt` supplies shared types | Existing repository and migrations 001–006 are referenced through existing CLI paths; no new dirty migration | Yes, high-risk integration surface | `CONFLICTED_WITH_REMOTE` |
| `B ASSET_CANDIDATE` | Candidate brief/draft/preflight/review workflow | 6 | 6 | Services expose JSON-boundary operations; paired tests call the services; `Main.kt` is the apparent CLI boundary | No direct migration/table or repository method found | Potentially through CLI wiring; not proven | `SUPERSEDED_BY_REMOTE` |
| `C CANDIDATE_REFERENCE` | Reference registry and acceptance workflow | 6 | 6 | Registry/acceptance services form a packet chain; one service differs from remote | No direct migration/table or repository method found | Potentially through CLI wiring; not proven | `CONFLICTED_WITH_REMOTE` |
| `D CONTINUITY_CHARACTER_GATE` | Chapter continuity and character gate checks | 2 | 2 | Audit/gate services are called by matching tests; no independent CLI registration proven | No direct migration/table or repository method found | Read-mostly validation boundary; integration unknown | `SUPERSEDED_BY_REMOTE` |
| `E IMAGE_CANDIDATE` | Prompt, reference, provider handoff and external result workflow | 13 | 13 | Preparation → reference → provider handoff → result review chain; matching tests exist | No direct migration/table or repository method found | External-file boundary; formal runtime registration unknown | `SUPERSEDED_BY_REMOTE` |
| `F NOVELIZATION` | Draft intake, revision and prose quality workflow | 8 | 8 | Writer brief → draft/revision → quality audit → script preflight; matching tests exist | No direct migration/table or repository method found | External-file boundary; formal runtime registration unknown | `SUPERSEDED_BY_REMOTE` |
| `G SCRIPT_PACKET_HANDOFF` | Script packet validation and accepted handoff | 4 | 4 | Source binding → structure bridge → validation → accepted handoff; matching tests exist | No direct migration/table or repository method found | External-file boundary; formal runtime registration unknown | `SUPERSEDED_BY_REMOTE` |
| `H STORYBOARD` | Storyboard planning, review and preflight | 6 | 6 | Planning brief → draft/review → human review/preflight; matching tests exist | No direct migration/table or repository method found | External-file boundary; formal runtime registration unknown | `SUPERSEDED_BY_REMOTE` |

The table totals **47 production Kotlin files, 45 test files, and 92 dirty Kotlin entries**. The two core files are counted only in A.

## File membership

The exact service and test paths are listed in the baseline index. Membership is deterministic by class/service responsibility:

- A: `Main.kt`, `Models.kt`.
- B: `AssetCandidate*Service.kt` and matching `AssetCandidate*Test.kt`.
- C: `CandidateReference*Service.kt` and matching `CandidateReference*Test.kt`.
- D: `ChapterContinuityAuditService.kt`, `CharacterNuwaGateValidationService.kt` and their matching tests.
- E: `ImageCandidate*Service.kt` and matching `ImageCandidate*Test.kt`.
- F: `Novelization*Service.kt` and matching `Novelization*Test.kt`.
- G: `AcceptedScriptPacketHandoffPacketService.kt`, `ScriptPacketDraft*Service.kt` and matching tests.
- H: `Storyboard*Service.kt` and matching tests.

No Kotlin path is assigned to two groups. Documentation, samples and `memory/` are supporting evidence only and are not counted as production/test members.

## Supporting evidence by cluster

| Cluster | Related docs / samples | Independent verification state | Main risks |
|---|---|---|---|
| A | `PLANS.md`, modified control docs | Cannot be isolated safely until model/API divergence is reconciled | Shared model drift, CLI dispatch drift, remote provisional-layer APIs |
| B/C/E | Candidate, image and production planning docs; no approval inferred from samples | Paired tests exist but were not run; compile status unknown | Duplicate remote workflow, packet contract drift, external-result trust boundary |
| D | `samples/chapter_continuity_anchor_sample.json`, `samples/character_nuwa_gate_sample.json` | No orphan tests; no runtime verification | Validation may be disconnected from formal canon/runtime |
| F/G | Novelization and script-packet planning docs and samples | Paired tests exist; no test execution | Draft/sample data may be mistaken for canon or accepted output |
| H | Storyboard and comic/video planning docs | Paired tests exist; no test execution | Remote storyboard pipeline may have a different model/CLI contract |

## Maturity evidence

`COMPLETE_CANDIDATE` was not assigned. Six groups have exact remote-identical production/test files, one group contains a divergent service, and the core group has two divergent files. `SUPERSEDED_BY_REMOTE` means the local content is already present at the fetched remote path; it does not authorize deletion or cleanup of the local worktree. No Gradle test or isolated compile was run in WP-001E.

## Suggested order, not executed

1. `WP-001F-1 Reconcile Core Integration`: compare `Main.kt`, `Models.kt` and the remote provisional/narrative-delivery model and CLI surfaces in a new clean worktree.
2. `WP-001F-2 Reconcile Candidate and Image Contracts`: select the smallest packet boundary after A is reconciled.
3. `WP-001F-3 Reconcile Novelization and Script Handoff`.
4. `WP-001F-4 Reconcile Storyboard`.
5. `WP-001F-5 Review continuity/character gates and supporting samples`.

No WP-001F package was started by this audit.
