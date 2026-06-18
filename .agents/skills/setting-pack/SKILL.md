---
name: setting-pack
description: Create, validate, preview, apply, export, and report local setting pack JSON for this repo's Kotlin/JVM interactive fiction engine. Use when Codex must turn author-provided worldbuilding into a setting pack and run only the official CLI pipeline: validate-setting-pack, preview-setting-import, apply-setting-pack, export-setting-pack, show-dependencies, and validate-project-canon.
---

# setting-pack

Use this skill to prepare one setting pack safely for the local interactive fiction engine.

## Guardrails

- Use only the official Gradle CLI commands in this repo.
- Do not write SQLite directly with SQL, `sqlite3`, JDBC snippets, scripts, Python, Kotlin scratch code, or DB tools.
- Do not add network, API, server, port, Docker, Rust, Android, web UI, embedding, vector database, or multi-agent behavior.
- Do not use the demo project for Pilot work.
- Do not put `author_only` secrets into character starting knowledge or character-facing context.
- Every character that can enter the opening scene must include a valid `initialRuntimeTemplate`.
- Do not put author-only secrets, unrevealed secret content, or story-thread `directorGoal` into `initialRuntimeTemplate`.
- Do not silently patch canon while a session is active. Treat active-session impact blockers as hard stops.
- Generate or edit only setting pack JSON until the author explicitly allows apply.

## Required Inputs

Identify these before working:

- DB path.
- Setting pack working directory.
- Author-provided setting template or worldbuilding source.
- Target `projectId`, `packId`, `packVersion`, and `mode`.

If the author template is missing, stop and ask for it.

## Workflow

Run these steps in order.

1. Read repo rules:

- `AGENTS.md`
- `docs/SETTING_PACKS_AND_DEPENDENCIES.md`
- `docs/VALIDATION_RULES.md`

2. Create or edit the setting pack JSON in the working directory. Keep IDs stable and ASCII-safe. Include only canon required by the author's template.

For opening-scene characters, include:

```json
"initialRuntimeTemplate": {
  "initialLocationId": "loc-id",
  "initialGoal": "character-local goal, not a director goal",
  "dominantEmotion": "emotion",
  "emotionState": {"emotion": 50},
  "availableActions": ["action visible to this character"],
  "relationshipDefaults": {"player": "unknown"},
  "initiativeLevel": 40
}
```

3. Validate the pack:

```powershell
.\gradlew.bat run --no-daemon --args="validate-setting-pack --db <db> --file <packJson> --out <validationOut>"
```

If validation fails, edit only the setting pack JSON and rerun validation.

4. Preview import:

```powershell
.\gradlew.bat run --no-daemon --args="preview-setting-import --db <db> --file <packJson> --out <previewOut>"
```

Review diff entries, impact paths, warnings, and blockers. Do not apply when blockers exist.

5. Apply only after validation and preview pass and the author has approved applying:

```powershell
.\gradlew.bat run --no-daemon --args="apply-setting-pack --db <db> --file <packJson> --out <applyOut>"
```

6. Validate canon:

```powershell
.\gradlew.bat run --no-daemon --args="validate-project-canon --db <db> --project <projectId> --out <canonValidationOut>"
```

7. Export and validate round-trip:

```powershell
.\gradlew.bat run --no-daemon --args="export-setting-pack --db <db> --project <projectId> --pack <packId> --version <packVersion> --out <exportJson>"
.\gradlew.bat run --no-daemon --args="validate-setting-pack --db <db> --file <exportJson> --out <exportValidationOut>"
```

8. Inspect dependency paths with the official CLI when needed:

```powershell
.\gradlew.bat run --no-daemon --args="show-dependencies --db <db> --project <projectId> --entity-type <type> --entity-id <id>"
```

## Report

Report:

- pack path and project ID
- validate / preview / apply / export status
- entity and relation counts
- changed revisions
- dependency paths checked
- active-session blockers, if any
- confirmation that no direct SQLite writes were used
