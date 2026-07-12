# ENGINE-RULES-01 / DIALOGUE-02 Final Report

Status: `ENGINE_RULES01_PASS_WITH_SEMANTIC_WARNINGS`

Date: `2026-07-13`

Boundary: `NON_CANON_PILOT`

## Outcome

Production Rule Set `engine-production-rules@1.0.0` is enforced by Kotlin, SQLite, CLI and tests. Dialogue may be drafted and reviewed, but it cannot become `SELECTED` unless the current hash-bound artifact has three independent PASS reviews, complete line coverage, exact quotes, zero blockers, no pending human approval and a valid revision lifecycle.

The implementation adds:

- versioned production rules, character voice profiles and a zh-Hant-TW language profile;
- full Artifact identity hashing, including Rule Set and profile content hashes;
- transaction-safe review intake and selection;
- normalized Artifact lines and review coverage for SQL-level quote/coverage checks;
- additive Migrations 014 and 015, including legacy hash-schema quarantine;
- four CLI commands: `evaluate-dialogue`, `show-dialogue-findings`, `finalize-dialogue`, `show-dialogue-selection`;
- strict nested JSON input decoding and non-zero blocked-finalization status;
- negative, bypass, rollback, upgrade, idempotency and recovery tests.

## DIALOGUE-01 Re-evaluation

Initial Artifact:

- path: `samples/dialogue02/re_evaluation/initial_review_handoff.json`
- Artifact SHA-256: `805cb62cd19ab42ca10df6b98cdde315fb6580a6d098ded7e235cd178f28a0a9`
- result: `NOT_SELECTABLE`
- blockers: `9`
- warnings: `1`

The line `好。先碰你。` is independently blocked as:

- `ACCIDENTAL_FLIRTATION_OR_DOUBLE_MEANING`
- `REQUIRES_PERFORMANCE_TO_AVOID_MISREADING`
- `FORCED_WIT_UNDER_DANGER`

The only automatic dialogue revision changes that line to `先救你。`.

Revised Artifact:

- path: `samples/dialogue02/re_evaluation/revised_artifact.json`
- Artifact SHA-256: `0d7c137a5180d87f32f88d8a42b9475ec8a6a4e12b185ca1abedbebd0b69c0f2`
- parent Artifact SHA-256: `805cb62cd19ab42ca10df6b98cdde315fb6580a6d098ded7e235cd178f28a0a9`
- auto revision count: `1`
- result: `SELECTED`
- blockers: `0`
- warnings: `1`
- decision SHA-256: `3927b17faeab988943c6833fe3011dfd7b6e54f2e4dc5b3118bd823648d183b0`

The remaining warning is the Pan-Chinese clarity note for `潮表還沒到。`; it is not a blocker and does not require human approval.

## Verification

- focused `EngineRulesDialogue*` tests: PASS
- full Gradle test suite: `872` tests, `0` failures, `0` errors, `0` skipped
- Migration 013→015 upgrade and data preservation: PASS
- installed Migration 014→015 trigger replacement: PASS
- migration idempotency and rollback: PASS
- direct INSERT/UPDATE selection bypass checks: PASS
- fake review, zero-line, missing JSON-path and late-evidence bypass checks: PASS
- early NOT_SELECTABLE decision recovery: PASS
- JSON parsing: `23` evidence files PASS before manifest generation
- review roles/instances/line coverage/exact quotes: PASS
- private path and secret scan: PASS
- Canon/media/Provider scope check: PASS
- `git diff --check`: PASS
- High read-only review: `PASS_WITH_RISKS`, release blockers `NONE`, recommendation `APPROVE_RELEASE`

## Residual Risks

- Reviewer independence is represented by distinct instance IDs, not signed identities.
- Raw SQLite access remains outside the authorized production workflow; structurally consistent fabricated evidence cannot be distinguished cryptographically.
- Migration SQL parsing remains constrained to the Repository's controlled migration format.

No image, video, Provider, Canon, next chapter or interactive-game work was performed.
