# NPC Actor And Model Routing Blueprint V1

Status: `NPC_ACTOR_01_TECHNICAL_PASS / HUMAN_BLIND_REVIEW_PENDING / LIVE_API_BLOCKED`

Date: `2026-07-13`

## 1. Decision

The final runtime should use a provider-neutral NPC Actor interface with an
optional GPT API implementation. ChatGPT is the manual character audition and
prompt-development surface. Codex builds, tests and reviews the system; Codex
is not the production NPC actor.

API access does not make a model intrinsically more natural. It makes model,
context, schema, state, latency, cost and retries controllable by the engine.
Naturalness must be measured against the same scenario set used in manual
ChatGPT auditions.

## 2. Mandatory Route Order

```text
RETRIEVAL-00 authority / rebuild / retention
→ RETRIEVAL-01 scoped local retrieval benchmark
→ NPC-ACTOR-01 single NPC offline/Fake pilot
→ explicit API Key / cost / privacy approval
→ optional live GPT API pilot
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
| Major-character high-stakes decision | `gpt-5.6-sol` | `medium`, compare `high` only with measured gain |
| Important ordinary NPC decision/dialogue | `gpt-5.6-terra` | `medium` |
| Background NPC or high-volume bounded action | `gpt-5.6-luna` or deterministic rule | `low` |
| Final short utterance after a separate decision | Sol or Terra | `low` / `medium` |

The first NPC-ACTOR-01 implementation uses one call with structured
intent/action/utterance. A two-call decision→realization route is allowed only
if blind evaluation proves enough quality gain to justify added latency/cost.

High/max reasoning is not a synonym for natural dialogue. It may produce
over-complete, symmetrical or explanatory speech. Max/Ultra is not the default
for NPC runtime.

## 8. Provider-Neutral Gateway

Future code must depend on an internal interface, not OpenAI SDK classes:

```text
NpcActorGateway
  propose(NpcActorRequest): NpcActorResponse

Implementations:
  FakeNpcActorGateway
  FileNpcActorGateway
  OpenAiResponsesNpcActorGateway (later, gated)
```

The Fake/File gateways must complete all schema, Validator and evaluation work
without network access or API Key.

## 9. OpenAI Responses API Mapping

If later approved, the OpenAI implementation should use the Responses API,
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

## 11. Stop Conditions

- API Key or paid call is required and not explicitly approved;
- live request would include author-only/private data;
- model output requires weakening Validator/StateReducer/transaction rules;
- character fate, romance, Canon or formal creative decision is required;
- the same quality failure persists after one evidenced prompt revision and one
  model/reasoning comparison.

## 12. Official OpenAI References

- Model/reasoning guidance: `https://developers.openai.com/api/docs/guides/latest-model`
- Conversation state: `https://developers.openai.com/api/docs/guides/conversation-state`
- Structured Outputs: `https://developers.openai.com/api/docs/guides/structured-outputs`

External model names and request shapes must be reverified immediately before
the live API implementation.
