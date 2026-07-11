# Current Route Gate V3 — Autonomous Playable Depth Reboot

Status: `ACTIVE_SINGLE_CURRENT_ENTRY_GATE`

Date: 2026-07-11

Supersedes for current routing:

- `docs/CURRENT_ROUTE_GATE_V1.md`
- `docs/CURRENT_ROUTE_GATE_V2.md`

Historical Gates remain evidence only.

## 1. Mandatory first read

Every GPT, Codex CLI, Reviewer, Subagent or external collaborator must read in this order:

1. `PROJECT_STATE.md`
2. `.codex/project_controller.md`
3. `docs/CURRENT_ROUTE_GATE_V3.md`
4. `docs/work-packages/REBOOT-01_PLAYABLE_DEPTH_SLICE.md`
5. `docs/work-packages/REBOOT-01_EXECUTION_ADDENDUM_V1.md`
6. `docs/REBOOT_2026_PLAYABLE_DEPTH_BLUEPRINT_V1.md`
7. `docs/REBOOT_2026_NARRATIVE_VOICE_AND_UNPREDICTABILITY_ADDENDUM_V1.md`
8. `docs/CODEX_MODEL_AND_BUDGET_POLICY_V2.md`
9. `docs/development_journal/INDEX.md`
10. `AGENTS.md`
11. only the code, tests, world data and historical evidence needed by the active Stage

Do not start from old `HANDOFF`, image Work Packages, route cards, charters, README status or historical `PLANS.md` headings.

## 2. Current active route

```text
Stage 0: run and measure the real engine
→ Stage 1: simplify overblocking and persist run evidence
→ Stage 2: bounded offscreen NPC autonomy
→ Stage 3: Drama Director, independent Critics, sarcasm voice and fair-surprise controls
→ Stage 4: real Chenxing 5–10 turn non-canon playable slice
→ Stage 5: same-seed replay and measurable rule evolution
→ Stage 6: minimal local Web playable interface
→ Stage 7: final integration, honest report and Push
```

Image lane is paused after a user-kept panorama candidate. Video lane is future provider-neutral Bake-off only.

## 3. Execution authority

`REBOOT-01` is a user-authorized Umbrella Work Package.

One explicitly started Codex CLI session may complete every Stage, with one or more bounded Commit(s) per Stage, and continue without returning to the former GPT control chat.

Codex must finish, Push and report directly to the user unless a True Stop Condition occurs.

This authority is limited to REBOOT-01. It does not authorize arbitrary future expansion.

## 4. Model decision

Main model:

```text
gpt-5.6-sol / medium
```

Do not run the entire package on High, Max or Ultra.

Use:

- `gpt-5.6-terra / low` for repository exploration;
- `gpt-5.6-terra / medium` for runtime/test analysis;
- `gpt-5.6-sol / medium` for narrative systems and normal implementation;
- `gpt-5.6-luna / low` for low-risk repetitive cost/complexity audits;
- `gpt-5.6-sol / high` only for Transaction, Migration, StateReducer, Knowledge, Version/Hash, core security, or final read-only integration review.

Follow `docs/CODEX_MODEL_AND_BUDGET_POLICY_V2.md` and project `.codex/config.toml`.

## 5. Subagent authority

Codex should use up to four direct Subagents when work is genuinely parallel.

Allowed read-heavy roles:

- Repository Explorer;
- Runtime Tester;
- Narrative Systems Critic;
- Cost and Complexity Auditor;
- High Risk Reviewer as an on-demand replacement.

Only the root/main Agent writes core Kotlin, SQL, migrations and transaction code. `max_depth = 1` prevents recursive agent fan-out.

## 6. Product and narrative target

The vertical slice must prove:

- trustworthy persisted world state;
- NPCs with independent goals and offscreen action;
- a Game Master that resolves actions without giving Agents direct state authority;
- a Drama Director that selects meaningful conflicts without controlling character will;
- independent logic, character/drama and prose/pacing critics;
- one bounded revision maximum;
- humor, warmth, conflict, anger/reflection, emotional cost and hope;
- protagonist sarcasm/banter used as character voice, not omniscience;
- intelligent recognition of obvious danger;
- no plot progression based mainly on unexplained stupidity;
- one fair second-order surprise after the obvious first danger is acknowledged;
- local closure with world continuation;
- a 5–10 turn real run, SQL evidence and same-seed replay;
- a minimal local Web playable interface.

## 7. Preserved hard boundaries

- SQLite formal runtime state;
- atomic Transaction;
- `stateVersion`, `contextHash`, idempotency;
- character Knowledge Boundary;
- location, item, skill and cost legality;
- formal Canon and asset approval;
- provenance and source Hash;
- no API key in Git;
- no paid Provider job without explicit user approval;
- no direct LLM write to formal state.

## 8. Retired modes

The following no longer control current work:

- document count equals progress;
- every prose warning blocks persistence;
- content must be near-perfect before SQL saves it;
- all Codex work uses highest reasoning;
- no Subagents;
- permanent ban on local Web/Ktor/React;
- full unit suite replaces real play;
- every internal Stage returns to GPT for permission;
- image or script is permanently the only terminal product;
- GPT-5.5-era assumptions remain current without revalidation;
- characters must ignore obvious danger to preserve a planned scare;
- random twists are acceptable substitutes for causal surprise.

## 9. Evidence policy

Always save:

- raw model result;
- Critic scores and findings;
- revision suggestion;
- revised result;
- protocol/world validation;
- rejected State Delta;
- accepted State Delta;
- model/reasoning metadata when available;
- elapsed time;
- replay seed;
- DecisionTrace;
- offscreen events;
- reader narrative.

Quality warnings do not erase evidence. World-illegal output does not apply state.

## 10. Development journal

The Journal is long-term project memory, not current routing authority.

Every Stage must add or update an entry under:

```text
docs/development_journal/reboot01/
```

Record failures, user criticism, model decisions, rules retired, rules adopted, real-play observations and replay results. Never write API keys or hidden system instructions.

## 11. Image and video state

```text
IMAGE_LANE_PAUSED_AFTER_USER_APPROVED_CANDIDATE
VIDEO_LANE_FUTURE_PROVIDER_BAKEOFF_NO_SPEND
```

The panorama candidate is not yet a formal Location Reference because Codex Intake/Hash/Candidate Reference was not completed.

Future provider selection uses `docs/VIDEO_PROVIDER_BAKEOFF_PLAN_V2.md` and rechecks official docs, price and terms on activation day.

## 12. True Stop Conditions

Stop and ask the user only for:

- formal Canon or AUTHOR_LOCKED conflict;
- permanent character death/fate or romance decision;
- formal visual approval;
- provider purchase, API key or spend;
- unavoidable destructive Git action;
- a requirement to weaken Transaction, Knowledge, Version, Hash or irreversible approval boundaries;
- the same core blocker still unresolved after one evidenced Medium attempt and one evidenced High attempt.

Do not stop for ordinary Bugs, tests, additive Migration, small architecture choices, Web technology selection, low Critic scores or failed first drafts.

## 13. Completion

REBOOT-01 is complete only when the Repository contains actual runnable evidence and the branch is pushed.

The final response to the user must include:

- final branch and Commit chain;
- exact run/test commands;
- test results and remaining failures;
- 5–10 turn evidence paths;
- SQL persistence evidence;
- NPC autonomy evidence;
- narrative score and replay comparison;
- local Web launch instructions;
- model/reasoning/subagent usage;
- High escalations;
- remaining risks;
- next recommendation.

Do not report completion merely because specifications or skeletons exist.