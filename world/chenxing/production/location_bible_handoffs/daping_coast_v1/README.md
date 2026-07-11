# Daping Coast Location Bible Foundation Handoff V1

This directory contains the preliminary Location Bible handoff for `loc-daping-coast`.
It is upstream of story Shot generation and does not use or weaken
`schemas/production/shot_queue_v1.schema.json`.

## Current executable task

Exactly one task is initially executable:

```text
daping-lb-01-master-panorama
```

All other tasks are blocked until their exact prerequisite candidates are approved by
the user and returned through Codex intake. No file in this directory is an image,
visual approval, Canon update, production lock, or final-media claim.

## ChatGPT invocation

Use exactly one task per request:

```text
讀取 z72124223/ai-novel-engine，
Commit <handoff-commit-sha>，
交接包 world/chenxing/production/location_bible_handoffs/daping_coast_v1/location_bible_handoff.json，
依 location-bible-handoff-v1 執行 daping-lb-01-master-panorama。
```

GPT must inspect the exact Commit and task first. If the required cinematic-misty
style reference is not attached in the active conversation, it must return
`REFERENCE_UPLOAD_REQUIRED` with this exact Repository path:

```text
world/chenxing/assets/style_reference/chenxing_opening_style_cinematic_misty_v1.png
```

Do not execute all seven tasks together. Do not begin Shot 01–03 generation from
this package.

## Hash identity

`handoffContentSha256` uses UTF-8 compact JSON with recursively sorted object keys
and Unicode preserved. During calculation, both `handoffContentSha256` and
`handoffCommitSha` are set to JSON `null`. The Commit containing the handoff is
provided by the caller and is not self-embedded.

## Files

- `location_bible_handoff.json`: real provider-neutral Daping Coast task graph.
- `reference_upload_checklist.json`: current and later task reference uploads.
- `location_bible_return_template.json`: candidate-only return template for Codex intake.
