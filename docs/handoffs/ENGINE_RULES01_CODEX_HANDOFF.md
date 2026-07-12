# ENGINE-RULES-01 Codex Handoff

## Role

Codex is the sole Writer and integrator.

Use read-only Subagents for:

- repository architecture；
- semantic dialogue review；
- migration/high-risk review；
- runtime/test evidence。

## Non-Negotiable

- This is an engine implementation package.
- Markdown alone is failure.
- Do not hide semantic limits: model judges meaning, engine owns authority.
- Fail closed.
- Preserve old DIALOGUE-01 artifacts.
- Additive migration only.
- All selected decisions must be Hash-bound and transactional.
- GitHub remote is the team authority.

## Model Routing

- Normal code: Sol Medium；
- Migration/Selection Gate/Transaction: Sol High；
- Semantic reviewers: Sol Medium；
- deterministic checks: scripts/tests；
- Max/Ultra prohibited。

## Final Report

Include:

```text
Base SHA
Final Branch SHA
Default Merge SHA
Migration
Rule Set Version
New services/CLI
Negative fixture results
DIALOGUE-01 re-evaluation
Focused/full tests
High review
Remaining semantic limitations
```
