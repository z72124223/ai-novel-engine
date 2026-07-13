# Physical World and Affordance Blueprint V1

## Product Problem

Language models can produce locally fluent actions that violate morphology or
ordinary physics. A persistent zone graph can prove identity, visibility and
adjacent movement, but it cannot prove that a body can support a load, reach a
target, pass through an opening or move through an obstacle.

The canonical regression example is an adult standing up by placing meaningful
body weight on the shoulder of a medium dog. The sentence is grammatically
valid and the dog may be nearby, but the described support is unsafe and
physically implausible.

## Architecture

```text
model or authored action proposal
→ identity and knowledge checks
→ PhysicalAffordanceValidator
   ├─ morphology and body-part compatibility
   ├─ continuous 3D distance and reach
   ├─ barrier intersection
   ├─ mass, support, lift and push limits
   ├─ container and passage dimensions
   └─ gravity-derived required effects
→ existing Game Master / AiTurnResultValidator
→ StateReducer transaction only after every gate passes
```

WORLD-PHYSICS-01 implements only the independent validation core and offline
evidence. It does not change the formal runtime pipeline.

WORLD-PHYSICS-02 adds an offline NPC action binding layer. It preserves the
existing NPC Actor V1 request/response contract and introduces a separate
provider-visible prompt plus engine-owned validation context. The model may
choose an allowlisted action and visible target identifier, but it cannot
author mass, dimensions, load capacity, barriers or distances. Both the Actor
contract and physical affordance result must pass before a proposal can be
forwarded to Game Master review. Formal turn state remains unchanged.

## Authoritative Inputs

The action proposal may choose an action and targets, but it may not declare
its own convenient physics. The engine-owned snapshot supplies:

- metric position and axis-aligned bounds;
- mass and movability;
- body plan, grasping ability, stable support surfaces and load capacity;
- lift, push and reach limits;
- container interior and opening dimensions;
- fixed barriers and whether they block reach or movement;
- held, supported and contained relationships;
- gravity strength.

Reusable archetypes should later supply common facts such as an adult human,
medium dog, wooden chair or stone wall. Scene instances only store differences.
This avoids repeating the same common physical data in every scene.

## V1 Action Contract

| Action | Required checks |
|---|---|
| `USE_AS_SUPPORT` | contact reach, stable support surface, allowed support role, remaining load capacity |
| `LIFT` | contact reach, grasping ability, movable target, actor lift limit |
| `PUSH` | contact reach, movable target, actor push limit, clear destination path |
| `REACH` | surface distance within reach and no blocking barrier |
| `PLACE_ON` | actor holds object, destination reachable, surface stable, capacity and footprint fit |
| `DROP` | actor holds object, target movable, positive gravity; result must include downward fall and loss of held state |
| `ENTER_CONTAINER` | opening fit, interior fit, load capacity and unobstructed entry |
| `PASS_THROUGH` | opening fit and no movement-blocking barrier |

All failures use stable machine-readable rejection codes. Validation returns a
new result and must not mutate the world snapshot.

## Rule Boundary

V1 is intentionally a conservative rigid-body approximation. It does not model
soft tissue, detailed joints, fracture, fluid dynamics, cloth, animal behavior,
combat animation or numerical time integration. A pass means only that no V1
rule disproved the proposal. It is not proof that the action is realistic in
every respect.

Unknown or missing engine-owned physical evidence must fail closed with an
explicit code; the validator may not guess Canon measurements.

## Data Growth Policy

- Store reusable physical facts once in versioned archetypes.
- Store per-scene positions and temporary relations as state, not duplicated prose.
- Keep generated test fixtures and indexes reproducible from Git inputs.
- Media remains files; SQL may later hold identity, path, hash and state only.
- New physical data enters formal SQLite only through a future additive
  migration and reviewed import path, never through ad hoc SQL.

## Rollout Order

1. `WORLD-PHYSICS-01`: standalone deterministic validator and adversarial suite.
2. `WORLD-PHYSICS-02`: bind reviewed physical archetypes to NPC action proposals.
3. `WORLD-PHYSICS-03`: integrate accepted fields into formal turn validation,
   with additive migration only if evidence proves it necessary.
4. Later optional work: richer spatial indexing or external simulation, only if
   measured failures justify the cost.

No later stage is activated by this blueprint alone.
