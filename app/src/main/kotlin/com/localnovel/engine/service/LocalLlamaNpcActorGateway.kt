package com.localnovel.engine.service

import com.localnovel.engine.model.LocalNpcActorGenerationResult
import com.localnovel.engine.model.LocalNpcActorInvocationAudit
import com.localnovel.engine.model.LocalNpcActorProcessResult
import com.localnovel.engine.model.LocalNpcActorRuntimeConfig
import com.localnovel.engine.model.NpcActorPhysicalPrompt
import com.localnovel.engine.model.NpcActorPhysicalResponse
import com.localnovel.engine.model.NpcActorSafetyRepairRequest
import java.nio.file.Files
import java.nio.file.Path
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

fun interface LocalNpcActorProcessExecutor {
    fun execute(
        command: List<String>,
        outputPath: Path,
        logPath: Path,
        timeoutSeconds: Long,
    ): LocalNpcActorProcessResult
}

class SystemLocalNpcActorProcessExecutor : LocalNpcActorProcessExecutor {
    override fun execute(
        command: List<String>,
        outputPath: Path,
        logPath: Path,
        timeoutSeconds: Long,
    ): LocalNpcActorProcessResult {
        val started = System.nanoTime()
        val process = ProcessBuilder(command)
            .redirectOutput(logPath.toFile())
            .redirectErrorStream(true)
            .start()
        val completed = process.waitFor(timeoutSeconds, TimeUnit.SECONDS)
        if (!completed) {
            process.destroy()
            if (!process.waitFor(2, TimeUnit.SECONDS)) {
                process.destroyForcibly()
                process.waitFor(2, TimeUnit.SECONDS)
            }
        }
        return LocalNpcActorProcessResult(
            exitCode = if (completed) process.exitValue() else -1,
            timedOut = !completed,
            elapsedMillis = (System.nanoTime() - started) / 1_000_000,
        )
    }
}

class LocalNpcActorPromptCompiler {
    fun compile(
        prompt: NpcActorPhysicalPrompt,
        repairRequest: NpcActorSafetyRepairRequest? = null,
    ): String {
        require(prompt.schemaVersion == "1.0") { "Unsupported NPC Actor physical prompt version." }
        require(prompt.boundary == "NON_CANON_EVALUATION") { "Local NPC Actor prompt must remain non-Canon." }
        repairRequest?.let {
            require(it.requestId == prompt.actorRequest.requestId) { "Repair request id does not match the actor prompt." }
            require(!it.engineOwnedContextIncluded) { "Repair request must not include engine-owned context." }
        }
        return buildString {
            appendLine("你正在演一名虛構世界中的 NPC。只根據下方角色可見資料做出一個短小、自然、可中斷的反應。")
            appendLine("不要解釋世界觀，不要替其他角色思考，不要假裝知道未提供的資料，不要輸出思考過程。")
            appendLine("你只是在提出候選；不可宣稱動作已成功，不可修改資料庫、Canon、Memory 或 Knowledge。")
            appendLine("utterance 使用繁體中文與自然華語；危急時優先短句、停頓、動作或沉默。")
            appendLine("所有 evidence id、request id、actor id、target id 與 action enum 必須逐字使用允許值。")
            appendLine("physicalActionProposal 只能使用 allowedPhysicalActionTypes 與 visiblePhysicalTargetIds。")
            appendLine("destinationCenterCm、supportSurfaceId、supportUse 等沒有角色可見依據時使用 null，不得猜測測量值。")
            appendLine("沒有內容的可選陣列與有預設值的可選欄位請省略，所有文字保持簡短。")
            appendLine("所有必填文字不可只放空白；speechAct 是 SILENCE 時 utterance 必須是空字串，否則 utterance 必須是自然繁體中文短句。")
            appendLine("只輸出符合指定 JSON Schema 的單一 JSON object；不得使用 Markdown。")
            appendLine()
            appendLine("ACTOR_VISIBLE_PROMPT_JSON:")
            appendLine(NpcActorJson.strict.encodeToString(prompt))
            repairRequest?.let {
                appendLine()
                appendLine("SANITIZED_REPAIR_REQUEST_JSON:")
                appendLine(NpcActorJson.strict.encodeToString(it))
                appendLine("請只修正列出的拒絕代碼，不得推測未提供的引擎資料。")
            }
        }
    }
}

interface LocalNpcActorGenerator {
    fun generate(
        prompt: NpcActorPhysicalPrompt,
        repairRequest: NpcActorSafetyRepairRequest? = null,
    ): LocalNpcActorGenerationResult
}

class LlamaCppNpcActorPhysicalGateway(
    private val config: LocalNpcActorRuntimeConfig,
    private val processExecutor: LocalNpcActorProcessExecutor = SystemLocalNpcActorProcessExecutor(),
    private val promptCompiler: LocalNpcActorPromptCompiler = LocalNpcActorPromptCompiler(),
) : NpcActorPhysicalGateway, LocalNpcActorGenerator {
    private val validatedConfig: Boolean by lazy {
        validateConfig()
        true
    }

    override fun propose(prompt: NpcActorPhysicalPrompt): NpcActorPhysicalResponse =
        generate(prompt).response

    override fun generate(
        prompt: NpcActorPhysicalPrompt,
        repairRequest: NpcActorSafetyRepairRequest?,
    ): LocalNpcActorGenerationResult {
        check(validatedConfig)
        val promptText = promptCompiler.compile(prompt, repairRequest)
        val workDir = Files.createTempDirectory("npc-actor06-local-")
        try {
            val promptPath = workDir.resolve("prompt.txt")
            val outputPath = workDir.resolve("response.txt")
            val logPath = workDir.resolve("llama-cli.log")
            val schemaPath = workDir.resolve("response.schema.json")
            Files.writeString(promptPath, promptText)
            Files.copy(config.responseSchemaPath, schemaPath)
            val processResult = processExecutor.execute(
                command = command(promptPath, outputPath, schemaPath),
                outputPath = outputPath,
                logPath = logPath,
                timeoutSeconds = config.timeoutSeconds,
            )
            require(!processResult.timedOut) { "Local NPC Actor generation timed out." }
            require(processResult.exitCode == 0) {
                val logTail = if (Files.isRegularFile(logPath)) {
                    Files.readString(logPath).takeLast(4_096)
                } else {
                    "No llama.cpp log file was produced."
                }
                "Local NPC Actor process failed with exit code ${processResult.exitCode}: $logTail"
            }
            require(Files.isRegularFile(outputPath)) { "Local NPC Actor output file was not created." }
            val rawBytes = Files.size(outputPath)
            require(rawBytes in 1..config.maxOutputBytes) {
                "Local NPC Actor output size is outside the allowed range: $rawBytes bytes."
            }
            val rawOutput = Files.readString(outputPath).removePrefix("\uFEFF")
            val responseJson = runCatching { extractSingleJsonObject(rawOutput) }.getOrElse { error ->
                val logTail = if (Files.isRegularFile(logPath)) Files.readString(logPath).takeLast(4_096) else ""
                throw IllegalArgumentException("${error.message}; llama.cpp log: $logTail", error)
            }
            val response = NpcActorJson.strict.decodeFromString<NpcActorPhysicalResponse>(responseJson)
            require(response.schemaVersion == "1.0") { "Unsupported local NPC Actor response version." }
            require(response.requestId == prompt.actorRequest.requestId) { "Local NPC Actor request id mismatch." }
            require(response.actorResponse.requestId == prompt.actorRequest.requestId) {
                "Local NPC Actor nested request id mismatch."
            }
            require(response.actorResponse.actorId == prompt.actorRequest.actorId) { "Local NPC Actor id mismatch." }
            val audit = LocalNpcActorInvocationAudit(
                modelId = config.modelId,
                modelRevision = config.modelRevision,
                modelSha256 = config.modelSha256,
                modelFileName = config.modelPath.fileName.toString(),
                runtimeId = config.runtimeId,
                contextTokens = config.contextTokens,
                maxOutputTokens = config.maxOutputTokens,
                seed = config.seed,
                elapsedMillis = processResult.elapsedMillis,
                rawOutputBytes = rawBytes,
                promptSha256 = sha256(promptText.toByteArray()),
                responseSha256 = sha256(responseJson.toByteArray()),
            )
            return LocalNpcActorGenerationResult(
                response = response,
                audit = audit,
                promptText = promptText,
                rawOutput = rawOutput,
                responseJson = responseJson,
            )
        } finally {
            workDir.toFile().deleteRecursively()
        }
    }

    private fun validateConfig() {
        require(config.llamaCliPath.isAbsolute && Files.isRegularFile(config.llamaCliPath)) {
            "LOCAL_NPC_ACTOR_LLAMA_CLI must point to a local executable file."
        }
        require(config.modelPath.isAbsolute && Files.isRegularFile(config.modelPath)) {
            "LOCAL_NPC_ACTOR_MODEL must point to a local GGUF file."
        }
        require(config.modelPath.fileName.toString().endsWith(".gguf", ignoreCase = true)) {
            "Local NPC Actor model must be a GGUF file."
        }
        require(config.responseSchemaPath.isAbsolute && Files.isRegularFile(config.responseSchemaPath)) {
            "Local NPC Actor response Schema file does not exist."
        }
        require(config.contextTokens in 1024..8192) { "Local NPC Actor context must remain within the Pilot limit." }
        require(config.maxOutputTokens in 128..2048) { "Local NPC Actor output token limit is invalid." }
        require(config.timeoutSeconds in 1..600) { "Local NPC Actor timeout is invalid." }
        require(config.maxOutputBytes in 1024..1_048_576) { "Local NPC Actor output byte limit is invalid." }
        val actualModelSha = sha256(config.modelPath)
        require(actualModelSha == config.modelSha256) { "Local NPC Actor model SHA-256 mismatch." }
    }

    private fun command(promptPath: Path, outputPath: Path, schemaPath: Path): List<String> = listOf(
        config.llamaCliPath.toString(),
        "-m", config.modelPath.toString(),
        "--gpu-layers", config.gpuLayers,
        "--ctx-size", config.contextTokens.toString(),
        "--predict", config.maxOutputTokens.toString(),
        "--threads", "6",
        "--threads-batch", "6",
        "--temp", "0.45",
        "--top-k", "20",
        "--top-p", "0.9",
        "--min-p", "0.0",
        "--seed", config.seed.toString(),
        "--reasoning", "off",
        "--no-conversation",
        "--no-jinja",
        "--single-turn",
        "--simple-io",
        "--no-display-prompt",
        "--no-show-timings",
        "--log-disable",
        "--json-schema-file", schemaPath.toString(),
        "--file", promptPath.toString(),
        "--output-file", outputPath.toString(),
    )

    internal fun extractSingleJsonObject(raw: String): String {
        val source = raw.substringAfterLast("Assistant:", raw).trim()
        val start = source.indexOf('{')
        require(start >= 0) {
            "Local NPC Actor output does not contain a JSON object: ${source.take(4_096)}"
        }
        require(source.substring(0, start).isBlank()) { "Local NPC Actor output contains text before JSON." }
        var depth = 0
        var inString = false
        var escaped = false
        var end = -1
        for (index in start until source.length) {
            val char = source[index]
            if (inString) {
                when {
                    escaped -> escaped = false
                    char == '\\' -> escaped = true
                    char == '"' -> inString = false
                }
            } else {
                when (char) {
                    '"' -> inString = true
                    '{' -> depth += 1
                    '}' -> {
                        depth -= 1
                        require(depth >= 0) { "Local NPC Actor output contains malformed JSON braces." }
                        if (depth == 0) {
                            end = index
                            break
                        }
                    }
                }
            }
        }
        require(end >= 0 && depth == 0 && !inString) { "Local NPC Actor JSON object is incomplete." }
        require(source.substring(end + 1).isBlank()) { "Local NPC Actor output contains text after JSON." }
        return source.substring(start, end + 1)
    }

    private fun sha256(bytes: ByteArray): String = MessageDigest.getInstance("SHA-256")
        .digest(bytes)
        .joinToString("") { "%02x".format(it) }

    private fun sha256(path: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")
        Files.newInputStream(path).buffered().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                if (read > 0) digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
