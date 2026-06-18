# Memory And Knowledge

MVP-3 separates three layers:

- World truth: canonical facts, secrets and events stored by the engine.
- Character knowledge: what one character knows, believes, suspects, doubts or misremembers.
- Character memory: a character's remembered experience, promise, rumor, trauma or relationship context.

World truth does not automatically enter character knowledge. A character can act from a wrong belief if that belief is present in `knowledgeClaims`, while the canonical fact remains unchanged.

## Memory Types

Supported runtime memory types include:

- `CANON_PAST`
- `EPISODIC`
- `EMOTIONAL`
- `RELATIONAL`
- `SEMANTIC`
- `TRAUMA`
- `PROMISE`
- `RUMOR`

Memory visibility controls whether a memory can be retrieved. `author_only` and `inaccessible` memories are excluded before ranking. `archived` memories are not sent to context.

## Retrieval Scoring

`MemoryRetriever` uses deterministic scoring. Given the same DB state and input, ordering is stable:

```text
score =
  salience * 2
  + tagOverlap * 20
  + directEntityMatch * 25
  + currentGoalMatch * 15
  + emotionalCongruence * 10
  + dependencyRelation * 10
  + recency
  + unresolvedPromiseOrConflict * 15
  - decayPenalty
```

Each selected memory includes `retrievalScore` and `retrievalReasons`, such as matched player input tag, related to current goal, direct entity match, emotional trigger matched or unresolved promise/conflict.

## Context Budget

`ContextBudgeter` defaults to:

- `maxContextBytes = 24576`
- `maxMemoriesPerCharacter = 5`
- `maxKnowledgeClaimsPerCharacter = 8`
- `maxRecentTurnSummaries = 3`

Safety fields are never removed for budget reasons: identity fields, forbidden reveals, scene contract and world hard rules are kept. Lower-ranked memories and knowledge claims are omitted first, and the context records included/omitted counts.

## Runtime Memory Candidates

Codex may propose `memoryCandidates`, but cannot write SQLite directly. The engine accepts candidates only after validation:

- owner exists and plausibly perceived the turn,
- source events/facts exist in the current accepted turn or visible context,
- candidate does not contain forbidden reveal content,
- candidate does not turn unknown hidden truth into character knowledge,
- summary is not a wholesale narrative copy,
- duplicate runtime memory becomes reinforcement instead of another row.

Reframe creates a new memory with `supersedesMemoryId`; it does not delete or overwrite the old memory.

## Knowledge Updates And Conflict

`knowledgeUpdates` request a transition for one character and proposition. Conflict is explicit: weak evidence cannot silently overwrite an existing belief as `KNOWN`. The runtime may keep or move the claim toward `DOUBTED`/`SUSPECTED` while preserving canonical facts.

## Emotion Deltas

Emotion changes must include:

- emotion
- delta
- trigger
- sourceType/sourceId
- reason

Normal single-turn absolute delta is capped at 25. Memory-triggered deltas must cite an available memory ID.

## Recall Logging

`prepare-turn` is read-only. It ranks memories but does not increase `recall_count` and does not write `memory_recall_logs`.

After successful `resolve-turn`, each memory included in context is logged once for that context and its `recall_count` / `last_recalled_turn_number` are updated in the same transaction.

## Codex Turn Generation

When writing `AiTurnResult`, Codex should use only:

- `retrievedMemories`,
- `knowledgeClaims`,
- public/revealed facts in context,
- visible items/techniques,
- scene contract and world rules,
- character state, goal, mood and legal actions.

Codex must not reveal forbidden phrases, must not inject author-only truth, and must let characters act from their own beliefs and memories rather than from plot convenience.
