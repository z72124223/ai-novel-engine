# Codex Blueprint Future Route Integration Map V0

Top-level blueprint note: use `docs/CODEX_WHOLE_PROJECT_BLUEPRINT_V0.md` as
the current whole-project route entry. This file remains supporting evidence for
future route families, but it is no longer the highest-level project blueprint.

Execution control note: for current task selection, start with
`docs/CODEX_BLUEPRINT_EXECUTION_CONTROL_INDEX_V0.md` and
`docs/CODEX_CURRENT_ROUTE_CARD_V0.md`. This map is supporting route evidence,
not the active task selector.

Status: `BLUEPRINT_FUTURE_ROUTE_INTEGRATION_MAP_V0`

Date: 2026-07-02

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

Branch: `codex/mvp6-checkpoint-branching-spec`

## Purpose

This docs-only control map records the ordered future product routes that must remain visible after the user redirected the project away from the old renderer-manifest micro-phase chain. It prevents new windows from mistaking the Phase 1 through Phase 19 local JSON renderer-manifest path for the whole final product route.

This map does not start Phase 20, Phase 21, implement queryers, create 09 cards, resume 04/05/06/07/08, generate storyboard images, create an asset registry, build voice timeline tooling, run the local renderer, generate media, call provider/API, write SQLite, approve canon, approve a registry, approve release, approve final media, or create production lock.

## Coordination And Quota

- Control thread 01: `019f0dd1-1543-7f22-b646-5c12d07d168d`.
- Worker thread 02: `019f1882-d5c6-72a3-893c-6d43cd097835`.
- Reviewer thread 03: `019f1ad1-2926-70c3-b85c-267df6fe331e`.
- Current 01 quota note: local time checked by watchdog was 2026-07-02 00:42 +08:00, and the last quota observation was inside the three-hour freshness window.
- Quota rule: preserve enough quota for at least one final handoff/status message before any high-cost 01/02/03 transition.

## Ordered Route Map

| Order | Route family | Evidence docs | Current status | Next smallest safe task candidate | Stop condition / boundary |
| --- | --- | --- | --- | --- | --- |
| 1 | User / 00 input and new-world foundation | `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md`, `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md`, `docs/HANDOFF.md` | Docs-only future input surface. The questionnaire is not canon, not a setting pack, not runtime data, and not permission to resume downstream windows. | Ask user / 00 for enough foundation answers, then draft a docs-only `NEW_WORLD_FOUNDATION_BRIEF` if 01 explicitly assigns it. | Stop if user direction is missing, if the route would convert candidates to canon, or if it implies setting-pack/runtime approval. |
| 2 | 09 foundation: world / character / location / premise cards | `docs/DEMAND_DRIVEN_QUERY_LAYER_BLUEPRINT.md`, `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md`, `docs/CODEX_BLUEPRINT_FUTURE_ROUTE_GAP_AUDIT.md` | Docs-only future. 09 foundation cards are not implemented and not approved as canon or setting-pack content. | Draft a docs-only 09 card organization brief after a foundation brief exists. | Stop if 09 card work would become schema, runtime enforcement, setting pack, approved asset data, or canon. |
| 3 | Demand-driven query layer: World Card Queryer, Asset Queryer, Story / Situation / Continuity / Pressure Queryer | `docs/DEMAND_DRIVEN_QUERY_LAYER_BLUEPRINT.md`, `docs/CODEX_DEMAND_DRIVEN_QUERY_CONTRACT_SPEC_V0.md`, `samples/local_story_input/rain_gate_demand_driven_query_contract_spec_sample.json`, `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md` | Docs/spec/data-contract result exists for `demand_driven_query_contract_spec_v0` and is review-pending / revision-fixed evidence. It is no queryer implementation, no SQLite schema, no runtime enforcement, no provider/API, no GPT automation, no Seedance, and no Oiioii. | After review PASS, 01 may consider only a docs-only `script_ready_scene_packet_alignment_v0` decision. This is not approval and not auto-start. | Stop if any task would implement queryers, write SQLite, connect provider/API, restart 04/05/06/07/08, claim production lock, or allow 04 to read a full world bible. |
| 4 | Script-ready scene packet | `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`, `docs/HANDOFF.md` | Docs-only contract. Script-ready scene packet is the planned bridge from scoped foundation/query data into 04/05/06/07/08, but no active packet generation is authorized. | Draft a packet template decision brief for the new-world path, docs-only, after 09/query scope is clearer. | Stop if the task would generate story content, create runtime data, restart 04/05/06/07/08, or expose author-only data. |
| 5 | 04 generation, 05 review, 06 display / user approval, 07 asset candidates, 08 GPT storyboard pickup / verification | `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`, `docs/HANDOFF.md`, `docs/STORYBOARD_AND_CAMERA_PLANNING.md` | Paused. Manual handoff only: 01 -> 04 -> 05 -> 06 -> explicit user approval -> 01 -> 07 -> 08. | Draft a 04/05/06/07/08 resumption decision brief only if 01/user asks whether to resume. | Stop if silence is treated as approval, if automation advances the flow, or if any window generates/reviews/displays without explicit 01 handoff. |
| 6 | Storyboard/camera packet route | `docs/STORYBOARD_AND_CAMERA_PLANNING.md`, `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`, `docs/PRODUCTION_ASSET_REGISTRY_PLANNING.md`, `docs/CODEX_STORYBOARD_CAMERA_PACKET_TEMPLATE_REVIEW_V0.md` | Docs/control template-review packet exists for `StoryboardCameraPacket`; storyboard generation tooling is still not implemented here. | After `REVIEW_PASS_NOT_APPROVAL`, B組01/A組01/00 may consider only a future bounded docs/control decision such as `asset_lifecycle_registry_decision_packet` or another route-selected next task. | Stop if the task would generate images, call GPT/browser/provider tools, launch Seedance/Oiioii, leak prompt surface, auto-start downstream work, or claim final visual approval. |
| 7 | Asset registry lifecycle and open storage decision | `docs/PRODUCTION_ASSET_REGISTRY_PLANNING.md`, `docs/ASSET_REGISTRY_IMPLEMENTATION_BLUEPRINT.md`, `docs/DEMAND_DRIVEN_QUERY_LAYER_BLUEPRINT.md`, `docs/CODEX_ASSET_LIFECYCLE_REGISTRY_DECISION_PACKET_V0.md` | Docs/control decision packet exists for `AssetLifecycleRegistryDecisionPacket`; formal SQLite registry, sidecar DB, draft-file policy, and import CLI are still not approved. | After `REVIEW_PASS_NOT_APPROVAL`, B組01/A組01/00 may ask for the required storage decision, defer it, or choose another route-selected docs/control task. | Stop if the task would choose storage architecture without approval, create schema/migrations, write SQLite, store images, approve assets, approve registry data, leak prompt surface, or treat prompt cards as approved registry entries. |
| 8 | Voice timeline / subtitle / SFX route | `docs/CODEX_NO_EXTERNAL_TOOLS_LOCAL_VIDEO_AUTOMATION_ROADMAP.md`, `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`, current local story-to-render samples, `docs/CODEX_VOICE_TIMELINE_SUBTITLE_SFX_CONTRACT_V0.md` | Docs/control contract exists for `VoiceTimelineSubtitleSfxContract`; voice timeline, subtitle, SFX, ambient, silence, and timing intent remain planning-only and are not TTS, renderer, media, or final-readiness implementation. | After `REVIEW_PASS_NOT_APPROVAL`, B組01/A組01/00 may consider another route-selected docs/control task or a future explicit decision about local renderer readiness; PASS must not auto-start execution. | Stop if it would run TTS, generate audio, call provider/API, run renderer/ffmpeg, generate media, leak prompt surface, or claim final media readiness. |
| 9 | Current local story-to-render / renderer-manifest chain through Phase 19 | `docs/CODEX_LOCAL_STORY_INPUT_PACK_V0.md`, `docs/CODEX_LOCAL_ASSET_PROMPT_CARDS_V0.md`, `docs/CODEX_LOCAL_RENDER_PACKAGE_V0.md`, `docs/CODEX_LOCAL_STORY_VIDEO_CHAIN_V0.md`, `docs/CODEX_LOCAL_STORY_VIDEO_PREVIEW_HANDOFF_V0.md`, `docs/CODEX_LOCAL_RENDERER_MANIFEST_ADAPTER_CONTRACT_DRAFT_V0.md`, `docs/HANDOFF.md` | Implemented local JSON draft/review/dry-run/contract chain through Phase 19. Phase 20 was stopped before edits. It is not final video readiness, renderer readiness, production manifest approval, release, or production lock. | Do not continue this chain until the whole-project blueprint and work package are reviewed and 01/user explicitly selects this route. | Stop if the task would treat this chain as the whole product route, run renderer/ffmpeg/TTS, generate media, or approve a production `ComicVideoRenderHandoffManifest`. |
| 10 | Future local renderer execution path after explicit assignment | `docs/CODEX_COMIC_VIDEO_LOCAL_PREVIEW_ARTIFACT_VERIFICATION.md`, `docs/CODEX_COMIC_VIDEO_WORKBENCH_LOCAL_DELIVERY_BRIEF.md`, Phase 7 through Phase 19 docs, `docs/CODEX_LOCAL_RENDERER_EXECUTION_READINESS_CHECKLIST_V0.md` | Docs/control readiness checklist exists for `LocalRendererExecutionReadinessChecklist`; the old Rain Gate local preview remains local artifact evidence only, not whole-product final media readiness. | After `REVIEW_PASS_NOT_APPROVAL`, B組01/A組01/00 may consider another route-selected docs/control task such as local package / QA / delivery definition, or ask for a future explicit renderer execution readiness decision. | Stop if it would execute renderer/ffmpeg/TTS, generate MP4 or other media, create media handoff, call provider/API, bypass human approval, or claim final video readiness without explicit 01/user task. |
| 11 | Human approval gates | `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`, `docs/HANDOFF.md`, `docs/CODEX_BLUEPRINT_FUTURE_ROUTE_GAP_AUDIT.md` | Required but not yet consolidated into a positive approval checklist. Existing docs repeatedly state what is not approval. | Draft a positive human approval checklist separating 05 PASS, 06 display, user explicit approval, 03 PASS, and production approval. | Stop if silence, vague assent, 03 technical PASS, or local JSON PASS is treated as user approval, final media approval, canon, registry, release, or production lock. |
| 12 | Optional external provider / paid-tool / funding gate | `docs/CODEX_NO_EXTERNAL_TOOLS_LOCAL_VIDEO_AUTOMATION_ROADMAP.md`, `docs/STORYBOARD_AND_CAMERA_PLANNING.md`, `docs/PRODUCTION_ASSET_REGISTRY_PLANNING.md`, `docs/HANDOFF.md` | Paused / requires user decision. Current path avoids external paid/provider tools until local demo/product evidence supports funding or monetization. | Draft an external provider decision packet only after user/01 asks and after storyboard/assets/local renderer boundaries are clear. | Stop if any task would call provider/API, spend paid-tool credits, start provider/media handoff, or bypass funding/budget approval. |

## Cross-Route Control Rules

- The current local renderer path is one route family, not the whole product route.
- 04/05/06/07/08 remain paused until explicit 01/user handoff.
- The demand-driven query layer is future product-layer retrieval, not runtime ContextPacket enforcement.
- Blueprint planning entries are non-canon candidates only.
- Current local asset/prompt cards are not approved assets and not asset registry entries.
- No route may claim final media approval, provider/media handoff, canon, registry, release, approval, or production lock by implication.
- No route may run renderer/ffmpeg/TTS, call provider/API, write SQLite directly, start web/server, generate media, or produce a real production manifest unless a future explicit task changes that boundary.

## Recommended Next Safe Tasks

These are candidates only. 01 must assign exactly one bounded task before any work starts.

1. `whole-project work package review`: accept or revise the work package before splitting tasks.
2. `true final product progress model`: use the new progress model instead of treating local baseline 100% as final completion.
3. `positive approval matrix`: keep user approval, 03 PASS, asset approval, canon, registry, final media, and release separate.
4. `script_ready_scene_packet_alignment_v0`: possible docs-only 01 decision after the demand-driven query contract/spec receives review PASS; not approval, not auto-start, no queryer implementation, no provider/API, no SQLite, and no production lock.
5. `09 foundation card organization`: draft card families and promotion rules before scene/storyboard generation.

## Stop Conditions

Stop and return to 01/user if the next step requires:

- user worldbuilding decisions;
- user review of story, storyboard, image, audio, or video output;
- 04/05/06/07/08 restart;
- asset approval or asset registry storage decision;
- renderer/ffmpeg/TTS/media generation;
- provider/API or external paid/provider tool;
- direct SQLite writes, schema, or migration;
- final media approval, provider/media handoff, canon, registry, release, approval, or production lock;
- stale quota, insufficient weekly quota, or not enough quota for a final handoff/status message.
