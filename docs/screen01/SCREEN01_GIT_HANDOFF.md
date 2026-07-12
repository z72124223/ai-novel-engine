# SCREEN-01 Git 交接

## Git 身分

- Repository：`z72124223/ai-novel-engine`
- Branch：`codex/screen01-video-first-pilot`
- Base：`510527c24a612331590e25e42b004d00cd66858a`
- Final Commit：推送完成後由 Git 提供並在最終回報列出。Commit SHA 無法安全寫入包含本檔自身的同一 Commit，否則會形成自我引用循環。
- Hash manifest SHA-256：`ff138b45be478cc2d6b48bd770b22c4cc07f9b92ee59a12a6d9224db718ee8aa`

## 先讀

1. `PROJECT_STATE.md`
2. `docs/screen01/SCREEN01_FINAL_REPORT.md`
3. `samples/screen01/screen01_hash_manifest.json`
4. `samples/screen01/screen01_story_production_master.json`
5. `samples/screen01/screen01_screenplay_revised_zh-Hant-TW.md`
6. `samples/screen01/screen01_dialogue_track_revised_zh-Hant-TW.json`
7. `samples/screen01/screen01_scene_list.json`
8. `samples/screen01/screen01_shot_list.json`
9. `samples/screen01/screen01_video_pilot_60_90s.json`
10. `samples/screen01/screen01_video_handoff.json`

## 重要路徑

- 故事生產主檔：`samples/screen01/screen01_story_production_master.json`
- 初版／選定劇本：`samples/screen01/screen01_screenplay_initial_zh-Hant-TW.md`、`samples/screen01/screen01_screenplay_revised_zh-Hant-TW.md`
- 對白 Bible／初版／選定對白：`samples/screen01/screen01_dialogue_bible_zh-Hant-TW.json`、`samples/screen01/screen01_dialogue_track_initial_zh-Hant-TW.json`、`samples/screen01/screen01_dialogue_track_revised_zh-Hant-TW.json`
- 三份評審：`samples/screen01/reviews/*.json`
- 場景／鏡頭：`samples/screen01/screen01_scene_list.json`、`samples/screen01/screen01_shot_list.json`
- Pilot／字幕：`samples/screen01/screen01_video_pilot_60_90s.json`、`samples/screen01/screen01_video_pilot_zh-Hant-TW.srt`
- 地區語言：`samples/screen01/screen01_locale_release_matrix.json`
- 未來媒體交接：`samples/screen01/screen01_video_handoff.json`
- 全部內容 Hash：`samples/screen01/screen01_hash_manifest.json`

## 下一工具邊界

目前只可做人工內容審查或另開視覺前期工作包。`screen01_video_handoff.json` 中所有圖片／影片任務仍被人類視覺批准與核准參考資產阻擋。不得因本交接存在就自動生成媒體、呼叫 Provider 或升格 Canon。
