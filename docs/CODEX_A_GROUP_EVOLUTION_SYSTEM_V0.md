# Codex A Group Evolution System V0

Status: `A_GROUP_EVOLUTION_SYSTEM_V0_PRIORITY_CONTROL_PACKET`

Date: 2026-07-04

Canonical workspace: `C:\Users\f7212\Projects\AI 劇本`

## Purpose

This document adds the missing A-group evolution system to the whole-project
blueprint. The project must not only move tasks forward. It must become
stronger after each stall, missed handoff, wrong stop, unclear authority gap, or
manual wakeup.

This is docs/control only. It does not implement Kotlin/JVM code, run CLI
commands, write SQLite, call provider/API tools, run renderer/ffmpeg/TTS,
generate media, approve canon, approve registry, approve release, approve final
media, or create production lock.

## Highest-Priority Rule

`A_GROUP_EVOLUTION_SYSTEM_V0` is the highest-priority planning/control route
until it is implemented and reviewed.

Before continuing ordinary blueprint routes, Group A must be able to record an
evolution event, find its root cause, propose a rule upgrade, update blueprint
or dispatch rules, and define a regression check so the same failure does not
repeat.

This priority does not authorize B-group implementation by itself. It only means
the next mature package should focus on installing this evolution loop before
normal feature-route continuation.

## Trigger Events

Create an `EVOLUTION_EVENT` when any of these happens:

- user has to wake the system because windows stopped;
- B-group cannot decide the next task from accepted blueprint evidence;
- A-group returns a package but the dispatch route misses it;
- B01 asks 00 for routine technical continuation instead of asking A01;
- a review PASS produces no safe next transition;
- a route stalls because no one owns classification;
- one blocker is treated as full project stop when other blueprint routes exist;
- a window role contradicts the latest operating model;
- the same type of issue appears twice;
- user says the system is not getting stronger or not thinking deeply.

Do not create an evolution event for tiny wording fixes that do not affect
future routing, product architecture, safety, or task quality.

## Required Output Shapes

### EVOLUTION_EVENT

```text
EVOLUTION_EVENT
Event id:
Observed symptom:
Where it happened:
Evidence:
Expected behavior:
Actual behavior:
Immediate impact:
Repeated-risk level: low | medium | high
Needs A02 divergence: yes | no
Needs A03 risk review: yes | no
Needs A04 synthesis: yes | no
Status: OPEN
```

### ROOT_CAUSE_ANALYSIS

```text
ROOT_CAUSE_ANALYSIS
Event id:
Primary cause:
Contributing causes:
Missing rule:
Missing owner:
Missing regression check:
What must not be blamed:
Status: ANALYZED
```

### RULE_UPGRADE_PROPOSAL

```text
RULE_UPGRADE_PROPOSAL
Event id:
Rule to add or change:
Files or window prompts affected:
Expected stronger behavior:
Regression check:
Forbidden side effects:
Owner:
Status: READY_FOR_A01_INTEGRATION | NEEDS_MORE_THINKING | BLOCKED
```

### EVOLUTION_BLUEPRINT_PATCH

```text
EVOLUTION_BLUEPRINT_PATCH
Event id:
Accepted rule upgrade:
Blueprint/control docs to update:
Window role prompts to notify:
Dispatch implications:
Regression check to preserve:
Status: READY_FOR_B01_PACKAGE | CONTROL_ONLY_RECORDED | TRUE_STOP_CONDITION
```

## Group Responsibilities

### 00

- Sends user-observed symptoms to `00-01 分流接頭`.
- Does not diagnose routine technical root cause when A-group can do it.
- Receives only true stop conditions or concise status.

### 00-01 分流接頭

- Routes any evolution trigger, user-observed stall, or system-strength concern
  to `A組01`.
- Routes only mature `IMPLEMENTATION_PACKAGE` output to `B組01`.
- Does not dispatch directly to `B組02` or `B組03`.

### A組01

- Owns the evolution system.
- Opens or coordinates A02/A03/A04/branch thinking when needed.
- Converts accepted rule upgrades into blueprint/control updates or mature
  `IMPLEMENTATION_PACKAGE` output.
- Must not dispatch directly to B02/B03.

### A組02

- Proposes stronger architecture alternatives.
- Must look for reusable fixed core, variable modules, and composition rules.
- Must answer: "How does this make the whole project stronger next time?"

### A組03

- Performs risk review and root-cause pressure testing.
- Must find hidden costs, false stops, owner gaps, route drift, and repeated
  failure patterns.
- Must propose at least one repair path.

### A組04

- Synthesizes the rule upgrade.
- Decides whether the issue needs a branch, can return to A01, or remains
  blocked.
- Must produce the regression check that prevents recurrence.

### B組01

- If B-group gets stuck, asks A01 for evolution/classification instead of
  asking 00 for routine judgment.
- Accepts only mature A01-approved packages.

## Regression Check Rule

Every accepted `EVOLUTION_EVENT` must end with one `REGRESSION_CHECK`.

```text
REGRESSION_CHECK
Event id:
Future symptom to detect:
Required routing behavior:
Owner window:
Pass condition:
Fail condition:
```

The regression check must be simple enough for a future window or watchdog to
apply without reading the whole project.

## Route Loop Convergence Rule

`docs/CODEX_ROUTE_LOOP_CONVERGENCE_RULE_SET_V0.md` is the A-group process rule
for repeated status/review/route-decision churn.

When A01 sees `2 adjacent docs/control PASSes` in the same family that only
produce status, review, review-status, route-decision, or route-package layers,
A01 must treat the next same-family status proposal as a convergence signal,
not ordinary continuation.

A01 must then apply:

- `Artifact-Shape-Ready Gate`
- `Final-Product Distance Gate`
- `No-New-Status-Doc Gate`
- `Bundle Review Gate`

The route must converge toward one of these outputs:

- a final-product-chain-tagged bundle surface;
- a process/rule patch;
- A-branch classification;
- an explicit blocker or true stop condition supported by reviewed evidence.

Every proposed task must name the final-product chain uncertainty it reduces:
character assets, scene, storyboard/camera, prompt/asset card,
voice/subtitle/SFX, or renderer/package QA. If it cannot name one, A01 must not
approve another status-only slice.

The convergence rule does not permit actual instances, sample JSON,
provider/API work, SQLite changes, renderer/ffmpeg/TTS execution, media
generation, approval, canon, release, or production lock.

### A06 Same-Family Status-Layer Signal

`a06-no-new-same-family-status-layer-without-bundle-v0` is an A06 lightweight
`RULE_UPGRADE_PROPOSAL` / `REGRESSION_CHECK` signal for route families that
reach the 3rd adjacent same-family layer. Same-family layer types are status,
review, review-status, route-decision, route-package, package-review, and
curation.

When this signal appears, A01 must not approve another standalone same-family
layer. A01 must converge to exactly one of these outputs:

- final-product-chain-tagged bounded package candidate;
- explicit blocker / `TRUE_STOP_CONDITION`;
- process/rule patch with regression check.

Every next task must name the final-product chain it reduces: scene packet,
storyboard/camera, prompt/asset cards, voice/subtitle/SFX, renderer/package QA,
or human approval gates. The signal preserves review gates, non-approval
status, no provider/API, no SQLite, no renderer/ffmpeg/TTS, no media
generation, no approval, no canon, no registry, no release, no final approval,
and no production lock. It does not permit A-group windows to dispatch directly
to B02/B03.

## Proactive Evolution Heartbeat Rule

`docs/CODEX_A_GROUP_PROACTIVE_EVOLUTION_HEARTBEAT_RULE_SET_V0.md` is the
A-group process rule for proactive heartbeat control.

A01 must apply the `No Ack-Only Close Rule` when a message contains manual
wakeup, missed handoff, repeated PASS loop, stale package risk, ignored latest
package, or system-not-getting-stronger signals. Only `CONFIG_UPDATE_ONLY`,
update-only, and do-not-start-work messages may close with acknowledgement-only
text.

When a proactive heartbeat is needed, only A01 may coordinate one bounded
`A02 -> A03 -> A04` loop. A02/A03/A04 must not self-restart the loop, and
A-group windows must not dispatch directly to B02/B03.

Every proactive heartbeat must update a `Product-Distance Ledger` entry and
run a `Latest-Package Freshness Check` before A01 approves any package. These
checks are checklist fields, not standalone status-only documents.

The heartbeat rule does not permit actual query instances, sample JSON,
provider/API work, SQLite changes, renderer/ffmpeg/TTS execution, media
generation, approval, canon, release, or production lock.

## Priority Placement In The Blueprint

This route sits before ordinary feature-route work:

```text
00 observed problem
  -> 00-01 分流接頭
  -> A組01 evolution intake
  -> A組02 / A組03 / A組04 or branch, if needed
  -> A組01 blueprint/control integration
  -> 00-01 分流接頭
  -> B組01 only if an AI_TECHNICAL_APPROVED package exists
```

Only after this evolution loop is installed should the project continue normal
route implementation such as query/ref, script-ready packets, storyboard/camera,
asset lifecycle, voice timeline, renderer readiness, or package QA.

## Stop Conditions

Return to 00 only when:

- user worldbuilding or product direction is genuinely missing;
- budget, external provider, paid tool, or media handoff decision is required;
- canon, registry, release, final approval, or production lock is required;
- quota is unsafe;
- A01 explicitly returns `TRUE_STOP_CONDITION`;
- the loop repeats without new evidence.

Routine technical uncertainty, route classification, missing owner, missed
handoff, or no-next-task confusion must go to A01 first, not to 00.

## Hard Boundaries

- No provider/API calls.
- No web/server/open ports.
- No direct SQLite writes.
- No schema/migration changes.
- No renderer/ffmpeg/TTS execution.
- No media generation.
- No direct B02/B03 dispatch by 00, 00-01, or A01.
- No Phase 20/21 restart.
- No 04/05/06/07/08 restart.
- No canon, registry, release, final approval, or production-lock claims.
- No staging, commit, or push unless user explicitly asks.
