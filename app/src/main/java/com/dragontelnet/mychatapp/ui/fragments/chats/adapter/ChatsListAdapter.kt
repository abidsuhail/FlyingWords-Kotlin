package com.dragontelnet.mychatapp.ui.fragments.chats.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.dragontelnet.mychatapp.R
import com.dragontelnet.mychatapp.model.entity.Chat
import com.dragontelnet.mychatapp.model.entity.User
import com.dragontelnet.mychatapp.ui.activities.messaging.view.MessagingActivity
import com.dragontelnet.mychatapp.ui.fragments.chats.adapter.viewholder.LastChatVH
import com.dragontelnet.mychatapp.ui.fragments.chats.view.ChatsFragment
import com.dragontelnet.mychatapp.utils.auth.CurrentUser
import com.dragontelnet.mychatapp.utils.firestore.MyFirestoreDbRefs

class ChatsListAdapter(private val olderChatsProgress: ProgressBar,
                       private val chatsFragment: ChatsFragment,
                       private var mList: MutableList<Chat>) : RecyclerView.Adapter<LastChatVH>() {


    fun updateChatList(list: MutableList<Chat>) {
        mList = list
        notifyDataSetChanged()
    }

    override fun getItemCount() = mList.size

    override fun onBindViewHolder(holder: LastChatVH, position: Int) {
        if (olderChatsProgress.visibility == View.VISIBLE) {
            olderChatsProgress.visibility = View.GONE
        }
        val lastChat = mList[position]

        holder.bindChatUserDetails(holder, lastChat.receiverUidUser)
        holder.bindLastChatViewsDetails(holder, lastChat, chatsFragment.resources.getColor(R.color.secondary_text), chatsFragment.resources.getColor(R.color.colorPrimary))
        setItemClickListener(holder, lastChat.receiverUidUser, position)
    }

    private fun setItemClickListener(holder: LastChatVH, receiverUser: User?, position: Int) {
        holder.itemView.setOnClickListener { v: View? ->
            chatsFragment.activity?.let {
                startMessagingActivity(receiverUser)
            }
        }

        holder.itemView.setOnLongClickListener {
            val newPos = holder.adapterPosition
            if (newPos != RecyclerView.NO_POSITION) {
                try {
                    val deletingChat = mList[newPos]
                    AlertDialog.Builder(chatsFragment.requireContext())
                            .setTitle("Delete Chat")
                            .setMessage("Confirm delete chats of ${deletingChat.receiverUidUser?.name} ?")
                            .setPositiveButton("DELETE") { _, _ ->
                                //confirm deleting process
                                deleteChat(deletingChat, newPos)
                            }
                            .setNegativeButton("CANCEL") { dialog, _ ->
                                //cancel
                                dialog.cancel()
                            }.create().show()
                } catch (e: ArrayIndexOutOfBoundsException) {
                }
            }
            true
        }
    }

    private fun deleteChat(deletingChat: Chat, position: Int) {
        MyFirestoreDbRefs.getOlderChatsRefOfUid(CurrentUser.getCurrentUser()?.uid)
                .document(deletingChat.receiverUid!!).delete().addOnSuccessListener {
                    //mList.remove(deletingChat)
                    //notifyItemRemoved(position)

                    //deleting all messages from cloud functions
                }

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LastChatVH {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.old_chat_layout, parent, false)
        return LastChatVH(view)
    }

    private fun startMessagingActivity(user: User?) {
        val i = Intent(chatsFragment.context, MessagingActivity::class.java)
                .apply { putExtra("user", user) }
        chatsFragment.startActivity(i)
    }


    companion object {
        private const val TAG = "ChatsAdapter"
    }

}