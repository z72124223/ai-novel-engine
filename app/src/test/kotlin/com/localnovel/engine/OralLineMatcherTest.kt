package com.localnovel.engine

import com.localnovel.engine.service.OralLineMatcher
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class OralLineMatcherTest {
    @Test
    fun matchesCandidateDialogueWithMinorEdits() {
        assertTrue(
            OralLineMatcher.matchesSpokenLine(
                spoken = "還來得及，潮水還沒漲上來。",
                candidate = "還來得及，潮水還沒漲上來。",
            ),
        )
    }

    @Test
    fun rejectsJargonDialogueUnlikeCandidate() {
        assertFalse(
            OralLineMatcher.matchesSpokenLine(
                spoken = "還來得及，退潮窗口還在。",
                candidate = "還來得及，潮水還沒漲上來。",
            ),
        )
    }

    @Test
    fun extractsQuotedDialogue() {
        val quotes = OralLineMatcher.extractQuotedDialogue("他說：「還來得及。」愛醬沒動。")
        assertTrue(quotes.contains("還來得及。"))
    }
}