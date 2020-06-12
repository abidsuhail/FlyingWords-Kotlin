package com.dragontelnet.mychatapp.ui.fragments.home.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.datasource.remote.repository.fragmentsrepos.FeedsFragmentRepo
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.ui.activities.commentsviewer.view.CommentsViewerActivity
import com.dragontelnet.mychatapp.ui.fragments.home.adapter.viewholder.PostVH
import com.dragontelnet.mychatapp.ui.fragments.home.view.FeedsFragment
import com.dragontelnet.mychatapp.ui.fragments.home.viewmodel.FeedsFragmentViewModel
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.firebase.ui.firestore.paging.FirestorePagingAdapter
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.firebase.ui.firestore.paging.LoadingState
import com.pedromassango.doubleclick.DoubleClick
import com.pedromassango.doubleclick.DoubleClickListener
import io.paperdb.Paper

class FeedsListAdapter(options: FirestorePagingOptions<Post>,
                       private val feedsFragment: FeedsFragment,
                       private val mViewModel: FeedsFragmentViewModel,
                       private val feedsSwipeRefresh: SwipeRefreshLayout) : FirestorePagingAdapter<Post, PostVH>(options) {
    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        Paper.book(Post.POSTS_BOOK_KEY).destroy()
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        Paper.book(Post.POSTS_BOOK_KEY).destroy()
    }

    override fun onBindViewHolder(holder: PostVH, position: Int, post: Post) {
        //checking if offline state post exists
        //is exists use it in variable updated post

        holder.userUid = post.byUid!!
        holder.mLastCommentOwnerUid = post.lastComment?.commentByUid
        if (post.byUid == getCurrentUser()?.uid) {
            //same user post
            holder.mEditPostBtn.visibility = View.VISIBLE
        } else {
            holder.mEditPostBtn.visibility = View.GONE
        }
        val updatedPost: Post = getLocalStatePost(post.postId) ?: post
        //setting post owner details
        holder.bindUserDetails(holder, updatedPost, mViewModel, feedsFragment)

        //setting post details
        holder.bindPostDetails(holder, updatedPost, mViewModel, feedsFragment)
        setUpClickListeners(holder, updatedPost, position)
    }

    private fun setUpClickListeners(holder: PostVH, updatedPost: Post, position: Int) {
        holder.itemView.setOnClickListener(DoubleClick(object : DoubleClickListener {
            override fun onSingleClick(view: View) {}
            override fun onDoubleClick(view: View) {
                likeBtnClicked(updatedPost, holder)
            }
        }))
        holder.mLikeBtn.setOnClickListener { likeBtnClicked(updatedPost, holder) }
        holder.mCommentBtn.setOnClickListener {
            startCommentsViewerActivity(updatedPost, position)
        }
        holder.mEditPostBtn.setOnClickListener {
            if (updatedPost.byUid == getCurrentUser()?.uid) {
                val popUp = PopupMenu(feedsFragment.context, it)
                val menu = popUp.menu
                val menuInflater = popUp.menuInflater
                menuInflater.inflate(R.menu.post_del_menu, menu)
                popUp.setOnMenuItemClickListener {
                    //deleting post
                    mViewModel.deletePost(updatedPost).observe(feedsFragment, Observer { isDelSuccess ->
                        if (isDelSuccess) {
                            Toast.makeText(feedsFragment.context, "Post deleted!!!,refresh for update changes", Toast.LENGTH_SHORT).show()
                        }
                    })
                    true
                }
                popUp.show()
            }
        }
    }

    private fun startCommentsViewerActivity(updatedPost: Post, position: Int) {
        val intent = Intent(feedsFragment.context, CommentsViewerActivity::class.java)
        intent.putExtra("post", updatedPost)
        intent.putExtra("position", position)
        feedsFragment.startActivityForResult(intent, FeedsFragment.ADDED_LIKE_COMMENT_REQ_CODE)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostVH {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.post_layout, parent, false)
        return PostVH(view)
    }

    override fun onLoadingStateChanged(state: LoadingState) {
        super.onLoadingStateChanged(state)
        when (state) {
            LoadingState.LOADING_INITIAL, LoadingState.LOADING_MORE -> feedsSwipeRefresh.isRefreshing = true
            LoadingState.LOADED, LoadingState.FINISHED -> feedsSwipeRefresh.isRefreshing = false
            LoadingState.ERROR -> {
                feedsSwipeRefresh.isRefreshing = false
                retry()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun likeBtnClicked(post: Post, holder: PostVH) {
        if (holder.mLikeBtn.tag != null) {
            if (holder.mLikeBtn.tag.toString() == FeedsFragment.NOT_LIKED_TAG) {
                post.likersUids?.add(getCurrentUser()?.uid)
                savePostStateToLocal(post)
                setLikedButtonView(holder)
            } else {
                post.likersUids?.remove(getCurrentUser()?.uid)
                //setting new post obj
                savePostStateToLocal(post)
                setRemovedLikeButtonView(holder)
            }
            holder.mLikesCountTv.text = "${post.likersUids?.size.toString()} Likes"
            updateLikeToDb(post, holder)
        }
    }

    private fun updateLikeToDb(post: Post, holder: PostVH) {
        mViewModel.sendLikeToPost(post).observe(feedsFragment.viewLifecycleOwner, Observer { likeCode: Int ->
            if (likeCode == FeedsFragmentRepo.SENT_LIKE_CODE) {
                //like sent
                setLikedButtonView(holder)
            } else {
                //like removed
                setRemovedLikeButtonView(holder)
            }
        })
    }

    private fun setLikedButtonView(holder: PostVH) {
        holder.mLikeBtn.setImageResource(R.drawable.ic_like_liked)
        holder.mLikeBtn.tag = FeedsFragment.LIKED_TAG
    }

    private fun setRemovedLikeButtonView(holder: PostVH) {
        holder.mLikeBtn.setImageResource(R.drawable.ic_like_not_liked)
        holder.mLikeBtn.tag = FeedsFragment.NOT_LIKED_TAG
    }

    companion object {
        private const val TAG = "FeedsListAdapter"
    }

    override fun refresh() {
        super.refresh()
        Paper.book(Post.POSTS_BOOK_KEY).destroy()
    }

    override fun updateOptions(options: FirestorePagingOptions<Post>) {
        super.updateOptions(options)
        Paper.book(Post.POSTS_BOOK_KEY).destroy()
    }

    private fun getLocalStatePost(postId: String?): Post? {
        return Paper.book(Post.POSTS_BOOK_KEY).read(postId)
    }

    private fun savePostStateToLocal(post: Post) {
        Paper.book(Post.POSTS_BOOK_KEY).write(post.postId, post)
    }
}