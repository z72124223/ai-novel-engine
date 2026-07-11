# REBOOT-01 Stage 1 — Validation And Run Evidence

Date: 2026-07-12

Status: `COMPLETED_WITH_KNOWN_UNRELATED_FAILURE`

## Plan alignment

- Active plan: `REBOOT01_PLAYABLE_DEPTH_SLICE`
- Stage executed: Stage 1 only
- Aligned with `PLANS.md`: yes
- Canon or images modified: no
- Original dirty worktree modified: no

## Files changed

- CLI resolve-turn option parsing/help for strict quality and separate irreversible artifacts.
- Runtime validation classification and evidence persistence.
- SQLite repository transaction integration.
- Additive migration `010_reboot_run_evidence.sql`.
- Direct quality, identity, rollback, concurrency, malformed-envelope, and irreversible-boundary tests.
- Stage report, journal, index, and active plan window.

## Why

Stage 0 proved that a prose-quality heuristic could reject a protocol-valid and world-legal turn. Stage 1 separates safety from prose quality while ensuring every accepted or rejected attempt remains inspectable in SQL.

## Model and cost record

| Role | Model | Reasoning | Work |
|---|---|---|---|
| Main agent | `gpt-5.6-sol` | `medium` | implementation, tests, evidence integration, reports |
| Safety reviewer | `gpt-5.6-sol` | `high` | migration, transaction, identity, irreversible approval, and bypass review |

- Direct subagents: 1
- Agent depth: 1
- High escalations: 1
- xhigh/max/ultra usage: 0
- Token/credit usage: `NOT_AVAILABLE`

## Verification and retry record

1. Initial focused tests passed, then high review found that omitted contracts could bypass irreversible approval.
2. Engine-side protected-change detection and separate approval artifacts were added.
3. Review found incomplete multi-type scope and weak timestamp validation; exact type-set matching and strict `Instant.parse` validation were added.
4. An overbroad secret-reveal rule broke nine existing legal runtime tests; it was removed because reveal is not itself one of the approved irreversible consequence classes.
5. Final focused suites: 101/101 PASS.
6. Final full suite: 826 total, 822 PASS, only the four known World Source integrity failures.
7. Final high safety review: PASS.

## Next stage

Stage 2: implement a minimal off-screen NPC perception, memory importance, reflection, goal, schedule, action proposal, and scheduler loop without bypassing formal validation.
