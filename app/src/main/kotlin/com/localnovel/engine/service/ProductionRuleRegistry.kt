package com.localnovel.engine.service

import com.localnovel.engine.model.CharacterVoiceProfile
import com.localnovel.engine.model.DialogueArtifact
import com.localnovel.engine.model.DialogueArtifactHashPayload
import com.localnovel.engine.model.DialogueFinding
import com.localnovel.engine.model.DialogueReviewerRole
import com.localnovel.engine.model.LanguageProductionProfile
import com.localnovel.engine.model.ProductionRuleCategory
import com.localnovel.engine.model.ProductionRuleCode
import com.localnovel.engine.model.ProductionRuleDefinition
import com.localnovel.engine.model.ProductionRuleSet
import com.localnovel.engine.model.ProductionRuleSeverity
import com.localnovel.engine.support.JsonSupport
import java.security.MessageDigest
import kotlinx.serialization.encodeToString

object ProductionRuleRegistry {
    const val RULE_SET_ID = "engine-production-rules"
    const val RULE_SET_VERSION = "1.0.0"
    const val LANGUAGE_PROFILE_ID = "language-zh-Hant-TW-natural-v1"

    private val hardBlockers = setOf(
        ProductionRuleCode.FORCED_WIT_UNDER_DANGER,
        ProductionRuleCode.ACCIDENTAL_FLIRTATION_OR_DOUBLE_MEANING,
        ProductionRuleCode.REQUIRES_PERFORMANCE_TO_AVOID_MISREADING,
    )

    val rules: List<ProductionRuleDefinition> = ProductionRuleCode.entries.map { code ->
        val category = when (code) {
            ProductionRuleCode.CHARACTER_VOICE_COLLAPSE -> ProductionRuleCategory.CHARACTER_VOICE
            ProductionRuleCode.UNSUPPORTED_REGIONAL_TERM,
            ProductionRuleCode.TRANSLATION_LIKE_PHRASING,
            ProductionRuleCode.UNNATURAL_SPOKEN_CHINESE -> ProductionRuleCategory.LANGUAGE_LOCALE
            else -> ProductionRuleCategory.DIALOGUE
        }
        ProductionRuleDefinition(
            code = code,
            category = category,
            defaultSeverity = if (code in hardBlockers) ProductionRuleSeverity.BLOCKER else ProductionRuleSeverity.WARNING,
            hardBlocker = code in hardBlockers,
            description = when (code) {
                ProductionRuleCode.FORCED_WIT_UNDER_DANGER -> "High-pressure dialogue sacrifices executable rescue information for a clever line."
                ProductionRuleCode.ACCIDENTAL_FLIRTATION_OR_DOUBLE_MEANING -> "A rescue or trauma line creates accidental flirtation or a consequential double meaning."
                ProductionRuleCode.REQUIRES_PERFORMANCE_TO_AVOID_MISREADING -> "The text requires acting or staging to avoid a major misreading."
                else -> code.name.lowercase().replace('_', ' ')
            },
        )
    }

    fun activeRuleSet(): ProductionRuleSet {
        val withoutHash = ProductionRuleSet(
            ruleSetId = RULE_SET_ID,
            version = RULE_SET_VERSION,
            rules = rules,
            requiredReviewerRoles = DialogueReviewerRole.entries,
            hardBlockerCodes = hardBlockers.sortedBy { it.name },
        )
        return withoutHash.copy(contentSha256 = sha256(JsonSupport.json.encodeToString(withoutHash)))
    }

    fun characterProfiles(): List<CharacterVoiceProfile> = listOf(
        profile(
            "voice-lu-chenxing-v1", "character-lu-chenxing",
            listOf("短句", "觀察型乾式幽默", "高壓時減少笑話", "真實傷害後停下", "不靠降智"),
            listOf("高壓救援中的調情雙關", "長篇解釋證明聰明"),
        ),
        profile(
            "voice-zhou-ke-v1", "character-zhou-ke",
            listOf("極短", "命令、路線、救援", "害怕時更少話"),
            listOf("配合主角所有笑點", "危急時完整論述"),
        ),
        profile(
            "voice-tide-picker-v1", "character-tide-picker",
            listOf("防衛、重複、碎裂", "受傷時只保留關鍵詞"),
            listOf("長篇解釋箱子", "在創傷中完整說理"),
        ),
        profile(
            "voice-aijiang-v1", "character-aijiang",
            listOf("無人類對白", "以動作、停步、低吼、方向與重心表達"),
            listOf("人類語句", "只等待口令"),
        ),
    )

    fun languageProfile(): LanguageProductionProfile {
        val withoutHash = LanguageProductionProfile(
            profileId = LANGUAGE_PROFILE_ID,
            version = "1.0.0",
            ruleSetId = RULE_SET_ID,
            ruleSetVersion = RULE_SET_VERSION,
            language = "zh-Hant-TW",
            spokenRegister = "Natural Taiwan Mandarin",
            localizationPolicy = listOf(
                "母稿維持自然台灣華語",
                "泛華語理解透過 metadata 與 localization，不混寫各地詞彙",
                "地區警告不得被誤升為 BLOCKER，除非造成重大語意誤讀",
            ),
        )
        return withoutHash.copy(profileSha256 = sha256(JsonSupport.json.encodeToString(withoutHash)))
    }

    fun artifactHash(artifact: DialogueArtifact): String = sha256(
        JsonSupport.json.encodeToString(
            DialogueArtifactHashPayload(
                artifactId = artifact.artifactId,
                productionId = artifact.productionId,
                artifactKind = artifact.artifactKind,
                parentArtifactSha256 = artifact.parentArtifactSha256,
                ruleSetId = artifact.ruleSetId,
                ruleSetVersion = artifact.ruleSetVersion,
                ruleSetContentSha256 = artifact.ruleSetContentSha256,
                characterVoiceProfileIds = artifact.characterVoiceProfileIds.sorted(),
                characterVoiceProfileHashes = artifact.characterVoiceProfileHashes.toSortedMap(),
                languageProfileId = artifact.languageProfileId,
                languageProfileSha256 = artifact.languageProfileSha256,
                lines = artifact.lines,
                autoRevisionCount = artifact.autoRevisionCount,
                boundary = artifact.boundary,
                sourceCommitSha = artifact.sourceCommitSha,
            ),
        ),
    )

    fun canonicalFixtureFindings(artifact: DialogueArtifact): List<DialogueFinding> {
        val lines = artifact.lines
        return lines.flatMapIndexed { index, line ->
            val prior = lines.getOrNull(index - 1)
            if (line.text == "好。先碰你。" && prior?.text == "別碰箱子。") {
                listOf(
                    blocker(line.lineId, line.text, ProductionRuleCode.ACCIDENTAL_FLIRTATION_OR_DOUBLE_MEANING, "救援回應形成可被直接讀成調情的雙關。", "先救你。"),
                    blocker(line.lineId, line.text, ProductionRuleCode.REQUIRES_PERFORMANCE_TO_AVOID_MISREADING, "文字本身必須依賴動作或表演才能避免重大誤讀。", "先救你。"),
                    blocker(line.lineId, line.text, ProductionRuleCode.FORCED_WIT_UNDER_DANGER, "受傷救援現場以機智句取代最直接的可執行救援資訊。", "先救你。"),
                )
            } else {
                buildList {
                    if (line.pressureLevel == "HIGH" && line.text.length > 32) {
                        add(
                            DialogueFinding(
                                findingId = "det-${line.lineId}-overlong",
                                lineId = line.lineId,
                                exactQuote = line.text,
                                ruleCode = ProductionRuleCode.HIGH_PRESSURE_OVERLONG_LINE,
                                severity = ProductionRuleSeverity.WARNING,
                                explanation = "高壓場景句長超過確定性檢查門檻，需語意 reviewer 判斷。",
                            ),
                        )
                    }
                }
            }
        }
    }

    fun isHardBlocker(code: ProductionRuleCode): Boolean = code in hardBlockers

    fun sha256(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }

    private fun profile(
        profileId: String,
        characterId: String,
        voiceRules: List<String>,
        forbiddenPatterns: List<String>,
    ): CharacterVoiceProfile {
        val withoutHash = CharacterVoiceProfile(
            profileId = profileId,
            version = "1.0.0",
            characterId = characterId,
            ruleSetId = RULE_SET_ID,
            ruleSetVersion = RULE_SET_VERSION,
            voiceRules = voiceRules,
            forbiddenPatterns = forbiddenPatterns,
        )
        return withoutHash.copy(profileSha256 = sha256(JsonSupport.json.encodeToString(withoutHash)))
    }

    private fun blocker(
        lineId: String,
        quote: String,
        code: ProductionRuleCode,
        explanation: String,
        suggestion: String,
    ) = DialogueFinding(
        findingId = "fixture-$lineId-${code.name.lowercase()}",
        lineId = lineId,
        exactQuote = quote,
        ruleCode = code,
        severity = ProductionRuleSeverity.BLOCKER,
        explanation = explanation,
        suggestedText = suggestion,
    )
}
