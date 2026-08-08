# WP-001F-1 Diverged File Evidence

## Scope and result

This report compares exactly the nine `REMOTE_DIVERGED` paths from the original 115-entry baseline. It uses three-way evidence: the fetched remote branch, local `HEAD`, and current working-tree content. It does not merge, stage, delete, move, commit, push, compile or repair anything.

Remote reference: `origin/codex/mvp6-checkpoint-branching-spec` at `74a939ae87f6536a95a5497b053b3367c14368f1`.

## Exact paths

1. `PLANS.md`
2. `app/src/main/kotlin/com/localnovel/engine/Main.kt`
3. `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
4. `app/src/main/kotlin/com/localnovel/engine/service/CandidateReferenceAcceptancePacketService.kt`
5. `docs/HANDOFF.md`
6. `docs/PROJECT_STRUCTURE_REPORT.md`
7. `docs/COMIC_VIDEO_PRODUCTION_FEATURE_PLAN.md`
8. `docs/PROJECT_COMPLETION_PROGRESS.md`
9. `docs/QUARTERFULL_WORLD_ALIGNMENT.md`

## Three-way hash and line evidence

`WT` means the current working-tree hash. `HEAD→WT` and `HEAD→remote` are added/deleted line counts relative to local `HEAD`. `remote→WT` is a temporary-file `git diff --no-index` comparison; the temporary exports were outside the repository. For `PLANS.md`, the WT counts include the WP-001F-1 plan section just added as an audit/control change.

| Path | Cluster | Remote blob | HEAD blob | WT hash | Remote last commit | Lines WT / HEAD / remote | HEAD→WT | HEAD→remote | remote→WT |
|---|---|---|---|---|---|---:|---|---|---|
| `PLANS.md` | Repository Governance | `01820951375fce669f30cb1830dc29950656a2a4` | `bf144795c8604c3cbe33cfe47ad756dc2f76f377` | `2edca19ae845b4b5d452f449121db11aa230058a` | `da124bd4` / 2026-07-09 / `feat(mvp8): add migration, models, CLI, specs, and reader-mode wiring` | 3988 / 363 / 24228 | 3738+ / 22- | 23887+ / 22- | 212+ / 20361- |
| `app/src/main/kotlin/com/localnovel/engine/Main.kt` | Core Integration | `cd4918f1f5ffcad946529a12ad7452f16a207871` | `e96d20ff8e1d512734179bfa9695b54e156cd670` | `d166db0a377bce2177fe817842354058685564fa` | `eb9e9795` / 2026-07-09 / `chore: freeze audio-novel experiment and hand off script-first pivot` | 2594 / 933 / 348 | 1661+ / 0- | 270+ / 855- | 2471+ / 225- |
| `app/src/main/kotlin/com/localnovel/engine/model/Models.kt` | Core Integration | `1394bff169f624000f9a10b7f88a160dd51993f1` | `779d7675b708659dcb94bf46389786e69aa9a068` | `af16bb7ee7d7057b80719ca682a0b3093783a4dd` | `eb9e9795` / 2026-07-09 / `chore: freeze audio-novel experiment and hand off script-first pivot` | 4462 / 1399 / 4809 | 3063+ / 0- | 3411+ / 1- | 223+ / 570- |
| `app/src/main/kotlin/com/localnovel/engine/service/CandidateReferenceAcceptancePacketService.kt` | Candidate Reference | `901584bb0fd8e5d678089584c6b2098eb4ef3a68` | `HEAD_MISSING` | `05b58058b0b9035f598df56f4ea617cdee93efc7` | `ab538d3c` / 2026-06-28 / `feat: add local JSON pipeline gates` | 207 / 0 / 237 | 207+ / 0- (HEAD missing) | 237+ / 0- (HEAD missing) | 1+ / 31- |
| `docs/HANDOFF.md` | Repository Governance | `60dcfb23219c6e7d3d1d5e79f19b353b138f4ec1` | `c8d5f4acb6197a5254d1c75fa11ca344037a997b` | `8d612581f9c11b8be72ef0025c8f4b9a69296e0c` | `eb9e9795` / 2026-07-09 / `chore: freeze audio-novel experiment and hand off script-first pivot` | 246 / 240 / 532 | 6+ / 0- | 301+ / 9- | 9+ / 295- |
| `docs/PROJECT_STRUCTURE_REPORT.md` | Repository Governance | `0847351959944080102050b00f5f3a57846952c6` | `3d478bb73011bc045055540c77405d226c766d49` | `3e49dd0a3da893df6ea4e720905dfefb37a08bcd` | `20bceb07` / 2026-06-28 / `docs: add governance register support notes` | 188 / 184 / 188 | 4+ / 0- | 5+ / 1- | 2+ / 2- |
| `docs/COMIC_VIDEO_PRODUCTION_FEATURE_PLAN.md` | Storyboard / Image Candidate | `72da6d3d982209788d360faac39aa2a85e5405991` | `HEAD_MISSING` | `7fbb5915e9bb1db37d966bb525a2c8c863cd86ab` | `3249db20` / 2026-07-07 / `feat: integrate comic-video workbench, local story chain, Chenxing source, and control docs` | 429 / 0 / 432 | 429+ / 0- (HEAD missing) | 432+ / 0- (HEAD missing) | 2+ / 5- |
| `docs/PROJECT_COMPLETION_PROGRESS.md` | Repository Governance | `0818d60c8573b23a9fc0f0816e3cacf599215f44` | `HEAD_MISSING` | `66db04253ee2d0188dd38fc037e5961837c96268` | `3249db20` / 2026-07-07 / `feat: integrate comic-video workbench, local story chain, Chenxing source, and control docs` | 83 / 0 / 102 | 83+ / 0- (HEAD missing) | 102+ / 0- (HEAD missing) | 46+ / 65- |
| `docs/QUARTERFULL_WORLD_ALIGNMENT.md` | Local Memory / Content | `eee54febf9bf8fceb1ba75e92be1a605701c6667` | `HEAD_MISSING` | `f08d406836c0fab7e9f09fa3fe043c06c584c627` | `ba316210` / 2026-06-28 / `docs: clarify memory parser evidence` | 102 / 0 / 104 | 102+ / 0- (HEAD missing) | 104+ / 0- (HEAD missing) | 5+ / 6- |

## Per-file content findings

### `PLANS.md`

- Local unique value: local dirty-worktree audit history and the WP-001E/WP-001F control trail. The current WT also contains the WP-001F-1 plan section, which is audit-generated.
- Remote unique value: a much larger current plan window with MVP-8 reader-mode, migration, model and CLI direction.
- Tests/docs: no test imports a plan; many control documents reference plan sections and governance names.
- Migration/schema: the remote plan mentions migrations and models, but this file itself does not create a schema dependency.
- Mechanical merge: `UNKNOWN`; the 20k-line divergence is not a safe mechanical operation.
- Recommendation: `REQUIRES_MANUAL_MERGE`, high confidence that owner review is required.

### `Main.kt`

- Local unique value: the declaration scan found 96 local-only function names, mostly private CLI handlers and wiring for the candidate, image, novelization, script-packet and storyboard services. Local imports `SqliteStoryRepository`, `AiTurnResultValidator`, `StateReducer` and `ContextPacket`.
- Remote unique value: the remote branch has a smaller core `Main.kt` and separate committed pipeline CLIs, including provisional-layer, asset-image-candidate, novelization and storyboard routes.
- Tests/docs: no test directly invokes the local `Main.kt` handler surface; matching service tests exist. The local CLI command names are referenced by the local `Main.kt` itself and control/audit documents.
- Migration/schema: directly touches the existing repository/runtime/Validator/StateReducer path; no new dirty migration is present.
- Local dependency: yes, it imports the dirty service/model surface, although those services are mostly remote-identical.
- Mechanical merge: `UNKNOWN`; this is a runtime/CLI dispatch conflict.
- Recommendation: `REQUIRES_MANUAL_MERGE`, high confidence.

### `Models.kt`

- Local unique value: 26 local-only type declarations, heavily covering storyboard packet/planning models.
- Remote unique value: 43 remote-only types, including audio-novel, comic-video, delivery-mode, learned-rule, narrative-delivery, setting-image-proof and setting-pack script-packet types.
- Tests/docs: local-only storyboard types are referenced by storyboard services and tests such as `StoryboardPacketDraftValidationTest`, `StoryboardPacketReviewTest`, `StoryboardPlanningBriefTest` and `StoryboardPlanningPreflightTest`.
- Migration/schema: no direct migration or repository call; it is a shared model surface consumed by `Main.kt` and services.
- Mechanical merge: `UNKNOWN`; removing either side can break a different service/test family.
- Recommendation: `REQUIRES_MANUAL_MERGE`, high confidence.

### `CandidateReferenceAcceptancePacketService.kt`

- Local unique value: no local-only public type/function was found. The local copy lacks the remote `sourceFileSha256` comparison in image-result binding and lacks the remote `imageResultIdentityDigest` / `sha256` helpers.
- Remote unique value: an additional source-file hash comparison and deterministic identity digest helper.
- Tests/docs: `app/src/test/kotlin/com/localnovel/engine/CandidateReferenceAcceptancePacketTest.kt` directly instantiates the service; `Main.kt` registers `create-candidate-reference-acceptance-packet`.
- Migration/schema: no direct repository or migration dependency; JSON packet/model boundary only.
- Mechanical merge: `YES_WITH_TESTS` for adopting the remote version in a later clean worktree, because the change is validation semantics and must be tested.
- Recommendation: `REMOTE_SUPERSEDES_LOCAL`, high confidence. This does not authorize replacing the current dirty file now.

### Divergent control/content documents

- `docs/HANDOFF.md`: the remote version has a current first-read override and route-control instructions; local is an older, much shorter handoff. No production/test/migration dependency. Mechanical action could be low risk only after owner review; recommendation `REMOTE_SUPERSEDES_LOCAL`.
- `docs/PROJECT_STRUCTURE_REPORT.md`: the local three-line delta records the actual OneDrive workspace path and local directory-register update, while the remote report retains the older Projects path. Recommendation `LOCAL_HAS_UNIQUE_VALUE` / `PORT_LOCAL_DELTA`; do not overwrite the local path without review.
- `docs/COMIC_VIDEO_PRODUCTION_FEATURE_PLAN.md`: remote adds current local workbench and next-version blueprint references. No runtime dependency. Recommendation `REMOTE_SUPERSEDES_LOCAL`.
- `docs/PROJECT_COMPLETION_PROGRESS.md`: remote is a later closed-phase control estimate and points to the newer voiced-comic progress models; local is an older active 58% estimate. Recommendation `REMOTE_SUPERSEDES_LOCAL`.
- `docs/QUARTERFULL_WORLD_ALIGNMENT.md`: both are content/control documents for local memory alignment; the remote revision has later memory-parser evidence and normalized text. No runtime import. Recommendation `REMOTE_SUPERSEDES_LOCAL`, subject to human content confirmation.

## Evidence limits

No test was run. No semantic merge was performed. Recommendations are not deletion instructions. No file was classified `GENERATED_OR_TEMPORARY` or `SAFE_TO_DROP_LOCAL_COPY` from the available evidence.
