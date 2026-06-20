# MVP-7 Flawed Audit Sample Pack

Purpose: deliberately flawed read-only fixtures for `audit-narrative-quality`.

These samples are not story canon. They are compact JSON context/result pairs used to check whether the current MVP-7 Candidate A.1 audit can detect obvious narrative quality problems and avoid known false positives.

Do not resolve these results into SQLite. Run them only through:

```powershell
gradlew.bat :app:run --args="audit-narrative-quality --context samples/mvp7_flawed_audit_pack/<case>/context.json --result samples/mvp7_flawed_audit_pack/<case>/result.json --out out/mvp7_flawed_audit_pack/<case>_report.json"
```

## Cases

| Case | Intentional target |
| --- | --- |
| `case_01_npc_toolman` | NPC conveniently serves the player without resistance. |
| `case_02_ai_overwritten_style` | Over-summary, over-explanation, direct emotion telling, parallelism, grand abstract prose. |
| `case_03_world_protagonist_only` | World pressure exists only because the protagonist is present. |
| `case_04_knowledge_boundary` | Forbidden reveal and unknown knowledge claim. |
| `case_05_missing_consequence` | Dangerous action with no state/event/thread consequence. |
| `case_06_scene_stasis` | Late active scene with thread pressure but no transition, event, time pressure, or progress. |
| `case_07_thin_context` | Nearly empty setting context despite omitted setting pressure. |
| `case_08_false_positive_guard_crack` | Contains `表情裂開` and `腰牌裂紋`; should not be treated as body injury or unrecorded cost. |

## Expected Use

- Commit the samples and this manifest.
- Generate reports under ignored `out/`.
- Record observed verdicts/findings in `docs/MVP7_FLAWED_AUDIT_SAMPLE_PACK_REPORT.md`.
- Do not adjust audit rules in this milestone.
