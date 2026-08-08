# WP-001F-1 Local-Only File Evidence

## Exact local-only status entries

1. `docs/PROJECT_PROGRESS_RUBRIC.md`
2. `memory/` (one directory status entry containing two local JSON files)

No exact remote file path exists for either status entry. The absence of a remote path is not treated as proof that the content should be discarded.

## `docs/PROJECT_PROGRESS_RUBRIC.md`

| Field | Evidence |
|---|---|
| Working-tree hash | `ef7d854b438a61472e1d792abb852424e6a8382c` |
| Lines | 82 |
| Remote blob / last commit | None; exact path not found in remote tree |
| Local `HEAD` blob | None; untracked file |
| Production import | None found |
| Test import | None found |
| CLI entry | None found |
| Related docs | `docs/PROJECT_COMPLETION_PROGRESS.md`, `PLANS.md` |
| Migration/table | None found |
| Independent compile | Not applicable; Markdown control document |
| Generated/temp evidence | None found |
| Status | `LOCAL_HAS_UNIQUE_VALUE` but completion/control authority is unverified |
| Recommendation | `NEEDS_MORE_EVIDENCE`, medium confidence |

The file defines a 58% product-progress and 79% gate-hardening distinction. The local completion document and planning history reference it, but the remote completion-control document has a different current-phase model. This is a governance-document reconciliation question, not a runtime feature.

## `memory/`

The status entry is a directory. The two local files are:

| Local file | Hash | Lines | Production import | Test import | CLI / migration |
|---|---|---:|---|---|---|
| `memory/project_directory_memory.json` | `3f4f34b5993904add53b23756608c1981ea54894` | 68 | None found | None found | None / none |
| `memory/quarterfull_world_memory_capsule.json` | `e5bb3e256cbe735e0c059db83dd528de7cbe3095` | 288 | None found | None found | None / none |

Related local documentation includes `docs/QUARTERFULL_WORLD_ALIGNMENT.md`, `docs/PROJECT_DIRECTORY_AND_SUBPROJECT_REGISTER.md`, `docs/BLUEPRINT_DATA_LAYER_AND_PLANNING_REGISTER.md` and `PLANS.md`. The files are local support data, not formal SQLite state, setting-pack canon or an approved production asset registry. No exact local filename reference was found in production Kotlin or tests.

- Generated/temp evidence: none. They are authored/support JSON files, not disposable build artifacts.
- Independent compile: not applicable; JSON data only.
- Formal runtime dependency: none found.
- Status: `LOCAL_HAS_UNIQUE_VALUE`, with formal authority still unverified.
- Recommendation: `NEEDS_MORE_EVIDENCE`, low confidence.

## Safety conclusion

Neither local-only entry is classified `SAFE_TO_DROP_LOCAL_COPY`, `DROP_GENERATED_COPY` or `REMOTE_SUPERSEDES_LOCAL`. No delete, move, cleanup or content rewrite was performed.
