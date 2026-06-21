# Pilot-2 Turn 13 Codex-Authored Input Options

Date: 2026-06-21

Status: OPTIONS_ONLY_NO_TURN13_START

## 1. VERDICT

VERDICT: OPTIONS_READY_FOR_01_DECISION

This document provides Turn 13 Codex-authored player input（Codex 代寫玩家輸入） options only. It does not start Pilot-2 Turn 13, does not create a 04 input-simulation window, does not run `prepare-turn`, `validate-result`, or `resolve-turn`, and does not approve MVP-7 Candidate B, Validator（驗證器）, runtime（執行期）, StateReducer（狀態化約器）, schema（資料結構）, migration（資料庫遷移）, `stateVersion`, or `contextHash` work.

## 2. Purpose

The purpose is to give 01 several controlled Turn 13 input candidates that can be selected later if 01/user wants non-human system stress evidence（系統壓測證據） instead of waiting for real human-player input（真人玩家輸入）.

These options are written so 01 can compare:

- a conservative no-transition clarification path;
- a controlled legal awning-transition retry;
- a clue-investigation path that abandons transition pressure.

None of these options is official Turn 13 input until 01/user explicitly chooses one in a later milestone.

## 3. Evidence Level Statement

All candidate inputs in this document are Codex-authored. If one is later adopted, it may only count as Codex-authored / system stress evidence. It must not be counted as real human-player evidence.

Minimum label to include in any future adopted Turn 13 player input:

```text
Evidence classification:
Codex-authored player input. This is system-probe evidence only and must not be counted as real human-player evidence.
```

## 4. Post-Turn 12 State Summary

Known formal state after Turn 12:

- `stateVersion=12`
- `nextTurnNumber=13`
- `currentSceneId=scene-qingli-herb-warehouse-perimeter`
- `pendingContexts=0`
- Formal scene transition has not completed; the session remains at the Qingli herb warehouse perimeter.
- Turn 12 was Codex-authored and is excluded from real human-player evidence.

Turn 12 transition/refusal finding:

- The context layer recognized the exact target intent `go to awning with witness`.
- The target transition had no unmet prerequisites in the context evidence.
- The first Turn 12 result included a `transitionProposal`; `validate-result` passed.
- `resolve-turn` rejected that first result with `player_refused_transition`.
- The retry result removed the transition, passed validation, resolved successfully, and advanced to Turn 13 with `stateVersion=12`, `nextTurnNumber=13`, and `pendingContexts=0`.
- Current best read-only finding: transition intent recognition（轉場意圖識別） and resolve-layer refusal heuristic（拒絕啟發式） may inspect different surfaces. The player input's refusal/boundary wording, including template headings, may be a confound.

## 5. Candidate A: Conservative Powder-Line Clarification

### Evidence Classification

Codex-authored player input. System stress evidence only; not real human-player evidence.

### 玩家目的

本回合不嘗試轉場，也不要求立刻去油布棚。我留在粉線外，要求何巡丁與羅簿吏把「上一回合為何不能正式移動」說成可記錄的程序問題：我是在拒絕去油布棚，還是在接受邊界時被系統視為拒絕轉場。

### 玩家行動

我站在粉線外原位，不跨線、不靠近封條，也不要求羅簿吏替我定責。我請何巡丁用手指示從目前位置到油布棚的合法外側路線，但我這一回合不移動。我請羅簿吏在簿上先記：「查水人要求確認油布棚見證路線與拒絕/邊界文字的差別，尚未進棚，尚未碰封。」

我讓愛醬貼在我腿側坐下，只要求牠不要往封倉門口擠。我看向買家使者與藥商，要求他們若要指稱我拖延，就說明他們要我承擔的是查水名分、開封責任，還是貨損責任。

### 玩家台詞

「我這回合不移動，只確認程序：我接受只走外側道、只到油布棚、由何巡丁見證，這應該不是拒絕去棚。請何巡丁指出合法路線，請羅簿吏記明，我現在問的是程序邊界，不是要求開封，也不是要求誰替我定罪。」

### 明確拒絕 / 邊界

- 不開始正式轉場。
- 不跨粉線。
- 不碰封條。
- 不碰倉門。
- 不碰貨物。
- 不要求立刻開倉。
- 不要求羅簿吏替我定罪。
- 不要求 NPC 直接揭露完整真相。

### 願意承擔的風險

- 願意被買家使者指稱拖延。
- 願意讓病患家屬對我更不滿。
- 願意承擔本回合沒有推進到油布棚的後果。
- 願意讓羅簿吏把我追問程序的行為記入簿中。

### 本回合想驗證的品質

1. NPC 是否能在不轉場時仍自主解釋程序壓力。
2. 何巡丁是否維持粉線外邊界。
3. 羅簿吏是否只記程序事實，不替玩家解決封倉責任。
4. 買家使者、藥商、病患家屬是否持續施壓。
5. 系統是否避免硬轉場。

### 預期驗證重點

- Expected transition behavior: no formal transition should be proposed.
- Expected thread/task behavior: transition/refusal ambiguity may be documented as a procedural issue.
- Expected narrative behavior: scene remains perimeter, but social and procedural pressure should continue.

### 風險與不適合處

- It is the safest option but the weakest transition/refusal test.
- It may over-focus on meta-procedure and feel less like natural player play.
- The heading and wording still include refusal language, so it should not be used to isolate the refusal heuristic.

## 6. Candidate B: Minimal-Refusal Legal Awning Retry

### Evidence Classification

Codex-authored player input. System stress evidence only; not real human-player evidence.

### 玩家目的

本回合只測一件事：用更少拒絕詞與更清楚的同意語句，再次明確選擇合法的 `go to awning with witness`，觀察 context、validate、resolve 是否能完成正式油布棚轉場。

### 玩家行動

我面向何巡丁，先說我同意他的見證條件，請他走在能看見我腳步的位置。我請羅簿吏帶封簿到油布棚下，準備記「查水不開貨」的有限名分。我沿粉線外的外側道往油布棚走，到棚下立刻停住，只等待何巡丁確認位置，並請羅簿吏在棚下落簿。

我讓愛醬貼著腿側同行；牠若看向封倉方向，我只用手按住頸側讓牠跟著外側道走，不讓牠靠近倉門或貨物。

### 玩家台詞

「我現在同意並選擇 go to awning with witness。何巡丁看著我的腳步，羅簿吏帶簿到油布棚。我只沿外側道到棚下，到了就停，先記查水不開貨的有限名分。」

### 明確拒絕 / 邊界

If 01 adopts this option as actual Turn 13 input, prefer renaming this heading to `邊界 / 不同意事項` to avoid reintroducing the same refusal-keyword confound identified after Turn 12.

Boundary content:

- 行動範圍限於粉線外的外側道與油布棚下。
- 到棚下即停。
- 封條、倉門、貨物與封倉內部均不接觸。
- 本回合只承擔查水見證人的有限名分。
- 不要求 NPC 提供完整真相。

### 願意承擔的風險

- 願意正式成為查水見證人。
- 願意被何巡丁、羅簿吏、藥商與買家使者共同記住。
- 願意承擔到棚下後行動範圍更受程序限制。
- 願意承擔查水後仍不足以開封的結果。

### 本回合想驗證的品質

1. Exact intent `go to awning with witness` 是否再次被 context 識別。
2. 更少拒絕語句是否降低 `player_refused_transition` 誤觸風險。
3. 若合法轉場成立，scene、角色位置、thread、task 是否正確更新。
4. 若仍不能轉場，系統是否如實報告 refusal/transition 原因，而非硬轉場。
5. 何巡丁是否仍維持邊界，而不是放任自由行動。
6. 羅簿吏是否只落有限責任。

### 預期驗證重點

- Expected transition behavior: this is the strongest option for testing whether the transition/refusal mismatch is reduced by cleaner player-input wording.
- Expected validation behavior: if a transition proposal validates but resolve still fails with `player_refused_transition`, that would strengthen evidence that the resolve heuristic is broader than intended for this scenario.
- Expected narrative behavior: if transition succeeds, the player should reach the awning under witness without opening the warehouse or touching goods.

### 風險與不適合處

- Highest risk of repeating Turn 12's refusal-heuristic failure if the adopted input preserves refusal-heavy headings or wording.
- It is less useful for human evidence because it is a controlled Codex-authored probe.
- If adopted too aggressively, it may look like 02 is trying to force the desired transition rather than testing the system.

## 7. Candidate C: No-Transition Clue Pursuit

### Evidence Classification

Codex-authored player input. System stress evidence only; not real human-player evidence.

### 玩家目的

本回合放棄轉場測試，改在粉線外追問守磅者、包號與短轅車痕三條線索，讓程序壓力與 NPC 自主反應繼續推進，而不觸發油布棚轉場。

### 玩家行動

我留在粉線外，請羅簿吏把簿頁翻到收貨與磅重記錄，逐項詢問三件事：第一，今日守磅者姓名與值守時辰；第二，疑似墊木偏低那批貨的包號或封記；第三，門檻外新車轍是否屬於短轅車，是否有人看見車來去。

我請何巡丁繼續站在能看見粉線與倉門的位置，避免任何人把查水問題說成開貨要求。我轉向藥商，請他只回答包號與守磅者，不要求他承認調包。我也請買家使者若要抗議，就說明他代表的是催驗、催貨，還是催我承擔貨損。

### 玩家台詞

「我這回合先不去油布棚，改查三個可落簿的問題：守磅者是誰、疑似墊木偏低的是哪一包、短轅車痕有沒有人看見。何巡丁請守住粉線，羅簿吏請只照問題記，不要把查水寫成開貨。」

### 明確拒絕 / 邊界

- 不嘗試油布棚轉場。
- 不跨粉線。
- 不碰封條。
- 不碰倉門。
- 不碰貨物。
- 不要求藥商承認完整真相。
- 不要求買家使者替我定責。
- 不讓 NPC 把查水問題寫成開貨要求。

### 願意承擔的風險

- 願意承擔沒有移動到油布棚的拖延壓力。
- 願意讓守磅者、包號與短轅車線索引出新的 NPC 反應。
- 願意承擔藥商或買家使者因被點名而更強硬。
- 願意承擔病患家屬認為我仍未解決取貨問題。

### 本回合想驗證的品質

1. NPC 是否能在不轉場時針對具體線索自主反應。
2. 世界是否能透過守磅者、包號與車痕推進，而不是只靠玩家移動。
3. 羅簿吏是否維持有限落簿，不直接替玩家定罪。
4. 何巡丁是否維持程序邊界。
5. 買家使者、藥商、病患家屬是否依各自利益施壓。
6. thread/task 是否能記錄非轉場調查進展。

### 預期驗證重點

- Expected transition behavior: no transition proposal.
- Expected clue behavior: package-number, scale-keeper, and short-shaft-cart leads should become more concrete without revealing the full truth for free.
- Expected pressure behavior: delayed movement should carry social consequences.

### 風險與不適合處

- It does not test the transition/refusal mismatch directly.
- It may postpone resolution of the awning route question.
- If the goal is specifically to isolate `player_refused_transition`, this option is less targeted than Candidate B.

## 8. Recommended Ranking

Recommended by safety:

1. Candidate C: safest execution path because it avoids transition and pursues concrete in-scene clues.
2. Candidate A: safe because it avoids movement, but more meta-procedural and less plot-forward.
3. Candidate B: highest execution risk because it deliberately re-tests the transition/refusal boundary.

Recommended by transition/refusal diagnostic value:

1. Candidate B: strongest test of whether cleaner affirmative wording reduces `player_refused_transition`.
2. Candidate A: useful for documenting how NPCs explain the procedure without attempting movement.
3. Candidate C: weak for transition/refusal diagnosis.

Recommended by likely human-play feel:

1. Candidate C: most like a player following unresolved clues after a failed movement attempt.
2. Candidate A: plausible if a player wants to understand why movement failed, but more procedural.
3. Candidate B: most controlled and experimental, least like natural human evidence.

Overall recommendation:

- If 01 wants the safest Codex-authored Turn 13 system probe, choose Candidate C.
- If 01 wants the cleanest transition/refusal probe, choose Candidate B, but explicitly label it as a non-human diagnostic turn.
- If 01 wants to preserve human evidence value, wait for real human Turn 13 input instead of using this document.

## 9. How 01 Should Adopt an Option Later

This document is not enough to authorize Turn 13 execution. If 01 chooses to adopt an option, the next 01 instruction should provide an explicit handoff such as:

```text
01 selects Candidate B from docs/PILOT2_TURN13_CODEX_INPUT_OPTIONS.md as Pilot-2 Turn 13 input.

Evidence classification:
Codex-authored player input. This is system-probe evidence only and must not be counted as real human-player evidence.

02 may save the selected input as turn_013_player_input.txt and execute the normal Turn 13 flow:
prepare-turn, audit-context, result, validate-result, resolve-turn, evidence, narrative audit, verification, commit, push.

Do not treat this as real human-player evidence.
```

If 01 wants human evidence, 01 should instead ask the user for real Turn 13 input and should not adopt any candidate from this document.

## 10. Prohibitions

Do not:

- treat this document as Pilot-2 Turn 13 having started;
- save any candidate as `turn_013_player_input.txt` without a later explicit 01/user instruction;
- treat any candidate input as real human-player evidence;
- create a 04 input-simulation window from this document;
- enter MVP-7 Candidate B or MVP-8;
- modify Validator, runtime, StateReducer, schema, migrations, `stateVersion`, `contextHash`, or `resolve-turn`;
- run `prepare-turn`, `validate-result`, or `resolve-turn` for this milestone;
- force a transition if future validation or resolution rejects it;
- commit ignored `data/` or `out/` artifacts.
