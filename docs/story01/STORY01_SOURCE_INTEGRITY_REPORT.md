# STORY-01 Source Integrity Report

## Result

`PASS`

The four former World Source #09 failures were caused by checkout line endings, not by a content or Canon disagreement.

## Evidence

- Source: `world/chenxing/raw/09_aijiang_detailed_setting.md`
- Git blob: 3019 bytes
- Git blob / formal manifest SHA-256: `F12334CBAA85EF31AA0250DDEA7189561A34D271E3C230D776F3102B029125DD`
- Previous Windows worktree: 3051 bytes / `425582828EBD99BB648539E71A1010F5F96F9B4B6D8D337B410FCE007BE3B54F`
- Line count: 32
- Difference: 32 CR bytes introduced by `core.autocrlf=true`

The fix adds a path-specific `.gitattributes` rule:

```gitattributes
world/chenxing/raw/09_aijiang_detailed_setting.md text eol=lf
```

No source text and no formal manifest field changed.

## Verification

Focused tests:

```text
WorldSourceArchiveImportCliTest     PASS
WorldSourceArchiveImportServiceTest PASS
WorldSourceQueryCliTest             PASS
```

Official CLI import, using the tracked formal manifest rather than an audit-local copy:

```text
verdict=PASS
status=WORLD_SOURCE_ARCHIVE_SQL_IMPORT_PASS
archiveRowsWritten=1
documentRowsWritten=9
chunkRowsWritten=255
sourceCount=9
totalByteSize=206344
findings=0
```

The classified `query-world-source` command for `愛醬` returned five official-source hits.

## Boundary

This repair makes the existing authority bytes reproducible across Windows checkouts. It does not approve new Canon, rewrite the user's source, summarize raw material, or convert the archive directly into Runtime entities.
