# REBOOT-01 Stage 2 — NPC Autonomy

Date: 2026-07-12

Status: `COMPLETED`

## Files and purpose

- `NpcAutonomyModels.kt` and `NpcAutonomyServices.kt`: deterministic, bounded NPC proposal pipeline and DecisionTrace.
- `011_npc_autonomy_runtime.sql`: additive sandbox-candidate and trace tables.
- repository/runtime option integration: atomic accepted trace persistence.
- `run-offscreen-npc-tick`: local CLI entry.
- two sandbox-only JSON samples and focused tests.
- plan/report/journal updates.

## Verification

- Direct NPC tests: 5 PASS.
- Related runtime suites: 86 PASS total.
- Illegal foreign knowledge: rejected, evidence retained, no formal event.
- Forced trace insert failure: event and turn rolled back.
- Player return: off-screen consequence present in subsequent context.

## Model and cost record

- Main agent: `gpt-5.6-sol / medium`.
- Requested read-only explorer: `gpt-5.6-terra / low`; no output because subagent usage limit was reached.
- High escalation: 0.
- Paid/external provider use: 0.

## Alignment

The work stayed inside Stage 2. It did not modify Canon or images, call an external model, start Stage 3, or touch the original dirty worktree.
