package com.dragontelnet.mychatapp.ui.fragments.home.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.ui.fragments.home.pageradapter.HomeBasePagerAdapter
import com.google.android.material.tabs.TabLayout

class HomeBaseFragment : Fragment() {
    @BindView(R.id.tabs)
    lateinit var tabs: TabLayout

    @BindView(R.id.view_pager)
    lateinit var viewPager: ViewPager

    private var adapter: HomeBasePagerAdapter? = null
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.home_base_fragment, container, false)
        ButterKnife.bind(this, view)
        val toolbarTxt = activity?.findViewById<TextView>(R.id.toolbar_title)
        toolbarTxt?.text = resources.getString(R.string.title_home)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter = activity?.supportFragmentManager?.let {
            HomeBasePagerAdapter(it, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT)
        }
        viewPager.adapter = adapter
        tabs.setupWithViewPager(viewPager)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            val toolbarTxt = activity?.findViewById<TextView>(R.id.toolbar_title)
            toolbarTxt?.text = resources.getString(R.string.title_home)
        }
        val homeFrag = activity?.supportFragmentManager
                ?.findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + adapter?.getItemId(0))
        val storyFrag = activity?.supportFragmentManager
                ?.findFragmentByTag("android:switcher:" + R.id.view_pager + ":" + adapter?.getItemId(1))
        homeFrag?.onHiddenChanged(hidden)
        storyFrag?.onHiddenChanged(hidden)
    }

    companion object {
        private const val TAG = "HomeBaseFragment"
    }
}