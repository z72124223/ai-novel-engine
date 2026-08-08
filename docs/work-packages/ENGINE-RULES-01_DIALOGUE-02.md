# ENGINE-RULES-01 / DIALOGUE-02 — 引擎强制生产规则

Status: `READY_FOR_AUTONOMOUS_CODEX_EXECUTION`

## 1. Outcome

建立统一 `ProductionRuleRegistry`，先完整落地对话最终选定门槛，并为角色、叙事、语言和媒体规则提供可扩展分类。

## 2. Required Kotlin Concepts

建议名称可依现有架构调整，但能力必须存在：

```text
ProductionRuleCode
ProductionRuleCategory
ProductionRuleSeverity
ProductionRuleSet
CharacterVoiceProfile
LanguageProductionProfile
DialogueArtifact
DialogueLineReview
DialogueFinding
DialogueReviewRun
DialogueApprovalState
DialogueSelectionDecision
ProductionRuleRegistry
DialogueReviewOrchestrator
DialogueSelectionGateService
DialogueFinalizationService
```

## 3. SQLite

Additive migration only.

至少记录：

```text
production_rule_sets
production_rules
character_voice_profiles
language_production_profiles
dialogue_artifacts
dialogue_review_runs
dialogue_line_findings
dialogue_selection_decisions
```

关键约束：

- selected artifact hash 不得为空；
- review artifact hash 必须等于 selected hash；
- required reviewer roles = 3；
- distinct reviewer instance = 3；
- blocker count = 0；
- all line IDs reviewed；
- revision hash != initial hash；
- auto revision count <= 1；
- rule set version 一致；
- decision actor/source/commit/hash 不得为空。

## 4. Engine-Owned Prompt Contract

生成和评审 Prompt 必须由引擎根据 Rule Registry、Character Profile、Language Profile 构建。

不得由外部手写 Prompt 成为唯一规则来源。

每次 Handoff 需包含：

```text
ruleSetId
ruleSetVersion
characterVoiceProfileIds
languageProfileId
artifactSha256
requiredReviewerRoles
hardBlockerCodes
```

## 5. Review Intake

引擎拒绝：

- missing quote；
- quote 不存在；
- wrong artifact hash；
- wrong rule set；
- duplicate reviewer instance；
- unknown line ID；
- incomplete line coverage；
- free-text finding without rule code；
- blocker marked as warning；
- reviewer claims revision without new hash。

## 6. Selection Gate

`select` 必须先执行：

```text
artifact identity
→ required reviews
→ reviewer independence
→ line coverage
→ blocker zero
→ revision lifecycle
→ rule set consistency
→ transaction
```

任何一步失败：

```text
NOT_SELECTABLE
```

并保存原因。

## 7. Mandatory Tests

### Negative

1. `好。先碰你。` cannot be selected。
2. Reviewer says “must rely on acting” -> BLOCKER。
3. Average 95 but one blocker -> reject。
4. Revised hash has only initial reviews -> reject。
5. Missing one reviewer -> reject。
6. Duplicate reviewer instance -> reject。
7. Quote not present -> reject。
8. Same hash revision -> reject。
9. All reviews pass but wrong Rule Set -> reject。
10. Direct repository selection bypass -> reject/transaction rollback。

### Positive

1. Natural short rescue line + three clean revised reviews -> selectable。
2. Warning-only regional term -> selectable with warning。
3. DIALOGUE-01 artifact re-reviewed under current Rule Set -> deterministic result。
4. CLI and service produce same gate result。

## 8. DIALOGUE-01 Re-evaluation

Do not alter history.

Create new tracked evidence under:

```text
samples/dialogue02/re_evaluation/
```

It must show:

- old artifact Hash；
- new Rule Set；
- each line finding；
- exact reason `好。先碰你。` blocks；
- candidate replacement；
- revised artifact；
- three revised-artifact reviews；
- final gate result。

If other blockers remain after one revision:

```text
HUMAN_DECISION_REQUIRED
```

Do not auto-revise again。

## 9. CLI

At minimum:

```text
evaluate-dialogue
show-dialogue-findings
finalize-dialogue
show-dialogue-selection
```

CLI output must be machine-readable and human-readable。

## 10. Evidence

Tracked outputs:

```text
docs/engine_rules01/
├── ENGINE_RULES01_FINAL_REPORT.md
├── ENGINE_RULES01_GIT_HANDOFF.md
└── ENGINE_RULES01_HIGH_REVIEW.json

samples/dialogue02/
├── active_rule_set.json
├── character_voice_profiles.json
├── language_profile_zh-Hant-TW.json
├── fixtures/
│   ├── accidental_flirtation_rescue.json
│   └── natural_rescue_control.json
└── re_evaluation/
    └── ...
```

## 11. Validation

- focused tests；
- full suite；
- migration upgrade from existing DB；
- rollback and idempotency；
- JSON/Hash/Quote/Line coverage；
- bypass tests；
- no secret/private path；
- no Canon/media/provider change；
- `git diff --check`；
- High read-only review。

## 12. Completion Classification

```text
ENGINE_RULES01_PASS
ENGINE_RULES01_PASS_WITH_SEMANTIC_WARNINGS
ENGINE_RULES01_HUMAN_DECISION_REQUIRED
ENGINE_RULES01_BLOCKED_BY_MIGRATION
ENGINE_RULES01_BLOCKED_BY_TECHNICAL_FAILURE
```

Do not start images, video, Provider or next chapter.
