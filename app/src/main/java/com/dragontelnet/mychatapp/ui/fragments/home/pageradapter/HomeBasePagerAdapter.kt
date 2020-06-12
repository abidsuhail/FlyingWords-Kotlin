package com.dragontelnet.mychatapp.ui.fragments.home.pageradapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.dragontelnet.mychatapp.ui.fragments.home.view.FeedsFragment
import com.dragontelnet.mychatapp.ui.fragments.story.view.StoryFragment

class HomeBasePagerAdapter(fm: FragmentManager, behavior: Int) : FragmentPagerAdapter(fm, behavior) {
    override fun getItem(position: Int): Fragment {
        var fragment: Fragment? = null
        fragment = when (position) {
            0 -> FeedsFragment()
            1 -> StoryFragment()
            else -> throw IllegalStateException("Unexpected value: $position")
        }
        return fragment
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence? {
        var title: String? = null
        title = when (position) {
            0 -> "Feeds"
            1 -> "Story"
            else -> throw IllegalStateException("Unexpected value: $position")
        }
        return title
    }
}