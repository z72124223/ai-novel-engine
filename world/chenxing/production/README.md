# Chenxing Production Handoffs

This directory stores reproducible text/JSON production handoffs for the Chenxing world.

## Boundary

- It does not replace `world/chenxing/formal/`.
- It does not replace SQLite Runtime or any official CLI validation stage.
- A file existing here is not human approval, Canon, a registered reference, final media, release status, or a production lock.
- Images produced from a handoff remain candidates until explicit user review and the existing External Result, Result Human Review, Candidate Reference Registry, and Candidate Reference Acceptance chain completes.
- Large video files and bulk rejected renders are not committed here by default.
- Locked character turnaround, costume, animal, cover, and style assets remain under their existing `world/chenxing/assets/` paths.

## Reproducible Invocation

A GPT image request identifies:

```text
Repository
Branch
handoff Commit SHA
handoff path
```

The handoff itself records the source Commit/branch and a normalized content SHA-256. It does not require the Commit SHA that contains itself.

Required visual references must also be available as visual attachments in the active ChatGPT conversation. A Repository path alone does not attach an image to the generation tool. Missing required likeness, body, costume, animal-appearance, or location-geometry references produces `REFERENCE_UPLOAD_REQUIRED`.

## Location Bible

Recurring locations should eventually bind a master panorama, top-down plan, four directional views, spatial anchors, important detail views, and a scale reference. A 360 panorama is optional and cannot replace the plan, directional views, or anchor map.

## Current Pilot

The first planned Pilot covers opening Shot 01-03. WP-002A creates only schemas and example placeholders; it does not create the real Shot 01-03 handoff, generate an image, or approve an asset. The real handoff belongs to WP-004 after WP-003 inventories the visual Bible.
