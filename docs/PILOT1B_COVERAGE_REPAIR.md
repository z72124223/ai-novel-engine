# Pilot-1b Coverage Repair

Pilot-1b is a narrow repair pass for the Pilot-1 coverage failure. It does not start MVP-6 and does not add checkpoint, branching, rewind or replay systems.

## Scope

Pilot-1b repairs only three gaps found in `docs/PILOT1_FINDINGS.md`:

1. A real setting pack can define legal scene transitions.
2. A scheduled world event can naturally become pending and apply pressure.
3. Relevant public setting context does not collapse to an empty `2` byte section when transitions or world events matter.

## Runtime Topology

Setting packs can now include:

- `locationConnections`
- `sceneTransitions`
- `scheduledWorldEvents`

These records are parsed, validated, diffed, previewed and applied through the normal setting pack pipeline. They use existing SQLite tables from MVP-4 and MVP-5; no migration is required.

## Validation Rules

- Scene transitions must reference existing source and target scenes.
- Cross-location transitions require a matching location connection.
- `requiredPlayerIntent` is mandatory for setting-pack scene transitions.
- Scheduled world events require `payload.publicEffect`.
- Scheduled world events cannot be `author_only` and cannot leak author-only secret IDs or content.
- `PATCH_CANON` that touches runtime topology is blocked while an active session exists.

## Context Rules

When relevant, the final `ContextPacket` includes compact setting cards for:

- available scene transitions
- target scene and target location settings
- location connection constraints
- pending scheduled world events

These cards are bounded by `settingContextBudget`, protected by semantic coverage when they are turn-critical, and still exclude the full setting pack, full dependency graph and author-only secrets.

## Pilot-1b Fixture

`samples/pilot1b_coverage_pack.json` provides a small repair fixture:

- `scene-abandoned-post`
- `scene-abandoned-post-perimeter`
- `conn-abandoned-post-to-perimeter`
- `transition-abandoned-post-to-perimeter-hold`
- `world-event-verifier-arrival-pressure`

Use it only with a fresh Pilot-1b database. Do not patch `data/pilot1-rerun.db`.
