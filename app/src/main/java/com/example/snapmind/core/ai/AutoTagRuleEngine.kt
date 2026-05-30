package com.example.snapmind.core.ai

import com.example.snapmind.data.local.entity.ClassificationEntity
import com.example.snapmind.data.local.entity.TagAssignedBy
import com.example.snapmind.data.local.entity.TagAssignmentSource
import com.example.snapmind.data.local.entity.VisionLabelEntity
import com.example.snapmind.data.repository.TagAssignmentRequest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoTagRuleEngine @Inject constructor() {

    fun buildAssignments(
        ocrText: String?,
        classifications: List<ClassificationEntity>,
        visionLabels: List<VisionLabelEntity>,
    ): List<TagAssignmentRequest> {
        val byNormalized = linkedMapOf<String, MutableSet<TagAssignmentSource>>()

        fun add(rawName: String, source: TagAssignmentSource) {
            val key = rawName.trim().lowercase().takeIf { it.isNotEmpty() } ?: return
            byNormalized.getOrPut(key) { linkedSetOf() }.add(source)
        }

        classifications
            .filter { it.label.isNotBlank() && it.label.lowercase() != UNKNOWN_LABEL }
            .filter { it.confidence >= CONFIDENCE_FLOOR }
            .forEach { add(it.label, TagAssignmentSource.TFLITE) }

        visionLabels
            .filter { it.label.isNotBlank() && it.score >= VISION_SCORE_THRESHOLD }
            .forEach { add(it.label, TagAssignmentSource.VISION) }

        if (!ocrText.isNullOrBlank()) {
            URL_PATTERN.findAll(ocrText).map { it.value }.forEach { url ->
                val host = extractHost(url) ?: return@forEach
                add(host, TagAssignmentSource.OCR)
            }
            EMAIL_PATTERN.findAll(ocrText).forEach { add("email", TagAssignmentSource.OCR) }
            if (KOREAN_PATTERN.containsMatchIn(ocrText)) add("korean", TagAssignmentSource.OCR)
        }

        return byNormalized.entries
            .take(MAX_TAGS)
            .map { (key, sources) ->
                TagAssignmentRequest(
                    rawName = key,
                    assignedBy = TagAssignedBy.AUTO,
                    sources = sources.toSet(),
                )
            }
    }

    private fun extractHost(url: String): String? {
        val noScheme = url.substringAfter("://", url)
        val hostPart = noScheme.substringBefore('/').substringBefore('?')
        val host = hostPart.removePrefix("www.").substringBefore(':')
        if (host.isBlank() || !host.contains('.')) return null
        return host.lowercase()
    }

    companion object {
        const val MAX_TAGS = 20
        private const val VISION_SCORE_THRESHOLD = 0.80f
        private const val CONFIDENCE_FLOOR = 0.50f
        private const val UNKNOWN_LABEL = "unknown"
        private val URL_PATTERN = Regex("https?://[\\w.\\-/?#=&%:+]+", RegexOption.IGNORE_CASE)
        private val EMAIL_PATTERN = Regex("[\\w.+-]+@[\\w-]+\\.[\\w.-]+")
        private val KOREAN_PATTERN = Regex("[가-힣]")
    }
}
