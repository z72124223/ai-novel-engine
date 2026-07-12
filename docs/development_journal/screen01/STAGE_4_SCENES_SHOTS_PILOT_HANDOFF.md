# SCREEN-01 Stage 4 — 場景、鏡頭、Pilot、字幕與媒體交接

日期：2026-07-12

## 產物與 Hash

| 產物 | 數量／時長 | SHA-256 |
|---|---:|---|
| `samples/screen01/screen01_scene_list.json` | 4 場／440 秒 | `3f87270c0a3da6769a0d8d5946a4df257f43aa20f271713b9cb5963b1915e009` |
| `samples/screen01/screen01_shot_list.json` | 31 Shot／440 秒 | `a43a0843b7c9b7404604b5463428a956de893f79f8ed33232d072b8bcede67bb` |
| `samples/screen01/screen01_video_pilot_60_90s.json` | 9 Shot／82 秒 | `057dba23288248a8b2997c6325f71b54a779ff137018c449c2b7ddbbf76752b5` |
| `samples/screen01/screen01_video_pilot_zh-Hant-TW.srt` | 11 cues／79.7 秒內 | `8dce6fd94b4d6ba3b1f3b2edfcaee8e6f9865289f01bd64863edae52f7ffb1e0` |
| `samples/screen01/screen01_locale_release_matrix.json` | 4 發行目標 | `fa95b4c5d6a17ba2424ebdcd53ae1a27af48728caf09c83c8c2c60bc563583ec` |
| `samples/screen01/screen01_video_handoff.json` | Future-only handoff | `3cb0b77e983736355982a5d63c6197c34ac67003da7e665d13d50e4b9b1b1e04` |

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
