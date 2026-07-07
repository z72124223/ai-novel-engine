# Codex Product Architecture Thinking Loop V1

Status: `CURRENT_CONTROL_PACKET`

This document defines the planning-side thinking loop for the AI screenplay /
voiced-comic project. It exists because the project needs more than fast task
delivery. The planning group must keep discovering reusable product
architecture, then pass only mature proposals to the implementation group.

This is docs/control only. It does not create implementation work, approve
canon, approve assets, approve registry data, generate media, call providers,
resume 04/05/06/07/08, resume Phase 20 / Phase 21, or claim production lock.

## Core Idea

The planning group should not stop at the first workable answer.

For example, a shallow asset plan says:

- make a character image with clothes already fixed.

A deeper product architecture plan asks:

- what is the fixed core of the character?
- what should be variable modules?
- what rules keep the modules compatible?

That can produce a reusable system:

- fixed core: identity, face family, body proportions, skin tone, scars,
  silhouette, personality anchors;
- variable modules: hair, expression, clothing, accessories, props, pose,
  injury state, lighting, scene context;
- composition rules: what can be swapped, what must stay stable, what needs
  review, what cannot be shown, and what breaks character continuity.

The purpose of this loop is to turn one-off outputs into reusable production
systems.

## Evolution System Priority

The planning loop must also make the project stronger after failures.

`docs/CODEX_A_GROUP_EVOLUTION_SYSTEM_V0.md` is the priority control packet for
this behavior. When a stall, manual wakeup, missed handoff, false stop, unclear
route authority, or repeated user correction appears, A-group must open an
`EVOLUTION_EVENT` before continuing ordinary feature-route planning.

An evolution cycle must produce:

- `ROOT_CAUSE_ANALYSIS`: why the system failed or stalled;
- `RULE_UPGRADE_PROPOSAL`: what rule, prompt, workflow, or blueprint section
  must change;
- `REGRESSION_CHECK`: the simple future check that proves the same issue will
  be routed correctly next time.

This evolution cycle does not authorize implementation. `A組01` must still use
the AI-governed decision lane before handing any mature package to `B組01`.

`docs/CODEX_A_GROUP_PROACTIVE_EVOLUTION_HEARTBEAT_RULE_SET_V0.md` adds the
proactive heartbeat layer for this thinking loop. A01 must not close with
acknowledgement-only text when a manual wakeup, missed handoff, repeated PASS
loop, stale package risk, ignored latest package, or system-not-getting-stronger
signal appears, except for explicit `CONFIG_UPDATE_ONLY`, update-only, or
do-not-start-work messages.

When A01 starts heartbeat work, the only bounded loop is `A02 -> A03 -> A04`.
The loop must produce a product-distance finding, freshness decision, rule
patch, or blocker before A01 decides whether a mature package may go to B01.
The `Product-Distance Ledger` must cite the generated video / 有聲漫畫 chain
being protected: character assets, scene, storyboard/camera, prompt/asset card,
voice/subtitle/SFX, or renderer/package QA.

The `Latest-Package Freshness Check` is a checklist field inside A01/B01
classification, not a new status-only document. It prevents stale-package
replay and does not authorize provider/API, SQLite, renderer/ffmpeg/TTS,
media generation, approval, canon, release, or production lock.

## Group A Roles

### 00 - 總控決策

- Owns user communication and product direction decisions.
- Does not serve as the routine manual approval gate for mature technical
  planning proposals.
- Receives only stop conditions, product-direction choices, or decisions that
  truly require the user.
- Does not directly assign implementation/review work to `B組02` / `B組03`.
- Sends mature planning proposals to `A組01 整合規劃`.

### A組01 - 整合規劃

- Integrates output from `A組02`, `A組03`, `A組04`, and planning branches.
- Updates or proposes blueprint/control changes.
- Decides whether a proposal should loop, branch, pause for `00`, or become an
  implementation-package candidate.
- Applies `docs/CODEX_AI_GOVERNED_DECISION_LANE_V0.md`; when the objective
  AI technical approval gate passes, `A組01` may produce an
  `AI_TECHNICAL_APPROVED` package without waiting for routine `00` approval.
- Sends accepted implementation-package candidates to `B組01 派工`.
- Must not dispatch implementation/review tasks directly to `B組02` / `B組03`.

### A組02 - 發散提案

`A組02` generates candidate directions. It is allowed to be imaginative, but
must keep each idea tied to the final product goal: reusable local production
capability for storyboard / voiced-comic / video delivery.

`A組02` should ask:

- What is the obvious solution?
- What stronger architecture might replace it?
- What future reuse would be lost if we hard-code this now?
- Which parts should be fixed core, variable modules, or composition rules?
- Which subtopic is large enough to require a branch window?

`A組02` must not assign implementation tasks.

### A組03 - 風險審閱

`A組03` attacks the proposal. It looks for drift, hidden cost, missing gates,
and premature commitments.

`A組03` should ask:

- Where does the plan overfit one demo?
- What would force rework later?
- What user approval, canon approval, asset approval, or registry approval is
  being implied accidentally?
- What would leak author-only or review-only information?
- What external tool, provider, media generation, or budget decision is being
  smuggled in?
- Which branch proposal should be split into its own planning window?

`A組03` must propose at least one repair path for each major critique.

### A組04 - 綜合分支

`A組04` turns `A組02` and `A組03` output into a clearer architecture proposal.
`A組04` does not automatically hand work to implementation. It first decides
whether the proposal is mature, should loop back to `A組02`, should go to
`A組01`, or should spawn a bounded planning branch.

`A組04` output should include:

- current proposal;
- rejected alternatives;
- fixed core;
- variable modules;
- composition rules;
- open risks;
- branch-window requests, if any;
- maturity score;
- implementation handoff candidate, if mature.

`A組04` must not bypass `00`, `A組01`, or `B組01`.

## Group B Relationship

The implementation group is a separate single-lane implementation pipeline:

1. `B組01 派工` assigns exactly one bounded task.
2. `B組02 實作` performs that task as the only active worker.
3. `B組02` reports to `B組01`.
4. `B組01` sends the result to `B組03 審核`.
5. `B組03` reports PASS / NEEDS_REVISION / FAIL to `B組01`.
6. `B組01` decides the next single B-group transition or reports back to `00` /
   `A組01`.

Planning may branch. Implementation must not branch unless the user explicitly
changes the operating model.

## Thinking Loop

The default planning loop is:

1. `A組02` proposes a direction or architecture.
2. `A組03` critiques it and proposes fixes.
3. `A組04` synthesizes the next version.
4. `A組04` either:
   - loops the revised question back to `A組02`;
   - creates a `SPAWN_REQUEST` for a bounded branch window;
   - asks `A組01` for an AI technical approval decision;
   - asks `00` only when a stop condition or true user decision is needed;
   - produces a mature work-package candidate for `A組01`.
5. `A組01` integrates the result and either updates blueprint/control docs,
   loops the question, asks `00` only for a stop-condition decision, or hands an
   AI-technically-approved implementation package to `B組01`.

The loop may repeat, but it must not become an unattended infinite loop. Each
cycle needs a clear question, new evidence, and a reason to continue.

When the trigger is an `EVOLUTION_EVENT`, the loop changes to:

1. `00-01 分流接頭` routes the symptom to `A組01`.
2. `A組01` decides whether A02/A03/A04 or a bounded branch is needed.
3. `A組03` performs root-cause and risk review.
4. `A組02` proposes stronger architecture alternatives.
5. `A組04` synthesizes one rule upgrade and one regression check.
6. `A組01` integrates the accepted rule into blueprint/control or produces an
   `AI_TECHNICAL_APPROVED` implementation package for `B組01`.
7. `B組01` may dispatch only after receiving the mature A01 package.

## Architecture Lens

Every planning proposal must be checked through this lens.

### Fixed Core

The identity and constraints that should remain stable:

- character identity, body type, face family, voice identity, inner motive;
- world rules, era, law, power system, knowledge boundary;
- scene identity, location topology, zone layout, continuity anchors;
- asset identity, approved reference ids, variant families;
- safety and visibility boundaries.

### Variable Modules

The parts designed to change without breaking the core:

- clothing, hair, expression, pose, injury state, prop loadout;
- scene dressing, weather, lighting, camera angle, framing;
- dialogue variant, audio cue, voice intensity, pacing;
- storyboard frame, shot purpose, transition, insert detail;
- review status, approval status, downstream eligibility.

### Composition Rules

The rules that decide whether modules can combine:

- which modules can be swapped freely;
- which modules require human review;
- which modules require asset registry approval;
- which modules cannot cross visibility boundaries;
- which combinations break canon, continuity, style, or production safety;
- which combinations are allowed only as draft-only planning.

## Maturity Gate

A planning proposal may move toward implementation only when it can answer:

- What reusable product capability does this create?
- What is fixed core?
- What is variable module?
- What are composition rules?
- What files or data shapes would be touched later?
- What must not be touched?
- What validation or review gate proves the step worked?
- What human approval is still required?
- What exact next bounded task could `B組01` assign to `B組02`?

If any answer is missing, the proposal should loop back through `A組02` /
`A組03` / `A組04` or spawn a branch window.

## Work Package Candidate Shape

When `A組04` thinks a proposal is mature, it should produce this shape for
`A組01`:

```text
ARCHITECTURE_PROPOSAL
Topic:
User goal:
Reusable capability:
Fixed core:
Variable modules:
Composition rules:
Evidence read:
Open risks:
Human decisions still needed:
Forbidden work:
Suggested implementation lane:
Smallest safe next task:
Review gate:
Status: MATURE_FOR_A組01_INTEGRATION | NEEDS_BRANCH | NEEDS_MORE_THINKING
```

When `A組01` accepts the proposal, it should convert it to:

```text
IMPLEMENTATION_PACKAGE_CANDIDATE
Source proposal:
Accepted scope:
Forbidden work:
Files likely touched:
Validation expected:
Suggested B組01 task:
Human decisions still needed:
Status: READY_FOR_B組01_DISPATCH | NEEDS_00_DECISION | NEEDS_MORE_PLANNING
```

Only `READY_FOR_B組01_DISPATCH` with `AI_TECHNICAL_APPROVED` can go to `B組01`
without routine `00` approval. It still does not authorize `B組02` to work until
`B組01` creates one bounded task.

## Stop Conditions

Planning stops and returns to `00` / user when:

- product direction is unclear;
- worldbuilding is missing;
- a branch would require provider/API, paid tools, media generation, or budget
  approval;
- canon, registry, release, production lock, or final media approval is needed;
- the proposal cannot be tied to the final storyboard / voiced-comic / video
  deliverable;
- the loop has repeated without new evidence;
- quota or context risk is high.

## Hard Boundaries

- No provider/API.
- No web/server/open ports.
- No SQLite direct writes.
- No schema/migration changes.
- No renderer/ffmpeg/TTS/media generation.
- No 04/05/06/07/08 restart without explicit `00` / user and `B組01` tasking.
- No Phase 20 / Phase 21 restart without explicit `00` / user and `B組01`
  tasking.
- No canon, registry, release, approval, final media approval, or production
  lock claims.
- No staging, commit, or push unless the user explicitly asks.
