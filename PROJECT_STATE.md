# AI Novel Engine 專案狀態

> **唯一現況入口。** 新的 GPT、Codex CLI、Reviewer、Subagent 或外部協作者必須先讀本檔，再依本檔指定的 Route Gate、Work Package、Blueprint、Addenda 與模型政策執行。舊 HANDOFF、舊 charter、舊 route card、圖片 Work Package 或舊 `PLANS.md` 中的 `current`／`active`／`目前定案` 都是歷史標籤，不能跳過本檔。

## 1. Current Product Direction

目前正式產品方向是：

> 建立一個具有正式世界狀態、自主 NPC、主題驅動戲劇、可重播演化、本機可玩介面，以及未來視覺／影片輸出的長篇 AI 敘事世界。

近期唯一主線：

```text
真實引擎基線
→ 世界觀／SQL／Runtime Reality Audit
→ 簡化過度阻擋
→ SQL 保存 Run Evidence
→ 非 Canon 世界候選修補包進 Sandbox
→ NPC 離場自主性
→ Drama Director／Critics／Audience Expectation
→ 塵行 5–10 回合可玩垂直切片
→ 同 Seed Replay／規則演化
→ 本機 Web 可玩介面
→ 誠實整合報告
```

圖片與影片仍是長期產品方向，但目前暫停，以免媒體產線再次取代引擎真實運行。

## 2. Mandatory Current Route

- Current Route Gate: `docs/CURRENT_ROUTE_GATE_V3.md`
- Active Work Package: `docs/work-packages/REBOOT-01_PLAYABLE_DEPTH_SLICE.md`
- Autonomous Execution Addendum: `docs/work-packages/REBOOT-01_EXECUTION_ADDENDUM_V1.md`
- Active Blueprint: `docs/REBOOT_2026_PLAYABLE_DEPTH_BLUEPRINT_V1.md`
- Mandatory Creative／World Addendum: `docs/REBOOT_2026_CREATIVE_WORLD_EXECUTION_ADDENDUM_V1.md`
- Mandatory Narrative Voice／Fair Surprise Addendum: `docs/REBOOT_2026_NARRATIVE_VOICE_AND_UNPREDICTABILITY_ADDENDUM_V1.md`
- Model／Subagent／Budget Policy: `docs/CODEX_MODEL_AND_BUDGET_POLICY_V2.md`
- Future Video Provider Plan: `docs/VIDEO_PROVIDER_BAKEOFF_PLAN_V2.md`
- Development Journal: `docs/development_journal/INDEX.md`

Required startup order:

1. `PROJECT_STATE.md`
2. `.codex/project_controller.md`
3. `docs/CURRENT_ROUTE_GATE_V3.md`
4. `docs/work-packages/REBOOT-01_PLAYABLE_DEPTH_SLICE.md`
5. `docs/work-packages/REBOOT-01_EXECUTION_ADDENDUM_V1.md`
6. `docs/REBOOT_2026_PLAYABLE_DEPTH_BLUEPRINT_V1.md`
7. `docs/REBOOT_2026_CREATIVE_WORLD_EXECUTION_ADDENDUM_V1.md`
8. `docs/REBOOT_2026_NARRATIVE_VOICE_AND_UNPREDICTABILITY_ADDENDUM_V1.md`
9. `docs/CODEX_MODEL_AND_BUDGET_POLICY_V2.md`
10. `docs/development_journal/INDEX.md`
11. `AGENTS.md`
12. Only the code, tests, world data and historical evidence required by the active Stage

The following are superseded for current execution:

- `docs/CURRENT_ROUTE_GATE_V1.md`
- `docs/CURRENT_ROUTE_GATE_V2.md`
- `docs/CODEX_MODEL_AND_BUDGET_POLICY_V1.md`
- image Work Packages as active routing
- the old one-package-then-return rule for REBOOT-01 internal Stages

## 3. Current Work Package

- ID: `REBOOT-01 Playable Depth Slice`
- Status: `IMPLEMENTATION_COMPLETE_ON_REBOOT_BRANCH`
- Required branch: `reboot/playable-depth-v1`
- Required base: latest remote default branch after `git fetch --prune origin`
- Main model: `gpt-5.6-sol`
- Default reasoning: `medium`
- Repository Explorer: `gpt-5.6-terra / low`
- Runtime Tester: `gpt-5.6-terra / medium`
- Narrative Systems Critic: `gpt-5.6-sol / medium`
- Cost／Complexity Auditor: `gpt-5.6-luna / low`
- High Risk Reviewer: `gpt-5.6-sol / high`, read-only and on demand
- Targeted High: Migration／Transaction／StateReducer／Validator／Knowledge Boundary／Version-Hash／source-authority conflict／hard replay bug／final integration review
- `xhigh`／`max`／`ultra`: prohibited by default; use only under the evidence conditions in Model Policy V2

Project-scoped defaults are stored in `.codex/config.toml`. Dedicated agent profiles are stored under `.codex/agents/`.

REBOOT-01 is an approved Umbrella Work Package. After the user explicitly starts one Codex CLI session, Codex may complete all internal Stages, validate and Commit each Stage, Push the final branch and report once. It does not need to return to GPT or the user after ordinary internal decisions.

REBOOT-01 operating evidence and honest limitations are recorded in `docs/reboot/REBOOT01_FINAL_REPORT.md` and `docs/reboot/REBOOT01_PLAY_GUIDE.md`. Do not start the recommended live-turn follow-up automatically; this branch must be reviewed and merged first.

## 4. Required Operating Outcomes

Completion requires operating evidence, not only documents:

- one resettable Chenxing non-Canon Scenario;
- 5–10 continuous turns;
- protagonist, two meaningful NPCs and at least one opposing actor/force;
- at least one offscreen NPC action while the player is absent;
- at least one value dilemma;
- at least one irreversible Runtime consequence;
- at least three lasting world／relationship／character-state changes;
- raw draft, Critic scores, one bounded revision, State Delta and SQL evidence preserved;
- Narrative Stall detection and recovery;
- same-seed replay comparison;
- a minimal operable local Web slice after the engine loop passes;
- an honest final report containing successes, failures, limitations, model use and next recommendations.

Mandatory World／Creative outcomes:

- `docs/reboot/REBOOT01_WORLD_AND_CHARACTER_REALITY_AUDIT.md`;
- machine-readable world audit inventory;
- proof separating formal-file existence, SQL load, Context retrieval, Runtime use and Scenario trigger;
- one validated non-Canon Chenxing World Patch Candidate applied only to a resettable Sandbox;
- evidence that the patch is actually retrieved and used by Runtime／NPC Agents;
- two deep NPCs and one opposing actor or force with an understandable value claim;
- at least one world pressure that advances while the player is absent.

Mandatory Narrative outcomes:

- protagonist voice candidate with observational sarcasm, self-deprecation, risk awareness, social cost and serious-scene restraint;
- `AudienceExpectationLedger` or equivalent;
- `IDIOT_PLOT_RISK` detection;
- predictability, reversal fairness and character-intelligence scoring;
- a natural banter moment that reveals character or relationship;
- an obvious danger recognized and reasonably mitigated;
- a fair second-order consequence after the first-order expectation;
- a moment where high pressure suppresses humor;
- warmth, friendship, cuteness or romantic tension;
- local closure plus a new unresolved pull.

## 5. Narrative And Emotional Direction

The project supports a long-running ensemble world with **local closure and global continuation**.

Desired emotional rhythm:

```text
lightness／humor／daily friction
→ warmth／friendship／romance
→ unease
→ conflict escalation
→ value dilemma
→ anger／loss／reflection
→ relationship change
→ partial relief／new hope／new unresolved question
```

The reader should laugh and become attached before heavier consequences land. Emotional turns must grow from prior causes, not arbitrary shock.

### Protagonist Voice Candidate

For the REBOOT-01 Sandbox, Lu Chenxing may use:

```text
DAILY_QUIP
DANGER_OBSERVATION
GRIEF_RESTRAINT
```

This means observational sarcasm, self-deprecating humor, risk-aware commentary, socially costly quips and restraint in serious scenes. It is a non-Canon candidate until the user later approves promotion.

Lu may recognize obvious danger or trope-like behavior, forcing the world and opponents to respond intelligently. He must not become fourth-wall omniscient, know author-only facts, make every joke correct or use humor to erase grief.

### Fair Surprise

The system must distinguish:

- fair setup from obvious repetition;
- character intelligence from author-forced stupidity;
- causal reversal from random twist;
- expected first-order outcome from unexpected second-order consequence;
- a smart character changing the outcome from a character ignoring a clear warning.

Any character entering obvious danger needs a documented motive, knowledge gap, urgency, obligation, pressure, resource constraint, false belief, coercion or calculated tradeoff. Otherwise record `IDIOT_PLOT_RISK`.

### Growth And Continuation

The protagonist may gradually gain enough power, knowledge, responsibility and influence to affect major conflicts, but growth must also increase cost, moral pressure, leadership burden and risk of loss. Supporting characters, rivals, romantic interests and ordinary people require their own goals and growth.

There is no single terminal ending that permanently removes the world from the reader. Each arc must still pay off promises, alter relationships, leave lasting state and close a character phase. `No final ending` must not become endless repetition or unresolved setup.

## 6. Architecture Direction

Absorb design strengths rather than copying whole stacks:

- Generative Agents: memory stream, importance, reflection, planning, schedule, offscreen simulation;
- Concordia-like Game Master: agents propose actions; the environment resolves legal outcomes;
- SOTOPIA-like social evaluation: trust, cooperation, deception, persuasion, relationships and cost;
- TextWorld-like replay: reset, step, observation, score and deterministic comparison;
- ink-like deterministic narrative structure: flags, conditions, choices, variables and local endings.

Core flow:

```text
World and Character Reality Audit
→ non-Canon Sandbox patch
→ NPC Agents
→ Game Master
→ Drama Director
→ Writer
→ independent Logic / Character-Drama / Prose-Pacing Critics
→ Audience Expectation / Fair Surprise checks
→ at most one bounded revision
→ legality re-check
→ SQL evidence and Runtime result
```

The Drama Director may choose what legal conflict is worth presenting, but may not force a character to violate identity or leak Director metadata into character knowledge.

## 7. Validation Simplification

Only three categories remain hard blockers:

1. protocol safety;
2. world legality and transactional state safety;
3. irreversible human approval such as Canon, formal assets, destructive changes, release or paid providers.

Prose quality, pacing, repetition, AI-writing feel, weak subtext, emotional impact and predictability are `WARNING／SCORE`, not reasons to discard Run Evidence.

Evidence policy:

```text
raw model output → always persist as run evidence
legal but unpolished result → persist as provisional/candidate
world-legal State Delta → apply through existing Runtime transaction
quality findings → persist scores and revision suggestions
```

## 8. Multi-Agent And Cost Policy

Codex may use up to four direct Subagents with `max_depth = 1`.

Only the main `gpt-5.6-sol` Agent writes core Kotlin／SQL. Subagents perform exploration, testing, criticism and cost auditing. A High Risk Reviewer may replace one Subagent slot when needed.

Do not use High／Max for routine work. Hashes, JSON parsing, path checks, diffs, test statistics and benchmark calculations should be handled by scripts or deterministic tools.

Every Stage records the actual model, reasoning, Subagents, elapsed time, retries and High escalations. If Token／credit data is unavailable, record `NOT_AVAILABLE` rather than inventing values.

## 9. Image Lane Status

```text
IMAGE_LANE_PAUSED_AFTER_USER_APPROVED_CANDIDATE
```

The Daping Coast master-panorama candidate was generated and the user agreed with it. It has not completed Codex file transfer, SHA-256, controlled intake or Candidate Reference acceptance.

It remains:

```text
USER_APPROVED_CANDIDATE
not Canon
not APPROVED_REFERENCE
not LOCATION_REFERENCE_ACCEPTED
not PRODUCTION_LOCK
```

Do not continue Daping tasks 02–07 during REBOOT-01. Preserve the candidate and handoff evidence.

When image work resumes, character art direction must be selected before background art direction:

```text
character likeness
→ character rendering style A/B/C
→ Lu + Aijiang shared frame
→ costume material/light
→ scene style
→ Location Bible
→ shot keyframes
→ video
```

## 10. Video Provider Status

```text
VIDEO_LANE_FUTURE_PROVIDER_BAKEOFF_NO_SPEND
```

Future providers may include Gemini Omni Flash, Veo, Grok Imagine Video, Seedance／Jimeng and Runway. The project must use one provider-neutral `VideoShotHandoff` and compare providers on the same approved clean keyframe and motion contract.

The user-supplied YouTube video's verified title and the transcript limitation are recorded in Video Provider Plan V2. Unverified spoken details are not API facts.

No paid API call, provider account purchase, API key, background generation or whole-episode generation is authorized during REBOOT-01.

## 11. Web Status

The old permanent prohibition on a local Web Server／React／Ktor／Port is retired.

REBOOT-01 may create a minimal local-only playable Web interface after the engine loop passes. It may not be publicly deployed or connected to paid external model APIs during this package.

## 12. True Stop Conditions

Codex stops and asks the user only when:

- a formal Canon, permanent character-fate, romance or visual taste decision is required;
- paid API／provider spending or an API key is required;
- a destructive Git action is unavoidable and no safe alternative exists;
- AUTHOR_LOCKED sources directly conflict and source authority cannot resolve them;
- progress would require weakening Transaction, Knowledge Boundary, `stateVersion`, `contextHash` or irreversible approval safety;
- one evidence-based Medium attempt and one evidence-based High attempt still cannot restore the same core blocker.

Ordinary architecture choices, additive migrations, test failures, bounded refactors, Critic disagreement, Sandbox world candidate design and local Web implementation choices are not user stop conditions.

## 13. Development Memory

The reconstructed project discussion is stored at:

```text
docs/development_journal/2026-07-11_PROJECT_DISCUSSION_ARCHIVE_V1.md
```

It preserves the available user-visible discussion and Git evidence. It is not a raw export of unavailable older chat sessions and does not contain hidden system prompts, private reasoning or credentials.

Every REBOOT Stage must add or update an entry under:

```text
docs/development_journal/reboot01/
```

Future product-direction changes, important failures, user criticisms, model/provider decisions and rule retirements must be added to the Journal.

## 14. User Command To Start

```text
Fetch the latest remote default branch. Read PROJECT_STATE.md and execute the active REBOOT-01 umbrella Work Package and all mandatory Addenda end-to-end on a clean reboot/playable-depth-v1 worktree. Use gpt-5.6-sol with medium reasoning by default, follow CODEX_MODEL_AND_BUDGET_POLICY_V2 and the project .codex agent profiles, validate and Commit every internal Stage, Push the final branch, and report once at completion. Do not return for ordinary implementation decisions. Stop only for the True Stop Conditions in PROJECT_STATE.md and the active Work Package.
```

## 15. Source Of Truth Order

1. latest explicit user instruction;
2. `PROJECT_STATE.md`;
3. `.codex/project_controller.md`;
4. `docs/CURRENT_ROUTE_GATE_V3.md`;
5. active REBOOT-01 Work Package and Execution Addendum;
6. Reboot Blueprint plus Creative／World and Narrative Addenda;
7. Model／Subagent／Budget Policy V2 and project agent profiles;
8. verified code, tests, SQL output and formal world sources;
9. development journal and historical route evidence.
