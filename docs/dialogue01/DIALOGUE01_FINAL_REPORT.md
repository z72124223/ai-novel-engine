# DIALOGUE-01 V2 最終報告

## 結論

`DIALOGUE01_PASS_WITH_REGIONAL_WARNINGS`

31 句對白已完成逐句稽核、AI 模擬朗讀與三份彼此隔離的內部評審。唯一一次修訂只變更 dlg-022：

```text
潮表還早。
→
潮表還沒到。
```

這項修訂降低「還早」究竟指時間或潮勢的歧義，不改周可的現場判斷、不讓陸塵行變笨，也不改四場、三十一鏡、主要事件或 82 秒 Pilot。

## 證據

- 逐句稽核：`samples/dialogue01/dialogue01_line_audit.json`
- 模擬朗讀：`samples/dialogue01/dialogue01_simulated_read_aloud_notes.md`
- 台灣日常口語：`samples/dialogue01/reviews/taiwan_everyday_speech_review.json`
- 演員呼吸表演：`samples/dialogue01/reviews/actor_breath_performance_review.json`
- 泛華語理解：`samples/dialogue01/reviews/pan_chinese_comprehension_review.json`
- 最終劇本：`samples/dialogue01/final/screen01_screenplay_dialogue_final_zh-Hant-TW.md`
- 最終對白：`samples/dialogue01/final/screen01_dialogue_track_final_zh-Hant-TW.json`
- 最終字幕：`samples/dialogue01/final/screen01_video_pilot_dialogue_final_zh-Hant-TW.srt`

## 三份評審摘要

- 台灣日常口語：94，`PASS_WITH_MINOR_NOTES`。整體自然；提醒 dlg-012 必須由救援動作維持語意、dlg-022 原句略壓縮。
- 演員呼吸表演：8.7/10，`CONDITIONAL_PASS`。31 句大多是一個呼吸群對應一個動作；dlg-023 以表演停頓處理，不改字。
- 泛華語理解：88，`PASS_WITH_BLOCKING_INTEGRITY_EXCEPTION`。母稿跨地區可懂；`一一九`、`潮表` 保留台灣地域，交由畫面或未來在地化處理。

三位 reviewer 都觀察到工作樹實體位元組 Hash 與 SCREEN-01 舊宣告 Hash 不一致。這不是 DIALOGUE-01 造成：

| 檔案 | SCREEN-01 舊宣告 SHA-256 | 目前 Git 檔案實測 SHA-256 |
|---|---|---|
| screenplay | `4b2ac13f84541dfa904096fe650ed99a9f8d58e1519ba9f0b0816d85e56a554c` | `e06e1f7abaf90649ad5cfa51bc28aab6b5cdf74d76023380dc2958b299f3aa04` |
| dialogue | `6936ec2099f5a61ab07db631e9d19ef2bb49a8154f572fb5f360e0f7fdcec65c` | `c93b035f57cc50d1b427ba52c5b5b813ce6d84024d1ada36de790b13873db4a5` |

DIALOGUE-01 沒有改寫 SCREEN-01 歷史。最終產物使用實際檔案 bytes 重新計算 Hash，並在 metadata 同時保留舊宣告值，避免把舊問題隱藏或誤稱已修正。

## 最終 Hash

- screenplay：`a3816d7ab99a6aa92285f86642ef8a857ff369c030b06253d05333f7f05aa7e4`
- dialogue：`a990cbb8e6da28c97dc59f7dfbc48defa7253f7fe3714fa99702230695abfc4d`
- SRT：`6bbfcc86ba42001f894b27ee38576342d9647be59f80f32a4bd041e5db23e379`

## 地區與表演警告

- `一一九` 是台灣緊急救援識別，跨地區版不得直接改母稿，應由在地化層說明。
- `潮表` 需由手機、海水與表演提供語境。
- dlg-012 `好。先碰你。` 必須貼著救援動作，不演成挑逗或笑點。
- 本次只有 AI 模擬朗讀，尚未做真人 table read（桌讀）。

## 邊界

- `NON_CANON_PILOT` 保持不變。
- 未生成圖片、影片或音訊。
- 未呼叫 Provider、API 或產生成本。
- 未修改 Kotlin、Test、Migration、Schema、Canon 或 SCREEN-01 歷史。
- 不得自動開始第二次修訂或下一工作包。

## 驗證

最終交接驗證結果：

- JSON／31 句／3 reviewer／15 組 Shot 綁定／11 段 SRT／82 秒／13 項 artifact Hash：PASS。
- 私人路徑、秘密、Provider、Canon、媒體與 Git scope：PASS。
- focused Gradle tests：PASS。
- `gradlew.bat clean test --no-daemon --console=plain`：PASS（6m04s）。
- Kotlin 編譯僅出現既有 ExperimentalSerializationApi warning，沒有錯誤。
- `git diff --check` 與最終遠端同步在 Commit／Push 階段再次執行。
