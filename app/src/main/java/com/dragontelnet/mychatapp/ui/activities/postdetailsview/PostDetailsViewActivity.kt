package com.dragontelnet.mychatapp.ui.activities.postdetailsview

import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.datasource.local.MySharedPrefs
import com.dragontelnet.mychatapp.datasource.remote.firebase.fragmentsrepos.FeedsFragmentRepo
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.ui.activities.commentsviewer.view.CommentsViewerActivity
import com.dragontelnet.mychatapp.ui.activities.likers.view.LikersActivity
import com.dragontelnet.mychatapp.ui.fragments.home.view.FeedsFragment
import com.dragontelnet.mychatapp.utils.auth.CurrentUser
import com.facebook.drawee.view.SimpleDraweeView

class PostDetailsViewActivity : AppCompatActivity() {

    @BindView(R.id.user_profile_pic)
    lateinit var userProfilePic: SimpleDraweeView

    @BindView(R.id.user_full_name)
    lateinit var userFullName: TextView

    @BindView(R.id.caption_tv)
    lateinit var captionTv: TextView

    @BindView(R.id.post_photo)
    lateinit var postPhoto: SimpleDraweeView

    @BindView(R.id.likes_count_tv)
    lateinit var likesTv: TextView

    @BindView(R.id.last_comment_owner_name)
    lateinit var lastCommentOwnerName: TextView

    @BindView(R.id.last_comment)
    lateinit var lastComment: TextView

    @BindView(R.id.edit_post_btn)
    lateinit var editPostBtn: ImageButton

    @BindView(R.id.like_btn)
    lateinit var likeBtn: ImageButton

    @BindView(R.id.comment_btn)
    lateinit var commentBtn: ImageButton

    @BindView(R.id.comments_count_tv)
    lateinit var commentsCountTv: TextView

    @BindView(R.id.post_content_include)
    lateinit var postContentInclude: View

    @BindView(R.id.post_swipe_refresh)
    lateinit var postSwipeRefresh: SwipeRefreshLayout

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    private var mViewModel: PostDetailsViewModel? = null

    private var newLikeCount: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_details_view)
        ButterKnife.bind(this)
        initUi()

        mViewModel = ViewModelProvider(this).get(PostDetailsViewModel::class.java)
        postSwipeRefresh.setOnRefreshListener {
            intentPostObj()?.let { refreshPostDetails(it) }
        }

        initIntentPostDetails()

        setUpClickListener()

    }

    private fun setUpClickListener() {
        likesTv.setOnClickListener {
            if (intentPostObj()?.likersUids?.size!! > 0) {
                val likersIntent = Intent(this, LikersActivity::class.java)
                likersIntent.putExtra("post", intentPostObj())
                startActivity(likersIntent)
            }
        }
    }

    private fun initIntentPostDetails() {
        intentPostObj()?.let { post -> initPostDetails(post) }
                ?: Toast.makeText(this, "post / id is null", Toast.LENGTH_SHORT).show()
    }

    private fun initUi() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun refreshPostDetails(post: Post) {
        mViewModel?.getPost(post)
                ?.observe(this, Observer { postUpdated: Post ->
                    newLikeCount = postUpdated.likersUids?.size?.toLong()!!
                    postSwipeRefresh.isRefreshing = false
                    MySharedPrefs.putPostObjToBook(postUpdated)
                    initPostDetails(postUpdated)
                })
    }

    private fun intentPostObj(): Post? {
        if (intent.hasExtra("post")) {
            return intent.getSerializableExtra("post") as Post
        }
        return null
    }

    private fun initPostDetails(post: Post) {
        if (post.byUid == CurrentUser.getCurrentUser()?.uid) {
            //same user post
            editPostBtn.visibility = View.VISIBLE

            setUpPostDelListener(post)

        } else {
            editPostBtn.visibility = View.GONE
            editPostBtn.setOnClickListener(null)
        }
        newLikeCount = post.likersUids?.size?.toLong()!!

        postContentInclude.visibility = View.VISIBLE
        with(post)
        {
            if (this.postPhotoUrl == "") {
                postPhoto.visibility = View.GONE
                captionTv.typeface = Typeface.DEFAULT
            } else {
                postPhoto.setImageURI(this.postPhotoUrl)
                postPhoto.visibility = View.VISIBLE
                captionTv.typeface = Typeface.defaultFromStyle(Typeface.ITALIC)
            }
            captionTv.text = this.caption
        }

        initUserDetails(post)
        checkMyLikesExistence(post)
        countLikes(post)
        countComments(post)
        checkForLastComment(post)
    }

    private fun setUpPostDelListener(post: Post) {
        editPostBtn.setOnClickListener {
            if (post.byUid == CurrentUser.getCurrentUser()?.uid) {
                val popUp = PopupMenu(this, it)
                val menu = popUp.menu
                val menuInflater = popUp.menuInflater
                menuInflater.inflate(R.menu.post_del_menu, menu)
                popUp.setOnMenuItemClickListener {
                    mViewModel?.deletePost(post)?.observe(this, Observer { isDelSuccess ->
                        if (isDelSuccess) {
                            //post deleted
                            setResult(Activity.RESULT_OK)
                            finish()
                            Toast.makeText(this, "Post deleted!!!", Toast.LENGTH_SHORT).show()
                        }
                    })
                    true
                }
                popUp.show()
            }
        }
    }

    private fun initUserDetails(post: Post) {
        if (post.postOwnerName != "") {
            userFullName.text = post.postOwnerName
            userProfilePic.setImageURI(post.postOwnerProfilePic)
        } else {
            post.byUid?.let {
                mViewModel?.getUser(it)?.observe(this, Observer { user: User ->
                    userFullName.text = user.name
                    userProfilePic.setImageURI(user.profilePic)
                })
            }
        }
    }

    private fun countComments(post: Post) {
        commentsCountTv.text = post.commentsCount.toString()
        post.lastComment?.commentByUid?.let {
            mViewModel?.getUser(it)?.observe(this,
                    Observer { user -> lastCommentOwnerName.text = user.name })
        }
    }

    private fun checkForLastComment(post: Post) {
        lastComment.text = post.lastComment?.content

        post.lastComment?.commentByUid?.let {
            mViewModel?.getUser(it)?.observe(this,
                    Observer { user -> lastCommentOwnerName.text = user.name })

        } ?: run {
            lastCommentOwnerName.text = ""
            lastComment.text = "No Comments Available!!!!"
        }
    }

    private fun countLikes(post: Post) {
        likesTv.text = "${post.likersUids?.size} Likes"
    }

    private fun checkMyLikesExistence(post: Post) {
        if (post.likersUids?.contains(CurrentUser.getCurrentUser()?.uid)!!) {
            //my like exists
            likeBtn.setImageResource(R.drawable.ic_like_liked)
            likeBtn.tag = FeedsFragment.LIKED_TAG
        } else {
            //my like not exists
            likeBtn.setImageResource(R.drawable.ic_like_not_liked)
            likeBtn.tag = FeedsFragment.NOT_LIKED_TAG
        }
    }

    @OnClick(R.id.like_btn)
    fun onLikeBtnClicked() {
        if (likeBtn.tag != null) {
            if (likeBtn.tag.toString() == FeedsFragment.NOT_LIKED_TAG) {
                likeBtn.setImageResource(R.drawable.ic_like_liked)
                likeBtn.tag = FeedsFragment.LIKED_TAG
                newLikeCount += 1
                likesTv.text = "$newLikeCount Likes"
                intentPostObj()?.let {
                    it.likersUids?.add(CurrentUser.getCurrentUser()?.uid)
                }
            } else {
                likeBtn.setImageResource(R.drawable.ic_like_not_liked)
                likeBtn.tag = FeedsFragment.NOT_LIKED_TAG
                newLikeCount -= 1
                likesTv.text = "$newLikeCount Likes"
                intentPostObj()?.let {
                    it.likersUids?.remove(CurrentUser.getCurrentUser()?.uid)
                }
            }
            MySharedPrefs.putPostObjToBook(intentPostObj())
            //sending like to db
            intentPostObj()?.let { postObj -> sendLike(postObj) }
        }
    }

    private fun sendLike(post: Post) {
        mViewModel?.sendLikeToPost(post)?.observe(this, Observer { likeCode: Int ->
            if (likeCode == FeedsFragmentRepo.SENT_LIKE_CODE) {
                /*like sent*/
            } else {
                //like removed
            }
        })
    }

    @OnClick(R.id.comment_btn)
    fun onCommentBtnClicked() {
        val intent = Intent(this, CommentsViewerActivity::class.java).apply {
            intentPostObj()?.let { post ->
                putExtra("post", post)
            }
        }
        startActivity(intent)
    }


}
