# EPISODE-01 全專案與歷史稽核

日期：2026-07-15
狀態：`COMPLETE / V1_BASELINE_REJECTED / FORMAL_FIRST_EPISODE_RECOVERED`

## 結論

先前 `EPISODE-01 V1` 選錯故事來源。它把 STORY-01／SCREEN-01／DIALOGUE-01 的非 Canon 測試故事
〈潮線之外〉當成第一話，並幾乎沿用舊稿。正式作者鎖定的第一話其實是：

```text
第一話　塵
2026 台灣基隆八斗子大坪海岸
陸塵行＋愛醬
查潮 → 發現異常 → 過度專注 → 海水提早封路 → 颱風外圍加劇
→ 天色變暗 → 無訊號 → 退入山坡廢墟
```

周克、拾潮者、金屬箱、第二名行動者與救援抉擇是後來的 `NON_CANON_PILOT`（非正式測試），
不得移植進正式第一話。〈潮線之外〉仍保留為故事生產、影視改編與對白流程的歷史測試證據。

## 1. Repository 現況

- tracked files：1,255。
- `docs/`：404；`samples/`：288；`world/`：91；`app/`：407；`schemas/`：29；`memory/`：17。
- Kotlin production：222；Kotlin tests：166；additive migrations：16；work packages：32。
- 目前資料分為正式來源、正式結構資料、可重建索引、測試／評審證據；檔案多不代表都屬於當前產品狀態。
- `PROJECT_STATE.md` 與 `PLANS.md` 會累積歷史 Override。判斷內容權威時，世界與故事仍以
  `world/chenxing/formal/` 的 `AUTHOR_LOCKED` 來源優先。

## 2. 現場引擎重建驗證

本次不是沿用舊資料庫。從目前 Git 追蹤來源，在 Repository 外建立全新臨時 SQLite，執行官方：

```text
rebuild-project-data
```

結果：

| 項目 | 現場結果 |
|---|---:|
| verdict | `PASS` |
| verified inputs | 22 |
| migrations | 16 |
| Setting Packs | 5 |
| World Source documents | 9 |
| World Source chunks | 255 |
| logical tables | 13 |
| logical rows | 380 |
| Canon validation | `PASS` |
| findings | 0 |
| logical-content SHA-256 | `30fcd67770e4977548d1eaa7777baca0518bd09085fb6397d7fd93fdb3056c32` |

結論：引擎、Migration、Setting Pack 與 SQLite 重建路徑可正常使用。舊報告仍記錄 15 個 Migration，
那是當時的快照；現場引擎正確執行 16 個，不應再抄舊統計。

## 3. 正式第一話權威來源

| 優先 | 來源 | 狀態 | 第一話用途 |
|---:|---|---|---|
| 1 | `world/chenxing/formal/20_protagonist_pair_v1.json` | `AUTHOR_LOCKED` | 陸塵行、愛醬及關係 |
| 2 | `world/chenxing/formal/21_volume_one_opening_arc_v1.json` | `AUTHOR_LOCKED` | 2026 大坪海岸九個事件節拍 |
| 3 | `world/chenxing/formal/22_volume_one_opening_prologue_v1.json` | `AUTHOR_LOCKED` | 鬼故事式歷史前導與秘密邊界 |
| 4 | `world/chenxing/formal/26_volume_one_opening_storyboard_v1.json` | `AUTHOR_LOCKED` | 明定章名〈第一話　塵〉及 shot 00–09 |
| 5 | `world/chenxing/engine/chenxing_setting_pack_v4_opening_scenes.json` | validated / loaded | 地點、8 分鐘上坡、事件與禁止揭露 |
| 6 | `world/chenxing/formal/29_project_script_first_charter_v1.json` | `AUTHOR_LOCKED` | 劇本優先、媒體另行交接 |
| 7 | `world/chenxing/formal/30_volume_one_secrets_author_only_v1.json` | author only | 只用來阻止洩密，不進角色台詞 |

`28_volume_one_ruin_interior_storyboard_v1.json` 與 `32_crossing_experience_v1.json` 從 beats 10–22
繼續遺構內部與穿越；不屬於本次以 shot 00–09 結束的第一話範圍。

## 4. 正式角色事實

### 陸塵行

- 唯一現行姓名是陸塵行；「陸知行」「衛昭」已被取代。
- 36 歲、平頭、戴眼鏡、中度肥胖、沒有鬍渣。
- 有多種實作技能，靠拆解、觀察、試錯理解事物；不是全知專家。
- 核心驅動是匠人式好奇心；缺點是可能盯謎題太久，超過安全時間。
- 壓力升高時話會變少；幽默只能來自當下觀察，不能變成連續金句。

### 愛醬

- 母犬、已結紮、長毛哈士奇，是共同主角，不是吉祥物、線索機器或承重道具。
- 她透過嗅覺、耳朵、尾巴、步態、拒絕與重新靠近表達，不說人話。
- 她能先感到環境不對，但不能自動提供作者秘密答案。
- 陸塵行把她當家人；她把陸塵行當作家。

## 5. 歷史開發路線與可用證據

| 階段 | 真正完成的能力 | 對第一話 V2 的用法 | 不得誤用 |
|---|---|---|---|
| MVP1–MVP6 | 回合、Validator、Transaction、記憶、知識、場景、設定包、checkpoint | 世界合法性與狀態邊界 | 不等於能自動寫好劇本 |
| MVP7–MVP8 | 敘事稽核、 provisional／reader layer | 品質 Finding 與非 Canon 邊界 | 不把候選升格 Canon |
| Visual WP002A–003B | 圖片交接與大坪海岸候選 | 未來媒體交接 | 圖片線已暫停，不生成媒體 |
| REBOOT-01 | 六回合世界壓力、Drama 評分、NPC 自主候選 | 因果、代價、公平意外、角色不變笨 | sandbox 人物與事件不進第一話 |
| STORY-01 | SQL 章節 lifecycle、三評審、一次修訂、Hash 綁定 | 管理初稿／評審／修訂證據 | 〈潮線之外〉不是正式第一話 |
| SCREEN-01 | 場景、鏡頭、字幕與影視交接 | 劇本格式與鏡頭可拍性 | 四場 31 句只是非 Canon 測試 |
| DIALOGUE-01／02 | 台灣華語、呼吸表演、泛華語評審、選定規則 | 對白審查與一次修訂 | 舊 31 句不可原樣保留 |
| Retrieval-00／01 | 乾淨重建、來源分類、離線檢索基準 | 來源與 Hash 可追溯 | 相似度不等於真相 |
| NPC Actor 01–05 | 角色視角、環境軌跡、短命令、安全閉環 | 感知→優先序→動作→反應→狀態 | 評審 fixture 不是正式劇情 |
| NPC Actor 06–10 | 本機模型試驗、shadow、人工批准、雲端路由規劃 | 證明模型只能提案 | Qwen3-8B 已退出；雲端未實作 |
| World Physics 01–05 | 接觸、承重、抓取、放置、掉落、通行、持續狀態 | 排除狗肩支撐、瞬移、幽靈道具 | V1 只是不被規則否定，不是完整現實模擬 |

## 6. 被取代、暫停與失敗證據

- `SUPERSEDED`：灰關市場冷開場、陸知行／衛昭名稱、愛醬性別待定、開場在山徑、立即穿越。
- `NON_CANON_PILOT`：〈潮線之外〉、周克、拾潮者、金屬箱救援線、REBOOT sandbox。
- `PAUSED`：圖片／影片生成；大坪海岸 panorama 候選不是 Canon 或 Production Lock。
- `RETIRED_FROM_ACTIVE_ROUTE`：Qwen3-8B 劇情／NPC 生成路線。
- `FAILED_EVIDENCE`：暴雨城門演員版雖有人味，但繩索無錨點、突然承重、濕坡以身擋車及跨位置操作，總判定 `BOTH_FAIL`。
- `REJECTED`：EPISODE-01 V1；錯選故事、沿用舊稿、不能代表引擎重新產生正式第一話。

## 7. 第一話 V2 能力矩陣

V2 必須同時滿足：

1. **故事權威**：只走 prologue＋正式九拍，結束在遺構門口。
2. **角色權威**：只有陸塵行與愛醬；陸塵行不故意變笨，愛醬保持犬隻身體與自主性。
3. **環境軌跡**：潮位、浪、風、雨、亮度、訊號與可走路線逐步改變；環境變化必須改變行動。
4. **物理連續性**：先建立背包、手機、牽繩、落腳與位置後才能使用；不瞬移、不跨位置同時操作。
5. **自然對白**：低壓可有一句自嘲；危險出現後只保留能幫助當下行動的短句；不用 AI 校正式句型。
6. **知識邊界**：角色只知道潮表、天氣、現場痕跡與公開歷史；日本遺留真相、穿越機制與遺構內部維持未知。
7. **可拍性**：每個 beat 有可見動作、聲音、空間與狀態變化；不靠旁白解釋角色心理。
8. **誠實品質**：自動測試只能證明來源、格式與已知規則；最終是否達到人類作品品質仍由 Product Owner 判斷。

## 8. V2 禁止退步條件

- 不再使用〈潮線之外〉的事件、人物或 31 句對白。
- 不用「這不是 X，而是 Y」及其變體做聰明感。
- 不讓陸塵行連續講解自己每個推理步驟。
- 不把愛醬寫成人、工具、超自然答案或人體支點。
- 不用總結句替場景收尾，不用每個 beat 都做金句。
- 不因格式、Hash、SQL 狀態或測試綠燈宣稱「人味通過」。

## 9. 下一步

建立 `EPISODE-01A`：以本稽核為唯一恢復基線，產出〈第一話　塵〉全新 V2 初稿，完成邏輯／物理、
角色／對白、讀者／反 AI 三個角色隔離評審，最多一次修訂。最終稿保持 `NON_CANON_DRAFT`，
等待 Product Owner 閱讀，不要求 Product Owner 再做逐題 A/B。
