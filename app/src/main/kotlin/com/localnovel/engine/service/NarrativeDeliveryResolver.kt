package com.localnovel.engine.service

import com.localnovel.engine.model.ContextPacket
import com.localnovel.engine.model.DeliveryMode
import com.localnovel.engine.model.NarrativeDeliveryExpectation
import com.localnovel.engine.model.OralLineCandidate
import com.localnovel.engine.model.OutputRequirements

internal data class OpeningBeatDefinition(
    val shotId: String,
    val title: String,
    val keywords: List<String>,
    val oralLines: List<OralLineCandidate> = emptyList(),
)

object NarrativeDeliveryResolver {
    private val chenxingOpeningBeatSchedule = listOf(
        "shot-01-tide-check",
        "shot-04-water-rises",
        "shot-09-hillside-retreat",
    )

    private val chenxingOpeningBeats = mapOf(
        "shot-01-tide-check" to OpeningBeatDefinition(
            shotId = "shot-01-tide-check",
            title = "查潮汐",
            keywords = listOf("潮汐", "退潮", "手機", "巴威", "潮間帶", "海蝕", "還來得及", "潮水"),
            oralLines = listOf(
                oralNarration("shot-01-tide-check", "narr-01", "2026年7月，颱風巴威還在海上。陸塵行照著潮汐表，估計還有兩小時。"),
                oralDialogue("shot-01-tide-check", "dlg-01", "陸塵行", "還來得及，潮水還沒漲上來。", "低聲自言"),
            ),
        ),
        "shot-02-strange-find" to OpeningBeatDefinition(
            shotId = "shot-02-strange-find",
            title = "異常痕跡",
            keywords = listOf("接縫", "痕跡", "岩層", "人工", "不自然"),
            oralLines = listOf(
                oralNarration("shot-02-strange-find", "narr-02", "海蝕平台上，有不該出現的痕跡。"),
                oralDialogue("shot-02-strange-find", "dlg-02", "陸塵行", "這不是天然岩層……誰在這裡接過東西？", "疑惑、壓低聲音"),
            ),
        ),
        "shot-03-overfocus" to OpeningBeatDefinition(
            shotId = "shot-03-overfocus",
            title = "看得太久",
            keywords = listOf("看得太久", "愛醬", "警戒", "蹲", "比對"),
            oralLines = listOf(
                oralNarration("shot-03-overfocus", "narr-03", "他看得太久。好奇心壓過了時間，也壓過了狗的低聲警告。"),
            ),
        ),
        "shot-04-water-rises" to OpeningBeatDefinition(
            shotId = "shot-04-water-rises",
            title = "海水上漲",
            keywords = listOf("海水", "上漲", "漲潮", "浪花", "不對", "平常漲潮"),
            oralLines = listOf(
                oralNarration("shot-04-water-rises", "narr-04", "海水比表上漲得快。"),
                oralDialogue("shot-04-water-rises", "dlg-04", "陸塵行", "不對——這不是平常漲潮。", "警覺"),
            ),
        ),
        "shot-05-return-blocked" to OpeningBeatDefinition(
            shotId = "shot-05-return-blocked",
            title = "回頭路被封",
            keywords = listOf("回不去", "岩徑", "白浪", "封", "來時"),
            oralLines = listOf(
                oralNarration("shot-05-return-blocked", "narr-05", "來時還乾的岩徑，現在只剩白浪拍擊。"),
                oralDialogue("shot-05-return-blocked", "dlg-05", "陸塵行", "回不去了。", "沉聲"),
            ),
        ),
        "shot-06-wind-worsens" to OpeningBeatDefinition(
            shotId = "shot-06-wind-worsens",
            title = "風浪加劇",
            keywords = listOf("風浪", "環流", "巴威", "雨", "浪勢"),
            oralLines = listOf(
                oralNarration("shot-06-wind-worsens", "narr-06", "巴威的外圍環流壓上來，浪勢一次比一次重。鹹味灌進鼻腔，皮膚開始黏。"),
            ),
        ),
        "shot-07-sky-darkens" to OpeningBeatDefinition(
            shotId = "shot-07-sky-darkens",
            title = "天色驟暗",
            keywords = listOf("天色", "暗", "能見度", "灰"),
            oralLines = listOf(
                oralNarration("shot-07-sky-darkens", "narr-07", "白天像被擰短了一截。"),
            ),
        ),
        "shot-08-no-signal" to OpeningBeatDefinition(
            shotId = "shot-08-no-signal",
            title = "沒有訊號",
            keywords = listOf("訊號", "無服務", "手機", "求救"),
            oralLines = listOf(
                oralNarration("shot-08-no-signal", "narr-08", "求救鍵按了兩次，螢幕只回無服務。"),
                oralDialogue("shot-08-no-signal", "dlg-08", "陸塵行", "……沒訊號。", "短促"),
            ),
        ),
        "shot-09-hillside-retreat" to OpeningBeatDefinition(
            shotId = "shot-09-hillside-retreat",
            title = "躲入山坡廢墟",
            keywords = listOf("山坡", "廢墟", "撤退", "混凝土", "避難", "愛醬"),
            oralLines = listOf(
                oralNarration("shot-09-hillside-retreat", "narr-09", "唯一能躲的地方，在山坡上——日治時期留下的廢棄構造物。"),
                oralDialogue("shot-09-hillside-retreat", "dlg-09", "陸塵行", "上面那棟……至少能擋風。", "喘而穩，自言"),
            ),
        ),
    )

    fun resolve(context: ContextPacket, readerMode: Boolean): ContextPacket {
        val deliveryMode = when {
            readerMode -> DeliveryMode.PLAIN_ORAL
            context.outputRequirements.deliveryMode != DeliveryMode.DEFAULT -> context.outputRequirements.deliveryMode
            else -> DeliveryMode.DEFAULT
        }
        if (deliveryMode == DeliveryMode.DEFAULT) {
            return context.copy(
                outputRequirements = context.outputRequirements.copy(deliveryMode = DeliveryMode.DEFAULT),
                narrativeDelivery = NarrativeDeliveryExpectation(),
                oralLineCandidates = emptyList(),
            )
        }

        val beat = resolveActiveBeat(context)
        val requiredExposition = if (context.turnNumber <= 1 && beat != null) {
            listOf("WHO", "WHERE", "WHY", "EVENT")
        } else {
            emptyList()
        }
        val advisoryNote = when (deliveryMode) {
            DeliveryMode.PLAIN_ORAL ->
                "白話口述：旁白與台詞優先使用 oralLineCandidates；引號內對白不得自創術語句。每段對白前須標明說話者，如「陸塵行低聲道：「……」」；旁白與對白分句，勿把台詞黏在旁白後面不換氣。家人／默契同伴（如愛醬）勿寫成被呼喚的寵物，禁止「跟我走」「過來」式口令；改為旁白寫默契跟隨，或主角自言。"
            DeliveryMode.SHOOTABLE ->
                "可拍正文：台詞須來自 oralLineCandidates；每句對應可見動作或鏡頭物件。"
            DeliveryMode.DEFAULT -> ""
        }

        return context.copy(
            outputRequirements = context.outputRequirements.copy(deliveryMode = deliveryMode),
            narrativeDelivery = NarrativeDeliveryExpectation(
                deliveryMode = deliveryMode,
                activeBeatId = beat?.shotId.orEmpty(),
                activeBeatTitle = beat?.title.orEmpty(),
                beatKeywords = beat?.keywords.orEmpty(),
                requiredExposition = requiredExposition,
                advisoryNote = advisoryNote,
            ),
            oralLineCandidates = beat?.oralLines.orEmpty(),
        )
    }

    fun resolveOutputRequirements(
        base: OutputRequirements,
        readerMode: Boolean,
    ): OutputRequirements =
        if (readerMode) {
            base.copy(deliveryMode = DeliveryMode.PLAIN_ORAL)
        } else {
            base
        }

    private fun resolveActiveBeat(context: ContextPacket): OpeningBeatDefinition? {
        val sceneId = context.activeScene?.id ?: return null
        val projectId = context.project.id
        if (projectId != "chenxing") return null

        val shotId = when (sceneId) {
            "scene-daping-coast-opening" -> {
                val scheduleIndex = (context.turnNumber - 1).coerceAtLeast(0)
                chenxingOpeningBeatSchedule.getOrElse(scheduleIndex) {
                    chenxingOpeningBeatSchedule.last()
                }
            }
            "scene-hillside-ruin-retreat" -> "shot-09-hillside-retreat"
            else -> null
        } ?: return null

        return chenxingOpeningBeats[shotId]
    }

    private fun oralNarration(beatId: String, lineId: String, line: String): OralLineCandidate =
        OralLineCandidate(
            lineId = lineId,
            speaker = "旁白",
            line = line,
            lineType = "narration",
            sourceBeatId = beatId,
        )

    private fun oralDialogue(
        beatId: String,
        lineId: String,
        speaker: String,
        line: String,
        delivery: String,
    ): OralLineCandidate =
        OralLineCandidate(
            lineId = lineId,
            speaker = speaker,
            line = line,
            lineType = "dialogue",
            delivery = delivery,
            sourceBeatId = beatId,
        )
}