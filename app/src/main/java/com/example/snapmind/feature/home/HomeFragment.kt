package com.example.snapmind.feature.home

import com.example.snapmind.data.model.MemoryItem
import com.example.snapmind.ui.main.MainUiState

class HomeFragment : MemoryGridFragment() {
    override val emptyTitle: String = "아직 저장한 이미지가 없어요"
    override val emptyMessage: String = "공유하기 또는 + 버튼으로 스크린샷을 추가해보세요."

    override fun selectItems(state: MainUiState): List<MemoryItem> = state.homeItems
}
