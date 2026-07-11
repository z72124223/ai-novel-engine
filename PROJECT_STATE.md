# AI Novel Engine 專案狀態

> **唯一現況入口。** 新的 GPT、Codex CLI、Reviewer、Subagent 或外部協作者必須先讀本檔，再依本檔指定的 Route Gate、Work Package 與 Blueprint 執行。舊 HANDOFF、舊 charter、舊 route card、舊圖片 Work Package 或舊 `PLANS.md` 中的 `current`／`active`／`目前定案` 都是歷史標籤，不能跳過本檔。

## 1. Current Product Direction

目前正式產品方向是：

> 建立一個具有正式世界狀態、自主 NPC、主題驅動戲劇、可重播演化、本機可玩介面，以及未來視覺／影片輸出的長篇 AI 敘事世界。

近期優先順序已由「繼續擴充圖片與審核文件」切換為：

```text
真實引擎基線
→ 簡化過度阻擋
→ SQL 保存 Run Evidence
→ NPC 離場自主性
→ Drama Director／Critics／有限修訂
→ 塵行 5–10 回合可玩垂直切片
→ 同 Seed Replay／規則演化
→ 本機 Web 可玩介面
```

圖片與影片仍是長期產品方向，但目前先暫停，以免媒體產線再次取代引擎真實運行。

## 2. Mandatory Current Route

- Current Route Gate: `docs/CURRENT_ROUTE_GATE_V2.md`
- Active Blueprint: `docs/REBOOT_2026_PLAYABLE_DEPTH_BLUEPRINT_V1.md`
- Active Work Package: `docs/work-packages/REBOOT-01_PLAYABLE_DEPTH_SLICE.md`
- Model/Budget Policy: `docs/CODEX_MODEL_AND_BUDGET_POLICY_V1.md`
- Future Video Provider Plan: `docs/VIDEO_PROVIDER_BAKEOFF_PLAN_V1.md`
- Development Journal: `docs/development_journal/INDEX.md`

Required startup order:

1. `PROJECT_STATE.md`
2. `.codex/project_controller.md`
3. `docs/CURRENT_ROUTE_GATE_V2.md`
4. `docs/work-packages/REBOOT-01_PLAYABLE_DEPTH_SLICE.md`
5. `docs/REBOOT_2026_PLAYABLE_DEPTH_BLUEPRINT_V1.md`
6. `docs/CODEX_MODEL_AND_BUDGET_POLICY_V1.md`
7. `AGENTS.md`
8. Only the code, tests, world sources and historical evidence needed by the current REBOOT stage

## 3. Current Work Package

- ID: `REBOOT-01 Playable Depth Slice`
- Status: `READY_FOR_AUTONOMOUS_CODEX_EXECUTION`
- Required branch: `reboot/playable-depth-v1`
- Required base: latest remote default branch after `git fetch --prune origin`
- Main Codex model: current GPT-5.6 Codex coding model
- Default reasoning: `MEDIUM`
- Targeted escalation: `HIGH` only for SQL Migration／Transaction／Validator／StateReducer／Knowledge Boundary／hard consistency bugs／final integration review
- `MAX`／highest-cost mode: prohibited by default; use only under the evidence conditions in the model policy

REBOOT-01 is an approved Umbrella Work Package. After the user explicitly starts one Codex CLI session, Codex may complete its internal Stages 0–7, validate and Commit each stage, Push the final branch and report once. It does not need to return to GPT after every internal stage.

## 4. REBOOT-01 Required Outcome

Completion requires operating evidence, not only documents:

- one resettable Chenxing non-Canon scenario;
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

## 5. Narrative And Emotional Direction

The project must support a long-running ensemble world with **local closure and global continuation**.

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

The story should let readers laugh and become attached before heavier consequences land. Emotional turns must grow from prior causes, not arbitrary shock.

The protagonist may gradually gain enough power, knowledge, responsibility and influence to affect major conflicts, but growth must also increase cost, responsibility, moral pressure and the risk of loss. Supporting characters, rivals, romantic interests and ordinary people require their own goals and growth; they must not exist only to serve the protagonist.

There is no single terminal ending that permanently removes the world from the reader. Each arc must still pay off promises, alter relationships, leave lasting state and close a character phase. `No final ending` must not become endless repetition or unresolved setup.

## 6. Architecture Direction

Absorb design strengths rather than copying whole stacks:

- Generative Agents: memory stream, importance, reflection, planning, schedule, offscreen simulation;
- Concordia-like Game Master: agents propose actions; the environment resolves legal outcomes;
- SOTOPIA-like social evaluation: trust, cooperation, deception, persuasion, relationships and cost;
- TextWorld-like replay: reset, step, observation, score and deterministic comparison;
- ink-like deterministic narrative structure: flags, conditions, choices, variables and local endings.

Core roles:

```text
NPC Agents
→ Game Master
→ Drama Director
→ Writer
→ independent Logic / Character-Drama / Prose-Pacing Critics
→ at most one bounded revision
→ legality re-check
→ SQL evidence and Runtime result
```

The Drama Director may choose what conflict is worth presenting, but may not force a character to violate identity or leak Director metadata into character knowledge.

## 7. Validation Simplification

Only three categories remain hard blockers:

1. protocol safety;
2. world legality and transactional state safety;
3. irreversible human approval such as Canon, formal assets, destructive changes, release or paid providers.

Prose quality, pacing, repetition, AI-writing feel, weak subtext and emotional impact are `WARNING／SCORE`, not reasons to discard Run Evidence.

New evidence policy:

```text
raw model output → always persist as run evidence
legal but unpolished result → persist as provisional/candidate
world-legal State Delta → apply through existing Runtime transaction
quality findings → persist scores and revision suggestions
```

## 8. Multi-Agent And Cost Policy

Codex may use up to four task-shaped Subagents:

- RepositoryExplorer;
- RuntimeTester;
- NarrativeSystemsCritic;
- CostAndComplexityAuditor.

Subagents primarily perform read-only exploration, criticism and test analysis. Only one Writer modifies core Kotlin／SQL at a time.

Do not use the strongest reasoning level for routine work. Hashes, JSON parsing, path checks, diffs and statistics should be handled by scripts or deterministic tools.

## 9. Image Lane Status

```text
IMAGE_LANE_PAUSED_AFTER_USER_APPROVED_CANDIDATE
```

The Daping Coast master-panorama candidate was generated in the GPT image session and the user replied that they agreed with it. It has **not** yet completed Codex file transfer, SHA-256, controlled intake or Candidate Reference acceptance.

Therefore it remains:

```text
USER_APPROVED_CANDIDATE
not Canon
not APPROVED_REFERENCE
not LOCATION_REFERENCE_ACCEPTED
not PRODUCTION_LOCK
```

Do not continue Daping tasks 02–07 during REBOOT-01. Do not discard the candidate or the existing handoff evidence.

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
VIDEO_LANE_FUTURE_BAKEOFF_NO_SPEND
```

Future providers include Gemini Omni Flash, Veo, Grok Imagine Video, Seedance／Jimeng and Runway. The project must use one provider-neutral `VideoShotHandoff` and compare providers on the same approved clean keyframe and motion contract.

No paid API call, provider account purchase, API key, background generation or whole-episode generation is authorized during REBOOT-01.

## 11. Web Status

The old permanent prohibition on a local Web Server／React／Ktor／Port is retired.

REBOOT-01 Stage 6 may create a minimal local-only playable Web interface after Stages 0–5 pass. It may not be publicly deployed or connected to paid external model APIs during this package.

## 12. True Stop Conditions

Codex stops and asks the user only when:

- a formal Canon, character-fate, romance or visual taste decision is required;
- paid API／provider spending or an API key is required;
- a destructive Git action is unavoidable and no safe alternative exists;
- AUTHOR_LOCKED sources directly conflict and source authority cannot resolve them;
- progress would require weakening Transaction, Knowledge Boundary, `stateVersion`, `contextHash` or irreversible approval safety;
- one evidence-based Medium attempt and one evidence-based High attempt still cannot restore the core build/test loop.

Ordinary architecture choices, additive migrations, test failures, bounded refactors, Critic disagreement and local Web implementation choices are not user stop conditions.

## 13. Development Memory

The detailed reconstructed record of the available project discussion is stored at:

```text
docs/development_journal/2026-07-11_PROJECT_DISCUSSION_ARCHIVE_V1.md
```

It records the available user-visible conversation and Git evidence in detail. It is not a raw export of unavailable older chat sessions and does not contain hidden system prompts, private reasoning or credentials.

Future product-direction changes, important failures, user criticisms and rule retirements must be appended to the journal.

## 14. User Command To Start

The user can start the complete package with one instruction to Codex CLI:

```text
Fetch the latest remote default branch. Read PROJECT_STATE.md and execute the active REBOOT-01 umbrella Work Package end-to-end on a clean reboot/playable-depth-v1 worktree. Use GPT-5.6 Codex MEDIUM by default, escalate only according to the model policy, use bounded Subagents where useful, validate and Commit every internal stage, Push the final branch, and report once at completion. Do not return for ordinary implementation decisions. Stop only for the True Stop Conditions in the active Work Package.
```

## 15. Source Of Truth Order

1. latest explicit user instruction;
2. `PROJECT_STATE.md`;
3. `.codex/project_controller.md`;
4. `docs/CURRENT_ROUTE_GATE_V2.md`;
5. active REBOOT-01 Work Package;
6. Reboot Blueprint and Model Policy;
7. verified code, tests, SQL output and formal world sources;
8. development journal and historical route evidence.
