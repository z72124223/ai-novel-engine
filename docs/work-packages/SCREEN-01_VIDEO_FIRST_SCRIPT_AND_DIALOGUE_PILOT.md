# SCREEN-01 — 影片優先影視劇本與華語對白試作

Status: `READY_FOR_AUTONOMOUS_CODEX_EXECUTION`

Date: 2026-07-12

## 1. 目的

將 STORY-01 的〈潮線之外〉從「小說試作」轉成真正能接圖片、分鏡與影片的生產資料。

本包不是再寫一章小說，也不是立即生成影片。

本包要證明：

> 同一份故事可以被固定成事件主檔、影視劇本、自然華語對白、場景表、鏡頭表與 60–90 秒影片試作規格，而且所有成果都能由 GitHub 交給另一個 Agent 繼續工作。

## 2. 基線

Source branch:

```text
codex/mvp6-checkpoint-branching-spec
```

Required source artifact:

```text
samples/story01/story01_revised_draft.md
```

Expected source SHA-256:

```text
ea0e6e148377947907e68883e08a3864b113acaa0c7b9fd30df2408bd1145e91
```

Required implementation branch:

```text
codex/screen01-video-first-pilot
```

Start from the latest remote default branch after `git fetch --prune origin` and record the exact base SHA.

Do not work in the old dirty worktree.

## 3. Product decisions that are already made

### 3.1 Final destination

Primary destination:

```text
serialized video / animation
```

Novel prose is a source and optional derived product, not the terminal authority.

### 3.2 Protagonist

Lu Chenxing may remain highly competent.

Do not manufacture stupidity or low-level mistakes to create conflict.

His growth focus is:

- adapting to the new world;
- understanding cultures, institutions and power structures;
- learning how responsibility changes when more people depend on him;
- handling political and moral consequences of power;
- moving from protecting himself and Aijiang to influencing larger conflicts.

### 3.3 Language

Master script:

```text
Traditional Chinese / zh-Hant-TW
```

Lu Chenxing's spoken base:

```text
natural Taiwan Mandarin
```

The result must remain readable for Chinese audiences in Taiwan, Mainland China, Hong Kong, Malaysia, Singapore and other regions.

### 3.4 Collaboration

GitHub remote is the team authority.

No required screenplay, dialogue, shot, review, subtitle, decision or report may exist only on the local host, temporary SQLite or `out/**`.

## 4. Required tracked outputs

Create and Commit at minimum:

```text
samples/screen01/
├── screen01_story_production_master.json
├── screen01_screenplay_initial_zh-Hant-TW.md
├── screen01_dialogue_bible_zh-Hant-TW.json
├── screen01_dialogue_track_initial_zh-Hant-TW.json
├── reviews/
│   ├── taiwan_spoken_dialogue_review.json
│   ├── pan_chinese_readability_review.json
│   └── screen_performance_review.json
├── screen01_screenplay_revised_zh-Hant-TW.md
├── screen01_dialogue_track_revised_zh-Hant-TW.json
├── screen01_scene_list.json
├── screen01_shot_list.json
├── screen01_video_pilot_60_90s.json
├── screen01_video_pilot_zh-Hant-TW.srt
├── screen01_locale_release_matrix.json
└── screen01_video_handoff.json

docs/screen01/
├── SCREEN01_FINAL_REPORT.md
├── SCREEN01_DIALOGUE_REVIEW_REPORT.md
└── SCREEN01_GIT_HANDOFF.md
```

Codex may add schemas, models, repository tables, CLI, tests or author-workbench code only when needed to make these outputs reproducible and reviewable.

## 5. Stage 0 — Git and source verification

1. Fetch remote.
2. Create clean `codex/screen01-video-first-pilot` worktree/branch.
3. Record exact base SHA.
4. Verify STORY-01 source file and SHA-256.
5. Verify STORY-01 remains `NON_CANON_PILOT`.
6. Confirm no image/video Provider job is active.
7. Write the Stage record to Git, not only local output.

## 6. Stage 1 — Story Production Master

Create `screen01_story_production_master.json`.

It must describe the story without depending on novel prose style.

Minimum fields/concepts:

```text
productionId
sourceRepository
sourceBranch
sourceCommitSha
sourceArtifactPath
sourceArtifactSha256
languageMaster
canonBoundary
targetFinalMedium
premise
timeline
locations
charactersPresent
characterGoals
characterKnowledge
characterRelationships
props
storyBeats
choices
consequences
reveals
unresolvedQuestions
emotionalWave
continuityLocks
forbiddenReveals
```

The Master is the production authority for SCREEN-01 derivations.

Do not promote it to formal Canon.

## 7. Stage 2 — Full screenplay conversion

Create a complete Traditional-Chinese screenplay from the Story Production Master and STORY-01 source.

Required screenplay form:

```text
場次編號
內／外景
地點
時間
畫面動作
角色動作
正式對白
重要聲音
轉場
預估時長
```

Rules:

- Convert long internal narration into visible behavior, dialogue, sound or controlled voice-over only when necessary.
- Do not let voice-over carry information that should be visible.
- Do not invent Canon answers.
- Preserve the rescue-versus-evidence choice and its cost.
- Preserve Aijiang's active agency.
- Preserve the second-person/second-actor mystery only as a supported working hypothesis.
- The screenplay must be filmable, not a prose chapter with scene headings pasted on top.

## 8. Stage 3 — Natural Chinese dialogue

### 8.1 Dialogue Bible

Create a role-specific dialogue bible.

For every speaking character include:

```text
speechLength
sentenceRhythm
commonResponses
avoidPatterns
humorStyle
silenceBehavior
pressureBehavior
relationshipChanges
regionalLanguageNotes
```

### 8.2 Natural-dialogue rules

Avoid:

- two AIs taking turns presenting full arguments;
- every line stating the character's value clearly;
- symmetrical speeches;
- long complete sentences during danger;
- too many quotable lines;
- repeated `不是 A，而是 B` structures;
- all characters sounding equally intelligent and polished;
- modern meme language without character reason;
- mixing Taiwan/Mainland/Hong Kong/Malaysia terms randomly.

Prefer:

- short spoken lines;
- interruption;
- hesitation;
- incomplete answers;
- avoidance;
- misunderstanding;
- physical response instead of explanation;
- silence;
- one character not cooperating with another's joke;
- distinct vocabulary by age, work, region and relationship.

## 9. Stage 4 — Three independent full-script reviews

Use three independent reviewers. They must read the full initial screenplay and dialogue track, quote exact evidence and bind the review to content SHA-256.

### Reviewer A — Taiwan spoken-dialogue review

Checks:

- sounds like real spoken Taiwan Mandarin;
- Lu's sarcasm is natural and not constant;
- danger dialogue is short and urgent;
- dialogue does not feel translated or AI-generated;
- roles have different voices.

### Reviewer B — Pan-Chinese readability review

Checks readability for:

- Taiwan;
- Mainland China;
- Hong Kong;
- Malaysia/Singapore.

It must flag words that require localization rather than flattening all dialogue into generic written Chinese.

### Reviewer C — Screen performance review

Checks:

- actors can perform the lines naturally;
- spoken timing is plausible;
- action and dialogue do not duplicate each other;
- camera can show the required information;
- emotion is carried by performance rather than explanation;
- the script can be divided into shots.

Each review returns:

```text
reviewerRole
reviewerInstanceId
sourceScreenplaySha256
sourceDialogueSha256
verdict
summary
quotedEvidence
strengths
findings
requiredChanges
scorecard
```

## 10. Stage 5 — One bounded screenplay/dialogue revision

At most one revision pass.

This is not a second novel revision. It is a derived screenplay/dialogue revision.

The revised version must:

- respond to concrete review findings;
- preserve the Story Production Master;
- not change formal Canon;
- not add unsupported plot answers;
- improve spoken naturalness and filmability;
- keep Lu competent rather than forcing a foolish mistake.

If no revision is needed, record the evidence honestly.

## 11. Stage 6 — Scene list and shot list

### Scene list

For each scene include:

```text
sceneId
locationId
timeOfDay
estimatedDuration
characters
sceneGoal
valueConflict
entryState
exitState
props
continuity
sound
requiredAssets
```

### Shot list

For each shot include:

```text
shotId
sceneId
estimatedSeconds
shotSize
cameraAngle
cameraMovement
composition
action
speakingCharacter
dialogueIds
sound
continuityFrom
continuityTo
requiredCharacterReferences
requiredCostumeReferences
requiredLocationReferences
forbiddenDrift
```

The shot list is a future media handoff, not permission to generate images or video now.

## 12. Stage 7 — 60–90 second future video pilot packet

Select one continuous 60–90 second segment that best tests:

- Lu's natural spoken voice;
- Aijiang's active behavior;
- visible danger recognition;
- human dialogue under pressure;
- cinematic action that does not rely on narration;
- a clear beginning and ending for a short video test.

Create:

```text
screen01_video_pilot_60_90s.json
screen01_video_pilot_zh-Hant-TW.srt
```

Do not call a Provider.

Do not generate images or video.

## 13. Stage 8 — Locale release matrix

Create a release matrix that distinguishes:

```text
zh-Hant-TW master
zh-Hans-CN subtitle/localization
zh-Hant-HK terminology review
zh-Hans-MY readability review
```

It must list terms that may require adaptation, but it must not rewrite the master dialogue into mixed-region generic language.

## 14. Stage 9 — Git-centered author workbench

Extend or add a local author workbench only if useful.

It should display:

- STORY-01 prose source;
- Story Production Master;
- initial/revised screenplay;
- dialogue track;
- three reviews;
- scene list;
- shot list;
- pilot segment;
- Git branch/Commit/path/Hash identity.

Any decision must produce a durable Hash-bound artifact that can be committed to Git.

The local DB may be used for execution, but the selected script and decisions must also be exported as tracked files.

## 15. Git and Commit rules

- Use focused Commit(s) for coherent stages.
- Push the final branch normally.
- No force push.
- No broad staging.
- No private absolute path in final artifacts.
- Every final report includes repo-relative paths and Hashes.
- Do not claim a local-only artifact is delivered.
- Important generated script files must be committed.

## 16. Model policy

Default engineering:

```text
gpt-5.6-sol / medium
```

Use High only for the bounded screenplay/dialogue creation/revision and high-risk state/Hash review when needed.

Independent reviewers:

```text
gpt-5.6-sol / medium
```

Repository/test exploration:

```text
gpt-5.6-terra / low or medium
```

Hash, JSON, subtitle timing, diffs and counts use deterministic tools.

## 17. Forbidden scope

Do not:

- generate Chapters 2 or 3;
- revise STORY-01 prose a second time;
- generate images;
- generate video;
- call or purchase a Provider;
- decide final art style;
- promote Canon;
- add interactive game features;
- publicly deploy;
- use a local-only file as final handoff.

## 18. Verification

At minimum:

- parse every JSON;
- verify every required file exists in the tracked branch;
- calculate content SHA-256;
- verify reviews quote text that exists in the bound source;
- verify screenplay and dialogue revision hashes differ when revision occurs;
- validate scene/shot/dialogue references;
- validate subtitle timing and dialogue linkage;
- scan for private paths, secrets and unsupported Provider claims;
- run focused tests for code changes;
- run full suite once;
- run `git diff --check`;
- confirm no Canon/image/video/Provider change;
- confirm clean final worktree and successful Push.

## 19. Final report

`docs/screen01/SCREEN01_FINAL_REPORT.md` must state in simple Traditional Chinese:

- what was produced;
- which script is selected;
- whether dialogue sounds natural;
- remaining weak points;
- exact Git branch and final Commit;
- every important repo-relative path and SHA-256;
- tests and failures;
- model/reasoning used;
- whether any High escalation occurred;
- Canon/image/video/Provider counts;
- recommended next package.

## 20. Completion classification

Choose one:

```text
SCREEN01_PASS_READY_FOR_VISUAL_PREPRODUCTION
SCREEN01_PASS_WITH_DIALOGUE_WARNINGS
SCREEN01_NEEDS_HUMAN_DIALOGUE_DECISION
SCREEN01_BLOCKED_BY_SOURCE_CONFLICT
SCREEN01_BLOCKED_BY_TECHNICAL_FAILURE
```

Do not automatically begin image or video work after completion.
