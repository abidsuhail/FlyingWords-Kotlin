package com.dragontelnet.mychatapp.ui.activities.messaging.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Chat
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.ui.activities.messaging.adapter.viewholder.DifferentUserChatStoryReplyVH
import com.dragontelnet.mychatapp.ui.activities.messaging.adapter.viewholder.DifferentUserChatVH
import com.dragontelnet.mychatapp.ui.activities.messaging.adapter.viewholder.SameUserChatStoryReplyVH
import com.dragontelnet.mychatapp.ui.activities.messaging.adapter.viewholder.SameUserChatVH
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class MessagesAdapter(options: FirestoreRecyclerOptions<Chat?>, private val receiverUser: User?) : FirestoreRecyclerAdapter<Chat, RecyclerView.ViewHolder>(options) {
    private val SAME_USER_SIMPLE_CHAT = 1
    private val DIFFERENT_USER_SIMPLE_CHAT = 2
    private val SAME_USER_STORY_REPLY = 3
    private val DIFFERENT_USER_STORY_REPLY = 4

    private var mRecyclerView: RecyclerView? = null
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, chat: Chat) {
        when (holder) {
            is SameUserChatVH -> holder.bindSenderChatViews(holder, chat, position, itemCount, mRecyclerView)
            is DifferentUserChatVH -> holder.bindReceiverChatViews(holder, chat, receiverUser)
            is SameUserChatStoryReplyVH -> holder.bindSameUserStoryReplyViews(holder, chat, position, itemCount, mRecyclerView)
            is DifferentUserChatStoryReplyVH -> holder.bindDifferentUserStoryReplyViews(holder, chat, receiverUser)
            else -> throw ClassCastException("Unknown Holder type!!")
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mRecyclerView = recyclerView
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun getItemViewType(position: Int): Int {
        val msg = getItem(position)

        //comparing chats owner uids with my uid
        //return if (msg.byUid == getCurrentUser()?.uid) SAME_USER_SIMPLE_CHAT else DIFFERENT_USER_SIMPLE_CHAT

        return if (msg.byUid == getCurrentUser()?.uid) {
            //same user chat
            if (msg.storyPhotoLink != null && msg.storyPhotoLink != "") {
                //replying to story
                SAME_USER_STORY_REPLY
            } else {
                //simple reply
                SAME_USER_SIMPLE_CHAT
            }
        } else {
            //other user
            //same user chat
            if (msg.storyPhotoLink != null && msg.storyPhotoLink != "") {
                //replying to story
                DIFFERENT_USER_STORY_REPLY
            } else {
                //simple reply
                DIFFERENT_USER_SIMPLE_CHAT
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (viewType) {
            SAME_USER_SIMPLE_CHAT -> {
                view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.same_user_chat_layout, parent, false)
                SameUserChatVH(view)
            }
            DIFFERENT_USER_SIMPLE_CHAT -> {
                view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.different_user_chat_layout, parent, false)
                DifferentUserChatVH(view)
            }
            SAME_USER_STORY_REPLY -> {
                view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.same_user_chat_story_reply, parent, false)
                SameUserChatStoryReplyVH(view)
            }
            DIFFERENT_USER_STORY_REPLY -> {
                view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.different_user_chat_story_reply, parent, false)
                DifferentUserChatStoryReplyVH(view)
            }
            else -> throw ClassCastException("Unknown Holder type...")
        }
    }
}