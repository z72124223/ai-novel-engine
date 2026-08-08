# WP-001E Dirty Worktree Dependency Map

## Extraction method

The map was extracted from local Kotlin text using package declarations, class/object/interface declarations, imports, public function declarations, model references, repository call names, migration references and test basenames. This is a lightweight dependency map, not a full abstract syntax tree (AST，抽象語法樹). It does not rewrite imports or format source.

## Cluster-level dependency map

| Cluster | Package(s) | Main types / public operations observed | Model references | Repository / migration references | Test relationship |
|---|---|---|---|---|---|
| A | `com.localnovel.engine`, `.model` | `Main` CLI handlers; shared `Models` types | Broad shared model surface; local model imports resolve locally | Existing `SqliteStoryRepository` calls; existing migrations 001–006; no dirty migration | No direct test for the whole dirty CLI batch |
| B | `com.localnovel.engine.service` | `create`, `validate`, `preflight`, review-decision intake | Asset candidate and review packet models | None found in dirty services | 7 basename-matched tests |
| C | `com.localnovel.engine.service` | Registry packet/preflight/validation and acceptance intake | Candidate-reference registry and acceptance models | None found in dirty services | 7 basename-matched tests |
| D | `com.localnovel.engine.service` | `audit`, `validate` | Continuity and character-gate models | None found in dirty services | 2 basename-matched tests |
| E | `com.localnovel.engine.service` | Prompt preparation/validation, reference packet, provider handoff, result intake/review | Image candidate, provider handoff and external result models | None found in dirty services | 13 basename-matched tests |
| F | `com.localnovel.engine.service` | `build`, `validate`, prose quality audit and revision brief | Novelization draft/revision/writer-brief models | None found in dirty services | 8 basename-matched tests |
| G | `com.localnovel.engine.service` | Source binding, structure bridge, draft validation, accepted handoff | Script packet and accepted handoff models | None found in dirty services | 4 basename-matched tests |
| H | `com.localnovel.engine.service` | Planning brief, draft validation, human review and preflight | Storyboard packet and planning models | None found in dirty services | 6 basename-matched tests |

## Dependency findings

### Production and test pairing

- Dirty Kotlin inventory: 47 production files and 45 test files.
- All 45 dirty test basenames map to a dirty production service basename.
- Orphan tests: 0 by basename pairing.
- This does not prove that a test compiles, passes or covers the full service behavior.

### Model references

The dirty Kotlin files reference 201 unique `com.localnovel.engine.model.*` names. All were found in the local `Models.kt` declaration surface. This is a local declaration check only: the remote `Models.kt` surface diverges, so compatibility after reconciliation remains unverified.

### Repository methods

The repository methods called by modified `Main.kt` were all found in the local `SqliteStoryRepository` declaration surface, including initialization, demo seeding, project/scene/session reads, character and memory reads, knowledge/fact/task reads, scene/thread/transition/world-event reads and table-row inspection. No missing repository method was found in this static check. This does not prove runtime behavior or remote compatibility.

### Migration and table dependencies

No dirty migration file appears in the original baseline. The local repository has migrations `001_init.sql` through `006_checkpoint_branching_runtime.sql`. The remote tree additionally contains `009_provisional_layer.sql`; that remote-only schema lane is a reconciliation risk, not a local missing-file repair task. No dirty service directly declared a migration/table dependency in the static scan.

### CLI wiring

All 45 dirty service names were found in the modified `Main.kt` import/usage surface. This shows intended wiring, not successful registration: command dispatch, argument validation and runtime construction still require isolated verification. The remote tree also contains dedicated CLIs for provisional-layer, asset-image-candidate, novelization and storyboard pipelines, so the local single-CLI path is a cross-cluster conflict point.

## Remote API conflict register

| File | Evidence | Risk |
|---|---|---|
| `Main.kt` | Git blob and parsed public/private function surface differ from remote; local imports many dirty services while remote has separate pipeline CLI surfaces | CLI command and service construction drift |
| `Models.kt` | Git blob and parsed type surface differ; remote includes additional provisional-layer, narrative-delivery and storyboard-related types | Shared model incompatibility |
| `CandidateReferenceAcceptancePacketService.kt` | Git blob and public function surface differ; remote includes additional digest helpers | Packet identity/validation contract drift |
| Remaining 89 Kotlin files | Git blob content is identical to remote | No local-vs-remote content divergence; surrounding integration still requires verification |

## Dependency anomalies

| Check | Result | Interpretation |
|---|---:|---|
| Production files without a matching test | 2 core files; 0 dirty service files | Core CLI/model coverage is not established |
| Tests without a matching production service | 0 | Pairing exists by basename only |
| Local model names missing | 0 of 201 unique refs | Local declaration presence only |
| Missing repository methods used by `Main.kt` | 0 | Static declaration check only |
| Dirty migration files | 0 | No local schema addition in this baseline |
| CLI registration proven end-to-end | 0 | Imports/usages are evidence of intent, not a completed dispatch audit |

## Independent verification state

None of the groups can yet be called independently compilable from this audit. The next safe verification must use a new clean worktree from the remote tip, then move only one selected group and its explicitly required dependencies into that worktree. The original dirty worktree must remain untouched.
