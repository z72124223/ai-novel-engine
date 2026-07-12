# STORY-01 Step 0 — Source Integrity

## Changed files

- `.gitattributes`
- `docs/story01/STORY01_SOURCE_INTEGRITY_REPORT.md`
- `docs/development_journal/story01/STEP_0_SOURCE_INTEGRITY.md`
- `PLANS.md`

## Why

The formal World Source import was blocked because Windows checkout changed one 32-line raw Markdown file from LF to CRLF. The Git blob and formal manifest already agreed; the worktree representation did not.

## Evidence and verification

- Git blob size: 3019 bytes.
- LF worktree SHA-256 matches the formal manifest.
- Focused World Source import/query tests pass.
- Official import writes 1 archive, 9 documents and 255 chunks.
- Official query returns five classified 愛醬 hits.

## Drift check

Aligned with `STORY01_REAL_CHAPTER_PRODUCTION_TEST`. No Kotlin, migration, source text, manifest, Canon, image or provider file changed.
