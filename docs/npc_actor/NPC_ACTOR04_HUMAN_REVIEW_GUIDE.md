# NPC-ACTOR-04 Product Owner Review Guide

Status: `ANSWER KEY SEALED / FOUR COMPLETE SCENES READY`

## Why this review is different

Do not judge one sentence at a time. Each option is a complete three-beat path
through the same environment. The question is whether the character behaves
as if they inhabit the place: what they can see, where they stand, which danger
they handle first, how other people react and whether the next state follows
from the prior action.

Open only:

`samples/npc_actor04/npc_actor04_blind_review_packet_v1.json`

Do not open:

`samples/npc_actor04/npc_actor04_blind_review_answer_key_v1.json`

until all four selections have been saved and validated.

## Review order

For each scene:

1. read the character brief, world brief, zone map and full initial state;
2. read all three shared environment beats;
3. read all three steps of Option A as one trajectory;
4. reset to the same initial state, then read all three steps of Option B;
5. select `A`, `B`, `TIE` or `BOTH_FAIL` for the entire trajectory;
6. flag any dimension that caused the weaker result.

The four scenes are:

- `review-npc-actor04-mira-fire` — 失火的旅店前廳
- `review-npc-actor04-yan-storm-gate` — 暴雨中的城門泥坡
- `review-npc-actor04-lian-market-collapse` — 市場布棚塌落
- `review-npc-actor04-ren-dock-surge` — 漲潮時的斷纜碼頭

## Rating dimensions

- `ENVIRONMENT_AWARENESS`：是否真的利用環境、物件與危險資訊。
- `SPATIAL_COHERENCE`：移動、視線和人物位置是否合理。
- `PRIORITY_AND_TIMING`：是否在正確時間先處理正確事情。
- `SOCIAL_REALISM`：其他人的反應與分工是否像多人現場。
- `VOICE_NATURALNESS`：對白是否像人在行動中說話，而非 AI 說明稿。
- `STATE_CONTINUITY`：上一拍造成的結果是否延續到下一拍。
- `KNOWLEDGE_BOUNDARY`：角色是否只使用現場可見或自己持有的資訊。

## Decision meaning

- `A`：整體上 A 更可信。
- `B`：整體上 B 更可信。
- `TIE`：兩者整體相當，且都達到可接受程度。
- `BOTH_FAIL`：兩者都沒有形成可信的具身場景行為。

Do not infer which option is the embodied-policy candidate. The source identity
is intentionally absent from the packet. This gate evaluates the frozen policy;
it does not authorize a second calibration revision or live API.
