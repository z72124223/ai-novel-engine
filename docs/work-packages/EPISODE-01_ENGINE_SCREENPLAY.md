# EPISODE-01 — Engine-Aligned First-Episode Screenplay

Status: `COMPLETED / NON_CANON_PILOT / NO_MEDIA`

Date: `2026-07-15`

## 目的

使用專案已提交的故事生命週期、正式來源 Hash 與物理引擎證據，產生〈潮線之外〉第一話完整繁體中文
影視劇本。新版不得覆寫 SCREEN-01／DIALOGUE-01 歷史，且必須修正已被 WORLD-PHYSICS-05 拒絕的
「把愛醬肩側當起身支點」錯誤。

## 必讀來源

- `samples/story01/story01_revised_draft.md`
- `samples/screen01/screen01_story_production_master.json`
- `samples/dialogue01/final/screen01_screenplay_dialogue_final_zh-Hant-TW.md`
- `samples/world_physics05/screen01_scene03_embodied_physical_pilot_v1.json`
- `world/chenxing/engine/chenxing_setting_pack_v4_opening_scenes.json`

## 允許修改

- `PLANS.md`
- `PROJECT_STATE.md`
- `.codex/project_controller.md`
- 本工作包
- `docs/episode01/EPISODE01_ENGINE_SCREENPLAY_REPORT.md`
- `samples/episode01/episode01_engine_source_manifest_v1.json`
- `samples/episode01/episode01_screenplay_engine_v1_zh-Hant-TW.md`

## 必須保留

- 四場完整故事與約 7 分 20 秒總長；
- 陸塵行、愛醬、周克與拾潮者的既有角色責任；
- 救人優先、證據暫失控制的選擇與代價；
- 31 句已選定對白；
- 箱內內容與第二名行動者身分保持未知；
- `NON_CANON_PILOT` 邊界。

## 物理修正

- 重新執行 `run-world-physics-embodied-pilot`。
- replay（重跑）必須與 tracked evidence byte-identical。
- `dogSupportRejected` 必須為 `true`。
- 起身動作改用自身腿部與穩定岩面；不得讓犬隻承受人類起身重量。

## 禁止事項

- 不修改任何歷史劇本或對白檔。
- 不新增沒有來源的劇情答案或台詞。
- 不修改 Kotlin、Test、Migration、Schema、SQLite 或 Canon。
- 不呼叫 API、Qwen3-8B、圖片／影片／音訊 Provider。
- 不產生第二話、下一章、圖片、影片或音訊。

## 完成驗證

- JSON 可解析，來源路徑與 Hash 全部吻合。
- 第一話 4 場、31 句對白、物理修正與秘密邊界通過。
- focused Story Chapter／World Physics tests 通過。
- `git diff --check` 通過；只提交允許檔案。
