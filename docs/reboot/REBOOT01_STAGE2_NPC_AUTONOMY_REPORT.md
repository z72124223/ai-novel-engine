# REBOOT-01 Stage 2 — Bounded Off-screen NPC Autonomy

Date: 2026-07-12

Status: `STAGE_2_PASS`

## Implemented loop

```text
formal ContextPacket
→ NPC perception
→ owned-memory importance
→ reflection
→ active goal
→ bounded schedule
→ action proposal
→ Game Master world/knowledge/location checks
→ existing resolve-turn validation
→ one SQLite transaction for event/time/state/evidence/DecisionTrace
```

The first executable profile is a sandbox-only Shen Shuang candidate. It contains a deep motivation, false belief, fear, relationship debt, and off-screen agenda. It is not Canon and cannot authorize Canon mutation.

## DecisionTrace evidence

Every proposal records:

- active goal;
- relevant owned memories;
- current owned beliefs;
- perceived situation and formal location;
- considered and selected actions;
- predicted cost;
- rejected alternatives;
- off-screen reason;
- complete proposal and Game Master resolution.

Accepted traces are inserted inside the same transaction as the formal event, world-time advance, turn log, state version, and Stage 1 run evidence. Rejected proposals are recorded as `REJECTED` after the pending context is safely aborted. A forced trace-write failure rolls back the event and turn.

## World and knowledge boundary

- actor must be a non-player character present in formal context;
- player and NPC must occupy different locations in `continuityConstraints.characterLocations`;
- action type must be allowlisted;
- knowledge and memory references must belong to that NPC;
- proposal remains a proposal until Game Master and existing runtime validators accept it;
- sandbox patch rows have `canon_mutation_allowed=0` enforced by schema and repository checks.

## Observable consequence

The accepted test advances formal world time by ten NPC minutes and writes an event describing physical signs of the off-screen search. A later player return context reads the event from `recentEvents`, proving the action can be observed after the player comes back.

## Verification

- `NpcAutonomyTest`: 5/5 PASS.
- Related focused suites: 86/86 PASS.
- Covers additive migration, accepted off-screen action, location/knowledge legality, rejected evidence, return observability, CLI reachability, and transaction rollback.
- Sample JSON parse: PASS.
- `git diff --check`: PASS.

## Non-goals preserved

- no Canon update;
- no external model or provider API;
- no image task resumed;
- no direct story-state SQL bypass;
- no claim that one deterministic NPC profile is a complete living-world simulation.

Stage 3 may consume DecisionTrace and run evidence for drama/critic scoring, but may revise a draft at most once and may never convert quality scores into world-authority bypasses.
