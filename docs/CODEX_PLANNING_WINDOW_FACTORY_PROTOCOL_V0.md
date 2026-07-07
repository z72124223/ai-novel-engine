# Codex Planning Window Factory Protocol V1

Status: `CURRENT_CONTROL_PACKET`

This document defines how the planning group may create bounded new planning
chat windows without letting context grow until the project drifts. It is the
branch-management layer for the product architecture thinking loop.

This is docs/control only. It does not create a live thread by itself, dispatch
implementation, approve canon, approve registry, generate media, call
providers, resume 04/05/06/07/08, resume Phase 20 / Phase 21, or claim
production lock.

## Purpose

Large planning topics should not stay inside one ever-growing `A組02` or
`A組04` conversation. When a subtopic becomes large, `A組02` / `A組03` /
`A組04` should create a bounded branch question with a strict output format and
stop condition.

Branch windows are for planning research and architecture refinement only. They
do not edit implementation files, do not assign work to `B組01`, `B組02`, or
`B組03`, and do not become hidden implementation lanes.

## When To Spawn A Planning Branch

Use `SPAWN_REQUEST` when any of these is true:

- a subtopic needs more than one answer path;
- the discussion is starting to change the original question;
- context is becoming too large to track safely;
- the topic needs a specialist lens, such as character assets, clothing
  modules, scene topology, camera grammar, voice timeline, query scope, or
  approval gates;
- `A組03` finds a risk that needs its own solution search;
- `A組04` cannot decide maturity without focused evidence.

Do not spawn a branch for tiny clarification tasks.

## Spawn Authority

- `A組02` or `A組03` may propose `SPAWN_REQUEST`.
- `A組04` may refine the request and recommend opening a branch.
- `A組01` checks whether the branch fits blueprint direction and may approve
  routine bounded planning branch creation under the AI-governed decision lane.
- `00` / user is needed only when the branch changes product direction, needs
  new user worldbuilding content, needs budget/provider/media/canon/registry/
  release approval, or crosses another stop condition.
- `B組01` may be informed only if the branch result could later affect an
  implementation package.
- Branch windows must report back to `A組04` / `A組01`, not to `B組01`,
  `B組02`, or `B組03`.

## SPAWN_REQUEST Shape

```text
SPAWN_REQUEST
Requested branch group-number/title:
Parent planning topic:
Original user goal:
Why this cannot stay in the parent window:
Questions to answer:
- 1.
- 2.
- 3.
Evidence allowed:
Files not allowed:
Output shape:
Stop conditions:
Forbidden work:
Return target:
Expected useful lifetime:
Status: REQUESTED
```

The request should be short enough that a new window can start without reading
the whole parent conversation.

## Branch Window Prompt Shape

When `A組01` or `00` approves a branch under the decision rules, the new
planning window should receive a prompt like:

```text
You are a planning branch window for AI劇本.

Branch group-number/title:
Parent topic:
Original user goal:
Your role:
Questions to answer:
Evidence allowed:
Files not allowed:
Output required:
Forbidden work:
Stop conditions:
Report back to:

Do not implement. Do not assign B組01 / B組02 / B組03. Do not claim approval,
canon, registry, release, final media approval, or production lock.
```

## BRANCH_REPORT Shape

Every branch must return a concise report:

```text
BRANCH_REPORT
Branch name:
Question answered:
Evidence used:
Fixed core candidates:
Variable module candidates:
Composition rule candidates:
Recommended architecture:
Risks:
What should be rejected:
What needs human decision:
Suggested next planning step:
Implementation-ready: yes | no
Status: PASS | NEEDS_MORE_PLANNING | BLOCKED
```

`Implementation-ready: yes` means only that `A組04` and `A組01` may consider
the result in an architecture proposal. It does not authorize `B組01` to
dispatch implementation.

## Branch Lifecycle

1. `A組02` / `A組03` / `A組04` identifies the need.
2. `A組04` writes or approves `SPAWN_REQUEST`.
3. `A組01` checks route fit.
4. `A組01` approves routine bounded branches through the AI-governed decision
   lane, or returns to `00` / user only for stop-condition decisions.
5. A new planning branch window answers the narrow question.
6. The branch returns `BRANCH_REPORT`.
7. `A組04` integrates the result into the main planning loop.
8. `A組01` integrates the proposal into blueprint/control, loops it, or returns
   it to `00` only when user-level direction is required.
9. Only after `A組01` accepts a mature package through the AI technical
   approval gate may it be handed to `B組01 派工`.

Branch windows should be archived or left idle after reporting. They should not
continue as general-purpose project workers.

## Context Drift Controls

A branch must stop when:

- it answers a different question than the request;
- it needs forbidden work to continue;
- it needs user/worldbuilding input;
- it needs provider/API, external paid tools, media generation, or budget
  approval;
- it needs canon, registry, release, final media approval, or production lock;
- it cannot produce fixed core / variable modules / composition rules;
- it produces no new evidence after one loop;
- its report would exceed the requested output shape.

If a branch discovers another large topic, it should propose a new
`SPAWN_REQUEST`, not continue expanding.

## Example: Character Asset Branch

Bad parent-window drift:

- keep discussing "make character art" until it becomes character identity,
  clothing variants, image prompts, asset approval, and video production all in
  one thread.

Better branch:

```text
SPAWN_REQUEST
Requested branch group-number/title: A組06 分支角色資產分層
Parent planning topic: reusable character asset system
Original user goal: produce consistent voiced-comic/video characters
Why this cannot stay in the parent window:
- the topic splits identity, body, costume, expression, pose, and approval gates.
Questions to answer:
- What is the fixed character core?
- Which modules can vary safely?
- What composition rules prevent identity drift?
Output shape: BRANCH_REPORT
Forbidden work:
- no image generation, no provider/API, no asset approval, no registry approval.
Return target: A組04 綜合分支 / A組01 整合規劃
Expected useful lifetime: one report
Status: REQUESTED
```

## Relationship To Implementation

Planning branches may generate ideas, architecture, risk lists, or proposal
shapes. They must not directly produce Kotlin, CLI, sample JSON, provider
outputs, storyboard images, audio, video, registry data, or SQLite state.

Only after `A組01` accepts an `IMPLEMENTATION_PACKAGE_CANDIDATE` through the AI
technical approval gate, or `00` / user explicitly redirects it, may
`B組01 派工` create an implementation task. The implementation group remains the
single-lane implementation pipeline.

## Hard Boundaries

- No provider/API.
- No web/server/open ports.
- No SQLite direct writes.
- No schema/migration changes.
- No renderer/ffmpeg/TTS/media generation.
- No 04/05/06/07/08 restart.
- No Phase 20 / Phase 21 restart.
- No canon, registry, release, approval, final media approval, or production
  lock claims.
- No staging, commit, or push unless the user explicitly asks.
