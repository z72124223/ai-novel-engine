# MVP-5：設定包匯入、依賴圖與影響分析

## 0. 前置狀態

MVP-4 已具備：

- 多回合 session、版本與 transaction
- 記憶、知識邊界、情緒因果
- 場景生命週期、轉場、劇情線
- 世界時間與 scheduled world events
- Context budget 與 continuity validation

本階段解決以下問題：

1. 作者如何安全加入人物、世界規則、勢力、地點、武器、功法、道具、秘密與關係。
2. AI/Codex 如何協助整理設定，但不能直接污染正式資料庫。
3. 新設定如何先驗證、預覽差異，再原子匯入。
4. 一項設定改動會影響哪些角色、場景、劇情線、記憶、道具與規則。
5. ContextCompiler 如何透過依賴圖只載入真正相關的設定。
6. 世界觀規模變大後，仍不能把整個設定庫送給 AI。

## 1. 禁止事項

- 不做 Android、Web UI、HTTP server 或任何 port。
- 不接外部 AI API。
- 不加入 Rust、Docker、embedding、向量資料庫或多代理框架。
- 不修改 `001`、`002`、`003`、`004` migration。
- AI/Codex 不得直接寫 SQLite。
- 不得繞過 schema validation、canon validation、impact preview 或 transaction。
- 不得以自然語言文字直接覆寫正式 canon；自然語言必須先轉成 setting pack JSON。
- 不得因匯入設定而自動把 author-only 真相交給角色。
- 不得讓新增武器、功法或道具無視既有世界規則、代價、前置條件與持有狀態。
- 不得在 active session 存在時靜默修改其依賴的既有 canon。
- 不得把所有設定載入每回合 context。
- 不得硬編碼 E2E setting pack 的具體 ID 或結果。

## 2. Git 與 migration

1. 先確認 MVP-4 已提交並建立 annotated tag `mvp-4`。
2. 建立分支：
   `feat/setting-pack-dependency-impact-runtime`
3. 新增：
   `app/src/main/resources/migrations/005_setting_pack_runtime.sql`
4. Schema 變更只能 additive，必須保留既有資料。
5. SQLite foreign keys 維持啟用。

## 3. Canon 與 Runtime 分離

### 3.1 Canon

長期世界設定，例如：

- character profile
- world rule
- faction
- location
- item / weapon
- technique / power system
- fact / secret
- scene definition
- story thread definition
- dependency edge

### 3.2 Runtime

某一 session 當下狀態，例如：

- character state
- emotion
- knowledge claim
- runtime memory
- scene instance
- item current owner/location
- active thread progress
- revealed secret
- world event state

匯入 canon 不得直接覆蓋 runtime state。

### 3.3 Active Session 保護

對既有 canon 的修改若影響任何 ACTIVE session：

- `preview-setting-import` 必須標記 `ACTIVE_SESSION_IMPACT`。
- 預設 `apply-setting-pack` 必須拒絕。
- 本階段只允許：
  1. 新增完全新的 canon entity；或
  2. 在沒有 ACTIVE session 時更新既有 canon。
- 不實作 live-session canon hot reload。
- 不得用 `--force` 繞過此限制。

## 4. Setting Pack 格式

新增版本化 JSON 協議。至少包含：

```json
{
  "schemaVersion": "1.0",
  "packId": "string",
  "packVersion": 1,
  "name": "string",
  "description": "string",
  "projectId": "string",
  "mode": "CREATE_PROJECT | ADDITIVE | PATCH_CANON",
  "createdAt": "ISO-8601",
  "source": {
    "type": "AUTHOR | CODEX_ASSISTED | IMPORTED",
    "name": "string"
  },
  "dependencies": [
    {
      "packId": "string",
      "minimumVersion": 1
    }
  ],
  "entities": {
    "characters": [],
    "worldRules": [],
    "factions": [],
    "locations": [],
    "items": [],
    "techniques": [],
    "facts": [],
    "secrets": [],
    "scenes": [],
    "storyThreads": []
  },
  "relations": [],
  "assertions": [],
  "metadata": {}
}
```

要求：

- `packId + packVersion` 唯一。
- stable entity ID 必填。
- 每個 entity 有 `entityVersion`。
- 每個 entity 有 `visibility`。
- 每個 entity 有 `status`。
- 每個 entity 有 `tags`。
- 每個 entity 有 `sourcePackId` 或可追溯 provenance。
- JSON 使用 UTF-8。
- canonical hash 使用穩定序列化，不依賴 key 原始順序。

## 5. Entity 規格

先重用現有資料表與 models；不得無理由建立重複的平行 canon 系統。

### 5.1 Character

至少驗證：

- stable ID、name、role
- public identity / private truth 分離
- core goal / fear / desire / moral line
- starting knowledge 不得包含 author-only secret
- starting items / techniques 必須存在
- faction / location reference 必須存在
- 不能用 runtime emotion 取代 canon personality

### 5.2 World Rule

至少：

- rule type
- statement
- scope
- priority
- hard / soft
- constraints
- exceptions
- visibility
- conflict keys

硬規則發生矛盾時必須阻止匯入，不能依文字順序任意覆蓋。

### 5.3 Faction

至少：

- public identity
- goals
- methods
- resources
- territory / locations
- leadership references
- visibility
- secrets 分離

### 5.4 Location

至少：

- location type
- parent location
- public description
- hidden features
- connections
- access conditions
- world-rule constraints

parent chain 不得形成 cycle。

### 5.5 Item / Weapon

武器作為 item subtype，不建立無必要的第二套持有系統。

至少：

- item type / subtype
- origin
- effects
- requirements
- costs
- limitations
- unique / stackable
- initial owner 或 initial location
- visibility
- related rules
- story hooks

驗證：

- unique item 不可同時有兩個 initial owner。
- item 不可同時存在互斥 location 與 owner 狀態。
- effect 不得違反 hard world rule。
- 高強度效果必須有明確 requirements、costs 或 limitations。
- hidden effect 不得進 player-known / character-known 初始資料。

### 5.6 Technique / Power

至少：

- technique type
- rank / stage
- effects
- prerequisites
- costs
- side effects
- forbidden uses
- required item / lineage / faction / prior technique
- visibility

驗證：

- prerequisite graph 不得形成 cycle。
- 引用 entity 必須存在。
- 不得無條件跨越 hard power limit。
- powerful effect 缺乏代價或限制時至少 fail 或明確 high-severity validation error。
- forbidden use 與 effect 自相矛盾時拒絕。

### 5.7 Fact / Secret

- canonical fact 與 secret 分離。
- secret 必須有 visibility、known-by、reveal conditions。
- author-only secret 不得成為角色初始 knowledge，除非角色明確列在 known-by。
- fact/secret reference 不得指向不存在 entity。

### 5.8 Scene / Story Thread

- 必須引用存在 location。
- requirement / thread relation 必須可解析。
- director goal 不得放入角色-facing knowledge。
- scene reference 不得形成無法進入且無合法入口的孤島，除非標記 intentional。

## 6. Relation 與依賴圖

每個 relation 至少包含：

- relationId
- sourceType
- sourceId
- relationType
- targetType
- targetId
- direction
- strength
- visibility
- runtimeRelevant
- reason
- metadata

至少支援：

- `MEMBER_OF`
- `LEADS`
- `OWNS`
- `LOCATED_AT`
- `KNOWS`
- `USES`
- `REQUIRES`
- `PREREQUISITE_FOR`
- `UNLOCKS`
- `AFFECTS`
- `GOVERNED_BY`
- `CONNECTED_TO`
- `OPPOSES`
- `ALLIED_WITH`
- `CAUSED_BY`
- `REVEALS`
- `PROTECTS`
- `TARGETS`
- `RELATED_TO`
- `PARENT_OF`

規則：

- source / target 必須存在。
- relation type 必須符合允許的 entity type 組合。
- `PARENT_OF`、`PREREQUISITE_FOR`、`REQUIRES` 不得形成非法 cycle。
- 對稱 relation 必須使用明確規則正規化，不建立不可控重複 edge。
- author-only edge 不得出現在 character-facing context。
- relation provenance 必須可追溯到 pack/import/revision。

## 7. Schema

先檢查現有 schema，再用 `005` 補足最小必要結構。

至少新增：

### 7.1 setting_packs

- pack_id
- pack_version
- project_id
- schema_version
- name
- description
- mode
- source_type
- source_name
- canonical_hash
- status
- imported_at
- created_at

唯一鍵：`pack_id + pack_version`。

### 7.2 setting_pack_imports

- id
- pack_id
- pack_version
- project_id
- requested_mode
- status
- validation_report_json
- diff_json
- impact_report_json
- source_file
- started_at
- completed_at
- failure_reason

### 7.3 entity_revisions

- id
- project_id
- entity_type
- entity_id
- revision_number
- entity_version
- canonical_hash
- source_pack_id
- source_pack_version
- snapshot_json
- change_type
- created_at

唯一鍵至少確保同一 entity revision 不重複。

### 7.4 canon_validation_runs

- id
- project_id
- pack_id
- pack_version
- status
- errors_json
- warnings_json
- statistics_json
- created_at

### 7.5 impact_analysis_runs

- id
- project_id
- change_set_hash
- status
- roots_json
- affected_entities_json
- affected_sessions_json
- affected_contexts_json
- recommended_tasks_json
- created_at

### 7.6 dependency_edges 擴充

若現有欄位不足，additive 增加：

- source_pack_id
- source_pack_version
- visibility
- runtime_relevant
- strength
- reason
- metadata_json
- canonical_hash
- status

不得破壞現有 edges。

## 8. 新增服務

至少實作：

- `SettingPackParser`
- `SettingPackSchemaValidator`
- `CanonSemanticValidator`
- `SettingPackDiffService`
- `SettingPackImportService`
- `EntityRevisionService`
- `DependencyGraphService`
- `ImpactAnalyzer`
- `CanonConsistencyValidator`
- `SettingContextResolver`

### SettingPackParser

- 讀取 UTF-8 JSON。
- 嚴格處理未知必要欄位與型別。
- 錯誤提供 JSON path。
- 不得在 parse 階段寫 DB。

### SettingPackSchemaValidator

檢查：

- protocol/schema version
- required fields
- ID 格式
- duplicate IDs
- pack dependency
- entity version
- stable canonical hash
- relation structure

### CanonSemanticValidator

檢查：

- referential integrity
- visibility / knowledge boundary
- world-rule conflict
- item owner/location conflict
- unique item conflict
- technique prerequisite cycle
- parent location cycle
- power/cost/limitation
- fact/secret consistency
- scene/thread references
- relation type compatibility
- active-session impact

### SettingPackDiffService

輸出：

- ADD
- UPDATE
- NO_CHANGE
- REMOVE_REQUESTED
- CONFLICT

本階段預設不允許透過 pack 物理刪除 canon entity。
若要求刪除，必須轉為 deprecation proposal 或拒絕。

Diff 必須顯示欄位級變化，且 sensitive/author-only 內容在一般 CLI 中遮罩。

### SettingPackImportService

流程：

1. parse
2. schema validate
3. semantic validate
4. diff
5. impact analysis
6. active-session guard
7. single SQLite transaction apply
8. entity revisions
9. dependency edges
10. import audit record
11. post-import canon consistency validation

任一步失敗整批 rollback。

重複匯入相同 `packId + packVersion + canonicalHash` 必須 idempotent，不重複建立資料。

### DependencyGraphService

提供：

- direct dependencies
- reverse dependencies
- bounded traversal
- cycle detection
- path explanation
- relation visibility filtering
- deterministic ordering

### ImpactAnalyzer

輸入 change roots，分析：

- 直接受影響 entity
- 反向依賴 entity
- scenes
- story threads
- character profiles
- memories / knowledge templates
- items / techniques
- active sessions
- context compilation risk
- suggested task queue

影響分析不得直接改資料。

至少有：

- severity
- relation path
- reason
- depth
- recommended action

預設 traversal depth 可配置，初始 4；必須防 cycle 與無限遍歷。

### CanonConsistencyValidator

可對整個 project canon 執行離線驗證，產生 errors/warnings/statistics，不改 DB。

### SettingContextResolver

ContextCompiler 不再只靠廣泛查詢；應結合：

- current scene
- active characters
- inventory / equipped items
- used techniques
- active threads
- world events
- dependency graph
- visibility
- knowledge boundary
- context budget

每個被選入的 setting 要有：

- entityId
- entityType
- relevanceScore
- relationPaths
- inclusionReasons
- allowedUses

不得把 author-only hidden truth 放入角色-facing區段。

## 9. Context 協議擴充

ContextPacket 新增：

- `relevantSettings`
- `settingContextBudget`
- `canonRevisionSummary`

### relevantSettings

按類型分組或統一陣列。每項至少：

- entityType
- entityId
- publicSummary
- characterAccessibleDetails
- constraints
- relevanceScore
- relationPaths
- inclusionReasons
- allowedUses
- visibility

禁止包含：

- 未授權 hidden truth
- 無關設定全文
- 完整 setting pack
- 全部 dependency graph

### settingContextBudget

至少：

- maxSettingBytes
- actualSettingBytes
- includedEntityCount
- omittedEntityCount
- includedByType
- omittedByType
- truncationApplied
- truncationReasons

沿用總 `maxContextBytes=24576`。設定區初始上限建議 `8192 bytes`，但不得裁掉：

- hard world rules
- continuity constraints
- forbidden reveals
- 當前使用 item / technique 的必要限制與代價

### canonRevisionSummary

至少：

- importedPackIds
- relevantEntityRevisions
- canonValidationStatus

不要放完整 revision snapshot。

## 10. CLI

新增：

### create-setting-pack-template

```bat
gradlew.bat run --args="create-setting-pack-template --project PROJECT_ID --out samples\setting_pack_template.json"
```

產生空白合法範本，不改 DB。

### validate-setting-pack

```bat
gradlew.bat run --args="validate-setting-pack --file path\pack.json"
```

只讀，輸出 schema 與 semantic validation。

### preview-setting-import

```bat
gradlew.bat run --args="preview-setting-import --file path\pack.json"
```

只讀，輸出：

- validation
- diff
- impact
- active-session blockers
- dependency cycles
- warnings

### apply-setting-pack

```bat
gradlew.bat run --args="apply-setting-pack --file path\pack.json"
```

只有 preview 可通過時才能 transaction apply。
不得提供跳過 validation 的參數。

### export-setting-pack

```bat
gradlew.bat run --args="export-setting-pack --project PROJECT_ID --out out\exported_setting_pack.json"
```

輸出可再次驗證的 canonical pack。
預設不輸出 runtime state。
author-only內容只在管理模式輸出並清楚標記。

### diff-setting-pack

```bat
gradlew.bat run --args="diff-setting-pack --file path\pack.json"
```

### impact-report

```bat
gradlew.bat run --args="impact-report --project PROJECT_ID --entity-type ITEM --entity-id item-id"
```

輸出反向依賴與路徑，不改 DB。

### show-dependencies

```bat
gradlew.bat run --args="show-dependencies --project PROJECT_ID --entity-type TECHNIQUE --entity-id technique-id --direction both --depth 3"
```

### validate-project-canon

```bat
gradlew.bat run --args="validate-project-canon --project PROJECT_ID"
```

## 11. Codex 作為設定窗口

Codex 可以根據作者自然語言生成 setting pack JSON，但固定流程必須是：

1. 作者描述設定。
2. Codex 只建立或修改 JSON pack 檔案。
3. 執行 `validate-setting-pack`。
4. 執行 `preview-setting-import`。
5. 顯示 errors、warnings、diff、impact。
6. 只有無阻塞錯誤時執行 `apply-setting-pack`。
7. 執行 `validate-project-canon`。
8. 回報正式匯入 entity、revision 與依賴。

Codex 不得使用 SQL、sqlite3 或自寫腳本直接改正式 DB。

本階段完成後，可建立 repo skill：

`.agents/skills/setting-pack/SKILL.md`

但 Skill 只能呼叫正式 CLI 流程，不可繞過引擎。

## 12. 測試

保留 MVP-1 至 MVP-4 全部測試，至少新增：

1. 合法 pack 可 parse 與 validate。
2. 不支援 schemaVersion 被拒絕。
3. duplicate entity IDs 被拒絕。
4. unknown reference 被拒絕並指出 JSON path。
5. pack dependency 未滿足被拒絕。
6. canonical hash deterministic。
7. 相同 pack/hash 重複 apply 為 idempotent。
8. 相同 pack/version 不同 hash 被拒絕。
9. technique prerequisite cycle 被拒絕。
10. location parent cycle 被拒絕。
11. hard world-rule contradiction 被拒絕。
12. unique item 多 owner 被拒絕。
13. item owner/location 互斥衝突被拒絕。
14. powerful technique 無 cost/limit 被拒絕。
15. author-only secret 不會成為角色 starting knowledge。
16. relation type/source/target 不相容被拒絕。
17. import 任一步失敗整批 rollback。
18. entity revisions 正確建立。
19. UPDATE 產生欄位級 diff。
20. NO_CHANGE 不建立多餘 revision。
21. REMOVE request 不物理刪除。
22. active session 存在時修改相關 canon 被拒絕。
23. active session 時新增無關 entity 可依規格處理且不自動注入 context。
24. impact analyzer 回傳 direct/reverse paths。
25. impact traversal 遇 cycle 不無限迴圈。
26. impact report read-only。
27. validate/preview/export CLI 不改 DB。
28. SettingContextResolver 只載入相關設定。
29. author-only edge/details 不進 character-facing context。
30. 當前使用武器/功法的限制與代價不得被 budget 裁切。
31. setting context 不超過設定區與總 context 預算。
32. export 後重新 validate 通過。
33. UTF-8 中文 round-trip 不損壞。
34. rejected pack 不留下 import/revision/edge 正式變更。
35. existing MVP scene/memory/runtime behavior 不被破壞。

## 13. E2E 驗收

使用全新 DB：

`data\setting-pack-e2e.db`

建立一份合法 setting pack，至少包含：

- 2 個 characters
- 2 個 world rules
- 1 個 faction
- 3 個 locations 與可達 connections
- 1 件 unique weapon
- 2 個一般 items
- 2 套 techniques，至少一條合法 prerequisite
- 2 facts
- 2 secrets，包含 author-only 與特定角色 known-by
- 2 scenes
- 3 story threads
- 至少 15 條 relations

要求：

1. validate PASS。
2. preview 顯示 ADD diff 與 impact。
3. apply transaction PASS。
4. validate-project-canon PASS。
5. export 後重新 validate PASS。
6. start session 後 compile context：
   - 只包含當前人物、地點、使用中的武器/功法與相關硬規則。
   - 不包含無關地點與勢力完整內容。
   - 不包含未授權 secret。
   - 武器/功法的 requirements、costs、limitations 保留。
7. 執行至少 5 回合，證明設定能實際影響：
   - 一次 item/weapon action legality
   - 一次 technique prerequisite
   - 一次 hard world rule
   - 一次 faction/world relation
   - 一次 location access condition
8. Context 仍小於 24576 bytes，setting section 小於或等於配置上限。
9. 角色仍依自己的 knowledge/goal 行動。

再建立 patch pack：

- 修改 weapon 的一項 hidden origin 或 limitation。
- 修改 technique cost。
- 新增一個 relation。
- 不刪除既有 entity。

在 session ACTIVE 時：

1. preview 必須顯示 impacted session。
2. apply 必須拒絕。
3. DB 不變。

結束/關閉 active session 後：

1. preview PASS。
2. apply PASS。
3. 建立新 entity revisions。
4. impact report 列出受影響 character、scene、thread、memory/context path。
5. 新 session 使用新 revision。
6. 舊 runtime log 不被改寫。

另建立至少 100 個設定 entity 的 deterministic scale fixture：

- validate/import PASS。
- dependency traversal 終止且 deterministic。
- 當前 context 仍只載入相關 subset。
- 不把 100 個 entity 全塞入 context。
- context 與 setting budget 均不超限。

## 14. 文件

更新：

- README.md
- AGENTS.md
- docs/ARCHITECTURE.md
- docs/DATA_MODEL.md
- docs/TURN_PROTOCOL.md
- docs/VALIDATION_RULES.md
- docs/ROADMAP.md
- docs/MEMORY_AND_KNOWLEDGE.md
- docs/SCENE_THREAD_RUNTIME.md

新增：

- `docs/SETTING_PACKS_AND_DEPENDENCIES.md`

說明：

- canon/runtime 分離
- setting pack schema
- stable IDs 與 entity versions
- validation pipeline
- preview/diff/apply
- provenance 與 revisions
- relation/dependency graph
- cycle rules
- impact analysis
- active session guard
- context relevance
- setting context budget
- Codex 正確新增人物、世界觀、武器、功法、道具的方式
- 不能直接寫 DB 的原因
- export / round-trip
- 已知限制：本階段不支援 active-session canon hot reload

AGENTS.md 只新增長期規則並引用本規格，不複製全文。

## 15. 驗收與回報

執行：

```bat
gradlew.bat clean test --no-daemon
```

最終回報：

1. VERDICT：PASS / FAIL
2. branch 與 commit status
3. migration 摘要
4. 新增與修改檔案
5. 全部測試數與結果
6. setting pack schema 摘要
7. 合法 E2E pack entity/relations 統計
8. validate/preview/apply/export 結果
9. canonical hash 與 idempotency 結果
10. dependency graph 與 cycle 測試
11. impact report 路徑
12. active session patch 阻擋結果
13. session 關閉後 patch/revision 結果
14. 5 回合設定實際影響摘要
15. context/setting budget
16. secret 與 knowledge boundary 結果
17. 100 entity scale fixture 結果
18. rollback 與 rejected pack DB counts
19. UTF-8 round-trip
20. 尚未解決問題

若失敗，先修到全部舊測試、新測試與 E2E 通過，不擴充規格外功能。
