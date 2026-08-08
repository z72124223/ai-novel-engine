# Cleanup Inventory Report

Date: 2026-06-25

Status: `UPDATED_AFTER_STANDALONE_RELOCATION`

This report records the cleanup inventory that led to moving the intentional `小說朗讀` tool back to its standalone project location. It does not modify SQLite, change runtime state, edit generated evidence, or approve any automatic cleanup behavior.

## 1. Summary

The project currently has no automatic abandoned-file cleanup. After relocation, growth inside the `AI 劇本` repository is concentrated in ignored reports and local SQLite databases, not in Kotlin/JVM source files.

Largest observed storage groups:

| Path | Files | Size | Notes |
|---|---:|---:|---|
| `out/` | 523 | 29.34 MB | Ignored reports, turn contexts, exported evidence, and generated handoff files. |
| `data/` | 22 | 16.53 MB | Ignored SQLite databases for pilot/e2e runs. |
| `.gradle/` | 19 | 3.18 MB | Gradle cache. Rebuildable. |
| `backups/` | 1 | 0.37 MB | One bundle backup. |
| `app/data/` | 1 | 0.17 MB | Local e2e database. |
| `.kotlin/` | 3 | ~0 MB | Kotlin local cache. |

The intentional standalone tool project now lives outside this repository:

| Path | Files | Size | Notes |
|---|---:|---:|---|
| `C:\Users\f7212\OneDrive\文件\小說朗讀` | 3,790 | 645.63 MB | Canonical standalone `novel-reader-video` tool project. Not cleanup waste. |

## 2. Standalone Tool Weight Source

The largest media weight source is now outside the main repo at `C:\Users\f7212\OneDrive\文件\小說朗讀\output\`.

| Path | Files | Size | Notes |
|---|---:|---:|---|
| `C:\Users\f7212\OneDrive\文件\小說朗讀\output\` | 3,114 | 629.45 MB | Rendered videos, narration audio, subtitles, metadata, images, and logs. |
| `C:\Users\f7212\OneDrive\文件\小說朗讀\tmp\` | 2 | 12.89 MB | Temporary generated entry file and related scratch output. |
| `C:\Users\f7212\OneDrive\文件\小說朗讀\node_modules\` | 522 | 2.91 MB | Reinstallable Node.js dependencies. |
| `C:\Users\f7212\OneDrive\文件\小說朗讀\quarterfull\` | 8 | 0.15 MB | Source text inputs; should not be auto-cleaned. |

Standalone `output/` by file type:

| Extension | Files | Size |
|---|---:|---:|
| `.mp3` | 1,523 | 325.29 MB |
| `.mp4` | 10 | 300.53 MB |
| `.json` | 1,531 | 2.02 MB |
| `.png` | 6 | 0.82 MB |
| `.ass` | 11 | 0.24 MB |
| `.srt` | 11 | 0.21 MB |
| `.jpg` | 4 | 0.18 MB |
| `.log` | 8 | 0.09 MB |
| `.txt` | 10 | 0.07 MB |

Largest individual files are rendered media, including:

| File | Size |
|---|---:|
| `C:\Users\f7212\OneDrive\文件\小說朗讀\output\quarterfull\塵行\02-第二話-根\video.mp4` | 62.52 MB |
| `C:\Users\f7212\OneDrive\文件\小說朗讀\output\quarterfull\塵行\01-第一話-塵\video.mp4` | 48.57 MB |
| `C:\Users\f7212\OneDrive\文件\小說朗讀\output\attachments\第三話-器\video.mp4` | 32.21 MB |
| `C:\Users\f7212\OneDrive\文件\小說朗讀\output\quarterfull\塵行\03-第三話-器\video.mp4` | 32.21 MB |
| `C:\Users\f7212\OneDrive\文件\小說朗讀\output\quarterfull\塵行\02-第二話-根\narration.mp3` | 31.62 MB |

## 3. Candidate Classes

### Safe To Regenerate

These should be eligible for a future cleanup preview by default:

- `.gradle/`
- `.kotlin/`
- `**/build/`
- `C:\Users\f7212\OneDrive\文件\小說朗讀\node_modules\`
- `C:\Users\f7212\OneDrive\文件\小說朗讀\tmp\`
- `*.log`
- `*.tmp`
- `tmp/`
- `temp/`

Reason: these are caches, dependencies, or temporary outputs that can usually be rebuilt or reinstalled.

### Requires Confirmation

These should never be deleted automatically without a preview report and explicit user confirmation:

- `C:\Users\f7212\OneDrive\文件\小說朗讀\output\`
- `out/`
- `data/*.db`
- `data/*.db-shm`
- `data/*.db-wal`
- `app/data/*.db`
- `backups/`
Reason: these may contain generated evidence, completed videos, pilot databases, replay artifacts, backups, or generated media that still matter.

### Do Not Auto-Clean

These must be protected from automatic cleanup:

- `app/src/`
- `app/src/main/resources/migrations/`
- `docs/`
- `samples/`
- `memory/`
- `PLANS.md`
- `AGENTS.md`
- `README.md`
- `DONE_CRITERIA.md`
- `VERIFY.md`
- `C:\Users\f7212\OneDrive\文件\小說朗讀\quarterfull\`
- `C:\Users\f7212\OneDrive\文件\小說朗讀\src\`
- `C:\Users\f7212\OneDrive\文件\小說朗讀\config\`
- `C:\Users\f7212\OneDrive\文件\小說朗讀\package.json`
- `C:\Users\f7212\OneDrive\文件\小說朗讀\package-lock.json`

Reason: these are source, protocol, documentation, memory, fixtures, or source input files.

## 4. Git State Caveat

The worktree already contains modified and untracked files. This report treats them as active user/project work and does not classify them as cleanup candidates.

Observed examples include modified `PLANS.md`, Kotlin source files, docs, new service/test files, and `memory/`.

## 5. Recommended Cleanup Design

Future cleanup should be implemented as a dry-run-first workflow:

1. `cleanup-preview`: list candidates, sizes, last modified times, and cleanup reasons. No deletion.
2. `cleanup-apply`: delete only a selected allowlisted group from a saved preview.
3. `cleanup-report`: write a record of what was deleted, with paths and byte counts.

Default policy:

- `safe`: allow preview, still require confirmation before deletion.
- `confirm`: require explicit selection and confirmation.
- `protected`: never delete through generic cleanup.

The first useful cleanup target outside this repo would be `C:\Users\f7212\OneDrive\文件\小說朗讀\output\`, but only after deciding which rendered `.mp4` files are production keeps and which variant renders can be discarded.

## 6. Verification

Commands used for this report were read-only inventory commands:

- `git status --short --ignored`
- top-level directory listing
- size aggregation for `.gradle`, `.kotlin`, `app/data`, `backups`, `data`, `out`, and the standalone `小說朗讀`
- size aggregation for standalone `output`, `tmp`, `node_modules`, and source-input folders
- large-file listing
- standalone path size check
- 2026-06-25 relocation verification: merge-copy into `C:\Users\f7212\OneDrive\文件\小說朗讀`, 0 missing source files, 0 size mismatches, then removal of `AI 劇本\小說朗讀`

No media cleanup command was executed.
