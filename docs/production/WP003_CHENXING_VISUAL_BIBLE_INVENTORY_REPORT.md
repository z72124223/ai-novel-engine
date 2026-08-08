# WP-003 Chenxing Visual Bible Inventory Report

Evidence base（證據基線）：`02c32925ed5c2dc5101f429fda2e6fa9184d732a`

The earlier remote inventory commit `eef2e05be88155da47b8b552cd82c68c1f5f145c` was revalidated after the current route-gate commits were merged. The formal, asset, preview, and WP-002A schema evidence paths were unchanged between the earlier and current bases.

## 結論

Primary result（主要結果）：`WP003_SCHEMA_GAP_FOUND`

Shot 01–03 尚不能進入真正的 WP-004 handoff（交接包）。大坪海岸沒有 master panorama（主全景）、top-down plan（俯視平面圖）、方向視圖、空間錨點表或比例參考；而 `shot_queue_v1` 又要求每一項至少有一個角色，無法誠實承載純 `LOCATION_BIBLE_CANDIDATE` 前置任務。這是場景幾何缺口加上有限度 schema 表達缺口，不授權修改 schema 或開始 WP-004。

## 已確認的正式視覺權威

- 陸塵行身材／全身：`lu_chenxing_photo_closest_174_80_turnaround_v2.png`，由 `23_character_visual_appearance_v1.json` 鎖定；開場基準為 36 歲、174 cm、80 kg、偏壯略胖、平頭、戴眼鏡、無鬍渣，瘦胖階段維持同一臉與骨相。
- 陸塵行臉：`lu_chenxing_face_reference_v1.jpg` 加 `photo_set_123123` 六張真人參考。獨立 face 檔與照片組其中一張內容相同，不能誤算成不同視覺證據。
- 愛醬：`aijiang_production_turnaround_v1.jpg` 是唯一 authoritative（權威）production turnaround；舊 concept sheet（概念表）是 `SUPERSEDED_HISTORICAL`。預設無項圈、無挽具。
- 2026 開場服裝：`lu_chenxing_opening_modern_costume_turnaround_v1.png`；黑色背心汗衫、炭灰短袖機能外套、深色戶外短褲、登山鞋、黑色側背包。封面／穿越後的 dark hooded traveler 不得代替開場服裝。
- 開場可用風格：黑白 storyboard 與 cinematic misty；dark-rain teal 限穿越後段。風格參考中的人物、衣著與道具不具 likeness（長相）或 costume（服裝）權威。

以上權威來自正式文件宣告；檔案雜湊與尺寸只證明檔案存在，不代表 Codex 重新做過視覺批准。

## 檔案完整性與異常

- 三個必要 production turnaround／costume 檔、六張 `photo_set_123123`、臉部檔及三張正式 style reference 都存在，已記錄 SHA-256、byte size（位元組數）與尺寸。
- `23_character_visual_appearance_v1.json` 引用的 lean、opening-stocky、cinematic 三張陸塵行 turnaround 不存在。
- 兩張已被正式文件標為歷史稿的 2D turnaround 路徑也不存在；這不影響 Shot 01–03，因現行 production turnaround 已存在。
- `candidates_v2/` 共有十五張圖片，但沒有正式來源選定，僅能列為 `CANDIDATE_ONLY`；每張均已記錄 Repository 相對路徑、SHA-256、byte size 與尺寸。

## Shot 01–03 props（道具）與 continuity（連續性）

- Shot 01：手機、潮汐筆記、眼鏡、側背包、戶外服裝／登山鞋；手機與潮汐筆記只有文字權威，沒有獨立 prop reference（道具參考圖）。
- Shot 02：岩面不自然接縫與人工邊角；只有 storyboard 文字，沒有位置／尺度圖。
- Shot 03：同一接縫、陸塵行蹲姿與指尖位置必須延續；愛醬耳朵朝海、毛微炸、前爪後挪半步。缺少可固定接縫與海蝕平台幾何的場景圖。

## Location Bible 缺口

`loc-daping-coast` 為 Tier 1 blocker（第一級阻擋）：master panorama、top-down plan、shot-facing directions、潮池／岩台／接縫 anchor table（錨點表）、人物與愛醬比例參考，以及 2026 年 7 月沿海天候光線圖全部缺少。Repository 沒有可批准或可上傳的海岸位置圖。

撤退至山坡廢墟的 Shot 04–09 為 Tier 2，同樣只有文字。遺跡 threshold、first shelter、service corridor、dead room、grey bottles、gold cache 與 two-door spatial pocket（雙門空間袋）為 Tier 3，具有充分文字權威但沒有正式幾何圖片。

360 panorama（全景圖）即使未來存在，也只能補充，不能取代主全景、平面圖、方向視圖、錨點與比例參考。

## WP-002A schema compatibility（相容性）

- 精確 Repository 路徑、版本、角色與 SHA-256 可由現行 reference binding／upload checklist 表達。
- 缺少對話附件可誠實標為 `REFERENCE_UPLOAD_REQUIRED`。
- return schema 的 `localFilePath` 可為 Repository 相對路徑或 `null`，不要求私人絕對路徑。
- 但是 `shot_queue_v1` 的 `characters` 為 `minItems: 1`，純場景 Location Bible 任務若硬塞角色即屬 schema misuse（結構描述誤用）。現行 handoff 的 shot scope 也以 shot IDs 為核心，沒有獨立 pre-shot location task（鏡頭前場景任務）。

Finding（發現）：`SCHEMA_ADJUSTMENT_REQUIRED_BEFORE_WP004`。總控需決定有限度修改 `shot_queue_v1`，或建立獨立 preliminary location handoff（前置場景交接）；WP-003 不修改 schema。

## ChatGPT 附件邊界

未來 Shot 01–03 至少需上傳十個現有檔案：陸塵行 production turnaround、六張 face photo set、開場 costume turnaround、愛醬 production turnaround、cinematic misty style reference。相同 costume 檔只上傳一次；獨立 face reference 與照片組末張內容重複，因此不另增一次必要上傳。

所有狀態目前都是 `UPLOAD_REQUIRED`，沒有任何 `UPLOADED_VERIFIED` 宣稱。場景幾何檔尚不存在，不能列為可上傳來源。

## 拒絕飄移

`world/chenxing/preview/opening_v1/DRIFT_NOTICE.json` 是十張舊 preview frame（預覽畫面）的拒絕權威。它們由純文字生成，未綁定鎖定身材、臉、服裝與愛醬參考，不得作正式生成 seed（種子參考），也沒有複製到本目錄。

## 下一個控制決策

在 WP-004 前，先決定並完成大坪海岸 Location Bible 的產出協議與 schema 承載方式。這份報告沒有啟動 WP-004，也沒有批准或生成任何圖片。

## 驗證摘要

- 三份 inventory JSON 均可解析，且 `sourceCommitSha` 已對齊最新證據基線。
- 16 筆資產紀錄的必填欄位、權威狀態、完整性狀態與正式證據路徑均已核對。
- 30 個不重複圖片檔、31 個紀錄綁定已重新驗證 SHA-256、byte size 與尺寸；重複綁定來自同一開場服裝檔同時服務 costume 與 continuity prop。
- `candidates_v2` 的 15 張候選圖已逐檔列出並雜湊，但維持 `CANDIDATE_ONLY`，不列入正式 Pilot 上傳。
- 十個未來必要 ChatGPT 上傳檔案皆存在且 Hash 相符，狀態全為 `UPLOAD_REQUIRED`。
- 三個 Location Tier 與 Shot 01–03 均完成來源路徑及 disposition 交叉檢查。
- 新增產物沒有私人絕對路徑、`UPLOADED_VERIFIED` 或未經證據的 final／Canon-image／production-lock 宣稱。

Primary result（主要結果）：`WP003_SCHEMA_GAP_FOUND`。WP-004 維持未啟動。
