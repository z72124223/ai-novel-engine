# WP-001F-2 Project Structure Report Delta

## Decision

Exactly one local segment is classified as `VALID_CURRENT_DELTA`: the workspace path in `docs/PROJECT_STRUCTURE_REPORT.md`. It was ported into the remote-based clean worktree. No other local segment was copied.

This is a documentation-only correction of the current checkout location. It does not claim a runtime, model, CLI, migration, asset, or product capability change.

## Baselines and hashes

- Remote baseline SHA: `74a939ae87f6536a95a5497b053b3367c14368f1`
- Clean worktree: `C:\Users\f7212\OneDrive\文件\ai-novel-engine-wp001f2`
- Integration branch: `reconciliation/wp001f2-project-structure`
- Remote `docs/PROJECT_STRUCTURE_REPORT.md` blob: `0847351959944080102050b00f5f3a57846952c6`
- Local dirty snapshot blob: `3e49dd0a3da893df6ea4e720905dfefb37a08bcd`
- Ported worktree file is intentionally a new working-tree version and was not copied from the dirty directory directly.

## Difference classification

| Difference | Classification | Evidence | Decision |
|---|---|---|---|
| `Workspace: C:\Users\f7212\Projects\AI 劇本` → `Workspace: C:\Users\f7212\OneDrive\文件\AI 劇本` | `VALID_CURRENT_DELTA` | `Test-Path C:\Users\f7212\OneDrive\文件\AI 劇本` succeeds; this is the current repository workspace | Ported one line |
| All other report paragraphs | `DUPLICATED_REMOTE_INFO` | The clean worktree already contains the remote report content | Not copied |
| Uncommitted Kotlin/service/model/test descriptions | `STALE_HISTORY` / out of scope | They are not part of this one-line document delta and were not introduced into the clean worktree | Not copied |
| Completion, compile, test or product-direction claims | `UNVERIFIED_CLAIM` | No compile/test was run for this docs-only package | Not added |
| Android, external AI, Sora, Seedance, Oiioii or video-runtime claims | `CONFLICTS_WITH_PROJECT_STATE` / out of scope | This package only corrects the local workspace path | Not added |

## Actual port

The only modified report line is:

```text
Workspace: `C:\Users\f7212\OneDrive\文件\AI 劇本`
```

The full local dirty report was not used as a replacement. The remote report remained the base, and only the verified current path was ported.

## Verified formal paths

The following paths mentioned by this report were verified in the remote tree with `git ls-tree -r --name-only HEAD`:

- `docs/PROJECT_STRUCTURE_REPORT.md`
- `docs/reconciliation/WP001F2_PROJECT_STRUCTURE_DELTA_REPORT.md` (new file in this commit)
- `docs/HANDOFF.md`
- `docs/COMIC_VIDEO_PRODUCTION_FEATURE_PLAN.md`
- `docs/PROJECT_COMPLETION_PROGRESS.md`
- `docs/QUARTERFULL_WORLD_ALIGNMENT.md`
- `app/src/main/kotlin/com/localnovel/engine/Main.kt`
- `app/src/main/kotlin/com/localnovel/engine/model/Models.kt`

The current workspace path itself was verified on disk. No uncommitted local-only path, memory file, sample, migration or unverified external-tool integration was added to the report.

## Product-direction conflict

No product-direction conflict was introduced by the port. The change is a local filesystem-location correction only. The report does not alter PLANS, Main.kt, Models.kt, Candidate Reference behavior, migration state, memory, samples or the visual pipeline direction.

## Reproducible verification record

- Clean worktree was created from `origin/codex/mvp6-checkpoint-branching-spec`.
- Before editing, `git status --short` in the clean worktree was empty.
- The dirty source report was copied to a snapshot outside the repository for comparison.
- `git diff --no-index` showed exactly one changed line.
- No merge, rebase, pull, reset, stash, clean, test repair or functional file edit was performed.
