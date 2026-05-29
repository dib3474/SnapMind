package com.example.snapmind.data.repository

import com.example.snapmind.data.model.CategoryCount
import com.example.snapmind.data.model.MemoryCategory
import com.example.snapmind.data.model.MemoryItem
import com.example.snapmind.data.model.ProcessingStatus
import com.example.snapmind.data.model.TagCount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InMemoryMemoryRepository @Inject constructor() : MemoryRepository {
    private val _memories = MutableStateFlow(seedMemories())
    override val memories: StateFlow<List<MemoryItem>> = _memories.asStateFlow()

    override fun getMemory(memoryId: Long): MemoryItem? =
        _memories.value.firstOrNull { it.id == memoryId }

    override fun activeMemories(): List<MemoryItem> =
        _memories.value.filterNot { it.isDeleted }.sortedByDescending { it.createdAtMillis }

    override fun favoriteMemories(): List<MemoryItem> =
        activeMemories().filter { it.isFavorite }

    override fun topTags(limit: Int): List<TagCount> =
        activeMemories()
            .flatMap { it.tags }
            .groupingBy { it.normalizeTag() }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key })
            .take(limit)
            .map { TagCount(name = it.key, displayName = "#${it.key}", count = it.value) }

    override fun categoryCounts(): List<CategoryCount> =
        activeMemories()
            .groupingBy { it.category }
            .eachCount()
            .entries
            .sortedWith(compareByDescending<Map.Entry<MemoryCategory, Int>> { it.value }.thenBy { it.key.displayName })
            .map { CategoryCount(category = it.key, count = it.value) }

    override fun tags(): List<TagCount> = topTags(Int.MAX_VALUE)

    override fun filterByTag(tagName: String?): List<MemoryItem> {
        val normalized = tagName?.normalizeTag()
        return activeMemories().filter { memory ->
            normalized == null || memory.tags.any { it.normalizeTag() == normalized }
        }
    }

    override fun filterByCategory(category: MemoryCategory?): List<MemoryItem> =
        activeMemories().filter { category == null || it.category == category }

    override fun toggleFavorite(memoryId: Long) {
        _memories.value = _memories.value.map { item ->
            if (item.id == memoryId) item.copy(isFavorite = !item.isFavorite) else item
        }
    }

    private fun String.normalizeTag(): String = trim().removePrefix("#").lowercase()

    private fun seedMemories(): List<MemoryItem> {
        val now = System.currentTimeMillis()
        return listOf(
            MemoryItem(
                id = 1,
                category = MemoryCategory.CODE,
                memo = "React hydration error - 아침 회의 전에 원인 확인",
                ocrText = "Hydration failed because the initial UI does not match.",
                tags = listOf("#React_Error", "#BugFix"),
                createdAtMillis = now - 2 * HOUR,
                processingStatus = ProcessingStatus.PROCESSING,
                isFavorite = true,
                geminiSuggestion = "내일 회의 전 수정할 React 오류를 저장했습니다.",
            ),
            MemoryItem(
                id = 2,
                category = MemoryCategory.SHOPPING,
                memo = "흰색 운동화 사이즈 280 재입고 확인",
                ocrText = "White sneakers size 10 availability",
                tags = listOf("#To_buy_later"),
                createdAtMillis = now - 5 * HOUR,
                processingStatus = ProcessingStatus.DONE,
            ),
            MemoryItem(
                id = 3,
                category = MemoryCategory.MAP,
                memo = "강남 카페 위치 - 주말에 방문",
                ocrText = "Gangnam coffee shop",
                tags = listOf("#Weekend", "#Cafe"),
                createdAtMillis = now - 26 * HOUR,
                processingStatus = ProcessingStatus.DONE,
                isFavorite = true,
            ),
            MemoryItem(
                id = 4,
                category = MemoryCategory.RECEIPT,
                memo = "점심 영수증 48,500원 - 비용 처리",
                ocrText = "Business lunch receipt 48,500 KRW",
                tags = listOf("#Expense", "#Receipt"),
                createdAtMillis = now - 30 * HOUR,
                processingStatus = ProcessingStatus.ERROR,
            ),
            MemoryItem(
                id = 5,
                category = MemoryCategory.CHAT,
                memo = "프로젝트 제안 답장 금요일까지",
                ocrText = "Project proposal reply by Friday",
                tags = listOf("#Action_Required"),
                createdAtMillis = now - 2 * DAY,
                processingStatus = ProcessingStatus.DONE,
                isFavorite = true,
            ),
            MemoryItem(
                id = 6,
                category = MemoryCategory.YOUTUBE,
                memo = "Compose 애니메이션 강의 다시 보기",
                ocrText = "Jetpack Compose animation tutorial",
                tags = listOf("#Study", "#Android"),
                createdAtMillis = now - 3 * DAY,
                processingStatus = ProcessingStatus.DONE,
                youtubeTitle = "Jetpack Compose animation tutorial",
                youtubeUrl = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
            ),
        )
    }

    private companion object {
        const val HOUR = 60 * 60 * 1000L
        const val DAY = 24 * HOUR
    }
}
