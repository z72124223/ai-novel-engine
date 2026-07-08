# Grok Build / Codex Production Handoff And Image Production Lane V0

Marker: `GROK_CODEX_PRODUCTION_HANDOFF_V0`

Status: `ACTIVE_USER_APPROVED_WORKFLOW`

Date: 2026-07-08

## One-Line Rule

Grok Build writes the story. Codex turns safe story inputs into usable
**script production material**: scene lists, shot plans, dialogue/narration
packets, optional downstream Seedance prompt fields, reference checks, and
verification.

Terminal repo deliverable is **script / shot text**, not voiced comic MP4 or
in-repo video. Image prompts and candidates are optional downstream support
only. See `world/chenxing/formal/29_project_script_first_charter_v1.json`.

## Roles

| Owner | Does | Does Not Do |
| --- | --- | --- |
| Grok Build | Story production, plot, dialogue, blocked story sections, sensitive or rejected story rewrites. | File hygiene, image reference tracking, validation reports, project integration. |
| Codex | Production support, prompt（提示詞）preparation, image candidate（圖片候選）tracking, reference-first checks, JSON（資料交換格式）cleanup, docs, tests, verification. | Rewrite blocked Grok story, bypass review, approve final images, silently change canon（正典）. |
| User | Final taste decisions, real approval, production direction, tool choice when external image tools are needed. | None. User can override the workflow. |

## Simple Flow

1. Grok Build creates or revises story material.
2. Codex receives the story material from the user or a file.
3. Codex separates it into:
   - safe production support work Codex can handle;
   - story material that must return to Grok.
4. Codex produces allowed support outputs:
   - scene summary;
   - beat list;
   - shot list;
   - image prompt cards;
   - reference checklist;
   - candidate image intake notes;
   - validation or drift report.
5. Anything Codex cannot process is returned as `RETURN_TO_GROK`.

## Implemented CLI Commands

Create a Grok handoff template:

```powershell
.\gradlew.bat :app:run --args="create-grok-story-handoff-template --project chenxing --out out/grok_codex/grok_story_handoff_template.json"
```

Validate a Grok handoff file:

```powershell
.\gradlew.bat :app:run --args="validate-grok-story-handoff --input out/grok_codex/grok_story_handoff_template.json --out out/grok_codex/grok_story_handoff_validation.json"
```

Draft the Codex production handoff package:

```powershell
.\gradlew.bat :app:run --args="draft-codex-production-handoff --grok-handoff out/grok_codex/grok_story_handoff_template.json --validation out/grok_codex/grok_story_handoff_validation.json --out out/grok_codex/codex_production_handoff.json"
```

The final draft contains:

- `sceneSummaries`
- `shotCandidates`
- `imagePromptDrafts`
- `referenceChecks`
- `returnToGrok`

## RETURN_TO_GROK Format

Use this exact shape when Codex must send material back:

```text
[RETURN_TO_GROK]
Source:
Location:
Reason:
Needed from Grok:
Codex can continue after:
```

Rules:

- Do not rewrite the blocked section.
- Do not expand the blocked section.
- Do not hide the reason.
- Keep the request practical: tell Grok exactly what needs to be changed or
  completed.

## Image Work Rule

For Chenxing images, Codex may prepare and organize image work only under these
rules:

- Use reference-first（先餵參考圖）workflow for formal character or frame images.
- Treat every generated or external image as candidate-only until the user
  explicitly accepts it.
- Do not mark images as canon, approved asset（已批准素材）, registered
  reference（已登錄參考）, final media（最終媒體）, or production lock（生產鎖定）.
- Do not reuse the rejected opening preview frames as formal frames.
- If a tool cannot attach the locked character references, use it only for
  rough style exploration, not final character frames.

Required Chenxing references before formal frame work:

- `world/chenxing/assets/turnaround/lu_chenxing_photo_closest_174_80_turnaround_v2.png`
- `world/chenxing/assets/costume/lu_chenxing_opening_modern_costume_turnaround_v1.png`
- `world/chenxing/assets/turnaround/aijiang_production_turnaround_v1.jpg`
- `world/chenxing/assets/style_reference/`

## Safe Codex Outputs

Codex may create or update:

- story-to-scene breakdowns;
- storyboard（分鏡）planning notes;
- image prompt cards;
- local candidate metadata;
- drift reports;
- handoff notes;
- validation reports;
- docs and tests that support the production workflow.

Codex should avoid:

- final story prose;
- hidden story decisions;
- unreviewed canon changes;
- direct database writes;
- automated provider/API calls;
- approval language.

## First Useful Production Step

For a new Grok story chunk, Codex should produce this minimum package:

1. `scene_summary`
2. `shot_candidates`
3. `character_reference_requirements`
4. `image_prompt_drafts`
5. `RETURN_TO_GROK` blocks, if any

This is enough for the user to decide whether to send the material back to Grok,
move into image generation, or request another pass.

## Current Known Warning

The existing opening preview frames under
`world/chenxing/preview/opening_v1/frames/` are explicitly rejected drift
examples. They can be used to explain what went wrong, not as formal visuals.
