package com.localnovel.engine.service

object OralLineMatcher {
    fun matchesSpokenLine(spoken: String, candidate: String): Boolean {
        val spokenNorm = normalize(spoken)
        val candidateNorm = normalize(candidate)
        if (spokenNorm.isBlank()) return true
        if (candidateNorm.isBlank()) return false
        if (candidateNorm.contains(spokenNorm) || spokenNorm.contains(candidateNorm)) return true
        val overlap = longestCommonSubstring(spokenNorm, candidateNorm)
        val threshold = (minOf(spokenNorm.length, candidateNorm.length) * 6) / 10
        return overlap.length >= maxOf(4, threshold)
    }

    fun extractQuotedDialogue(narrative: String): List<String> =
        Regex("「([^」]+)」")
            .findAll(narrative)
            .map { it.groupValues[1].trim() }
            .filter { it.isNotBlank() }
            .toList()

    private fun normalize(text: String): String =
        text.lowercase()
            .replace(Regex("\\s+"), "")
            .replace(Regex("[，。！？；：、,.!?;:「」\"'…—\\-]"), "")

    private fun longestCommonSubstring(left: String, right: String): String {
        if (left.isEmpty() || right.isEmpty()) return ""
        var best = ""
        for (start in left.indices) {
            for (end in start + 1..left.length) {
                val slice = left.substring(start, end)
                if (slice.length <= best.length || !right.contains(slice)) continue
                best = slice
            }
        }
        return best
    }
}