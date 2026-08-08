# SCREEN-01 Stage 4 — 場景、鏡頭、Pilot、字幕與媒體交接

日期：2026-07-12

## 產物與 Hash

| 產物 | 數量／時長 | SHA-256 |
|---|---:|---|
| `samples/screen01/screen01_scene_list.json` | 4 場／440 秒 | `3f87270c0a3da6769a0d8d5946a4df257f43aa20f271713b9cb5963b1915e009` |
| `samples/screen01/screen01_shot_list.json` | 31 Shot／440 秒 | `646d58feeffd565214cd361b6123c1e41857fd88c68b12d846946fbce45950f9` |
| `samples/screen01/screen01_video_pilot_60_90s.json` | 9 Shot／82 秒 | `057dba23288248a8b2997c6325f71b54a779ff137018c449c2b7ddbbf76752b5` |
| `samples/screen01/screen01_video_pilot_zh-Hant-TW.srt` | 11 cues／79.7 秒內 | `8dce6fd94b4d6ba3b1f3b2edfcaee8e6f9865289f01bd64863edae52f7ffb1e0` |
| `samples/screen01/screen01_locale_release_matrix.json` | 4 發行目標 | `3542a990a296cc531962a775eb7b9a4851a3b7f376315d9e56a480d9e2b43098` |
| `samples/screen01/screen01_video_handoff.json` | Future-only handoff | `d586e5fcf0b7fbf56197cbb921ae9d9f6f22d442c7b8dc9180e3c5c859129576` |

## Pilot 選擇

- Segment：`shot-015` 至 `shot-023`。
- 時長：82 秒。
- 起點：救援空間與角色分工。
- 轉折：愛醬預警，陸塵行離開撬點救她。
- 終點：傷者脫困，陸塵行辨識下一波浪不可預測。
- 測試重點：自然華語、愛醬主體性、救人代價、空間連續與聲音敘事。

## 驗證

- Scene duration sum = 440：PASS。
- Shot duration sum = 440：PASS。
- Pilot shot sum = 82 且位於 60–90：PASS。
- Scene／Shot／Dialogue ID 引用：PASS。
- SRT 11 cues，時間單調、無重疊、全部在 82 秒內：PASS。
- JSON parse：PASS。

## 邊界

- Handoff status：`READY_FOR_FUTURE_VISUAL_PREPRODUCTION_NOT_GENERATION`。
- 圖片、影片、Provider authorization 均為 `false`。
- 未建立任何媒體檔、Provider job 或最終美術決策。
