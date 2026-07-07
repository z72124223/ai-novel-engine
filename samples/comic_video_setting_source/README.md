# Comic Video Setting Source Sample

This sample proves the local setting-pack-to-script-draft bridge and the local-preview setting-to-image-facing-proof bridge.

Run from the repository root:

```powershell
.\gradlew.bat :app:run --args="create-setting-script-packet-draft --setting-pack samples/comic_video_setting_source/setting_pack_rain_gate_sample.json --scene scene-rain-gate --packet script-packet-draft-rain-gate-sample-001 --build setting-script-packet-rain-gate-sample-001 --out samples/comic_video_setting_source/script_packet_draft_rain_gate_sample.json --validation-out samples/comic_video_setting_source/script_packet_draft_rain_gate_validation.json --report-out samples/comic_video_setting_source/setting_script_packet_draft_build_report.json"
```

Expected result:

- a candidate `ScriptPacketDraft`;
- a PASS `ScriptPacketDraftValidationReport`;
- a build report that records public source counts and excluded secret count.

To build the local-preview image-facing proof chain:

```powershell
.\gradlew.bat :app:run --args="build-setting-image-facing-proof-preview --setting-pack samples/comic_video_setting_source/setting_pack_rain_gate_sample.json --scene scene-rain-gate --output-dir samples/comic_video_setting_source/image_facing_preview_build --build setting-image-facing-preview-sample-001 --script-packet script-packet-draft-rain-gate-preview-sample-001 --storyboard-brief storyboard-brief-rain-gate-preview-sample-001 --storyboard-draft storyboard-draft-rain-gate-preview-sample-001 --storyboard-review storyboard-review-rain-gate-preview-sample-001 --human-review-preflight storyboard-human-review-preflight-rain-gate-preview-sample-001 --story-movement-audit storyboard-story-movement-audit-rain-gate-preview-sample-001 --reviewable-storyboard reviewable-storyboard-rain-gate-preview-sample-001 --proof image-facing-delivery-proof-rain-gate-preview-sample-001"
```

Expected result:

- a PASS `SettingImageFacingProofPreviewBuildReport`;
- a PASS `ImageFacingDeliveryProof`;
- 3 storyboard frames;
- 3 image-facing frame delivery cards;
- 1 excluded secret.

To build a local preview MP4 from the generated proof plus the editable local voice fill sample:

```powershell
.\gradlew.bat :app:run --args="build-comic-video-local-preview --image-facing-delivery-proof samples/comic_video_setting_source/image_facing_preview_build/image_facing_delivery_proof.json --voice-timeline-fill samples/comic_video_setting_source/voice_timeline_fill_rain_gate_preview_sample.json --output-dir out/comic_video_setting_source_preview --out-video out/comic_video_setting_source_preview/rain_gate_setting_preview.mp4 --build comic-video-setting-rain-gate-build-001 --packet comic-video-setting-rain-gate-preview-001 --validation comic-video-validation-setting-rain-gate-preview-001 --manifest comic-video-render-handoff-setting-rain-gate-preview-001 --render comic-video-setting-rain-gate-local-render-001 --panel-duration-ms 3000 --report-out out/comic_video_setting_source_preview/comic_video_local_preview_build_report.json"
```

Expected result:

- a PASS `ComicVideoLocalPreviewBuildReport`;
- `out/comic_video_setting_source_preview/rain_gate_setting_preview.mp4`;
- 9 seconds of local preview video;
- H.264 video plus AAC audio when `ffmpeg` is available.

To build the same local preview MP4 from the setting pack and voice fill in one command:

```powershell
.\gradlew.bat :app:run --args="build-setting-comic-video-local-preview --setting-pack samples/comic_video_setting_source/setting_pack_rain_gate_sample.json --scene scene-rain-gate --voice-timeline-fill samples/comic_video_setting_source/voice_timeline_fill_rain_gate_preview_sample.json --output-dir out/setting_comic_video_one_command_preview --out-video out/setting_comic_video_one_command_preview/rain_gate_one_command_preview.mp4 --build setting-comic-video-one-command-001 --panel-duration-ms 3000"
```

Expected result:

- a PASS setting image-facing preview report under `out/setting_comic_video_one_command_preview/setting_image_facing_preview/`;
- a PASS comic video local preview report under `out/setting_comic_video_one_command_preview/comic_video_local_preview/`;
- `out/setting_comic_video_one_command_preview/rain_gate_one_command_preview.mp4`;
- 9 seconds of local preview video.

To build the next-version draft production package from the generated proof plus the editable local voice fill sample:

```powershell
.\gradlew.bat :app:run --args="create-comic-video-production-package-draft --image-facing-delivery-proof samples/comic_video_setting_source/image_facing_preview_build/image_facing_delivery_proof.json --voice-timeline-fill samples/comic_video_setting_source/voice_timeline_fill_rain_gate_preview_sample.json --package comic-video-production-package-rain-gate-preview-001 --packet comic-video-setting-rain-gate-preview-001 --panel-duration-ms 3000 --out samples/comic_video_setting_source/production_package_preview/production_package_draft.json"
```

Expected result:

- a PASS `ComicVideoProductionPackageDraft`;
- 3 storyboard production rows;
- 3 shot prompt drafts;
- 5 asset prompt cards;
- 2 voice style cards;
- no provider jobs, submitted prompts, generated media, approvals, canon changes, releases, or production locks.

To validate the draft production package locally:

```powershell
.\gradlew.bat :app:run --args="validate-comic-video-production-package --production-package-draft samples/comic_video_setting_source/production_package_preview/production_package_draft.json --validation comic-video-production-package-rain-gate-validation-001 --out samples/comic_video_setting_source/production_package_preview/production_package_validation.json"
```

Expected result:

- a PASS `ComicVideoProductionPackageValidationReport`;
- 0 cross-reference mismatches;
- 0 prompt safety findings;
- 0 provider/API references;
- 0 forbidden completion/status claims;
- `nextAllowedDownstreamStep=comic_video_production_package_validated_for_review`.

This sample does not create a formal accepted script, human decision, provider media, registry entries, canon changes, releases, final voice acting, final art, or production locks.
