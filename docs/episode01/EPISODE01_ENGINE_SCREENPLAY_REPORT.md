# EPISODE-01 引擎對齊版第一話完成報告

日期：2026-07-15

結果：`EPISODE01_PASS / NON_CANON_PILOT / NO_MEDIA`

## 完成內容

已建立〈潮線之外〉第一話完整繁體中文影視劇本：

`samples/episode01/episode01_screenplay_engine_v1_zh-Hant-TW.md`

- 4 場完整故事；
- 31 句正式對白，與 DIALOGUE-01 選定稿逐句相同；
- 預估總長約 7 分 20 秒；
- 救人優先、證據暫失控制與第二串鞋印的懸念均保留；
- 箱內內容、第二名行動者身分與遺跡真相沒有被提前揭露。

劇本 SHA-256：`fd75b331cfa2050e72900b9503793c0f30424af7dae7b35ab465f81aec22ef22`
檔案大小：7,545 bytes。

## 怎麼使用專案引擎

本次不是讓 Codex 自由忽略引擎重寫。來源 manifest 綁定：

- STORY-01 引擎故事生命週期選定稿；
- SCREEN-01 Story Production Master；
- DIALOGUE-01 31 句選定對白；
- Chenxing 正式開場 Scene Setting Pack；
- WORLD-PHYSICS-05 正式物理回合證據。

並重新執行官方 CLI：

`run-world-physics-embodied-pilot`

重跑輸出與 Git 既有證據 byte-identical（位元組完全相同），SHA-256 為
`899f342e87ee5e1209a721f19266bddbe9683700172d66fc0512a38e126708e7`。

## 修正的具體錯誤

舊劇本寫「陸塵行握一下愛醬肩側，站起來」。引擎已正式拒絕把狗當成人類起身支點：

```text
dogSupportRejected=true
rejectedActionId=wp05-turn1-dog-shoulder-rejected
rejectionCode=physical_affordance_rejected
```

新版改成陸塵行用自己的腿與穩定岩面撐起身；愛醬只貼近陪伴，沒有承受人類重量。救援浪擊時的
搬移動作也改成前臂護胸、另一手托腹側，讓動作方向更清楚，但不改故事事件。

## 驗證

- WORLD-PHYSICS-05 replay：PASS，5 個正式回合、1 個拒絕嘗試。
- Focused Gradle tests：6／6 PASS。
  - `StoryChapterProductionRepositoryTest`：4／4；
  - `StoryChapterProductionCliTest`：1／1；
  - `WorldPhysics05EmbodiedScenePilotTest`：1／1。
- 劇本場數：4／4。
- 正式對白：31／31；與選定稿差異 0。
- Source manifest JSON：PASS。
- 來源路徑／SHA-256／bytes：PASS。
- 秘密與未揭露答案掃描：PASS。
- Production Kotlin／Test／Migration／Schema 修改：0。
- 完整近千項 Gradle suite：未重跑；本包沒有程式變更，已以 6 個直接測試取代無關重測。
- Provider／API 呼叫：0；費用：0。
- SQLite 直接寫入：0；Canon 升格：0；圖片／影片／音訊：0。

## 誠實限制

- 物理引擎只證明這個固定場景與已建模關係，不代表理解所有現實物理。
- 本稿尚未經真人 table read（桌讀）、海岸安全顧問或犬隻動作顧問確認。
- 本稿仍是 `NON_CANON_PILOT`；完成不等於正式 Canon 或最終拍攝版。

## 下一步

先交由 Product Owner 閱讀第一話。未收到新的明確工作包前，不自動開始第二次修訂、第二話、
OpenAI API、圖片或影片。
