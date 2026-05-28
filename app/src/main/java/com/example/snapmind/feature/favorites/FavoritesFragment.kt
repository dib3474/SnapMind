package com.example.snapmind.feature.favorites

import com.example.snapmind.data.model.MemoryItem
import com.example.snapmind.feature.home.MemoryGridFragment
import com.example.snapmind.ui.main.MainUiState

class FavoritesFragment : MemoryGridFragment() {
    override val emptyTitle: String = "즐겨찾기가 비어 있어요"
    override val emptyMessage: String = "상세 화면의 하트 버튼으로 중요한 이미지를 모아둘 수 있어요."

    override fun selectItems(state: MainUiState): List<MemoryItem> = state.favoriteItems
}
