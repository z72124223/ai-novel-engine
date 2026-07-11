# REBOOT-01 Stage 3 — Drama Scoring And Bounded Revision

Date: 2026-07-12

Status: `STAGE_3_PASS`

## Implemented

- structured dramatic question, value conflict, emotional wave, scene-value movement, stakes, cost, irreversible/relationship/reveal beats, and promise/payoff tracking;
- Audience Expectation Ledger with obvious danger, recognition, countermeasure, world/antagonist adaptation, second-order surprise, and retrospective clue validity;
- Logic, Character Drama, and Prose Pacing critics;
- all eleven required score dimensions;
- SQL persistence of raw draft/scores, findings, suggestion, optional revised draft/scores, selected draft, revision count, scene frame, ledger, and stall status;
- local `evaluate-reboot-drama` CLI.

## Safety and authorship boundary

The drama layer evaluates proposals. It does not create character knowledge, directly mutate state, or authorize Canon. `DIRECTOR_METADATA_LEAK` is a critical finding. Genre awareness is rewarded only when it does not use unavailable knowledge.

`IDIOT_PLOT_RISK` is raised when risk-taking lacks an explicit information, time, responsibility, relationship, false-belief, or cost reason. Fair surprise requires both a second-order consequence and a retrospectively valid clue; arbitrary twists increase `cheapTwistRisk`.

## Revision boundary

`NarrativeRevisionCoordinator` evaluates at most one revised candidate. If `priorRevisionCount=1`, no second revision is evaluated or selected. The database also constrains `revision_count` to `0..1`.

## Stall behavior

Two consecutive frames without state value movement, cost, irreversible consequence, relationship movement, or reveal trigger `STALL_DETECTED`. The only allowed next moves are:

```text
ESCALATE / CHOOSE / REVEAL / PAY_COST / END_SCENE
```

## Verification

- `RebootDramaLoopTest`: 5/5 PASS.
- Stage 1–3 and existing quality related suites: 47/47 PASS.
- Sample JSON parse: PASS.
- `git diff --check`: PASS.

No external model, provider, Canon, image, or public deployment work was added.
