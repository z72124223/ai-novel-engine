# Codex Multi-Window Operating Model V1

Status: `CURRENT_CONTROL_PACKET`

This document records the current chat-window role model. The project is too
large for one mixed planning / dispatch / worker / reviewer loop, but the
implementation side should still remain a single lane. The current answer is a
two-group model with group-prefixed flow numbers.

This is docs/control only. It does not start implementation, approve canon,
approve registry, generate media, resume Phase 20 / Phase 21, or claim final
project completion.

## Purpose

Separate these concerns:

- 00-01: route user-facing input to the correct A/B owner.
- Group A: planning, product architecture thinking, blueprint integration.
- Group A optimization: route-weight audit, lightweight rules, and drift
  prevention through `A組06 優化稽核`.
- Group B: implementation dispatch, implementation, review/testing.

Group A may think in loops and open bounded planning branch windows. Group B
must stay a single-lane implementation pipeline.

## Authority Rule

- `00 總控決策` is the user-facing product direction window, not a routine
  manual approval gate for mature technical planning decisions.
- `A組01 整合規劃` integrates Group A planning output into blueprint changes
  and implementation-package candidates.
- `A組01 整合規劃` may apply the AI technical approval gate in
  `docs/CODEX_AI_GOVERNED_DECISION_LANE_V0.md` and promote a mature package to
  `READY_FOR_B組01_DISPATCH` without waiting for routine `00` approval.
- `A組06 優化稽核` may audit project weight, duplicate route layers, stalled
  loops, and task-drift risk, but reports to `A組01` and does not dispatch
  implementation work.
- `A組01 整合規劃` must not dispatch implementation or review work to `B組02` /
  `B組03`.
- `B組01 派工` is the only Group B dispatch authority.
- `B組01 派工` turns accepted packages into exactly one bounded task for
  `B組02`.
- `B組01 派工` sends completed `B組02` work to `B組03`.
- `B組01 派工` reports review results back to `00` / `A組01`.
- `B組02 實作` and `B組03 審核` report to `B組01`, not directly to `00` /
  `A組01`, unless `00` or the user explicitly redirects the handoff.

Planning branch windows report to `A組04 綜合分支` / `A組01 整合規劃`. They do
not report to `B組01`, `B組02`, or `B組03`, and they do not authorize
implementation.

## Canonical Workspace

All project work must use:

`C:\Users\f7212\Projects\AI 劇本`

Saved Codex project entries may open in OneDrive. That is not the canonical
workspace. Every worker/reviewer must explicitly use the canonical workspace.

## Role Map

### 00 - 總控決策

- Owns user communication, product direction, and decisions that truly require
  the user.
- May create or revise blueprint/control docs.
- Does not manually approve every mature technical proposal.
- Does not perform routine implementation while workers are active.
- Does not dispatch tasks directly to `B組02` / `B組03`.
- Stops work when a human decision, final media approval, budget/provider
  decision, canon/registry/release decision, or quota risk is needed.

### A組01 - 整合規劃

- Receives `A組02` / `A組03` / `A組04` / branch planning outputs.
- Integrates thinking into blueprint/control docs or implementation-package
  candidates.
- Returns only major direction choices, stop conditions, or human-required
  decisions to `00`.
- Applies the AI technical approval gate for mature technical proposals.
- Hands accepted implementation-package candidates to `B組01`.
- Must not assign work directly to `B組02` or `B組03`.
- Must not replace `B組01` as Group B traffic control.

- Applies `docs/CODEX_A_GROUP_PROACTIVE_EVOLUTION_HEARTBEAT_RULE_SET_V0.md`
  when manual wakeup, missed handoff, repeated PASS loop, stale package risk,
  ignored latest package, or system-not-getting-stronger signals appear.
- Must enforce the `No Ack-Only Close Rule` except for explicit
  `CONFIG_UPDATE_ONLY`, update-only, and do-not-start-work messages.
- Coordinates the only allowed proactive heartbeat loop, `A02 -> A03 -> A04`;
  A02/A03/A04 must not self-start another loop or dispatch to B02/B03.
- Records `Product-Distance Ledger` and `Latest-Package Freshness Check`
  fields inside A01/B01 classification rather than creating new status-only
  documents.

### Group A - Planning / Product Architecture

For Group A details, read:

- `docs/CODEX_PRODUCT_ARCHITECTURE_THINKING_LOOP_V0.md`
- `docs/CODEX_PLANNING_WINDOW_FACTORY_PROTOCOL_V0.md`
- `docs/CODEX_AI_GOVERNED_DECISION_LANE_V0.md`
- `docs/CODEX_A_GROUP_OPTIMIZATION_AUDIT_LANE_V0.md`

Active Group A roles:

- `A組01 整合規劃`: integration and blueprint/work-package candidate owner.
- `A組02 發散提案`: divergent proposal window.
- `A組03 風險審閱`: critique, risk, and solution window.
- `A組04 綜合分支`: synthesis and branch manager.
- `A組05 分支提示詞邊界`: bounded planning branch for prompt-surface /
  leakage-boundary work.
- `A組06 優化稽核`: planning-only optimization audit lane for project
  lightening, duplicate-layer reduction, route convergence, and drift
  prevention.
- Future `A組xx` planning branches: bounded branch windows created from
  `SPAWN_REQUEST`.

Group A output is not implementation. It produces architecture proposals,
fixed-core / variable-module / composition-rule analysis, and branch reports.
Only a mature proposal accepted by `A組01` through the AI technical approval
gate, or explicitly redirected by `00` / user, may be handed to `B組01`.

### B組01 - 派工

- The only B-group dispatch / traffic-control window.
- Converts accepted work packages into exactly one bounded `B組02` task.
- Checks scope, quota/load safety, and file-conflict risk before dispatch.
- Sends completed `B組02` reports to `B組03` for review.
- Receives `B組03` PASS / NEEDS_REVISION / FAIL.
- Chooses one next B-group transition or reports back to `00` / `A組01`.
- Must not run A-group ideation or rewrite the blueprint.

### B組02 - 實作

- The only active Kotlin/JVM CLI + JSON file-protocol implementation worker.
- Handles model/service/CLI/sample/test/docs slices when assigned by `B組01`.
- Reports completion to `B組01` only.
- Does not contact `B組03` directly and does not start the next task.

### B組03 - 審核

- The only active reviewer.
- Reviews code/docs/sample changes for correctness, boundary drift, and missing
  tests.
- Default mode is read-only. No edits, no staging, no commit, no push.
- Runs commands only when the review request explicitly allows them.
- Reports PASS / NEEDS_REVISION / FAIL to `B組01`.

### Verification Timeout False-Negative Guard

- Use `docs/CODEX_VERIFICATION_TIMEOUT_FALSE_NEGATIVE_GUARD_V0.md` whenever
  `rg` or batch verification times out or returns partial output.
- A timeout is `INCONCLUSIVE_TIMEOUT` under `Timeout Is Inconclusive, Not
  Fail`; it is not PASS evidence, FAIL evidence, NEEDS_REVISION evidence, or
  proof that required evidence is missing.
- B02 completion reports and B03 reviews must use `Completed-Output Verdict
  Only`, the `Fallback Precision Ladder`, and `Dirty-Worktree Scope Guard`
  before making a verdict from verification output.
- When fallback checks pass after a timeout, include a `False-Negative Note`
  with original command, timeout duration, partial output status, fallback
  commands/scopes, and remaining verification gaps.
- This guard preserves B01 dispatch authority and B03 review authority; it does
  not allow direct B02/B03 dispatch by A-group windows.

### A Group Branch Delivery Failure Fallback

- Use `docs/CODEX_A_GROUP_BRANCH_DELIVERY_FAILURE_FALLBACK_RULE_V0.md` when an
  A-group branch produces `systemError`, no agent result, a userMessage-only
  turn, repeated empty completion, or a stale branch with no synthesis output.
- A01 may run one compact retry only. If it fails, A01 must synthesize from
  available A02/A03 evidence, reassign one bounded A-group branch, or issue
  `HOLD_FOR_A_GROUP_PLANNING` with exact owner, blocker, and action.
- Failed A04 or branch output must not block B01 forever when reviewed evidence
  is enough for one bounded docs/control package.
- Every fallback must name final-product chain impact or process-risk
  reduction toward generated video / 有聲漫畫.
- A-group windows must not dispatch directly to B02/B03.

### A06 Same-Family Status-Layer Convergence Rule

- `A組06 優化稽核` may flag
  `a06-no-new-same-family-status-layer-without-bundle-v0` when a route family
  reaches the 3rd adjacent same-family layer.
- Same-family layer types are status, review, review-status, route-decision,
  route-package, package-review, and curation.
- A01 must not approve another standalone same-family layer at that point. A01
  must converge to a final-product-chain-tagged bounded package candidate,
  explicit blocker / `TRUE_STOP_CONDITION`, or process/rule patch with
  regression check.
- Every next task must name the final-product chain it reduces: scene packet,
  storyboard/camera, prompt/asset cards, voice/subtitle/SFX,
  renderer/package QA, or human approval gates.
- This is a planning/control guard only. It preserves review gates,
  non-approval status, no provider/API, no SQLite, no renderer/ffmpeg/TTS, no
  media generation, no approval, no canon, no release, and no production lock.
- A06 reports to A01 only. A-group windows must not dispatch directly to
  B02/B03.

### Archived Old Side Lanes

- `90 封存 舊02B副線`: `019f2228-1aaf-7513-9457-7e7557fca40c`
- `91 封存 舊03B副線`: `019f2228-756a-7091-8294-885dc5ddaadd`

These are archived obsolete standby windows. They must not receive new work
unless the user explicitly changes the operating model.

## Current Window IDs

- `00 總控決策`: `019f0dd1-1543-7f22-b646-5c12d07d168d`
- `A組01 整合規劃`: `019f2227-aeea-7870-9418-c7f5d399a2b1`
- `A組02 發散提案`: `019f258f-f29d-7843-bd2d-725476bf346a`
- `A組03 風險審閱`: `019f2590-1ce1-7271-ad94-036b27662b52`
- `A組04 綜合分支`: `019f2590-461b-7502-afb0-f5f65d79fc45`
- `A組05 分支提示詞邊界`: `019f25cb-dce1-78c1-9c93-5acd3c48f7aa`
- `A組06 優化稽核`: `019f30fe-b469-7ed1-aeea-2cfd3d97ccc3`
- `B組01 派工`: `019f25e7-7b64-7c91-b552-7287ef206e7d`
- `B組02 實作`: `019f1882-d5c6-72a3-893c-6d43cd097835`
- `B組03 審核`: `019f1ad1-2926-70c3-b85c-267df6fe331e`
- `90 封存 舊02B副線`: `019f2228-1aaf-7513-9457-7e7557fca40c`
- `91 封存 舊03B副線`: `019f2228-756a-7091-8294-885dc5ddaadd`

These ids record the current local Codex chat-window arrangement only. If a
future window is replaced, update this section and keep the role names stable.

## Default Flows

Group A thinking loop:

`00 -> A組01 -> A組02 -> A組03 -> A組04 -> optional A組 branch -> A組04 -> A組01 -> 00`

Group B implementation pipeline:

`accepted package -> B組01 -> B組02 -> B組01 -> B組03 -> B組01 -> 00/A組01`

`A組01` is not in the B-group dispatch loop except as a source/recipient of
accepted planning packages.

## Parallelism Rules

- Default Group B active workers: 1.
- Default Group B active reviewers: 1.
- Group A may open bounded planning branch windows, but those windows are
  planning-only and must not edit implementation files or dispatch `B組02` /
  `B組03`.
- Parallel Group B work is disabled. If a future task appears to require
  parallel work, stop and ask `00` / user to change the operating model before
  assigning it.
- Never let two implementation workers edit `PLANS.md`, `docs/HANDOFF.md`, or
  `app/src/main/kotlin/com/localnovel/engine/Main.kt` at the same time; under
  the current model there should be only one active implementation worker.
- Never let two implementation workers edit the same model/service/test/sample
  family at the same time; under the current model there should be only one
  active implementation worker.
- If a task touches shared CLI wiring, only one worker may be active.
- If scopes are uncertain, `B組01` must request a docs/control clarification
  before implementation.

## A/B Hold Relay Rule

The B-group dispatcher must not treat every A-group hold as a stop.

When `B組03` PASS or `B組02` completion leaves an ambiguous next step, `B組01`
asks `A組01` for classification. If `A組01` returns
`A_GROUP_BRANCH_REQUIRED`, `HOLD_FOR_A_GROUP_PLANNING`,
`HOLD_FOR_EXPLICIT_CLASSIFICATION`, or a non-stop route-authority gap, `B組01`
routes one bounded planning/control request back to `A組01`.

`A組01` then owns the planning response: coordinate an A branch, define missing
route authority, produce an `AI_TECHNICAL_APPROVED` package, or declare
`TRUE_STOP_CONDITION`. `B組01` stays dispatch-only and does not write the
blueprint, run ideation, or send the case to `00` for routine approval.

## Model Policy

- `00` status and user communication may use the current/default model.
- `00` route planning, `A組01` integration, Group A architecture work,
  branch-window synthesis, `B組01` dispatch decisions, `B組02` implementation,
  and `B組03` review should use `gpt-5.5` with `thinking=xhigh` when supported.
- Lightweight status reads may use default/current settings.
- Model choice never overrides project boundaries or user decisions.

## Quota / Usage Rules

- Check quota before high-cost transitions when visible or recently observed.
- Preserve enough quota for one final status / handoff message.
- If weekly quota is unknown, stale, or low, start at most one bounded task.
- If quota is near exhaustion, pause instead of starting implementation.
- Do not wake only to check quota unless the user requested it or a handoff is
  waiting.

## Current Accepted Route State

- Whole-project blueprint V0: 03 PASS.
- Blueprint execution control layer V0: 03 PASS.
- 09 foundation card execution preflight V0: 02 PASS and 03 PASS.
- Deep unattended 3/3:
  - Round 1 09 foundation card JSON contract/spec V0: 02 PASS and 03 PASS.
  - Round 2 foundation card pack validation V0: 02 PASS and 03 PASS.
  - Round 3 foundation card pack review gate V0: 02 PASS and 03 PASS.
- Next explicit route is likely `demand_driven_query_contract_spec_v0`.
- This next route must not start automatically.

Historical `02` / `03` labels in accepted route evidence mean the old worker /
reviewer roles. Under the current title map those roles are now `B組02` /
`B組03`.

## Stop Conditions

Stop and return to `00` / user when any of these occurs:

- Human product direction is needed.
- User worldbuilding is missing.
- Canon, registry, release, production lock, or final media approval is needed.
- Provider/API, external paid tool, or funding decision is needed.
- Quota is low, unknown, or stale for high-cost work.
- Verification fails or scope becomes ambiguous.
- True final project completion reaches 100%.

The old local product-delivery baseline phrase `Project completion progress:
100%` is not true final project completion.

## Hard Boundaries

- No provider/API calls.
- No web/server/open ports.
- No direct SQLite writes or schema/migration changes unless explicitly assigned
  and validated through project rules.
- No renderer/ffmpeg/TTS/media generation unless explicitly assigned.
- No real production manifest generation or approval unless explicitly assigned.
- No canon, registry, release, final media approval, approval, or production
  lock claims.
- No staging, commit, or push unless the user explicitly asks.

## A-Group Evolution Addendum

`00-01 分流接頭` is now the routing window between `00` and the A/B groups.
It routes user-facing symptoms, product direction, blueprint issues, stalls,
missed handoffs, evolution triggers, and route ambiguity to `A組01`. It routes
mature `IMPLEMENTATION_PACKAGE` / `AI_TECHNICAL_APPROVED` /
`READY_FOR_B組01_DISPATCH` packages to `B組01`. It must not dispatch B02/B03
directly and must not replace A01 as the evolution/root-cause owner.

`A組01` owns `docs/CODEX_A_GROUP_EVOLUTION_SYSTEM_V0.md`. When the project
stalls, needs manual wakeup, misses a handoff, false-stops on a routine
technical issue, or repeats a routing problem, A01 must create or coordinate an
`EVOLUTION_EVENT`, `ROOT_CAUSE_ANALYSIS`, `RULE_UPGRADE_PROPOSAL`, and
`REGRESSION_CHECK` before ordinary feature-route continuation.

`B組01` remains the only B-group dispatcher. It may dispatch only after A01 or
the 00-01 splitter supplies a mature A01-approved package. If B-group stalls on
classification, B01 asks A01 for evolution/classification instead of asking 00
for routine judgment.

## A-Group Optimization Audit Addendum

`A組06 優化稽核` owns `docs/CODEX_A_GROUP_OPTIMIZATION_AUDIT_LANE_V0.md`.
When the project becomes heavy, repeats route/status layers, appears idle,
creates work without visible final-product progress, or risks "lightweight"
becoming "missing required scope", A01 may route a bounded optimization audit
to A06.

A06 returns `OPTIMIZATION_AUDIT`, `LIGHTWEIGHT_RULE_PROPOSAL`, or
`CONVERGENCE_PACKAGE_CANDIDATE` to A01 only. A06 must not dispatch B02/B03,
rewrite the blueprint directly, claim approval, or cut required final-product
work. A01 decides whether to integrate the rule, ask A02/A03/A04 for more
thinking, or produce a mature package for B01.
- Do not resume Phase 20 / Phase 21 unless `00` / user explicitly selects that
  route.
