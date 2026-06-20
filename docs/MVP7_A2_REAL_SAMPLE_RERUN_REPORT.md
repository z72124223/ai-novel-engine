# MVP-7 Candidate A.2 Real Sample Rerun Report

Date: 2026-06-20

Status: `A2_REAL_SAMPLE_RERUN_COMPLETE`

This milestone reran the existing read-only `audit-narrative-quality` command after Candidate A.2 calibration. It did not change Kotlin code, tests, audit rules, schema, Validator, `stateVersion`, `contextHash`, `resolve-turn`, `StateReducer`, or SQLite data.

## 1. Test Data Sources

Audit command used through the repository Gradle CLI:

```powershell
.\gradlew.bat :app:run --args="audit-narrative-quality --context <context> --result <result> [--evidence <evidence>] --out <report>"
```

The installed distribution app was not present after the last clean test, so this rerun used the repository CLI. No `--db` argument was supplied. Generated reports are under ignored `out/`:

```text
out/mvp7_a2_real_sample_rerun/
```

## 2. Used Turns And Samples

The rerun used 14 real or near-real samples across three sources.

| Sample | Source | Context | Result | Evidence |
| --- | --- | --- | --- | --- |
| `pilot1_turn001` | Pilot-1 stabilized rerun | `out/pilot1/rerun/turns/turn_001_context.json` | `out/pilot1/rerun/turns/turn_001_result.json` | `out/pilot1/rerun/turns/turn_001_evidence.json` |
| `pilot1_turn004` | Pilot-1 stabilized rerun | `out/pilot1/rerun/turns/turn_004_context.json` | `out/pilot1/rerun/turns/turn_004_result.json` | `out/pilot1/rerun/turns/turn_004_evidence.json` |
| `pilot1_turn005` | Pilot-1 stabilized rerun | `out/pilot1/rerun/turns/turn_005_context.json` | `out/pilot1/rerun/turns/turn_005_result.json` | `out/pilot1/rerun/turns/turn_005_evidence.json` |
| `pilot1_turn008` | Pilot-1 stabilized rerun | `out/pilot1/rerun/turns/turn_008_context.json` | `out/pilot1/rerun/turns/turn_008_result.json` | `out/pilot1/rerun/turns/turn_008_evidence.json` |
| `pilot1_turn009` | Pilot-1 stabilized rerun | `out/pilot1/rerun/turns/turn_009_context.json` | `out/pilot1/rerun/turns/turn_009_result.json` | `out/pilot1/rerun/turns/turn_009_evidence.json` |
| `pilot1_turn010` | Pilot-1 stabilized rerun | `out/pilot1/rerun/turns/turn_010_context.json` | `out/pilot1/rerun/turns/turn_010_result.json` | `out/pilot1/capability_contract/final_formal_evidence.json` |
| `pilot1_turn011` | Pilot-1 stabilized rerun | `out/pilot1/rerun/turns/turn_011_context.json` | `out/pilot1/rerun/turns/turn_011_result.json` | `out/pilot1/rerun/evidence.json` |
| `pilot1_turn012` | Pilot-1 stabilized rerun | `out/pilot1/rerun/turns/turn_012_context.json` | `out/pilot1/rerun/turns/turn_012_result.json` | `out/pilot1/rerun/evidence.json` |
| `pilot1b_turn001` | Pilot-1b coverage repair | `out/pilot1b/turn1_context.json` | `out/pilot1b/turn1_result.json` | none |
| `pilot1b_turn002` | Pilot-1b coverage repair | `out/pilot1b/turn2_context.json` | `out/pilot1b/turn2_result.json` | none |
| `mvp6_turn001` | MVP-6 CLI branch acceptance | `out/mvp6_turn1_context.json` | `out/mvp6_turn1_result.json` | `out/mvp6_replay_original.json` |
| `mvp6_turn002` | MVP-6 CLI branch acceptance | `out/mvp6_turn2_context.json` | `out/mvp6_turn2_result.json` | `out/mvp6_replay_original.json` |
| `mvp6_turn003_original` | MVP-6 CLI branch acceptance | `out/mvp6_turn3_original_context.json` | `out/mvp6_turn3_original_result.json` | `out/mvp6_replay_original.json` |
| `mvp6_turn003_fork` | MVP-6 CLI branch acceptance | `out/mvp6_turn3_fork_context.json` | `out/mvp6_turn3_fork_result.json` | `out/mvp6_replay_fork.json` |

Evidence limitation: Pilot-1 Turns 10 through 12 and MVP-6 samples use broader exported evidence rather than precise per-turn evidence. Pilot-1b samples had context/result files but no evidence file.

## 3. Per-Sample Audit Results

| Sample | Verdict | Score | Severity | Finding count | Rule IDs |
| --- | --- | ---: | --- | ---: | --- |
| `pilot1_turn001` | `PASS` | 100 | `INFO` | 0 | none |
| `pilot1_turn004` | `PASS_WITH_WARNINGS` | 84 | `WARNING` | 2 | `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_SETTING_TOO_THIN` |
| `pilot1_turn005` | `PASS_WITH_WARNINGS` | 84 | `WARNING` | 2 | `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_SETTING_TOO_THIN` |
| `pilot1_turn008` | `PASS_WITH_WARNINGS` | 76 | `WARNING` | 3 | `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_SETTING_TOO_THIN`, `CONTEXT_TRANSITION_UNAVAILABLE` |
| `pilot1_turn009` | `PASS_WITH_WARNINGS` | 76 | `WARNING` | 3 | `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_SETTING_TOO_THIN`, `CONTEXT_TRANSITION_UNAVAILABLE` |
| `pilot1_turn010` | `PASS_WITH_WARNINGS` | 76 | `WARNING` | 3 | `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_SETTING_TOO_THIN`, `CONTEXT_TRANSITION_UNAVAILABLE` |
| `pilot1_turn011` | `PASS_WITH_WARNINGS` | 76 | `WARNING` | 3 | `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_SETTING_TOO_THIN`, `CONTEXT_TRANSITION_UNAVAILABLE` |
| `pilot1_turn012` | `PASS_WITH_WARNINGS` | 76 | `WARNING` | 3 | `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_SETTING_TOO_THIN`, `CONTEXT_TRANSITION_UNAVAILABLE` |
| `pilot1b_turn001` | `PASS` | 100 | `INFO` | 0 | none |
| `pilot1b_turn002` | `PASS` | 100 | `INFO` | 0 | none |
| `mvp6_turn001` | `PASS_WITH_WARNINGS` | 82 | `MAJOR` | 1 | `NPC_LIVELINESS_MISSING_ACTION` |
| `mvp6_turn002` | `PASS_WITH_WARNINGS` | 82 | `MAJOR` | 1 | `NPC_LIVELINESS_MISSING_ACTION` |
| `mvp6_turn003_original` | `PASS_WITH_WARNINGS` | 82 | `MAJOR` | 1 | `NPC_LIVELINESS_MISSING_ACTION` |
| `mvp6_turn003_fork` | `PASS_WITH_WARNINGS` | 82 | `MAJOR` | 1 | `NPC_LIVELINESS_MISSING_ACTION` |

Summary:

- `PASS`: 3
- `PASS_WITH_WARNINGS`: 11
- `NEEDS_REVIEW`: 0
- `FAIL`: 0
- Average score: 84
- Minimum score: 76
- Maximum score: 100

## 4. Warning Type Distribution

| Category | Count | Rule IDs |
| --- | ---: | --- |
| `context_quality` | 19 | `CONTEXT_SETTING_TOO_THIN`, `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_TRANSITION_UNAVAILABLE` |
| `npc_liveliness` | 4 | `NPC_LIVELINESS_MISSING_ACTION` |

Rule distribution:

| Rule ID | Count |
| --- | ---: |
| `CONTEXT_SETTING_TOO_THIN` | 7 |
| `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE` | 7 |
| `CONTEXT_TRANSITION_UNAVAILABLE` | 5 |
| `NPC_LIVELINESS_MISSING_ACTION` | 4 |

## 5. Candidate A.2 New Rule Behavior

A.2 did not make the real sample set noisy:

- No sample emitted `AI_FEELING_EMOTION_TOLD_NOT_SHOWN`.
- No sample emitted `NPC_TOOLMAN_CONVENIENT_COMPLIANCE`.
- No sample emitted `CONSEQUENCE_COST_NOT_RECORDED`.
- No sample became `NEEDS_REVIEW` or `FAIL`.

The most visible A.2-related result is the absence of the prior consequence false positives. Earlier sample collection showed possible `CONSEQUENCE_COST_NOT_RECORDED` noise around cracked-expression or cracked-token wording. After A.2:

- `pilot1_turn001` is `PASS` with score 100.
- `pilot1_turn005` has only context-quality findings.
- `case_08_false_positive_guard_crack` remains covered by the flawed sample pack and is still expected to stay quiet.

## 6. Useful Warnings

The context-quality warnings remain useful on Pilot-1:

- `CONTEXT_SETTING_TOO_THIN` matches the known Pilot-1 context budget issue where relevant setting context often collapsed to nearly empty.
- `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE` matches the known Pilot-1 coverage gap: no scheduled world event pressure arrived during the original 12-turn run.
- `CONTEXT_TRANSITION_UNAVAILABLE` appears only when the later retreat/hold turns were asking transition-like questions without legal transition support.

Pilot-1b produced two `PASS` results with no context-quality warnings. That is a useful sanity check: once the coverage repair pack supplied transition/event topology, the audit stopped flagging the original Pilot-1 topology gap.

## 7. Noisy Or Questionable Warnings

The four MVP-6 samples all emitted `NPC_LIVELINESS_MISSING_ACTION`.

This is useful in one narrow sense: the MVP-6 acceptance results are very small synthetic branch/checkpoint fixtures, and their `characterActions` do not include every NPC present in context. The audit correctly notices that this is not rich story evidence.

It may be noisy if interpreted as product failure:

- MVP-6 CLI acceptance samples were designed to prove checkpoint/fork/replay behavior, not prose quality.
- Their narratives are intentionally short and synthetic.
- The finding should remain advisory and should not become a gate.

Recommendation: future reports should label acceptance-fixture samples separately from real story-turn samples.

## 8. Suspected Missed Issues

No obvious new missed issue was proven by this rerun, but several gaps remain:

1. Emotion-telling rules were not exercised by these real samples.
   - No `AI_FEELING_EMOTION_TOLD_NOT_SHOWN` finding appeared.
   - The flawed pack still remains the better regression source for that rule.

2. Subtle NPC toolman behavior remains hard to assess from these samples.
   - No real sample emitted a toolman finding.
   - This may be correct, or it may mean the sample set does not include a subtle structural convenience case.

3. MVP-6 branch samples are not strong narrative-quality samples.
   - They prove that the audit can identify thin NPC action evidence.
   - They should not be used alone to tune normal story severity.

4. Some generated Pilot-1 audit reports did not parse as strict JSON.
   - The command completed and emitted readable verdict/score/rule summaries.
   - `ConvertFrom-Json` failed on `pilot1_turn008` through `pilot1_turn012`, apparently due malformed or unescaped evidence strings inherited from those artifacts.
   - This is a report-output robustness issue to investigate separately; it is not a SQLite or runtime state issue.

## 9. New False Positives

No confirmed new A.2 false positives were found.

Potentially noisy but expected:

- `NPC_LIVELINESS_MISSING_ACTION` on MVP-6 synthetic acceptance samples is accurate as an advisory finding but should not be treated as a story runtime defect.

Previously suspected consequence false positives did not reproduce in this rerun:

- No `CONSEQUENCE_COST_NOT_RECORDED` appeared in any of the 14 samples.

## 10. Comparison With A.1 / Pre-A.2 Results

Compared with the Candidate A.1 sample collection:

- The old consequence false-positive risk is reduced in the checked Pilot samples.
- Context-quality findings remain stable on Pilot-1 and absent on the two Pilot-1b coverage samples.
- Warning count is higher only because this rerun added MVP-6 acceptance fixtures, which have intentionally minimal NPC action surfaces.
- Scores were not over-compressed: the lowest score was 76, and no real sample fell to `NEEDS_REVIEW`.

Compared with the flawed sample pack:

- A.2's emotion-telling improvement remains proven by `case_02_ai_overwritten_style`, not by these real samples.
- A.2's crack-word false-positive guard remains proven by `case_08_false_positive_guard_crack`; the real samples do not contradict it.

## 11. Should The Audit Stay Read-Only?

Yes.

Reasons:

- Findings are useful as reviewer hints.
- Context-quality observations describe missing context support, not necessarily bad result prose.
- Some generated audit reports need strict JSON-output robustness work before any stronger automation.
- MVP-6 acceptance fixtures show that sample type matters; a gate could overreact to intentionally minimal E2E JSON.

## 12. Candidate B Readiness

Do not enter Candidate B yet.

This rerun improved confidence that A.2 is not too noisy, but it does not prove gate readiness.

Blockers before Candidate B discussion:

- Fix or investigate strict JSON parsing of generated audit reports for Pilot-1 turns with noisy evidence strings.
- Collect more real story arcs beyond this Pilot-1/Pilot-1b/MVP-6 artifact set.
- Separate acceptance-fixture quality warnings from true story-turn quality warnings.
- Decide whether context-quality findings should remain advisory or become a separate preflight category.

## 13. Next Step Recommendation

Recommended next milestone:

1. `MVP-7 audit report JSON robustness investigation`: read-only first, focused on why some generated reports are not strict JSON.
2. Or collect a fresh non-Pilot-1 story arc with per-turn evidence and rerun A.2 on that set.
3. Do not open Candidate B until report-output robustness and sample diversity are better.

## 14. Verdict

RERUN VERDICT: `PASS_WITH_LIMITATIONS`

Candidate A.2 stayed stable on 14 real or near-real samples. It did not become too noisy, did not produce new consequence false positives, and correctly left Pilot-1b samples clean where the repaired topology exists. The audit remains a useful read-only reviewer aid, but it is not ready to become Candidate B or any pre-resolve gate.
