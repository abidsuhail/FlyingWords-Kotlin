package com.dragontelnet.mychatapp.ui.activities.messaging.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Chat
import com.dragontelnet.mychatapp.ui.activities.messaging.adapter.viewholder.DifferentUserChatVH
import com.dragontelnet.mychatapp.ui.activities.messaging.adapter.viewholder.SameUserChatVH
import com.dragontelnet.mychatapp.utils.auth.CurrentUser.getCurrentUser
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions

class MessagesAdapter(options: FirestoreRecyclerOptions<Chat?>) : FirestoreRecyclerAdapter<Chat, RecyclerView.ViewHolder>(options) {
    private val SAME_USER = 1
    private val DIFFERENT_USER = 2
    private var mRecyclerView: RecyclerView? = null
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int, chat: Chat) {
        when (holder) {
            is SameUserChatVH -> holder.bindSenderChatViews(holder, chat, position, itemCount, mRecyclerView)
            is DifferentUserChatVH -> holder.bindReceiverChatViews(holder, chat)
            else -> throw ClassCastException("Unknown Holder type!!")
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        mRecyclerView = recyclerView
        super.onAttachedToRecyclerView(recyclerView)
    }

    override fun getItemViewType(position: Int): Int {
        val byUid = getItem(position).byUid
        //comparing chats owner uids with my uid
        return if (byUid == getCurrentUser()?.uid) SAME_USER else DIFFERENT_USER
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        return when (viewType) {
            SAME_USER -> {
                view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.same_user_chat_layout, parent, false)
                SameUserChatVH(view)
            }
            DIFFERENT_USER -> {
                view = LayoutInflater.from(parent.context)
                        .inflate(R.layout.different_user_chat_layout, parent, false)
                DifferentUserChatVH(view)
            }
            else -> throw ClassCastException("Unknown Holder type...")
        }
    }
}