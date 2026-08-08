# Chenxing Default Sample Chain

Active creative world samples for `塵行` / 灰關.

## Files

| File | Role |
|------|------|
| `chenxing_story_input_pack_sample.json` | Local story input pack for 灰關外城市集開場 |
| `chenxing_story_input_pack_validation_sample.json` | PASS validation report for the story input |
| `chenxing_09_foundation_card_pack_validation_report_sample.json` | PASS validation report for the 09 foundation card contract |
| `pipeline/` | Full local story video + renderer chain PASS artifacts |
| `comic_video_setting_source/` | Setting pack, voice timeline, and image-facing preview build samples |

Foundation card contract source:

`samples/local_story_input/chenxing_09_foundation_card_pack_contract_sample.json`

## Validate story input

```powershell
.\gradlew.bat :app:run --args="validate-local-story-input-pack --input samples/chenxing/chenxing_story_input_pack_sample.json --validation chenxing-story-input-validation-v0 --out samples/chenxing/chenxing_story_input_pack_validation_sample.json"
```

## Validate 09 foundation card pack

```powershell
.\gradlew.bat :app:run --args="validate-09-foundation-card-pack --input samples/local_story_input/chenxing_09_foundation_card_pack_contract_sample.json --validation chenxing-09-foundation-card-pack-validation-v0 --out samples/chenxing/chenxing_09_foundation_card_pack_validation_report_sample.json"
```

## Verify local story video chain

```powershell
.\gradlew.bat :app:run --args="verify-local-story-video-chain --story-input samples/chenxing/chenxing_story_input_pack_sample.json --output-dir samples/chenxing/pipeline/chain_outputs --chain chenxing-story-video-chain-v0 --out samples/chenxing/pipeline/chenxing_story_video_chain_report_sample.json"
```

Regenerate the full pipeline chain:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/generate_chenxing_pipeline_samples.ps1
```

## Setting source preview

```powershell
.\gradlew.bat :app:run --args="build-setting-image-facing-proof-preview --setting-pack samples/chenxing/comic_video_setting_source/setting_pack_chenxing_preview_sample.json --scene scene-huiguan-market --output-dir samples/chenxing/comic_video_setting_source/image_facing_preview_build --build setting-image-facing-chenxing-preview-001 --report-out samples/chenxing/comic_video_setting_source/image_facing_preview_build/setting_image_facing_proof_preview_build_report.json"
```

## World source lookup (official SQL)

```powershell
.\gradlew.bat :app:run --args="query-world-source --db data/story.db --q 灰關 --limit 3"
```

Fresh clones must import the manifest first:

```powershell
.\gradlew.bat :app:run --args="apply-world-source-archive --manifest memory/chenxing_world_source_manifest.json --db data/story.db"
```

## Boundaries

- File-protocol JSON validation only.
- No provider/API, renderer/ffmpeg/TTS, media generation, canon, registry,
  release, or production lock.
- See `memory/active_world_source.json` for active world lock and
  `docs/HANDOFF.md` for first-read pointers.