# Plan

## Goal

Create and maintain docs-only authoring quality, scene-production boundary, script-ready packet, Qingli scene registry seed, scene packet dry-run review, storyboard / camera planning, production asset registry planning, world-bible rebuild decision notes, storyboard-first final deliverable alignment, new-world foundation questionnaire, and project-control notes that preserve playtest learning about narrative prose, NPC consistency, review workflow, narrative review gates, anti-AI-writing guidance, micro-prose review, sensory attribution, epoch / material culture consistency, protagonist reaction logic, initial survival orientation / goal transition, scene lock / animation asset planning, 04/05/06/07/08 handoff contracts, scene-registry / dry-run completion status, and manual 01/02/03 engineering-loop rules while clearly separating engine layer rules from content / setting pack specifics.

## Global Strategy

Treat 04 playtest learning and user product-direction discussion as authoring evidence, not runtime approval. Keep focused docs that can guide future engine prompts, review checklists, scene packet preparation, storyboard planning, camera guidance, production asset locking, and setting-pack authoring without hardcoding Qingli content, Ai-chan details, or copyrighted external styles into engine core. For the current milestone, add a docs-only new world foundation questionnaire so the user / 00 can provide premise, material culture, power system, scene set, social structure, asset, and author-only boundary decisions before any world foundation brief or 04/05/06/07/08 restart.

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
- No treating Qingli as the new opening base after the world-bible rebuild decision.
- No treating Seedance, Oiioii, or high-quality video generation as the current final deliverable or implemented system.
- No filling in worldbuilding answers on behalf of the user.
- No turning questionnaire prompts into canon, setting-pack data, runtime fields, or asset records.

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
- `docs/QINGLI_SCRIPT_PACKET_DRY_RUN_REVIEW.md`
- `docs/STORYBOARD_AND_CAMERA_PLANNING.md`
- `docs/PRODUCTION_ASSET_REGISTRY_PLANNING.md`
- `docs/ASSET_REGISTRY_IMPLEMENTATION_BLUEPRINT.md`
- `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md`
- `ROADMAP.md`
- `docs/HANDOFF.md`

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
- 03 passed commit `c248cefd0dcde8a878aae290bbb3215ca4d5cb1b` (`docs: seed Qingli scene registry brief`).
- `ROADMAP.md` and `docs/HANDOFF.md` still list scene registry seed brief as a future recommended candidate, so they need status alignment.
- User instruction for this milestone forbids moving directly to Pilot-2 human turn collection, Candidate A.3, Candidate B, MVP-8, runtime enforcement, Oiioii integration, automation, or 04/05/06 restart.
- 03 passed commit `bbcd1df6bbfaebc7834eea1338249eaa70a55772` (`docs: align control docs after Qingli scene registry seed`).
- `ROADMAP.md` now recommends `scene packet sample / registry dry-run review` as a safe docs-only next candidate.
- User instruction for this milestone requires a dry-run review only. It is not 04 generation, 05 formal review gate, 06 display, formal turn, AI result JSON, setting-pack patch, runtime scene definition, approved animation asset lock, or Oiioii integration.
- 03 passed commit `ee80a16f83825ac56b2449285c19981a81f746a1` (`docs: dry-run Qingli script packet registry review`).
- `ROADMAP.md` and `docs/HANDOFF.md` still list the dry-run review as a safe next candidate, so they need status alignment.
- User instruction for this milestone says the completed dry-run review proves the spec and seed brief can support a conservative sample, but does not approve 04/05/06 restart, formal Turn work, setting-pack patch, runtime/schema/Oiioii integration, or treating the sample as user-approved content.
- User requested a not-yet-implemented future planning document inside the project for storyboard images, scene locking for storyboard frames, camera language /運鏡, GPT-generated storyboard images, and future Oiioii-oriented workflow.
- Existing docs already define script-ready scene packets and scene lock / animation asset boundary, but do not yet contain a dedicated storyboard / camera planning note.
- User identified a severe missing production layer: characters, scenes, props, weapons, costumes, and important objects must be locked after adoption so GPT and Oiioii do not regenerate drifting versions every time.
- Existing docs mention scene registry and allowed props, but do not yet define a dedicated production asset registry planning document with reference requirements such as three-view assets, panorama / zone maps, variants, and lifecycle states.
- User agreed that long-term asset data volume will require SQLite for approved assets, while unreviewed asset candidates should remain in draft files until human / review approval.
- Existing project rules still forbid direct SQLite writes and require formal state-changing work to go through approved validation / apply flows.
- 03 passed `docs/WORLD_BIBLE_REBUILD_DECISION_BRIEF.md` in commit `657e708cdf7514474f8a865e569f0d7da5769051`; Qingli should not be used as the new opening base or assetization starting point.
- User explicitly decided the current final deliverable is reviewable storyboard images / storyboard packets, not high-quality animation.
- User explicitly allowed this milestone to organize and submit the existing dirty storyboard / asset planning docs if they are rewritten toward the storyboard-first direction.
- The required product flow for future manual work is `04 scene packet -> 05 packet review -> 06 human approval -> 07 asset candidates -> 08 GPT storyboard pickup / verification -> user-reviewable storyboard delivery`.
- 03 passed `docs: align storyboard as final deliverable` at commit `16f0c8446d3479bfec03888781c0cdbf6232f767`.
- 01 confirmed the next safe milestone is a docs-only `NEW_WORLD_FOUNDATION_QUESTIONNAIRE`.
- User / 01 instruction for this milestone says the questionnaire is not canon, not a setting pack, not a 04 generation packet, not storyboard pickup, and must not be pre-filled by 02.

## Assumptions

- `docs/AUTHORING_QUALITY_GUIDELINES.md` is the right location because the requested content is cross-cutting authoring guidance, not a Pilot-only report.
- Full Gradle test is not necessary for this docs-only milestone if `git diff --check` passes and no code/runtime files are touched.

## Open Questions

- Whether future milestones should turn parts of the guideline into prompt templates, setting-pack schema fields, or read-only audit checks. This milestone does not decide that.
- Whether future milestones should turn the packet contract into runtime schema, Validator rules, UI forms, or Oiioii integration. This milestone does not decide that.
- Whether 04/05/06 should resume after these contracts is a 01/user decision. This milestone does not resume them.
- Whether Qingli scene registry seed entries should become formal setting-pack fields or approved animation asset locks requires a later 01/user-approved milestone.
- Whether to run a scene packet sample / registry dry-run review is a future 01 handoff decision. This milestone only records it as a safe docs-only candidate.
- Whether to turn any dry-run recommendations into spec edits, packet templates, or formal 04/05/06 work requires a later 01/user-approved milestone.
- Whether to resume 04/05/06, collect Pilot-2 human turns, collect author-led samples, draft Candidate A.3, or run remote restore smoke requires later explicit 01/user input or on-demand selection.
- Whether storyboard image generation should become an implemented tool, Oiioii integration, schema field, or runtime workflow requires later explicit approval. This milestone only records future planning.
- Whether production asset registry should become implemented schema, files, image storage, Validator checks, GPT prompt tooling, or Oiioii integration requires later explicit approval. This milestone only records future planning.
- Whether the future SQLite asset registry belongs inside the existing runtime DB, a separate production DB, or an exportable sidecar DB requires later architecture approval.
- Whether the new world foundation should preserve, rewrite, or replace any non-Qingli production planning examples requires later 01/user approval.
- Which questionnaire answers the user will provide, and which items should be marked "unknown / let 01 propose", remains open until the user / 00 responds.

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
- [x] Step 47: Align ROADMAP and HANDOFF after 03 passed the Qingli scene registry seed brief.
- [x] Step 48: Run `git diff --check` and inspect diff/stat/staged scope for the scene-registry control docs alignment.
- [x] Step 49: Commit `docs: align control docs after Qingli scene registry seed` and push tracked docs only.
- [x] Step 50: Directly notify 03 read-only review thread using a 03 verifier-window prompt.
- [x] Step 51: Add a docs-only Qingli script packet dry-run review with one conservative historical sample packet.
- [x] Step 52: Run `git diff --check` and inspect diff/stat/staged scope for the dry-run review.
- [x] Step 53: Commit `docs: dry-run Qingli script packet registry review` and push tracked docs only.
- [x] Step 54: Directly notify 03 read-only review thread using a 03 verifier-window prompt.
- [x] Step 55: Align ROADMAP and HANDOFF after 03 passed the Qingli script packet registry dry-run review.
- [x] Step 56: Run `git diff --check` and inspect diff/stat/staged scope for the dry-run control-doc alignment.
- [x] Step 57: Commit `docs: align control docs after Qingli dry-run review` and push tracked docs only.
- [x] Step 58: Directly notify 03 read-only review thread using a 03 verifier-window prompt.
- [x] Step 59: Inspect existing script packet, scene lock, and authoring guideline docs for storyboard / camera planning overlap.
- [x] Step 60: Add a docs-only future planning note for storyboard frames, scene lock, camera intent, and Oiioii-oriented image/storyboard workflow.
- [x] Step 61: Run `git diff --check` and inspect diff scope for the storyboard planning note.
- [x] Step 62: Add docs-only production asset registry planning for locked characters, scenes, props, weapons, costumes, variants, and required reference views.
- [x] Step 63: Cross-reference asset registry planning from storyboard / scene-lock / script-packet docs.
- [x] Step 64: Run `git diff --check` and inspect diff scope for asset registry planning.
- [x] Step 65: Add docs-only asset registry implementation blueprint covering draft files, SQLite formal registry, validate / preview / apply / export / audit flow, and future engineering phases.
- [x] Step 66: Align ROADMAP and HANDOFF with the future asset registry blueprint as a recommended milestone, without approving schema or runtime work.
- [x] Step 67: Run `git diff --check` and inspect diff scope for the blueprint milestone.
- [x] Step 68: Read the dirty storyboard / asset planning docs, world-bible rebuild decision brief, ROADMAP, HANDOFF, and current PLANS before editing.
- [x] Step 69: Update `PLANS.md` for the storyboard-first final deliverable alignment milestone and dirty tree handling.
- [x] Step 70: Align storyboard / camera, production asset registry, implementation blueprint, scene lock, and script packet docs to storyboard-first delivery and optional downstream video.
- [x] Step 71: Align `ROADMAP.md` and `docs/HANDOFF.md` with the storyboard-first final deliverable and safe next steps.
- [x] Step 72: Run `git diff --check`, inspect diff/stat/status, commit, push, and directly notify 03 for read-only verification.
- [x] Step 73: Read AGENTS, PLANS, ROADMAP, HANDOFF, world-bible rebuild, storyboard, asset, and packet docs before the new-world questionnaire milestone.
- [x] Step 74: Update `PLANS.md` for the docs-only new world foundation questionnaire milestone.
- [x] Step 75: Add `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md` without filling in user answers.
- [x] Step 76: Run `git diff --check`, inspect diff/stat/status/staged files, commit, push, and directly notify 03 for read-only verification.

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
- For post-Qingli scene registry seed control alignment, allow `ROADMAP.md` and `docs/HANDOFF.md`.
- For Qingli scene packet dry-run review, allow `docs/QINGLI_SCRIPT_PACKET_DRY_RUN_REVIEW.md` and narrow cross-references only if needed.
- For post-dry-run review control alignment, allow `ROADMAP.md` and `docs/HANDOFF.md`.
- For storyboard / camera future planning, allow `docs/STORYBOARD_AND_CAMERA_PLANNING.md` and `PLANS.md`.
- For production asset registry future planning, allow `docs/PRODUCTION_ASSET_REGISTRY_PLANNING.md`, `docs/STORYBOARD_AND_CAMERA_PLANNING.md`, `docs/SCENE_LOCK_AND_ANIMATION_ASSET_BOUNDARY.md`, `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`, and `PLANS.md`.
- For asset registry implementation blueprint planning, also allow `docs/ASSET_REGISTRY_IMPLEMENTATION_BLUEPRINT.md`, `ROADMAP.md`, and `docs/HANDOFF.md`.
- For storyboard-first final deliverable alignment, allow `docs/STORYBOARD_AND_CAMERA_PLANNING.md`, `docs/PRODUCTION_ASSET_REGISTRY_PLANNING.md`, `docs/ASSET_REGISTRY_IMPLEMENTATION_BLUEPRINT.md`, `docs/SCENE_LOCK_AND_ANIMATION_ASSET_BOUNDARY.md`, `docs/SCRIPT_READY_SCENE_PACKET_SPEC.md`, `ROADMAP.md`, `docs/HANDOFF.md`, and `PLANS.md`.
- For new world foundation questionnaire, allow `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md`, `PLANS.md`, and only minimal `ROADMAP.md` / `docs/HANDOFF.md` if needed.
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
- Accidentally leaving completed scene registry seed work listed as a next candidate.
- Accidentally implying the next step can run Pilot turns or restart 04/05/06 without user / 01 handoff.
- Accidentally treating a dry-run sample packet as formal story output, user-approved content, setting-pack patch, formal turn result, or a 05/06 deliverable.
- Accidentally leaving completed dry-run review work listed as a next candidate.
- Accidentally implying future human turn collection, author-led samples, Candidate A.3, remote restore, or 04/05/06 resumption can proceed without explicit 01/user decision.
- Accidentally treating storyboard image generation, camera fields, Oiioii prompt output, or storyboard-frame locks as implemented runtime/tooling rather than future planning.
- Accidentally treating planned asset registry entries, reference images, three-view sheets, panoramas, variants, or asset lifecycle states as implemented runtime/tooling or approved production assets.
- Accidentally implying SQLite schema, migrations, import CLI, or asset Validator work is approved by the blueprint document.
- Accidentally treating Seedance / Oiioii / high-quality video generation as the current deliverable rather than optional future downstream work.
- Accidentally treating Qingli historical samples as the new-world base after the world-bible rebuild decision.
- Accidentally treating 07 asset candidates as approved references or production locks before review and explicit approval.
- Accidentally answering the questionnaire instead of leaving it for user / 00 input.
- Accidentally turning questionnaire options into canon, setting-pack data, runtime fields, or approved storyboard / asset work.

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
- 2026-06-22: 03 passed Qingli scene registry seed brief commit `c248cef`. New control-doc alignment milestone started to mark the seed brief completed/pushed and update recommended candidates.
- 2026-06-22: Post-Qingli scene registry control alignment `git diff --check` passed with only Windows line-ending warnings. Full Gradle is intentionally skipped under the explicit docs-only documentation/control exception.
- 2026-06-22: 03 passed control-doc alignment commit `bbcd1df`. New docs-only scene packet sample / registry dry-run review milestone started. It must verify packet/registry usability without restarting 04/05/06 or creating formal turn / setting-pack / runtime content.
- 2026-06-22: Added `docs/QINGLI_SCRIPT_PACKET_DRY_RUN_REVIEW.md` with a `historical_sample` / dry-run-only packet using `scene-qingli-herb-warehouse-perimeter`. `git diff --check` passed with only Windows line-ending warnings. Full Gradle is intentionally skipped under the explicit docs-only documentation/control exception.
- 2026-06-22: 03 passed dry-run review commit `ee80a16`. New control-doc alignment milestone started to mark the dry-run review completed/pushed and update recommended candidates.
- 2026-06-22: Post-dry-run review control alignment `git diff --check` passed with only Windows line-ending warnings. Full Gradle is intentionally skipped under the explicit docs-only documentation/control exception.
- 2026-06-22: User requested storing a future, not-yet-implemented planning note about storyboard frames, GPT-generated storyboard images, scene lock, camera intent, and Oiioii-oriented animation handoff. Scope is docs-only and does not restart 04/05/06 or implement Oiioii integration.
- 2026-06-22: Added `docs/STORYBOARD_AND_CAMERA_PLANNING.md` as a future-planning-only document. `git diff --check` passed with only Windows line-ending warnings. Full Gradle is intentionally skipped because this milestone changes only docs and `PLANS.md`.
- 2026-06-22: User identified missing production asset locking as a severe gap. New docs-only milestone started to plan an asset registry for characters, scenes, props, weapons, costumes, variants, reference view requirements, GPT draft use, and Oiioii cost boundaries.
- 2026-06-22: Added `docs/PRODUCTION_ASSET_REGISTRY_PLANNING.md` and cross-referenced it from storyboard, scene-lock, and script-packet docs. `git diff --check` passed with only Windows line-ending warnings. Full Gradle is intentionally skipped because this milestone changes only docs and `PLANS.md`.
- 2026-06-22: User approved updating the project construction blueprint. New docs-only milestone started to describe how asset candidates remain in draft files, approved assets later enter SQLite through validate / preview / apply, and downstream packets / storyboards / GPT / Oiioii reference asset ids.
- 2026-06-22: Added `docs/ASSET_REGISTRY_IMPLEMENTATION_BLUEPRINT.md` and aligned `ROADMAP.md` / `docs/HANDOFF.md` so the future asset registry path is draft files first, SQLite formal registry later, and no schema/runtime/Oiioii work is approved by the blueprint alone.
- 2026-06-22: Asset registry blueprint milestone `git diff --check` passed with only Windows line-ending warnings. Full Gradle is intentionally skipped because this milestone changes only docs and `PLANS.md`.
- 2026-06-22: User decided the current final deliverable is reviewable storyboard images / storyboard packets, not high-quality animation. New milestone starts to align dirty storyboard / asset planning docs and control docs to a `04 -> 05 -> 06 -> 07 -> 08 -> user-reviewable storyboard` flow, with Seedance / Oiioii only as optional future downstream work.
- 2026-06-22: Dirty tree handling: the existing modified / untracked storyboard and asset planning docs are explicitly allowed in this milestone if rewritten to the storyboard-first direction and kept docs-only. No runtime, schema, CLI, SQLite, or generated artifact work is approved.
- 2026-06-22: Storyboard-first alignment edited only allowed docs/control files. `git diff --check` and staged scope inspection passed before commit; full Gradle remains intentionally skipped because no code/runtime/schema/CLI files are touched.
- 2026-06-22: 03 passed storyboard-first alignment commit `16f0c84`. New docs-only milestone starts to create `docs/NEW_WORLD_FOUNDATION_QUESTIONNAIRE.md` as a user-input questionnaire. It must not answer worldbuilding questions, create canon, patch setting packs, or resume 04/05/06/07/08.
- 2026-06-22: New world foundation questionnaire added as a user / 00 input surface and control docs minimally aligned. `git diff --check`, staged scope inspection, ignored artifact check, and `mvp-6` tag check passed before commit; full Gradle remains intentionally skipped because no code/runtime/schema/CLI files are touched.
