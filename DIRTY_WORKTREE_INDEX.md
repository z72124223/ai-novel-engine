# Dirty Worktree Index

## WP-001E Assignment Record

The original 115-entry inventory below remains unchanged. WP-001E assigns the 92 dirty Kotlin entries exactly once: `CORE_INTEGRATION` (2), `ASSET_CANDIDATE` (12), `CANDIDATE_REFERENCE` (12), `CONTINUITY_CHARACTER_GATE` (4), `IMAGE_CANDIDATE` (26), `NOVELIZATION` (16), `SCRIPT_PACKET_HANDOFF` (8), and `STORYBOARD` (12). These counts include production and test files; the split is 47 production and 45 tests. Audit-generated files are excluded. See `docs/DIRTY_WORKTREE_FUNCTIONAL_CLUSTERS.md` and `docs/DIRTY_WORKTREE_DEPENDENCY_MAP.md` for the evidence and pairing rules.

## Baseline

- Audit baseline: 2026-07-10
- Baseline source: `git status --porcelain=v1`
- Baseline status entries: `115`
- Tracked modifications: `7`
- Untracked status entries: `108`
- Deleted entries: `0`
- Local worktree HEAD: `89e522f18a946426232f5d56009fea0262329b47`
- Remote branch currently points to: `74a939ae87f6536a95a5497b053b3367c14368f1`
- The two audit documents created by WP-001D are excluded from the 115-entry baseline.

## Category Summary

| Category | Status entries | Tracked modified | Untracked | Evidence-based disposition |
|---|---:|---:|---:|---|
| Engine / CLI | 47 | 2 | 45 | `UNKNOWN`; retain for separate review |
| Tests | 45 | 0 | 45 | `UNKNOWN`; retain with matching engine candidates |
| Documentation | 14 | 4 | 10 | `UNKNOWN`; preserve, do not declare canonical |
| Local memory | 1 | 0 | 1 | `UNKNOWN`; local support data, not formal canon |
| Samples / fixtures | 7 | 0 | 7 | `UNKNOWN`; preserve as evidence candidates |
| Planning control | 1 | 1 | 0 | `UNKNOWN`; `PLANS.md` mixes this plan with prior work |
| **Total** | **115** | **7** | **108** | No discard decision made |

## Engine / CLI Inventory

### Tracked modified core files

- `app/src/main/kotlin/com/localnovel/engine/Main.kt` — ` M`; core CLI wiring; completion and ownership `UNKNOWN`.
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt` — ` M`; shared model surface; completion and compatibility `UNKNOWN`.

### Untracked service files

All entries below are `??` and are not committed in the local worktree:

- `app/src/main/kotlin/com/localnovel/engine/service/AcceptedScriptPacketHandoffPacketService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/AssetCandidateBriefService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/AssetCandidateDraftService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/AssetCandidateDraftValidationService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/AssetCandidateHumanReviewDecisionIntakeService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/AssetCandidatePreflightService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/AssetCandidateReviewPreflightService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/CandidateReferenceAcceptancePacketService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/CandidateReferenceHumanAcceptanceDecisionIntakeService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/CandidateReferenceRegistryHumanAcceptanceDecisionIntakeService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/CandidateReferenceRegistryPacketService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/CandidateReferenceRegistryPacketValidationService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/CandidateReferenceRegistryPreflightService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ChapterContinuityAuditService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/CharacterNuwaGateValidationService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidateBriefService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidateExternalResultIntakeService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidatePreflightService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidatePromptPreparationService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidatePromptValidationService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidateProviderHandoffDecisionIntakeService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidateProviderHandoffPacketService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidateProviderHandoffPacketValidationService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidateProviderHandoffPreflightService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidateReferencePacketService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidateReferencePacketValidationService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidateResultHumanReviewDecisionIntakeService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidateResultReviewPreflightService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/NovelizationAcceptedDraftPacketService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/NovelizationDraftIntakeValidationService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/NovelizationPreflightValidationService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/NovelizationProseQualityAuditService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/NovelizationRevisedDraftIntakeValidationService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/NovelizationRevisionBriefService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/NovelizationScriptPacketPreflightService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/NovelizationWriterBriefValidationService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ScriptPacketDraftSourceBindingValidationService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ScriptPacketDraftStructureBridgeService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ScriptPacketDraftValidationService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/StoryboardPacketDraftValidationService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/StoryboardPacketHumanReviewDecisionIntakeService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/StoryboardPacketHumanReviewPreflightService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/StoryboardPacketReviewService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/StoryboardPlanningBriefService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/StoryboardPlanningPreflightService.kt`

Evidence from names and package declarations indicates candidate/reference, image, novelization, script-packet and storyboard boundary services. That is a scope signal only; it is not evidence of completion, integration, approval or product direction.

## Tests Inventory

All 45 entries below are `??` and are not committed:

- `app/src/test/kotlin/com/localnovel/engine/AcceptedScriptPacketHandoffPacketTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/AssetCandidateBriefTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/AssetCandidateDraftTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/AssetCandidateDraftValidationTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/AssetCandidateHumanReviewDecisionIntakeTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/AssetCandidatePreflightTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/AssetCandidateReviewPreflightTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/CandidateReferenceAcceptancePacketTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/CandidateReferenceHumanAcceptanceDecisionIntakeTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/CandidateReferenceRegistryHumanAcceptanceDecisionIntakeTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/CandidateReferenceRegistryPacketTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/CandidateReferenceRegistryPacketValidationTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/CandidateReferenceRegistryPreflightTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ChapterContinuityAuditTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/CharacterNuwaGateValidationTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidateBriefTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidateExternalResultIntakeTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidatePreflightTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidatePromptPreparationTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidatePromptValidationTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidateProviderHandoffDecisionIntakeTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidateProviderHandoffPacketTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidateProviderHandoffPacketValidationTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidateProviderHandoffPreflightTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidateReferencePacketTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidateReferencePacketValidationTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidateResultHumanReviewDecisionIntakeTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidateResultReviewPreflightTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/NovelizationAcceptedDraftPacketTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/NovelizationDraftIntakeValidationTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/NovelizationPreflightValidationTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/NovelizationProseQualityAuditTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/NovelizationRevisedDraftIntakeValidationTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/NovelizationRevisionBriefTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/NovelizationScriptPacketPreflightTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/NovelizationWriterBriefValidationTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ScriptPacketDraftSourceBindingValidationTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ScriptPacketDraftStructureBridgeTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ScriptPacketDraftValidationTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/StoryboardPacketDraftValidationTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/StoryboardPacketHumanReviewDecisionIntakeTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/StoryboardPacketHumanReviewPreflightTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/StoryboardPacketReviewTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/StoryboardPlanningBriefTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/StoryboardPlanningPreflightTest.kt`

The filename pairing suggests many service/test pairs, but no test execution is part of this audit; implementation status remains `UNKNOWN`.

## Documentation, Memory, Samples And Planning Inventory

### Tracked modified documentation

- `docs/AUTHORING_QUALITY_GUIDELINES.md` — ` M`
- `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md` — ` M`
- `docs/HANDOFF.md` — ` M`
- `docs/PROJECT_STRUCTURE_REPORT.md` — ` M`

### Untracked documentation

- `docs/BLUEPRINT_REDESIGN_DECISION_BRIEF.md`
- `docs/CLEANUP_INVENTORY_REPORT.md`
- `docs/COMIC_VIDEO_PRODUCTION_FEATURE_PLAN.md`
- `docs/HUMANLIKE_PROSE_AND_DIALOGUE_DISTILLATION_PLAN.md`
- `docs/NOVELIZATION_SUPERIOR_TO_QUARTERFULL_PLAN.md`
- `docs/NOVEL_TO_SCRIPT_PACKET_BLUEPRINT.md`
- `docs/PROJECT_COMPLETION_PROGRESS.md`
- `docs/PROJECT_DIRECTORY_AND_SUBPROJECT_REGISTER.md`
- `docs/PROJECT_PROGRESS_RUBRIC.md`
- `docs/QUARTERFULL_WORLD_ALIGNMENT.md`

The inspected headers explicitly label these as docs-only plans, blueprints, registers or control estimates. They are therefore planning evidence, not proof of implemented runtime capability or current canon.

### Local memory

- `memory/` — `??` status entry; directory inspection found `project_directory_memory.json` and `quarterfull_world_memory_capsule.json`.
- These are local support data and are not treated as formal SQLite state or canon by this audit.

### Samples / fixtures

- `samples/chapter_continuity_anchor_sample.json`
- `samples/character_nuwa_gate_sample.json`
- `samples/novelization_draft_intake_sample.md`
- `samples/novelization_revision_continuity_anchors_sample.json`
- `samples/novelization_revision_handling_declaration_sample.json`
- `samples/novelization_writer_brief_sample.json`
- `samples/script_packet_draft_sample.json`

These filenames and headers indicate sample, draft, anchor or fixture material. They are not automatically accepted data, canon, or production assets.

### Planning control

- `PLANS.md` — ` M`; the file contains prior planning history and the current WP-001D entry. Its 3,605-line diff is mixed history and cannot be safely attributed to one Work Package during this audit.
