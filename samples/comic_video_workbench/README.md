# Comic Video Workbench Sample Pack

Status: `LOCAL_JSON_SAMPLE_PACK`

This sample pack demonstrates the current metadata-only comic video workbench. It does not contain generated images, audio files, subtitle files, MP4 output, provider/API payloads, sidecar output, approval, canon, registry, release, or production-lock artifacts.

## Files

- `image_facing_delivery_proof_sample.json`: PASS source proof used as the workflow entry point.
- `comic_video_packet_sample.json`: packet produced from the source proof.
- `voice_timeline_fill_template_sample.json`: editable fill template produced from the packet.
- `voice_timeline_fill_sample.json`: edited voice/audio/subtitle metadata.
- `comic_video_packet_filled_sample.json`: packet after applying the edited fill.
- `comic_video_packet_validation_sample.json`: PASS validation report.
- `comic_video_render_handoff_manifest_sample.json`: PASS metadata-only handoff manifest.

## Rebuild

Run from the repository root:

```powershell
.\gradlew.bat :app:run --args="create-comic-video-packet --image-facing-delivery-proof samples/comic_video_workbench/image_facing_delivery_proof_sample.json --packet comic-video-sample-packet-001 --panel-duration-ms 2600 --out samples/comic_video_workbench/comic_video_packet_sample.json"
.\gradlew.bat :app:run --args="create-comic-video-voice-timeline-fill-template --comic-video-packet samples/comic_video_workbench/comic_video_packet_sample.json --fill voice-timeline-fill-template-sample-001 --voice-id voice-lin --character-id char-lin --display-name Lin --voice-label local-lin-clear --out samples/comic_video_workbench/voice_timeline_fill_template_sample.json"
.\gradlew.bat :app:run --args="fill-comic-video-voice-timeline --comic-video-packet samples/comic_video_workbench/comic_video_packet_sample.json --voice-timeline-fill samples/comic_video_workbench/voice_timeline_fill_sample.json --out samples/comic_video_workbench/comic_video_packet_filled_sample.json"
.\gradlew.bat :app:run --args="validate-comic-video-packet --comic-video-packet samples/comic_video_workbench/comic_video_packet_filled_sample.json --validation comic-video-validation-sample-001 --out samples/comic_video_workbench/comic_video_packet_validation_sample.json"
.\gradlew.bat :app:run --args="create-comic-video-render-handoff-manifest --comic-video-packet-validation samples/comic_video_workbench/comic_video_packet_validation_sample.json --manifest comic-video-render-handoff-sample-001 --out samples/comic_video_workbench/comic_video_render_handoff_manifest_sample.json"
```

The committed sample outputs should remain metadata-only and PASS validation.
