# WP-003B — Daping Coast Master Panorama GPT Session

Status: `REFERENCE_UPLOAD_REQUIRED`

## 1. Accepted Upstream Evidence

- WP-003A accepted Commit: `4dea7f071b58f300c13c16e76fe2f89651b9cc40`
- Handoff path: `world/chenxing/production/location_bible_handoffs/daping_coast_v1/location_bible_handoff.json`
- Handoff content SHA-256: `a3b6d6f421fb51983f2652c48ff9bf9a9138e1ee1d497dcc5bce8442255798da`
- Active task: `daping-lb-01-master-panorama`
- Expected candidate filename: `daping_lb_01_master_panorama_candidate_v1.png`

WP-003B is a GPT image-generation session, not a Codex implementation package. Codex must not start another package while this session is waiting for user review.

## 2. Required Reference Upload

Formal generation is blocked until the following exact file is attached as an image in the active ChatGPT conversation:

```text
world/chenxing/assets/style_reference/chenxing_opening_style_cinematic_misty_v1.png
```

Expected SHA-256:

```text
a983170bb67dcd78961acf6deffb80068b6e2daf6387f87eb9f1720dd5288d6a
```

Role: `OPENING_RENDER_STYLE_ONLY`.

This image controls atmosphere and rendering style only. Its depicted people, clothing, props, architecture, and geography are not likeness, costume, location-geometry, or Canon authority.

A Repository path alone does not attach the image to the image-generation tool. Until the file is uploaded in the current conversation, the formal state remains:

```text
REFERENCE_UPLOAD_REQUIRED
```

## 3. Generation Contract

After the required style image is attached, GPT must read the exact handoff Commit and task before generating exactly one candidate.

Required output:

- component: `MASTER_PANORAMA`
- aspect ratio: `16:9`
- camera: wide cinematic establishing panorama from a stable elevated human-eye viewpoint
- projection: ordinary perspective panorama, not equirectangular 360
- visible characters: none
- location: July 2026, Keelung Badouzi Daping Coast, Taiwan
- status: `CANDIDATE`

Required spatial anchors:

- sea-facing direction
- intertidal rock platform
- tide pools
- localized unnatural-seam area
- spatially legible safe/return route

Required atmosphere:

- daylight before the rapid water rise
- offshore storm influence
- misty, muted coastal atmosphere
- wet but readable rock textures

Allowed creative freedom:

- ordinary rock variation
- tide-pool outlines
- neutral coastal vegetation
- cloud pattern
- worn concrete-like texture only when it does not imply an unapproved historical explanation

Forbidden content:

- visible characters
- fantasy architecture
- ancient clothing
- post-crossing structures
- machinery
- glowing portal
- supernatural effects
- readable pseudo-text
- disclosed causal explanations
- later-story discoveries
- approval, Canon, final-media, release, or production-lock markings

The candidate must reserve geometry that can later be reproduced by a top-down zone plan and directional views.

## 4. Review Contract

After generation, the user chooses exactly one:

```text
APPROVE_CANDIDATE
REJECT_DRIFT
REQUEST_VARIATION
CHANGE_VISUAL_DIRECTION
```

Useful rejection codes include:

```text
SCENE_GEOMETRY_DRIFT
STYLE_DRIFT
WEATHER_OR_TIME_DRIFT
EXTRA_CHARACTER_OR_OBJECT
PROP_DRIFT
OTHER
```

A generated candidate is not an accepted location reference. Even `APPROVE_CANDIDATE` still requires file transfer to Codex, SHA-256 calculation, controlled intake, human-review evidence, and the existing Candidate Reference chain.

## 5. Downstream Gate

Do not unlock `daping-lb-02-top-down-zone-plan` merely because an image was generated.

Required order:

```text
candidate generated
→ user decision
→ candidate file returned to Codex
→ SHA-256 and intake validation
→ reference acceptance evidence
→ controller unlocks task 02
```

WP-003C begins only after the user keeps a candidate and the image file is available to Codex. WP-004 remains blocked until the required Daping Coast Location Bible components are accepted.

## 6. Current User Action

Upload this one image into the active ChatGPT conversation:

```text
world/chenxing/assets/style_reference/chenxing_opening_style_cinematic_misty_v1.png
```

After upload, say:

```text
參考圖已上傳，執行 daping-lb-01-master-panorama。
```
