# Plan

## Active Owner Closure Option Selection Pending Plan

### Goal

Keep Stage 42 honest after the pushed owner decision intake record: the current project phase still requires an explicit owner/user selection of Option A, B, or C before completion wording or progress alignment.

### Global Strategy

- Update docs/control status only:
  - `PLANS.md`
- Record that `docs/PROJECT_COMPLETION_OWNER_DECISION_INTAKE.md` has been committed and pushed.
- Preserve Option A as the current recommendation only, not as selected.
- Keep official progress at `Project completion progress: 84%`.
- Ask for explicit owner/user selection before creating any selection record or completion alignment.

### Non-Goals

- No Kotlin/source/test/schema/runtime/CLI behavior edits.
- No Gradle rerun.
- No release tag, release branch, cleanup, delete, move, revert, DB, server, provider, media generation, approval, canon, formal registry write, or production-lock action.
- No implicit owner selection, project-complete wording, or progress increase.

### Confirmed Facts

- Current pushed HEAD/upstream are `72db12281c91e8bc18305081c2aadde8aec43966` / `72db122 docs: add owner decision intake`.
- `03` passed the owner decision intake repair, staging apply, commit apply, push readiness, and push apply.
- `docs/PROJECT_COMPLETION_OWNER_DECISION_INTAKE.md` records `Decision status: PENDING_EXPLICIT_OWNER_SELECTION`.
- `docs/PROJECT_COMPLETION_OWNER_DECISION_INTAKE.md` records Option A as current recommendation only and `Selection source: none yet.`
- `docs/PIPELINE_LIFECYCLE_MAP.md` still marks Stage 42 `Owner closure option selection` as Next.
- `docs/PROJECT_COMPLETION_PROGRESS.md` still displays `Project completion progress: 84%`.

### Assumptions

- The owner/user remains the authority for choosing Option A, B, or C.

### Open Questions

- Whether the owner selects Option A, Option B, or Option C for project closure.

### Implementation Steps

- [x] Step 1: Align `PLANS.md` after pushed owner decision intake record.
- [x] Step 2: Run docs-only verification and send to `03`.
- [ ] Step 3: Stage, commit, and push only after review.
- [ ] Step 4: Ask the owner/user for explicit A/B/C selection.

### Verification

- `git status --short --untracked-files=all`.
- `git diff --check -- PLANS.md`.
- Trailing whitespace scan on `PLANS.md`.
- Confirm `Project completion progress: 84%` remains unchanged in `docs/PROJECT_COMPLETION_PROGRESS.md`.
- Confirm HEAD/upstream remain `72db12281c91e8bc18305081c2aadde8aec43966` before this alignment commit.

### Risks

- Accidentally treating this status alignment as the missing owner selection.
- Accidentally changing progress/completion wording before explicit owner selection.
- Accidentally implying formal registry write, provider/media readiness, generated media, approval, canon, runtime write, or production lock.

### Drift Log

- 2026-06-28: Started post-owner-decision-intake `PLANS.md` alignment after `03` passed pushed intake record. Scope is one docs/control file only; official progress remains 84%, and A/B/C remains pending explicit owner/user selection.
- 2026-06-28: Docs-only verification passed: dirty set is only `PLANS.md`; diff check returned only LF/CRLF warning; trailing whitespace scan returned no matches; `docs/PROJECT_COMPLETION_PROGRESS.md` remains 84%; HEAD/upstream remain `72db12281c91e8bc18305081c2aadde8aec43966`.
- 2026-06-28: `03` passed `minimal post-owner-decision-intake PLANS alignment`; no blocking findings. Next action is exact one-file staging after review.

## Completed Owner Closure Decision Intake Repair Plan

### Goal

Replace the rejected Stage 42 owner selection draft with a neutral owner decision intake record that keeps Option A as the current recommendation while leaving the final owner choice pending.

### Global Strategy

- Produce docs/control evidence only:
  - `PLANS.md`
  - `docs/PROJECT_COMPLETION_OWNER_DECISION_INTAKE.md`
- Remove the rejected `docs/PROJECT_COMPLETION_OWNER_SELECTION.md` draft from the dirty tree.
- Record `OPTION_A_CLOSE_CURRENT_PHASE_AS_LOCAL_PRODUCT_DELIVERY_BASELINE` as the current recommendation only, not as selected.
- Keep official progress at `Project completion progress: 84%`; completion/progress wording can be aligned only after explicit owner selection and `03` review.
- Keep formal registry write, release tag, provider/media production, canon/approval, and production lock out of the current project phase.

### Non-Goals

- No Kotlin/source/test/schema/runtime/CLI behavior edits.
- No Gradle rerun.
- No release tag, release branch, cleanup, delete, move, revert, DB, server, provider, media generation, approval, canon, formal registry write, or production-lock action.
- No progress increase, project-complete wording, or implicit owner selection in this intake-record slice.

### Confirmed Facts

- Current pushed HEAD/upstream are `3e57a1bc654533f30af03446cc8c71e68571f925` / `3e57a1b docs: align closure decision progress`.
- `03` passed the post-closure-decision-brief lifecycle/progress alignment push apply.
- `docs/PIPELINE_LIFECYCLE_MAP.md` marks Stage 42 `Owner closure option selection` as Next.
- `docs/PROJECT_COMPLETION_CLOSURE_DECISION_BRIEF.md` lists Option A as closing the current phase as a local product-delivery baseline and moving formal registry / release / provider-media work to future separately planned projects.
- `03` rejected the prior selected-Option-A draft because the closure brief framed Option A/B/C as an explicit owner decision, not an implicit `01_control` selection.

### Assumptions

- A neutral decision-intake record can preserve the current recommendation and evidence without changing the definition of done.

### Open Questions

- Whether the owner selects Option A, Option B, or Option C for project closure.

### Implementation Steps

- [x] Step 1: Confirm clean pushed Stage 42 baseline and Option A wording.
- [x] Step 2: Create owner selection record for Option A.
- [x] Step 3: Run docs-only verification and send to `03`.
- [x] Step 4: Receive `03` REVISE and identify implicit-selection blocker.
- [x] Step 5: Replace selected record with neutral owner decision intake record.
- [x] Step 6: Run docs-only verification and send to `03`.
- [x] Step 7: Stage, commit, and push only after review.

### Verification

- `git status --short --untracked-files=all`.
- `git diff --check -- PLANS.md`.
- For the new untracked doc, `git diff --no-index --check -- NUL docs/PROJECT_COMPLETION_OWNER_DECISION_INTAKE.md`.
- Trailing whitespace scan on changed docs.
- Confirm `Project completion progress: 84%` remains unchanged in `docs/PROJECT_COMPLETION_PROGRESS.md`.

### Risks

- Accidentally treating Option A as selected instead of recommended.
- Accidentally changing progress/completion wording before explicit owner selection.
- Accidentally implying formal registry write, provider/media readiness, generated media, approval, canon, runtime write, or production lock.

### Drift Log

- 2026-06-28: Started Stage 42 owner closure option selection record after `03` passed pushed lifecycle/progress alignment. Scope is docs/control selection evidence only; official progress remains 84%.
- 2026-06-28: Created `docs/PROJECT_COMPLETION_OWNER_SELECTION.md` selecting Option A under `01_control` as current-phase local product-delivery baseline closure. The record keeps progress at 84% and leaves formal registry, release tag, provider/media production, approval/canon, and production lock out of scope.
- 2026-06-28: Docs-only verification passed: dirty set is `PLANS.md` plus new `docs/PROJECT_COMPLETION_OWNER_SELECTION.md`; diff/no-index checks returned only LF/CRLF warnings; trailing whitespace scan returned no matches; progress remains 84%.
- 2026-06-28: `03` returned REVISE for the selected-Option-A draft. Blocking finding: continuing the project does not equal explicit owner selection among A/B/C. Required repair is a neutral decision-intake record or explicit owner choice before selection.
- 2026-06-28: Replaced the selected Option A draft with `docs/PROJECT_COMPLETION_OWNER_DECISION_INTAKE.md`. The new record marks Option A as the current recommendation, keeps decision status `PENDING_EXPLICIT_OWNER_SELECTION`, and keeps official progress at 84%.
- 2026-06-28: Docs-only repair verification passed: dirty set is `PLANS.md` plus new `docs/PROJECT_COMPLETION_OWNER_DECISION_INTAKE.md`; diff/no-index checks returned only LF/CRLF warnings; trailing whitespace scan returned no matches; progress remains 84%.
- 2026-06-28: `03` passed `minimal owner closure decision intake record REVISE repair`; no blocking findings. The repair preserves pending owner choice, keeps Option A as recommendation only, and leaves progress at 84%. Next action is exact two-file staging after review.
- 2026-06-28: `03` passed staging apply, commit apply, push readiness, and push apply for this intake record. Final pushed HEAD/upstream is `72db12281c91e8bc18305081c2aadde8aec43966`; official progress remains 84%, and owner selection remains pending.

## Completed Post Closure Decision Brief Progress Alignment Plan

### Goal

Align lifecycle and progress docs after the accepted and pushed project completion closure decision brief.

### Global Strategy

- Update docs/control status only:
  - `PLANS.md`
  - `docs/PIPELINE_LIFECYCLE_MAP.md`
  - `docs/PROJECT_COMPLETION_PROGRESS.md`
- Mark Stage 41 as Done and make the next step an owner closure option selection.
- Keep official progress at `Project completion progress: 84%`; the decision brief frames the remaining choice but does not decide it.
- Preserve all no-release-tag/no-provider/no-generation/no-approval/no-canon/no-runtime-write boundaries.

### Non-Goals

- No Kotlin/source/test/schema/runtime/CLI behavior edits.
- No Gradle rerun.
- No release tag, branch change, cleanup, delete, move, revert, DB, server, provider, media generation, approval, canon, formal registry write, or production-lock action.
- No owner selection of Option A/B/C in this alignment slice.

### Confirmed Facts

- Current pushed HEAD/upstream are `b37ff6303ef30184f459055be8a17018a23e4b0a` / `b37ff63 docs: add project completion closure brief`.
- `03` passed the project completion closure decision brief, staging apply, commit apply, push readiness, and push apply.
- `docs/PIPELINE_LIFECYCLE_MAP.md` still marks Stage 41 `Project completion closure decision brief` as Next.
- `docs/PROJECT_COMPLETION_PROGRESS.md` still displays `Project completion progress: 84%`.

### Assumptions

- The correct next project-control step is an explicit owner selection among the closure options, not an implicit completion claim.

### Open Questions

- Whether the owner selects Option A, B, or C for project closure.

### Implementation Steps

- [x] Step 1: Confirm pushed closure decision brief baseline and stale lifecycle/progress docs.
- [x] Step 2: Update lifecycle/progress/plan status wording while keeping progress at 84%.
- [x] Step 3: Run docs-only verification and send to `03`.
- [x] Step 4: Stage, commit, and push docs/control alignment after review.

### Verification

- `git diff --name-only` should list only `PLANS.md`, `docs/PIPELINE_LIFECYCLE_MAP.md`, and `docs/PROJECT_COMPLETION_PROGRESS.md`.
- `git diff --check -- PLANS.md docs/PIPELINE_LIFECYCLE_MAP.md docs/PROJECT_COMPLETION_PROGRESS.md`.
- Trailing whitespace scan on the three docs/control files.
- Confirm displayed progress remains `Project completion progress: 84%`.

### Risks

- Accidentally treating the closure decision brief as owner approval of project completion.
- Accidentally implying release tag, formal registry write, provider/media readiness, approval, canon, runtime write, or production lock.

### Drift Log

- 2026-06-28: Started post-closure-decision-brief docs/control alignment after `03` passed closure brief push apply. Scope is three docs/control files only; official progress remains 84%.
- 2026-06-28: Marked Stage 41 project completion closure decision brief as Done, added Stage 42 owner closure option selection as Next, and kept official progress at `Project completion progress: 84%`.
- 2026-06-28: Docs-only verification passed: changed files are exactly `PLANS.md`, `docs/PIPELINE_LIFECYCLE_MAP.md`, and `docs/PROJECT_COMPLETION_PROGRESS.md`; diff check returned only LF/CRLF warnings; trailing whitespace scan returned no matches.
- 2026-06-28: `03` passed `minimal post-closure-decision-brief lifecycle/progress alignment`; no blocking findings. Next action is exact three-file staging after review.
- 2026-06-28: `03` passed staging apply, commit apply, push readiness, and push apply for this docs/control alignment slice. Final pushed HEAD/upstream is `3e57a1bc654533f30af03446cc8c71e68571f925`; official progress remains 84%.

## Completed Project Completion Closure Decision Brief Plan

### Goal

Create a docs/control decision brief that lets `01` / the user decide what remains before calling this project complete.

### Global Strategy

- Produce decision evidence only:
  - `PLANS.md`
  - `docs/PROJECT_COMPLETION_CLOSURE_DECISION_BRIEF.md`
- Keep official progress at `Project completion progress: 84%`; this slice frames the remaining decision and does not add product behavior.
- Present conservative options for closure:
  - accept the current local product-delivery baseline as the project target and keep formal registry/release as future work;
  - plan a separate formal registry boundary slice;
  - plan a release tag / PR readiness slice.
- Preserve all no-release-tag/no-provider/no-generation/no-approval/no-canon/no-runtime-write boundaries.

### Non-Goals

- No Kotlin/source/test/schema/runtime/CLI behavior edits.
- No Gradle rerun.
- No staging/commit/push until `03` reviews the brief.
- No release tag, branch change, cleanup, delete, move, revert, DB, server, provider, media generation, approval, canon, formal registry write, or production-lock action.

### Confirmed Facts

- Current pushed HEAD/upstream are `43c11ec90c8032c38c83dbbc618f9e9a92ee2978` / `43c11ec docs: align final verification progress`.
- `03` passed the post-final-local-verification lifecycle/progress alignment push apply.
- `docs/PROJECT_COMPLETION_PROGRESS.md` displays `Project completion progress: 84%`.
- `docs/PIPELINE_LIFECYCLE_MAP.md` marks Stage 41 `Project completion closure decision brief` as Next.

### Assumptions

- The remaining gap is a product ownership decision, not a missing local JSON gate in the current pipeline baseline.
- A decision brief is safer than silently changing the definition of done.

### Open Questions

- Whether the current project target should stop at local product-delivery verification, or whether formal registry / release work is in scope before completion.

### Implementation Steps

- [x] Step 1: Confirm clean pushed 84% baseline and Stage 41 scope.
- [x] Step 2: Create project completion closure decision brief.
- [x] Step 3: Run docs-only verification and send to `03`.
- [x] Step 4: Stage, commit, and push only after review.

### Verification

- `git diff --name-only` should list only `PLANS.md` and `docs/PROJECT_COMPLETION_CLOSURE_DECISION_BRIEF.md`.
- `git diff --check -- PLANS.md docs/PROJECT_COMPLETION_CLOSURE_DECISION_BRIEF.md`.
- Trailing whitespace scan on changed docs.
- Confirm progress remains `Project completion progress: 84%`.

### Risks

- Accidentally treating the decision brief as project completion approval.
- Accidentally implying release tag, formal registry write, provider/media readiness, approval, canon, runtime write, or production lock.

### Drift Log

- 2026-06-28: Started Stage 41 project completion closure decision brief after `03` passed pushed progress alignment at 84%. Scope is docs/control decision evidence only.
- 2026-06-28: Created `docs/PROJECT_COMPLETION_CLOSURE_DECISION_BRIEF.md` with Option A current-phase closure, Option B formal registry boundary, Option C release packaging, and Option D provider/media production out-of-scope. Official progress remains 84%.
- 2026-06-28: `03` passed `minimal project completion closure decision brief`; no blocking findings. Next action is exact two-file staging after review.
- 2026-06-28: `03` passed staging apply, commit apply, push readiness, and push apply for this decision brief. Final pushed HEAD/upstream is `b37ff6303ef30184f459055be8a17018a23e4b0a`; official progress remains 84% pending owner option selection.

## Completed Post Final Local Verification Progress Alignment Plan

### Goal

Align lifecycle and progress docs after the accepted and pushed final local product-delivery verification bundle.

### Global Strategy

- Update docs/control status only:
  - `PLANS.md`
  - `docs/PIPELINE_LIFECYCLE_MAP.md`
  - `docs/PROJECT_COMPLETION_PROGRESS.md`
- Mark Stage 40 as Done and make the next slice a project completion closure decision brief.
- Increase official progress only for the now-accepted final local verification evidence.
- Preserve all no-release-tag/no-provider/no-generation/no-approval/no-canon/no-runtime-write boundaries.

### Non-Goals

- No Kotlin/source/test/schema/runtime/CLI behavior edits.
- No Gradle rerun.
- No release tag, release branch, cleanup, delete, move, revert, DB, server, provider, media generation, approval, canon, formal registry write, or production-lock action.
- No `.gitignore`, memory JSON, samples, or unrelated docs beyond the three control files.

### Confirmed Facts

- Current pushed HEAD/upstream are `d42808cac2b8096377ed7c7115249d894a1458fe` / `d42808c docs: add final local verification`.
- `03` passed the final local product-delivery verification report, staging apply, commit apply, push readiness, and push apply.
- `docs/PIPELINE_LIFECYCLE_MAP.md` still marks Stage 40 `Final local product-delivery verification bundle` as Next.
- `docs/PROJECT_COMPLETION_PROGRESS.md` still lists final local product-delivery verification as not yet counted.
- Official progress is `Project completion progress: 82%` before this alignment.

### Assumptions

- Accepted final local verification evidence is worth a conservative 2-point product-deliverable increase.
- Remaining completion risk is now mostly explicit owner decision around formal registry/release boundary, not missing local product-path evidence.

### Open Questions

- Whether the next owner decision should exclude formal registry write from the current project target or plan a separate formal registry boundary slice.

### Implementation Steps

- [x] Step 1: Confirm pushed final verification baseline and stale lifecycle/progress docs.
- [x] Step 2: Update lifecycle/progress/plan status wording and official display estimate.
- [x] Step 3: Run docs-only verification and send to `03`.
- [x] Step 4: Stage, commit, and push docs/control alignment after review.

### Verification

- `git diff --name-only` should list only `PLANS.md`, `docs/PIPELINE_LIFECYCLE_MAP.md`, and `docs/PROJECT_COMPLETION_PROGRESS.md`.
- `git diff --check -- PLANS.md docs/PIPELINE_LIFECYCLE_MAP.md docs/PROJECT_COMPLETION_PROGRESS.md`.
- Trailing whitespace scan on the three docs/control files.
- Confirm displayed progress is `Project completion progress: 84%`.

### Risks

- Accidentally treating local verification evidence as release approval.
- Accidentally implying provider/media readiness, generated image/video/audio, approval, canon, formal registry write, or production lock.
- Accidentally jumping to 100% without an explicit owner decision about the remaining formal registry/release boundary.

### Drift Log

- 2026-06-28: Started post-final-local-verification docs/control alignment after `03` passed final verification push apply. Scope is three docs/control files only.
- 2026-06-28: Marked Stage 40 final local product-delivery verification as Done, added Stage 41 project completion closure decision brief as Next, and updated official display estimate to `Project completion progress: 84%`.
- 2026-06-28: Docs-only verification passed: changed files are exactly `PLANS.md`, `docs/PIPELINE_LIFECYCLE_MAP.md`, and `docs/PROJECT_COMPLETION_PROGRESS.md`; diff check returned only LF/CRLF warnings; trailing whitespace scan returned no matches.
- 2026-06-28: `03` passed `minimal post-final-local-verification lifecycle/progress alignment`; no blocking findings. Next action is exact three-file staging after review.
- 2026-06-28: `03` passed staging apply, commit apply, push readiness, and push apply for this docs/control alignment slice. Final pushed HEAD/upstream is `43c11ec90c8032c38c83dbbc618f9e9a92ee2978`; official progress is now 84%.

## Completed Final Local Product-Delivery Verification Plan

### Goal

Create a final local product-delivery verification bundle that records the clean pushed baseline and focused product-path tests after the E2E image-facing delivery proof fixture.

### Global Strategy

- Produce docs/control evidence only:
  - `PLANS.md`
  - `docs/FINAL_LOCAL_PRODUCT_DELIVERY_VERIFICATION.md`
- Verify the current clean pushed branch and run the smallest aggregate product-path Gradle command that covers:
  - `PipelineHappyPathFixtureTest`
  - `ReviewableStoryboardPacketTest`
  - `ImageFacingDeliveryProofTest`
- Keep the report as verification evidence, not a release tag, provider/media readiness claim, approval, canon, or production lock.

### Non-Goals

- No Kotlin/source/test/schema/runtime/CLI edits.
- No full release tag, release branch, cleanup, delete, move, revert, DB, server, provider, media generation, approval, canon, formal registry write, or production-lock action.
- No `.gitignore`, memory JSON, samples, or unrelated docs.

### Confirmed Facts

- Current pushed HEAD/upstream are `09c8ea75928adcc5d2b0c0b53f1a78fa9a61259e` / `09c8ea7 docs: update image-facing fixture progress`.
- `03` passed the post E2E image-facing proof lifecycle/progress alignment push apply.
- `docs/PIPELINE_LIFECYCLE_MAP.md` marks Stage 40 `Final local product-delivery verification bundle` as Next.
- Official progress is `Project completion progress: 82%` before this verification report.

### Assumptions

- A focused aggregate command covering the current product path is enough for this local verification slice; full test suite remains a separate heavier release decision.

### Open Questions

- Whether later progress should increase after `03` accepts this report; this slice will not change the progress file before review.

### Implementation Steps

- [x] Step 1: Confirm clean pushed baseline and Stage 40 scope.
- [x] Step 2: Run focused aggregate product-path verification.
- [x] Step 3: Create final local verification report.
- [x] Step 4: Run docs-only hygiene checks and send to `03`.
- [x] Step 5: Stage, commit, and push only after review.

### Verification

- `git status --short --untracked-files=all`.
- `git rev-parse HEAD`, `git rev-parse @{u}`, and `git rev-list --left-right --count @{u}...HEAD`.
- Focused aggregate Gradle command:
  `./gradlew.bat test --tests "*PipelineHappyPathFixtureTest" --tests "*ReviewableStoryboardPacketTest" --tests "*ImageFacingDeliveryProofTest" --no-daemon --console=plain`
- `git diff --check -- PLANS.md docs/FINAL_LOCAL_PRODUCT_DELIVERY_VERIFICATION.md`.
- Trailing whitespace scan on changed docs.

### Risks

- Treating local verification evidence as formal release approval.
- Running too broad a test set while the user is using the machine.

### Drift Log

- 2026-06-28: Started Stage 40 final local product-delivery verification bundle after `03` passed pushed progress alignment. Scope is docs/control evidence only.
- 2026-06-28: Focused aggregate product-path verification passed: `./gradlew.bat test --tests "*PipelineHappyPathFixtureTest" --tests "*ReviewableStoryboardPacketTest" --tests "*ImageFacingDeliveryProofTest" --no-daemon --console=plain` returned BUILD SUCCESSFUL in 30s.
- 2026-06-28: Created `docs/FINAL_LOCAL_PRODUCT_DELIVERY_VERIFICATION.md` as docs/control evidence only. Hygiene checks passed: changed paths are `PLANS.md` plus the new report, diff/no-index checks returned only LF/CRLF warnings, and trailing whitespace scan returned no matches.
- 2026-06-28: `03` passed the final local verification report, staging apply, commit apply, push readiness, and push apply. Final pushed HEAD/upstream is `d42808cac2b8096377ed7c7115249d894a1458fe`; official progress remains 82% until progress docs are updated and reviewed.

## Completed Post E2E Image-Facing Delivery Proof Alignment Plan

### Goal

Align lifecycle and progress docs after the accepted and pushed E2E image-facing delivery proof fixture.

### Global Strategy

- Update docs/control status only:
  - `PLANS.md`
  - `docs/PIPELINE_LIFECYCLE_MAP.md`
  - `docs/PROJECT_COMPLETION_PROGRESS.md`
- Mark Stage 39 as Done and make the next slice a final local product-delivery verification bundle.
- Increase official progress only for the now-accepted fixture that was explicitly listed as not yet counted.
- Preserve all no-provider/no-generation/no-approval/no-canon/no-runtime-write boundaries.

### Non-Goals

- No Kotlin/source/test/schema/runtime/CLI behavior edits.
- No Gradle rerun.
- No `.gitignore`, memory JSON, sample, cleanup, delete, move, revert, DB, server, provider, media generation, approval, canon, or production-lock changes.

### Confirmed Facts

- Current pushed HEAD/upstream are `66112aef302a1ec166076763f614987a07e6c7b8` / `66112ae test: extend image-facing delivery proof fixture`.
- `03` passed `minimal E2E image-facing delivery proof fixture`, staging apply, commit apply, push readiness, and push apply.
- `docs/PIPELINE_LIFECYCLE_MAP.md` still marks Stage 39 as `Next`.
- `docs/PROJECT_COMPLETION_PROGRESS.md` still lists the E2E image-facing delivery proof fixture as not yet counted.

### Assumptions

- Completing the previously uncounted fixture should raise the product-deliverable estimate from 80% to 82% by increasing the reviewable storyboard / image-facing deliverable path row from 13 to 15.

### Open Questions

- Whether the final local product-delivery verification bundle should be a docs/control evidence report only, or include an additional aggregate test command.

### Implementation Steps

- [x] Step 1: Confirm pushed E2E fixture baseline and stale lifecycle/progress docs.
- [x] Step 2: Update lifecycle/progress/plan status wording and official display estimate.
- [x] Step 3: Run docs-only verification and send to `03`.
- [x] Step 4: Stage, commit, and push docs/control alignment after review.

### Verification

- `git diff --name-only` should list only `PLANS.md`, `docs/PIPELINE_LIFECYCLE_MAP.md`, and `docs/PROJECT_COMPLETION_PROGRESS.md`.
- `git diff --check -- PLANS.md docs/PIPELINE_LIFECYCLE_MAP.md docs/PROJECT_COMPLETION_PROGRESS.md`.
- Trailing whitespace scan on the three docs/control files.
- Confirm displayed progress is `Project completion progress: 82%`.

### Risks

- Over-counting progress from a test/product-proof fixture.
- Accidentally implying provider/media readiness, generated image/video, approval, canon, formal registry write, or production lock.

### Drift Log

- 2026-06-28: Started post-E2E fixture docs/control alignment after `03` passed E2E fixture push apply. Scope is three docs/control files only.
- 2026-06-28: Progress adjustment is limited to the previously listed missing E2E image-facing fixture; final delivery verification and formal registry boundary remain not counted.
- 2026-06-28: Marked Stage 39 E2E image-facing delivery proof fixture as Done, added Stage 40 final local product-delivery verification bundle as Next, and updated official display estimate to `Project completion progress: 82%`.
- 2026-06-28: Docs-only verification passed: changed files are exactly `PLANS.md`, `docs/PIPELINE_LIFECYCLE_MAP.md`, and `docs/PROJECT_COMPLETION_PROGRESS.md`; diff check returned only LF/CRLF warnings; trailing whitespace scan returned no matches.
- 2026-06-28: `03` passed docs/control alignment, staging apply, commit apply, push readiness, and push apply. Final pushed HEAD/upstream is `09c8ea75928adcc5d2b0c0b53f1a78fa9a61259e`; official progress is now 82%.

## Completed E2E Image-Facing Delivery Proof Fixture Plan

### Goal

Add the smallest end-to-end product-proof fixture that starts from an accepted script packet, reaches `ReviewableStoryboardPacket`, then produces `ImageFacingDeliveryProof` without adding new production behavior.

### Global Strategy

- Extend existing test/product-proof coverage rather than adding new service/model/CLI surfaces.
- Reuse the real local JSON service chain already covered by `PipelineHappyPathFixtureTest`, then continue through:
  - `StoryboardStoryMovementAuditService`
  - `ReviewableStoryboardPacketService`
  - `ImageFacingDeliveryProofService`
- Keep the slice test-only plus `PLANS.md`.
- Preserve candidate-only, reviewable, non-canon, non-production boundaries.

### Non-Goals

- No new CLI command, model, service, schema migration, SQLite write, runtime `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.
- No provider API/job, remote download, image/video/audio generation, output path, registry write, asset approval, approved/registered reference, canon, or production lock.
- No docs/progress increase in this implementation step; progress can be reconsidered only after review.

### Confirmed Facts

- Current pushed HEAD/upstream are `3ec5e64f6e36f1715c32b5b6c56e1d3da475ae6a` / `3ec5e64 docs: align image-facing proof progress`.
- `03` passed the post image-facing delivery proof lifecycle/progress alignment push apply.
- `PipelineHappyPathFixtureTest` currently starts from `AcceptedScriptPacketHandoffPacket` and reaches `StoryboardPacketHumanReviewPreflightReport` with `nextAllowedDownstreamStep=storyboard_packet_human_review_ready`.
- `ReviewableStoryboardPacketService` can package draft/review/preflight/audit inputs into `ReviewableStoryboardPacket` with `nextAllowedDownstreamStep=reviewable_storyboard_packet_ready`.
- `ImageFacingDeliveryProofService` can create `ImageFacingDeliveryProof` from a passed `ReviewableStoryboardPacket`.

### Assumptions

- Extending `PipelineHappyPathFixtureTest` is the narrowest coherent E2E proof because it already owns the accepted-script to storyboard-human-review-ready path.

### Open Questions

- Whether later product scoring should increase after `03` accepts the fixture; this slice itself will not change the score.

### Implementation Steps

- [x] Step 1: Inspect current happy-path, reviewable packet, story movement audit, and image-facing proof tests/services.
- [x] Step 2: Extend the E2E fixture to create story movement audit, reviewable storyboard packet, and image-facing delivery proof.
- [x] Step 3: Run focused fixture verification and docs/test safety checks.
- [x] Step 4: Send to `03` for read-only review.
- [x] Step 5: Stage, commit, and push only after review.

### Verification

- Focused test: `./gradlew.bat test --tests "*PipelineHappyPathFixtureTest" --no-daemon --console=plain`.
- `git diff --check -- PLANS.md app/src/test/kotlin/com/localnovel/engine/PipelineHappyPathFixtureTest.kt`.
- Trailing whitespace scan on changed files.
- Safety scan confirming no DB/server/provider/media/runtime implementation surfaces.

### Risks

- Accidentally implying that `ImageFacingDeliveryProof` is provider-ready or generated media.
- Duplicating too much fixture setup instead of reusing the existing happy-path chain.

### Drift Log

- 2026-06-28: Started Stage 39 E2E fixture slice after `03` passed pushed docs/control alignment. Scope is `PLANS.md` plus `PipelineHappyPathFixtureTest` unless evidence requires otherwise.
- 2026-06-28: Evidence pass confirmed no new model/service/CLI is needed; the fixture can extend the existing accepted-script-to-human-review-ready chain through story movement audit, reviewable storyboard packet, and image-facing delivery proof.
- 2026-06-28: Extended `PipelineHappyPathFixtureTest` to create `StoryboardStoryMovementAuditReport`, `ReviewableStoryboardPacket`, and `ImageFacingDeliveryProof` from the existing accepted-script fixture, with boundary assertions for candidate-only product proof output.
- 2026-06-28: Focused verification passed: `./gradlew.bat test --tests "*PipelineHappyPathFixtureTest" --no-daemon --console=plain` returned BUILD SUCCESSFUL. Diff check returned only LF/CRLF warnings; trailing whitespace scan returned no matches; safety scan hits were limited to forbidden-boundary assertion strings inside the test.
- 2026-06-28: `03` passed `minimal E2E image-facing delivery proof fixture` with no blocking findings; accepted as test/product-proof plus plan only.
- 2026-06-28: `03` passed staging apply, commit apply, push readiness, and push apply. Final pushed HEAD/upstream is `66112aef302a1ec166076763f614987a07e6c7b8`; official progress remains 80% until the progress docs are updated and reviewed.

## Completed Post Image-Facing Delivery Proof Alignment Plan

### Goal

Align control documentation after the accepted and pushed `ImageFacingDeliveryProof` gate so the plan and lifecycle map no longer show the completed proof gate as pending.

### Global Strategy

- Update docs/control status only:
  - `PLANS.md`
  - `docs/PIPELINE_LIFECYCLE_MAP.md`
  - `docs/PROJECT_COMPLETION_PROGRESS.md`
- Keep official progress at `80%`; the proof gate is accepted, but the next product proof still needs an end-to-end fixture from accepted draft through `ReviewableStoryboardPacket` into `ImageFacingDeliveryProof`.
- Preserve all no-provider/no-generation/no-approval/no-canon/no-runtime-write boundaries.

### Non-Goals

- No Kotlin/source/test/schema/runtime/CLI behavior edits.
- No Gradle rerun.
- No memory JSON, `.gitignore`, sample, cleanup, delete, move, revert, DB, server, provider, media generation, approval, canon, or production-lock changes.

### Confirmed Facts

- `03` passed `minimal image-facing delivery proof gate`, staging apply, commit apply, push readiness, and push apply.
- Current pushed HEAD/upstream are `fe866e3116fa28908c0b493c5849347759767bfd` / `fe866e3 feat: add image-facing delivery proof gate`.
- The pushed commit exact file set was the accepted five-file proof gate set.
- `docs/PIPELINE_LIFECYCLE_MAP.md` still marks Stage 38 as `Next`.
- `docs/PROJECT_COMPLETION_PROGRESS.md` still lists image-facing delivery proof itself as not yet counted.

### Assumptions

- The next product-completion slice should be an E2E proof fixture from accepted draft to image-facing delivery proof, not provider/media generation.

### Open Questions

- Whether the future E2E fixture should reuse `PipelineHappyPathFixtureTest` or create a narrower dedicated fixture.

### Implementation Steps

- [x] Step 1: Confirm pushed proof gate baseline and stale docs/control status.
- [x] Step 2: Update lifecycle/progress/plan status wording.
- [x] Step 3: Run docs-only verification and send to `03`.
- [x] Step 4: Commit and push docs/control alignment after review.

### Verification

- `git diff --name-only` should list only `PLANS.md`, `docs/PIPELINE_LIFECYCLE_MAP.md`, and `docs/PROJECT_COMPLETION_PROGRESS.md`.
- `git diff --check -- PLANS.md docs/PIPELINE_LIFECYCLE_MAP.md docs/PROJECT_COMPLETION_PROGRESS.md`.
- Trailing whitespace scan on the three docs/control files.
- Confirm displayed progress remains `Project completion progress: 80%`.

### Risks

- Accidentally increasing product progress without an E2E product-delivery fixture.
- Accidentally implying image/provider readiness, generated media, approval, canon, or production lock.

### Drift Log

- 2026-06-28: Started post-push docs/control alignment after `03` passed image-facing delivery proof push apply. Scope is three docs/control files only; official progress remains 80%.
- 2026-06-28: Marked Stage 38 image-facing delivery proof as Done, added Stage 39 E2E image-facing delivery proof fixture as Next, and updated progress evidence while keeping official progress at 80%.
- 2026-06-28: Docs-only verification passed: changed files are exactly `PLANS.md`, `docs/PIPELINE_LIFECYCLE_MAP.md`, and `docs/PROJECT_COMPLETION_PROGRESS.md`; diff check returned only LF/CRLF warnings; trailing whitespace scan returned no matches; progress display remains 80%.
- 2026-06-28: `03` passed docs/control alignment, staging apply, commit apply, push readiness, and push apply. Final pushed HEAD/upstream is `3ec5e64f6e36f1715c32b5b6c56e1d3da475ae6a`; official progress remains 80%.

## Completed Image-Facing Delivery Proof Plan

### Goal

Add the smallest local JSON product proof that turns a passed `ReviewableStoryboardPacket` into an image-facing delivery proof artifact without generating images, calling providers, approving assets, writing runtime state, or claiming canon/production readiness.

### Global Strategy

- Treat this as a product proof gate after `reviewable_storyboard_packet_ready`.
- Read only a `ReviewableStoryboardPacket` JSON and produce a deterministic local JSON report.
- Preserve the candidate-only boundary: the report may summarize frame-level visual delivery requirements, but it must not become a provider handoff, generated image record, approved reference, registered reference, canon, or production lock.
- Keep source binding from the reviewable storyboard packet: packet file/id, accepted script packet id, script packet draft file, and script packet draft SHA-256.
- Add focused tests before any broader verification.

### Non-Goals

- No image/video/audio generation.
- No provider API, provider job, remote download, output path, or generated media storage.
- No formal registry write, asset approval, approved/registered reference, canon, user approval, runtime scene write, or production lock.
- No SQLite, schema migration, server/port, browser, React/Next, Docker, or runtime `resolve-turn` / `StateReducer` changes.
- No changes to memory JSON files or `.gitignore`.

### Confirmed Facts

- Current canonical workspace is `C:\Users\f7212\Projects\AI 劇本`.
- Current HEAD/upstream are `4d0a5d8332faff7493afef829f79b66c81f2482c` / `4d0a5d8 docs: align lifecycle progress baseline`.
- `docs/PIPELINE_LIFECYCLE_MAP.md` marks Stage 38, `Image-facing delivery proof after reviewable storyboard packet`, as the next slice.
- `ReviewableStoryboardPacket` exists and passes to `nextAllowedDownstreamStep=reviewable_storyboard_packet_ready`.
- Existing image-candidate gates remain provider-free and candidate-only, but they start from the asset-candidate human review path rather than directly from `ReviewableStoryboardPacket`.

### Assumptions

- A proof artifact that summarizes image-facing frame delivery cards from `ReviewableStoryboardPacket.frameSummaries` is the smallest coherent product-completion step.
- This proof should use a new downstream marker instead of pretending to approve existing image-candidate or provider handoff gates.

### Open Questions

- Whether a later slice should connect this proof to the existing image-candidate human review path, or keep it as a reviewable product artifact only.

### Implementation Steps

- [x] Step 1: Confirm clean pushed baseline and inspect current storyboard/image-candidate model and CLI boundaries.
- [x] Step 2: Add `ImageFacingDeliveryProof` model, service, CLI handler, and focused tests.
- [x] Step 3: Run focused verification and safety/diff checks.
- [x] Step 4: Send the slice to `03` for read-only review.
- [x] Step 5: Stage the exact accepted five-file implementation set for commit review.
- [x] Step 6: Commit the exact accepted image-facing delivery proof gate set.
- [x] Step 7: Push the commit after push-readiness review.

### Verification

- Focused tests for the new proof gate.
- CLI smoke test for the new command, if a CLI command is added.
- `git diff --check` over changed files.
- Trailing whitespace scan over new/changed service and test files.
- Safety scan for SQLite/server/provider/browser/runtime boundary keywords in new/changed service/test files.

### Risks

- Accidentally turning image-facing proof into provider readiness.
- Under-validating forged `ReviewableStoryboardPacket` direct input.
- Letting `assetCandidateRefs` or frame text carry status-upgrade wording such as approved reference, registered reference, canon, or production lock.

### Drift Log

- 2026-06-28: Started the next product-completion slice after `03` passed and the branch pushed `minimal post-clean lifecycle and progress alignment`. Scope is a local JSON proof gate after `ReviewableStoryboardPacket`; official progress remains 80% until reviewed product proof evidence is accepted.
- 2026-06-28: Added `ImageFacingDeliveryProof` local JSON model/service/CLI/test. Focused test `.\gradlew.bat test --tests "*ImageFacingDeliveryProofTest" --rerun-tasks --no-daemon --console=plain` passed; `git diff --check` passed with only LF/CRLF warnings; trailing whitespace scan returned no matches. Safety scan found only expected guardrail wording in the new service, not provider/API/runtime execution.
- 2026-06-28: Hardened the proof text scan to include direct source fields `packetId`, `acceptedScriptPacketId`, and `scriptPacketDraftFile`; added regression coverage for `scriptPacketDraftFile=https://...` failing with `REMOTE_REFERENCE_FORBIDDEN`. Focused test `.\gradlew.bat test --tests "*ImageFacingDeliveryProofTest" --no-daemon --console=plain` passed.
- 2026-06-28: `03` passed `minimal image-facing delivery proof gate`, confirming the slice remains Kotlin/JVM CLI + JSON file protocol only, with no provider/image generation/approval/canon/production-lock boundary drift. `03` could not rerun Gradle due local file locks and relied on submitted BUILD SUCCESSFUL plus read-only code review; 01/02 focused test had passed before handoff.
- 2026-06-28: Staged exactly the accepted five-file set for this slice: `PLANS.md`, `Main.kt`, `Models.kt`, `ImageFacingDeliveryProofService.kt`, and `ImageFacingDeliveryProofTest.kt`. Cached diff check passed and no unstaged drift remained inside the staged set.
- 2026-06-28: `03` passed staging apply, commit apply, push readiness, and push apply for this slice. Final pushed HEAD/upstream is `fe866e3116fa28908c0b493c5849347759767bfd`; official progress remains 80%.

## Completed Post-Clean Lifecycle and Progress Alignment Plan

### Goal

Align control documentation after the accepted local-memory exact ignore commit/push so the project map no longer points at already-completed staging, commit, or push slices.

### Global Strategy

- Update only docs/control status surfaces:
  - `PLANS.md`
  - `docs/PIPELINE_LIFECYCLE_MAP.md`
  - `docs/PROJECT_COMPLETION_PROGRESS.md`
- Keep official progress at `80%`; this slice is lifecycle/progress wording alignment, not a new product-deliverable capability.
- Make the next product-completion target explicit: image-facing delivery proof after the `ReviewableStoryboardPacket` artifact.
- Preserve the clean canonical workspace baseline at `C:\Users\f7212\Projects\AI 劇本`.

### Non-Goals

- No `.gitignore` edits.
- No memory JSON edits, staging, repair, delete, move, or cleanup.
- No source, test, schema, runtime, sample, or CLI behavior changes.
- No Gradle test rerun unless a later implementation slice needs it.
- No formal registry write, asset approval, approved/registered reference, canon, provider job, image/video/audio generation, or production lock.

### Confirmed Facts

- `03` passed `minimal local-memory exact ignore push apply` before this lifecycle/progress alignment slice.
- This lifecycle/progress alignment slice was committed and pushed as `4d0a5d8332faff7493afef829f79b66c81f2482c` / `4d0a5d8 docs: align lifecycle progress baseline`.
- `docs/PIPELINE_LIFECYCLE_MAP.md` still lists older release/staging states as active or next.
- `docs/PROJECT_COMPLETION_PROGRESS.md` still lists last control review only through early control-doc staging.
- The top local-memory ignore plan was completed and pushed, so it should no longer be the active plan.

### Assumptions

- Updating lifecycle/progress docs after completed staging/commit/push flow does not itself increase product-deliverable completion.

### Open Questions

- Exact design of the next image-facing delivery proof slice.

### Implementation Steps

- [x] Step 1: Confirm stale lifecycle/progress wording in the three control files.
- [x] Step 2: Update `PLANS.md` active plan and local-memory ignore plan status.
- [x] Step 3: Update lifecycle map statuses and next-slice order.
- [x] Step 4: Update progress document last-control-review and release hygiene wording.
- [x] Step 5: Run docs-only verification and send evidence to `03`.

### Verification

- `git diff --name-only` should list only:
  - `PLANS.md`
  - `docs/PIPELINE_LIFECYCLE_MAP.md`
  - `docs/PROJECT_COMPLETION_PROGRESS.md`
- `git diff --check -- PLANS.md docs/PIPELINE_LIFECYCLE_MAP.md docs/PROJECT_COMPLETION_PROGRESS.md`
- Trailing whitespace scan on the three changed files.
- Confirm no source/test/schema/runtime/CLI, memory JSON, `.gitignore`, or sample files changed.
- Confirm displayed progress remains `Project completion progress: 80%`.

### Risks

- Accidentally claiming release completion from docs/control cleanup.
- Accidentally raising progress without a product-deliverable slice.
- Leaving the lifecycle map pointing at completed commit/push tasks as the next work item.

### Drift Log

- 2026-06-28: Started `minimal post-clean lifecycle and progress alignment` after `03` passed the exact local-memory ignore push apply. Scope is three docs/control files only; official progress remains 80%.
- 2026-06-28: Docs-only verification passed: changed files are exactly `PLANS.md`, `docs/PIPELINE_LIFECYCLE_MAP.md`, and `docs/PROJECT_COMPLETION_PROGRESS.md`; diff check returned only LF/CRLF warnings; trailing whitespace scan returned no matches. Remaining older Stage 26/34 references in lower `PLANS.md` sections are historical drift logs, not the active plan.
- 2026-06-28: `03` passed staging, commit, push readiness, and push apply for this docs/control alignment slice. Final pushed HEAD/upstream is `4d0a5d8332faff7493afef829f79b66c81f2482c`; official progress remains 80%.

## Completed Local-Memory Exact Ignore Apply Plan

### Goal

Apply the explicit `01` owner decision to keep the two local memory support files untracked/local while preventing them from continuing to appear as dirty git status noise.

### Global Strategy

- Edit only `.gitignore` and `PLANS.md`.
- Add anchored exact ignore rules for only:
  - `/memory/project_directory_memory.json`
  - `/memory/quarterfull_world_memory_capsule.json`
- Keep both memory JSON files local-only and do not stage, commit, repair, delete, move, or rewrite them.
- Keep official progress at `80%`; this is workspace hygiene for local memory support, not product/release progress.

### Non-Goals

- No memory JSON edits.
- No broad ignore rule such as `/memory/` or `memory/*.json`.
- No staging, commit, push, fetch, cleanup, delete, move, or revert.
- No `.gitignore` rule for any docs, source, test, schema, runtime, sample, or app file.
- No app/source/test/schema/runtime/CLI behavior changes.
- No SQLite/runtime write path, provider/API/media work, canon, approval, or production-lock claim.

### Confirmed Facts

- `03` passed `minimal remaining memory surface disposition refresh after parser evidence docs`.
- `01` explicitly decided both memory files are local-only memory support files.
- `memory/project_directory_memory.json` and `memory/quarterfull_world_memory_capsule.json` must not be staged or committed as part of this decision.
- HEAD/upstream after push are `66111b4085fbf160dc181c843005a29fad456d33`.
- Official progress remains `80%`.

### Assumptions

- Anchored exact ignore rules are sufficient to hide only the two local memory support files and no other memory/docs files.

### Open Questions

- None for this completed slice. The apply, staging, commit, and push flow was completed and passed `03` review.

### Implementation Steps

- [x] Step 1: Verify baseline, clean index/tracked diff, and exact untracked memory set.
- [x] Step 2: Add exact two-file local memory ignore rules and record this owner decision in `PLANS.md`.
- [x] Step 3: Run post-apply verification and send evidence to `03`.

### Verification

- `git diff --name-only` exact changed tracked files: `.gitignore` and `PLANS.md`.
- `git status --short --untracked-files=all` no longer shows the two memory files as untracked.
- `git check-ignore -v -- memory/project_directory_memory.json memory/quarterfull_world_memory_capsule.json` matches the two exact rules.
- `git check-ignore -v -- memory/other.json memory/nested/project_directory_memory.json docs/MEMORY_OWNER_DECISION.md` returns no matches.
- `git diff --check -- .gitignore PLANS.md`.
- Trailing whitespace scan on `.gitignore` and changed `PLANS.md` content.
- Confirm memory JSON files and app/source/test/schema/runtime/CLI files were not changed.

### Risks

- Accidentally adding a broad ignore rule that hides future memory work.
- Accidentally staging or modifying local memory JSON instead of only ignoring them.
- Treating local memory support as product/runtime/canon state.

### Drift Log

- 2026-06-28: Started `minimal local-memory exact ignore apply` after `01` explicit owner decision. Scope is exact `.gitignore` and `PLANS.md` only; memory files remain local-only and untouched.
- 2026-06-28: Post-apply verification confirmed only `.gitignore` and `PLANS.md` changed, the two local memory files are ignored by exact anchored rules, negative ignore checks do not match adjacent paths/docs, and memory/source/runtime files remain untouched.
- 2026-06-28: `03` passed exact ignore staging, commit, push readiness, and push apply. Final pushed HEAD/upstream is `66111b4085fbf160dc181c843005a29fad456d33`; memory JSON files remain local-only and are not in the commit.

## Active Memory Parser Evidence Wording Correction Plan

### Goal

Correct docs/control wording for local memory parser evidence after `03` command capture showed the QuarterFull memory capsule is command/host encoding-sensitive, not universally invalid JSON.

### Global Strategy

- Update only docs/control evidence wording.
- Keep both memory files untracked/local and do not repair, stage, delete, or ignore them in this slice.
- Preserve the conservative boundary: local memory is not canon, runtime state, SQLite input/write path, approval evidence, provider/media readiness, or production lock.
- Record this slice in `PLANS.md` because the repo uses `PLANS.md` as the active handoff and drift-control surface.

### Non-Goals

- No memory JSON edits.
- No `.gitignore` edits.
- No staging, commit, push, fetch, cleanup, delete, move, or revert.
- No app/source/test/schema/runtime/CLI behavior changes.
- No SQLite/runtime write path, provider/API/media work, canon, approval, or production-lock claim.

### Confirmed Facts

- `03` command capture reported `memory/quarterfull_world_memory_capsule.json` SHA-256 `5a24dfced4475d6ee9eee0d0dc4ed392eb9d347b91a3b94e5941c2d1ce8ee9f6`.
- Node `JSON.parse` parses `memory/quarterfull_world_memory_capsule.json` and reports status `ACTIVE_LOCAL_WORLD_MEMORY`.
- Windows PowerShell 5.1 default `Get-Content -Raw | ConvertFrom-Json` can fail due to mojibake/default decoding.
- Explicit UTF-8 PowerShell reads parse the capsule successfully.
- The correct interpretation is command/host encoding-sensitive parser behavior, not universal JSON syntax failure.
- Official progress remains `80%`.

### Assumptions

- The next safe step is a docs/control wording correction only; memory ignore/staging decisions remain separate owner-approved slices.

### Open Questions

- Whether `01` will later approve a precise `.gitignore` apply slice for the two local memory files.

### Implementation Steps

- [x] Step 1: Update `docs/MEMORY_OWNER_DECISION.md` parser status and future gate wording.
- [x] Step 2: Update `docs/QUARTERFULL_WORLD_ALIGNMENT.md` verification wording.
- [x] Step 3: Record this slice in `PLANS.md`.
- [x] Step 4: Run docs-only verification and send evidence to `03`.

### Verification

- `git diff --check -- PLANS.md docs/MEMORY_OWNER_DECISION.md docs/QUARTERFULL_WORLD_ALIGNMENT.md`
- Trailing whitespace scan on changed docs.
- Old active workspace path scan on changed docs.
- Read-only parser evidence: explicit UTF-8 PowerShell OK and Node OK; default PowerShell may fail and must be documented as encoding-sensitive.
- `git diff --name-only` includes only intended docs.

### Risks

- Accidentally implying memory files are ready to stage.
- Accidentally erasing the conservative owner-decision boundary.
- Confusing default PowerShell decoding failure with universal JSON syntax failure.

### Drift Log

- 2026-06-28: Started `minimal memory parser evidence wording correction` after `03` command capture and `01` handoff clarified the parser discrepancy is command/host encoding-sensitive. Scope remains docs/control only; memory files stay untracked/local.
- 2026-06-28: Verification completed for the wording correction: diff check passed with only LF/CRLF warnings, explicit UTF-8 PowerShell and Node parser checks passed, default PowerShell failure remained documented as encoding-sensitive, and only the intended docs/control files changed.

## Active Control-Doc Commit Decision / Apply Plan

### Goal

Prepare the next decision boundary after `minimal control-doc staging apply` reached `03` PASS and the refreshed staged set is sent back to `03`: decide whether `01` should authorize a control-doc-only commit in a separate explicit step.

### Global Strategy

- Treat the current staged state as a reviewed control-doc handoff candidate, not a commit approval.
- Preserve the exact Option A staged set unless `01` explicitly approves a different action.
- Keep the next step decision-only unless `01` explicitly authorizes commit apply.
- Do not push, branch, cleanup, delete, move, revert, or stage non-Option-A files.

### Non-Goals

- No commit, push, branch change, release tag, cleanup apply, file delete, file move, revert, or generated package in this plan until `01` explicitly authorizes the next operation.
- No Kotlin/source/test/schema/runtime/CLI behavior edits.
- No staging outside the exact Option A candidate files.
- No image/video/audio generation, provider API/job, remote download, asset approval, approved reference, registered reference, user approval, canon, production lock, runtime scene creation, SQLite/runtime write, schema migration, server/port, QuarterFull/browser, or old OneDrive workspace edits.
- No `--db`, `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.
- No unattended scheduler / automation advancement.

### Confirmed Facts

- `03` passed `minimal control-doc staging apply` on 2026-06-27.
- The git index was confirmed by `03` to contain exactly the 10 Option A control docs.
- Official product progress remains `80%` because staging is release handoff progress, not new product behavior and not release approval.
- `docs/PIPELINE_LIFECYCLE_MAP.md` now marks Stage 33 done and Stage 34 next: `Control-doc commit decision / apply`.
- This staged-set refresh updated only files inside the already-approved Option A set and restaged the exact same 10 files.

### Assumptions

- The next safe step after `03` reviews this refreshed staged state is an explicit `01` commit decision, not an automatic commit.
- The exact staged set should remain stable unless `01` decides to unstage, commit, or broaden the candidate.

### Open Questions

- Whether `01` will authorize a commit after `03` reviews the refreshed staged state.
- Whether a future commit should be followed by a push or by another local verification slice.

### Implementation Steps

- [x] Step 1: Receive `03` PASS for control-doc staging apply.
- [x] Step 2: Refresh staged control docs with the apply PASS status while keeping progress at `80%`.
- [x] Step 3: Restage the exact same 10 Option A files and send staged-state evidence to `03`.
- [ ] Step 4: On 03 PASS / REVISE, show progress and decide whether commit approval is appropriate.

### Verification

- `git diff --cached --name-only` exact 10 check before and after refresh.
- `git status --short --untracked-files=all`.
- `git diff --check --` plus exact 10 files.
- Old workspace full-path scan on touched control docs, with `PLANS.md` historical relocation notes separated from current canonical instructions.
- `git diff --cached --check`.
- `git diff --name-only` must still show excluded unstaged tracked implementation/planning surfaces and no unstaged drift in the 10 staged files.

### Risks

- Accidentally treating a staged set as commit approval.
- Accidentally staging an eleventh file during refresh.
- Creating staged + unstaged drift inside the 10 Option A files.
- Forgetting that implementation files remain dirty and intentionally unstaged.

### Drift Log

- 2026-06-27: `03` returned PASS for `minimal control-doc staging apply`; `01` kept official progress at 80% because staging is not product behavior, commit approval, or release approval.
- 2026-06-27: Lifecycle Stage 33 marked Done and Stage 34 added as Next: `Control-doc commit decision / apply`. This next boundary still requires explicit `01` authorization before any commit.
- 2026-06-27: Refreshed staged-set control docs to record the apply PASS, verified the pre-refresh cached set was exactly the 10 Option A files, and prepared to restage the same 10 files for `03` review.

## Completed Control-Doc Staging Apply Plan

### Goal

Apply the accepted Option A control-doc staging candidate to the git index as an exact-file operation after `minimal control-doc staging candidate dry run` reached `03` PASS.

### Global Strategy

- Treat the passed dry-run as sufficient 01 control evidence to approve one narrow staging action: `git add --` only the exact Option A control-doc candidate files.
- Keep this slice version-control staging only: no commit, push, branch change, cleanup, delete, move, revert, or implementation edits.
- Require 02 to rerun inventory and candidate file status immediately before staging.
- Require 02 to verify the staged set after `git add` and send the staged-state evidence to 03 for read-only review.

### Non-Goals

- No commit, push, branch change, release tag, cleanup apply, file delete, file move, revert, or generated package.
- No Kotlin/source/test/schema/runtime/CLI behavior edits.
- No staging outside the exact Option A candidate files.
- No image/video/audio generation, provider API/job, remote download, asset approval, approved reference, registered reference, user approval, canon, production lock, runtime scene creation, SQLite/runtime write, schema migration, server/port, QuarterFull/browser, or old OneDrive workspace edits.
- No `--db`, `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.
- No unattended scheduler / automation advancement.

### Confirmed Facts

- `03` passed `minimal control-doc staging candidate dry run` on 2026-06-27.
- The accepted dry-run records inventory after the report: tracked modified `7`, untracked total `123`, services `48`, tests `49`, docs `17`, samples `7`, memory entries `2`, other `0`, classified sum `123`.
- The accepted dry-run exact Option A candidate contains `PLANS.md` plus nine control-doc reports, including `docs/CONTROL_DOC_STAGING_CANDIDATE_DRY_RUN.md`.
- The accepted dry-run explicitly excludes `Main.kt`, `Models.kt`, service/test groups, samples, memory, and planning-only/historical docs.
- Official product progress remains `80%` because the dry-run is not product behavior and did not stage files.
- `docs/PIPELINE_LIFECYCLE_MAP.md` now marks Stage 32 done and Stage 33 next: `Control-doc staging apply`.

### Assumptions

- Option A is still the safest first actual staging action because it excludes broad source/test/shared implementation surfaces.
- If inventory changes before 02 applies staging, 02 should record the drift and continue only if the exact Option A candidate files are still present and no extra files are staged.

### Open Questions

- After 03 reviews the staged state, whether 01 should approve a commit, unstage, or continue with further staged grouping.

### Implementation Steps

- [x] Step 1: Receive `03` PASS for control-doc staging candidate dry run.
- [x] Step 2: Update progress and lifecycle control docs without increasing percentage.
- [ ] Step 3: Dispatch exact Option A staging apply to 02.
- [ ] Step 4: Wait for 02 to stage exact files and send staged-state evidence to 03.
- [ ] Step 5: On 03 PASS / REVISE, show progress and decide whether commit approval is appropriate.

### Verification

- Mechanical progress check: product rubric weights sum to `100` and current score remains `80`.
- `git diff --check -- PLANS.md docs/PROJECT_COMPLETION_PROGRESS.md docs/PIPELINE_LIFECYCLE_MAP.md`
- Old workspace full-path scan on newly touched active section and control docs.
- 02 staging apply must verify before and after staging:
  - `git status --short --untracked-files=all`
  - exact candidate file existence/status
  - `git diff --check` on candidate files before staging
  - `git diff --cached --check` after staging
  - `git diff --cached --name-only` exactly matches the Option A file list

### Risks

- Accidentally staging files outside Option A.
- Forgetting that `PLANS.md`, progress, and lifecycle changed again after the dry-run and must be included in the staged set.
- Treating staged state as commit approval.
- Stale inventory counts if another handoff changes docs before staging.

### Drift Log

- 2026-06-27: `03` returned PASS for `minimal control-doc staging candidate dry run`. `01` kept official progress at 80% because the dry-run did not stage files or add product behavior.
- 2026-06-27: Lifecycle Stage 32 marked Done and Stage 33 added as Next: `Control-doc staging apply`. This authorizes only exact Option A staging in 02, not commit, push, cleanup, or implementation staging.
- 2026-06-27: Exact Option A staging apply completed and sent to `03`: pre-stage index was empty, `git add --` used only the 10 approved control docs, `git diff --cached --name-only` exactly matched the intended set, and `git diff --cached --check` passed.

## Completed Control-Doc Staging Candidate Dry Run Plan

### Goal

Create the next read-only docs/control slice after `minimal first staging candidate decision brief` reached `03` PASS: a control-doc staging candidate dry-run report that simulates the exact files and checks for Option A without running `git add`.

### Global Strategy

- Treat the accepted decision brief as a recommendation, not staging approval.
- Use Option A (`control docs only`) as the dry-run target because it was the lowest-risk recommended first staging candidate.
- Produce a dry-run report with exact files, current status, preflight checks, and blocking conditions before any real staging.
- Preserve `01 -> 02 -> 03 -> 01` handoff mode: 02 drafts the dry-run report, 03 reviews read-only, then 01 decides whether to approve actual staging in a separate explicit step.

### Non-Goals

- No `git add`, commit, push, branch change, release tag, cleanup apply, file delete, file move, revert, or generated package.
- No Kotlin/source/test/schema/runtime/CLI behavior edits.
- No image/video/audio generation, provider API/job, remote download, asset approval, approved reference, registered reference, user approval, canon, production lock, runtime scene creation, SQLite/runtime write, schema migration, server/port, QuarterFull/browser, or old OneDrive workspace edits.
- No `--db`, `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.
- No unattended scheduler / automation advancement.

### Confirmed Facts

- `03` passed `minimal first staging candidate decision brief` on 2026-06-27.
- The accepted brief recommends Option A (`control docs only`) as the lowest-risk first staging candidate if `01` wants to start staging.
- The accepted brief says explicit `01` approval is required before any actual staging.
- The accepted brief records inventory after the brief: tracked modified `7`, untracked total `122`, services `48`, tests `49`, docs `16`, samples `7`, memory entries `2`, other `0`, classified sum `122`.
- Current pre-report `git status --short --untracked-files=all` still reports tracked modified `7`, untracked total `122`, services `48`, tests `49`, docs `16`, samples `7`, memory entries `2`, other `0`, classified sum `122`.
- Official product progress remains `80%` because this PASS is staging decision preparation, not product behavior or staging execution.
- `docs/PIPELINE_LIFECYCLE_MAP.md` now marks Stage 31 done and Stage 32 next: `Control-doc staging candidate dry run`.

### Assumptions

- A dry-run exact-file report is safer than actual staging as the immediate next slice.
- Option A should remain limited to control docs and should not include mixed planning docs unless the dry-run explicitly identifies them and marks them as excluded or needing separate ownership.

### Open Questions

- Whether `01` will approve actual Option A staging after the dry-run passes `03`.
- Whether modified planning docs outside the control-doc candidate should remain dirty for later grouping.

### Implementation Steps

- [x] Step 1: Receive `03` PASS for first staging candidate decision brief.
- [x] Step 2: Update progress and lifecycle control docs without increasing percentage.
- [x] Step 3: Dispatch control-doc staging candidate dry run to 02.
- [x] Step 4: Add `docs/CONTROL_DOC_STAGING_CANDIDATE_DRY_RUN.md` with exact Option A file status, excluded surfaces, and pre-stage checks.
- [x] Step 5: Run docs-only verification and prepare the dry-run report for 03 read-only review.
- [ ] Step 6: On 03 PASS / REVISE, show progress and decide whether actual staging needs explicit approval.

### Verification

- Mechanical progress check: product rubric weights sum to `100` and current score remains `80`.
- `git diff --check -- PLANS.md docs/CONTROL_DOC_STAGING_CANDIDATE_DRY_RUN.md`
- New report trailing-whitespace scan.
- Old workspace full-path scan on the new report and active touched control docs, with `PLANS.md` historical relocation notes treated separately from current canonical workspace instructions.
- Exact inventory command/result and candidate file existence/status checks.
- Gradle intentionally skipped unless source/test files are changed accidentally; this slice is docs/control only.

### Risks

- Accidentally treating the dry-run as actual staged state.
- Accidentally including broad implementation files or planning-only docs in a control-doc candidate.
- Stale inventory counts if another handoff adds docs before the dry-run report.
- Failing to distinguish historical OneDrive path notes in `PLANS.md` from current canonical workspace references.

### Drift Log

- 2026-06-27: `03` returned PASS for `minimal first staging candidate decision brief`. `01` kept official progress at 80% because the brief is not actual staging approval or product behavior.
- 2026-06-27: Lifecycle Stage 31 marked Done and Stage 32 added as Next: `Control-doc staging candidate dry run`. This next slice remains read-only; no `git add` is approved.
- 2026-06-27: Evidence pass confirmed current pre-report inventory remains tracked modified `7`, untracked total `122`, services `48`, tests `49`, docs `16`, samples `7`, memory entries `2`, other `0`, classified sum `122`.
- 2026-06-27: Added `docs/CONTROL_DOC_STAGING_CANDIDATE_DRY_RUN.md` as a read-only dry-run report for Option A (`control docs only`). The report records candidate exact files, excluded dirty surfaces, required pre-stage checks, and states that explicit `01` approval is still required before any actual `git add`.
- 2026-06-27: Dry-run verification passed: post-report inventory is tracked modified `7`, untracked total `123`, services `48`, tests `49`, docs `17`, samples `7`, memory entries `2`, other `0`, classified sum `123`; `git diff --check` returned only the existing `PLANS.md` Windows line-ending warning; new report trailing-whitespace and legacy full-path scans were clean except for pre-existing historical notes in older `PLANS.md` sections.

## Completed Minimal First Staging Candidate Decision Brief Plan

### Goal

Create the next docs/control-only slice after `minimal adjacent storyboard suite reproducibility` reached `03` PASS: a first staging candidate decision brief that helps `01` choose a bounded staging strategy without performing staging.

### Global Strategy

- Keep this slice read-only planning / docs-control only: update `PLANS.md` and add `docs/FIRST_STAGING_CANDIDATE_DECISION_BRIEF.md`.
- Do not execute any actual `git add`, commit, push, cleanup, move, delete, revert, or release packaging.
- Use the accepted tracked handoff manifest, release grouping preview, focused reproducibility bundle, and adjacent storyboard suite report as evidence.
- Compare at least control-doc group, storyboard product-proof group, and a conservative no-stage-yet option.
- Preserve `01 -> 02 -> 03 -> 01` handoff mode: 02 drafts the brief, 03 reviews read-only, then 01 decides.

### Non-Goals

- No staging, commit, push, branch change, release tag, cleanup apply, file delete, file move, revert, or generated package.
- No Kotlin/source/test/schema/runtime/CLI behavior edits.
- No image/video/audio generation, provider API/job, remote download, asset approval, approved reference, registered reference, user approval, canon, production lock, runtime scene creation, SQLite/runtime write, schema migration, server/port, QuarterFull/browser, or old OneDrive workspace edits.
- No `--db`, `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.
- No unattended scheduler / automation advancement.

### Confirmed Facts

- `03` passed `minimal adjacent storyboard suite reproducibility` on 2026-06-27.
- The accepted report records nine sequential focused Gradle commands as BUILD SUCCESSFUL: six adjacent storyboard planning / packet / human-review tests plus three product-proof carry-forward tests.
- The accepted report records reproducible inventory after the report: tracked modified `7`, untracked total `121`, services `48`, tests `49`, docs `15`, samples `7`, memory entries `2`, other `0`, classified sum `121`.
- Official product progress is now `80%`.
- `docs/PIPELINE_LIFECYCLE_MAP.md` now marks Stage 30 done and Stage 31 next: `First staging candidate decision brief`.
- Current pre-report `git status --short --untracked-files=all` still reports tracked modified `7`, untracked total `121`, services `48`, tests `49`, docs `15`, samples `7`, memory entries `2`, other `0`, classified sum `121`.
- Read-only diff scope shows shared tracked files remain broad: `Main.kt` has accepted CLI wiring across local JSON gates, `Models.kt` has 216 added `data class` declarations, and modified planning docs remain mixed ownership.

### Assumptions

- The safest next slice is a decision brief rather than actual staging because shared tracked files (`Main.kt`, `Models.kt`, `PLANS.md`, planning docs) span many accepted slices.
- The decision brief should recommend options, risks, and verification gates, but must not execute any option.
- If `01` wants the lowest-risk first staging candidate, control docs are the easiest to isolate; if `01` wants product proof first, storyboard product-proof group needs explicit shared-file handling.

### Open Questions

- Which first staging candidate `01` will choose after the decision brief: control docs, storyboard product-proof group, a different bounded group, or no staging yet.
- Whether memory support entries should be included in any future staging group or remain local-only support.

### Implementation Steps

- [x] Step 1: Receive `03` PASS for adjacent storyboard suite reproducibility.
- [x] Step 2: Update product progress and lifecycle control docs.
- [x] Step 3: Read evidence: active plan, progress, lifecycle, tracked manifest, release grouping preview, focused bundle, adjacent storyboard report, current inventory, and shared tracked diff scope.
- [x] Step 4: Add `docs/FIRST_STAGING_CANDIDATE_DECISION_BRIEF.md`.
- [x] Step 5: Run docs-only verification and prepare package for `03` read-only review.

### Verification

- `git diff --check -- PLANS.md docs/FIRST_STAGING_CANDIDATE_DECISION_BRIEF.md`
- New report trailing-whitespace scan.
- Old workspace full-path scan on the new report and touched control docs.
- Exact inventory command/result with classification sum equal to untracked total.
- No Gradle rerun required because this slice is docs/control only unless source/test changes accidentally occur.

### Risks

- Accidentally treating a decision brief as approval to stage.
- Accidentally grouping shared tracked files with a single release group without `01` ownership decision.
- Stale inventory counts if another handoff adds docs before the decision brief.
- Over-expanding into release verification instead of staying at staging decision planning.
- Understating the risk of staging `Main.kt` / `Models.kt` without a chosen implementation group.

### Drift Log

- 2026-06-27: `03` returned PASS for `minimal adjacent storyboard suite reproducibility`. `01` counted the accepted adjacent storyboard suite evidence as product-proof progress and raised the official display from 79% to 80%.
- 2026-06-27: Lifecycle Stage 30 marked Done and Stage 31 added as Next: `First staging candidate decision brief`. This next slice remains read-only planning; no staging is approved.
- 2026-06-27: Evidence pass confirmed current pre-report inventory remains tracked modified `7`, untracked total `121`, services `48`, tests `49`, docs `15`, samples `7`, memory entries `2`, other `0`, classified sum `121`; shared tracked files remain broad and should not be staged without an explicit `01` option decision.
- 2026-06-27: Added `docs/FIRST_STAGING_CANDIDATE_DECISION_BRIEF.md` comparing control-doc-only, storyboard product-proof, no-staging-yet, and optional broader reproducibility options. The brief recommends control-doc-only as the first staging candidate if `01` wants to start staging, but says explicit `01` approval is required before any `git add`.
- 2026-06-27: Docs-only verification passed for the first staging candidate decision brief. Recomputed inventory after adding the brief: tracked modified `7`, untracked total `122`, services `48`, tests `49`, docs `16`, samples `7`, memory entries `2`, other `0`, classified sum `122`; `git diff --check` returned only the existing `PLANS.md` Windows line-ending warning.

## Completed Minimal Adjacent Storyboard Suite Reproducibility Plan

### Goal

Create a docs/control + command-evidence reproducibility report after `minimal tracked handoff manifest`: broaden verification from the three product-proof tests to adjacent storyboard planning / packet / human-review tests without editing source behavior.

### Global Strategy

- Keep this slice docs/control + test execution only: update `PLANS.md` and add `docs/ADJACENT_STORYBOARD_SUITE_REPRODUCIBILITY.md`.
- Treat the accepted tracked handoff manifest as a handoff map, not staging, commit, or release approval.
- Run adjacent storyboard tests sequentially to avoid Windows / Gradle lock issues, then carry forward the three product-proof tests.
- If any focused test fails, record the exact command/result and send it for review instead of fixing code.

### Non-Goals

- No cleanup apply step.
- No delete, move, stage, commit, push, revert, or generated package.
- No unattended scheduler / automation advancement.
- No image/video/audio generation, provider API/job, remote download, asset approval, approved reference, registered reference, user approval, canon, production lock, runtime scene creation, SQLite/runtime write, schema migration, server/port, QuarterFull/browser, or old OneDrive workspace edits.
- No `--db`, `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.
- No Kotlin/source/test/schema/runtime/CLI behavior edits.

### Confirmed Facts

- `03` passed `minimal tracked handoff manifest` on 2026-06-27.
- Official product progress is now `79%`.
- `docs/TRACKED_HANDOFF_MANIFEST.md` recommends `adjacent storyboard suite reproducibility` as the next slice before any staging decision.
- `docs/PIPELINE_LIFECYCLE_MAP.md` marks Stage 29 done and Stage 30 next: `Adjacent storyboard suite reproducibility`.
- Current pre-report `git status --short --untracked-files=all` reports tracked modified `7`, untracked total `120`, services `48`, tests `49`, docs `14`, samples `7`, memory entries `2`, other `0`, classified sum `120`.
- Current adjacent storyboard tests present in the repo are `StoryboardPlanningPreflightTest`, `StoryboardPlanningBriefTest`, `StoryboardPacketDraftValidationTest`, `StoryboardPacketReviewTest`, `StoryboardPacketHumanReviewPreflightTest`, and `StoryboardPacketHumanReviewDecisionIntakeTest`.
- Product-proof carry-forward tests present in the repo are `ReviewableStoryboardPacketTest`, `StoryboardStoryMovementAuditTest`, and `PipelineHappyPathFixtureTest`.

### Assumptions

- The next test surface should expand to adjacent storyboard tests such as `*StoryboardPacket*Test` and `*StoryboardPlanning*Test`, while retaining the product-proof tests if runtime is acceptable.
- Failures should be recorded and sent to `03` rather than fixed within this docs/control + command-evidence slice.

### Open Questions

- Whether the next slice after this should move to a first staging candidate decision brief or broaden into full `*Storyboard*Test` suite verification.

### Implementation Steps

- [x] Step 1: Receive `03` PASS for tracked handoff manifest.
- [x] Step 2: Update product progress and lifecycle control docs.
- [x] Step 3: Read evidence: control docs, tracked manifest, focused bundle, release grouping preview, current inventory, and actual storyboard test files.
- [x] Step 4: Run adjacent storyboard tests sequentially and carry forward product-proof tests.
- [x] Step 5: Add `docs/ADJACENT_STORYBOARD_SUITE_REPRODUCIBILITY.md`.
- [x] Step 6: Run docs-only verification and prepare package for `03` read-only review.

### Verification

- `./gradlew.bat test --tests "*StoryboardPlanningPreflightTest" --no-daemon --console=plain`
- `./gradlew.bat test --tests "*StoryboardPlanningBriefTest" --no-daemon --console=plain`
- `./gradlew.bat test --tests "*StoryboardPacketDraftValidationTest" --no-daemon --console=plain`
- `./gradlew.bat test --tests "*StoryboardPacketReviewTest" --no-daemon --console=plain`
- `./gradlew.bat test --tests "*StoryboardPacketHumanReviewPreflightTest" --no-daemon --console=plain`
- `./gradlew.bat test --tests "*StoryboardPacketHumanReviewDecisionIntakeTest" --no-daemon --console=plain`
- `./gradlew.bat test --tests "*ReviewableStoryboardPacketTest" --no-daemon --console=plain`
- `./gradlew.bat test --tests "*StoryboardStoryMovementAuditTest" --no-daemon --console=plain`
- `./gradlew.bat test --tests "*PipelineHappyPathFixtureTest" --no-daemon --console=plain`
- `git diff --check -- PLANS.md docs/ADJACENT_STORYBOARD_SUITE_REPRODUCIBILITY.md`
- New report trailing-whitespace scan.
- Old workspace full-path scan on the new report and touched control docs.
- Exact inventory command/result with classification sum equal to untracked total.

### Risks

- Accidentally treating broader storyboard test evidence as full release verification.
- Accidentally editing tests or source to make a suite pass.
- Running tests in parallel and hitting Windows Gradle output locks; prefer sequential commands.
- Stale inventory counts if another handoff adds docs before `03` review.

### Drift Log

- 2026-06-27: `03` returned PASS for `minimal tracked handoff manifest`. `01` updated official product progress from 78% to 79%, marked lifecycle Stage 29 done, and prepared the next slice as adjacent storyboard suite reproducibility.
- 2026-06-27: Evidence pass confirmed pre-report dirty-tree counts: tracked modified `7`, untracked total `120`, services `48`, tests `49`, docs `14`, samples `7`, memory entries `2`, other `0`, classified sum `120`.
- 2026-06-27: Evidence pass selected six adjacent storyboard tests from actual repo files plus three product-proof carry-forward tests from the focused reproducibility bundle.
- 2026-06-27: Ran all six adjacent storyboard tests and all three product-proof carry-forward tests sequentially; all nine returned BUILD SUCCESSFUL. Added `docs/ADJACENT_STORYBOARD_SUITE_REPRODUCIBILITY.md`; inventory after adding the report is tracked modified `7`, untracked total `121`, docs `15`, classified sum `121`.
- 2026-06-27: Docs-only verification passed for the adjacent storyboard suite reproducibility report. `git diff --check` returned only the existing `PLANS.md` Windows line-ending warning; new report trailing-whitespace scan was clean; new report old-workspace path scan was clean.

## Completed Minimal Tracked Handoff Manifest Plan

### Goal

Create the next verified docs/control slice after `minimal focused reproducibility bundle`: a tracked handoff manifest that lists exact files or path patterns required for reproducible local JSON product proof by release group, without staging or committing them.

### Global Strategy

- Keep this slice docs/control only: update `PLANS.md` and add `docs/TRACKED_HANDOFF_MANIFEST.md`.
- Treat the manifest as a handoff map, not staging approval, commit approval, release approval, or cleanup apply.
- Use the current `git status --short --untracked-files=all` inventory and `Main.kt` / `Models.kt` read-only diff scope as evidence.
- Preserve the `01 -> 02 -> 03 -> 01` loop: produce the report, verify it mechanically, then send it to `03` for read-only review.

### Non-Goals

- No cleanup apply step.
- No delete, move, stage, commit, push, revert, or generated package.
- No unattended scheduler / automation advancement.
- No image/video/audio generation, provider API/job, remote download, asset approval, approved reference, registered reference, user approval, canon, production lock, runtime scene creation, SQLite/runtime write, schema migration, server/port, QuarterFull/browser, or old OneDrive workspace edits.
- No `--db`, `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.
- No Kotlin/source/test/schema/runtime/CLI behavior edits.

### Confirmed Facts

- `03` passed `minimal focused reproducibility bundle` on 2026-06-27.
- Official product progress is now `78%`.
- `docs/FOCUSED_REPRODUCIBILITY_BUNDLE.md` records three core product-proof tests as BUILD SUCCESSFUL: `ReviewableStoryboardPacketTest`, `StoryboardStoryMovementAuditTest`, and `PipelineHappyPathFixtureTest`.
- `docs/PIPELINE_LIFECYCLE_MAP.md` marks Stage 28 done and Stage 29 next: `Tracked handoff manifest`.
- Current `git status --short --untracked-files=all` before this new report reports tracked modified `7`, untracked total `119`, services `48`, tests `49`, docs `13`, samples `7`, memory entries `2`, other `0`, classified sum `119`.
- `app/src/main/kotlin/com/localnovel/engine/Main.kt` is a shared tracked modified dependency with CLI wiring spanning novelization, script packet, storyboard, asset candidate, image candidate, candidate reference, and quality-support gates.
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt` is a shared tracked modified dependency with JSON report/model classes spanning the same accepted gate groups.

### Assumptions

- A tracked handoff manifest is the safest next step before any staging or commit because shared files such as `Main.kt` and `Models.kt` span many accepted slices.
- The manifest should map each release group to exact files / path patterns, required shared tracked files, verification commands, and blocking ownership decisions.

### Open Questions

- Whether `01` wants the next slice to be adjacent storyboard suite reproducibility or a first staging candidate decision brief after this manifest is reviewed.
- Whether `memory/` entries are intended release material or local-only support files.

### Implementation Steps

- [x] Step 1: Receive `03` PASS for focused reproducibility bundle.
- [x] Step 2: Update product progress and lifecycle control docs.
- [x] Step 3: Read evidence: control docs, focused reproducibility bundle, release grouping preview, dirty-tree inventory, and shared tracked diff scope.
- [x] Step 4: Add `docs/TRACKED_HANDOFF_MANIFEST.md`.
- [x] Step 5: Run docs-only verification and prepare package for `03` read-only review.

### Verification

- `git diff --check -- PLANS.md docs/TRACKED_HANDOFF_MANIFEST.md`
- New report trailing-whitespace scan.
- Old workspace full-path scan on the new report and touched control docs.
- Exact inventory command/result with classification sum equal to untracked total.

### Risks

- Accidentally treating a manifest as approval to stage or commit.
- Missing shared tracked files that are required for untracked service/test files to compile.
- Hiding unresolved ownership questions for planning-only docs or memory support files.
- Stale inventory counts if another handoff adds docs before `03` review.

### Drift Log

- 2026-06-27: `03` returned PASS for `minimal focused reproducibility bundle`. `01` updated official product progress from 76% to 78%, marked lifecycle Stage 28 done, and prepared the next slice as a tracked handoff manifest.
- 2026-06-27: Evidence pass confirmed current pre-report dirty-tree counts: tracked modified `7`, untracked total `119`, services `48`, tests `49`, docs `13`, samples `7`, memory entries `2`, other `0`, classified sum `119`. Read-only diff scope shows `Main.kt` and `Models.kt` are shared tracked dependencies across all gate groups.
- 2026-06-27: Added `docs/TRACKED_HANDOFF_MANIFEST.md` as a docs/control handoff map. The manifest treats `Main.kt`, `Models.kt`, `PLANS.md`, and modified planning docs as shared tracked surfaces that need explicit `01` approval before any staging strategy.
- 2026-06-27: Docs-only verification passed for the tracked handoff manifest. Recomputed inventory after adding the manifest: tracked modified `7`, untracked total `120`, services `48`, tests `49`, docs `14`, samples `7`, memory entries `2`, other `0`, classified sum `120`. `git diff --check` returned only the existing `PLANS.md` Windows line-ending warning.
- 2026-06-27: `03` returned PASS for `minimal tracked handoff manifest`. `01` counted it as release-hardening progress and raised the official display from 78% to 79%.

## Completed Minimal Focused Reproducibility Bundle Plan

### Goal

Create a docs/control + command-evidence focused reproducibility bundle after `minimal release grouping preview` reached `03` PASS and official product progress moved to `76%`.

### Global Strategy

- Keep this slice docs/control + test execution only: update `PLANS.md` and add `docs/FOCUSED_REPRODUCIBILITY_BUNDLE.md`.
- Run the three highest-value product-proof commands exactly: `ReviewableStoryboardPacketTest`, `StoryboardStoryMovementAuditTest`, and `PipelineHappyPathFixtureTest`.
- Record exact command results and inventory counts without editing source/test/runtime/schema/CLI behavior.
- If any focused test fails, record the failure and send it for review instead of fixing code.

### Non-Goals

- No cleanup apply step.
- No delete, move, stage, commit, push, revert, or generated package.
- No unattended scheduler / automation advancement.
- No image/video/audio generation, provider API/job, remote download, asset approval, approved reference, registered reference, user approval, canon, production lock, runtime scene creation, SQLite/runtime write, schema migration, server/port, QuarterFull/browser, or old OneDrive workspace edits.
- No `--db`, `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.
- No Kotlin/source/test/schema/runtime/CLI behavior edits.

### Confirmed Facts

- `03` passed `minimal release grouping preview` on 2026-06-27.
- Official product progress is now `76%`.
- `docs/RELEASE_GROUPING_PREVIEW.md` provides reproducible inventory counts and grouping strategy with no unexplained delta.
- `docs/PIPELINE_LIFECYCLE_MAP.md` marks Stage 27 done and Stage 28 next: `Focused reproducibility bundle`.
- Current `git status --short --untracked-files=all` after adding `docs/FOCUSED_REPRODUCIBILITY_BUNDLE.md` reports tracked modified `7`, untracked total `119`, services `48`, tests `49`, docs `13`, samples `7`, memory entries `2`, other `0`, classified sum `119`.

### Assumptions

- The highest-value next verification set should include `ReviewableStoryboardPacketTest`, `StoryboardStoryMovementAuditTest`, and `PipelineHappyPathFixtureTest`.
- Adjacent storyboard suite should remain future work unless `01` asks for broader runtime because this slice is the minimum focused bundle.

### Open Questions

- Whether the next slice should expand into adjacent storyboard suite verification or produce a tracked handoff manifest first.

### Implementation Steps

- [x] Step 1: Receive `03` PASS for release grouping preview.
- [x] Step 2: Update product progress and lifecycle control docs.
- [x] Step 3: Read evidence: control docs, release grouping preview, and current inventory.
- [x] Step 4: Run focused product-proof tests and exact inventory command.
- [x] Step 5: Add `docs/FOCUSED_REPRODUCIBILITY_BUNDLE.md`.
- [x] Step 6: Run docs-only verification and prepare package for `03` read-only review.

### Verification

- `./gradlew.bat test --tests "*ReviewableStoryboardPacketTest" --no-daemon --console=plain`
- `./gradlew.bat test --tests "*StoryboardStoryMovementAuditTest" --no-daemon --console=plain`
- `./gradlew.bat test --tests "*PipelineHappyPathFixtureTest" --no-daemon --console=plain`
- `git diff --check -- PLANS.md docs/FOCUSED_REPRODUCIBILITY_BUNDLE.md`
- New report trailing-whitespace scan.
- Old workspace full-path scan on the new report and touched control docs.

### Risks

- Accidentally treating a focused reproducibility report as full release verification.
- Accidentally hiding a failed focused test instead of reporting it as a blocker.
- Accidentally editing source/test files to make the bundle pass.
- Inventory can drift by +1 after the new report is added; the report must state the exact command/result used after creation.

### Drift Log

- 2026-06-27: `03` returned PASS for `minimal release grouping preview`. `01` updated official product progress from 74% to 76%, marked lifecycle Stage 27 done, and prepared the next slice as a focused reproducibility bundle.
- 2026-06-27: Evidence pass selected the three minimum product-proof commands from the release grouping preview's Group D and confirmed current inventory remains at tracked modified `7`, untracked total `118`, classified sum `118` before adding this new report.
- 2026-06-27: Ran the focused product-proof bundle: `ReviewableStoryboardPacketTest`, `StoryboardStoryMovementAuditTest`, and `PipelineHappyPathFixtureTest` all returned BUILD SUCCESSFUL. Added `docs/FOCUSED_REPRODUCIBILITY_BUNDLE.md`; inventory after adding the report is tracked modified `7`, untracked total `119`, docs `13`, classified sum `119`.
- 2026-06-27: Docs-only verification passed for the focused reproducibility bundle. Recomputed inventory with the correct `^\?\? ` untracked-line mouth: tracked modified `7`, untracked total `119`, services `48`, tests `49`, docs `13`, samples `7`, memory entries `2`, other `0`, classified sum `119`.
- 2026-06-27: `03` returned PASS for `minimal focused reproducibility bundle`. `01` counted the focused product-proof test evidence as release-hardening progress and raised the official display from 76% to 78%.

## Completed Minimal Release Grouping Preview Plan

### Goal

Create a read-only release grouping preview after the release hygiene inventory reached `03` PASS and official product progress moved to `74%`.

### Global Strategy

- Keep this slice docs/control only: update `PLANS.md` and add `docs/RELEASE_GROUPING_PREVIEW.md`.
- Treat grouping as a preview for later `01` decisions, not permission to stage, commit, delete, move, revert, push, or clean files.
- Use `git status --short --untracked-files=all` as the single inventory mouth for reproducible counts.
- Separate product-gate implementation groups from planning/historical docs and local memory support files.

### Non-Goals

- No cleanup apply step.
- No delete, move, stage, commit, push, revert, or generated package.
- No unattended scheduler / automation advancement.
- No image/video/audio generation, provider API/job, remote download, asset approval, approved reference, registered reference, user approval, canon, production lock, runtime scene creation, SQLite/runtime write, schema migration, server/port, QuarterFull/browser, or old OneDrive workspace edits.
- No `--db`, `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.
- No Kotlin/source/test/schema/runtime/CLI behavior edits.

### Confirmed Facts

- `03` passed `minimal release hygiene and local reproducibility report` REVISE P1 on 2026-06-27.
- Official product progress is now `74%`.
- `docs/RELEASE_HYGIENE_AND_REPRODUCIBILITY_REPORT.md` uses `git status --short --untracked-files=all` and has reproducible dirty-tree counts: tracked modified `7`, untracked total `117`, services `48`, tests `49`, docs `11`, samples `7`, memory entries `2`, other `0`.
- `docs/PIPELINE_LIFECYCLE_MAP.md` marks Stage 26 in progress and Stage 27 next: `Release grouping preview`.
- Current inventory command now reports tracked modified `7`, untracked total `118`, services `48`, tests `49`, docs `12`, samples `7`, memory entries `2`, other `0`, classified sum `118`; the extra docs entry is this new release grouping preview report.
- Service/test grouping counts from current status: novelization `8/8`, script `4/4`, storyboard `8/9`, asset `6/6`, image `13/13`, candidate-reference `7/7`, quality-support `2/2`.

### Assumptions

- A read-only grouping preview is the safest next step before any staging, commit, cleanup, or broad reproducibility bundle.
- Grouping should separate product pipeline code/tests, docs/control reports, samples/memory fixtures, and unrelated/historical docs if present.

### Open Questions

- `01` still needs to decide which groups become commit-sized packages and whether any planning-only docs require expert-model review before staging.

### Implementation Steps

- [x] Step 1: Receive `03` PASS for release hygiene report REVISE P1.
- [x] Step 2: Update product progress and lifecycle control docs.
- [x] Step 3: Read evidence: control docs, release hygiene report, and current dirty-tree inventory.
- [x] Step 4: Add `docs/RELEASE_GROUPING_PREVIEW.md`.
- [x] Step 5: Run docs-only verification and exact inventory command.
- [x] Step 6: Send release grouping preview package to `03` for read-only review.

### Verification

- `git diff --check -- PLANS.md docs/RELEASE_GROUPING_PREVIEW.md`
- New report trailing-whitespace scan.
- Old workspace full-path scan on the new report and touched control docs.
- Exact inventory command/result: `git status --short --untracked-files=all`.

### Risks

- Accidentally turning a grouping preview into an actual cleanup/stage/commit.
- Grouping unreviewed or stale files together with accepted pipeline artifacts.
- Letting stale inventory counts reappear.
- Treating grouping as release approval, production readiness, or cleanup completion.

### Drift Log

- 2026-06-27: `03` returned PASS for `minimal release hygiene and local reproducibility report` REVISE P1. `01` updated official product progress from 72% to 74%, marked lifecycle Stage 26 in progress, and prepared the next slice as a read-only release grouping preview.
- 2026-06-27: Evidence pass confirmed the accepted release hygiene report's `117` untracked count drifted to `118` after this new preview report was added; current counts remain fully classified without delta using `git status --short --untracked-files=all`.
- 2026-06-27: Added `docs/RELEASE_GROUPING_PREVIEW.md` with read-only commit/handoff-sized groups and verification results. No cleanup, staging, commit, revert, runtime, DB, CLI, provider, or media work was performed.
- 2026-06-27: Prepared the release grouping preview package for `03` read-only review. The recommended next slice is a focused reproducibility bundle or tracked handoff manifest, not cleanup apply.
- 2026-06-27: `03` returned PASS for `minimal release grouping preview`. `01` counted the grouping preview as release-hardening progress and raised the official display from 74% to 76%.

## Completed Minimal Release Hygiene And Local Reproducibility Report Plan

### Goal

Create a docs/control-only release hygiene and local reproducibility report after `minimal reviewable storyboard packet product artifact` reached `03` PASS and the official product progress display moved to `72%`.

### Global Strategy

- Keep this slice docs/control only: update `PLANS.md` and add one release hygiene / reproducibility report.
- Treat `reviewable_storyboard_packet_ready` as a local product artifact marker, not approval, canon, provider readiness, image generation, or production status.
- Inventory the current dirty worktree without deleting, moving, staging, committing, reverting, or cleaning files.
- Provide reproducible local verification commands and stale-path checks for the next release-hardening slice.

### Non-Goals

- No unattended scheduler / automation advancement.
- No image/video/audio generation, provider API/job, remote download, asset approval, approved reference, registered reference, user approval, canon, production lock, runtime scene creation, SQLite/runtime write, schema migration, server/port, QuarterFull/browser, or old OneDrive workspace edits.
- No `--db`, `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.
- No Kotlin/source/test/schema/runtime/CLI behavior edits.

### Confirmed Facts

- `03` passed `minimal reviewable storyboard packet product artifact` on 2026-06-27.
- Official product progress is now `72%`.
- `docs/PIPELINE_LIFECYCLE_MAP.md` marks Stage 25 done and Stage 26 next: `Release hygiene and local reproducibility hardening`.
- Current worktree remains dirty with many reviewed but untracked service/test files from prior slices.
- `Get-Location` executed with explicit workdir confirmed `C:\Users\f7212\Projects\AI 劇本`.
- `git status --short --untracked-files=all` currently reports 7 tracked modified paths and 117 untracked entries; the untracked entries classify as 48 service files, 49 test files, 11 docs entries, 7 sample entries, 2 memory entries, and 0 other entries.
- Existing `docs/CLEANUP_INVENTORY_REPORT.md` is dated 2026-06-25 and records cleanup inventory before the current 72% product artifact state.

### Assumptions

- A new report file is safer than rewriting the older cleanup inventory because this slice is about current release hygiene and reproducibility, not media/tool cleanup.
- A focused rerun of `ReviewableStoryboardPacketTest` is useful evidence even though the slice is docs-only.

### Open Questions

- Whether the next slice should formalize a broader release candidate checklist or first reduce the dirty worktree by reviewed package groups.

### Implementation Steps

- [x] Step 1: Receive `03` PASS for reviewable storyboard packet product artifact.
- [x] Step 2: Update product progress and lifecycle control docs.
- [x] Step 3: Read evidence: control docs, git status, and existing cleanup inventory.
- [x] Step 4: Add `docs/RELEASE_HYGIENE_AND_REPRODUCIBILITY_REPORT.md`.
- [x] Step 5: Run docs-only verification and optional focused product-artifact reproducibility test.
- [x] Step 6: Send report package to `03` for read-only review.
- [x] Step 7: Receive `03` REVISE P1 on dirty tree inventory reproducibility.
- [x] Step 8: Recompute inventory with `git status --short --untracked-files=all` and repair report counts / verification record.
- [x] Step 9: Send REVISE P1 repair package to `03` for read-only review.

### Verification

- `git diff --check -- PLANS.md docs/RELEASE_HYGIENE_AND_REPRODUCIBILITY_REPORT.md`
- Report file trailing-whitespace scan.
- Old workspace full-path scan on the new report.
- Optional focused check: `./gradlew.bat test --tests "*ReviewableStoryboardPacketTest" --no-daemon --console=plain`.

### Risks

- Accidentally treating release hygiene as permission to write SQLite, clean/revert unrelated user work, or formalize unreviewed artifacts.
- Accidentally starting unattended automation instead of keeping `01` as the controlling handoff window.
- Accidentally expanding into image/provider/media generation instead of reproducibility evidence.
- Existing historical `PLANS.md` and cleanup docs include old OneDrive/standalone-tool references; this slice must not introduce a stale workspace path in the new report.

### Drift Log

- 2026-06-27: `03` returned PASS for `minimal reviewable storyboard packet product artifact`. `01` updated official product progress from 67% to 72%, marked lifecycle Stage 25 done, and prepared the next slice as release hygiene and local reproducibility hardening.
- 2026-06-27: Evidence pass found an existing 2026-06-25 cleanup report, a dirty worktree with reviewed untracked service/test/docs artifacts, and no need to touch Kotlin/runtime for this slice.
- 2026-06-27: Added `docs/RELEASE_HYGIENE_AND_REPRODUCIBILITY_REPORT.md`; verification passed with `git diff --check` over `PLANS.md` and the report, report trailing-whitespace scan, legacy full-path scan caveat, and `./gradlew.bat test --tests "*ReviewableStoryboardPacketTest" --no-daemon --console=plain`.
- 2026-06-27: Prepared the report package for `03` read-only review. This slice remains docs/control only and does not clean, stage, commit, revert, or modify runtime behavior.
- 2026-06-27: REVISE P1 from `03` found stale dirty-tree inventory counts. Recomputed with `git status --short --untracked-files=all` and repaired the report to show tracked modified `7`, untracked total `117`, services `48`, tests `49`, docs `11`, samples `7`, memory entries `2`, other `0`, classified sum `117`.
- 2026-06-27: `03` returned REVISE P1: dirty tree inventory totals and category counts are stale / not reproducible. Official project progress remains 72%. Next action is a narrow report repair: rerun explicit inventory commands, state the exact command mouth, fix totals/categories/delta, and update verification record.
- 2026-06-27: `03` returned PASS for the REVISE P1 repair. `01` counted the reproducible inventory report as release-hygiene progress and raised the official display from 72% to 74%.

## Completed Minimal Reviewable Storyboard Packet Product Artifact Implementation Plan

### Goal

Add `create-reviewable-storyboard-packet`, a named local JSON product artifact that packages an audited storyboard draft/review/preflight chain into a human-reviewable storyboard packet skeleton.

### Global Strategy

- Keep the artifact Kotlin/JVM CLI + JSON files/tests only.
- Read `StoryboardPacketDraft`, `StoryboardPacketReviewPacket`, `StoryboardPacketHumanReviewPreflightReport`, and `StoryboardStoryMovementAuditReport`.
- Treat `story_movement_audited` as upstream readiness evidence, not approval or production status.
- PASS emits only `reviewable_storyboard_packet_ready`; FAIL emits no downstream step and clears frame summaries to avoid misuse.

### Non-Goals

- No rewrite, generation, image/video/audio output, provider handoff/API/job, remote download, asset approval, approved reference, registered reference, user approval, canon, production lock, runtime scene creation, SQLite/runtime write, schema migration, server/port, QuarterFull/browser, or old OneDrive workspace edits.
- No `--db`, `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ReviewableStoryboardPacketService.kt`
- `app/src/test/kotlin/com/localnovel/engine/ReviewableStoryboardPacketTest.kt`

### Confirmed Facts

- `03` passed the story movement audit REVISE P1; official progress is `67%`.
- Lifecycle Stage 25 is `Reviewable storyboard packet product artifact`.
- Existing audit PASS output is `nextAllowedDownstreamStep=story_movement_audited`.
- `StoryboardStoryMovementAuditReport` carries source files/ids, movement evidence counts, continuity coverage, and forbidden-claim count.
- `StoryboardPacketDraft` still carries full frame summaries and output/provider/approval fields that must be guarded again during product packaging.

### Assumptions

- A first-version product artifact can safely include frame summaries derived from text-only draft frame fields plus audit/review/preflight evidence summaries.
- `reviewable_storyboard_packet_ready` is a local product handoff marker and does not imply approval, canon, production readiness, media output, or provider readiness.

### Open Questions

- Future product hardening may define a richer human review UI/export format after this JSON artifact is accepted.

### Implementation Steps

- [x] Step 1: Confirm canonical workspace and read `strata-agent-loop`.
- [x] Step 2: Read control docs, audit service/test, and storyboard models/services/tests.
- [x] Step 3: Add reviewable storyboard packet models.
- [x] Step 4: Implement read-only packet service and CLI.
- [x] Step 5: Add focused tests for happy path, upstream/source/frame/movement/continuity/output-field/forbidden-claim failures, and CLI smoke.
- [x] Step 6: Run focused tests, diff check, trailing-whitespace scan, and safety scan.
- [x] Step 7: Send package to `03` for read-only review.

### Verification

- `./gradlew.bat test --tests "*ReviewableStoryboardPacketTest" --no-daemon --console=plain`
- If the audit fixture is changed, also run `./gradlew.bat test --tests "*StoryboardStoryMovementAuditTest" --no-daemon --console=plain`.
- `git diff --check` over touched files.
- New-file trailing-whitespace scan.
- Safety scan for DB/server/API/runtime/provider/media/approval/canon/production-lock drift; forbidden-input test fixtures are allowed only as negative assertions.

### Risks

- The artifact must not be mistaken for approval or final storyboard media.
- The packet reads full draft JSON, so it must repeat output/provider/approval field guards instead of trusting only prior reports.

### Drift Log

- 2026-06-27: Started minimal reviewable storyboard packet artifact after `03` passed the story movement audit REVISE P1. Evidence shows the safe input surface is draft + review + human review preflight + story movement audit report.
- 2026-06-27: Added `ReviewableStoryboardPacket` models, `ReviewableStoryboardPacketService`, `create-reviewable-storyboard-packet` CLI, and focused tests. PASS output is still a local candidate handoff marker only: `reviewable_storyboard_packet_ready`.
- 2026-06-27: Verification passed: `./gradlew.bat test --tests "*ReviewableStoryboardPacketTest" --no-daemon --console=plain`; `git diff --check` over touched tracked files returned only Windows line-ending warnings; new service/test trailing-whitespace scan was clean; targeted safety scan found no DB/server/API/runtime boundary calls.
- 2026-06-27: `03` returned PASS for `minimal reviewable storyboard packet product artifact`; no blocking findings remain.

## Completed Reviewable Storyboard Packet Product Artifact Handoff Plan

### Goal

Dispatch the next verified slice after `minimal storyboard story movement audit gate`: a named local JSON reviewable storyboard packet product artifact that packages the audited storyboard draft/review/preflight chain for human review.

### Global Strategy

- Keep `01` in control-loop mode and continue the `01 -> 02 -> 03 -> 01` handoff.
- Treat `story_movement_audited` as readiness evidence, not approval, canon, provider readiness, image generation, or production status.
- Update only control docs in `01`; implementation goes to `02` and read-only review goes to `03`.
- Keep the next slice Kotlin/JVM CLI + JSON files/tests only.

### Non-Goals

- No image/video/audio generation, provider API/job, remote download, asset approval, approved reference, registered reference, user approval, canon, production lock, runtime scene creation, SQLite/runtime write, schema migration, server/port, QuarterFull/browser, or old OneDrive workspace edits.
- No `--db`, `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Confirmed Facts

- `03` passed `minimal storyboard story movement audit gate` REVISE P1 on 2026-06-27.
- Official product progress is now `67%`.
- `docs/PIPELINE_LIFECYCLE_MAP.md` marks Stage 24 done and Stage 25 next: `Reviewable storyboard packet product artifact`.
- The next artifact must package audited evidence without changing the storyboard or claiming approval/production readiness.

### Assumptions

- The smallest next product artifact can read `StoryboardStoryMovementAuditReport` plus the source draft/review/preflight artifacts and emit a reviewable packet skeleton.
- PASS should only allow a local downstream marker such as `reviewable_storyboard_packet_ready`, not image/provider/canon/approval work.

### Open Questions

- `02` should confirm the exact model names and source surface from existing report fields before implementation.

### Implementation Steps

- [x] Step 1: Receive `03` PASS for story movement audit REVISE P1.
- [x] Step 2: Update product progress and lifecycle control docs.
- [x] Step 3: Dispatch reviewable storyboard packet product artifact slice to `02`.
- [x] Step 4: Wait for `02` implementation and `03` read-only review.

### Verification

- `git diff --check -- PLANS.md docs/PROJECT_COMPLETION_PROGRESS.md docs/PIPELINE_LIFECYCLE_MAP.md`
- Mechanical progress check: product rubric scores must add to `72`.
- Old OneDrive full path scan on the two control docs should stay clean.

### Risks

- Accidentally treating a reviewable packet as human approval, production lock, image-ready output, provider-ready output, canon, or runtime scene creation.
- Packaging source-truncated evidence instead of preserving the audited draft/review/preflight lineage.

### Drift Log

- 2026-06-27: `03` returned PASS for `minimal storyboard story movement audit gate` REVISE P1. `01` updated official product progress from 63% to 67%, marked lifecycle Stage 24 done, and prepared the next slice as a named reviewable storyboard packet product artifact.
- 2026-06-27: `03` returned PASS for `minimal reviewable storyboard packet product artifact`. `01` updated official product progress from 67% to 72%, marked lifecycle Stage 25 done, and prepared the next slice as release hygiene and local reproducibility hardening.

## Completed Minimal Storyboard Story Movement Audit Gate Plan

### Goal

Add a read-only `audit-storyboard-story-movement` gate that checks whether a human-reviewable storyboard packet skeleton has minimum deterministic story movement evidence, beyond JSON validity.

### Global Strategy

- Keep the gate Kotlin/JVM CLI + JSON files only.
- Use `StoryboardPacketDraft` for frame/beat text because review and preflight reports only carry frame ids and source summaries.
- Use `StoryboardPacketReviewPacket` plus `StoryboardPacketHumanReviewPreflightReport` to prove the draft has reached the existing human-reviewable endpoint `storyboard_packet_human_review_ready`.
- Audit only local readiness; PASS emits `story_movement_audited`, not approval, canon, provider readiness, or production status.

### Non-Goals

- No rewrite, generation, image/video/audio output, provider API/job, remote download, asset approval, approved reference, registered reference, user approval, canon, production lock, runtime scene creation, SQLite/runtime write, schema migration, server/port, or QuarterFull/browser work.
- No `--db`, `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.
- Do not edit the old OneDrive workspace. Canonical workspace is `C:\Users\f7212\Projects\AI 劇本`.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/StoryboardStoryMovementAuditService.kt`
- `app/src/test/kotlin/com/localnovel/engine/StoryboardStoryMovementAuditTest.kt`

### Confirmed Facts

- `03` passed `pipeline E2E happy-path fixture v1`; official product progress is now `63%`.
- The current human-reviewable storyboard endpoint is `StoryboardPacketHumanReviewPreflightReport.nextAllowedDownstreamStep=storyboard_packet_human_review_ready`.
- `StoryboardPacketReviewPacket` carries frame count, frame ids, continuity summary, source report lineage, residual risks, and review checklist, but not full frame visual/action text.
- `StoryboardPacketDraft` carries frame-level scene reference, beat reference, visual intent, camera intent, must-show, do-not-show, continuity anchors, asset candidate refs, review notes, and output/provider fields.
- `03` returned REVISE P1: full draft input can carry short output/provider/approval field values that do not reliably match wording regex.

### Assumptions

- First-version story movement can be deterministic by requiring source-bound frame content plus simple movement markers for pressure/action/consequence-or-choice evidence.
- `story_movement_audited` is a safe local readiness marker and does not imply approval or production readiness.

### Open Questions

- Later product work may need richer story movement criteria after real chapter fixtures reveal false positives or false negatives.

### Implementation Steps

- [x] Step 1: Confirm canonical workspace and read `strata-agent-loop`.
- [x] Step 2: Read current plans, progress rubric, lifecycle map, E2E fixture, and storyboard model/service/test evidence.
- [x] Step 3: Add story movement audit report models.
- [x] Step 4: Implement read-only audit service and CLI.
- [x] Step 5: Add focused tests for happy path, upstream/source/frame/static/continuity/forbidden failures, and CLI smoke.
- [x] Step 6: Run focused tests, diff check, trailing-whitespace scan, and safety scan.
- [x] Step 7: Send package to `03` for read-only review.
- [x] Step 8: REVISE P1: add field-level guards for frame image/video output paths, generation API, and asset approval status.
- [x] Step 9: Add short-value regression tests and rerun focused verification.
- [x] Step 10: Send the REVISE P1 repair package to `03` for read-only review.

### Verification

- `./gradlew.bat test --tests "*StoryboardStoryMovementAuditTest" --no-daemon --console=plain`
- `git diff --check` over touched files.
- New-file trailing-whitespace scan.
- Safety scan for DB/server/API/runtime/provider/media/approval/canon/production-lock drift; forbidden-input test fixtures are allowed only as negative assertions.

### Risks

- Heuristic movement markers are intentionally conservative and deterministic; they are not a substitute for literary quality review.
- The audit needs the draft JSON in addition to review/preflight reports because otherwise frame content is source-truncated.

### Drift Log

- 2026-06-27: Started minimal storyboard story movement audit after `03` passed E2E fixture v1. Evidence showed the minimal safe audit surface is storyboard draft + review packet + human review preflight report.
- 2026-06-27: Added `audit-storyboard-story-movement`, report models, read-only audit service, and focused tests. Verification passed: `./gradlew.bat test --tests "*StoryboardStoryMovementAuditTest" --no-daemon --console=plain`; `git diff --check` over touched files reported only Windows line-ending warnings; safety scan hits were limited to forbidden-pattern detectors and negative test fixtures.
- 2026-06-27: `03` returned REVISE P1. Required repair is narrow: add deterministic field-level failures for nonblank `imageOutputPath`, nonblank `videoOutputPath`, nonblank `generationApi`, and non-candidate `assetApprovalStatus` in storyboard draft frames.
- 2026-06-27: Repaired REVISE P1 by adding field-level guards and short-value regression tests for `frame-001.png`, `shot-001`, `manual-provider`, and `approved`. Focused tests passed; diff check reported only Windows line-ending warnings; safety scan hits were limited to guard text and negative test fixtures.
- 2026-06-27: `03` returned PASS for the REVISE P1 repair. No blocking findings remain for the story movement audit gate.

## Completed Story Movement Audit Handoff Plan

### Goal

Dispatch the next verified slice after `pipeline E2E happy-path fixture v1`: a read-only story movement audit for storyboard readiness.

### Global Strategy

- Keep `01` in control-loop mode and continue the `01 -> 02 -> 03 -> 01` handoff.
- Treat `03` PASS for the E2E fixture as product-proof progress, not final storyboard delivery.
- Update only control docs in `01` before dispatching implementation to `02`.
- Keep the next slice read-only and local JSON only: no runtime write, no SQLite, no provider/image/video generation, no approval/canon/lock semantics.

### Non-Goals

- No Kotlin implementation in `01` for the next audit slice.
- No formal registry write, asset approval, approved reference, registered reference, canon, runtime scene creation, provider job, remote download, image generation, video generation, or production lock.
- No OneDrive workspace edits; canonical workspace remains `C:\Users\f7212\Projects\AI 劇本`.

### Confirmed Facts

- `03` passed `pipeline E2E happy-path fixture v1` on 2026-06-27.
- The accepted E2E fixture reaches `StoryboardPacketHumanReviewPreflightReport` with `nextAllowedDownstreamStep=storyboard_packet_human_review_ready`.
- The E2E fixture is test/product-proof only and does not add source service/model/CLI behavior.
- `docs/PROJECT_COMPLETION_PROGRESS.md` now displays `63%` after counting this product-proof progress conservatively.
- `docs/PIPELINE_LIFECYCLE_MAP.md` now marks Stage 23 done and sets story movement audit as the next slice.

### Assumptions

- A read-only story movement audit is the smallest next product-quality slice before naming a final reviewable storyboard product artifact.

### Open Questions

- The exact story movement criteria should be confirmed from existing storyboard fields and kept deterministic in `02`'s evidence pass.

### Implementation Steps

- [x] Step 1: Receive `03` PASS for E2E happy-path fixture v1.
- [x] Step 2: Update product progress and lifecycle control docs.
- [x] Step 3: Dispatch read-only story movement audit slice to `02`.
- [x] Step 4: Wait for `02` implementation and `03` read-only review.

### Verification

- `git diff --check -- PLANS.md docs/PROJECT_COMPLETION_PROGRESS.md docs/PIPELINE_LIFECYCLE_MAP.md`
- Mechanical progress check: product rubric scores must add to `67`.
- Old OneDrive full path scan on the two control docs should stay clean.

### Risks

- Inflating progress by treating E2E v1 as final storyboard delivery instead of product proof.
- Defining story movement audit too broadly and accidentally requiring generation, rewriting, or author approval.

### Drift Log

- 2026-06-27: `03` returned PASS for `pipeline E2E happy-path fixture v1`. `01` updated official product progress from 59% to 63%, marked lifecycle Stage 23 done, and prepared the next slice as a read-only story movement audit.
- 2026-06-27: `03` returned PASS for the story movement audit REVISE P1 repair. `01` updated official product progress from 63% to 67% and marked lifecycle Stage 24 done.

## Completed Pipeline E2E Happy-Path Fixture V1 Plan

### Goal

Add a deterministic E2E happy-path fixture test proving that a coherent local JSON artifact chain can start at the accepted script packet boundary and reach an existing human-reviewable storyboard endpoint.

### Global Strategy

- Keep this slice test/product-proof only.
- Use existing services and models; do not invent a formal production artifact.
- Start at `AcceptedScriptPacketHandoffPacket`, because the lifecycle map allows accepted draft or accepted script packet boundary for v1.
- Use existing endpoint `StoryboardPacketHumanReviewPreflightReport` with `nextAllowedDownstreamStep=storyboard_packet_human_review_ready` as the v1 human-reviewable storyboard endpoint.
- Verify source binding continuity across ids, paths, hashes, protected continuity anchors, candidate-only boundaries, and no generated/provider/approval/production-lock claims.

### Non-Goals

- No SQLite/runtime write, schema migration, `--db`, formal registry write, asset approval, approved reference, registered reference, production lock, canon, provider API/job, remote download, image/video/audio generation, server/port, web UI, QuarterFull/browser work, or runtime scene creation.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.
- Do not edit the old OneDrive workspace. Canonical workspace is `C:\Users\f7212\Projects\AI 劇本`.

### Scope

- `PLANS.md`
- `app/src/test/kotlin/com/localnovel/engine/PipelineHappyPathFixtureTest.kt`

### Confirmed Facts

- `03` passed `candidate_reference_acceptance_packet_validation`; official product progress is now `59%`.
- `docs/PIPELINE_LIFECYCLE_MAP.md` marks Stage 23 E2E happy-path fixture as Next.
- Existing service chain can run accepted script packet handoff -> storyboard planning preflight -> storyboard planning brief -> storyboard packet draft validation -> storyboard packet review -> storyboard packet human review preflight.
- `StoryboardPacketHumanReviewPreflightService` emits `nextAllowedDownstreamStep=storyboard_packet_human_review_ready` on PASS and performs source, frame, checklist, continuity, residual risk, and forbidden-claim checks.

### Assumptions

- V1 endpoint should be `storyboard_packet_human_review_ready`, not a nonexistent final reviewable storyboard product model.
- Temporary-directory fixture JSON is sufficient product proof for v1 because it exercises real services with deterministic local artifacts.

### Open Questions

- A future slice may still need a named `reviewable_storyboard_packet` artifact after story movement audit defines product-level readiness.

### Implementation Steps

- [x] Step 1: Confirm canonical workspace with explicit `Get-Location`.
- [x] Step 2: Read active plan, progress rubric, lifecycle map, and existing storyboard pipeline tests/services.
- [x] Step 3: Add focused E2E happy-path fixture test.
- [x] Step 4: Run focused test, diff check, whitespace scan, and safety scan.
- [x] Step 5: Send package to `03` for read-only review.

### Verification

- `./gradlew.bat test --tests "*PipelineHappyPathFixtureTest" --no-daemon --console=plain`
- `git diff --check -- PLANS.md app/src/test/kotlin/com/localnovel/engine/PipelineHappyPathFixtureTest.kt`
- Trailing whitespace scan for the new test file.
- Safety scan for DB/server/API/runtime/provider/image/video/audio/approval/canon/production lock boundary drift.

### Risks

- This proves a coherent JSON fixture to a human-reviewable storyboard state, not story movement quality or visual asset production.
- It intentionally does not cover the full image candidate / reference candidate chain because the requested endpoint is reviewable storyboard packet skeleton v1.

### Drift Log

- 2026-06-27: Started E2E happy-path fixture v1 after `03` passed candidate reference acceptance packet validation and `01` updated progress to 59%. Evidence indicates the existing human-reviewable storyboard endpoint is `storyboard_packet_human_review_ready`.
- 2026-06-27: Added `PipelineHappyPathFixtureTest`, starting at `AcceptedScriptPacketHandoffPacket` and exercising the real storyboard planning preflight, planning brief, storyboard draft validation, storyboard review, and human review preflight services. The v1 endpoint is `storyboard_packet_human_review_ready`; it is product proof only and does not create final storyboard media or production assets.
- 2026-06-27: Verification completed: focused `PipelineHappyPathFixtureTest` passed with `--no-daemon --console=plain`; `git diff --check` over touched files reported only the existing PLANS Windows line-ending warning; new test trailing-whitespace scan was clean; safety scan hits in the test were limited to forbidden-boundary assertion strings.
- 2026-06-27: `03` returned PASS for `pipeline E2E happy-path fixture v1`; `01` counted it as conservative product-proof progress, not final storyboard delivery, and raised the official display from 59% to 63%.

## Active Candidate Reference Acceptance Packet Validation Plan

### Goal

Add `candidate_reference_acceptance_packet_validation` as a boundary-closure gate for local candidate-only acceptance packets. The gate validates that the packet remains non-canon, non-production, non-approved, source-bound, and safe to mark as locally validated without writing any formal registry/runtime state.

### Global Strategy

- Use only Kotlin/JVM CLI + JSON file inputs/outputs.
- Read only `--candidate-reference-acceptance-packet`; optionally write a validation report with `--out`.
- Keep PASS output at `nextAllowedDownstreamStep=candidate_reference_acceptance_packet_validated`.
- Add deterministic source, hash, human-source, item-identity, remote/provider, and forbidden-status checks.
- Preserve the product progress display at `58%` until `03` reviews this slice and `01` applies the rubric.

### Non-Goals

- No formal registry write, SQLite/runtime write, schema migration, asset approval, approved reference, registered reference, asset lock, production lock, canon, provider API/job, remote download, image/video/audio generation, server/port, web UI, or QuarterFull/browser work.
- No changes to `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion`.
- Do not edit the old OneDrive workspace. Canonical workspace is `C:\Users\f7212\Projects\AI 劇本`.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/CandidateReferenceAcceptancePacketService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/CandidateReferenceAcceptancePacketValidationService.kt`
- `app/src/test/kotlin/com/localnovel/engine/CandidateReferenceAcceptancePacketValidationTest.kt`

### Confirmed Facts

- `03` passed the lifecycle map + progress rubric repair; official displayed progress is `58%`.
- `CandidateReferenceAcceptancePacketService` currently creates local candidate-only packets with `nextAllowedDownstreamStep=candidate_reference_acceptance_packet_validation`.
- `ImageCandidateExternalResultItem` includes `sourceFileSha256`, so the validation gate can include it in item-level identity checks.
- The previous acceptance packet carries upstream verdict evidence but did not yet carry human decision source or a deterministic image-result identity digest for closure validation.

### Implementation Steps

- [x] Step 1: Confirm canonical workspace with explicit `Get-Location`.
- [x] Step 2: Read active plan, progress rubric, lifecycle map, and existing acceptance packet service/model/test/CLI evidence.
- [x] Step 3: Add validation report models and source-closure metadata.
- [x] Step 4: Implement the read-only validation service and CLI.
- [x] Step 5: Add focused tests for PASS, upstream/state/hash/source/item/security failures, and CLI smoke.
- [x] Step 6: Run focused tests, `git diff --check`, trailing-whitespace scan for new files, and a safety keyword scan.
- [x] Step 7: Send the implementation package to `03` for read-only review.

### Verification

- `./gradlew.bat test --tests "*CandidateReferenceAcceptancePacketValidationTest" --no-daemon --console=plain`
- `git diff --check` over touched files.
- Trailing-whitespace scan for new/untracked files if needed.
- Safety scan for forbidden DB/server/API/runtime boundary changes; forbidden-input strings inside tests are allowed when asserted as failures.

### Risks

- Packet-only validation cannot independently query prior artifacts, so source closure must rely on carried source evidence and deterministic item identity digests from the previous gate.
- Human source is available through the decision intake evidence; the acceptance packet must carry it forward so this validation gate can reject non-human acceptance sources.

### Drift Log

- 2026-06-27: Started boundary-closure slice after `03` passed the lifecycle/progress docs repair. This slice intentionally validates only local candidate-only acceptance packets and does not advance to formal registry write, approval, lock, canon, or runtime.
- 2026-06-27: Added `validate-candidate-reference-acceptance-packet`, validation report models, human decision source carry-forward, and image-result identity digest closure. Focused validation tests and adjacent acceptance packet tests passed with `--no-daemon --console=plain`.
- 2026-06-27: Verification completed: focused tests passed, `git diff --check` reported only Windows line-ending warnings, new service/test trailing-whitespace scan was clean, and safety scan found only expected forbidden-pattern detectors / negative test fixtures. Existing worktree remains dirty with many pre-existing untracked pipeline files; this slice did not revert or clean them.
- 2026-06-27: `03` returned PASS for `candidate_reference_acceptance_packet_validation`. `01` updated official product-deliverable progress from 58% to 59%, marked lifecycle Stage 22 done, and set the next slice to an E2E happy-path fixture from accepted draft to reviewable storyboard packet.

## Active Pipeline Lifecycle Map And Progress Rubric Repair Plan

### Goal

Repair project-control documents before the next implementation slice: separate product deliverable progress from gate hardening progress, create a single pipeline lifecycle map, and send the updated blueprint to the ChatGPT expert model for review before `03` review.

### Global Strategy

- Keep this slice docs/control only.
- Update `docs/PROJECT_COMPLETION_PROGRESS.md` so the displayed percentage is internally consistent and tied to product-deliverable progress.
- Add `docs/PIPELINE_LIFECYCLE_MAP.md` as the control map for artifact lifecycle, candidate-only boundaries, source-binding risks, and next slice order.
- Record that the previous expert-model blueprint review returned `REVISE`: strong safety gates, weak product-delivery proof and progress governance.
- After the docs draft is verified, `01` sends the updated blueprint to the ChatGPT expert model. If the expert model returns `REVISE`, repair the docs before `03`. If it returns `PASS` or non-blocking notes, send to `03` for read-only review.

### Non-Goals

- No Kotlin source, tests, schema, migration, CLI behavior, SQLite/runtime write, `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.
- No formal registry write, asset approval, approved reference, registered reference, production lock, canon application, provider API/job, remote download, image generation, video generation, audio generation, server/port, web UI, Docker, React/Next, FastAPI/Express/Ktor/Spring, Android UI, or Rust.
- Do not edit the old OneDrive workspace. Canonical workspace is `C:\Users\f7212\Projects\AI 劇本`.

### Scope

- `PLANS.md`
- `docs/PROJECT_COMPLETION_PROGRESS.md`
- `docs/PIPELINE_LIFECYCLE_MAP.md`

### Confirmed Facts

- `docs/PROJECT_COMPLETION_PROGRESS.md` previously displayed `79%`, but its table rows added to `93`.
- `03` passed the `Accelerated Candidate Reference Registry Validation and Acceptance Boundary Batch` REVISE P1 repair.
- The ChatGPT expert model reviewed the blueprint and returned `REVISE`, mainly because the project has strong gate hardening but lacks product-delivery proof, lifecycle clarity, story movement validation, and a consistent progress rubric.
- The current canonical workspace is `C:\Users\f7212\Projects\AI 劇本`.

### Assumptions

- `58%` is a conservative product-deliverable display estimate until an E2E happy-path fixture proves reviewable storyboard packet delivery.
- `79%` remains useful as a separate gate hardening indicator but should not be displayed as total product completion.

### Open Questions

- Whether expert-model review will accept the new 58/79 split or request a different rubric.
- Whether `03` will require a separate `docs/PROJECT_PROGRESS_RUBRIC.md` after expert review, or accept the rubric inside `docs/PROJECT_COMPLETION_PROGRESS.md`.

### Implementation Steps

- [x] Step 1: Add this docs/control repair plan to `PLANS.md`.
- [x] Step 2: Rewrite `docs/PROJECT_COMPLETION_PROGRESS.md` with a consistent product-deliverable rubric and separate gate-hardening indicator.
- [x] Step 3: Add `docs/PIPELINE_LIFECYCLE_MAP.md`.
- [x] Step 4: Run docs-only verification.
- [x] Step 5: Send the updated blueprint to the ChatGPT expert model for review.
- [x] Step 6: Decide whether to repair again or hand off to `03`.

### Verification

- Run `git diff --check -- PLANS.md docs/PROJECT_COMPLETION_PROGRESS.md docs/PIPELINE_LIFECYCLE_MAP.md`.
- Confirm no Kotlin/source/test/schema/runtime/CLI files were changed by this slice.
- Confirm new docs do not introduce the old OneDrive path as canonical workspace.

### Risks

- Lowering displayed progress may be disruptive, but keeping an inconsistent number is worse for control.
- The lifecycle map can become stale if future gates are added without updating it.
- Expert-model review uses limited quota, so this review should focus only on the updated blueprint and rubric.

### Drift Log

- 2026-06-27: User instructed 01 to update the blueprint first and then send it to the ChatGPT expert model for review. 02 had started a similar docs repair in a thread still rooted at the old OneDrive path, so 01 took over the canonical workspace update in `C:\Users\f7212\Projects\AI 劇本` and left old-path changes untouched.
- 2026-06-27: Updated `docs/PROJECT_COMPLETION_PROGRESS.md` to display `58%` as product-deliverable progress and preserve `79%` only as a gate-hardening indicator. Added `docs/PIPELINE_LIFECYCLE_MAP.md` as the lifecycle control map. `git diff --check` on the three docs/control files showed only the existing Windows line-ending warning.
- 2026-06-27: Sent the updated blueprint to the ChatGPT expert model. Expert verdict was `PASS` for sending this docs/control update to `03` read-only review, with one non-blocking suggestion to clarify lifecycle Stage 21/22 wording. 01 applied that wording clarification before `03` handoff.
- 2026-06-27: Sent docs/control update to `03` for read-only review. Requested verdict on the 58% product-deliverable progress display, 79% gate-hardening indicator, lifecycle map boundaries, next slice order, and OneDrive canonical-path incident.
- 2026-06-27: `03` passed the docs/control update. Official display is now `Project completion progress: 58%`. Next approved slice order starts with `candidate_reference_acceptance_packet_validation` as boundary closure only, not formal registry write or asset lock.

## Active Accelerated Candidate Reference Registry Validation And Acceptance Boundary Batch Plan

### Goal

Continue after `candidate_reference_registry_packet_validation` by validating the local candidate reference registry preparation packet, then creating a human acceptance boundary intake and a candidate-only acceptance packet. This remains local JSON boundary work only: no formal registry write, no SQLite/runtime write, no asset approval, no approved or registered reference, no production lock, no canon, and no image/video generation.

### Global Strategy

- Keep the whole package Kotlin/JVM CLI plus JSON file protocol only.
- Step 1 adds `validate-candidate-reference-registry-packet`: read a candidate reference registry preparation packet, require PASS/candidate status/validation next step/source fields/SHA/image results, and block remote/provider/API/generated/approval/registered/canon/lock wording.
- Step 1 PASS authorizes only `candidate_reference_registry_human_acceptance_ready`, meaning a human may decide whether to create the next local acceptance-boundary packet.
- Step 2 adds `validate-candidate-reference-registry-human-acceptance-decision`: read Step 1 validation report plus an external human/controller decision JSON; verify human source, narrow status, checklist coverage, source binding, and item-level image result identity.
- Step 2 PASS with `ALLOW_CANDIDATE_REFERENCE_ACCEPTANCE_PACKET` authorizes only `candidate_reference_acceptance_packet`; `REQUEST_CHANGES` and `REJECT` produce no downstream step.
- Step 3 adds `create-candidate-reference-acceptance-packet`: create a local candidate-only acceptance-boundary packet that can later be validated, not a formal registry write or approval record.
- Step 3 PASS authorizes only `candidate_reference_acceptance_packet_validation`.

### Non-Goals

- No SQLite write, schema migration, formal registry write, asset approval, approved reference, registered reference, production lock, canon application, runtime scene creation, provider API/job, remote download, image generation, video generation, script rewriting, or external AI call.
- No server/port, browser control, QuarterFull work, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/CandidateReferenceRegistryPacketValidationService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/CandidateReferenceRegistryHumanAcceptanceDecisionIntakeService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/CandidateReferenceAcceptancePacketService.kt`
- `app/src/test/kotlin/com/localnovel/engine/CandidateReferenceRegistryPacketValidationTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/CandidateReferenceRegistryHumanAcceptanceDecisionIntakeTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/CandidateReferenceAcceptancePacketTest.kt`

### Confirmed Facts

- `01` reported `03` PASS for the prior candidate reference registry preparation package.
- `Project completion progress` reported by `01` is currently `77%`.
- `CandidateReferenceRegistryPacketService` emits `nextAllowedDownstreamStep=candidate_reference_registry_packet_validation` on PASS.
- `CandidateReferenceRegistryPacket` currently carries source fields and candidate image result entries, but can carry additional lineage fields from the previous preflight with defaulted additive model fields.
- User explicitly requires this batch to remain validation and acceptance-boundary JSON only, not formal registry write, asset approval, approved reference, registered reference, production lock, or canon.

### Assumptions

- `candidate_reference_acceptance_packet_validation` is a safe next step because it means later validation of a local boundary packet, not asset registry write or approval.

### Open Questions

- A future package must decide whether an acceptance-boundary packet can ever lead to a formal registry write, and if so what separate manual gate and validator rules are required.

### Implementation Steps

- [x] Step 1: Record this accelerated candidate reference registry validation and acceptance-boundary batch in `PLANS.md`.
- [x] Step 2: Add `minimal candidate reference registry packet validation gate`.
- [x] Step 3: Add `minimal candidate reference registry human acceptance decision intake gate`.
- [x] Step 4: Add `minimal candidate reference acceptance packet gate`.
- [x] Step 5: Run focused tests and diff checks for the package.
- [x] Step 6: Notify `03` that this is a user-requested accelerated medium package and ask for package-level read-only review.

### Verification

- Step 2: Unit tests should prove PASS packet validates and emits `candidate_reference_registry_human_acceptance_ready`.
- Step 2: Unit tests should prove upstream packet FAIL, wrong next step, missing source fields, invalid SHA, empty image results, remote/provider/API/generated/approval/registered/canon/lock wording fail.
- Step 2: CLI smoke should write JSON without `--db`.
- Step 3: Unit tests should prove PASS validation report plus `ALLOW_CANDIDATE_REFERENCE_ACCEPTANCE_PACKET` human decision passes and emits `candidate_reference_acceptance_packet`.
- Step 3: Unit tests should prove auto/model/generated source, incomplete checklist coverage, request/reject no downstream, source mismatch, and item-level image result mismatch fail or do not release downstream.
- Step 3: CLI smoke should write JSON without `--db`.
- Step 4: Unit tests should prove PASS validation report plus PASS decision intake creates a candidate-only acceptance packet and emits `candidate_reference_acceptance_packet_validation`.
- Step 4: Unit tests should prove forbidden registered/approved/locked/final/canon/generated/provider/API/remote wording fails.
- Step 4: CLI smoke should write JSON without `--db`.
- Step 5: Run focused Gradle tests for the three new test classes and `git diff --check`.

### Risks

- Accidentally using acceptance wording that implies user approval, approved reference, registered reference, formal registry write, production lock, final state, or canon.
- Allowing a forged human decision intake to truncate or rewrite the candidate image result source chain.
- Allowing remote provider URLs, provider job ids, or API responses to become source of truth through metadata-only references.

### Drift Log

- 2026-06-25: `03` passed the prior candidate reference registry preparation package; `01` asked `02` to continue accelerated medium package mode at 77% progress.
- 2026-06-25: Evidence pass found the next package can safely include three read-only JSON gates because all outputs stop before formal registry write, approved/registered reference, production lock, canon, or SQLite/runtime changes.
- 2026-06-25: Implemented the three validation/acceptance-boundary gates with CLI commands, additive source-lineage report fields, item-level image result binding, and focused CLI smoke tests. Verified with `.\gradlew.bat test --tests "*CandidateReferenceRegistryPacketValidationTest" --tests "*CandidateReferenceRegistryHumanAcceptanceDecisionIntakeTest" --tests "*CandidateReferenceAcceptancePacketTest" --no-daemon --console=plain`; `git diff --check` showed only Windows line-ending warnings.
- 2026-06-25: Sent package-level read-only review handoff to `03`, asking it to report PASS / REVISE / FAIL back to `01`.
- 2026-06-25: `03` returned REVISE P1 for Step 2 and Step 3: `imageResults[].assetType` must be included in item-level source binding and forbidden status / claim scan surfaces to prevent approved/registered/locked wording from crossing the acceptance boundary. Progress remains 77%.
- 2026-06-25: Repaired REVISE P1 by comparing `assetType` during Step 2 and Step 3 item-level image result source binding, scanning `assetType` for forbidden status/claim wording, and adding forged `assetType` regression tests. Focused tests passed; diff check showed only Windows line-ending warnings.
- 2026-06-26: `03` passed the REVISE P1 repair; `01` counted this validation/acceptance-boundary package and updated displayed project completion progress to 79%.
- 2026-06-26: `01` moved Codex active global workspace configuration away from OneDrive to `C:\Users\f7212\Projects`, stopped the local OneDrive process/startup/tasks, updated `config.toml`, global state, and process-manager cwd records, and verified active Codex config files have zero `C:\Users\f7212\OneDrive` path hits. Current project progress remains 79%.

## Active Accelerated Candidate Reference Registry Preparation Batch Plan

### Goal

Continue after `candidate_reference_human_acceptance_ready` by validating an external human/controller acceptance decision, preflighting candidate-reference registry preparation, and optionally creating a local candidate-only registry preparation packet. This remains local JSON preparation only: no SQLite write, no formal registry write, no asset approval, no approved reference, no production lock, no canon, no image/video generation, and no remote download.

### Global Strategy

- Keep the whole package Kotlin/JVM CLI plus JSON file protocol only.
- Step 1 adds `validate-candidate-reference-human-acceptance-decision`: read a PASS candidate reference packet validation report and an external human decision JSON; verify source binding, human source, narrow status, checklist coverage, blocking issues, item-level image result identity, and forbidden claims.
- Step 1 PASS with `ALLOW_CANDIDATE_REFERENCE_REGISTRY_PREFLIGHT` authorizes only `candidate_reference_registry_preflight`; `REQUEST_CHANGES` and `REJECT` produce no downstream step.
- Step 2 adds `validate-candidate-reference-registry-preflight`: read the PASS decision intake plus the direct candidate reference packet validation report, prove both still match and are PASS-shaped, then authorize only `candidate_reference_registry_packet`.
- Step 3 adds `create-candidate-reference-registry-packet`: read the PASS registry preflight and create a candidate-only local registry preparation packet, not a registry write or approval record.
- Step 3 PASS authorizes only `candidate_reference_registry_packet_validation`.

### Non-Goals

- No SQLite write, schema migration, formal registry write, asset approval, approved reference, production lock, canon application, runtime scene creation, provider API/job, remote download, image generation, video generation, script rewriting, or external AI call.
- No server/port, browser control, QuarterFull work, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/CandidateReferenceHumanAcceptanceDecisionIntakeService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/CandidateReferenceRegistryPreflightService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/CandidateReferenceRegistryPacketService.kt`
- `app/src/test/kotlin/com/localnovel/engine/CandidateReferenceHumanAcceptanceDecisionIntakeTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/CandidateReferenceRegistryPreflightTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/CandidateReferenceRegistryPacketTest.kt`

### Confirmed Facts

- `01` reported `03` PASS for the prior package REVISE P1 item-level image result source-binding repair.
- `Project completion progress` reported by `01` is currently `75%`.
- `ImageCandidateReferencePacketValidationService` emits `nextAllowedDownstreamStep=candidate_reference_human_acceptance_ready` on PASS.
- `ImageCandidateReferencePacketValidationReport` carries accepted script packet id, script packet draft file/SHA-256, provider/result/reference source fields, and candidate image result entries.
- User explicitly requires this batch to remain registry preparation only, not formal registry write, asset approval, approved reference, production lock, or canon.

### Assumptions

- `candidate_reference_registry_packet_validation` is a safe next step because it means later validation of a preparation packet, not formal registry write.

### Open Questions

- A future package must define the boundary between candidate registry preparation and any formal registry/acceptance workflow before SQLite or runtime writes are considered.

### Implementation Steps

- [x] Step 1: Record this accelerated candidate reference registry preparation batch in `PLANS.md`.
- [x] Step 2: Add `minimal candidate reference human acceptance decision intake gate`.
- [x] Step 3: Add `minimal candidate reference registry preflight`.
- [x] Step 4: Add `minimal candidate reference registry packet draft gate`.
- [x] Step 5: Run focused tests and diff checks for the package.
- [x] Step 6: Notify `03` that this is a user-requested accelerated medium package and ask for package-level read-only review.

### Verification

- Step 2: Unit tests should prove PASS validation report plus `ALLOW_CANDIDATE_REFERENCE_REGISTRY_PREFLIGHT` human decision passes and emits `candidate_reference_registry_preflight`.
- Step 2: Unit tests should prove upstream non-PASS, wrong next step, source mismatch, auto/model source, unknown status, incomplete checklist coverage, unresolved blocking issue, `REQUEST_CHANGES` / `REJECT` no downstream, item-level image result mismatch, approved/reference/locked/final/canon/generated/provider/remote/API wording fail or do not release downstream.
- Step 2: CLI smoke should write JSON without `--db`.
- Step 3: Unit tests should prove PASS decision intake plus direct PASS validation report passes and emits `candidate_reference_registry_packet`.
- Step 3: Unit tests should prove forged intake/direct validation source mismatch, upstream non-PASS, wrong next step, and forbidden wording fail.
- Step 3: CLI smoke should write JSON without `--db`.
- Step 4: Unit tests should prove PASS preflight creates a candidate-only registry preparation packet and emits `candidate_reference_registry_packet_validation`.
- Step 4: Unit tests should prove approved_reference / approved / locked / final / production-ready / canon / generated / provider job / remote URL / API response wording fails.
- Step 4: CLI smoke should write JSON without `--db`.
- Step 5: Run focused Gradle tests for the three new test classes and `git diff --check`.

### Risks

- Letting registry preparation wording imply formal registry write, asset approval, approved reference, user approval, production lock, final state, or canon.
- Allowing a forged decision intake to truncate or rewrite the candidate image result source chain.
- Allowing remote provider URLs, provider job ids, or API responses to become source of truth through metadata-only references.

### Drift Log

- 2026-06-25: `03` passed the prior image candidate result human review package after item-level image result source binding was added to Step 2; `01` asked `02` to continue accelerated medium package mode at 75% progress.
- 2026-06-25: Evidence pass found the next package can safely include three read-only JSON gates because all outputs stop before formal registry write, approved reference, production lock, canon, or SQLite/runtime changes.
- 2026-06-25: Implemented the three registry-preparation gates and focused tests with CLI smoke. Verified with `.\gradlew.bat test --tests "*CandidateReferenceHumanAcceptanceDecisionIntakeTest" --tests "*CandidateReferenceRegistryPreflightTest" --tests "*CandidateReferenceRegistryPacketTest" --no-daemon --console=plain`; diff checks found only Windows line-ending warnings.
- 2026-06-25: Sent package-level read-only review request to `03`, noting the user-requested accelerated medium package and deterministic JSON boundary.
- 2026-06-25: `03` passed the package-level review; `01` counted this registry-preparation package and updated displayed project completion progress to 77%.

## Active Accelerated Image Candidate Result Human Review Batch Plan

### Goal

Continue after `image_candidate_result_human_review_ready` by validating an external human/controller decision for candidate image results, creating a local candidate reference packet, and validating that packet before any future asset-registry or approval work. This remains candidate-only JSON workflow: no SQLite write, no asset approval, no approved reference, no production lock, no canon, no image generation, and no remote download.

### Global Strategy

- Keep the whole package Kotlin/JVM CLI plus JSON file protocol only.
- Step 1 adds `validate-image-candidate-result-human-review-decision`: read a PASS result review preflight and an external human decision JSON; verify source binding, human source, narrow status, checklist coverage, blocking issues, candidate result safety, and forbidden claims.
- Step 1 PASS with `ALLOW_CANDIDATE_REFERENCE_PACKET` authorizes only `image_candidate_reference_packet`; `REQUEST_CHANGES` and `REJECT` produce no downstream step.
- Step 2 adds `create-image-candidate-reference-packet`: read the PASS result review preflight and PASS decision intake report, then create a candidate-only local reference packet.
- Step 2 PASS authorizes only `candidate_reference_packet_validation`.
- Step 3 adds `validate-image-candidate-reference-packet`: read only the packet and ensure status remains candidate-only, local/metadata references remain remote-free, and the packet does not imply approved reference, production lock, final asset, canon, or registry write.
- Step 3 PASS authorizes only `candidate_reference_human_acceptance_ready`, meaning a future human decision may decide whether to start a separate asset registry/acceptance workflow.

### Non-Goals

- No provider API call, provider job, remote image download, image generation, video generation, asset registry write, asset approval, approved reference, production lock, canon application, runtime scene creation, script rewriting, or external AI call.
- No SQLite write, schema migration, server/port, browser control, QuarterFull work, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidateResultHumanReviewDecisionIntakeService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidateReferencePacketService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidateReferencePacketValidationService.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidateResultHumanReviewDecisionIntakeTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidateReferencePacketTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidateReferencePacketValidationTest.kt`

### Confirmed Facts

- `01` reported `03` PASS for the external result intake package REVISE P1 second-layer repair.
- `Project completion progress` reported by `01` is currently `73%`.
- `ImageCandidateResultReviewPreflightService` emits `nextAllowedDownstreamStep=image_candidate_result_human_review_ready` on PASS.
- `ImageCandidateResultReviewPreflightReport` carries preflight id, external result intake file/result id, provider handoff validation report file, packet id, preflight source id, decision id, accepted script packet id, script packet draft file/SHA-256, and candidate image results.
- Existing adjacent decision intake gates use human source checks, narrow status vocabulary, required checklist coverage, blocking issue checks, and no downstream release for request/reject states.

### Assumptions

- `candidate_reference_human_acceptance_ready` is a safe stop point because it means waiting for a later human acceptance/registry decision, not approving a reference or writing an asset registry.

### Open Questions

- Future slices should define if and how a candidate reference packet can enter a formal asset registry or acceptance workflow.

### Implementation Steps

- [x] Step 1: Record this accelerated image candidate result human review batch in `PLANS.md`.
- [x] Step 2: Add `minimal image candidate result human review decision intake gate`.
- [x] Step 3: Add `minimal image candidate reference packet gate`.
- [x] Step 4: Add `minimal image candidate reference packet validation gate`.
- [x] Step 5: Run focused tests and diff checks for the package.
- [x] Step 6: Notify `03` that this is a user-requested accelerated medium package and ask for package-level read-only review.
- [x] Step 7: REVISE P1 repair: add item-level image result source binding to `create-image-candidate-reference-packet`.

### Verification

- Step 2: Unit tests should prove PASS preflight plus `ALLOW_CANDIDATE_REFERENCE_PACKET` human decision passes and emits `image_candidate_reference_packet`.
- Step 2: Unit tests should prove upstream non-PASS, wrong next step, source mismatch, auto/model source, unknown status, incomplete checklist coverage, unresolved blocking issue, `REQUEST_CHANGES` / `REJECT` no downstream, approved/reference/locked/final/canon wording, and remote metadata references fail or do not release downstream.
- Step 2: CLI smoke should write JSON without `--db`.
- Step 3: Unit tests should prove PASS preflight plus PASS decision intake creates a PASS candidate reference packet and emits `candidate_reference_packet_validation`.
- Step 3: Unit tests should prove source mismatch, upstream non-PASS, intake not downstream-allowed, approved/reference/locked/final/canon packet status, remote URL / provider URI in metadata references, provider jobs, API responses, and approval/canon/runtime claims fail.
- Step 3: CLI smoke should write JSON without `--db`.
- Step 4: Unit tests should prove PASS packet validates and emits `candidate_reference_human_acceptance_ready`.
- Step 4: Unit tests should prove non-PASS packet, wrong next step, missing source fields, approved_reference / production_locked status, remote URL / provider URI in metadata references, provider jobs, API responses, and approval/canon/runtime claims fail.
- Step 4: CLI smoke should write JSON without `--db`.
- Step 5: Run focused Gradle tests for the three new test classes and `git diff --check`.

### Risks

- Letting candidate reference wording imply approved reference, formal asset registry, production lock, final state, or canon.
- Allowing remote provider URLs, provider job ids, or API responses to become source of truth through metadata-only references.
- Losing source lineage from accepted script packet, script packet draft, provider handoff validation, external result intake, result review preflight, and human decision.

### Drift Log

- 2026-06-25: `03` passed the external result intake package REVISE P1 second-layer repair; `01` asked `02` to continue medium package mode without expanding safety boundaries.
- 2026-06-25: Evidence pass found this package can safely include a read-only candidate reference packet validation gate because it still stops before any asset registry write, approved reference, production lock, or canon.
- 2026-06-25: Implemented the three package gates, added focused tests with CLI smoke, and verified with `.\gradlew.bat test --tests "*ImageCandidateResultHumanReviewDecisionIntakeTest" --tests "*ImageCandidateReferencePacketTest" --tests "*ImageCandidateReferencePacketValidationTest" --no-daemon --console=plain`; `git diff --check` found only Windows line-ending warnings.
- 2026-06-25: Sent package-level read-only review request to `03`, then sent a clean focused-test command correction to avoid a copied control character in the review prompt.
- 2026-06-25: `03` returned REVISE P1 for Step 2: candidate reference packet creation must prove `decisionIntake.imageResults` are the same reviewed items carried by `resultReviewPreflight.imageResults`, not forged replacements with matching top-level source fields.
- 2026-06-25: REVISE P1 repaired by adding item-level image result source binding to candidate reference packet creation and a forged-image-result regression test. Verified with `.\gradlew.bat test --tests "*ImageCandidateResultHumanReviewDecisionIntakeTest" --tests "*ImageCandidateReferencePacketTest" --tests "*ImageCandidateReferencePacketValidationTest" --no-daemon --console=plain`; diff checks found only Windows line-ending warnings.
- 2026-06-25: `03` passed the REVISE P1 repair; `01` counted this package and updated displayed project completion progress to 75%.

## Active Accelerated Image Candidate External Result Intake Batch Plan

### Goal

Continue after `image_candidate_manual_external_action_ready` by validating human-provided external image candidate result metadata and preparing it for human result review. This remains local intake and review preflight only: no provider API call, no provider job submission, no remote download, no SQLite write, no asset approval, no approved reference, no production lock, and no canon.

### Global Strategy

- Keep the whole package Kotlin/JVM CLI plus JSON file protocol only.
- Step 1 adds `validate-image-candidate-external-result-intake`: read a PASS provider handoff packet validation report plus a human-provided external result JSON, validate source binding, human source, candidate-only status, local/metadata-only result references, prompt/candidate id linkage, and forbidden provider/API/approval claims.
- Step 1 PASS authorizes only `image_candidate_result_review_preflight`.
- Step 2 adds `validate-image-candidate-result-review-preflight`: read the PASS external result intake report and confirm the candidate image result package is ready for later human review, not approved or locked.
- Step 2 PASS authorizes only `image_candidate_result_human_review_ready`.
- Do not add the optional human review decision intake in this package; it is closer to candidate reference packet creation and should wait for a separate reviewed slice.
- REVISE P1 repair: treat `metadataOnlyReference` as a source-bearing field and block remote URL / URI scheme / provider job / API response wording in both external result intake and result review preflight.
- REVISE P1 second-layer repair: detect embedded URL / URI schemes anywhere inside `metadataOnlyReference`, not only when the field starts with a URL.

### Non-Goals

- No provider API call, provider job, remote image download, image generation, video generation, output registration into runtime data, asset approval, approved reference, production lock, canon application, runtime scene creation, script rewriting, or external AI call.
- No SQLite write, schema migration, server/port, browser control, QuarterFull work, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidateExternalResultIntakeService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidateResultReviewPreflightService.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidateExternalResultIntakeTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidateResultReviewPreflightTest.kt`

### Confirmed Facts

- `01` reported `03` PASS for the manual provider handoff package REVISE P1 repair.
- `Project completion progress` reported by `01` is currently `71%`.
- `ImageCandidateProviderHandoffPacketValidationService` emits `nextAllowedDownstreamStep=image_candidate_manual_external_action_ready` on PASS.
- `ImageCandidateProviderHandoffPacketValidationReport` now carries `acceptedScriptPacketId`, `scriptPacketDraftFile`, prompt items, provider handoff packet file, packet id, preflight id, decision id, preparation id, and source report fields.
- Existing adjacent gates use local report models, source-binding nonblank checks, exact matches for carried ids/files/hashes, candidate-only status wording, forbidden claim scans, and CLI smoke tests.

### Assumptions

- A local relative image path can be accepted as a human-provided candidate file reference if it is not a URL, provider job reference, API response, or approval/lock signal.
- `image_candidate_result_human_review_ready` is a safe stop point because it means waiting for human review, not asset approval or production use.

### Open Questions

- Future slices should decide whether accepted result candidates become candidate reference packets and what extra human decision categories are required before that step.

### Implementation Steps

- [x] Step 1: Record this accelerated external result intake package in `PLANS.md`.
- [x] Step 2: Add `minimal image candidate external result intake gate`.
- [x] Step 3: Add `minimal image candidate result review preflight`.
- [x] Step 4: Run focused tests and diff checks for the package.
- [x] Step 5: Notify `03` that this is a user-requested accelerated medium package and ask for package-level read-only review.
- [x] Step 6: Repair REVISE P1 remote/provider/API guard for `metadataOnlyReference`.
- [x] Step 7: Add regression tests and rerun focused tests plus diff checks.
- [x] Step 8: Notify `03` that the REVISE P1 repair is ready for read-only review.
- [x] Step 9: Repair REVISE P1 second-layer embedded URL / URI scheme bypass in `metadataOnlyReference`.
- [x] Step 10: Add embedded URL / URI regression tests and rerun focused tests plus diff checks.
- [x] Step 11: Notify `03` that the second-layer REVISE P1 repair is ready for read-only review.

### Verification

- Step 2: Unit tests should prove PASS provider handoff packet validation plus human external result passes and emits `image_candidate_result_review_preflight`.
- Step 2: Unit tests should prove upstream non-PASS, wrong next step, source mismatch, auto/model/generated source, remote URL/provider job/API response, missing prompt/candidate linkage, missing result metadata/path, approved/reference/locked/final/generated/canon/runtime claims, and unsafe status fail.
- Step 2: CLI smoke should write JSON without `--db`.
- Step 3: Unit tests should prove PASS external result intake validates for human review readiness and emits `image_candidate_result_human_review_ready`.
- Step 3: Unit tests should prove non-PASS intake, wrong next step, missing source fields, missing do-not-show checklist, missing drift review checklist, missing residual risks, remote/provider/API fields, and approval/canon/runtime claims fail.
- Step 3: CLI smoke should write JSON without `--db`.
- Step 4: Run focused Gradle tests for the two new test classes and `git diff --check`.
- Step 7: Regression tests should prove `metadataOnlyReference=https://...` fails in both external result intake and result review preflight, with no downstream step released.
- Step 10: Regression tests should prove `metadataOnlyReference=manual metadata from https://...` and embedded URI schemes fail in both external result intake and result review preflight, with no downstream step released.

### Risks

- Treating external result intake as repo-side image generation instead of human-supplied metadata.
- Allowing remote provider URLs, provider job ids, or API responses to become local source of truth.
- Letting candidate result wording imply asset approval, approved reference, production lock, final state, or canon.
- Losing prompt item / candidate id linkage between handoff packet validation and returned result metadata.

### Drift Log

- 2026-06-25: `03` passed the manual provider handoff package REVISE P1 repair; `01` asked `02` to continue medium package mode without expanding safety boundaries.
- 2026-06-25: Evidence pass found this package can stay local/read-only JSON by accepting only human-provided candidate result metadata/local controlled references and stopping at human result review readiness.
- 2026-06-25: Added `validate-image-candidate-external-result-intake` models, service, CLI wiring, and focused tests. It accepts only human/user/control result sources, local or metadata-only candidate references, and rejects remote URLs, provider jobs, API responses, generated status, approval/lock/canon wording, and source mismatch.
- 2026-06-25: Added `validate-image-candidate-result-review-preflight` models, service, CLI wiring, and focused tests. It stops at `image_candidate_result_human_review_ready` and does not create a review decision, candidate reference packet, asset approval, or output registration.
- 2026-06-25: Initial parallel/sequential Gradle attempts hit Kotlin daemon memory pressure in this large workspace; rerunning with `-Pkotlin.daemon.jvmargs=-Xmx1536m` compiled successfully, then the clean focused test command passed without the extra property from cache. Diff checks showed only Windows line-ending warnings.
- 2026-06-25: Sent accelerated external result intake package read-only review handoff to `03`, asking for package-level PASS / REVISE / FAIL back to `01`.
- 2026-06-25: `03` returned REVISE P1 because `metadataOnlyReference` can carry a remote provider URL while both intake and review preflight only blocked URL schemes in `localImagePath` and explicit remote/provider fields.
- 2026-06-25: Repaired REVISE P1 by blocking URL / URI schemes and provider job / API response wording in `metadataOnlyReference` for both external result intake and result review preflight. Added regression tests proving remote metadata references fail with no downstream step; focused tests passed and diff checks showed only Windows line-ending warnings.
- 2026-06-25: Sent REVISE P1 repair handoff to `03`, asking for PASS / REVISE / FAIL back to `01`.
- 2026-06-25: `03` returned REVISE P1 again because the URL detector was anchored to the start of the field, allowing embedded URLs such as `manual metadata from https://provider.example/image.png` inside `metadataOnlyReference`.
- 2026-06-25: Repaired the second-layer P1 bypass by adding an unanchored embedded URL / URI scheme detector for `metadataOnlyReference` in both external result intake and result review preflight. Added regression tests for embedded `https://...` and `provider://...`; focused tests passed and diff checks showed only Windows line-ending warnings.
- 2026-06-25: Sent second-layer REVISE P1 repair handoff to `03`, asking for PASS / REVISE / FAIL back to `01`.

## Active Accelerated Image Candidate Manual Provider Handoff Batch Plan

### Goal

Continue after `image_candidate_manual_provider_handoff_ready` by validating an external human/controller decision to allow a local manual provider handoff packet, creating that packet, and validating the packet before any future manual external action. This remains local preparation only: no provider API call, no provider job, no image generation, no output registration, no asset approval, no production lock, and no canon.

### Global Strategy

- Keep the whole package Kotlin/JVM CLI plus JSON file protocol only.
- Step 1 adds `validate-image-candidate-provider-handoff-decision`: read a PASS provider handoff preflight and an external human decision JSON; verify source binding, human source, narrow status, checklist coverage, blocking issues, prompt item safety, and forbidden claims.
- Step 1 PASS with `ALLOW_MANUAL_PROVIDER_HANDOFF_PACKET` authorizes only `image_candidate_manual_provider_handoff_packet`; `REQUEST_CHANGES` and `REJECT` produce no downstream step.
- Step 2 adds `create-image-candidate-provider-handoff-packet`: read the PASS preflight and PASS decision intake report, then create a local manual packet that a human may inspect/copy later.
- Step 2 PASS authorizes only `image_candidate_provider_handoff_packet_validation`.
- Step 3 adds `validate-image-candidate-provider-handoff-packet`: read only the packet and ensure it remains provider-neutral, unsubmitted, output-free, unapproved, unlocked, non-canon, and ready only for future manual external action.
- Step 3 PASS authorizes only `image_candidate_manual_external_action_ready`.
- REVISE P1 repair: require `acceptedScriptPacketId` and `scriptPacketDraftFile` to remain nonblank / preserved across decision intake, packet creation, and packet validation, and require packet creation to reject blank decision lineage.

### Non-Goals

- No provider API call, provider job, prompt submission, image generation, video generation, output registration, asset approval, approved reference, production asset, production lock, canon application, runtime scene creation, script rewriting, or external AI call.
- No SQLite write, schema migration, server/port, browser control, QuarterFull work, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidateProviderHandoffDecisionIntakeService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidateProviderHandoffPacketService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidateProviderHandoffPacketValidationService.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidateProviderHandoffDecisionIntakeTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidateProviderHandoffPacketTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidateProviderHandoffPacketValidationTest.kt`

### Confirmed Facts

- `01` reported `03` PASS for the previous prompt preparation package REVISE P1 repair.
- `Project completion progress` reported by `01` is currently `68%`.
- `ImageCandidateProviderHandoffPreflightService` emits `nextAllowedDownstreamStep=image_candidate_manual_provider_handoff_ready` on PASS.
- `ImageCandidateProviderHandoffPreflightReport` carries preflight id, prompt validation report file, prompt preparation file/id, image candidate brief/preflight fields, upstream asset/storyboard/script source fields, prompt items, and a local-only downstream boundary.
- Existing human decision intake services use a narrow source/status/checklist/blocking-issue pattern and forbid auto/model/generated decisions.
- `03` reported REVISE P1: this package guarded `scriptPacketDraftContentSha256` but did not consistently require `acceptedScriptPacketId` / `scriptPacketDraftFile`, and packet creation did not reject blank `decisionIntake.decisionId`.

### Assumptions

- `image_candidate_provider_handoff_packet_validation` is safer than moving directly from packet creation to manual external action because it gives `03` and future gates a concrete validation report step.
- `image_candidate_manual_external_action_ready` is safe because it means waiting for a human outside this repo; it is not a provider submission, provider job, generated image, output registration, or asset approval.

### Open Questions

- Future slices can define how a human records the result of external manual provider work. This package deliberately does not register outputs or model provider responses.

### Implementation Steps

- [x] Step 1: Record this accelerated manual provider handoff batch in `PLANS.md`.
- [x] Step 2: Add `minimal image candidate manual provider handoff decision intake gate`.
- [x] Step 3: Add `minimal image candidate manual provider handoff packet gate`.
- [x] Step 4: Add `minimal image candidate provider handoff packet validation gate`.
- [x] Step 5: Run focused tests and diff checks for the whole package.
- [x] Step 6: Notify `03` that this is a user-requested accelerated medium package and ask for package-level read-only review.
- [x] Step 7: Repair REVISE P1 source lineage guards for accepted script packet id, script packet draft file, and decision id.
- [x] Step 8: Add regression tests and rerun focused tests plus diff checks.
- [x] Step 9: Notify `03` that the REVISE P1 repair is ready for read-only review.

### Verification

- Step 2: Unit tests should prove PASS preflight plus `ALLOW_MANUAL_PROVIDER_HANDOFF_PACKET` human decision passes and emits `image_candidate_manual_provider_handoff_packet`.
- Step 2: Unit tests should prove upstream non-PASS, wrong next step, source mismatch, auto/model source, unknown status, incomplete checklist coverage, unresolved blocking issue, `REQUEST_CHANGES` / `REJECT` no downstream, provider/API/job/output fields, and approved/reference/locked/final/generated/canon/runtime claims fail or do not release downstream.
- Step 2: CLI smoke should write JSON without `--db`.
- Step 3: Unit tests should prove PASS preflight plus PASS decision intake creates a PASS packet and emits `image_candidate_provider_handoff_packet_validation`.
- Step 3: Unit tests should prove source mismatch, upstream non-PASS, intake not downstream-allowed, provider/job/submitted/output/generated fields, and approval/canon/runtime claims fail.
- Step 3: CLI smoke should write JSON without `--db`.
- Step 4: Unit tests should prove PASS packet validates and emits `image_candidate_manual_external_action_ready`.
- Step 4: Unit tests should prove non-PASS packet, wrong next step, missing source fields, providerJobId, submittedAt, outputPath `x.png`, generated status, and approval/canon/runtime claims fail.
- Step 4: CLI smoke should write JSON without `--db`.
- Step 5: Run focused Gradle tests for the three new test classes and `git diff --check`.
- Step 8: Regression tests should prove blank `acceptedScriptPacketId` / `scriptPacketDraftFile` fail in decision intake, packet creation, and packet validation; blank `decisionIntake.decisionId` fails in packet creation.

### Risks

- Letting manual handoff packet language sound like provider submission, generated output, output registration, or asset approval.
- Carrying provider/job/submitted/output fields into a packet that should only be copied manually later.
- Treating a human decision to create a packet as user approval, production lock, or canon.
- Source-truncated reports that preserve only script hash but lose accepted script packet id or script packet draft file.

### Drift Log

- 2026-06-25: `03` passed the previous package REVISE P1; `01` asked `02` to continue medium package mode without expanding safety boundaries.
- 2026-06-25: Evidence pass found this package can stay local/read-only JSON by stopping at manual external action readiness and not defining provider response or output registration.
- 2026-06-25: Added `validate-image-candidate-provider-handoff-decision` models, service, CLI wiring, and focused tests. Verification passed with `ImageCandidateProviderHandoffDecisionIntakeTest`.
- 2026-06-25: Added `create-image-candidate-provider-handoff-packet` service, CLI wiring, and focused tests. Verification passed with `ImageCandidateProviderHandoffPacketTest`.
- 2026-06-25: Added `validate-image-candidate-provider-handoff-packet` service, CLI wiring, and focused tests. Verification passed with `ImageCandidateProviderHandoffPacketValidationTest`.
- 2026-06-25: Package verification passed with the three focused test classes in one Gradle run; diff checks showed only Windows line-ending warnings.
- 2026-06-25: Sent accelerated manual provider handoff package read-only review handoff to `03`, asking for package-level PASS / REVISE / FAIL back to `01`.
- 2026-06-25: `03` returned REVISE P1 because manual provider handoff source lineage did not require accepted script packet id and script packet draft file across all three gates, and packet creation could accept a blank decision id from a forged PASS decision intake report.
- 2026-06-25: Repaired REVISE P1 by requiring `acceptedScriptPacketId` and `scriptPacketDraftFile` through decision intake, packet creation, and packet validation; packet creation now rejects blank `decisionIntake.decisionId`, and packet validation reports carry both source fields forward. Added regression tests; focused package tests passed and diff checks showed only Windows line-ending warnings.
- 2026-06-25: Sent REVISE P1 repair handoff to `03`, asking for PASS / REVISE / FAIL back to `01`.

## Active Accelerated Image Candidate Prompt Preparation Batch Plan

### Goal

Continue the accelerated image-candidate lane after `image_candidate_prompt_preparation` by creating prompt-safe local preparation JSON, validating it, and adding a provider handoff preflight that only marks the package ready for future manual handoff. This package remains preparation-only: no provider API call, no provider job, no image generation, no output path, no asset approval, no approved reference, no production lock, and no canon.

### Global Strategy

- Keep the whole package Kotlin/JVM CLI plus JSON file protocol only.
- Step 1 adds `create-image-candidate-prompt-preparation`: read a PASS image candidate brief and create local prompt-facing preparation items from candidate-only requirements.
- Step 1 PASS authorizes only `image_candidate_prompt_validation`.
- Step 2 adds `validate-image-candidate-prompt-preparation`: read only prompt preparation JSON, reject author-only / hidden-truth prompt text, provider/API/job/output fields, status upgrades, generated-media claims, runtime/canon claims, and incomplete prompt items.
- Step 2 PASS authorizes only `image_candidate_provider_handoff_preflight`.
- Step 3 adds `validate-image-candidate-provider-handoff-preflight`: read only prompt validation report and confirm the package can wait for manual provider handoff. It does not create a provider job or choose a provider.
- Step 3 PASS authorizes only `image_candidate_manual_provider_handoff_ready`.
- REVISE P1 repair: include `assetType` in the same forbidden status, forbidden claim, and author-only scans used by this package, without changing the package boundary or adding any new downstream capability.

### Non-Goals

- No provider API call, provider job, prompt submission, image generation, video generation, asset approval, approved reference, production asset, production lock, canon application, runtime scene creation, script rewriting, or external AI call.
- No SQLite write, schema migration, server/port, browser control, QuarterFull work, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidatePromptPreparationService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidatePromptValidationService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidateProviderHandoffPreflightService.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidatePromptPreparationTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidatePromptValidationTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidateProviderHandoffPreflightTest.kt`

### Confirmed Facts

- `01` reported `03` PASS for `Accelerated Asset Candidate Human Review Batch`.
- `Project completion progress` reported by `01` is currently `65%`.
- `ImageCandidateBriefService` emits `nextAllowedDownstreamStep=image_candidate_prompt_preparation` on PASS.
- `ImageCandidateBrief` carries source ids/files/hash, candidate image requirements, do-not-show boundaries, prompt safety notes, and candidate-only downstream boundary.
- `docs/DEMAND_DRIVEN_QUERY_LAYER_BLUEPRINT.md` and prior image-candidate services treat prompt-facing material as local preparation and forbid author-only truth, provider/job/output, generated media, approval, lock, runtime, and canon claims.
- `03` reported REVISE P1: `assetType` is structural but can carry status-upgrade or author-only wording, and the three prompt-preparation package services were not scanning it.

### Assumptions

- `image_candidate_provider_handoff_preflight` is safe in this package because it can be a read-only report over prompt validation output and still remains before any provider handoff or job submission.
- `image_candidate_manual_provider_handoff_ready` is narrower than any image-generation step because it only means a later human/external tool may inspect the validated prompt package.

### Open Questions

- Future slices can decide provider-specific handoff schema, provider selection, job envelope shape, and storage policy. This package deliberately does not choose those.

### Implementation Steps

- [x] Step 1: Record this accelerated prompt preparation batch in `PLANS.md`.
- [x] Step 2: Add `minimal image candidate prompt preparation gate`.
- [x] Step 3: Add `minimal image candidate prompt validation gate`.
- [x] Step 4: Add `minimal image candidate provider handoff preflight`.
- [x] Step 5: Run focused tests and diff checks for the whole package.
- [x] Step 6: Notify `03` that this is a user-requested accelerated medium package and ask for package-level read-only review.
- [x] Step 7: Repair REVISE P1 by scanning `assetType` in prompt preparation, prompt validation, and provider handoff preflight.
- [x] Step 8: Add regression tests for status-upgrade / author-only `assetType` values and rerun focused tests plus diff checks.
- [x] Step 9: Notify `03` that the REVISE P1 repair is ready for read-only review.

### Verification

- Step 2: Unit tests should prove a PASS image candidate brief creates PASS prompt preparation and emits `image_candidate_prompt_validation`.
- Step 2: Unit tests should prove upstream non-PASS, wrong next step, missing source binding, missing image candidate requirements, provider/API/job/output fields, and forbidden approval/lock/canon/generated-media claims fail.
- Step 2: CLI smoke should write JSON without `--db`.
- Step 3: Unit tests should prove PASS prompt preparation validates and emits `image_candidate_provider_handoff_preflight`.
- Step 3: Unit tests should prove non-PASS preparation, wrong next step, missing source binding, incomplete prompt items, author-only / hidden-truth prompt text, provider/API/job/output fields, and forbidden status/claim text fail.
- Step 3: CLI smoke should write JSON without `--db`.
- Step 4: Unit tests should prove PASS prompt validation report passes and emits `image_candidate_manual_provider_handoff_ready`.
- Step 4: Unit tests should prove non-PASS validation, wrong next step, missing source binding, missing prompt items, author-only text, provider/API/job/output fields, and forbidden status/claim text fail.
- Step 4: CLI smoke should write JSON without `--db`.
- Step 5: Run focused Gradle tests for the three new test classes and `git diff --check`.
- Step 8: Regression tests should prove `assetType=approved_reference` or `assetType=production_locked` fails in all three gates, and `assetType=author_only` or `assetType=hidden truth` fails in prompt-facing validation / provider handoff.

### Risks

- Letting provider/API/job/output fields slip into prompt-facing local preparation.
- Letting author-only, hidden-truth, secret-cause, or hidden-motive text leak into prompt items.
- Letting manual provider handoff readiness sound like prompt submission, image generation, or asset approval.
- Missing a structural text field that can carry forbidden wording even when free-text prompt fields are clean.

### Drift Log

- 2026-06-25: `03` passed the previous accelerated package; `01` asked `02` to continue medium package mode without expanding safety boundaries.
- 2026-06-25: Evidence pass found the three-step package can remain local JSON preparation and validation before any provider/API/job or generated image output.
- 2026-06-25: Added `create-image-candidate-prompt-preparation` models, service, CLI wiring, and focused tests. Verification passed with `ImageCandidatePromptPreparationTest`.
- 2026-06-25: Added `validate-image-candidate-prompt-preparation` service, CLI wiring, and focused tests. Verification passed with `ImageCandidatePromptValidationTest`.
- 2026-06-25: Added `validate-image-candidate-provider-handoff-preflight` service, CLI wiring, and focused tests. Verification passed with `ImageCandidateProviderHandoffPreflightTest`.
- 2026-06-25: Package verification passed with the three focused test classes in one Gradle run; diff checks showed only Windows line-ending warnings.
- 2026-06-25: Sent accelerated prompt preparation package read-only review handoff to `03`, including corrected clean focused test command for prompt preparation.
- 2026-06-25: `03` returned REVISE P1 because `assetType` was not included in forbidden status / forbidden claim / author-only scans. Planned a narrow repair limited to adding `assetType` to existing scan surfaces and regression tests.
- 2026-06-25: Repaired REVISE P1 by adding `assetType` to the prompt preparation, prompt validation, and provider handoff preflight scan surfaces. Added regression tests for `approved_reference`, `production_locked`, `author_only`, and `hidden truth` asset type values; focused package tests passed and diff checks showed only Windows line-ending warnings.
- 2026-06-25: Sent REVISE P1 repair handoff to `03`, asking for PASS / REVISE / FAIL back to `01`.

## Active Accelerated Asset Candidate Human Review Batch Plan

### Goal

Continue the accelerated asset-candidate lane after `asset_candidate_human_review_ready` by validating an external human / controller decision, confirming whether image-candidate preparation may begin, and creating a local image-candidate brief. This package remains preparation-only: no asset approval, no approved reference, no production lock, no canon, no image generation, and no external generation API.

### Global Strategy

- Keep the whole package Kotlin/JVM CLI plus JSON file protocol only.
- Step 1 adds `validate-asset-candidate-human-review-decision`: read a PASS asset candidate review preflight and an external human decision JSON, then verify source binding, human source, decision status, required checklist coverage, blocking issues, image-candidate requirements, and forbidden claims.
- Step 1 PASS with `ALLOW_IMAGE_CANDIDATE_PREFLIGHT` authorizes only `image_candidate_preflight`; `REQUEST_CHANGES` and `REJECT` produce no downstream step.
- Step 2 adds `validate-image-candidate-preflight`: read only the Step 1 intake report and confirm the package may proceed to local image-candidate brief preparation.
- Step 2 PASS authorizes only `image_candidate_brief`.
- Step 3 adds `create-image-candidate-brief`: read only the Step 2 preflight report and create a deterministic local JSON brief of candidate image requirements, reference requirements, do-not-show boundary, and prompt safety notes.
- Step 3 PASS authorizes only `image_candidate_prompt_preparation`, meaning a later local prompt-preparation gate may begin. It does not authorize image generation, image output paths, providers, jobs, or approved references.

### Non-Goals

- No asset approval, asset lock, approved reference, production asset, production lock, canon application, storyboard image generation, asset image generation, video generation, voice generation, script rewriting, runtime scene creation, or external AI call.
- No SQLite write, schema migration, server/port, browser control, QuarterFull work, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/AssetCandidateHumanReviewDecisionIntakeService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidatePreflightService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ImageCandidateBriefService.kt`
- `app/src/test/kotlin/com/localnovel/engine/AssetCandidateHumanReviewDecisionIntakeTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidatePreflightTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/ImageCandidateBriefTest.kt`

### Confirmed Facts

- `01` reported `03` PASS for `Accelerated Asset Candidate Batch`.
- `Project completion progress` reported by `01` is currently `62%`.
- `AssetCandidateReviewPreflightService` emits `nextAllowedDownstreamStep=asset_candidate_human_review_ready` on PASS.
- `AssetCandidateReviewPreflightReport` carries preflight id, draft validation report file, asset candidate draft file/id, brief file/id, asset candidate preflight file/id, human review decision intake file/id, storyboard packet review file/id, accepted script packet id, script packet draft file/hash, and candidate item count.
- `docs/PRODUCTION_ASSET_REGISTRY_PLANNING.md` says `candidate` and `review_ready` are not downstream-usable, while `approved_reference` and `production_locked` are later approval states.
- `docs/DEMAND_DRIVEN_QUERY_LAYER_BLUEPRINT.md` and `docs/PRODUCTION_ASSET_REGISTRY_PLANNING.md` mention prompt-safe asset notes and warn that author-only or approval states must not leak into prompt-facing material.

### Assumptions

- `image_candidate_brief` is safe as a Step 2 next step because it is local JSON preparation and still before any provider/API/job/image output.
- `image_candidate_prompt_preparation` is the safest Step 3 next step because it remains one gate before any prompt packet and far before image generation.

### Open Questions

- Future slices can decide whether prompt preparation should split by asset type, reference view, or downstream image tool, but this package does not choose a provider.

### Implementation Steps

- [x] Step 1: Record this accelerated human review batch in `PLANS.md`.
- [x] Step 2: Add `minimal asset candidate human review decision intake gate`.
- [x] Step 3: Add `minimal image candidate preflight gate`.
- [x] Step 4: Add `minimal image candidate brief gate`.
- [x] Step 5: Run focused tests and diff checks for the whole package.
- [x] Step 6: Notify `03` that this is a user-requested accelerated medium package and ask for package-level read-only review.

### Verification

- Step 2: Unit tests should prove PASS preflight plus `ALLOW_IMAGE_CANDIDATE_PREFLIGHT` human decision passes and emits `image_candidate_preflight`.
- Step 2: Unit tests should prove upstream non-PASS, wrong preflight next step, source mismatch, auto/model source, unknown status, incomplete checklist coverage, unresolved blocking issue, forbidden approval/lock/canon/generated-media claim, generation API/provider/job/output path, and approved/reference/locked wording fail.
- Step 2: Unit tests should prove `REQUEST_CHANGES` / `REJECT` do not emit a next step.
- Step 2: CLI smoke should write JSON without `--db`.
- Step 3: Unit tests should prove PASS decision intake passes and emits `image_candidate_brief`; non-PASS, no downstream allowance, wrong decision status/next step, missing source fields, forbidden claims, output paths, generation API/provider/job, and approved/reference/locked wording fail.
- Step 3: CLI smoke should write JSON without `--db`.
- Step 4: Unit tests should prove PASS image candidate preflight creates a PASS brief and emits `image_candidate_prompt_preparation`; non-PASS, wrong next step, missing source fields, missing requirements, forbidden claims, output paths, generation API/provider/job, and approved/reference/locked wording fail.
- Step 4: CLI smoke should write JSON without `--db`.
- Step 5: Run focused Gradle tests for the three new test classes and `git diff --check`.

### Risks

- Letting `ALLOW_IMAGE_CANDIDATE_PREFLIGHT` sound like asset approval or image generation permission.
- Carrying provider/API/job/output-path fields into prompt-facing preparation.
- Letting approved/reference/locked wording sneak through an image-candidate requirement.

### Drift Log

- 2026-06-25: `03` passed the previous accelerated package; `01` asked `02` to continue in medium package mode without expanding safety boundaries.
- 2026-06-25: Evidence pass found a 3-step package is safe because all steps can remain local JSON preparation and the next-step names stay before provider prompt packets or image generation.
- 2026-06-25: Added `validate-asset-candidate-human-review-decision` models, service, CLI wiring, and focused tests. Verification passed with `AssetCandidateHumanReviewDecisionIntakeTest`.
- 2026-06-25: Added `validate-image-candidate-preflight` models, service, CLI wiring, and focused tests. Verification passed with `ImageCandidatePreflightTest`.
- 2026-06-25: Added `create-image-candidate-brief` models, service, CLI wiring, and focused tests. Verification passed with `ImageCandidateBriefTest`; a self-conflicting checklist phrase was revised after tests correctly caught forbidden production-lock wording.
- 2026-06-25: Package verification passed with `AssetCandidateHumanReviewDecisionIntakeTest`, `ImageCandidatePreflightTest`, and `ImageCandidateBriefTest`; diff checks showed only Windows line-ending warnings.
- 2026-06-25: Sent accelerated package read-only review handoff to `03`, asking for package-level PASS / REVISE / FAIL back to `01`.

## Active Accelerated Asset Candidate Batch Plan

### Goal

Accelerate the asset-candidate lane by completing a medium package of adjacent local JSON gates: keep the already-built candidate-only asset candidate draft gate, add an explicit asset candidate draft validation report, and add a narrow asset candidate review preflight that only means the package may wait for human review preparation.

### Global Strategy

- Keep the whole package Kotlin/JVM CLI plus JSON file protocol only.
- Step 1 remains `create-asset-candidate-draft`: read only `--asset-candidate-brief`, create candidate-only draft JSON, no approval, lock, image, or video.
- Step 2 adds `validate-asset-candidate-draft`: read only `--asset-candidate-draft`, re-check source binding, candidate-only status, item completeness, forbidden claims, and emit an explicit validation report.
- Step 3 adds `validate-asset-candidate-review-preflight`: read only the draft validation report and confirm the package may enter human review preparation.
- PASS from Step 2 should authorize only `asset_candidate_review_preflight`.
- PASS from Step 3 should authorize only `asset_candidate_human_review_ready`, which means waiting for human review and does not mean asset approval, asset lock, approved reference, production readiness, canon, image generation, or video generation.

### Non-Goals

- No asset approval, asset lock, approved reference, production asset, production lock, canon application, storyboard image generation, asset image generation, video generation, voice generation, script rewriting, runtime scene creation, or external AI call.
- No SQLite write, schema migration, server/port, browser control, QuarterFull work, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/AssetCandidateDraftService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/AssetCandidateDraftValidationService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/AssetCandidateReviewPreflightService.kt`
- `app/src/test/kotlin/com/localnovel/engine/AssetCandidateDraftTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/AssetCandidateDraftValidationTest.kt`
- `app/src/test/kotlin/com/localnovel/engine/AssetCandidateReviewPreflightTest.kt`

### Confirmed Facts

- User asked to increase throughput because the project felt slow; `01` authorized a medium package while keeping one verified step at a time.
- `Project completion progress` reported by `01` is currently `60%`.
- `AssetCandidateDraftService` already emits `nextAllowedDownstreamStep=asset_candidate_review_preflight` on PASS.
- `AssetCandidateDraft` already carries draft id, brief file/id, preflight file/id, human review decision intake file/id, storyboard packet review file/id, accepted script packet id, script packet draft file, script packet draft SHA-256, and candidate items.
- `docs/PRODUCTION_ASSET_REGISTRY_PLANNING.md` marks `candidate` and `review_ready` as not downstream-usable; `approved_reference` and `production_locked` are later explicit approval states.

### Assumptions

- Adding an explicit validation report is safer for `03` review than treating draft creation as both creation and final validation.
- `asset_candidate_human_review_ready` is narrower than any asset approval or generation step, because it only means the draft has passed local checks and can wait for human review.

### Open Questions

- Future slices can define the human review decision intake for asset candidates and decide how `review_ready` status is represented without implying approval.

### Implementation Steps

- [x] Step 1: Keep and verify `minimal asset candidate draft gate`.
- [x] Step 2: Add `minimal asset candidate draft validation gate`.
- [x] Step 3: Add `minimal asset candidate review preflight`.
- [x] Step 4: Run focused tests and diff checks for the whole package.
- [x] Step 5: Notify `03` that this is a user-requested accelerated medium package and ask for package-level read-only review.

### Verification

- Step 1: Run `.\gradlew.bat test --tests "*AssetCandidateDraftTest" --no-daemon`.
- Step 2: Unit tests should prove PASS draft validates and emits `nextAllowedDownstreamStep=asset_candidate_review_preflight`.
- Step 2: Unit tests should prove non-PASS draft, wrong next step, missing source binding, incomplete candidate item, forbidden status, and forbidden generated/approval/canon/runtime claims fail.
- Step 2: CLI smoke should write JSON without `--db`.
- Step 3: Unit tests should prove PASS validation report passes and emits `nextAllowedDownstreamStep=asset_candidate_human_review_ready`.
- Step 3: Unit tests should prove non-PASS validation, wrong next step, missing source binding, missing candidate items, forbidden status, and forbidden generated/approval/canon/runtime claims fail.
- Step 3: CLI smoke should write JSON without `--db`.
- Step 4: Run `.\gradlew.bat test --tests "*AssetCandidateDraftValidationTest" --no-daemon`.
- Step 4: Run `.\gradlew.bat test --tests "*AssetCandidateReviewPreflightTest" --no-daemon`.
- Step 4: Run `git diff --check` on tracked package files and `git diff --no-index --check` for new untracked service/test files.

### Risks

- Letting a validation report pass an edited draft whose top-level state or candidate wording no longer matches the original draft gate.
- Accidentally using `review_ready` as if it were approved or downstream-usable.
- Letting the review preflight sound like asset approval rather than human-review readiness.

### Drift Log

- 2026-06-25: User requested more throughput; `01` authorized moving this lane from one small slice to an `Accelerated Asset Candidate Batch`.
- 2026-06-25: Evidence pass found Step 2 and Step 3 can remain read-only JSON checks over existing `AssetCandidateDraft` data, so the package can safely expand beyond the already implemented draft gate.
- 2026-06-25: Added `AssetCandidateDraftValidationReport` and `AssetCandidateReviewPreflightReport` models, `validate-asset-candidate-draft` CLI, and `validate-asset-candidate-review-preflight` CLI. Step 2 emits only `asset_candidate_review_preflight`; Step 3 emits only `asset_candidate_human_review_ready`.
- 2026-06-25: Verification passed with `AssetCandidateDraftValidationTest`, `AssetCandidateReviewPreflightTest`, and the existing `AssetCandidateDraftTest`.
- 2026-06-25: Sent accelerated package read-only review handoff to `03`, asking for package-level PASS / REVISE / FAIL back to `01`.

## Active Minimal Asset Candidate Draft Gate Plan

### Goal

Add a local JSON draft gate after a PASS asset candidate brief. It turns candidate-only brief requirements into a deterministic asset candidate draft that remains unapproved, unlocked, non-canon, and not usable for image/video generation.

### Global Strategy

- Keep this slice Kotlin/JVM CLI plus JSON file protocol only.
- Read only `--asset-candidate-brief`; evidence shows `AssetCandidateBrief` carries source ids/files/hash, requirements, checklist, and downstream boundary.
- Require brief verdict `PASS` and `nextAllowedDownstreamStep=asset_candidate_draft`.
- Require nonblank source provenance: brief id/file, preflight id/file, human review decision id/file, storyboard packet review id/file, accepted script packet id, script packet draft file, and SHA-256.
- Convert each brief requirement into a draft item with candidate id, inferred asset type, source requirement id, candidate-only status, requirement, public description, and visual reference requirement.
- Candidate status is allowed only as candidate / candidate_only / draft_candidate. `review_ready` is intentionally not allowed at this draft stage; the next step is a separate review preflight.
- PASS should authorize only `asset_candidate_review_preflight`; FAIL should leave no downstream step.

### Non-Goals

- No asset approval, asset lock, approved reference, production asset, production lock, canon application, storyboard image generation, asset image generation, video generation, voice generation, script rewriting, runtime scene creation, or external AI call.
- No SQLite write, schema migration, server/port, browser control, QuarterFull work, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/AssetCandidateDraftService.kt`
- `app/src/test/kotlin/com/localnovel/engine/AssetCandidateDraftTest.kt`

### Confirmed Facts

- `01` assigned this slice after `03` passed the asset candidate brief gate.
- `Project completion progress` reported by `01` is currently `60%`.
- `AssetCandidateBriefService` emits `nextAllowedDownstreamStep=asset_candidate_draft` on PASS.
- `AssetCandidateBrief` carries brief id, preflight file/id, human review decision intake file/id, storyboard packet review file/id, accepted script packet id, script packet draft file, script packet draft SHA-256, candidate-only requirements, and preparation checklist.
- `docs/PRODUCTION_ASSET_REGISTRY_PLANNING.md` lists `candidate` and `review_ready` as not downstream-usable, while `approved_reference` and `production_locked` are later approved statuses.
- The assignment forbids DB writes, server/port, external AI API, image/video generation, asset approval, canon, and runtime path changes.

### Assumptions

- `asset_candidate_review_preflight` is safer than `asset_candidate_review` because it creates one more validation stop before any review-ready status.
- Draft item asset type can be inferred deterministically from candidate id / requirement terms; unclear types should fail rather than default to a broad category.

### Open Questions

- Future review preflight can decide whether `draft_candidate` may become `review_ready`.

### Implementation Steps

- [x] Step 1: Record this minimal asset candidate draft plan in `PLANS.md`.
- [x] Step 2: Add asset candidate draft models.
- [x] Step 3: Add pure draft service, CLI wiring, and focused tests.
- [x] Step 4: Run focused tests and `git diff --check`.
- [x] Step 5: Notify `03` thread with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.

### Verification

- Step 3: Unit tests should prove PASS brief creates PASS draft and emits `nextAllowedDownstreamStep=asset_candidate_review_preflight`.
- Step 3: Unit tests should prove non-PASS brief or wrong brief next step fails.
- Step 3: Unit tests should prove source binding missing fails.
- Step 3: Unit tests should prove missing candidate id / asset type / requirement / source requirement id fails.
- Step 3: Unit tests should prove candidate status `approved`, `locked`, `final`, `review_ready`, `production_locked`, `核准`, `批准`, or `鎖定` fails.
- Step 3: Unit tests should prove forbidden generated media / canon / runtime / approval claims fail.
- Step 3: CLI smoke should write JSON without `--db`.
- Step 4: Run `.\gradlew.bat test --tests "*AssetCandidateDraftTest" --no-daemon`.
- Step 4: Run `git diff --check` on changed files for this slice.

### Risks

- Inferring asset type too loosely and letting incomplete draft items pass.
- Accidentally treating `review_ready` as acceptable in the draft gate.

### Drift Log

- 2026-06-25: Started `02` implementation of `minimal asset candidate draft gate` after `03` PASS for asset candidate brief; current project completion progress from `01` is `60%`.
- 2026-06-25: Evidence pass found `AssetCandidateBrief` already carries source fields and candidate requirements, so this slice keeps the CLI to a single read-only brief input and chooses `asset_candidate_review_preflight` as the narrow next step.
- 2026-06-25: Implemented `AssetCandidateDraft` models, `create-asset-candidate-draft` CLI, pure draft service, and focused tests. Type inference was tightened to use word-boundary matching after a test exposed that `asset` could falsely match the `set` scene term.
- 2026-06-25: Verification passed with `AssetCandidateDraftTest` and adjacent `AssetCandidateBriefTest`.
- 2026-06-25: Diff checks passed with only Windows line-ending warnings; untracked service/test files were additionally checked via `git diff --no-index --check`.
- 2026-06-25: Sent read-only review handoff to `03` for PASS / REVISE / FAIL reporting back to `01`.

## Active Minimal Asset Candidate Brief Gate Plan

### Goal

Add a local read-only JSON brief gate after a PASS asset candidate preflight. It turns preflight-approved candidate-only asset requirements into a deterministic asset candidate brief for later manual / draft preparation without approving assets, locking production, generating images/video, applying canon, or creating runtime scenes.

### Global Strategy

- Keep this slice Kotlin/JVM CLI plus JSON file protocol only.
- Read only `--asset-candidate-preflight`; evidence shows `AssetCandidatePreflightReport` already carries source ids/files/hash and `assetCandidateRequirements`.
- Require preflight verdict `PASS` and `nextAllowedDownstreamStep=asset_candidate_brief`.
- Require nonblank source provenance: preflight id, human review decision intake file/id, storyboard packet review file/id, accepted script packet id, script packet draft file, and SHA-256.
- Carry asset requirements forward as candidate-only brief requirements; forbid approved, locked, final, production-ready, generated media, canon, runtime, and approval wording.
- Add deterministic preparation checklist items that explicitly state the brief is not image/video generation, asset approval, production lock, or canon.
- PASS should authorize only `asset_candidate_draft`; FAIL should leave no downstream step.

### Non-Goals

- No asset approval, asset lock, production lock, canon application, storyboard image generation, asset image generation, video generation, voice generation, script rewriting, runtime scene creation, or external AI call.
- No SQLite write, schema migration, server/port, browser control, QuarterFull work, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/AssetCandidateBriefService.kt`
- `app/src/test/kotlin/com/localnovel/engine/AssetCandidateBriefTest.kt`

### Confirmed Facts

- `01` assigned this slice after `03` passed the asset candidate preflight REVISE P1 fix.
- `Project completion progress` reported by `01` is currently `58%`.
- `AssetCandidatePreflightService` emits `nextAllowedDownstreamStep=asset_candidate_brief` on PASS.
- `AssetCandidatePreflightReport` carries preflight id, decision id, human review decision intake file, storyboard packet review file/id, accepted script packet id, script packet draft file, script packet draft SHA-256, downstream boundary, and asset candidate requirements.
- `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md` says 07 output remains candidate material and is not automatically an approved reference or production lock.
- The assignment forbids DB writes, server/port, external AI API, image/video generation, asset approval, canon, and runtime path changes.

### Assumptions

- `asset_candidate_draft` is the safest next step after a deterministic brief because it still remains local JSON draft preparation, not asset approval or media generation.

### Open Questions

- Future asset candidate draft can decide whether to split candidates by character/prop/location or keep one packet.

### Implementation Steps

- [x] Step 1: Record this minimal asset candidate brief plan in `PLANS.md`.
- [x] Step 2: Add asset candidate brief models.
- [x] Step 3: Add pure brief service, CLI wiring, and focused tests.
- [x] Step 4: Run focused tests and `git diff --check`.
- [x] Step 5: Notify `03` thread with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.

### Verification

- Step 3: Unit tests should prove PASS preflight creates PASS brief and emits `nextAllowedDownstreamStep=asset_candidate_draft`.
- Step 3: Unit tests should prove non-PASS preflight or wrong next step fails.
- Step 3: Unit tests should prove source binding missing fails.
- Step 3: Unit tests should prove missing asset candidate requirements fail.
- Step 3: Unit tests should prove forbidden approved / locked / final / generated media / canon claims fail.
- Step 3: CLI smoke should write JSON without `--db`.
- Step 4: Run `.\gradlew.bat test --tests "*AssetCandidateBriefTest" --no-daemon`.
- Step 4: Run `git diff --check` on changed files for this slice.

### Risks

- Accidentally making a brief sound like approval or production readiness.
- Carrying over unsafe candidate wording from preflight into the brief.

### Drift Log

- 2026-06-25: Started `02` implementation of `minimal asset candidate brief gate` after `03` PASS for asset candidate preflight REVISE P1; current project completion progress from `01` is `58%`.
- 2026-06-25: Evidence pass found `AssetCandidatePreflightReport` already carries the candidate requirements and source fields, so this slice keeps the CLI to a single read-only preflight input.
- 2026-06-25: Implemented `AssetCandidateBrief` models, `create-asset-candidate-brief` CLI, pure brief service, and focused tests; verification passed with `AssetCandidateBriefTest`.
- 2026-06-25: Diff checks passed with only Windows line-ending warnings; untracked service/test files were additionally checked via `git diff --no-index --check`.
- 2026-06-25: Sent read-only review handoff to `03` for PASS / REVISE / FAIL reporting back to `01`.

## Active Minimal Asset Candidate Preflight Gate Plan

### Goal

Add a local read-only JSON preflight gate after a PASS human review decision intake report allows `asset_candidate_preflight`. This confirms the reviewed storyboard packet may enter asset candidate preparation without approving assets, locking production, generating images/video, applying canon, or creating runtime scenes.

### Global Strategy

- Keep this slice Kotlin/JVM CLI plus JSON file protocol only.
- Read `--human-review-decision-intake` and, based on evidence, a read-only `--storyboard-packet-review` input because asset candidate requirements live on `StoryboardPacketReviewPacket`, not on the decision intake report.
- Add optional source fields to `StoryboardPacketHumanReviewDecisionIntakeReport` so the asset preflight can compare review file/id, draft/report/id, accepted script packet id, script packet draft file, and SHA-256 deterministically.
- Require intake verdict `PASS`, `decisionAllowedDownstream=true`, `decisionStatus=ALLOW_ASSET_CANDIDATE_PREFLIGHT`, and `nextAllowedDownstreamStep=asset_candidate_preflight`.
- Require asset candidate requirements to remain candidate/requirement/reference-needed only; forbid approved, locked, final, production-ready, canon, generated media, and runtime completion claims.
- PASS should authorize only `asset_candidate_brief`, the narrowest local next step before drafting any candidate packet or producing images. FAIL should leave no downstream step.

### Non-Goals

- No asset approval, asset lock, production lock, canon application, storyboard image generation, asset image generation, video generation, voice generation, script rewriting, runtime scene creation, or external AI call.
- No SQLite write, schema migration, server/port, browser control, QuarterFull work, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/StoryboardPacketHumanReviewDecisionIntakeService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/AssetCandidatePreflightService.kt`
- `app/src/test/kotlin/com/localnovel/engine/AssetCandidatePreflightTest.kt`
- Existing decision intake focused test may be rerun because the report model is being extended additively.

### Confirmed Facts

- `01` assigned this slice after `03` passed the human review decision intake REVISE P1 fix.
- `Project completion progress` reported by `01` is currently `56%`.
- `StoryboardPacketHumanReviewDecisionIntakeService` currently emits `nextAllowedDownstreamStep=asset_candidate_preflight` only for valid `ALLOW_ASSET_CANDIDATE_PREFLIGHT`.
- `StoryboardPacketHumanReviewDecisionIntakeReport` currently lacks storyboard packet review file, accepted script packet id, script packet draft file, and script packet draft SHA-256 fields.
- `StoryboardPacketReviewPacket` contains `assetCandidateRequirements`, source draft/report/id fields, accepted script packet id, script packet draft file, and script packet draft SHA-256.
- `docs/STORYBOARD_AND_CAMERA_PLANNING.md` says after explicit approval 01 may assign 07 to prepare asset candidates needed for storyboard.
- `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md` says 07 output remains candidate material and is not automatically approved reference or production lock.
- The assignment forbids DB writes, server/port, external AI API, image/video generation, asset approval, canon, and runtime path changes.

### Assumptions

- `asset_candidate_brief` is safer than `asset_candidate_draft` because it keeps this slice one step before drafting any candidate packet or image-facing material.
- Adding optional source fields to the decision intake report is the smallest safe way to make the new preflight deterministic without re-reading the original human decision JSON.

### Open Questions

- Future asset candidate brief shape can decide how candidate requirements become a structured candidate planning packet.

### Implementation Steps

- [x] Step 1: Record this minimal asset candidate preflight plan in `PLANS.md`.
- [x] Step 2: Add additive decision intake source fields and new asset candidate preflight report models.
- [x] Step 3: Add pure preflight service, CLI wiring, and focused tests.
- [x] Step 4: Run focused tests and `git diff --check`.
- [x] Step 5: Notify `03` thread with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.
- [x] Step 6: REVISE P1: validate direct storyboard packet review state before asset candidate preflight can pass.

### Verification

- Step 3: Unit tests should prove PASS decision intake plus matching review packet passes and emits `nextAllowedDownstreamStep=asset_candidate_brief`.
- Step 3: Unit tests should prove non-PASS intake, `decisionAllowedDownstream=false`, wrong decision status, or wrong next step fails.
- Step 3: Unit tests should prove source binding missing or mismatch fails.
- Step 3: Unit tests should prove missing asset candidate requirements fail.
- Step 3: Unit tests should prove forbidden approval / lock / canon / generated media claims fail.
- Step 3: CLI smoke should write JSON without `--db`.
- Step 4: Run `.\gradlew.bat test --tests "*AssetCandidatePreflightTest" --no-daemon`.
- Step 4: Run `.\gradlew.bat test --tests "*StoryboardPacketHumanReviewDecisionIntakeTest" --no-daemon`.
- Step 4: Run `git diff --check` on changed files for this slice.
- Step 6: Unit tests should prove matching source fields still fail when review verdict is `FAIL`, reviewStatus is not `REVIEW_READY`, or review next step is not `storyboard_packet_human_review`.
- Step 6: Run `.\gradlew.bat test --tests "*AssetCandidatePreflightTest" --no-daemon`.
- Step 6: Run `git diff --check` on changed files for this REVISE.

### Risks

- Accidentally treating asset candidate preflight as asset approval.
- Under-binding the review packet and letting requirements from the wrong storyboard packet through.
- Over-scanning candidate wording and blocking legitimate "candidate only" language.

### Drift Log

- 2026-06-25: Started `02` implementation of `minimal asset candidate preflight gate` after `03` PASS for human review decision intake REVISE P1; current project completion progress from `01` is `56%`.
- 2026-06-25: Evidence pass found asset candidate requirements live on `StoryboardPacketReviewPacket`, so this slice will add a read-only `--storyboard-packet-review` input and additive source fields on the decision intake report for deterministic binding.
- 2026-06-25: Implemented additive decision intake source fields, `AssetCandidatePreflightReport` models, `validate-asset-candidate-preflight` CLI, pure asset candidate preflight service, and focused tests.
- 2026-06-25: Verification passed with `AssetCandidatePreflightTest`, `StoryboardPacketHumanReviewDecisionIntakeTest`, and diff checks showing only Windows line-ending warnings.
- 2026-06-25: Tightened asset preflight source binding to compare storyboard draft file/report/id and planning brief id in addition to review id, accepted script packet id, script packet draft file, and SHA-256.
- 2026-06-25: Sent read-only review handoff to `03` for PASS / REVISE / FAIL reporting back to `01`.
- 2026-06-25: `03` returned REVISE P1: direct `StoryboardPacketReviewPacket` input can match source fields while its own verdict/status/next-step state is invalid. Current repair scope is limited to adding review packet state guards and regression tests.
- 2026-06-25: REVISE P1 fixed by requiring direct review packet `verdict=PASS`, `reviewStatus=REVIEW_READY`, and `nextAllowedDownstreamStep=storyboard_packet_human_review`; focused asset candidate preflight tests passed.

## Active Minimal Storyboard Packet Human Review Decision Intake Gate Plan

### Goal

Add a local read-only JSON decision intake gate after a PASS storyboard packet human review preflight. It validates an externally supplied human / controller storyboard packet review decision JSON for format, source binding, checklist completion, unresolved blockers, and downstream safety without automatically approving assets, canon, production locks, images, or video.

### Global Strategy

- Keep this slice Kotlin/JVM CLI plus JSON file protocol only.
- Read only `--human-review-preflight` and `--human-review-decision`; write an optional JSON intake report via `--out`.
- Require human review preflight verdict `PASS` and `nextAllowedDownstreamStep == "storyboard_packet_human_review_ready"`.
- Additive source fields are needed on `StoryboardPacketHumanReviewPreflightReport` because current reports only carry review id/file plus counts; decision intake needs deterministic comparison for draft/report/hash fields.
- Decision source must be explicitly human/controller/user sourced, not automatic/model/generated.
- Decision status vocabulary is narrow: `ALLOW_ASSET_CANDIDATE_PREFLIGHT`, `REQUEST_CHANGES`, `REJECT`.
- `ALLOW_ASSET_CANDIDATE_PREFLIGHT` means only "may enter asset candidate preflight"; it does not mean asset approval, production lock, canon, image generation, or video generation.
- If allow-downstream is requested, all human checklist items must be positive and no unresolved blocking issue may remain.
- PASS plus allow-downstream should authorize only `asset_candidate_preflight`; valid request-changes / reject decisions produce no downstream step.

### Non-Goals

- No automatic human decision, user approval, asset approval, production lock, canon application, storyboard image generation, video generation, voice generation, script rewriting, runtime scene creation, or setting-pack canon application.
- No external AI API, model install, web server, port, browser control, SQLite write, schema migration, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/StoryboardPacketHumanReviewPreflightService.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/StoryboardPacketHumanReviewDecisionIntakeService.kt`
- `app/src/test/kotlin/com/localnovel/engine/StoryboardPacketHumanReviewDecisionIntakeTest.kt`

### Confirmed Facts

- `01` assigned this exact next slice to `02` after `03` passed the storyboard packet human review preflight REVISE P1 fix.
- `Project completion progress` reported by `01` is currently `54%`.
- Existing `StoryboardPacketHumanReviewPreflightService` emits `nextAllowedDownstreamStep = "storyboard_packet_human_review_ready"` on PASS.
- Existing `StoryboardPacketHumanReviewPreflightReport` carries verdict, preflight id, review packet file/id/status, next step, findings, and evidence, but not the source draft/report/hash fields requested for deterministic decision binding.
- Existing docs state the path after explicit review / approval can move toward asset candidates, while asset candidates remain candidate material and are not approved references.
- The assignment requires decision intake only and explicitly forbids image/video production, script modification, SQLite writes, server/port, external AI API, QuarterFull/browser work, runtime write-path changes, and approval/canon/lock claims.

### Assumptions

- Adding optional source fields to the existing preflight report model is the smallest safe way to make the new decision intake gate deterministic.
- `ALLOW_ASSET_CANDIDATE_PREFLIGHT` is clearer than a generic `APPROVED` status because it names the narrow allowed next step and avoids asset/canon/production approval wording.
- A syntactically valid `REQUEST_CHANGES` or `REJECT` decision should produce a PASS intake report with no downstream step, because the decision itself is valid but does not authorize progress.

### Open Questions

- Future asset candidate preflight can define its own packet shape and whether it needs the human decision intake report as input.

### Implementation Steps

- [x] Step 1: Record this minimal storyboard packet human review decision intake gate plan in `PLANS.md`.
- [x] Step 2: Add decision / intake report models and additive source fields to human review preflight report.
- [x] Step 3: Add pure decision intake service, CLI wiring, and focused tests.
- [x] Step 4: Run focused tests and `git diff --check`.
- [x] Step 5: Notify `03` thread with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.
- [x] Step 6: REVISE P1: require allow-downstream checklist coverage for scene lock, continuity, knowledge boundary, asset candidates/assets, and downstream boundary.

### Verification

- Step 3: Unit tests should prove PASS preflight plus `ALLOW_ASSET_CANDIDATE_PREFLIGHT` human decision passes and emits `nextAllowedDownstreamStep=asset_candidate_preflight`.
- Step 3: Unit tests should prove non-PASS preflight or wrong preflight next step fails.
- Step 3: Unit tests should prove `auto`, `model`, generated, or blank decision source fails.
- Step 3: Unit tests should prove source binding missing or mismatch fails.
- Step 3: Unit tests should prove unknown decision status fails.
- Step 3: Unit tests should prove allow-downstream with failed checklist or unresolved blocking issue fails.
- Step 3: Unit tests should prove `REQUEST_CHANGES` / `REJECT` are valid but do not emit a downstream step.
- Step 3: Unit tests should prove forbidden approval / canon / generated media claims fail.
- Step 3: CLI smoke should write JSON without `--db`.
- Step 4: Run `.\gradlew.bat test --tests "*StoryboardPacketHumanReviewDecisionIntakeTest" --no-daemon`.
- Step 4: Run `git diff --check` on changed files for this slice.
- Step 6: Unit tests should prove `ALLOW_ASSET_CANDIDATE_PREFLIGHT` with only `downstream_boundary=PASS` fails and does not emit `asset_candidate_preflight`.
- Step 6: Run `.\gradlew.bat test --tests "*StoryboardPacketHumanReviewDecisionIntakeTest" --no-daemon`.
- Step 6: Run `git diff --check` on changed files for this REVISE.

### Risks

- Accidentally treating a human decision intake as automatic approval.
- Under-specifying source fields and allowing a decision JSON to bind to the wrong review packet.
- Letting "allow asset candidate preflight" wording drift into asset approval or production lock semantics.

### Drift Log

- 2026-06-25: Started `02` implementation of `minimal storyboard packet human review decision intake gate` after `03` PASS for human review preflight REVISE P1; current project completion progress from `01` is `54%`.
- 2026-06-25: Evidence pass found existing preflight reports lacked the source draft/report/hash fields needed for deterministic decision binding, so this slice will add optional source fields to the preflight report model and populate them from the review packet.
- 2026-06-25: Implemented additive preflight source fields, decision/intake report models, `validate-storyboard-packet-human-review-decision` CLI, deterministic human-source / source-binding / checklist / blocking-issue / forbidden-claim validation, and focused tests.
- 2026-06-25: Verification passed with focused decision intake tests, existing human review preflight tests, and `git diff --check` showing only existing Windows line-ending warnings.
- 2026-06-25: Sent read-only review handoff to `03` for PASS / REVISE / FAIL reporting back to `01`.
- 2026-06-25: `03` returned REVISE P1: allow-downstream checklist completion could be bypassed by a short decision JSON containing only positive provided items. Current repair scope is limited to required checklist category coverage.
- 2026-06-25: REVISE P1 fixed by requiring accepted checklist coverage for `scene_lock`, `continuity`, `knowledge_boundary`, `downstream_boundary`, and either `asset_candidates` or `assets`; focused decision intake tests passed.
- 2026-06-25: REVISE P1 diff checks passed with only Windows line-ending warnings; untracked service/test files were additionally checked via `git diff --no-index --check`.

## Active Minimal Storyboard Packet Human Review Preflight Plan

### Goal

Add a local read-only JSON preflight after a PASS storyboard packet review packet. It validates that a review-ready storyboard packet can be handed to human / controller review without claiming approval, asset approval, production lock, canon, image generation, video generation, or runtime completion.

### Global Strategy

- Keep this slice Kotlin/JVM CLI plus JSON file protocol only.
- Read only `--storyboard-packet-review`; write an optional JSON preflight report via `--out`.
- Require review packet verdict `PASS`, reviewStatus `REVIEW_READY`, and `nextAllowedDownstreamStep == "storyboard_packet_human_review"`.
- Require nonblank review id, source draft file, source validation report file, draft id, planning brief id, accepted script packet id, script packet draft file, script packet draft SHA-256, and validation source report links.
- Require review checklist coverage for scene lock, continuity anchors, secrecy / knowledge boundary, asset candidates, and downstream boundary.
- Require frame count > 0, nonblank unique frame ids, protected continuity anchors summary, and remaining residual risks.
- Scan review packet text surfaces for forbidden approval / lock / canon / generated media / runtime claims.
- PASS should authorize only `storyboard_packet_human_review_ready`; FAIL should leave no downstream step.

### Non-Goals

- No human review decision, user approval, asset approval, production lock, canon application, storyboard image generation, video generation, voice generation, script rewriting, runtime scene creation, or setting-pack canon application.
- No external AI API, model install, web server, port, browser control, SQLite write, schema migration, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/StoryboardPacketHumanReviewPreflightService.kt`
- `app/src/test/kotlin/com/localnovel/engine/StoryboardPacketHumanReviewPreflightTest.kt`

### Confirmed Facts

- `01` assigned this exact next slice to `02` after `03` passed the storyboard packet review gate REVISE P1 fix.
- `Project completion progress` reported by `01` is currently `52%`.
- Existing `StoryboardPacketReviewService` emits `nextAllowedDownstreamStep = "storyboard_packet_human_review"` on PASS.
- Existing `StoryboardPacketReviewPacket` carries verdict, review id/status, source files, draft id, planning brief id, accepted script packet id, script packet draft file/hash, source report link, frame count/ids, protected continuity anchors, asset requirements, residual risks, review checklist, findings, and evidence.
- Existing review checklist categories include scene lock, continuity, knowledge boundary, assets, and downstream boundary.
- The assignment requires preflight/report only and explicitly forbids image/video production, script modification, SQLite writes, server/port, external AI API, QuarterFull/browser work, runtime write-path changes, and approval/canon/lock claims.

### Assumptions

- `storyboard_packet_human_review_ready` is the safest next step because it means waiting for human review rather than completing human review.
- Review packet text surfaces are sufficient for this first preflight because the review packet intentionally does not expose image/video output path or generation API fields.

### Open Questions

- Future human review slice can define the explicit human decision JSON and decide whether a PASS human decision moves to asset candidate preflight.

### Implementation Steps

- [x] Step 1: Record this minimal storyboard packet human review preflight plan in `PLANS.md`.
- [x] Step 2: Add human review preflight report models, pure preflight service, CLI wiring, and focused tests.
- [x] Step 3: Run focused tests and `git diff --check`.
- [x] Step 4: Notify `03` thread with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.
- [x] Step 5: REVISE P1 fix: require source draft validation verdict PASS and source draft validation next step `storyboard_packet_review`.
- [x] Step 6: Add regression tests for source validation FAIL/unknown verdict and blank/wrong source next step.
- [x] Step 7: Rerun focused tests and `git diff --check`, then notify `03` for read-only review.

### Verification

- Step 2: Unit tests should prove complete REVIEW_READY packet passes and emits `nextAllowedDownstreamStep=storyboard_packet_human_review_ready`.
- Step 2: Unit tests should prove review packet FAIL / non-REVIEW_READY fails.
- Step 2: Unit tests should prove wrong review packet next step fails.
- Step 2: Unit tests should prove missing source binding / review id / source report path fails.
- Step 2: Unit tests should prove missing checklist categories fail.
- Step 2: Unit tests should prove blank or duplicate frame ids fail.
- Step 2: Unit tests should prove forbidden approval / canon / generated media claims fail.
- Step 2: CLI smoke should write JSON without `--db`.
- Step 3: Run `.\gradlew.bat test --tests "*StoryboardPacketHumanReviewPreflightTest" --no-daemon`.
- Step 3: Run `git diff --check` on changed files for this slice.
- Step 7: Rerun `.\gradlew.bat test --tests "*StoryboardPacketHumanReviewPreflightTest" --no-daemon`.
- Step 7: Rerun `git diff --check` on changed files for this REVISE fix.

### Risks

- Accidentally treating preflight as human review approval.
- Under-scanning review packet text surfaces and allowing approval/canon/lock claims through.
- Letting incomplete source links or duplicate frame ids pass into human review.

### Drift Log

- 2026-06-25: Started `02` implementation of `minimal storyboard packet human review preflight` after `03` PASS for storyboard packet review gate REVISE P1; current project completion progress from `01` is `52%`.
- 2026-06-25: Evidence pass confirmed this slice only needs the review packet JSON and should choose conservative downstream `storyboard_packet_human_review_ready`.
- 2026-06-25: Added human review preflight report models, pure preflight service, `validate-storyboard-packet-human-review-preflight` CLI wiring, and focused tests covering PASS, upstream FAIL/non-ready, wrong next step, missing source fields, checklist category gaps, blank/duplicate frame ids, forbidden claims, and CLI output.
- 2026-06-25: Verification passed: `.\gradlew.bat test --tests "*StoryboardPacketHumanReviewPreflightTest" --no-daemon`; `git diff --check` on this slice reported only Windows line-ending warnings.
- 2026-06-25: Notified `03` for read-only review with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.
- 2026-06-25: `03` returned REVISE P1: human review preflight must require source draft validation verdict PASS and source draft validation next step `storyboard_packet_review`.
- 2026-06-25: REVISE fix added source draft validation verdict checks for missing, unknown, and non-PASS values, plus source draft validation next-step missing/mismatch checks.
- 2026-06-25: REVISE tests added coverage for source validation verdict `FAIL`, unknown verdict, blank next step, and wrong next step.
- 2026-06-25: REVISE verification passed: `.\gradlew.bat test --tests "*StoryboardPacketHumanReviewPreflightTest" --no-daemon`; `git diff --check` on this REVISE slice reported only the existing `PLANS.md` Windows line-ending warning.
- 2026-06-25: Notified `03` for read-only review of the REVISE P1 fix with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.

## Active Minimal Storyboard Packet Review Gate Plan

### Goal

Add a local read-only JSON review-ready gate after a PASS storyboard packet draft validation report. It packages a text-only storyboard packet draft and its validation report into a deterministic review packet for later human / controller review without approving assets, locking production, creating canon, generating images/video, or changing script content.

### Global Strategy

- Keep this slice Kotlin/JVM CLI plus JSON file protocol only.
- Read only `--storyboard-packet-draft` and `--storyboard-packet-draft-report`; write an optional JSON review packet via `--out`.
- Require storyboard packet draft validation report verdict `PASS` and `nextAllowedDownstreamStep == "storyboard_packet_review"`.
- Deterministically bind draft and report by draft file, draft id, planning brief id, accepted script packet id, script packet draft file, and script packet draft SHA-256.
- Preserve review-ready evidence: frame count, frame ids, source report links, protected continuity anchor summary, asset candidate requirements, residual risks, and review checklist.
- Review status must be only `REVIEW_READY` or `FAIL`; never user-approved, asset-approved, locked, final, production-ready, runtime, or canon.
- Keep forbidden claim scanning and field-level guards for image/video output path, generation API, and asset approval status.
- PASS should authorize only `storyboard_packet_human_review`; FAIL should leave no downstream step.

### Non-Goals

- No storyboard image generation, video generation, voice generation, script rewriting, asset approval, user approval, production lock, runtime scene creation, setting-pack canon application, or automatic human review.
- No external AI API, model install, web server, port, browser control, SQLite write, schema migration, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/StoryboardPacketReviewService.kt`
- `app/src/test/kotlin/com/localnovel/engine/StoryboardPacketReviewTest.kt`

### Confirmed Facts

- `01` assigned this exact next slice to `02` after `03` passed the storyboard packet draft gate REVISE P1 fix.
- `Project completion progress` reported by `01` is currently `50%`.
- Existing `StoryboardPacketDraftValidationService` emits `nextAllowedDownstreamStep = "storyboard_packet_review"` on PASS.
- Existing `StoryboardPacketDraftValidationReport` carries draft/report file links, draft id, planning brief id, accepted script packet id, script packet draft file/hash, verdict, findings, and evidence counts.
- Existing `StoryboardPacketDraft` carries frame entries, protected continuity anchors, asset candidates, review notes, image/video output path fields, generation API fields, and asset approval status fields.
- `docs/STORYBOARD_AND_CAMERA_PLANNING.md` recommends text frames -> review scene lock / blocking / secrecy boundaries -> asset candidates -> image pickup.
- The assignment requires validation/report packaging only and explicitly forbids image/video production, script modification, SQLite writes, server/port, external AI API, QuarterFull/browser work, runtime write-path changes, and approval/canon/lock claims.

### Assumptions

- `storyboard_packet_human_review` is safer than `asset_candidate_preflight` because this deterministic review packet is not a human approval.
- A review packet can include deterministic residual risks such as "human review not complete" because the input draft/report do not carry a separate residual risk list.
- Source report links for this slice are the draft file and the draft validation report file; deeper upstream reports remain represented through the draft/report source fields already present.

### Open Questions

- A future human review slice can decide whether PASS moves to `asset_candidate_preflight` or another stricter review handoff.

### Implementation Steps

- [x] Step 1: Record this minimal storyboard packet review gate plan in `PLANS.md`.
- [x] Step 2: Add storyboard packet review packet models, pure review service, CLI wiring, and focused tests.
- [x] Step 3: Run focused tests and `git diff --check`.
- [x] Step 4: Notify `03` thread with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.
- [x] Step 5: REVISE P1 fix: require validation report `storyboardPacketDraftFile` to be nonblank before source comparison.
- [x] Step 6: Add regression tests for blank and mismatched validation report draft file.
- [x] Step 7: Rerun focused tests and `git diff --check`, then notify `03` for read-only review.

### Verification

- Step 2: Unit tests should prove complete PASS draft + PASS validation report produces `REVIEW_READY` review packet and does not claim approval / canon / lock.
- Step 2: Unit tests should prove non-PASS validation report fails.
- Step 2: Unit tests should prove wrong validation report next step fails.
- Step 2: Unit tests should prove source binding missing or mismatch fails.
- Step 2: Unit tests should prove forbidden downstream completion claims in draft/report text fail.
- Step 2: Unit tests should prove image/video output path, generation API, and approved asset status short values fail.
- Step 2: CLI smoke should write JSON without `--db`.
- Step 3: Run `.\gradlew.bat test --tests "*StoryboardPacketReviewTest" --no-daemon`.
- Step 3: Run `git diff --check` on changed files for this slice.
- Step 7: Rerun `.\gradlew.bat test --tests "*StoryboardPacketReviewTest" --no-daemon`.
- Step 7: Rerun `git diff --check` on changed files for this REVISE fix.

### Risks

- Accidentally treating a deterministic review-ready packet as user approval, asset approval, production lock, final output, canon, or runtime evidence.
- Under-scanning text surfaces from draft/report and allowing forbidden completion claims to pass.
- Letting report/draft source mismatch pass and breaking handoff traceability.

### Drift Log

- 2026-06-25: Started `02` implementation of the `minimal storyboard packet review gate` after `03` PASS for storyboard packet draft gate REVISE P1; current project completion progress from `01` is `50%`.
- 2026-06-25: Evidence pass found existing `storyboard_packet_review` output from draft validation and docs recommending text frame review before asset candidates. This slice therefore chooses conservative downstream `storyboard_packet_human_review`.
- 2026-06-25: Added storyboard packet review packet models, source binding and boundary review service, `create-storyboard-packet-review` CLI wiring, and focused tests covering PASS review-ready output, non-PASS report, wrong report next step, source binding missing/mismatch, forbidden downstream claims, short output/API/approval values, and CLI output.
- 2026-06-25: Verification passed: `.\gradlew.bat test --tests "*StoryboardPacketReviewTest" --no-daemon`; `git diff --check` on this slice reported only Windows line-ending warnings.
- 2026-06-25: Notified `03` for read-only review with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.
- 2026-06-25: `03` returned REVISE P1: validation report `storyboardPacketDraftFile` must fail when blank; mismatched report draft file must still fail.
- 2026-06-25: REVISE fix added deterministic missing check `DRAFT_REPORT_DRAFT_FILE_MISSING` for validation report `storyboardPacketDraftFile`, while preserving mismatch detection for nonmatching draft files.
- 2026-06-25: REVISE tests added blank report draft file and mismatched report draft file coverage.
- 2026-06-25: REVISE verification passed: `.\gradlew.bat test --tests "*StoryboardPacketReviewTest" --no-daemon`; `git diff --check` on this REVISE slice reported only the existing `PLANS.md` Windows line-ending warning.
- 2026-06-25: Notified `03` for read-only review of the REVISE P1 fix with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.

## Active Minimal Storyboard Packet Draft Gate Plan

### Goal

Add a local read-only JSON validation gate for a `storyboard_packet_draft` after a PASS storyboard planning brief. It validates a text-only storyboard packet draft against the planning brief and emits a validation report without creating images, video, approved assets, canon, runtime scenes, or script changes.

### Global Strategy

- Keep this slice Kotlin/JVM CLI plus JSON file protocol only.
- Read only `--storyboard-planning-brief` and `--storyboard-packet-draft`; write an optional JSON validation report via `--out`.
- Require storyboard planning brief verdict `PASS` and `nextAllowedDownstreamStep == "storyboard_packet_draft"`.
- Require source binding from the draft back to the planning brief: brief id, accepted script packet id, script packet draft file, and script packet draft SHA-256 must be nonblank and match.
- Require text-only frame entries with frame id, scene/beat reference, visual or camera intent, and must-show or do-not-show boundary.
- Preserve protected continuity anchors with CJK-aware matching so Traditional Chinese anchors cannot false-pass when omitted.
- Block downstream completion claims, image/video output paths, external generation API details, asset approval claims, canon claims, and runtime scene claims.
- PASS should authorize only `storyboard_packet_review`; FAIL should leave no downstream step.

### Non-Goals

- No storyboard image generation, video generation, voice generation, script rewriting, asset approval, runtime scene creation, setting-pack canon application, or automatic storyboard packet creation.
- No external AI API, model install, web server, port, browser control, SQLite write, schema migration, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/StoryboardPacketDraftValidationService.kt`
- `app/src/test/kotlin/com/localnovel/engine/StoryboardPacketDraftValidationTest.kt`

### Confirmed Facts

- `01` assigned this exact next slice to `02` after `03` passed the storyboard planning brief REVISE P1 fix.
- `Project completion progress` reported by `01` is currently `48%`.
- Existing `StoryboardPlanningBrief` carries verdict, brief id, accepted script packet id, script packet draft file/hash, protected continuity anchors summary, must-show / must-not-show boundaries, asset carry-forward policy, review checklist, and `nextAllowedDownstreamStep`.
- Existing `StoryboardPlanningBriefService` emits `nextAllowedDownstreamStep = "storyboard_packet_draft"` on PASS.
- `docs/STORYBOARD_AND_CAMERA_PLANNING.md` says text storyboard frames come before review of scene lock, blocking, and secrecy boundaries, and before asset candidates / image pickup.
- The assignment requires validation/report only and explicitly forbids image/video production, script modification, SQLite writes, server/port, external AI API, QuarterFull/browser work, and runtime write-path changes.

### Assumptions

- `storyboard_packet_review` is the safest next step because docs require text storyboard frame review before asset candidates and image pickup.
- A minimal draft schema can be introduced locally for this gate because no implemented storyboard packet draft model exists yet.
- External generation API details can be detected conservatively through obvious provider/API/key/endpoint fields or text patterns in this first slice.

### Open Questions

- Future slices can decide whether a reviewed storyboard packet should next enter `asset_candidate_preflight` or a stricter storyboard review handoff packet.

### Implementation Steps

- [x] Step 1: Record this minimal storyboard packet draft gate plan in `PLANS.md`.
- [x] Step 2: Add storyboard packet draft/report models, pure validation service, CLI wiring, and focused tests.
- [x] Step 3: Run focused tests and `git diff --check`.
- [x] Step 4: Notify `03` thread with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.
- [x] Step 5: REVISE P1 fix: add deterministic field-level guards for image/video output path, generation API, and asset approval status fields.
- [x] Step 6: Add regression tests for short field values that bypass regex scanning.
- [x] Step 7: Rerun focused tests and `git diff --check`, then notify `03` for read-only review.

### Verification

- Step 2: Unit tests should prove complete PASS planning brief plus text-only storyboard packet draft produces PASS and `nextAllowedDownstreamStep=storyboard_packet_review`.
- Step 2: Unit tests should prove non-PASS planning brief fails.
- Step 2: Unit tests should prove wrong planning brief next step fails.
- Step 2: Unit tests should prove source binding missing or mismatch fails.
- Step 2: Unit tests should prove pure Traditional Chinese protected anchor omission fails and reasonable reference passes.
- Step 2: Unit tests should prove forbidden downstream completion claims in frame / asset / review surfaces fail.
- Step 2: Unit tests should prove image/video output paths and generated asset approval claims fail.
- Step 2: CLI smoke should write JSON without `--db`.
- Step 3: Run `.\gradlew.bat test --tests "*StoryboardPacketDraftValidationTest" --no-daemon`.
- Step 3: Run `git diff --check` on changed files for this slice.
- Step 7: Rerun `.\gradlew.bat test --tests "*StoryboardPacketDraftValidationTest" --no-daemon`.
- Step 7: Rerun `git diff --check` on changed files for this REVISE fix.

### Risks

- Accidentally treating a text storyboard packet draft as approved storyboard, approved asset, final image, final video, canon, or runtime scene.
- Under-scanning nested draft text fields and allowing forbidden completion claims to pass.
- False-passing CJK protected continuity anchors if matching only uses Latin tokens.

### Drift Log

- 2026-06-25: Started `02` implementation of the `minimal storyboard packet draft gate` after `03` PASS for the storyboard planning brief REVISE P1 fix; current project completion progress from `01` is `48%`.
- 2026-06-25: Evidence pass found no existing storyboard packet draft implementation, and `docs/STORYBOARD_AND_CAMERA_PLANNING.md` recommends text frames -> review -> asset candidates -> image pickup. This slice therefore chooses conservative downstream `storyboard_packet_review`.
- 2026-06-25: Added storyboard packet draft models, validation report models, `validate-storyboard-packet-draft` CLI wiring, and focused tests covering PASS, upstream FAIL/wrong step, source binding missing/mismatch, CJK protected anchor omission, forbidden completion claims, media output paths, external generation API hints, and CLI output.
- 2026-06-25: Verification passed: `.\gradlew.bat test --tests "*StoryboardPacketDraftValidationTest" --no-daemon`; `git diff --check` on this slice reported only Windows line-ending warnings.
- 2026-06-25: Notified `03` for read-only review with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.
- 2026-06-25: `03` returned REVISE P1: explicit fields `imageOutputPath`, `videoOutputPath`, `generationApi`, `assetApprovalStatus`, and `assetCandidates[].approvalStatus` need deterministic guards because short values can bypass regex scanning.
- 2026-06-25: REVISE fix added field-level guards: nonblank image/video output paths fail, nonblank generation API fails, and asset approval status only allows blank or `candidate_only`.
- 2026-06-25: REVISE tests added short-value coverage for asset candidate approval `approved`, frame asset approval `approved`, image output `frame-001`, video output `shot-001`, and generation API `manual-provider-job`.
- 2026-06-25: REVISE verification passed: `.\gradlew.bat test --tests "*StoryboardPacketDraftValidationTest" --no-daemon`; `git diff --check` on this REVISE slice reported only the existing `PLANS.md` Windows line-ending warning.
- 2026-06-25: Notified `03` for read-only review of the REVISE P1 fix with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.

## Active Minimal Storyboard Planning Brief Gate Plan

### Goal

Add a local read-only JSON gate after storyboard planning preflight PASS. It creates / validates a deterministic storyboard planning brief from the accepted script packet handoff packet and storyboard planning preflight report without creating storyboard packets, images, videos, or modifying script content.

### Global Strategy

- Keep this slice Kotlin/JVM CLI plus JSON file protocol only.
- Read only `--accepted-script-packet` and `--storyboard-planning-preflight`; write an optional JSON brief via `--out`.
- Require accepted script packet verdict `PASS`, storyboard planning preflight verdict `PASS`, and preflight `nextAllowedDownstreamStep == "storyboard_planning_brief"`.
- Preserve source report links, script packet draft path/hash, protected continuity anchors summary, and remaining residual risks from the accepted script packet.
- Produce deterministic planning fields for the next step: scene/beat planning summary, frame planning constraints, must-show / must-not-show boundaries, asset needs carried forward, and review checklist.
- Block any text carried into the brief if it claims storyboard/image/video/runtime/canon/asset approval completion.
- PASS should authorize only `storyboard_packet_draft`; FAIL should leave no downstream step.
- Notify `03` for read-only review after implementation and verification, with PASS / REVISE / FAIL requested back to `01`.

### Non-Goals

- No storyboard packet drafting, storyboard frame creation, image generation, video generation, voice generation, script rewriting, asset approval, runtime scene creation, or setting-pack canon application.
- No external AI API, model install, web server, port, browser control, SQLite write, schema migration, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/StoryboardPlanningBriefService.kt`
- `app/src/test/kotlin/com/localnovel/engine/StoryboardPlanningBriefTest.kt`

### Confirmed Facts

- `01` assigned this exact next slice to `02` after `03` passed the storyboard planning preflight slice.
- `Project completion progress` reported by `01` is currently `46%`.
- Existing `AcceptedScriptPacketHandoffPacket` carries verdict, packet id, script packet draft file/hash, source reports, protected continuity anchors summary, remaining residual risks, and downstream step.
- Existing `StoryboardPlanningPreflightReport` carries verdict, accepted script packet id/file, evidence counts, blocked reason/findings, and `nextAllowedDownstreamStep`.
- The assignment requires this slice to read only accepted script packet handoff packet JSON and storyboard planning preflight report JSON.
- The assignment explicitly forbids storyboard packet creation, image/video generation, script modification, SQLite writes, server/port, external AI API, and runtime write-path changes.

### Assumptions

- Because this slice cannot read the script packet draft JSON, scene/beat planning summary should be a deterministic handoff summary based on the script packet draft path/hash and preflight/packet metadata.
- Asset needs carried forward can be represented as a source pointer and review policy rather than extracted asset rows, because extracting real asset needs would require reading the script packet draft.
- Forbidden completion claim scanning should cover accepted packet strings and preflight strings before they are carried into the brief.

### Open Questions

- Future storyboard packet draft schema can decide whether to read the script packet draft JSON and expand real beat/frame rows.

### Implementation Steps

- [x] Step 1: Record this minimal storyboard planning brief gate plan in `PLANS.md`.
- [x] Step 2: Add storyboard planning brief models, pure brief service, CLI wiring, and focused tests.
- [x] Step 3: Run focused tests and `git diff --check`.
- [x] Step 4: Notify `03` thread with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.
- [x] Step 5: REVISE fix: require accepted script packet `packetId` and storyboard planning preflight `acceptedScriptPacketId` to both be nonblank and exact-match.
- [x] Step 6: Add regression tests for missing accepted packet id, missing preflight accepted packet id, and mismatched ids.
- [x] Step 7: Rerun focused tests and `git diff --check`, then notify `03` for read-only review.

### Verification

- Step 2: Unit tests should prove complete PASS preflight + accepted packet produces PASS brief and `nextAllowedDownstreamStep=storyboard_packet_draft`.
- Step 2: Unit tests should prove non-PASS accepted packet fails.
- Step 2: Unit tests should prove non-PASS preflight or wrong preflight downstream step fails.
- Step 2: Unit tests should prove missing source links, hash, anchors, or residual risks fail.
- Step 2: Unit tests should prove forbidden downstream completion claims carried by accepted packet / preflight text fail.
- Step 2: CLI smoke should write a JSON brief without requiring `--db`.
- Step 3: Run `.\gradlew.bat test --tests "*StoryboardPlanningBriefTest" --no-daemon`.
- Step 3: Run `git diff --check` on changed files for this slice.
- Step 7: Rerun `.\gradlew.bat test --tests "*StoryboardPlanningBriefTest" --no-daemon`.
- Step 7: Rerun `git diff --check` on `PLANS.md`, `StoryboardPlanningBriefService.kt`, and `StoryboardPlanningBriefTest.kt`.

### Risks

- Accidentally implying storyboard frames exist when only a planning brief exists.
- Under-specifying future asset needs because this slice intentionally does not read the script packet draft JSON.
- Duplicating preflight validation instead of checking the preflight report as an upstream gate.

### Drift Log

- 2026-06-25: Started `02` implementation of the `minimal storyboard planning brief gate` handoff from `01`; current project completion progress from `01` is `46%`.
- 2026-06-25: Evidence pass confirmed this slice must not read script packet draft JSON, so planning summaries will be deterministic source/linkage summaries, not storyboard frames.
- 2026-06-25: Added storyboard planning brief models, pure brief service, CLI command, help text, and focused tests covering PASS, accepted packet FAIL, preflight FAIL/wrong next step, missing source/hash/anchors/risks, forbidden downstream completion claims, and CLI output.
- 2026-06-25: Verification passed: `.\gradlew.bat test --tests "*StoryboardPlanningBriefTest" --no-daemon`; `git diff --check` reported only Windows line-ending warnings.
- 2026-06-25: Notified `03` for read-only review with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.
- 2026-06-25: `03` returned REVISE P1: the brief gate must fail when either accepted packet id or preflight target id is blank, and must require an exact id match before allowing `storyboard_packet_draft`.
- 2026-06-25: REVISE fix added explicit missing/mismatch accepted script packet id checks and regression tests for missing preflight id, missing accepted packet id, and mismatched ids.
- 2026-06-25: REVISE verification passed: `.\gradlew.bat test --tests "*StoryboardPlanningBriefTest" --no-daemon`; `git diff --check` on this slice reported only the existing Windows line-ending warning.
- 2026-06-25: Notified `03` for read-only review of the REVISE fix with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.

## Active Minimal Storyboard Planning Preflight Plan

### Goal

Add a local read-only JSON preflight after accepted script packet handoff packet PASS. It validates that the accepted script packet is ready to enter a conservative storyboard planning brief step without generating storyboards, images, video, or modifying script content.

### Global Strategy

- Keep this slice Kotlin/JVM CLI plus JSON file protocol only.
- Read exactly one accepted script packet handoff packet JSON via `--accepted-script-packet <path>` and optionally write a preflight report via `--out`.
- Require packet verdict `PASS`, `nextAllowedDownstreamStep == "storyboard_planning_preflight"`, a valid 64-character lowercase SHA-256 script packet draft hash, source bridge/source-binding/structure verdicts, protected continuity anchors summary, and listed residual risks.
- Output a deterministic storyboard planning preflight report with verdict, accepted script packet id/file, evidence counts, blocked reason, findings, and next allowed downstream step.
- Use the conservative next step `storyboard_planning_brief` because existing docs describe storyboard planning / text-frame preparation before any storyboard packet, image, or video generation.
- Notify `03` for read-only review after implementation and verification, with PASS / REVISE / FAIL requested back to `01`.

### Non-Goals

- No storyboard creation, image generation, video generation, voice generation, script rewriting, asset approval, runtime scene creation, or setting-pack canon application.
- No external AI API, model install, web server, port, browser control, SQLite write, schema migration, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/StoryboardPlanningPreflightService.kt`
- `app/src/test/kotlin/com/localnovel/engine/StoryboardPlanningPreflightTest.kt`

### Confirmed Facts

- `01` assigned this exact next slice to `02` after `03` passed the accepted script packet handoff packet REVISE fix.
- `Project completion progress` reported by `01` is currently `44%`.
- Existing `AcceptedScriptPacketHandoffPacket` carries verdict, packet id, script packet draft file, SHA-256 content hash, source report verdicts/paths, protected continuity anchors summary, residual risks, and `nextAllowedDownstreamStep`.
- Existing docs mention storyboard planning, text storyboard frames, storyboard pickup / verification, and reviewable storyboard delivery, but the evidence pass did not find an existing code/docs string for `storyboard_packet_draft`.
- The assignment requires preflight / validation only and explicitly forbids storyboard/image/video production, script modification, and SQLite writes in this slice.

### Assumptions

- `storyboard_planning_brief` is the safest first downstream step after this preflight because it is narrower than drafting a full storyboard packet.
- A first preflight can validate source report verdict presence from packet metadata without opening the source report files.
- Requiring at least one residual risk is intentional because the accepted script packet handoff packet should preserve downstream caveats before storyboard planning.

### Open Questions

- Future slices can define the exact storyboard planning brief schema; this slice only authorizes moving to that brief stage.

### Implementation Steps

- [x] Step 1: Record this minimal storyboard planning preflight plan in `PLANS.md`.
- [x] Step 2: Add storyboard planning preflight report models, pure validation service, CLI wiring, and focused tests.
- [x] Step 3: Run focused tests and `git diff --check`.
- [x] Step 4: Notify `03` thread with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.

### Verification

- Step 2: Unit tests should prove a complete PASS accepted script packet handoff packet passes preflight and emits `nextAllowedDownstreamStep=storyboard_planning_brief`.
- Step 2: Unit tests should prove non-PASS packet verdict or wrong downstream step blocks preflight.
- Step 2: Unit tests should prove missing script packet hash, source verdicts, protected continuity anchors, or residual risks fail preflight.
- Step 2: CLI smoke should write a preflight report without requiring `--db`.
- Step 3: Run `.\gradlew.bat test --tests "*StoryboardPlanningPreflightTest" --no-daemon`.
- Step 3: Run `git diff --check` on changed files for this slice.

### Risks

- Accidentally treating this preflight as storyboard packet creation or storyboard approval.
- Choosing a downstream step that is too broad before the storyboard planning brief schema exists.
- Under-validating source linkage because this slice intentionally reads only the accepted script packet JSON.

### Drift Log

- 2026-06-25: Started `02` implementation of the `minimal storyboard planning preflight` handoff from `01`; current project completion progress from `01` is `44%`.
- 2026-06-25: Evidence pass found existing storyboard docs but no established `storyboard_packet_draft` code/docs string, so this slice chooses conservative downstream `storyboard_planning_brief`.
- 2026-06-25: Added storyboard planning preflight report models, pure validation service, CLI command, help text, and focused tests covering PASS, non-PASS/wrong downstream, missing hash/source/anchors/risks, structure failure, and CLI output.
- 2026-06-25: Verification passed: `.\gradlew.bat test --tests "*StoryboardPlanningPreflightTest" --no-daemon`; `git diff --check` reported only Windows line-ending warnings.
- 2026-06-25: Notified `03` for read-only review with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.

## Active 01 Progress Display Rule Plan

### Goal

Add a control rule requiring `01` to display the current project completion percentage after every `03` PASS / REVISE / FAIL report.

### Global Strategy

- Treat the percentage as a transparent control estimate, not an objective product guarantee.
- Keep the estimate in a docs-only progress register so future 01 messages can cite the same basis.
- Update the 01/02/03 handoff rule so every 03 report response includes `Project completion progress: NN%`.
- Do not change runtime, SQLite, CLI behavior, or task automation.

### Non-Goals

- No automated scheduler, background loop, or progress bot.
- No pretending the percentage is exact.
- No changing current 02 work or interrupting the active REVISE fix.
- No code, schema, migration, SQLite, or runtime write-path changes.

### Scope

- `PLANS.md`
- `docs/HANDOFF.md`
- `docs/PROJECT_COMPLETION_PROGRESS.md`

### Confirmed Facts

- User requested: every time the control window receives a 03 report, show the current project completion percentage.
- `docs/HANDOFF.md` already defines the chat-window loop: `01 -> 02 -> 03 -> 01`.
- Current project completion still includes product decisions and downstream storyboard / comic video work, so an exact percentage would be misleading without a documented rubric.
- Current active loop has multiple passed local gates from character / novelization / script packet flow, while storyboard planning, asset locking, comic/video production, and release packaging are not complete.

### Assumptions

- A weighted completion rubric is acceptable as a control estimate if it is clearly marked as such.
- `03` REVISE or FAIL should not increase progress; it should display the last confirmed percentage plus the blocking status.
- `03` PASS can update the percentage only when a slice is actually accepted.

### Open Questions

- The final product definition may still change; if it changes, `docs/PROJECT_COMPLETION_PROGRESS.md` must be revised before future percentages are trusted.

### Implementation Steps

- [x] Step 1: Add `docs/PROJECT_COMPLETION_PROGRESS.md` with the current percentage and calculation rule.
- [x] Step 2: Update `docs/HANDOFF.md` so 01 must display progress after each 03 report.
- [x] Step 3: Verify docs and report the current percentage.

### Verification

- Search for `Project completion progress` in `docs/HANDOFF.md` and the progress register.
- Run `git diff --check -- PLANS.md docs\HANDOFF.md docs\PROJECT_COMPLETION_PROGRESS.md`.

### Risks

- A percentage can create false certainty if the basis is not shown.
- The current dirty worktree means this docs-only update must avoid unrelated edits.

### Drift Log

- 2026-06-25: Started docs-only 01 progress display rule after the user requested a percentage after every 03 report.
- 2026-06-25: Added `docs/PROJECT_COMPLETION_PROGRESS.md` with current control estimate `44%` and updated `docs/HANDOFF.md` so `01` must show progress after every `03` report.
- 2026-06-25: Verified progress display docs with `rg` and `git diff --check -- PLANS.md docs\HANDOFF.md docs\PROJECT_COMPLETION_PROGRESS.md`; diff check exited 0 with only Windows line-ending warnings. First displayed estimate after the `03` PASS for accepted script packet handoff packet is `44%`.
- 2026-06-25: After `03` PASS for `minimal storyboard planning preflight`, updated the displayed control estimate to `46%`.
- 2026-06-25: After `03` PASS for `minimal storyboard planning brief gate` REVISE P1 fix, updated the displayed control estimate to `48%`.
- 2026-06-25: After `03` PASS for `minimal storyboard packet draft gate` REVISE P1 fix, updated the displayed control estimate to `50%`.
- 2026-06-25: After `03` PASS for `minimal storyboard packet review gate` REVISE P1 fix, updated the displayed control estimate to `52%`.
- 2026-06-25: After `03` PASS for `minimal storyboard packet human review preflight` REVISE P1 fix, updated the displayed control estimate to `54%`.
- 2026-06-25: After `03` PASS for `minimal storyboard packet human review decision intake gate` REVISE P1 fix, updated the displayed control estimate to `56%`.
- 2026-06-25: After `03` PASS for `minimal asset candidate preflight gate` REVISE P1 fix, updated the displayed control estimate to `58%`.
- 2026-06-25: After `03` PASS for `minimal asset candidate brief gate`, updated the displayed control estimate to `60%`.
- 2026-06-25: After `03` PASS for the `Accelerated Asset Candidate Batch` covering draft, draft validation, and review preflight gates, updated the displayed control estimate to `62%`.
- 2026-06-25: After `03` PASS for the `Accelerated Asset Candidate Human Review Batch` covering asset candidate human review decision, image candidate preflight, and image candidate brief gates, updated the displayed control estimate to `65%`.
- 2026-06-25: After `03` PASS for the `Accelerated Image Candidate Prompt Preparation Batch` REVISE P1 fix, updated the displayed control estimate to `68%`.
- 2026-06-25: After `03` PASS for the `Accelerated Image Candidate Manual Provider Handoff Batch` REVISE P1 fix, updated the displayed control estimate to `71%`.
- 2026-06-25: After `03` PASS for the `Accelerated Image Candidate External Result Intake Batch` REVISE P1 second fix, updated the displayed control estimate to `73%`.

## Active Minimal Accepted Script Packet Handoff Packet Plan

### Goal

Add a local read-only JSON handoff packet gate after `validate-script-packet-structure-bridge` PASS. It packages a script packet draft and its structure bridge report into an accepted script packet handoff packet that can be handed to the future storyboard planning preflight.

### Global Strategy

- Keep this slice Kotlin/JVM CLI plus JSON file protocol only.
- Read `--script-packet-draft` and `--structure-bridge-report`; write a deterministic JSON packet to optional `--out`.
- Treat the bridge report as the upstream authority: it must have verdict `PASS` and `downstreamAllowed=true`.
- Compute a UTF-8 SHA-256 hash from the script packet draft JSON file contents.
- Carry source linkage from the bridge report: bridge report file/verdict, source-binding verdict, structure verdict, script packet preflight file, and accepted draft packet file.
- Carry protected continuity anchor summary from the script packet draft surfaces that the source-binding gate already validated.
- Block downstream if the bridge failed, downstream is not allowed, or the script packet draft claims storyboard/image/video/runtime/canon completion.
- Notify `03` for read-only review after implementation and verification, with PASS / REVISE / FAIL requested back to `01`.

### Non-Goals

- No storyboard planning, image generation, video generation, voice generation, script rewriting, asset approval, runtime scene creation, or setting-pack canon application.
- No external AI API, model install, web server, port, browser control, SQLite write, schema migration, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/AcceptedScriptPacketHandoffPacketService.kt`
- `app/src/test/kotlin/com/localnovel/engine/AcceptedScriptPacketHandoffPacketTest.kt`

### Confirmed Facts

- `01` assigned this exact next slice to `02` after `03` passed the script packet draft structure bridge slice.
- Existing `ScriptPacketDraftStructureBridgeReport` carries `verdict`, `downstreamAllowed`, `scriptPacketDraftFile`, `scriptPacketPreflightFile`, `acceptedDraftPacketFile`, `sourceBindingVerdict`, `structureVerdict`, findings, and evidence.
- Existing `NovelizationAcceptedDraftPacketService` uses SHA-256 content hashing, source report metadata, protected continuity anchors, residual risks, and a `nextAllowedDownstreamStep` field for a downstream handoff packet.
- Existing `ScriptPacketDraft` carries storyboard notes continuity locks, review summary, source binding metadata, segments, assets, and text fields that can be scanned for forbidden downstream claims.
- The assignment requires packet creation / validation only and explicitly forbids storyboard/image/video production, script modification, and SQLite writes in this slice.

### Assumptions

- For this first handoff packet, protected continuity anchor summary can use `scriptPacketDraft.storyboardNotes.continuityLocks` because the source-binding gate already validated those protected anchors were referenced.
- `PASS_WITH_WARNINGS` bridge reports should not create a PASS accepted script packet because the assignment requires bridge verdict `PASS`.
- Remaining residual risks should include bridge source-binding/structure findings when any warnings remain, plus a standard note that storyboard planning has not yet run.

### Open Questions

- Future slices can load the accepted draft packet from `acceptedDraftPacketFile` and compare protected anchors exactly; this slice stays anchored on the bridge report plus script packet draft file.

### Implementation Steps

- [x] Step 1: Record this minimal accepted script packet handoff packet plan in `PLANS.md`.
- [x] Step 2: Add accepted script packet models, pure packet service, CLI wiring, and focused tests.
- [x] Step 3: Run focused tests and `git diff --check`.
- [x] Step 4: Notify `03` thread with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.

### Verification

- Step 2: Unit tests should prove bridge PASS plus downstreamAllowed true builds a PASS packet with script packet path/hash, source linkage, continuity summary, residual risks, and `nextAllowedDownstreamStep=storyboard_planning_preflight`.
- Step 2: Unit tests should prove bridge FAIL or downstreamAllowed false creates a FAIL packet with empty next downstream step.
- Step 2: Unit tests should prove forbidden claims about storyboard/image/video/runtime/canon completion block the packet.
- Step 2: CLI smoke should write an accepted script packet handoff packet without requiring `--db`.
- Step 3: Run `.\gradlew.bat test --tests "*AcceptedScriptPacketHandoffPacketTest" --no-daemon`.
- Step 3: Run `git diff --check` on changed files for this slice.

### Risks

- Accidentally treating this packet as approved storyboard, asset, final video, runtime scene, or canon rather than a storyboard-planning handoff.
- Under-validating source traceability if future bridge reports omit source file paths.
- Duplicating source-binding or structure validation instead of relying on the reviewed bridge report.

### Drift Log

- 2026-06-25: Started `02` implementation of the `minimal accepted script packet handoff packet` handoff from `01`.
- 2026-06-25: Evidence pass confirmed the new packet can mirror the accepted draft packet pattern while using `ScriptPacketDraftStructureBridgeReport` as the upstream gate.
- 2026-06-25: Added accepted script packet handoff packet models, pure packet service, CLI command, help text, and focused tests covering PASS, bridge FAIL, downstreamAllowed=false, forbidden downstream completion claims, and CLI output.
- 2026-06-25: Verification passed: `.\gradlew.bat test --tests "*AcceptedScriptPacketHandoffPacketTest" --no-daemon`; `git diff --check` reported only Windows line-ending warnings.
- 2026-06-25: Notified `03` for read-only review with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.
- 2026-06-25: `03` returned REVISE. Required fix: expand forbidden downstream completion claim scanning to mirror script packet text surfaces covered by source-binding, include list string fields such as visual mustShow/mustNotShow, knowledge pass, interaction choices, frame doNotShow, and cover asset approval wording such as `asset approval complete` / `assets approved`.

### Revision Steps

- [x] Step 5: Expand accepted script packet handoff text-field collection to cover script packet text surfaces required by `03`.
- [x] Step 6: Add regression tests for forbidden completion claims in previously missed fields and asset approval wording.
- [x] Step 7: Run focused tests and `git diff --check`, then notify `03` again.

- 2026-06-25: Completed REVISE fix. Expanded accepted script packet handoff text scanning to include source fields, scene reference fields, location zone fields, visual mustShow/mustNotShow, knowledge pass lists including authorOnlyExtracted, beat ids/list fields, action blocking zones, dialogue knowledge boundaries, interaction choices, storyboard frame ids/beat ids/doNotShow, asset ids/types, and review/secrecy fields. Added asset approval wording patterns for `assets approved` and `asset approval complete`.
- 2026-06-25: Added regression tests for forbidden claims in previously missed fields and asset approval wording. Verification passed: `.\gradlew.bat test --tests "*AcceptedScriptPacketHandoffPacketTest" --no-daemon`; `git diff --check` reported only Windows line-ending warnings.

## Active Minimal Script Packet Draft Structure Bridge Plan

### Goal

Add a local read-only JSON bridge command that validates a script packet draft through both the existing source-binding gate and the existing script packet draft structure validator, then writes one combined report for downstream handoff decisions.

### Global Strategy

- Keep this slice Kotlin/JVM CLI plus JSON file protocol only.
- Reuse `ScriptPacketDraftSourceBindingValidationService` and `ScriptPacketDraftValidationService`; do not duplicate their rule sets.
- Read `--script-packet-draft`, `--script-packet-preflight`, and `--accepted-draft-packet`; write an optional JSON combined report.
- Require source-binding verdict `PASS`; allow structure verdict `PASS` or `PASS_WITH_WARNINGS`.
- Expose source-binding findings, structure findings, downstream allowed state, and blocked reason in the combined report.
- Notify `03` for read-only review after implementation and verification, with PASS / REVISE / FAIL requested back to `01`.

### Non-Goals

- No automatic script rewriting, prose-to-script conversion, storyboard generation, video generation, voice generation, or runtime scene creation.
- No external AI API, model install, web server, port, browser control, SQLite write, schema migration, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ScriptPacketDraftStructureBridgeService.kt`
- `app/src/test/kotlin/com/localnovel/engine/ScriptPacketDraftStructureBridgeTest.kt`

### Confirmed Facts

- `01` assigned this exact next slice to `02` after `03` passed the revised source-binding gate.
- Existing `validate-script-packet-source-binding` reads script packet draft, script packet preflight report, and accepted draft packet, then emits a source-binding report.
- Existing `validate-script-packet-draft` reads a script packet draft and emits a structure report with `PASS`, `PASS_WITH_WARNINGS`, or `FAIL`.
- Existing `ScriptPacketDraftValidationService` already validates packet structure, source status, segment fields, beat/action/dialogue/choice fields, asset/storyboard readiness, review finality, and author-only leakage.
- The assignment requires orchestration / combined validation only and explicitly forbids automatic script/storyboard/video production in this slice.

### Assumptions

- A bridge report can use `PASS_WITH_WARNINGS` only when source-binding is `PASS` and structure validation is `PASS_WITH_WARNINGS`.
- A first combined report can embed both existing finding lists directly instead of translating every upstream finding into a new rule id.
- Future slices can add stricter downstream routing; this slice only exposes whether the next handoff is allowed or blocked.

### Open Questions

- None blocking for the first local read-only bridge command.

### Implementation Steps

- [x] Step 1: Record this minimal script packet draft structure bridge plan in `PLANS.md`.
- [x] Step 2: Add combined report models, pure bridge service, CLI wiring, and focused tests.
- [x] Step 3: Run focused tests and `git diff --check`.
- [x] Step 4: Notify `03` thread with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.

### Verification

- Step 2: Unit tests should prove source-binding PASS plus structure PASS allows downstream.
- Step 2: Unit tests should prove source-binding FAIL blocks the bridge even if structure passes.
- Step 2: Unit tests should prove structure FAIL blocks the bridge even if source-binding passes.
- Step 2: Unit tests should prove structure `PASS_WITH_WARNINGS` is allowed but preserved in the combined report.
- Step 2: CLI smoke should write a bridge report without requiring `--db`.
- Step 3: Run `.\gradlew.bat test --tests "*ScriptPacketDraftStructureBridgeTest" --no-daemon`.
- Step 3: Run `git diff --check` on changed files for this slice.

### Risks

- Accidentally making the bridge a third validator instead of a small orchestrator over the two existing validators.
- Treating structure warnings as full approval instead of an allowed downstream handoff with warnings preserved.
- Expanding scope into script generation or storyboard generation before this gate is reviewed.

### Drift Log

- 2026-06-25: Started `02` implementation of the `minimal script packet draft structure bridge` handoff from `01`.
- 2026-06-25: Evidence pass confirmed the bridge can reuse existing source-binding and structure validation services without touching runtime state.
- 2026-06-25: Added combined report models, bridge validation service, CLI command, help text, and focused tests covering PASS, source-binding FAIL, structure FAIL, structure PASS_WITH_WARNINGS, and CLI output.
- 2026-06-25: Verification passed: `.\gradlew.bat test --tests "*ScriptPacketDraftStructureBridgeTest" --no-daemon`; `git diff --check` reported only Windows line-ending warnings.
- 2026-06-25: Notified `03` for read-only review with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.

## Active Minimal Script Packet Draft Source-Binding Gate Plan

### Goal

Add a local read-only JSON source-binding gate for a manually/Codex-authored script packet draft after accepted-draft-to-script preflight PASS. It validates that the script packet draft is bound to the accepted draft packet and preflight report, preserves or explicitly references protected continuity anchors, and does not claim downstream approval status.

### Global Strategy

- Keep this slice Kotlin/JVM CLI plus JSON file protocol only.
- Complement the existing `validate-script-packet-draft` structural validator; do not duplicate full segment, beat, asset, storyboard, or author-only leakage validation.
- Read `--script-packet-draft`, `--script-packet-preflight`, and `--accepted-draft-packet`; write an optional JSON report.
- Validate upstream PASS status, source binding evidence, protected continuity anchor references, and downstream boundary claims.
- Add explicit optional source-binding fields to `ScriptPacketSource` while still accepting existing `sourceId` / source text as binding evidence.
- Notify `03` for read-only review after implementation and verification, with PASS / REVISE / FAIL requested back to `01`.

### Non-Goals

- No automatic prose-to-script conversion, storyboard generation, video generation, voice generation, prose rewriting, or runtime scene creation.
- No external AI API, model install, web server, port, browser control, SQLite write, schema migration, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ScriptPacketDraftSourceBindingValidationService.kt`
- `app/src/test/kotlin/com/localnovel/engine/ScriptPacketDraftSourceBindingValidationTest.kt`

### Confirmed Facts

- `01` assigned this exact next slice to `02` after `03` passed the accepted draft to script packet preflight slice.
- Existing `ScriptPacketDraftValidationService` validates draft structure, beat/action/dialogue fields, asset/storyboard readiness, review finality, and author-only leakage.
- Existing `ScriptPacketDraft.source` currently carries `inputType`, `sourceId`, `sourceTitle`, `excerptSummary`, and `transformationStatus`.
- `NovelizationScriptPacketPreflightReport` carries the accepted draft packet id and preflight verdict.
- `NovelizationAcceptedDraftPacket` carries packet id, revised draft content hash, revised draft file, protected continuity anchors, source report verdicts, and next downstream step.
- The assignment requires validation only and explicitly forbids automatic script/storyboard/video production in this slice.

### Assumptions

- Adding optional default source-binding fields to `ScriptPacketSource` is backward-compatible for existing script packet samples.
- A first deterministic continuity reference check can use exact or significant-term matching against script packet text surfaces.
- Boundary claim detection should catch explicit positive claims such as canon / runtime scene / approved storyboard / final video, while avoiding existing neutral notes like "not canon".

### Open Questions

- Future slices can require exact field-level binding only; this first gate accepts either explicit binding fields or existing source text evidence.

### Implementation Steps

- [x] Step 1: Record this minimal script packet draft source-binding gate plan in `PLANS.md`.
- [x] Step 2: Add source-binding model fields/report models, pure service, CLI wiring, and focused tests.
- [x] Step 3: Run focused tests and `git diff --check`.
- [x] Step 4: Notify `03` thread with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.

### Verification

- Step 2: Unit tests should prove a script packet draft bound by accepted packet id/hash/path passes with upstream PASS reports.
- Step 2: Unit tests should prove failed preflight or accepted draft packet blocks the gate.
- Step 2: Unit tests should prove missing source binding fails.
- Step 2: Unit tests should prove missing protected continuity anchor references fail.
- Step 2: Unit tests should prove canon/runtime/approved storyboard/final video claims fail.
- Step 2: CLI smoke should write a source-binding report without requiring `--db`.
- Step 3: Run `.\gradlew.bat test --tests "*ScriptPacketDraftSourceBindingValidationTest" --no-daemon`.
- Step 3: Run `git diff --check` on changed files for this slice.

### Risks

- False positives/negatives from deterministic anchor text matching.
- Confusing this source-binding gate with full `validate-script-packet-draft` structural validation.
- Allowing source text evidence as a fallback may be looser than future exact metadata requirements.

### Drift Log

- 2026-06-25: Started `02` implementation of the `minimal script packet draft source-binding gate` handoff from `01`.
- 2026-06-25: Evidence pass confirmed existing `validate-script-packet-draft` already handles structure, so this slice will focus only on source binding and downstream boundary.
- 2026-06-25: Added optional source-binding fields on `ScriptPacketSource`, source-binding report models, pure validation service, CLI command, help text, and focused tests.
- 2026-06-25: Verification passed: `.\gradlew.bat test --tests "*ScriptPacketDraftSourceBindingValidationTest" --no-daemon`; `git diff --check` reported only Windows line-ending warnings.
- 2026-06-25: Notified `03` for read-only review with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.
- 2026-06-25: `03` returned REVISE. Required fix: make protected continuity anchor matching CJK-aware so pure Traditional Chinese anchors do not false-pass when absent.
- 2026-06-25: Completed REVISE code and tests. Added CJK 2-character window matching, made nonblank anchors without exact or term evidence fail, and added Chinese missing-anchor FAIL / referenced-anchor PASS regression tests. Verification passed: `.\gradlew.bat test --tests "*ScriptPacketDraftSourceBindingValidationTest" --no-daemon`; `git diff --check` reported only Windows line-ending warnings.
- 2026-06-25: Notified `03` read-only review thread again for the REVISE fix pass and requested PASS / REVISE / FAIL back to `01`.

### Revision Steps

- [x] Step 5: Add CJK-aware protected continuity anchor matching and make nonblank anchors without exact match or usable terms fail.
- [x] Step 6: Add focused Chinese missing-anchor FAIL and Chinese referenced-anchor PASS regression tests.
- [x] Step 7: Run focused tests and `git diff --check`, then notify `03` again.

## Active Minimal Accepted Draft To Script Packet Preflight Plan

### Goal

Add a local read-only JSON preflight after accepted draft packet PASS. It reads an accepted draft packet and validates that the packet is ready to enter the `script_packet_draft` stage without converting prose, creating storyboards, producing video, or modifying content.

### Global Strategy

- Keep this slice Kotlin/JVM CLI plus JSON file protocol only.
- Validate the accepted draft packet itself; do not reopen or transform the revised draft content.
- Require packet verdict `PASS`, `nextAllowedDownstreamStep == "script_packet_draft"`, a 64-character SHA-256 revised draft content hash, source report file paths and verdicts, and at least one protected continuity anchor.
- Output a deterministic preflight report with findings and evidence counts.
- Notify `03` for read-only review after implementation and verification, with PASS / REVISE / FAIL requested back to `01`.

### Non-Goals

- No script conversion, storyboard generation, video generation, voice generation, prose rewriting, or downstream packet drafting.
- No external AI API, model install, web server, port, browser control, SQLite write, schema migration, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/NovelizationScriptPacketPreflightService.kt`
- `app/src/test/kotlin/com/localnovel/engine/NovelizationScriptPacketPreflightTest.kt`

### Confirmed Facts

- `01` assigned this exact next slice to `02` after `03` passed the accepted draft packet slice.
- `NovelizationAcceptedDraftPacket` already carries verdict, packet id, revised draft file, SHA-256 content hash, source reports, required fixes handled summary, protected continuity anchors, residual risks, and next allowed downstream step.
- The assignment requires a preflight / validation report only and explicitly forbids automatic script/storyboard/video production in this slice.
- Current repo rules allow Kotlin/JVM CLI and JSON file protocols, while forbidding server/port/external AI API/direct SQLite bypasses.

### Assumptions

- A valid first preflight can treat `script_packet_draft` as the only accepted downstream step string.
- Source report verdict presence can be checked from packet metadata without rereading source report files.
- A 64-character lowercase hex SHA-256 string is sufficient evidence that the accepted packet carries a revised draft content hash.

### Open Questions

- Future slices can verify source report files still exist and match packet metadata; this slice stays read-only on the packet JSON only.

### Implementation Steps

- [x] Step 1: Record this minimal accepted draft to script packet preflight plan in `PLANS.md`.
- [x] Step 2: Add preflight report models, pure service, CLI wiring, and focused tests.
- [x] Step 3: Run focused tests and `git diff --check`.
- [x] Step 4: Notify `03` thread with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.

### Verification

- Step 2: Unit tests should prove a structurally complete PASS accepted draft packet passes preflight.
- Step 2: Unit tests should prove non-PASS packet verdict blocks preflight.
- Step 2: Unit tests should prove missing hash, wrong downstream step, or missing protected anchors fails preflight.
- Step 2: CLI smoke should write a preflight report without requiring `--db`.
- Step 3: Run `.\gradlew.bat test --tests "*NovelizationScriptPacketPreflightTest" --no-daemon`.
- Step 3: Run `git diff --check` on changed files for this slice.

### Risks

- Treating preflight PASS as actual script packet creation.
- Under-validating source report freshness because this slice only reads the accepted packet.
- Over-coupling to one downstream step string if future flows add alternatives.

### Drift Log

- 2026-06-25: Started `02` implementation of the `minimal accepted draft to script packet preflight` handoff from `01`.
- 2026-06-25: Evidence pass confirmed the preflight can read only `NovelizationAcceptedDraftPacket` and follow existing JSON report patterns.
- 2026-06-25: Added script packet preflight report models, pure validation service, CLI command, help text, and focused tests.
- 2026-06-25: Verification passed: `.\gradlew.bat test --tests "*NovelizationScriptPacketPreflightTest" --no-daemon`; `git diff --check` reported only Windows line-ending warnings.
- 2026-06-25: Notified `03` for read-only review with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.

## Active Minimal Accepted Draft Handoff Packet Plan

### Goal

Add a local read-only accepted draft handoff packet gate after revised draft intake PASS. It reads the revised draft, revised draft intake report, revision brief report, and continuity anchors, then writes a structured JSON packet for later script, storyboard, and video flow handoff without transforming the prose.

### Global Strategy

- Keep this slice Kotlin/JVM CLI plus JSON file protocol only.
- Create a deterministic packet report, not a script, storyboard, video plan, or rewrite.
- Require `NovelizationRevisedDraftIntakeReport.verdict == "PASS"` for a PASS packet.
- Include revised draft path, SHA-256 content hash, source report file paths and verdicts, handled required fix summary, protected continuity anchors, remaining residual risks, and the next allowed downstream step.
- Notify `03` for read-only review after implementation and verification, with PASS / REVISE / FAIL requested back to `01`.

### Non-Goals

- No automatic script conversion, storyboard generation, video generation, voice generation, prose rewriting, or quality re-audit.
- No external AI API, model install, web server, port, browser control, SQLite write, schema migration, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/NovelizationAcceptedDraftPacketService.kt`
- `app/src/test/kotlin/com/localnovel/engine/NovelizationAcceptedDraftPacketTest.kt`

### Confirmed Facts

- `01` assigned this exact next slice to `02` after `03` passed the revised draft intake REVISE fixes.
- Existing gates use Kotlin/JVM CLI commands and JSON reports.
- `NovelizationRevisedDraftIntakeReport` carries the revised draft intake verdict and evidence counts.
- `NovelizationRevisionBriefReport` carries required fixes, protected anchors, `doNotChange`, and revision brief source state.
- `NovelizationRevisionContinuityAnchors` carries the anchor file id, anchors, and do-not-change hints.
- The assignment requires packet creation / validation only and explicitly forbids script/storyboard/video production in this slice.

### Assumptions

- SHA-256 over the exact revised draft text bytes is sufficient for the first content hash field.
- A first packet can summarize handled fixes from the revision brief and intake evidence counts, even though the separate declared handling JSON is not an input to this slice.
- `nextAllowedDownstreamStep` can be a deterministic string, `script_packet_draft`, because script conversion is the next documented downstream format but is not executed here.

### Open Questions

- Future slices can add packet signatures or canonical JSON hashes; this slice only hashes revised draft content.

### Implementation Steps

- [x] Step 1: Record this minimal accepted draft handoff packet plan in `PLANS.md`.
- [x] Step 2: Add accepted draft packet models, pure service, CLI wiring, and focused tests.
- [x] Step 3: Run focused tests and `git diff --check`.
- [x] Step 4: Notify `03` thread with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.

### Verification

- Step 2: Unit tests should prove a PASS revised draft intake builds a PASS packet with a draft SHA-256 hash, source reports, handled fix summary, protected anchors, residual risks, and next allowed downstream step.
- Step 2: Unit tests should prove non-PASS revised draft intake blocks a PASS packet.
- Step 2: CLI smoke should write an accepted draft packet without requiring `--db`.
- Step 3: Run `.\gradlew.bat test --tests "*NovelizationAcceptedDraftPacketTest" --no-daemon`.
- Step 3: Run `git diff --check` on changed files for this slice.

### Risks

- Treating the accepted draft packet as permission to bypass future script/storyboard/video gates.
- Under-reporting residual risks if upstream reports contain warnings not yet mapped.
- Overstating hash stability if later slices normalize content before hashing.

### Drift Log

- 2026-06-25: Started `02` implementation of the `minimal accepted draft handoff packet` handoff from `01`.
- 2026-06-25: Evidence pass confirmed the packet can follow existing revision brief and revised draft intake JSON report patterns.
- 2026-06-25: Added accepted draft packet models, pure packet service, CLI command, help text, and focused tests.
- 2026-06-25: Verification passed: `.\gradlew.bat test --tests "*NovelizationAcceptedDraftPacketTest" --no-daemon`; `git diff --check` reported only Windows line-ending warnings.
- 2026-06-25: Notified `03` for read-only review with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.

## Active Minimal Revised Draft Intake Gate Plan

### Goal

Add a local read-only revised draft intake gate after the revision brief. It reads the original draft, revised draft, revision brief report, and an optional declared handling JSON file, then outputs a structured JSON validation report without rewriting prose.

### Global Strategy

- Keep this slice Kotlin/JVM CLI plus JSON file protocol only.
- Add a deterministic gate that validates revised draft readiness, not prose quality or final literary quality.
- Check the revised draft is non-empty and not the unchanged original draft.
- Require declared handling for revision brief mandatory checklist items and required fixes when the revision brief asks for them.
- Check banned calibration phrases are not still present in the revised draft.
- Check continuity anchors from the revision brief are still represented by simple deterministic preservation heuristics.
- Notify `03` for read-only review after implementation and verification, with PASS / REVISE / FAIL requested back to `01`.

### Non-Goals

- No automatic rewriting, style rewriting, chapter generation, prose quality re-audit, continuity deep audit, script conversion, storyboard generation, video generation, or voice generation.
- No external AI API, model install, web server, port, browser control, SQLite write, schema migration, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/NovelizationRevisedDraftIntakeValidationService.kt`
- `app/src/test/kotlin/com/localnovel/engine/NovelizationRevisedDraftIntakeValidationTest.kt`
- `samples/novelization_revision_handling_declaration_sample.json`

### Confirmed Facts

- `01` assigned this exact next slice to `02` after `03` passed the minimal prose revision brief gate.
- Existing gates use Kotlin/JVM CLI commands and JSON reports.
- `NovelizationRevisionBriefReport` already carries `mandatoryChecklist`, `requiredFixes`, `continuityAnchors`, `doNotChange`, and upstream evidence.
- The assignment requires a read-only gate that confirms revised draft non-empty, not identical to original, required items have declared handling, banned phrases are not residual, and continuity anchors are not removed or obviously damaged.
- Current repo rules allow Kotlin/JVM CLI and JSON file protocols, while forbidding server/port/external AI API/direct SQLite bypasses.

### Assumptions

- Because the original/revised draft and revision brief report do not themselves contain post-revision handling declarations, a small optional declared handling JSON file is the cleanest local JSON protocol. If required declarations are missing, the gate will fail.
- A first deterministic continuity check can use exact/term-preservation heuristics rather than a semantic model.

### Open Questions

- Future slices can replace deterministic anchor preservation with deeper continuity audit evidence, but this slice will stay local and heuristic-only.

### Implementation Steps

- [x] Step 1: Record this minimal revised draft intake gate plan in `PLANS.md`.
- [x] Step 2: Add revised draft intake models, pure service, CLI wiring, sample declared handling JSON, and focused tests.
- [x] Step 3: Run focused tests and `git diff --check`.
- [x] Step 4: Notify `03` thread with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.

### Verification

- Step 2: Unit tests should prove a handled revision passes when it changes the draft, declares required handling, removes banned phrases, and preserves anchors.
- Step 2: Unit tests should prove unchanged revised draft fails.
- Step 2: Unit tests should prove missing declared handling fails when the revision brief requires fixes.
- Step 2: Unit tests should prove removed/damaged continuity anchors fail.
- Step 2: CLI smoke should write a revised draft intake report without requiring `--db`.
- Step 3: Run `.\gradlew.bat test --tests "*NovelizationRevisedDraftIntakeValidationTest" --no-daemon`.
- Step 3: Run `git diff --check` on changed files for this slice.

### Risks

- Treating this gate as proof of literary quality rather than revision readiness.
- False positives from deterministic continuity heuristics.
- Accepting a declared handling file as truthful; this slice only checks declaration presence and basic structure, not full semantic compliance.

### Drift Log

- 2026-06-25: Started `02` implementation of the `minimal revised draft intake gate` handoff from `01`.
- 2026-06-25: Evidence pass confirmed the slice can follow existing draft intake, prose quality audit, and revision brief JSON report patterns.
- 2026-06-25: Added revised draft intake report and declared handling models, pure validation service, CLI command, sample handling declaration, and focused tests.
- 2026-06-25: Verification passed: `.\gradlew.bat test --tests "*NovelizationRevisedDraftIntakeValidationTest" --no-daemon`; `git diff --check` reported only Windows line-ending warnings.
- 2026-06-25: Notified `03` for read-only review with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.
- 2026-06-25: `03` returned REVISE. Required fixes: add deterministic continuity damage guard for polarity / negation reversal, use `doNotChange` hints, and fix `not only ... but ...` banned residual detection.
- 2026-06-25: Completed REVISE code and test fixes. Added paid/unpaid/reset continuity damage guard, corrected `not only ... but ...` residual regex, and added focused regression tests. Verification passed: `.\gradlew.bat test --tests "*NovelizationRevisedDraftIntakeValidationTest" --no-daemon`; `git diff --check` reported only Windows line-ending warnings.
- 2026-06-25: Notified `03` read-only review thread again for the REVISE fix pass and requested PASS / REVISE / FAIL back to `01`.

### Revision Steps

- [x] Step 5: Add deterministic continuity damage guard for obvious paid / unpaid and reset polarity reversals, using both anchors and `doNotChange` hints.
- [x] Step 6: Fix `CALIBRATION_EN_NOT_ONLY` to catch `not only ... but ...` and `not only ... but also ...`.
- [x] Step 7: Add focused FAIL tests for anchor damage and `not only` banned residual.
- [x] Step 8: Run focused tests and `git diff --check`, then notify `03` again.

## Active Minimal Prose Revision Brief Gate Plan

### Goal

Add the first local read-only prose revision brief gate after draft intake and prose quality audit. It reads the draft, draft intake report, prose quality audit report, and continuity anchors, then outputs a structured JSON revision brief for human/Codex revision without rewriting the novel.

### Global Strategy

- Keep this slice Kotlin/JVM CLI plus JSON file protocol only.
- Generate a deterministic revision brief report, not revised prose.
- Convert prose quality findings into concrete revision items while always carrying the mandatory revision checklist: AI taste, dialogue, character voice, scene pressure, banned phrase removal, and continuity anchor protection.
- Require explicit continuity anchors so a revision brief cannot silently permit continuity drift.
- Notify `03` for read-only review after implementation and verification, with PASS / REVISE / FAIL requested back to `01`.

### Non-Goals

- No automatic rewrite, chapter generation, style cloning, continuity audit, script conversion, storyboard generation, video generation, or voice generation.
- No external AI API, model install, web server, port, browser control, SQLite write, schema migration, or runtime write-path change.
- No `resolve-turn`, `StateReducer`, `contextHash`, or `stateVersion` changes.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/NovelizationRevisionBriefService.kt`
- `app/src/test/kotlin/com/localnovel/engine/NovelizationRevisionBriefTest.kt`
- `samples/novelization_revision_continuity_anchors_sample.json`

### Confirmed Facts

- `01` assigned this exact next slice to `02` after `03` passed the revised draft prose quality audit.
- Existing gates now include draft intake and prose quality audit as read-only JSON reports.
- The assignment requires a revision brief or validation report that lists AI taste fixes, dialogue fixes, character voice fixes, scene pressure reinforcement, banned phrase removal, and continuity anchors that must not change.
- Current repo rules allow Kotlin/JVM CLI and JSON file protocols, while forbidding server/port/external AI API/direct SQLite bypasses.

### Assumptions

- The smallest useful revision brief can map prose quality findings to revision items and carry a mandatory checklist for categories not yet detected directly.
- A continuity anchor JSON file is acceptable as the explicit anchor source for this first gate.

### Open Questions

- Future slices can make revision items character-card-aware, but this slice will not compare against full Character Nuwa cards.

### Implementation Steps

- [x] Step 1: Record this minimal prose revision brief gate plan in `PLANS.md`.
- [x] Step 2: Add revision brief models, pure service, CLI wiring, sample continuity anchors, and focused tests.
- [x] Step 3: Run focused tests and `git diff --check`.
- [x] Step 4: Notify `03` thread with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.

### Verification

- Step 2: Unit tests should prove prose quality findings become required revision items.
- Step 2: Unit tests should prove missing continuity anchors blocks the revision brief.
- Step 2: CLI smoke should write a revision brief report without requiring `--db`.
- Step 3: Run `.\gradlew.bat test --tests "*NovelizationRevisionBriefTest" --no-daemon`.
- Step 3: Run `git diff --check` on changed files for this slice.

### Risks

- Treating the revision brief as revised prose or final approval.
- Overfitting the first mapping from prose audit categories to revision instructions.
- Losing continuity anchors in later revision steps if anchors are optional.

### Drift Log

- 2026-06-25: Started `02` implementation of the `minimal prose revision brief gate` handoff from `01`.
- 2026-06-25: Added revision brief report models, continuity anchor JSON model, pure service, CLI command, sample anchors, and focused tests.
- 2026-06-25: Verification passed: `.\gradlew.bat test --tests "*NovelizationRevisionBriefTest" --no-daemon`; `git diff --check` reported only Windows line-ending warnings.
- 2026-06-25: Notified `03` for read-only review with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.

## Active Draft Prose Quality Audit Plan

### Goal

Add the first local read-only draft prose quality audit for novelization drafts, focused on AI-like prose, generic phrasing, banned calibration structures, dialogue drift, weak character voice, missing scene pressure, missing subtext, and mechanical rhythm.

### Global Strategy

- Keep this slice Kotlin/JVM CLI plus JSON file protocol only.
- Read draft text plus a `NovelizationDraftIntakeReport`; write a JSON prose quality audit report.
- Treat this as a review aid after draft intake, not as canon acceptance and not as runtime validation.
- Use deterministic local heuristics only; no external AI API, model install, web server, port, browser control, SQLite write, or runtime write-path change.
- Notify `03` for read-only review after implementation and verification, with PASS / REVISE / FAIL requested back to `01`.

### Non-Goals

- No automatic rewriting, chapter generation, style cloning, fact extraction, continuity audit, script conversion, storyboard generation, video generation, or voice generation.
- No SQLite migration, direct database write, Validator, StateReducer, `resolve-turn`, `stateVersion`, or `contextHash` change.
- No unattended infinite automation; this is one bounded `02 -> 03 -> 01` slice.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/NovelizationProseQualityAuditService.kt`
- `app/src/test/kotlin/com/localnovel/engine/NovelizationProseQualityAuditTest.kt`

### Confirmed Facts

- `01` assigned this exact slice to `02` and required `03` read-only review after completion.
- Existing novelization gates now include Character Nuwa Gate, novelization preflight, writer brief validation, and draft intake validation.
- `NovelizationDraftIntakeValidationService` produces a read-only JSON report and blocks drafts whose writer brief failed.
- `docs/NOVELIZATION_SUPERIOR_TO_QUARTERFULL_PLAN.md` requires an anti-AI prose and dialogue audit after draft intake.
- Current repo rules allow Kotlin/JVM CLI and JSON file protocols, while forbidding server/port/external AI API/direct SQLite bypasses.

### Assumptions

- The smallest useful first prose audit should detect deterministic visible failure modes, not attempt subjective literary scoring.
- A passing draft intake report is the right upstream dependency for this first prose audit.
- Later slices can add character-card-aware voice comparison and calibrated real-project examples.

### Open Questions

- The exact threshold for "AI-like" prose will need calibration on real accepted/rejected drafts later.
- This first slice will not prove final story quality; it only catches obvious review blockers.

### Implementation Steps

- [x] Step 1: Record this draft prose quality audit plan in `PLANS.md`.
- [x] Step 2: Add prose quality report models, pure audit service, CLI wiring, and focused tests.
- [x] Step 3: Run focused tests and `git diff --check`.
- [x] Step 4: Notify `03` thread with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.

### Verification

- Step 2: Unit tests should prove a clean sample draft passes and a failed draft intake report blocks prose audit.
- Step 2: Unit tests should prove banned calibration and exposition-dump dialogue are caught.
- Step 2: CLI smoke should write an audit report without requiring `--db`.
- Step 3: Run `.\gradlew.bat test --tests "*NovelizationProseQualityAuditTest" --no-daemon`.
- Step 3: Run `git diff --check` on changed files for this slice.

### Risks

- False positives from deterministic heuristics before enough real samples exist.
- Treating this audit as final prose approval instead of a review aid.
- Duplicating continuity audit or writer brief validation logic inside prose quality audit.

### Drift Log

- 2026-06-25: Started `02` implementation of the `draft prose quality audit` handoff from `01`.
- 2026-06-25: Added prose quality audit report models, pure local audit service, CLI command, help text, and focused tests.
- 2026-06-25: Verification passed: `.\gradlew.bat test --tests "*NovelizationProseQualityAuditTest" --no-daemon`; `git diff --check` reported only Windows line-ending warnings.
- 2026-06-25: Notified `03` read-only review thread and requested PASS / REVISE / FAIL back to `01`.
- 2026-06-25: `03` returned REVISE. Required fixes: Traditional Chinese marker coverage, Chinese punctuation-aware sentence segmentation, and Chinese PASS/FAIL tests.
- 2026-06-25: Completed REVISE fixes. Added Traditional Chinese marker coverage, Chinese-aware sentence segmentation, and Chinese PASS/FAIL tests. Verification passed: `.\gradlew.bat test --tests "*NovelizationProseQualityAuditTest" --no-daemon`; `git diff --check` reported only Windows line-ending warnings.
- 2026-06-25: Notified `03` read-only review thread again for the REVISE fix pass and requested PASS / REVISE / FAIL back to `01`.

### Revision Steps

- [x] Step 5: Add Traditional Chinese marker sets and Chinese punctuation-aware sentence segmentation to the prose quality audit.
- [x] Step 6: Add Chinese PASS/FAIL tests for pressure scenes and AI-style exposition scenes.
- [x] Step 7: Run focused tests and `git diff --check`.
- [x] Step 8: Notify `03` thread again with changed files, verification, scope, safety boundaries, and PASS / REVISE / FAIL request back to `01`.

## Active 01/02/03 Task Handoff Mode Check

### Goal

Open a safe chat-window task handoff mode where `01` assigns one project-completion slice to `02`, `02` completes and sends the result to `03` for read-only review, `03` reports back to `01`, and `01` decides the next slice.

### Global Strategy

- Use Codex chat-window thread messages for handoff, not scheduler or unattended background automation.
- Keep each cycle bounded to one smallest verifiable project slice.
- Preserve the existing repo boundaries: update `PLANS.md` before implementation, verify each step, and stop at blockers or human-decision points.
- Treat "infinite loop" as repeatable manual chat-window cycling, not an autonomous scheduler.

### Non-Goals

- No automation / scheduler wakeups.
- No unbounded background execution without a current chat-window handoff.
- No bypass of 03 read-only review before 01 chooses the next assignment.
- No SQLite, setting-pack, runtime, schema, Validator, StateReducer, `resolve-turn`, `stateVersion`, or `contextHash` change from this control-mode check.

### Scope

- `PLANS.md`
- `docs/HANDOFF.md`
- Codex thread messages to `02`

### Confirmed Facts

- User requested this mode on 2026-06-25: `01 -> 02 -> 03 -> 01 -> next work` until project work is complete.
- `docs/HANDOFF.md` already says manual handoff only and forbids automation / scheduler wakeups for the 04 / 05 / 06 / 07 / 08 flow.
- `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md` says `02` executes repo docs / CLI / engineering milestones only after 01 explicit handoff, and `03` performs read-only verification after 02 directly notifies it.
- Thread tooling found `01 主控窗口` (`019ef802-e81d-7ea3-a35d-b675248c5402`), `02` (`019ef6ed-b458-7641-8b3f-e3083edf510f`), and `03` (`019ef82d-b434-7660-be6d-0c84990375e0`).
- `02` is currently idle and its latest answer says the project is not complete; it identifies draft prose quality audit as the next smallest slice.
- `03` exists but is currently `notLoaded`.

### Assumptions

- A safe first handoff can assign `02` the next smallest slice: draft prose quality audit.
- `02` can notify `03` through thread tooling after completing its slice.
- `03` can report back to this `01` thread through thread tooling after review.

### Open Questions

- A truly autonomous infinite loop is not approved because current docs prohibit scheduler/automation advancement and project completion still has human-decision points.
- Whether later cycles should create commits / pushes remains a per-slice decision, not part of this control-mode check.

### Implementation Steps

- [x] Step 1: Read current handoff evidence and locate `01`, `02`, and `03` threads.
- [x] Step 2: Document the safe task handoff loop and its non-automation boundary.
- [x] Step 3: Send the first `01 -> 02` assignment for the next smallest project-completion slice.
- [x] Step 4: Verify dispatch and report whether infinite cycling is possible.

### Verification

- Step 2: Search `PLANS.md` and `docs/HANDOFF.md` for the new handoff loop wording.
- Step 3: Confirm `send_message_to_thread` accepts the assignment to `02`.
- Step 4: Run `git diff --check` for docs touched in this control-mode check.

### Risks

- Accidentally starting unattended automation.
- Letting `02` continue without `03` review.
- Treating project completion as objectively decidable without 01/user product decisions.
- Adding more dirty work on top of the existing uncommitted worktree.

### Drift Log

- 2026-06-25: Started safe task handoff mode check. Evidence supports a manual 01/02/03 chat-window loop, but not an unattended infinite automation loop.
- 2026-06-25: Dispatched the first task-handoff assignment from `01` to `02`: implement the next smallest `draft prose quality audit` slice, then notify `03` for read-only review instead of returning directly to `01`.
- 2026-06-25: Verified first dispatch. `02` is active and acknowledged the `draft prose quality audit` task. `git diff --check -- PLANS.md docs\HANDOFF.md` exited 0 with only Windows line-ending warnings. The loop can repeat through chat-window handoffs, but it must not run as unattended infinite automation.

## Active Minimal Draft Intake Gate Plan

### Goal

Add the first local draft intake gate so prose from Codex, GPT, QuarterFull, manual writing, or mixed sources cannot enter review without a passing writer brief report and basic text integrity checks.

### Global Strategy

- Keep this slice file-output/read-only only: draft text in, writer brief validation report in, JSON intake report out.
- Treat all draft sources equally at intake; none are trusted just because they are fluent.
- Validate only intake readiness: source type, non-empty draft text, text integrity, and upstream writer brief verdict.
- Do not perform continuity audit, prose quality audit, or revision generation in this slice.
- Notify project `02` after completion, then continue to the next smallest slice unless stopped.

### Non-Goals

- No automatic chapter generation, rewriting, story extraction, prose conversion, or acceptance of canon.
- No web server, open port, OpenAI API, browser automation, QuarterFull control, or model installation.
- No SQLite migration, direct database write, Validator, StateReducer, `resolve-turn`, `stateVersion`, or `contextHash` change.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/NovelizationDraftIntakeValidationService.kt`
- `app/src/test/kotlin/com/localnovel/engine/NovelizationDraftIntakeValidationTest.kt`
- `samples/novelization_draft_intake_sample.md`

### Confirmed Facts

- The novelization plan says drafts may come from Codex, another GPT chat, QuarterFull, manual writing, or mixed notes, and none are trusted until audited.
- Writer brief validation now produces a report and can be blocked by failed preflight.
- Existing CLI patterns support read-only validation commands and JSON reports without requiring SQLite.

### Assumptions

- The next smallest useful gate after writer brief is draft intake validation.
- Later work can feed a passed intake report into continuity audit, anti-AI prose audit, and minimal revision brief generation.

### Open Questions

- Exact production source taxonomy can be refined later after real drafts are processed.

### Implementation Steps

- [x] Step 1: Add draft intake report models, pure validation service, CLI command, sample draft, and focused tests.
- [x] Step 2: Run focused tests and `git diff --check`.
- [x] Step 3: Notify project `02` window with the completion report and next-work prompt.

### Verification

- Step 1: Unit tests should prove a valid draft with a passing writer brief report passes and a failed writer brief report blocks intake.
- Step 1: CLI smoke should read a draft file and writer brief report without requiring `--db`.
- Step 2: Run `.\gradlew.bat test --tests "*NovelizationDraftIntakeValidationTest" --no-daemon`.
- Step 2: Run `git diff --check` on the changed files for this slice.

### Risks

- Making draft intake look like content approval; this slice only approves entry into review.
- Duplicating continuity or prose quality audits in the intake gate.
- Accidentally coupling draft intake to runtime state or SQLite.

### Drift Log

- 2026-06-25: Started the minimal draft intake gate slice after linking writer brief validation to upstream preflight.
- 2026-06-25: Added draft intake JSON report models, pure validation service, CLI command, sample draft, and focused tests.
- 2026-06-25: Verification passed for the draft intake slice: `.\gradlew.bat test --tests "*NovelizationDraftIntakeValidationTest" --no-daemon`; `git diff --check` reported only Windows line-ending warnings.

## Active Minimal Writer Brief Gate Plan

### Goal

Add the first local JSON writer brief gate so next-chapter drafting has a structured brief before prose is written.

### Global Strategy

- Keep this slice file-output/read-only only: JSON template and JSON validation report.
- Build a minimal writer brief around the novelization plan fields: previous facts, character states, open threads, must advance, must not repeat, allowed scenes, known unknowns, tone/pacing, style contract, Character Nuwa references, anti-AI constraints, and author-only exclusion.
- Follow existing CLI patterns for template creation and validation.
- Do not generate prose; only create and validate the brief shape.
- Notify project `02` after completion, then continue to the next smallest slice unless stopped.

### Non-Goals

- No automatic chapter generation, rewriting, story extraction, or prose conversion.
- No web server, open port, OpenAI API, browser automation, QuarterFull control, or model installation.
- No SQLite migration, direct database write, Validator, StateReducer, `resolve-turn`, `stateVersion`, or `contextHash` change.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/NovelizationWriterBriefValidationService.kt`
- `app/src/test/kotlin/com/localnovel/engine/NovelizationWriterBriefValidationTest.kt`
- `samples/novelization_writer_brief_sample.json`

### Confirmed Facts

- The novelization plan places the writer brief after Character Nuwa Gate and before draft intake.
- Existing repo patterns support JSON template commands and validation report commands without requiring SQLite.
- The user wants the project to keep moving in repeated minimal completed work items.

### Assumptions

- A minimal writer brief validator is the next smallest useful step after preflight.
- Later work can connect this writer brief to continuity anchors, accepted chapter extraction, and revision briefs.

### Open Questions

- Exact author-facing wording of real project briefs remains human-controlled.

### Implementation Steps

- [x] Step 1: Add writer brief models, validation service, CLI template/validation commands, sample JSON, and focused tests.
- [x] Step 2: Run focused tests and `git diff --check`.
- [x] Step 3: Notify project `02` window with the completion report and next-work prompt.

### Verification

- Step 1: Unit tests should prove a complete writer brief passes and a missing `mustNotRepeat` list fails.
- Step 1: CLI smoke should create a template and validate the sample without requiring `--db`.
- Step 2: Run `.\gradlew.bat test --tests "*NovelizationWriterBriefValidationTest" --no-daemon`.
- Step 2: Run `git diff --check` on the changed files for this slice.

### Risks

- Making the brief look like accepted canon; this slice validates draft planning data only.
- Over-validating the brief before real author examples exist.
- Accidentally coupling the brief to runtime state or SQLite.

### Drift Log

- 2026-06-25: Started the minimal writer brief gate slice after completing direct Character Nuwa Gate preflight input support.
- 2026-06-25: Added writer brief JSON models, pure validation service, CLI template/validation commands, sample JSON, and focused tests.
- 2026-06-25: Verification passed for the writer brief slice: `.\gradlew.bat test --tests "*NovelizationWriterBriefValidationTest" --no-daemon`; `git diff --check` reported only Windows line-ending warnings.
- 2026-06-25: Linked writer brief validation to optional Novelization Preflight report input so a failed preflight blocks the brief.
- 2026-06-25: Verification passed for the writer brief / preflight link: `.\gradlew.bat test --tests "*NovelizationWriterBriefValidationTest" --no-daemon`; `git diff --check` reported only Windows line-ending warnings.

### Continuation Steps

- [x] Step 4: Allow writer brief validation to optionally read a Novelization Preflight report and fail when the upstream preflight failed.
- [x] Step 5: Run focused tests and `git diff --check` for the writer-brief/preflight link.
- [x] Step 6: Notify project `02` window with the completion report and next-work prompt.

## Active Minimal Novelization Preflight Plan

### Goal

Complete the smallest useful project work after the Character Nuwa Gate: add a local Kotlin/JVM CLI preflight that reads Character Nuwa Gate evidence and tells the writer workflow whether the next-chapter drafting stage may start.

### Global Strategy

- Keep this slice file-output/read-only only: JSON report in, JSON report out.
- Reuse the existing Character Nuwa Gate report when present.
- Also allow direct raw Character Nuwa Gate JSON input so the preflight can run as a one-step command for authors.
- Make the preflight a clear upstream gate for novel drafting, not a generation system.
- Return exit code 1 on FAIL and 0 on PASS or PASS_WITH_WARNINGS, matching existing report validators.
- After completion, notify project `02` with the requested delegation prompt.

### Non-Goals

- No chapter generation, rewriting, writer brief generation, or prose conversion.
- No web server, open port, OpenAI API, browser automation, QuarterFull control, or model installation.
- No SQLite migration, direct database write, Validator, StateReducer, `resolve-turn`, `stateVersion`, or `contextHash` change.
- No indefinite background automation loop.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/NovelizationPreflightValidationService.kt`
- `app/src/test/kotlin/com/localnovel/engine/NovelizationPreflightValidationTest.kt`

### Confirmed Facts

- The user asked to complete the project's smallest work, then report to the project `02` window.
- Existing CLI commands already validate Character Nuwa Gate JSON and write a `CharacterNuwaGateValidationReport`.
- The novelization plan says Character Nuwa Gate comes before the next-chapter writer brief and draft intake.
- Current repository rules allow Kotlin/JVM CLI and JSON file protocols, while forbidding web servers, ports, external AI APIs, and direct SQLite bypasses.

### Assumptions

- The smallest useful next step is a pre-draft readiness gate over the existing Character Nuwa report.
- Later milestones can extend this preflight with continuity ledger, style source pack, writer brief, and draft-intake checks.

### Open Questions

- The exact long-running loop policy should remain human-controlled; this slice records no scheduler or background automation.

### Implementation Steps

- [x] Step 1: Record this minimal preflight plan in `PLANS.md`.
- [x] Step 2: Add preflight report models, pure validation service, CLI wiring, and focused tests.
- [x] Step 3: Run focused tests and `git diff --check`.
- [x] Step 4: Notify project `02` window with the requested next-work prompt.

### Verification

- Step 2: Unit tests should prove PASS from a passing Character Nuwa report and FAIL from a failing Character Nuwa report.
- Step 2: CLI smoke should read a Character Nuwa report and write a preflight report without requiring `--db`.
- Step 3: Run `.\gradlew.bat test --tests "*NovelizationPreflightValidationTest" --no-daemon`.
- Step 3: Run `git diff --check` on the changed files for this slice.
- Step 5: CLI smoke should read a raw Character Nuwa Gate JSON and write a preflight report without requiring an intermediate report file.

### Risks

- Making the preflight look like it approves prose quality; this slice only approves readiness to start drafting.
- Duplicating Character Nuwa finding details instead of referencing the upstream report.
- Confusing raw gate input with report input in the preflight output.
- Accidentally adding runtime or SQLite coupling.

### Drift Log

- 2026-06-25: Started the minimal project work slice after the user requested a smallest completed work item plus notification to project `02`.
- 2026-06-25: Completed implementation for the local novelization preflight slice: report models, pure validation service, CLI wiring, help text, and focused tests.
- 2026-06-25: Verification passed for the minimal preflight slice: `.\gradlew.bat test --tests "*NovelizationPreflightValidationTest" --no-daemon`; `git diff --check` reported only Windows line-ending warnings.
- 2026-06-25: Notified project `02` window with the completion report and next-work prompt.
- 2026-06-25: Continued the minimal preflight work after user asked why the project did not keep moving. Added direct raw Character Nuwa Gate JSON input support while preserving report input support.

### Continuation Steps

- [x] Step 5: Allow `validate-novelization-preflight` to accept either `--character-nuwa-report` or raw `--character-nuwa-gate`.
- [x] Step 6: Run focused tests and `git diff --check` for the direct-gate input slice.
- [x] Step 7: Notify project `02` window with the next completion report and next-work prompt.

## Active Character Nuwa Gate Implementation Plan

### Goal

Implement the first local Kotlin/JVM CLI slice for the Character Nuwa Gate: create an editable character-card template and validate character cards before any speaking character can be accepted for drafting.

### Global Strategy

- Keep the implementation file-output/read-only only: JSON templates and JSON validation reports.
- Follow existing CLI patterns used by `audit-chapter-continuity` and `validate-script-packet-draft`.
- Add serializable models, a pure validation service, CLI wiring, sample JSON, and focused tests.
- Treat missing character cards, missing required full cards, missing light-card essentials, knowledge-boundary gaps, absent accepted/rejected sample lines, and hard-banned calibration slogans as report findings.
- Do not integrate this into runtime state, SQLite, setting packs, `resolve-turn`, or any generation system in this slice.

### Non-Goals

- No automatic chapter generation or rewriting.
- No real Nuwa research-agent workflow, web search, browser automation, QuarterFull control, or external AI API.
- No SQLite migration, database read/write, Validator, StateReducer, `stateVersion`, `contextHash`, or `resolve-turn` changes.
- No author-style cloning or model installation.

### Scope

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/CharacterNuwaGateValidationService.kt`
- `app/src/test/kotlin/com/localnovel/engine/CharacterNuwaGateValidationTest.kt`
- `samples/character_nuwa_gate_sample.json`

### Confirmed Facts

- User approved the Character Nuwa Gate and requested completion.
- User asked to report in the project `02` window and send `請指派新工作 完成藍圖` after completion.
- Current repo rules allow Kotlin/JVM CLI, SQLite, and JSON file protocols, but forbid web server, ports, external AI APIs, and direct SQLite bypasses.
- Existing `Main.kt` has read-only/file-output command patterns: `audit-chapter-continuity --previous --draft --anchors [--out]` and `validate-script-packet-draft --packet [--out]`.
- Existing validation services return JSON reports with verdict, finding count, findings, severity, evidence, and suggested fixes.
- `docs/HUMANLIKE_PROSE_AND_DIALOGUE_DISTILLATION_PLAN.md` defines hard rules: no character card means no accepted dialogue, important characters need full cards, one-off background speakers may use light cards, and `這不是XX，這是XXX` / `not X, but Y` structures are hard-banned.

### Assumptions

- The first card file can hold both required speaking character IDs and character cards.
- A `requiredDepth` field is enough for the first implementation to distinguish full Nuwa cards from light voice cards.
- The validator should return exit code 1 on FAIL and 0 on PASS or PASS_WITH_WARNINGS, matching existing draft validators.

### Open Questions

- Whether later milestones should generate character cards from accepted chapters remains open.
- Whether later writer-brief generation should consume this exact report format remains open.

### Implementation Steps

- [x] Step 1: Record this implementation plan in `PLANS.md`.
- [x] Step 2: Add serializable Character Nuwa card and validation report models.
- [x] Step 3: Add a pure `CharacterNuwaGateValidationService`.
- [x] Step 4: Wire CLI commands `create-character-nuwa-card-template` and `validate-character-nuwa-gate`.
- [x] Step 5: Add sample JSON and focused tests for PASS / missing card FAIL / hard-ban FAIL / CLI report writing.
- [x] Step 6: Run focused tests and `git diff --check`.
- [x] Step 7: Notify project `02` window with `請指派新工作 完成藍圖` if the thread tooling is available.

### Verification

- Step 2: Compile-time coverage through focused tests after service and CLI wiring exist.
- Step 3: Unit tests should prove valid cards pass and missing/insufficient cards fail.
- Step 4: CLI smoke should create a template and validate a sample without requiring `--db`.
- Step 5: Focused test command: `.\gradlew.bat test --tests "*CharacterNuwaGateValidationTest" --no-daemon`.
- Step 6: `git diff --check -- PLANS.md app/src/main/kotlin/com/localnovel/engine/Main.kt app/src/main/kotlin/com/localnovel/engine/model/Models.kt app/src/main/kotlin/com/localnovel/engine/service/CharacterNuwaGateValidationService.kt app/src/test/kotlin/com/localnovel/engine/CharacterNuwaGateValidationTest.kt samples/character_nuwa_gate_sample.json`.

### Risks

- Over-validating creative fields too early and making cards tedious to draft.
- Under-validating generic speech and allowing the gate to become decorative.
- Introducing accidental runtime or SQLite coupling.
- Missing the project `02` window if thread tooling cannot identify it.

### Drift Log

- 2026-06-25: Started implementation slice after user approved the next step and requested completion plus notification to project `02` window.
- 2026-06-25: Completed Character Nuwa Gate implementation. Added models, pure validation service, CLI template/validation commands, sample gate JSON, and focused tests. Verification passed: `.\gradlew.bat test --tests "*CharacterNuwaGateValidationTest" --no-daemon`; `git diff --check` reported only Windows line-ending warnings.
- 2026-06-25: Notified project `02` window with the requested completion message through Codex thread tooling.

## Active Character Nuwa Gate And Hard Ban Plan

### Goal

Promote character-level Nuwa distillation from a suggested voice-card concept into a mandatory pre-writing gate, and hard-ban the AI calibration structure `這不是XX，這是XXX` / `這不是 X，這是 Y` / `not X, but Y` before any draft can be accepted.

### Global Strategy

- Keep this slice docs-only and upstream of generation.
- Add a Character Nuwa Gate: every recurring or important speaking character must have a Nuwa-style character card before dialogue generation.
- Add a lighter fallback for one-off NPCs, but still require identity, pressure, knowledge boundary, register, and forbidden speech patterns before they speak.
- Treat `這不是XX，這是XXX`, `這不是 X，這是 Y`, and equivalent `not X, but Y` calibration as a hard fail pattern, not a stylistic warning.
- Wire the gate into the novelization plan, humanlike prose plan, and authoring quality guideline.

### Non-Goals

- No actual character skill generation in this slice.
- No story rewrite, chapter generation, browser automation, scraping, external AI API, or model installation.
- No Kotlin source, schema, SQLite, Validator, StateReducer, `resolve-turn`, `stateVersion`, or `contextHash` changes.
- No enforcing a single author clone or copyrighted style imitation.

### Scope

Allowed files for this planning slice:

- `PLANS.md`
- `docs/HUMANLIKE_PROSE_AND_DIALOGUE_DISTILLATION_PLAN.md`
- `docs/NOVELIZATION_SUPERIOR_TO_QUARTERFULL_PLAN.md`
- `docs/AUTHORING_QUALITY_GUIDELINES.md`

### Confirmed Facts

- User approved adding the Character Nuwa Gate.
- User explicitly required banning the pattern `這不是XX 這是XXX`.
- `docs/HUMANLIKE_PROSE_AND_DIALOGUE_DISTILLATION_PLAN.md` already defines character dialogue voice cards and anti-AI prose audit.
- `docs/NOVELIZATION_SUPERIOR_TO_QUARTERFULL_PLAN.md` already makes style source pack, expression DNA, and character voice cards part of the better-than-QuarterFull loop.
- `docs/AUTHORING_QUALITY_GUIDELINES.md` already warns about AI calibration phrasing such as repeated "not X, only Y", but it is not yet phrased as a hard ban.

### Assumptions

- Full Nuwa-style cards should be required for main, recurring, POV, antagonist, and emotionally important characters.
- Lightweight cards are enough for disposable background speakers as long as the text does not let them become generic exposition tools.

### Open Questions

- The exact storage format for future character Nuwa cards remains open: Markdown, JSON, or both.
- Whether future checks should be manual first or CLI-audited first remains open.

### Implementation Steps

- [x] Step 1: Record this character Nuwa gate and hard-ban plan in `PLANS.md`.
- [x] Step 2: Update the humanlike prose plan with mandatory Character Nuwa Gate rules and banned calibration forms.
- [x] Step 3: Update the QuarterFull-superiority plan so the minimum loop and metrics require Character Nuwa cards.
- [x] Step 4: Update authoring quality guidance so `這不是 X，這是 Y` / `not X, but Y` becomes a hard fail pattern.
- [x] Step 5: Verify docs-only scope with `git diff --check` and targeted searches.

### Verification

- Step 2: Inspect the humanlike prose plan for full-card vs light-card rules and hard-fail banned phrases.
- Step 3: Inspect the novelization plan for Character Nuwa Gate integration.
- Step 4: Inspect authoring guidelines for the hard-ban language.
- Step 5: Run `git diff --check -- PLANS.md docs/HUMANLIKE_PROSE_AND_DIALOGUE_DISTILLATION_PLAN.md docs/NOVELIZATION_SUPERIOR_TO_QUARTERFULL_PLAN.md docs/AUTHORING_QUALITY_GUIDELINES.md`.

### Risks

- Making background NPC setup too heavy and slowing drafting.
- Turning the ban into brittle phrase policing while missing deeper calibration habits.
- Letting character cards become lore dumps instead of speech and pressure controls.

### Drift Log

- 2026-06-25: Started docs-only hardening slice after user approved Character Nuwa Gate and explicitly banned `這不是XX 這是XXX` phrasing.
- 2026-06-25: Completed docs-only hardening. Added mandatory Character Nuwa Gate rules, full-vs-light character card policy, writer-brief gate status, novelization loop metrics, and hard bans for `這不是XX，這是XXX`, `這不是 X，這是 Y`, `不是XX，而是XXX`, `不是 X，而是 Y`, and `not X, but Y`. Verification passed with `git diff --check` reporting only Windows line-ending warnings.

## Active Humanlike Prose And Dialogue Distillation Plan

### Goal

Add a docs-only plan for making dialogue, prose style, and text generation the primary quality system. The target is not "prettier AI prose"; the target is a source-backed style distillation workflow that produces text with character-specific speech, scene pressure, subtext, rhythm, and minimal AI fingerprints before any comic/video production handoff.

### Global Strategy

- Treat prose quality as upstream of script, storyboard, comic panels, audio, and video.
- Adapt the Nuwa skill-distillation method into a project-specific prose style protocol: collect sources, extract expression DNA, define dialogue rules, define anti-patterns, generate a writer brief, draft, audit, and revise.
- Keep this slice docs-only: no OpenAI API, no story generation command, no web automation, no SQLite write, no runtime schema change, and no automatic style model installation.
- Make "unlike AI" auditable through concrete gates: character voice separation, dialogue pressure, subtext, rhythm variance, sensory grounding, low exposition, and banned AI-ish patterns.
- Integrate this into the existing novelization superiority plan so future chapters are not accepted by continuity alone.

### Non-Goals

- No actual chapter rewrite in this slice.
- No scraping, QuarterFull control, browser login, prompt submission, or private-account extraction.
- No local LLM, image model, voice model, video model, ComfyUI, AnimateDiff, Seedance, Oiioii, React, web server, Docker, Android UI, or Rust work.
- No SQLite migrations, setting-pack apply, Validator, StateReducer, `resolve-turn`, `stateVersion`, or `contextHash` changes.
- No blind imitation of a copyrighted author or living writer as a target style; the project should distill principles and user-approved reference traits, not clone a protected voice.

### Scope

Allowed files for this planning slice:

- `PLANS.md`
- `docs/HUMANLIKE_PROSE_AND_DIALOGUE_DISTILLATION_PLAN.md`
- `docs/NOVELIZATION_SUPERIOR_TO_QUARTERFULL_PLAN.md`

### Confirmed Facts

- User stated the most important layer is dialogue, style, and text generation that should feel completely unlike AI.
- User suggested using Nuwa skill distillation.
- `docs/AUTHORING_QUALITY_GUIDELINES.md` already includes anti-AI-writing guidance, micro-prose review, NPC dialogue boundaries, sensory attribution, material culture consistency, and protagonist reaction beats.
- `docs/NOVELIZATION_SUPERIOR_TO_QUARTERFULL_PLAN.md` already requires a writer brief, continuity audit, authoring quality audit, minimal revision brief, and downstream script/storyboard/video handoff.
- The Nuwa skill workflow emphasizes source-backed distillation, expression DNA, anti-patterns, honest boundaries, and quality checks rather than vague roleplay.
- Project constraints still forbid runtime generation APIs, web/server work, direct SQLite writes, and bypassing validation protocols.

### Assumptions

- A project-specific style skill should be treated as a theme/framework skill rather than a single-person perspective skill.
- The first implementation after this planning slice should produce editable JSON/Markdown packets and audits before any automatic rewriting.
- The project will need user-approved positive samples and negative samples to distill a durable house style.

### Open Questions

- Which reference texts should be approved as positive style sources remains open.
- Whether the first source set should use the accepted first chapter, the rejected second chapter as a negative sample, and user-written taste notes remains open.
- Whether future style checks should start as manual rubric, CLI text lint, or GPT-pasteable review packet remains open.

### Implementation Steps

- [x] Step 1: Record this humanlike prose and dialogue distillation plan in `PLANS.md`.
- [x] Step 2: Add `docs/HUMANLIKE_PROSE_AND_DIALOGUE_DISTILLATION_PLAN.md`.
- [x] Step 3: Update `docs/NOVELIZATION_SUPERIOR_TO_QUARTERFULL_PLAN.md` so style distillation becomes a first-class layer/gate before draft acceptance.
- [x] Step 4: Verify the docs-only slice with `git diff --check` and targeted searches for dialogue/style/anti-AI/Nuwa references.

### Verification

- Step 1: Inspect `PLANS.md` to confirm scope, non-goals, confirmed facts, and steps are recorded.
- Step 2: Inspect the new document for concrete source inputs, style DNA outputs, dialogue rules, anti-AI gates, and implementation order.
- Step 3: Inspect the novelization plan to confirm prose style is upstream of production readiness.
- Step 4: Run `git diff --check -- PLANS.md docs/HUMANLIKE_PROSE_AND_DIALOGUE_DISTILLATION_PLAN.md docs/NOVELIZATION_SUPERIOR_TO_QUARTERFULL_PLAN.md`.

### Risks

- Mistaking surface decoration for human writing quality.
- Overfitting to one author or one sample and creating a brittle or legally risky imitation.
- Adding too many abstract writing rules without a concrete draft/review packet.
- Treating "anti-AI" as a classifier label instead of a set of visible prose failures.

### Drift Log

- 2026-06-25: Started docs-only planning slice after the user clarified that dialogue, style, and text generation quality are more important than downstream comic/video packaging, and suggested Nuwa-style distillation.
- 2026-06-25: Added `docs/HUMANLIKE_PROSE_AND_DIALOGUE_DISTILLATION_PLAN.md`, integrated style distillation into the QuarterFull-superiority plan, and verified the docs-only slice with `git diff --check` plus targeted searches for Nuwa, expression DNA, dialogue, anti-AI, style source pack, character voice, and writer brief references.

## Active Comic Video Production Feature Plan

### Goal

Plan the new features required to make Codex-assisted output become a finished comic video product: video, audio, independent character voices, comic panels, storyboard timing, same character across 30+ panels, automatic long-form comic layout, action continuity, and stable scenes, costumes, props, and object states.

### Global Strategy

- Treat "guarantee" as a production workflow guarantee, not a promise that a generative image model will never drift.
- Extend the existing scene-lock, storyboard, asset-registry, and standalone `小說朗讀` narrated-video sidecar evidence with a dedicated comic-video production layer.
- Keep this slice docs-only: define required feature modules, data contracts, gates, and implementation order before writing runtime code.
- Preserve project boundaries: Kotlin/JVM CLI and JSON planning only; no web server, no external AI API, no image generation automation, no SQLite writes, no animation/video tool integration in this slice.
- Make every future image or panel reference locked assets, locked variants, and frame-to-frame continuity checks before it can be accepted.

### Non-Goals

- No actual comic image generation in this slice.
- No actual audio/video rendering, animation generation, Seedance, Oiioii, ComfyUI, AnimateDiff, or external model installation in this slice.
- No OpenAI API, GPT browser automation, web server, React, Next.js, Docker, or UI implementation.
- No SQLite migration or formal asset registry import.
- No treating visual drafts as approved assets or final comic pages.

### Scope

Allowed files for this planning slice:

- `PLANS.md`
- `docs/COMIC_VIDEO_PRODUCTION_FEATURE_PLAN.md`

### Confirmed Facts

- User explicitly identified four missing capabilities: same character consistency across 30 panels, automatic long-form comic layout, action-continuous animation, and no drift in scene, costume, or props per frame.
- User clarified the final target: a finished product with video, audio, independent voice lines per character, comic visuals, and storyboard structure.
- `docs/STORYBOARD_AND_CAMERA_PLANNING.md` already defines scene-locked, asset-aware storyboard planning and says image generation must not invent new rooms, costumes, props, weather, lighting, character designs, or author-only disclosures without approval.
- `docs/PRODUCTION_ASSET_REGISTRY_PLANNING.md` already defines production assets, reference sheets, variants, and required views for characters, animals, weapons, props, costumes, scenes, and zones.
- `docs/ASSET_REGISTRY_IMPLEMENTATION_BLUEPRINT.md` already proposes draft asset files first and a future SQLite formal registry only after an explicit milestone.
- The standalone `小說朗讀` project is named `novel-reader-video` and already describes converting novel text into narrated videos with stable character voices and aligned subtitles.
- The standalone `小說朗讀` config already has `speakerVoices`, `speakerProsody`, subtitle settings, and MP4 settings.
- The standalone `小說朗讀` renderer currently combines narration audio and renders a subtitle video over a solid color background; it does not yet consume comic panels or storyboard frame images.
- Current constraints still forbid web/API/model/server/video implementation inside this repo without later approval.

### Assumptions

- Codex can generate useful comic-style images, but cross-panel consistency needs locked visual references, prompt contracts, and review gates.
- Absolute pixel-level identity across 30 panels is not realistic from text prompts alone; the project can instead guarantee acceptance only after drift checks pass.
- The first implementation should likely be draft JSON templates and validators before image generation.

### Open Questions

- Whether future visual drift checks should be manual, image-model-assisted, or basic metadata-only at first remains open.
- Whether generated image files should live inside repo, ignored output folders, or a separate asset workspace remains open.
- Secondary layout targets such as webtoon vertical scroll, manga pages, and storyboard boards remain open after the first 16:9 comic-video target.

### Implementation Steps

- [x] Step 1: Record this visual consistency feature plan in `PLANS.md`.
- [x] Step 2: Add `docs/COMIC_VIDEO_PRODUCTION_FEATURE_PLAN.md`.
- [x] Step 3: Verify the new document references existing scene/asset/storyboard evidence and does not imply forbidden implementation.

### Verification

- Step 1: Inspect `PLANS.md` to confirm the scope, constraints, and current steps.
- Step 2: Inspect the new feature plan for concrete modules, data contracts, gate criteria, and implementation order.
- Step 3: Run `git diff --check -- PLANS.md docs/COMIC_VIDEO_PRODUCTION_FEATURE_PLAN.md`.

### Risks

- Overstating model reliability instead of designing reviewable acceptance gates.
- Accidentally moving from docs planning into image generation or external tool integration too early.
- Creating asset locks too heavy for early comic exploration.
- Confusing storyboard consistency with final animation continuity.

### Drift Log

- 2026-06-25: Started docs-only planning slice after the user identified the missing visual consistency features needed for comics and animation-ready storyboards.
- 2026-06-25: User clarified the product target is a finished comic video: video + audio + independent character voices + comic panels + storyboard. Scope updated from visual consistency only to comic-video production planning.
- 2026-06-25: Added `docs/COMIC_VIDEO_PRODUCTION_FEATURE_PLAN.md` and verified it stays docs-only while connecting video, audio, voice casting, comic panels, storyboard timing, asset locks, and continuity gates.

## Active Novelization Superiority Plan

### Goal

Arrange a concrete method for the project to surpass QuarterFull when upgrading into a novel-production workflow. The plan should turn the project from "generate prose from prompt" into a local, auditable novel system with memory, continuity, revision, script-packet, storyboard, and optional narrated-video readiness.

### Global Strategy

- Define "surpass QuarterFull" as verifiable product capability: stronger long-form continuity, local memory, audit reports, revision gates, and downstream script/storyboard outputs.
- Keep this milestone docs-only: no browser control, no external AI API, no web server, no SQLite writes, and no runtime state changes.
- Use existing project evidence: novel-to-script blueprint, script-ready packet spec, storyboard planning, authoring quality checklist, and the new chapter-continuity audit gate.
- Treat the standalone `小說朗讀` project as an optional downstream sidecar for narrated video / animatic output, not as part of the Kotlin/JVM engine core.
- Produce one product blueprint that can drive later implementation milestones.

### Non-Goals

- No QuarterFull browser control, login, saves, prompt sending, scraping, or comparisons against live private account data.
- No automatic story generation or rewriting in this milestone.
- No OpenAI API, video model, image model, ComfyUI, AnimateDiff, Seedance, Oiioii, React, server, Docker, or web app work.
- No direct SQLite writes, migrations, setting-pack apply, Validator, StateReducer, `resolve-turn`, `stateVersion`, or `contextHash` changes.
- No treating this blueprint as formal canon, approved asset data, or a finished product.

### Scope

Allowed files for this planning slice:

- `PLANS.md`
- `docs/NOVELIZATION_SUPERIOR_TO_QUARTERFULL_PLAN.md`

### Confirmed Facts

- User requested a method for the project, when upgraded into novel production, to surpass QuarterFull.
- User previously stopped QuarterFull control and asked Codex to upgrade its own process.
- The project already has `docs/NOVEL_TO_SCRIPT_PACKET_BLUEPRINT.md`, which defines prose/chapter/turn narrative to script-ready scene packet transformation.
- The project already has `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`, which defines draft/review packet expectations and continuity checks.
- The project already has `docs/STORYBOARD_AND_CAMERA_PLANNING.md`, which defines scene-locked, asset-aware storyboard planning.
- The project already has `docs/AUTHORING_QUALITY_GUIDELINES.md`, including a fixed review checklist.
- The new local chapter-continuity audit gate caught the downloaded QuarterFull item 36 draft as FAIL with four findings.
- The `小說朗讀` tool now exists as a standalone workspace at `C:\Users\f7212\OneDrive\文件\小說朗讀`, with chapter text under `quarterfull\塵行\`.

### Assumptions

- QuarterFull's strongest visible advantage is low-friction prose generation and a pleasant authoring flow.
- The user's target is not merely prettier prose, but a workflow that preserves long-form story quality while enabling future script/storyboard/video work.
- A local audit-first workflow can outperform a black-box writing tool for long-running serialized fiction if it reduces continuity regressions and makes revisions actionable.

### Open Questions

- Whether the first implementation should focus on chapter fact extraction, novel writer brief generation, or a full novel-quality audit command remains to be selected later.
- Whether future prose generation will be done by Codex, another GPT chat, QuarterFull, or manual authoring remains open; the blueprint should support all sources.
- Whether `小說朗讀` should receive an explicit bridge file format from script/storyboard packets is a later sidecar milestone.

### Implementation Steps

- [x] Step 1: Record this planning slice in `PLANS.md`.
- [x] Step 2: Add `docs/NOVELIZATION_SUPERIOR_TO_QUARTERFULL_PLAN.md`.
- [x] Step 3: Verify the document references existing evidence and does not imply forbidden implementation.

### Verification

- Step 1: Inspect `PLANS.md` to confirm scope, non-goals, confirmed facts, assumptions, and next steps are recorded.
- Step 2: Inspect the new document for concrete phases, gates, deliverables, and success metrics.
- Step 3: Run `git diff --check` and confirm only docs/plan files were touched by this planning slice.

### Risks

- Over-promising "better than QuarterFull" without measurable gates.
- Accidentally designing a web app or API service that violates repo constraints.
- Confusing narrated-video sidecar output with core engine runtime.
- Treating generated prose quality as sufficient without continuity and authoring audits.

### Drift Log

- 2026-06-25: Started docs-only planning slice for surpassing QuarterFull in novel-production workflow. Strategy is product capability design, not implementation.
- 2026-06-25: Completed docs-only plan. Added `docs/NOVELIZATION_SUPERIOR_TO_QUARTERFULL_PLAN.md` with a six-layer novelization product spine, three key gates, phased implementation path, success metrics, and first recommended milestone. Verification passed with `git diff --check -- PLANS.md docs/NOVELIZATION_SUPERIOR_TO_QUARTERFULL_PLAN.md`, which reported only the existing Windows line-ending warning for `PLANS.md`.

## Active Directory Hygiene Plan

### Goal

Move the intentional `小說朗讀` tool project out of the `AI 劇本` Kotlin/JVM engine repository and back to its standalone project location, so the main repository stays focused while the video/narration tool is preserved.

### Global Strategy

- Treat `小說朗讀` as a protected standalone tool, not cleanup waste.
- Preserve the current in-repo `小說朗讀` contents by merging them into `C:\Users\f7212\OneDrive\文件\小說朗讀`.
- Preserve the pre-existing external `C:\Users\f7212\OneDrive\文件\小說朗讀` by copying over newer/missing files only after a path comparison. The first rename-to-backup strategy was blocked by a Windows file lock.
- Do not delete generated videos, narration audio, source text, package files, or nested Git metadata during relocation.
- Update local documentation/memory after the move so future work uses the standalone path.

### Non-Goals

- No cleanup of `小說朗讀/output/` in this step.
- No deletion of external-only files without explicit later approval.
- No SQLite writes, setting-pack changes, runtime state changes, migrations, Validator, StateReducer, `resolve-turn`, `stateVersion`, or `contextHash` changes.
- No changes to the `小說朗讀` tool behavior.

### Scope

- `PLANS.md`
- `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md`
- `docs/CLEANUP_INVENTORY_REPORT.md`
- `docs/HANDOFF.md`
- `docs/PROJECT_DIRECTORY_AND_SUBPROJECT_REGISTER.md`
- `docs/PROJECT_STRUCTURE_REPORT.md`
- `memory/project_directory_memory.json`
- Filesystem relocation from `C:\Users\f7212\OneDrive\文件\AI 劇本\小說朗讀` to `C:\Users\f7212\OneDrive\文件\小說朗讀`

### Confirmed Facts

- User clarified that `小說朗讀` was intentionally created and should not be treated as abandoned waste.
- User requested putting it back in the `小說朗讀` project if the main project should stay clean.
- The previous in-repo source path was `C:\Users\f7212\OneDrive\文件\AI 劇本\小說朗讀`.
- The external destination path exists: `C:\Users\f7212\OneDrive\文件\小說朗讀`.
- The in-repo copy was larger than the external copy before relocation because it included newer output artifacts.
- The external path contains one `.git/refs/codex/...` file not present in the in-repo copy, so an overwrite/mirror would risk losing external-only metadata.
- No active `node`, `ffmpeg`, or `ffprobe` process was observed during the pre-move check.
- Windows refused to rename the existing external `小說朗讀` folder because it was in use by another process.
- Merge-copy verification found 0 missing source files and 0 size mismatches before removing the in-repo copy.
- The standalone path now contains 3790 files and 676,992,556 bytes.

### Assumptions

- The intended standalone project path is `C:\Users\f7212\OneDrive\文件\小說朗讀`.
- A non-mirroring merge copy is safer than renaming while the external folder is locked.

### Open Questions

- Which standalone `output/` variants should be kept or cleaned later is a separate human decision.

### Implementation Steps

- [x] Step 1: Record this relocation plan in `PLANS.md`.
- [x] Step 2: Merge-copy the in-repo `小說朗讀` folder into the existing standalone path without deleting external-only files; after verification, remove the in-repo copy.
- [x] Step 3: Update memory/docs so the standalone path is canonical and the main repo no longer treats `小說朗讀` as an in-repo subproject.
- [x] Step 4: Verify source removal, destination presence, file counts/sizes, nested Git status, and main repo cleanliness impact.

### Verification

- Step 1: Inspect `PLANS.md`.
- Step 2: Verify the standalone path contains all files formerly under the in-repo copy, preserves external-only files, and the in-repo copy has been removed.
- Step 3: Inspect updated docs/memory references.
- Step 4: Run path existence checks, size summaries, `git status --short --ignored`, and `git -C C:\Users\f7212\OneDrive\文件\小說朗讀 status --short --ignored`.

### Risks

- OneDrive or a running process may lock files during rename/move.
- The external path already contains a project copy, so mirroring or overwrite cleanup would risk data loss. The revised plan avoids deletion of external-only files.
- Moving a nested `.git` project changes local path assumptions in docs and memory.

### Drift Log

- 2026-06-25: Plan created after the user clarified `小說朗讀` is an intentional tool and should live in its own project location.
- 2026-06-25: Rename-to-backup strategy failed because the external folder was locked. Revised Step 2 to a non-mirroring merge copy followed by verification-gated removal of the in-repo copy.
- 2026-06-25: Merge-copy succeeded with 0 missing files and 0 size mismatches; removed the in-repo copy. Scope expanded to update stale cross-references in handoff, blueprint register, and project structure docs.
- 2026-06-25: Final verification passed: in-repo `小說朗讀` path is absent; standalone path has 3790 files / 645.63 MB; `memory/project_directory_memory.json` parses; `git diff --check` reported only line-ending warnings.

## Previous Active Self-Upgrade Plan

### Goal

Add a local, read-only chapter continuity gate so Codex cannot accept or hand off a generated next-chapter draft until it has checked the previous accepted chapter, explicit continuity anchors, regression phrases, and text-integrity risks. This upgrade addresses the QuarterFull handoff failure by making continuity verification a required local step before any future external or internal prose draft is treated as usable.

### Global Strategy

- Turn the failure into a deterministic local audit flow, not a memory promise.
- Keep the first implementation narrow: a text-file continuity audit command that reads previous chapter text, draft chapter text, and a reviewer-supplied anchor JSON file.
- Make the gate read-only: it may write a JSON report under `out/`, but it must not modify SQLite, setting packs, runtime state, accepted canon, or external websites.
- Preserve engine boundaries: Kotlin/JVM CLI only, no web server, no API service, no OpenAI API, no QuarterFull browser control.
- Use explicit author/reviewer anchors instead of guessing story truth from prose.

### Non-Goals

- No QuarterFull control, saves, deletes, uploads, prompt sending, or browser automation.
- No automatic prose rewriting, story generation, or AI API calls.
- No SQLite writes, migrations, setting-pack apply, Validator, StateReducer, `resolve-turn`, `stateVersion`, or `contextHash` changes.
- No treating audit PASS as human approval or formal canon.
- No hardcoding `塵行`, `陸知行`, `愛醬`, `沈固`, or any project-specific story facts into engine rules.
- No broad narrative-quality redesign beyond this continuity gate.

### Scope

Allowed files for this upgrade slice:

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ChapterContinuityAuditService.kt`
- `app/src/test/kotlin/com/localnovel/engine/ChapterContinuityAuditTest.kt`
- `samples/chapter_continuity_anchor_sample.json`
- `out/quarterfull/chenxing_episode_02_continuity_anchors.json`

### Confirmed Facts

- User explicitly stopped QuarterFull control and told Codex to upgrade itself.
- The in-app QuarterFull draft for item 36 was downloaded to `out/quarterfull/chenxing_episode_02_root_quarterfull_item36.md`.
- The accepted first chapter exists locally at `小說朗讀/quarterfull/塵行/01-第一話-塵.txt`.
- Manual comparison showed the downloaded second-chapter draft repeats or reopens first-chapter continuity beats instead of cleanly continuing them.
- Existing CLI patterns include read-only commands such as `audit-narrative-quality` and `validate-script-packet-draft`.
- `validate-script-packet-draft` returns exit code 1 on FAIL and writes optional JSON reports without opening SQLite.
- `JsonSupport.json` is already used for tolerant JSON serialization / deserialization in CLI tests.
- Current git status has unrelated dirty files; this slice must not revert or clean them.

### Assumptions

- A simple anchor JSON contract is enough for the first upgrade because the failure was caused by missing explicit continuity anchors, not by a lack of generative capability.
- The first audit should rely on reviewer-provided `mustAppearInPrevious`, `mustAppearInDraft`, and `mustNotAppearInDraft` checks rather than attempting full semantic understanding.
- A replacement-character check is useful because the downloaded QuarterFull draft contained one broken glyph.

### Open Questions

- Whether this continuity gate should later become part of a broader prose-to-script packet workflow is not decided in this slice.
- Whether future anchors should be generated from accepted chapters automatically is a later milestone.
- Whether the current QuarterFull second draft should be revised, discarded, or mined for parts remains a human authoring decision.

### Implementation Steps

- [x] Step 1: Record this self-upgrade plan in `PLANS.md`.
- [x] Step 2: Add serializable chapter-continuity audit models and a pure read-only service.
- [x] Step 3: Wire a CLI command `audit-chapter-continuity --previous <path> --draft <path> --anchors <path> [--out <path>]`.
- [x] Step 4: Add a neutral sample anchor JSON and focused tests for PASS / FAIL / broken-glyph cases.
- [x] Step 5: Add a `塵行`-specific anchor file under `out/quarterfull/` and run the new audit against first chapter + downloaded second draft.
- [x] Step 6: Run focused Gradle tests, CLI smoke, and diff hygiene checks, then update this plan with verification results.

### Verification

- Step 1: Inspect `PLANS.md` to confirm the upgrade goal, scope, non-goals, and current steps are recorded before code edits.
- Step 2: Compile-time coverage through tests once CLI wiring exists; model/service changes remain pure and SQLite-free.
- Step 3: CLI smoke should print verdict, file paths, and finding count; FAIL should exit 1.
- Step 4: Unit tests should prove a clean draft passes, a repeated previous beat fails, and a replacement character is caught.
- Step 5: The real `塵行` audit should fail the downloaded second draft for continuity regression and broken text, proving the gate catches this exact class of mistake.
- Step 6: Run `.\gradlew.bat test --tests "*ChapterContinuityAuditTest" --no-daemon`, one manual CLI smoke, and `git diff --check`.

### Risks

- A text-match audit can miss paraphrased continuity regressions.
- Overly strict anchors can reject legitimate callbacks or memory references.
- Story-specific anchors in `out/` must stay evidence/config, not engine rules.
- Existing dirty work in `Main.kt`, `Models.kt`, and `PLANS.md` increases merge-drift risk.

### Drift Log

- 2026-06-24: Started self-upgrade after user rejected the QuarterFull handoff quality and stopped external-site control. Strategy is a local read-only continuity gate before any future generated chapter can be treated as usable.
- 2026-06-24: Completed Step 2. Added generic chapter-continuity audit models and a pure read-only service; `.\gradlew.bat :app:compileKotlin --no-daemon` passed after fixing three Kotlin lambda trailing-comma syntax errors.
- 2026-06-24: Completed Step 3. Added `audit-chapter-continuity --previous <path> --draft <path> --anchors <path> [--out <path>]` CLI wiring and help text; `.\gradlew.bat :app:compileKotlin --no-daemon` passed.
- 2026-06-24: Completed Step 4. Added neutral `samples/chapter_continuity_anchor_sample.json` and focused `ChapterContinuityAuditTest` coverage for PASS, forbidden regression FAIL, replacement-character FAIL, and CLI report writing. `.\gradlew.bat test --tests "*ChapterContinuityAuditTest" --no-daemon` passed.
- 2026-06-24: Completed Step 5. Added `out/quarterfull/chenxing_episode_02_continuity_anchors.json` and ran `audit-chapter-continuity` against accepted first chapter plus downloaded QuarterFull item 36. The command intentionally returned FAIL and wrote `out/quarterfull/chenxing_episode_02_continuity_audit_report.json`, catching three forbidden continuity regressions plus one replacement-character text-integrity issue.
- 2026-06-24: Completed Step 6. Final focused verification passed: `.\gradlew.bat test --tests "*ChapterContinuityAuditTest" --no-daemon`; `git diff --check` exited 0 with only existing Windows line-ending warnings; parsed real audit report shows verdict FAIL and findingCount 4. No QuarterFull browser control, SQLite writes, runtime state changes, setting-pack apply, or external API work occurred.

## Superseded Delegated Registration Plan

### Goal

Historical 2026-06-24 plan that registered the moved `小說朗讀` subproject inside the `AI 劇本` project. This plan has been superseded by the 2026-06-25 Active Directory Hygiene Plan, which restores `小說朗讀` to the standalone canonical location `C:\Users\f7212\OneDrive\文件\小說朗讀`.

### Global Strategy

- Treat the user's delegation from `01 主控窗口` as the source of truth for relocation status, canonical path, original locked path, project summary, output paths, and `ffprobe` verification.
- Add a dedicated docs-only directory/subproject register so the root project has a clear place to find nested local tooling without confusing it with runtime entities.
- Cross-reference the registration from the existing planning register instead of silently inserting the tool into the blueprint candidate pool.
- Add one local JSON memory file under `memory/` for future Codex windows to recall the canonical location and process-lock caveat.
- Keep all changes additive and documentation/local-memory only; do not write SQLite, do not delete the original locked folder, and do not modify the Node.js subproject implementation.

### Non-Goals

- No direct SQLite writes, migrations, setting-pack apply, Validator, StateReducer, `resolve-turn`, `stateVersion`, or `contextHash` changes.
- No cleanup or deletion of standalone `C:\Users\f7212\OneDrive\文件\小說朗讀\output\` media in that historical registration slice.
- No Node.js CLI, TTS, rendering, `ffmpeg`, or video output changes.
- No web server, port, API, OpenAI API, React, Next.js, Docker, Android UI, Rust, Oiioii, Seedance, or automation work.
- No treating completed MP4 outputs as approved production assets, storyboard deliverables, setting-pack content, or runtime evidence.

### Scope

Allowed files for this registration slice:

- `PLANS.md`
- `docs/PROJECT_DIRECTORY_AND_SUBPROJECT_REGISTER.md`
- `docs/PROJECT_STRUCTURE_REPORT.md`
- `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md`
- `docs/HANDOFF.md`
- `memory/project_directory_memory.json`

### Confirmed Facts

- User delegated from `01 主控窗口` and asked to register the moved `小說朗讀` subproject into directory, planning register, and memory.
- At the time of this historical plan, the in-repo path was treated as canonical: `C:\Users\f7212\OneDrive\文件\AI 劇本\小說朗讀`.
- The 2026-06-25 relocation supersedes that decision; the canonical path is now `C:\Users\f7212\OneDrive\文件\小說朗讀`.
- `C:\Users\f7212\OneDrive\文件\小說朗讀\package.json` confirms the project name is `novel-reader-video`, a Node.js CLI with `novel-video` as its bin entry.
- 2026-06-25 merge-copy verification recorded 3790 files and 676,992,556 bytes at the standalone path, with 0 missing source files and 0 size mismatches before the in-repo copy was removed.
- Root `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md` is a planning register, but it currently focuses on non-canon blueprint candidates and must not be confused with a local tool/subproject registry.
- Root `docs/PROJECT_STRUCTURE_REPORT.md` contains the current directory overview.
- Root `memory/` already exists as a local memory-file area, separate from runtime SQLite memory tables.
- `git status --short --branch` showed existing unrelated dirty work in `PLANS.md`, Kotlin files, untracked docs, and `memory/`; this registration must not revert or clean those changes.

### Assumptions

- A new `docs/PROJECT_DIRECTORY_AND_SUBPROJECT_REGISTER.md` is the safest directory register because existing docs do not yet have a general local subproject register.
- `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md` should receive only a cross-reference and boundary note, not a blueprint candidate row for `小說朗讀`.
- A compact JSON memory file is appropriate because the existing `memory/` directory already stores local repo memory outside SQLite.

### Open Questions

- Which standalone media outputs should be kept or cleaned later remains a separate cleanup decision.
- Whether `小說朗讀` should become a Git submodule, ignored nested repo, or tracked subtree is not decided in this registration slice.
- Whether completed MP4 files should ever enter an asset registry is not decided here and would require a separate 01/user-approved milestone.

### Implementation Steps

- [x] Step 1: Update this registration plan in `PLANS.md`.
- [x] Step 2: Add the directory/subproject register and root directory cross-reference.
- [x] Step 3: Add the planning-register cross-reference and local memory JSON.
- [x] Step 4: Run docs/JSON verification and update `PLANS.md` with results.

### Verification

- Step 1: Inspect `PLANS.md` to confirm scope, facts, assumptions, and current step are recorded before registration edits.
- Step 2: Inspect the new register and directory overview for canonical path, old path, process-lock caveat, and non-runtime boundary.
- Step 3: Parse `memory/project_directory_memory.json` as JSON and confirm the planning register distinguishes local subproject registration from blueprint candidates.
- Step 4: Run `git diff --check`, `git diff --stat`, and `git status --short --branch`; skip full Gradle because this slice is docs/local-memory only and touches no Kotlin/runtime/schema/CLI implementation.

### Risks

- Accidentally treating a local Node.js video tool as part of the Kotlin/JVM runtime.
- Accidentally implying the old locked folder was removed or should be removed immediately.
- Accidentally treating generated MP4 outputs as formal production assets or runtime evidence.
- Accidentally mixing this local tool registration into the non-canon blueprint pool as if it were a world/system candidate.
- Accidentally overwriting existing dirty work in `PLANS.md` or other in-progress files.

### Drift Log

- 2026-06-24: Started delegated `小說朗讀` registration from `01 主控窗口`. Scope is docs/local-memory only: no SQLite, setting-pack, Kotlin runtime, Node.js implementation, generated video edits, original-folder cleanup, or asset approval.
- 2026-06-24: Completed delegated `小說朗讀` registration. Added `docs/PROJECT_DIRECTORY_AND_SUBPROJECT_REGISTER.md`, updated `docs/PROJECT_STRUCTURE_REPORT.md`, cross-referenced the local subproject from `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md` and `docs/HANDOFF.md`, and added `memory/project_directory_memory.json`. Verification passed: JSON parsed, `rg` found canonical path and boundary references, new-file trailing-whitespace scan returned no matches, and `git diff --check` exited 0 with only Windows line-ending warnings. Full Gradle was intentionally skipped because this slice changed only docs/local-memory files and did not touch runtime/schema/CLI implementation.

## Active Implementation Plan

### Goal

Replace the active creative world baseline with a local memory capsule aligned to the visible QuarterFull draft for `塵行 · 第 1 章 · 塵`. The local memory must contain only the QuarterFull-aligned world direction and must not carry legacy old-world exclusion lists into prompts. This milestone stores the aligned world facts in local repo memory files only; it does not modify SQLite, setting packs, runtime state, or QuarterFull content.

### Strategy

- Treat the QuarterFull visible draft as the current authoring source of truth for world direction.
- Extract only worldbuilding facts, constraints, character memory, and continuity anchors, not a full prose copy.
- Remove legacy old-world references from the active local memory capsule and QuarterFull support prompt.
- Store the result in a local memory capsule that can be pasted into QuarterFull or used by future repo tooling.
- Add a short alignment note documenting that the active local memory has been replaced by the QuarterFull-aligned baseline.

### Non-Goals

- No direct deletion of historical docs, setting packs, databases, or generated evidence unless separately requested as a repo-cleanup milestone.
- No SQLite writes, setting-pack apply, schema migration, Validator, StateReducer, `resolve-turn`, `stateVersion`, or `contextHash` changes.
- No QuarterFull edits, saves, clears, submissions, or uploads.
- No AI API, GPT automation, storyboard generation, image generation, Oiioii, Seedance, or animation pipeline work.
- No treating the memory capsule as formal setting-pack canon until a later approved import / validation flow exists.

### Scope

Allowed tracked files for this alignment slice:

- `PLANS.md`
- `docs/QUARTERFULL_WORLD_ALIGNMENT.md`
- `memory/quarterfull_world_memory_capsule.json`

### Current Evidence

- User explicitly asked to delete the old worldview and align our worldview with QuarterFull's current data content.
- User then clarified not to keep a legacy prohibition list; related old-world content should be removed from the active local memory and prompt.
- QuarterFull page is logged in and visibly shows `塵行 · 第 1 章 · 塵` with protagonist `陸知行`, companion `愛醬`, the fortress `灰關`, survival pressure, language barrier, soldiers, cultivators, ordinary workers, and repair-craft / relic clues.
- Existing control docs already moved the prior world baseline out of the active new-opening path.
- No existing repo `memory/` directory convention was found, so this milestone creates a simple local JSON memory capsule.

### Implementation Steps

- [x] Step A: Inspect current world-bible / foundation docs and QuarterFull visible draft evidence.
- [x] Step B: Add QuarterFull-aligned local memory capsule JSON.
- [x] Step C: Add alignment note that removes old world material from active generation use.
- [x] Step D: Verify JSON parsing and diff hygiene.
- [x] Step E: Remove legacy old-world exclusion content from the local memory capsule and alignment note.
- [x] Step F: Re-verify JSON parsing and diff hygiene after deletion.

## Prior Implementation Plan

### Goal

Implement the first read-only prose-to-script packet implementation slice: a `validate-script-packet-draft` Kotlin/JVM CLI command that validates a draft script packet JSON for structure, visible causality, actor/knowledge boundaries, asset/storyboard readiness, and author-only leakage risk. This is a validation tool only, not an automatic prose converter and not a formal approval gate.

### Strategy

Mirror existing read-only audit commands:

- define serializable draft packet and validation report models in the model layer;
- add a pure service that validates packet drafts without opening SQLite or mutating runtime state;
- expose the service through `Cli().run(...)` with `--packet <path>` and optional `--out <path>`;
- add one neutral sample draft under `samples/`;
- add focused Kotlin tests for the service and CLI;
- verify with `git diff --check` and full Gradle tests because this milestone touches source code.

### Non-Goals

- No automatic novel-to-script conversion.
- No AI API, OpenAI API, GPT prompt execution, Seedance, Oiioii, animation, image generation, or storyboard image generation.
- No web server, API server, UI, React, Next.js, Docker, Android UI, FastAPI, Express, Ktor server, Spring server, or Rust.
- No SQLite writes, migrations, setting-pack apply, formal scene creation, `resolve-turn` changes, Validator bypass, `StateReducer` changes, `contextHash` changes, or `stateVersion` changes.
- No treating draft script packets as canon, setting-pack data, runtime scene definitions, AI result JSON, approved storyboard frames, or user-approved output.
- No hardcoding project-specific characters, Qingli content, copyrighted source material, or author-only secrets as engine rules.

### Scope

Allowed tracked files for this implementation slice:

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/ScriptPacketDraftValidationService.kt`
- `app/src/test/kotlin/com/localnovel/engine/ScriptPacketDraftValidationTest.kt`
- `samples/script_packet_draft_sample.json`

### Current Evidence

- `Main.kt` already has read-only CLI commands such as `audit-context`, `audit-narrative-quality`, and `explain-context-budget`.
- `audit-narrative-quality` reads JSON files, calls a pure service, optionally writes a report, prints a compact summary, and returns success without touching SQLite.
- `Models.kt` already contains `@Serializable` report models for read-only audit output.
- `Mvp7NarrativeQualityAuditTest.kt` shows the project test pattern for service-level assertions and `Cli().run(...)` output-file checks.
- `JsonSupport.json` uses kotlinx serialization with `ignoreUnknownKeys=true`, so draft packets can evolve while old validators remain tolerant.

### Implementation Steps

- [x] Step A: Inspect current CLI, model, service, sample, and test patterns.
- [x] Step B: Update `PLANS.md` with the implementation slice, scope, and non-goals.
- [x] Step C: Add script packet draft and validation report models.
- [x] Step D: Add pure read-only validation service.
- [x] Step E: Add `validate-script-packet-draft` CLI wiring and help text.
- [x] Step F: Add neutral sample draft JSON.
- [x] Step G: Add focused service and CLI tests.
- [x] Step H: Run `git diff --check`, full Gradle tests, and one manual CLI smoke test.

## Prior Docs Milestone Goal

Create a docs-only novel-to-script packet blueprint that defines how prose, novel chapters, or turn narratives can be transformed into a script-ready scene packet and later storyboard frames without implementing runtime code, CLI commands, schema, queryers, asset registry, image generation, or animation tooling.

## Global Strategy

Use current repo evidence to bridge the existing authoring/runtime side and the future storyboard production side:

- input is prose or existing narrative output, not raw DB dumps;
- output is a script-ready scene packet compatible with `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`;
- later storyboard frames follow `docs/STORYBOARD_AND_CAMERA_PLANNING.md`;
- the transformation must preserve simulation-first causality, actor knowledge boundaries, scene locks, and author-only boundaries;
- the document should be concrete enough to guide a future template or CLI milestone, but must not approve implementation.

The intended product pattern is low-friction author intake with high-discipline output: accept messy prose, extract scene beats, separate user-facing material from review-only and production-only notes, then prepare storyboard-safe structure. This is how the project can eventually offer QuarterFull-like ease while staying local, auditable, and storyboard-first.

## Non-Goals

- No Kotlin source changes.
- No tests or runtime behavior changes.
- No schema or migration changes.
- No Validator, StateReducer, `resolve-turn`, `stateVersion`, or `contextHash` changes.
- No direct SQLite writes.
- No `mvp-6` retagging.
- No deletion of `data/`, `out/`, or backups.
- No committing ignored artifacts.
- No hardcoding Qingli, Ai-chan, specific NPCs, or copyrighted work elements as engine core.
- No treating Qingli as the new opening base after the world-bible rebuild decision.
- No treating Seedance, Oiioii, or high-quality video generation as the current final deliverable or implemented system.
- No filling in worldbuilding answers on behalf of the user.
- No turning questionnaire prompts into canon, setting-pack data, runtime fields, or asset records.
- No implementing World Card Queryer, Asset Queryer, Story Card Queryer, 09 canon data, runtime enforcement, schema, CLI, SQLite tables, or external automation.
- No treating 11 / 12 candidate rules as canon, runtime rules, setting-pack content, or worldbuilding answers.
- No inserting candidate world content such as future Earth, great transformation, qi, or ley-line material into project docs.
- No treating blueprint pool entries as canon, setting-pack content, runtime data, approved assets, or permission to start 09/04/05/06/07/08.
- No archiving, waking, scheduling, or assigning external windows from this repo milestone; only document the window archive and handoff rules supplied by 01.
- No bulk converting the blueprint pool into setting packs or runtime data.
- No restoring mojibake / corrupted source names by guessing.
- No changing `ROADMAP.md` or `docs/HANDOFF.md` unless the decision brief exposes a current control-doc contradiction.
- No automatic prose rewriting, final script generation, storyboard image generation, GPT prompt execution, Seedance / Oiioii work, or AI API calls.
- No treating a conversion template as canon, approved scene packet, approved storyboard frame, or user-approved output.

## Scope

Tracked files allowed:

- `PLANS.md`
- `docs/NOVEL_TO_SCRIPT_PACKET_BLUEPRINT.md`

Docs to inspect:

- `AGENTS.md`
- `PLANS.md`
- `ROADMAP.md`
- `VERIFY.md`
- `DONE_CRITERIA.md`
- `docs/HANDOFF.md`
- `docs/PROJECT_COMPLETION_AUDIT.md`
- `docs/BLUEPRINT_ALIGNMENT.md`
- `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md`
- `docs/DEMAND_DRIVEN_QUERY_LAYER_BLUEPRINT.md`
- `docs/SIMULATION_FIRST_KNOWLEDGE_TRUTH_RULES.md`
- `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md`
- `docs/STORYBOARD_AND_CAMERA_PLANNING.md`
- `docs/PRODUCTION_ASSET_REGISTRY_PLANNING.md`
- `docs/ASSET_REGISTRY_IMPLEMENTATION_BLUEPRINT.md`
- `docs/ARCHITECTURE.md`
- `docs/DATA_MODEL.md`
- `docs/BLUEPRINT_REDESIGN_DECISION_BRIEF.md`

## Confirmed Facts

- User approved proceeding with a docs-only `NOVEL_TO_SCRIPT_PACKET_BLUEPRINT` after the assistant proposed the `小說內容 -> 劇本場景包` milestone.
- `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md` defines a script-ready scene packet as structured production data for 05 review, 06 display/intake, 07 asset planning, and 08 GPT storyboard pickup/review.
- `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md` says a script-ready scene packet is not final novel prose, not a formal setting-pack patch, not a runtime scene definition, not AI result JSON, and not a direct player-facing transcript.
- `docs/STORYBOARD_AND_CAMERA_PLANNING.md` says storyboard frames are a future child layer under an approved or candidate scene packet and should preserve scene lock, asset awareness, camera purpose, blocking, and do-not-show boundaries.
- `docs/BLUEPRINT_REDESIGN_DECISION_BRIEF.md` defines the product spine as low-friction author input -> new-world foundation brief -> candidate triage -> 09 cards -> query templates -> script-ready scene packet -> reviewable storyboard packet -> optional future implementation only after approval.
- Current working tree already contains prior docs-only changes to `PLANS.md` and untracked `docs/BLUEPRINT_REDESIGN_DECISION_BRIEF.md`; this milestone must work with those changes and not revert them.
- Current branch is `codex/mvp6-checkpoint-branching-spec` and is synchronized with `origin/codex/mvp6-checkpoint-branching-spec` at `68d6be9`.
- The latest commit is `68d6be9 docs: register blueprint planning data layer`.
- `docs/BLUEPRINT_ALIGNMENT.md` says the external blueprint is `KEEP_AS_DESIGN_INTENT_NOT_CURRENT_STATE`; stale operational notes must not drive current work.
- `ROADMAP.md` states the current final deliverable target is reviewable storyboard images / storyboard packets, not high-quality animation.
- `ROADMAP.md` states 04 / 05 / 06 / 07 / 08 are paused and may resume only through manual handoff.
- `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md` registers the current blueprint pool as `BLUEPRINT_PLANNING_REGISTERED` and marks all entries as non-canon candidates.
- `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md` says no blueprint pool entry is canon, setting-pack content, runtime data, approved asset data, SQLite data, or permission to start 09 / 04 / 05 / 06 / 07 / 08.
- Several human-readable Chinese names in `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md` appear mojibake/corrupted in the current file; ids, source windows, source versions, systems, and verdicts are still readable enough for planning, but names must not be restored by guessing.
- `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md` is the current user / 00 input surface before a new-world foundation brief.
- `docs/DEMAND_DRIVEN_QUERY_LAYER_BLUEPRINT.md` defines future product-layer queryers only; it does not implement runtime, schema, CLI, SQLite, external API, GPT automation, Seedance, or Oiioii work.
- `docs/SIMULATION_FIRST_KNOWLEDGE_TRUTH_RULES.md` says bottom-layer truth is world causality, not a plot objective or reveal schedule.
- `docs/ASSET_REGISTRY_IMPLEMENTATION_BLUEPRINT.md` says the formal SQLite asset registry is a future phase only and requires a separate validation / preview / apply flow.
- `docs/ARCHITECTURE.md` confirms AI/Codex may produce result JSON only, and formal SQLite writes remain behind `resolve-turn`, validator checks, `stateVersion`, `contextHash`, and transaction boundaries.
- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Working tree was clean at milestone start.
- `ROADMAP.md` records MVP-7 narrative-quality work as completed read-only audit/spec work and does not approve Candidate B.
- `VERIFY.md` requires `git diff --check` and normally `gradlew.bat clean test --no-daemon`; the current user instruction allows skipping full Gradle for docs-only if explained.
- `DONE_CRITERIA.md` says documentation/control milestones need docs to match current behavior and `git diff --check` to pass.
- Search found `docs/MVP7_NARRATIVE_QUALITY_SPEC.md` as the existing narrative-quality spec.
- Search did not find an existing dedicated authoring quality guidelines file that separates engine layer rules from content / setting pack layer specifics.
- User instruction for this milestone explicitly requests docs-only authoring quality / style guide / review workflow work and forbids runtime/schema/Validator/StateReducer changes.
- User instruction for the current milestone explicitly requests docs-only script-ready scene packet, scene registry, 04 generation, 05 review, and 06 display contracts before 04/05/06 resume.
- Active addendum states that the project will not use automation / scheduler handoff for this workflow. Human approval must follow `01 explicit handoff -> 04 generation -> 05 review -> 06 display -> user explicit approval -> report to 01 -> 01 assigns next step`.
- Existing docs already include authoring review gate guidance and scene lock / animation asset boundary guidance, but search did not find a dedicated script-ready scene packet specification.
- 03 passed commit `63cc2f17b968dd9f1c9fff602150802d1ebf2ff2` (`docs: define script-ready scene packet contracts`).
- `ROADMAP.md`, `VERIFY.md`, `DONE_CRITERIA.md`, and `docs/HANDOFF.md` still described an older control state before the script-ready packet contract track.
- User instruction for the current milestone explicitly allows docs-only verification without full Gradle when explained, but code/runtime/schema/CLI milestones must keep full tests and E2E.
- 03 passed commit `834533e6e3ea7fa18894b1e7eb0391c964c3364f` (`docs: align project control after script packet contracts`).
- `ROADMAP.md` lists `scene registry seed brief` as a safe docs-only next candidate.
- The Pilot-2 setting pack file exists at `out/pilot2/bootstrap/pilot2_setting_pack.json`.
- The formal scene ids confirmed from docs and setting pack are `scene-qingli-herb-warehouse-perimeter`, `scene-qingli-herb-awning`, `scene-qingli-runoff-channel`, and `scene-qingli-outer-waiting-line`.
- The setting pack has scene contracts with forbidden reveal boundaries for those scenes; this milestone must record the boundary without copying secret contents into user-facing prose.
- 03 passed commit `c248cefd0dcde8a878aae290bbb3215ca4d5cb1b` (`docs: seed Qingli scene registry brief`).
- `ROADMAP.md` and `docs/HANDOFF.md` still list scene registry seed brief as a future recommended candidate, so they need status alignment.
- User instruction for this milestone forbids moving directly to Pilot-2 human turn collection, Candidate A.3, Candidate B, MVP-8, runtime enforcement, Oiioii integration, automation, or 04/05/06 restart.
- 03 passed commit `bbcd1df6bbfaebc7834eea1338249eaa70a55772` (`docs: align control docs after Qingli scene registry seed`).
- `ROADMAP.md` now recommends `scene packet sample / registry dry-run review` as a safe docs-only next candidate.
- User instruction for this milestone requires a dry-run review only. It is not 04 generation, 05 formal review gate, 06 display, formal turn, AI result JSON, setting-pack patch, runtime scene definition, approved animation asset lock, or Oiioii integration.
- 03 passed commit `ee80a16f83825ac56b2449285c19981a81f746a1` (`docs: dry-run Qingli script packet registry review`).
- `ROADMAP.md` and `docs/HANDOFF.md` still list the dry-run review as a safe next candidate, so they need status alignment.
- User instruction for this milestone says the completed dry-run review proves the spec and seed brief can support a conservative sample, but does not approve 04/05/06 restart, formal Turn work, setting-pack patch, runtime/schema/Oiioii integration, or treating the sample as user-approved content.
- User requested a not-yet-implemented future planning document inside the project for storyboard images, scene locking for storyboard frames, camera language /運鏡, GPT-generated storyboard images, and future Oiioii-oriented workflow.
- Existing docs already define script-ready scene packets and scene lock / animation asset boundary, but do not yet contain a dedicated storyboard / camera planning note.
- User identified a severe missing production layer: characters, scenes, props, weapons, costumes, and important objects must be locked after adoption so GPT and Oiioii do not regenerate drifting versions every time.
- Existing docs mention scene registry and allowed props, but do not yet define a dedicated production asset registry planning document with reference requirements such as three-view assets, panorama / zone maps, variants, and lifecycle states.
- User agreed that long-term asset data volume will require SQLite for approved assets, while unreviewed asset candidates should remain in draft files until human / review approval.
- Existing project rules still forbid direct SQLite writes and require formal state-changing work to go through approved validation / apply flows.
- 03 passed `docs/WORLD_BIBLE_REBUILD_DECISION_BRIEF.md` in commit `657e708cdf7514474f8a865e569f0d7da5769051`; Qingli should not be used as the new opening base or assetization starting point.
- User explicitly decided the current final deliverable is reviewable storyboard images / storyboard packets, not high-quality animation.
- User explicitly allowed this milestone to organize and submit the existing dirty storyboard / asset planning docs if they are rewritten toward the storyboard-first direction.
- The required product flow for future manual work is `04 scene packet -> 05 packet review -> 06 human approval -> 07 asset candidates -> 08 GPT storyboard pickup / verification -> user-reviewable storyboard delivery`.
- 03 passed `docs: align storyboard as final deliverable` at commit `16f0c8446d3479bfec03888781c0cdbf6232f767`.
- 01 confirmed the next safe milestone is a docs-only `NEW_WORLD_FOUNDATION_QUESTIONNAIRE`.
- User / 01 instruction for this milestone says the questionnaire is not canon, not a setting pack, not a 04 generation packet, not storyboard pickup, and must not be pre-filled by 02.
- 03 passed `docs: add new world foundation questionnaire` at commit `0d24be24d67820a60351b031014b7c8086a397f9`.
- 01 confirmed this milestone may proceed docs-only without additional user decision.
- The current final deliverable remains reviewable storyboard images / storyboard packets, not high-quality animation.
- 04/05/06/07/08 remain paused and manual-handoff only.
- The three demand-driven queryers are future implementation paths, not existing capabilities.
- 03 passed `docs: define demand-driven query layer blueprint` at commit `f26e79b2fc224dce00a1e4afc4fa9504399b0e88`.
- Thread `019ef199-68fe-7391-959a-d14ee199a6a2` produced a v0.1 Simulation-First Knowledge And Truth Rules candidate without modifying the repo.
- Thread `019ef198-c653-7620-942a-97a29e9b17db` reviewed that candidate as PASS, with scores 5/5 for simulation priority, distributed knowledge, anti-forced-reveal, workflow clarity, and overreach risk 0/5.
- Thread `019ef19a-9016-7782-8cf0-c7ccb399ea24` reported `RISKS_FOUND`: no severe overreach landed, but product-layer wording around reveal rhythm / story beats can drift toward plot-first truth handling.
- User / 00 / 01 direction says bottom-layer truth is world causality, not a plot objective; players, NPCs, factions, sects, officials, and ordinary people form partial knowledge from contact, position, profession, education, interest, bias, evidence, and action.
- 03 passed `docs: clean simulation-first source evidence labels` at commit `64dc94a9e6cdbce9b77fa05c6f8a4f66c622cc9f`.
- User agreed that current planning should be arranged into a blueprint layer.
- 01 supplied the blueprint pool source-window index as planning state. 02 treats that handoff as source evidence for a docs-only register, not as independently verified canon or setting-pack content.
- Current final deliverable remains reviewable storyboard images / storyboard packets, not high-quality animation.
- Simulation-first remains the highest content principle for future blueprint, query, packet, asset, and storyboard work.
- Blueprint planning entries are non-canon candidates by default: not canon, not setting pack, not runtime, not approved assets, and not ready for direct SQLite or setting-pack apply.
- 01 supplemental status says `bp-character-lu-hesheng-v0.2` is a non-canon candidate character blueprint: name 陸禾生, sourceWindow 47, sourceVersion v0.2, PASS by 38, settingPackReady false, and waiting for 01. It should not enter the first setting-pack conversion batch by default.
- 01 supplemental status says 34 and 37 remain REVISE pending later 01 decision; 35, 36, and 47 are PASS; 38 is standby.

## Assumptions

- A dedicated `docs/NOVEL_TO_SCRIPT_PACKET_BLUEPRINT.md` is the right artifact because the existing scene packet spec defines the target packet but not the prose-to-packet conversion method.
- The first version should be a transformation blueprint and template, not a sample converter or implementation plan.
- A conservative example may be useful only if it is clearly marked as illustrative and not canon.
- `docs/BLUEPRINT_REDESIGN_DECISION_BRIEF.md` is the right artifact for this request because the user asked for blueprint planning and review, not implementation.
- This milestone can stay docs-only because the requested work is strategic triage of existing blueprints.
- The useful near-term product comparison to QuarterFull is funnel shape and blueprint ergonomics, not its web/mobile/cloud stack.
- `docs/AUTHORING_QUALITY_GUIDELINES.md` is the right location because the requested content is cross-cutting authoring guidance, not a Pilot-only report.
- Full Gradle test is not necessary for this docs-only milestone if `git diff --check` passes and no code/runtime files are touched.

## Open Questions

- Whether the future converter should begin from novel chapters, runtime narrative outputs, author notes, or all three remains a later product decision.
- Whether a future CLI should validate prose-to-packet drafts requires a later implementation decision.
- Whether sample transformation should use invented neutral prose or project-specific material requires later user / 01 approval; this milestone should avoid creating canon story output.
- Which user / 00 answers should fill the new-world foundation questionnaire remains open.
- Whether 01 wants a later `NEW_WORLD_FOUNDATION_BRIEF`, 09 card organization brief, query template brief, or first-batch blueprint conversion brief remains open.
- Whether mojibake/corrupted blueprint names can be restored from source windows 15 / 16 / 17 / 18 / 19 / 21 / 24 / 25 / 27 / 45 / 46 / 47 requires later source-window evidence; this milestone must not guess.
- Whether any non-canon blueprint candidate should be cut permanently rather than deferred requires later 01/user review; this milestone can recommend cut/defer/redesign buckets.
- Whether future milestones should turn parts of the guideline into prompt templates, setting-pack schema fields, or read-only audit checks. This milestone does not decide that.
- Whether future milestones should turn the packet contract into runtime schema, Validator rules, UI forms, or Oiioii integration. This milestone does not decide that.
- Whether 04/05/06 should resume after these contracts is a 01/user decision. This milestone does not resume them.
- Whether Qingli scene registry seed entries should become formal setting-pack fields or approved animation asset locks requires a later 01/user-approved milestone.
- Whether to run a scene packet sample / registry dry-run review is a future 01 handoff decision. This milestone only records it as a safe docs-only candidate.
- Whether to turn any dry-run recommendations into spec edits, packet templates, or formal 04/05/06 work requires a later 01/user-approved milestone.
- Whether to resume 04/05/06, collect Pilot-2 human turns, collect author-led samples, draft Candidate A.3, or run remote restore smoke requires later explicit 01/user input or on-demand selection.
- Whether storyboard image generation should become an implemented tool, Oiioii integration, schema field, or runtime workflow requires later explicit approval. This milestone only records future planning.
- Whether production asset registry should become implemented schema, files, image storage, Validator checks, GPT prompt tooling, or Oiioii integration requires later explicit approval. This milestone only records future planning.
- Whether the future SQLite asset registry belongs inside the existing runtime DB, a separate production DB, or an exportable sidecar DB requires later architecture approval.
- Whether the new world foundation should preserve, rewrite, or replace any non-Qingli production planning examples requires later 01/user approval.
- Which questionnaire answers the user will provide, and which items should be marked "unknown / let 01 propose", remains open until the user / 00 responds.
- Whether World Card Queryer, Asset Queryer, or Story Card Queryer should become templates, files, CLI commands, schema, SQLite tables, or runtime enforcement requires later explicit 01/user approval.
- How 09 will organize new-world cards remains open until the user / 00 answers the questionnaire and 01 drafts a foundation brief.
- Whether future docs should rename Story Card Queryer entirely to Situation / Continuity / Pressure Queryer remains a product wording decision; this milestone may clarify the alias and guardrails without implementing a new capability.
- Which blueprint candidates 01 may later select for a first minimal setting-pack skeleton remains open; this milestone only records the candidate pool and conversion rule.
- Whether any derived aliases should become reviewed blueprint ids requires future review; they must not be treated as 14 PASS original card names.
- Whether archived windows should be physically archived in the app is outside this repo milestone; this milestone only records the archive rules provided by 01.

## Implementation Steps

- [x] Step 1: Read AGENTS, PLANS, ROADMAP, VERIFY, DONE_CRITERIA, and search existing docs for authoring/narrative-quality coverage.
- [x] Step 2: Update `PLANS.md` for this docs-only authoring quality boundary milestone.
- [x] Step 3: Add `docs/AUTHORING_QUALITY_GUIDELINES.md`.
- [x] Step 4: Run `git diff --check` and inspect diff scope.
- [x] Step 5: Commit `docs: add authoring quality guidelines` and push tracked docs only.
- [x] Step 6: Directly notify 03 read-only review thread.
- [x] Step 7: Add micro-prose review addendum for AI calibration phrasing and visible nonverbal action.
- [x] Step 8: Run `git diff --check` for addendum scope.
- [x] Step 9: Commit `docs: add micro-prose authoring review guidance` and push tracked docs only.
- [x] Step 10: Directly notify 03 read-only review thread for the addendum.
- [x] Step 11: Add epoch / material culture / tech-level consistency addendum.
- [x] Step 12: Run `git diff --check` for tech-level addendum scope.
- [x] Step 13: Commit `docs: add material culture consistency guidance` and push tracked docs only.
- [x] Step 14: Directly notify 03 read-only review thread for the tech-level addendum.
- [x] Step 15: Add protagonist reaction beats addendum.
- [x] Step 16: Run `git diff --check` for protagonist reaction addendum scope.
- [x] Step 17: Commit `docs: add protagonist reaction beat guidance` and push tracked docs only.
- [x] Step 18: Directly notify 03 read-only review thread for the protagonist reaction addendum.
- [x] Step 19: Add initial survival orientation and goal transition addendum.
- [x] Step 20: Run `git diff --check` for orientation addendum scope.
- [x] Step 21: Commit `docs: add opening goal transition guidance` and push tracked docs only.
- [x] Step 22: Directly notify 03 read-only review thread for the orientation addendum.
- [x] Step 23: Add sensory attribution and body-part logic addendum.
- [x] Step 24: Run `git diff --check` for sensory attribution addendum scope.
- [x] Step 25: Commit `docs: add sensory attribution guidance` and push tracked docs only.
- [x] Step 26: Directly notify 03 read-only review thread for the sensory attribution addendum.
- [x] Step 27: Add narrative review gate and eight-point checklist addendum.
- [x] Step 28: Run `git diff --check` for review gate addendum scope.
- [x] Step 29: Commit `docs: add narrative review gate checklist` and push tracked docs only.
- [x] Step 30: Directly notify 03 read-only review thread for the review gate addendum.
- [x] Step 31: Add scene lock and animation asset boundary docs.
- [x] Step 32: Run `git diff --check` for scene lock addendum scope.
- [x] Step 33: Commit `docs: add scene lock asset boundary guidance` and push tracked docs only.
- [x] Step 34: Directly notify 03 read-only review thread for the scene lock addendum.
- [x] Step 35: Add script-ready scene packet, scene registry, 04 generation, 05 review, 06 display, human approval, and 01/02/03 loop contracts.
- [x] Step 36: Run `git diff --check` and inspect diff/stat/staged scope for script packet contract docs.
- [x] Step 37: Commit `docs: define script-ready scene packet contracts` and push tracked docs only.
- [x] Step 38: Directly notify 03 read-only review thread for the script packet contract milestone.
- [x] Step 39: Align ROADMAP / VERIFY / DONE_CRITERIA / HANDOFF with completed script packet contracts, manual handoff only, and docs-only verification rules.
- [x] Step 40: Run `git diff --check` and inspect diff/stat/staged scope for project-control docs alignment.
- [x] Step 41: Commit `docs: align project control after script packet contracts` and push tracked docs only.
- [x] Step 42: Directly notify 03 read-only review thread using a 03 verifier-window prompt.
- [x] Step 43: Add Qingli scene registry seed brief for the four formal Pilot-2 scene ids, keeping author-only boundaries non-public.
- [x] Step 44: Run `git diff --check` and inspect diff/stat/staged scope for the scene registry seed brief.
- [x] Step 45: Commit `docs: seed Qingli scene registry brief` and push tracked docs only.
- [x] Step 46: Directly notify 03 read-only review thread using a 03 verifier-window prompt.
- [x] Step 47: Align ROADMAP and HANDOFF after 03 passed the Qingli scene registry seed brief.
- [x] Step 48: Run `git diff --check` and inspect diff/stat/staged scope for the scene-registry control docs alignment.
- [x] Step 49: Commit `docs: align control docs after Qingli scene registry seed` and push tracked docs only.
- [x] Step 50: Directly notify 03 read-only review thread using a 03 verifier-window prompt.
- [x] Step 51: Add a docs-only Qingli script packet dry-run review with one conservative historical sample packet.
- [x] Step 52: Run `git diff --check` and inspect diff/stat/staged scope for the dry-run review.
- [x] Step 53: Commit `docs: dry-run Qingli script packet registry review` and push tracked docs only.
- [x] Step 54: Directly notify 03 read-only review thread using a 03 verifier-window prompt.
- [x] Step 55: Align ROADMAP and HANDOFF after 03 passed the Qingli script packet registry dry-run review.
- [x] Step 56: Run `git diff --check` and inspect diff/stat/staged scope for the dry-run control-doc alignment.
- [x] Step 57: Commit `docs: align control docs after Qingli dry-run review` and push tracked docs only.
- [x] Step 58: Directly notify 03 read-only review thread using a 03 verifier-window prompt.
- [x] Step 59: Inspect existing script packet, scene lock, and authoring guideline docs for storyboard / camera planning overlap.
- [x] Step 60: Add a docs-only future planning note for storyboard frames, scene lock, camera intent, and Oiioii-oriented image/storyboard workflow.
- [x] Step 61: Run `git diff --check` and inspect diff scope for the storyboard planning note.
- [x] Step 62: Add docs-only production asset registry planning for locked characters, scenes, props, weapons, costumes, variants, and required reference views.
- [x] Step 63: Cross-reference asset registry planning from storyboard / scene-lock / script-packet docs.
- [x] Step 64: Run `git diff --check` and inspect diff scope for asset registry planning.
- [x] Step 65: Add docs-only asset registry implementation blueprint covering draft files, SQLite formal registry, validate / preview / apply / export / audit flow, and future engineering phases.
- [x] Step 66: Align ROADMAP and HANDOFF with the future asset registry blueprint as a recommended milestone, without approving schema or runtime work.
- [x] Step 67: Run `git diff --check` and inspect diff scope for the blueprint milestone.
- [x] Step 68: Read the dirty storyboard / asset planning docs, world-bible rebuild decision brief, ROADMAP, HANDOFF, and current PLANS before editing.
- [x] Step 69: Update `PLANS.md` for the storyboard-first final deliverable alignment milestone and dirty tree handling.
- [x] Step 70: Align storyboard / camera, production asset registry, implementation blueprint, scene lock, and script packet docs to storyboard-first delivery and optional downstream video.
- [x] Step 71: Align `ROADMAP.md` and `docs/HANDOFF.md` with the storyboard-first final deliverable and safe next steps.
- [x] Step 72: Run `git diff --check`, inspect diff/stat/status, commit, push, and directly notify 03 for read-only verification.
- [x] Step 73: Read AGENTS, PLANS, ROADMAP, HANDOFF, world-bible rebuild, storyboard, asset, and packet docs before the new-world questionnaire milestone.
- [x] Step 74: Update `PLANS.md` for the docs-only new world foundation questionnaire milestone.
- [x] Step 75: Add `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md` without filling in user answers.
- [x] Step 76: Run `git diff --check`, inspect diff/stat/status/staged files, commit, push, and directly notify 03 for read-only verification.
- [x] Step 77: Update `PLANS.md` for the docs-only demand-driven query layer blueprint milestone.
- [x] Step 78: Add `docs/DEMAND_DRIVEN_QUERY_LAYER_BLUEPRINT.md` defining World Card Queryer, Asset Queryer, and Story Card Queryer.
- [x] Step 79: Add narrow cross-references in roadmap, handoff, script packet, asset registry, storyboard, and questionnaire docs without implying implementation.
- [x] Step 80: Run `git diff --check`, inspect diff/stat/status/staged files, commit, push, and directly notify 03 for read-only verification.
- [x] Step 81: Update `PLANS.md` for the docs-only simulation-first knowledge / truth rules milestone.
- [x] Step 82: Add `docs/SIMULATION_FIRST_KNOWLEDGE_TRUTH_RULES.md` as the product-layer guardrail anchor.
- [x] Step 83: Revise relevant docs to replace plot/reveal-first wording with observation, evidence, causality, local misconception, and actor-knowledge wording.
- [x] Step 84: Run `git diff --check`, inspect search results / diff scope / status, commit, push, and directly notify 03 for read-only verification.
- [x] Step 85: Update `PLANS.md` for the docs-only blueprint data layer and planning register milestone.
- [x] Step 86: Add `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md` with schema, non-canon candidate pool, retained character candidate intake, derived alias rule, setting-pack conversion rule, archive rules, and manual handoff rules.
- [x] Step 87: Add narrow `ROADMAP.md`, `docs/HANDOFF.md`, and relevant cross-references without implying canon, setting-pack, runtime, approved asset, or window activation.
- [x] Step 88: Run `git diff --check`, inspect diff/stat/status/staged files, commit, push, and directly notify 03 for read-only verification.
- [x] Step 89: Read current blueprint, roadmap, handoff, foundation, query, simulation, storyboard, asset, architecture, and data-model evidence for the blueprint redesign request.
- [x] Step 90: Update `PLANS.md` for the docs-only blueprint redesign decision brief milestone.
- [x] Step 91: Add `docs/BLUEPRINT_REDESIGN_DECISION_BRIEF.md` with preserve / cut / redesign / postpone decisions.
- [x] Step 92: Run `git diff --check`, inspect diff/stat/status, and report docs-only verification.
- [x] Step 93: Read current scene packet, storyboard, and blueprint redesign evidence for the novel-to-script packet request.
- [x] Step 94: Update `PLANS.md` for the docs-only novel-to-script packet blueprint milestone.
- [x] Step 95: Add `docs/NOVEL_TO_SCRIPT_PACKET_BLUEPRINT.md` defining prose/chapter/turn narrative to script-ready scene packet transformation.
- [x] Step 96: Run docs-only verification: `git diff --check`, status/stat inspection, and manual whitespace checks for new untracked docs.

## Verification

Required:

- `git status`
- `git diff --check`
- `git diff --stat`

Not planned:

- Full Gradle test, for explicitly docs-only project-control milestones where 01/user permits the docs-only exception and no Kotlin, tests, schema, runtime, or CLI behavior is touched. If the final diff expands beyond docs/control files, rerun full tests.

Safety checks:

- Confirm only `PLANS.md` and `docs/AUTHORING_QUALITY_GUIDELINES.md` are staged.
- For scene lock addendum, also allow `docs/SCENE_LOCK_AND_ANIMATION_ASSET_BOUNDARY.md`.
- For script packet contract milestone, also allow `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`.
- For project-control alignment, also allow `ROADMAP.md`, `VERIFY.md`, `DONE_CRITERIA.md`, and `docs/HANDOFF.md`.
- For Qingli scene registry seed brief, also allow `docs/QINGLI_SCENE_REGISTRY_SEED_BRIEF.md` and a narrow cross-reference in `docs/SCENE_LOCK_AND_ANIMATION_ASSET_BOUNDARY.md` or `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`.
- For post-Qingli scene registry seed control alignment, allow `ROADMAP.md` and `docs/HANDOFF.md`.
- For Qingli scene packet dry-run review, allow `docs/QINGLI_SCRIPT_PACKET_DRY_RUN_REVIEW.md` and narrow cross-references only if needed.
- For post-dry-run review control alignment, allow `ROADMAP.md` and `docs/HANDOFF.md`.
- For storyboard / camera future planning, allow `docs/STORYBOARD_AND_CAMERA_PLANNING.md` and `PLANS.md`.
- For production asset registry future planning, allow `docs/PRODUCTION_ASSET_REGISTRY_PLANNING.md`, `docs/STORYBOARD_AND_CAMERA_PLANNING.md`, `docs/SCENE_LOCK_AND_ANIMATION_ASSET_BOUNDARY.md`, `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`, and `PLANS.md`.
- For asset registry implementation blueprint planning, also allow `docs/ASSET_REGISTRY_IMPLEMENTATION_BLUEPRINT.md`, `ROADMAP.md`, and `docs/HANDOFF.md`.
- For storyboard-first final deliverable alignment, allow `docs/STORYBOARD_AND_CAMERA_PLANNING.md`, `docs/PRODUCTION_ASSET_REGISTRY_PLANNING.md`, `docs/ASSET_REGISTRY_IMPLEMENTATION_BLUEPRINT.md`, `docs/SCENE_LOCK_AND_ANIMATION_ASSET_BOUNDARY.md`, `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`, `ROADMAP.md`, `docs/HANDOFF.md`, and `PLANS.md`.
- For new world foundation questionnaire, allow `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md`, `PLANS.md`, and only minimal `ROADMAP.md` / `docs/HANDOFF.md` if needed.
- For demand-driven query layer blueprint, allow `docs/DEMAND_DRIVEN_QUERY_LAYER_BLUEPRINT.md`, `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`, `docs/ASSET_REGISTRY_IMPLEMENTATION_BLUEPRINT.md`, `docs/STORYBOARD_AND_CAMERA_PLANNING.md`, `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md`, `ROADMAP.md`, `docs/HANDOFF.md`, and `PLANS.md`.
- For simulation-first knowledge / truth rules, allow `docs/SIMULATION_FIRST_KNOWLEDGE_TRUTH_RULES.md`, `docs/DEMAND_DRIVEN_QUERY_LAYER_BLUEPRINT.md`, `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`, `docs/STORYBOARD_AND_CAMERA_PLANNING.md`, `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md`, `docs/WORLD_BIBLE_REBUILD_DECISION_BRIEF.md`, `ROADMAP.md`, `docs/HANDOFF.md`, and `PLANS.md`.
- For blueprint data layer and planning register, allow `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md`, `docs/DEMAND_DRIVEN_QUERY_LAYER_BLUEPRINT.md`, `docs/SIMULATION_FIRST_KNOWLEDGE_TRUTH_RULES.md`, `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md`, `ROADMAP.md`, `docs/HANDOFF.md`, and `PLANS.md`.
- For blueprint redesign decision brief, allow only `docs/BLUEPRINT_REDESIGN_DECISION_BRIEF.md` and `PLANS.md` unless a control-doc contradiction is found.
- For novel-to-script packet blueprint, allow only `docs/NOVEL_TO_SCRIPT_PACKET_BLUEPRINT.md` and `PLANS.md` unless a narrow cross-reference is explicitly needed.
- Confirm no ignored `data/` or `out/` artifacts are staged.
- Confirm `mvp-6` remains unchanged.

## Risks

- Accidentally turning playtest-specific Qingli/Ai-chan/NPC details into engine requirements.
- Accidentally implying Candidate B or runtime quality gates are approved.
- Overfitting anti-AI-writing guidance into brittle phrase bans instead of review principles.
- Copying or imitating protected external story style too closely.
- Accidentally treating a `sceneCandidate` or script packet draft as approved canon, setting-pack data, or animation asset lock.
- Accidentally making 04/05/06 look active before 01 explicitly resumes that workflow.
- Accidentally broadening docs-only verification exceptions to code/runtime/schema/CLI milestones.
- Accidentally implying automation / scheduler may continue the 04/05/06 handoff without manual 01/user approval.
- Accidentally copying author-only forbidden reveal text into a public scene registry.
- Accidentally treating conservative scene seed entries as final canon, schema, runtime scene definitions, or approved production asset locks.
- Accidentally leaving completed scene registry seed work listed as a next candidate.
- Accidentally implying the next step can run Pilot turns or restart 04/05/06 without user / 01 handoff.
- Accidentally treating a dry-run sample packet as formal story output, user-approved content, setting-pack patch, formal turn result, or a 05/06 deliverable.
- Accidentally leaving completed dry-run review work listed as a next candidate.
- Accidentally implying future human turn collection, author-led samples, Candidate A.3, remote restore, or 04/05/06 resumption can proceed without explicit 01/user decision.
- Accidentally treating storyboard image generation, camera fields, Oiioii prompt output, or storyboard-frame locks as implemented runtime/tooling rather than future planning.
- Accidentally treating planned asset registry entries, reference images, three-view sheets, panoramas, variants, or asset lifecycle states as implemented runtime/tooling or approved production assets.
- Accidentally implying SQLite schema, migrations, import CLI, or asset Validator work is approved by the blueprint document.
- Accidentally treating Seedance / Oiioii / high-quality video generation as the current deliverable rather than optional future downstream work.
- Accidentally treating Qingli historical samples as the new-world base after the world-bible rebuild decision.
- Accidentally treating 07 asset candidates as approved references or production locks before review and explicit approval.
- Accidentally answering the questionnaire instead of leaving it for user / 00 input.
- Accidentally turning questionnaire options into canon, setting-pack data, runtime fields, or approved storyboard / asset work.
- Accidentally treating World Card Queryer, Asset Queryer, or Story Card Queryer as implemented capability, runtime ContextPacket enforcement, SQLite schema, CLI, external API, or approval to restart 04/05/06/07/08.
- Accidentally preserving reveal / hidden-truth wording that makes world causality look like a plot objective, reveal schedule, or forced mystery product.
- Accidentally weakening author-only boundaries while removing reveal-oriented wording.
- Accidentally treating simulation-first guardrails as runtime enforcement, canon world content, setting-pack patch, or approval to start 09/04/05/06/07/08.
- Accidentally treating non-canon blueprint candidates as canon, setting-pack content, runtime data, approved assets, or approval to start 09/04/05/06/07/08.
- Accidentally letting derived blueprint aliases impersonate 14 PASS original card names.
- Accidentally implying the whole blueprint pool should be bulk-converted into a setting pack or runtime.
- Accidentally turning window archive rules into live app actions rather than documented handoff policy.

## Drift Log

- 2026-06-24: Started docs-only blueprint redesign decision brief after the user requested blueprint planning and review of what to cut or redesign. Scope is strategic triage only: no code, schema, runtime, queryer, setting-pack conversion, asset registry implementation, 04/05/06/07/08 activation, Candidate B, MVP-8, or external platform work.
- 2026-06-24: Added `docs/BLUEPRINT_REDESIGN_DECISION_BRIEF.md` with preserve / cut / redesign / postpone decisions. `git diff --check` passed for tracked changes, and manual trailing-whitespace checks passed for both changed docs. Full Gradle is intentionally skipped because this milestone changes only docs and `PLANS.md`.
- 2026-06-24: Started docs-only novel-to-script packet blueprint after the user approved proceeding. Scope is transformation planning only: no converter, CLI, schema, runtime, setting pack, queryer implementation, image generation, animation tooling, or 04/05/06/07/08 activation.
- 2026-06-24: Added `docs/NOVEL_TO_SCRIPT_PACKET_BLUEPRINT.md` defining prose/chapter/turn narrative -> script-ready scene packet transformation stages, field mapping, review checklist, draft template, and future CLI boundaries. `git diff --check` passed for tracked changes, and manual trailing-whitespace checks passed for `PLANS.md`, `docs/BLUEPRINT_REDESIGN_DECISION_BRIEF.md`, and `docs/NOVEL_TO_SCRIPT_PACKET_BLUEPRINT.md`. Full Gradle is intentionally skipped because this milestone changes only docs and `PLANS.md`.
- 2026-06-21: Started docs-only authoring quality / style guide / review workflow milestone after 01 classified 04 playtest learning as docs-first evidence, not runtime approval.
- 2026-06-21: Added `docs/AUTHORING_QUALITY_GUIDELINES.md` with engine/content layer separation, anti-AI-writing guidance, NPC consistency checks, review loop, and copyright/style safety. No runtime or schema files touched.
- 2026-06-21: `git diff --check` passed with only Windows line-ending warnings. Full Gradle was intentionally skipped because this milestone changed only docs and `PLANS.md`.
- 2026-06-21: Addendum requested micro-prose review guidance for avoiding AI calibration phrasing such as repeated "not X, only Y", using visible behavior for limits, checking stacked weakening words, and handling animal/nonverbal action through concrete beats. This remains docs-only.
- 2026-06-21: Addendum `git diff --check` passed with only Windows line-ending warnings. Full Gradle remains intentionally skipped because the addendum changed only docs and `PLANS.md`.
- 2026-06-21: Addendum requested epoch / material culture / tech-level consistency guidance, including object material/toolchain checks, modern artifact exceptions, cart wheel versus tire track wording, player-introduced modern objects, and future optional schema ideas that are not approved in this milestone.
- 2026-06-21: Tech-level addendum `git diff --check` passed with only Windows line-ending warnings. Full Gradle remains intentionally skipped because the addendum changed only docs and `PLANS.md`.
- 2026-06-21: Addendum requested protagonist reaction beat guidance for openings where abnormal cues accumulate. The rule requires cue -> notice -> check/adjust -> advance, while keeping truth withheld and avoiding long internal explanation. This remains docs-only.
- 2026-06-21: Protagonist reaction addendum `git diff --check` passed with only Windows line-ending warnings. Full Gradle remains intentionally skipped because the addendum changed only docs and `PLANS.md`.
- 2026-06-22: Addendum requested initial survival orientation and goal transition guidance for displacement openings. The rule requires opening objectives to follow protagonist knowledge boundary and immediate needs before local mysteries become main goals. This remains docs-only.
- 2026-06-22: Opening goal-transition addendum `git diff --check` passed with only Windows line-ending warnings. Full Gradle remains intentionally skipped because the addendum changed only docs and `PLANS.md`.
- 2026-06-22: Addendum requested sensory attribution and body-part logic guidance for animal/nonverbal beats, including smell-to-nose, sound-to-ears, sight-to-gaze, touch-to-body-contact mapping and avoiding mechanical balanced clauses. This remains docs-only.
- 2026-06-22: Sensory attribution addendum `git diff --check` passed with only Windows line-ending warnings. Full Gradle remains intentionally skipped because the addendum changed only docs and `PLANS.md`.
- 2026-06-22: Addendum requested a narrative review gate and eight-point checklist to move line-by-line review away from the user. The flow is draft -> self-check -> review gate -> minimal revision -> user-facing text and remains docs-only.
- 2026-06-22: Narrative review gate addendum `git diff --check` passed with only Windows line-ending warnings. Full Gradle remains intentionally skipped because the addendum changed only docs and `PLANS.md`.
- 2026-06-22: Addendum requested scene lock and animation asset boundary guidance because runtime scene continuity is not the same as animation-ready production asset planning. This remains docs-only and does not add runtime/schema fields.
- 2026-06-22: Scene lock addendum `git diff --check` passed with only Windows line-ending warnings. Full Gradle remains intentionally skipped because the addendum changed only docs and `PLANS.md`.
- 2026-06-22: New docs-only milestone requested script-ready scene packet, scene registry, 04 generation, 05 review, 06 display, and 01/02/03 loop contracts before 04/05/06 resume. This remains docs-only and does not add runtime/schema/Validator gates.
- 2026-06-22: Active addendum clarified no automation / scheduler push for this workflow. 06 is a human approval window; silence, ambiguity, or lack of reply is not approval. User explicit approval must return to 01 before 01 schedules 02/03 or formal Turn work.
- 2026-06-22: Script packet contract docs added. `git diff --check` passed for tracked changes with only Windows line-ending warnings; full Gradle is intentionally skipped because this milestone changes only docs and `PLANS.md`.
- 2026-06-22: Script packet contract milestone is limited to tracked docs. Planned commit is `docs: define script-ready scene packet contracts`; 02 will push to the current branch and directly notify 03 for read-only verification.
- 2026-06-22: 03 passed the script packet contract commit `63cc2f1`. New project-control alignment milestone started to update `ROADMAP.md`, verification criteria, and handoff notes so the repo records manual handoff only and the paused 04/05/06 state.
- 2026-06-22: Project-control alignment remains docs-only. It may skip full Gradle only because no code/runtime/schema/CLI files are touched and 01/user explicitly allowed this docs-only exception; the exception is not generalized to implementation milestones.
- 2026-06-22: Project-control alignment `git diff --check` passed with only Windows line-ending warnings. Full Gradle is intentionally skipped under the explicit docs-only documentation/control exception.
- 2026-06-22: 03 passed project-control alignment commit `834533e`. New docs-only Qingli scene registry seed brief started from existing formal scene ids and public setting pack scene/location/transition evidence. Author-only boundaries are recorded as boundaries only, not copied as reveal content.
- 2026-06-22: Qingli scene registry seed brief added for four formal scene ids. `git diff --check` passed with only Windows line-ending warnings; full Gradle is intentionally skipped because this milestone changes only docs/control files.
- 2026-06-22: 03 passed Qingli scene registry seed brief commit `c248cef`. New control-doc alignment milestone started to mark the seed brief completed/pushed and update recommended candidates.
- 2026-06-22: Post-Qingli scene registry control alignment `git diff --check` passed with only Windows line-ending warnings. Full Gradle is intentionally skipped under the explicit docs-only documentation/control exception.
- 2026-06-22: 03 passed control-doc alignment commit `bbcd1df`. New docs-only scene packet sample / registry dry-run review milestone started. It must verify packet/registry usability without restarting 04/05/06 or creating formal turn / setting-pack / runtime content.
- 2026-06-22: Added `docs/QINGLI_SCRIPT_PACKET_DRY_RUN_REVIEW.md` with a `historical_sample` / dry-run-only packet using `scene-qingli-herb-warehouse-perimeter`. `git diff --check` passed with only Windows line-ending warnings. Full Gradle is intentionally skipped under the explicit docs-only documentation/control exception.
- 2026-06-22: 03 passed dry-run review commit `ee80a16`. New control-doc alignment milestone started to mark the dry-run review completed/pushed and update recommended candidates.
- 2026-06-22: Post-dry-run review control alignment `git diff --check` passed with only Windows line-ending warnings. Full Gradle is intentionally skipped under the explicit docs-only documentation/control exception.
- 2026-06-22: User requested storing a future, not-yet-implemented planning note about storyboard frames, GPT-generated storyboard images, scene lock, camera intent, and Oiioii-oriented animation handoff. Scope is docs-only and does not restart 04/05/06 or implement Oiioii integration.
- 2026-06-22: Added `docs/STORYBOARD_AND_CAMERA_PLANNING.md` as a future-planning-only document. `git diff --check` passed with only Windows line-ending warnings. Full Gradle is intentionally skipped because this milestone changes only docs and `PLANS.md`.
- 2026-06-22: User identified missing production asset locking as a severe gap. New docs-only milestone started to plan an asset registry for characters, scenes, props, weapons, costumes, variants, reference view requirements, GPT draft use, and Oiioii cost boundaries.
- 2026-06-22: Added `docs/PRODUCTION_ASSET_REGISTRY_PLANNING.md` and cross-referenced it from storyboard, scene-lock, and script-packet docs. `git diff --check` passed with only Windows line-ending warnings. Full Gradle is intentionally skipped because this milestone changes only docs and `PLANS.md`.
- 2026-06-22: User approved updating the project construction blueprint. New docs-only milestone started to describe how asset candidates remain in draft files, approved assets later enter SQLite through validate / preview / apply, and downstream packets / storyboards / GPT / Oiioii reference asset ids.
- 2026-06-22: Added `docs/ASSET_REGISTRY_IMPLEMENTATION_BLUEPRINT.md` and aligned `ROADMAP.md` / `docs/HANDOFF.md` so the future asset registry path is draft files first, SQLite formal registry later, and no schema/runtime/Oiioii work is approved by the blueprint alone.
- 2026-06-22: Asset registry blueprint milestone `git diff --check` passed with only Windows line-ending warnings. Full Gradle is intentionally skipped because this milestone changes only docs and `PLANS.md`.
- 2026-06-22: User decided the current final deliverable is reviewable storyboard images / storyboard packets, not high-quality animation. New milestone starts to align dirty storyboard / asset planning docs and control docs to a `04 -> 05 -> 06 -> 07 -> 08 -> user-reviewable storyboard` flow, with Seedance / Oiioii only as optional future downstream work.
- 2026-06-22: Dirty tree handling: the existing modified / untracked storyboard and asset planning docs are explicitly allowed in this milestone if rewritten to the storyboard-first direction and kept docs-only. No runtime, schema, CLI, SQLite, or generated artifact work is approved.
- 2026-06-22: Storyboard-first alignment edited only allowed docs/control files. `git diff --check` and staged scope inspection passed before commit; full Gradle remains intentionally skipped because no code/runtime/schema/CLI files are touched.
- 2026-06-22: 03 passed storyboard-first alignment commit `16f0c84`. New docs-only milestone starts to create `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md` as a user-input questionnaire. It must not answer worldbuilding questions, create canon, patch setting packs, or resume 04/05/06/07/08.
- 2026-06-22: New world foundation questionnaire added as a user / 00 input surface and control docs minimally aligned. `git diff --check`, staged scope inspection, ignored artifact check, and `mvp-6` tag check passed before commit; full Gradle remains intentionally skipped because no code/runtime/schema/CLI files are touched.
- 2026-06-22: 03 passed new world foundation questionnaire commit `0d24be2`. New docs-only milestone starts to define World Card Queryer, Asset Queryer, and Story Card Queryer as future product-layer demand-driven retrieval contracts for 09 -> 04 -> 05 -> 06 -> 07 -> 08, not as implemented runtime/schema/CLI/SQLite behavior.
- 2026-06-22: Demand-driven query layer blueprint added with narrow cross-references. `git diff --check` and staged scope inspection passed; full Gradle remains intentionally skipped because no code/runtime/schema/CLI files are touched.
- 2026-06-23: 03 passed demand-driven query layer commit `f26e79b`. New docs-only milestone starts to integrate simulation-first knowledge / truth guardrails from 11/12/13 thread evidence, correcting product-layer wording without implementing runtime/schema/Validator/CLI behavior or adding world canon.
- 2026-06-23: Added `docs/SIMULATION_FIRST_KNOWLEDGE_TRUTH_RULES.md` and revised product-layer wording in query, packet, storyboard, questionnaire, world-bible, roadmap, and handoff docs. `git diff --check` passed with only Windows line-ending warnings; full Gradle remains intentionally skipped because no code/runtime/schema/CLI files are touched.
- 2026-06-23: Started docs-only blueprint data layer and planning register milestone after user approved arranging current planning into the blueprint layer. Scope is non-canon planning registration only: no setting-pack patch, runtime, SQLite, approved assets, or 09/04/05/06/07/08 activation.
- 2026-06-23: Added `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md`, including the required pool index, 01 supplemental retained character candidate `bp-character-lu-hesheng-v0.2`, setting-pack conversion limits, window archive policy, and manual handoff rules. Added only narrow control-doc and cross-reference updates.
- 2026-06-23: `git diff --check` and staged `git diff --cached --check` passed. Full Gradle is intentionally skipped under the explicit docs-only exception because no code/runtime/schema/CLI files are touched.
