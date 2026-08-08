# A Group Proactive Evolution Heartbeat Rule Set V0

Status: `DRAFT_FOR_REVIEW`

Control artifact:
`AGroupProactiveEvolutionHeartbeatRuleSet`

This document is docs/control only. It installs proactive A-group heartbeat
rules so A01 cannot close with acknowledgement-only replies except explicit
configuration/update-only cases, can run bounded `EVOLUTION_EVENT` heartbeats,
and can keep A02/A03/A04 active without drifting, replaying stale packages,
creating repeated-PASS loops, or bypassing B01. It does not create Kotlin/JVM
code, no CLI, no runtime, no schema, no SQLite, no provider/API, no web/server,
no renderer/ffmpeg/TTS, no media generation, no sample JSON, no actual query
instances, no `QueryRequest`, no `QueryResponse`, no `QueryScopeReviewReport`,
no evidence bundles, no `ScriptReadyScenePacket`, no story/source content, no
concrete refs, no source-basis selection, no canon, no registry, no release, no
approval, no final media approval, and no production lock.

## Upstream Planning/Control Evidence

- A02 produced `P1_PROACTIVE_EVOLUTION_HEARTBEAT_SEED`.
- A03 returned `P2_PROACTIVE_EVOLUTION_HEARTBEAT_RISK_REVIEW` with verdict
  `SAFE_WITH_GUARDS`.
- A04 returned `P3_PROACTIVE_EVOLUTION_HEARTBEAT_SYNTHESIS` with decision
  `PROCESS_RULE_PATCH_READY`.
- A01 approved one bounded docs/control implementation package with
  `AI_TECHNICAL_APPROVED` and `READY_FOR_B01_DISPATCH`.
- A01 checked current docs for duplicate complete heartbeat rule markers and
  found generic `EVOLUTION_EVENT` references but no complete
  `A Group Proactive Evolution Heartbeat Rule Set`,
  `No Ack-Only Close Rule`, `Product-Distance Ledger`, or
  `Latest-Package Freshness Check` patch before approving this package.
- Local pre-edit evidence found no complete heartbeat rule-set marker before
  this patch.

B03 PASS, 03 PASS, `QA_PASS_NOT_APPROVAL`, and `REVIEW_PASS_NOT_APPROVAL` are
technical review states only. They are not user approval, asset approval,
registry approval, canon, release, final media approval, provider/media
handoff, final approval, or production lock.

## Rule Set Purpose

This rule set closes a governance gap: A-group may acknowledge an event, replay
a stale package, or continue a repeated PASS loop without proactively reducing
the uncertainty that blocks generated video / voiced-comic delivery. The patch
makes heartbeat checks mandatory when the system shows wakeup, missed handoff,
ignored latest package, stale-package risk, repeated PASS loop, or
system-not-getting-stronger symptoms.

Selected proactive-evolution process-rule outcome:

`A_GROUP_PROACTIVE_EVOLUTION_HEARTBEAT_PROCESS_RULE_PATCH_ALIGNED`

No blocker is selected. A02/A03/A04 already completed the branch convergence
and A04 returned `PROCESS_RULE_PATCH_READY`.

## No Ack-Only Close Rule

The `No Ack-Only Close Rule` says A01 must not close a planning/control turn
with acknowledgement-only text when the message contains a route-risk signal,
handoff risk, stale package risk, repeated PASS loop, or product-distance gap.

An acknowledgement-only close is allowed only when the incoming message is one
of these explicit exempt cases:

- `CONFIG_UPDATE_ONLY`: the message only updates configuration, names, labels,
  model settings, workspace paths, or routing metadata, and asks for no work.
- update-only: the message records an external fact and explicitly does not
  ask A01/B01/B02/B03 to act.
- do-not-start-work: the message explicitly says not to start work, not to
  dispatch, or only to acknowledge receipt.

If the message is not exempt, A01 must perform at least one bounded control
action:

- open or update an `EVOLUTION_EVENT`;
- run the Latest-Package Freshness Check;
- update the Product-Distance Ledger;
- form one bounded A02 -> A03 -> A04 heartbeat loop;
- return a precise blocker or true stop condition.

Compatibility marker for dispatch text that escaped the arrows:
`A02 -&gt; A03 -&gt; A04`.

## A01-Controlled EVOLUTION_EVENT Trigger Map

A01 owns the trigger map and may start heartbeat analysis when one of these
signals appears:

| Signal | Required A01 action | Forbidden close |
| --- | --- | --- |
| Manual wakeup | Open or update `EVOLUTION_EVENT` and identify which window failed to advance. | Do not reply with acknowledgement only unless the wakeup is explicitly `CONFIG_UPDATE_ONLY`. |
| Missed handoff | Record the expected owner, actual owner, missing relay, and current B01 dispatch authority. | Do not send directly to B02/B03. |
| Repeated PASS loop | Apply the route-loop convergence rule and ask whether a bundle, rule patch, A-branch classification, or explicit blocker is required. | Do not approve another status-only slice. |
| Stale package risk | Run Latest-Package Freshness Check before approving or dispatching anything. | Do not replay an older package when a newer PASS or corrected package exists. |
| Ignored latest package | Compare the requested task name against the latest B03 PASS / A01 approval chain. | Do not close without naming the mismatch or next owner. |
| System-not-getting-stronger signal | Start bounded heartbeat analysis and require product-distance evidence. | Do not treat the symptom as ordinary status. |

Each `EVOLUTION_EVENT` heartbeat must include:

- observed symptom;
- latest reviewed package or approved package checked;
- product-distance chain affected;
- owner gap or freshness risk;
- required regression guard;
- next bounded A01 decision.

## Bounded Heartbeat Loop

Only A01 may start another proactive heartbeat loop. The only allowed bounded
loop is:

```text
A02 -> A03 -> A04
```

The loop shape is:

1. A01 opens or updates one `EVOLUTION_EVENT` and defines the heartbeat
   question.
2. A02 proposes one stronger process or product-distance interpretation.
3. A03 reviews risk and must identify stale-package, repeated-PASS, direct-B,
   infinite-planning, and approval-drift risks.
4. A04 synthesizes one rule patch, one regression guard, or one precise
   blocker.
5. A01 decides whether the synthesis becomes an `AI_TECHNICAL_APPROVED`
   package for B01, a bounded A-branch continuation, a true stop condition, or
   no action.

The loop must not self-restart. A02, A03, and A04 may not start another loop.
A01 may start another loop only when there is new evidence, a new trigger, or a
failed regression guard.

## Branch Delivery Failure Fallback

Use `docs/CODEX_A_GROUP_BRANCH_DELIVERY_FAILURE_FALLBACK_RULE_V0.md` when an
A-group branch returns `systemError`, no agent result, a userMessage-only turn,
repeated empty completion, or a stale branch with no synthesis output.

A01 may perform one compact retry. If that retry fails, A01 must apply the
fallback policy: synthesize from available A02/A03 evidence, reassign one
bounded A-group branch, or issue `HOLD_FOR_A_GROUP_PLANNING` with exact owner,
blocker, and action. The failed branch must not block B01 forever when A01 has
enough reviewed evidence to form a bounded docs/control package.

Every fallback must state final-product chain impact or process-risk reduction
toward generated video / 有聲漫畫. A02/A03/A04 and future A branches must not
dispatch directly to B02/B03.

## Product-Distance Ledger

The `Product-Distance Ledger` is a control checklist, not a new status-only
document. It records which final-product uncertainty the A-group action reduces
toward generated video / 有聲漫畫 delivery.

Every future A-group proposal must cite at least one final-product chain:

- `FINAL_PRODUCT_CHAIN_CHARACTER_ASSETS`: identity, model sheet, wardrobe,
  expressions, props, or reusable asset identity.
- `FINAL_PRODUCT_CHAIN_SCENE`: source scene, beat, zone, continuity,
  visibility, or setting constraints.
- `FINAL_PRODUCT_CHAIN_STORYBOARD_CAMERA`: shot list, panel sequence, framing,
  camera move, or review-only storyboard planning.
- `FINAL_PRODUCT_CHAIN_PROMPT_ASSET_CARD`: prompt-safe asset card,
  negative-prompt boundary, allowed prompt surface, or protected-boundary
  reference.
- `FINAL_PRODUCT_CHAIN_VOICE_SUBTITLE_SFX`: voice plan, subtitle timing, SFX
  cue, audio visibility, or non-runtime sound planning.
- `FINAL_PRODUCT_CHAIN_RENDERER_PACKAGE_QA`: renderer readiness, package QA,
  delivery shape, artifact review, or final package boundary.

Ledger fields:

```text
Product-Distance Ledger
Task/proposal:
Final-product chain tag:
Uncertainty reduced:
Evidence cited:
Artifact-shape effect:
What remains unresolved:
Why this is not status-only:
Boundary reminders:
```

If a proposal cannot fill these fields, A01 must not approve it as another
status/review/route-decision slice. A01 must return it to A02/A03/A04,
converge it into a bundle/rule patch, or name an explicit blocker.

## Latest-Package Freshness Check

The `Latest-Package Freshness Check` is a checklist field inside A01/B01
classification, not a new status-only docs/control document.

Before A01 approves a package or B01 dispatches a package, the owner must check:

- latest B03 PASS task name;
- latest selected outcome;
- latest future candidate named by reviewed docs;
- whether a corrected A01 package supersedes an earlier package;
- whether a stale package replay risk exists;
- whether a repeated PASS loop has already triggered the route-loop
  convergence rule;
- whether B01, not A01/00/A02/A03/A04, is the correct B dispatch authority.

Checklist result values:

- `FRESHNESS_CHECK_CURRENT`: package matches latest reviewed/approved route.
- `FRESHNESS_CHECK_CORRECTED_PACKAGE`: package supersedes an older package and
  must name the corrected basis.
- `FRESHNESS_CHECK_STALE_REPLAY_BLOCKED`: package is stale and must not be
  dispatched.
- `FRESHNESS_CHECK_NEEDS_A01_CLASSIFICATION`: latest package is unclear and
  must return to A01, not to direct B dispatch.

This check must not become a standalone status-only document whose only purpose
is to restate freshness or heartbeat status.

## Progress Display Guard

If an A-group heartbeat, package, or synthesis mentions project progress, it
must apply `docs/CODEX_BLUEPRINT_PROGRESS_DISPLAY_MODEL_RULE_SET_V0.md`.
Progress text must obey `No Naked Percentage`, `Percent Change Gate`, and
`Completion Claim Firewall`.

The old `Local baseline progress: 100%` line is allowed only with same-line firewall language: not final media approval, not release, not production lock, not canon, not registry, not video completion, and not true product completion. `True final product progress: not final / model under review`
remains non-numeric until an accepted progress model changes it.

Old dispatch-chain progress must be named `B組01 dispatch-chain local evidence
metric`, `local fallback only / not global blueprint progress`. A proactive
heartbeat must not treat B03 PASS, A-group synthesis PASS, route status update,
future-candidate docs, or non-approval review as true final product progress.

## Authority Separation

- A01 forms and approves mature implementation packages.
- B01 dispatches B02 implementation and B03 review.
- B02 implements only the single bounded B01-dispatched package.
- B03 reviews only when B01 dispatches review.
- A02/A03/A04 can think, critique, and synthesize, but they do not dispatch
  directly to B01, B02, or B03.
- A01/00 must not directly contact B02 or B03 for implementation or review.

The authority chain remains:

```text
00 / 00-01 -> A01 package formation -> B01 dispatch -> B02 implementation -> B01 -> B03 review
```

## Regression Guards

These regression guards are mandatory after this patch:

- ack-only guard: if a non-exempt message contains route risk, A01 must produce
  an `EVOLUTION_EVENT`, Product-Distance Ledger entry, freshness decision, or
  blocker.
- stale-package guard: if the requested package does not match the latest
  reviewed/approved route, A01/B01 must block stale replay or request A01
  classification.
- repeated-PASS guard: if same-family adjacent PASSes only add status layers,
  A01 must apply the route-loop convergence rule.
- direct-B guard: A01/A02/A03/A04/00 must not dispatch directly to B02/B03.
- infinite-planning guard: a proactive heartbeat loop may run only once per
  trigger unless A01 names new evidence.
- product-distance guard: every future A-group proposal must name the
  final-product chain uncertainty it reduces.

## Missing Tags And Routing Gaps Closed

This patch identifies and closes these gaps:

- no explicit No Ack-Only Close Rule;
- no A01-controlled trigger map for proactive `EVOLUTION_EVENT` heartbeats;
- no bounded heartbeat loop preventing unattended A02/A03/A04 recursion;
- no Product-Distance Ledger tied to generated video / 有聲漫畫 uncertainty;
- no Latest-Package Freshness Check field to prevent stale-package replay;
- no regression guard for direct B dispatch from A-group windows;
- no guard against turning freshness/heartbeat into another status-only doc.

No unresolved missing status tag remains for this process-rule patch. Future
gaps should be routed through A01 classification with product-distance
evidence.

## Downstream Allowance

On B03 PASS, B01/A01/00 may use this heartbeat rule set to classify future
docs/control packages, block acknowledgement-only closes, require freshness
checks, and start bounded A02/A03/A04 heartbeat loops only through A01. PASS
must not auto-start Kotlin/JVM code, CLI, runtime, schema, SQLite,
provider/API, web/server, renderer/ffmpeg/TTS, media generation, sample JSON,
actual query instances, `QueryRequest`, `QueryResponse`,
`QueryScopeReviewReport`, evidence bundles, `ScriptReadyScenePacket`,
story/source content, concrete refs, source-basis selection, canon, registry,
release, approval, final media approval, or production lock.

## Boundary Summary

- docs/control only
- A Group Proactive Evolution Heartbeat Rule Set only
- No Ack-Only Close Rule with `CONFIG_UPDATE_ONLY`, update-only, and
  do-not-start-work exemptions
- A01-controlled `EVOLUTION_EVENT` trigger map
- bounded `A02 -> A03 -> A04` heartbeat loop only under A01 coordination
- Product-Distance Ledger for generated video / 有聲漫畫 uncertainty
- Latest-Package Freshness Check as a checklist field, not a new status-only
  document
- no provider/API
- no SQLite
- no renderer/ffmpeg/TTS
- no media generation
- no sample JSON
- no actual query instances
- no evidence bundles
- no concrete refs
- no direct B02/B03 dispatch by A-group windows
- no approval, canon, registry, release, final media approval, or production
  lock
