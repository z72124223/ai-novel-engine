# WP-002A Existing Image Pipeline Inventory

Status: `DOCS_ONLY_EVIDENCE`

Base SHA: `7811d5975dac842755aaed3c74ed8982f64de3b7`

Branch: `feature/wp002a-gpt-image-handoff-foundation`

## 1. Result

The Repository already contains a complete, file-based safety chain from reviewed storyboard material to candidate-reference acceptance. The new Codex / GPT image handoff is an upstream interoperability contract. It does not replace, skip, or silently complete any existing stage.

The inspected Services are pure JSON transformation and validation code. No inspected Storyboard / Asset Candidate / Image Candidate / Candidate Reference Service imports a Repository, database, `StateReducer`, `AiTurnResultValidator`, or transaction writer. The CLI reads input JSON and writes output JSON only. File existence, a technical `PASS`, a generated image, or a descriptive handoff status does not write SQLite, change Canon, register a reference, or create a production lock.

## 2. Convention Decision

No existing general JSON Schema directory or matching production-handoff sample convention was found. WP-002A therefore establishes one non-competing convention:

```text
schemas/production/           reusable file-protocol schemas
samples/production_handoff/  example-only, non-production instances
world/<world-id>/production/ reproducible world-specific handoff packages later
```

Existing `world/chenxing/formal/` JSON and locked assets remain authoritative source material. They are not moved into the new directories.

## 3. Existing Stage Inventory

### 3.1 Storyboard Review And Human Decision

- CLI commands: `create-storyboard-packet-review`, `validate-storyboard-packet-human-review-preflight`, `validate-storyboard-packet-human-review-decision`.
- Model types: `StoryboardPacketReviewPacket`, `StoryboardPacketHumanReviewPreflightReport`, `StoryboardPacketHumanReviewDecision`, `StoryboardPacketHumanReviewDecisionIntakeReport`.
- Service types: `StoryboardPacketReviewService`, `StoryboardPacketHumanReviewPreflightService`, `StoryboardPacketHumanReviewDecisionIntakeService`.
- Input files: storyboard packet draft, draft validation report, packet review, human-review preflight, explicit human decision.
- Output files: review packet, human-review preflight report, human-decision intake report.
- Status / verdict fields: `verdict`, `reviewStatus`, `decisionStatus`, `decisionAllowedDownstream`, `nextAllowedDownstreamStep`, `blockedReason`.
- Hash / source identity: `acceptedScriptPacketId`, `scriptPacketDraftFile`, `scriptPacketDraftContentSha256`, storyboard draft / review / preflight IDs and file paths.
- Human approval boundary: downstream asset preparation requires an explicit decision intake that allows it; review `PASS` alone is not user approval.
- Canon / SQLite behavior: no write; JSON evidence only.
- Reusable handoff fields: source script ID/hash, review lineage, frame IDs, continuity anchors, asset requirements.
- Missing for GPT handoff: Git branch/Commit identity, upload checklist, explicit per-character appearance/costume versions, ChatGPT-visible secret boundary, and return-file contract.

### 3.2 Asset Candidate Preparation And Human Decision

- CLI commands: `validate-asset-candidate-preflight`, `create-asset-candidate-brief`, `create-asset-candidate-draft`, `validate-asset-candidate-draft`, `validate-asset-candidate-review-preflight`, `validate-asset-candidate-human-review-decision`.
- Model types: `AssetCandidatePreflightReport`, `AssetCandidateBrief`, `AssetCandidateDraft`, `AssetCandidateDraftValidationReport`, `AssetCandidateReviewPreflightReport`, `AssetCandidateHumanReviewDecision`, `AssetCandidateHumanReviewDecisionIntakeReport`.
- Service types: `AssetCandidatePreflightService`, `AssetCandidateBriefService`, `AssetCandidateDraftService`, `AssetCandidateDraftValidationService`, `AssetCandidateReviewPreflightService`, `AssetCandidateHumanReviewDecisionIntakeService`.
- Input files: storyboard human-decision intake, storyboard review, asset preflight/brief/draft/validation/review preflight, explicit asset-candidate decision.
- Output files: preflight, brief, draft, validation, review-preflight, and human-decision intake JSON.
- Status / verdict fields: `verdict`, `decisionStatus`, `decisionAllowedDownstream`, `downstreamBoundary`, `nextAllowedDownstreamStep`, `blockedReason`.
- Hash / source identity: script draft hash and full storyboard / decision / asset-candidate ID and file lineage.
- Human approval boundary: an explicit asset-candidate human decision must allow image-candidate preflight.
- Canon / SQLite behavior: no write; the draft stays candidate material.
- Reusable handoff fields: asset IDs/types, requirements, checklists, do-not-show constraints, residual risks.
- Missing for GPT handoff: conversation upload availability, repository asset hash binding per reference, per-shot placement/scale, and location-bible requirements.

### 3.3 Image Candidate Preflight

- CLI command: `validate-image-candidate-preflight`.
- Model type: `ImageCandidatePreflightReport`.
- Service type: `ImageCandidatePreflightService`.
- Input file: `AssetCandidateHumanReviewDecisionIntakeReport`.
- Output file: image-candidate preflight report.
- Status / verdict fields: `verdict`, `downstreamBoundary=image_candidate_preflight_only`, `nextAllowedDownstreamStep`, `blockedReason`.
- Hash / source identity: complete asset/storyboard/script lineage and `scriptPacketDraftContentSha256`.
- Human approval boundary: requires `decisionAllowedDownstream=true` and `decisionStatus=ALLOW_IMAGE_CANDIDATE_PREFLIGHT`.
- Canon / SQLite behavior: no write.
- Reusable handoff fields: image-candidate requirements, source IDs, do-not-show and forbidden-status evidence.
- Missing for GPT handoff: Git identity, reference upload state, shot output filename, and structured continuity fields.

### 3.4 Image Candidate Brief

- CLI command: `create-image-candidate-brief`.
- Model type: `ImageCandidateBrief`.
- Service type: `ImageCandidateBriefService`.
- Input file: image-candidate preflight report.
- Output file: image-candidate brief JSON.
- Status / verdict fields: `verdict`, `downstreamBoundary=image_candidate_brief_only`, `nextAllowedDownstreamStep`, `blockedReason`.
- Hash / source identity: inherited script hash, source IDs, and file lineage.
- Human approval boundary: relies on the passed upstream human decision; it cannot create approval.
- Canon / SQLite behavior: no write.
- Reusable handoff fields: requirements and preparation checklist.
- Missing for GPT handoff: explicit visual reference version/hash and conversation upload readiness.

### 3.5 Prompt Preparation And Validation

- CLI commands: `create-image-candidate-prompt-preparation`, `validate-image-candidate-prompt-preparation`.
- Model types: `ImageCandidatePromptPreparation`, `ImageCandidatePromptItem`, `ImageCandidatePromptValidationReport`.
- Service types: `ImageCandidatePromptPreparationService`, `ImageCandidatePromptValidationService`.
- Input files: image-candidate brief and prompt-preparation JSON.
- Output files: prompt-preparation and prompt-validation JSON.
- Status / verdict fields: `verdict`, `downstreamBoundary`, `nextAllowedDownstreamStep`, `blockedReason`.
- Hash / source identity: inherited script hash, prompt item ID, source candidate ID, and full upstream lineage.
- Human approval boundary: prompt validation only checks preparation safety; it cannot approve an image or claim generation.
- Canon / SQLite behavior: no write.
- Reusable handoff fields: `promptItemId`, `sourceCandidateId`, prompt text sections, do-not-show checklist, drift checklist, residual risks.
- Missing for GPT handoff: strict separation of facts/style/reference bindings/creative freedom/secrets, aspect ratio/use, and exact expected filename.

### 3.6 Manual Provider Handoff

- CLI commands: `validate-image-candidate-provider-handoff-preflight`, `validate-image-candidate-provider-handoff-decision`, `create-image-candidate-provider-handoff-packet`, `validate-image-candidate-provider-handoff-packet`.
- Model types: `ImageCandidateProviderHandoffPreflightReport`, `ImageCandidateProviderHandoffDecision`, `ImageCandidateProviderHandoffDecisionIntakeReport`, `ImageCandidateProviderHandoffPacket`, `ImageCandidateProviderHandoffPacketValidationReport`.
- Service types: `ImageCandidateProviderHandoffPreflightService`, `ImageCandidateProviderHandoffDecisionIntakeService`, `ImageCandidateProviderHandoffPacketService`, `ImageCandidateProviderHandoffPacketValidationService`.
- Input files: prompt validation, provider-handoff preflight, explicit decision, decision intake, and packet.
- Output files: preflight, decision intake, packet, and packet validation.
- Status / verdict fields: `verdict`, `decisionStatus`, `decisionAllowedDownstream`, `generatedStatus`, `downstreamBoundary`, `nextAllowedDownstreamStep`, `blockedReason`.
- Hash / source identity: prompt/source candidate IDs, packet/preflight/decision IDs, script hash, and file lineage.
- Human approval boundary: explicit manual handoff decision is required; packet validation only permits manual external action.
- Canon / SQLite behavior: no write; API endpoint, remote job, response, generated-status, and completion claims are rejected or required to stay empty.
- Reusable handoff fields: manual instructions, prompt items, IDs, source hash, next-step boundary.
- Missing for GPT handoff: ChatGPT Commit/path invocation, reference upload checklist, and structured return-to-Codex candidate identity.

### 3.7 External Result Intake

- CLI command: `validate-image-candidate-external-result-intake`.
- Model types: `ImageCandidateExternalResult`, `ImageCandidateExternalResultItem`, `ImageCandidateExternalResultIntakeReport`.
- Service type: `ImageCandidateExternalResultIntakeService`.
- Input files: provider-handoff packet validation and external-result JSON.
- Output file: external-result intake report.
- Status / verdict fields: `verdict`, `resultStatus`, `candidateStatus`, `nextAllowedDownstreamStep`, `blockedReason`.
- Hash / source identity: result/packet/prompt/source-candidate IDs, script hash, local image path or metadata-only reference, optional `sourceFileSha256`.
- Human approval boundary: result source must be explicit human/user/control metadata; result presence is never approval.
- Canon / SQLite behavior: no write. Only local/controlled paths are accepted; provider job IDs, API responses, remote URLs, and generated-status claims are forbidden.
- Reusable handoff fields: exact local candidate path, file SHA-256, candidate ID, prompt ID, do-not-show checklist, drift checklist, residual risks.
- Missing for GPT handoff: user decision vocabulary and conversation handback note.

### 3.8 Result Review And Human Decision

- CLI commands: `validate-image-candidate-result-review-preflight`, `validate-image-candidate-result-human-review-decision`.
- Model types: `ImageCandidateResultReviewPreflightReport`, `ImageCandidateResultReviewDecision`, `ImageCandidateResultReviewDecisionIntakeReport`.
- Service types: `ImageCandidateResultReviewPreflightService`, `ImageCandidateResultHumanReviewDecisionIntakeService`.
- Input files: external-result intake, review-preflight, and explicit result-review decision.
- Output files: result-review preflight and decision-intake report.
- Status / verdict fields: `verdict`, `decisionStatus`, `decisionAllowedDownstream`, `nextAllowedDownstreamStep`, `blockedReason`.
- Hash / source identity: result/packet/source IDs, local candidate fields, source file SHA-256, and script hash.
- Human approval boundary: exact result set and checklist must be reviewed; technical preflight cannot approve a candidate.
- Canon / SQLite behavior: no write.
- Reusable handoff fields: exact candidate identity, decision source/status, drift/do-not-show evidence, and downstream gate.
- Missing for GPT handoff: standardized drift-code enum and explicit `REQUEST_VARIATION` / `CHANGE_VISUAL_DIRECTION` states.

### 3.9 Image Candidate Reference Packet

- CLI commands: `create-image-candidate-reference-packet`, `validate-image-candidate-reference-packet`.
- Model types: `ImageCandidateReferencePacket`, `ImageCandidateReferencePacketValidationReport`.
- Service types: `ImageCandidateReferencePacketService`, `ImageCandidateReferencePacketValidationService`.
- Input files: result-review preflight and result-review decision intake.
- Output files: candidate-reference packet and validation report.
- Status / verdict fields: `verdict`, `packetStatus=reference_candidate`, `downstreamBoundary`, `nextAllowedDownstreamStep`, `blockedReason`.
- Hash / source identity: exact image result set, source file hash, prompt/source IDs, result/packet lineage, and script hash.
- Human approval boundary: only a reviewed candidate set may become a reference candidate; it is not yet registered or accepted.
- Canon / SQLite behavior: no write.
- Reusable handoff fields: review summary/notes, image result identity, source chain.
- Missing for GPT handoff: none that should bypass the later acceptance stages.

### 3.10 Candidate Reference Human Acceptance And Registry Preflight

- CLI commands: `validate-candidate-reference-human-acceptance-decision`, `validate-candidate-reference-registry-preflight`.
- Model types: `CandidateReferenceHumanAcceptanceDecision`, `CandidateReferenceHumanAcceptanceDecisionIntakeReport`, `CandidateReferenceRegistryPreflightReport`.
- Service types: `CandidateReferenceHumanAcceptanceDecisionIntakeService`, `CandidateReferenceRegistryPreflightService`.
- Input files: candidate-reference packet validation, explicit human-acceptance decision, and decision intake.
- Output files: human-acceptance decision intake and registry preflight.
- Status / verdict fields: `verdict`, `decisionStatus`, `decisionAllowedDownstream`, `downstreamBoundary`, `nextAllowedDownstreamStep`, `blockedReason`.
- Hash / source identity: exact candidate result set and full script/provider/result/reference lineage.
- Human approval boundary: explicit human acceptance is required before registry preparation; image generation or earlier user review cannot substitute for it.
- Canon / SQLite behavior: no write.
- Reusable handoff fields: exact candidate identity, checklist evidence, decision source, next-step gate.
- Missing for GPT handoff: none; this remains an existing downstream safety boundary.

### 3.11 Candidate Reference Registry Packet And Second Human Gate

- CLI commands: `create-candidate-reference-registry-packet`, `validate-candidate-reference-registry-packet`, `validate-candidate-reference-registry-human-acceptance-decision`.
- Model types: `CandidateReferenceRegistryPacket`, `CandidateReferenceRegistryPacketValidationReport`, `CandidateReferenceRegistryHumanAcceptanceDecision`, `CandidateReferenceRegistryHumanAcceptanceDecisionIntakeReport`.
- Service types: `CandidateReferenceRegistryPacketService`, `CandidateReferenceRegistryPacketValidationService`, `CandidateReferenceRegistryHumanAcceptanceDecisionIntakeService`.
- Input files: registry preflight, registry packet, packet validation, and a second explicit human-acceptance decision.
- Output files: registry packet, validation report, and second decision-intake report.
- Status / verdict fields: `verdict`, `packetStatus=candidate_reference_candidate`, `decisionStatus`, `decisionAllowedDownstream`, `nextAllowedDownstreamStep`, `blockedReason`.
- Hash / source identity: image result set, source file hashes, source IDs, script hash, and full reference lineage.
- Human approval boundary: acceptance-packet creation requires every required checklist category to pass and no unresolved blocking issue.
- Canon / SQLite behavior: no write; even the registry packet is preparation-only and forbids registered/approved/locked/Canon claims.
- Reusable handoff fields: source binding, local-reference integrity, drift/do-not-show evidence, and no-approval/no-lock boundary.
- Missing for GPT handoff: none; it must remain downstream of GPT return.

### 3.12 Candidate Reference Acceptance Packet

- CLI commands: `create-candidate-reference-acceptance-packet`, `validate-candidate-reference-acceptance-packet`.
- Model types: `CandidateReferenceAcceptancePacket`, `CandidateReferenceAcceptancePacketValidationReport`.
- Service types: `CandidateReferenceAcceptancePacketService`, `CandidateReferenceAcceptancePacketValidationService`.
- Input files: registry packet validation and second human-acceptance decision intake.
- Output files: acceptance packet and validation report.
- Status / verdict fields: `verdict`, `packetStatus=candidate_reference_acceptance_candidate`, `downstreamBoundary`, `nextAllowedDownstreamStep`, `blockedReason`.
- Hash / source identity: exact `sourceFileSha256` per image result, `sourceImageResultIdentityDigest`, evidence `imageResultIdentityDigest`, script hash, and complete lineage.
- Human approval boundary: requires a passed human decision intake with `ALLOW_CANDIDATE_REFERENCE_ACCEPTANCE_PACKET`; the exact result set, local path, source file hash, and derived identity digest must remain stable.
- Canon / SQLite behavior: no write. Its checklist explicitly records `formal_registry_write_not_performed`, `no_status_upgrade_no_lock_no_canon`, and future validation required.
- Reusable handoff fields: source file hash and identity digest are the final anti-substitution boundary for later work.
- Missing for GPT handoff: no direct gap. GPT must never manufacture or bypass this acceptance evidence.

## 4. ChatGPT Handoff Insertion Point

The safe insertion is:

```text
reviewed script / storyboard
-> existing asset and image-candidate preparation
-> WP-002A ChatGPT handoff files
-> ChatGPT receives required visual references in the conversation
-> ChatGPT returns candidate files and explicit user decision metadata
-> Codex computes sourceFileSha256 and records controlled local paths
-> existing External Result Intake
-> existing Result Human Review
-> existing Image Candidate Reference Packet
-> existing Candidate Reference human acceptance and registry stages
-> existing Candidate Reference Acceptance validation
```

The handoff schemas do not authorize calling a provider, accepting a candidate, registering a reference, writing Canon, or writing SQLite.

## 5. New Contracts Added By WP-002A

- Git identity: source Commit, branch, normalized handoff-content hash, and caller-supplied handoff Commit/path.
- Conversation reference readiness: exact Repository asset path/hash, upload state, and `REFERENCE_UPLOAD_REQUIRED` behavior.
- Shot identity: character ID plus appearance/costume version, position, scale, pose, visible props, location zone, camera, spatial anchors, continuity, and output filename.
- Prompt separation: visible facts, style, reference bindings, camera/composition, allowed freedom, negative constraints, secret exclusions, and output use.
- User review vocabulary: pending, approve candidate, reject drift, request variation, or change direction, with standardized drift codes.
- Return-to-Codex: exact candidate identity, expected filename, user decision, conversation note, nullable pre-intake hash, and the existing CLI stages still required.

## 6. Hash Normalization Rule

`handoffContentSha256` is computed over UTF-8 canonical JSON with recursively sorted object keys, compact separators, and Unicode preserved. Before hashing, set `handoffContentSha256` and optional `handoffCommitSha` to JSON `null`. This avoids a self-referential hash while keeping the field required in the committed handoff. The Commit that contains the handoff remains caller-supplied metadata.

## 7. Potential WP-002B Integration Points

If a real Pilot proves manual assembly error-prone, a future Kotlin package could:

1. map reviewed storyboard/script IDs and `ImageCandidatePromptItem` data into the six schemas;
2. calculate normalized handoff SHA-256 and referenced-file hashes;
3. validate reference upload readiness and per-shot version bindings;
4. map `image_return_to_codex_v1` into an `ImageCandidateExternalResult` draft without bypassing the existing validators;
5. verify exact candidate file/hash before invoking the current External Result / Reference chain.

WP-002B is not currently necessary. The first real WP-004 handoff should test these contracts manually before any Kotlin compiler/validator is approved.

## 8. Known Gaps For WP-003 And WP-004

- WP-003 must inventory which locked character, costume, animal, style, location, prop, and continuity references actually exist and which are only planned.
- The recurring location needs a Location Bible: master panorama, top-down plan, four directional views, spatial anchors, key details, scale reference, and optional 360 panorama. A 360 image alone is not authoritative geometry.
- WP-004 must bind real opening Shot 01-03 data, not reuse the example IDs in `samples/production_handoff/`.
- ChatGPT cannot use a GitHub image as a visual input merely because the path exists; the required files must be attached in the active conversation or the handoff stays `REFERENCE_UPLOAD_REQUIRED`.
- A returned image hash is nullable before Codex receives the file, but must be computed and verified before existing external-result and candidate-reference acceptance stages.

## 9. WP-002A Decision

Decision: `WP002A_PASS_WITH_SCHEMA_VALIDATOR_GAP`

Evidence:

- six schemas and six examples parse as valid JSON;
- all six example `schemaVersion` values match their schema contracts;
- all local `$ref` pointers resolve and every schema regular expression compiles;
- 17 Repository/example paths were checked, with one missing Location Bible image explicitly marked as a placeholder;
- eight bound source/reference file SHA-256 values match the Repository files;
- normalized handoff content hash matches `c62da115282f83702952f182125c0075b84c238487d8d50101076dd843a8449d`;
- no example contains a private absolute path, real Shot 01-03 ID, or positive approved-reference/Canon/final-media/release/production-lock status;
- no preinstalled JSON Schema validator was available and no dependency was added, so full instance-to-schema validation is recorded as `SCHEMA_META_VALIDATION_NOT_AVAILABLE`;
- no Kotlin, test, migration, SQLite, Canon, locked asset, image, or video file was modified.
