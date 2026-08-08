# AI 影片 Provider Bake-off 計畫 V2

Status: `FUTURE_PROVIDER_SELECTION_PLAN_NO_SPEND`

Date: 2026-07-11

Supersedes for future provider selection:

- `docs/VIDEO_PROVIDER_BAKEOFF_PLAN_V1.md`

## 1. 現在不選冠軍

目前圖片線暫停、REBOOT-01 優先。此文件只保留未來可執行的比較方法，不授權購買 API、建立 Job、保存 Key 或產生影片。

正式狀態：

```text
NO_PROVIDER_SPEND_AUTHORIZED
NO_API_KEY_STORAGE_AUTHORIZED
NO_VIDEO_JOB_AUTHORIZED
```

## 2. YouTube 影片紀錄

使用者提供：

```text
https://www.youtube.com/watch?v=dqbWp_Aqj1k
```

Repository 先前辨識到標題為：

```text
GPT5.6 + Codex：自动化生成视频全流程实战分享！
```

目前工具環境無法取得完整逐字稿或可靠字幕，因此本專案不得聲稱已逐句看完影片，也不得把影片中未經官方文件核對的模型、價格、參數或限制寫成事實。

可吸收的工程模式是：

```text
GPT-5.6 / Codex = Orchestrator
專用影片模型 = Generator
```

Codex負責腳本、分鏡、Prompt、參考圖、Request、輪詢、下載、Hash、Review 與重試；生成 Provider 可替換，不綁死故事引擎。

## 3. Provider-neutral 核心

引擎與劇本層只認：

```text
VideoShotHandoff
ProviderProfile
ProviderRequest
ProviderResult
VideoReview
VideoIntake
```

`VideoShotHandoff` 至少包含：

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

Provider差異只能放在 `ProviderProfile`，不能滲入故事 Canon 或角色資料。

## 4. 當前官方候選

### 4.1 Google Gemini Omni Flash

Google 官方影片總覽目前建議一般影片生成優先使用 Gemini Omni Flash，理由包括：

- 多輸入推理：文字、圖片、音訊、影片；
- 影片一致性；
- 角色一致性；
- 事實準確性；
- 多輪對話式編輯；
- 元素替換或視角調整。

官方來源：

- https://ai.google.dev/gemini-api/docs/video

對本專案的潛在優勢：可同時理解 Keyframe、角色／服裝／場景參考和修訂要求，適合一致性與局部修正。

風險：模型與價格可能快速變更，正式 Bake-off 時重查。

### 4.2 Google Veo 3.1

Google 官方將 Veo 3.1 定位於需要：

- 原生音訊；
- 影片延長；
- Last-frame control；
- First／last frame；
- 參考圖片；
- 既有管線整合。

官方來源：

- https://ai.google.dev/gemini-api/docs/video
- https://cloud.google.com/vertex-ai/generative-ai/docs/video/generate-videos-from-an-image

對本專案的潛在優勢：精確首尾狀態與鏡頭延續。

### 4.3 xAI Grok Imagine Video

xAI 官方目前提供：

- Text-to-video；
- Image-to-video；
- Reference-to-video；
- Video editing；
- Video extension；
- 非同步 Job 流程；
- 不同解析度／時長模式。

官方來源：

- https://docs.x.ai/developers/model-capabilities/video/generation
- https://docs.x.ai/developers/model-capabilities/video/reference-to-video
- https://docs.x.ai/developers/pricing

對本專案的潛在優勢：

- 已核准單鏡 Keyframe 可走 Image-to-video；
- 多張角色、服裝、愛醬、場景、風格參考可走 Reference-to-video；
- Edit／Extension 可處理小範圍修復與延續。

重要：同一 Provider 請求的模式限制、支援模型和價格要在啟動時重新查官方文件。

### 4.4 ByteDance Seedance 2.0／即夢

ByteDance 官方 Seedance 2.0 頁面目前描述：

- 統一多模態音視訊生成；
- 文字、圖片、音訊、影片輸入；
- 多模態參考；
- 影片編輯與較強導演控制；
- 官方頁面提供 API 取得入口。

官方來源：

- https://seed.bytedance.com/en/seedance2_0

對本專案的潛在優勢：中文創作、運動、鏡頭、音視訊共同生成與即夢／剪映生態。

正式比較前必查：

- 使用者所在地是否可取得 API；
- 即夢消費者介面與 Seedance API 的模型版本是否一致；
- 商用條款；
- 真人／角色參考與資料保存；
- 價格、解析度、時長、併發；
- 產出權利與水印。

### 4.5 Runway

Runway 作為成熟影片 API 與工作流候選，適合：

- Image-to-video；
- Video-to-video；
- Character Control；
- Multi-shot；
- 後續音訊／語音工作流。

正式啟動時才核對當時最新模型、價格與商用條款。

官方來源：

- https://docs.dev.runwayml.com/

## 5. 初步測試順序，不是最終排名

在已經有核准乾淨 Keyframe、角色參考與場景參考時：

1. Gemini Omni Flash；
2. Grok Imagine Image-to-video；
3. Veo 3.1；
4. Seedance 2.0／即夢；
5. Runway。

排序理由：先測最能同時處理多輸入一致性與修訂的模型，再測鎖首幀、首尾幀、中文電影感和成熟工作流。

如果使用者已經有即夢／Seedance 或 xAI 可用帳號，可以調整測試順序，但不能跳過同鏡頭公平比較。

## 6. Bake-off 測試單位

首次比較：

```text
一個乾淨單鏡 Keyframe
5–8 秒
16:9
低幅度動作
單一相機運動
不含對話框、字幕、Panel 邊框
```

候選鏡頭：

- 純場景：海浪、霧氣、潮池反光、慢推；
- 單角色：呼吸、目光、衣物微動；
- 愛醬：耳朵轉動、毛髮受風。

不得以多人戰鬥作第一次比較。

## 7. 公平比較

每個 Provider：

```text
最多 2 個 Candidate
```

同一份：

- Keyframe；
- 參考圖；
- Motion Prompt；
- Camera Contract；
- Forbidden Drift；
- Duration；
- Aspect Ratio；
- Review Rubric。

不得為某家 Provider 特別放寬角色一致性要求，除非明確記錄模式限制。

## 8. 評分

每項 0–10：

```text
identityConsistency
costumeConsistency
animalMarkingConsistency
sceneGeometryConsistency
firstFrameFidelity
lastFrameUsefulness
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

- 換臉；
- 服裝改變；
- 愛醬花紋改變；
- 主要場景幾何移動；
- 多出人物／肢體；
- 鏡頭意圖被重寫；
- 未授權文字、Logo 或他人受保護角色；
- Provider回傳無法穩定下載或辨識的輸出。

## 9. 預算批准

啟動前由使用者明確決定：

```text
providerList
budgetCap
candidateCount
duration
resolution
apiOrManualUI
commercialTermsAccepted
portraitUploadAllowed
providerDataRetentionAccepted
```

未有決定時，Codex只能做 Dry Run／Request Preview，不得呼叫 API。

## 10. API Key

- 只放環境變數或秘密管理；
- 不進 Git；
- 不進 Journal；
- 不出現在 Prompt／Result；
- 不回傳給 Subagent；
- 日誌只記 Provider、模型、參數、Request ID、成本與結果 Hash。

## 11. 啟動條件

只有全部成立才開始：

- REBOOT-01 可玩垂直切片完成；
- 人物美術風格由使用者選定；
- 至少一張乾淨 Keyframe 通過 Candidate Reference；
- Motion／Camera／Continuity Contract 完成；
- 使用者批准小額上限；
- 官方模型、限制、價格與商用條款已當日重查。

在此之前，影片路線保持：

```text
FUTURE_PROVIDER_BAKEOFF_NO_SPEND
```