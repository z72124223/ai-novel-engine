# Project Directory And Subproject Register

Status: `LOCAL_STANDALONE_TOOL_REGISTERED`

Date: 2026-06-25

This register records local project folders and moved subprojects that future Codex windows need to locate. It is docs-only directory memory. It is not runtime data, not a setting pack, not SQLite data, not approved production assets, and not permission to start new generation or rendering work.

## 1. Verdict

`小說朗讀` is registered as a canonical standalone local tool project outside the `AI 劇本` workspace:

```text
C:\Users\f7212\OneDrive\文件\小說朗讀
```

The previous in-repo location was merge-copied into the standalone project and then removed:

```text
C:\Users\f7212\OneDrive\文件\AI 劇本\小說朗讀
```

Future work should treat `C:\Users\f7212\OneDrive\文件\小說朗讀` as the formal working location. Do not recreate `AI 劇本\小說朗讀`, write SQLite, patch setting packs, or alter runtime state because of this registration.

## 2. Register Schema

Each local subproject entry should record:

| Field | Meaning |
| --- | --- |
| `id` | Stable local register id. |
| `name` | Human-facing project name. |
| `kind` | Local project type or tooling role. |
| `canonicalPath` | Formal current working location. |
| `previousPath` | Previous path, if relevant. |
| `status` | Current directory/register status. |
| `summary` | Short project purpose. |
| `keyPaths` | Important files or folders future windows may need. |
| `outputs` | Known generated outputs that should be discoverable. |
| `verification` | Evidence for relocation or output checks. |
| `boundaries` | What this registration does not approve. |
| `nextAction` | Safe next action or cleanup note. |

## 3. Local Subproject Entries

### `subproject-novel-reader-video-2026-06-24`

| Field | Value |
| --- | --- |
| `name` | `小說朗讀` |
| `kind` | Local Node.js CLI subproject |
| `package` | `novel-reader-video` |
| `canonicalPath` | `C:\Users\f7212\OneDrive\文件\小說朗讀` |
| `previousPath` | `C:\Users\f7212\OneDrive\文件\AI 劇本\小說朗讀` |
| `status` | `standalone_location_restored`; previous in-repo path removed after merge-copy verification |
| `summary` | Converts novel text into narrated videos with stable character voices and aligned subtitles. |
| `source` | `src/` |
| `config` | `config/default.config.json` |
| `QuarterFullInput` | `quarterfull/塵行/` |
| `verification` | 2026-06-25 merge-copy verification recorded 3790 files and 676,992,556 bytes at the standalone path, with 0 missing source files and 0 size mismatches before the in-repo copy was removed. User also reported all three MP4 outputs were rechecked with `ffprobe` as H.264 1920x1080 video plus AAC audio. |
| `gitBoundary` | Folder currently contains its own `.git`; do not assume submodule, subtree, ignored folder, or tracked source policy until a separate Git hygiene decision is made. |

Known completed video outputs:

- `output/quarterfull/塵行/01-第一話-塵/video.mp4`
- `output/quarterfull/塵行/02-第二話-根/video.mp4`
- `output/quarterfull/塵行/03-第三話-器/video.mp4`

Boundary:

- This is not part of the Kotlin/JVM story runtime.
- This is not a web server, port listener, external AI API integration, storyboard generator, Oiioii integration, Seedance integration, or production asset registry.
- The completed MP4 files are discoverable outputs, not approved production assets or setting-pack data.
- The standalone output folder may be large, but its files are intentional generated media and must not be cleaned without preview and explicit approval.

Next safe action:

- Use `C:\Users\f7212\OneDrive\文件\小說朗讀` for future `小說朗讀` work.
- If cleanup is requested later, preview `output/`, `tmp/`, and `node_modules/` inside the standalone project rather than treating the tool itself as waste.

## 4. Relationship To Existing Registers

`docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md` remains the planning register for non-canon world/system blueprint candidates. `小說朗讀` is a local tool subproject, so it is indexed here and cross-referenced from the planning register only as local directory infrastructure, not as a blueprint candidate.

`memory/project_directory_memory.json` stores the compact local-memory version of this registration for future Codex windows.
