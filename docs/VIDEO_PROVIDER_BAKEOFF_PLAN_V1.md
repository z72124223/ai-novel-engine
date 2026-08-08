# AI 影片 Provider Bake-off 計畫 V1

Status: `FUTURE_PROVIDER_SELECTION_PLAN_NO_SPEND`

Date: 2026-07-11

## 1. 觸發背景

使用者提供 YouTube 影片：

```text
GPT5.6 + Codex：自动化生成视频全流程实战分享！
https://www.youtube.com/watch?v=dqbWp_Aqj1k
```

目前可驗證到影片標題；本次工具環境沒有取得完整逐字稿，因此不得把影片中未逐項核對的說法寫成正式 API 規格。

但影片代表的核心工程模式值得吸收：

```text
GPT / Codex 負責規劃、拆鏡、組 Prompt、呼叫工具、輪詢、下載、驗證與重試

專用影片模型負責影片生成
```

也就是：GPT-5.6／Codex 是 Orchestrator，不必和影片生成模型綁成同一家供應商。

## 2. 決策原則

不先相信行銷展示，也不因單支影片就綁定 Provider。

正式選擇必須用同一份：

- 核准乾淨 Keyframe；
- Motion Prompt；
- Camera Contract；
- Forbidden Drift；
- Duration；
- Aspect Ratio；
- Review Rubric；

進行可重複 A/B 測試。

## 3. 目前候選

### 3.1 Gemini Omni Flash

官方定位：快速多模態影片生成與對話式編輯，可同時處理文字、圖片、音訊與影片；可將 First Frame 與 Reference Images 一起指定，並透過多輪對話修正影片。

優點：

- 多輸入整合；
- 可在同一請求中同時指定起始畫面與角色／物件參考；
- 對話式編輯適合修正局部漂移；
- 原生音訊；
- 官方文件將其列為一般影片生成的預設選擇。

限制：

- 目前為 Preview；
- 不支援影片延長與 First/Last Frame interpolation；
- 多影片參照有限制；
- Provider 規格可能變更。

目前官方估算：720p 約 USD 0.10／秒。

### 3.2 Google Veo 3.1

優點：

- Text-to-video、Image-to-video、Video-to-video；
- First Frame＋Last Frame；
- 最多三張 Reference Images；
- 原生音訊；
- 720p、1080p；部分模式可 4K；
- 4／6／8 秒與影片延長；
- 適合需要精確首尾狀態或後續延長的鏡頭。

限制：

- 參考圖、1080p／4K 與延長通常要求 8 秒；
- 標準版成本較高；
- Preview 模型可能變更；
- 生成檔需及時下載，官方保留時間有限。

目前官方每秒價格：

- Lite：720p USD 0.05；1080p USD 0.08；
- Fast：720p USD 0.10；1080p USD 0.12；
- Standard：720p／1080p USD 0.40；4K USD 0.60。

### 3.3 xAI Grok Imagine Video

官方 API 支援：

- Text-to-video；
- Image-to-video；
- Reference-to-video；
- Video editing；
- Video extension；
- 1–15 秒；
- 480p／720p；
- `grok-imagine-video-1.5` 的 Image-to-video 可到 1080p。

優點：

- API 清楚、非同步輪詢流程容易自動化；
- 影片生成、編輯、延長皆有正式端點；
- Image-to-video 適合從核准關鍵影格開始；
- Reference-to-video 最多七張參考圖，適合角色、服裝、愛醬、場景與風格；
- 價格透明。

重要限制：

- 同一請求不能同時使用 Image-to-video 與 Reference-to-video；
- `grok-imagine-video-1.5` 不支援 Reference-to-video；
- Reference-to-video 最長 10 秒；
- 需要在「鎖第一幀」與「多參考圖」之間選擇模式。

目前官方價格：

- `grok-imagine-video`：480p USD 0.05／秒；720p USD 0.07／秒；
- `grok-imagine-video-1.5` Image-to-video：480p USD 0.08／秒；720p USD 0.14／秒；1080p USD 0.25／秒。

### 3.4 ByteDance Seedance 2.0／即夢路線

官方 Seedance 2.0 頁面宣稱統一多模態音視訊共同生成，支援文字、圖片、音訊、影片輸入，以及多模態參考與編輯，並提供 `Get API` 入口。

潛在優點：

- 多模態參考範圍廣；
- 動作、鏡頭與電影感可能很有競爭力；
- 即夢／剪映生態方便人工試驗；
- 若 API 帳號可取得，可能適合中文創作工作流。

目前需要先確認：

- 使用者所在地與帳號是否可合法取得 API；
- 即夢消費者介面與 Seedance API 是否為同一能力／版本；
- 個人或企業資格；
- 自訂真人／角色參考限制；
- 商用條款、肖像授權與資料保留；
- 實際價格、併發、解析度與輸入限制。

因此 Seedance／即夢列為強候選，但在 API 帳號與條款未核實前，不作唯一主線。

### 3.5 Runway

Runway 提供正式 API，包含 Image-to-video、Text-to-video、Video-to-video、Character Control、Multi-shot workflow、聲音與語音工具。

定位為成熟工作流候選，特別適合：

- 多鏡頭工作流；
- Character Control；
- Video-to-video；
- 後續聲音／配音整合。

進入正式 Bake-off 前需再核對當時模型、成本與角色一致性表現。

## 4. 初步推薦

在「已經有核准乾淨 Keyframe」的情況：

1. **第一順位實驗：Gemini Omni Flash**
   - 理由：可以把 First Frame 與多張 Reference 一起使用，並用對話修正；最適合我們的角色一致性與反覆微調需求。
2. **第二順位實驗：Grok Imagine Video 1.5 Image-to-video**
   - 理由：API 簡潔、1080p、首幀鎖定明確，適合短鏡頭生產。
3. **第三順位實驗：Veo 3.1 Fast 或 Lite**
   - 理由：原生音訊、First/Last Frame、參考圖與延長能力完整。
4. **Seedance 2.0／即夢**
   - 使用者若能取得可用帳號或 API，必須納入同鏡頭比較；它可能在運動與電影感上勝出，但先驗證接入與商用條款。

這只是測試順序，不是最終冠軍。

## 5. 正式 Bake-off 測試設計

### 5.1 使用同一鏡頭

第一個影片比較不得用多人戰鬥。

建議：

```text
一名角色或純場景
5–8 秒
16:9
低幅度動作
單一相機運動
清楚的首幀
```

例如：

- 陸塵行停在雨中，呼吸、衣物與目光小幅變化；
- 愛醬耳朵轉動、毛髮受風；
- 大坪海岸霧氣、潮池反光與緩慢推鏡。

### 5.2 每家生成數

```text
每個 Provider 2 個 Candidate
```

不做大量抽卡。

若四家參加、每支 6 秒，則先估算硬上限後再由使用者批准預算。

### 5.3 評分

每項 0–10：

```text
identityConsistency
costumeConsistency
animalMarkingConsistency
sceneGeometryConsistency
firstFrameFidelity
motionNaturalness
cameraControl
promptFidelity
temporalCoherence
artifactRate
audioUsefulness
editability
apiReliability
latency
costEfficiency
commercialFit
```

硬失敗：

- 角色換臉；
- 服裝改變；
- 愛醬花紋改變；
- 場景主要幾何移位；
- 多出人物／肢體；
- 劇情或鏡頭意圖被重寫；
- 產生未授權文字、Logo 或著作權角色。

## 6. Provider-neutral Contract

核心引擎只認：

```text
VideoShotHandoff
ProviderProfile
ProviderRequest
ProviderResult
VideoReview
VideoIntake
```

`VideoShotHandoff` 最少包含：

```text
shotId
sourceKeyframePath
sourceKeyframeSha256
optionalLastFramePath
referenceImages
mode
aspectRatio
duration
resolution
motionPrompt
cameraMotion
allowedCharacterMotion
forbiddenDrift
continuityFrom
continuityTo
expectedOutputFile
reviewContract
```

Provider-specific 差異留在 Profile，不寫進故事引擎核心。

## 7. 成本與安全

目前狀態：

```text
NO_PROVIDER_SPEND_AUTHORIZED
```

啟動任何 Provider 前必須由使用者在當時的工作介面明確決定：

- Provider；
- 預算上限；
- API／人工介面；
- 商用／肖像條款；
- 是否允許上傳真人參考；
- 是否保存 Provider 輸入。

API Key：

- 只放環境變數或秘密管理；
- 不寫 Git；
- 不寫日誌；
- 不回傳給 Agent；
- 每次請求記錄 Provider、模型、參數與成本估算，不記 Key。

## 8. 何時啟動

只有全部成立才進入 Bake-off：

- REBOOT-01 可玩垂直切片完成；
- 人物美術風格已由使用者選定；
- 至少一張乾淨單鏡 Keyframe 通過 Candidate Reference；
- Motion／Camera／Continuity Contract 完成；
- 使用者批准一個小額比較預算。

## 9. 官方來源（建立文件時）

- xAI Video Generation: https://docs.x.ai/developers/model-capabilities/video/generation
- xAI Reference-to-Video: https://docs.x.ai/developers/model-capabilities/video/reference-to-video
- xAI Models/Pricing: https://docs.x.ai/developers/models and https://docs.x.ai/developers/pricing
- Gemini Video Overview: https://ai.google.dev/gemini-api/docs/video
- Gemini Omni Flash: https://ai.google.dev/gemini-api/docs/omni
- Veo 3.1: https://ai.google.dev/gemini-api/docs/veo
- Gemini Pricing: https://ai.google.dev/gemini-api/docs/pricing
- ByteDance Seedance 2.0: https://seed.bytedance.com/en/seedance2_0
- Runway API: https://docs.dev.runwayml.com/api/

Provider 能力與價格高度易變。真正啟動時必須重新查官方文件，不以本檔歷史數字直接花費。