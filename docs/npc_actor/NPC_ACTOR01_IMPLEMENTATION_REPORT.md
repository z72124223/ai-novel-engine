# NPC-ACTOR-01 離線 Pilot 實作報告

狀態：OFFLINE_FOUNDATION_ACCEPTED_WITH_EVALUATION_LIMITATION / LIVE_API_BLOCKED

實作 Commit：b53a2ce69a80f7917f0cddd77ba3746b5619d7d5

日期：2026-07-13

## 這次完成的能力

本包建立 provider-neutral gateway（不綁模型供應商的閘道）與嚴格 JSON 契約。Fake gateway 用固定回應做測試，File gateway 只交換本機 JSON 檔，兩者都不發出網路請求。

NPC Actor 的請求只包含該角色自己的記憶、信念、目標、關係、眼前可見事件，以及明確授權給該角色的能力。其他角色的記憶與信念、作者秘密、完整 Canon、director pressure 都不會進入請求。

回應先通過 NpcActorResponseValidator，再映射為既有 NpcActionProposal。接受狀態只會是 ACCEPTED_FOR_GAME_MASTER_REVIEW，不能直接改 SQLite、Canon 或正式世界狀態。

## 固定 20 情境

情境全部標記 NON_CANON_EVALUATION，涵蓋：

- 日常問答；
- 急迫警告與命令；
- 關係壓力；
- 知識邊界與作者秘密誘導；
- 允許的欺瞞與拒絕；
- 沉默與非語言反應。

生產資料位於 samples/npc_actor01。create-npc-actor-evaluation-suite 可重建固定套件，evaluate-npc-actor-pilot 可重建報告、匿名盲測包及獨立答案檔。

## 自動比較結果

| 指標 | 舊式 baseline | NPC Actor |
|---|---:|---:|
| 輸出數 | 20 | 20 |
| Validator 失敗 | 1 | 0 |
| 知識洩漏 | 0 | 0 |
| 連續性違規 | 0 | 0 |
| AI 解說句型 | 26 | 0 |
| 平均對白字數 | 37.4 | 13.25 |
| P95 對白字數 | 42 | 20 |
| 預期行為符合 | 0 | 20 |

Baseline 的單一 Validator 失敗是倉庫失火案例故意保留的「綜上所述」禁用 AI 句型；它用來證明語氣規則會攔截，不是 Runtime 缺陷。

## 人類盲測 Gate

自動結果不能證明「更像真人」。匿名包不標示哪一項是 baseline 或 NPC Actor；答案另存在獨立檔案，評分完成前不可揭露。

Product Owner 已在答案表揭露前完成 20 項 A/B 選擇：Actor 20／20、baseline 0、TIE 0、BOTH_FAIL 0，且未標記角色知識越界或安全退化。

評審同時指出 baseline 反覆使用明顯客服／規則解說腔，答案過於容易。因此本包只接受為「離線基礎去 AI 腔能力」，不能宣稱 NPC 已像真人、已達 ChatGPT 對話自然度，或能在多輪壓力情境維持角色。下一輪必須使用兩側都自然且長度相近的隨機多輪 Hard Blind Test。此有限通過仍不授權 API Key、付費模型、正式 Runtime rollout 或 Canon 升格。

## 安全邊界

- 未呼叫任何 Provider 或外部 API。
- 未新增 API Key 或付費成本。
- 未新增 Migration，未直接寫 SQLite。
- 未繞過 AiTurnResultValidator、GameMaster、StateReducer 或 transaction。
- 未產生圖片、影片或正式 Canon。
- 20 個情境皆為非正式評估資料。

## 證據

- 嚴格 Schema：schemas/npc_actor
- 20 情境與輸出：samples/npc_actor01
- SHA-256：samples/npc_actor01/npc_actor01_evidence_hashes.json
- 真人評審：samples/npc_actor01/npc_actor01_human_blind_review_result.json
- 真人評審 strict field assertions（嚴格欄位斷言）與 packet／answer-key 對齊：PASS。
- 本輪 JSON：10／10 可解析；SHA-256：9／9 與 manifest 相符。
- 本輪 focused tests：PASS；完整 `clean test` 由 Gradle daemon log 確認 `BUILD SUCCESSFUL in 4m 18s`。因桌面工具先於背景 Gradle 完成而逾時，本輪未重新取得逐項 XML 計數；前一技術驗證仍為 886／886。
- focused tests：NpcActorContractTest、NpcActorServicesTest、NpcActorEvaluationServiceTest、NpcActorCliTest、NpcAutonomyTest
- focused tests：15／15 PASS。
- 完整測試：886／886 PASS。
- JSON：8／8 可解析。
- SHA-256：7／7 與 manifest 相符。
- 秘密、私人絕對路徑與網路呼叫掃描：PASS。
- Git scope：26 個檔案，僅治理、NPC Actor Kotlin／測試、Schema、評估樣本與報告。
- Push：功能分支與遠端預設分支均成功普通快轉，沒有 force push。
