package com.dragontelnet.mychatapp.ui.fragments.home.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.ui.activities.createpost.CreateNewPostActivity
import com.dragontelnet.mychatapp.ui.fragments.home.adapter.FeedsListAdapter
import com.dragontelnet.mychatapp.ui.fragments.home.viewmodel.FeedsFragmentViewModel
import com.dragontelnet.mychatapp.utils.auth.CurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import com.google.firebase.firestore.Query

class FeedsFragment : Fragment() {

    @BindView(R.id.posts_rv)
    lateinit var postsRv: RecyclerView

    @BindView(R.id.feeds_swipe_refresh)
    lateinit var feedsSwipeRefresh: SwipeRefreshLayout

    @BindView(R.id.feeds_empty_checker_tv)
    lateinit var feedsEmptyCheckerTv: TextView

    private var adapter: FeedsListAdapter? = null

    private var mViewModel: FeedsFragmentViewModel? = null

    private val feedsQuery = MyFirestoreDbRefs.getFeedsCollectionOfUid(CurrentUser.getCurrentUser()!!.uid)
            .orderBy(Post.ORDER_BY_TIMESTAMP, Query.Direction.DESCENDING)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_feeds, container, false)
        ButterKnife.bind(this, view)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(this).get(FeedsFragmentViewModel::class.java)
        feedsSwipeRefresh.isRefreshing = true

        feedsSwipeRefresh.setOnRefreshListener {
            adapter?.refresh()
            checkFeedsEmptiness()
        }

        populatePostsList()
    }

    private fun checkFeedsEmptiness() {
        mViewModel?.checkFeedsEmptiness(feedsQuery)?.observe(viewLifecycleOwner, Observer { isEmpty ->
            if (isEmpty) {
                feedsEmptyCheckerTv.visibility = View.VISIBLE
            } else {
                feedsEmptyCheckerTv.visibility = View.GONE
            }
        })
    }


    private val config: PagedList.Config
        get() = PagedList.Config.Builder()
                .setEnablePlaceholders(true)
                .setPrefetchDistance(10)
                .setPageSize(8)
                .build()

    private fun getDefaultPagingOptions(): FirestorePagingOptions<Post> {

        return FirestorePagingOptions.Builder<Post>()
                .setLifecycleOwner(this)
                .setQuery(feedsQuery, config, Post::class.java).build()
    }

    private fun populatePostsList() {
        adapter = mViewModel?.let {
            FeedsListAdapter(getDefaultPagingOptions(), this, it, feedsSwipeRefresh)
        }
        postsRv.adapter = adapter
        adapter?.startListening()
        checkFeedsEmptiness()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == UPLOADED_REQ_CODE) {
            adapter?.refresh()
        }
        if (resultCode == Activity.RESULT_OK && requestCode == ADDED_LIKE_COMMENT_REQ_CODE && data != null) {
            val position = data.getIntExtra("position", 0)
            postsRv.smoothScrollToPosition(position)
            adapter?.notifyItemChanged(position)
        }
    }

    @OnClick(R.id.create_new_post_btn)
    fun onNewPostBtnClicked() {
        val intent = Intent(activity, CreateNewPostActivity::class.java)
        startActivityForResult(intent, 123)
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter?.stopListening()
        adapter = null
        mViewModel?.removeFeedsCheckerListener()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            mViewModel?.removeFeedsCheckerListener()
        } else {
            checkFeedsEmptiness()
        }
    }

    companion object {
        private const val TAG = "FeedsFragment"
        private const val TYPE_CREATE_NEW_POST = 1
        private const val TYPE_POST_WITH_PHOTO = 2
        private const val TYPE_POST_WITHOUT_PHOTO = 3
        const val LIKED_TAG = "LIKED"
        const val NOT_LIKED_TAG = "NOT_LIKED"
        const val UPLOADED_REQ_CODE = 123
        const val ADDED_LIKE_COMMENT_REQ_CODE = 456
    }
}