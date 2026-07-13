# WORLD-PHYSICS-03 — Formal Turn Physical Gate

Status: `ACTIVE`

Date: `2026-07-14`

Parent blueprint: `docs/PHYSICAL_WORLD_AND_AFFORDANCE_BLUEPRINT_V1.md`

## Goal

Connect declared `PhysicalActionProposal` values to formal turn validation before
any pending turn can enter the SQLite transaction. The engine-owned physical
context must be strict, bounded and hash-bound to the exact Context and Result.

## Contract

`AiTurnResult` may declare zero or more `physicalActionProposals`. A proposal
contains only action, actor, target and action parameters. It never contains
mass, dimensions, reach, load capacity, geometry or barriers.

The separate engine-owned context contains:

- exact project, session, turn, context and state-version bindings;
- `contextHash` and canonical `AiTurnResult` SHA-256;
- reviewed archetypes and one physical world snapshot;
- allowlisted action, actor and target identifiers.

The context is parsed with unknown keys rejected. Both sides must be present or
both absent. Supplying proposals without context, or context without proposals,
is a validation error.

## Validation Order

1. Run existing protocol, identity, state-version and knowledge checks.
2. Validate physical context schema, boundary, bounds and unique identifiers.
3. Verify Context identity/hash and canonical Result SHA-256.
4. Require unique proposal IDs and exact action/actor/target allowlists.
5. Reject stateful action types until formal physical relation persistence exists.
6. Run `PhysicalAffordanceValidator` for eligible actions.
7. Return all physical errors before narrative quality evaluation and before
   `repository.resolvePendingTurn`.

## Formal V1 Action Policy

Allowed for transaction-preflight validation:

- `USE_AS_SUPPORT`;
- `REACH`.

Fail closed with `physical_state_persistence_unavailable`:

- `LIFT`;
- `PUSH`;
- `PLACE_ON`;
- `DROP`;
- `ENTER_CONTAINER`;
- `PASS_THROUGH`.

These blocked actions change position or held/supported/contained relations.
They require a later reviewed state model and additive migration; acknowledging
an effect in prose is not persistence.

## Required Cases

- stable chair support passes the formal physical gate;
- adult using a medium dog's shoulder is rejected with stable physical evidence;
- result proposal without physical context is rejected;
- physical context without a result proposal is rejected;
- context identity/hash or result hash mismatch is rejected;
- non-allowlisted actor/action/target is rejected;
- stateful action is rejected before transaction even if affordance rules pass;
- physical rejection leaves pending context and formal session state unchanged;
- old non-physical turn JSON and tests remain compatible.

## Allowed Scope

- governance and WORLD-PHYSICS documents;
- formal-turn physical context models, validator and strict JSON support;
- narrow changes to `AiTurnResult`, `AiTurnResultValidator`, `MultiTurnRuntime`,
  `ResolveTurnOptions`, `StorySessionCli` and CLI help;
- direct tests, one Schema and non-Canon/review evidence under
  `schemas/world_physics/` and `samples/world_physics03/`.

## Prohibited Scope

- `StateReducer`, Repository writes, migrations, SQLite tables or ad hoc SQL;
- Canon physical measurements or Setting Pack changes;
- sending engine-owned geometry or physical truth to a model or Provider;
- semantic extraction of undeclared physical actions from prose;
- API, Provider, media generation, Web server, port or force push;
- broad staging or edits in the original dirty Worktree.

## Verification

- focused formal-gate and transaction-boundary tests;
- related Actor and physical regression tests;
- full Gradle suite;
- strict JSON, Schema, SHA-256 and immutable-input checks;
- no secret, private absolute path, network endpoint or credential;
- Git diff limited to the reviewed package scope.

## Completion Decision

Use `FORMAL_TURN_PHYSICAL_GATE_PASS_STATEFUL_ACTIONS_BLOCKED` only when all
required cases and tests pass. This means declared support/reach actions are
enforced before formal state writes. It does not prove semantic coverage of
undeclared prose, persist physical world relations, promote Canon data or
authorize WORLD-PHYSICS-04.
