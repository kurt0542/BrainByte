package com.example.brainbyte.ocr

import com.google.mlkit.vision.text.Text


object SmartTextExtractor {


    data class FlashcardSuggestion(
        val term: String,
        val definition: String,
        val confidence: Float
    )

    fun extractFlashcardSuggestions(text: Text): List<FlashcardSuggestion> {
        val suggestions = mutableListOf<FlashcardSuggestion>()

        for (block in text.textBlocks) {
            for (line in block.lines) {
                extractFromColonPattern(line.text)?.let { suggestions.add(it) }
                    ?: extractFromDashPattern(line.text)?.let { suggestions.add(it) }
                    ?: extractFromNumberedPattern(line.text)?.let { suggestions.add(it) }
            }
        }

        suggestions.addAll(extractFromBlockStructure(text))

        return suggestions.distinctBy { it.term.lowercase().trim() }
    }

    private fun extractFromColonPattern(lineText: String): FlashcardSuggestion? {
        val colonIndex = lineText.indexOf(':')
        if (colonIndex > 0 && colonIndex < lineText.length - 1) {
            val term = lineText.substring(0, colonIndex).trim()
            val definition = lineText.substring(colonIndex + 1).trim()

            if (term.length in 2..50 && definition.length > term.length) {
                return FlashcardSuggestion(
                    term = term,
                    definition = definition,
                    confidence = 0.9f
                )
            }
        }
        return null
    }

    private fun extractFromDashPattern(lineText: String): FlashcardSuggestion? {

        val dashPatterns = listOf(" - ", " – ", " — ", " : ")

        for (pattern in dashPatterns) {
            val dashIndex = lineText.indexOf(pattern)
            if (dashIndex > 0 && dashIndex < lineText.length - pattern.length) {
                val term = lineText.substring(0, dashIndex).trim()
                val definition = lineText.substring(dashIndex + pattern.length).trim()

                if (term.length in 2..50 && definition.length > 2) {
                    return FlashcardSuggestion(
                        term = term,
                        definition = definition,
                        confidence = 0.85f
                    )
                }
            }
        }
        return null
    }


    private fun extractFromNumberedPattern(lineText: String): FlashcardSuggestion? {
        val numberedPattern = Regex("""^\s*\d+[.)\]]\s*(.+)""")
        val match = numberedPattern.find(lineText)

        if (match != null) {
            val content = match.groupValues[1]
            return extractFromColonPattern(content) ?: extractFromDashPattern(content)
        }
        return null
    }

    private fun extractFromBlockStructure(text: Text): List<FlashcardSuggestion> {
        val suggestions = mutableListOf<FlashcardSuggestion>()

        for (block in text.textBlocks) {
            val lines = block.lines
            if (lines.size >= 2) {
                var i = 0
                while (i < lines.size - 1) {
                    val currentLine = lines[i].text.trim()
                    val nextLine = lines[i + 1].text.trim()

                    if (looksLikeTerm(currentLine) && looksLikeDefinition(nextLine, currentLine)) {
                        suggestions.add(FlashcardSuggestion(
                            term = currentLine,
                            definition = nextLine,
                            confidence = 0.7f
                        ))
                        i += 2
                    } else {
                        i++
                    }
                }
            }
        }

        return suggestions
    }

    private fun looksLikeTerm(text: String): Boolean {
        val cleaned = text.trim()

        val isShort = cleaned.length in 2..60
        val wordCount = cleaned.split(Regex("\\s+")).size
        val hasReasonableWordCount = wordCount in 1..6
        val endsWithColon = cleaned.endsWith(":")
        val startsWithCapital = cleaned.firstOrNull()?.isUpperCase() == true
        val isAllCaps = cleaned == cleaned.uppercase() && cleaned.length > 2
        val hasBulletOrNumber = cleaned.matches(Regex("""^[\d•\-*]\s*.+"""))

        var score = 0
        if (isShort) score += 2
        if (hasReasonableWordCount) score += 2
        if (endsWithColon) score += 3
        if (startsWithCapital) score += 1
        if (isAllCaps) score += 2
        if (hasBulletOrNumber) score += 1

        return score >= 3
    }

    private fun looksLikeDefinition(text: String, potentialTerm: String): Boolean {
        val cleaned = text.trim()

        if (cleaned.length <= potentialTerm.length) return false

        if (cleaned.length < 10) return false

        val startsWithArticle = cleaned.lowercase().startsWith("a ") ||
                               cleaned.lowercase().startsWith("the ") ||
                               cleaned.lowercase().startsWith("an ")

        val startsWithLowercase = cleaned.firstOrNull()?.isLowerCase() == true

        return cleaned.length > 10 || startsWithArticle || startsWithLowercase
    }

    fun detectTableStructure(text: Text): Boolean {
        val leftEdges = mutableListOf<Int>()

        for (block in text.textBlocks) {
            for (line in block.lines) {
                line.boundingBox?.let { leftEdges.add(it.left) }
            }
        }

        if (leftEdges.size < 4) return false

        val distinctEdges = leftEdges.groupBy { it / 50 }
        return distinctEdges.size >= 2 && distinctEdges.values.any { it.size >= 2 }
    }

    fun groupByColumns(text: Text): Map<Int, List<String>> {
        val columns = mutableMapOf<Int, MutableList<String>>()

        for (block in text.textBlocks) {
            for (line in block.lines) {
                val leftEdge = line.boundingBox?.left ?: 0
                val columnIndex = leftEdge / 100

                if (!columns.containsKey(columnIndex)) {
                    columns[columnIndex] = mutableListOf()
                }
                columns[columnIndex]?.add(line.text)
            }
        }

        return columns.toSortedMap()
    }

    fun extractFromTwoColumnLayout(text: Text): List<FlashcardSuggestion> {
        val columns = groupByColumns(text)

        if (columns.size != 2) return emptyList()

        val leftColumn = columns.values.first()
        val rightColumn = columns.values.last()

        val suggestions = mutableListOf<FlashcardSuggestion>()
        val minSize = minOf(leftColumn.size, rightColumn.size)

        for (i in 0 until minSize) {
            val term = leftColumn[i].trim()
            val definition = rightColumn[i].trim()

            if (term.isNotEmpty() && definition.isNotEmpty()) {
                suggestions.add(FlashcardSuggestion(
                    term = term,
                    definition = definition,
                    confidence = 0.8f
                ))
            }
        }

        return suggestions
    }
}
