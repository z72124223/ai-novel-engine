# Traditional Chinese Comic Video Workbench Sample Pack

Status: `LOCAL_JSON_SAMPLE_PACK_ZH_TW`

This sample pack demonstrates a Traditional Chinese comic video path through the local Kotlin/JVM CLI and JSON file protocol. It can be rendered into a local preview MP4 with `render-comic-video-local`.

It is not canon, not a setting-pack mutation, not provider output, not approval, not registry data, not a release, and not a production lock.

## Files

- `image_facing_delivery_proof_zh_tw_sample.json`: PASS source proof used as the workflow entry point.
- `comic_video_packet_zh_tw_sample.json`: packet produced from the source proof.
- `voice_timeline_fill_template_zh_tw_sample.json`: editable fill template produced from the packet.
- `voice_timeline_fill_zh_tw_sample.json`: edited Traditional Chinese voice/audio/subtitle metadata.
- `comic_video_packet_filled_zh_tw_sample.json`: packet after applying the edited fill.
- `comic_video_packet_validation_zh_tw_sample.json`: PASS validation report.
- `comic_video_render_handoff_manifest_zh_tw_sample.json`: PASS render handoff manifest.
- `production_package_preview/production_package_draft_zh_tw_sample.json`: PASS draft production package with storyboard rows, shot prompts, asset prompt cards, and voice style cards.
- `production_package_preview/production_package_validation_zh_tw_sample.json`: PASS local validation report for the draft production package.

## Rebuild

Run from the repository root:

```powershell
.\gradlew.bat :app:run --args="create-comic-video-packet --image-facing-delivery-proof samples/comic_video_workbench_zh_tw/image_facing_delivery_proof_zh_tw_sample.json --packet comic-video-zh-tw-sample-packet-001 --panel-duration-ms 3000 --out samples/comic_video_workbench_zh_tw/comic_video_packet_zh_tw_sample.json"
.\gradlew.bat :app:run --args="create-comic-video-voice-timeline-fill-template --comic-video-packet samples/comic_video_workbench_zh_tw/comic_video_packet_zh_tw_sample.json --fill voice-timeline-fill-template-zh-tw-sample-001 --voice-id voice-lin --character-id char-lin --display-name 林 --voice-label local-hanhan-lin --out samples/comic_video_workbench_zh_tw/voice_timeline_fill_template_zh_tw_sample.json"
.\gradlew.bat :app:run --args="fill-comic-video-voice-timeline --comic-video-packet samples/comic_video_workbench_zh_tw/comic_video_packet_zh_tw_sample.json --voice-timeline-fill samples/comic_video_workbench_zh_tw/voice_timeline_fill_zh_tw_sample.json --out samples/comic_video_workbench_zh_tw/comic_video_packet_filled_zh_tw_sample.json"
.\gradlew.bat :app:run --args="validate-comic-video-packet --comic-video-packet samples/comic_video_workbench_zh_tw/comic_video_packet_filled_zh_tw_sample.json --validation comic-video-validation-zh-tw-sample-001 --out samples/comic_video_workbench_zh_tw/comic_video_packet_validation_zh_tw_sample.json"
.\gradlew.bat :app:run --args="create-comic-video-render-handoff-manifest --comic-video-packet-validation samples/comic_video_workbench_zh_tw/comic_video_packet_validation_zh_tw_sample.json --manifest comic-video-render-handoff-zh-tw-sample-001 --out samples/comic_video_workbench_zh_tw/comic_video_render_handoff_manifest_zh_tw_sample.json"
```

## Production Package Draft and Validation

```powershell
.\gradlew.bat :app:run --args="create-comic-video-production-package-draft --image-facing-delivery-proof samples/comic_video_workbench_zh_tw/image_facing_delivery_proof_zh_tw_sample.json --voice-timeline-fill samples/comic_video_workbench_zh_tw/voice_timeline_fill_zh_tw_sample.json --package comic-video-production-package-zh-tw-sample-001 --packet comic-video-zh-tw-sample-packet-001 --panel-duration-ms 3000 --out samples/comic_video_workbench_zh_tw/production_package_preview/production_package_draft_zh_tw_sample.json"
.\gradlew.bat :app:run --args="validate-comic-video-production-package --production-package-draft samples/comic_video_workbench_zh_tw/production_package_preview/production_package_draft_zh_tw_sample.json --validation comic-video-production-package-zh-tw-validation-001 --out samples/comic_video_workbench_zh_tw/production_package_preview/production_package_validation_zh_tw_sample.json"
```

Expected result:

- a PASS `ComicVideoProductionPackageDraft`;
- a PASS `ComicVideoProductionPackageValidationReport`;
- 3 storyboard production rows;
- 3 shot prompt drafts;
- 5 asset prompt cards;
- 2 voice style cards;
- 0 cross-reference mismatches;
- 0 prompt safety findings;
- 0 provider/API references;
- 0 author-only leaks;
- 9000 ms total duration;
- `nextAllowedDownstreamStep=comic_video_production_package_validated_for_review`.

This package is local review evidence only. It is not provider media, asset approval, user approval, canon, registry data, a release, or a production lock.

## Local Preview Render

```powershell
.\gradlew.bat :app:run --args="render-comic-video-local --render-handoff-manifest samples/comic_video_workbench_zh_tw/comic_video_render_handoff_manifest_zh_tw_sample.json --out-video out/comic_video_zh_tw/zh_tw_local_render.mp4 --work-dir out/comic_video_zh_tw/work --report-out out/comic_video_zh_tw/zh_tw_local_render_report.json"
```
