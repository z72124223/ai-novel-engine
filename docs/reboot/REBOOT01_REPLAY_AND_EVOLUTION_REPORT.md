# REBOOT-01 Stage 5 — Replay, Benchmark And Evolution

Date: 2026-07-12

Status: `STAGE_5_PASS`

## Comparison design

The six-turn Chenxing sandbox scenario was executed twice with the same seed, formal base pack, sandbox add-on, NPC profile, actions, state deltas, and approval boundary.

- Baseline: current Stage 4 behavior, where turn one enters the bounded revision loop once.
- Evolved: one evidence-backed rule adjustment—use the already validated danger-recognition draft as the initial turn-one draft.

The benchmark normalizes generated UUIDs and timestamps before hashing story outcomes. It compares applied turn narratives, state deltas, events, character state, NPC trace status, selected drama drafts, and scene frames. Baseline and evolved databases and learned-rule stores are isolated from each other.

## Measured result

| Metric | Baseline | Evolved |
|---|---:|---:|
| legal apply rate | 1.0 | 1.0 |
| stall count | 0 | 0 |
| autonomy score | 100 | 100 |
| emotional wave coverage | 85.0 | 85.0 |
| irreversible change count | 1 | 1 |
| repetition / AI prose risk | 10.0 | 10.0 |
| overall drama score | 70.4 | 70.4 |
| fair surprise | 57.0 | 57.0 |
| reader attachment | 73.0 | 73.0 |
| elapsed world minutes | 35 | 35 |
| retry count | 1 | 0 |
| model | deterministic-local | deterministic-local |
| reasoning | none | none |

Both runs produced normalized replay fingerprint:

```text
33600a42f1ae18d033f4cc7131ebffc246652378431d5b3f2ddfd851c43a2447
```

Local execution time was 3180 ms for the baseline and 1972 ms for the evolved run. Timing is recorded as observational evidence, not a stable performance guarantee.

## Decision

`ADOPT`

The evolved rule preserves the normalized story outcome, session time/version, facts, knowledge/memory, scene/thread state, NPC proposal/resolution, external approval evidence, legality, autonomy, emotional coverage, fair surprise, reader attachment, irreversible boundary, and selected drama quality while reducing revision retries from one to zero. The evolved profile is now the default for the vertical-slice runner; the benchmark still selects both profiles explicitly so future comparisons cannot silently collapse into the same run.

## Verification

- Benchmark CLI decision: `ADOPT`.
- Benchmark test plus Stage 1–4 related suites: 29/29 PASS.
- Full suite: 838 total, 834 PASS, 4 known World Source #09 hash/byte-size failures, 0 new failures.
- Benchmark output: `out/reboot01/benchmark/**` (ignored local evidence).
- `git diff --check`: PASS before staging.

The remaining narrative-quality advisories are not silently promoted into this rule change. Stage 6 may display them as diagnostics but must not weaken protocol, world legality, irreversible approval, or Canon boundaries.
