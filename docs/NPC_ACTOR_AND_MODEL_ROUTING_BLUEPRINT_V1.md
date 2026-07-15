# NPC Actor And Model Routing Blueprint V1

Status: `NPC_ACTOR_09C_SAFE_REJECTION / QWEN3_8B_RETIRED_FROM_ACTIVE_ROUTE / OPENAI_RESPONSES_ROUTE_SELECTED / LIVE_API_NOT_YET_CALLED`

Date: `2026-07-15`

## 1. Decision

The final runtime should use a provider-neutral NPC Actor interface with an
optional GPT API implementation. ChatGPT is the manual character audition and
prompt-development surface. Codex builds, tests and reviews the system; Codex
is not the production NPC actor.

API access does not make a model intrinsically more natural. It makes model,
context, schema, state, latency, cost and retries controllable by the engine.
Naturalness must be measured against the same scenario set used in manual
ChatGPT auditions.

### 2026-07-15 Route Override

The Product Owner removed Qwen3-8B from the future NPC Actor generation route after NPC-ACTOR-09C. Its local
installation, historical tests and Git evidence stay preserved, but it is no longer a production candidate or
fallback. Do not delete those files and do not rerun the model to tune until it passes.

The selected replacement is the OpenAI Responses API:

- default actor model: `gpt-5.6-terra`, reasoning `medium`;
- controlled quality escalation: `gpt-5.6-sol`, reasoning `high`;
- no default `max`, Pro mode or per-turn Sol usage;
- no `gpt-5.6-luna` for major-character acting without a separate quality evaluation;
- no paid request until API Key, budget, privacy packet and one non-Canon live Pilot are separately approved.

## 2. Mandatory Route Order

```text
RETRIEVAL-00 authority / rebuild / retention
→ RETRIEVAL-01 scoped local retrieval benchmark
→ NPC-ACTOR-01 single NPC offline/Fake pilot
→ NPC-ACTOR-02 hard blind multi-turn human review
→ NPC-ACTOR-03 single adaptive profile-expression calibration
→ NPC-ACTOR-04～09C physical, safety, formal-candidate and rejection evidence
→ NPC-ACTOR-10 cloud API route pivot
→ NPC-ACTOR-10A offline OpenAI Responses adapter foundation with Fake HTTP
→ explicit API Key / cost / privacy approval
→ NPC-ACTOR-10B one non-Canon live cloud Pilot
```

No stage may skip the preceding acceptance gate.

## 3. Responsibility Split

| Component | Responsibility | Must not do |
| --- | --- | --- |
| Git + rebuild manifest | Portable formal sources, versions and approvals | Act as runtime state |
| SQLite | Formal structured state, memories, knowledge, relationships and traces | Treat retrieval score as truth |
| Retriever | Select visibility-safe evidence for one NPC and moment | Broaden character knowledge |
| NPC Decision Actor | Propose goal, speech act, action and withheld information | Write formal state |
| Dialogue Realizer | Produce one short performable utterance/nonverbal beat | Explain the whole world |
| Validator/GameMaster | Check knowledge, location, capability, cost and continuity | Invent missing evidence |
| StateReducer | Apply accepted deltas transactionally | Accept unvalidated model output |
| Codex | Implement, test, evaluate and maintain | Serve as the production NPC runtime |
| ChatGPT | Manual audition and prompt comparison | Become the source of formal memory |

## 4. NPC Context Boundary

An actor packet may contain only the current NPC's:

- stable identity and voice profile;
- current physical/emotional state;
- current location and perceived sensory facts;
- owned memories and knowledge claims returned by scoped retrieval;
- current goals, debts, fears, false beliefs and relationships;
- relevant recent dialogue and actions;
- capabilities, costs and local constraints;
- information the NPC wants to reveal, withhold, distort or avoid.

It must not contain director goals, future reveals, other characters' private
memory, author-only secrets or unrestricted full-world context.

## 5. Structured Proposal

The model returns a proposal, not hidden chain-of-thought and not a database
mutation. Minimum future fields:

```text
actorId
targetIds
speechAct
immediateGoal
beliefClaimIds
memoryIds
emotionShown
emotionHidden
withheldInformationIds
actionIntent
visibleAction
utterance
nonverbalAction
interruptible
silenceAllowed
capabilityUseChecks
confidence
```

Private reasoning text is not persisted. A short evidence-bound decision
summary may be stored only as ids, selected action, rejected action labels and
validation findings.

## 6. Natural Dialogue Rules

The actor is allowed to:

- remain silent;
- answer only part of a question;
- interrupt, hesitate, restart or change wording;
- misunderstand from incomplete information;
- lie according to motive and belief;
- use an action instead of exposition;
- vary language by relationship, urgency, fatigue and danger.

Story information needing eventual delivery does not force the current NPC to
say it now. The final line should not restate structured intent, emotion or
world facts unless the character has a reason to do so.

## 7. Model And Reasoning Router

Model and reasoning effort are separate controls. Initial policy candidates:

| Workload | Model candidate | Reasoning candidate |
| --- | --- | --- |
| Important ordinary NPC decision/dialogue | `gpt-5.6-terra` | `medium` default |
| Major-character or high-stakes scene | `gpt-5.6-sol` | `high` only after measured escalation need |
| Background deterministic action | deterministic rule first | none |
| Background generated dialogue | `gpt-5.6-terra`; evaluate Luna separately | `low` / `medium` |
| Final short utterance after a separately approved two-call design | Terra or Sol | `low` / `medium` |

The first NPC-ACTOR-01 implementation uses one call with structured
intent/action/utterance. A two-call decision→realization route is allowed only
if blind evaluation proves enough quality gain to justify added latency/cost.

High/max reasoning is not a synonym for natural dialogue. It may produce
over-complete, symmetrical or explanatory speech. Max/Ultra is not the default
for NPC runtime.

Qwen3-8B and llama.cpp are historical evidence only after the 2026-07-15 override. They are not a hidden fallback
when cloud configuration is missing. If cloud access is unavailable, the engine must stop safely or use an approved
deterministic／File candidate path; it must not silently change model quality.

## 8. Provider-Neutral Gateway

Future code must depend on an internal interface, not OpenAI SDK classes:

```text
NpcActorGateway
  propose(NpcActorRequest): NpcActorResponse

Implementations:
  FakeNpcActorGateway
  FileNpcActorGateway
  OpenAiResponsesNpcActorGateway (NPC-ACTOR-10A, offline tested before live use)
```

The Fake/File gateways must complete all schema, Validator and evaluation work
without network access or API Key.

## 9. OpenAI Responses API Mapping

The selected cloud implementation uses the Responses API,
Structured Outputs with a strict JSON Schema, an explicitly selected model and
reasoning effort, and a visibility-safe input packet.

Default privacy/cost controls:

```text
store: false
no full database upload
no author-only collection
no API Key in Git/log/report
bounded retry count
per-call model/reasoning/latency/token audit
no direct tool that writes SQLite/Canon
```

V1 request policy:

```text
endpoint: POST /v1/responses
model: gpt-5.6-terra
reasoning.effort: medium
store: false
text.format.type: json_schema
text.format.strict: true
max_output_tokens: bounded by the NPC response contract
tools: none
API key source: OPENAI_API_KEY environment variable only
```

Escalation to `gpt-5.6-sol`／`high` is a routing decision recorded in the audit, not a retry lottery. The trigger must
be a predeclared critical-scene policy or a representative evaluation failure; ordinary schema or safety rejection
does not automatically spend more money. At most one sanitized repair call may be designed later, and it still must
pass every local Gate.

Conversation state may be used for a bounded scene, but formal NPC memory and
relationships remain in SQLite. A provider conversation id is never the sole
copy of character state.

## 10. Evaluation

NPC-ACTOR-01 must compare baseline versus actor output on at least 20 fixed
situations. Required measures:

- knowledge/secret leak count;
- location/capability/continuity violation count;
- character voice consistency;
- AI exposition and repeated contrast-pattern rate;
- average and P95 spoken line length;
- silence/evasion/nonverbal appropriateness;
- contradiction with owned memory/beliefs;
- human blind preference;
- latency, tokens and estimated cost when live API is eventually allowed.

No candidate passes with any knowledge-boundary or formal-state bypass.

The first 20-item human review preferred the Actor option 20/20, but the
reviewer correctly identified an external-validity limitation: the baseline
used conspicuously explanatory AI phrasing. This is evidence for removing gross
AI exposition only. Before any human-likeness, ChatGPT-naturalness, live API or
production claim, run a randomized hard blind evaluation where both candidates
are concise and plausible and where multi-turn scenes test stable voice,
private motives, hesitation, deception, silence and repair.

NPC-ACTOR-02 supplies that harder offline package with 12 unseen non-Canon
multi-turn scenes, four recurring profiles, balanced deterministic A/B order,
separate answer identity and a 25 percent spoken-length gate. Both candidates
are manually authored evaluation fixtures, so Product Owner preference can
guide the project but cannot be mislabeled as a live-model benchmark. Its
technical gates passed, but its completed human review preferred Actor 4 times,
natural control 6 times and tied twice. The decision is
`NO_ACTOR_ADVANTAGE_OBSERVED`, so live API work and any broad human-likeness
claim remain blocked.

NPC-ACTOR-03 may perform one offline calibration revision. Its hypothesis is
that profile expression should scale with situation and relationship instead
of appearing in every line. The revealed NPC-ACTOR-02 set is development data,
not final evidence; a new holdout is mandatory. If the single revision still
shows no advantage, stop rather than tuning fixtures until they win.

The single NPC-ACTOR-03 revision passed its technical gates, but before rating
the Product Owner identified that point-to-point dialogue did not provide a
persistent environment deep enough to judge a three-dimensional world. Its
packet remains preserved and answer key sealed; no result is fabricated.

NPC-ACTOR-04 therefore widens only the evaluation unit, not the calibration
revision. It supplies four full scene trajectories with zone adjacency,
visibility, actors, objects, hazards, monotonic beats, actions, reactions and
explicit state changes. Its technical gates passed with 4 scenes, 12 beats,
balanced 2/2 anonymous identity, focused 7/7 and full 905/905 tests. The current
Gate is Product Owner comparison of all four complete trajectories. This still
does not authorize a live API, second calibration revision or production use.

NPC-ACTOR-04A closes that repeated Product Owner rating request with three
sealed internal reviews. The aggregate preferred Actor twice, natural control
once and marked the storm-gate pair `BOTH_FAIL`. This is reproducible internal
evidence, not a replacement for human taste or live-model evidence.

NPC-ACTOR-05 turns the measured failure into an offline action-safety loop. It
does not add another style prompt. It composes the existing actor contract,
physical binding, physical affordance, Game Master world-legality and ten-minute
time-budget checks in that order. A proposal gets at most two attempts. After
the first failure, only a sanitized repair request may be returned; after the
second failure, the proposal is rejected. A passing proposal is only a
`ACCEPTED_FOR_FORMAL_RESOLVE_CANDIDATE` and cannot write formal state.

The frozen four-case result is: Mira passes on the first attempt; Lian repairs
foreign knowledge; Ren repairs an over-limit push; Yan remains rejected when
both storm-gate attempts violate physical or world constraints. Rope tension,
friction coefficients, wet-slope dynamics and richer multi-object physics are
still outside the current model. No live model, Provider or API is connected;
that remains a separate authorization Gate.

NPC-ACTOR-06～09C tested a local Qwen3-8B route. The final 09C run reached formal Runtime validation but was rejected
with `low_character_fit`; no candidate or formal turn was written. This is valid safety evidence, not proof that all
local models are unusable. The Product Owner nevertheless chose cloud quality as the product route. Therefore no
local-model retry, 09D tuning loop or Qwen fallback remains on the active roadmap. The post-safety rejection
observability requirement moves into the cloud adapter foundation because the same local Validator can reject any
provider's output.

## 11. Stop Conditions

- API Key or paid call is required and not explicitly approved;
- a live call has no explicit per-call／daily budget or would silently escalate Terra to Sol;
- live request would include author-only/private data;
- model output requires weakening Validator/StateReducer/transaction rules;
- character fate, romance, Canon or formal creative decision is required;
- the same quality failure persists after one evidenced prompt revision and one
  model/reasoning comparison.

## 12. Official OpenAI References

- Model/reasoning guidance: `https://developers.openai.com/api/docs/guides/latest-model`
- Conversation state: `https://developers.openai.com/api/docs/guides/conversation-state`
- Structured Outputs: `https://developers.openai.com/api/docs/guides/structured-outputs`
- Pricing: `https://developers.openai.com/api/docs/pricing`
- Data controls: `https://developers.openai.com/api/docs/guides/your-data`

External model names and request shapes must be reverified immediately before
the live API implementation.
