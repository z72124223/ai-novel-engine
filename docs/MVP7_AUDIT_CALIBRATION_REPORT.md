# MVP-7 Audit Calibration Report

Date: 2026-06-20

Status: `CANDIDATE_A_CALIBRATION`

This report calibrates the existing `audit-narrative-quality` read-only audit against real Pilot turn artifacts. It does not change audit rules, runtime code, schema, Validator, `stateVersion`, `contextHash`, `resolve-turn`, or `StateReducer`.

## Candidate A.1 Refinement Result

Date: 2026-06-20

Status: `CANDIDATE_A1_IMPLEMENTED`

Candidate A.1 made a narrow read-only calibration pass based on this report:

- Fixed the false positive where `沒有立刻答應` triggered `NPC_TOOLMAN_CONVENIENT_COMPLIANCE`.
- Added advisory `context_quality` observations for:
  - `CONTEXT_SETTING_TOO_THIN`
  - `CONTEXT_TRANSITION_UNAVAILABLE`
  - `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`
  - `CONTEXT_PROCEDURAL_STASIS_RISK`

Turn 12 was rerun after the refinement:

| Turn | Verdict | Score | Findings | Rule IDs |
| --- | --- | ---: | ---: | --- |
| 12 after A.1 | PASS_WITH_WARNINGS | 76 | 3 | `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_SETTING_TOO_THIN`, `CONTEXT_TRANSITION_UNAVAILABLE` |

The old `NPC_TOOLMAN_CONVENIENT_COMPLIANCE` warning is gone. The remaining findings are advisory context-quality observations, not Validator failures and not Candidate B behavior.

## 1. Test Data Sources

Audit command used:

```powershell
C:\Users\f7212\AppData\Local\local-interactive-fiction-engine-build\app\install\app\bin\app.bat audit-narrative-quality --context <context> --result <result> --evidence <evidence> --out <report>
```

The installed distribution CLI was used with an external temporary JDK environment. No SQLite DB path was supplied.

Selected formal Pilot-1 stabilized rerun turns:

| Turn | Reason selected | Context | Result | Evidence |
| --- | --- | --- | --- | --- |
| 4 | NPC refusal / modified cooperation around sound-position test. | `out/pilot1/rerun/turns/turn_004_context.json` | `out/pilot1/rerun/turns/turn_004_result.json` | `out/pilot1/rerun/turns/turn_004_evidence.json` |
| 8 | Memory cue and retreat/transition question without forced movement. | `out/pilot1/rerun/turns/turn_008_context.json` | `out/pilot1/rerun/turns/turn_008_result.json` | `out/pilot1/rerun/turns/turn_008_evidence.json` |
| 9 | Attempted conditional retreat without legal scene transition. | `out/pilot1/rerun/turns/turn_009_context.json` | `out/pilot1/rerun/turns/turn_009_result.json` | `out/pilot1/rerun/turns/turn_009_evidence.json` |
| 10 | Referenced capability use and cost reflection. | `out/pilot1/rerun/turns/turn_010_context.json` | `out/pilot1/rerun/turns/turn_010_result.json` | `out/pilot1/capability_contract/final_formal_evidence.json` |
| 12 | Final boundary negotiation and thread blockage evidence. | `out/pilot1/rerun/turns/turn_012_context.json` | `out/pilot1/rerun/turns/turn_012_result.json` | `out/pilot1/rerun/evidence` |

Generated audit reports:

- `out/mvp7_calibration/turn_004_audit_report.json`
- `out/mvp7_calibration/turn_008_audit_report.json`
- `out/mvp7_calibration/turn_009_audit_report.json`
- `out/mvp7_calibration/turn_010_audit_report.json`
- `out/mvp7_calibration/turn_012_audit_report.json`

These generated reports are under ignored `out/` and are not intended for commit.

## 2. Per-Turn Audit Verdicts

| Turn | Verdict | Score | Severity | Warning count | Rule IDs |
| --- | --- | ---: | --- | ---: | --- |
| 4 | PASS | 100 | INFO | 0 | none |
| 8 | PASS | 100 | INFO | 0 | none |
| 9 | PASS | 100 | INFO | 0 | none |
| 10 | PASS | 100 | INFO | 0 | none |
| 12 | PASS | 92 | WARNING | 1 | `NPC_TOOLMAN_CONVENIENT_COMPLIANCE` |

Observed audit evidence counts:

| Turn | Narrative length | Character actions | State deltas | Retrieved memories | Knowledge claims | Active threads |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| 4 | 712 | 2 | 4 | 0 | 0 | 1 |
| 8 | 713 | 2 | 5 | 4 | 4 | 1 |
| 9 | 700 | 2 | 5 | 0 | 4 | 1 |
| 10 | 574 | 3 | 6 | 0 | 4 | 1 |
| 12 | 530 | 3 | 6 | 4 | 4 | 1 |

## 3. Warning Type Distribution

| Category | Count | Rule IDs |
| --- | ---: | --- |
| `npc_liveliness` | 1 | `NPC_TOOLMAN_CONVENIENT_COMPLIANCE` |
| `ai_feeling` | 0 | none |
| `world_autonomy` | 0 | none |
| `knowledge_boundary` | 0 | none |
| `style` | 0 | none |
| `consequence` | 0 | none |

The audit is not noisy in volume. Across five real turns, it produced only one warning.

## 4. Useful Warnings

The current warning category is conceptually useful: detecting NPCs who conveniently comply with the player is a real MVP-7 quality concern.

However, the only observed warning is not substantively useful for this turn. Turn 12 narrative says:

> 他沒有立刻答應，也沒有拿漂亮話糊弄過去

The rule matched the phrase `立刻答應` even though it appeared inside a negation. Zhao Wujiu did not serve the player conveniently; the narrative and turn log both show a limited, conditional answer grounded in authority limits and hard world rules.

Calibration result: the category is useful, but the literal phrase matching is currently too shallow for negated phrases.

## 5. Noisy Warnings

Noise level is low by count but meaningful by precision.

- `NPC_TOOLMAN_CONVENIENT_COMPLIANCE` on Turn 12 is a false positive.
- Severity `WARNING` is acceptable because it does not block anything.
- The evidence string is too short to explain the surrounding negation.

Suggested future calibration, not implemented in this milestone:

- Account for negation windows such as `沒有立刻答應`, `並未立刻答應`, `不曾立刻答應`.
- Include a short narrative excerpt around matched evidence so reviewers can judge context.

## 6. Missed Issues

The audit missed several known quality or coverage risks already documented in `docs/PILOT1_FINDINGS.md` and `docs/PILOT1_TURN_LOG.md`:

1. Setting context thinness.
   - Turns 4, 8, 9, 10, and 12 all had `setting bytes = 2 / 8192` in the turn log.
   - The audit did not flag this because it focuses on result quality, not context coverage quality.

2. No legal scene transition in the selected turns.
   - Turn 9 explicitly attempted conditional retreat, but no legal transition candidate existed.
   - The audit did not flag this because result JSON correctly avoided forced transition.

3. No scheduled world event pressure.
   - Original Pilot-1 findings marked scheduled event pressure as `NOT_COVERED`.
   - The audit only flags pending world events that are present in context but unused; it does not flag absence of event pressure as a quality gap.

4. Narrative momentum / controlled stasis.
   - The selected late turns are procedurally coherent but still orbit the same blocked scene.
   - The audit scored them as PASS because they contain actions, deltas, memories, and no boundary violations.

5. Player-perspective style consistency is not checked.
   - Some results use first-person narration because the Pilot style used it. The current audit does not judge first-person versus third-person consistency.
   - This may be fine for the current project, but it should be a configurable style question before any stricter rule.

These are not bugs in read-only Candidate A, but they show Candidate A is currently a lint-style report rather than a full Pilot coverage judge.

## 7. Evidence Sufficiency

Evidence was sufficient for this calibration pass:

- Every selected turn had context and result JSON.
- Turns 4, 8, and 9 had per-turn evidence files.
- Turns 10 and 12 used larger exported evidence files.
- `docs/PILOT1_TURN_LOG.md` provided human-written quality scores and interpretation.
- `docs/PILOT1_FINDINGS.md` provided known coverage gaps for comparison.

Evidence limitation:

- Whole-session evidence is less precise than per-turn evidence. If future evidence-based warnings become stronger, calibration should prefer per-turn exports to avoid attributing whole-session repetition to one turn.

## 8. Suggested Fix Quality

The one emitted `suggested_fix` was:

> Show why the NPC accepts, refuses, modifies, or conditions the player's request from their own motive.

This is generally actionable. It tells the author what to add or check.

For the Turn 12 false positive, the fix is not useful because the result already does exactly that. Future reports should either suppress negated matches or include enough evidence context for a reviewer to dismiss the warning quickly.

## 9. Severity Calibration

Current severity looks safe:

- The false positive is only `WARNING`.
- No warning becomes a Validator failure.
- Verdict remained `PASS` with score `92`.

This is appropriate for a first read-only audit. The report should stay advisory until precision improves.

## 10. Candidate B Readiness

Do not move directly to Candidate B yet.

Reason:

- Candidate A is not noisy, but it is under-sensitive.
- It missed setting context thinness, transition coverage gaps, scheduled event absence, and late-scene momentum/stasis.
- The one warning found was a false positive caused by literal phrase matching.

Candidate B should be considered only after one of these conditions is met:

1. Candidate A gets a small calibration pass that improves precision without changing runtime writes.
2. A larger calibration set includes both good and deliberately flawed result JSON.
3. The team decides which quality issues are advisory only and which could ever become pre-resolve lint.
4. The report can distinguish "quality lint" from "Pilot coverage gap".

## 11. Next Step Recommendation

Recommended next milestone:

`MVP-7 Candidate A.1 audit calibration refinements`

Scope should remain read-only and small:

- Fix negated convenient-compliance phrase matching.
- Add optional context-quality observations for setting bytes, omitted settings, retrieved memory count, and pending event absence.
- Add a tiny set of deliberately flawed sample result files for regression tests.
- Keep all findings advisory; do not make Validator failures.

Alternative if no code changes are desired:

- Run the current audit against more Pilot-1b and MVP-6 context/result pairs and build a larger calibration matrix before changing rules.

## 12. Verdict

CALIBRATION VERDICT: `PASS_WITH_LIMITATIONS`

The audit command is stable and safe as a read-only report. It is not too noisy, but it is currently too conservative to catch several known narrative-quality and coverage issues. It is useful as a first reviewer aid, not yet as a gate before `resolve-turn`.
