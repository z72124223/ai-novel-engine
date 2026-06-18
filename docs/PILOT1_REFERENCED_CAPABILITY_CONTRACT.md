# Pilot-1：Referenced Capability Contract 修補規格

## 0. 現況

正式 Pilot：

- DB：`data/pilot1-rerun.db`
- SESSION_ID：`session-75e1b190-542c-49c0-9a5a-6b242bed8e97`
- stateVersion：9
- nextTurnNumber：10
- completed real turns：9
- pendingContextCount：0

Turn 10 尚未成立。

Turn 10 Context：

- audit：PASS
- qualityStatus：PASS
- actualBytes：21498 / 24576
- 玩家明確嘗試使用：`tech-basic-blood-breath`

但 final ContextPacket 中該 technique 只有：

- id
- projectId
- name
- description
- ownerCharacterId

缺少：

- prerequisites
- requirements
- costs
- limitations
- forbiddenUses
- current eligibility

因此不得生成結果或套用代價。

## 1. 目標

當玩家或角色在本回合明確：

- 使用 technique
- 詢問 technique
- 依賴 item / weapon 的效果
- 依賴能力前置條件或代價

Context 必須包含一份完整但精簡的「能力契約卡」。

AI 不得只憑名稱或描述使用能力。

若必要資料不存在或無法放入 Context，prepare 必須失敗，不得產生空洞但 qualityStatus=PASS 的 ContextPacket。

## 2. 禁止事項

- 不提高 `maxContextBytes=24576`。
- 不修改 Turn 10 玩家輸入。
- 不修改正式 Pilot DB 的故事狀態。
- 不正式執行 Turn 10。
- 不修改 migration 001 至 005。
- 不新增 migration，除非先證明現有 schema 完全無法保存既有 canonical technique data。
- 不直接寫 SQLite。
- 不為 `tech-basic-blood-breath`、此 session 或中文句子寫硬編碼。
- 不從文件、先前對話或模型常識補出 DB/context 中不存在的能力。
- 不讓 `description` 取代 prerequisites、costs、limitations。
- 不把整個 setting pack 或完整 entity revision snapshot 塞入 Context。
- 不新增 Android、網路、API、server、port、Rust、Docker、embedding 或向量資料庫。

## 3. Git 基線

先確認 Context Relevance Floor 已提交並合併至 Pilot 分支。

執行：

```bat
git status
git branch --show-current
gradlew.bat clean test --no-daemon
git diff --check
```

若 relevance-floor 尚未封存，先依既有流程提交/tag/merge。

建立修補分支：

```bat
git switch -c fix/pilot1-referenced-capability-contract
```

保留正式 DB 與 session，不得重建。

所有可修改 E2E 使用 DB 複本：

`data/pilot1-capability-contract-e2e.db`

## 4. 先做 Canonical Data Provenance 稽核

新增或擴充只讀診斷，針對某 technique 顯示：

- canonical entity source
- techniques table row
- JSON columns
- setting pack source
- entity revision
- dependency relations
- owner
- prerequisites
- requirements
- effects
- costs
- limitations
- forbidden uses
- visibility
- current session accessibility

建議 CLI：

```bat
gradlew.bat run --args="explain-capability --db DB --session SESSION_ID --entity-type technique --entity-id TECHNIQUE_ID --out OUT.json"
```

要求：

- read-only
- 不建立 pending context
- 不修改 stateVersion
- 不輸出未授權 author-only secret
- 明確指出每個欄位的 provenance

若 canonical DB 中真的沒有 prerequisites / costs / limitations：

- 回報 `CANON_CAPABILITY_CONTRACT_INCOMPLETE`
- 不得捏造
- 不得在 active session 中 patch canon
- 停止正式 Turn 10
- 只可在 session 關閉後透過 setting pack patch 修正

若資料存在，只是 Context resolver 沒有帶入，繼續以下修補。

## 5. Referenced Capability Semantic Requirement

擴充 `TurnSemanticPlanner`。

當玩家輸入或角色 proposal 明確涉及能力時，產生：

- `REFERENCED_TECHNIQUE`
- `REFERENCED_ITEM`
- `REFERENCED_WEAPON`

每項 requirement 至少包含：

- requirementId
- actorId
- entityType
- entityId
- referenceReason
- requestedUse
- mustBeSatisfied=true
- requiredContractFields
- allowedContextPaths

Technique 的 requiredContractFields 至少：

- id
- name
- owner / authorized actors
- effect scope
- prerequisites
- requirements
- costs
- limitations
- forbiddenUses
- currentEligibility
- eligibilityReasons

只有提到名稱但沒有使用意圖時，可以使用較小的 reference card；明確使用時必須使用完整 capability contract。

## 6. Compact Capability Contract

Final ContextPacket 新增或正規化為單一 section：

`activeCapabilityContracts`

每份 technique contract 至少包含：

```json
{
  "entityType": "TECHNIQUE",
  "entityId": "tech-id",
  "name": "name",
  "authorizedActorIds": ["character-id"],
  "intendedUse": "compact description",
  "effects": [],
  "prerequisites": [],
  "requirements": [],
  "costs": [],
  "limitations": [],
  "forbiddenUses": [],
  "currentEligibility": "ELIGIBLE | INELIGIBLE | UNCERTAIN",
  "eligibilityReasons": [],
  "allowedNarrativeClaims": [],
  "forbiddenNarrativeClaims": [],
  "sourceRevision": "compact revision reference"
}
```

規則：

- 不重複輸出 legacy `techniques` full record 與 capability contract。
- 若 legacy list 仍需存在，只保留 ID reference。
- contract 屬 Tier 1，明確使用能力時不可裁切。
- provenance 詳情留在 audit sidecar，不放入 AI Context。
- 空欄位不能被當成「沒有代價」；空欄位需由 canonical validator 判斷是否合法。

## 7. Capability Context Quality Guard

`ContextQualityGuard` 必須新增：

1. 明確使用 technique 時，必須存在對應 contract。
2. actor 必須在 authorized actors 中。
3. prerequisites / requirements / costs / limitations / forbiddenUses 必須有明確值或 canonical `NONE` 標記。
4. `description` 不能滿足 contract。
5. currentEligibility 必須可計算。
6. contract 不得包含角色未知的 hidden effect。
7. 若缺資料，qualityStatus=FAIL。
8. failure code：
   - `REFERENCED_CAPABILITY_CONTRACT_MISSING`
   - `CAPABILITY_CONTRACT_INCOMPLETE`
   - `CAPABILITY_ACTOR_NOT_AUTHORIZED`
   - `CAPABILITY_PREREQUISITE_UNSATISFIED`
   - `CAPABILITY_CONTEXT_BUDGET_UNSATISFIABLE`

Failure 必須發生在 pending context 建立前。

## 8. Result Protocol 與 Validator

AiTurnResult 對正式使用能力新增或正規化：

`capabilityUseChecks`

每項至少包含：

- actorId
- entityType
- entityId
- intendedUse
- eligibilityClaim
- satisfiedPrerequisites
- unsatisfiedPrerequisites
- appliedCosts
- observedLimitations
- resultScope
- evidenceStateDeltaIds
- reason

Validator 必須檢查：

1. `usedTechniqueIds` 與 capabilityUseChecks 對應。
2. actor 有權使用。
3. 不得宣稱 contract 外效果。
4. prerequisites 不滿足時：
   - 可描寫嘗試失敗或放棄
   - 不得產生成功效果
5. 成功使用且 contract 有 cost 時：
   - 必須有合法 cost state delta 或明確 canonical deferred cost
6. 不得把疲勞、治療、辨識、強化等效果自行加到 contract 外。
7. limitation 必須反映於 narrative / result scope。
8. forbidden use 必須拒絕。
9. duplicate result 不重複套用 cost。
10. rejected result 不留下 cost、memory 或 state change。

## 9. Item / Weapon 同步適用

雖然本次重現是 technique，架構必須可共用於 item / weapon。

當玩家明確：

- 使用武器
- 啟動道具
- 依賴鑰匙效果
- 要求某物辨識/治療/開門

必須同樣提供：

- authorized owner/user
- effects
- requirements
- costs
- limitations
- current availability
- location/ownership
- forbidden uses

不得為湊測試只做 technique 特例。

## 10. Budget 規則

目前 Turn 10 context 為 21498 bytes，理論上有足夠空間。

Capability contract：

- Tier 1
- 優先於低相關 memories、knowledge、threads、settings、recent summaries
- 不得被 `settingContextBudget actualBytes=2` 影響
- 可從 canonical setting / revision 解析後放入專用 compact section

若加入 contract 後超過 hard max：

1. 先裁 Tier 3。
2. 再裁 Tier 2。
3. 不裁 capability contract。
4. 仍無法 fit 時 fail：
   `CAPABILITY_CONTEXT_BUDGET_UNSATISFIABLE`

不得讓 qualityStatus PASS 但 capability details 缺失。

## 11. Audit Sidecar

`explain-context-budget` 與 `audit-context` 新增：

```json
{
  "capabilityCoverage": {
    "referencedCapabilities": [],
    "selectedContracts": [],
    "missingContracts": [],
    "authorizationChecks": [],
    "prerequisiteChecks": [],
    "costCoverage": [],
    "limitationCoverage": [],
    "qualityStatus": "PASS | FAIL"
  }
}
```

每個 selected contract 回報：

- entityId
- actorId
- selected context path
- bytes
- source provenance
- required fields present
- current eligibility
- omitted alternatives
- visibility status

完整 provenance 僅在 sidecar。

## 12. 測試

保留全部 178 tests，至少新增：

1. 明確使用 technique 產生 REFERENCED_TECHNIQUE。
2. technique contract 包含 required fields。
3. description-only technique 不通過 quality guard。
4. canonical data 存在時 resolver 正確帶入。
5. canonical data 缺失時 fail，不捏造。
6. owner mismatch 被拒絕。
7. prerequisite 不滿足時成功效果被拒絕。
8. prerequisite 不滿足可允許「嘗試失敗」。
9. 成功使用有 cost 時必須套用 cost delta。
10. duplicate result 不重複套用 cost。
11. rejected result 不留下 cost。
12. limitation 會限制 result scope。
13. forbidden use 被拒絕。
14. hidden effect 不進 character-facing contract。
15. capability contract Tier 1 不被低優先資料擠掉。
16. setting array 可為空，但 activeCapabilityContracts 必須完整。
17. legacy technique record 不與 contract 重複全文。
18. capability audit sidecar read-only。
19. explain-capability read-only。
20. actualBytes/hash 仍正確。
21. final context <=24576。
22. item contract 共用流程測試。
23. weapon contract 共用流程測試。
24. Turn 10 fixture 含完整 tech-basic-blood-breath contract，但不得硬編碼其 ID。
25. Turn 1 至 Turn 9 正式 evidence 不被修改。
26. forbidden secret 不洩漏。

## 13. DB 複本 E2E

複製正式 DB：

`data/pilot1-rerun.db`
→
`data/pilot1-capability-contract-e2e.db`

使用完全相同 Turn 10 輸入：

```text
我停在門檻內側，不再後退。

我只運起粗淺氣血調息，把呼吸和手腳的顫抖壓穩，不碰影子、不碰官差，也不碰趙無咎的腰牌。

等氣息穩下來，我才低聲問趙無咎：

「如果無籍之人和未登記的雪犬不能入城，你能不能給出一個不進城、不替愛醬契印，也暫時不把我們當犯人的合法處置？」

「規矩若不允許，你就直接說。」
```

流程：

1. explain-capability
2. explain-context-budget
3. prepare-turn
4. audit-context
5. 驗證 capabilityCoverage PASS
6. 使用 deterministic fixture result 驗證：
   - capabilityUseChecks
   - cost state delta
   - limitation
7. resolve-turn 只在 DB 複本執行
8. 驗證 stateVersion 只增加一次
9. 驗證 cost 只套用一次
10. duplicate resolve 被拒絕

另建立一份新 DB 複本或 transaction rollback fixture，驗證：

- 缺 cost delta 的 result 被拒絕
- DB 不變

不得把複本 E2E 算成真人 Pilot 回合。

## 14. 正式 DB 只讀驗證

對正式：

`data/pilot1-rerun.db`

只執行：

- explain-capability
- explain-context-budget
- pilot-preflight

回報：

- technique canonical source
- prerequisites
- costs
- limitations
- current eligibility
- selected capability contract bytes
- final predicted context bytes
- capabilityCoverage qualityStatus
- stateVersion=9
- nextTurnNumber=10
- pendingContextCount=0

不得正式 prepare 或 resolve Turn 10。

## 15. 文件

更新：

- README.md
- AGENTS.md
- docs/ARCHITECTURE.md
- docs/DATA_MODEL.md
- docs/TURN_PROTOCOL.md
- docs/VALIDATION_RULES.md
- docs/CONTEXT_RELEVANCE_FLOOR.md
- docs/CONTEXT_PACKET_NORMALIZATION.md
- docs/PILOT1_FINDINGS.md

新增：

- `docs/REFERENCED_CAPABILITY_CONTRACTS.md`

說明：

- capability reference detection
- canonical provenance
- compact contract
- actor authorization
- prerequisite evaluation
- cost application
- limitation enforcement
- hidden effects
- item / weapon / technique 共用流程
- quality fail behavior
- audit sidecar
- 為何能力名稱與 description 不足以讓 AI 使用能力

## 16. 最終回報

執行：

```bat
gradlew.bat clean test --no-daemon
git diff --check
```

回報：

1. VERDICT
2. branch / status
3. tests
4. canonical capability provenance
5. contract required fields
6. Turn 10 selected capability contract
7. prerequisites / costs / limitations
8. current eligibility
9. capabilityCoverage
10. context before / after bytes
11. safety / hidden effect validation
12. cost application / duplicate protection
13. DB 複本 E2E
14. 正式 DB read-only preflight
15. stateVersion / nextTurnNumber / pending count
16. 明確確認未正式執行 Turn 10
17. 若 canonical contract 本身缺資料，明確回報 blocker
18. 尚未解決問題
