# Current Route Gate V4 — Video-First / Git-First Production

Status: `ACTIVE_SINGLE_CURRENT_ENTRY_GATE`

Date: 2026-07-12

Supersedes for current routing:

- `docs/CURRENT_ROUTE_GATE_V1.md`
- `docs/CURRENT_ROUTE_GATE_V2.md`
- `docs/CURRENT_ROUTE_GATE_V3.md`

Older Gates remain historical evidence only.

## 1. Mandatory startup order

Every GPT, Codex CLI, Reviewer, Subagent or external collaborator reads:

1. `PROJECT_STATE.md`
2. `.codex/project_controller.md`
3. `docs/CURRENT_ROUTE_GATE_V4.md`
4. `docs/VIDEO_FIRST_GIT_FIRST_PRODUCTION_CHARTER_V1.md`
5. `docs/work-packages/SCREEN-01_VIDEO_FIRST_SCRIPT_AND_DIALOGUE_PILOT.md`
6. `docs/CODEX_MODEL_AND_BUDGET_POLICY_V2.md`
7. `docs/development_journal/INDEX.md`
8. `AGENTS.md`
9. STORY-01 source and only the code/data required by SCREEN-01

Do not start from historical REBOOT Work Packages, image Work Packages, old HANDOFF files, README status, route cards, old `PLANS.md` sections or a local machine path.

## 2. Product route

```text
World / Character Data
→ Story Production Master
→ Traditional-Chinese Screenplay
→ Natural Spoken-Chinese Dialogue Track
→ Scene List
→ Shot List / Storyboard Packet
→ Character and Location Assets
→ Keyframes
→ Video Shots
→ Edit / Sound / Subtitles
→ Episode Video
```

The primary destination is serialized video/animation. Novel prose is a supporting source, optional publication output and quality-test artifact; it is not the terminal production authority.

## 3. Current Work Package

```text
SCREEN-01 — Video-First Script and Dialogue Pilot
```

Status:

```text
READY_FOR_AUTONOMOUS_CODEX_EXECUTION
```

Required branch:

```text
codex/screen01-video-first-pilot
```

Required base:

```text
latest remote default branch after STORY-01 and this Route V4 are present
```

Codex records the exact base SHA before editing.

## 4. GitHub is the collaboration authority

GitHub remote Repository is the highest authority for:

- current direction;
- branch and Commit history;
- story/script/shot artifacts;
- reviews and decisions;
- handoffs;
- hashes;
- team memory;
- recovery on another computer.

Local computers, Worktrees, terminal sessions, `out/**`, temporary SQLite and generated caches are replaceable execution environments.

SCREEN-01 is incomplete if its important script, dialogue, scene, shot, review, subtitle, decision or handoff artifacts exist only on a local host.

Every handoff must name:

```text
repository
branch
commit SHA
repo-relative path
content SHA-256 where required
```

## 5. Story and character decisions

- STORY-01 is accepted as a successful `NON_CANON_PILOT` technical/content milestone.
- Preserve `samples/story01/story01_revised_draft.md` unchanged as the source text for this conversion.
- Do not perform a second prose revision.
- Do not promote STORY-01 or SCREEN-01 to formal Canon.
- Do not force Lu Chenxing to make low-intelligence mistakes for drama.
- His primary long-term growth is adapting to the new world's culture, power, institutions, responsibility and political scale.
- Conflict should come from incomplete information, value tradeoffs, adaptive opponents and consequences of correct choices.

## 6. Language decision

Master creative language:

```text
Traditional Chinese / zh-Hant-TW
```

Spoken base for Lu Chenxing:

```text
natural Taiwan Mandarin
```

The script must remain understandable to Chinese-speaking audiences in Taiwan, Mainland China, Hong Kong, Malaysia, Singapore and other regions.

Codex must avoid AI-style dialogue: symmetrical speeches, complete essays, repeated slogans, constant quotable lines, excessive `not A but B`, and identical voices.

## 7. Model routing

Default engineering and integration:

```text
gpt-5.6-sol / medium
```

Use bounded High only for:

- final screenplay and dialogue transformation where product quality requires it;
- SQLite/Transaction/Hash/decision boundaries;
- final high-risk review.

Independent dialogue and screenability reviewers may use Sol Medium. Repository/tests use Terra Low/Medium. Deterministic Hash, JSON, subtitle timing, diff and counts use scripts.

No Max/Ultra by default.

## 8. Execution authority

One user-started Codex session may complete SCREEN-01 end-to-end:

```text
fetch
→ clean worktree/branch
→ implement data and author-workbench support where needed
→ generate all screenplay/dialogue/scene/shot artifacts
→ run independent reviews
→ perform at most one screenplay/dialogue revision
→ validate
→ Commit in focused stages
→ Push
→ report once
```

Codex does not return for ordinary wording, file-structure, schema, test or UI choices.

## 9. Hard boundaries

Preserve:

- formal Canon approval;
- SQLite transaction safety;
- source/commit/content hashes;
- project and source allowlists;
- Knowledge Boundary;
- human decision artifacts;
- no secret/API key in Git;
- no paid Provider request without explicit approval.

## 10. Not part of SCREEN-01

Do not:

- generate images;
- generate video;
- call Grok, Gemini/Veo, Seedance/Jimeng, Runway or another Provider;
- choose a final Provider;
- produce Chapters 2–3 novels;
- add interactive RPG features;
- publicly deploy a website;
- decide permanent romance, death or Canon;
- store required results only in local SQLite or `out/**`.

## 11. True Stop Conditions

Stop only when:

- a formal Canon or permanent character-fate decision is unavoidable;
- a final visual choice is required;
- paid Provider/API/API key is required;
- a destructive Git/data operation has no safe alternative;
- continuing requires weakening Transaction, Knowledge, Hash or approval safety;
- the exact same core blocker survives one evidenced Medium attempt and one evidenced High attempt.

Ordinary dialogue choices, screenplay layout, subtitle timing, shot count, additive migration, tests, or author-workbench design are not stop conditions.

## 12. Completion

SCREEN-01 completes only when the remote Git branch contains:

- Story Production Master;
- full Traditional-Chinese screenplay;
- dialogue bible and dialogue track;
- three independent full-script reviews;
- one bounded revision if needed;
- scene list;
- shot list;
- 60–90 second future video-pilot packet;
- subtitle file;
- locale/readability notes;
- future image/video handoff entry;
- code/tests if required;
- final report with exact branch, Commits, paths and Hashes.

Do not report completion from local files, an unpushed worktree or a database row alone.
