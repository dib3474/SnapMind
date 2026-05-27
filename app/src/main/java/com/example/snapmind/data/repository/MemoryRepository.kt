package com.example.snapmind.data.repository

import com.example.snapmind.data.model.CategoryCount
import com.example.snapmind.data.model.MemoryCategory
import com.example.snapmind.data.model.MemoryItem
import com.example.snapmind.data.model.TagCount
import kotlinx.coroutines.flow.StateFlow

interface MemoryRepository {
    val memories: StateFlow<List<MemoryItem>>

    fun getMemory(memoryId: Long): MemoryItem?
    fun activeMemories(): List<MemoryItem>
    fun favoriteMemories(): List<MemoryItem>
    fun topTags(limit: Int = 3): List<TagCount>
    fun categoryCounts(): List<CategoryCount>
    fun tags(): List<TagCount>
    fun filterByTag(tagName: String?): List<MemoryItem>
    fun filterByCategory(category: MemoryCategory?): List<MemoryItem>
    fun toggleFavorite(memoryId: Long)
}
