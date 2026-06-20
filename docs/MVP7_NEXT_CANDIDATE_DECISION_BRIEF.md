# MVP-7 Next Candidate Decision Brief

Date: 2026-06-20

Status: `DECISION_BRIEF_ONLY`

This document compares possible next directions after MVP-7 Candidate A, A.1, and A.2. It is not an implementation approval. It does not open Candidate B, does not change runtime behavior, does not change audit rules, and does not touch SQLite, Validator, `stateVersion`, `contextHash`, `resolve-turn`, or `StateReducer`.

## 1. Current Situation

Confirmed from the current control docs and MVP-7 reports:

- MVP-6 is stable and tagged at `mvp-6`.
- Candidate A, A.1, and A.2 are complete.
- Candidate A.2 is still read-only audit calibration.
- A.2 did not make normal samples noisy.
- A.2 caught important flawed samples, including direct emotion telling and missing consequence cases.
- A.2 still is not ready to become a pre-resolve gate.
- Existing audit samples are useful but still too narrow: many are Pilot-1, Pilot-1b, MVP-4, MVP-6, or deliberately synthetic fixtures.
- The automatic stable-completion loop stopped because the remaining options require human direction.

## 2. Recommendation Ranking

| Rank | Direction | Recommendation | Why |
| ---: | --- | --- | --- |
| 1 | More real audit samples | Safest next step if the user wants better evidence. | Improves confidence without code, schema, or write-path changes. |
| 2 | Pilot-2 scenario design | Good next step if the user wants to continue story playtesting. | Creates a better real-use scenario before more implementation. |
| 3 | Candidate A.3 read-only audit calibration | Reasonable only after more samples identify a precise small gap. | Still safe if kept read-only, but needs a concrete target. |
| 4 | Candidate B pre-resolve quality gate | Not recommended now. | High risk because it approaches `resolve-turn`, Validator-adjacent behavior, false positives, and author workflow friction. |
| 5 | UI / product direction | Too early for implementation. | The core story loop and quality signals need more real use before UI investment. |

Safest next step: collect more real audit samples or design Pilot-2. If the user wants a code milestone, first choose Candidate A.3 with a narrow read-only target. Do not jump to Candidate B.

## 3. Candidate A.3: Continue Read-Only Audit Calibration

### Purpose

Improve `audit-narrative-quality` as a reviewer aid without moving it into the formal turn write path.

Candidate A.3 should be chosen only if there is a specific read-only issue to improve, such as:

- better style-tone detection,
- clearer distinction between synthetic acceptance fixtures and real story turns,
- more precise severity labels,
- better report grouping for context-quality observations,
- stronger sample metadata in audit reports.

### Benefits

- Low risk.
- Keeps SQLite untouched.
- Keeps `resolve-turn` and `StateReducer` untouched.
- Can be tested with fixtures.
- Helps the user understand story quality without blocking play.

### Risks

- Repeated audit calibration can become busywork if not tied to real samples.
- Heuristics can overfit the current sample pack.
- More warnings may feel noisy if severity is not calibrated.
- It still does not solve missing real story coverage by itself.

### Needed Samples

Candidate A.3 should not start from vague concerns. It needs at least one of:

- 5 to 10 new real story turns from a different story arc.
- A small flawed sample pair that demonstrates a known miss.
- A normal sample pair that demonstrates a false positive.
- A real author complaint such as "this warning is useless" or "this bad prose passed cleanly."

### Completion Criteria

- A focused A.3 plan exists before implementation.
- The implementation stays read-only and requires no `--db`.
- `git diff --check` passes.
- `gradlew.bat clean test --no-daemon` passes.
- Repo CLI and installDist audit smoke pass.
- Existing false-positive guards remain quiet.
- New or updated samples prove the targeted improvement.
- Generated audit reports remain under ignored `out/`.

### Blockers

- No precise target.
- No new evidence beyond the current sample set.
- Any desire to block `resolve-turn` or mutate formal state.

## 4. Candidate B: Pre-Resolve Quality Gate

### Purpose

Run narrative quality checks before formal turn resolution so obviously bad result JSON can be stopped, warned, or sent back for human revision.

### Benefits

- Could prevent poor quality turns from becoming formal history.
- Could catch repeated stasis, unsupported character behavior, or obvious AI-feeling prose earlier.
- Could help enforce author review discipline.

### High-Risk Points

- False positives could block good turns.
- Context-quality warnings may be mistaken for result defects.
- Synthetic acceptance fixtures could fail for reasons unrelated to story quality.
- Human author workflow may become frustrating if warnings become hard failures.
- The audit is subjective; Validator is security/correctness-oriented. Mixing them is risky.

### Runtime Boundaries It Would Touch Or Approach

Candidate B would likely touch or need a design around:

- `resolve-turn`
- `AiTurnResultValidator`
- `StateReducer`
- `stateVersion`
- `contextHash`
- pending turn lifecycle
- transaction boundaries
- CLI result flow
- retry behavior after warnings
- report persistence or evidence export

Touching these areas is not approved.

### Why It Cannot Start Automatically Now

- Candidate B is explicitly not approved in current roadmap/control docs.
- A separate safety design is required before any write-path integration.
- A.2 findings are useful but still advisory.
- The sample set is not diverse enough.
- The current audit is not calibrated as a hard gate.
- The user has explicitly forbidden changes to Validator, `stateVersion`, `contextHash`, `resolve-turn`, `StateReducer`, schema, and SQLite writes unless separately approved.

### Required Conditions Before Entering Candidate B

Before Candidate B can even be discussed as implementation:

- A separate Candidate B spec must define exact entry point and non-goals.
- Human override behavior must be specified.
- False-positive tolerance must be defined.
- All finding severities must be reviewed.
- Context-quality observations must be separated from result-quality failures.
- At least one additional real story arc should be audited.
- The design must prove Validator, `stateVersion`, `contextHash`, transactions, and SQLite boundaries remain intact.
- Rollback behavior must be documented.

### Completion Criteria If Later Approved

Not approved now. If approved later, completion would require:

- new spec,
- focused tests,
- CLI E2E against fresh DB,
- no old migration edits,
- explicit retry behavior,
- proof that failed quality checks do not write state,
- documentation updates in `TURN_PROTOCOL.md`, `VALIDATION_RULES.md`, and `VERIFY.md` if protocol behavior changes.

### Blockers

- No Candidate B spec.
- No explicit approval.
- Audit findings are advisory, not validated as gate-ready.
- Write-path safety design does not exist yet.

## 5. Pilot-2 Scenario Design

### Purpose

Design a second real-use story validation scenario that generates stronger evidence for memory, scene movement, world pressure, NPC agency, setting context, and narrative quality.

Pilot-2 is not necessarily code work. It can be a scenario, setting pack, test protocol, and evidence plan.

### Best Fit

Pilot-2 is a good choice if the user wants to continue interactive fiction playtesting and produce better real samples before more engineering.

It is especially useful if the next question is:

- "Does the engine feel good in a new story arc?"
- "Does audit quality generalize beyond the snow dog Pilot?"
- "Can the runtime support a different scene, faction, or conflict style?"

### Does It Need Real Story Samples?

Yes, if it is meant to prove quality. A scenario design alone can be written, but useful validation requires real turns.

Minimum useful sample:

- 8 to 12 player turns,
- per-turn context/result/evidence,
- at least one scene transition attempt,
- at least one scheduled world event,
- at least one NPC refusal or conditional cooperation,
- at least one memory recall after several turns,
- at least one player choice that delays or redirects a thread.

### Does It Change Code?

Not by default.

Pilot-2 can be documentation-only or setting-pack/sample work. It should not change Kotlin, migrations, Validator, `resolve-turn`, `StateReducer`, or SQLite directly unless a later separate milestone explicitly approves a bugfix.

### Blockers

- The user must choose or approve a scenario premise.
- Real turns require human input.
- If a new setting pack is needed, it must go through existing setting-pack CLI flow, not direct SQL.

### Completion Criteria

- Scenario brief exists.
- Turn protocol for evidence collection is defined.
- Required coverage items are listed.
- No code changes unless separately approved.
- If turns are executed, they use fresh DB/session and formal CLI commands.

## 6. More Real Audit Samples

### Purpose

Improve confidence in `audit-narrative-quality` by testing it against real author-led story turns outside the current Pilot-1/Pilot-1b/MVP synthetic fixture family.

### What The User Should Provide

The easiest input format:

```text
Story arc name:
Short premise:
Current scene:
Player character:
Important NPCs:
Current conflict:
Player turn 1:
Player turn 2:
Player turn 3:
...
```

If using the existing engine session flow, each turn should eventually produce:

- context JSON,
- result JSON,
- evidence JSON if available,
- brief human note on whether the turn felt good, too mechanical, too obedient, too slow, or too confusing.

### How To Avoid Thin Samples

Do not use only simple "ask NPC a question" turns. Include:

- a request an NPC may refuse,
- a risky action with possible cost,
- a scene movement or blocked movement,
- a scheduled pressure or offscreen world event,
- a memory cue that should matter later,
- a faction/rule constraint,
- a player boundary that should not be overwritten.

### How To Judge Whether The Audit Is Accurate

For each sample, compare:

- audit verdict and score,
- warning count and rule IDs,
- human judgment,
- whether findings cite real evidence,
- whether suggested fixes are usable,
- whether warnings are too noisy,
- whether obvious problems were missed.

### Blockers

- Requires user-provided or user-approved story turns.
- Existing samples are not enough to prove quality across story types.
- Without human judgment, audit accuracy can only be approximated.

### Completion Criteria

- At least 10 new real or near-real turn pairs are audited.
- At least 2 different story situations are represented.
- Findings are summarized in a new report.
- No SQLite writes are made by the audit.
- Generated reports remain ignored under `out/`.

## 7. UI / Product Direction

### Is It Suitable Now?

Not as an implementation milestone.

The engine is still primarily a local CLI runtime and JSON protocol. UI work would add product-surface decisions before the authoring workflow and quality gates are settled.

### Needed Preconditions

Before UI work:

- Decide whether the product is for author tooling, player-facing play, or engine debugging.
- Choose local-only UI constraints without violating no-server/no-port rules.
- Define how UI would read/write JSON without bypassing Validator and CLI/runtime boundaries.
- Confirm whether generated evidence, contexts, and audit reports need visual browsing.
- Decide if UI is allowed at all, since current constraints prohibit Web UI and Android UI.

### Why It May Be Too Early

- Candidate B is not approved.
- More real story samples are needed.
- The author workflow is still being clarified.
- A UI could hide important protocol evidence and make debugging harder.
- It risks pulling the project toward web/server/product architecture that is explicitly forbidden.

### Blockers

- No UI spec.
- Current roadmap says UI is not approved.
- Web UI, server, port, Android UI, and external API work are forbidden.

### Completion Criteria If Later Approved

Not approved now. A future UI direction would first need:

- explicit scope,
- allowed technology list,
- no-server/no-port proof or roadmap change,
- CLI/runtime safety boundary design,
- verification plan.

## 8. Not Recommended Directly

Do not directly do these next:

1. Candidate B implementation.
   - Too close to formal write path.
   - Needs separate spec and approval.

2. MVP-8.
   - Not defined.
   - Not approved.

3. UI implementation.
   - Too many product decisions unresolved.
   - Current constraints forbid common UI routes.

4. Schema or migration changes.
   - No current evidence requires them.

5. Audit rule changes without a new target.
   - A.2 is stable enough to pause.
   - Further changes need new evidence or a selected A.3 target.

## 9. If The User Wants To Continue Interactive Fiction Playtesting

Provide one of these:

### Option A: Continue Existing Style

```text
我想做 Pilot-2。

作品名稱：
類型與基調：
主角：
重要 NPC：
主要地點：
目前衝突：
公開事實：
秘密：
開場場景：
我希望驗證的品質：
```

### Option B: Just Give Player Turns

```text
使用現有或新建 session。
玩家輸入 1：
玩家輸入 2：
玩家輸入 3：
```

### Option C: Audit Existing Outputs

```text
請對以下 context/result/evidence 做 audit sample collection：
context:
result:
evidence:
我覺得這回合的問題是：
```

The strongest evidence comes from Option A plus 8 to 12 turns.

## 10. If The User Wants Candidate B

The user must explicitly approve these risks before any Candidate B spec or implementation:

- Candidate B may block or warn on result JSON before formal resolution.
- It may require touching or wrapping `resolve-turn`.
- It may require new retry or override behavior.
- It may create false positives that slow story play.
- It must separate quality findings from security/correctness validation.
- It must not write SQLite on failed checks.
- It must preserve `stateVersion`, `contextHash`, Validator, transaction, and idempotency boundaries.
- It needs rollback and emergency bypass policy.
- It needs fresh tests and CLI E2E.

Recommended approval wording if the user really wants only a spec:

```text
批准起草 Candidate B decision brief，只做文件，不實作，不改 runtime。
```

Recommended approval wording if the user later wants implementation:

```text
批准實作 Candidate B pre-resolve quality gate，並接受需要設計 false-positive、人為 override、retry、transaction safety 的風險。先寫 spec，通過後再實作。
```

## 11. Final Recommendation

Recommended order:

1. More real audit samples, if the user can provide real story turns.
2. Pilot-2 scenario design, if the user wants to keep playing and produce better evidence.
3. Candidate A.3 read-only calibration, only after a specific new issue is proven.
4. Candidate B decision brief, only if the user deliberately wants to explore a risky gate.
5. UI/product direction, only after the author workflow and allowed technology surface are clarified.

Most safe next command for the user:

```text
批准設計 Pilot-2 scenario，不改程式，只產生設定、驗收項目與真人回合收集格式。
```

If the user wants zero story input:

```text
批准起草 Candidate A.3 read-only audit calibration brief，只做文件，不實作。
```

## 12. Verdict

`PASS_WITH_DECISION_PENDING`

The project is stable enough to choose a next direction, but not enough to auto-enter Candidate B. The next safest work is evidence collection or a documentation-only scenario/candidate brief.
