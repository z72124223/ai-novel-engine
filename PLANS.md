# Plan

## Goal

Create and maintain docs-only authoring quality, scene-production boundary, script-ready packet, Qingli scene registry seed, and project-control notes that preserve playtest learning about narrative prose, NPC consistency, review workflow, narrative review gates, anti-AI-writing guidance, micro-prose review, sensory attribution, epoch / material culture consistency, protagonist reaction logic, initial survival orientation / goal transition, scene lock / animation asset planning, 04/05/06 handoff contracts, and manual 01/02/03 engineering-loop rules while clearly separating engine layer rules from content / setting pack specifics.

## Global Strategy

Treat 04 playtest learning as authoring evidence, not runtime approval. Keep focused docs that can guide future engine prompts, review checklists, scene packet preparation, and setting-pack authoring without hardcoding Qingli content, Ai-chan details, or copyrighted external styles into engine core. For the current milestone, draft a conservative Qingli scene registry seed brief from existing formal scene ids, setting pack public scene/location data, and evidence docs, without turning it into schema, runtime scene definitions, or approved animation asset lock.

## Non-Goals

- No Kotlin source changes.
- No tests or runtime behavior changes.
- No schema or migration changes.
- No Validator, StateReducer, `resolve-turn`, `stateVersion`, or `contextHash` changes.
- No direct SQLite writes.
- No `mvp-6` retagging.
- No deletion of `data/`, `out/`, or backups.
- No committing ignored artifacts.
- No hardcoding Qingli, Ai-chan, specific NPCs, or copyrighted work elements as engine core.

## Scope

Tracked files allowed:

- `PLANS.md`
- `docs/AUTHORING_QUALITY_GUIDELINES.md`
- `docs/SCENE_LOCK_AND_ANIMATION_ASSET_BOUNDARY.md`
- `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`
- `ROADMAP.md`
- `VERIFY.md`
- `DONE_CRITERIA.md`
- `docs/HANDOFF.md`
- `docs/QINGLI_SCENE_REGISTRY_SEED_BRIEF.md`

Docs to inspect:

- `AGENTS.md`
- `PLANS.md`
- `ROADMAP.md`
- `VERIFY.md`
- `DONE_CRITERIA.md`
- relevant existing narrative-quality docs found by search, especially `docs/MVP7_NARRATIVE_QUALITY_SPEC.md` and Pilot-2 scenario/turn docs.

## Confirmed Facts

- Current branch is `codex/mvp6-checkpoint-branching-spec`.
- Working tree was clean at milestone start.
- `ROADMAP.md` records MVP-7 narrative-quality work as completed read-only audit/spec work and does not approve Candidate B.
- `VERIFY.md` requires `git diff --check` and normally `gradlew.bat clean test --no-daemon`; the current user instruction allows skipping full Gradle for docs-only if explained.
- `DONE_CRITERIA.md` says documentation/control milestones need docs to match current behavior and `git diff --check` to pass.
- Search found `docs/MVP7_NARRATIVE_QUALITY_SPEC.md` as the existing narrative-quality spec.
- Search did not find an existing dedicated authoring quality guidelines file that separates engine layer rules from content / setting pack layer specifics.
- User instruction for this milestone explicitly requests docs-only authoring quality / style guide / review workflow work and forbids runtime/schema/Validator/StateReducer changes.
- User instruction for the current milestone explicitly requests docs-only script-ready scene packet, scene registry, 04 generation, 05 review, and 06 display contracts before 04/05/06 resume.
- Active addendum states that the project will not use automation / scheduler handoff for this workflow. Human approval must follow `01 explicit handoff -> 04 generation -> 05 review -> 06 display -> user explicit approval -> report to 01 -> 01 assigns next step`.
- Existing docs already include authoring review gate guidance and scene lock / animation asset boundary guidance, but search did not find a dedicated script-ready scene packet specification.
- 03 passed commit `63cc2f17b968dd9f1c9fff602150802d1ebf2ff2` (`docs: define script-ready scene packet contracts`).
- `ROADMAP.md`, `VERIFY.md`, `DONE_CRITERIA.md`, and `docs/HANDOFF.md` still described an older control state before the script-ready packet contract track.
- User instruction for the current milestone explicitly allows docs-only verification without full Gradle when explained, but code/runtime/schema/CLI milestones must keep full tests and E2E.
- 03 passed commit `834533e6e3ea7fa18894b1e7eb0391c964c3364f` (`docs: align project control after script packet contracts`).
- `ROADMAP.md` lists `scene registry seed brief` as a safe docs-only next candidate.
- The Pilot-2 setting pack file exists at `out/pilot2/bootstrap/pilot2_setting_pack.json`.
- The formal scene ids confirmed from docs and setting pack are `scene-qingli-herb-warehouse-perimeter`, `scene-qingli-herb-awning`, `scene-qingli-runoff-channel`, and `scene-qingli-outer-waiting-line`.
- The setting pack has scene contracts with forbidden reveal boundaries for those scenes; this milestone must record the boundary without copying secret contents into user-facing prose.

## Assumptions

- `docs/AUTHORING_QUALITY_GUIDELINES.md` is the right location because the requested content is cross-cutting authoring guidance, not a Pilot-only report.
- Full Gradle test is not necessary for this docs-only milestone if `git diff --check` passes and no code/runtime files are touched.

## Open Questions

- Whether future milestones should turn parts of the guideline into prompt templates, setting-pack schema fields, or read-only audit checks. This milestone does not decide that.
- Whether future milestones should turn the packet contract into runtime schema, Validator rules, UI forms, or Oiioii integration. This milestone does not decide that.
- Whether 04/05/06 should resume after these contracts is a 01/user decision. This milestone does not resume them.
- Whether Qingli scene registry seed entries should become formal setting-pack fields or approved animation asset locks requires a later 01/user-approved milestone.

## Implementation Steps

- [x] Step 1: Read AGENTS, PLANS, ROADMAP, VERIFY, DONE_CRITERIA, and search existing docs for authoring/narrative-quality coverage.
- [x] Step 2: Update `PLANS.md` for this docs-only authoring quality boundary milestone.
- [x] Step 3: Add `docs/AUTHORING_QUALITY_GUIDELINES.md`.
- [x] Step 4: Run `git diff --check` and inspect diff scope.
- [x] Step 5: Commit `docs: add authoring quality guidelines` and push tracked docs only.
- [x] Step 6: Directly notify 03 read-only review thread.
- [x] Step 7: Add micro-prose review addendum for AI calibration phrasing and visible nonverbal action.
- [x] Step 8: Run `git diff --check` for addendum scope.
- [x] Step 9: Commit `docs: add micro-prose authoring review guidance` and push tracked docs only.
- [x] Step 10: Directly notify 03 read-only review thread for the addendum.
- [x] Step 11: Add epoch / material culture / tech-level consistency addendum.
- [x] Step 12: Run `git diff --check` for tech-level addendum scope.
- [x] Step 13: Commit `docs: add material culture consistency guidance` and push tracked docs only.
- [x] Step 14: Directly notify 03 read-only review thread for the tech-level addendum.
- [x] Step 15: Add protagonist reaction beats addendum.
- [x] Step 16: Run `git diff --check` for protagonist reaction addendum scope.
- [x] Step 17: Commit `docs: add protagonist reaction beat guidance` and push tracked docs only.
- [x] Step 18: Directly notify 03 read-only review thread for the protagonist reaction addendum.
- [x] Step 19: Add initial survival orientation and goal transition addendum.
- [x] Step 20: Run `git diff --check` for orientation addendum scope.
- [x] Step 21: Commit `docs: add opening goal transition guidance` and push tracked docs only.
- [x] Step 22: Directly notify 03 read-only review thread for the orientation addendum.
- [x] Step 23: Add sensory attribution and body-part logic addendum.
- [x] Step 24: Run `git diff --check` for sensory attribution addendum scope.
- [x] Step 25: Commit `docs: add sensory attribution guidance` and push tracked docs only.
- [x] Step 26: Directly notify 03 read-only review thread for the sensory attribution addendum.
- [x] Step 27: Add narrative review gate and eight-point checklist addendum.
- [x] Step 28: Run `git diff --check` for review gate addendum scope.
- [x] Step 29: Commit `docs: add narrative review gate checklist` and push tracked docs only.
- [x] Step 30: Directly notify 03 read-only review thread for the review gate addendum.
- [x] Step 31: Add scene lock and animation asset boundary docs.
- [x] Step 32: Run `git diff --check` for scene lock addendum scope.
- [x] Step 33: Commit `docs: add scene lock asset boundary guidance` and push tracked docs only.
- [x] Step 34: Directly notify 03 read-only review thread for the scene lock addendum.
- [x] Step 35: Add script-ready scene packet, scene registry, 04 generation, 05 review, 06 display, human approval, and 01/02/03 loop contracts.
- [x] Step 36: Run `git diff --check` and inspect diff/stat/staged scope for script packet contract docs.
- [x] Step 37: Commit `docs: define script-ready scene packet contracts` and push tracked docs only.
- [x] Step 38: Directly notify 03 read-only review thread for the script packet contract milestone.
- [x] Step 39: Align ROADMAP / VERIFY / DONE_CRITERIA / HANDOFF with completed script packet contracts, manual handoff only, and docs-only verification rules.
- [x] Step 40: Run `git diff --check` and inspect diff/stat/staged scope for project-control docs alignment.
- [x] Step 41: Commit `docs: align project control after script packet contracts` and push tracked docs only.
- [x] Step 42: Directly notify 03 read-only review thread using a 03 verifier-window prompt.
- [x] Step 43: Add Qingli scene registry seed brief for the four formal Pilot-2 scene ids, keeping author-only boundaries non-public.
- [x] Step 44: Run `git diff --check` and inspect diff/stat/staged scope for the scene registry seed brief.
- [x] Step 45: Commit `docs: seed Qingli scene registry brief` and push tracked docs only.
- [x] Step 46: Directly notify 03 read-only review thread using a 03 verifier-window prompt.

## Verification

Required:

- `git status`
- `git diff --check`
- `git diff --stat`

Not planned:

- Full Gradle test, for explicitly docs-only project-control milestones where 01/user permits the docs-only exception and no Kotlin, tests, schema, runtime, or CLI behavior is touched. If the final diff expands beyond docs/control files, rerun full tests.

Safety checks:

- Confirm only `PLANS.md` and `docs/AUTHORING_QUALITY_GUIDELINES.md` are staged.
- For scene lock addendum, also allow `docs/SCENE_LOCK_AND_ANIMATION_ASSET_BOUNDARY.md`.
- For script packet contract milestone, also allow `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`.
- For project-control alignment, also allow `ROADMAP.md`, `VERIFY.md`, `DONE_CRITERIA.md`, and `docs/HANDOFF.md`.
- For Qingli scene registry seed brief, also allow `docs/QINGLI_SCENE_REGISTRY_SEED_BRIEF.md` and a narrow cross-reference in `docs/SCENE_LOCK_AND_ANIMATION_ASSET_BOUNDARY.md` or `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`.
- Confirm no ignored `data/` or `out/` artifacts are staged.
- Confirm `mvp-6` remains unchanged.

## Risks

- Accidentally turning playtest-specific Qingli/Ai-chan/NPC details into engine requirements.
- Accidentally implying Candidate B or runtime quality gates are approved.
- Overfitting anti-AI-writing guidance into brittle phrase bans instead of review principles.
- Copying or imitating protected external story style too closely.
- Accidentally treating a `sceneCandidate` or script packet draft as approved canon, setting-pack data, or animation asset lock.
- Accidentally making 04/05/06 look active before 01 explicitly resumes that workflow.
- Accidentally broadening docs-only verification exceptions to code/runtime/schema/CLI milestones.
- Accidentally implying automation / scheduler may continue the 04/05/06 handoff without manual 01/user approval.
- Accidentally copying author-only forbidden reveal text into a public scene registry.
- Accidentally treating conservative scene seed entries as final canon, schema, runtime scene definitions, or approved production asset locks.

## Drift Log

- 2026-06-21: Started docs-only authoring quality / style guide / review workflow milestone after 01 classified 04 playtest learning as docs-first evidence, not runtime approval.
- 2026-06-21: Added `docs/AUTHORING_QUALITY_GUIDELINES.md` with engine/content layer separation, anti-AI-writing guidance, NPC consistency checks, review loop, and copyright/style safety. No runtime or schema files touched.
- 2026-06-21: `git diff --check` passed with only Windows line-ending warnings. Full Gradle was intentionally skipped because this milestone changed only docs and `PLANS.md`.
- 2026-06-21: Addendum requested micro-prose review guidance for avoiding AI calibration phrasing such as repeated "not X, only Y", using visible behavior for limits, checking stacked weakening words, and handling animal/nonverbal action through concrete beats. This remains docs-only.
- 2026-06-21: Addendum `git diff --check` passed with only Windows line-ending warnings. Full Gradle remains intentionally skipped because the addendum changed only docs and `PLANS.md`.
- 2026-06-21: Addendum requested epoch / material culture / tech-level consistency guidance, including object material/toolchain checks, modern artifact exceptions, cart wheel versus tire track wording, player-introduced modern objects, and future optional schema ideas that are not approved in this milestone.
- 2026-06-21: Tech-level addendum `git diff --check` passed with only Windows line-ending warnings. Full Gradle remains intentionally skipped because the addendum changed only docs and `PLANS.md`.
- 2026-06-21: Addendum requested protagonist reaction beat guidance for openings where abnormal cues accumulate. The rule requires cue -> notice -> check/adjust -> advance, while keeping truth withheld and avoiding long internal explanation. This remains docs-only.
- 2026-06-21: Protagonist reaction addendum `git diff --check` passed with only Windows line-ending warnings. Full Gradle remains intentionally skipped because the addendum changed only docs and `PLANS.md`.
- 2026-06-22: Addendum requested initial survival orientation and goal transition guidance for displacement openings. The rule requires opening objectives to follow protagonist knowledge boundary and immediate needs before local mysteries become main goals. This remains docs-only.
- 2026-06-22: Opening goal-transition addendum `git diff --check` passed with only Windows line-ending warnings. Full Gradle remains intentionally skipped because the addendum changed only docs and `PLANS.md`.
- 2026-06-22: Addendum requested sensory attribution and body-part logic guidance for animal/nonverbal beats, including smell-to-nose, sound-to-ears, sight-to-gaze, touch-to-body-contact mapping and avoiding mechanical balanced clauses. This remains docs-only.
- 2026-06-22: Sensory attribution addendum `git diff --check` passed with only Windows line-ending warnings. Full Gradle remains intentionally skipped because the addendum changed only docs and `PLANS.md`.
- 2026-06-22: Addendum requested a narrative review gate and eight-point checklist to move line-by-line review away from the user. The flow is draft -> self-check -> review gate -> minimal revision -> user-facing text and remains docs-only.
- 2026-06-22: Narrative review gate addendum `git diff --check` passed with only Windows line-ending warnings. Full Gradle remains intentionally skipped because the addendum changed only docs and `PLANS.md`.
- 2026-06-22: Addendum requested scene lock and animation asset boundary guidance because runtime scene continuity is not the same as animation-ready production asset planning. This remains docs-only and does not add runtime/schema fields.
- 2026-06-22: Scene lock addendum `git diff --check` passed with only Windows line-ending warnings. Full Gradle remains intentionally skipped because the addendum changed only docs and `PLANS.md`.
- 2026-06-22: New docs-only milestone requested script-ready scene packet, scene registry, 04 generation, 05 review, 06 display, and 01/02/03 loop contracts before 04/05/06 resume. This remains docs-only and does not add runtime/schema/Validator gates.
- 2026-06-22: Active addendum clarified no automation / scheduler push for this workflow. 06 is a human approval window; silence, ambiguity, or lack of reply is not approval. User explicit approval must return to 01 before 01 schedules 02/03 or formal Turn work.
- 2026-06-22: Script packet contract docs added. `git diff --check` passed for tracked changes with only Windows line-ending warnings; full Gradle is intentionally skipped because this milestone changes only docs and `PLANS.md`.
- 2026-06-22: Script packet contract milestone is limited to tracked docs. Planned commit is `docs: define script-ready scene packet contracts`; 02 will push to the current branch and directly notify 03 for read-only verification.
- 2026-06-22: 03 passed the script packet contract commit `63cc2f1`. New project-control alignment milestone started to update `ROADMAP.md`, verification criteria, and handoff notes so the repo records manual handoff only and the paused 04/05/06 state.
- 2026-06-22: Project-control alignment remains docs-only. It may skip full Gradle only because no code/runtime/schema/CLI files are touched and 01/user explicitly allowed this docs-only exception; the exception is not generalized to implementation milestones.
- 2026-06-22: Project-control alignment `git diff --check` passed with only Windows line-ending warnings. Full Gradle is intentionally skipped under the explicit docs-only documentation/control exception.
- 2026-06-22: 03 passed project-control alignment commit `834533e`. New docs-only Qingli scene registry seed brief started from existing formal scene ids and public setting pack scene/location/transition evidence. Author-only boundaries are recorded as boundaries only, not copied as reveal content.
- 2026-06-22: Qingli scene registry seed brief added for four formal scene ids. `git diff --check` passed with only Windows line-ending warnings; full Gradle is intentionally skipped because this milestone changes only docs/control files.
