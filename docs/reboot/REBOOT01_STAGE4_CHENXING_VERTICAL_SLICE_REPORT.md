# REBOOT-01 Stage 4 — Chenxing Non-Canon Vertical Slice

Date: 2026-07-12

Status: `STAGE_4_PASS`

## Executed slice

The local runner applied the formal Chenxing setting pack to a fresh temporary database, then applied `samples/reboot01/chenxing_world_patch_candidate_v1.json`. The candidate is explicitly non-Canon, resettable and promotion-forbidden; separate parse, validation, diff, impact-preview and sandbox-apply evidence is preserved.

The six-turn slice includes:

- 陸塵行 and 愛醬;
- 林遙 and 周克, each with an independent goal;
- 拾潮者 as the opposing actor;
- a dramatic question about preserving evidence without sacrificing people;
- a value conflict between proof, safety, and responsibility;
- one accepted off-screen action by 林遙;
- one approved, irreversible Runtime injury to the sandbox antagonist;
- humor, warmth, conflict, loss/reflection, a conditional truce, and an open question.

All story-state changes were applied through the existing prepare/validate/resolve transaction path. The off-screen action used the Stage 2 proposal, legality, DecisionTrace, and transaction boundary. The irreversible injury used the Stage 1 contract and separate human approval artifact. No formal Canon source was edited.

## Actual run result

```text
applied turns: 6
final state version: 6
elapsed world minutes: 35
decision traces: 1
drama evaluations: 6
run evidence rows: 6
irreversible approvals: 1
```

Long-term evidence is stored under `samples/reboot01/vertical_slice_run/`, including the five world-patch pipeline records, turn contexts, AI results, reader narratives, six drama/critic records, the off-screen result and DecisionTrace row, session evidence, protagonist growth evidence and the final SQL snapshot. The executable reset test also checks the antagonist injury and verifies that every sandbox world-patch candidate has `canon_mutation_allowed=0`.

## Verification

- `RebootVerticalSliceRunnerTest` plus Stage 1–3 related suites: 28/28 PASS.
- Generated JSON: 32/32 parsed successfully, including the world-patch pipeline and separate user-authority approval grant.
- `git diff --check`: PASS before staging.
- Original dirty worktree: untouched.

## Observed quality debt

The real run produced narrative-quality advisories and local learned-rule suggestions, but no hard quality block. These suggestions remain ignored local output under `out/reboot01/` and were not staged. Stage 5 must use the replay benchmark to decide whether one evidence-backed rule adjustment improves the slice; advisories alone are not proof.

No image task, provider, external model API, Canon promotion, or public deployment was started.
