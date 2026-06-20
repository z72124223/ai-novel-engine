# Plan

## Goal

Run a read-only remote restore smoke check from the current GitHub remote to prove the pushed branch can be freshly cloned and verified outside this working tree.

## Global Strategy

Keep this milestone operational and non-product. Use the current remote branch as the source of truth, clone it into a temporary directory outside the repo, verify branch/tag identity, run the standard Gradle test baseline there, then record the evidence in `PLANS.md`, commit, and push. Do not change Kotlin code, schema, runtime safety boundaries, or product direction.

## Non-Goals

- No Kotlin code changes.
- No test changes.
- No audit rule changes.
- No schema or migration changes.
- No Validator changes.
- No `stateVersion` or `contextHash` changes.
- No `resolve-turn` or `StateReducer` changes.
- No SQLite writes.
- No Candidate B.
- No Candidate A.3 implementation.
- No MVP-8.
- No web server, API, Docker, Android UI, Rust, embedding, vector DB, or multi-agent runtime.
- No retagging `mvp-6`.
- No edits to `data/`, `out/`, `backups/`, or generated build artifacts.

## Scope

- `PLANS.md`
- Temporary clone outside the repository, used only for verification evidence.

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Local `HEAD` and `origin/codex/mvp6-checkpoint-branching-spec` both point to `0019da552f482898956715f239938d3a4e1b2608`.
- `mvp-6` remains the old stable tag and points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- Working tree is clean after the stale-document refresh milestone.
- `ROADMAP.md` lists `remote restore smoke on demand` as a valid next milestone option and explicitly says Candidate B is not the default next step.
- `VERIFY.md` requires `git diff --check` and `gradlew.bat clean test --no-daemon` for stable completion.

## Assumptions

- A temporary clone outside the repo is enough to prove the remote branch remains restorable.
- No CLI story E2E is needed because this milestone does not change runtime behavior.

## Open Questions

- None blocking this milestone.

## Implementation Steps

- [x] Step 1: Read `ROADMAP.md`, `PLANS.md`, and `VERIFY.md` to choose the next safe milestone.
- [x] Step 2: Update `PLANS.md` for the remote restore smoke milestone.
- [x] Step 3: Clone `origin/codex/mvp6-checkpoint-branching-spec` into a temporary directory outside the repo.
- [x] Step 4: Verify cloned branch, `HEAD`, `origin` tracking, and `mvp-6` tag target.
- [x] Step 5: Run `gradlew.bat clean test --no-daemon` in the temporary clone.
- [x] Step 6: Run `git diff --check` in the primary repo.
- [x] Step 7: Run `gradlew.bat clean test --no-daemon` in the primary repo.
- [x] Step 8: Commit `docs: record remote restore smoke verification`.
- [x] Step 9: Push `codex/mvp6-checkpoint-branching-spec` to origin and confirm remote state.

## Verification

- Temporary clone branch and tag checks.
- Temporary clone `gradlew.bat clean test --no-daemon`.
- Primary repo `git diff --check`.
- Primary repo `gradlew.bat clean test --no-daemon`.
- Primary repo `git status`.
- `git ls-remote --heads origin codex/mvp6-checkpoint-branching-spec`.
- `git rev-parse "mvp-6^{}"`.

## Risks

- Temporary clone may fail if GitHub credentials are unavailable in this environment.
- Test execution may need per-command `JAVA_HOME` because Java is not on the default PATH.
- A remote restore smoke does not prove Candidate B readiness or narrative quality completeness.

## Drift Log

- 2026-06-20: Started remote restore smoke because current roadmap lists it as a safe next milestone option and no product-direction approval is needed.
- 2026-06-20: Temporary clone outside the repo succeeded from `origin/codex/mvp6-checkpoint-branching-spec`; cloned `HEAD` was `0019da552f482898956715f239938d3a4e1b2608`, and cloned `mvp-6` resolved to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- 2026-06-20: Temporary clone passed `gradlew.bat clean test --no-daemon` using the existing external Temurin JDK 17.0.19 environment.
- 2026-06-20: Primary repo verification passed: `git diff --check` reported no whitespace errors, and `gradlew.bat clean test --no-daemon` completed with `BUILD SUCCESSFUL`.
- 2026-06-20: Sealed this remote restore smoke as a documentation-only commit and prepared it for push to `origin/codex/mvp6-checkpoint-branching-spec`.
