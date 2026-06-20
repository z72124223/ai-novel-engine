# MVP-7 Candidate A.2 Calibration Plan

This document plans a small read-only calibration pass for `audit-narrative-quality`. It does not approve implementation by itself.

## 1. Candidate A.2 Goal

Candidate A.2 should improve the precision of the existing read-only narrative quality audit in three narrow areas found by calibration evidence:

1. Detect obvious emotion telling when the narrative names emotions without enough concrete behavior.
2. Detect subtler NPC toolman behavior where an NPC loses independent motive, procedure, risk awareness, or resistance evidence without using the exact phrase `立刻答應`.
3. Reduce likely false positives in `CONSEQUENCE_COST_NOT_RECORDED`, especially when `裂開` describes facial expression, voice, object condition, or metaphor rather than bodily injury, fatigue, resource loss, or paid cost.

Candidate A.2 remains advisory. It must not become a Validator gate, pre-resolve blocker, story rewriter, or SQLite writer.

## 2. Non-Goals

- Do not enter Candidate B.
- Do not write SQLite.
- Do not modify `resolve-turn`, `StateReducer`, `AiTurnResultValidator`, `stateVersion`, or `contextHash`.
- Do not add schema or migration changes.
- Do not rewrite narrative output automatically.
- Do not turn narrative quality findings into hard turn rejection.
- Do not add server, API, port listener, Web UI, Android UI, Rust, Docker, embedding, vector DB, external AI calls, or multi-agent runtime.
- Do not retag `mvp-6`.

## 3. Problems The Current Audit Already Catches

Confirmed from `docs/MVP7_AUDIT_CALIBRATION_REPORT.md`, `docs/MVP7_AUDIT_SAMPLE_COLLECTION_REPORT.md`, `docs/MVP7_FLAWED_AUDIT_SAMPLE_PACK_REPORT.md`, and current tests:

- Forbidden reveal and unknown knowledge claim usage.
- Missing NPC actions or low `characterFitScore`.
- Convenient NPC compliance when expressed with direct unnegated phrases such as `立刻答應`.
- Negated convenient compliance such as `沒有立刻答應` no longer triggers the old false positive.
- Over-summary, over-explanation, repeated parallelism, and empty grandiose prose in obvious cases.
- Missing state consequence evidence.
- Thin setting context, missing legal transition surface, missing scheduled world event pressure, and late procedural stasis risk.
- Crack-word guard sample `case_08_false_positive_guard_crack` passes when the result has enough non-cost consequence evidence.

## 4. Problems The Current Audit Misses Or Underweights

Confirmed or strongly indicated by the sample reports:

- Direct emotion telling can be under-detected when generic action cue words appear in the same narrative.
- Subtle NPC toolman behavior can pass if the narrative avoids literal phrases such as `立刻答應` but still makes the NPC serve player convenience without procedure, cost, fear, goal, or authority constraints.
- Some deliberately flawed cases remain `PASS_WITH_WARNINGS` even when a human reviewer would treat them as poor quality.
- Real Pilot sample collection still produced likely `CONSEQUENCE_COST_NOT_RECORDED` false positives around `表情裂開`, `裂紋腰牌`, and similar non-injury uses.
- The current audit is useful as a reviewer hint, but not calibrated enough for Candidate B or any pre-resolve gate.

## 5. Known False-Positive Risks

1. `CONSEQUENCE_COST_NOT_RECORDED` currently treats `裂開` as an injury/cost cue.
   - Good trigger: `傷口裂開`, `皮肉裂開`, `骨裂`, `氣血反噬`, `疲憊`, `消耗`, `代價`.
   - Risky trigger: `表情裂開`, `聲音裂開`, `裂紋腰牌`, `裂開的門板`, `裂開般的沉默`.

2. NPC convenience phrases can be safe if clearly negated.
   - Candidate A.1 fixed the known `沒有立刻答應` case.
   - A.2 should preserve that protection and extend tests around nearby negation patterns only if implementation is later approved.

3. Context-quality observations can describe missing context surfaces, not necessarily bad result prose.
   - They should remain advisory and should not be treated as direct narrative failure.

## 6. Emotion Telling Detection Plan

If Candidate A.2 implementation is approved, refine `AI_FEELING_EMOTION_TOLD_NOT_SHOWN` around evidence balance instead of simple term presence.

Planned heuristic direction:

- Count direct emotion labels such as `害怕`, `恐懼`, `悲傷`, `憤怒`, `焦慮`, `緊張`, `震驚`, `崩潰`, `愧疚`, `安心`, `感動`.
- Count visible behavior cues, but weight them by specificity:
  - Strong cues: body part plus action, e.g. `指節發白`, `喉結滾動`, `退半步`, `握緊`, `屏住呼吸`, `目光避開`, `尾巴壓低`.
  - Weak cues: generic verbs such as `看`, `停`, `說`, `想`, `覺得`.
- Flag when emotion labels dominate and behavior cues are weak, generic, or absent.
- Avoid flagging when emotion labels are paired with concrete sensory or behavioral evidence.
- Keep severity as `WARNING` unless later evidence justifies otherwise.

Candidate A.2 tests should prove:

- A narrative listing emotions directly without concrete behavior triggers the finding.
- A narrative showing emotion through action without explicit labels does not trigger.
- A mixed narrative with one emotion word plus enough behavior does not over-trigger.

## 7. Subtle NPC Toolman Detection Plan

If Candidate A.2 implementation is approved, add a small advisory rule for structural convenience, not just phrase matching.

Signals to consider:

- NPC has an explicit goal, fear, duty, procedure, risk, or constraint in context, but the result shows immediate compliance without referencing any of them.
- NPC action grants the player the desired outcome while `characterFitScore` is mediocre or absent.
- NPC has no visible action except answering or permitting the player's request.
- NPC ignores known hard rules, authority pressure, personal risk, or scene danger without a reason.
- Result contains compliance verbs such as `允許`, `放行`, `照辦`, `配合`, `答應`, `讓開`, `交出`, `帶路`, but lacks resistance, condition, procedure, negotiation, or cost terms.

False-positive guard:

- Do not flag when the NPC complies after a recorded condition, cost, scene requirement, transition prerequisite, explicit belief change, or prior memory evidence.
- Do not flag when the NPC refuses, modifies the request, delays, escalates, or preserves procedure.
- Keep this advisory and contextual; it is not a correctness validator.

Candidate A.2 tests should include:

- A subtle toolman NPC who grants access without condition despite duty context.
- A non-toolman NPC who cooperates conditionally after evidence and procedure.
- A refusal or modified plan that should not trigger.

## 8. `CONSEQUENCE_COST_NOT_RECORDED` False-Positive Reduction Plan

If Candidate A.2 implementation is approved, split broad cost words into stronger categories:

1. Body injury / fatigue cues:
   - Examples: `傷口`, `流血`, `骨`, `皮肉`, `疼`, `痛`, `疲憊`, `氣血`, `反噬`, `消耗`, `代價`.
   - These can still require matching state delta evidence.

2. Object crack / expression crack / metaphor cues:
   - Examples: `表情裂開`, `聲音裂開`, `腰牌裂紋`, `裂紋`, `裂開的木板`, `裂開般`.
   - These should not trigger cost evidence unless nearby words indicate bodily injury, capability cost, or resource loss.

3. Capability-use cost cues:
   - If a result records `capabilityUseChecks` or applied cost language, ensure a related cost state delta exists only when the capability contract includes costs.

Candidate A.2 tests should prove:

- `傷口裂開` without state delta triggers.
- `表情裂開` with ordinary event consequence does not trigger.
- `裂紋腰牌` as item description does not trigger.
- `疲憊` after capability use without cost delta triggers.
- Recorded fatigue/cost state delta suppresses the warning.

## 9. Flawed Sample Cases To Add Or Update

Recommended sample changes for a future A.2 implementation milestone:

1. Add `case_09_emotion_telling_without_behavior`.
   - Purpose: obvious direct emotion labels, almost no physical action.
   - Expected: `AI_FEELING_EMOTION_TOLD_NOT_SHOWN`.

2. Add `case_10_emotion_shown_with_behavior_guard`.
   - Purpose: concrete behavior without over-explaining emotion.
   - Expected: no emotion-telling finding.

3. Add `case_11_subtle_npc_toolman_access_grant`.
   - Purpose: NPC grants access or permission despite duty/risk context, without explicit `立刻答應`.
   - Expected: subtle NPC toolman observation.

4. Add `case_12_conditional_npc_cooperation_guard`.
   - Purpose: NPC cooperates only after condition, memory, or procedure.
   - Expected: no toolman finding.

5. Update or add crack/cost false-positive cases:
   - `表情裂開` guard.
   - `裂紋腰牌` guard.
   - `傷口裂開` true positive.
   - `疲憊` after capability cost true positive.

These samples should stay in `samples/mvp7_flawed_audit_pack/` and should not require SQLite.

## 10. Tests To Add Or Update

If implementation is later approved, update `Mvp7NarrativeQualityAuditTest` with focused unit tests:

- `auditFlagsDirectEmotionTellingWithoutVisibleBehavior`
- `auditDoesNotFlagEmotionShownThroughConcreteBehavior`
- `auditFlagsSubtleNpcToolmanConvenience`
- `auditDoesNotFlagConditionalNpcCooperation`
- `auditDoesNotTreatObjectCrackAsUnrecordedCost`
- `auditFlagsBodyInjuryCrackWithoutCostDelta`
- `auditTreatsRecordedFatigueCostAsConsequence`

These tests should instantiate `ContextPacket` and `AiTurnResult` in memory or read fixtures. They should not open SQLite.

## 11. No SQLite Write Guarantee

Candidate A.2 must continue to use only:

- JSON context files.
- JSON result files.
- optional evidence text or JSON files.
- `NarrativeQualityAuditService.audit(...)`.
- `audit-narrative-quality`.

It must not accept `--db`, open JDBC connections, call repository write APIs, call `resolve-turn`, call `StateReducer`, or create pending contexts.

## 12. No `resolve-turn` / `StateReducer` Change Guarantee

Candidate A.2 implementation, if approved, must not change:

- `TurnRunner.resolveTurn` behavior.
- `StateReducer` behavior.
- `AiTurnResultValidator` security boundaries.
- transaction handling.
- `stateVersion` checks.
- `contextHash` checks.
- replay/checkpoint/branch writes.

The audit report can inform human review only. It must not become part of the formal turn write path.

## 13. Minimum Acceptable A.2 Implementation Scope

The smallest acceptable implementation would be:

1. Refine emotion-telling observation.
2. Add subtle NPC toolman observation.
3. Reduce `CONSEQUENCE_COST_NOT_RECORDED` crack-word false positives.
4. Add targeted tests.
5. Update flawed sample pack and A.2 report.
6. Keep CLI contract stable and read-only.

Anything beyond this, especially Candidate B gate behavior, should be rejected unless separately approved.

## 14. A.2 Implementation Done Criteria

A future A.2 implementation can be considered complete only if:

- `git diff --check` passes.
- `gradlew.bat clean test --no-daemon` passes.
- `gradlew.bat :app:installDist` passes.
- Repo CLI sample audit passes.
- installDist sample audit passes.
- Flawed sample pack audit output shows:
  - emotion-telling true positive is caught,
  - behavior-based emotion guard remains quiet,
  - subtle NPC toolman true positive is caught,
  - conditional cooperation guard remains quiet,
  - object/expression crack false positives are reduced,
  - bodily injury or real fatigue/cost true positives remain caught.
- No SQLite writes occur.
- No runtime write path changes occur.

## 15. When Not To Enter Candidate B

Do not enter Candidate B if any of these remain true:

- Findings are still noisy on normal Pilot turns.
- Direct emotion telling or subtle toolman behavior remains mostly uncaught.
- Consequence findings still conflate object/expression cracks with bodily injury or capability cost.
- Context-quality observations are being interpreted as hard result failures.
- The sample set is still dominated by one Pilot arc.
- There is no explicit Candidate B decision brief.
- Candidate B would touch `resolve-turn`, `StateReducer`, Validator, `stateVersion`, `contextHash`, or SQLite writes without a separate safety design.

## 16. When Candidate B Can Be Discussed

Candidate B can be discussed only after:

- Candidate A.2, if approved, passes tests and sample calibration.
- At least one additional real or near-real sample set is audited after A.2.
- The team decides whether narrative-quality findings should remain advisory or become a soft preflight gate.
- A separate Candidate B decision brief defines:
  - exact entry point,
  - non-goals,
  - rollback plan,
  - false-positive tolerance,
  - human override behavior,
  - proof that Validator, `stateVersion`, `contextHash`, transactions, and SQLite write boundaries remain intact.

Until then, Candidate A.2 should remain a read-only calibration step, not a runtime enforcement step.
