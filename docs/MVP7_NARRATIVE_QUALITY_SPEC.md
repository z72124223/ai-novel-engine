# MVP-7 敘事品質規格草案

日期：2026-06-20

狀態：`CANDIDATE_A_IMPLEMENTED`。本文件原先只是規格草案；目前只完成 Candidate A 的 read-only audit（只讀稽核）入口，不代表 MVP-7 其他候選已批准。

## 1. MVP-7 目標

MVP-7 的目標是定義「這個互動小說引擎怎樣才算寫得好」，並為未來可能的 implementation（實作）建立驗收標準。

它不是單純追求華麗文筆，而是讓每一回合都像是從以下資料自然長出來：

- 玩家當回合輸入。
- 角色目前的目標、情緒、記憶、知識與可用行動。
- 場景狀態、scene requirements（場景條件）、story threads（劇情線）與 scheduled world events（排程世界事件）。
- 世界規則、物品、功法、能力代價與限制。
- Validator（驗證器）已經保護的秘密、狀態版本與上下文雜湊。

MVP-7 想改善的是：

- 角色更像活人，不像工具人。
- NPC（非玩家角色）的語氣與行動方式更可區分。
- 情緒反應有因果，而不是突然變臉。
- 回合不重複、不空轉。
- 世界能施壓，但不強迫玩家。
- 敘事有可讀性，但不犧牲規則邊界。

MVP-7 必須保留既有流程：

```text
玩家輸入
-> ContextPacket
-> 人工或 Codex 產生 AiTurnResult JSON
-> Validator
-> resolve-turn transaction
-> SQLite
```

## 2. 非目標

MVP-7 不做以下事情：

- 不建立 web server（網頁伺服器）、HTTP API（網路介面）或任何 port listener（連接埠監聽）。
- 不做 Web UI（網頁介面）或 Android UI（安卓介面）。
- 不接 OpenAI API 或其他外部 AI。
- 不加入 Rust、Docker、embedding（向量嵌入）或 vector database（向量資料庫）。
- 不加入 multi-agent runtime（多代理執行時）。
- 不讓 Codex 或 AI 直接寫 SQLite。
- 不改 schema（資料庫結構）或 migration（遷移），除非未來另有明確批准。
- 不把完整 setting pack（設定包）、完整 database（資料庫）或完整 dependency graph（依賴圖）塞進 context（上下文）。
- 不繞過 Validator、`stateVersion`、`contextHash` 或 SQLite transaction（交易）。
- 不用「增加上下文上限」來解決敘事品質；`24576` UTF-8 bytes 上限仍然保留。

## 3. 敘事品質定義

一個回合的敘事品質合格，至少要同時滿足：

- 尊重玩家輸入，不替玩家新增重大台詞、同意、移動、攻擊、轉場或決定。
- NPC 行動來自自己的目標、情緒、記憶、知識、約束與可用行動。
- 世界反應來自既有事實、規則、場景條件、劇情線、轉場、物品、功法與排程事件。
- 若 context 支援變化，回合要留下有意義的變化；若不支援變化，也要維持清楚的張力，而不是空轉。
- 文字具體、可讀、貼合當前情境。
- 不重複上一回合的套話、節奏或結構。
- 不提前揭露 forbidden secrets（禁止揭露的秘密）。

因此，敘事品質不是「文筆漂亮」而已，而是「規則一致、角色可信、玩家可操作、故事可讀」的總和。

## 4. NPC 活著感定義

NPC 有活著感，是指讀者能從 context 或 runtime（執行時）資料回答：

- 他現在想要什麼？
- 他在怕什麼、守什麼、懷疑什麼、拒絕什麼？
- 他知道什麼？又不知道什麼？
- 哪段記憶、信念、關係、傷勢、職責、道具或規則影響了他的反應？
- 為什麼這個反應像「這個人」，而不是任意 NPC 都能說？

NPC 活著感失敗的例子：

- 玩家一問，NPC 就立刻照辦。
- NPC 為了推劇情而坦白。
- NPC 忽略自己的職責、恐懼、懷疑、傷勢或關係。
- NPC 使用自己不知道的資訊。
- 所有人說話都像同一個旁白。
- story thread 的 director goal（導演目標）變成角色內在動機。

## 5. AI 感過重的判定

AI 感過重，是指輸出像泛用模型作文，而不像這個世界、這個場景、這些角色自然產生的回合。

出現以下情況時，應被標記為風險：

- 大量抽象反思，缺少具體觀察或行動。
- 多回合使用相同句型、相同節奏或相同收尾。
- 文字華麗，但實際資訊很少。
- 角色開始講主題、講道理，而不是從場景中行動。
- 所有人都方便地配合劇情。
- 敘事只是重述玩家輸入，沒有真正反應。
- 回合像驗收報告，不像故事現場。
- 過度使用排比、對稱句、抽象名詞或道德總結。

AI 感過重不等於「文筆不能好」。問題在於文字脫離了 runtime evidence（執行時證據）。

## 6. 世界自主推進定義

世界自主推進，是指世界可以在玩家沒有主動要求時施加壓力，但壓力必須來自已存在的 runtime evidence。

允許來源：

- 已到期的 scheduled world event。
- scene requirements、exit gates（離場門檻）。
- active story threads（活躍劇情線）的壓力。
- 地點危險。
- 已知 faction rules（勢力規則）。
- item / technique（物品 / 功法）限制。
- NPC 職責、懷疑、恐懼或目標。
- world clock（世界時間）或 travel time（移動時間）。

不允許：

- 沒有合法 transition（轉場）與玩家意圖就強制轉場。
- 臨時發明法律、組織或上級來推動劇情。
- 未解鎖就揭露隱藏真相。
- 世界事件未到期就硬觸發。
- 角色根據導演目標，而不是自己的知識與動機行動。

## 7. 角色知識邊界與敘事品質的關係

敘事品質必須建立在知識邊界上。即使一段文字很戲劇化，只要角色不可能知道那些資訊，就是品質失敗。

MVP-7 應把以下情況視為品質問題：

- 角色根據自己 knowledge claims（知識主張）、memory（記憶）或可見事實中不存在的資訊行動。
- 角色把 hidden secret（隱藏秘密）當成已知事實。
- 把 recent fact/event（近期事實/事件）偽裝成 recalled memory（召回記憶）。
- 角色無根據辨認物品、功法、地點、勢力或規則。
- 旁白用情緒、比喻或暗示提前透露 author-only truth（作者專用真相）。

允許角色誤解、懷疑、推測、動搖。很多時候，錯誤但有根據的推論，比突然正確更像活人。

## 8. 場景、Thread、World Event 如何支援敘事品質

MVP-7 不應把場景、thread（劇情線）和 world event（世界事件）當成導演命令，而應把它們當作可見壓力來源。

場景提供：

- 目前地點限制。
- 目前參與者。
- 眼前危險。
- 必要條件。
- exit gates。
- 可用 transition。

story thread 提供：

- 尚未解決的壓力。
- 已前進的證據。
- 被阻擋的證據。
- 玩家拒絕或 NPC 抵抗造成的後果。

scheduled world event 提供：

- 時間壓力。
- 外部人員抵達。
- 環境變化。
- 勢力介入。
- 截止點與升級壓力。

這些資料只有在 context 中存在、且通過 runtime 驗證時，才能支援敘事。它們不能偷偷變成強迫角色行動的隱藏命令。

## 9. 古白融合語感規則

本引擎的中文敘事可採「古白融合」：現代中文為主，少量古意為輔。

允許：

- 具體感官描寫。
- 短句與節奏感。
- 合理的古風詞彙，但不堆砌。
- 對話符合身分、時代與關係。
- 先寫行動，再寫感受。

避免：

- 生硬仿古，造成閱讀困難。
- 不符合角色的現代網路語。
- 用詩意抽象掩蓋實際發生了什麼。
- 每句都重鎚式煽情。
- 旁白替場景做道德總結。

原則：先清楚，再有味道。

## 10. 禁止散文腔與排比的規則

MVP-7 應降低「漂亮但不利於遊玩」的文字。

應標記或未來可能拒絕：

- 連續三段式排比。
- 為裝飾而使用「不是 X，而是 Y」連鎖句。
- 用抽象情緒詞取代可見行為。
- 普通回合結尾硬升華主題。
- 用過多比喻暗示尚未揭露的事實。
- 重複「空氣凝住 / 沉默落下 / 所有人都明白」這類固定橋段。
- 長段文字埋住真正的 state delta（狀態變化）。

好的輸出應清楚交代：

- 可見發生了什麼。
- 誰做了什麼選擇。
- 什麼改變了。
- 什麼仍不確定。
- 玩家下一步能合理做什麼。

## 11. 可驗證品質項目

以下項目未來可自動或半自動驗證：

| 項目 | 證據來源 | 可能檢查方式 |
| --- | --- | --- |
| 不替玩家決定 | 玩家輸入、narrative、character actions | 不新增重大未輸入玩家台詞、同意、移動或行動 |
| 不提前揭密 | secrets、reveal 狀態 | 沿用並擴充既有 forbidden reveal 檢查 |
| 知識邊界 | knowledge claims、retrieved memories、visible facts | 角色引用資訊必須能對應到已知或可見證據 |
| 能力使用有根據 | active capability contracts | 成功使用需有 `capabilityUseChecks` 與 cost delta |
| 場景合法性 | scene requirements、transitions | 沒有合法 transition 就不得轉場 |
| 世界事件時機 | scheduled events、world clock | 事件不能早於 due turn / due time |
| 記憶誠實性 | `retrievedMemories` | 只有真正選入的記憶能稱為召回 |
| 反重複 | recent turn summaries、narratives | 偵測重複句式、重複橋段、重複收尾 |
| 角色貼合度 | character runtime state | 行動應能對應到目標、情緒、信念或記憶 |
| 敘事密度 | narrative、state deltas | 不只總結，需有具體觀察、行動或後果 |

未來測試應包含正例與反例。

## 12. 不可自動驗證、需要人工判斷的項目

以下項目不應在第一版就變成硬性 Validator failure（驗證失敗）：

- 角色聲音是否有魅力。
- 幽默是否自然。
- 古白融合是否剛好。
- 緊張感是否好看。
- 節奏是否太慢或太快。
- 文筆是否足夠美。
- 一次拒絕是否有戲，而不只是程序正確。

這些更適合先做 audit prompt（稽核提示）、review checklist（審稿清單）或 Pilot scorecard（試玩評分表）。

## 13. MVP-7 最小可實作範圍候選

本規格不批准實作。若未來要開始 MVP-7 implementation，只能先選一個小範圍。

候選 A：Narrative quality audit report（敘事品質稽核報告）。

- 對 context/result pair（上下文/結果配對）做 read-only analyzer（只讀分析器）。
- 輸出玩家代理權、重複、弱根據、工具人化等 warning（警告）。
- 不寫 state。
- 第一版不增加硬性 Validator failure。
- 已批准的最小入口是 `audit-narrative-quality --context <path> --result <path> [--evidence <path>] [--out <path>]`。
- 這個入口只讀 JSON，不接收 `--db`，不得呼叫 `resolve-turn`、`StateReducer` 或 SQLite repository（資料庫存取層）。

候選 B：Result lint before resolve（resolve 前結果檢查）。

- 在 resolve 前提供 warning-level findings（警告級發現）。
- 除非另行批准，硬性失敗仍限於既有安全與協議邊界。
- 每種 warning 都要有測試。

候選 C：Pilot quality scorecard（試玩品質評分表）。

- 建立人工評分模板。
- 不改 runtime。
- 適合在品質標準仍偏主觀時先用。

候選 D：Narrow hard Validator rule（窄範圍硬性驗證規則）。

- 只針對非常客觀、已有 runtime 證據的違規，例如替玩家決定或偽稱記憶召回。
- 風險最高，應在 audit evidence（稽核證據）證明誤判少之後才做。

若未來批准實作，建議先選候選 A。

## 14. 風險

- 誤判：品質檢查可能把合理的含蓄寫法判成問題。
- 漏判：泛用 AI 腔可能通過結構檢查。
- 過度修正：故事可能變成程序報告，不像小說。
- context 壓力：品質證據可能擠壓必要 runtime context。
- 隱性 prompt drift（提示漂移）：風格規則可能變成非正式劇情命令。
- 角色扁平化：語氣規則可能讓角色變成刻板印象。
- Validator 過載：主觀文風不應太早變成硬性拒絕。
- 範圍蔓延：MVP-7 可能被拉向 UI、外部 AI 或自動生成；這些仍是非目標。

## 15. 後續是否進入 Implementation 的決策條件

不得進入 MVP-7 implementation，除非同時滿足：

- 使用者明確批准。
- 只選一個最小候選範圍。
- 候選範圍有清楚非目標。
- 寫 code（程式碼）前已列出必測項目。
- 寫 code 前已列出 CLI E2E 或 read-only audit E2E。
- 不需要 web / API / server / UI / external AI。
- 保留 Validator、`stateVersion`、`contextHash`、transaction 與 `24576` bytes 上限。
- 可以用一個聚焦 commit 完成。

建議的下一步批准語句：

```text
請執行 MVP-7 Candidate A：Narrative quality audit report。
先更新 PLANS.md，讀相關文件，只做 read-only analyzer，
不改 schema，不改 resolve-turn 寫入流程。
```
