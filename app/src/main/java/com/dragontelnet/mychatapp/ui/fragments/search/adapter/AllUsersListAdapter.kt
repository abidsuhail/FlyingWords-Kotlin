package com.dragontelnet.mychatapp.ui.fragments.search.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.ui.activities.profile.view.ProfileActivity
import com.dragontelnet.mychatapp.ui.fragments.search.adapter.viewholder.UserVH
import com.dragontelnet.mychatapp.ui.fragments.search.view.SearchFragment
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState

class AllUsersListAdapter(options: FirestorePagingOptions<User>, private val searchFragment: SearchFragment, private val swipeRefreshLayout: SwipeRefreshLayout) : FirestorePagingAdapter<User, UserVH>(options) {
    override fun onBindViewHolder(holder: UserVH, position: Int, user: User) {
        holder.bindUserDetails(holder, user)
        holder.itemView.setOnClickListener { startProfileActivity(user) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserVH {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.user_layout, parent, false)
        return UserVH(view)
    }

    override fun onLoadingStateChanged(state: LoadingState) {
        super.onLoadingStateChanged(state)
        when (state) {
            LoadingState.LOADING_INITIAL, LoadingState.LOADING_MORE -> swipeRefreshLayout.isRefreshing = true
            LoadingState.LOADED, LoadingState.FINISHED -> swipeRefreshLayout.isRefreshing = false
            LoadingState.ERROR -> {
                swipeRefreshLayout.isRefreshing = false
                retry()
            }
        }
    }

    private fun startProfileActivity(user: User) {
        val i = Intent(searchFragment.context, ProfileActivity::class.java)
        i.putExtra("user", user)
        searchFragment.startActivity(i)
    }

}