package com.example.snapmind.ui.main

import com.example.snapmind.data.model.CategoryCount
import com.example.snapmind.data.model.MemoryCategory
import com.example.snapmind.data.model.MemoryItem
import com.example.snapmind.data.model.TagCount

data class MainUiState(
    val memories: List<MemoryItem> = emptyList(),
    val homeItems: List<MemoryItem> = emptyList(),
    val favoriteItems: List<MemoryItem> = emptyList(),
    val tagItems: List<MemoryItem> = emptyList(),
    val tags: List<TagCount> = emptyList(),
    val topTags: List<TagCount> = emptyList(),
    val categories: List<CategoryCount> = emptyList(),
    val selectedTag: String? = null,
    val selectedCategory: MemoryCategory? = null,
) {
    val activeFilterLabel: String?
        get() = selectedTag?.let { "#${it.removePrefix("#")}" } ?: selectedCategory?.displayName
}
