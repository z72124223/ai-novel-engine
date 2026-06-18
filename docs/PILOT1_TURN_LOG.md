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
