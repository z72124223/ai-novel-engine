# NPC-ACTOR-02 — Hard Blind Multi-Turn Evaluation

Status: `COMPLETE / NO_ACTOR_ADVANTAGE_OBSERVED / LIVE_API_BLOCKED`

Date: `2026-07-13`

Parent blueprint: `docs/NPC_ACTOR_AND_MODEL_ROUTING_BLUEPRINT_V1.md`

## Goal

Replace the easy NPC-ACTOR-01 contrast with a discriminating human evaluation.
Both options must sound plausible; the test must measure stable character voice,
private motive, knowledge boundaries and multi-turn behavior rather than merely
detecting obvious customer-service or rule-recitation phrasing.

## Evidence That Changed The Plan

The Product Owner completed NPC-ACTOR-01 before answer-key disclosure and chose
the Actor option 20/20. The same reviewer immediately identified that the
baseline repeatedly exposed itself through conspicuous AI explanation. The
result is valid evidence for anti-exposition behavior but weak evidence for
human-like acting. This limitation is recorded in
`samples/npc_actor01/npc_actor01_human_blind_review_result.json`.

## Evaluation Design

- Use at least 12 previously unseen, `NON_CANON_EVALUATION` scenes.
- Each scene contains 3–5 conversational turns, not a single isolated reply.
- Cover at least four different NPC profiles and relationship pressures.
- Include ordinary conversation, ambiguous intent, private motive, hesitation,
  deception, refusal, silence, emotional repair and conflicting instructions.
- Both candidates must be concise, idiomatic and plausible. Neither side may use
  deliberately bad AI exposition as a strawman.
- Candidate spoken-length difference should normally remain within 25%; any
  exception needs an explicit scene reason.
- Randomize A/B order with a recorded deterministic seed and keep the answer key
  separate until every rating is complete.
- Do not reveal architecture names, source labels, prompt style or expected
  winner in the review packet.
- Collect per-scene preference plus naturalness, voice consistency, motive
  coherence, knowledge-boundary, continuity and repair flags.
- Allow `TIE` and `BOTH_FAIL`; never force a preference.

## Candidate Boundary

The first implementation remains offline. It may use committed File/Fake
fixtures and the existing visibility-safe request contract. A manually authored
candidate is evaluation evidence only and must be labeled as such; it cannot be
described as a model benchmark. No live provider, API Key, paid call or privacy
export is authorized.

Both candidate paths must receive the same visible scene facts. The Actor path
may additionally use only actor-owned profile, memory, belief, goal,
relationship and authorized-capability evidence already allowed by
`NpcActorRequestBuilder`. Neither side may receive author secrets, another
character's private memory, director pressure or hidden answer labels.

## Pass And Claim Rules

The offline hard-blind package may pass only when:

- every packet, answer-key and result reference is complete and hash-bound;
- no knowledge, Canon, transaction or Validator violation occurs;
- no option contains an obvious label-revealing strawman pattern;
- Product Owner preference is measurable across the unseen multi-turn scenes;
- TIE/BOTH_FAIL and qualitative objections remain visible in the report;
- all focused and full tests pass.

One Product Owner review may support a project-direction decision. A broad claim
that NPC dialogue is generally human-like requires at least one additional
independent human rater or another explicitly approved evidence source.

Passing this package still does not authorize live API use, API Key handling,
paid models, production rollout, direct SQLite writes or Canon promotion.

## Implementation Scope

Allowed:

- provider-neutral evaluation models and strict JSON Schema;
- deterministic A/B randomization and answer-key separation;
- multi-turn offline fixtures under a new `samples/npc_actor02/` directory;
- focused tests, reports, hash manifests and governance updates;
- minimal CLI support needed to rebuild and validate the evaluation package.

Not allowed:

- live network or Provider calls;
- OpenAI SDK/API integration;
- direct SQLite, Knowledge, Memory or Canon writes;
- changing `AiTurnResultValidator`, `StateReducer` or transaction boundaries;
- image, video or audio generation;
- using NPC-ACTOR-01's 20 scenarios as the hard-test set;
- fabricating any human rating.

## Verification

- focused contract, randomization, secrecy, boundary and evaluation tests;
- full Gradle test suite;
- JSON parsing and strict Schema validation;
- deterministic seed and answer-key mapping verification;
- spoken-length and strawman-pattern audit;
- SHA-256 manifest verification;
- secret, absolute-path, network and Git-scope scans.

## True Stop Conditions

Stop for Product Owner input only when the anonymous hard-blind packet is ready
for real rating. Stop separately before any live API/API Key/cost/privacy step,
production rollout, Canon decision or safety-boundary weakening.
