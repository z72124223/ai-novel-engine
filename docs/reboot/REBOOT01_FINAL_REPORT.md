# REBOOT-01 Final Report

Date: 2026-07-12

Classification: `PASS_WITH_KNOWN_UNRELATED_FAILURES`

Base SHA: `c0bebd316d4e429abcfa6b0d1b1ff5f08ea9c1ac`

Branch: `reboot/playable-depth-v1`

## Completed operating capabilities

- The existing Kotlin/JVM, SQLite, CLI, Context, Validator, StateReducer, transaction, knowledge, version and hash boundaries are exercised rather than replaced.
- Quality findings remain advisory by default; protocol, world legality and irreversible human approval remain hard boundaries.
- SQL preserves context, raw/revised draft, validation class, state delta, apply result, critic scores and irreversible approval evidence.
- Bounded off-screen NPC autonomy records perception, memory relevance, goal, proposal, Game Master resolution, `DecisionTrace` and observable consequence.
- Drama evaluation records a dramatic question, value conflict, scene-value movement, `AudienceExpectationLedger`, critic scores, bounded revision and stall state.
- The six-turn resettable Chenxing slice contains two motivated NPCs, one understandable opposing actor, off-screen action, value dilemma, danger recognition, fair second-order consequence, daily banter with social friction, grief restraint, local closure and an unresolved continuation.
- The exact non-Canon world patch candidate passes `parse → validate → diff → impact preview → sandbox apply`, then appears in SQL, Context, NPC Runtime and the Scenario.
- The local Web page is now a `LOCAL_ENGINE_BACKED_SCENARIO`: every submit rebuilds an isolated SQLite sandbox and executes the real `prepare → validate → resolve` path through that turn. Its deterministic scenario adapter is intentionally not described as a free-form model.
- Same-seed replay compares isolated databases and full normalized story-state fingerprints.

## World patch and reality evidence

```text
candidate: samples/reboot01/chenxing_world_patch_candidate_v1.json
SHA-256: 57bb8ddeeae6908e307425b1d1e7f98f27c49df142d8378d3dabc1e464e2b0cf
validation: valid=true
diff entries: 8
sandbox apply: applied=true
Canon mutation allowed: false
```

Long-term evidence is under `samples/reboot01/vertical_slice_run/`:

- `world-patch-parse.json`
- `world-patch-validation.json`
- `world-patch-diff.json`
- `world-patch-impact-preview.json`
- `world-patch-sandbox-apply.json`
- `turn-01-context.json`
- `turn-02-decision-trace-row.json`
- `sql-state-snapshot.json`
- `protagonist-growth-evidence.json`

The local machine-readable cross-layer inventory remains at `out/reboot01/world_audit/world_reality_inventory.json` because `out/**` is intentionally ignored.

## Final scenario evidence

```text
turn logs: 6
state deltas: 15
events: 10
APPLIED run evidence: 6
DecisionTrace rows: 1
drama evaluations: 6
irreversible approvals: 1
final State Version: 6
final world time: T+35m
antagonist health: permanent_hand_injury
sandbox canon_mutation_allowed: 0
```

The slice separately exercises:

- `DAILY_QUIP`: turn 3 daily friction with Zhou Ke, followed by an observable social response;
- `DANGER_OBSERVATION`: turns 1 and 4 recognize visible danger and take a countermeasure;
- `GRIEF_RESTRAINT`: turns 5 and 6 suppress humor after permanent injury and respond through concrete care;
- `IDIOT_PLOT_RISK`: negative critic tests fail unsupported risk-taking;
- fair surprise: safe countermeasure succeeds, while the opponent adapts and moves the real evidence;
- protagonist growth evidence across knowledge, judgment, relationships, influence, burden, compromise risk, loss and false-belief correction;
- `local_closure + persistent_world_continuation`: the injury is stabilized and a conditional truce forms, while the second mover remains unresolved.

## Verification

- Compile: PASS.
- Completion targeted tests: PASS.
- Fresh six-turn CLI end-to-end: PASS.
- World-patch JSON chain: PASS.
- Same-seed replay: PASS / `ADOPT`.
- Engine-backed loopback Web, six submits, Context-hash divergence, debug, save/load: PASS.
- SQL evidence and irreversible boundary queries: PASS.
- Credential scan: PASS.
- Formal Chenxing Canon diff: empty.
- Existing image diff: empty.
- `git diff --check`: PASS.
- Full suite: 843 total, 839 PASS, 4 known World Source #09 authority failures, 0 errors, 0 skipped.

The four known failures remain:

```text
WorldSourceArchiveImportCliTest.cliAppliesWorldSourceArchiveIntoSqlite
WorldSourceArchiveImportServiceTest.validatesChenxingWorldSourceArchiveManifest
WorldSourceArchiveImportServiceTest.importsChenxingWorldSourceArchiveIntoSqlite
WorldSourceQueryCliTest.cliQueriesImportedOfficialWorldSourceChunks
```

The tracked source is 3051 bytes with SHA-256 `425582...B54F`, while the formal manifest declares an older 3019-byte payload and hash. REBOOT-01 does not rewrite either authority source to hide the mismatch.

## Honest limitations

- The Web result adapter is deterministic and Scenario-bounded. It is engine-backed, but it is not an LLM, free-form authoring model or unlimited branching game.
- There is no paid provider, model gateway, local LLM adapter, public deployment, login or cloud database.
- Narrative-quality advisories around oral candidates, beat matching, transition availability and scheduled pressure remain technical debt.
- The World Source #09 authority mismatch remains unresolved.
- No formal Canon promotion, visual approval, image generation or video generation occurred.

## Model, Subagents and cost

| Stage / audit | Main model / reasoning | Subagents | Retry / escalation | Token / currency |
| --- | --- | --- | --- | --- |
| Stage 0 | `gpt-5.6-sol / medium` | repository explorer, Runtime tester, cost auditor | baseline compile/runtime retries recorded in journal | `NOT_AVAILABLE` |
| Stage 1 | `gpt-5.6-sol / medium`, targeted High review | high-risk reviewer | one High safety review | `NOT_AVAILABLE` |
| Stages 2–6 | `gpt-5.6-sol / medium` | earlier Subagent capacity was unavailable; main agent used Repository evidence | no routine High | `NOT_AVAILABLE` |
| Stage 7 | `gpt-5.6-sol / medium`, final High review | high-risk reviewer | initial BLOCK, fixes, final PASS | `NOT_AVAILABLE` |
| Completion audit | `gpt-5.6-sol / medium`, final High review | Terra Low explorer, Terra Medium Runtime tester, Sol Medium narrative critic, Luna Low cost auditor; then High reviewer | initial world/Web BLOCK, safety fixes, close-race BLOCK, lifecycle fix, final review; no Max/Ultra | `NOT_AVAILABLE` |

External provider calls: `0`. Provider spend: `0`. No cost value is estimated when the platform does not expose it.

## Image lane

The image lane remains paused after the Daping panorama candidate. It is not Canon, Production Lock or a final location. Tasks 02–07 remain blocked. REBOOT-01 changed no image or visual authority file.

## Recommended next step

Review and merge this branch. A later package may add richer branching or a provider-neutral manual/local result adapter, while preserving this deterministic Scenario as a regression fixture. Do not connect a paid provider without separate authorization.
