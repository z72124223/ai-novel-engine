# Blueprint Alignment

Date: 2026-06-20

Source reviewed:

```text
C:\Users\f7212\Downloads\AI_story_engine_blueprint.md
```

## Verdict

KEEP_AS_DESIGN_INTENT_NOT_CURRENT_STATE.

The blueprint is valuable as a product-intent and safety-boundary document. It should not be used as the current project status because several operational notes are now stale.

## Confirmed Current Anchors

- Current stable branch: `codex/mvp6-checkpoint-branching-spec`.
- Remote: `origin https://github.com/z72124223/ai-novel-engine.git`.
- Release tag: `mvp-6`.
- `mvp-6` points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- `ROADMAP.md` records approved MVP-7 work so far as read-only narrative quality audit milestones through Candidate A.2 and sample reruns.
- Candidate B, pre-resolve gates, MVP-8, and runtime write-path integration remain unapproved.
- `VERIFY.md` defines the read-only `audit-narrative-quality` verification path and explicit UTF-8 JSON report parsing for PowerShell 5.1.
- `docs/HANDOFF.md` is the current repo handoff index for a new Codex window.

## Blueprint Parts To Preserve

These principles match the current project architecture and should remain active:

- SQLite remains the source of truth.
- AI produces JSON proposals only; it does not directly write SQLite.
- Validator and `resolve-turn` govern formal state mutation.
- `stateVersion`, `contextHash`, and transaction boundaries must not be bypassed.
- Context should contain only turn-relevant information, not the whole database, whole setting pack, or full dependency graph.
- Forbidden secrets must not be revealed before the runtime allows them.
- Characters must act from their own goals, emotions, memories, knowledge, and available actions, not from story-thread director goals.
- Referenced techniques, items, and weapons require capability contracts, not name or description alone.
- Setting packs must go through parse, validate, diff, impact preview, and transaction apply.
- Context hard max remains `24576` actual UTF-8 bytes.

## Blueprint Parts That Are Stale

These notes were true or useful when the blueprint was written, but they must not drive current work:

- The blueprint describes Pilot-1 as still near Turn 10/11. Current repo docs record Pilot-1 and Pilot-1b repair work as completed milestones.
- The blueprint says MVP-6 is not started. Current Git evidence shows `mvp-6` exists and points to `be527031335efe8e2b0facae0145e1bbc084f8c6`.
- The blueprint recommends running a Turn 11 prompt. That is obsolete and must not be used as a current operational instruction.
- The blueprint treats legal scene transition and scheduled world event coverage as unresolved Pilot-1 gaps. Current roadmap records Pilot-1b Coverage Repair as completed.
- The blueprint lists installDist packaging as future or unresolved technical debt by implication. Current roadmap and handoff state that packaging repair is complete.
- The blueprint lists MVP-8 Android APK as a later concept. Android UI remains explicitly outside the approved scope.

## Current Source Of Truth

When the external blueprint conflicts with repo files, use these repo files first:

1. `AGENTS.md`
2. `docs/HANDOFF.md`
3. `ROADMAP.md`
4. `DONE_CRITERIA.md`
5. `VERIFY.md`
6. `docs/ROADMAP.md`
7. `docs/TURN_PROTOCOL.md`
8. `docs/VALIDATION_RULES.md`
9. `docs/SETTING_PACKS_AND_DEPENDENCIES.md`
10. `docs/MEMORY_AND_KNOWLEDGE.md`
11. `docs/SCENE_THREAD_RUNTIME.md`
12. `docs/PROJECT_COMPLETION_AUDIT.md`

## Safe Use In Future Work

Use the blueprint to answer:

- What kind of engine is this supposed to become?
- What safety boundaries must remain stable?
- Why does the system prefer CLI, SQLite, JSON, Validator, and transaction-controlled state writes?
- What product direction was originally imagined after the stable runtime?

Do not use the blueprint to answer:

- Which turn number is current.
- Which branch is current.
- Whether MVP-6 is complete.
- Whether Pilot-1 is still running.
- Whether Candidate B, MVP-8, or any new write-path integration is approved.

## Drift Guardrail

Before starting any new milestone, read current repo docs and Git state first. If a blueprint claim is not confirmed by repo files or command output, write it as a hypothesis, not as a fact.
