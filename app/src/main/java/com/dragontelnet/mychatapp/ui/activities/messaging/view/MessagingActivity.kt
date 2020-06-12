package com.dragontelnet.mychatapp.ui.activities.messaging.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.datasource.local.MySharedPrefs
import com.dragontelnet.mychatapp.model.entity.Chat
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.ui.activities.messaging.adapter.MessagesAdapter
import com.dragontelnet.mychatapp.ui.activities.messaging.viewmodel.MessagingViewModel
import com.dragontelnet.mychatapp.ui.activities.profile.view.ProfileActivity
import com.dragontelnet.mychatapp.utils.MyConstants
import com.dragontelnet.mychatapp.utils.auth.CurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.toolbar.*

class MessagingActivity : AppCompatActivity() {

    @BindView(R.id.private_msg_content_et)
    lateinit var privateMsgContentEt: EditText

    @BindView(R.id.my_toolbar)
    lateinit var toolbar: androidx.appcompat.widget.Toolbar

    @BindView(R.id.private_messages_rv)
    lateinit var privateMessagesRv: RecyclerView

    @BindView(R.id.messages_checker_tv)
    lateinit var messagesEmptyCheckerTv: TextView

    private var mViewModel: MessagingViewModel? = null
    private var chatMsg: String? = null
    private var adapter: MessagesAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messaging)
        ButterKnife.bind(this)
        mViewModel = ViewModelProvider(this).get(MessagingViewModel::class.java)
        initUi()
        addEtTextChangedListener()

        getChatUser()?.uid?.let {
            initToolbarUserDetails(it)
        }
        populateMessages()
    }

    private fun initUi() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        privateMessagesRv.recycledViewPool.setMaxRecycledViews(0, 0)

        toolbar.setOnClickListener {
            val i = Intent(this@MessagingActivity, ProfileActivity::class.java)
            i.putExtra("user", getChatUser())
            startActivity(i)
        }
    }

    private fun addEtTextChangedListener() {
        privateMsgContentEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                //text is changing...
                mViewModel?.setTypingStatus(getChatUser()!!)
            }

            override fun afterTextChanged(s: Editable) { //text changed.
                mViewModel?.stopTypingStatus(getChatUser()!!)
            }
        })
    }

    private fun scrollToLastMsgPosOnKeyboardUp() {
        privateMessagesRv.addOnLayoutChangeListener { _: View?, _: Int, _: Int, _: Int, bottom: Int, _: Int, _: Int, _: Int, oldBottom: Int ->
            if (bottom < oldBottom) {
                privateMessagesRv.adapter?.let {
                    privateMessagesRv
                            .smoothScrollToPosition(it.itemCount) //total msgs count
                }
            }
        }
    }

    private fun setScrollingToLastChat() {
        scrollToLastMsgPosOnKeyboardUp()
        //make it efficient later
        getChatUser()?.uid?.let {
            mViewModel?.notifyOnSentLastMsg(it, this)
                    ?.observe(this,
                            Observer { msgCount: Int ->
                                //msgCount keeps on notifying/updating when i send or receive msgs
                                privateMessagesRv.adapter?.let {
                                    privateMessagesRv.smoothScrollToPosition(msgCount)
                                }
                            })
        }
    }

    private fun populateMessages() {
        val query: Query = MyFirestoreDbRefs.getChatsListCollectionRef(CurrentUser.getCurrentUser()?.uid, getChatUser()?.uid)
        val options = FirestoreRecyclerOptions.Builder<Chat>()
                .setQuery(query.orderBy(MyConstants.FirestoreKeys.TIMESTAMP), Chat::class.java)
                .build()
        adapter = MessagesAdapter(options)
        privateMessagesRv.adapter = adapter

        checkMessagesEmptiness(query)
        setScrollingToLastChat()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkMessagesEmptiness(query: Query) {
        mViewModel?.checkMessagesEmptiness(query)?.observe(this, Observer { isEmpty ->
            if (isEmpty) {
                //no messages
                messagesEmptyCheckerTv.visibility = View.VISIBLE
            } else {
                messagesEmptyCheckerTv.visibility = View.GONE
            }

        })
    }

    private fun getChatUser(): User? {
        return if (intent.hasExtra("user")) {
            intent.getSerializableExtra("user") as User
        } else null
    }

    private fun initToolbarUserDetails(receiverUid: String) {
        mViewModel?.getLiveChatUserDetails(receiverUid, this)?.observe(this,
                Observer { user: User ->
                    toolbar_full_name.text = user.name

                    when (user.status) {
                        MyConstants.FirestoreKeys.ONLINE -> {
                            toolbar_status.text = MyConstants.FirestoreKeys.ONLINE
                        }
                        else -> {
                            mViewModel?.getLastChatDocRef(CurrentUser.getCurrentUser()?.uid!!, receiverUid)?.observe(this,
                                    Observer {
                                        if (it == "typing") {
                                            toolbar_status.text = MyConstants.FirestoreKeys.TYPING_FIELD
                                        } else {
                                            toolbar_status.text = user.date + " " + user.time

                                        }
                                    })

                        }
                    }
                    if (user.profilePic != "") {
                        toolbar_profile_pic.setImageURI(user.profilePic)
                    } else {
                        if ((user.gender == "male")) {
                            toolbar_profile_pic.setImageResource(R.drawable.user_male_placeholder)
                        } else {
                            toolbar_profile_pic.setImageResource(R.drawable.user_female_placeholder)
                        }
                    }
                })
        my_toolbar.setOnClickListener {
            val i = Intent(this, ProfileActivity::class.java)
            i.putExtra("user", getChatUser())
            startActivity(i)
        }
    }

    @OnClick(R.id.private_send_msg_btn)
    fun onSendBtnClicked() {
        //here observer is this
        chatMsg = privateMsgContentEt.text.toString()
        getChatUser()?.uid?.let { uid ->
            chatMsg?.trim()?.let { chatMsg ->
                if (chatMsg.isNotBlank()) {
                    privateMsgContentEt.setText("")
                    mViewModel?.sendMessage(chatMsg, uid, this)
                            ?.observe(this, Observer { isChatSent ->
                                if (isChatSent) {
                                    //now send notification
                                    getChatUser()?.deviceToken?.let {
                                        mViewModel?.sendNotification(getChatUser(), chatMsg, this)
                                    }
                                }
                            })
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        adapter?.stopListening()
    }

    override fun onStart() {
        super.onStart()
        adapter?.startListening()
    }

    override fun onResume() {
        super.onResume()
        //start listening
        getChatUser()?.uid?.let {
            mViewModel?.addSeenListener(it, this)
        }
        //don't receive notification when user is chatting
        MySharedPrefs.putDoReceiverNotification((this), false)
    }

    override fun onPause() {
        super.onPause()
        //stop listening
        mViewModel?.removeSeenListener()
        //receiver notification when user is not chatting
        MySharedPrefs.putDoReceiverNotification((this), true)
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter?.let {
            it.stopListening()
            adapter = null
        }
        mViewModel?.removeSeenListener()
    }

    companion object {
        private val TAG = "MessagingFragment"
    }
}
