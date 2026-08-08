# Release Hygiene And Local Reproducibility Report

Status: `RELEASE_HYGIENE_REPORT_V1`

Date: 2026-06-27

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

This report is documentation and verification evidence only. It does not delete, move, stage, commit, push, revert, clean, approve, canonize, lock, register, generate media, call providers, write SQLite, or change runtime behavior.

## 1. Scope

This slice follows the `minimal reviewable storyboard packet product artifact` PASS by `03`, with official product progress displayed by `01` as `72%`.

In scope:

- Record the current local release hygiene state.
- Record reproducibility commands for the local JSON pipeline.
- Record dirty worktree caveats without changing the worktree.
- Record stale legacy workspace risks without promoting any legacy path as canonical.
- Recommend the next cleanup / handoff sequence.

Out of scope:

- Kotlin source, tests, schema, migrations, runtime, CLI behavior, SQLite writes, provider jobs, media generation, browser work, remote downloads, formal registry writes, asset approval, approved reference, registered reference, canon, user approval, production lock, or unattended automation.

## 2. Canonical Workspace Status

The canonical workspace for this repo is:

```text
C:\Users\f7212\Projects\AI 劇本
```

Evidence:

- `Get-Location` was run with explicit `workdir` and returned the canonical workspace above.
- All commands used for this report were run with explicit `workdir`.
- No file edit in this slice targeted the legacy workspace.

Legacy workspace risk:

- The thread metadata may still show an old default `cwd`.
- Existing historical docs and plans contain older relocation notes and standalone-tool paths.
- Future edits must continue to set explicit `workdir` and absolute canonical `apply_patch` targets.

## 3. Dirty Tree Inventory

Read-only command used:

```powershell
git status --short --untracked-files=all
```

Inventory counting rule:

- Tracked modified paths are lines not starting with `?? `.
- Untracked entries are lines starting with `?? `.
- Untracked files are classified by path prefix. This command expands untracked files instead of folding directories.
- Classification sum equals the untracked total: `48 + 49 + 11 + 7 + 2 + 0 = 117`.

Observed current inventory:

| Class | Count | Notes |
| --- | ---: | --- |
| Tracked modified paths | 7 | Includes control docs and Kotlin/model files from accepted prior slices. |
| Untracked entries | 117 | Reviewed but not yet committed service/test/docs/sample/memory artifacts from prior slices; counted with `--untracked-files=all`. |
| Untracked service files | 48 | Local JSON gate services from the novelization, script, storyboard, asset, image, and candidate-reference pipeline. |
| Untracked test files | 49 | Focused tests for those gates and product-proof slices. |
| Untracked docs entries | 11 | Blueprint, progress, lifecycle, cleanup, release hygiene, and feature-planning documents. |
| Untracked sample entries | 7 | Deterministic local sample JSON / markdown fixtures. |
| Untracked memory entries | 2 | Local memory support surface, expanded with `--untracked-files=all`. |
| Untracked other entries | 0 | No unclassified untracked entries under this counting rule. |

Interpretation:

- This dirty tree is not automatically a release blocker because many entries correspond to prior `03`-reviewed slices.
- It is a release packaging blocker until `01` decides how to group, review, stage, and commit the accepted artifacts.
- No cleanup, deletion, move, revert, stage, commit, or push was performed by this report.

## 4. Accepted Product-Proof Commands

Recently accepted product-proof / readiness commands include:

```powershell
./gradlew.bat test --tests "*PipelineHappyPathFixtureTest" --no-daemon --console=plain
./gradlew.bat test --tests "*StoryboardStoryMovementAuditTest" --no-daemon --console=plain
./gradlew.bat test --tests "*ReviewableStoryboardPacketTest" --no-daemon --console=plain
```

Meaning:

- `PipelineHappyPathFixtureTest` proves a coherent local JSON chain can reach the human-reviewable storyboard endpoint used in v1.
- `StoryboardStoryMovementAuditTest` proves deterministic story movement readiness checks exist for the text-only storyboard skeleton.
- `ReviewableStoryboardPacketTest` proves the named local JSON product artifact can be created from the audited chain.

This report recommends rerunning at least `ReviewableStoryboardPacketTest` during each release-hygiene pass because it is the newest product artifact boundary.

## 5. Reproducibility Command Set

Minimum docs/control check for this report:

```powershell
git diff --check -- PLANS.md docs/RELEASE_HYGIENE_AND_REPRODUCIBILITY_REPORT.md
```

Recommended focused product-proof checks:

```powershell
./gradlew.bat test --tests "*ReviewableStoryboardPacketTest" --no-daemon --console=plain
./gradlew.bat test --tests "*StoryboardStoryMovementAuditTest" --no-daemon --console=plain
./gradlew.bat test --tests "*PipelineHappyPathFixtureTest" --no-daemon --console=plain
```

Recommended stale workspace checks:

```powershell
rg -n "legacy workspace full path pattern" docs/RELEASE_HYGIENE_AND_REPRODUCIBILITY_REPORT.md PLANS.md
rg -n "OneDrive" docs/RELEASE_HYGIENE_AND_REPRODUCIBILITY_REPORT.md
```

Recommended dirty-tree evidence check:

```powershell
git status --short
```

Recommended release grouping preview:

```powershell
git status --short -- app/src/main/kotlin/com/localnovel/engine/service app/src/test/kotlin/com/localnovel/engine docs samples memory
```

## 6. Release Blockers And Non-Blockers

Release blockers:

- Dirty worktree packaging is unresolved: accepted artifacts have not been grouped into an intentional commit / handoff package.
- Many service/test files are still untracked, so a fresh clone would not reproduce the accepted pipeline until these files are deliberately added.
- Historical docs contain legacy relocation references; the new canonical path rule must remain explicit in future handoffs.
- Full release verification has not yet run across the full test suite after all accepted artifacts.

Current non-blockers:

- The dirty tree itself is not evidence of incorrect behavior; it is expected after many small accepted slices.
- Existing negative test strings such as provider, approval, canon, generated media, and lock wording are allowed when they are used to assert boundaries.
- The older cleanup inventory can remain as historical evidence, but it should not be treated as the current release-hygiene report.

## 7. Next Cleanup Sequence

Recommended next slices:

1. `release grouping preview`: produce a read-only grouping report that maps accepted untracked services/tests/docs/samples into commit-sized packages.
2. `focused reproducibility bundle`: rerun the three product-proof tests and any adjacent gate tests that touch the reviewable storyboard packet chain.
3. `stale path hygiene`: review historical docs for legacy workspace references and decide which are historical notes versus confusing current instructions.
4. `tracked handoff manifest`: write a manifest listing the exact files that must be included for a reproducible local JSON product proof.
5. `release candidate verification`: after `01` approves grouping, run broader Gradle verification before any commit / handoff.

Do not run a cleanup apply step before a separate dry-run preview and explicit `01` approval.

## 8. Boundary Statement

This report does not mean:

- The storyboard is approved.
- The storyboard is canon.
- The storyboard is image-ready, provider-ready, video-ready, audio-ready, or production-ready.
- Any asset, reference, registry entry, runtime scene, or generated media has been approved, locked, registered, or created.
- Any SQLite/runtime state changed.

This report only says:

- The current repo has a named local JSON product artifact.
- The current release-hygiene risk is mostly packaging and reproducibility, not a need for more gate proliferation.
- The next useful work is controlled grouping, focused reproducibility, stale-path hygiene, and handoff manifest preparation.

## 9. Verification Record

Commands run for this slice:

```powershell
git status --short --untracked-files=all
git diff --check -- PLANS.md docs/RELEASE_HYGIENE_AND_REPRODUCIBILITY_REPORT.md
./gradlew.bat test --tests "*ReviewableStoryboardPacketTest" --no-daemon --console=plain
```

Results:

- Inventory command result: tracked modified `7`; untracked total `117`; services `48`; tests `49`; docs `11`; samples `7`; memory entries `2`; other `0`; classified sum `117`.
- `git diff --check` exited 0 with only the existing Windows line-ending warning for `PLANS.md`.
- `ReviewableStoryboardPacketTest` returned `BUILD SUCCESSFUL`.
- Report file trailing-whitespace scan returned no matches.
- Legacy full-path scan returned no matches in this report. It still returns pre-existing historical relocation notes in `PLANS.md`.

Additional checks:

- This slice intentionally changed only `PLANS.md` and this report. `git status` still shows Kotlin/model/service/test changes from prior accepted slices; those are pre-existing release-packaging inventory, not new behavior from this report.
