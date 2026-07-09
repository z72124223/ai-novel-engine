package com.localnovel.engine

import com.localnovel.engine.model.OralLineCandidate
import com.localnovel.engine.service.NarrativeOralFormatter
import com.localnovel.engine.service.OralAudioSegmentKind
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NarrativeOralFormatterTest {
    @Test
    fun splitsNarrationIntoSentencesAndFormatsAttributedDialogue() {
        val narrative =
            "2026年7月，颱風巴威還在海上。陸塵行站在潮間帶。他低聲說：「還來得及，潮水還沒漲上來。」愛醬豎耳望向海面。"
        val segments = NarrativeOralFormatter.splitIntoOralSegments(
            narrative = narrative,
            oralLineCandidates = listOf(
                OralLineCandidate(
                    lineId = "dlg-01",
                    speaker = "陸塵行",
                    line = "還來得及，潮水還沒漲上來。",
                    lineType = "dialogue",
                    delivery = "低聲",
                ),
            ),
            characterNames = listOf("陸塵行"),
        )

        assertEquals(4, segments.size)
        assertEquals(OralAudioSegmentKind.NARRATION, segments[0].kind)
        assertEquals("2026年7月，颱風巴威還在海上。", segments[0].text)
        assertEquals(OralAudioSegmentKind.DIALOGUE, segments[2].kind)
        assertEquals("陸塵行低聲說：「還來得及，潮水還沒漲上來。」", segments[2].text)
        assertEquals("愛醬豎耳望向海面。", segments[3].text)
    }

    @Test
    fun findsDialogueMissingSpeakerAttribution() {
        val narrative = "海水漲得很快。「不對——這不是平常漲潮。」愛醬貼近他小腿。"
        val unattributed = NarrativeOralFormatter.findDialogueWithoutAttribution(
            narrative = narrative,
            characterNames = listOf("陸塵行"),
        )

        assertEquals(listOf("不對——這不是平常漲潮。"), unattributed)
    }

    @Test
    fun formatsLongLeadInDialogueWithNamedSpeaker() {
        val narrative =
            "來時的岩徑已被海水吞沒。愛醬已貼在他身側。陸塵行收紧項圈，喘而穩地說：「上面那棟……至少能擋風。」"
        val segments = NarrativeOralFormatter.splitIntoOralSegments(
            narrative = narrative,
            characterNames = listOf("陸塵行"),
        )

        assertEquals(OralAudioSegmentKind.DIALOGUE, segments.last { it.kind == OralAudioSegmentKind.DIALOGUE }.kind)
        assertEquals(
            "陸塵行喘而穩地說：「上面那棟……至少能擋風。」",
            segments.last { it.kind == OralAudioSegmentKind.DIALOGUE }.text,
        )
    }

    @Test
    fun acceptsNamedSpeakerAttributionBeforeQuote() {
        val narrative = "陸塵行皺眉道：「不對——這不是平常漲潮。」"
        val unattributed = NarrativeOralFormatter.findDialogueWithoutAttribution(
            narrative = narrative,
            characterNames = listOf("陸塵行"),
        )

        assertTrue(unattributed.isEmpty())
    }
}