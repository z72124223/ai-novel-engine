# Pilot-2 Codex-Authored Player Input Workflow

Date: 2026-06-21

Status: WORKFLOW_ALIGNMENT_ONLY

## 1. VERDICT

VERDICT: WORKFLOW_READY_NO_TURN13_START

Future Pilot-2 player input（玩家輸入） may be drafted by AI/Codex as Codex-authored player input（Codex 代寫玩家輸入） when 01 or the user explicitly approves it. Such input may be useful for stress testing（壓力測試） and system evidence（系統證據）, but it must never be counted as real human-player evidence（真人玩家證據）.

This document is a workflow（工作流程） alignment note. It does not create a new thread（聊天窗口）, does not start Pilot-2 Turn 13, does not run `prepare-turn`, `validate-result`, or `resolve-turn`, and does not approve Candidate B, Validator（驗證器）, runtime（執行期）, StateReducer（狀態化約器）, schema（資料結構）, migration（資料庫遷移）, `stateVersion`, or `contextHash` work.

## 2. Purpose

The purpose is to reduce role pollution（職責污染） across 01/02/03 and any optional 04 player-input simulation window:

- 01 should control milestone selection and decide whether input is real human, Codex-authored, or waiting.
- 02 should execute only the approved milestone and should not freely invent player input while also executing and reporting the same turn.
- 03 should perform read-only acceptance review and should not be treated as the author of player choices.
- Optional 04 may draft player intent/action/dialogue/boundaries/risks/quality targets, but must not execute repo commands or validate its own result.

This keeps evidence labels clear and prevents one window from acting as player, executor, and reviewer in the same loop.

## 3. Evidence Level Rules

Codex-authored player input is allowed only under explicit 01/user direction.

Evidence levels:

| Input Source | Allowed Use | Evidence Count | Required Label |
| --- | --- | --- | --- |
| Real user / human player | Pilot-2 human evidence collection | Counts as real human-player evidence if provenance is recorded | `human-authored` or equivalent note |
| Codex-authored by 01/02/04 | System probe, stress test, transition/refusal investigation, workflow dry run | Does not count as real human-player evidence | `Codex-authored player input; non-human evidence` |
| Unknown / inherited provenance | Historical reference only until accepted by 01 | Count only if 01 accepts prior provenance | `provenance inherited` or `provenance unclear` |

Minimum label for any future Codex-authored input:

```text
Evidence classification:
Codex-authored player input. This is system-probe evidence only and must not be counted as real human-player evidence.
```

## 4. Codex-Authored Player Input Rules

Codex-authored player input may:

- stress-test transition intent recognition（轉場意圖識別）;
- test refusal heuristic（拒絕啟發式） behavior;
- test context budget（上下文預算） and evidence surfaces;
- exercise NPC boundary handling and player-consequence recording;
- produce controlled non-human evidence for future decision briefs.

Codex-authored player input must:

- be explicitly approved by 01 or the user before use;
- be labeled in the player input file, result notes, evidence report, and final handoff when feasible;
- stay separate from real human evidence counts;
- preserve player boundaries rather than using the operator's desired outcome to force story success;
- avoid being used as proof that Pilot-2 has enough real human turns.

Codex-authored player input must not:

- be described as real human input;
- be used to satisfy Pilot-2 human-turn success conditions;
- authorize Candidate B;
- authorize Validator/runtime/StateReducer/schema changes;
- justify forcing a transition after `player_refused_transition`.

## 5. Recommended Optional 04 Role

If 01/user explicitly approves a 04 player-input simulation window, 04 should only produce player-input text and should not touch repo state.

04 responsibilities:

- Draft player purpose（玩家目的）.
- Draft concrete player action（玩家行動）.
- Draft player dialogue（玩家台詞）.
- Draft boundaries / refusals / non-consent areas（邊界 / 不同意事項）.
- Draft accepted risks（願意承擔的風險）.
- Draft intended quality checks（想驗證的品質）.
- Clearly label the input as Codex-authored and non-human evidence.

04 must not:

- modify repo files;
- run CLI commands;
- touch DB files;
- run `prepare-turn`, `validate-result`, or `resolve-turn`;
- generate result JSON unless explicitly reassigned by 01 in a separate milestone;
- commit or push;
- act as 03 acceptance reviewer for its own input.

Suggested 04 output shape:

```text
Pilot-2 Turn N

Evidence classification:
Codex-authored player input. This is system-probe evidence only and must not be counted as real human-player evidence.

玩家目的：
...

玩家行動：
...

玩家台詞：
「...」

邊界 / 不同意事項：
...

願意承擔的風險：
...

本回合想驗證的品質：
...
```

Note: use `邊界 / 不同意事項` instead of `明確拒絕 / 邊界` when testing transitions, unless 01 intentionally wants to test the refusal keyword path.

## 6. Role Closure Loop

Recommended role split:

| Role | Main Duty | May Author Player Input | May Execute Turn | May Review |
| --- | --- | --- | --- | --- |
| 01 主控 | Decide next milestone and approval boundaries | Yes, if explicitly choosing Codex-authored input | Usually no | Can accept/reject handoff |
| 02 執行 | Execute approved milestone, update docs, run validation, commit/push when approved | Only if 01/user supplies exact input or explicitly directs 02 to draft Codex-authored input | Yes | No final 03-style acceptance of its own work |
| 03 驗收 | Read-only review of 02 output | No | No | Yes |
| 04 optional | Draft player input simulation only | Yes, Codex-authored only | No | No |

Closed loop examples:

1. Human input path:
   - User provides Turn 13 input.
   - 01 approves Turn 13 execution.
   - 02 executes prepare/result/validate/resolve/evidence.
   - 03 reviews.

2. Optional 04 Codex-authored path:
   - 01 approves 04 to draft Turn 13 input.
   - 04 drafts only player input and labels it non-human evidence.
   - 01 reviews/approves the input.
   - 02 executes the approved turn.
   - 03 reviews.

3. 01-supplied Codex-authored path:
   - 01 provides exact Codex-authored input text and evidence classification.
   - 02 executes only that approved input.
   - 03 reviews.

## 7. If There Is No 04

If no 04 exists, 02 should not freely choose player intent and then execute and report the same turn as though it were independent evidence.

Allowed:

- 02 may execute player input explicitly supplied by the user or 01.
- 02 may draft Codex-authored input only when 01/user explicitly requests 02 to draft it and labels the resulting turn non-human evidence.
- 02 may recommend input-format guardrails in documents.

Not allowed:

- 02 should not silently become the player.
- 02 should not count its own drafted player input as real human evidence.
- 02 should not make 03-style acceptance claims about its own turn outcome.

## 8. Turn 13 Optional Paths

Turn 13 has three safe path options:

1. Wait for real human input.
   - Best for Pilot-2 human evidence collection.
   - Evidence level: real human-player evidence if provenance is recorded.

2. 04 drafts Codex-authored input.
   - Requires explicit 01/user approval to create or use 04.
   - Evidence level: non-human system-probe evidence.
   - 02 should execute only after 01 approves the drafted input.

3. 01 explicitly assigns Codex-authored input.
   - 01 provides the exact text or explicitly instructs 02 to draft within a named scope.
   - Evidence level: non-human system-probe evidence.
   - The final report must repeat the evidence classification.

None of these paths is started by this document.

## 9. Prohibitions

Do not:

- treat Codex-authored input as real human-player evidence;
- enter Candidate B or MVP-8;
- modify Validator, runtime, StateReducer, schema, migrations, `stateVersion`, `contextHash`, or `resolve-turn`;
- start Pilot-2 Turn 13 from this workflow note;
- create a new thread unless 01/user explicitly approves it in a separate instruction;
- let 02 simultaneously invent player input, execute the turn, and self-approve the result as acceptance evidence;
- commit ignored `data/` or `out/` artifacts by default.

## 10. Recommended Next Step

Recommended next 01 decision:

1. Ask the user for real Turn 13 input if Pilot-2 should continue as human evidence.
2. Or explicitly approve a 04 player-input simulation window to draft a Codex-authored Turn 13 input.
3. Or explicitly assign 02 a Turn 13 preflight/document-only milestone while waiting for human input.

Until 01 chooses one of those paths, 02 should stop after this workflow document and wait.
