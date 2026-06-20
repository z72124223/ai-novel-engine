# MVP-7 Flawed Audit Sample Pack Report

Date: 2026-06-20

Status: `FLAWED_SAMPLE_PACK_COMPLETE`

This milestone created deliberately flawed read-only samples for `audit-narrative-quality`. It did not change Kotlin code, tests, audit rules, schema, Validator, `stateVersion`, `contextHash`, `resolve-turn`, `StateReducer`, or SQLite data.

## 1. Sample Pack

Sample directory:

```text
samples/mvp7_flawed_audit_pack/
```

Each case contains:

- `context.json`
- `result.json`

The manifest is:

```text
samples/mvp7_flawed_audit_pack/MANIFEST.md
```

Generated audit reports are under ignored `out/`:

```text
out/mvp7_flawed_audit_pack/
```

## 2. Audit Command

The repository Gradle CLI was used:

```powershell
.\gradlew.bat :app:run --args="audit-narrative-quality --context samples/mvp7_flawed_audit_pack/<case>/context.json --result samples/mvp7_flawed_audit_pack/<case>/result.json --out out/mvp7_flawed_audit_pack/<case>_report.json"
```

No `--db` argument was supplied. The audit did not write SQLite.

## 3. Per-Case Results

| Case | Purpose | Expected major signal | Actual verdict | Score | Findings | Actual rule IDs |
| --- | --- | --- | --- | ---: | ---: | --- |
| `case_01_npc_toolman` | NPC has no independent resistance and conveniently serves the player. | NPC toolman / low agency. | `PASS_WITH_WARNINGS` | 84 | 2 | `NPC_LIVELINESS_LOW_FIT_SCORE`, `NPC_TOOLMAN_CONVENIENT_COMPLIANCE` |
| `case_02_ai_overwritten_style` | Heavy AI-feeling prose, abstract summary, parallelism, direct emotion telling. | AI-feeling / style warnings. | `NEEDS_REVIEW` | 50 | 5 | `CONSEQUENCE_NO_STATE_EVIDENCE`, `AI_FEELING_OVER_EXPLAINED`, `AI_FEELING_OVER_SUMMARY`, `STYLE_EMPTY_GRANDIOSE_PROSE`, `STYLE_PARALLELISM_STACKING` |
| `case_03_world_protagonist_only` | World reacts only because the protagonist exists. | World autonomy issue. | `PASS_WITH_WARNINGS` | 84 | 2 | `NPC_LIVELINESS_LOW_FIT_SCORE`, `WORLD_AUTONOMY_PROTAGONIST_ONLY_PRESSURE` |
| `case_04_knowledge_boundary` | Forbidden reveal plus unknown knowledge claim. | Critical knowledge-boundary issue. | `FAIL` | 39 | 3 | `KNOWLEDGE_FORBIDDEN_REVEAL`, `KNOWLEDGE_UNKNOWN_CLAIM_USED`, `NPC_LIVELINESS_LOW_FIT_SCORE` |
| `case_05_missing_consequence` | Dangerous action and pain without state consequence. | Missing consequence / unrecorded cost. | `PASS_WITH_WARNINGS` | 74 | 2 | `CONSEQUENCE_NO_STATE_EVIDENCE`, `CONSEQUENCE_COST_NOT_RECORDED` |
| `case_06_scene_stasis` | Late active scene with thread pressure but no transition, world event, time pressure, or progress. | Stasis / context-quality / thread idle. | `NEEDS_REVIEW` | 42 | 6 | `CONSEQUENCE_NO_STATE_EVIDENCE`, `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_PROCEDURAL_STASIS_RISK`, `CONTEXT_SETTING_TOO_THIN`, `CONTEXT_TRANSITION_UNAVAILABLE`, `WORLD_AUTONOMY_THREADS_IDLE` |
| `case_07_thin_context` | Setting context nearly empty while turn asks for legal/world support. | Thin setting context. | `PASS_WITH_WARNINGS` | 76 | 3 | `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE`, `CONTEXT_SETTING_TOO_THIN`, `CONTEXT_TRANSITION_UNAVAILABLE` |
| `case_08_false_positive_guard_crack` | Contains `表情裂開` and `腰牌裂紋` without injury or cost. | Should not trigger unrecorded cost. | `PASS` | 100 | 0 | none |

## 4. Warning Distribution

| Category | Count |
| --- | ---: |
| `context_quality` | 7 |
| `npc_liveliness` | 4 |
| `consequence` | 4 |
| `ai_feeling` | 2 |
| `knowledge_boundary` | 2 |
| `style` | 2 |
| `world_autonomy` | 2 |

| Rule ID | Count |
| --- | ---: |
| `AI_FEELING_OVER_EXPLAINED` | 1 |
| `AI_FEELING_OVER_SUMMARY` | 1 |
| `CONSEQUENCE_COST_NOT_RECORDED` | 1 |
| `CONSEQUENCE_NO_STATE_EVIDENCE` | 3 |
| `CONTEXT_NO_SCHEDULED_WORLD_EVENT_PRESSURE` | 2 |
| `CONTEXT_PROCEDURAL_STASIS_RISK` | 1 |
| `CONTEXT_SETTING_TOO_THIN` | 2 |
| `CONTEXT_TRANSITION_UNAVAILABLE` | 2 |
| `KNOWLEDGE_FORBIDDEN_REVEAL` | 1 |
| `KNOWLEDGE_UNKNOWN_CLAIM_USED` | 1 |
| `NPC_LIVELINESS_LOW_FIT_SCORE` | 3 |
| `NPC_TOOLMAN_CONVENIENT_COMPLIANCE` | 1 |
| `STYLE_EMPTY_GRANDIOSE_PROSE` | 1 |
| `STYLE_PARALLELISM_STACKING` | 1 |
| `WORLD_AUTONOMY_PROTAGONIST_ONLY_PRESSURE` | 1 |
| `WORLD_AUTONOMY_THREADS_IDLE` | 1 |

## 5. Successfully Caught Problems

The current Candidate A.1 audit successfully caught:

1. NPC toolman behavior.
   - `case_01_npc_toolman` emitted `NPC_TOOLMAN_CONVENIENT_COMPLIANCE`.

2. AI-feeling prose and decorative style.
   - `case_02_ai_overwritten_style` emitted summary, over-explanation, grandiose prose, and parallelism findings.

3. World pressure centered only on protagonist presence.
   - `case_03_world_protagonist_only` emitted `WORLD_AUTONOMY_PROTAGONIST_ONLY_PRESSURE`.

4. Knowledge-boundary violation.
   - `case_04_knowledge_boundary` emitted `KNOWLEDGE_FORBIDDEN_REVEAL` and `KNOWLEDGE_UNKNOWN_CLAIM_USED`; verdict was `FAIL`.

5. Missing consequences.
   - `case_05_missing_consequence` emitted both no-state-evidence and unrecorded-cost findings.

6. Scene stasis and missing runtime pressure.
   - `case_06_scene_stasis` emitted stasis, no scheduled event pressure, thin setting, missing transition, and thread-idle findings.

7. Thin setting context.
   - `case_07_thin_context` emitted `CONTEXT_SETTING_TOO_THIN`.

8. Crack-word false-positive guard.
   - `case_08_false_positive_guard_crack` emitted no findings. `表情裂開` and `腰牌裂紋` did not trigger `CONSEQUENCE_COST_NOT_RECORDED`.

## 6. Missed Or Underweighted Problems

The audit did not fully capture every intended flaw:

1. Direct emotion telling was under-detected.
   - `case_02_ai_overwritten_style` intentionally names emotions directly.
   - The audit caught summary/explanation/style, but did not emit `AI_FEELING_EMOTION_TOLD_NOT_SHOWN`.
   - This is partly because the narrative also contains generic action-like terms that suppress that rule.

2. Some obvious bad cases remain `PASS_WITH_WARNINGS`.
   - `case_01_npc_toolman`, `case_03_world_protagonist_only`, `case_05_missing_consequence`, and `case_07_thin_context` remain warning-level.
   - This is acceptable for Candidate A because it is advisory, but it is not strong enough for Candidate B gating.

3. NPC structural convenience is still mostly phrase/score based.
   - `case_01_npc_toolman` is caught because the narrative uses explicit compliance phrases.
   - Subtler toolman behavior without those phrases may still be missed.

4. Style quality remains partial.
   - The audit catches obvious summary, abstract grandeur, and parallelism.
   - It does not yet judge more nuanced old/modern Chinese tone balance.

## 7. False Positives

No confirmed false positives in this pack.

Important regression observation:

- `case_08_false_positive_guard_crack` passed with score `100`.
- This means the previously suspected `裂開` false-positive risk is not reproduced when the sample has a recorded event consequence and uses the phrase only as facial expression / object crack description.

Remaining caveat:

- The real Pilot sample collection still showed `CONSEQUENCE_COST_NOT_RECORDED` on `表情裂開` / `裂紋腰牌` contexts.
- The difference appears to depend on whether the result has enough consequence evidence, not only the word `裂開`.
- This should be investigated in a future calibration pass before turning consequence findings into a stronger gate.

## 8. Candidate A.1 Conclusion

Candidate A.1 is useful as a read-only reviewer aid:

- It catches the major deliberately flawed categories at least as warnings.
- It correctly fails a forbidden reveal / unknown-knowledge case.
- It catches context-quality problems that matched the earlier Pilot-1 coverage failures.
- It did not become noisy on the crack-word guard sample.

But it is still advisory:

- Some severe narrative-quality failures remain `PASS_WITH_WARNINGS`.
- The direct-emotion rule is conservative.
- Subtle NPC toolman behavior likely needs more evidence or stronger heuristics.

## 9. Candidate B Readiness

Do not enter Candidate B yet.

Reason:

- Candidate B would put findings closer to the resolve path, and this sample pack shows severity calibration is still not ready.
- The audit is good at reviewer hints, but not yet precise enough to become a pre-resolve gate.
- More calibration is needed for direct emotion telling, subtle NPC convenience, and consequence false-positive behavior.

## 10. Candidate A.2 Recommendation

Candidate A.2 is reasonable, but it should remain read-only and small.

Suggested Candidate A.2 scope:

1. Improve direct emotion telling detection so obvious emotion lists without concrete action are flagged.
2. Add a subtler NPC convenience fixture and decide whether the audit should catch it.
3. Investigate why real Pilot crack-word cases triggered `CONSEQUENCE_COST_NOT_RECORDED` while the guard fixture did not.
4. Keep findings advisory and do not modify Validator, `resolve-turn`, `StateReducer`, schema, or SQLite writes.

## 11. Verdict

FLAWED SAMPLE PACK VERDICT: `PASS_WITH_LIMITATIONS`

The sample pack is useful and the current read-only audit catches many deliberately bad outputs. It is not sufficient to proceed directly to Candidate B. The safer next milestone is Candidate A.2 calibration planning or additional fixture refinement, still without runtime write-path changes.
