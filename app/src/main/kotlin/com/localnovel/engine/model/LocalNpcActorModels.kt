package com.localnovel.engine.model

import kotlinx.serialization.Serializable

data class LocalNpcActorRuntimeConfig(
    val llamaCliPath: java.nio.file.Path,
    val modelPath: java.nio.file.Path,
    val responseSchemaPath: java.nio.file.Path,
    val modelId: String = "Qwen/Qwen3-8B-GGUF",
    val modelRevision: String = "7c41481f57cb95916b40956ab2f0b139b296d974",
    val modelSha256: String = "068bae163faa96ad48032daf4e071a6a28fe67d8dcc95367609c2ff165e52738",
    val runtimeId: String = "llama.cpp-b10002-vulkan",
    val gpuLayers: String = "all",
    val contextTokens: Int = 4096,
    val maxOutputTokens: Int = 512,
    val timeoutSeconds: Long = 180,
    val maxOutputBytes: Long = 262_144,
    val seed: Int = 20_260_714,
)

data class LocalNpcActorProcessResult(
    val exitCode: Int,
    val timedOut: Boolean,
    val elapsedMillis: Long,
)

@Serializable
data class LocalNpcActorInvocationAudit(
    val schemaVersion: String = "1.0",
    val provider: String = "LOCAL_LLAMA_CPP_DIRECT_CLI",
    val modelId: String,
    val modelRevision: String,
    val modelSha256: String,
    val modelFileName: String,
    val runtimeId: String,
    val reasoning: String = "off",
    val contextTokens: Int,
    val maxOutputTokens: Int,
    val seed: Int,
    val elapsedMillis: Long,
    val rawOutputBytes: Long,
    val promptSha256: String,
    val responseSha256: String,
    val serverStarted: Boolean = false,
    val portOpened: Boolean = false,
    val formalStateWritePerformed: Boolean = false,
)

data class LocalNpcActorGenerationResult(
    val response: NpcActorPhysicalResponse,
    val audit: LocalNpcActorInvocationAudit,
    val promptText: String,
    val rawOutput: String,
    val responseJson: String,
)

@Serializable
data class LocalNpcActorSafetyLoopRequest(
    val schemaVersion: String = "1.0",
    val loopId: String,
    val boundary: String = "NON_CANON_EVALUATION",
    val prompt: NpcActorPhysicalPrompt,
    val engineContext: NpcActorPhysicalEngineContext,
    val maxAttempts: Int = 2,
    val maxElapsedMinutes: Int = 10,
)

@Serializable
data class LocalNpcActorSafetyLoopReport(
    val schemaVersion: String = "1.0",
    val loopId: String,
    val boundary: String = "NON_CANON_EVALUATION",
    val generatedAttemptCount: Int,
    val generatedResponses: List<NpcActorPhysicalResponse>,
    val invocationAudits: List<LocalNpcActorInvocationAudit>,
    val safetyResult: NpcActorSafetyLoopResult,
    val repairAttempted: Boolean,
    val formalStateWritePerformed: Boolean = false,
    val claimBoundary: List<String> = listOf(
        "Local model output is an untrusted candidate.",
        "Only the existing NPC Actor safety chain may accept a formal-resolve candidate.",
        "This command does not write SQLite, Canon, Memory, or Knowledge.",
    ),
)
