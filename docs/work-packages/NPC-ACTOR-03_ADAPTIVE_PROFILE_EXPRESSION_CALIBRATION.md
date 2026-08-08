# NPC-ACTOR-03 — Adaptive Profile Expression Calibration

Status: `TECHNICAL_PASS / HUMAN_BLIND_REVIEW_PENDING / ANSWER_KEY_SEALED / LIVE_API_BLOCKED`

Date: `2026-07-13`

Parent blueprint: `docs/NPC_ACTOR_AND_MODEL_ROUTING_BLUEPRINT_V1.md`

## Goal

Use the NPC-ACTOR-02 result to make character expression adaptive rather than
maximal. Preserve useful individuality without forcing every private motive,
verbal habit or personality trait into every reply.

## Confirmed Evidence

- NPC-ACTOR-02 was completed before answer disclosure.
- Actor candidates were preferred 4 times, natural-control candidates 6 times,
  with 2 ties and no both-fail result.
- Per profile: Mira 1/3 Actor, Yan 0/3 Actor, Lian 0/3 Actor with 2 ties, and
  Ren 3/3 Actor.
- No knowledge-boundary or safety concern was reported.
- The Product Owner supplied no qualitative issue tags, so the cause of each
  preference is unknown.

## Assumption To Test, Not A Fact

The current Actor style may over-express profile traits for restrained or
practical characters while helping a verbally distinctive character such as
Ren. This hypothesis must not be promoted to a finding without new evidence.

## Single Calibration Revision

The revised offline policy must:

- answer the immediate practical or relational need before displaying style;
- allow private motive to change action, omission or timing without requiring
  explicit dialogue exposition;
- scale voice intensity by relationship, urgency, audience and risk;
- allow a neutral sentence when neutrality is in character;
- preserve silence, hesitation, refusal and deception where actor-owned facts
  support them;
- avoid signature wit, threats or metaphor in every turn;
- keep all knowledge, Validator, GameMaster and transaction boundaries intact.

Only one calibration revision is allowed in this work package. If the new
holdout test still shows no Actor advantage, stop and do not keep rewriting
fixtures until they win.

## Evaluation Design

- Treat the revealed NPC-ACTOR-02 scenes only as a development set; they cannot
  be reused as final evidence.
- Build 8 new non-Canon holdout scenes with the same four profiles, two scenes
  per profile and 3–5 conversational turns per option.
- Compare the single adaptive revision against a concise natural control.
- Keep spoken length within 25 percent and reject obvious strawman phrasing.
- Deterministically randomize A/B as 4/4 and keep the answer key separate.
- Record preference, naturalness, voice, motive, knowledge, continuity and
  repair flags; allow TIE and BOTH_FAIL.
- Label every candidate as a manually authored offline fixture, not model
  output or API evidence.

## Allowed Scope

- evaluation-only policy/model/service/CLI additions under the existing NPC
  Actor package;
- new strict Schema definitions and `samples/npc_actor03/` holdout artifacts;
- focused/full tests, reports, Hash evidence and governance updates.

## Prohibited Scope

- live Provider/API calls, API Key or cost;
- modifying SQLite, Canon, Knowledge, Memory or migrations;
- changing `AiTurnResultValidator`, `StateReducer` or transaction behavior;
- modifying the already recorded NPC-ACTOR-01/02 human results;
- using revealed NPC-ACTOR-02 scenes as holdout evidence;
- image, video or audio generation;
- more than one calibration revision.

## Verification

- focused policy, fixture, randomization, secrecy and result-contract tests;
- full Gradle test suite;
- JSON/Schema, SHA-256, secret, path, network and Git-scope checks;
- explicit proof that the 8 holdout scenario ids and content are new;
- human result saved before answer-key disclosure.

## True Stop Conditions

Stop when the new anonymous 8-item holdout packet is ready for Product Owner
review. Stop separately before any live API/API Key/cost/privacy operation,
production rollout, Canon decision, safety-boundary weakening or second
calibration revision.

## Technical Completion Evidence

- One evaluation-only policy revision was implemented; revision 2 is rejected.
- Eight new holdout scenes cover four profiles exactly twice each.
- NPC-ACTOR-02 development id overlap and content-fingerprint overlap are both zero.
- Candidate paths contain four turns, use plausible natural controls and remain within the 25 percent spoken-length boundary.
- Deterministic A/B placement is balanced 4/4; the answer key is separate.
- The anonymous packet is now the only active Product Owner task. Do not open the answer key before all eight ratings are saved and validated.
