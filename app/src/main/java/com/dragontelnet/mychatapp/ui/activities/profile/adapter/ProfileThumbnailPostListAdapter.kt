package com.dragontelnet.mychatapp.ui.activities.profile.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.ui.activities.postdetailsview.PostDetailsViewActivity
import com.dragontelnet.mychatapp.ui.activities.profile.adapter.viewholder.ThumbnailPostVH
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState

class ProfileThumbnailPostListAdapter(options: FirestorePagingOptions<Post?>,
                                      private val activity: Activity,
                                      private val myFeedsSwipeRefresh: SwipeRefreshLayout) : FirestorePagingAdapter<Post, ThumbnailPostVH>(options) {
    override fun onBindViewHolder(holder: ThumbnailPostVH, itemPosition: Int,
                                  post: Post) {
        if (post.postPhotoUrl == "") {
            holder.bindPostStatus(holder, post)
        } else {
            holder.bindPostPhoto(holder, post)
        }
        holder.view.setOnClickListener { v: View? -> startPostDetailsViewActivity(post) }
    }

    private fun startPostDetailsViewActivity(post: Post) {
        //start PostDetailsViewActivity
        val intent = Intent(activity, PostDetailsViewActivity::class.java)
        intent.putExtra("post", post)
        activity.startActivityForResult(intent, 123)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailPostVH {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.my_post_layout, parent, false)
        return ThumbnailPostVH(view)
    }

    override fun onLoadingStateChanged(state: LoadingState) {
        super.onLoadingStateChanged(state)
        when (state) {
            LoadingState.LOADING_INITIAL, LoadingState.LOADING_MORE -> myFeedsSwipeRefresh.isRefreshing = true
            LoadingState.LOADED, LoadingState.FINISHED -> myFeedsSwipeRefresh.isRefreshing = false
            LoadingState.ERROR -> {
                myFeedsSwipeRefresh.isRefreshing = false
                retry()
            }
        }
    }

}