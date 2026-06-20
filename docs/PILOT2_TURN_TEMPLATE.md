# Pilot-2 Turn Collection Template

Date: 2026-06-20

Status: `TEMPLATE_ONLY`

This document is a repeatable template for collecting Pilot-2 real story turns. It does not start Pilot-2 Turn 1, does not create result JSON, does not write SQLite, does not change runtime behavior, and does not approve Candidate B.

Use this together with `docs/PILOT2_SCENARIO_DESIGN.md`.

## 1. Pilot-2 Turn N Player Input Format

Preferred format:

```text
Pilot-2 Turn N

玩家目的：
<我想達成什麼，或想測什麼。>

玩家行動：
<我做了什麼具體動作。>

玩家台詞：
「<我實際說出口的話。>」

明確拒絕 / 邊界：
<我不同意什麼、不碰什麼、不去哪裡、不替誰承諾什麼。>

願意承擔的風險：
<我可以接受等待、被拒絕、被觀察、付出小代價，或其他明確風險。>

本回合想驗證的品質：
<例如 NPC 是否拒絕、world event 是否到期、transition 是否合法、memory 是否召回。>
```

Minimum format:

```text
Pilot-2 Turn N

玩家輸入：
<完整玩家行動與台詞。>

本回合底線：
<不得替玩家做的重大決定。>
```

Operator note: if the input lacks a clear player boundary, Codex may still proceed only when the action is low-risk. If the turn asks for a risky transition, consent, injury, arrest, separation, contract, or irreversible choice, stop and ask the user to clarify.

## 2. Turn 1 Suggested Player Input Example

The following is an example only. Do not treat it as an already executed turn.

```text
Pilot-2 Turn 1

玩家目的：
我想先了解封倉規矩，觀察誰真正掌控現場，不急著碰藥材或越線。

玩家行動：
我牽著愛醬停在粉線外，把手攤開給巡查的人看，表示自己沒有藏器物，也沒有要闖入。

玩家台詞：
「我不進倉，也不碰你們封住的東西。我只想問清楚：這些藥材是因為懷疑有毒才封，還是因為牽涉到雪犬才封？」

明確拒絕 / 邊界：
我不同意愛醬被帶走、被契印或被單獨檢查；我也不替任何商家作保。

願意承擔的風險：
我可以留在粉線外等候查問，也可以接受遠距離觀察，但不接受分開處置。

本回合想驗證的品質：
測 NPC 是否會依職責拒絕或設條件，而不是因我有禮貌就直接配合；也測現場規矩是否真的限制行動。
```

## 3. Files Codex Should Produce Or Save Each Turn

For each formal Pilot-2 turn, Codex should preserve these files when available:

| File | Required | Source |
| --- | --- | --- |
| player input | Yes | User-provided turn text |
| context JSON | Yes | `prepare-turn` output |
| context audit JSON | Preferred | `audit-context` output |
| result JSON | Yes after context passes | Human/Codex-authored result file |
| resolved evidence JSON | Preferred | `export-session-evidence` output |
| narrative audit JSON | Yes after result exists | `audit-narrative-quality` output |
| human notes | Preferred | User or operator scoring notes |

Generated artifacts should remain under ignored `out/` unless a future milestone explicitly approves committing selected fixtures.

Do not save generated Pilot-2 outputs into `samples/` by default.

## 4. Context / Result / Evidence Naming Rules

Use a stable scenario id:

```text
pilot2-qingli-herb-warehouse
```

Recommended directory:

```text
out/pilot2/pilot2-qingli-herb-warehouse/turns/
```

Recommended file names:

```text
turn_001_player_input.txt
turn_001_context.json
turn_001_context_audit.json
turn_001_result.json
turn_001_evidence.json
turn_001_narrative_audit.json
turn_001_human_notes.md
```

Use three-digit turn numbers:

```text
turn_001
turn_002
turn_003
```

Session-level files:

```text
out/pilot2/pilot2-qingli-herb-warehouse/session_preflight.json
out/pilot2/pilot2-qingli-herb-warehouse/session_evidence_after_turn_012.json
out/pilot2/pilot2-qingli-herb-warehouse/pilot2_summary_notes.md
```

Do not overwrite earlier turn files. If a result needs retry after Validator rejection, use:

```text
turn_001_result_retry_01.json
turn_001_result_retry_02.json
```

Then record which retry resolved successfully in the turn notes.

## 5. Per-Turn Codex Execution Steps

This section describes future execution only. Do not run it until the user provides an actual Pilot-2 DB, session, and player input.

For each turn:

1. Confirm current session state.

```powershell
gradlew.bat :app:run --args="show-session --db <db> --session <session-id>"
```

2. Save the exact player input to:

```text
out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_NNN_player_input.txt
```

3. Explain context budget if the turn asks for transition, scheduled pressure, capability use, or memory recall.

```powershell
gradlew.bat :app:run --args="explain-context-budget --db <db> --session <session-id> --input-file out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_NNN_player_input.txt --out out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_NNN_context_budget.json"
```

4. Prepare context.

```powershell
gradlew.bat :app:run --args="prepare-turn --db <db> --session <session-id> --input-file out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_NNN_player_input.txt --out out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_NNN_context.json"
```

5. Audit context.

```powershell
gradlew.bat :app:run --args="audit-context --file out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_NNN_context.json --out out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_NNN_context_audit.json"
```

6. Only after context passes, draft result JSON.

Do not draft result JSON if:

- context exceeds `24576` UTF-8 bytes;
- `audit-context` fails;
- semantic coverage is missing for explicit memory, transition, location, or capability use;
- the turn needs a player decision not present in the input.

7. Resolve turn through the formal CLI.

```powershell
gradlew.bat :app:run --args="resolve-turn --db <db> --session <session-id> --result out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_NNN_result.json"
```

8. Export evidence.

```powershell
gradlew.bat :app:run --args="export-session-evidence --db <db> --session <session-id> --out out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_NNN_evidence.json"
```

9. Run narrative quality audit.

```powershell
gradlew.bat :app:run --args="audit-narrative-quality --context out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_NNN_context.json --result out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_NNN_result.json --evidence out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_NNN_evidence.json --out out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_NNN_narrative_audit.json"
```

10. Validate the audit report as UTF-8 JSON.

```powershell
Get-Content -LiteralPath out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_NNN_narrative_audit.json -Encoding UTF8 -Raw |
    ConvertFrom-Json |
    Out-Null
```

11. Append human notes to:

```text
out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_NNN_human_notes.md
```

## 6. How To Run `audit-narrative-quality` Each Turn

Preferred command with evidence:

```powershell
gradlew.bat :app:run --args="audit-narrative-quality --context out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_001_context.json --result out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_001_result.json --evidence out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_001_evidence.json --out out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_001_narrative_audit.json"
```

Fallback command without evidence:

```powershell
gradlew.bat :app:run --args="audit-narrative-quality --context out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_001_context.json --result out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_001_result.json --out out/pilot2/pilot2-qingli-herb-warehouse/turns/turn_001_narrative_audit.json"
```

Expected:

- command exits `0`;
- report includes `verdict`, `score`, `severity`, `findings`, and `evidence`;
- report parses as UTF-8 JSON;
- no `--db` argument is needed for this audit command;
- audit remains advisory and does not change SQLite.

## 7. How To Judge NPC Liveliness Each Turn

Use this checklist in `turn_NNN_human_notes.md`:

```text
NPC liveliness score 1-5:

Which NPC mattered this turn:

Their own goal:

Their duty / fear / constraint:

Did they refuse, delay, bargain, misunderstand, or set conditions:

Did they remember earlier interaction:

Did they act from their own knowledge, not author-only truth:

Useful audit findings:

Noisy or missing audit findings:
```

Good signs:

- NPCs can say no.
- Cooperation has conditions.
- Their voice and priority differ from other NPCs.
- Their behavior follows visible memory, duty, risk, or relationship.

Bad signs:

- NPCs immediately solve the player's problem.
- NPCs reveal hidden truth to move the plot.
- NPCs forget their job because the player asks politely.
- Everyone sounds like the same narrator.

## 8. How To Judge World Autonomy Each Turn

Use this checklist:

```text
World autonomy score 1-5:

External pressure this turn:

Scheduled event status:

Offscreen NPC / faction action:

Location, weather, time, resource, or procedure pressure:

Did the world act only because the player asked:

Useful audit findings:

Noisy or missing audit findings:
```

Good signs:

- scheduled world events arrive when due;
- people offscreen keep acting;
- time, rain, goods, injury, procedure, or trade pressure matters;
- scene requirements or transition constraints affect options.

Bad signs:

- the world waits forever for the player;
- pressure appears without runtime support;
- a new authority or rule is invented only to push the scene;
- no scheduled event or transition pressure ever matters.

## 9. How To Judge Player Choice Consequences Each Turn

Use this checklist:

```text
Consequence score 1-5:

Player choice this turn:

Immediate consequence:

State delta / memory / belief / thread / scene change:

Relationship or trust change:

Cost, resource, time, or risk change:

Was the player's boundary respected:

Useful audit findings:

Noisy or missing audit findings:
```

Good signs:

- risky actions have visible cost or risk;
- refusal changes thread status or NPC attitude;
- waiting changes time pressure;
- careful observation creates evidence or memory;
- transition attempts succeed, fail, or become conditional through formal constraints.

Bad signs:

- dangerous action has no cost;
- refusal is ignored;
- the narrative says something mattered but no evidence changes;
- the result moves the player into a scene or agreement they did not choose.

## 10. When To Stop And Ask The User

Stop before generating result JSON or resolving a turn if:

- the user has not supplied actual player input;
- DB path, session id, project id, or current turn number is unknown;
- `show-session` does not match the expected turn;
- `prepare-turn` fails;
- `audit-context` fails;
- context exceeds `24576` UTF-8 bytes;
- required memory, transition, location, setting, or capability coverage is missing;
- the turn asks for a major player decision not present in the input;
- an NPC response would require author-only secret knowledge;
- the story would need a new legal rule, faction, scene, or transition not present in context;
- Validator rejects the result more than once for the same issue;
- continuing would require changing Kotlin, schema, Validator, `stateVersion`, `contextHash`, `resolve-turn`, `StateReducer`, or SQLite outside the formal CLI;
- the user asks to enter Candidate B, MVP-8, UI, server, API, Docker, Android, Rust, embedding, or vector DB work.

When stopping, report:

```text
What was completed:
Where it stopped:
Exact command/result:
What user decision or input is needed:
Whether any DB state changed:
```

## 11. Candidate B Reminder

Pilot-2 is evidence collection only.

Do not:

- add a pre-resolve quality gate;
- make `audit-narrative-quality` block `resolve-turn`;
- modify Validator;
- modify `stateVersion`;
- modify `contextHash`;
- modify `resolve-turn`;
- modify `StateReducer`;
- write SQLite outside formal CLI transactions;
- treat audit warnings as hard failures.

Candidate B requires a separate decision brief and explicit user approval.

## 12. Per-Turn Report Template

After a future Pilot-2 turn completes, report in this shape:

```text
Turn:
Narrative summary:
Player boundary respected:
NPC liveliness:
World autonomy:
Player choice consequence:
Memory / belief / scene / thread changes:
Scheduled event status:
Transition status:
Audit verdict / score:
Useful warnings:
Noisy warnings:
Suspected missed issues:
Context bytes:
Setting bytes:
Validator retries:
StateVersion before / after:
Files saved:
Counted as real human-input turn:
```

## 13. Next User Input Needed

To start Pilot-2 later, the user should provide:

```text
批准開始 Pilot-2 Turn 1。

DB：
<Pilot-2 DB path>

SESSION_ID：
<Pilot-2 session id>

預期回合：
Turn 1

玩家輸入：
<完整玩家行動與台詞>

本回合底線：
<不得替玩家做的重大決定>
```

If the Pilot-2 DB/session has not been created yet, ask for approval to prepare a fresh Pilot-2 DB/session as a separate milestone.

## 14. Verdict

`TURN_TEMPLATE_READY`

This template is ready for future Pilot-2 turn collection. No Pilot-2 turn has been executed by this document.
