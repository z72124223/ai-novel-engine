# SCREEN-01 Stage 1 — 主檔、初版劇本與對白

日期：2026-07-12

## 產物

| 產物 | SHA-256 |
|---|---|
| `samples/screen01/screen01_story_production_master.json` | `f8cd061bf9b57f0e923839bd386dc11bec2cbd1b722fc18a7457f9406555579f` |
| `samples/screen01/screen01_screenplay_initial_zh-Hant-TW.md` | `376b2c33b2ef75193b48d79365c81aba0a13182fc5677d4910e97003d9053bd4` |
| `samples/screen01/screen01_dialogue_bible_zh-Hant-TW.json` | `023d5a1361bf8bb22afc847e02bb07823f7a3c7e5bb89a4772b125d9514f3cd2` |
| `samples/screen01/screen01_dialogue_track_initial_zh-Hant-TW.json` | `01672b1be3ba2d3340977dc42dfc9d41a25bccfba095cdd6be34a58686a16c1b` |

## 創作處理

- 將小說拆為 4 場、約 10 分 20 秒完整影視劇本。
- 內心推理改為手機特寫、鞋印／血點、停頓、急救動作與有限短句。
- 愛醬以停步、退半步、低吼、選路與身體支撐表達主體性，不使用人類對白。
- 陸塵行保持聰明；戲劇壓力來自潮路、不可兼得與未知行動者。
- 對白軌共 31 句，角色 ID、場次、意圖、潛台詞與估計時長可追蹤。

## 模型路由

- Story Production Master 與 JSON 整合：Medium。
- 初版影視劇本、角色聲線與正式對白：限定範圍 High。
- Hash 與 JSON parse：程式工具。
- High 使用完成後回到 Medium。

## 驗證

- JSON parse：PASS。
- STORY-01 來源未修改。
- Canon／圖片／影片／Provider：0。
- `git diff --check`：PASS。
