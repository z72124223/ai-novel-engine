# NPC-ACTOR-06 本機模型安裝與供應鏈證據

日期：2026-07-14

## 結論

本機已安裝並驗證官方 Qwen3-8B Q5_K_M 與 llama.cpp Vulkan CLI。模型能在 GTX 1080 Ti
上載入並產生繁體中文；沒有啟動 server（伺服器）、沒有開 port（連接埠），也沒有把模型
權重或執行器放入 Repository。

## 模型

```text
repository: Qwen/Qwen3-8B-GGUF
revision: 7c41481f57cb95916b40956ab2f0b139b296d974
file: Qwen3-8B-Q5_K_M.gguf
bytes: 5851112224
sha256: 068bae163faa96ad48032daf4e071a6a28fe67d8dcc95367609c2ff165e52738
format: GGUF
quantization: Q5_K_M
license: Apache-2.0
```

官方來源：`https://huggingface.co/Qwen/Qwen3-8B-GGUF`

## 執行器

```text
repository: ggml-org/llama.cpp
release: b10002
build: a7312ae94
archive: llama-b10002-bin-win-vulkan-x64.zip
archiveBytes: 32950223
archiveSha256: c2b66ab6912e9fad75c7c6d2000f660bb40bc1f063aa62ec671d471e27dd92ea
device: Vulkan0 / NVIDIA GeForce GTX 1080 Ti
```

官方來源：`https://github.com/ggml-org/llama.cpp/releases/tag/b10002`

## 本機路徑政策

實際安裝位置在 Repository 外，不作 Git 交接。程式只認以下環境變數：

```text
LOCAL_NPC_ACTOR_LLAMA_CLI=<absolute path to llama-cli.exe>
LOCAL_NPC_ACTOR_MODEL=<absolute path to Qwen3-8B-Q5_K_M.gguf>
```

Git 證據不得保存使用者名稱或絕對路徑。新電腦依本報告的來源、檔名、bytes 與 SHA-256
重建，再於本機設定環境變數。

## 繁體中文冒煙測試

固定輸入要求模型在危急場景只用自然台灣華語提醒同伴退後，不得解釋大道理。

結果：

```text
快退後！危險！
```

包含模型載入的總耗時為 12.95 秒。這只證明本機 GPU、模型、文字輸出與繁體中文基本能力
可用，不代表完整 NPC JSON、長劇本、人類感或物理理解已通過；後續必須經真實契約 Pilot。

## 已確認的執行特性

- Vulkan 成功辨識 GTX 1080 Ti 11264MiB。
- Qwen 模型 metadata 顯示 `Q5_K - Medium`、text modality（文字模態）。
- 直接 CLI 可用；不需要 Ollama、LM Studio 或網路 API。
- CLI output file 仍可能帶有 `User:`／`Assistant:` transcript（對話包裝）；正式 gateway 不得
  直接信任整個檔案，必須擷取唯一 JSON object 後再 strict parse。
- reasoning 已關閉；正式輸出還會加上 JSON Schema、逾時與 byte 上限。

## 未完成

- 尚未證明完整 `NpcActorPhysicalResponse` 一次生成成功。
- 尚未量測固定 8K context 的生成速度與穩定性。
- 尚未完成 Kotlin gateway、兩次安全修訂與完整回歸測試。
