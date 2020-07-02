package com.dragontelnet.mychatapp.ui.activities.commentsviewer.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Comment
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.ui.activities.commentsviewer.adapter.AllCommentsListAdapter
import com.dragontelnet.mychatapp.ui.activities.commentsviewer.adapter.viewholder.CommentVH
import com.dragontelnet.mychatapp.ui.activities.commentsviewer.viewmodel.CommentsViewerViewModel
import com.dragontelnet.mychatapp.utils.MyConstants.FirestoreKeys
import com.dragontelnet.mychatapp.utils.UserProfileDetailsSetter
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.facebook.drawee.view.SimpleDraweeView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.activity_comments_viewer.*
import kotlinx.android.synthetic.main.content_comments_viewer.*
import kotlinx.android.synthetic.main.post_top_include.*

class CommentsViewerActivity : AppCompatActivity() {
    @BindView(R.id.post_photo)
    lateinit var postPhoto: SimpleDraweeView

    @BindView(R.id.comments_rv)
    lateinit var commentsRv: RecyclerView

    @BindView(R.id.comment_content_et)
    lateinit var commentContentEt: EditText

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    private var adapter: FirestoreRecyclerAdapter<Comment, CommentVH>? = null
    private var mViewModel: CommentsViewerViewModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments_viewer)
        ButterKnife.bind(this)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        mViewModel = ViewModelProvider(this).get(CommentsViewerViewModel::class.java)
        commentContentEt.requestFocus()

        initPostDetails()
        populateComments()
    }

    private fun initPostDetails() {
        getIntentPostObj()?.let { post ->

            post_date_time_tv.text = post.dateTime?.date + " " + post.dateTime?.time

            mViewModel?.getUser(post.byUid)?.observe(this, Observer { user ->
                UserProfileDetailsSetter.setAllUserDetails(user = user, nameTv = user_full_name, sdv = user_profile_pic)
            })
            if (post.postPhotoUrl != "") {
                postPhoto.setImageURI(post.postPhotoUrl)
                post_text_caption.visibility = View.GONE
                postPhoto.visibility = View.VISIBLE
            } else {
                post_text_caption.text = post.caption
                post_text_caption.visibility = View.VISIBLE
                postPhoto.visibility = View.GONE
            }

        }
    }

    private fun getIntentPostObj(): Post? {
        if (intent.hasExtra("post"))
            return intent.getSerializableExtra("post") as Post
        return null
    }

    override fun onBackPressed() {
        val i = Intent()
        i.putExtra("position", intent.getIntExtra("position", 0))
        setResult(Activity.RESULT_OK, i)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            val i = Intent()
            i.putExtra("position", intent.getIntExtra("position", 0))
            setResult(Activity.RESULT_OK, i)
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun populateComments() {
        val query = getIntentPostObj()?.let {
            it.postId?.let { postId ->
                MyFirestoreDbRefs.getCommentsCollectionRefOfPostUid(postId).orderBy(FirestoreKeys.TIMESTAMP)
            }
        }

        val options = query?.let { FirestoreRecyclerOptions.Builder<Comment>().setQuery(it, Comment::class.java).build() }
        adapter = options?.let { AllCommentsListAdapter(it, mViewModel, this) }
        commentsRv.adapter = adapter
        adapter?.startListening()

        checkCommentsEmptiness(query!!)
    }

    private fun checkCommentsEmptiness(query: Query) {
        mViewModel?.getCommentsEmptiness(query)?.observe(this, Observer { isEmpty ->
            if (isEmpty) {
                comment_na_tv.visibility = View.VISIBLE
            } else {
                comment_na_tv.visibility = View.GONE
            }
        })


    }

    @OnClick(R.id.comment_btn)
    fun onCommentBtnClicked() {
        val content = commentContentEt.text.toString().trim { it <= ' ' }
        if (!TextUtils.isEmpty(content)) {
            commentContentEt.setText("")
            getIntentPostObj()?.let { post ->
                sendComment(post, content)
            }
        }
    }

    private fun sendComment(post: Post, content: String) {
        mViewModel?.sendComment(post, content)?.observe(this, Observer { updatedPost ->
            updatedPost?.let {
                //comment + notif sent
            }
        })
    }

    companion object {
        private const val TAG = "PostDetailsActivity"
    }
}