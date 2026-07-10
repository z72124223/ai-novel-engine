# WP-001F-3 Candidate Reference Remote Validation

## Result summary

- Remote baseline: `74a939ae87f6536a95a5497b053b3367c14368f1`
- Clean worktree: `C:\Users\f7212\OneDrive\文件\ai-novel-engine-wp001f3`
- Branch: `reconciliation/wp001f3-candidate-reference`
- Production source under review: `app/src/main/kotlin/com/localnovel/engine/service/CandidateReferenceAcceptancePacketService.kt`
- Direct CLI: `app/src/main/kotlin/com/localnovel/engine/cli/AssetImageCandidateCli.kt`
- Direct tests: `CandidateReferenceAcceptancePacketTest`, `CandidateReferenceAcceptancePacketValidationTest`, `ImageCandidateReferencePacketTest`, `ImageCandidateReferencePacketValidationTest`, plus upstream human/registry/image-result boundary tests.
- Production changes: none
- Test changes: none

The remote Candidate Reference implementation is internally coherent under the targeted compile and test evidence. The full suite has four unrelated existing failures in World Source import/query tests, so the package conclusion is `BLOCKED_BY_UNRELATED_FAILURE`; no Candidate Reference defect was found.

## Contract chain

### 1. How an image candidate enters the acceptance packet

The packet is created by `CandidateReferenceAcceptancePacketService.create(...)`, called by `AssetImageCandidateCli.createCandidateReferenceAcceptancePacket(...)`. It consumes:

1. a `PASS` `CandidateReferenceRegistryPacketValidationReport`;
2. a `PASS` human acceptance decision-intake report;
3. `decisionAllowedDownstream=true`;
4. exact decision status `ALLOW_CANDIDATE_REFERENCE_ACCEPTANCE_PACKET`;
5. matching lineage/source identifiers and image-result sets;
6. nonblank provenance and source file fields.

Only after those checks does the service emit a `CandidateReferenceAcceptancePacket` with candidate-only status, evidence, source bindings, image-result identity digest and the next allowed validation step.

### 2. Human approval boundary

Human approval is represented by the registry human-acceptance decision intake. The upstream service requires a human decision source, reviewer/checklist evidence and the allowed downstream transition. The final acceptance service additionally requires the intake verdict to be `PASS`, `decisionAllowedDownstream=true` and the exact allow status.

`REQUEST_CHANGES` and `REJECT` can produce a validation report, but they do not release the next downstream step. They cannot produce an acceptance packet eligible for validation.

### 3. Automatic generation cannot promote a candidate

Generation or provider output is not treated as approval. The services reject provider job/API response fields, remote URLs, generated-status claims, `approved`, `registered`, `final`, production-lock and canon claims. The generated packet remains `candidate_reference_acceptance_candidate`; it does not become a registered reference or formal canon.

### 4. `sourceFileSha256` enforcement

The acceptance service compares the source-file hash from registry validation with the human decision-intake image result using normalized lowercase values. The acceptance validation service then requires a nonblank 64-character lowercase SHA-256 value for every image result and rejects invalid or mismatched values.

The direct validation test `missingSourceFieldInvalidShaOrSourceShaMismatchFails` covers missing, malformed and mismatched source hashes.

### 5. `imageResultIdentityDigest` construction and validation

The acceptance service sorts image results by trimmed `resultItemId`, then canonicalizes these fields:

- `resultItemId`
- `promptItemId`
- `sourceCandidateId`
- `assetType`
- normalized local image path
- metadata-only reference
- normalized lowercase `sourceFileSha256`

The fields are joined with the unit-separator delimiter and hashed with SHA-256. The resulting digest is stored in both packet source linkage and packet evidence. `CandidateReferenceAcceptancePacketValidationService` recomputes the digest from the packet image results and rejects forged or mismatched values.

`forgedImageResultIdentityDigestFails` and `sourceMismatchAndForgedImageResultIdentityFail` cover this boundary.

### 6. Registry packet versus acceptance packet

The registry packet validation layer proves upstream lineage, source binding, image-result presence and readiness for human acceptance. The human decision-intake layer proves that a person reviewed the candidate and that the decision is allowed downstream. The acceptance packet combines those two verified inputs into a candidate-only handoff. The acceptance packet validation layer then checks its own source fields, digest, status and safety claims.

Neither packet writes SQLite or formal canon. They are JSON-file protocol boundaries.

### 7. Drift rejection cannot be formally referenced

Source mismatches, forged image-result identifiers, altered image paths, altered prompt/candidate bindings and incomplete provenance create `FAIL` findings. A failed acceptance packet clears its image results, identity digest and next downstream step. Upstream `REQUEST_CHANGES` and `REJECT` decisions also leave the downstream step unreleased. The tests cover `IMAGE_RESULT_SET_MISMATCH`, `IMAGE_RESULT_NOT_IN_VALIDATION_REPORT`, source mismatch and forbidden status/claim cases.

There is no approved path from a drift-rejected candidate to a registered reference in this remote production chain.

### 8. CLI registration and reachability

`Main.kt` dispatches:

- `create-candidate-reference-acceptance-packet`
- `validate-candidate-reference-acceptance-packet`

Both dispatch to `AssetImageCandidateCli`. The CLI constructs the actual acceptance creation or validation service and reads/writes JSON files. The direct tests `cliWritesAcceptancePacketWithoutDatabase` and `cliWritesValidationReportWithoutDatabase` exercised those paths successfully.

### 9. Remote safety additions compared with the dirty local copy

The remote file adds:

- comparison of `sourceFileSha256` for each image result;
- deterministic `imageResultIdentityDigest` construction;
- SHA-256 helper logic used by that digest;
- corresponding validation of packet evidence and packet image-result identity.

The local dirty copy lacked these remote additions. No local code was imported into this worktree.

### 10. Bypass analysis

- No production path found that writes formal Canon or SQLite from an acceptance packet.
- The CLI only calls the create/validate JSON boundary services.
- Direct JSON fabrication is not sufficient: upstream PASS reports, human approval fields, source bindings, SHA-256 values, identity digest and forbidden-claim checks are revalidated.
- A caller could construct an in-memory object, but the formal downstream validation boundary still rejects missing or inconsistent evidence.
- No automatic provider-success-to-approval transition was found.

## Targeted test evidence

The following eight test classes were selected from actual keyword/path searches and executed with Gradle filters. They contain 51 test methods in total; result was 51 passed, 0 failed, 0 skipped:

- `CandidateReferenceAcceptancePacketTest`
- `CandidateReferenceAcceptancePacketValidationTest`
- `ImageCandidateReferencePacketTest`
- `ImageCandidateReferencePacketValidationTest`
- `CandidateReferenceHumanAcceptanceDecisionIntakeTest`
- `CandidateReferenceRegistryHumanAcceptanceDecisionIntakeTest`
- `CandidateReferenceRegistryPacketValidationTest`
- `ImageCandidateExternalResultIntakeTest`

The ten requested behaviors are represented by existing tests for the happy path, SHA-256 mismatch, identity digest forgery, non-human source, invalid status, drift/request-changes/reject, forbidden canon/approval claims, source/result spoofing, missing lineage/provenance and actual CLI reachability. No duplicate test was added.

## Build and full-suite classification

### Compile

Command:

```text
gradlew.bat :app:compileKotlin :app:compileTestKotlin --no-daemon --console=plain --stacktrace
```

Result: `BUILD SUCCESSFUL`. Only existing Kotlin serialization opt-in warnings were emitted.

### Targeted tests

Command:

```text
gradlew.bat :app:test --tests com.localnovel.engine.CandidateReferenceAcceptancePacketTest --tests com.localnovel.engine.CandidateReferenceAcceptancePacketValidationTest --tests com.localnovel.engine.ImageCandidateReferencePacketTest --tests com.localnovel.engine.ImageCandidateReferencePacketValidationTest --tests com.localnovel.engine.CandidateReferenceHumanAcceptanceDecisionIntakeTest --tests com.localnovel.engine.CandidateReferenceRegistryHumanAcceptanceDecisionIntakeTest --tests com.localnovel.engine.CandidateReferenceRegistryPacketValidationTest --tests com.localnovel.engine.ImageCandidateExternalResultIntakeTest --no-daemon --console=plain
```

Result: `BUILD SUCCESSFUL`; 51 targeted tests passed.

### Full test

Required command:

```text
gradlew.bat clean test --no-daemon --console=plain
```

The clean run exceeded the 5-minute execution limit before producing test reports. A subsequent non-clean `gradlew.bat test --no-daemon --console=plain` completed with 809 tests and 4 failures. All four failures were unrelated World Source tests:

- `WorldSourceArchiveImportCliTest.cliAppliesWorldSourceArchiveIntoSqlite`
- `WorldSourceArchiveImportServiceTest.validatesChenxingWorldSourceArchiveManifest`
- `WorldSourceArchiveImportServiceTest.importsChenxingWorldSourceArchiveIntoSqlite`
- `WorldSourceQueryCliTest.cliQueriesImportedOfficialWorldSourceChunks`

Their failures report `WORLD_SOURCE_HASH_MISMATCH` and `WORLD_SOURCE_BYTE_SIZE_MISMATCH`, or the resulting CLI exit code 1. No Candidate Reference test failed. Classification: `UNRELATED_EXISTING_FAILURE`; the clean-run timeout is separately an environment/performance limitation.

## Decision

**BLOCKED_BY_UNRELATED_FAILURE**

The remote Candidate Reference Production code compiles and all 51 directly relevant targeted tests pass. The safety contract supports `KEEP_REMOTE` / local `SUPERSEDED_BY_REMOTE`. Full-suite green status is blocked only by the unrelated World Source hash/byte-size failures and the initial clean-build timeout. No Production bug or missing Candidate Reference security test was found.
