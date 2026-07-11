# REBOOT-01 Final Report

Date: 2026-07-12

Classification: `PASS_WITH_KNOWN_UNRELATED_FAILURES`

Base SHA: `c0bebd316d4e429abcfa6b0d1b1ff5f08ea9c1ac`

Branch: `reboot/playable-depth-v1`

## Completed operating capabilities

- Existing Kotlin/JVM, SQLite, CLI, Context, Validator, StateReducer, transaction, knowledge, version, and hash boundaries were exercised rather than replaced.
- Quality findings are advisory by default while protocol, world legality, and irreversible approval remain hard boundaries.
- SQL stores run evidence, raw/revised drafts, validation classification, state delta, apply result, model metadata, and irreversible approval evidence.
- A bounded off-screen NPC loop records perception, memory relevance, goal, proposal, Game Master resolution, DecisionTrace, and observable consequence.
- Drama evaluation records a dramatic question, value conflict, scene-value movement, Audience Expectation Ledger, eleven scores, concrete findings, stall state, and at most one revision.
- The resettable non-Canon Chenxing slice applies six turns with two important independent NPCs, one opposing actor, an off-screen event, value dilemma, fair second-order consequence, and an approved permanent sandbox injury.
- Same-seed replay compares isolated baseline/evolved databases and learned-rule stores using a normalized story-state fingerprint.
- A loopback-only local Web page supports six submit steps, story, world time, character/relationship summaries, save/load, and developer evidence views.

## Actual final evidence

```text
turn logs: 6
state deltas: 10
events: 5
APPLIED run evidence: 6
DecisionTrace rows: 1
drama evaluations: 5
irreversible approvals: 1
final State Version: 6
final world time: T+35m
antagonist health: permanent_hand_injury
sandbox canon_mutation_allowed: 0
```

The final replay comparison used seed `reboot01-chenxing-depth-v1`:

```text
normalized fingerprint (both): 33600a42f1ae18d033f4cc7131ebffc246652378431d5b3f2ddfd851c43a2447
legal apply rate: 1.0 → 1.0
stall count: 0 → 0
autonomy score: 100 → 100
emotional wave coverage: 85.0 → 85.0
overall drama score: 70.4 → 70.4
fair surprise: 57.0 → 57.0
reader attachment: 73.0 → 73.0
retry count: 1 → 0
decision: ADOPT
```

## Verification

- Compile: PASS as part of targeted and full Gradle runs.
- REBOOT targeted selection: 31/31 PASS.
- Full suite: 840 total, 836 PASS, 4 FAIL, 0 errors, 0 skipped.
- Final six-turn CLI end-to-end: PASS.
- Replay benchmark: PASS / `ADOPT`.
- Real loopback Web smoke, six submits, debug, save/load: PASS.
- SQL evidence query: PASS.
- High-confidence secret/API-key scan: PASS.
- Formal Chenxing Canon diff: empty.
- Existing image diff: empty.
- `git diff --check`: PASS before final staging.

The four failing tests are unchanged World Source #09 manifest integrity failures:

```text
WorldSourceArchiveImportCliTest.cliAppliesWorldSourceArchiveIntoSqlite
WorldSourceArchiveImportServiceTest.validatesChenxingWorldSourceArchiveManifest
WorldSourceArchiveImportServiceTest.importsChenxingWorldSourceArchiveIntoSqlite
WorldSourceQueryCliTest.cliQueriesImportedOfficialWorldSourceChunks
```

The tracked source is 3051 bytes with SHA-256 `425582...B54F`, while the formal manifest still declares an older 3019-byte payload and hash. REBOOT-01 did not rewrite either authority source to hide the mismatch.

## Incomplete or intentionally excluded

- The local Web UI is a verified fixed scenario, not a live free-form model loop.
- No provider-neutral model gateway or local LLM adapter is implemented.
- The World Source #09 authority mismatch remains unresolved.
- Narrative-quality advisories around oral candidates, beat matching, transition availability, and scheduled pressure remain technical debt.
- No formal Canon promotion, visual approval, image generation, video generation, public deployment, login, cloud database, or paid provider integration occurred.

## Model, reasoning, subagents, and cost

- Main implementation: `gpt-5.6-sol / medium`, Stages 0–7.
- High escalation: Stage 1 read-only safety review and Stage 7 final read-only safety review only.
- Stage 7 High review initially blocked self-issued approval, incomplete replay coverage, and DNS-rebinding exposure. All three were corrected; the final High re-review returned `PASS`.
- Stage 2 explorer attempt produced no result because the then-current subagent usage limit was exhausted; local Repository evidence was used instead.
- External model/provider calls: 0.
- Provider spend: 0.
- Exact Codex token/currency cost was not exposed to the task, so no fabricated number is reported.

## Retired rules

- Narrative quality is no longer automatically a persistence hard blocker; strict quality mode remains available.
- The permanent no-Web rule is retired only for the approved loopback-only local interface. Public Web, cloud, login, and deployment remain prohibited in REBOOT-01.
- REBOOT internal stages no longer require the user to relay every ordinary decision back to GPT.
- Images or scripts are not treated as the only terminal product; the runnable engine loop is again the mainline evidence.

## Image lane status

The image lane remains paused after `daping-lb-01-master-panorama` candidate task 01. The kept file remains a candidate only; it is not Canon, Production Lock, or a final location. Tasks 02–07 remain blocked by prior-task approval. REBOOT-01 changed no image or visual authority file.

## Recommended next product step

Open a separately approved `REBOOT-02 Live Turn Gateway` package: connect the local UI to the existing `prepare-turn → structured result → validate → resolve-turn` loop through a provider-neutral manual/local adapter first. Preserve the fixed replay as a regression fixture. Do not add a paid provider until the live local contract, error repair loop, branch behavior, and author-visible evidence are independently tested.

Do not start REBOOT-02 automatically. Review and merge this branch first.
