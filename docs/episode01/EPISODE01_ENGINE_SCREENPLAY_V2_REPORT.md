# EPISODE-01A 正式第一話恢復報告

日期：2026-07-15
結果：`COMPLETE / READY_FOR_PRODUCT_OWNER_REVIEW / NON_CANON_DRAFT`

## 最重要的修正

V1 不是正式第一話。它誤把後來為流程測試建立的非 Canon 故事〈潮線之外〉當作第一話，並幾乎沿用
DIALOGUE-01 舊稿。這次先讀完整 Repository 與 Git 歷史，再依作者鎖定來源重新建立：

```text
《塵行》第一話　塵
2026 台灣基隆八斗子大坪海岸
陸塵行＋愛醬
前導＋正式 beats 01–09
結束於山坡遺構入口
```

完整稽核在 `docs/episode01/EPISODE01_FULL_PROJECT_HISTORY_AUDIT.md`。V1 沒有刪除或改寫，保留為
`REJECTED_FOR_WRONG_STORY_BASELINE_AND_HUMAN_VOICE_REGRESSION` 歷史證據。

## 引擎與正式資料是否真的可用

本次從目前 Git 來源在 Repository 外重建全新臨時 SQLite，不沿用舊資料庫：

- `rebuild-project-data`：PASS；
- verified inputs：22；
- Migration：16；
- Setting Pack：5；
- World Source：9 份文件、255 chunks；
- logical tables：13；logical rows：380；
- logical-content SHA-256：`30fcd67770e4977548d1eaa7777baca0518bd09085fb6397d7fd93fdb3056c32`；
- `validate-project-canon`：PASS，findings 0。

這證明引擎、Migration、Setting Pack、世界資料與乾淨重建路徑目前能正常運作。它不等於引擎能自動
判斷劇本是否像真人；劇本品質仍需以角色、環境、物理與閱讀評審處理。

## 新稿如何產生

正式來源只採：

- `20_protagonist_pair_v1.json`：陸塵行與愛醬；
- `21_volume_one_opening_arc_v1.json`：九個開場事件節拍；
- `22_volume_one_opening_prologue_v1.json`：公開歷史前導；
- `26_volume_one_opening_storyboard_v1.json`：〈第一話　塵〉與 shot 00–09；
- `chenxing_setting_pack_v4_opening_scenes.json`：地點、八分鐘上坡、事件與秘密邊界；
- `29_project_script_first_charter_v1.json`：劇本優先；
- `30_volume_one_secrets_author_only_v1.json`：只作防洩密邊界，內容沒有輸入劇本。

初稿完成後做三個角色分離的內部審查：邏輯／物理、角色／對白、讀者／反 AI。三份審查都綁定
初稿 SHA-256 `50c20babb50ae935c19c27ca25bd94416514642a0a5c698060892b9fab7ccb3e`。

誠實說明：三份是同一 Codex 工作階段的角色分離審查，不冒稱三個外部獨立模型。之後只修訂一次。

## 唯一修訂處理了什麼

- 刪除無訊號後仍硬講的兩句笑話；
- 將校正式台詞改成現場反應：「還沒滿潮啊。」；
- 補出八分鐘上坡中的呼吸、步速、滑步、等待和位置連續；
- 愛醬只用嗅聞、耳朵、重心、停步、回望與低鳴表達；
- 陸塵行使用完整水泥牆面恢復平衡，不抓鏽管、不壓狗、不瞬移；
- 刪除門內不動垂線、刮痕與工整神秘句，避免提前洩漏 beats 10–22；
- 結尾停在門外，愛醬拒絕進入，陸塵行先檢查結構並尊重她的判斷。

## 產物

- 最終候選稿：`samples/episode01/episode01_screenplay_engine_v2_zh-Hant-TW.md`
- 最終稿 SHA-256：`03521bc397bfcbb8e0b3c43128c52af4c7dc53e63129e288fbd9d232044d4bdb`
- 最終稿大小：6,517 bytes；4 場（含前導）；約 6 分 20 秒。
- 來源 manifest：`samples/episode01/episode01_engine_source_manifest_v2.json`
- 初稿與三份審查：`samples/episode01/v2/`

## 驗證

- 聚焦測試：48／48 PASS。
  - Story Chapter production；
  - Engine Rules dialogue；
  - NPC embodied rollout；
  - NPC safety loop；
  - WORLD-PHYSICS-05。
- 完整測試：976／976 PASS。
- JSON：全部可解析。
- 來源／初稿／評審／最終稿 SHA-256：全部重新計算並符合 manifest。
- 正式九拍：完整；結尾未進入遺構內部。
- 禁止來源人物／事件：`潮線之外`、周克、拾潮者、金屬箱均未出現在最終稿。
- 禁止 AI 校正式句型：未出現「這不是」或「不是……而是……」。
- Production Kotlin／Test／Migration／Schema 修改：0。
- API／Provider／圖片／影片／音訊：0。
- SQLite 正式寫入／Canon 升格：0。

## 現在的真實狀態

這一版已修正錯誤故事來源，也通過目前專案能自動驗證的規則；它比 V1 更接近「角色真的在一個環境中
行動」，而不是把 AI 台詞排成劇本。但自動測試不能替 Product Owner 宣布作品好看，因此狀態仍是
`READY_FOR_PRODUCT_OWNER_REVIEW / NON_CANON_DRAFT`，不是 Canon、最終拍攝版或品質保證。

下一步只需要 Product Owner 直接閱讀完整劇本；不再要求填 A/B 表或替系統做無限測試。未收到閱讀結論
前，不自動開始第二話、第二次修訂、API 或媒體生產。
