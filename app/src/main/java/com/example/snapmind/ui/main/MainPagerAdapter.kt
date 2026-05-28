package com.example.snapmind.ui.main

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.snapmind.feature.favorites.FavoritesFragment
import com.example.snapmind.feature.home.HomeFragment
import com.example.snapmind.feature.settings.SettingsFragment
import com.example.snapmind.feature.tagbrowse.TagBrowseFragment

class MainPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = PAGE_COUNT

    override fun createFragment(position: Int): Fragment =
        when (position) {
            PAGE_FAVORITES -> FavoritesFragment()
            PAGE_TAGS -> TagBrowseFragment()
            PAGE_SETTINGS -> SettingsFragment()
            else -> HomeFragment()
        }

    companion object {
        const val PAGE_HOME = 0
        const val PAGE_FAVORITES = 1
        const val PAGE_TAGS = 2
        const val PAGE_SETTINGS = 3
        const val PAGE_COUNT = 4
    }
}
