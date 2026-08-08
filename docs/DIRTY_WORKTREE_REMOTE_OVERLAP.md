# WP-001E Dirty Worktree Remote Overlap

## WP-001F-1 Exceptional Path Register

The exact exceptional baseline paths are listed here before any recommendation. This register is evidence-only; no path is being changed or removed.

### `REMOTE_DIVERGED` (9)

- `PLANS.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`
- `app/src/main/kotlin/com/localnovel/engine/service/CandidateReferenceAcceptancePacketService.kt`
- `docs/HANDOFF.md`
- `docs/PROJECT_STRUCTURE_REPORT.md`
- `docs/COMIC_VIDEO_PRODUCTION_FEATURE_PLAN.md`
- `docs/PROJECT_COMPLETION_PROGRESS.md`
- `docs/QUARTERFULL_WORLD_ALIGNMENT.md`

### `LOCAL_ONLY` (2 status entries)

- `docs/PROJECT_PROGRESS_RUBRIC.md`
- `memory/` (directory status entry; the two local files are recorded in `docs/LOCAL_ONLY_FILE_EVIDENCE.md`)

## Scope

This is a read-only comparison of the original 115-entry dirty baseline against `origin/codex/mvp6-checkpoint-branching-spec`. It does not merge, reconcile, stage, commit or push anything. The local worktree remains at `89e522f18a946426232f5d56009fea0262329b47`; the fetched remote reference is `74a939ae87f6536a95a5497b053b3367c14368f1`.

## Method

1. `git fetch --prune origin` refreshed remote references only.
2. `git ls-tree -r --name-only origin/codex/mvp6-checkpoint-branching-spec` provided the remote path inventory: 760 paths.
3. Every baseline path was checked for exact remote-path presence.
4. Every dirty Kotlin path was compared with `git show origin:<path>` after line-ending normalization. API signatures were separately approximated from package, type and function declarations.
5. `git log --all --oneline -- <path>` was used as provenance evidence where a path existed in history. A differing body is never promoted to `REMOTE_EQUIVALENT` merely because the filename or API looks similar.

## Baseline result

| Overlap state | Count | Interpretation |
|---|---:|---|
| `REMOTE_IDENTICAL` | 104 | Exact Git blob content matches the remote path. |
| `REMOTE_EQUIVALENT` | 0 | No semantic equivalence was proven by this audit. |
| `REMOTE_OLDER` | 0 | A dirty working-tree file has no commit timestamp suitable for this label. |
| `REMOTE_NEWER` | 0 | A dirty working-tree file has no commit timestamp suitable for this label. |
| `REMOTE_DIVERGED` | 9 | Same path exists remotely, but content differs; semantic direction remains to be reconciled. |
| `LOCAL_ONLY` | 2 | `docs/PROJECT_PROGRESS_RUBRIC.md` and the local `memory/` status entry have no exact matching remote path. |
| `UNKNOWN` | 0 | No unresolved baseline path remained after the path/hash check. |
| **Total original baseline** | **115** | Audit-generated files are excluded. |

## Program-file evidence

There are 92 dirty Kotlin files: 47 production files and 45 tests. All 92 have an exact remote path.

- Git blob content was identical for 89 files.
- Git blob content differed for 3 files: `app/src/main/kotlin/com/localnovel/engine/Main.kt`, `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`, and `app/src/main/kotlin/com/localnovel/engine/service/CandidateReferenceAcceptancePacketService.kt`.
- Parsed API signatures also differed for those 3 files; the 89 identical files do not require semantic-equivalence inference.
- All 45 dirty service paths have remote counterparts, and all 45 test basenames pair with a dirty service basename. No orphan test was found by this pairing check.

## Remote overlap signals requiring reconciliation

The remote tree contains the dirty candidate/reference/image/novelization/script/storyboard service names with identical content for 89 of the 92 Kotlin paths, plus separate committed lanes such as `ProvisionalLayerCli.kt`, `StoryboardPipelineCli.kt`, `NarrativeQualityGateService.kt`, and `009_provisional_layer.sql`. Exact identity proves the local file is already present remotely; it does not by itself prove the surrounding product lane is approved or that the local dirty worktree may be cleaned.

`Main.kt` and `Models.kt` are the highest-risk overlap. The remote model surface contains additional provisional-layer, narrative-delivery and storyboard-related types that are absent or different in the local dirty model surface. `CandidateReferenceAcceptancePacketService.kt` also has a public API difference.

## Per-file status rule

The status assigned in this package is conservative:

- `REMOTE_IDENTICAL` means the exact Git blob content matches the remote path.
- `REMOTE_DIVERGED` means the exact remote path exists and content differs.
- It does not mean the remote version is newer, correct, compatible or safe to delete locally.
- It does not mean the local version should be copied, cherry-picked or discarded.
- `REMOTE_EQUIVALENT` requires stronger evidence than a matching path, class name or API signature.

## Safety record

No dirty functional file was edited. No file was staged. No pull, merge, rebase, reset, stash, cleanup, commit or push was performed.
