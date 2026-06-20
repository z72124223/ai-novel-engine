# MVP-7 Audit Sample Collection Report

Date: 2026-06-20

Status: `CANDIDATE_A1_SAMPLE_COLLECTION_COMPLETE`

This milestone collected a larger read-only sample set for the existing `audit-narrative-quality` command. It did not change Kotlin code, audit rules, tests, schema, Validator, `stateVersion`, `contextHash`, `resolve-turn`, `StateReducer`, or SQLite data.

## 1. Test Data Sources

Audit command used through the repository Gradle CLI:

```powershell
.\gradlew.bat :app:run --args="audit-narrative-quality --context <context> --result <result> --evidence <evidence> --out <report>"
```

The command was run with a temporary JDK outside the repository. No `--db` argument was supplied and no SQLite write path was used.

Generated audit reports are under ignored `out/` and are not intended for commit:

```text
out/mvp7_sample_collection/turn_001_audit_report.json
out/mvp7_sample_collection/turn_002_audit_report.json
out/mvp7_sample_collection/turn_003_audit_report.json
out/mvp7_sample_collection/turn_004_audit_report.json
out/mvp7_sample_collection/turn_005_audit_report.json
out/mvp7_sample_collection/turn_006_audit_report.json
out/mvp7_sample_collection/turn_007_audit_report.json
out/mvp7_sample_collection/turn_008_audit_report.json
out/mvp7_sample_collection/turn_009_audit_report.json
out/mvp7_sample_collection/turn_010_audit_report.json
```

## 2. Used Turns And Samples

Selected sample set: formal Pilot-1 stabilized rerun Turns 1 through 10.

This adds more coverage beyond the initial calibration report by including Turns 1, 2, 3, 5, 6, and 7, while also rechecking Turns 4, 8, 9, and 10 after Candidate A.1.

| Turn | Context | Result | Evidence |
| ---: | --- | --- | --- |
| 1 | `out/pilot1/rerun/turns/turn_001_context.json` | `out/pilot1/rerun/turns/turn_001_result.json` | `out/pilot1/rerun/turns/turn_001_evidence.json` |
| 2 | `out/pilot1/rerun/turns/turn_002_context.json` | `out/pilot1/rerun/turns/turn_002_result.json` | `out/pilot1/rerun/turns/turn_002_evidence.json` |
| 3 | `out/pilot1/rerun/turns/turn_003_context.json` | `out/pilot1/rerun/turns/turn_003_result.json` | `out/pilot1/rerun/turns/turn_003_evidence.json` |
| 4 | `out/pilot1/rerun/turns/turn_004_context.json` | `out/pilot1/rerun/turns/turn_004_result.json` | `out/pilot1/rerun/turns/turn_004_evidence.json` |
| 5 | `out/pilot1/rerun/turns/turn_005_context.json` | `out/pilot1/rerun/turns/turn_005_result.json` | `out/pilot1/rerun/turns/turn_005_evidence.json` |
| 6 | `out/pilot1/rerun/turns/turn_006_context.json` | `out/pilot1/rerun/turns/turn_006_result.json` | `out/pilot1/rerun/turns/turn_006_evidence.json` |
| 7 | `out/pilot1/rerun/turns/turn_007_context.json` | `out/pilot1/rerun/turns/turn_007_result.json` | `out/pilot1/rerun/turns/turn_007_evidence.json` |
| 8 | `out/pilot1/rerun/turns/turn_008_context.json` | `out/pilot1/rerun/turns/turn_008_result.json` | `out/pilot1/rerun/turns/turn_008_evidence.json` |
| 9 | `out/pilot1/rerun/turns/turn_009_context.json` | `out/pilot1/rerun/turns/turn_009_result.json` | `out/pilot1/rerun/turns/turn_009_evidence.json` |
| 10 | `out/pilot1/rerun/turns/turn_010_context.json` | `out/pilot1/rerun/turns/turn_010_result.json` | `out/pilot1/capability_contract/final_formal_evidence.json` |

Evidence note: Turns 1 through 9 have per-turn evidence files. Turn 10 uses the previously exported formal capability evidence because no `turn_010_evidence.json` exists in `out/pilot1/rerun/turns`.

## 3. Per-Sample Audit Results

| Turn | Verdict | Score | Warning count | Rule IDs |
| ---: | --- | ---: | ---: | --- |
| 1 | `PASS` | 92 | 1 | `CONSEQUENCE_COST_NOT_RECORDED` |
| 2 | `PASS` | 92 | 1 | `CONTEXT_SETTING_TOO_THIN` |
| 3 | `PASS` | 92 | 1 | `CONTEXT_SETTING_TOO_THIN` |
| 4 | `PASS_WITH_WARNINGS` | 84 | 2 | `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_SETTING_TOO_THIN` |
| 5 | `PASS_WITH_WARNINGS` | 76 | 3 | `CONSEQUENCE_COST_NOT_RECORDED`, `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_SETTING_TOO_THIN` |
| 6 | `PASS_WITH_WARNINGS` | 84 | 2 | `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_SETTING_TOO_THIN` |
| 7 | `PASS_WITH_WARNINGS` | 84 | 2 | `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_SETTING_TOO_THIN` |
| 8 | `PASS_WITH_WARNINGS` | 76 | 3 | `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_SETTING_TOO_THIN`, `CONTEXT_TRANSITION_UNAVAILABLE` |
| 9 | `PASS_WITH_WARNINGS` | 76 | 3 | `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_SETTING_TOO_THIN`, `CONTEXT_TRANSITION_UNAVAILABLE` |
| 10 | `PASS_WITH_WARNINGS` | 76 | 3 | `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_SETTING_TOO_THIN`, `CONTEXT_TRANSITION_UNAVAILABLE` |

## 4. Warning Type Distribution

| Category | Count | Rule IDs |
| --- | ---: | --- |
| `context_quality` | 19 | `CONTEXT_SETTING_TOO_THIN`, `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_TRANSITION_UNAVAILABLE` |
| `consequence` | 2 | `CONSEQUENCE_COST_NOT_RECORDED` |

Rule distribution:

| Rule ID | Count |
| --- | ---: |
| `CONTEXT_SETTING_TOO_THIN` | 9 |
| `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE` | 7 |
| `CONTEXT_TRANSITION_UNAVAILABLE` | 3 |
| `CONSEQUENCE_COST_NOT_RECORDED` | 2 |

## 5. Useful Warnings

The Candidate A.1 context-quality observations were useful.

- `CONTEXT_SETTING_TOO_THIN` accurately matches the stabilized Pilot evidence: after Turn 1, most turns had `setting bytes = 2 / 8192` and 24 omitted settings.
- `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE` accurately matches the original Pilot-1 blocker: no scheduled world event applied pressure during the 12-turn run.
- `CONTEXT_TRANSITION_UNAVAILABLE` accurately appears in the late retreat / legal-hold phase where transition questions existed but no legal transition candidate was available.

These warnings are advisory and correctly remain outside Validator behavior. They are better described as Pilot coverage and context-quality observations than prose-quality failures.

## 6. Noisy Warnings

The audit is not noisy by volume. Ten samples produced 21 findings, and 19 of those reflect already known Pilot coverage problems.

Potential noise:

- `CONSEQUENCE_COST_NOT_RECORDED` fired on Turn 1 and Turn 5 from evidence term `裂開`.
- In Turn 1, the phrase is `表情裂開了一瞬` and `裂紋腰牌`; this is expressive description / item description, not an injury or cost.
- In Turn 5, the phrase concerns the cracked token and withheld token meaning, not an unrecorded injury or paid cost.

Conclusion: the consequence rule is still useful in concept, but `裂開` is too broad as a cost/injury cue unless paired with bodily injury, fatigue, resource loss, or explicit cost semantics.

## 7. Suspected Missed Issues

The larger sample confirms the audit is no longer as blind to context coverage as the first calibration pass. However, it still likely misses:

1. Style-level quality issues.
   - No `style` warnings appeared, even though style consistency and prose texture are part of the MVP-7 spec.
   - This may be acceptable until style policy becomes more concrete.

2. NPC liveliness nuance.
   - No NPC toolman warning appeared after A.1, which is good for the known false positive.
   - The audit still may not detect subtler cases where an NPC is structurally convenient but not phrased with obvious compliance terms.

3. Momentum quality versus expected scene state.
   - `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE` and `CONTEXT_TRANSITION_UNAVAILABLE` catch some stasis causes.
   - They do not fully judge whether a scene is narratively satisfying, only whether supporting runtime pressure is absent.

4. Evidence precision for Turn 10.
   - Turn 10 uses broader capability evidence rather than per-turn evidence, so evidence attribution is less precise than Turns 1 through 9.

## 8. Context-Quality Observation Accuracy

Accuracy is good for this sample set.

- Thin setting context was repeatedly present in both audit output and `docs/PILOT1_TURN_LOG.md`.
- Missing scheduled world event pressure is corroborated by `docs/PILOT1_FINDINGS.md`.
- Missing legal transition is corroborated by Turn 8 and Turn 9 records and the original Pilot-1 coverage checklist.

The main caveat is that these are coverage observations. They should remain advisory unless a future milestone defines a specific gate.

## 9. Is The Audit Still Conservative?

Yes, but less than before Candidate A.1.

Before A.1, the audit produced almost no warnings and missed known Pilot coverage gaps. After A.1:

- It consistently flags thin setting context.
- It flags missing scheduled world event pressure once later turns have active threads.
- It flags transition unavailability in the late transition-relevant turns.

It remains conservative on style, NPC liveliness, and subtle momentum quality. That is acceptable for Candidate A.1 because the tool is read-only and advisory.

## 10. Is This Enough For Candidate B?

No.

Do not proceed directly to Candidate B yet.

Reasons:

- The sample set is still one Pilot story arc, not diverse enough to become a pre-resolve lint gate.
- `CONSEQUENCE_COST_NOT_RECORDED` produced two likely false positives around `裂開`.
- The context-quality findings are useful but are coverage diagnostics, not necessarily result-quality defects.
- No deliberately flawed samples were added in this milestone, so negative-case precision is still under-tested.

## 11. Next Step Recommendation

Recommended next milestone:

1. Build a tiny deliberately flawed sample pack for audit calibration only, still read-only.
2. Or run the same audit against Pilot-1b and MVP-6 branch/fork samples to test whether context-quality warnings calm down when topology and scheduled pressure exist.
3. Only after that, write a Candidate B decision brief. Do not start Candidate B implementation by default.

## 12. Verdict

SAMPLE COLLECTION VERDICT: `PASS_WITH_LIMITATIONS`

The read-only audit is useful as a reviewer aid. Candidate A.1 successfully made it more sensitive to known context-quality gaps without becoming noisy. The next calibration need is precision around consequence cues and a broader, more diverse sample set.
