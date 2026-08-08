# Codex Default Route Decision Rules V0

Status: `DEFAULT_ROUTE_DECISION_RULES_V0_03_PASS`

Date: 2026-07-02

## Purpose

These rules let 01 choose a bounded next task when the user says "continue"
without choosing a route. They reduce decision friction without turning silence
into approval.

## When These Rules Apply

Apply these rules only when all are true:

- the user or 01 explicitly asks to continue;
- the latest required 03 review is PASS or no review is needed for the current
  docs-only decision;
- quota is not near exhaustion;
- no human approval gate is currently blocking the route;
- the task can stay within Kotlin/JVM CLI, SQLite, and JSON file protocol
  boundaries.

If any condition fails, stop and report the blocker.

## Default Route Priority

When the user says "continue" and gives no route choice, choose the first
eligible route in this order:

0. `a_group_evolution_system_v0`
1. `09_foundation_card_organization`
2. `demand_driven_query_templates`
3. `script_ready_scene_packet_alignment`
4. `storyboard_camera_packet_planning`
5. `asset_lifecycle_registry_decision_packet`
6. `voice_timeline_subtitle_sfx_contract`
7. `local_renderer_execution_readiness_checklist`
8. `local_package_qa_delivery_definition`

Skip a route if it needs user input, human approval, schema/direct SQLite,
provider/API, paid tools, renderer execution, media generation, canon, registry,
release, or production lock.

`a_group_evolution_system_v0` is eligible when the project has recently had a
stall, manual wakeup, missed handoff, false stop, unclear route authority, or
user-observed "A-group is not getting stronger" symptom. It should install the
`EVOLUTION_EVENT` / `ROOT_CAUSE_ANALYSIS` / `RULE_UPGRADE_PROPOSAL` /
`REGRESSION_CHECK` loop before ordinary feature-route continuation.

## Task Size Rule

Prefer one bounded medium task over many tiny interruptions when the user asks
for sustained progress. A bounded medium task must still have:

- named route family;
- explicit non-goals;
- exact file list or expected file families;
- verification plan;
- 03 review request when appropriate;
- no automatic next task.

## Route Loop Convergence Rule

Use `docs/CODEX_ROUTE_LOOP_CONVERGENCE_RULE_SET_V0.md` when the current family
has repeated docs/control PASSes that only add status, review, review-status,
route-decision, or route-package layers.

After `2 adjacent docs/control PASSes` in the same family that only produce
status/review/route-decision layers, A01 must not approve another narrow status
slice. A01 must converge into one of these:

- a bundle surface;
- a process/rule patch;
- A-branch classification;
- an explicit blocker or true stop condition supported by reviewed evidence.

Every proposed task must carry at least one final-product chain tag:

- `FINAL_PRODUCT_CHAIN_CHARACTER_ASSETS`
- `FINAL_PRODUCT_CHAIN_SCENE`
- `FINAL_PRODUCT_CHAIN_STORYBOARD_CAMERA`
- `FINAL_PRODUCT_CHAIN_PROMPT_ASSET_CARD`
- `FINAL_PRODUCT_CHAIN_VOICE_SUBTITLE_SFX`
- `FINAL_PRODUCT_CHAIN_RENDERER_PACKAGE_QA`

Apply `a06-no-new-same-family-status-layer-without-bundle-v0` when a route
family reaches the 3rd adjacent same-family layer. Same-family layer types are
status, review, review-status, route-decision, route-package, package-review,
and curation. A01 must not approve another standalone same-family layer at that
point. It must converge to one of these: a final-product-chain-tagged bounded
package candidate, an explicit blocker / `TRUE_STOP_CONDITION`, or a
process/rule patch with regression check.

Every next task must name the final-product chain it reduces: scene packet,
storyboard/camera, prompt/asset cards, voice/subtitle/SFX, renderer/package QA,
or human approval gates.

Apply the `Artifact-Shape-Ready Gate`, `Final-Product Distance Gate`,
`No-New-Status-Doc Gate`, and `Bundle Review Gate` before approving another
docs/control status/review/route-decision task.

These gates preserve safety reminders but do not authorize actual instances,
sample JSON, provider/API work, SQLite changes, renderer/ffmpeg/TTS execution,
media generation, approval, canon, release, or production lock.

## Proactive Evolution Heartbeat Rule

Use `docs/CODEX_A_GROUP_PROACTIVE_EVOLUTION_HEARTBEAT_RULE_SET_V0.md` when a
continue/dispatch decision shows manual wakeup, missed handoff, repeated PASS
loop, stale package risk, ignored latest package, or
system-not-getting-stronger signals.

A01 must apply the `No Ack-Only Close Rule`: no acknowledgement-only close is
allowed unless the incoming message is explicitly `CONFIG_UPDATE_ONLY`,
update-only, or do-not-start-work.

Before approving or dispatching a package, A01/B01 must record a
`Latest-Package Freshness Check` field. The check is not a new status-only
docs/control document; it prevents stale-package replay.

Every future A-group proposal must include a `Product-Distance Ledger` field
that names one final-product chain uncertainty being reduced: character
assets, scene, storyboard/camera, prompt/asset card, voice/subtitle/SFX, or
renderer/package QA.

Only A01 may coordinate a bounded `A02 -> A03 -> A04` heartbeat loop, and
A-group windows must not dispatch directly to B02/B03. This rule does not
authorize sample JSON, provider/API work, SQLite changes,
renderer/ffmpeg/TTS execution, media generation, approval, canon, release, or
production lock.

## Progress Display Model Rule

Use `docs/CODEX_BLUEPRINT_PROGRESS_DISPLAY_MODEL_RULE_SET_V0.md` whenever a
route decision, B01 status line, or progress-control document displays a
percentage.

The `No Naked Percentage` rule requires every percentage line to name metric
object, scope, evidence range, non-meaning, and change gate. The old
`Local baseline progress: 100%` line is allowed only with same-line firewall language: not final media approval, not release, not production lock, not canon, not registry, not video completion, and not true product completion.

`True final product progress: not final / model under review` remains the
default and must not receive a numeric estimate before an accepted model
update. Any old dispatch-chain percentage is a `B組01 dispatch-chain local
evidence metric`, `local fallback only / not global blueprint progress`.

The `Percent Change Gate` allows percentage changes only after accepted
final-product capability movement in world/foundation, query/source scope,
scene packet, storyboard/camera, asset cards, voice/subtitle/SFX, local render
package, or human approval gates. The `Completion Claim Firewall` blocks any
100% line that does not carry same-line non-approval and non-completion language: not final media approval, not release, not production lock, not canon, not registry, not video completion, and not true product completion.

## Verification Timeout False-Negative Guard

Use `docs/CODEX_VERIFICATION_TIMEOUT_FALSE_NEGATIVE_GUARD_V0.md` when a broad
`rg` or batch verification command times out or returns partial output.

Timeouts must be classified as `INCONCLUSIVE_TIMEOUT` under `Timeout Is
Inconclusive, Not Fail`. Apply the `Fallback Precision Ladder`: broad search
-> touched files -> scoped control docs -> single file -> exact phrase ->
stale/forbidden negative phrase check.

Use `Completed-Output Verdict Only`: timed-out output is not PASS, FAIL,
NEEDS_REVISION, or missing-evidence proof. Use `Dirty-Worktree Scope Guard`
when unrelated dirty files could inflate the search scope. Timeout reports
must record original command, scope, timeout duration, partial output status,
fallback commands/scopes, and remaining verification gaps.

If fallback checks pass after a timeout, include a `False-Negative Note`.
B01 dispatch authority and B03 review authority remain unchanged.

## A Group Branch Delivery Failure Fallback Rule

Use `docs/CODEX_A_GROUP_BRANCH_DELIVERY_FAILURE_FALLBACK_RULE_V0.md` when an
A-group branch delivery fails with `systemError`, no agent result,
userMessage-only turn, repeated empty completion, or stale branch with no
synthesis output.

A01 may run one compact retry. If the retry fails, A01 must apply the fallback
policy: synthesize from available A02/A03 evidence, reassign one bounded
A-group branch, or issue `HOLD_FOR_A_GROUP_PLANNING` with exact owner,
blocker, and action.

The failed branch output must not block B01 forever if A01 has enough reviewed
evidence to form a bounded docs/control package. Every fallback must state
final-product chain impact or process-risk reduction toward generated video /
有聲漫畫. A-group windows must not dispatch directly to B02/B03.

## Review Rule

- 02 implementation or verification reports to 01.
- 01 sends the result to 03 when review is needed.
- 03 reports PASS / NEEDS_REVISION / FAIL to 01.
- 01 chooses the next route or stops.

03 PASS allows a route decision. It does not approve final media, canon,
registry, release, provider/media handoff, production lock, or user direction.

## Quota Rule

Check quota before assigning high-cost work when quota data is available. If
weekly quota is unknown, stale, or low, prefer docs-only review, status, or
pause. Never exhaust quota completely; preserve enough for a final status or
handoff message.

## Mandatory Stop Rules

Stop and ask user/01 before:

- spending money or using paid/provider tools;
- calling provider/API;
- changing schema or writing SQLite directly;
- running renderer/ffmpeg/TTS;
- generating final media;
- approving assets, canon, registry, release, final media, or production lock;
- resuming 04/05/06/07/08;
- restarting Phase 20 / Phase 21;
- treating silence as approval.

## Boundary

These rules are docs/control only. They do not start implementation, approve
runtime behavior, or grant permission to bypass project boundaries.
