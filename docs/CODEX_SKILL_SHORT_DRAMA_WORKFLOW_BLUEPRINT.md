# Codex Skill Short Drama Workflow Blueprint

Status: `DOCS_ONLY_NEXT_VERSION_PROTOTYPE`

Date: 2026-06-30

This document records a next-version prototype (試作) direction for turning the current comic video workbench into a creator-facing production package workflow. It is not part of the completed current-phase baseline and does not change Kotlin/JVM CLI behavior, SQLite state, validators, renderer output, provider/media execution, approval, canon, registry, release, or production lock.

Current baseline remains: public setting data plus an edited voice timeline fill can produce a validated local preview MP4 through `build-setting-comic-video-local-preview`.

## 1. Source Status

Reference video:

- [【附免费skill技能】【Seedance2.0】手把手教你用Codex智能体跑通精品AI短剧全流程 #aieducation](https://www.youtube.com/watch?v=jcNoL0hcabE)

Confirmed from page metadata and inspection:

- The video presents a Codex-oriented AI short drama workflow.
- The description links to external materials and a RunningHub canvas workflow.
- No public caption track was available during inspection, so this project does not have a reliable transcript of the Codex segment.

Therefore, this document treats the referenced method as research input. It does not claim that the video's spoken details are fully verified or superior to the current project workflow.

## 2. Why This Prototype Is Needed

The current project has a working local proof path:

```text
public SettingPack JSON
  -> setting-derived script/storyboard/image-facing proof artifacts
  -> edited ComicVideoVoiceTimelineFill JSON
  -> validated comic video packet and render handoff
  -> local preview MP4
```

That is enough to prove the local product-delivery baseline, but not enough for a high-quality production workflow. The missing layer is a creator-facing production package (製作交付包) that can be reviewed, edited, and optionally handed to external image/video tools later.

Needed next-version artifacts:

- storyboard table (正式分鏡表);
- prompt packet (提示詞包);
- asset cards (素材卡);
- character consistency cards (角色一致性卡);
- scene and prop cards (場景卡 / 道具卡);
- external tool handoff checklist (外部工具交接檢查表);
- project-local Codex skill/runbook (Codex 技能包 / 工作流包).

## 3. Difference From The Current Workbench

| Area | Referenced external workflow | Current project workflow | Prototype lesson |
| --- | --- | --- | --- |
| Primary style | Likely prompt-first production operation | Validator-first local JSON workbench | Keep validation as the authority, but add prompt-ready outputs |
| Codex role | Workflow operator that organizes short-drama production steps | CLI/code/workbench operator with strict gates | Package the repeatable production workflow into a local Codex skill/runbook |
| Storyboard | Likely creator-facing shot planning for video generation | Image-facing proof and panel tracks exist, but not a polished production table | Add a readable storyboard production table generated from validated artifacts |
| Prompts | Likely explicit image/video prompts for external tools | No formal provider prompt packet yet | Add prompt packets as reviewable metadata, not provider execution |
| Assets | Likely character/scene/material references for consistency | Asset lock is planned, not fully implemented in the comic-video path | Add candidate asset cards and consistency cards |
| External tools | Likely Seedance/RunningHub-style handoff | No external provider handoff in current baseline | Keep handoff optional and gated by a future decision |

## 4. Prototype Boundary

This prototype may define documents, JSON contracts, and CLI concepts.

It must not:

- call OpenAI, Seedance, RunningHub, Runway, Kling, or any other external provider;
- create provider job ids or remote URLs;
- bypass `AiTurnResultValidator`, storyboard validators, comic-video validators, or existing source-binding reports;
- write SQLite directly;
- claim final art, final voice acting, final production media, approval, canon, registry, release, or production lock;
- treat unverified video claims as project facts.

## 5. Proposed Production Package Layer

The prototype layer should sit after a PASS image-facing proof and before any optional external tool handoff.

```text
PASS ImageFacingDeliveryProof
  + ComicVideoVoiceTimelineFill
  -> ComicVideoProductionPackage
     -> StoryboardProductionTable
     -> ShotPromptPacket
     -> AssetPromptCards
     -> CharacterConsistencyCards
     -> ScenePropCards
     -> ExternalToolHandoffChecklist
```

The package is a reviewable planning artifact. It is not provider media and not an approval record.

Implemented first slice:

```text
create-comic-video-production-package-draft
validate-comic-video-production-package
```

This command takes a PASS `ImageFacingDeliveryProof` plus an edited `ComicVideoVoiceTimelineFill`, internally reuses the existing comic-video packet and validation path, and writes a PASS `ComicVideoProductionPackageDraft` when the source packet remains valid. The output contains a storyboard production table, shot prompt drafts, asset prompt cards, and voice style cards. It remains draft-only and does not submit prompts, create provider jobs, generate media, approve assets, write registry entries, apply canon, or create a production lock.

The validator writes a `ComicVideoProductionPackageValidationReport` that checks source linkage, storyboard/shot/asset/voice cross references, provider/API text, author-only leaks, forbidden completion claims, and final/approval/canon/production-lock wording. A PASS report means the package can move to local review, not external generation or approval.

### 5.1 StoryboardProductionTable

Purpose: make the existing panel/proof chain readable for creators.

Suggested fields:

```yaml
storyboardProductionTableId: ""
sourceProofId: ""
sourceVoiceTimelineFillId: ""
rows:
  - shotId: ""
    panelId: ""
    beatId: ""
    durationMillis: 3000
    visualGoal: ""
    camera: ""
    onScreenCharacters: []
    spokenText: ""
    subtitleText: ""
    continuityMustShow: []
    continuityMustNotShow: []
    productionNotes: []
```

### 5.2 ShotPromptPacket

Purpose: prepare prompt-safe shot instructions without executing any provider.

Suggested fields:

```yaml
shotPromptPacketId: ""
sourceStoryboardProductionTableId: ""
promptBoundary: provider_handoff_not_executed
shots:
  - shotId: ""
    positivePrompt: ""
    negativePrompt: ""
    referenceAssetCardIds: []
    cameraMotion: none
    durationMillis: 3000
    mustPreserve: []
    forbiddenContent: []
    reviewStatus: draft
```

Prompt text must be derived from visible, allowed, source-bound material. It must not smuggle author-only secrets into a visual prompt.

### 5.3 AssetPromptCards

Purpose: give each reusable visual element a stable creator-facing card.

Suggested card types:

- `CharacterConsistencyCard`
- `SceneCard`
- `PropCard`
- `CostumeCard`
- `VisualStyleCard`
- `VoiceStyleCard`

Suggested fields:

```yaml
assetCardId: ""
assetType: character | scene | prop | costume | visual_style | voice_style
sourceIds: []
publicDescription: ""
mustRemainStable: []
allowedVariation: []
forbiddenDrift: []
promptSafeDetails: []
authorOnlyExcludedCount: 0
reviewStatus: draft
```

## 6. Project-Local Codex Skill

Implemented repo-local skill (技能包 / 工作流包):

```text
.agents/skills/comic-video-production-package/SKILL.md
```

Its job would be to help Codex operate the workflow consistently:

1. inspect current proof, voice fill, and validation reports;
2. build or update a production package draft;
3. check prompt safety and source boundaries;
4. report what is missing before external generation;
5. avoid treating prompt packets as final approval.

This skill should be a workflow wrapper only. It should not grant permission to call providers or bypass validators.

## 7. Suggested Implementation Order

Do not start with external video tools.

Recommended order:

1. Define the production package JSON contract and docs.
2. Add a read-only CLI draft command from existing PASS proof plus voice fill.
3. Add validators for prompt safety, asset-card completeness, and source-boundary claims. Implemented first local validator: `validate-comic-video-production-package`.
4. Add a project-local Codex skill/runbook that uses only official CLI commands and files. Implemented first repo-local skill: `.agents/skills/comic-video-production-package/SKILL.md`.
5. Add sample Traditional Chinese production package output. Implemented first sample: `samples/comic_video_workbench_zh_tw/production_package_preview/`.
6. Only after review, decide whether external tool handoff should exist.

Possible first command name:

```text
create-comic-video-production-package-draft
```

Possible later command names:

```text
validate-comic-video-production-package
export-comic-video-provider-handoff
```

The last command must remain future and optional until the user explicitly approves provider/media handoff work.

## 8. Success Criteria For The Prototype

The prototype is useful only if it can prove:

- each shot is tied back to validated source artifacts;
- every prompt is visible-scene-safe and excludes author-only secrets;
- recurring characters have stable consistency cards;
- scenes and props have reusable cards;
- voice/subtitle timing still matches the local preview timeline;
- the package improves handoff clarity without weakening validation gates;
- the current local MP4 preview path still works unchanged.

## 9. Current Decision

Decision: keep this as a next-version prototype, not a formal blueprint replacement.

The current project version remains the stable base. This document only marks the next layer that appears necessary if the final target is a higher-quality voiced comic or AI short-drama video.
