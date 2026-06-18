# Referenced Capability Contracts

When a turn explicitly uses a technique, item, or weapon, the AI-facing context must include a compact `activeCapabilityContracts` entry. A name and description are not enough to authorize use.

## Detection

`TurnSemanticPlanner` derives a `REFERENCED_TECHNIQUE`, `REFERENCED_ITEM`, or `REFERENCED_WEAPON` requirement when the player input names an available capability and expresses an explicit use intent. The requirement is Tier 1 and must be satisfied before `prepare-turn` saves a pending context.

## Provenance

`CapabilityContractResolver` reads the compact contract from canonical runtime sources:

- core `techniques` / `items` rows for ID, name, owner, and public description;
- latest `entity_revisions.snapshot_json` for effects, prerequisites, costs, limitations, forbidden uses, visibility, and source revision;
- `dependency_edges` for compact relation provenance.

Use the read-only diagnostic command:

```powershell
./gradlew.bat run --args="explain-capability --db data/pilot1-rerun.db --session SESSION_ID --entity-type technique --entity-id TECH_ID --actor CHARACTER_ID --out out/capability.json"
```

If canonical data is incomplete, the report returns `CANON_CAPABILITY_CONTRACT_INCOMPLETE`. The engine must not invent missing prerequisites, costs, limitations, or hidden effects.

## Context Shape

`activeCapabilityContracts` contains one compact card per referenced capability:

- `entityType`
- `entityId`
- `name`
- `authorizedActorIds`
- `intendedUse`
- `effects`
- `prerequisites`
- `requirements`
- `costs`
- `limitations`
- `forbiddenUses`
- `currentEligibility`
- `eligibilityReasons`
- `allowedNarrativeClaims`
- `forbiddenNarrativeClaims`
- `sourceRevision`

Empty canonical lists are represented as `NONE` when the canonical source explicitly allows no prerequisite, cost, limitation, or forbidden use. Detailed provenance stays in `explain-capability`, `explain-context-budget`, and `audit-context` sidecars.

## Validation

`AiTurnResult.capabilityUseChecks` must accompany successful capability use. The validator checks actor authorization, prerequisite status, contract scope, forbidden uses, and cost application.

When a successful use has costs, the result must include a matching character-state delta that records the cost. Failed attempts may cite unsatisfied prerequisites, but cannot claim successful effects.

The same path is used for techniques, items, and weapons. This avoids special-casing one Pilot fixture and keeps AI output inside the context-visible contract.
