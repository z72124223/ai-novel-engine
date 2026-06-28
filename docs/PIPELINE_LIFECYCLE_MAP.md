# Pipeline Lifecycle Map

Status: `CONTROL_DOC_ACTIVE`

Date: 2026-06-28

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

This file is the control map for the local novel-to-storyboard pipeline. It describes artifact lifecycle, allowed downstream steps, and boundary rules. It is documentation only: no Kotlin/JVM CLI behavior, SQLite schema, runtime state, validator behavior, or external provider behavior is changed by this file.

## Global Boundary

- Runtime state changes are still limited to the established local engine path: validator, `StateReducer`, `resolve-turn`, `contextHash`, `stateVersion`, and SQLite transaction rules.
- Storyboard, asset, image-candidate, and reference-candidate files are JSON pipeline artifacts. They are not canon, not formal registry entries, not production locks, not approved assets, and not runtime writes.
- Human decision sources are limited to `human`, `user`, or `01_control` unless a future reviewed slice explicitly changes the rule.
- The current pipeline must not use a web server, port, OpenAI API, external provider API, remote download, image generation, video generation, audio generation, Docker, React/Next, FastAPI/Express/Ktor/Spring server, Android UI, or Rust.

## Lifecycle Table

| Stage | Artifact / gate | Allowed next step | Boundary | Human source | Main source-binding risk | Forbidden wording / fields | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| 1 | Accepted draft packet | `script_packet_draft` | Reviewable draft handoff, non-runtime | None | Draft hash and source reports drifting | Claims that draft is script/storyboard/video complete | Done |
| 2 | Script packet preflight | `script_packet_draft` | Metadata preflight only | None | Packet id/hash/source report mismatch | DB, runtime, storyboard/image/video completion claims | Done |
| 3 | Script packet draft source binding | Structure bridge / review path | Candidate script JSON, non-canon | None | Protected continuity anchors, CJK anchor references, draft source fields | Downstream completion, canon, asset approval claims | Done |
| 4 | Accepted script packet handoff | `storyboard_planning_preflight` | Candidate script accepted for planning only | None | Text-surface gaps in script packet | Storyboard/image/video/runtime/canon/asset approval completion | Done |
| 5 | Storyboard planning preflight | `storyboard_planning_brief` | Preflight only | None | Accepted script packet id/hash/source report mismatch | Generated storyboard/image/video claims | Done |
| 6 | Storyboard planning brief | `storyboard_packet_draft` | Planning brief only | None | Accepted script packet id vs preflight target id | Asset approval, canon, production claims | Done |
| 7 | Storyboard packet draft | `storyboard_packet_review` | Text-only storyboard packet draft, non-production | None | Source fields, CJK anchors, output/provider fields | `imageOutputPath`, `videoOutputPath`, `generationApi`, approved/locked/final status | Done |
| 8 | Storyboard packet review | `storyboard_packet_human_review` | Review packet, not approval | None | Validation report draft file, draft id, SHA/source mismatch | Generated media, approval, lock, canon, runtime claims | Done |
| 9 | Storyboard packet human review preflight | `storyboard_packet_human_review_ready` | Ready for human decision only | None | Source validation verdict/next step provenance | Generated media, approval, lock, canon, runtime claims | Done |
| 10 | Storyboard packet human review decision | `asset_candidate_preflight` only if allowed | Human decision intake, not approval | `human` / `user` / `01_control` | Incomplete checklist coverage | Asset approval, canon, production lock, generated status | Done |
| 11 | Asset candidate preflight | `asset_candidate_brief` | Candidate asset requirements only | None | Direct review packet state and source chain | Approved, locked, final, generated, output/provider fields | Done |
| 12 | Asset candidate brief | `asset_candidate_draft` | Candidate-only brief | None | Missing source fields or requirements | Approved/locked/final/canon/generated wording | Done |
| 13 | Asset candidate draft / validation / review preflight | `asset_candidate_human_review_ready` | Candidate-only local JSON | None | Candidate item identity and status | Output path, generation API, provider job, approval/lock/final | Done |
| 14 | Asset candidate human review decision | `image_candidate_preflight` only if allowed | Human decision intake, not asset approval | `human` / `user` / `01_control` | Short checklist bypass, output/provider fields | Approved/locked/final/generated/canon/production lock | Done |
| 15 | Image candidate preflight / brief | `image_candidate_prompt_preparation` | Prompt-preparation input, no provider call | None | Requirement/source fields | Output path, generation API, provider job, approval/reference/lock | Done |
| 16 | Image candidate prompt preparation / validation / provider handoff preflight | `image_candidate_manual_provider_handoff_ready` | Prompt packet handoff preparation only | None | `assetType` status upgrade and author-only text | Approved/reference/locked/generated/author-only/provider/output | Done |
| 17 | Manual provider handoff decision / packet / validation | `image_candidate_manual_external_action_ready` | Manual external action readiness only, no provider API | `human` / `user` / `01_control` | Missing accepted script id or script packet file across chain | Provider job submitted/output/generated/approval/canon/runtime | Done |
| 18 | External result intake / result review preflight | `image_candidate_result_human_review_ready` | Local/manual result intake only | `human` / `user` / `01_control` for result source | Remote URL hidden in `metadataOnlyReference` | Remote URL/URI, provider job, API response, generated/approved/lock/canon | Done |
| 19 | Image result human review decision / candidate reference packet / validation | `candidate_reference_human_acceptance_ready` | Candidate reference packet only | `human` / `user` / `01_control` | Forged decision replacing reviewed `imageResults` | Approved reference, production lock, remote/provider/API/canon | Done |
| 20 | Candidate reference human acceptance / registry preflight / registry packet | `candidate_reference_registry_packet_validation` | Candidate registry preparation only | `human` / `user` / `01_control` | Intake truncating direct validation source fields | Approved/registered/reference lock/final/canon/generated/provider | Done |
| 21 | Candidate reference registry packet validation / acceptance decision / acceptance packet creation | `candidate_reference_acceptance_packet_validation` | Acceptance-boundary packet only | `human` / `user` / `01_control` | `assetType` or image result source mismatch | Approved/registered/production lock/canon/user approval | Done through packet creation only |
| 22 | Candidate reference acceptance packet validation | `candidate_reference_acceptance_packet_validated` | Candidate-only validation, not formal registry write | None unless a decision gate is added | Acceptance packet forged or source-truncated | Formal registry write, approved reference, registered reference, asset lock, canon | Done |
| 23 | E2E happy-path fixture | Reviewable storyboard packet skeleton | Product proof only, no runtime write | None | Gate chain discontinuity across real chapter fixture | Provider/API/output/generated/canon/approval/lock | Done |
| 24 | Story movement audit / reviewable storyboard readiness | `story_movement_audited` | Read-only story movement readiness audit, non-canon | None | Story movement missing despite valid JSON; output/provider/approval short fields | Storyboard image/video finalization, asset approval, production lock | Done |
| 25 | Reviewable storyboard packet product artifact | `reviewable_storyboard_packet_ready` | Product artifact packaging only, non-canon | Future human review rules | Audited draft/review/preflight source chain drifting before packaging | Storyboard image/video finalization, provider job, asset approval, canon, production lock | Done |
| 26 | Release hygiene and local reproducibility hardening | Clean pushed control / implementation / docs baseline | Cleanup / reproducibility proof only, non-runtime | None | Dirty worktree, untracked service/test files, stale OneDrive path references, incomplete command evidence | New runtime behavior, provider/API calls, DB writes, approval/canon/production claims | Done |
| 27 | Release grouping preview | Grouping preview report | Read-only grouping plan only, no staging | None | Grouping accepted and unreviewed files together, stale inventory counts | Delete/move/stage/commit/push/revert, DB writes, provider/media/canon claims | Done |
| 28 | Focused reproducibility bundle | Focused test evidence report | Verification evidence only, no source edits | None | Product-proof tests drifting from grouped artifacts | New code behavior, DB writes, provider/media/canon claims, skipped failing tests | Done |
| 29 | Tracked handoff manifest | Handoff manifest report | Manifest only, no staging | None | Missing shared tracked files or untracked dependencies from handoff groups | Stage/commit/push/revert, DB writes, provider/media/canon claims | Done |
| 30 | Adjacent storyboard suite reproducibility | Storyboard suite evidence report | Verification evidence only, no source edits | None | Broader storyboard tests drifting from product-proof path | New code behavior, DB writes, provider/media/canon claims, skipped failing tests | Done |
| 31 | First staging candidate decision brief | Staging candidate decision brief | Read-only staging decision prep, no staging | `01_control` | Shared tracked files spanning multiple groups without ownership decision | Stage/commit/push/revert, DB writes, provider/media/canon claims, release approval wording | Done |
| 32 | Control-doc staging candidate dry run | Dry-run staging candidate report | Read-only exact-file staging simulation, no `git add` | `01_control` | Accidentally treating dry-run as actual staged state | Actual staging, commit, push, cleanup, DB writes, provider/media/canon claims | Done |
| 33 | Control-doc staging apply | Git index staging of exact control-doc candidate files | Exact-file staging only, no commit | `01_control` | Staging files outside Option A or missing updated control docs | Commit, push, cleanup, broad source/test staging, DB writes, provider/media/canon claims | Done |
| 34 | Control-doc commit / push apply | Pushed control-doc baseline | Exact-file commit/push only, no cleanup or runtime behavior | `01_control` | Treating staged state as release approval | Cleanup, broad source/test staging, DB writes, provider/media/canon claims | Done |
| 35 | Broad implementation bundle commit / push apply | Pushed local JSON pipeline gate baseline | Implementation bundle only, no docs/memory cleanup | `01_control` | Shared `Main.kt` / `Models.kt` scope hiding extra files | DB writes, provider/media/canon claims, docs/memory staging | Done |
| 36 | Remaining planning docs split groups | Pushed D1/D2/D3 docs baselines | Docs/control only | `01_control` | Mixing governance, planning, and QuarterFull support docs without review | Source/runtime edits, memory staging, canon/approval claims | Done |
| 37 | Memory owner / parser / exact ignore closure | Clean local memory support boundary | Local-only memory support, exact ignore rules | `01_control` | Treating local memory as runtime/canon state or broadly ignoring future memory work | Memory JSON staging, repair/delete/cleanup, SQLite/runtime/provider claims | Done |
| 38 | Image-facing delivery proof after reviewable storyboard packet | `image_facing_delivery_proof_ready` | Product proof only, no provider/media execution | None | Mistaking proof readiness for generated image/provider readiness | Provider API job, remote download, generated image storage, approval/canon/production lock | Done |
| 39 | E2E image-facing delivery proof fixture | Accepted draft to `ImageFacingDeliveryProof` product proof fixture | Test/product proof only | None | Proving the gate in isolation but not the product path | Provider API job, remote download, generated image storage, approval/canon/production lock | Done |
| 40 | Final local product-delivery verification bundle | Local reproducibility and product-path verification evidence | Verification evidence only, no release tag | `01_control` | Treating clean pushed state as release approval without final evidence | Provider/media generation, formal registry write, approval/canon/production lock | Done |
| 41 | Project completion closure decision brief | Owner decision on remaining formal registry / release boundary | Docs/control decision only, not release approval | `01_control` | Treating 84% as 100% without explicit target decision | Release tag, formal registry write, approval/canon/production lock | Next |

## Current Mainline

The safe mainline is:

```text
accepted draft packet
-> script packet gates
-> accepted script packet handoff
-> storyboard planning gates
-> storyboard packet draft/review/human decision
-> asset candidate gates
-> image candidate prompt/provider handoff preparation
-> external result intake/review
-> candidate reference packet/validation
-> candidate reference registry preparation
-> candidate reference acceptance-boundary packet
-> candidate_reference_acceptance_packet_validation
-> E2E happy-path fixture
-> story movement audit
-> reviewable storyboard packet product artifact
-> release hygiene and local reproducibility hardening
-> release grouping preview
-> focused reproducibility bundle
-> tracked handoff manifest
-> adjacent storyboard suite reproducibility
-> first staging candidate decision brief
-> control-doc staging candidate dry run
-> control-doc staging apply
-> control-doc commit / push apply
-> broad implementation bundle commit / push apply
-> remaining planning docs split groups
-> memory owner / parser / exact ignore closure
-> image-facing delivery proof after reviewable storyboard packet
-> E2E image-facing delivery proof fixture
-> final local product-delivery verification bundle
-> project completion closure decision brief
```

## Next Slice Order

1. Complete this docs/control repair and obtain expert-model review. Done.
2. Build an E2E happy-path fixture from accepted draft to reviewable storyboard packet. Done.
3. Add a read-only story movement audit for storyboard readiness. Done.
4. Add a named reviewable storyboard packet product artifact. Done.
5. Perform release hygiene and local reproducibility hardening. Done.
6. Produce a read-only release grouping preview. Done.
7. Produce a focused reproducibility bundle. Done.
8. Produce a tracked handoff manifest. Done.
9. Produce adjacent storyboard suite reproducibility evidence. Done.
10. Produce the first staging candidate decision brief. Done.
11. Produce a control-doc staging candidate dry run. Done.
12. Apply exact control-doc staging candidate to the git index. Done.
13. Apply and push the control-doc-only commit. Done.
14. Apply and push the broad implementation bundle. Done.
15. Split, commit, and push remaining planning/support docs. Done.
16. Record memory owner/parser evidence and apply exact local-memory ignore rules. Done.
17. Define and implement the next image-facing delivery proof after `ReviewableStoryboardPacket`. Done.
18. Build an E2E fixture from accepted draft to `ImageFacingDeliveryProof`. Done.
19. Produce final local product-delivery verification evidence. Done.
20. Produce a project completion closure decision brief for the remaining formal registry / release boundary. Next.

## Not Approved

- Formal registry write.
- Asset approval.
- Approved reference.
- Registered reference.
- Production lock.
- Canon application.
- Runtime scene creation from storyboard artifacts.
- Provider API job, remote download, generated image storage, video output, audio generation.

Any future slice that wants one of these must first create a separate decision brief and pass `03` review.
