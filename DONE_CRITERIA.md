# Done Criteria

Every milestone must satisfy this file before it can be committed. If any item fails, do not commit.

## Global Completion Rules

| Rule | Required |
| --- | --- |
| Read before writing | Yes |
| Plan after confirming repo state | Yes |
| One milestone per commit | Yes |
| Tests passing before commit | Yes |
| CLI E2E passing when the milestone changes CLI/runtime behavior | Yes |
| No unrelated refactor | Yes |
| No unrelated technology | Yes |
| Safety boundaries preserved | Yes |

## Required Safety Boundaries

These must remain true for every milestone:

- AI/Codex writes only JSON result files, never formal SQLite state directly.
- Formal story writes go through CLI/runtime, Validator, `stateVersion`, `contextHash`, and SQLite transactions.
- `resolve-turn` must validate before applying state.
- Forbidden reveals remain blocked in narrative, facts, memories, knowledge, scene/thread updates, and payload surfaces.
- `contextBudget.maxBytes` remains `24576` unless a future approved roadmap explicitly changes it.
- Author-only secrets must not enter character-facing context.
- Inactive timeline branches must not accept `prepare-turn` or `resolve-turn`.
- No web server, port, external API, Android UI, Rust, Docker, embedding, vector DB, or multi-agent runtime.

## Milestone Completion Criteria

| Milestone Type | Done Means |
| --- | --- |
| Documentation/control milestone | Required docs exist, match current repo behavior, and do not claim unverified facts. `git diff --check` passes. |
| Schema/runtime milestone | Additive migration only when required; old migrations unchanged unless explicitly authorized; unit tests cover schema and critical behavior. |
| CLI milestone | README or acceptance doc shows real command syntax; command is exercised against a fresh DB or documented fixture. |
| Protocol milestone | `TURN_PROTOCOL.md`, `VALIDATION_RULES.md`, samples or tests are updated as needed. |
| Pilot milestone | Evidence export, turn log, findings, context budget, and validator retry information are recorded. |
| Bugfix milestone | Reproduction is documented, fix is narrow, regression test exists or the reason for no test is recorded. |

## Required Tests

Always run:

```powershell
git diff --check
gradlew.bat clean test --no-daemon
```

For CLI/runtime milestones, also run the matching CLI E2E from `VERIFY.md`.

## Required CLI E2E

| Area | Required E2E |
| --- | --- |
| MVP-1 base loop | `init-db`, `seed-demo`, `compile-context`, `validate-result`, `apply-result`, `show-state` |
| MVP-2 multi-turn | `start-session`, `prepare-turn`, `resolve-turn`, `show-session`, duplicate/stale rejection where applicable |
| MVP-3 memory/knowledge | `show-character-mind`, `preview-recall`, `prepare-turn`, `resolve-turn` with memory/knowledge evidence |
| MVP-4 scene/thread | `show-scene`, `preview-transitions`, `show-world-events`, legal transition or documented rejection |
| MVP-5 setting pack | `validate-setting-pack`, `preview-setting-import`, `apply-setting-pack`, `diff-setting-pack`, `impact-report`, `export-setting-pack` |
| Pilot stabilization | `explain-context-budget`, `audit-context`, `pilot-preflight`, `export-session-evidence` |
| MVP-6 timeline | `create-checkpoint`, `fork-session`, `checkout-branch`, `show-timeline`, `verify-replay-chain`, `export-timeline` |

## Required Documentation Updates

Update only the docs that the milestone actually changes.

| Change Type | Required Docs |
| --- | --- |
| New CLI | `README.md` or milestone acceptance doc; `VERIFY.md` if it affects verification |
| New protocol fields | `docs/TURN_PROTOCOL.md`, `docs/VALIDATION_RULES.md`, samples/tests |
| New schema | matching migration file, `docs/DATA_MODEL.md` or milestone spec |
| New runtime behavior | `docs/ARCHITECTURE.md` or focused runtime doc |
| Pilot evidence | `docs/PILOT1_TURN_LOG.md`, `docs/PILOT1_FINDINGS.md` |
| Project control | `ROADMAP.md`, `DONE_CRITERIA.md`, `VERIFY.md` |

## Commit Conditions

Commit only when all are true:

- `git status` has been reviewed.
- `git diff --stat` matches the milestone scope.
- `git diff --check` passes.
- `gradlew.bat clean test --no-daemon` passes.
- Required CLI E2E passes, or the reason it cannot run is documented.
- No generated DB, output JSON, build directory, Gradle cache, temp JDK, or local machine path is staged.
- Commit message describes exactly one milestone.

## Unacceptable Conditions

Do not commit if any are true:

- Any test failure or test error.
- Any unresolved E2E failure.
- Any accidental migration edit outside the authorized migration.
- Any direct SQLite mutation outside approved CLI/runtime paths.
- Any hidden/author-only secret appears in user-facing export or context.
- Any unrelated product feature, framework, server, API, UI, Docker/Rust/embedding/vector DB addition.
- Any hardcoded user path, temp JDK path, session-specific hack, or E2E-input-specific special case.
