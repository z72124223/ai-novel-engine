# REBOOT-01 Stage 0 — Executable Baseline

Date: 2026-07-11

Status: `COMPLETED_WITH_KNOWN_UNRELATED_FAILURE`

## Plan alignment

- Active plan: `REBOOT01_PLAYABLE_DEPTH_SLICE`
- Stage executed: Stage 0 only
- Aligned with `PLANS.md`: yes
- Canon modified: no
- Runtime code modified: no
- Image lane resumed: no
- Original dirty worktree modified: no

## Files changed

- `PLANS.md`
- `docs/reboot/REBOOT01_BASELINE_REPORT.md`
- `docs/reboot/REBOOT01_WORLD_AND_CHARACTER_REALITY_AUDIT.md`
- `docs/development_journal/reboot01/STAGE_0_EXECUTABLE_BASELINE.md`
- `docs/development_journal/INDEX.md`
- ignored local evidence under `out/reboot01/baseline/**`
- ignored local inventory under `out/reboot01/world_audit/**`

## Why

The current route required executable evidence before any new Runtime architecture. Stage 0 proved the current engine, setting import, session protocol, transaction, SQL evidence, World Source query path, and current Chenxing world/character reality.

## Model and cost record

| Role | Model | Reasoning | Work |
|---|---|---|---|
| Main agent | `gpt-5.6-sol` | `medium` | plan, evidence integration, CLI execution, result drafting, reports |
| Repository Explorer | `gpt-5.6-terra` | `low` | read-only CLI/schema/path map |
| Runtime Evidence Tester | `gpt-5.6-terra` | `medium` | read-only test/fixture/source-integrity audit |
| Narrative Systems Critic | `gpt-5.6-sol` | `medium` | read-only world/character/voice gap audit |
| Cost / Complexity Auditor | `gpt-5.6-luna` | `low` | read-only reuse and Stage 6 dependency audit |

- Direct subagents: 4
- Agent depth: 1
- High escalations: 0
- xhigh/max/ultra usage: 0
- Token usage: `NOT_AVAILABLE`
- Credit usage: `NOT_AVAILABLE`
- Total elapsed time: `NOT_AVAILABLE`

## Commands and results

- Fetch/prune and clean worktree creation: pass.
- Compile: first attempt hit a shared output lock; isolated output retry passed in 3m40s.
- Targeted tests: 113 total, 109 pass, 4 known World Source failures.
- Full tests: 809 total, 805 pass, same 4 failures.
- Setting Pack validate/preview/apply: pass, 21 additive diff entries.
- Formal World Source import: correctly blocked, zero rows written, two integrity findings.
- Audit-local observed World Source import: pass, 1 archive / 9 documents / 255 chunks.
- `query-world-source`: pass, five classified hits.
- Real session start/prepare/resolve/show: pass after one quality-gate wording retry.
- Session evidence export: 1 turn / 1 delta / 0 pending contexts.
- Direct read-only JDBC query: state version, turn identity, delta, event, world time, and archive counts confirmed.

## Retry record

1. Compile retry: environmental file lock caused by shared Gradle output. Resolution: isolated `LOCALAPPDATA`; no shared directory deletion.
2. Resolve retry: valid protocol/world result was blocked by `CONTEXT_TRANSITION_UNAVAILABLE` based on ordinary route wording. Resolution for baseline only: remove transition-like wording. Stage 1 must classify this as a quality warning rather than weakening protocol/world checks.

## Known failures

The tracked `09_aijiang_detailed_setting.md` is 3051 bytes with SHA-256 `425582828EBD99BB648539E71A1010F5F96F9B4B6D8D337B410FCE007BE3B54F`; the formal manifest expects 3019 bytes and a different SHA-256. Four tests fail for this one source-integrity drift. Stage 0 did not repair or accept it.

## Verification

See:

- `docs/reboot/REBOOT01_BASELINE_REPORT.md`
- `docs/reboot/REBOOT01_WORLD_AND_CHARACTER_REALITY_AUDIT.md`
- `out/reboot01/baseline/session-evidence.json`
- `out/reboot01/world_audit/world_reality_inventory.json`

## Next stage

Stage 1: classify hard failures versus quality warnings and persist run evidence without weakening Validator, transaction, knowledge, version, or hash boundaries.
