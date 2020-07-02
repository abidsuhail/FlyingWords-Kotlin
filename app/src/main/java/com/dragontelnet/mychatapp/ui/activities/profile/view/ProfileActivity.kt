package com.dragontelnet.mychatapp.ui.activities.profile.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.paging.PagedList
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.datasource.local.MySharedPrefs
import com.dragontelnet.mychatapp.model.entity.Post
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.ui.activities.friends.view.FriendsActivity
import com.dragontelnet.mychatapp.ui.activities.profile.adapter.ProfileThumbnailPostListAdapter
import com.dragontelnet.mychatapp.ui.activities.profile.viewmodel.ProfileActivityViewModel
import com.dragontelnet.mychatapp.ui.activities.registration.RegistrationDetailsActivity
import com.dragontelnet.mychatapp.utils.MyConstants.ProfileFriendRequestCodes
import com.dragontelnet.mychatapp.utils.UserProfileDetailsSetter
import com.dragontelnet.mychatapp.utils.auth.CurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.facebook.drawee.view.SimpleDraweeView
import com.firebase.ui.firestore.paging.FirestorePagingOptions
import kotlinx.android.synthetic.main.content_profile.*

class ProfileActivity : AppCompatActivity() {


    @BindView(R.id.user_profile_pic)
    lateinit var profilePic: SimpleDraweeView

    @BindView(R.id.user_fullname)
    lateinit var fullNameTv: TextView

    @BindView(R.id.user_username)
    lateinit var userNameTv: TextView

    @BindView(R.id.user_gender)
    lateinit var userGender: TextView

    @BindView(R.id.edit_profile_btn)
    lateinit var editProfileBtn: Button

    @BindView(R.id.accept_request_btn)
    lateinit var acceptRequestBtn: Button

    @BindView(R.id.send_request_btn)
    lateinit var sendRequestBtn: Button

    @BindView(R.id.cancel_sent_request)
    lateinit var cancelSentRequestBtn: Button

    @BindView(R.id.decline_req_btn)
    lateinit var declineReqBtn: Button

    @BindView(R.id.unfriend_btn)
    lateinit var unfriendBtn: Button

    @BindView(R.id.my_posts_rv)
    lateinit var myPostsRv: RecyclerView

    @BindView(R.id.my_feeds_swipe_refresh)
    lateinit var myFeedsSwipeRefresh: SwipeRefreshLayout

    @BindView(R.id.profile_progressbar)
    lateinit var profileProgressbar: ProgressBar

    @BindView(R.id.toolbar)
    lateinit var toolbar: Toolbar

    @BindView(R.id.no_profile_posts_tv)
    lateinit var noProfilePostsTv: TextView

    @BindView(R.id.posts_hidden_tv)
    lateinit var postsHiddenTv: TextView

    private var mViewModel: ProfileActivityViewModel? = null
    private var adapter: ProfileThumbnailPostListAdapter? = null
    private var buttonList: HashSet<Button>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        ButterKnife.bind(this)
        postsHiddenTv.text = "Posts Hidden!!\nAdd ${if (profileUser()?.gender == "male") "him" else "her"} as Friend to see posts"
        buildButtonsList()
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mViewModel = ViewModelProvider(this).get(ProfileActivityViewModel::class.java)

        myFeedsSwipeRefresh.setOnRefreshListener {
            updateOnlineProfileUserDetails()
        }

        setUpProfileDetails(profileUser())

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123 && resultCode == Activity.RESULT_OK) {
            updateOnlineProfileUserDetails()
        }
    }

    private fun updateOnlineProfileUserDetails() {
        profileUser()?.uid?.let { userUid ->
            mViewModel?.getUser(userUid)?.observe(this, Observer { user ->
                setUpProfileDetails(user)
            })
        }
    }

    private fun setUpProfileDetails(profileUser: User?) {
        profileUser?.let {
            setUpUi(it)
            setFriendsCount(it)
        } ?: run {
            profileProgressbar.visibility = View.GONE
            myFeedsSwipeRefresh.isRefreshing = false
            Toast.makeText(this, "user is null", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setFriendsCount(profileUser: User) {
        mViewModel?.getFriendsCount(profileUser.uid!!)?.observe(this, Observer { count ->
            see_all_friends_btn.text = "Friends : $count"
        })
    }

    private fun buildButtonsList() {
        buttonList = hashSetOf(acceptRequestBtn, declineReqBtn, sendRequestBtn, cancelSentRequestBtn, unfriendBtn, editProfileBtn)
    }

    private fun setUpUi(profileUser: User) {
        title = if (profileUser.uid == CurrentUser.getCurrentUser()?.uid) {
            resources.getString(R.string.account) //my account
        } else {
            profileUser.name //setting user's full name in toolbar
        }
        initUserDetails(profileUser) //setting user profile details
    }

    private fun profileUser(): User? {
        return if (intent.hasExtra("user")) {
            intent.getSerializableExtra("user") as User
        } else {
            MySharedPrefs.getCurrentOfflineUserFromBook
        }
    }

    private fun initUserDetails(user: User) {
        //here user null check is previously checked
        UserProfileDetailsSetter.setAllUserDetails(user = user,
                sdv = profilePic,
                userNameTv = userNameTv,
                nameTv = fullNameTv,
                genderTv = userGender,
                cityTv = user_city,
                bioTv = user_bio)
        listenProfileButtonsVisibility(user)
    }

    private fun listenProfileButtonsVisibility(user: User) {
        mViewModel?.initProfileButtonsVisibility(user.uid!!)
                ?.observe(this, Observer { friendReqCode ->
                    when (friendReqCode) {
                        ProfileFriendRequestCodes.SENT -> setSentReqBtnsVisibility()
                        ProfileFriendRequestCodes.RECEIVED -> setReceivedReqBtnsVisibility()
                        ProfileFriendRequestCodes.NO_REQUEST_EXISTS -> setNoReqExistsBtnsVisibility()
                        ProfileFriendRequestCodes.SAME_USER, ProfileFriendRequestCodes.UNKNOWN_REQUEST -> setSameUserBtnsVisibility(friendReqCode)
                        ProfileFriendRequestCodes.FRIENDS -> setAreFriendsBtnsVisibility()
                        else -> Toast.makeText(this@ProfileActivity, "Unknown error", Toast.LENGTH_SHORT).show()
                    }
                    profileProgressbar.visibility = View.GONE
                    myFeedsSwipeRefresh.isRefreshing = false
                })
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }


    @OnClick(R.id.send_request_btn)
    fun onSendReqBtnClicked() {
        //sending request on click
        profileUser()
                ?: Toast.makeText(this@ProfileActivity, "user is null", Toast.LENGTH_SHORT).show()
        sendRequestBtn.isEnabled = false
        profileProgressbar.visibility = View.VISIBLE
        //sending req to profile user
        val receiverUid = profileUser()?.uid
        mViewModel?.sendFriendRequest(receiverUid!!)?.observe(this, Observer { isSent ->
            if (isSent) {
                setSentReqBtnsVisibility()
            } else {
                Toast.makeText(this@ProfileActivity, "req not sent", Toast.LENGTH_SHORT).show()
            }
            sendRequestBtn.isEnabled = true
        })

        profileProgressbar.visibility = View.GONE
    }

    @OnClick(R.id.accept_request_btn)
    fun onAcceptRequestBtnClicked() {
        acceptRequestBtn.isEnabled = false
        profileProgressbar.visibility = View.VISIBLE

        profileUser()?.let {
            it.uid?.let { uid ->
                mViewModel?.acceptRequest(uid, this)
                        ?.observe(this, Observer { isSuccess ->
                            if (isSuccess) {
                                setAreFriendsBtnsVisibility()
                            }
                            profileProgressbar.visibility = View.GONE
                            acceptRequestBtn.isEnabled = true
                        })
            }
        }
    }

    @OnClick(R.id.cancel_sent_request)
    fun onCancelSentRequestClicked() {
        //onCancelSentRequestClicked and onDeclineReqBtnClicked functionality is same
        cancelSentRequestBtn.isEnabled = false
        onDeclineReqBtnClicked()
    }

    @OnClick(R.id.edit_profile_btn)
    fun onClickEditProfileBtn() {
        val intent = Intent(this, RegistrationDetailsActivity::class.java)
        intent.putExtra(RegistrationDetailsActivity.IS_EDITING_INTENT_FLAG, true)
        startActivity(intent)

    }

    @OnClick(R.id.decline_req_btn)
    fun onDeclineReqBtnClicked() {
        declineReqBtn.isEnabled = false
        profileProgressbar.visibility = View.VISIBLE
        profileUser()?.uid?.let {
            mViewModel?.declineRequest(it)
                    ?.observe(this, Observer { isReqDeclineSuccess: Boolean ->
                        if (isReqDeclineSuccess) {
                            //request declined
                            setNoReqExistsBtnsVisibility()
                        }
                        profileProgressbar.visibility = View.GONE
                        declineReqBtn.isEnabled = true
                        cancelSentRequestBtn.isEnabled = true
                    })
        }
    }

    @OnClick(R.id.unfriend_btn)
    fun onUnfriendBtnClicked() {
        //set alert dialogue box
        unfriendBtn.isEnabled = false
        profileProgressbar.visibility = View.VISIBLE

        AlertDialog.Builder(this)
                .setTitle("Unfriend ${profileUser()?.name}")
                .setMessage("Are you sure want to unfriend ${profileUser()?.name} ?")
                .setPositiveButton("Unfriend") { dialog, which ->
                    //unFriend confirm
                    continueUnfriendProcess()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    //cancel unfriend
                    dialog.cancel()
                    unfriendBtn.isEnabled = true
                    profileProgressbar.visibility = View.GONE
                }.create().show()

    }

    private fun continueUnfriendProcess() {
        profileUser()?.uid?.let {
            mViewModel?.unFriendRequest(it, this)
                    ?.observe(this, Observer { isUnfriendSuccess: Boolean ->
                        if (isUnfriendSuccess) {
                            setNoReqExistsBtnsVisibility()
                        }
                        profileProgressbar.visibility = View.GONE
                        unfriendBtn.isEnabled = true
                    })
        }
    }

    private val config: PagedList.Config
        get() = PagedList.Config.Builder()
                .setEnablePlaceholders(true)
                .setPrefetchDistance(10)
                .setPageSize(20)
                .build()

    private val defaultPagingOptions: FirestorePagingOptions<Post?>?
        get() {
            val query = profileUser()?.uid?.let { MyFirestoreDbRefs.getOrganizedPostsCollRefOfUid(it) }
            return query?.let {
                FirestorePagingOptions.Builder<Post>()
                        .setLifecycleOwner(this)
                        .setQuery(it, config, Post::class.java)
                        .build()
            }

        }

    private fun populateMyPosts() {
        adapter = defaultPagingOptions?.let {
            ProfileThumbnailPostListAdapter(it,
                    this,
                    myFeedsSwipeRefresh)
        }
        myPostsRv.adapter = adapter
        adapter?.startListening()

        //getProfileUserPostAvailability()
    }

    private fun getProfileUserPostAvailability() {
        profileUser()?.let { nonNullProfileUser ->
            mViewModel?.isPostsAvailable(nonNullProfileUser)?.observe(this, Observer { isEmpty ->
                if (isEmpty) {
                    //show error
                    noProfilePostsTv.visibility = View.VISIBLE
                } else {
                    noProfilePostsTv.visibility = View.GONE
                }
                postsHiddenTv.visibility = View.GONE
            })
        }
    }

    private fun showSuitableBtns(vararg showingBtns: Button) {

        buttonList?.forEach {
            it.visibility = View.GONE
        }
        showingBtns.forEach {
            it.visibility = View.VISIBLE
        }
    }

    private fun setAreFriendsBtnsVisibility() {
        showSuitableBtns(unfriendBtn)
        showRecyclerViewUserPosts()
        getProfileUserPostAvailability()

        see_all_friends_btn.isEnabled = true

        see_all_friends_btn.setOnClickListener {
            val i = Intent(this, FriendsActivity::class.java)
            i.putExtra(FriendsActivity.FRIEND_UID_KEY, profileUser())
            startActivity(i)
        }
    }

    private fun setSameUserBtnsVisibility(friendReqCode: Int) {
        showSuitableBtns(editProfileBtn)
        showRecyclerViewUserPosts()
        getProfileUserPostAvailability()
        if (friendReqCode == ProfileFriendRequestCodes.SAME_USER) {
            see_all_friends_btn.isEnabled = true
            see_all_friends_btn.setOnClickListener {
                val i = Intent(this, FriendsActivity::class.java)
                startActivity(i)
            }
        } else {
            see_all_friends_btn.setOnClickListener(null)
            see_all_friends_btn.isEnabled = false
        }
    }

    private fun setNoReqExistsBtnsVisibility() {
        showSuitableBtns(sendRequestBtn)
        hideRecyclerViewUserPosts()

        //not friends
        noProfilePostsTv.visibility = View.GONE
        postsHiddenTv.visibility = View.VISIBLE
        see_all_friends_btn.setOnClickListener(null)
        see_all_friends_btn.isEnabled = false
    }

    private fun setReceivedReqBtnsVisibility() {
        showSuitableBtns(acceptRequestBtn, declineReqBtn)
        hideRecyclerViewUserPosts()

        //not friends
        noProfilePostsTv.visibility = View.GONE
        postsHiddenTv.visibility = View.VISIBLE
        see_all_friends_btn.setOnClickListener(null)
        see_all_friends_btn.isEnabled = false
    }

    private fun setSentReqBtnsVisibility() {
        showSuitableBtns(cancelSentRequestBtn)
        hideRecyclerViewUserPosts()

        //not friends
        noProfilePostsTv.visibility = View.GONE
        postsHiddenTv.visibility = View.VISIBLE
        see_all_friends_btn.setOnClickListener(null)
        see_all_friends_btn.isEnabled = false
    }

    private fun hideRecyclerViewUserPosts() {

        adapter?.stopListening().let {
            adapter = null
            myPostsRv.visibility = View.GONE
            myFeedsSwipeRefresh.isRefreshing = false
        }
    }

    private fun showRecyclerViewUserPosts() {
        myPostsRv.visibility = View.VISIBLE
        //populating posts
        populateMyPosts()
        adapter?.refresh() ?: run { myFeedsSwipeRefresh.isRefreshing = false }
    }

    companion object {
        private const val TAG = "ProfileFragment"
    }
}