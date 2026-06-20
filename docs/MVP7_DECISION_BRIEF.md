# MVP-7 Decision Brief

Date: 2026-06-20

## Historical Status

This is a historical decision brief. It records the state before MVP-7 Candidate A and Candidate A.1 were implemented.

Current status is tracked in `ROADMAP.md`, `PLANS.md`, `docs/PROJECT_COMPLETION_AUDIT.md`, and the MVP-7 audit reports. As of the current handoff refresh, Candidate A, Candidate A.1, Candidate A.2, JSON robustness hardening, and diverse sample reruns are complete and pushed, while Candidate B remains unapproved.

## Verdict

READY_FOR_SPEC_DRAFT, NOT_READY_FOR_IMPLEMENTATION.

At the time this brief was written, MVP-7 was a plausible next product milestone, but it was not approved for implementation. The next safe step then was a specification-only milestone if the user explicitly chose it.

## Why MVP-7 Is Plausible

The stable runtime now covers the main safety and persistence layers through MVP-6:

- local Kotlin/JVM CLI and SQLite runtime,
- multi-turn sessions,
- memory and knowledge boundaries,
- scene, thread, transition, and scheduled event runtime,
- setting pack validation and dependency analysis,
- context budget and relevance guards,
- capability contracts,
- checkpoint and branch timeline runtime.

The original blueprint's remaining product ambition is narrative quality: characters should feel distinct, turns should avoid repetition, and story pressure should emerge without forcing player decisions.

## Historical Non-Approval

At the time this brief was written, MVP-7 was not approved yet.

`ROADMAP.md` and `VERIFY.md` both require a focused `docs/MVP7_*.md` spec before implementation. That spec must define measurable acceptance tests and CLI E2E before any runtime change.

## Candidate MVP-7 Scope For A Future Spec

If the user approves an MVP-7 spec milestone, the spec should decide whether to include:

- Character voice consistency: each active character keeps recognizable diction, priorities, and emotional habits.
- Anti-repetition checks: repeated beat, repeated wording, or repeated stalled response detection.
- Player agency checks: result text must not silently move, agree for, speak for, or decide for the player.
- Scene pacing guidance: pressure can increase through existing scene/thread/world-event evidence, but transition is never forced.
- Narrative density targets: output should be story-readable, not just an acceptance-test report.
- Result quality audit fields: if added, they must support Validator review without allowing AI to bypass Validator.

## Candidate Acceptance Questions

An MVP-7 spec should answer these before code changes:

- What exact failure counts as character toolmanization?
- What exact failure counts as repetition?
- What evidence proves a character acted from memory, belief, emotion, or knowledge rather than director intent?
- How should the system distinguish good quiet tension from a stalled turn?
- Should quality checks be hard Validator failures, soft audit warnings, or both?
- What CLI E2E proves improvement without adding external AI or web services?

## Non-Goals

MVP-7 must not include these unless the roadmap is explicitly changed first:

- Web server, HTTP API, or port listener.
- Web UI or Android UI.
- OpenAI API or external AI calls.
- Rust.
- Docker.
- Embedding or vector database.
- Multi-agent runtime.
- Direct SQLite writes by Codex or AI.
- Validator, `stateVersion`, `contextHash`, or transaction bypass.
- Full setting pack or full database injection into turn context.

## Recommended Next Milestone

If the user wants product progress:

```text
MVP-7 narrative quality spec draft
```

Suggested scope:

- Read `AGENTS.md`, `ROADMAP.md`, `VERIFY.md`, `docs/BLUEPRINT_ALIGNMENT.md`, `docs/PILOT1_FINDINGS.md`, `docs/PILOT1B_COVERAGE_REPAIR.md`, `docs/TURN_PROTOCOL.md`, and `docs/VALIDATION_RULES.md`.
- Create `docs/MVP7_NARRATIVE_QUALITY_SPEC.md`.
- Define exact non-goals, acceptance tests, CLI E2E, and safety boundaries.
- Do not implement Kotlin code in the spec milestone.

If the user wants operational safety instead:

```text
branch strategy review
```

If the user wants more playtesting before MVP-7:

```text
Pilot-2 scenario design brief
```

## Decision Needed

The next milestone should still be chosen explicitly from current evidence. Do not infer Candidate B or any new write-path implementation from the existence of this historical brief.
