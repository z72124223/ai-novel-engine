# Comic Video Workbench CLI Workflow

Status: `IMPLEMENTED_LOCAL_JSON_WORKBENCH`

This document describes the current local comic video workbench. It is a Kotlin/JVM CLI plus JSON file protocol workflow. It produces metadata packets, validation reports, and a render handoff manifest. It does not generate images, audio, subtitles as files, MP4 output, provider jobs, sidecar renderer output, registry entries, canon changes, approvals, releases, or production locks.

Current-phase completion report: `docs/COMIC_VIDEO_WORKBENCH_CURRENT_PHASE_COMPLETION.md`.

## Boundary

Current allowed output:

- `ComicVideoPacket` JSON.
- `ComicVideoVoiceTimelineFill` JSON supplied by the author/Codex as metadata.
- `ComicVideoPacketValidationReport` JSON.
- `ComicVideoRenderHandoffManifest` JSON.

Current forbidden output:

- generated panel image files;
- generated voice or narration audio files;
- rendered video files;
- remote provider URLs or job ids;
- sidecar renderer execution;
- SQLite writes;
- formal asset approval, canon application, registry write, release, or production lock.

## Entry Point

Start after the existing upstream pipeline has produced a PASS `ImageFacingDeliveryProof` JSON:

```text
accepted script/storyboard pipeline
  -> create-image-facing-delivery-proof
  -> PASS ImageFacingDeliveryProof JSON
  -> comic video workbench workflow
```

The current end-to-end CLI fixture proves this downstream chain:

```text
ImageFacingDeliveryProof JSON
  -> create-comic-video-packet
  -> create-comic-video-voice-timeline-fill-template
  -> fill-comic-video-voice-timeline
  -> validate-comic-video-packet
  -> create-comic-video-render-handoff-manifest
  -> PASS metadata-only render handoff manifest
```

## Commands

Run commands from the repository root.

### 1. Create the Packet

```powershell
.\gradlew.bat :app:run --args="create-comic-video-packet --image-facing-delivery-proof out/comic_video/image_facing_delivery_proof.json --packet comic-video-packet-001 --panel-duration-ms 3000 --out out/comic_video/comic_video_packet.json"
```

Expected result:

- the packet has panel tracks copied from the image-facing proof;
- `voiceCast`, `audioCues`, and `subtitleCues` are still empty;
- `nextAllowedDownstreamStep` remains `comic_video_packet_validation`;
- validation is still required.

### 2. Create the Voice Timeline Fill Template

```powershell
.\gradlew.bat :app:run --args="create-comic-video-voice-timeline-fill-template --comic-video-packet out/comic_video/comic_video_packet.json --fill voice-timeline-fill-001 --voice-id voice-protagonist --character-id char-protagonist --display-name Protagonist --voice-label local-manual-voice-protagonist --out out/comic_video/voice_timeline_fill_template.json"
```

Expected result:

- the template uses exact panel ids from `comic_video_packet.json`;
- one audio cue and one subtitle cue are created for each panel track;
- cue timings are derived from panel `displayDurationMillis`;
- media/provider/output fields stay blank;
- the output is still an editable metadata template.

### 3. Edit the Voice Timeline Fill

Copy or rename the template to `out/comic_video/voice_timeline_fill.json`, then replace the placeholder spoken and subtitle text.

A minimal edited fill looks like this:

```json
{
  "fillId": "voice-timeline-fill-001",
  "sourceComicVideoPacketId": "comic-video-packet-001",
  "fillStatus": "comic_video_voice_timeline_fill_candidate",
  "voiceCast": [
    {
      "voiceId": "voice-protagonist",
      "characterId": "char-protagonist",
      "displayName": "Protagonist",
      "voiceLabel": "local-manual-voice-protagonist",
      "voiceBoundary": "voice_cast_plan_only",
      "notes": "Manual voice plan only. Keep media fields blank."
    }
  ],
  "audioCues": [
    {
      "cueId": "audio-001",
      "panelId": "panel-frame-001",
      "voiceId": "voice-protagonist",
      "characterId": "char-protagonist",
      "text": "Replace with spoken line.",
      "startMillis": 0,
      "durationMillis": 3000,
      "audioAssetPath": "",
      "remoteAudioUrl": "",
      "providerJobId": "",
      "generatedStatus": ""
    }
  ],
  "subtitleCues": [
    {
      "cueId": "subtitle-001",
      "panelId": "panel-frame-001",
      "audioCueId": "audio-001",
      "text": "Replace with subtitle text.",
      "startMillis": 0,
      "endMillis": 3000
    }
  ],
  "timing": {
    "timelineStatus": "metadata_only_timing_plan",
    "totalDurationMillis": 3000,
    "frameRate": "24",
    "resolution": "1920x1080",
    "backgroundPolicy": "panel_track_only",
    "outputVideoPath": "",
    "renderedVideoPath": ""
  },
  "boundaryChecklist": [
    "metadata_only",
    "manual_voice_plan",
    "media_execution_absent",
    "remote_payload_absent"
  ],
  "notes": [
    "Local JSON metadata only."
  ]
}
```

Keep `sourceComicVideoPacketId` equal to the packet id. Do not add audio paths, generated media status, remote URLs, or provider job ids.

### 4. Fill the Voice Timeline

```powershell
.\gradlew.bat :app:run --args="fill-comic-video-voice-timeline --comic-video-packet out/comic_video/comic_video_packet.json --voice-timeline-fill out/comic_video/voice_timeline_fill.json --out out/comic_video/comic_video_packet_filled.json"
```

Expected result:

- panel/source bindings stay intact;
- voice, audio, subtitle, and timing metadata are copied into the packet;
- the filled packet is still a candidate and still needs validation.

### 5. Validate the Packet

```powershell
.\gradlew.bat :app:run --args="validate-comic-video-packet --comic-video-packet out/comic_video/comic_video_packet_filled.json --validation comic-video-validation-001 --out out/comic_video/comic_video_packet_validation.json"
```

PASS requirements include:

- non-empty panel tracks;
- non-empty voice cast, audio cues, subtitle cues, and timing;
- valid cue references between panels, voices, audio, and subtitles;
- no non-empty generated media output values;
- no non-empty remote/provider values;
- no forbidden final-output, approval, canon, registry, release, or production-lock claims.

On PASS, `nextAllowedDownstreamStep` is `comic_video_packet_validated`.

### 6. Create the Render Handoff Manifest

```powershell
.\gradlew.bat :app:run --args="create-comic-video-render-handoff-manifest --comic-video-packet-validation out/comic_video/comic_video_packet_validation.json --manifest comic-video-render-handoff-001 --out out/comic_video/comic_video_render_handoff_manifest.json"
```

Expected result:

- `verdict` is `PASS`;
- `manifestStatus` is `comic_video_render_handoff_manifest_candidate`;
- panel timeline items use `external_panel_image_required`;
- voice timeline items use `external_audio_required`;
- the manifest remains metadata-only and does not contain media file paths, remote URLs, provider ids, or rendered video paths.

## Operator Checklist

Before handing the manifest to any future manual renderer or sidecar, confirm:

- the validation report is PASS;
- the manifest was created from the PASS validation report, not from an unvalidated packet;
- every spoken cue has a subtitle cue when subtitles are expected;
- panel ids in voice/subtitle cues match panel ids in the packet;
- no field contains non-empty generated media output, provider job ids, remote URLs, or final render paths;
- all output remains JSON metadata.

## Evidence

Current project evidence:

- `Main.kt` help text exposes the five workbench CLI commands.
- `ComicVideoWorkbenchPipelineFixtureTest` runs the full CLI JSON chain with temp files.
- Focused `ComicVideo*` tests and full Gradle tests passed for the implementation slice that introduced this workflow.

This means the local workbench can prepare and validate a metadata-only comic video handoff. It does not mean the project has produced final media.
