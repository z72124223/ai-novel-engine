# Engine-Enforced Production Rules V1

Status: `ACTIVE_MANDATORY_ENGINE_CHARTER`

## 1. 核心決策

所有會影響「作品能否成為選定版」的規則，不能只存在：

- Prompt；
- Markdown；
- Agent 記憶；
- Reviewer 自由文字；
- 本機終端。

它們必須由引擎持有：

```text
Kotlin Rule Registry
+ SQLite Rule/Review/Decision Evidence
+ Selection Gate
+ CLI
+ Tests
```

## 2. 重要區分

自然度、雙關、調情誤讀、AI 味等屬於語意判斷，不能誠實地只靠 Regex 完成。

因此「寫死在引擎」的正確意思是：

1. 引擎寫死規則代碼、嚴重度與完成條件；
2. 引擎強制三個獨立評審逐句回傳結構化 Finding；
3. 引擎驗證引用、Hash、角色、Rule Set 與 Artifact 身分；
4. 任何 BLOCKER 都不准選定；
5. 修訂稿必須重新評審；
6. 缺少評審、格式錯誤、引用不存在時一律 fail closed；
7. 模型只負責判斷語意，不擁有批准權。

## 3. Rule Categories

```text
DIALOGUE
CHARACTER_VOICE
LANGUAGE_LOCALE
NARRATIVE_CAUSALITY
KNOWLEDGE_BOUNDARY
APPROVAL_LIFECYCLE
MEDIA_HANDOFF
```

## 4. Dialogue Rule Codes

至少實作：

```text
UNNATURAL_SPOKEN_CHINESE
ESSAY_LIKE_DIALOGUE
SYMMETRIC_ARGUMENT_EXCHANGE
CHARACTER_VOICE_COLLAPSE
HIGH_PRESSURE_OVERLONG_LINE
FORCED_WIT_UNDER_DANGER
ACCIDENTAL_FLIRTATION_OR_DOUBLE_MEANING
REQUIRES_PERFORMANCE_TO_AVOID_MISREADING
DIALOGUE_ACTION_REDUNDANCY
EXPOSITION_AS_SPEECH
AMBIGUOUS_PRONOUN_OR_OBJECT
MEMORABLE_LINE_DENSITY_EXCEEDED
UNSUPPORTED_REGIONAL_TERM
TRANSLATION_LIKE phrasing
```

實作時將非法空格名稱正規化為有效 enum，例如：
`TRANSLATION_LIKE_PHRASING`。

## 5. Severity

```text
INFO
WARNING
BLOCKER
```

规则：

- 平均分不能抵消 BLOCKER。
- `PASS_WITH_NOTE` 不能覆盖 BLOCKER。
- 需要靠导演／演员表演才能避免重大误解：
  `REQUIRES_PERFORMANCE_TO_AVOID_MISREADING = BLOCKER`
- 救援、死亡、创伤场景中的意外调情或双关：
  `ACCIDENTAL_FLIRTATION_OR_DOUBLE_MEANING = BLOCKER`
- 高压场景为了金句而牺牲可执行信息：
  `FORCED_WIT_UNDER_DANGER = BLOCKER`

## 6. Approval Lifecycle

```text
DRAFT
→ REVIEW_PENDING
→ REVISE_REQUIRED 或 SELECTABLE
→ REVISED_PENDING_REVIEW
→ SELECTABLE 或 HUMAN_DECISION_REQUIRED
→ SELECTED
```

禁止：

- 初稿評審直接批准不同 Hash 的修訂稿；
- 相同 Hash 冒充修訂；
- 未完整覆蓋所有對白行；
- Reviewer instance 重複冒充獨立；
- Review 引用不存在；
- Rule Set Version 不一致；
- BLOCKER 存在時選定；
- 超過自動修訂上限。

## 7. Three Required Reviewer Roles

```text
TAIWAN_SPOKEN_DIALOGUE
ACTOR_BREATH_AND_PERFORMANCE
PAN_CHINESE_COMPREHENSION
```

每位必须：

- 读取完整 Artifact；
- 逐句覆盖；
- 绑定 Artifact SHA-256；
- 绑定 Rule Set Version；
- 提供 exact quote；
- 提供 Rule Code／Severity；
- 使用不同 reviewerInstanceId；
- 不直接修改最终作品。

## 8. Character and Language Profiles

引擎需持有版本化 Profile：

### 陸塵行

- 短句；
- 观察型乾式幽默；
- 高压时减少笑话；
- 真实伤害后停下；
- 不靠降智；
- 不用长篇解释证明聪明。

### 周克

- 极短；
- 命令、路线、救援；
- 害怕时更少话；
- 不负责接主角所有笑点。

### 拾潮者

- 防卫、重复、碎裂；
- 不长篇解释箱子；
- 受伤时只保留关键词。

### 爱酱

- 无人类对白；
- 以动作、停步、低吼、方向与重心表达；
- 不成为等待口令的工具。

### Language Master

```text
zh-Hant-TW
Natural Taiwan Mandarin
Pan-Chinese comprehensibility through localization metadata
```

## 9. Deterministic Checks

引擎直接检查：

- line length／pressure level；
- 每句 speaker；
- dialogue／shot／subtitle reference；
- subtitle overlap；
- 每场 memorable-line count；
- revised Hash differs；
- reviewer count／role／instance；
- quote exists；
- all line IDs covered；
- blocker count；
- selected artifact equals reviewed artifact；
- Rule Set Version；
- transaction／rollback；
- no provider/canon/media authority escalation。

## 10. Semantic Checks

由模型评审，但由引擎强制格式与结果：

- 真人口语；
- AI 论文感；
- 意外调情；
- 双关误读；
- 强行机智；
- 角色声线；
- 台灣口语；
- 泛华语理解；
- 动作可替代性；
- 表演依赖风险。

## 11. Canonical Negative Fixture

以下句子必须阻挡：

```text
拾潮者：别碰箱子。
陆尘行：好。先碰你。
```

至少触发：

```text
ACCIDENTAL_FLIRTATION_OR_DOUBLE_MEANING / BLOCKER
REQUIRES_PERFORMANCE_TO_AVOID_MISREADING / BLOCKER
FORCED_WIT_UNDER_DANGER / BLOCKER
```

不能因为上下文“可以解释”为救援而降级。

正向对照：

```text
拾潮者：别碰箱子。
陆尘行：先救你。
```

正向对照仍需完整评审，不因 fixture 自动批准。

## 12. GitHub Evidence

Rule Set、Profile、Review、Finding、Selection、Migration、Test Report 与最终 Artifact 都必须有：

```text
Repository
Branch
Commit SHA
Repo-relative Path
Content SHA-256
Rule Set Version
```

本机数据库不是团队最终交接。
