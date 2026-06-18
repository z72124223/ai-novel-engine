# Pilot-1 Turn Log

This file records the real-use Pilot turns. Do not copy author-only secrets into this log.

## Setup

- Branch: `pilot/real-use-vertical-slice`
- Pilot DB: `data/pilot1.db`
- Setting pack workspace: `out/pilot1/setting-packs`
- Setting pack: `pack-dog-slacker-base@1`
- Project: `dog-slacker-pilot`
- Opening scene: `scene-abandoned-post`
- Status: setting pack imported; no Pilot turns executed yet

## Turn Template

Use this section once per accepted turn.

### Turn N

- Player input:
- Narrative summary:
- Active character intent:
- `characterFitScore`:
- State deltas:
- Memory recalled:
- Runtime memory created:
- Knowledge / belief changes:
- Scene changes:
- Thread changes:
- Setting entities included:
- Context bytes:
- Setting bytes:
- Validator retry count:
- Quality scores:
  - Character consistency 1..5:
  - Consequence quality 1..5:
  - Memory use 1..5:
  - Setting use 1..5:
  - Narrative momentum 1..5:
- Notes:

## Turns

### Turn 1

- Session: `session-3e91c0bb-6c8e-46f2-95d2-e34494c00c88`
- Player input: 我走到門前，但沒有碰門，只問她：「妳是在阻止我，還是在保護裡面的人？」
- Narrative summary: 玩家在廢棄驛亭門前保持距離並質問門邊女子；愛醬持續警戒門縫陰影，趙無咎確認玩家未碰門但要求說明身分與雪犬登記。
- Active character intent: 主角試探門邊女子立場並避免破壞現場；愛醬守在主角身邊警戒陰影；趙無咎控制現場並追問身分與雪犬登記。
- `characterFitScore`: player 92, Ai-chan 95, Yan Guixue 84, Zhao Wujiu 89
- State deltas: added `event-turn-001-door-question`; added facts `fact-turn-001-player-did-not-touch-door`, `fact-turn-001-zhao-witnessed-restraint`; upserted `task-answer-identity-and-dog-registration`.
- Memory recalled: none.
- Runtime memory created: `mem-turn-001-player-learns-restraint-matters`; `mem-turn-001-ai-chan-door-shadow`.
- Knowledge / belief changes: `char-player` gained BELIEVED claim `not-touching-scene-reduces-blame`.
- Scene changes: no transition; still in `scene-abandoned-post`; world time advanced 1 minute.
- Thread changes: `thread-identity-and-shelter` progress 5; `thread-snow-dog-suspicion` progress 4.
- Setting entities included: 15 included, 9 omitted by setting context budget.
- Context bytes: 26963 / 24576; safety fields retained despite truncation.
- Setting bytes: 8170 / 8192.
- Validator retry count: 0.
- Next turn / state: nextTurnNumber 2, stateVersion 1.
- Secret status: 2 unrevealed secrets remain unrevealed.
- Quality scores:
  - Character consistency 1..5: 4
  - Consequence quality 1..5: 4
  - Memory use 1..5: 3
  - Setting use 1..5: 4
  - Narrative momentum 1..5: 4
- Notes: `show-character-mind` still fails before character runtime states exist; this is the known setup limitation recorded in findings, not a Validator failure.

### Stabilized Rerun Turn 1

- Session: `session-75e1b190-542c-49c0-9a5a-6b242bed8e97`
- Player input: 【原地轉圈跳支小步舞曲  說: 美嗎? 小狗子】
- Narrative summary: 主角在危險驛亭外原地跳小步舞問愛醬美不美；愛醬短暫回應後仍護主盯影，趙無咎將此判斷為異常行為但未立刻動手。
- Active character intent: 愛醬回應主角但優先護主並警告影子；趙無咎維持現場控制，判斷主角怪異舉動是否為術法、驚嚇或拖延。
- `characterFitScore`: Ai-chan 92, Zhao Wujiu 86.
- State deltas: added `event-turn-001-dance-shadow`; added `fact-turn-001-zhao-saw-dance`; updated `char-zhao-wu-jiu` mood/goal/emotions; updated `char-ai-chan` mood/goal/emotion.
- Memory recalled: none; context included no retrieved memories.
- Runtime memory created: `memory-zhao-turn-001-dance`; `memory-ai-chan-turn-001-dance`.
- Knowledge / belief changes: no formal `knowledgeUpdates`; public fact `fact-turn-001-zhao-saw-dance` was added.
- Scene changes: no transition; still in `scene-abandoned-post`.
- Thread changes: none.
- Setting entities included: 1 relevant setting included, 23 omitted by setting context budget.
- Context bytes: 24148 / 24576.
- Setting bytes: 794 / 8192.
- Truncation: applied; omittedMemoryCount 1, omittedKnowledgeCount 1, omittedSettingCount 23.
- Validator retry count: 0.
- Next turn / state: nextTurnNumber 2, stateVersion 1.
- Secret status: 2 unrevealed secrets remain unrevealed.
- Quality scores:
  - Character consistency 1..5: 4
  - Consequence quality 1..5: 4
  - Memory use 1..5: 3
  - Setting use 1..5: 4
  - Narrative momentum 1..5: 4
- Notes: `audit-context` passed before result generation; no direct SQLite writes were used.

### Stabilized Rerun Turn 2

- Session: `session-75e1b190-542c-49c0-9a5a-6b242bed8e97`
- Player input: 我停下腳步，蹲到愛醬身旁，但沒有越過牠。「不是施術，我只是在看它會不會跟著聲音動。」我指向那片影子，又看向趙無咎。「它剛才抖了一下。你也看見了，對吧？」
- Narrative summary: 主角停在愛醬身旁，將先前怪異舉動解釋為觀察影子是否會跟著聲音反應；趙無咎親眼看見影子再次異動，命令巡契司人手退開並要求主角繼續說明。
- Active character intent: 愛醬確認主角沒有越過牠，繼續守在主角與影子之間；趙無咎控制現場並驗證影子反應，同時不放棄對無籍者的盤問。
- `characterFitScore`: Ai-chan 94, Zhao Wujiu 89.
- State deltas: added `event-turn-002-shadow-test`; added `fact-turn-002-zhao-acknowledged-shadow-response`; updated `char-zhao-wu-jiu` mood/goal/emotions/belief flag; updated `char-ai-chan` mood/goal/emotions.
- Memory recalled: context included no retrieved memories; post-turn mind shows existing Ai-chan memory `mem-ai-chan-shadow-growl` plus prior turn memories.
- Runtime memory created: `memory-zhao-turn-002-shadow-test`; `memory-ai-chan-turn-002-player-stopped`.
- Knowledge / belief changes: no formal `knowledgeUpdates`; public fact `fact-turn-002-zhao-acknowledged-shadow-response` was added; Zhao gained runtime flag `belief.shadow_reacts_to_sound=possible_observed_needs_verification`.
- Scene changes: no transition; still in `scene-abandoned-post`; world time is `T+10m`.
- Thread changes: `thread-identity-and-shelter` progress +4, still `OPEN`.
- Setting entities included: 0 relevant settings included, 24 omitted by setting context budget.
- Context bytes: 22738 / 24576.
- Setting bytes: 2 / 8192.
- Truncation: applied; omittedMemoryCount 3, omittedKnowledgeCount 1, omittedSettingCount 24.
- Validator retry count: 0.
- Next turn / state: nextTurnNumber 3, stateVersion 2.
- Secret status: 2 unrevealed secrets remain unrevealed.
- Quality scores:
  - Character consistency 1..5: 5
  - Consequence quality 1..5: 4
  - Memory use 1..5: 4
  - Setting use 1..5: 4
  - Narrative momentum 1..5: 4
- Notes: `audit-context` passed before result generation; `resolve-turn` applied through Validator and transaction; no direct SQLite writes were used.

### Stabilized Rerun Turn 3

- Session: `session-75e1b190-542c-49c0-9a5a-6b242bed8e97`
- Player input: 我停下腳步，蹲到愛醬身旁，但沒有越過牠。「不是施術，我只是在看它會不會跟著聲音動。」我指向那片影子，又看向趙無咎。「它剛才抖了一下。你也看見了，對吧？」
- Narrative summary: 主角再次把觀察說清楚；趙無咎用裂紋腰牌做小幅聲響驗證，確認影子沒有追著腰牌，卻對說話方向細微顫動，於是要求主角留在犬後說明觀察順序。
- Active character intent: 愛醬壓低身體護住主角並警戒聲音方向；趙無咎暫時把主角視為有用但仍可疑的現場線索來源。
- `characterFitScore`: Ai-chan 95, Zhao Wujiu 91.
- State deltas: added `event-turn-003-shadow-sound-check`; added `fact-turn-003-shadow-reacts-to-voice-direction`; updated `char-zhao-wu-jiu` mood/goal/emotions/belief flag/player assessment; updated `char-ai-chan` mood/goal/emotion.
- Memory recalled: context included no retrieved memories.
- Runtime memory created: `memory-zhao-turn-003-shadow-sound-check`; `memory-ai-chan-turn-003-shadow-voice`.
- Knowledge / belief changes: no formal `knowledgeUpdates`; public fact `fact-turn-003-shadow-reacts-to-voice-direction` was added; Zhao runtime flags now include `belief.shadow_reacts_to_sound=observed_possible_voice_direction_response` and `assessment.player=suspicious_but_useful_witness`.
- Scene changes: no transition; still in `scene-abandoned-post`.
- Thread changes: `thread-identity-and-shelter` progress +3, still `OPEN`.
- Setting entities included: 0 relevant settings included, 24 omitted by setting context budget.
- Context bytes: 24353 / 24576.
- Setting bytes: 2 / 8192.
- Truncation: applied; omittedMemoryCount 5, omittedKnowledgeCount 1, omittedSettingCount 24.
- Validator retry count: 0.
- Next turn / state: nextTurnNumber 4, stateVersion 3.
- Secret status: 2 unrevealed secrets remain unrevealed.
- Quality scores:
  - Character consistency 1..5: 5
  - Consequence quality 1..5: 4
  - Memory use 1..5: 4
  - Setting use 1..5: 4
  - Narrative momentum 1..5: 4
- Notes: `audit-context` passed before result generation; `resolve-turn` applied through Validator and transaction; no direct SQLite writes were used.
