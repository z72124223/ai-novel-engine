# Pilot-2 Scenario Design

Date: 2026-06-20

Status: `SCENARIO_DESIGN_ONLY`

This document designs a Pilot-2 real-use scenario for collecting better MVP-7 narrative-quality audit samples. It does not implement runtime behavior, does not change audit rules, does not write SQLite, does not open Candidate B, and does not approve MVP-8.

## 1. Pilot-2 Goal

Pilot-2 exists to collect a new real story arc outside the current Pilot-1 / Pilot-1b / MVP-6 fixture family. The output should give `audit-narrative-quality` more realistic context（上下文）, result（回合結果）, and evidence（證據包） pairs to judge:

- whether NPCs act from their own goals, duties, fears, memories, and limits;
- whether the world creates pressure without serving only the player;
- whether prose becomes too AI-like, too explanatory, or too neatly patterned;
- whether player choices leave visible state, relationship, thread, memory, or scene consequences;
- whether the read-only audit（只讀稽核） remains useful without becoming too noisy.

Pilot-2 is not a proof that Candidate B is safe. It is an evidence-collection scenario.

## 2. Non-Goals

Pilot-2 does not:

- change Kotlin code;
- change tests;
- change audit rules;
- change schema or migrations;
- change Validator, `stateVersion`, `contextHash`, `resolve-turn`, or `StateReducer`;
- write SQLite directly;
- open a server, API, port, Web UI, Android UI, Docker, Rust, embedding, vector database, or multi-agent runtime;
- retag `mvp-6`;
- create a pre-resolve quality gate;
- force the story to succeed;
- require NPCs to cooperate;
- require a scene transition to happen if the rules do not support it.

## 3. Narrative Quality Issues To Test

Pilot-2 should deliberately create chances to observe these issues:

1. NPC liveliness（NPC 活著感）
   - An NPC should sometimes refuse, delay, bargain, misunderstand, or protect their own duty.
   - A cooperative NPC should still have reasons, limits, and risks.

2. AI-feeling prose（AI 感過重）
   - The story should avoid empty summary, direct emotion labels, excessive parallelism, and abstract moralizing.
   - The audit should flag obvious cases but not punish concrete action-supported emotion.

3. World autonomy（世界自主推進）
   - Scheduled or offscreen pressure should occur because of time, duty, faction, danger, or location rules, not just because the player is present.

4. Knowledge boundary（知識邊界）
   - NPCs may guess or suspect, but they must not know author-only secrets or unrevealed truths.

5. Consequence（後果感）
   - Risky actions should leave a cost, relation shift, memory, thread movement, scene change, or visible refusal.

6. Scene and thread movement（場景與劇情線推進）
   - The scenario should include at least one possible legal transition, one blocked or conditional transition, and one thread that advances or becomes blocked for evidence-based reasons.

7. Setting context precision（設定上下文精準度）
   - Context must include the public rules, scene constraints, transition constraints, scheduled event pressure, item or technique limits relevant to the turn.

## 4. Recommended Story Scenario

Recommended Pilot-2 scenario: **青犁鎮藥市封倉**.

This scenario stays close to the existing Taiheng setting but moves away from the exact abandoned-post snow-dog Pilot. It tests social pressure, resource rules, NPC duty, faction procedure, and slow-burn consequence.

### 4.1 Premise

Rain has ruined several carts of medicinal herbs outside Qingli Town. A temporary herb warehouse has been sealed by local trade clerks because one batch may be contaminated by unknown beast blood. The player and Ai-chan are not inside town; they are near the sealed warehouse perimeter, seeking medicine, shelter, or proof that Ai-chan is not responsible for recent incidents.

The warehouse has:

- merchants who care about loss and reputation;
- a clerk who cares about seal procedure;
- a physician who wants samples but cannot openly violate rules;
- a patrol officer who suspects unregistered outsiders;
- an offscreen buyer or faction envoy who will arrive soon and pressure everyone.

### 4.2 Main Scene

`scene-qingli-herb-warehouse-perimeter`

Visible elements:

- sealed herb warehouse door;
- rainwater runoff channel;
- damaged herb carts;
- chalk quarantine line;
- a registry table;
- a locked sample chest;
- Ai-chan near the player;
- NPCs arguing over whether to burn, preserve, or inspect the herbs.

### 4.3 Suggested NPC Roles

| NPC Role | Goal | Fear / Constraint | What Good Play Should Show |
| --- | --- | --- | --- |
| Warehouse clerk | Preserve procedure and avoid blame. | Breaking seal rules may cost position or invite punishment. | May refuse the player even if the player is useful. |
| Physician | Obtain enough evidence to treat possible poison or contamination. | Cannot openly steal samples or endanger patients. | May ask for observation, not blind trust. |
| Patrol officer | Keep unregistered people and animals controlled. | If the situation worsens, blame may fall on patrol. | May bargain, constrain movement, or demand witnesses. |
| Merchant representative | Protect goods, profit, and reputation. | Delay means financial loss. | May pressure others, hide partial truth, or demand fast action. |

Names may be filled later. The scenario design does not require hardcoding these NPCs into code.

### 4.4 Public Rules To Surface

- A sealed warehouse cannot be opened without an accountable witness or emergency exception.
- Contaminated medicinal goods may be burned, quarantined, or sampled depending on authority and evidence.
- Unregistered outsiders may be kept outside the town gate and restricted near evidence.
- A beast or dog suspected of contamination may be observed, but forced handling should require formal cause.

### 4.5 Hidden Or Restricted Truths

Pilot-2 may include hidden truths, but they must remain unrevealed unless the engine legitimately reveals them later. Example hidden material:

- The contamination may be caused by illegal dilution, not Ai-chan.
- A merchant clerk may have falsified one inventory seal.
- The offscreen buyer may belong to a faction that wants the batch destroyed for unrelated reasons.

These are author-only until revealed. They must not be placed in character-facing context.

### 4.6 Scheduled Pressure

At least one scheduled world event should be defined or selected before play:

- `world-event-herb-buyer-arrival-pressure`
- Due around Turn 4 to Turn 6.
- Public effect: a buyer envoy or trade supervisor approaches and pressures the clerk to either certify, burn, or transfer the goods.
- Forbidden uses: must not expose hidden culprit, must not force the player to enter town, must not force Ai-chan to be separated from the player.

### 4.7 Transition Opportunities

Pilot-2 should include at least one legal transition candidate:

- From warehouse perimeter to a rain-sheltered inspection awning.
- From perimeter to runoff channel edge.
- From perimeter to outside waiting boundary.

The transition should require explicit player intent and relevant conditions. It must not be forced by scheduled pressure alone.

## 5. Player Input Format

Use one block per human turn:

```text
Pilot-2 Turn N

玩家目的：
我想測什麼，或想達成什麼。

玩家行動：
我做了什麼具體動作。

玩家台詞：
「我實際說出口的話。」

明確拒絕 / 邊界：
我不同意什麼，不碰什麼，不去哪裡，不替誰承諾什麼。

願意承擔的風險：
我接受等待、被觀察、被拒絕、付出小代價，或其他明確風險。

本回合想驗證的品質：
例如 NPC 是否會拒絕、world event 是否到期、transition 是否合法、memory 是否召回。
```

If the player only wants to keep the story moving, the minimum input is:

```text
Pilot-2 Turn N

玩家輸入：
<完整玩家行動與台詞>

本回合底線：
<不得替我做的重大決定>
```

## 6. Per-Turn Data To Collect

Each formal Pilot-2 turn should collect:

- player input text;
- context JSON;
- context audit report if available;
- result JSON;
- exported session evidence if available;
- narrative quality audit report;
- Validator retry count and reason, if any;
- context actual bytes and max bytes;
- setting actual bytes and max bytes;
- selected memories and knowledge claims;
- active scene and transition information;
- active story threads;
- pending or consumed scheduled world events;
- human rating notes.

Human rating notes should include four 1 to 5 scores:

| Score | Meaning |
| --- | --- |
| NPC naturalness | Did NPCs feel like people with their own constraints? |
| Plot movement | Did the turn change pressure, options, knowledge, or state? |
| Emotion credibility | Were emotions shown through behavior and context? |
| Setting use | Did rules, items, locations, and events matter without flooding context? |

Also record:

- useful audit warnings;
- noisy audit warnings;
- suspected missed issues;
- whether the player felt overruled;
- whether secrets stayed unrevealed.

## 7. Naming Rules

Generated files should stay under ignored `out/` unless a future milestone explicitly approves committing selected fixtures.

Recommended path:

```text
out/pilot2/<scenario-id>/turns/
```

Recommended names:

```text
turn_001_player_input.txt
turn_001_context.json
turn_001_context_audit.json
turn_001_result.json
turn_001_evidence.json
turn_001_narrative_audit.json
turn_001_human_notes.md
```

Session-level evidence:

```text
out/pilot2/<scenario-id>/session_evidence_after_turn_012.json
out/pilot2/<scenario-id>/pilot2_summary_notes.md
```

Do not commit generated `out/` files by default.

## 8. Audit Execution

Use the existing read-only CLI（命令列介面） command:

```powershell
gradlew.bat :app:run --args="audit-narrative-quality --context out/pilot2/<scenario-id>/turns/turn_001_context.json --result out/pilot2/<scenario-id>/turns/turn_001_result.json --evidence out/pilot2/<scenario-id>/turns/turn_001_evidence.json --out out/pilot2/<scenario-id>/turns/turn_001_narrative_audit.json"
```

If a turn has no evidence file, omit `--evidence`:

```powershell
gradlew.bat :app:run --args="audit-narrative-quality --context out/pilot2/<scenario-id>/turns/turn_001_context.json --result out/pilot2/<scenario-id>/turns/turn_001_result.json --out out/pilot2/<scenario-id>/turns/turn_001_narrative_audit.json"
```

Validate audit JSON as UTF-8:

```powershell
Get-Content -LiteralPath out/pilot2/<scenario-id>/turns/turn_001_narrative_audit.json -Encoding UTF8 -Raw |
    ConvertFrom-Json |
    Out-Null
```

Node.js alternative:

```powershell
node -e "const fs=require('fs'); JSON.parse(fs.readFileSync(process.argv[1], 'utf8')); console.log('JSON OK')" out/pilot2/<scenario-id>/turns/turn_001_narrative_audit.json
```

## 9. Success Conditions

Pilot-2 is successful as an evidence collection if:

- at least 8 real human-input turns are completed, with 12 preferred;
- each counted turn has a context JSON and result JSON;
- at least 6 counted turns have evidence JSON or a documented reason why evidence is unavailable;
- all audit reports parse as UTF-8 JSON;
- no forbidden secret is prematurely revealed;
- the player is not forced into a major action they did not input;
- at least one NPC reasonably refuses, delays, or constrains the player;
- at least one NPC reasonably cooperates with conditions;
- at least one scheduled pressure appears naturally;
- at least one legal transition is proposed, attempted, completed, or explicitly blocked by formal constraints;
- at least one memory or belief changes and later matters;
- at least one player choice creates a state, relation, resource, thread, or scene consequence;
- a final report compares audit findings with human notes.

## 10. Failure Conditions

Pilot-2 should be marked failed or incomplete if:

- fewer than 8 human turns are collected;
- context/result pairs are missing for most turns;
- audit reports cannot be parsed as UTF-8 JSON;
- generated data is accidentally committed without approval;
- the story reveals author-only secrets too early;
- NPCs repeatedly comply with the player without their own reasons;
- the world only reacts to the player and no external pressure appears;
- the scenario has no legal transition surface;
- player boundaries are overwritten;
- audit warnings are too vague to act on;
- the run requires code, schema, Validator, or write-path changes to continue.

## 11. Judging NPC Liveliness

An NPC has liveliness if evidence can answer:

- What does this NPC want in this scene?
- What are they protecting?
- What are they afraid of losing?
- What procedure, duty, relationship, item, memory, belief, or risk constrains them?
- What would they refuse even if the player wants it?
- Did their later behavior remember earlier interaction?

Warning signs:

- The NPC immediately obeys every request.
- The NPC explains hidden truth without a reason.
- The NPC forgets their duty when the player asks nicely.
- Different NPCs share the same voice, rhythm, and priorities.
- The NPC acts from the story's need rather than their own knowledge.

## 12. Judging World Autonomy

The world has autonomy if pressure comes from:

- due scheduled events;
- faction or trade procedure;
- weather, time, injury, shortage, or location danger;
- NPCs acting offscreen;
- consequences of previous turns;
- formal scene requirements or transition constraints.

The world lacks autonomy if:

- every event starts only because the player is present;
- no one acts unless the player asks;
- scheduled pressure never arrives;
- scene conditions never matter;
- world rules are mentioned but never block, cost, or redirect anything.

## 13. Judging AI-Feeling Output

An output has too much AI-feeling if:

- it summarizes themes instead of showing concrete action;
- it says emotions directly without action, dialogue, or sensory support;
- it uses neat parallel lists or symmetrical sentence patterns repeatedly;
- it ends with broad moral conclusions;
- it repeats the player's input with little new state;
- it describes what everyone "understands" instead of what they do;
- it sounds like a review note instead of a scene.

A good Pilot-2 result should:

- show behavior before explaining emotion;
- keep prose concrete;
- let NPC voices differ;
- leave some uncertainty alive;
- make the next player choice clear without narrating it for the player.

## 14. Judging Player Choice Consequences

A player choice has consequence if it changes at least one of:

- character state;
- memory;
- belief;
- relationship;
- thread status or evidence;
- scene requirement;
- transition availability;
- scheduled event handling;
- item/resource status;
- time or location pressure.

No consequence is needed for every tiny gesture, but repeated risk, negotiation, refusal, waiting, injury, technique use, or boundary-setting should eventually leave evidence.

## 15. Why Pilot-2 Does Not Enter Candidate B

Candidate B would move quality audit closer to the formal pre-resolve flow. Pilot-2 does not approve that.

Reasons:

- The audit is still advisory.
- More real story evidence is needed.
- Context-quality findings may describe missing support rather than bad prose.
- Human override and false-positive policy are not designed.
- Candidate B may touch or wrap `resolve-turn`, Validator-adjacent behavior, retry flow, `stateVersion`, `contextHash`, or transaction boundaries.
- The user has not approved those risks.

Pilot-2 should therefore produce evidence first. Any future Candidate B work needs a separate decision brief and explicit approval.

## 16. After Pilot-2

Possible next steps after Pilot-2:

1. `Pilot-2 findings report`
   - Summarize all turns, audit outputs, human scores, false positives, missed issues, and coverage status.

2. `More real audit samples`
   - Continue collecting another short arc if Pilot-2 is too narrow.

3. `Candidate A.3 read-only audit brief`
   - Only if Pilot-2 proves one precise read-only audit gap.

4. `Candidate B decision brief`
   - Only if the user explicitly wants to explore a gate and approves the risks in writing.

5. `No further engineering`
   - If the audit remains useful enough as a manual reviewer aid, keep it read-only and continue playtesting.

Recommended next human action:

```text
我要開始 Pilot-2。

Scenario:
青犁鎮藥市封倉

Turn 1 玩家輸入：
<玩家第一回合行動與台詞>

本回合底線：
<不得替玩家做的重大決定>
```

## 17. Verdict

`PILOT2_READY_FOR_HUMAN_INPUT`

The design is ready to collect a new real story arc. It does not approve Candidate B and does not require code changes.
