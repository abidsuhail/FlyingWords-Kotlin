package com.dragontelnet.mychatapp.ui.fragments.story.view

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.datasource.local.MySharedPrefs
import com.dragontelnet.mychatapp.model.entity.Story
import com.dragontelnet.mychatapp.model.entity.StoryItem
import com.dragontelnet.mychatapp.ui.activities.showallmystories.ShowMyAllStoryActivity
import com.dragontelnet.mychatapp.ui.activities.storyviewer.StoryViewerActivity
import com.dragontelnet.mychatapp.ui.fragments.story.adapter.AllStoriesListAdapter
import com.dragontelnet.mychatapp.ui.fragments.story.viewmodel.StoryViewModel
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirebaseStorageRefs.storiesStorageRef
import com.facebook.drawee.view.SimpleDraweeView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.theartofdev.edmodo.cropper.CropImage

class StoryFragment : Fragment() {
    @BindView(R.id.add_story_constraintLayout)
    lateinit var addStoryConstraintLayout: ConstraintLayout

    @BindView(R.id.stories_rv)
    lateinit var storiesRv: RecyclerView

    @BindView(R.id.fab)
    lateinit var fab: FloatingActionButton

    @BindView(R.id.add_photo_sign)
    lateinit var addPhotoSign: SimpleDraweeView

    @BindView(R.id.add_story_tv)
    lateinit var addStoryTv: TextView

    @BindView(R.id.add_story_date_time_tv)
    lateinit var addStoryDateTimeTv: TextView

    @BindView(R.id.show_all_story_icon_iv)
    lateinit var showAllStoryIconIv: SimpleDraweeView

    @BindView(R.id.story_check_progress_bar)
    lateinit var storyCheckProgressBar: ProgressBar

    @BindView(R.id.no_story_error_tv)
    lateinit var noStoryErrorTv: TextView

    private var mViewModel: StoryViewModel? = null
    private var croppedImageUri: Uri? = null
    private var story: Story? = null
    private var adapter: AllStoriesListAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.fragment_story, container, false)
        ButterKnife.bind(this, view)
        fab.setColorFilter(Color.WHITE)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        mViewModel = ViewModelProvider(this).get(StoryViewModel::class.java)
        populateStoryList()
        checkStoryCount()
        checkMyStoryExistenceLive()
        //myFriendsListOfUids()
        populateStoryList()
        checkStoryCount()
        checkMyStoryExistenceLive()
    }


    private fun checkStoryCount() {
        Log.d(TAG, "checkStoryCount: in")
        mViewModel?.queryStoryListCount()?.observe(viewLifecycleOwner, Observer {
            if (it > 0) {
                noStoryErrorTv.visibility = View.GONE
            } else {
                noStoryErrorTv.visibility = View.VISIBLE
            }
            storyCheckProgressBar.visibility = View.GONE

        })
    }

    private fun populateStoryList() {
        //TimeUnit.MILLISECONDS.convert(1,TimeUnit.DAYS);
        adapter = AllStoriesListAdapter(emptyList(), this, mViewModel)
        storiesRv.adapter = adapter
        mViewModel?.getMyStoriesList()?.observe(viewLifecycleOwner, Observer { storyList ->
            adapter?.updateStoryList(storyList.sortedByDescending { it.timeStamp })
        })
    }

    private fun checkMyStoryExistenceLive() {
        mViewModel?.checkLiveStoryExistenceOfUid(getCurrentUser()?.uid)
                ?.observe(viewLifecycleOwner, Observer { story ->
                    this@StoryFragment.story = story?.let {
                        it.ownerName = MySharedPrefs.getCurrentOfflineUserFromBook?.name
                        it.ownerProfileUrl = MySharedPrefs.getCurrentOfflineUserFromBook?.profilePic
                        destroyStory(it)
                        setUpMyStoryViews(it)
                        it
                    } ?: run {
                        setUpMyStoryNotExists()
                        null
                    }
                })
    }

    private fun setUpMyStoryNotExists() {
        showAllStoryIconIv.visibility = View.GONE
        addStoryDateTimeTv.visibility = View.GONE
        addPhotoSign.setImageResource(R.drawable.ic_add_story_icon)
        addStoryTv.text = "Click here to add story"
        addStoryConstraintLayout.setOnClickListener { cropImage() }
    }

    private fun setUpMyStoryViews(story: Story) {
        addStoryDateTimeTv.visibility = View.VISIBLE
        showAllStoryIconIv.visibility = View.VISIBLE
        val lastStory: StoryItem? = story.storyItemList?.lastOrNull()
        addStoryTv.text = "My Status"

        //add loading placeholder in imgview(drawer view)
        addPhotoSign.setImageURI(lastStory?.imageUrl)
        addStoryDateTimeTv.text = lastStory?.date + " " + lastStory?.time
        addStoryConstraintLayout.setOnClickListener {
            val intent = Intent(activity, StoryViewerActivity::class.java)
            intent.putExtra("story", story)
            startActivity(intent)
        }
    }

    private fun destroyStory(story: Story) {
        mViewModel?.destroyStory(story)
    }

    private fun cropImage() {
        (context)?.let {
            CropImage.activity()
                    .setAspectRatio(9, 16)
                    .start(it, this)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result: CropImage.ActivityResult? = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK && result != null) {
                croppedImageUri = result.uri
                uploadPhoto()
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                croppedImageUri = null
                val error: Exception? = result?.error
                Toast.makeText(activity, error?.message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadPhoto() {
        croppedImageUri?.let { uri ->
            context?.let { context ->
                Toast.makeText(activity, "Uploading story...", Toast.LENGTH_SHORT).show()
                addPhotoSign.setImageURI(croppedImageUri)

                mViewModel?.getImageUrl(uri, context, storiesStorageRef)
                        ?.observe(viewLifecycleOwner, Observer { imageUrl ->
                            addDataToDb(imageUrl)
                        })
            }
        } ?: Toast.makeText(activity, "image uri is null", Toast.LENGTH_SHORT).show()

    }

    private fun addDataToDb(imageUrl: String) {
        mViewModel?.addDataToDb(imageUrl)?.observe(viewLifecycleOwner, Observer {
            if (it) {
                Toast.makeText(activity, "Uploading success", Toast.LENGTH_SHORT).show()
            }
        })
    }

    @OnClick(R.id.fab)
    fun onFabClicked() {
        cropImage()
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            mViewModel?.removeMyLiveStoryListener()
        } else {

            // if adapter is not null then it must listen
            //start listening story count after removed listener in hidden fragment
            checkStoryCount()
            activity?.title = resources.getString(R.string.title_story)
            checkMyStoryExistenceLive() //start listener
        }
    }

    @OnClick(R.id.show_all_story_icon_iv)
    fun onShowMyAllStoriesClicked() {
        story?.let {
            val intent = Intent(activity, ShowMyAllStoryActivity::class.java)
            intent.putExtra("story", it)
            startActivity(intent)
        }
    }

    companion object {
        private val TAG: String = "StoryFragment"
        var ONE_DAY_IN_MILLISECONDS: Int = 86400000 //24 hour
    }
}