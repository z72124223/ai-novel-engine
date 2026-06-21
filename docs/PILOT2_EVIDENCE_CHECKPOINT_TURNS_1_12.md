# Pilot-2 Evidence Checkpoint After Turns 1-12

Date: 2026-06-21

Status: `EVIDENCE_CHECKPOINT`

## 1. VERDICT

`CHECKPOINT_PASS_WITH_LIMITATIONS`

Pilot-2 now has complete local artifacts（本機產物） for Turns 1-12: player input（玩家輸入）, context（上下文）, result（回合結果）, evidence（證據）, and narrative audit report（敘事稽核報告） files are present for every turn. The run provides useful evidence for NPC liveliness（NPC 活著感）, procedure pressure, player consequence, context budget（上下文預算） behavior, and transition（轉場） handling.

This checkpoint is not approval for Candidate B, MVP-8, Validator（驗證器） changes, runtime（執行期） changes, schema（資料結構） changes, `resolve-turn`, `StateReducer`, `stateVersion`, or `contextHash` work.

## 2. Sources And Limits

Sources read for this checkpoint:

- `docs/PILOT2_SCENARIO_DESIGN.md`
- `docs/PILOT2_TURN_TEMPLATE.md`
- `docs/PILOT2_TURN12_TRANSITION_INTENT_FINDING.md`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_001_evidence.json` through `turn_012_evidence.json`
- `out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_001_audit_report.json` through `turn_012_audit_report.json`
- related Turn 1-12 context and result artifacts under the same ignored `out/` directory

Source classification:

- Turns 1-11: player input files exist and do not contain a Codex-authored marker. This checkpoint treats them as prior collected Pilot-2 player-input evidence according to the established 01/03 workflow, but the local JSON artifacts alone do not independently prove human authorship.
- Turn 12: explicitly Codex-authored player input. It is useful operator-authored test evidence, but it must not be counted as real human-player evidence.

Implication:

- If 01 accepts the prior workflow provenance, Turns 1-11 are the counted human-player evidence set.
- Turn 12 remains excluded from human-player evidence counts.
- No `turn_NNN_human_notes.md` files were found for Turns 1-12, so human qualitative scores are still missing.

## 3. Turn State Summary

All rows below are from existing evidence/context artifacts and prior recorded export state. `pendingContexts=0` means no pending context residue was observed for the resolved turn in the available checkpoint evidence.

| Turn | Counted As Human Evidence | Result Used | Audit | Score | State Version | Next Turn | Current Scene | Pending Contexts | Context Bytes |
| --- | --- | --- | --- | ---: | ---: | ---: | --- | ---: | --- |
| 1 | Yes, provenance inherited | `turn_001_result.json` | PASS | 100 | 1 | 2 | `scene-qingli-herb-warehouse-perimeter` | 0 | 22212/24576 |
| 2 | Yes, provenance inherited | `turn_002_result.json` | PASS | 100 | 2 | 3 | `scene-qingli-herb-warehouse-perimeter` | 0 | 22504/24576 |
| 3 | Yes, provenance inherited | `turn_003_result.json` | PASS | 100 | 3 | 4 | `scene-qingli-herb-warehouse-perimeter` | 0 | 23509/24576 |
| 4 | Yes, provenance inherited | `turn_004_result.json` | PASS | 92 | 4 | 5 | `scene-qingli-herb-warehouse-perimeter` | 0 | 24065/24576 |
| 5 | Yes, provenance inherited | `turn_005_result.json` | PASS_WITH_WARNINGS | 84 | 5 | 6 | `scene-qingli-herb-warehouse-perimeter` | 0 | 20068/24576 |
| 6 | Yes, provenance inherited | `turn_006_result.json` | PASS_WITH_WARNINGS | 76 | 6 | 7 | `scene-qingli-herb-warehouse-perimeter` | 0 | 23201/24576 |
| 7 | Yes, provenance inherited | `turn_007_result.json` | PASS | 92 | 7 | 8 | `scene-qingli-herb-warehouse-perimeter` | 0 | 23818/24576 |
| 8 | Yes, provenance inherited | `turn_008_result.json` | PASS | 92 | 8 | 9 | `scene-qingli-herb-warehouse-perimeter` | 0 | 23727/24576 |
| 9 | Yes, provenance inherited | `turn_009_result.json` | PASS | 92 | 9 | 10 | `scene-qingli-herb-warehouse-perimeter` | 0 | 24004/24576 |
| 10 | Yes, provenance inherited | `turn_010_result.json` | PASS | 92 | 10 | 11 | `scene-qingli-herb-warehouse-perimeter` | 0 | 23906/24576 |
| 11 | Yes, provenance inherited | `turn_011_result.json` | PASS | 92 | 11 | 12 | `scene-qingli-herb-warehouse-perimeter` | 0 | 22959/24576 |
| 12 | No, Codex-authored | `turn_012_result_retry_01.json` | PASS | 92 | 12 | 13 | `scene-qingli-herb-warehouse-perimeter` | 0 | 21366/24576 |

Final checkpoint state:

- `stateVersion=12`
- `nextTurnNumber=13`
- `currentSceneId=scene-qingli-herb-warehouse-perimeter`
- `pendingContexts=0`
- `turnLogs=12`
- `stateDeltas=138`
- Formal scene transition: not completed

## 4. Audit Summary

Overall audit signal:

- Turns 1-3: PASS at 100.
- Turn 4: PASS at 92 with one warning.
- Turns 5-6: PASS_WITH_WARNINGS at 84 and 76.
- Turns 7-12: PASS at 92, each with one recurring advisory warning.

Recurring findings:

| Finding | Turns | Interpretation |
| --- | --- | --- |
| Active story threads exist at a later turn, but no pending scheduled world event pressure is present. | 5-12 | Main recurring gap. Pilot-2 has ongoing thread/task pressure, but scheduled world event pressure is not present in later contexts. |
| Narrative mentions injury, fatigue, or cost without matching state delta evidence. | 4, 6 | Possible consequence-evidence precision gap or false-positive risk; needs human review before any rule change. |
| Setting context is nearly empty while runtime-relevant setting pressure appears to exist. | 5 | One-turn context-support warning; later context compaction work appears to have kept contexts within budget. |
| Narrative uses repeated parallel structures. | 6 | Style warning, not a resolve blocker. |

Audit limitation:

- Audit findings remain advisory and read-only.
- No finding should be treated as a hard Validator or `resolve-turn` gate without a separate approved Candidate B decision.

## 5. Evidence By Quality Dimension

### NPC Liveliness

Evidence supports NPCs acting from constraints instead of simple compliance:

- 何巡丁 repeatedly holds the line, constrains movement, and requires witnessable procedure.
- 羅簿吏 records limited wording, refuses free responsibility expansion, and protects the seal/ledger process.
- 沈青禾 helps define medical evidence thresholds but refuses to turn incomplete information into open-goods authority.
- 藥商、買家使者、病患家屬, and 搬運工 create pressure from different incentives rather than becoming one cooperative helper.

Checkpoint signal:

- NPC liveliness is a relative strength of Turns 1-12.
- The strongest examples are procedural refusal, conditional cooperation, and visible fear of liability.

### World Autonomy

Evidence supports some world pressure:

- Buyer and merchant pressure recurs through multiple turns.
- Rainwater, runoff, cart tracks, ledger rules, patient urgency, and sealed-warehouse procedure continue to matter.
- Player waiting or narrowing responsibility does not stop the world from pushing back.

Confirmed gap:

- Scheduled world event pressure is repeatedly absent from later audit contexts.
- This is the most consistent audit warning from Turns 5-12.

### Player Consequence

Evidence supports meaningful consequences:

- Final Turn 12 evidence has cumulative deltas: `add_event=12`, `add_fact=56`, `update_character_state=60`, `upsert_task=10`.
- The player becomes associated with查水見證, limited liability, and named procedural risk.
- NPCs remember the player's involvement through facts, beliefs, character state flags, and task updates.
- The task `task-pilot2-legal-opening-path` remains active and has been updated toward guardrails such as守磅人車號, 包號, 藥味, and limited witness wording.

Checkpoint signal:

- The run produces visible state, fact, task, and character-state consequences.
- The lack of formal scene transition does not mean no consequence occurred.

### Transition Evidence

Transition surfaces exist:

- The context repeatedly exposes transition candidates such as awning witness route, runoff edge, and outside waiting boundary.
- Turn 12 showed that exact intent `go to awning with witness` can be recognized by the context / available-transition layer.

Confirmed unresolved issue:

- No formal scene transition has completed after Turn 12.
- Current scene remains `scene-qingli-herb-warehouse-perimeter`.
- Turn 12 first result with `transitionProposal` passed `validate-result`, but `resolve-turn` failed with `player_refused_transition`.
- Retry result resolved without transition and advanced to Turn 13.

Interpretation:

- Transition recognition and resolve-layer refusal detection can diverge when player input includes both exact movement intent and broad refusal/boundary wording.
- This is evidence for a future decision, not authorization to change Validator or transition parser behavior.

### Thread / Task Evidence

Thread/task evidence exists, but remains incomplete:

- `thread-ai-chan-suspicion` remains visible as an active thread in later contexts.
- `task-pilot2-legal-opening-path` remains the central task surface for legal opening, package number, medicine, witness, and liability path.
- Later contexts and audit reports repeatedly note absence of pending scheduled world event pressure.

Checkpoint signal:

- Task continuity is present.
- Scheduled pressure continuity is the clearest missing runtime evidence surface.

## 6. Confirmed Gaps

1. Scheduled world event pressure.
   - Repeated warning from Turns 5-12.
   - Do not fix by inventing pressure in result prose; any runtime change needs approval.

2. Formal scene transition.
   - Legal transition candidates exist.
   - Narrative/procedure moved within the perimeter, but formal `currentSceneId` never changed from `scene-qingli-herb-warehouse-perimeter`.

3. Turn 12 resolve-layer refusal heuristic.
   - Context layer recognized the exact intent.
   - First result transition proposal validated.
   - Formal resolve failed with `player_refused_transition`.

4. Human notes.
   - No `turn_NNN_human_notes.md` files found.
   - Audit output lacks paired human qualitative scoring.

5. Turn 12 provenance.
   - Turn 12 is Codex-authored and cannot count as human-player evidence.

## 7. Non-Goals And Guardrails

Do not use this checkpoint to:

- approve Candidate B;
- modify Validator;
- modify `resolve-turn`;
- modify `StateReducer`;
- modify schema or migrations;
- change `stateVersion` or `contextHash` logic;
- raise the `24576` context budget;
- bypass formal validation;
- write SQLite directly;
- count Turn 12 as human evidence;
- commit ignored `out/` or `data/` artifacts.

## 8. Recommended Next Options

01 can choose one of these safe next steps:

1. Wait for user-provided real Turn 13.
   - Best if Pilot-2 should continue as human-input evidence.
   - Turn 13 should be clearly authored by the user and should include explicit boundaries.

2. Explicitly approve a Codex-authored Turn 13.
   - Must be labeled non-human evidence.
   - Useful only for operator testing, not for human-turn collection count.

3. Write a transition/refusal heuristic decision brief.
   - Documentation only.
   - No Validator, parser, runtime, `resolve-turn`, or `StateReducer` implementation.

4. Stop Pilot-2 and perform summary acceptance.
   - Appropriate if 01 wants to preserve current evidence and avoid further turn execution until a product decision is made.

## 9. Checkpoint Conclusion

Pilot-2 Turns 1-12 are useful as a story-system evidence set, with one important provenance limitation: Turn 12 is Codex-authored. The strongest positive evidence is NPC constraint and consequence tracking. The strongest gaps are scheduled world event pressure and unresolved formal scene transition. The most specific technical finding is Turn 12's divergence between transition intent recognition and resolve-layer refusal handling.

Recommended default: ask the user for a real Turn 13, or have 01 explicitly choose a documentation-only decision brief before any Validator-adjacent work.
