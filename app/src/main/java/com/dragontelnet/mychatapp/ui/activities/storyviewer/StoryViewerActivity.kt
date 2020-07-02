package com.dragontelnet.mychatapp.ui.activities.storyviewer

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Story
import com.dragontelnet.mychatapp.model.entity.StoryItem
import com.dragontelnet.mychatapp.ui.activities.messaging.viewmodel.MessagingViewModel
import com.dragontelnet.mychatapp.ui.fragments.story.view.SeenListBottomFragment
import com.dragontelnet.mychatapp.ui.fragments.story.view.SeenListBottomFragment.BottomFragmentSheetListener
import com.dragontelnet.mychatapp.ui.fragments.story.view.StoryReplyBottomFragment
import com.dragontelnet.mychatapp.utils.StoryImgControllerListener
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.interfaces.DraweeController
import com.facebook.drawee.view.SimpleDraweeView
import jp.shts.android.storiesprogressview.StoriesProgressView
import jp.shts.android.storiesprogressview.StoriesProgressView.StoriesListener
import kotlinx.android.synthetic.main.activity_story_viewer.*

class StoryViewerActivity : AppCompatActivity(), StoriesListener, BottomFragmentSheetListener, StoryReplyBottomFragment.StoryReplyBottomSheetListener {

    @BindView(R.id.story_progress)
    lateinit var storiesProgressView: StoriesProgressView

    @BindView(R.id.profilePic)
    lateinit var profilePic: SimpleDraweeView

    @BindView(R.id.full_name_tv)
    lateinit var fullNameTv: TextView

    @BindView(R.id.date_time_tv)
    lateinit var dateTimeTv: TextView

    @BindView(R.id.story_photo)
    lateinit var storyPhoto: SimpleDraweeView

    @BindView(R.id.left_click_layout)
    lateinit var leftClickLayout: View

    @BindView(R.id.right_click_layout)
    lateinit var rightClickLayout: View

    @BindView(R.id.seen_eye_layout)
    lateinit var seenEyeLayout: LinearLayout

    @BindView(R.id.seen_count_tv)
    lateinit var seenCountTv: TextView

    private var count = 0
    private var pressTime = 0L
    private val limit = 500L
    private var story: Story? = null
    private var sheet: SeenListBottomFragment? = null
    private var mViewModel: StoryViewerActivityViewModel? = null
    private var messagingViewModel: MessagingViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_story_viewer)
        ButterKnife.bind(this)
        mViewModel = ViewModelProvider(this).get(StoryViewerActivityViewModel::class.java)
        messagingViewModel = ViewModelProvider(this).get(MessagingViewModel::class.java)
        //getting story obj from previous story fragment
        story = intent.getSerializableExtra("story") as Story
        sheet = SeenListBottomFragment()
        if (story?.byUid == getCurrentUser()?.uid) {
            seenEyeLayout.visibility = View.VISIBLE
            reply_layout.visibility = View.GONE
        } else {
            seenEyeLayout.visibility = View.GONE
            reply_layout.visibility = View.VISIBLE
        }
        initStoryUi()

        //showing first story
        showFirstStory()
        setTouchListeners()
        story?.let {
            destroyStory(it)
        }
    }

    private fun setUpOwnerProfilePhoto() {
        if (story?.ownerProfileUrl == "") {
            if (story?.ownerGender == "male") {
                profilePic.setActualImageResource(R.drawable.user_male_placeholder)
            } else {
                profilePic.setActualImageResource(R.drawable.user_female_placeholder)
            }
        } else {
            profilePic.setImageURI(story?.ownerProfileUrl)
        }
    }

    private fun showFirstStory() {
        val storyItem = story?.storyItemList?.get(count)
        dateTimeTv.text = storyItem?.date + " " + storyItem?.time
        fullNameTv.text = story?.ownerName
        setUpOwnerProfilePhoto()
        storyItem?.let { storyItemNN ->
            storyPhoto.controller = story?.byUid?.let { storyUid -> getController(storyItemNN, storyUid, storyItemNN.timeStamp) }
            getStoryItemCount(storyItemNN)
            setUpStoryReplyListener(storyItemNN)
        }

    }

    private fun setUpStoryReplyListener(storyItem: StoryItem) {
        reply_layout.setOnClickListener {
            val replySheet = StoryReplyBottomFragment(messagingViewModel, story?.byUid!!, this)
            val bundle = Bundle()
            bundle.putSerializable(STORY_ITEM_INTENT_KEY, storyItem)
            replySheet.arguments = bundle
            replySheet.show(supportFragmentManager, StoryReplyBottomFragment::class.java.simpleName)
        }
    }

    private fun initStoryUi() {
        val storyList = story?.storyItemList
        storiesProgressView.setStoriesCount(storyList!!.size) // <- set stories
        storiesProgressView.setStoryDuration(3000L) // <- set a story duration
        storiesProgressView.setStoriesListener(this) // <- set listener
        storiesProgressView.startStories() // <- start progress
        //setting views details
        dateTimeTv.text = storyList.get(count).date + " " + storyList?.get(count).time
    }

    private fun destroyStory(story: Story) {
        mViewModel?.destroyStory(story)
    }

    private fun getStoryItemCount(storyItem: StoryItem) {
        story?.let {
            mViewModel?.getStorySeenCount(it, storyItem)?.observe(this, Observer { count ->
                if (count == "0") {
                    seenEyeLayout.setOnClickListener {
                        //DO NOTHING!!!
                    }
                } else {
                    seenEyeLayout.setOnClickListener {
                        val bundle = Bundle()
                        bundle.putSerializable(STORY_ITEM_INTENT_KEY, storyItem)
                        sheet?.arguments = bundle
                        sheet?.show(supportFragmentManager, SeenListBottomFragment::class.java.simpleName)
                    }
                }
                seenCountTv.text = count
            })
        }
    }

    private fun getController(storyItem: StoryItem, storyByUid: String, timeStamp: Long): DraweeController {
        return Fresco.newDraweeControllerBuilder()
                .setControllerListener(StoryImgControllerListener(storyByUid, timeStamp))
                .setUri(storyItem.imageUrl)
                .setAutoPlayAnimations(true)
                .build()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListeners() {
        val onTouchListener = OnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_DOWN) {
                //button pressed
                pressTime = System.currentTimeMillis()
                storiesProgressView.pause()
                return@OnTouchListener false
            } else if (motionEvent.action == MotionEvent.ACTION_UP) {
                val now = System.currentTimeMillis()
                //button released
                storiesProgressView.resume()
                return@OnTouchListener limit < now - pressTime
            }
            false
        }
        leftClickLayout.setOnTouchListener(onTouchListener)
        rightClickLayout.setOnTouchListener(onTouchListener)
    }

    override fun onNext() {
        val storyItem = story?.storyItemList?.get(++count)
        setUpOwnerProfilePhoto()
        storyItem?.let { storyItemNonNull ->
            story?.byUid?.let {
                getStoryItemCount(storyItemNonNull)
                storyPhoto.controller = getController(storyItem, it, storyItem.timeStamp)
                dateTimeTv.text = storyItemNonNull.date + " " + storyItemNonNull.time
                Log.d(TAG, "onNext: starting sheet opening listeners")
            }
            setUpStoryReplyListener(storyItemNonNull)
        }

    }

    override fun onPrev() {
        if (count - 1 >= 0) {
            val storyItem = story?.storyItemList?.get(--count)
            setUpOwnerProfilePhoto()
            getStoryItemCount(storyItem!!)
            storyPhoto.controller = story?.byUid?.let { getController(storyItem, it, storyItem.timeStamp) }
            //fullNameTv.setText(MySharedPrefs.getCurrentOfflineUser().getName());
            dateTimeTv.text = storyItem.date + " " + storyItem.time

            setUpStoryReplyListener(storyItem)
        }

    }

    override fun onComplete() {
        finish()
    }

    @OnClick(R.id.left_click_layout)
    fun onLeftClickLayoutClicked() {
        storiesProgressView.reverse()
    }

    @OnClick(R.id.right_click_layout)
    fun onRightClickLayoutClicked() {
        storiesProgressView.skip()
    }

    override fun onBottomFragmentSheetDismiss() {
        storiesProgressView.resume()
    }

    override fun onBottomFragmentSheetShown() {
        storiesProgressView.pause()
    }

    override fun onStoryReplySheetDismiss() {
        storiesProgressView.resume()

    }

    override fun onStoryReplySheetShown() {
        storiesProgressView.pause()
    }

    companion object {
        private const val TAG = "StoryViewerActivity"
        const val STORY_ITEM_INTENT_KEY = "storyItem"
    }
}