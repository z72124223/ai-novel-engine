package com.localnovel.engine

import com.localnovel.engine.model.LocalStoryInputPack
import com.localnovel.engine.support.JsonSupport
import java.nio.file.Files
import java.nio.file.Path
import kotlinx.serialization.decodeFromString

object ChenxingStoryTestFixtures {
    fun resolveRepoPath(relative: String): Path =
        generateSequence(Path.of("").toAbsolutePath().normalize()) { it.parent }
            .map { it.resolve(relative) }
            .first { Files.exists(it) }

    fun chenxingStoryInputPack(): LocalStoryInputPack =
        JsonSupport.json.decodeFromString(
            Files.readString(chenxingStoryInputPackPath()).removePrefix("\uFEFF"),
        )

    fun chenxingStoryInputPackPath(): Path =
        resolveRepoPath("samples/chenxing/chenxing_story_input_pack_sample.json")

    fun chenxingFoundationCardPackPath(): Path =
        resolveRepoPath("samples/local_story_input/chenxing_09_foundation_card_pack_contract_sample.json")

    fun chenxingStoryValidationPath(): Path =
        resolveRepoPath("samples/chenxing/chenxing_story_input_pack_validation_sample.json")

    fun chenxingFoundationValidationPath(): Path =
        resolveRepoPath("samples/chenxing/chenxing_09_foundation_card_pack_validation_report_sample.json")

    fun chenxingPipelineSample(fileName: String): Path =
        resolveRepoPath("samples/chenxing/pipeline/$fileName")

    fun chenxingSettingSourceSample(relative: String): Path =
        resolveRepoPath("samples/chenxing/comic_video_setting_source/$relative")
}